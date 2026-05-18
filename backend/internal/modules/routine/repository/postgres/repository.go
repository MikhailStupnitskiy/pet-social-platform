package postgres

import (
	"context"
	"errors"

	"pet-social-platform/backend/internal/modules/routine/domain"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Repository struct {
	db *pgxpool.Pool
}

func New(db *pgxpool.Pool) *Repository {
	return &Repository{db: db}
}

func (r *Repository) IsPetOwnedByUser(ctx context.Context, petID string, ownerID string) (bool, error) {
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

func (r *Repository) ListByPetID(ctx context.Context, petID string) ([]domain.RoutineItem, error) {
	const query = `
		SELECT id, pet_id, title, category, schedule_time::text, repeat_rule, notes, is_enabled, created_at, updated_at
		FROM routine_items
		WHERE pet_id = $1
		ORDER BY created_at ASC
	`

	rows, err := r.db.Query(ctx, query, petID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var items []domain.RoutineItem
	for rows.Next() {
		var item domain.RoutineItem
		if err := rows.Scan(
			&item.ID,
			&item.PetID,
			&item.Title,
			&item.Category,
			&item.ScheduleTime,
			&item.RepeatRule,
			&item.Notes,
			&item.IsEnabled,
			&item.CreatedAt,
			&item.UpdatedAt,
		); err != nil {
			return nil, err
		}
		items = append(items, item)
	}

	return items, rows.Err()
}

func (r *Repository) Create(
	ctx context.Context,
	petID string,
	title string,
	category string,
	scheduleTime *string,
	repeatRule string,
	notes *string,
) (*domain.RoutineItem, error) {
	const query = `
		INSERT INTO routine_items (
			pet_id,
			title,
			category,
			schedule_time,
			repeat_rule,
			notes
		)
		VALUES ($1, $2, $3, $4, $5, $6)
		RETURNING id, pet_id, title, category, schedule_time::text, repeat_rule, notes, is_enabled, created_at, updated_at
	`

	var item domain.RoutineItem
	err := r.db.QueryRow(ctx, query, petID, title, category, scheduleTime, repeatRule, notes).Scan(
		&item.ID,
		&item.PetID,
		&item.Title,
		&item.Category,
		&item.ScheduleTime,
		&item.RepeatRule,
		&item.Notes,
		&item.IsEnabled,
		&item.CreatedAt,
		&item.UpdatedAt,
	)
	if err != nil {
		return nil, err
	}

	return &item, nil
}

func (r *Repository) Update(
	ctx context.Context,
	itemID string,
	ownerID string,
	title string,
	category string,
	scheduleTime *string,
	repeatRule string,
	notes *string,
	isEnabled bool,
) (*domain.RoutineItem, error) {
	const query = `
		UPDATE routine_items ri
		SET
			title = $3,
			category = $4,
			schedule_time = $5,
			repeat_rule = $6,
			notes = $7,
			is_enabled = $8,
			updated_at = NOW()
		FROM pets p
		WHERE ri.id = $1
		  AND ri.pet_id = p.id
		  AND p.owner_id = $2
		RETURNING ri.id, ri.pet_id, ri.title, ri.category, ri.schedule_time::text, ri.repeat_rule, ri.notes, ri.is_enabled, ri.created_at, ri.updated_at
	`

	var item domain.RoutineItem
	err := r.db.QueryRow(ctx, query, itemID, ownerID, title, category, scheduleTime, repeatRule, notes, isEnabled).Scan(
		&item.ID,
		&item.PetID,
		&item.Title,
		&item.Category,
		&item.ScheduleTime,
		&item.RepeatRule,
		&item.Notes,
		&item.IsEnabled,
		&item.CreatedAt,
		&item.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrRoutineItemNotFound
		}
		return nil, err
	}

	return &item, nil
}

func (r *Repository) Delete(ctx context.Context, itemID string, ownerID string) error {
	const query = `
		DELETE FROM routine_items ri
		USING pets p
		WHERE ri.id = $1
		  AND ri.pet_id = p.id
		  AND p.owner_id = $2
	`

	result, err := r.db.Exec(ctx, query, itemID, ownerID)
	if err != nil {
		return err
	}
	if result.RowsAffected() == 0 {
		return domain.ErrRoutineItemNotFound
	}
	return nil
}

func (r *Repository) Complete(ctx context.Context, itemID string, ownerID string) (*domain.Completion, error) {
	const query = `
		INSERT INTO routine_completions (routine_item_id)
		SELECT ri.id
		FROM routine_items ri
		JOIN pets p ON p.id = ri.pet_id
		WHERE ri.id = $1 AND p.owner_id = $2
		RETURNING id, routine_item_id, completed_at
	`

	var completion domain.Completion
	err := r.db.QueryRow(ctx, query, itemID, ownerID).Scan(
		&completion.ID,
		&completion.RoutineItemID,
		&completion.CompletedAt,
	)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrRoutineItemNotFound
		}
		return nil, err
	}

	return &completion, nil
}
