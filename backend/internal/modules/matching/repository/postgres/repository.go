package postgres

import (
	"context"
	"fmt"
	"sort"
	"strings"

	"pet-social-platform/backend/internal/modules/matching/domain"

	"github.com/jackc/pgx/v5/pgxpool"
)

type Repository struct {
	db *pgxpool.Pool
}

func New(db *pgxpool.Pool) *Repository {
	return &Repository{db: db}
}

func (r *Repository) IsOwnedByUser(ctx context.Context, petID string, ownerID string) (bool, error) {
	const query = `
		SELECT EXISTS (
			SELECT 1
			FROM pets
			WHERE id = $1 AND owner_id = $2
		)
	`

	var exists bool
	err := r.db.QueryRow(ctx, query, petID, ownerID).Scan(&exists)
	return exists, err
}

func (r *Repository) GetPetSnapshot(ctx context.Context, petID string) (*domain.Recommendation, error) {
	const query = `
		SELECT
			p.id,
			p.owner_id,
			p.name,
			p.species,
			p.breed,
			p.sex,
			p.birth_date,
			p.weight_kg::text,
			p.bio,
			p.photo_url,
			COALESCE(p.personality_tags, '{}'),
			COALESCE(p.interests, '{}'),
			p.health_notes,
			p.matching_goal,
			p.search_radius_meters,
			p.latitude::text,
			p.longitude::text,
			NULL::integer AS distance_meters,
			0 AS behavior_score,
			p.is_active,
			p.created_at,
			p.updated_at
		FROM pets p
		WHERE p.id = $1
	`

	var item domain.Recommendation
	if err := r.db.QueryRow(ctx, query, petID).Scan(recommendationDest(&item)...); err != nil {
		return nil, err
	}
	item.PersonalityTags = normalizeList(item.PersonalityTags)
	item.Interests = normalizeList(item.Interests)
	return &item, nil
}

