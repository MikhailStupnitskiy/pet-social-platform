package domain

import "time"

type Recommendation struct {
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
	DistanceMeters     *int
	IsActive           bool
	CreatedAt          time.Time
	UpdatedAt          time.Time
}
