package http

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"
	"time"

	"pet-social-platform/backend/internal/app/response"
	authhttp "pet-social-platform/backend/internal/modules/auth/transport/http"
	"pet-social-platform/backend/internal/modules/matching/domain"
	"pet-social-platform/backend/internal/modules/matching/service"
)

type Handler struct {
	service *service.Service
}

func NewHandler(service *service.Service) *Handler {
	return &Handler{service: service}
}

func (h *Handler) Recommendations(w http.ResponseWriter, r *http.Request) {
	ownerID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || ownerID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	sourcePetID := strings.TrimSpace(r.URL.Query().Get("pet_id"))
	if sourcePetID == "" {
		response.BadRequest(w, "pet_id is required")
		return
	}

	items, err := h.service.GetRecommendations(r.Context(), sourcePetID, ownerID)
	if err != nil {
		if errors.Is(err, domain.ErrPetAccessDenied) {
			response.Forbidden(w, "pet access denied")
			return
		}
		response.InternalServerError(w)
		return
	}

	result := make([]RecommendationResponse, 0, len(items))
	for _, item := range items {
		result = append(result, toRecommendationResponse(&item))
	}

	response.JSON(w, http.StatusOK, result)
}

func (h *Handler) Swipe(w http.ResponseWriter, r *http.Request) {
	ownerID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || ownerID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	var req SwipeRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	req.SourcePetID = strings.TrimSpace(req.SourcePetID)
	req.TargetPetID = strings.TrimSpace(req.TargetPetID)
	req.Action = strings.TrimSpace(req.Action)

	if req.SourcePetID == "" {
		response.BadRequest(w, "source_pet_id is required")
		return
	}
	if req.TargetPetID == "" {
		response.BadRequest(w, "target_pet_id is required")
		return
	}
	if req.Action == "" {
		response.BadRequest(w, "action is required")
		return
	}

	isMatch, err := h.service.Swipe(r.Context(), req.SourcePetID, req.TargetPetID, ownerID, req.Action)
	if err != nil {
		switch {
		case errors.Is(err, domain.ErrInvalidSwipe):
			response.BadRequest(w, "action must be like or pass")
		case errors.Is(err, domain.ErrSamePetSwipe):
			response.BadRequest(w, "source and target pets must be different")
		case errors.Is(err, domain.ErrPetAccessDenied):
			response.Forbidden(w, "pet access denied")
		default:
			response.InternalServerError(w)
		}
		return
	}

	response.JSON(w, http.StatusOK, map[string]any{
		"status":   "ok",
		"is_match": isMatch,
	})
}

func (h *Handler) Matches(w http.ResponseWriter, r *http.Request) {
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

	items, err := h.service.ListMatches(r.Context(), petID, ownerID)
	if err != nil {
		if errors.Is(err, domain.ErrPetAccessDenied) {
			response.Forbidden(w, "pet access denied")
			return
		}
		response.InternalServerError(w)
		return
	}

	result := make([]MatchResponse, 0, len(items))
	for _, item := range items {
		result = append(result, MatchResponse{
			ID:        item.ID,
			Pet1ID:    item.Pet1ID,
			Pet2ID:    item.Pet2ID,
			CreatedAt: item.CreatedAt.Format(time.RFC3339),
		})
	}

	response.JSON(w, http.StatusOK, result)
}

func toRecommendationResponse(item *domain.Recommendation) RecommendationResponse {
	var birthDate *string
	if item.BirthDate != nil {
		formatted := item.BirthDate.Format("2006-01-02")
		birthDate = &formatted
	}

	return RecommendationResponse{
		ID:        item.ID,
		OwnerID:   item.OwnerID,
		Name:      item.Name,
		Species:   item.Species,
		Breed:     item.Breed,
		Sex:       item.Sex,
		BirthDate: birthDate,
		WeightKg:  item.WeightKg,
		Bio:       item.Bio,
		IsActive:  item.IsActive,
		CreatedAt: item.CreatedAt.Format(time.RFC3339),
		UpdatedAt: item.UpdatedAt.Format(time.RFC3339),
	}
}