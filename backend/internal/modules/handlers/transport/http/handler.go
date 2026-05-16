package http

import (
	"encoding/json"
	"errors"
	"net/http"
	"strconv"
	"strings"
	"time"

	"pet-social-platform/backend/internal/app/response"
	authhttp "pet-social-platform/backend/internal/modules/auth/transport/http"
	"pet-social-platform/backend/internal/modules/handlers/domain"
	"pet-social-platform/backend/internal/modules/handlers/service"

	"github.com/go-chi/chi/v5"
)

type Handler struct {
	service *service.Service
}

func NewHandler(service *service.Service) *Handler {
	return &Handler{service: service}
}

func (h *Handler) ListProfiles(w http.ResponseWriter, r *http.Request) {
	filter := domain.ProfileFilter{
		City:        optionalQuery(r, "city"),
		ServiceType: optionalQuery(r, "service_type"),
		ActiveOnly:  r.URL.Query().Get("active_only") != "false",
	}
	if minRating := strings.TrimSpace(r.URL.Query().Get("min_rating")); minRating != "" {
		value, err := strconv.ParseFloat(minRating, 64)
		if err != nil {
			response.BadRequest(w, "min_rating must be a number")
			return
		}
		filter.MinRating = &value
	}

	profiles, err := h.service.ListProfiles(r.Context(), filter)
	if err != nil {
		writeError(w, err)
		return
	}

	result := make([]HandlerProfileResponse, 0, len(profiles))
	for _, profile := range profiles {
		result = append(result, toProfileResponse(&profile))
	}
	response.JSON(w, http.StatusOK, result)
}

func (h *Handler) GetProfile(w http.ResponseWriter, r *http.Request) {
	profile, err := h.service.GetProfile(r.Context(), strings.TrimSpace(chi.URLParam(r, "id")))
	if err != nil {
		writeError(w, err)
		return
	}
	response.JSON(w, http.StatusOK, toProfileResponse(profile))
}

func (h *Handler) GetMe(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	profile, err := h.service.GetProfile(r.Context(), userID)
	if err != nil {
		writeError(w, err)
		return
	}
	response.JSON(w, http.StatusOK, toProfileResponse(profile))
}

func (h *Handler) UpsertMe(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	var req UpsertHandlerProfileRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	profile, err := h.service.UpsertProfile(
		r.Context(),
		userID,
		req.DisplayName,
		req.City,
		req.Bio,
		req.AvatarURL,
		req.ExperienceYears,
		req.Conditions,
		req.IsActive,
		req.Latitude,
		req.Longitude,
	)
	if err != nil {
		writeError(w, err)
		return
	}
	response.JSON(w, http.StatusOK, toProfileResponse(profile))
}

func (h *Handler) CreateService(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	var req UpsertHandlerServiceRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	created, err := h.service.CreateService(
		r.Context(),
		userID,
		req.ServiceType,
		req.Title,
		req.Description,
		req.PriceCents,
		req.DurationMinutes,
		req.IsActive,
	)
	if err != nil {
		writeError(w, err)
		return
	}
	response.JSON(w, http.StatusCreated, toServiceResponse(created))
}

func (h *Handler) UpdateService(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	var req UpsertHandlerServiceRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	updated, err := h.service.UpdateService(
		r.Context(),
		userID,
		strings.TrimSpace(chi.URLParam(r, "id")),
		req.ServiceType,
		req.Title,
		req.Description,
		req.PriceCents,
		req.DurationMinutes,
		req.IsActive,
	)
	if err != nil {
		writeError(w, err)
		return
	}
	response.JSON(w, http.StatusOK, toServiceResponse(updated))
}

func (h *Handler) DeleteService(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	if err := h.service.DeleteService(r.Context(), userID, strings.TrimSpace(chi.URLParam(r, "id"))); err != nil {
		writeError(w, err)
		return
	}
	w.WriteHeader(http.StatusNoContent)
}

func (h *Handler) ListRequests(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	requests, err := h.service.ListRequests(
		r.Context(),
		userID,
		r.URL.Query().Get("role"),
		optionalQuery(r, "status"),
	)
	if err != nil {
		writeError(w, err)
		return
	}

	result := make([]ServiceRequestResponse, 0, len(requests))
	for _, request := range requests {
		result = append(result, toRequestResponse(&request))
	}
	response.JSON(w, http.StatusOK, result)
}

func (h *Handler) CreateRequest(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	var req CreateServiceRequestRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	created, err := h.service.CreateRequest(
		r.Context(),
		userID,
		req.ServiceID,
		req.PetID,
		req.RequestedDate,
		req.RequestedTime,
		req.Comment,
	)
	if err != nil {
		writeError(w, err)
		return
	}
	response.JSON(w, http.StatusCreated, toRequestResponse(created))
}

func (h *Handler) UpdateRequestStatus(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	var req UpdateServiceRequestStatusRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	updated, err := h.service.UpdateRequestStatus(r.Context(), userID, strings.TrimSpace(chi.URLParam(r, "id")), req.Status)
	if err != nil {
		writeError(w, err)
		return
	}
	response.JSON(w, http.StatusOK, toRequestResponse(updated))
}

func (h *Handler) CreateReview(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	var req CreateHandlerReviewRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	review, err := h.service.CreateReview(r.Context(), userID, strings.TrimSpace(chi.URLParam(r, "id")), req.Rating, req.Body)
	if err != nil {
		writeError(w, err)
		return
	}
	response.JSON(w, http.StatusCreated, toReviewResponse(review))
}

