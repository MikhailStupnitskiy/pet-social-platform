package postgres

import (
	"context"
	"database/sql"
	"encoding/json"
	"errors"

	"pet-social-platform/backend/internal/modules/notifications/domain"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Repository struct {
	db *pgxpool.Pool
}

func New(db *pgxpool.Pool) *Repository {
	return &Repository{db: db}
}

func (r *Repository) Create(ctx context.Context, notification domain.CreateNotification) error {
	metadata := notification.Metadata
	if metadata == nil {
		metadata = map[string]string{}
	}
	metadataJSON, err := json.Marshal(metadata)
	if err != nil {
		return err
	}

	const query = `
		INSERT INTO notifications (
			user_id,
			type,
			title,
			body,
			entity_type,
			entity_id,
			metadata,
			dedupe_key
		)
		VALUES ($1, $2, $3, $4, $5, $6, $7::jsonb, $8)
	`

	_, err = r.db.Exec(
		ctx,
		query,
		notification.UserID,
		notification.Type,
		notification.Title,
		notification.Body,
		notification.EntityType,
		notification.EntityID,
		string(metadataJSON),
		notification.DedupeKey,
	)
	if err != nil {
		var pgErr *pgconn.PgError
		if errors.As(err, &pgErr) && pgErr.Code == "23505" {
			return nil
		}
	}
	return err
}

func (r *Repository) List(ctx context.Context, userID string, limit int, unreadOnly bool) ([]domain.Notification, error) {
	const query = `
		SELECT id, user_id, type, title, body, entity_type, entity_id::text, metadata, read_at, dedupe_key, created_at
		FROM notifications
		WHERE user_id = $1
		  AND ($2::boolean = FALSE OR read_at IS NULL)
		ORDER BY created_at DESC
		LIMIT $3
	`

	rows, err := r.db.Query(ctx, query, userID, unreadOnly, limit)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	items := make([]domain.Notification, 0)
	for rows.Next() {
		item, err := scanNotification(rows)
		if err != nil {
			return nil, err
		}
		items = append(items, item)
	}
	return items, rows.Err()
}

func (r *Repository) UnreadCount(ctx context.Context, userID string) (int, error) {
	const query = `
		SELECT COUNT(*)::integer
		FROM notifications
		WHERE user_id = $1 AND read_at IS NULL
	`
	var count int
	err := r.db.QueryRow(ctx, query, userID).Scan(&count)
	return count, err
}

func (r *Repository) MarkRead(ctx context.Context, userID string, id string) (*domain.Notification, error) {
	const query = `
		UPDATE notifications
		SET read_at = COALESCE(read_at, NOW())
		WHERE id = $1 AND user_id = $2
		RETURNING id, user_id, type, title, body, entity_type, entity_id::text, metadata, read_at, dedupe_key, created_at
	`
	item, err := scanNotification(r.db.QueryRow(ctx, query, id, userID))
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrNotificationNotFound
		}
		return nil, err
	}
	return &item, nil
}

func (r *Repository) MarkAllRead(ctx context.Context, userID string) error {
	const query = `
		UPDATE notifications
		SET read_at = COALESCE(read_at, NOW())
		WHERE user_id = $1 AND read_at IS NULL
	`
	_, err := r.db.Exec(ctx, query, userID)
	return err
}

type scanner interface {
	Scan(dest ...any) error
}

func scanNotification(scanner scanner) (domain.Notification, error) {
	var item domain.Notification
	var entityType sql.NullString
	var entityID sql.NullString
	var readAt sql.NullTime
	var dedupeKey sql.NullString
	var metadataBytes []byte

	err := scanner.Scan(
		&item.ID,
		&item.UserID,
		&item.Type,
		&item.Title,
		&item.Body,
		&entityType,
		&entityID,
		&metadataBytes,
		&readAt,
		&dedupeKey,
		&item.CreatedAt,
	)
	if err != nil {
		return domain.Notification{}, err
	}

	item.EntityType = nullableString(entityType)
	item.EntityID = nullableString(entityID)
	item.DedupeKey = nullableString(dedupeKey)
	if readAt.Valid {
		item.ReadAt = &readAt.Time
	}
	item.Metadata = map[string]string{}
	if len(metadataBytes) > 0 {
		_ = json.Unmarshal(metadataBytes, &item.Metadata)
	}
	return item, nil
}

func nullableString(value sql.NullString) *string {
	if !value.Valid {
		return nil
	}
	result := value.String
	return &result
}
