package http

type SwipeRequest struct {
	SourcePetID string `json:"source_pet_id"`
	TargetPetID string `json:"target_pet_id"`
	Action      string `json:"action"`
}

type MatchingEventRequest struct {
	SourcePetID string `json:"source_pet_id"`
	TargetPetID string `json:"target_pet_id"`
	EventType   string `json:"event_type"`
}

type RecommendationResponse struct {
	ID                 string   `json:"id"`
	OwnerID            string   `json:"owner_id"`
	Name               string   `json:"name"`
	Species            string   `json:"species"`
	Breed              *string  `json:"breed,omitempty"`
	Sex                *string  `json:"sex,omitempty"`
	BirthDate          *string  `json:"birth_date,omitempty"`
	WeightKg           *string  `json:"weight_kg,omitempty"`
	Bio                *string  `json:"bio,omitempty"`
	PhotoURL           *string  `json:"photo_url,omitempty"`
	PersonalityTags    []string `json:"personality_tags"`
	Interests          []string `json:"interests"`
	HealthNotes        *string  `json:"health_notes,omitempty"`
	MatchingGoal       *string  `json:"matching_goal,omitempty"`
	SearchRadiusMeters int      `json:"search_radius_meters"`
	Latitude           *string  `json:"latitude,omitempty"`
	Longitude          *string  `json:"longitude,omitempty"`
	DistanceMeters     *int     `json:"distance_meters,omitempty"`
	CompatibilityScore int      `json:"compatibility_score"`
	ScoreReasons       []string `json:"score_reasons"`
	Goal               string   `json:"goal"`
	IsActive           bool     `json:"is_active"`
	CreatedAt          string   `json:"created_at"`
	UpdatedAt          string   `json:"updated_at"`
}

type MatchResponse struct {
	ID        string `json:"id"`
	Pet1ID    string `json:"pet1_id"`
	Pet2ID    string `json:"pet2_id"`
	CreatedAt string `json:"created_at"`
}
