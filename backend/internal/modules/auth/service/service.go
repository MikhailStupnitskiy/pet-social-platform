package service

import (
	"context"
	"errors"

	"pet-social-platform/backend/internal/modules/auth/domain"

	"github.com/jackc/pgx/v5"
)

type Service struct {
	repo domain.Repository
	jwt  *JWTService
}

func New(repo domain.Repository, jwt *JWTService) *Service {
	return &Service{
		repo: repo,
		jwt:  jwt,
	}
}

func (s *Service) Register(ctx context.Context, email string, password string) (string, *domain.User, error) {
	passwordHash, err := HashPassword(password)
	if err != nil {
		return "", nil, err
	}

	user, err := s.repo.CreateUser(ctx, email, passwordHash)
	if err != nil {
		return "", nil, err
	}

	token, err := s.jwt.GenerateToken(user.ID)
	if err != nil {
		return "", nil, err
	}

	return token, user, nil
}

func (s *Service) Login(ctx context.Context, email string, password string) (string, *domain.User, error) {
	user, err := s.repo.GetUserByEmail(ctx, email)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return "", nil, domain.ErrInvalidCredentials
		}
		return "", nil, err
	}

	if err := CheckPassword(password, user.PasswordHash); err != nil {
		return "", nil, domain.ErrInvalidCredentials
	}

	token, err := s.jwt.GenerateToken(user.ID)
	if err != nil {
		return "", nil, err
	}

	return token, user, nil
}

func (s *Service) Me(ctx context.Context, userID string) (*domain.User, error) {
	user, err := s.repo.GetUserByID(ctx, userID)
	if err != nil {
		return nil, err
	}
	return user, nil
}

func (s *Service) ParseToken(token string) (string, error) {
	return s.jwt.ParseToken(token)
}