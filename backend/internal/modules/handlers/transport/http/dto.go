package http

type UpsertHandlerProfileRequest struct {
	DisplayName     string  `json:"display_name"`
	City            *string `json:"city"`
	Bio             *string `json:"bio"`
	AvatarURL       *string `json:"avatar_url"`
	ExperienceYears int     `json:"experience_years"`
	Conditions      *string `json:"conditions"`
	IsActive        bool    `json:"is_active"`
	Latitude        *string `json:"latitude"`
	Longitude       *string `json:"longitude"`
}

type HandlerProfileResponse struct {
	UserID          string                   `json:"user_id"`
	DisplayName     string                   `json:"display_name"`
	City            *string                  `json:"city,omitempty"`
	Bio             *string                  `json:"bio,omitempty"`
	AvatarURL       *string                  `json:"avatar_url,omitempty"`
	ExperienceYears int                      `json:"experience_years"`
	Conditions      *string                  `json:"conditions,omitempty"`
	IsActive        bool                     `json:"is_active"`
	Latitude        *string                  `json:"latitude,omitempty"`
	Longitude       *string                  `json:"longitude,omitempty"`
	RatingAvg       float64                  `json:"rating_avg"`
	ReviewsCount    int                      `json:"reviews_count"`
	Services        []HandlerServiceResponse `json:"services"`
	CreatedAt       string                   `json:"created_at"`
	UpdatedAt       string                   `json:"updated_at"`
}

type UpsertHandlerServiceRequest struct {
	ServiceType     string  `json:"service_type"`
	Title           string  `json:"title"`
	Description     *string `json:"description"`
	PriceCents      int     `json:"price_cents"`
	DurationMinutes *int    `json:"duration_minutes"`
	IsActive        bool    `json:"is_active"`
}

type HandlerServiceResponse struct {
	ID              string  `json:"id"`
	HandlerUserID   string  `json:"handler_user_id"`
	ServiceType     string  `json:"service_type"`
	Title           string  `json:"title"`
	Description     *string `json:"description,omitempty"`
	PriceCents      int     `json:"price_cents"`
	DurationMinutes *int    `json:"duration_minutes,omitempty"`
	IsActive        bool    `json:"is_active"`
	CreatedAt       string  `json:"created_at"`
	UpdatedAt       string  `json:"updated_at"`
}

type CreateServiceRequestRequest struct {
	ServiceID     string  `json:"service_id"`
	PetID         string  `json:"pet_id"`
	RequestedDate string  `json:"requested_date"`
	RequestedTime string  `json:"requested_time"`
	Comment       *string `json:"comment"`
}

type UpdateServiceRequestStatusRequest struct {
	Status string `json:"status"`
}

type ServiceRequestResponse struct {
	ID            string                 `json:"id"`
	ServiceID     string                 `json:"service_id"`
	ClientUserID  string                 `json:"client_user_id"`
	ClientName    string                 `json:"client_name"`
	HandlerUserID string                 `json:"handler_user_id"`
	HandlerName   string                 `json:"handler_name"`
	PetID         string                 `json:"pet_id"`
	PetName       string                 `json:"pet_name"`
	ServiceType   string                 `json:"service_type"`
	ServiceTitle  string                 `json:"service_title"`
	RequestedDate string                 `json:"requested_date"`
	RequestedTime string                 `json:"requested_time"`
	Comment       *string                `json:"comment,omitempty"`
	Status        string                 `json:"status"`
	Review        *HandlerReviewResponse `json:"review,omitempty"`
	CreatedAt     string                 `json:"created_at"`
	UpdatedAt     string                 `json:"updated_at"`
}

type CreateHandlerReviewRequest struct {
	Rating int     `json:"rating"`
	Body   *string `json:"body"`
}

type HandlerReviewResponse struct {
	ID            string  `json:"id"`
	RequestID     string  `json:"request_id"`
	HandlerUserID string  `json:"handler_user_id"`
	ClientUserID  string  `json:"client_user_id"`
	ClientName    string  `json:"client_name"`
	Rating        int     `json:"rating"`
	Body          *string `json:"body,omitempty"`
	CreatedAt     string  `json:"created_at"`
}
