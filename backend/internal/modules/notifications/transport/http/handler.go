package http

import (
	"errors"
	"net/http"
	"strconv"
	"strings"
	"time"

	"pet-social-platform/backend/internal/app/response"
	authhttp "pet-social-platform/backend/internal/modules/auth/transport/http"
	"pet-social-platform/backend/internal/modules/notifications/domain"
	"pet-social-platform/backend/internal/modules/notifications/service"

	"github.com/go-chi/chi/v5"
)

type Handler struct {
	service *service.Service
}

func NewHandler(service *service.Service) *Handler {
	return &Handler{service: service}
}

func (h *Handler) List(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	limit := 50
	if rawLimit := strings.TrimSpace(r.URL.Query().Get("limit")); rawLimit != "" {
		parsed, err := strconv.Atoi(rawLimit)
		if err != nil {
			response.BadRequest(w, "limit must be a number")
			return
		}
		limit = parsed
	}
	unreadOnly := false
	if rawUnread := strings.TrimSpace(r.URL.Query().Get("unread_only")); rawUnread != "" {
		parsed, err := strconv.ParseBool(rawUnread)
		if err != nil {
			response.BadRequest(w, "unread_only must be true or false")
			return
		}
		unreadOnly = parsed
	}

	items, err := h.service.List(r.Context(), userID, limit, unreadOnly)
	if err != nil {
		writeError(w, err)
		return
	}

	result := make([]NotificationResponse, 0, len(items))
	for _, item := range items {
		result = append(result, toNotificationResponse(&item))
	}
	response.JSON(w, http.StatusOK, result)
}

func (h *Handler) UnreadCount(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	count, err := h.service.UnreadCount(r.Context(), userID)
	if err != nil {
		writeError(w, err)
		return
	}
	response.JSON(w, http.StatusOK, UnreadCountResponse{Count: count})
}

func (h *Handler) MarkRead(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	item, err := h.service.MarkRead(r.Context(), userID, strings.TrimSpace(chi.URLParam(r, "id")))
	if err != nil {
		writeError(w, err)
		return
	}
	response.JSON(w, http.StatusOK, toNotificationResponse(item))
}

func (h *Handler) MarkAllRead(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	if err := h.service.MarkAllRead(r.Context(), userID); err != nil {
		writeError(w, err)
		return
	}
	response.JSON(w, http.StatusOK, map[string]string{"status": "ok"})
}

func toNotificationResponse(item *domain.Notification) NotificationResponse {
	var readAt *string
	if item.ReadAt != nil {
		formatted := item.ReadAt.Format(time.RFC3339)
		readAt = &formatted
	}
	metadata := item.Metadata
	if metadata == nil {
		metadata = map[string]string{}
	}
	return NotificationResponse{
		ID:         item.ID,
		Type:       item.Type,
		Title:      item.Title,
		Body:       item.Body,
		EntityType: item.EntityType,
		EntityID:   item.EntityID,
		Metadata:   metadata,
		ReadAt:     readAt,
		CreatedAt:  item.CreatedAt.Format(time.RFC3339),
	}
}

func writeError(w http.ResponseWriter, err error) {
	switch {
	case errors.Is(err, domain.ErrNotificationNotFound):
		response.NotFound(w, err.Error())
	case errors.Is(err, domain.ErrAccessDenied):
		response.Forbidden(w, err.Error())
	case errors.Is(err, domain.ErrInvalidNotification):
		response.BadRequest(w, err.Error())
	default:
		response.InternalServerError(w)
	}
}
