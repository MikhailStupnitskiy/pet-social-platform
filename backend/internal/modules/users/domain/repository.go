package domain

import "context"

type Repository interface {
	GetProfileByUserID(ctx context.Context, userID string) (*Profile, error)
	GetProfileStats(ctx context.Context, userID string) (*ProfileStats, error)
	GetPublicProfile(ctx context.Context, userID string) (*PublicProfile, error)
	UpsertProfile(
		ctx context.Context,
		userID string,
		name string,
		birthDate *string,
		city *string,
		bio *string,
		avatarURL *string,
	) (*Profile, error)
}
