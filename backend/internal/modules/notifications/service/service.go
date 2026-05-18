package service

import (
	"context"
	"strings"

	"pet-social-platform/backend/internal/modules/notifications/domain"
)

type Service struct {
	repo domain.Repository
}

func New(repo domain.Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) List(ctx context.Context, userID string, limit int, unreadOnly bool) ([]domain.Notification, error) {
	userID = strings.TrimSpace(userID)
	if userID == "" {
		return nil, domain.ErrAccessDenied
	}
	if limit <= 0 || limit > 100 {
		limit = 50
	}
	return s.repo.List(ctx, userID, limit, unreadOnly)
}

func (s *Service) UnreadCount(ctx context.Context, userID string) (int, error) {
	userID = strings.TrimSpace(userID)
	if userID == "" {
		return 0, domain.ErrAccessDenied
	}
	return s.repo.UnreadCount(ctx, userID)
}

func (s *Service) MarkRead(ctx context.Context, userID string, id string) (*domain.Notification, error) {
	userID = strings.TrimSpace(userID)
	id = strings.TrimSpace(id)
	if userID == "" || id == "" {
		return nil, domain.ErrNotificationNotFound
	}
	return s.repo.MarkRead(ctx, userID, id)
}

func (s *Service) MarkAllRead(ctx context.Context, userID string) error {
	userID = strings.TrimSpace(userID)
	if userID == "" {
		return domain.ErrAccessDenied
	}
	return s.repo.MarkAllRead(ctx, userID)
}

func (s *Service) NotifyMatchCreated(ctx context.Context, userID string, matchID string, petID string, peerPetName string) error {
	return s.create(ctx, domain.CreateNotification{
		UserID:     userID,
		Type:       domain.TypeMatchCreated,
		Title:      "Новый мэтч",
		Body:       "У вас новый мэтч с " + valueOrDefault(peerPetName, "питомцем"),
		EntityType: stringPtr("match"),
		EntityID:   stringPtr(matchID),
		Metadata: map[string]string{
			"match_id": matchID,
			"pet_id":   petID,
		},
		DedupeKey: stringPtr("match_created:" + userID + ":" + matchID),
	})
}

func (s *Service) NotifyMessageReceived(ctx context.Context, userID string, chatID string, messageID string, body string) error {
	preview := strings.TrimSpace(body)
	if len([]rune(preview)) > 80 {
		preview = string([]rune(preview)[:80])
	}
	if preview == "" {
		preview = "Откройте чат, чтобы прочитать сообщение"
	}
	return s.create(ctx, domain.CreateNotification{
		UserID:     userID,
		Type:       domain.TypeMessageReceived,
		Title:      "Новое сообщение",
		Body:       preview,
		EntityType: stringPtr("chat"),
		EntityID:   stringPtr(chatID),
		Metadata: map[string]string{
			"chat_id":    chatID,
			"message_id": messageID,
		},
		DedupeKey: stringPtr("message_received:" + userID + ":" + messageID),
	})
}

func (s *Service) NotifyServiceRequestCreated(ctx context.Context, handlerUserID string, requestID string, serviceTitle string, clientName string) error {
	return s.create(ctx, domain.CreateNotification{
		UserID:     handlerUserID,
		Type:       domain.TypeServiceRequestCreated,
		Title:      "Новая заявка",
		Body:       valueOrDefault(clientName, "Клиент") + " отправил заявку на " + valueOrDefault(serviceTitle, "услугу"),
		EntityType: stringPtr("service_request"),
		EntityID:   stringPtr(requestID),
		Metadata: map[string]string{
			"request_id": requestID,
		},
		DedupeKey: stringPtr("service_request_created:" + handlerUserID + ":" + requestID),
	})
}

func (s *Service) NotifyServiceRequestStatusChanged(ctx context.Context, clientUserID string, requestID string, status string, serviceTitle string) error {
	return s.create(ctx, domain.CreateNotification{
		UserID:     clientUserID,
		Type:       domain.TypeServiceRequestStatusChanged,
		Title:      "Статус заявки изменен",
		Body:       "Заявка на " + valueOrDefault(serviceTitle, "услугу") + ": " + statusLabel(status),
		EntityType: stringPtr("service_request"),
		EntityID:   stringPtr(requestID),
		Metadata: map[string]string{
			"request_id": requestID,
			"status":     status,
		},
		DedupeKey: stringPtr("service_request_status:" + clientUserID + ":" + requestID + ":" + status),
	})
}

func (s *Service) create(ctx context.Context, notification domain.CreateNotification) error {
	notification.UserID = strings.TrimSpace(notification.UserID)
	notification.Type = strings.TrimSpace(notification.Type)
	notification.Title = strings.TrimSpace(notification.Title)
	notification.Body = strings.TrimSpace(notification.Body)
	if notification.UserID == "" || notification.Type == "" || notification.Title == "" || notification.Body == "" {
		return domain.ErrInvalidNotification
	}
	return s.repo.Create(ctx, notification)
}

func valueOrDefault(value string, fallback string) string {
	value = strings.TrimSpace(value)
	if value == "" {
		return fallback
	}
	return value
}

func statusLabel(status string) string {
	switch status {
	case "accepted":
		return "принята"
	case "rejected":
		return "отклонена"
	case "cancelled":
		return "отменена"
	case "completed":
		return "завершена"
	default:
		return status
	}
}

func stringPtr(value string) *string {
	return &value
}
