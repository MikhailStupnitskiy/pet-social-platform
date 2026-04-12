package service

import (
	"context"

	"pet-social-platform/backend/internal/modules/routine/domain"
)

type Service struct {
	repo domain.Repository
}

func New(repo domain.Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) ListRoutine(ctx context.Context, petID string, ownerID string) ([]domain.RoutineItem, error) {
	ok, err := s.repo.IsPetOwnedByUser(ctx, petID, ownerID)
	if err != nil {
		return nil, err
	}
	if !ok {
		return nil, domain.ErrPetAccessDenied
	}

	return s.repo.ListByPetID(ctx, petID)
}

func (s *Service) CreateRoutineItem(
	ctx context.Context,
	petID string,
	ownerID string,
	title string,
	category string,
	scheduleTime *string,
	notes *string,
) (*domain.RoutineItem, error) {
	ok, err := s.repo.IsPetOwnedByUser(ctx, petID, ownerID)
	if err != nil {
		return nil, err
	}
	if !ok {
		return nil, domain.ErrPetAccessDenied
	}

	return s.repo.Create(ctx, petID, title, category, scheduleTime, notes)
}

func (s *Service) UpdateRoutineItem(
	ctx context.Context,
	itemID string,
	ownerID string,
	title string,
	category string,
	scheduleTime *string,
	notes *string,
	isEnabled bool,
) (*domain.RoutineItem, error) {
	return s.repo.Update(ctx, itemID, ownerID, title, category, scheduleTime, notes, isEnabled)
}

func (s *Service) CompleteRoutineItem(ctx context.Context, itemID string, ownerID string) (*domain.Completion, error) {
	return s.repo.Complete(ctx, itemID, ownerID)
}