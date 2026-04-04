package postgres

import (
	"context"
	"errors"

	"pet-social-platform/backend/internal/modules/auth/domain"

	"github.com/jackc/pgx/v5/pgconn"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Repository struct {
	db *pgxpool.Pool
}

func New(db *pgxpool.Pool) *Repository {
	return &Repository{db: db}
}

func (r *Repository) CreateUser(ctx context.Context, email string, passwordHash string) (*domain.User, error) {
	const query = `
		INSERT INTO users (email, password_hash)
		VALUES ($1, $2)
		RETURNING id, email, password_hash, created_at, updated_at
	`

	var user domain.User

	err := r.db.QueryRow(ctx, query, email, passwordHash).Scan(
		&user.ID,
		&user.Email,
		&user.PasswordHash,
		&user.CreatedAt,
		&user.UpdatedAt,
	)
	if err != nil {
		var pgErr *pgconn.PgError
		if errors.As(err, &pgErr) && pgErr.Code == "23505" {
			return nil, domain.ErrUserAlreadyExists
		}
		return nil, err
	}

	return &user, nil
}

func (r *Repository) GetUserByEmail(ctx context.Context, email string) (*domain.User, error) {
	const query = `
		SELECT id, email, password_hash, created_at, updated_at
		FROM users
		WHERE email = $1
	`

	var user domain.User

	err := r.db.QueryRow(ctx, query, email).Scan(
		&user.ID,
		&user.Email,
		&user.PasswordHash,
		&user.CreatedAt,
		&user.UpdatedAt,
	)
	if err != nil {
		return nil, err
	}

	return &user, nil
}

func (r *Repository) GetUserByID(ctx context.Context, userID string) (*domain.User, error) {
	const query = `
		SELECT id, email, password_hash, created_at, updated_at
		FROM users
		WHERE id = $1
	`

	var user domain.User

	err := r.db.QueryRow(ctx, query, userID).Scan(
		&user.ID,
		&user.Email,
		&user.PasswordHash,
		&user.CreatedAt,
		&user.UpdatedAt,
	)
	if err != nil {
		return nil, err
	}

	return &user, nil
}