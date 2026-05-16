package domain

import "context"

type Repository interface {
	IsOwnedByUser(ctx context.Context, petID string, ownerID string) (bool, error)
	GetRecommendations(ctx context.Context, sourcePetID string, ownerID string) ([]Recommendation, error)
	SaveSwipe(ctx context.Context, sourcePetID string, targetPetID string, action string) error
	HasReciprocalLike(ctx context.Context, sourcePetID string, targetPetID string) (bool, error)
	CreateMatchIfNotExists(ctx context.Context, pet1ID string, pet2ID string) error
	ListMatchesByPetID(ctx context.Context, petID string, ownerID string) ([]Match, error)
}
