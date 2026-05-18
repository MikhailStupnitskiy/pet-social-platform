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

	"github.com/go-chi/chi/v5"
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

func (h *Handler) GetMyStats(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	stats, err := h.service.GetMyProfileStats(r.Context(), userID)
	if err != nil {
		if errors.Is(err, domain.ErrProfileNotFound) {
			response.NotFound(w, "profile not found")
			return
		}
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusOK, ProfileStatsResponse{
		PetsCount:    stats.PetsCount,
		MatchesCount: stats.MatchesCount,
		PostsCount:   stats.PostsCount,
	})
}

func (h *Handler) GetPublicProfile(w http.ResponseWriter, r *http.Request) {
	userID := strings.TrimSpace(chi.URLParam(r, "id"))
	if userID == "" {
		response.BadRequest(w, "user id is required")
		return
	}

	profile, err := h.service.GetPublicProfile(r.Context(), userID)
	if err != nil {
		if errors.Is(err, domain.ErrProfileNotFound) {
			response.NotFound(w, "profile not found")
			return
		}
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusOK, toPublicProfileResponse(profile))
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

func toPublicProfileResponse(profile *domain.PublicProfile) PublicProfileResponse {
	var birthDate *string
	if profile.BirthDate != nil {
		formatted := profile.BirthDate.Format("2006-01-02")
		birthDate = &formatted
	}

	pets := make([]PublicPetSummaryResponse, 0, len(profile.Pets))
	for _, pet := range profile.Pets {
		pets = append(pets, toPublicPetSummaryResponse(&pet))
	}

	return PublicProfileResponse{
		UserID:    profile.UserID,
		Name:      profile.Name,
		BirthDate: birthDate,
		City:      profile.City,
		Bio:       profile.Bio,
		AvatarURL: profile.AvatarURL,
		Stats: ProfileStatsResponse{
			PetsCount:    profile.Stats.PetsCount,
			MatchesCount: profile.Stats.MatchesCount,
			PostsCount:   profile.Stats.PostsCount,
		},
		Pets:      pets,
		Handler:   toOptionalPublicHandlerProfileResponse(profile.Handler),
		CreatedAt: profile.CreatedAt.Format(time.RFC3339),
		UpdatedAt: profile.UpdatedAt.Format(time.RFC3339),
	}
}

func toPublicPetSummaryResponse(pet *domain.PublicPetSummary) PublicPetSummaryResponse {
	var birthDate *string
	if pet.BirthDate != nil {
		formatted := pet.BirthDate.Format("2006-01-02")
		birthDate = &formatted
	}

	return PublicPetSummaryResponse{
		ID:              pet.ID,
		Name:            pet.Name,
		Species:         pet.Species,
		Breed:           pet.Breed,
		Sex:             pet.Sex,
		BirthDate:       birthDate,
		Bio:             pet.Bio,
		PhotoURL:        pet.PhotoURL,
		PersonalityTags: pet.PersonalityTags,
		Interests:       pet.Interests,
		MatchingGoal:    pet.MatchingGoal,
		IsActive:        pet.IsActive,
		CreatedAt:       pet.CreatedAt.Format(time.RFC3339),
		UpdatedAt:       pet.UpdatedAt.Format(time.RFC3339),
	}
}

func toOptionalPublicHandlerProfileResponse(handler *domain.PublicHandlerProfile) *PublicHandlerProfileResponse {
	if handler == nil {
		return nil
	}

	services := make([]PublicHandlerServiceResponse, 0, len(handler.Services))
	for _, service := range handler.Services {
		services = append(services, PublicHandlerServiceResponse{
			ID:              service.ID,
			ServiceType:     service.ServiceType,
			Title:           service.Title,
			Description:     service.Description,
			PriceCents:      service.PriceCents,
			DurationMinutes: service.DurationMinutes,
		})
	}

	return &PublicHandlerProfileResponse{
		UserID:          handler.UserID,
		DisplayName:     handler.DisplayName,
		City:            handler.City,
		Bio:             handler.Bio,
		AvatarURL:       handler.AvatarURL,
		ExperienceYears: handler.ExperienceYears,
		Conditions:      handler.Conditions,
		RatingAvg:       handler.RatingAvg,
		ReviewsCount:    handler.ReviewsCount,
		Services:        services,
	}
}

func trimPtr(value *string) *string {
	if value == nil {
		return nil
	}

	trimmed := strings.TrimSpace(*value)
	return &trimmed
}
