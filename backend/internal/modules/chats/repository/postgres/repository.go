package postgres

import (
	"context"
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
		SELECT DISTINCT c.id, c.match_id, c.pet1_id, c.pet2_id, c.created_at
		FROM chats c
		JOIN pets p ON p.id = c.pet1_id OR p.id = c.pet2_id
		WHERE p.owner_id = $1
		ORDER BY c.created_at DESC
	`

	rows, err := r.db.Query(ctx, query, userID)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var chats []domain.Chat
	for rows.Next() {
		var chat domain.Chat
		if err := rows.Scan(
			&chat.ID,
			&chat.MatchID,
			&chat.Pet1ID,
			&chat.Pet2ID,
			&chat.CreatedAt,
		); err != nil {
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
			JOIN pets p ON p.id = c.pet1_id OR p.id = c.pet2_id
			WHERE c.id = $1 AND p.owner_id = $2
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