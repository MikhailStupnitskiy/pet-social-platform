package domain

import "time"

type Pet struct {
	ID                 string
	OwnerID            string
	Name               string
	Species            string
	Breed              *string
	Sex                *string
	BirthDate          *time.Time
	WeightKg           *string
	Bio                *string
	PhotoURL           *string
	PersonalityTags    []string
	Interests          []string
	HealthNotes        *string
	MatchingGoal       *string
	SearchRadiusMeters int
	Latitude           *string
	Longitude          *string
	IsActive           bool
	CreatedAt          time.Time
	UpdatedAt          time.Time
}

type PublicPetProfile struct {
	ID              string
	Owner           PublicOwnerSummary
	Name            string
	Species         string
	Breed           *string
	Sex             *string
	BirthDate       *time.Time
	WeightKg        *string
	Bio             *string
	PhotoURL        *string
	PersonalityTags []string
	Interests       []string
	MatchingGoal    *string
	IsActive        bool
	CreatedAt       time.Time
	UpdatedAt       time.Time
}

type PublicOwnerSummary struct {
	UserID    string
	Name      string
	City      *string
	Bio       *string
	AvatarURL *string
}
