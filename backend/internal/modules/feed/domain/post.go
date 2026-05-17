package domain

import "time"

type Post struct {
	ID                 string
	AuthorUserID       string
	AuthorName         string
	AuthorEmail        string
	AuthorCity         *string
	AuthorBio          *string
	AuthorAvatarURL    *string
	PetID              string
	PetName            string
	PetSpecies         string
	PetBreed           *string
	PetSex             *string
	PetBirthDate       *time.Time
	PetBio             *string
	PetPhotoURL        *string
	PetPersonalityTags []string
	PetInterests       []string
	Body               string
	ImageURL           *string
	CommentsCount      int
	ReactionsCount     int
	MyReaction         *string
	ReactionCounts     map[string]int
	CreatedAt          time.Time
	UpdatedAt          time.Time
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
