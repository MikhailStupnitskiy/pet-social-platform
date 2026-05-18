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
		SELECT
			c.id,
			c.match_id,
			c.service_request_id,
			c.pet1_id,
			c.pet2_id,
			c.client_user_id,
			c.handler_user_id,
			CASE
				WHEN c.service_request_id IS NOT NULL AND c.client_user_id = $1 THEN c.handler_user_id
				WHEN c.service_request_id IS NOT NULL THEN c.client_user_id
				WHEN p1.owner_id = $1 THEN p2.owner_id
				ELSE p1.owner_id
			END AS peer_user_id,
			CASE
				WHEN c.match_id IS NOT NULL AND p1.owner_id = $1 THEN c.pet2_id
				WHEN c.match_id IS NOT NULL THEN c.pet1_id
				ELSE NULL
			END AS peer_pet_id,
			CASE
				WHEN c.service_request_id IS NOT NULL THEN 'service_request'
				ELSE 'match'
			END AS source,
			CASE
				WHEN c.service_request_id IS NOT NULL AND c.client_user_id = $1 THEN COALESCE(hp.display_name, U&'\0421\043F\0435\0446\0438\0430\043B\0438\0441\0442')
				WHEN c.service_request_id IS NOT NULL THEN COALESCE(client_profile.name, U&'\041A\043B\0438\0435\043D\0442')
				WHEN p1.owner_id = $1 THEN COALESCE(p2_owner_profile.name, U&'\0412\043B\0430\0434\0435\043B\0435\0446')
				ELSE COALESCE(p1_owner_profile.name, U&'\0412\043B\0430\0434\0435\043B\0435\0446')
			END AS peer_name,
			CASE
				WHEN c.service_request_id IS NOT NULL THEN COALESCE(client_profile.name, U&'\041A\043B\0438\0435\043D\0442')
				WHEN p1.owner_id = $1 THEN COALESCE(p2_owner_profile.name, U&'\0412\043B\0430\0434\0435\043B\0435\0446')
				ELSE COALESCE(p1_owner_profile.name, U&'\0412\043B\0430\0434\0435\043B\0435\0446')
			END AS owner_name,
			CASE
				WHEN c.service_request_id IS NOT NULL THEN COALESCE(p1.name, U&'\041F\0438\0442\043E\043C\0435\0446')
				WHEN p1.owner_id = $1 THEN COALESCE(p2.name, U&'\041F\0438\0442\043E\043C\0435\0446')
				ELSE COALESCE(p1.name, U&'\041F\0438\0442\043E\043C\0435\0446')
			END AS pet_name,
			CASE
				WHEN c.service_request_id IS NOT NULL THEN COALESCE(hs.title, U&'\0423\0441\043B\0443\0433\0430')
				ELSE NULL
			END AS service_title,
			CASE
				WHEN c.service_request_id IS NOT NULL AND c.client_user_id = $1 THEN COALESCE(hp.display_name, U&'\0421\043F\0435\0446\0438\0430\043B\0438\0441\0442')
				WHEN c.service_request_id IS NOT NULL THEN COALESCE(client_profile.name, U&'\041A\043B\0438\0435\043D\0442')
				WHEN p1.owner_id = $1 THEN COALESCE(p2.name, U&'\041F\0438\0442\043E\043C\0435\0446')
				ELSE COALESCE(p1.name, U&'\041F\0438\0442\043E\043C\0435\0446')
			END AS title,
			CASE
				WHEN c.service_request_id IS NOT NULL THEN COALESCE(hs.title, U&'\0423\0441\043B\0443\0433\0430')
				WHEN p1.owner_id = $1 THEN COALESCE(p2.breed, p2.species, U&'\041C\044D\0442\0447')
				ELSE COALESCE(p1.breed, p1.species, U&'\041C\044D\0442\0447')
			END AS subtitle,
			CASE
				WHEN c.service_request_id IS NOT NULL AND c.client_user_id = $1 THEN hp.avatar_url
				WHEN c.service_request_id IS NOT NULL THEN client_profile.avatar_url
				WHEN p1.owner_id = $1 THEN p2.photo_url
				ELSE p1.photo_url
			END AS avatar_url,
			last_message.body,
			last_message.created_at,
			COALESCE(unread.unread_count, 0),
			(c.match_id IS NOT NULL AND last_message.id IS NULL),
			c.created_at
		FROM chats c
		LEFT JOIN pets p1 ON p1.id = c.pet1_id
		LEFT JOIN pets p2 ON p2.id = c.pet2_id
		LEFT JOIN user_profiles p1_owner_profile ON p1_owner_profile.user_id = p1.owner_id
		LEFT JOIN user_profiles p2_owner_profile ON p2_owner_profile.user_id = p2.owner_id
		LEFT JOIN service_requests sr ON sr.id = c.service_request_id
		LEFT JOIN handler_services hs ON hs.id = sr.service_id
		LEFT JOIN handler_profiles hp ON hp.user_id = c.handler_user_id
		LEFT JOIN user_profiles client_profile ON client_profile.user_id = c.client_user_id
		LEFT JOIN chat_read_states read_state
			ON read_state.chat_id = c.id AND read_state.user_id = $1
		LEFT JOIN LATERAL (
			SELECT id, body, created_at
			FROM messages
			WHERE chat_id = c.id
			ORDER BY created_at DESC
			LIMIT 1
		) last_message ON TRUE
		LEFT JOIN LATERAL (
			SELECT COUNT(*) AS unread_count
			FROM messages m
			WHERE m.chat_id = c.id
			  AND m.sender_user_id <> $1
			  AND m.created_at > COALESCE(read_state.last_read_at, 'epoch'::timestamptz)
		) unread ON TRUE
		WHERE (
				(p1.owner_id = $1 AND p1.is_active)
				OR (p2.owner_id = $1 AND p2.is_active)
				OR (c.client_user_id = $1 AND p1.is_active)
				OR (c.handler_user_id = $1 AND p1.is_active)
			)
		ORDER BY COALESCE(last_message.created_at, c.created_at) DESC
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

