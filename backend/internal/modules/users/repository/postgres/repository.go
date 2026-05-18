package postgres

import (
	"context"
	"database/sql"
	"errors"
	"strings"

	"pet-social-platform/backend/internal/modules/users/domain"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Repository struct {
	db *pgxpool.Pool
}

func New(db *pgxpool.Pool) *Repository {
	return &Repository{db: db}
}

func (r *Repository) GetProfileByUserID(ctx context.Context, userID string) (*domain.Profile, error) {
	const query = `
		SELECT 
			u.id,
			u.email,
			COALESCE(up.name, ''),
			up.birth_date,
			up.city,
			up.bio,
			up.avatar_url,
			COALESCE(up.created_at, u.created_at),
			COALESCE(up.updated_at, u.updated_at)
		FROM users u
		LEFT JOIN user_profiles up ON up.user_id = u.id
		WHERE u.id = $1
	`

	var profile domain.Profile

	err := r.db.QueryRow(ctx, query, userID).Scan(
		&profile.UserID,
		&profile.Email,
		&profile.Name,
		&profile.BirthDate,
		&profile.City,
		&profile.Bio,
		&profile.AvatarURL,
		&profile.CreatedAt,
		&profile.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrProfileNotFound
		}
		return nil, err
	}

	return &profile, nil
}

func (r *Repository) GetProfileStats(ctx context.Context, userID string) (*domain.ProfileStats, error) {
	const query = `
		SELECT
			COUNT(DISTINCT p.id)::integer AS pets_count,
			COUNT(DISTINCT m.id)::integer AS matches_count,
			COUNT(DISTINCT sp.id)::integer AS posts_count
		FROM users u
		LEFT JOIN pets p ON p.owner_id = u.id
		LEFT JOIN matches m ON m.pet1_id = p.id OR m.pet2_id = p.id
		LEFT JOIN social_posts sp ON sp.author_user_id = u.id
		WHERE u.id = $1
	`

	var stats domain.ProfileStats
	err := r.db.QueryRow(ctx, query, userID).Scan(
		&stats.PetsCount,
		&stats.MatchesCount,
		&stats.PostsCount,
	)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrProfileNotFound
		}
		return nil, err
	}

	return &stats, nil
}

func (r *Repository) GetPublicProfile(ctx context.Context, userID string) (*domain.PublicProfile, error) {
	profile, err := r.getPublicProfileBase(ctx, userID)
	if err != nil {
		return nil, err
	}

	stats, err := r.GetProfileStats(ctx, userID)
	if err != nil {
		return nil, err
	}
	profile.Stats = *stats

	pets, err := r.listPublicPetsByOwnerID(ctx, userID)
	if err != nil {
		return nil, err
	}
	profile.Pets = pets

	handler, err := r.getPublicHandlerProfile(ctx, userID)
	if err != nil {
		return nil, err
	}
	profile.Handler = handler

	return profile, nil
}

func (r *Repository) getPublicProfileBase(ctx context.Context, userID string) (*domain.PublicProfile, error) {
	const query = `
		SELECT
			u.id,
			COALESCE(up.name, ''),
			up.birth_date,
			up.city,
			up.bio,
			up.avatar_url,
			COALESCE(up.created_at, u.created_at),
			COALESCE(up.updated_at, u.updated_at)
		FROM users u
		LEFT JOIN user_profiles up ON up.user_id = u.id
		WHERE u.id = $1
	`

	var profile domain.PublicProfile
	err := r.db.QueryRow(ctx, query, userID).Scan(
		&profile.UserID,
		&profile.Name,
		&profile.BirthDate,
		&profile.City,
		&profile.Bio,
		&profile.AvatarURL,
		&profile.CreatedAt,
		&profile.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrProfileNotFound
		}
		return nil, err
	}

	return &profile, nil
}

