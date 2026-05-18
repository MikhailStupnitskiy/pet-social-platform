package service

import (
	"context"
	"strings"

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
	repeatRule string,
	notes *string,
) (*domain.RoutineItem, error) {
	repeatRule = normalizeRepeatRule(repeatRule)
	if !isValidRepeatRule(repeatRule) {
		return nil, domain.ErrInvalidRepeatRule
	}

	ok, err := s.repo.IsPetOwnedByUser(ctx, petID, ownerID)
	if err != nil {
		return nil, err
	}
	if !ok {
		return nil, domain.ErrPetAccessDenied
	}

	return s.repo.Create(ctx, petID, title, category, scheduleTime, repeatRule, notes)
}

func (s *Service) UpdateRoutineItem(
	ctx context.Context,
	itemID string,
	ownerID string,
	title string,
	category string,
	scheduleTime *string,
	repeatRule string,
	notes *string,
	isEnabled bool,
) (*domain.RoutineItem, error) {
	repeatRule = normalizeRepeatRule(repeatRule)
	if !isValidRepeatRule(repeatRule) {
		return nil, domain.ErrInvalidRepeatRule
	}

	return s.repo.Update(ctx, itemID, ownerID, title, category, scheduleTime, repeatRule, notes, isEnabled)
}

func (s *Service) DeleteRoutineItem(ctx context.Context, itemID string, ownerID string) error {
	itemID = strings.TrimSpace(itemID)
	if itemID == "" {
		return domain.ErrRoutineItemNotFound
	}
	return s.repo.Delete(ctx, itemID, ownerID)
}

func (s *Service) CompleteRoutineItem(ctx context.Context, itemID string, ownerID string) (*domain.Completion, error) {
	return s.repo.Complete(ctx, itemID, ownerID)
}

func normalizeRepeatRule(value string) string {
	value = strings.TrimSpace(value)
	if value == "" {
		return "none"
	}
	return value
}

func isValidRepeatRule(value string) bool {
	switch value {
	case "none", "daily", "weekly", "monthly":
		return true
	default:
		return false
	}
}
