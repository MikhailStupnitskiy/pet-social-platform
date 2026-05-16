package postgres

import (
	"context"
	"errors"
	"strings"

	"pet-social-platform/backend/internal/modules/pets/domain"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Repository struct {
	db *pgxpool.Pool
}

func New(db *pgxpool.Pool) *Repository {
	return &Repository{db: db}
}

const petColumns = `
	id,
	owner_id,
	name,
	species,
	breed,
	sex,
	birth_date,
	weight_kg::text,
	bio,
	photo_url,
	COALESCE(personality_tags, '{}'),
	COALESCE(interests, '{}'),
	health_notes,
	matching_goal,
	search_radius_meters,
	latitude::text,
	longitude::text,
	is_active,
	created_at,
	updated_at
`

func (r *Repository) ListByOwnerID(ctx context.Context, ownerID string) ([]domain.Pet, error) {
	const query = `
		SELECT ` + petColumns + `
		FROM pets
		WHERE owner_id = $1
		ORDER BY created_at ASC
	`

	rows, err := r.db.Query(ctx, query, ownerID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var pets []domain.Pet

	for rows.Next() {
		pet, err := scanPet(rows)
		if err != nil {
			return nil, err
		}

		pets = append(pets, pet)
	}

	return pets, rows.Err()
}

func (r *Repository) Create(ctx context.Context, pet domain.Pet) (*domain.Pet, error) {
	const query = `
		INSERT INTO pets (
			owner_id,
			name,
			species,
			breed,
			sex,
			birth_date,
			weight_kg,
			bio,
			photo_url,
			personality_tags,
			interests,
			health_notes,
			matching_goal,
			search_radius_meters,
			latitude,
			longitude
		)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16)
		RETURNING ` + petColumns + `
	`

	err := r.db.QueryRow(
		ctx,
		query,
		pet.OwnerID,
		pet.Name,
		pet.Species,
		pet.Breed,
		pet.Sex,
		pet.BirthDate,
		pet.WeightKg,
		pet.Bio,
		pet.PhotoURL,
		pet.PersonalityTags,
		pet.Interests,
		pet.HealthNotes,
		pet.MatchingGoal,
		radiusOrDefault(pet.SearchRadiusMeters),
		pet.Latitude,
		pet.Longitude,
	).Scan(scanPetDest(&pet)...)
	if err != nil {
		return nil, err
	}

	return &pet, nil
}

func (r *Repository) GetByIDAndOwnerID(ctx context.Context, petID string, ownerID string) (*domain.Pet, error) {
	const query = `
		SELECT ` + petColumns + `
		FROM pets
		WHERE id = $1 AND owner_id = $2
	`

	pet, err := scanPet(r.db.QueryRow(ctx, query, petID, ownerID))
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrPetNotFound
		}
		return nil, err
	}

	return &pet, nil
}

func (r *Repository) Update(ctx context.Context, pet domain.Pet) (*domain.Pet, error) {
	const query = `
		UPDATE pets
		SET
			name = $3,
			species = $4,
			breed = $5,
			sex = $6,
			birth_date = $7,
			weight_kg = $8,
			bio = $9,
			photo_url = $10,
			personality_tags = $11,
			interests = $12,
			health_notes = $13,
			matching_goal = $14,
			search_radius_meters = $15,
			latitude = $16,
			longitude = $17,
			updated_at = NOW()
		WHERE id = $1 AND owner_id = $2
		RETURNING ` + petColumns + `
	`

	err := r.db.QueryRow(
		ctx,
		query,
		pet.ID,
		pet.OwnerID,
		pet.Name,
		pet.Species,
		pet.Breed,
		pet.Sex,
		pet.BirthDate,
		pet.WeightKg,
		pet.Bio,
		pet.PhotoURL,
		pet.PersonalityTags,
		pet.Interests,
		pet.HealthNotes,
		pet.MatchingGoal,
		radiusOrDefault(pet.SearchRadiusMeters),
		pet.Latitude,
		pet.Longitude,
	).Scan(scanPetDest(&pet)...)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrPetNotFound
		}
		return nil, err
	}

	return &pet, nil
}

func (r *Repository) SetActive(ctx context.Context, petID string, ownerID string) error {
	tx, err := r.db.Begin(ctx)
	if err != nil {
		return err
	}
	defer tx.Rollback(ctx)

	const resetQuery = `
		UPDATE pets
		SET is_active = FALSE, updated_at = NOW()
		WHERE owner_id = $1
	`
	if _, err := tx.Exec(ctx, resetQuery, ownerID); err != nil {
		return err
	}

	const setQuery = `
		UPDATE pets
		SET is_active = TRUE, updated_at = NOW()
		WHERE id = $1 AND owner_id = $2
	`
	tag, err := tx.Exec(ctx, setQuery, petID, ownerID)
	if err != nil {
		return err
	}
	if tag.RowsAffected() == 0 {
		return domain.ErrPetNotFound
	}

	return tx.Commit(ctx)
}

type petScanner interface {
	Scan(dest ...any) error
}

func scanPet(scanner petScanner) (domain.Pet, error) {
	var pet domain.Pet
	err := scanner.Scan(scanPetDest(&pet)...)
	if err != nil {
		return domain.Pet{}, err
	}
	pet.PersonalityTags = normalizeList(pet.PersonalityTags)
	pet.Interests = normalizeList(pet.Interests)
	return pet, nil
}

func scanPetDest(pet *domain.Pet) []any {
	return []any{
		&pet.ID,
		&pet.OwnerID,
		&pet.Name,
		&pet.Species,
		&pet.Breed,
		&pet.Sex,
		&pet.BirthDate,
		&pet.WeightKg,
		&pet.Bio,
		&pet.PhotoURL,
		&pet.PersonalityTags,
		&pet.Interests,
		&pet.HealthNotes,
		&pet.MatchingGoal,
		&pet.SearchRadiusMeters,
		&pet.Latitude,
		&pet.Longitude,
		&pet.IsActive,
		&pet.CreatedAt,
		&pet.UpdatedAt,
	}
}

func radiusOrDefault(value int) int {
	if value <= 0 {
		return 3000
	}
	return value
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
