package http

import (
	"encoding/json"
	"errors"
	"net/http"
	"strings"
	"time"

	"pet-social-platform/backend/internal/app/response"
	authhttp "pet-social-platform/backend/internal/modules/auth/transport/http"
	"pet-social-platform/backend/internal/modules/pets/domain"
	"pet-social-platform/backend/internal/modules/pets/service"

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

	pets, err := h.service.ListMyPets(r.Context(), ownerID)
	if err != nil {
		response.InternalServerError(w)
		return
	}

	result := make([]PetResponse, 0, len(pets))
	for _, pet := range pets {
		result = append(result, toPetResponse(&pet))
	}

	response.JSON(w, http.StatusOK, result)
}

func (h *Handler) Create(w http.ResponseWriter, r *http.Request) {
	ownerID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || ownerID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	var req CreatePetRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	name := strings.TrimSpace(req.Name)
	species := strings.TrimSpace(req.Species)

	if name == "" {
		response.BadRequest(w, "name is required")
		return
	}
	if species == "" {
		response.BadRequest(w, "species is required")
		return
	}

	pet, err := h.service.CreatePet(
		r.Context(),
		ownerID,
		name,
		species,
		trimPtr(req.Breed),
		trimPtr(req.Sex),
		req.BirthDate,
		trimPtr(req.WeightKg),
		trimPtr(req.Bio),
	)
	if err != nil {
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusCreated, toPetResponse(pet))
}

func (h *Handler) GetByID(w http.ResponseWriter, r *http.Request) {
	ownerID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || ownerID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	petID := chi.URLParam(r, "id")
	if strings.TrimSpace(petID) == "" {
		response.BadRequest(w, "pet id is required")
		return
	}

	pet, err := h.service.GetMyPet(r.Context(), petID, ownerID)
	if err != nil {
		if errors.Is(err, domain.ErrPetNotFound) {
			response.NotFound(w, "pet not found")
			return
		}
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusOK, toPetResponse(pet))
}

func (h *Handler) Patch(w http.ResponseWriter, r *http.Request) {
	ownerID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || ownerID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	petID := chi.URLParam(r, "id")
	if strings.TrimSpace(petID) == "" {
		response.BadRequest(w, "pet id is required")
		return
	}

	var req UpdatePetRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		response.BadRequest(w, "invalid request body")
		return
	}

	name := strings.TrimSpace(req.Name)
	species := strings.TrimSpace(req.Species)

	if name == "" {
		response.BadRequest(w, "name is required")
		return
	}
	if species == "" {
		response.BadRequest(w, "species is required")
		return
	}

	pet, err := h.service.UpdateMyPet(
		r.Context(),
		petID,
		ownerID,
		name,
		species,
		trimPtr(req.Breed),
		trimPtr(req.Sex),
		req.BirthDate,
		trimPtr(req.WeightKg),
		trimPtr(req.Bio),
	)
	if err != nil {
		if errors.Is(err, domain.ErrPetNotFound) {
			response.NotFound(w, "pet not found")
			return
		}
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusOK, toPetResponse(pet))
}

func (h *Handler) SetActive(w http.ResponseWriter, r *http.Request) {
	ownerID, ok := authhttp.UserIDFromContext(r.Context())
	if !ok || ownerID == "" {
		response.Unauthorized(w, "unauthorized")
		return
	}

	petID := chi.URLParam(r, "id")
	if strings.TrimSpace(petID) == "" {
		response.BadRequest(w, "pet id is required")
		return
	}

	if err := h.service.SetActivePet(r.Context(), petID, ownerID); err != nil {
		if errors.Is(err, domain.ErrPetNotFound) {
			response.NotFound(w, "pet not found")
			return
		}
		response.InternalServerError(w)
		return
	}

	response.JSON(w, http.StatusOK, map[string]string{
		"status": "ok",
	})
}

func toPetResponse(pet *domain.Pet) PetResponse {
	var birthDate *string
	if pet.BirthDate != nil {
		formatted := pet.BirthDate.Format("2006-01-02")
		birthDate = &formatted
	}

	return PetResponse{
		ID:        pet.ID,
		OwnerID:   pet.OwnerID,
		Name:      pet.Name,
		Species:   pet.Species,
		Breed:     pet.Breed,
		Sex:       pet.Sex,
		BirthDate: birthDate,
		WeightKg:  pet.WeightKg,
		Bio:       pet.Bio,
		IsActive:  pet.IsActive,
		CreatedAt: pet.CreatedAt.Format(time.RFC3339),
		UpdatedAt: pet.UpdatedAt.Format(time.RFC3339),
	}
}

func trimPtr(value *string) *string {
	if value == nil {
		return nil
	}
	trimmed := strings.TrimSpace(*value)
	return &trimmed
}