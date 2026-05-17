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

	return r.GetByID(ctx, id, authorUserID)
}

func (r *Repository) List(ctx context.Context, userID string, limit int) ([]domain.Post, error) {
	const query = `
		SELECT
			sp.id,
			sp.author_user_id,
			COALESCE(up.name, ''),
			u.email,
			up.city,
			up.bio,
			up.avatar_url,
			sp.pet_id,
			p.name,
			p.species,
			p.breed,
			p.sex,
			p.birth_date,
			p.bio,
			p.photo_url,
			COALESCE(p.personality_tags, '{}'),
			COALESCE(p.interests, '{}'),
			sp.body,
			sp.image_url,
			COALESCE(cc.comments_count, 0),
			COALESCE(rc.reactions_count, 0),
			my_reaction.reaction_type,
			COALESCE(rc.like_count, 0),
			COALESCE(rc.love_count, 0),
			COALESCE(rc.funny_count, 0),
			COALESCE(rc.support_count, 0),
			sp.created_at,
			sp.updated_at
		FROM social_posts sp
		JOIN pets p ON p.id = sp.pet_id
		JOIN users u ON u.id = sp.author_user_id
		LEFT JOIN user_profiles up ON up.user_id = sp.author_user_id
		LEFT JOIN (
			SELECT post_id, COUNT(*) AS comments_count
			FROM social_post_comments
			GROUP BY post_id
		) cc ON cc.post_id = sp.id
		LEFT JOIN (
			SELECT
				post_id,
				COUNT(*) AS reactions_count,
				COUNT(*) FILTER (WHERE reaction_type = 'like') AS like_count,
				COUNT(*) FILTER (WHERE reaction_type = 'love') AS love_count,
				COUNT(*) FILTER (WHERE reaction_type = 'funny') AS funny_count,
				COUNT(*) FILTER (WHERE reaction_type = 'support') AS support_count
			FROM social_post_reactions
			GROUP BY post_id
		) rc ON rc.post_id = sp.id
		LEFT JOIN social_post_reactions my_reaction
			ON my_reaction.post_id = sp.id AND my_reaction.user_id = $2
		ORDER BY sp.created_at DESC, sp.id DESC
		LIMIT $1
	`

	rows, err := r.db.Query(ctx, query, limit, userID)
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

func (r *Repository) GetByID(ctx context.Context, id string, userID string) (*domain.Post, error) {
	const query = `
		SELECT
			sp.id,
			sp.author_user_id,
			COALESCE(up.name, ''),
			u.email,
			up.city,
			up.bio,
			up.avatar_url,
			sp.pet_id,
			p.name,
			p.species,
			p.breed,
			p.sex,
			p.birth_date,
			p.bio,
			p.photo_url,
			COALESCE(p.personality_tags, '{}'),
			COALESCE(p.interests, '{}'),
			sp.body,
			sp.image_url,
			COALESCE(cc.comments_count, 0),
			COALESCE(rc.reactions_count, 0),
			my_reaction.reaction_type,
			COALESCE(rc.like_count, 0),
			COALESCE(rc.love_count, 0),
			COALESCE(rc.funny_count, 0),
			COALESCE(rc.support_count, 0),
			sp.created_at,
			sp.updated_at
		FROM social_posts sp
		JOIN pets p ON p.id = sp.pet_id
		JOIN users u ON u.id = sp.author_user_id
		LEFT JOIN user_profiles up ON up.user_id = sp.author_user_id
		LEFT JOIN (
			SELECT post_id, COUNT(*) AS comments_count
			FROM social_post_comments
			GROUP BY post_id
		) cc ON cc.post_id = sp.id
		LEFT JOIN (
			SELECT
				post_id,
				COUNT(*) AS reactions_count,
				COUNT(*) FILTER (WHERE reaction_type = 'like') AS like_count,
				COUNT(*) FILTER (WHERE reaction_type = 'love') AS love_count,
				COUNT(*) FILTER (WHERE reaction_type = 'funny') AS funny_count,
				COUNT(*) FILTER (WHERE reaction_type = 'support') AS support_count
			FROM social_post_reactions
			GROUP BY post_id
		) rc ON rc.post_id = sp.id
		LEFT JOIN social_post_reactions my_reaction
			ON my_reaction.post_id = sp.id AND my_reaction.user_id = $2
		WHERE sp.id = $1
	`

	post, err := scanPost(r.db.QueryRow(ctx, query, id, userID))
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
			if exists, existsErr := r.PostExists(ctx, postID); existsErr != nil {
				return nil, existsErr
			} else if exists {
				return nil, domain.ErrPostAccessDenied
			}

			return nil, domain.ErrPostNotFound
		}
		return nil, err
	}

	return r.GetByID(ctx, id, authorUserID)
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

	exists, err := r.PostExists(ctx, postID)
	if err != nil {
		return err
	}
	if exists {
		return domain.ErrPostAccessDenied
	}

	return domain.ErrPostNotFound
}

