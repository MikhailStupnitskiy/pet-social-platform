package http

import (
	"encoding/json"
	"errors"
	"net/http"
	"strconv"
	"strings"

	"pet-social-platform/backend/internal/app/response"
	authhttp "pet-social-platform/backend/internal/modules/auth/transport/http"
	"pet-social-platform/backend/internal/modules/feed/domain"
	"pet-social-platform/backend/internal/modules/feed/service"

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

	limit, err := parseLimit(r.URL.Query().Get("limit"))
	if err != nil {
		response.BadRequest(w, "limit must be a number")
		return
	}

	posts, err := h.service.ListFeed(r.Context(), userID, limit)
	if err != nil {
		if errors.Is(err, domain.ErrFeedLimitTooSmall) || errors.Is(err, domain.ErrFeedLimitTooLarge) {
			response.BadRequest(w, "limit must be between 1 and 100")
			return
		}

		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusOK, toPostResponses(posts))
}

func (h *Handler) Create(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	var req CreatePostRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	post, err := h.service.CreatePost(r.Context(), userID, req.PetID, req.Body, req.ImageURL)
	if err != nil {
		writePostError(w, err)
		return
	}

	response.JSON(w, http.StatusCreated, toPostResponse(post))
}

func (h *Handler) GetByID(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	postID := strings.TrimSpace(chi.URLParam(r, "id"))
	if postID == "" {
		response.BadRequest(w, "post id is required")
		return
	}

	post, err := h.service.GetPostByID(r.Context(), postID, userID)
	if err != nil {
		writePostError(w, err)
		return
	}

	response.JSON(w, http.StatusOK, toPostResponse(post))
}

func (h *Handler) Patch(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	postID := strings.TrimSpace(chi.URLParam(r, "id"))
	if postID == "" {
		response.BadRequest(w, "post id is required")
		return
	}

	var req UpdatePostRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	post, err := h.service.UpdatePost(r.Context(), postID, userID, req.Body, req.ImageURL)
	if err != nil {
		writePostError(w, err)
		return
	}

	response.JSON(w, http.StatusOK, toPostResponse(post))
}

func (h *Handler) Delete(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	postID := strings.TrimSpace(chi.URLParam(r, "id"))
	if postID == "" {
		response.BadRequest(w, "post id is required")
		return
	}

	if err := h.service.DeletePost(r.Context(), postID, userID); err != nil {
		writePostError(w, err)
		return
	}

	response.JSON(w, http.StatusOK, map[string]string{
		"status": "ok",
	})
}

func (h *Handler) ListComments(w http.ResponseWriter, r *http.Request) {
	if !isAuthenticated(r) {
		response.Unauthorized(w, "unauthorized")
		return
	}

	postID := strings.TrimSpace(chi.URLParam(r, "id"))
	if postID == "" {
		response.BadRequest(w, "post id is required")
		return
	}

	comments, err := h.service.ListComments(r.Context(), postID)
	if err != nil {
		writePostError(w, err)
		return
	}

	response.JSON(w, http.StatusOK, toCommentResponses(comments))
}

func (h *Handler) CreateComment(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	postID := strings.TrimSpace(chi.URLParam(r, "id"))
	if postID == "" {
		response.BadRequest(w, "post id is required")
		return
	}

	var req CreateCommentRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	comment, err := h.service.CreateComment(r.Context(), postID, userID, req.Body)
	if err != nil {
		writePostError(w, err)
		return
	}

	response.JSON(w, http.StatusCreated, toCommentResponse(comment))
}

func (h *Handler) PatchComment(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	postID := strings.TrimSpace(chi.URLParam(r, "id"))
	commentID := strings.TrimSpace(chi.URLParam(r, "comment_id"))
	if postID == "" {
		response.BadRequest(w, "post id is required")
		return
	}
	if commentID == "" {
		response.BadRequest(w, "comment id is required")
		return
	}

	var req UpdateCommentRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	comment, err := h.service.UpdateComment(r.Context(), postID, commentID, userID, req.Body)
	if err != nil {
		writePostError(w, err)
		return
	}

	response.JSON(w, http.StatusOK, toCommentResponse(comment))
}

