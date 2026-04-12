package service

import (
	"context"

	"pet-social-platform/backend/internal/modules/pets/domain"
)

type Service struct {
	repo domain.Repository
}

func New(repo domain.Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) ListMyPets(ctx context.Context, ownerID string) ([]domain.Pet, error) {
	return s.repo.ListByOwnerID(ctx, ownerID)
}

func (s *Service) CreatePet(
	ctx context.Context,
	ownerID string,
	name string,
	species string,
	breed *string,
	sex *string,
	birthDate *string,
	weightKg *string,
	bio *string,
) (*domain.Pet, error) {
	return s.repo.Create(ctx, ownerID, name, species, breed, sex, birthDate, weightKg, bio)
}

func (s *Service) GetMyPet(ctx context.Context, petID string, ownerID string) (*domain.Pet, error) {
	return s.repo.GetByIDAndOwnerID(ctx, petID, ownerID)
}

func (s *Service) UpdateMyPet(
	ctx context.Context,
	petID string,
	ownerID string,
	name string,
	species string,
	breed *string,
	sex *string,
	birthDate *string,
	weightKg *string,
	bio *string,
) (*domain.Pet, error) {
	return s.repo.Update(ctx, petID, ownerID, name, species, breed, sex, birthDate, weightKg, bio)
}

func (s *Service) SetActivePet(ctx context.Context, petID string, ownerID string) error {
	return s.repo.SetActive(ctx, petID, ownerID)
}