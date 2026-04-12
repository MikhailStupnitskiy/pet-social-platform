package postgres

import (
	"context"
	"errors"

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

func (r *Repository) ListByOwnerID(ctx context.Context, ownerID string) ([]domain.Pet, error) {
	const query = `
		SELECT id, owner_id, name, species, breed, sex, birth_date, weight_kg::text, bio, is_active, created_at, updated_at
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
		var pet domain.Pet

		if err := rows.Scan(
			&pet.ID,
			&pet.OwnerID,
			&pet.Name,
			&pet.Species,
			&pet.Breed,
			&pet.Sex,
			&pet.BirthDate,
			&pet.WeightKg,
			&pet.Bio,
			&pet.IsActive,
			&pet.CreatedAt,
			&pet.UpdatedAt,
		); err != nil {
			return nil, err
		}

		pets = append(pets, pet)
	}

	return pets, rows.Err()
}

func (r *Repository) Create(
	ctx context.Context,
	ownerID string,
	name string,
	species string,
	breed *string,
	sex *string,
	birthDate *string,
	weightKg *string,
	bio *string,
) (*domain.Pet, error) {
	const query = `
		INSERT INTO pets (
			owner_id,
			name,
			species,
			breed,
			sex,
			birth_date,
			weight_kg,
			bio
		)
		VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
		RETURNING id, owner_id, name, species, breed, sex, birth_date, weight_kg::text, bio, is_active, created_at, updated_at
	`

	var pet domain.Pet

	err := r.db.QueryRow(
		ctx,
		query,
		ownerID,
		name,
		species,
		breed,
		sex,
		birthDate,
		weightKg,
		bio,
	).Scan(
		&pet.ID,
		&pet.OwnerID,
		&pet.Name,
		&pet.Species,
		&pet.Breed,
		&pet.Sex,
		&pet.BirthDate,
		&pet.WeightKg,
		&pet.Bio,
		&pet.IsActive,
		&pet.CreatedAt,
		&pet.UpdatedAt,
	)
	if err != nil {
		return nil, err
	}

	return &pet, nil
}

func (r *Repository) GetByIDAndOwnerID(ctx context.Context, petID string, ownerID string) (*domain.Pet, error) {
	const query = `
		SELECT id, owner_id, name, species, breed, sex, birth_date, weight_kg::text, bio, is_active, created_at, updated_at
		FROM pets
		WHERE id = $1 AND owner_id = $2
	`

	var pet domain.Pet

	err := r.db.QueryRow(ctx, query, petID, ownerID).Scan(
		&pet.ID,
		&pet.OwnerID,
		&pet.Name,
		&pet.Species,
		&pet.Breed,
		&pet.Sex,
		&pet.BirthDate,
		&pet.WeightKg,
		&pet.Bio,
		&pet.IsActive,
		&pet.CreatedAt,
		&pet.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrPetNotFound
		}
		return nil, err
	}

	return &pet, nil
}

func (r *Repository) Update(
	ctx context.Context,
	petID string,
	ownerID string,
	name string,
	species string,
	breed *string,
	sex *string,
	birthDate *string,
	weightKg *string,
	bio *string,
) (*domain.Pet, error) {
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
			updated_at = NOW()
		WHERE id = $1 AND owner_id = $2
		RETURNING id, owner_id, name, species, breed, sex, birth_date, weight_kg::text, bio, is_active, created_at, updated_at
	`

	var pet domain.Pet

	err := r.db.QueryRow(
		ctx,
		query,
		petID,
		ownerID,
		name,
		species,
		breed,
		sex,
		birthDate,
		weightKg,
		bio,
	).Scan(
		&pet.ID,
		&pet.OwnerID,
		&pet.Name,
		&pet.Species,
		&pet.Breed,
		&pet.Sex,
		&pet.BirthDate,
		&pet.WeightKg,
		&pet.Bio,
		&pet.IsActive,
		&pet.CreatedAt,
		&pet.UpdatedAt,
	)
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