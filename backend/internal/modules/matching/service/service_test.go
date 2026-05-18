package service

import (
	"context"
	"errors"
	"testing"

	"pet-social-platform/backend/internal/modules/matching/domain"
)

func TestGetRecommendationsRejectsInvalidGoal(t *testing.T) {
	svc := New(&fakeMatchingRepository{owned: true}, nil)

	_, err := svc.GetRecommendations(context.Background(), "pet-1", "user-1", domain.RecommendationFilters{
		Goal: "unknown",
	})

	if !errors.Is(err, domain.ErrInvalidGoal) {
		t.Fatalf("expected invalid goal, got %v", err)
	}
}

func TestGetRecommendationsScoresWalkCompatibility(t *testing.T) {
	sourceBreed := "Лабрадор"
	targetBreed := "Золотистый ретривер"
	distance := 700
	source := &domain.Recommendation{
		ID:                 "pet-1",
		Breed:              &sourceBreed,
		PersonalityTags:    []string{"Активный", "Дружелюбный"},
		Interests:          []string{"Парк", "Игры"},
		SearchRadiusMeters: 3000,
	}
	item := domain.Recommendation{
		ID:              "pet-2",
		Breed:           &targetBreed,
		PersonalityTags: []string{"Активный"},
		Interests:       []string{"Парк"},
		DistanceMeters:  &distance,
		BehaviorScore:   4,
	}
	svc := New(&fakeMatchingRepository{
		owned:  true,
		source: source,
		items:  []domain.Recommendation{item},
	}, nil)

	items, err := svc.GetRecommendations(context.Background(), "pet-1", "user-1", domain.RecommendationFilters{Goal: "walk"})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if len(items) != 1 {
		t.Fatalf("expected one recommendation, got %d", len(items))
	}
	if items[0].CompatibilityScore <= 50 {
		t.Fatalf("expected boosted score, got %d", items[0].CompatibilityScore)
	}
	if len(items[0].ScoreReasons) == 0 {
		t.Fatalf("expected score reasons")
	}
}

func TestSaveEventRequiresSourcePetOwnership(t *testing.T) {
	svc := New(&fakeMatchingRepository{owned: false}, nil)

	err := svc.SaveEvent(context.Background(), "user-1", domain.MatchingEvent{
		SourcePetID: "pet-1",
		TargetPetID: "pet-2",
		EventType:   "profile_open",
	})

	if !errors.Is(err, domain.ErrPetAccessDenied) {
		t.Fatalf("expected access denied, got %v", err)
	}
}

type fakeMatchingRepository struct {
	owned  bool
	source *domain.Recommendation
	items  []domain.Recommendation
}

func (f *fakeMatchingRepository) IsOwnedByUser(context.Context, string, string) (bool, error) {
	return f.owned, nil
}

func (f *fakeMatchingRepository) GetPetSnapshot(context.Context, string) (*domain.Recommendation, error) {
	if f.source == nil {
		return &domain.Recommendation{}, nil
	}
	return f.source, nil
}

func (f *fakeMatchingRepository) GetRecommendations(context.Context, string, string, domain.RecommendationFilters) ([]domain.Recommendation, error) {
	return f.items, nil
}

func (f *fakeMatchingRepository) SaveSwipe(context.Context, string, string, string) error {
	return nil
}

func (f *fakeMatchingRepository) SaveEvent(context.Context, domain.MatchingEvent) error {
	return nil
}

func (f *fakeMatchingRepository) HasReciprocalLike(context.Context, string, string) (bool, error) {
	return false, nil
}

func (f *fakeMatchingRepository) CreateMatchIfNotExists(context.Context, string, string) (*domain.Match, error) {
	return &domain.Match{ID: "match-1", Pet1ID: "pet-1", Pet2ID: "pet-2"}, nil
}

func (f *fakeMatchingRepository) ListMatchesByPetID(context.Context, string, string) ([]domain.Match, error) {
	return nil, nil
}