func (r *Repository) listPublicPetsByOwnerID(ctx context.Context, ownerID string) ([]domain.PublicPetSummary, error) {
	const query = `
		SELECT
			id,
			name,
			species,
			breed,
			sex,
			birth_date,
			bio,
			photo_url,
			COALESCE(personality_tags, '{}'),
			COALESCE(interests, '{}'),
			matching_goal,
			is_active,
			created_at,
			updated_at
		FROM pets
		WHERE owner_id = $1
		ORDER BY is_active DESC, created_at ASC
	`

	rows, err := r.db.Query(ctx, query, ownerID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	pets := make([]domain.PublicPetSummary, 0)
	for rows.Next() {
		var pet domain.PublicPetSummary
		if err := rows.Scan(
			&pet.ID,
			&pet.Name,
			&pet.Species,
			&pet.Breed,
			&pet.Sex,
			&pet.BirthDate,
			&pet.Bio,
			&pet.PhotoURL,
			&pet.PersonalityTags,
			&pet.Interests,
			&pet.MatchingGoal,
			&pet.IsActive,
			&pet.CreatedAt,
			&pet.UpdatedAt,
		); err != nil {
			return nil, err
		}
		pet.PersonalityTags = normalizeList(pet.PersonalityTags)
		pet.Interests = normalizeList(pet.Interests)
		pets = append(pets, pet)
	}

	return pets, rows.Err()
}

func (r *Repository) getPublicHandlerProfile(ctx context.Context, userID string) (*domain.PublicHandlerProfile, error) {
	const query = `
		SELECT
			user_id,
			display_name,
			city,
			bio,
			avatar_url,
			experience_years,
			conditions,
			rating_avg,
			reviews_count
		FROM handler_profiles
		WHERE user_id = $1 AND is_active = TRUE
	`

	var handler domain.PublicHandlerProfile
	var city sql.NullString
	var bio sql.NullString
	var avatarURL sql.NullString
	var conditions sql.NullString
	err := r.db.QueryRow(ctx, query, userID).Scan(
		&handler.UserID,
		&handler.DisplayName,
		&city,
		&bio,
		&avatarURL,
		&handler.ExperienceYears,
		&conditions,
		&handler.RatingAvg,
		&handler.ReviewsCount,
	)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, nil
		}
		return nil, err
	}
	handler.City = nullableString(city)
	handler.Bio = nullableString(bio)
	handler.AvatarURL = nullableString(avatarURL)
	handler.Conditions = nullableString(conditions)

	services, err := r.listPublicHandlerServices(ctx, userID)
	if err != nil {
		return nil, err
	}
	handler.Services = services

	return &handler, nil
}

func (r *Repository) listPublicHandlerServices(ctx context.Context, userID string) ([]domain.PublicHandlerService, error) {
	const query = `
		SELECT
			id,
			service_type,
			title,
			description,
			price_cents,
			duration_minutes
		FROM handler_services
		WHERE handler_user_id = $1 AND is_active = TRUE
		ORDER BY created_at ASC
	`

	rows, err := r.db.Query(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	services := make([]domain.PublicHandlerService, 0)
	for rows.Next() {
		var service domain.PublicHandlerService
		var description sql.NullString
		var durationMinutes sql.NullInt64
		if err := rows.Scan(
			&service.ID,
			&service.ServiceType,
			&service.Title,
			&description,
			&service.PriceCents,
			&durationMinutes,
		); err != nil {
			return nil, err
		}
		service.Description = nullableString(description)
		if durationMinutes.Valid {
			value := int(durationMinutes.Int64)
			service.DurationMinutes = &value
		}
		services = append(services, service)
	}

	return services, rows.Err()
}

func (r *Repository) UpsertProfile(
	ctx context.Context,
	userID string,
	name string,
	birthDate *string,
	city *string,
	bio *string,
	avatarURL *string,
) (*domain.Profile, error) {
	const query = `
		INSERT INTO user_profiles (
			user_id,
			name,
			birth_date,
			city,
			bio,
			avatar_url
		)
		VALUES ($1, $2, $3, $4, $5, $6)
		ON CONFLICT (user_id)
		DO UPDATE SET
			name = EXCLUDED.name,
			birth_date = EXCLUDED.birth_date,
			city = EXCLUDED.city,
			bio = EXCLUDED.bio,
			avatar_url = EXCLUDED.avatar_url,
			updated_at = NOW()
		RETURNING
			user_id,
			name,
			birth_date,
			city,
			bio,
			avatar_url,
			created_at,
			updated_at
	`

	var profile domain.Profile

	err := r.db.QueryRow(ctx, query, userID, name, birthDate, city, bio, avatarURL).Scan(
		&profile.UserID,
		&profile.Name,
		&profile.BirthDate,
		&profile.City,
		&profile.Bio,
		&profile.AvatarURL,
		&profile.CreatedAt,
		&profile.UpdatedAt,
	)
	if err != nil {
		return nil, err
	}

	const emailQuery = `SELECT email FROM users WHERE id = $1`
	err = r.db.QueryRow(ctx, emailQuery, userID).Scan(&profile.Email)
	if err != nil {
		return nil, err
	}

	return &profile, nil
}

func nullableString(value sql.NullString) *string {
	if !value.Valid {
		return nil
	}
	result := value.String
	return &result
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
