package service

import (
	"context"
	"testing"

	"pet-social-platform/backend/internal/modules/pets/domain"
)

func TestGetPublicPetReturnsRepositoryPet(t *testing.T) {
	expected := &domain.PublicPetProfile{ID: "pet-1"}
	repo := &fakeRepository{publicPet: expected}
	svc := New(repo)

	pet, err := svc.GetPublicPet(context.Background(), "pet-1")
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if pet != expected {
		t.Fatalf("expected public pet pointer from repository")
	}
	if repo.publicPetID != "pet-1" {
		t.Fatalf("expected pet-1, got %q", repo.publicPetID)
	}
}

type fakeRepository struct {
	publicPet   *domain.PublicPetProfile
	publicPetID string
}

func (f *fakeRepository) ListByOwnerID(context.Context, string) ([]domain.Pet, error) {
	return nil, nil
}

func (f *fakeRepository) Create(context.Context, domain.Pet) (*domain.Pet, error) {
	return &domain.Pet{}, nil
}

func (f *fakeRepository) GetByIDAndOwnerID(context.Context, string, string) (*domain.Pet, error) {
	return &domain.Pet{}, nil
}

func (f *fakeRepository) GetPublicByID(_ context.Context, petID string) (*domain.PublicPetProfile, error) {
	f.publicPetID = petID
	return f.publicPet, nil
}

func (f *fakeRepository) Update(context.Context, domain.Pet) (*domain.Pet, error) {
	return &domain.Pet{}, nil
}

func (f *fakeRepository) SetActive(context.Context, string, string) error {
	return nil
}
