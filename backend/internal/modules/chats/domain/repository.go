package domain

import "context"

type Repository interface {
	ListByUserID(ctx context.Context, userID string) ([]Chat, error)
	CanUserAccessChat(ctx context.Context, chatID string, userID string) (bool, error)
	ListMessages(ctx context.Context, chatID string) ([]Message, error)
	CreateMessage(ctx context.Context, chatID string, senderUserID string, body string) (*Message, error)

	CreateChatIfNotExists(ctx context.Context, matchID string, pet1ID string, pet2ID string) error
	GetMatchIDByPets(ctx context.Context, pet1ID string, pet2ID string) (string, error)
}