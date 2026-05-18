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
	CompatibilityScore int
	ScoreReasons       []string
	Goal               string
	BehaviorScore      int
	IsActive           bool
	CreatedAt          time.Time
	UpdatedAt          time.Time
}

type RecommendationFilters struct {
	Goal              string
	MaxDistanceMeters *int
	Species           *string
	Breed             *string
	Sex               *string
	AgeMinMonths      *int
	AgeMaxMonths      *int
	Tags              []string
	Interests         []string
	HasPhoto          *bool
}

type MatchingEvent struct {
	SourcePetID string
	TargetPetID string
	EventType   string
}
