package domain

import "context"

type Repository interface {
	IsPetOwnedByUser(ctx context.Context, petID string, userID string) (bool, error)
	Create(ctx context.Context, authorUserID string, petID string, body string, imageURL *string) (*Post, error)
	List(ctx context.Context, limit int) ([]Post, error)
	GetByID(ctx context.Context, id string) (*Post, error)
	Update(ctx context.Context, postID string, authorUserID string, body string, imageURL *string) (*Post, error)
	Delete(ctx context.Context, postID string, authorUserID string) error
}
