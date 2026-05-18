package domain

import "time"

type Profile struct {
	UserID    string
	Email     string
	Name      string
	BirthDate *time.Time
	City      *string
	Bio       *string
	AvatarURL *string
	CreatedAt time.Time
	UpdatedAt time.Time
}

type ProfileStats struct {
	PetsCount    int
	MatchesCount int
	PostsCount   int
}

type PublicProfile struct {
	UserID    string
	Name      string
	BirthDate *time.Time
	City      *string
	Bio       *string
	AvatarURL *string
	Stats     ProfileStats
	Pets      []PublicPetSummary
	Handler   *PublicHandlerProfile
	CreatedAt time.Time
	UpdatedAt time.Time
}

type PublicPetSummary struct {
	ID              string
	Name            string
	Species         string
	Breed           *string
	Sex             *string
	BirthDate       *time.Time
	Bio             *string
	PhotoURL        *string
	PersonalityTags []string
	Interests       []string
	MatchingGoal    *string
	IsActive        bool
	CreatedAt       time.Time
	UpdatedAt       time.Time
}

type PublicHandlerProfile struct {
	UserID          string
	DisplayName     string
	City            *string
	Bio             *string
	AvatarURL       *string
	ExperienceYears int
	Conditions      *string
	RatingAvg       float64
	ReviewsCount    int
	Services        []PublicHandlerService
}

type PublicHandlerService struct {
	ID              string
	ServiceType     string
	Title           string
	Description     *string
	PriceCents      int
	DurationMinutes *int
}
