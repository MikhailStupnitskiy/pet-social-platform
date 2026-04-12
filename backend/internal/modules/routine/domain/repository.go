package domain

import "context"

type Repository interface {
	IsPetOwnedByUser(ctx context.Context, petID string, ownerID string) (bool, error)
	ListByPetID(ctx context.Context, petID string) ([]RoutineItem, error)
	Create(
		ctx context.Context,
		petID string,
		title string,
		category string,
		scheduleTime *string,
		notes *string,
	) (*RoutineItem, error)
	Update(
		ctx context.Context,
		itemID string,
		ownerID string,
		title string,
		category string,
		scheduleTime *string,
		notes *string,
		isEnabled bool,
	) (*RoutineItem, error)
	Complete(ctx context.Context, itemID string, ownerID string) (*Completion, error)
}