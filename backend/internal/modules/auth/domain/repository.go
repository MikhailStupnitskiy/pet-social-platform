package domain

import "context"

type Repository interface {
	CreateUser(ctx context.Context, email string, passwordHash string, isHandler bool) (*User, error)
	GetUserByEmail(ctx context.Context, email string) (*User, error)
	GetUserByID(ctx context.Context, userID string) (*User, error)
}
