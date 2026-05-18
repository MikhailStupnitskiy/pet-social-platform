package domain

import "context"

type Repository interface {
	Create(ctx context.Context, notification CreateNotification) error
	List(ctx context.Context, userID string, limit int, unreadOnly bool) ([]Notification, error)
	UnreadCount(ctx context.Context, userID string) (int, error)
	MarkRead(ctx context.Context, userID string, id string) (*Notification, error)
	MarkAllRead(ctx context.Context, userID string) error
}
