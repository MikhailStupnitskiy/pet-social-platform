package service

import (
	"context"
	"errors"
	"testing"
	"time"

	"pet-social-platform/backend/internal/modules/handlers/domain"
)

func TestCreateRequestValidatesPetOwnership(t *testing.T) {
	svc := New(&fakeRepository{
		petOwned: true,
		service: domain.HandlerService{
			ID:            "service-1",
			HandlerUserID: "handler-1",
			IsActive:      true,
		},
		profile: domain.HandlerProfile{
			UserID:   "handler-1",
			IsActive: true,
		},
	})

	if _, err := svc.CreateRequest(context.Background(), "client-1", "service-1", "pet-1", "2026-05-20", "10:30", nil); err != nil {
		t.Fatalf("expected request to be created, got %v", err)
	}
}

func TestCreateRequestRejectsOwnService(t *testing.T) {
	svc := New(&fakeRepository{
		petOwned: true,
		service: domain.HandlerService{
			ID:            "service-1",
			HandlerUserID: "user-1",
			IsActive:      true,
		},
		profile: domain.HandlerProfile{
			UserID:   "user-1",
			IsActive: true,
		},
	})

	if _, err := svc.CreateRequest(context.Background(), "user-1", "service-1", "pet-1", "2026-05-20", "10:30", nil); !errors.Is(err, domain.ErrSelfRequest) {
		t.Fatalf("expected ErrSelfRequest, got %v", err)
	}
}

func TestHandlerStatusTransitions(t *testing.T) {
	repo := &fakeRepository{
		request: domain.ServiceRequest{
			ID:            "request-1",
			ClientUserID:  "client-1",
			HandlerUserID: "handler-1",
			Status:        domain.RequestStatusPending,
		},
	}
	svc := New(repo)

	updated, err := svc.UpdateRequestStatus(context.Background(), "handler-1", "request-1", domain.RequestStatusAccepted)
	if err != nil {
		t.Fatalf("expected status update, got %v", err)
	}
	if updated.Status != domain.RequestStatusAccepted {
		t.Fatalf("expected accepted status, got %q", updated.Status)
	}

	if _, err := svc.UpdateRequestStatus(context.Background(), "client-1", "request-1", domain.RequestStatusCompleted); !errors.Is(err, domain.ErrRequestAccessDenied) {
		t.Fatalf("expected ErrRequestAccessDenied, got %v", err)
	}
}

func TestCreateReviewRequiresCompletedRequest(t *testing.T) {
	svc := New(&fakeRepository{
		request: domain.ServiceRequest{
			ID:            "request-1",
			ClientUserID:  "client-1",
			HandlerUserID: "handler-1",
			Status:        domain.RequestStatusAccepted,
		},
	})

	if _, err := svc.CreateReview(context.Background(), "client-1", "request-1", 5, nil); !errors.Is(err, domain.ErrReviewNotAllowed) {
		t.Fatalf("expected ErrReviewNotAllowed, got %v", err)
	}
}

func TestCreateReviewRecalculatesRating(t *testing.T) {
	repo := &fakeRepository{
		request: domain.ServiceRequest{
			ID:            "request-1",
			ClientUserID:  "client-1",
			HandlerUserID: "handler-1",
			Status:        domain.RequestStatusCompleted,
		},
	}
	svc := New(repo)

	if _, err := svc.CreateReview(context.Background(), "client-1", "request-1", 5, nil); err != nil {
		t.Fatalf("expected review, got %v", err)
	}
	if !repo.ratingRecalculated {
		t.Fatal("expected rating recalculation")
	}
}

type fakeRepository struct {
	petOwned           bool
	service            domain.HandlerService
	profile            domain.HandlerProfile
	request            domain.ServiceRequest
	reviewExists       bool
	ratingRecalculated bool
}

func (f *fakeRepository) ListProfiles(context.Context, domain.ProfileFilter) ([]domain.HandlerProfile, error) {
	return []domain.HandlerProfile{f.profile}, nil
}

func (f *fakeRepository) GetProfile(context.Context, string) (*domain.HandlerProfile, error) {
	if f.profile.UserID == "" {
		return nil, domain.ErrProfileNotFound
	}
	return &f.profile, nil
}

func (f *fakeRepository) UpsertProfile(_ context.Context, profile domain.HandlerProfile) (*domain.HandlerProfile, error) {
	f.profile = profile
	f.profile.CreatedAt = time.Now()
	f.profile.UpdatedAt = f.profile.CreatedAt
	return &f.profile, nil
}

func (f *fakeRepository) SetUserHandler(context.Context, string, bool) error {
	return nil
}

func (f *fakeRepository) ListServices(context.Context, string, bool) ([]domain.HandlerService, error) {
	return []domain.HandlerService{f.service}, nil
}

func (f *fakeRepository) CreateService(_ context.Context, service domain.HandlerService) (*domain.HandlerService, error) {
	f.service = service
	return &f.service, nil
}

func (f *fakeRepository) UpdateService(_ context.Context, service domain.HandlerService) (*domain.HandlerService, error) {
	f.service = service
	return &f.service, nil
}

func (f *fakeRepository) DeleteService(context.Context, string, string) error {
	return nil
}

func (f *fakeRepository) GetServiceForRequest(context.Context, string) (*domain.HandlerService, *domain.HandlerProfile, error) {
	return &f.service, &f.profile, nil
}

func (f *fakeRepository) IsPetOwnedByUser(context.Context, string, string) (bool, error) {
	return f.petOwned, nil
}

func (f *fakeRepository) CreateRequest(_ context.Context, request domain.ServiceRequest) (*domain.ServiceRequest, error) {
	f.request = request
	f.request.ID = "request-1"
	return &f.request, nil
}

func (f *fakeRepository) ListRequests(context.Context, string, string, *string) ([]domain.ServiceRequest, error) {
	return []domain.ServiceRequest{f.request}, nil
}

func (f *fakeRepository) GetRequest(context.Context, string) (*domain.ServiceRequest, error) {
	if f.request.ID == "" {
		return nil, domain.ErrRequestNotFound
	}
	return &f.request, nil
}

func (f *fakeRepository) UpdateRequestStatus(_ context.Context, _ string, status string) (*domain.ServiceRequest, error) {
	f.request.Status = status
	return &f.request, nil
}

func (f *fakeRepository) CreateServiceRequestChatIfNotExists(context.Context, string) error {
	return nil
}

func (f *fakeRepository) CreateReview(_ context.Context, review domain.HandlerReview) (*domain.HandlerReview, error) {
	review.ID = "review-1"
	return &review, nil
}

func (f *fakeRepository) ReviewExists(context.Context, string) (bool, error) {
	return f.reviewExists, nil
}

func (f *fakeRepository) RecalculateRating(context.Context, string) error {
	f.ratingRecalculated = true
	return nil
}
