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
		petFromCreateRequest(ownerID, name, species, req),
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
		petFromUpdateRequest(petID, ownerID, name, species, req),
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
		ID:                 pet.ID,
		OwnerID:            pet.OwnerID,
		Name:               pet.Name,
		Species:            pet.Species,
		Breed:              pet.Breed,
		Sex:                pet.Sex,
		BirthDate:          birthDate,
		WeightKg:           pet.WeightKg,
		Bio:                pet.Bio,
		PhotoURL:           pet.PhotoURL,
		PersonalityTags:    pet.PersonalityTags,
		Interests:          pet.Interests,
		HealthNotes:        pet.HealthNotes,
		MatchingGoal:       pet.MatchingGoal,
		SearchRadiusMeters: pet.SearchRadiusMeters,
		Latitude:           pet.Latitude,
		Longitude:          pet.Longitude,
		IsActive:           pet.IsActive,
		CreatedAt:          pet.CreatedAt.Format(time.RFC3339),
		UpdatedAt:          pet.UpdatedAt.Format(time.RFC3339),
	}
}

func petFromCreateRequest(ownerID string, name string, species string, req CreatePetRequest) domain.Pet {
	return domain.Pet{
		OwnerID:            ownerID,
		Name:               name,
		Species:            species,
		Breed:              trimPtr(req.Breed),
		Sex:                trimPtr(req.Sex),
		BirthDate:          parseDatePtr(req.BirthDate),
		WeightKg:           trimPtr(req.WeightKg),
		Bio:                trimPtr(req.Bio),
		PhotoURL:           trimPtr(req.PhotoURL),
		PersonalityTags:    cleanList(req.PersonalityTags),
		Interests:          cleanList(req.Interests),
		HealthNotes:        trimPtr(req.HealthNotes),
		MatchingGoal:       trimPtr(req.MatchingGoal),
		SearchRadiusMeters: radiusOrDefault(req.SearchRadiusMeters),
		Latitude:           trimPtr(req.Latitude),
		Longitude:          trimPtr(req.Longitude),
	}
}

func petFromUpdateRequest(petID string, ownerID string, name string, species string, req UpdatePetRequest) domain.Pet {
	pet := domain.Pet{
		ID: petID,
	}
	create := CreatePetRequest(req)
	created := petFromCreateRequest(ownerID, name, species, create)
	created.ID = pet.ID
	return created
}

func radiusOrDefault(value *int) int {
	if value == nil || *value <= 0 {
		return 3000
	}
	return *value
}

func parseDatePtr(value *string) *time.Time {
	if value == nil || strings.TrimSpace(*value) == "" {
		return nil
	}
	parsed, err := time.Parse("2006-01-02", strings.TrimSpace(*value))
	if err != nil {
		return nil
	}
	return &parsed
}

func cleanList(values []string) []string {
	result := make([]string, 0, len(values))
	seen := make(map[string]struct{}, len(values))
	for _, value := range values {
		trimmed := strings.TrimSpace(value)
		if trimmed == "" {
			continue
		}
		key := strings.ToLower(trimmed)
		if _, ok := seen[key]; ok {
			continue
		}
		seen[key] = struct{}{}
		result = append(result, trimmed)
	}
	return result
}

func trimPtr(value *string) *string {
	if value == nil {
		return nil
	}
	trimmed := strings.TrimSpace(*value)
	return &trimmed
}
