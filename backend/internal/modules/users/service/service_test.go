package service

import (
	"context"
	"testing"

	"pet-social-platform/backend/internal/modules/users/domain"
)

func TestGetMyProfileStatsReturnsRepositoryStats(t *testing.T) {
	expected := &domain.ProfileStats{
		PetsCount:    2,
		MatchesCount: 3,
		PostsCount:   5,
	}
	repo := &fakeRepository{stats: expected}
	svc := New(repo)

	stats, err := svc.GetMyProfileStats(context.Background(), "user-1")
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if stats != expected {
		t.Fatalf("expected stats pointer from repository")
	}
	if repo.statsUserID != "user-1" {
		t.Fatalf("expected user-1, got %q", repo.statsUserID)
	}
}

type fakeRepository struct {
	stats       *domain.ProfileStats
	statsUserID string
}

func (f *fakeRepository) GetProfileByUserID(context.Context, string) (*domain.Profile, error) {
	return &domain.Profile{}, nil
}

func (f *fakeRepository) GetProfileStats(_ context.Context, userID string) (*domain.ProfileStats, error) {
	f.statsUserID = userID
	return f.stats, nil
}

func (f *fakeRepository) UpsertProfile(
	context.Context,
	string,
	string,
	*string,
	*string,
	*string,
	*string,
) (*domain.Profile, error) {
	return &domain.Profile{}, nil
}
