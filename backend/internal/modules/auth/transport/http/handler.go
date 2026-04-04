package http

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"

	"pet-social-platform/backend/internal/app/response"
	"pet-social-platform/backend/internal/modules/auth/domain"
	"pet-social-platform/backend/internal/modules/auth/service"
)

type Handler struct {
	service *service.Service
}

func NewHandler(service *service.Service) *Handler {
	return &Handler{service: service}
}

func (h *Handler) Register(w http.ResponseWriter, r *http.Request) {
	var req RegisterRequest

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	req.Email = strings.TrimSpace(req.Email)
	req.Password = strings.TrimSpace(req.Password)

	if req.Email == "" {
		response.BadRequest(w, "email is required")
		return
	}

	if req.Password == "" {
		response.BadRequest(w, "password is required")
		return
	}

	token, user, err := h.service.Register(r.Context(), req.Email, req.Password)
	if err != nil {
		if errors.Is(err, domain.ErrUserAlreadyExists) {
			response.Error(w, http.StatusConflict, "user_already_exists", "user already exists")
			return
		}

		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusCreated, AuthResponse{
		Token: token,
		User: UserPayload{
			ID:    user.ID,
			Email: user.Email,
		},
	})
}

func (h *Handler) Login(w http.ResponseWriter, r *http.Request) {
	var req LoginRequest

	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	req.Email = strings.TrimSpace(req.Email)
	req.Password = strings.TrimSpace(req.Password)

	if req.Email == "" {
		response.BadRequest(w, "email is required")
		return
	}

	if req.Password == "" {
		response.BadRequest(w, "password is required")
		return
	}

	token, user, err := h.service.Login(r.Context(), req.Email, req.Password)
	if err != nil {
		if errors.Is(err, domain.ErrInvalidCredentials) {
			response.Unauthorized(w, "invalid email or password")
			return
		}

		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusOK, AuthResponse{
		Token: token,
		User: UserPayload{
			ID:    user.ID,
			Email: user.Email,
		},
	})
}

func (h *Handler) Me(w http.ResponseWriter, r *http.Request) {
	userID, ok := UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	user, err := h.service.Me(r.Context(), userID)
	if err != nil {
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusOK, UserPayload{
		ID:    user.ID,
		Email: user.Email,
	})
}