package postgres

import (
	"context"
	"errors"

	"pet-social-platform/backend/internal/modules/users/domain"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Repository struct {
	db *pgxpool.Pool
}

func New(db *pgxpool.Pool) *Repository {
	return &Repository{db: db}
}

func (r *Repository) GetProfileByUserID(ctx context.Context, userID string) (*domain.Profile, error) {
	const query = `
		SELECT 
			u.id,
			u.email,
			COALESCE(up.name, ''),
			up.birth_date,
			up.city,
			up.bio,
			up.avatar_url,
			COALESCE(up.created_at, u.created_at),
			COALESCE(up.updated_at, u.updated_at)
		FROM users u
		LEFT JOIN user_profiles up ON up.user_id = u.id
		WHERE u.id = $1
	`

	var profile domain.Profile

	err := r.db.QueryRow(ctx, query, userID).Scan(
		&profile.UserID,
		&profile.Email,
		&profile.Name,
		&profile.BirthDate,
		&profile.City,
		&profile.Bio,
		&profile.AvatarURL,
		&profile.CreatedAt,
		&profile.UpdatedAt,
	)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrProfileNotFound
		}
		return nil, err
	}

	return &profile, nil
}

func (r *Repository) GetProfileStats(ctx context.Context, userID string) (*domain.ProfileStats, error) {
	const query = `
		SELECT
			COUNT(DISTINCT p.id)::integer AS pets_count,
			COUNT(DISTINCT m.id)::integer AS matches_count,
			COUNT(DISTINCT sp.id)::integer AS posts_count
		FROM users u
		LEFT JOIN pets p ON p.owner_id = u.id
		LEFT JOIN matches m ON m.pet1_id = p.id OR m.pet2_id = p.id
		LEFT JOIN social_posts sp ON sp.author_user_id = u.id
		WHERE u.id = $1
	`

	var stats domain.ProfileStats
	err := r.db.QueryRow(ctx, query, userID).Scan(
		&stats.PetsCount,
		&stats.MatchesCount,
		&stats.PostsCount,
	)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrProfileNotFound
		}
		return nil, err
	}

	return &stats, nil
}

func (r *Repository) UpsertProfile(
	ctx context.Context,
	userID string,
	name string,
	birthDate *string,
	city *string,
	bio *string,
	avatarURL *string,
) (*domain.Profile, error) {
	const query = `
		INSERT INTO user_profiles (
			user_id,
			name,
			birth_date,
			city,
			bio,
			avatar_url
		)
		VALUES ($1, $2, $3, $4, $5, $6)
		ON CONFLICT (user_id)
		DO UPDATE SET
			name = EXCLUDED.name,
			birth_date = EXCLUDED.birth_date,
			city = EXCLUDED.city,
			bio = EXCLUDED.bio,
			avatar_url = EXCLUDED.avatar_url,
			updated_at = NOW()
		RETURNING
			user_id,
			name,
			birth_date,
			city,
			bio,
			avatar_url,
			created_at,
			updated_at
	`

	var profile domain.Profile

	err := r.db.QueryRow(ctx, query, userID, name, birthDate, city, bio, avatarURL).Scan(
		&profile.UserID,
		&profile.Name,
		&profile.BirthDate,
		&profile.City,
		&profile.Bio,
		&profile.AvatarURL,
		&profile.CreatedAt,
		&profile.UpdatedAt,
	)
	if err != nil {
		return nil, err
	}

	const emailQuery = `SELECT email FROM users WHERE id = $1`
	err = r.db.QueryRow(ctx, emailQuery, userID).Scan(&profile.Email)
	if err != nil {
		return nil, err
	}

	return &profile, nil
}
