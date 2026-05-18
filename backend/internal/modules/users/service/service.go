package service

import (
	"context"

	"pet-social-platform/backend/internal/modules/users/domain"
)

type Service struct {
	repo domain.Repository
}

func New(repo domain.Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) GetMyProfile(ctx context.Context, userID string) (*domain.Profile, error) {
	return s.repo.GetProfileByUserID(ctx, userID)
}

func (s *Service) GetMyProfileStats(ctx context.Context, userID string) (*domain.ProfileStats, error) {
	return s.repo.GetProfileStats(ctx, userID)
}

func (s *Service) GetPublicProfile(ctx context.Context, userID string) (*domain.PublicProfile, error) {
	return s.repo.GetPublicProfile(ctx, userID)
}

func (s *Service) UpdateMyProfile(
	ctx context.Context,
	userID string,
	name string,
	birthDate *string,
	city *string,
	bio *string,
	avatarURL *string,
) (*domain.Profile, error) {
	return s.repo.UpsertProfile(ctx, userID, name, birthDate, city, bio, avatarURL)
}
