package domain

import "time"

type Post struct {
	ID             string
	AuthorUserID   string
	AuthorName     string
	PetID          string
	PetName        string
	PetSpecies     string
	Body           string
	ImageURL       *string
	CommentsCount  int
	ReactionsCount int
	MyReaction     *string
	ReactionCounts map[string]int
	CreatedAt      time.Time
	UpdatedAt      time.Time
}

type Comment struct {
	ID           string
	PostID       string
	AuthorUserID string
	AuthorName   string
	Body         string
	CreatedAt    time.Time
	UpdatedAt    time.Time
}
