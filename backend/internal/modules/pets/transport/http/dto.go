package http

type CreatePetRequest struct {
	Name               string   `json:"name"`
	Species            string   `json:"species"`
	Breed              *string  `json:"breed"`
	Sex                *string  `json:"sex"`
	BirthDate          *string  `json:"birth_date"`
	WeightKg           *string  `json:"weight_kg"`
	Bio                *string  `json:"bio"`
	PhotoURL           *string  `json:"photo_url"`
	PersonalityTags    []string `json:"personality_tags"`
	Interests          []string `json:"interests"`
	HealthNotes        *string  `json:"health_notes"`
	MatchingGoal       *string  `json:"matching_goal"`
	SearchRadiusMeters *int     `json:"search_radius_meters"`
	Latitude           *string  `json:"latitude"`
	Longitude          *string  `json:"longitude"`
}

type UpdatePetRequest struct {
	Name               string   `json:"name"`
	Species            string   `json:"species"`
	Breed              *string  `json:"breed"`
	Sex                *string  `json:"sex"`
	BirthDate          *string  `json:"birth_date"`
	WeightKg           *string  `json:"weight_kg"`
	Bio                *string  `json:"bio"`
	PhotoURL           *string  `json:"photo_url"`
	PersonalityTags    []string `json:"personality_tags"`
	Interests          []string `json:"interests"`
	HealthNotes        *string  `json:"health_notes"`
	MatchingGoal       *string  `json:"matching_goal"`
	SearchRadiusMeters *int     `json:"search_radius_meters"`
	Latitude           *string  `json:"latitude"`
	Longitude          *string  `json:"longitude"`
}

type PetResponse struct {
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
	IsActive           bool     `json:"is_active"`
	CreatedAt          string   `json:"created_at"`
	UpdatedAt          string   `json:"updated_at"`
}
