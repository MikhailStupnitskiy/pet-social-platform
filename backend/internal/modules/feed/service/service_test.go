package service

import (
	"context"
	"errors"
	"testing"

	"pet-social-platform/backend/internal/modules/feed/domain"
)

func TestCreateCommentValidation(t *testing.T) {
	svc := New(&fakeRepository{postExists: true})

	if _, err := svc.CreateComment(context.Background(), "post-1", "user-1", "   "); !errors.Is(err, domain.ErrCommentBodyRequired) {
		t.Fatalf("expected ErrCommentBodyRequired, got %v", err)
	}

	longBody := make([]rune, MaxCommentLength+1)
	for i := range longBody {
		longBody[i] = 'a'
	}
	if _, err := svc.CreateComment(context.Background(), "post-1", "user-1", string(longBody)); !errors.Is(err, domain.ErrCommentBodyTooLong) {
		t.Fatalf("expected ErrCommentBodyTooLong, got %v", err)
	}
}

func TestCreateCommentChecksPostExists(t *testing.T) {
	svc := New(&fakeRepository{postExists: false})

	if _, err := svc.CreateComment(context.Background(), "missing-post", "user-1", "hello"); !errors.Is(err, domain.ErrPostNotFound) {
		t.Fatalf("expected ErrPostNotFound, got %v", err)
	}
}

func TestUpdateCommentReturnsRepositoryAccessDenied(t *testing.T) {
	svc := New(&fakeRepository{updateCommentErr: domain.ErrCommentAccessDenied})

	if _, err := svc.UpdateComment(context.Background(), "post-1", "comment-1", "user-1", "updated"); !errors.Is(err, domain.ErrCommentAccessDenied) {
		t.Fatalf("expected ErrCommentAccessDenied, got %v", err)
	}
}

func TestSetReactionValidation(t *testing.T) {
	repo := &fakeRepository{postExists: true}
	svc := New(repo)

	if err := svc.SetReaction(context.Background(), "post-1", "user-1", "wow"); !errors.Is(err, domain.ErrInvalidReactionType) {
		t.Fatalf("expected ErrInvalidReactionType, got %v", err)
	}
	if repo.upsertedReaction != "" {
		t.Fatalf("invalid reaction should not call repository, got %q", repo.upsertedReaction)
	}
}

func TestSetReactionUpsertsValidReaction(t *testing.T) {
	repo := &fakeRepository{postExists: true}
	svc := New(repo)

	if err := svc.SetReaction(context.Background(), "post-1", "user-1", "support"); err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if repo.upsertedReaction != "support" {
		t.Fatalf("expected support reaction, got %q", repo.upsertedReaction)
	}
}

type fakeRepository struct {
	postExists       bool
	updateCommentErr error
	upsertedReaction string
}

func (f *fakeRepository) IsPetOwnedByUser(context.Context, string, string) (bool, error) {
	return true, nil
}

func (f *fakeRepository) Create(context.Context, string, string, string, *string) (*domain.Post, error) {
	return &domain.Post{}, nil
}

func (f *fakeRepository) List(context.Context, string, int) ([]domain.Post, error) {
	return nil, nil
}

func (f *fakeRepository) GetByID(context.Context, string, string) (*domain.Post, error) {
	return &domain.Post{}, nil
}

func (f *fakeRepository) Update(context.Context, string, string, string, *string) (*domain.Post, error) {
	return &domain.Post{}, nil
}

func (f *fakeRepository) Delete(context.Context, string, string) error {
	return nil
}

func (f *fakeRepository) PostExists(context.Context, string) (bool, error) {
	return f.postExists, nil
}

func (f *fakeRepository) ListComments(context.Context, string, int) ([]domain.Comment, error) {
	return nil, nil
}

func (f *fakeRepository) CreateComment(context.Context, string, string, string) (*domain.Comment, error) {
	return &domain.Comment{}, nil
}

func (f *fakeRepository) UpdateComment(context.Context, string, string, string, string) (*domain.Comment, error) {
	if f.updateCommentErr != nil {
		return nil, f.updateCommentErr
	}
	return &domain.Comment{}, nil
}

func (f *fakeRepository) DeleteComment(context.Context, string, string, string) error {
	return nil
}

func (f *fakeRepository) UpsertReaction(_ context.Context, _ string, _ string, reactionType string) error {
	f.upsertedReaction = reactionType
	return nil
}

func (f *fakeRepository) DeleteReaction(context.Context, string, string) error {
	return nil
}
