package domain

import "context"

type Repository interface {
	IsPetOwnedByUser(ctx context.Context, petID string, userID string) (bool, error)
	Create(ctx context.Context, authorUserID string, petID string, body string, imageURL *string) (*Post, error)
	List(ctx context.Context, userID string, limit int) ([]Post, error)
	GetByID(ctx context.Context, id string, userID string) (*Post, error)
	Update(ctx context.Context, postID string, authorUserID string, body string, imageURL *string) (*Post, error)
	Delete(ctx context.Context, postID string, authorUserID string) error
	PostExists(ctx context.Context, postID string) (bool, error)
	ListComments(ctx context.Context, postID string, limit int) ([]Comment, error)
	CreateComment(ctx context.Context, postID string, authorUserID string, body string) (*Comment, error)
	UpdateComment(ctx context.Context, postID string, commentID string, authorUserID string, body string) (*Comment, error)
	DeleteComment(ctx context.Context, postID string, commentID string, authorUserID string) error
	UpsertReaction(ctx context.Context, postID string, userID string, reactionType string) error
	DeleteReaction(ctx context.Context, postID string, userID string) error
}
