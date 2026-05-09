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
	CommentLimit     = 100
	MaxCommentLength = 1000
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
	userID string,
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

	return s.repo.List(ctx, userID, limit)
}

func (s *Service) GetPostByID(
	ctx context.Context,
	id string,
	userID string,
) (*domain.Post, error) {
	id = strings.TrimSpace(id)

	if id == "" {
		return nil, domain.ErrPostNotFound
	}

	return s.repo.GetByID(ctx, id, userID)
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

func (s *Service) ListComments(ctx context.Context, postID string) ([]domain.Comment, error) {
	postID = strings.TrimSpace(postID)
	if postID == "" {
		return nil, domain.ErrPostNotFound
	}

	exists, err := s.repo.PostExists(ctx, postID)
	if err != nil {
		return nil, err
	}
	if !exists {
		return nil, domain.ErrPostNotFound
	}

	return s.repo.ListComments(ctx, postID, CommentLimit)
}

func (s *Service) CreateComment(ctx context.Context, postID string, userID string, body string) (*domain.Comment, error) {
	postID = strings.TrimSpace(postID)
	body = strings.TrimSpace(body)

	if postID == "" {
		return nil, domain.ErrPostNotFound
	}
	if body == "" {
		return nil, domain.ErrCommentBodyRequired
	}
	if len([]rune(body)) > MaxCommentLength {
		return nil, domain.ErrCommentBodyTooLong
	}

	exists, err := s.repo.PostExists(ctx, postID)
	if err != nil {
		return nil, err
	}
	if !exists {
		return nil, domain.ErrPostNotFound
	}

	return s.repo.CreateComment(ctx, postID, userID, body)
}

func (s *Service) UpdateComment(ctx context.Context, postID string, commentID string, userID string, body string) (*domain.Comment, error) {
	postID = strings.TrimSpace(postID)
	commentID = strings.TrimSpace(commentID)
	body = strings.TrimSpace(body)

	if postID == "" {
		return nil, domain.ErrPostNotFound
	}
	if commentID == "" {
		return nil, domain.ErrCommentNotFound
	}
	if body == "" {
		return nil, domain.ErrCommentBodyRequired
	}
	if len([]rune(body)) > MaxCommentLength {
		return nil, domain.ErrCommentBodyTooLong
	}

	return s.repo.UpdateComment(ctx, postID, commentID, userID, body)
}

func (s *Service) DeleteComment(ctx context.Context, postID string, commentID string, userID string) error {
	postID = strings.TrimSpace(postID)
	commentID = strings.TrimSpace(commentID)

	if postID == "" {
		return domain.ErrPostNotFound
	}
	if commentID == "" {
		return domain.ErrCommentNotFound
	}

	return s.repo.DeleteComment(ctx, postID, commentID, userID)
}

func (s *Service) SetReaction(ctx context.Context, postID string, userID string, reactionType string) error {
	postID = strings.TrimSpace(postID)
	reactionType = strings.TrimSpace(reactionType)

	if postID == "" {
		return domain.ErrPostNotFound
	}
	if !isValidReactionType(reactionType) {
		return domain.ErrInvalidReactionType
	}

	exists, err := s.repo.PostExists(ctx, postID)
	if err != nil {
		return err
	}
	if !exists {
		return domain.ErrPostNotFound
	}

	return s.repo.UpsertReaction(ctx, postID, userID, reactionType)
}

func (s *Service) DeleteReaction(ctx context.Context, postID string, userID string) error {
	postID = strings.TrimSpace(postID)

	if postID == "" {
		return domain.ErrPostNotFound
	}

	exists, err := s.repo.PostExists(ctx, postID)
	if err != nil {
		return err
	}
	if !exists {
		return domain.ErrPostNotFound
	}

	return s.repo.DeleteReaction(ctx, postID, userID)
}

func isValidReactionType(reactionType string) bool {
	switch reactionType {
	case "like", "love", "funny", "support":
		return true
	default:
		return false
	}
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
