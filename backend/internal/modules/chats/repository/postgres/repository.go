package postgres

import (
	"context"
	"database/sql"
	"errors"
	"sort"

	"pet-social-platform/backend/internal/modules/chats/domain"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Repository struct {
	db *pgxpool.Pool
}

func New(db *pgxpool.Pool) *Repository {
	return &Repository{db: db}
}

func (r *Repository) ListByUserID(ctx context.Context, userID string) ([]domain.Chat, error) {
	const query = `
		SELECT DISTINCT
			c.id,
			c.match_id,
			c.service_request_id,
			c.pet1_id,
			c.pet2_id,
			c.client_user_id,
			c.handler_user_id,
			c.created_at
		FROM chats c
		LEFT JOIN pets p ON p.id = c.pet1_id OR p.id = c.pet2_id
		WHERE p.owner_id = $1
		   OR c.client_user_id = $1
		   OR c.handler_user_id = $1
		ORDER BY c.created_at DESC
	`

	rows, err := r.db.Query(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var chats []domain.Chat
	for rows.Next() {
		chat, err := scanChat(rows)
		if err != nil {
			return nil, err
		}
		chats = append(chats, chat)
	}

	return chats, rows.Err()
}

func (r *Repository) CanUserAccessChat(ctx context.Context, chatID string, userID string) (bool, error) {
	const query = `
		SELECT EXISTS (
			SELECT 1
			FROM chats c
			LEFT JOIN pets p ON p.id = c.pet1_id OR p.id = c.pet2_id
			WHERE c.id = $1
			  AND (
				p.owner_id = $2
				OR c.client_user_id = $2
				OR c.handler_user_id = $2
			  )
		)
	`

	var exists bool
	err := r.db.QueryRow(ctx, query, chatID, userID).Scan(&exists)
	return exists, err
}

func (r *Repository) ListMessages(ctx context.Context, chatID string) ([]domain.Message, error) {
	const query = `
		SELECT id, chat_id, sender_user_id, body, created_at
		FROM messages
		WHERE chat_id = $1
		ORDER BY created_at ASC
	`

	rows, err := r.db.Query(ctx, query, chatID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var messages []domain.Message
	for rows.Next() {
		var message domain.Message
		if err := rows.Scan(
			&message.ID,
			&message.ChatID,
			&message.SenderUserID,
			&message.Body,
			&message.CreatedAt,
		); err != nil {
			return nil, err
		}
		messages = append(messages, message)
	}

	return messages, rows.Err()
}

func (r *Repository) CreateMessage(ctx context.Context, chatID string, senderUserID string, body string) (*domain.Message, error) {
	const query = `
		INSERT INTO messages (chat_id, sender_user_id, body)
		VALUES ($1, $2, $3)
		RETURNING id, chat_id, sender_user_id, body, created_at
	`

	var message domain.Message

	err := r.db.QueryRow(ctx, query, chatID, senderUserID, body).Scan(
		&message.ID,
		&message.ChatID,
		&message.SenderUserID,
		&message.Body,
		&message.CreatedAt,
	)
	if err != nil {
		return nil, err
	}

	return &message, nil
}

func (r *Repository) CreateChatIfNotExists(ctx context.Context, matchID string, pet1ID string, pet2ID string) error {
	ordered := []string{pet1ID, pet2ID}
	sort.Strings(ordered)

	const query = `
		INSERT INTO chats (match_id, pet1_id, pet2_id)
		VALUES ($1, $2, $3)
		ON CONFLICT (match_id) DO NOTHING
	`

	_, err := r.db.Exec(ctx, query, matchID, ordered[0], ordered[1])
	return err
}

func (r *Repository) CreateServiceRequestChatIfNotExists(ctx context.Context, requestID string) error {
	const query = `
		INSERT INTO chats (
			service_request_id,
			pet1_id,
			client_user_id,
			handler_user_id
		)
		SELECT
			sr.id,
			sr.pet_id,
			sr.client_user_id,
			sr.handler_user_id
		FROM service_requests sr
		WHERE sr.id = $1
		ON CONFLICT (service_request_id) DO NOTHING
	`

	_, err := r.db.Exec(ctx, query, requestID)
	return err
}

func (r *Repository) GetMatchIDByPets(ctx context.Context, pet1ID string, pet2ID string) (string, error) {
	ordered := []string{pet1ID, pet2ID}
	sort.Strings(ordered)

	const query = `
		SELECT id
		FROM matches
		WHERE pet1_id = $1 AND pet2_id = $2
	`

	var matchID string
	err := r.db.QueryRow(ctx, query, ordered[0], ordered[1]).Scan(&matchID)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return "", domain.ErrChatNotFound
		}
		return "", err
	}

	return matchID, nil
}

type chatScanner interface {
	Scan(dest ...any) error
}

func scanChat(scanner chatScanner) (domain.Chat, error) {
	var chat domain.Chat
	var matchID sql.NullString
	var serviceRequestID sql.NullString
	var pet2ID sql.NullString
	var clientUserID sql.NullString
	var handlerUserID sql.NullString

	err := scanner.Scan(
		&chat.ID,
		&matchID,
		&serviceRequestID,
		&chat.Pet1ID,
		&pet2ID,
		&clientUserID,
		&handlerUserID,
		&chat.CreatedAt,
	)
	if err != nil {
		return domain.Chat{}, err
	}

	chat.MatchID = nullableString(matchID)
	chat.ServiceRequestID = nullableString(serviceRequestID)
	chat.Pet2ID = nullableString(pet2ID)
	chat.ClientUserID = nullableString(clientUserID)
	chat.HandlerUserID = nullableString(handlerUserID)
	return chat, nil
}

func nullableString(value sql.NullString) *string {
	if !value.Valid {
		return nil
	}
	result := value.String
	return &result
}
