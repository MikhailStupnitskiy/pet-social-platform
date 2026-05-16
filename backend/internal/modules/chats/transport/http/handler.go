package http

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"
	"time"

	"pet-social-platform/backend/internal/app/response"
	authhttp "pet-social-platform/backend/internal/modules/auth/transport/http"
	"pet-social-platform/backend/internal/modules/chats/domain"
	"pet-social-platform/backend/internal/modules/chats/service"

	"github.com/go-chi/chi/v5"
)

type Handler struct {
	service *service.Service
}

func NewHandler(service *service.Service) *Handler {
	return &Handler{service: service}
}

func (h *Handler) ListChats(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	chats, err := h.service.ListChats(r.Context(), userID)
	if err != nil {
		response.InternalServerError(w)
		return
	}

	result := make([]ChatResponse, 0, len(chats))
	for _, chat := range chats {
		var lastMessageAt *string
		if chat.LastMessageAt != nil {
			formatted := chat.LastMessageAt.Format(time.RFC3339)
			lastMessageAt = &formatted
		}
		result = append(result, ChatResponse{
			ID:               chat.ID,
			MatchID:          chat.MatchID,
			ServiceRequestID: chat.ServiceRequestID,
			Pet1ID:           chat.Pet1ID,
			Pet2ID:           chat.Pet2ID,
			ClientUserID:     chat.ClientUserID,
			HandlerUserID:    chat.HandlerUserID,
			Title:            chat.Title,
			Subtitle:         chat.Subtitle,
			AvatarURL:        chat.AvatarURL,
			LastMessage:      chat.LastMessage,
			LastMessageAt:    lastMessageAt,
			UnreadCount:      chat.UnreadCount,
			IsNewMatch:       chat.IsNewMatch,
			CreatedAt:        chat.CreatedAt.Format(time.RFC3339),
		})
	}

	response.JSON(w, http.StatusOK, result)
}

func (h *Handler) ListMessages(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	chatID := strings.TrimSpace(chi.URLParam(r, "id"))
	if chatID == "" {
		response.BadRequest(w, "chat id is required")
		return
	}

	messages, err := h.service.ListMessages(r.Context(), chatID, userID)
	if err != nil {
		if errors.Is(err, domain.ErrAccessDenied) {
			response.Forbidden(w, "chat access denied")
			return
		}
		response.InternalServerError(w)
		return
	}

	result := make([]MessageResponse, 0, len(messages))
	for _, message := range messages {
		result = append(result, MessageResponse{
			ID:           message.ID,
			ChatID:       message.ChatID,
			SenderUserID: message.SenderUserID,
			Body:         message.Body,
			CreatedAt:    message.CreatedAt.Format(time.RFC3339),
		})
	}

	response.JSON(w, http.StatusOK, result)
}

func (h *Handler) SendMessage(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	chatID := strings.TrimSpace(chi.URLParam(r, "id"))
	if chatID == "" {
		response.BadRequest(w, "chat id is required")
		return
	}

	var req SendMessageRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	message, err := h.service.SendMessage(r.Context(), chatID, userID, req.Body)
	if err != nil {
		switch {
		case errors.Is(err, domain.ErrAccessDenied):
			response.Forbidden(w, "chat access denied")
		case errors.Is(err, domain.ErrInvalidMessage):
			response.BadRequest(w, "message body is required")
		default:
			response.InternalServerError(w)
		}
		return
	}

	response.JSON(w, http.StatusCreated, MessageResponse{
		ID:           message.ID,
		ChatID:       message.ChatID,
		SenderUserID: message.SenderUserID,
		Body:         message.Body,
		CreatedAt:    message.CreatedAt.Format(time.RFC3339),
	})
}
