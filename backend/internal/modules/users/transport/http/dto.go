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