package http

import (
	"errors"
	"io"
	"net/http"
	"strconv"

	"pet-social-platform/backend/internal/app/response"
	authhttp "pet-social-platform/backend/internal/modules/auth/transport/http"
	"pet-social-platform/backend/internal/modules/images/domain"
	"pet-social-platform/backend/internal/modules/images/service"

	"github.com/go-chi/chi/v5"
)

type Handler struct {
	service *service.Service
}

func NewHandler(service *service.Service) *Handler {
	return &Handler{service: service}
}

func (h *Handler) Upload(w http.ResponseWriter, r *http.Request) {
	if !isAuthenticated(r) {
		response.Unauthorized(w, "unauthorized")
		return
	}

	r.Body = http.MaxBytesReader(w, r.Body, service.MaxImageBytes+1024*1024)
	file, header, err := r.FormFile("file")
	if err != nil {
		response.BadRequest(w, "image file is required")
		return
	}
	defer file.Close()

	imageURL, err := h.service.Upload(r.Context(), service.UploadInput{
		Content:     file,
		Size:        header.Size,
		ContentType: header.Header.Get("Content-Type"),
		Filename:    header.Filename,
	})
	if err != nil {
		writeImageError(w, err)
		return
	}

	response.JSON(w, http.StatusCreated, UploadImageResponse{ImageURL: imageURL})
}

func (h *Handler) Get(w http.ResponseWriter, r *http.Request) {
	if !isAuthenticated(r) {
		response.Unauthorized(w, "unauthorized")
		return
	}

	key := chi.URLParam(r, "key")
	object, err := h.service.Get(r.Context(), key)
	if err != nil {
		writeImageError(w, err)
		return
	}
	defer object.Content.Close()

	if object.ContentType != "" {
		w.Header().Set("Content-Type", object.ContentType)
	}
	if object.Size > 0 {
		w.Header().Set("Content-Length", strconv.FormatInt(object.Size, 10))
	}
	w.WriteHeader(http.StatusOK)
	_, _ = io.Copy(w, object.Content)
}

func isAuthenticated(r *http.Request) bool {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	return ok && userID != ""
}

func writeImageError(w http.ResponseWriter, err error) {
	switch {
	case errors.Is(err, domain.ErrFileRequired):
		response.BadRequest(w, "image file is required")
	case errors.Is(err, domain.ErrFileTooLarge):
		response.BadRequest(w, "image file is too large")
	case errors.Is(err, domain.ErrUnsupportedType):
		response.BadRequest(w, "image must be JPEG, PNG, or WebP")
	case errors.Is(err, domain.ErrImageNotFound):
		response.NotFound(w, "image not found")
	default:
		response.InternalServerError(w)
	}
}
