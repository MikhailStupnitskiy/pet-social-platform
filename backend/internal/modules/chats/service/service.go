package service

import (
	"context"
	"strings"

	"pet-social-platform/backend/internal/modules/chats/domain"
)

type Service struct {
	repo domain.Repository
}

func New(repo domain.Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) ListChats(ctx context.Context, userID string) ([]domain.Chat, error) {
	return s.repo.ListByUserID(ctx, userID)
}

func (s *Service) ListMessages(ctx context.Context, chatID string, userID string) ([]domain.Message, error) {
	ok, err := s.repo.CanUserAccessChat(ctx, chatID, userID)
	if err != nil {
		return nil, err
	}
	if !ok {
		return nil, domain.ErrAccessDenied
	}

	return s.repo.ListMessages(ctx, chatID)
}

func (s *Service) SendMessage(ctx context.Context, chatID string, userID string, body string) (*domain.Message, error) {
	ok, err := s.repo.CanUserAccessChat(ctx, chatID, userID)
	if err != nil {
		return nil, err
	}
	if !ok {
		return nil, domain.ErrAccessDenied
	}

	body = strings.TrimSpace(body)
	if body == "" {
		return nil, domain.ErrInvalidMessage
	}

	return s.repo.CreateMessage(ctx, chatID, userID, body)
}

func (s *Service) EnsureChatForMatch(ctx context.Context, pet1ID string, pet2ID string) error {
	matchID, err := s.repo.GetMatchIDByPets(ctx, pet1ID, pet2ID)
	if err != nil {
		return err
	}

	return s.repo.CreateChatIfNotExists(ctx, matchID, pet1ID, pet2ID)
}
