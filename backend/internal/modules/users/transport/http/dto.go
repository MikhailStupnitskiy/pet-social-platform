package http

type UpdateProfileRequest struct {
	Name      string  `json:"name"`
	BirthDate *string `json:"birth_date"`
	City      *string `json:"city"`
	Bio       *string `json:"bio"`
	AvatarURL *string `json:"avatar_url"`
}

type ProfileResponse struct {
	UserID    string  `json:"user_id"`
	Email     string  `json:"email"`
	Name      string  `json:"name"`
	BirthDate *string `json:"birth_date,omitempty"`
	City      *string `json:"city,omitempty"`
	Bio       *string `json:"bio,omitempty"`
	AvatarURL *string `json:"avatar_url,omitempty"`
	CreatedAt string  `json:"created_at"`
	UpdatedAt string  `json:"updated_at"`
}

type ProfileStatsResponse struct {
	PetsCount    int `json:"pets_count"`
	MatchesCount int `json:"matches_count"`
	PostsCount   int `json:"posts_count"`
}

type PublicProfileResponse struct {
	UserID    string                        `json:"user_id"`
	Name      string                        `json:"name"`
	BirthDate *string                       `json:"birth_date,omitempty"`
	City      *string                       `json:"city,omitempty"`
	Bio       *string                       `json:"bio,omitempty"`
	AvatarURL *string                       `json:"avatar_url,omitempty"`
	Stats     ProfileStatsResponse          `json:"stats"`
	Pets      []PublicPetSummaryResponse    `json:"pets"`
	Handler   *PublicHandlerProfileResponse `json:"handler,omitempty"`
	CreatedAt string                        `json:"created_at"`
	UpdatedAt string                        `json:"updated_at"`
}

type PublicPetSummaryResponse struct {
	ID              string   `json:"id"`
	Name            string   `json:"name"`
	Species         string   `json:"species"`
	Breed           *string  `json:"breed,omitempty"`
	Sex             *string  `json:"sex,omitempty"`
	BirthDate       *string  `json:"birth_date,omitempty"`
	Bio             *string  `json:"bio,omitempty"`
	PhotoURL        *string  `json:"photo_url,omitempty"`
	PersonalityTags []string `json:"personality_tags"`
	Interests       []string `json:"interests"`
	MatchingGoal    *string  `json:"matching_goal,omitempty"`
	IsActive        bool     `json:"is_active"`
	CreatedAt       string   `json:"created_at"`
	UpdatedAt       string   `json:"updated_at"`
}

type PublicHandlerProfileResponse struct {
	UserID          string                         `json:"user_id"`
	DisplayName     string                         `json:"display_name"`
	City            *string                        `json:"city,omitempty"`
	Bio             *string                        `json:"bio,omitempty"`
	AvatarURL       *string                        `json:"avatar_url,omitempty"`
	ExperienceYears int                            `json:"experience_years"`
	Conditions      *string                        `json:"conditions,omitempty"`
	RatingAvg       float64                        `json:"rating_avg"`
	ReviewsCount    int                            `json:"reviews_count"`
	Services        []PublicHandlerServiceResponse `json:"services"`
}

type PublicHandlerServiceResponse struct {
	ID              string  `json:"id"`
	ServiceType     string  `json:"service_type"`
	Title           string  `json:"title"`
	Description     *string `json:"description,omitempty"`
	PriceCents      int     `json:"price_cents"`
	DurationMinutes *int    `json:"duration_minutes,omitempty"`
}