func (r *Repository) GetRecommendations(ctx context.Context, sourcePetID string, ownerID string, filters domain.RecommendationFilters) ([]domain.Recommendation, error) {
	args := []any{sourcePetID, ownerID}
	where := []string{
		"p.owner_id <> $2",
		"p.id <> $1",
		"p.is_active = TRUE",
		"p.species = source_pet.species",
		`NOT EXISTS (
			SELECT 1
			FROM swipe_actions s
			WHERE s.source_pet_id = $1 AND s.target_pet_id = p.id
		)`,
	}

	if filters.Goal == "breeding" {
		where = append(where,
			"source_pet.matching_goal = 'breeding'",
			"p.matching_goal = 'breeding'",
			"(source_pet.sex IS NULL OR p.sex IS NULL OR lower(source_pet.sex) <> lower(p.sex))",
		)
	} else {
		where = append(where, "(p.matching_goal IS NULL OR p.matching_goal = '' OR p.matching_goal = 'walk')")
	}
	if filters.Species != nil {
		args = append(args, *filters.Species)
		where = append(where, fmt.Sprintf("lower(p.species) = lower($%d)", len(args)))
	}
	if filters.Breed != nil {
		args = append(args, *filters.Breed)
		where = append(where, fmt.Sprintf("lower(p.breed) = lower($%d)", len(args)))
	}
	if filters.Sex != nil {
		args = append(args, *filters.Sex)
		where = append(where, fmt.Sprintf("lower(p.sex) = lower($%d)", len(args)))
	}
	if filters.Tags != nil && len(filters.Tags) > 0 {
		args = append(args, filters.Tags)
		where = append(where, fmt.Sprintf("p.personality_tags && $%d", len(args)))
	}
	if filters.Interests != nil && len(filters.Interests) > 0 {
		args = append(args, filters.Interests)
		where = append(where, fmt.Sprintf("p.interests && $%d", len(args)))
	}
	if filters.HasPhoto != nil && *filters.HasPhoto {
		where = append(where, "NULLIF(trim(p.photo_url), '') IS NOT NULL")
	}
	if filters.AgeMinMonths != nil {
		args = append(args, *filters.AgeMinMonths)
		where = append(where, fmt.Sprintf("p.birth_date IS NOT NULL AND p.birth_date <= (CURRENT_DATE - ($%d::int * INTERVAL '1 month'))", len(args)))
	}
	if filters.AgeMaxMonths != nil {
		args = append(args, *filters.AgeMaxMonths)
		where = append(where, fmt.Sprintf("p.birth_date IS NOT NULL AND p.birth_date >= (CURRENT_DATE - ($%d::int * INTERVAL '1 month'))", len(args)))
	}

	distanceExpr := `
		CASE
			WHEN source_pet.latitude IS NULL
			  OR source_pet.longitude IS NULL
			  OR p.latitude IS NULL
			  OR p.longitude IS NULL
			THEN NULL
			ELSE ROUND(
				6371000 * 2 * ASIN(
					SQRT(
						POWER(SIN(RADIANS((p.latitude - source_pet.latitude) / 2)), 2)
						+ COS(RADIANS(source_pet.latitude))
						* COS(RADIANS(p.latitude))
						* POWER(SIN(RADIANS((p.longitude - source_pet.longitude) / 2)), 2)
					)
				)
			)::integer
		END
	`
	if filters.MaxDistanceMeters != nil {
		args = append(args, *filters.MaxDistanceMeters)
		where = append(where, fmt.Sprintf("(%s IS NULL OR %s <= $%d)", distanceExpr, distanceExpr, len(args)))
	}

	query := `
		WITH source_pet AS (
			SELECT species, sex, matching_goal, latitude, longitude
			FROM pets
			WHERE id = $1
		)
		SELECT
			p.id,
			p.owner_id,
			p.name,
			p.species,
			p.breed,
			p.sex,
			p.birth_date,
			p.weight_kg::text,
			p.bio,
			p.photo_url,
			COALESCE(p.personality_tags, '{}'),
			COALESCE(p.interests, '{}'),
			p.health_notes,
			p.matching_goal,
			p.search_radius_meters,
			p.latitude::text,
			p.longitude::text,
			` + distanceExpr + ` AS distance_meters,
			COALESCE((
				SELECT SUM(CASE WHEN s.action = 'like' THEN 5 ELSE -3 END)
				FROM swipe_actions s
				JOIN pets seen ON seen.id = s.target_pet_id
				WHERE s.source_pet_id = $1
				  AND (
					  lower(seen.breed) = lower(p.breed)
					  OR seen.personality_tags && p.personality_tags
					  OR seen.interests && p.interests
				  )
			), 0)
			+ COALESCE((
				SELECT COUNT(*)::int
				FROM matching_events e
				JOIN pets opened ON opened.id = e.target_pet_id
				WHERE e.source_pet_id = $1
				  AND e.event_type = 'profile_open'
				  AND (
					  lower(opened.breed) = lower(p.breed)
					  OR opened.personality_tags && p.personality_tags
					  OR opened.interests && p.interests
				  )
			), 0) AS behavior_score,
			p.is_active,
			p.created_at,
			p.updated_at
		FROM pets p
		CROSS JOIN source_pet
		WHERE ` + strings.Join(where, "\n\t\t  AND ") + `
		ORDER BY distance_meters NULLS LAST, behavior_score DESC, p.created_at ASC
		LIMIT 20
	`

	rows, err := r.db.Query(ctx, query, args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []domain.Recommendation

	for rows.Next() {
		var item domain.Recommendation
		if err := rows.Scan(recommendationDest(&item)...); err != nil {
			return nil, err
		}
		item.PersonalityTags = normalizeList(item.PersonalityTags)
		item.Interests = normalizeList(item.Interests)

		result = append(result, item)
	}

	return result, rows.Err()
}

func (r *Repository) SaveSwipe(ctx context.Context, sourcePetID string, targetPetID string, action string) error {
	const query = `
		INSERT INTO swipe_actions (source_pet_id, target_pet_id, action)
		VALUES ($1, $2, $3)
		ON CONFLICT (source_pet_id, target_pet_id)
		DO UPDATE SET
			action = EXCLUDED.action,
			created_at = NOW()
	`

	_, err := r.db.Exec(ctx, query, sourcePetID, targetPetID, action)
	return err
}

func (r *Repository) SaveEvent(ctx context.Context, event domain.MatchingEvent) error {
	const query = `
		INSERT INTO matching_events (source_pet_id, target_pet_id, event_type)
		VALUES ($1, $2, $3)
	`

	_, err := r.db.Exec(ctx, query, event.SourcePetID, event.TargetPetID, event.EventType)
	return err
}

func (r *Repository) HasReciprocalLike(ctx context.Context, sourcePetID string, targetPetID string) (bool, error) {
	const query = `
		SELECT EXISTS (
			SELECT 1
			FROM swipe_actions
			WHERE source_pet_id = $1
			  AND target_pet_id = $2
			  AND action = 'like'
		)
	`

	var exists bool
	err := r.db.QueryRow(ctx, query, targetPetID, sourcePetID).Scan(&exists)
	return exists, err
}

func (r *Repository) CreateMatchIfNotExists(ctx context.Context, pet1ID string, pet2ID string) error {
	ordered := []string{pet1ID, pet2ID}
	sort.Strings(ordered)

	const query = `
		INSERT INTO matches (pet1_id, pet2_id)
		VALUES ($1, $2)
		ON CONFLICT (pet1_id, pet2_id) DO NOTHING
	`

	_, err := r.db.Exec(ctx, query, ordered[0], ordered[1])
	return err
}

func (r *Repository) ListMatchesByPetID(ctx context.Context, petID string, ownerID string) ([]domain.Match, error) {
	const query = `
		SELECT m.id, m.pet1_id, m.pet2_id, m.created_at
		FROM matches m
		JOIN pets p ON (p.id = m.pet1_id OR p.id = m.pet2_id)
		WHERE (m.pet1_id = $1 OR m.pet2_id = $1)
		  AND p.owner_id = $2
		GROUP BY m.id
		ORDER BY m.created_at DESC
	`

	rows, err := r.db.Query(ctx, query, petID, ownerID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var matches []domain.Match

	for rows.Next() {
		var item domain.Match
		if err := rows.Scan(
			&item.ID,
			&item.Pet1ID,
			&item.Pet2ID,
			&item.CreatedAt,
		); err != nil {
			return nil, err
		}

		matches = append(matches, item)
	}

	return matches, rows.Err()
}

func normalizeList(values []string) []string {
	result := make([]string, 0, len(values))
	for _, value := range values {
		trimmed := strings.TrimSpace(value)
		if trimmed != "" {
			result = append(result, trimmed)
		}
	}
	return result
}

func recommendationDest(item *domain.Recommendation) []any {
	return []any{
		&item.ID,
		&item.OwnerID,
		&item.Name,
		&item.Species,
		&item.Breed,
		&item.Sex,
		&item.BirthDate,
		&item.WeightKg,
		&item.Bio,
		&item.PhotoURL,
		&item.PersonalityTags,
		&item.Interests,
		&item.HealthNotes,
		&item.MatchingGoal,
		&item.SearchRadiusMeters,
		&item.Latitude,
		&item.Longitude,
		&item.DistanceMeters,
		&item.BehaviorScore,
		&item.IsActive,
		&item.CreatedAt,
		&item.UpdatedAt,
	}
}
