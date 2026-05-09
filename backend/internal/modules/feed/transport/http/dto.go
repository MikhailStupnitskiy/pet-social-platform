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

type PostResponse struct {
	ID           string  `json:"id"`
	AuthorUserID string  `json:"author_user_id"`
	AuthorName   string  `json:"author_name"`
	PetID        string  `json:"pet_id"`
	PetName      string  `json:"pet_name"`
	PetSpecies   string  `json:"pet_species"`
	Body         string  `json:"body"`
	ImageURL     *string `json:"image_url,omitempty"`
	CreatedAt    string  `json:"created_at"`
	UpdatedAt    string  `json:"updated_at"`
}

func toPostResponse(post *domain.Post) PostResponse {
	return PostResponse{
		ID:           post.ID,
		AuthorUserID: post.AuthorUserID,
		AuthorName:   post.AuthorName,
		PetID:        post.PetID,
		PetName:      post.PetName,
		PetSpecies:   post.PetSpecies,
		Body:         post.Body,
		ImageURL:     post.ImageURL,
		CreatedAt:    post.CreatedAt.Format(time.RFC3339),
		UpdatedAt:    post.UpdatedAt.Format(time.RFC3339),
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