func toProfileResponse(profile *domain.HandlerProfile) HandlerProfileResponse {
	services := make([]HandlerServiceResponse, 0, len(profile.Services))
	for _, service := range profile.Services {
		services = append(services, toServiceResponse(&service))
	}
	return HandlerProfileResponse{
		UserID:          profile.UserID,
		DisplayName:     profile.DisplayName,
		City:            profile.City,
		Bio:             profile.Bio,
		AvatarURL:       profile.AvatarURL,
		ExperienceYears: profile.ExperienceYears,
		Conditions:      profile.Conditions,
		IsActive:        profile.IsActive,
		Latitude:        profile.Latitude,
		Longitude:       profile.Longitude,
		RatingAvg:       profile.RatingAvg,
		ReviewsCount:    profile.ReviewsCount,
		Services:        services,
		CreatedAt:       profile.CreatedAt.Format(time.RFC3339),
		UpdatedAt:       profile.UpdatedAt.Format(time.RFC3339),
	}
}

func toServiceResponse(service *domain.HandlerService) HandlerServiceResponse {
	return HandlerServiceResponse{
		ID:              service.ID,
		HandlerUserID:   service.HandlerUserID,
		ServiceType:     service.ServiceType,
		Title:           service.Title,
		Description:     service.Description,
		PriceCents:      service.PriceCents,
		DurationMinutes: service.DurationMinutes,
		IsActive:        service.IsActive,
		CreatedAt:       service.CreatedAt.Format(time.RFC3339),
		UpdatedAt:       service.UpdatedAt.Format(time.RFC3339),
	}
}

func toRequestResponse(request *domain.ServiceRequest) ServiceRequestResponse {
	return ServiceRequestResponse{
		ID:            request.ID,
		ServiceID:     request.ServiceID,
		ClientUserID:  request.ClientUserID,
		ClientName:    request.ClientName,
		HandlerUserID: request.HandlerUserID,
		HandlerName:   request.HandlerName,
		PetID:         request.PetID,
		PetName:       request.PetName,
		ServiceType:   request.ServiceType,
		ServiceTitle:  request.ServiceTitle,
		RequestedDate: request.RequestedDate,
		RequestedTime: request.RequestedTime,
		Comment:       request.Comment,
		Status:        request.Status,
		Review:        toOptionalReviewResponse(request.Review),
		CreatedAt:     request.CreatedAt.Format(time.RFC3339),
		UpdatedAt:     request.UpdatedAt.Format(time.RFC3339),
	}
}

func toOptionalReviewResponse(review *domain.HandlerReview) *HandlerReviewResponse {
	if review == nil {
		return nil
	}
	response := toReviewResponse(review)
	return &response
}

func toReviewResponse(review *domain.HandlerReview) HandlerReviewResponse {
	return HandlerReviewResponse{
		ID:            review.ID,
		RequestID:     review.RequestID,
		HandlerUserID: review.HandlerUserID,
		ClientUserID:  review.ClientUserID,
		ClientName:    review.ClientName,
		Rating:        review.Rating,
		Body:          review.Body,
		CreatedAt:     review.CreatedAt.Format(time.RFC3339),
	}
}

func optionalQuery(r *http.Request, name string) *string {
	value := strings.TrimSpace(r.URL.Query().Get(name))
	if value == "" {
		return nil
	}
	return &value
}

func writeError(w http.ResponseWriter, err error) {
	switch {
	case errors.Is(err, domain.ErrProfileNotFound),
		errors.Is(err, domain.ErrServiceNotFound),
		errors.Is(err, domain.ErrRequestNotFound):
		response.NotFound(w, err.Error())
	case errors.Is(err, domain.ErrProfileAccessDenied),
		errors.Is(err, domain.ErrServiceAccessDenied),
		errors.Is(err, domain.ErrRequestAccessDenied),
		errors.Is(err, domain.ErrPetAccessDenied):
		response.Forbidden(w, err.Error())
	case errors.Is(err, domain.ErrDisplayNameRequired),
		errors.Is(err, domain.ErrDisplayNameTooLong),
		errors.Is(err, domain.ErrBioTooLong),
		errors.Is(err, domain.ErrConditionsTooLong),
		errors.Is(err, domain.ErrInvalidExperience),
		errors.Is(err, domain.ErrInvalidServiceType),
		errors.Is(err, domain.ErrServiceTitleRequired),
		errors.Is(err, domain.ErrServiceTitleTooLong),
		errors.Is(err, domain.ErrDescriptionTooLong),
		errors.Is(err, domain.ErrInvalidPrice),
		errors.Is(err, domain.ErrInvalidDuration),
		errors.Is(err, domain.ErrInactiveHandler),
		errors.Is(err, domain.ErrInactiveService),
		errors.Is(err, domain.ErrSelfRequest),
		errors.Is(err, domain.ErrInvalidRequestDate),
		errors.Is(err, domain.ErrInvalidRequestTime),
		errors.Is(err, domain.ErrInvalidRequestStatus),
		errors.Is(err, domain.ErrInvalidStatusChange),
		errors.Is(err, domain.ErrReviewNotAllowed),
		errors.Is(err, domain.ErrReviewAlreadyExists),
		errors.Is(err, domain.ErrInvalidRating),
		errors.Is(err, domain.ErrReviewBodyTooLong):
		response.BadRequest(w, err.Error())
	default:
		response.InternalServerError(w)
	}
}
