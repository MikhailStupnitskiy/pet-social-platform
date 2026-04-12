package service

import (
	"context"

	"pet-social-platform/backend/internal/modules/matching/domain"
)

type ChatEnsurer interface {
	EnsureChatForMatch(ctx context.Context, pet1ID string, pet2ID string) error
}

type Service struct {
	repo         domain.Repository
	chatEnsurer  ChatEnsurer
}

func New(repo domain.Repository, chatEnsurer ChatEnsurer) *Service {
	return &Service{
		repo:        repo,
		chatEnsurer: chatEnsurer,
	}
}

func (s *Service) GetRecommendations(ctx context.Context, sourcePetID string, ownerID string) ([]domain.Recommendation, error) {
	ok, err := s.repo.IsOwnedByUser(ctx, sourcePetID, ownerID)
	if err != nil {
		return nil, err
	}
	if !ok {
		return nil, domain.ErrPetAccessDenied
	}

	return s.repo.GetRecommendations(ctx, sourcePetID, ownerID)
}

func (s *Service) Swipe(ctx context.Context, sourcePetID string, targetPetID string, ownerID string, action string) (bool, error) {
	if action != "like" && action != "pass" {
		return false, domain.ErrInvalidSwipe
	}

	if sourcePetID == targetPetID {
		return false, domain.ErrSamePetSwipe
	}

	ok, err := s.repo.IsOwnedByUser(ctx, sourcePetID, ownerID)
	if err != nil {
		return false, err
	}
	if !ok {
		return false, domain.ErrPetAccessDenied
	}

	if err := s.repo.SaveSwipe(ctx, sourcePetID, targetPetID, action); err != nil {
		return false, err
	}

	if action != "like" {
		return false, nil
	}

	reciprocal, err := s.repo.HasReciprocalLike(ctx, sourcePetID, targetPetID)
	if err != nil {
		return false, err
	}

	if reciprocal {
		if err := s.repo.CreateMatchIfNotExists(ctx, sourcePetID, targetPetID); err != nil {
			return false, err
		}

		if s.chatEnsurer != nil {
			if err := s.chatEnsurer.EnsureChatForMatch(ctx, sourcePetID, targetPetID); err != nil {
				return false, err
			}
		}

		return true, nil
	}

	return false, nil
}

func (s *Service) ListMatches(ctx context.Context, petID string, ownerID string) ([]domain.Match, error) {
	ok, err := s.repo.IsOwnedByUser(ctx, petID, ownerID)
	if err != nil {
		return nil, err
	}
	if !ok {
		return nil, domain.ErrPetAccessDenied
	}

	return s.repo.ListMatchesByPetID(ctx, petID, ownerID)
}