func (h *Handler) DeleteComment(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	postID := strings.TrimSpace(chi.URLParam(r, "id"))
	commentID := strings.TrimSpace(chi.URLParam(r, "comment_id"))
	if postID == "" {
		response.BadRequest(w, "post id is required")
		return
	}
	if commentID == "" {
		response.BadRequest(w, "comment id is required")
		return
	}

	if err := h.service.DeleteComment(r.Context(), postID, commentID, userID); err != nil {
		writePostError(w, err)
		return
	}

	response.JSON(w, http.StatusOK, map[string]string{
		"status": "ok",
	})
}

func (h *Handler) PutReaction(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	postID := strings.TrimSpace(chi.URLParam(r, "id"))
	if postID == "" {
		response.BadRequest(w, "post id is required")
		return
	}

	var req SetReactionRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	if err := h.service.SetReaction(r.Context(), postID, userID, req.ReactionType); err != nil {
		writePostError(w, err)
		return
	}

	post, err := h.service.GetPostByID(r.Context(), postID, userID)
	if err != nil {
		writePostError(w, err)
		return
	}

	response.JSON(w, http.StatusOK, toPostResponse(post))
}

func (h *Handler) DeleteReaction(w http.ResponseWriter, r *http.Request) {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || userID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	postID := strings.TrimSpace(chi.URLParam(r, "id"))
	if postID == "" {
		response.BadRequest(w, "post id is required")
		return
	}

	if err := h.service.DeleteReaction(r.Context(), postID, userID); err != nil {
		writePostError(w, err)
		return
	}

	post, err := h.service.GetPostByID(r.Context(), postID, userID)
	if err != nil {
		writePostError(w, err)
		return
	}

	response.JSON(w, http.StatusOK, toPostResponse(post))
}

func isAuthenticated(r *http.Request) bool {
	userID, ok := authhttp.UserIDFromContext(r.Context())
	return ok && userID != ""
}

func parseLimit(value string) (int, error) {
	value = strings.TrimSpace(value)
	if value == "" {
		return 0, nil
	}

	return strconv.Atoi(value)
}

func writePostError(w http.ResponseWriter, err error) {
	switch {
	case errors.Is(err, domain.ErrPetIDRequired):
		response.BadRequest(w, "pet_id is required")
	case errors.Is(err, domain.ErrPostBodyRequired):
		response.BadRequest(w, "body is required")
	case errors.Is(err, domain.ErrPostBodyTooLong):
		response.BadRequest(w, "body is too long")
	case errors.Is(err, domain.ErrImageURLTooLong):
		response.BadRequest(w, "image_url is too long")
	case errors.Is(err, domain.ErrPetForbidden):
		response.Forbidden(w, "pet does not belong to user")
	case errors.Is(err, domain.ErrPostAccessDenied):
		response.Forbidden(w, "post access denied")
	case errors.Is(err, domain.ErrPostNotFound):
		response.NotFound(w, "post not found")
	case errors.Is(err, domain.ErrCommentBodyRequired):
		response.BadRequest(w, "comment body is required")
	case errors.Is(err, domain.ErrCommentBodyTooLong):
		response.BadRequest(w, "comment body is too long")
	case errors.Is(err, domain.ErrCommentAccessDenied):
		response.Forbidden(w, "comment access denied")
	case errors.Is(err, domain.ErrCommentNotFound):
		response.NotFound(w, "comment not found")
	case errors.Is(err, domain.ErrInvalidReactionType):
		response.BadRequest(w, "reaction_type must be one of like, love, funny, support")
	default:
		response.InternalServerError(w)
	}
}
