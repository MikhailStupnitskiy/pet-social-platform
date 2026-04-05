package domain

import "context"

type Repository interface {
	GetProfileByUserID(ctx context.Context, userID string) (*Profile, error)
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