package service

import (
	"context"
	"strings"

	"pet-social-platform/backend/internal/modules/feed/domain"
)

const (
	DefaultFeedLimit = 30
	MaxFeedLimit     = 100
	MaxBodyLength    = 2000
	MaxImageURL      = 2048
)

type Service struct {
	repo domain.Repository
}

func New(repository domain.Repository) *Service {
	return &Service{
		repo: repository,
	}
}

func (s *Service) CreatePost(
	ctx context.Context,
	userID string,
	petID string,
	body string,
	imageURL *string,
) (*domain.Post, error) {
	petID = strings.TrimSpace(petID)
	body = strings.TrimSpace(body)
	imageURL = trimOptional(imageURL)

	if petID == "" {
		return nil, domain.ErrPetIDRequired
	}

	if body == "" {
		return nil, domain.ErrPostBodyRequired
	}
	if len([]rune(body)) > MaxBodyLength {
		return nil, domain.ErrPostBodyTooLong
	}
	if imageURL != nil && len([]rune(*imageURL)) > MaxImageURL {
		return nil, domain.ErrImageURLTooLong
	}

	owned, err := s.repo.IsPetOwnedByUser(ctx, petID, userID)
	if err != nil {
		return nil, err
	}

	if !owned {
		return nil, domain.ErrPetForbidden
	}

	return s.repo.Create(ctx, userID, petID, body, imageURL)
}

func (s *Service) ListFeed(
	ctx context.Context,
	limit int,
) ([]domain.Post, error) {
	if limit == 0 {
		limit = DefaultFeedLimit
	}
	if limit < 0 {
		return nil, domain.ErrFeedLimitTooSmall
	}
	if limit > MaxFeedLimit {
		return nil, domain.ErrFeedLimitTooLarge
	}

	return s.repo.List(ctx, limit)
}

func (s *Service) GetPostByID(
	ctx context.Context,
	id string,
) (*domain.Post, error) {
	id = strings.TrimSpace(id)

	if id == "" {
		return nil, domain.ErrPostNotFound
	}

	return s.repo.GetByID(ctx, id)
}

func (s *Service) UpdatePost(
	ctx context.Context,
	postID string,
	userID string,
	body string,
	imageURL *string,
) (*domain.Post, error) {
	postID = strings.TrimSpace(postID)
	body = strings.TrimSpace(body)
	imageURL = trimOptional(imageURL)

	if postID == "" {
		return nil, domain.ErrPostNotFound
	}
	if body == "" {
		return nil, domain.ErrPostBodyRequired
	}
	if len([]rune(body)) > MaxBodyLength {
		return nil, domain.ErrPostBodyTooLong
	}
	if imageURL != nil && len([]rune(*imageURL)) > MaxImageURL {
		return nil, domain.ErrImageURLTooLong
	}

	return s.repo.Update(ctx, postID, userID, body, imageURL)
}

func (s *Service) DeletePost(ctx context.Context, postID string, userID string) error {
	postID = strings.TrimSpace(postID)
	if postID == "" {
		return domain.ErrPostNotFound
	}

	return s.repo.Delete(ctx, postID, userID)
}

func trimOptional(value *string) *string {
	if value == nil {
		return nil
	}

	trimmed := strings.TrimSpace(*value)
	if trimmed == "" {
		return nil
	}

	return &trimmed
}
