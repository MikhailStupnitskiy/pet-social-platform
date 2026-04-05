package http

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"
	"time"

	"pet-social-platform/backend/internal/app/response"
	authhttp "pet-social-platform/backend/internal/modules/auth/transport/http"
	"pet-social-platform/backend/internal/modules/users/domain"
	"pet-social-platform/backend/internal/modules/users/service"
)

type Handler struct {
	service *service.Service
}

func NewHandler(service *service.Service) *Handler {
	return &Handler{service: service}
}

func (h *Handler) GetMe(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	profile, err := h.service.GetMyProfile(r.Context(), userID)
	if err != nil {
		if errors.Is(err, domain.ErrProfileNotFound) {
			response.NotFound(w, "profile not found")
			return
		}
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusOK, toProfileResponse(profile))
}

func (h *Handler) PatchMe(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	var req UpdateProfileRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	name := strings.TrimSpace(req.Name)
	if name == "" {
		response.BadRequest(w, "name is required")
		return
	}

	profile, err := h.service.UpdateMyProfile(
		r.Context(),
		userID,
		name,
		req.BirthDate,
		trimPtr(req.City),
		trimPtr(req.Bio),
		trimPtr(req.AvatarURL),
	)
	if err != nil {
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusOK, toProfileResponse(profile))
}

func toProfileResponse(profile *domain.Profile) ProfileResponse {
	var birthDate *string
	if profile.BirthDate != nil {
		formatted := profile.BirthDate.Format("2006-01-02")
		birthDate = &formatted
	}

	return ProfileResponse{
		UserID:    profile.UserID,
		Email:     profile.Email,
		Name:      profile.Name,
		BirthDate: birthDate,
		City:      profile.City,
		Bio:       profile.Bio,
		AvatarURL: profile.AvatarURL,
		CreatedAt: profile.CreatedAt.Format(time.RFC3339),
		UpdatedAt: profile.UpdatedAt.Format(time.RFC3339),
	}
}

func trimPtr(value *string) *string {
	if value == nil {
		return nil
	}

	trimmed := strings.TrimSpace(*value)
	return &trimmed
}