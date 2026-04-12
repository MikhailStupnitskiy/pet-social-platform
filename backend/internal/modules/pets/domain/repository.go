package domain

import "context"

type Repository interface {
	ListByOwnerID(ctx context.Context, ownerID string) ([]Pet, error)
	Create(
		ctx context.Context,
		ownerID string,
		name string,
		species string,
		breed *string,
		sex *string,
		birthDate *string,
		weightKg *string,
		bio *string,
	) (*Pet, error)
	GetByIDAndOwnerID(ctx context.Context, petID string, ownerID string) (*Pet, error)
	Update(
		ctx context.Context,
		petID string,
		ownerID string,
		name string,
		species string,
		breed *string,
		sex *string,
		birthDate *string,
		weightKg *string,
		bio *string,
	) (*Pet, error)
	SetActive(ctx context.Context, petID string, ownerID string) error
}