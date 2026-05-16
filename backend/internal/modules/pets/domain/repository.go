package domain

import "context"

type Repository interface {
	ListByOwnerID(ctx context.Context, ownerID string) ([]Pet, error)
	Create(ctx context.Context, pet Pet) (*Pet, error)
	GetByIDAndOwnerID(ctx context.Context, petID string, ownerID string) (*Pet, error)
	Update(ctx context.Context, pet Pet) (*Pet, error)
	SetActive(ctx context.Context, petID string, ownerID string) error
}
