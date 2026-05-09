package postgres

import (
	"context"
	"errors"

	"pet-social-platform/backend/internal/modules/feed/domain"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Repository struct {
	db *pgxpool.Pool
}

func New(db *pgxpool.Pool) *Repository {
	return &Repository{db: db}
}

func (r *Repository) IsPetOwnedByUser(ctx context.Context, petID string, userID string) (bool, error) {
	const query = `
		SELECT EXISTS (
			SELECT 1
			FROM pets
			WHERE id = $1 AND owner_id = $2
		)
	`

	var exists bool
	err := r.db.QueryRow(ctx, query, petID, userID).Scan(&exists)
	return exists, err
}

func (r *Repository) Create(
	ctx context.Context,
	authorUserID string,
	petID string,
	body string,
	imageURL *string,
) (*domain.Post, error) {
	const query = `
		INSERT INTO social_posts (
			author_user_id,
			pet_id,
			body,
			image_url
		)
		VALUES ($1, $2, $3, $4)
		RETURNING id
	`

	var id string
	if err := r.db.QueryRow(ctx, query, authorUserID, petID, body, imageURL).Scan(&id); err != nil {
		return nil, err
	}

	return r.GetByID(ctx, id)
}

func (r *Repository) List(ctx context.Context, limit int) ([]domain.Post, error) {
	const query = `
		SELECT
			sp.id,
			sp.author_user_id,
			COALESCE(up.name, ''),
			sp.pet_id,
			p.name,
			p.species,
			sp.body,
			sp.image_url,
			sp.created_at,
			sp.updated_at
		FROM social_posts sp
		JOIN pets p ON p.id = sp.pet_id
		LEFT JOIN user_profiles up ON up.user_id = sp.author_user_id
		ORDER BY sp.created_at DESC, sp.id DESC
		LIMIT $1
	`

	rows, err := r.db.Query(ctx, query, limit)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	posts := make([]domain.Post, 0)
	for rows.Next() {
		post, err := scanPost(rows)
		if err != nil {
			return nil, err
		}

		posts = append(posts, post)
	}

	return posts, rows.Err()
}

func (r *Repository) GetByID(ctx context.Context, id string) (*domain.Post, error) {
	const query = `
		SELECT
			sp.id,
			sp.author_user_id,
			COALESCE(up.name, ''),
			sp.pet_id,
			p.name,
			p.species,
			sp.body,
			sp.image_url,
			sp.created_at,
			sp.updated_at
		FROM social_posts sp
		JOIN pets p ON p.id = sp.pet_id
		LEFT JOIN user_profiles up ON up.user_id = sp.author_user_id
		WHERE sp.id = $1
	`

	post, err := scanPost(r.db.QueryRow(ctx, query, id))
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrPostNotFound
		}
		return nil, err
	}

	return &post, nil
}

func (r *Repository) Update(
	ctx context.Context,
	postID string,
	authorUserID string,
	body string,
	imageURL *string,
) (*domain.Post, error) {
	const query = `
		UPDATE social_posts
		SET
			body = $3,
			image_url = $4,
			updated_at = NOW()
		WHERE id = $1 AND author_user_id = $2
		RETURNING id
	`

	var id string
	err := r.db.QueryRow(ctx, query, postID, authorUserID, body, imageURL).Scan(&id)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			if exists, existsErr := r.postExists(ctx, postID); existsErr != nil {
				return nil, existsErr
			} else if exists {
				return nil, domain.ErrPostAccessDenied
			}

			return nil, domain.ErrPostNotFound
		}
		return nil, err
	}

	return r.GetByID(ctx, id)
}

func (r *Repository) Delete(ctx context.Context, postID string, authorUserID string) error {
	const query = `
		DELETE FROM social_posts
		WHERE id = $1 AND author_user_id = $2
	`

	tag, err := r.db.Exec(ctx, query, postID, authorUserID)
	if err != nil {
		return err
	}
	if tag.RowsAffected() > 0 {
		return nil
	}

	exists, err := r.postExists(ctx, postID)
	if err != nil {
		return err
	}
	if exists {
		return domain.ErrPostAccessDenied
	}

	return domain.ErrPostNotFound
}

func (r *Repository) postExists(ctx context.Context, postID string) (bool, error) {
	const query = `
		SELECT EXISTS (
			SELECT 1
			FROM social_posts
			WHERE id = $1
		)
	`

	var exists bool
	err := r.db.QueryRow(ctx, query, postID).Scan(&exists)
	return exists, err
}

type postScanner interface {
	Scan(dest ...any) error
}

func scanPost(scanner postScanner) (domain.Post, error) {
	var post domain.Post

	err := scanner.Scan(
		&post.ID,
		&post.AuthorUserID,
		&post.AuthorName,
		&post.PetID,
		&post.PetName,
		&post.PetSpecies,
		&post.Body,
		&post.ImageURL,
		&post.CreatedAt,
		&post.UpdatedAt,
	)
	if err != nil {
		return domain.Post{}, err
	}

	return post, nil
}