func (r *Repository) PostExists(ctx context.Context, postID string) (bool, error) {
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

func (r *Repository) ListComments(ctx context.Context, postID string, limit int) ([]domain.Comment, error) {
	const query = `
		SELECT
			limited.id,
			limited.post_id,
			limited.author_user_id,
			COALESCE(up.name, ''),
			limited.body,
			limited.created_at,
			limited.updated_at
		FROM (
			SELECT
				id,
				post_id,
				author_user_id,
				body,
				created_at,
				updated_at
			FROM social_post_comments
			WHERE post_id = $1
			ORDER BY created_at DESC, id DESC
			LIMIT $2
		) limited
		LEFT JOIN user_profiles up ON up.user_id = limited.author_user_id
		ORDER BY limited.created_at ASC, limited.id ASC
	`

	rows, err := r.db.Query(ctx, query, postID, limit)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	comments := make([]domain.Comment, 0)
	for rows.Next() {
		comment, err := scanComment(rows)
		if err != nil {
			return nil, err
		}

		comments = append(comments, comment)
	}

	return comments, rows.Err()
}

func (r *Repository) CreateComment(ctx context.Context, postID string, authorUserID string, body string) (*domain.Comment, error) {
	const query = `
		INSERT INTO social_post_comments (
			post_id,
			author_user_id,
			body
		)
		VALUES ($1, $2, $3)
		RETURNING id
	`

	var id string
	if err := r.db.QueryRow(ctx, query, postID, authorUserID, body).Scan(&id); err != nil {
		return nil, err
	}

	return r.getCommentByID(ctx, postID, id)
}

func (r *Repository) UpdateComment(ctx context.Context, postID string, commentID string, authorUserID string, body string) (*domain.Comment, error) {
	const query = `
		UPDATE social_post_comments
		SET
			body = $4,
			updated_at = NOW()
		WHERE post_id = $1 AND id = $2 AND author_user_id = $3
		RETURNING id
	`

	var id string
	err := r.db.QueryRow(ctx, query, postID, commentID, authorUserID, body).Scan(&id)
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, r.commentMissingOrDenied(ctx, postID, commentID)
		}
		return nil, err
	}

	return r.getCommentByID(ctx, postID, id)
}

func (r *Repository) DeleteComment(ctx context.Context, postID string, commentID string, authorUserID string) error {
	const query = `
		DELETE FROM social_post_comments
		WHERE post_id = $1 AND id = $2 AND author_user_id = $3
	`

	tag, err := r.db.Exec(ctx, query, postID, commentID, authorUserID)
	if err != nil {
		return err
	}
	if tag.RowsAffected() > 0 {
		return nil
	}

	return r.commentMissingOrDenied(ctx, postID, commentID)
}

func (r *Repository) UpsertReaction(ctx context.Context, postID string, userID string, reactionType string) error {
	const query = `
		INSERT INTO social_post_reactions (
			post_id,
			user_id,
			reaction_type
		)
		VALUES ($1, $2, $3)
		ON CONFLICT ON CONSTRAINT social_post_reactions_unique_user
		DO UPDATE SET
			reaction_type = EXCLUDED.reaction_type,
			updated_at = NOW()
	`

	_, err := r.db.Exec(ctx, query, postID, userID, reactionType)
	return err
}

