package domain

import "time"

type Post struct {
	ID           string
	AuthorUserID string
	AuthorName   string
	PetID        string
	PetName      string
	PetSpecies   string
	Body         string
	ImageURL     *string
	CreatedAt    time.Time
	UpdatedAt    time.Time
}