func (r *Repository) MarkChatRead(ctx context.Context, chatID string, userID string) error {
	const query = `
		INSERT INTO chat_read_states (chat_id, user_id, last_read_at)
		VALUES ($1, $2, NOW())
		ON CONFLICT (chat_id, user_id)
		DO UPDATE SET last_read_at = EXCLUDED.last_read_at
	`
	_, err := r.db.Exec(ctx, query, chatID, userID)
	return err
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
	var peerUserID sql.NullString
	var peerPetID sql.NullString
	var peerName sql.NullString
	var ownerName sql.NullString
	var petName sql.NullString
	var serviceTitle sql.NullString
	var avatarURL sql.NullString
	var lastMessage sql.NullString
	var lastMessageAt sql.NullTime

	err := scanner.Scan(
		&chat.ID,
		&matchID,
		&serviceRequestID,
		&chat.Pet1ID,
		&pet2ID,
		&clientUserID,
		&handlerUserID,
		&peerUserID,
		&peerPetID,
		&chat.Source,
		&peerName,
		&ownerName,
		&petName,
		&serviceTitle,
		&chat.Title,
		&chat.Subtitle,
		&avatarURL,
		&lastMessage,
		&lastMessageAt,
		&chat.UnreadCount,
		&chat.IsNewMatch,
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
	chat.PeerUserID = nullableString(peerUserID)
	chat.PeerPetID = nullableString(peerPetID)
	chat.PeerName = nullableString(peerName)
	chat.OwnerName = nullableString(ownerName)
	chat.PetName = nullableString(petName)
	chat.ServiceTitle = nullableString(serviceTitle)
	chat.AvatarURL = nullableString(avatarURL)
	chat.LastMessage = nullableString(lastMessage)
	if lastMessageAt.Valid {
		chat.LastMessageAt = &lastMessageAt.Time
	}
	return chat, nil
}

func nullableString(value sql.NullString) *string {
	if !value.Valid {
		return nil
	}
	result := value.String
	return &result
}
