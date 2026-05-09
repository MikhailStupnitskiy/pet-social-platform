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
	ID             string         `json:"id"`
	AuthorUserID   string         `json:"author_user_id"`
	AuthorName     string         `json:"author_name"`
	PetID          string         `json:"pet_id"`
	PetName        string         `json:"pet_name"`
	PetSpecies     string         `json:"pet_species"`
	Body           string         `json:"body"`
	ImageURL       *string        `json:"image_url,omitempty"`
	CommentsCount  int            `json:"comments_count"`
	ReactionsCount int            `json:"reactions_count"`
	MyReaction     *string        `json:"my_reaction,omitempty"`
	ReactionCounts map[string]int `json:"reaction_counts"`
	CreatedAt      string         `json:"created_at"`
	UpdatedAt      string         `json:"updated_at"`
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
	return PostResponse{
		ID:             post.ID,
		AuthorUserID:   post.AuthorUserID,
		AuthorName:     post.AuthorName,
		PetID:          post.PetID,
		PetName:        post.PetName,
		PetSpecies:     post.PetSpecies,
		Body:           post.Body,
		ImageURL:       post.ImageURL,
		CommentsCount:  post.CommentsCount,
		ReactionsCount: post.ReactionsCount,
		MyReaction:     post.MyReaction,
		ReactionCounts: post.ReactionCounts,
		CreatedAt:      post.CreatedAt.Format(time.RFC3339),
		UpdatedAt:      post.UpdatedAt.Format(time.RFC3339),
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