func (r *Repository) DeleteReaction(ctx context.Context, postID string, userID string) error {
	const query = `
		DELETE FROM social_post_reactions
		WHERE post_id = $1 AND user_id = $2
	`

	_, err := r.db.Exec(ctx, query, postID, userID)
	return err
}

func (r *Repository) getCommentByID(ctx context.Context, postID string, commentID string) (*domain.Comment, error) {
	const query = `
		SELECT
			spc.id,
			spc.post_id,
			spc.author_user_id,
			COALESCE(up.name, ''),
			spc.body,
			spc.created_at,
			spc.updated_at
		FROM social_post_comments spc
		LEFT JOIN user_profiles up ON up.user_id = spc.author_user_id
		WHERE spc.post_id = $1 AND spc.id = $2
	`

	comment, err := scanComment(r.db.QueryRow(ctx, query, postID, commentID))
	if err != nil {
		if errors.Is(err, pgx.ErrNoRows) {
			return nil, domain.ErrCommentNotFound
		}
		return nil, err
	}

	return &comment, nil
}

func (r *Repository) commentMissingOrDenied(ctx context.Context, postID string, commentID string) error {
	postExists, err := r.PostExists(ctx, postID)
	if err != nil {
		return err
	}
	if !postExists {
		return domain.ErrPostNotFound
	}

	commentExists, err := r.commentExists(ctx, postID, commentID)
	if err != nil {
		return err
	}
	if commentExists {
		return domain.ErrCommentAccessDenied
	}

	return domain.ErrCommentNotFound
}

func (r *Repository) commentExists(ctx context.Context, postID string, commentID string) (bool, error) {
	const query = `
		SELECT EXISTS (
			SELECT 1
			FROM social_post_comments
			WHERE post_id = $1 AND id = $2
		)
	`

	var exists bool
	err := r.db.QueryRow(ctx, query, postID, commentID).Scan(&exists)
	return exists, err
}

type postScanner interface {
	Scan(dest ...any) error
}

func scanPost(scanner postScanner) (domain.Post, error) {
	var post domain.Post
	var commentsCount int64
	var reactionsCount int64
	var likeCount int64
	var loveCount int64
	var funnyCount int64
	var supportCount int64

	err := scanner.Scan(
		&post.ID,
		&post.AuthorUserID,
		&post.AuthorName,
		&post.AuthorEmail,
		&post.AuthorCity,
		&post.AuthorBio,
		&post.AuthorAvatarURL,
		&post.PetID,
		&post.PetName,
		&post.PetSpecies,
		&post.PetBreed,
		&post.PetSex,
		&post.PetBirthDate,
		&post.PetBio,
		&post.PetPhotoURL,
		&post.PetPersonalityTags,
		&post.PetInterests,
		&post.Body,
		&post.ImageURL,
		&commentsCount,
		&reactionsCount,
		&post.MyReaction,
		&likeCount,
		&loveCount,
		&funnyCount,
		&supportCount,
		&post.CreatedAt,
		&post.UpdatedAt,
	)
	if err != nil {
		return domain.Post{}, err
	}

	post.CommentsCount = int(commentsCount)
	post.ReactionsCount = int(reactionsCount)
	post.ReactionCounts = map[string]int{
		"like":    int(likeCount),
		"love":    int(loveCount),
		"funny":   int(funnyCount),
		"support": int(supportCount),
	}
	post.PetPersonalityTags = normalizeList(post.PetPersonalityTags)
	post.PetInterests = normalizeList(post.PetInterests)

	return post, nil
}

func scanComment(scanner postScanner) (domain.Comment, error) {
	var comment domain.Comment

	err := scanner.Scan(
		&comment.ID,
		&comment.PostID,
		&comment.AuthorUserID,
		&comment.AuthorName,
		&comment.Body,
		&comment.CreatedAt,
		&comment.UpdatedAt,
	)
	if err != nil {
		return domain.Comment{}, err
	}

	return comment, nil
}

func normalizeList(values []string) []string {
	result := make([]string, 0, len(values))
	for _, value := range values {
		if value != "" {
			result = append(result, value)
		}
	}
	return result
}
