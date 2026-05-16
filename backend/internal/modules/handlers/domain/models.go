package domain

import "time"

const (
	ServiceTypeWalking  = "walking"
	ServiceTypeSitting  = "sitting"
	ServiceTypeTraining = "training"
	ServiceTypeGrooming = "grooming"
	ServiceTypeOther    = "other"

	RequestStatusPending   = "pending"
	RequestStatusAccepted  = "accepted"
	RequestStatusRejected  = "rejected"
	RequestStatusCancelled = "cancelled"
	RequestStatusCompleted = "completed"
)

type HandlerProfile struct {
	UserID          string
	DisplayName     string
	City            *string
	Bio             *string
	ExperienceYears int
	Conditions      *string
	IsActive        bool
	RatingAvg       float64
	ReviewsCount    int
	Services        []HandlerService
	CreatedAt       time.Time
	UpdatedAt       time.Time
}

type HandlerService struct {
	ID              string
	HandlerUserID   string
	ServiceType     string
	Title           string
	Description     *string
	PriceCents      int
	DurationMinutes *int
	IsActive        bool
	CreatedAt       time.Time
	UpdatedAt       time.Time
}

type ServiceRequest struct {
	ID            string
	ServiceID     string
	ClientUserID  string
	ClientName    string
	HandlerUserID string
	HandlerName   string
	PetID         string
	PetName       string
	ServiceType   string
	ServiceTitle  string
	RequestedDate string
	RequestedTime string
	Comment       *string
	Status        string
	Review        *HandlerReview
	CreatedAt     time.Time
	UpdatedAt     time.Time
}

type HandlerReview struct {
	ID            string
	RequestID     string
	HandlerUserID string
	ClientUserID  string
	ClientName    string
	Rating        int
	Body          *string
	CreatedAt     time.Time
}
