package http

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"
	"time"

	"pet-social-platform/backend/internal/app/response"
	authhttp "pet-social-platform/backend/internal/modules/auth/transport/http"
	"pet-social-platform/backend/internal/modules/routine/domain"
	"pet-social-platform/backend/internal/modules/routine/service"

	"github.com/go-chi/chi/v5"
)

type Handler struct {
	service *service.Service
}

func NewHandler(service *service.Service) *Handler {
	return &Handler{service: service}
}

func (h *Handler) List(w http.ResponseWriter, r *http.Request) {
	ownerID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || ownerID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	petID := strings.TrimSpace(r.URL.Query().Get("pet_id"))
	if petID == "" {
		response.BadRequest(w, "pet_id is required")
		return
	}

	items, err := h.service.ListRoutine(r.Context(), petID, ownerID)
	if err != nil {
		if errors.Is(err, domain.ErrPetAccessDenied) {
			response.Forbidden(w, "pet access denied")
			return
		}
		response.InternalServerError(w)
		return
	}

	result := make([]RoutineItemResponse, 0, len(items))
	for _, item := range items {
		result = append(result, toRoutineItemResponse(&item))
	}

	response.JSON(w, http.StatusOK, result)
}

func (h *Handler) Create(w http.ResponseWriter, r *http.Request) {
	ownerID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || ownerID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	var req CreateRoutineItemRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	req.PetID = strings.TrimSpace(req.PetID)
	req.Title = strings.TrimSpace(req.Title)
	req.Category = strings.TrimSpace(req.Category)

	if req.PetID == "" {
		response.BadRequest(w, "pet_id is required")
		return
	}
	if req.Title == "" {
		response.BadRequest(w, "title is required")
		return
	}
	if req.Category == "" {
		response.BadRequest(w, "category is required")
		return
	}

	item, err := h.service.CreateRoutineItem(
		r.Context(),
		req.PetID,
		ownerID,
		req.Title,
		req.Category,
		trimPtr(req.ScheduleTime),
		trimPtr(req.Notes),
	)
	if err != nil {
		if errors.Is(err, domain.ErrPetAccessDenied) {
			response.Forbidden(w, "pet access denied")
			return
		}
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusCreated, toRoutineItemResponse(item))
}

func (h *Handler) Patch(w http.ResponseWriter, r *http.Request) {
	ownerID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || ownerID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	itemID := strings.TrimSpace(chi.URLParam(r, "id"))
	if itemID == "" {
		response.BadRequest(w, "routine item id is required")
		return
	}

	var req UpdateRoutineItemRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	req.Title = strings.TrimSpace(req.Title)
	req.Category = strings.TrimSpace(req.Category)

	if req.Title == "" {
		response.BadRequest(w, "title is required")
		return
	}
	if req.Category == "" {
		response.BadRequest(w, "category is required")
		return
	}

	item, err := h.service.UpdateRoutineItem(
		r.Context(),
		itemID,
		ownerID,
		req.Title,
		req.Category,
		trimPtr(req.ScheduleTime),
		trimPtr(req.Notes),
		req.IsEnabled,
	)
	if err != nil {
		if errors.Is(err, domain.ErrRoutineItemNotFound) {
			response.NotFound(w, "routine item not found")
			return
		}
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusOK, toRoutineItemResponse(item))
}

func (h *Handler) Complete(w http.ResponseWriter, r *http.Request) {
	ownerID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || ownerID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	itemID := strings.TrimSpace(chi.URLParam(r, "id"))
	if itemID == "" {
		response.BadRequest(w, "routine item id is required")
		return
	}

	completion, err := h.service.CompleteRoutineItem(r.Context(), itemID, ownerID)
	if err != nil {
		if errors.Is(err, domain.ErrRoutineItemNotFound) {
			response.NotFound(w, "routine item not found")
			return
		}
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusCreated, CompletionResponse{
		ID:            completion.ID,
		RoutineItemID: completion.RoutineItemID,
		CompletedAt:   completion.CompletedAt.Format(time.RFC3339),
	})
}

func toRoutineItemResponse(item *domain.RoutineItem) RoutineItemResponse {
	return RoutineItemResponse{
		ID:           item.ID,
		PetID:        item.PetID,
		Title:        item.Title,
		Category:     item.Category,
		ScheduleTime: item.ScheduleTime,
		Notes:        item.Notes,
		IsEnabled:    item.IsEnabled,
		CreatedAt:    item.CreatedAt.Format(time.RFC3339),
		UpdatedAt:    item.UpdatedAt.Format(time.RFC3339),
	}
}

func trimPtr(value *string) *string {
	if value == nil {
		return nil
	}
	trimmed := strings.TrimSpace(*value)
	return &trimmed
}