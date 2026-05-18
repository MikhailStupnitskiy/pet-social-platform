package service

import (
	"context"
	"log"
	"strings"

	"pet-social-platform/backend/internal/modules/chats/domain"
)

type MessageNotifier interface {
	NotifyMessageReceived(ctx context.Context, userID string, chatID string, messageID string, body string) error
}

type Service struct {
	repo     domain.Repository
	notifier MessageNotifier
}

func New(repo domain.Repository, notifier ...MessageNotifier) *Service {
	var messageNotifier MessageNotifier
	if len(notifier) > 0 {
		messageNotifier = notifier[0]
	}
	return &Service{repo: repo, notifier: messageNotifier}
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

	messages, err := s.repo.ListMessages(ctx, chatID)
	if err != nil {
		return nil, err
	}
	if err := s.repo.MarkChatRead(ctx, chatID, userID); err != nil {
		return nil, err
	}
	return messages, nil
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

	message, err := s.repo.CreateMessage(ctx, chatID, userID, body)
	if err != nil {
		return nil, err
	}

	if s.notifier != nil {
		participants, err := s.repo.ListParticipantUserIDs(ctx, chatID)
		if err != nil {
			log.Printf("load chat participants for message notification: %v", err)
		} else {
			for _, participantID := range participants {
				if participantID == userID {
					continue
				}
				if err := s.notifier.NotifyMessageReceived(ctx, participantID, chatID, message.ID, body); err != nil {
					log.Printf("notify message received: %v", err)
				}
			}
		}
	}

	return message, nil
}

func (s *Service) EnsureChatForMatch(ctx context.Context, pet1ID string, pet2ID string) error {
	matchID, err := s.repo.GetMatchIDByPets(ctx, pet1ID, pet2ID)
	if err != nil {
		return err
	}

	return s.repo.CreateChatIfNotExists(ctx, matchID, pet1ID, pet2ID)
}
