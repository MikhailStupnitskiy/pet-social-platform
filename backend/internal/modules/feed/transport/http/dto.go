package http

import (
	"time"

	"pet-social-platform/backend/internal/modules/feed/domain"
)

type CreatePostRequest struct {
	PetID    string  `json:"pet_id"`
	Body     string  `json:"body"`
	ImageURL *string `json:"image_url"`
}

type UpdatePostRequest struct {
	Body     string  `json:"body"`
	ImageURL *string `json:"image_url"`
}

type CreateCommentRequest struct {
	Body string `json:"body"`
}

type UpdateCommentRequest struct {
	Body string `json:"body"`
}

type SetReactionRequest struct {
	ReactionType string `json:"reaction_type"`
}

type PostResponse struct {
	ID                 string         `json:"id"`
	AuthorUserID       string         `json:"author_user_id"`
	AuthorName         string         `json:"author_name"`
	AuthorEmail        string         `json:"author_email"`
	AuthorCity         *string        `json:"author_city,omitempty"`
	AuthorBio          *string        `json:"author_bio,omitempty"`
	AuthorAvatarURL    *string        `json:"author_avatar_url,omitempty"`
	PetID              string         `json:"pet_id"`
	PetName            string         `json:"pet_name"`
	PetSpecies         string         `json:"pet_species"`
	PetBreed           *string        `json:"pet_breed,omitempty"`
	PetSex             *string        `json:"pet_sex,omitempty"`
	PetBirthDate       *string        `json:"pet_birth_date,omitempty"`
	PetBio             *string        `json:"pet_bio,omitempty"`
	PetPhotoURL        *string        `json:"pet_photo_url,omitempty"`
	PetPersonalityTags []string       `json:"pet_personality_tags"`
	PetInterests       []string       `json:"pet_interests"`
	Body               string         `json:"body"`
	ImageURL           *string        `json:"image_url,omitempty"`
	CommentsCount      int            `json:"comments_count"`
	ReactionsCount     int            `json:"reactions_count"`
	MyReaction         *string        `json:"my_reaction,omitempty"`
	ReactionCounts     map[string]int `json:"reaction_counts"`
	CreatedAt          string         `json:"created_at"`
	UpdatedAt          string         `json:"updated_at"`
}

type CommentResponse struct {
	ID           string `json:"id"`
	PostID       string `json:"post_id"`
	AuthorUserID string `json:"author_user_id"`
	AuthorName   string `json:"author_name"`
	Body         string `json:"body"`
	CreatedAt    string `json:"created_at"`
	UpdatedAt    string `json:"updated_at"`
}

func toPostResponse(post *domain.Post) PostResponse {
	var petBirthDate *string
	if post.PetBirthDate != nil {
		formatted := post.PetBirthDate.Format("2006-01-02")
		petBirthDate = &formatted
	}

	return PostResponse{
		ID:                 post.ID,
		AuthorUserID:       post.AuthorUserID,
		AuthorName:         post.AuthorName,
		AuthorEmail:        post.AuthorEmail,
		AuthorCity:         post.AuthorCity,
		AuthorBio:          post.AuthorBio,
		AuthorAvatarURL:    post.AuthorAvatarURL,
		PetID:              post.PetID,
		PetName:            post.PetName,
		PetSpecies:         post.PetSpecies,
		PetBreed:           post.PetBreed,
		PetSex:             post.PetSex,
		PetBirthDate:       petBirthDate,
		PetBio:             post.PetBio,
		PetPhotoURL:        post.PetPhotoURL,
		PetPersonalityTags: post.PetPersonalityTags,
		PetInterests:       post.PetInterests,
		Body:               post.Body,
		ImageURL:           post.ImageURL,
		CommentsCount:      post.CommentsCount,
		ReactionsCount:     post.ReactionsCount,
		MyReaction:         post.MyReaction,
		ReactionCounts:     post.ReactionCounts,
		CreatedAt:          post.CreatedAt.Format(time.RFC3339),
		UpdatedAt:          post.UpdatedAt.Format(time.RFC3339),
	}
}

func toPostResponses(posts []domain.Post) []PostResponse {
	responses := make([]PostResponse, 0, len(posts))
	for _, post := range posts {
		postCopy := post
		responses = append(responses, toPostResponse(&postCopy))
	}

	return responses
}

func toCommentResponse(comment *domain.Comment) CommentResponse {
	return CommentResponse{
		ID:           comment.ID,
		PostID:       comment.PostID,
		AuthorUserID: comment.AuthorUserID,
		AuthorName:   comment.AuthorName,
		Body:         comment.Body,
		CreatedAt:    comment.CreatedAt.Format(time.RFC3339),
		UpdatedAt:    comment.UpdatedAt.Format(time.RFC3339),
	}
}

func toCommentResponses(comments []domain.Comment) []CommentResponse {
	responses := make([]CommentResponse, 0, len(comments))
	for _, comment := range comments {
		commentCopy := comment
		responses = append(responses, toCommentResponse(&commentCopy))
	}

	return responses
}
