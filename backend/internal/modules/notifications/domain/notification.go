package domain

import "time"

const (
	TypeMatchCreated                = "match_created"
	TypeMessageReceived             = "message_received"
	TypeServiceRequestCreated       = "service_request_created"
	TypeServiceRequestStatusChanged = "service_request_status_changed"
)

type Notification struct {
	ID         string
	UserID     string
	Type       string
	Title      string
	Body       string
	EntityType *string
	EntityID   *string
	Metadata   map[string]string
	ReadAt     *time.Time
	DedupeKey  *string
	CreatedAt  time.Time
}

type CreateNotification struct {
	UserID     string
	Type       string
	Title      string
	Body       string
	EntityType *string
	EntityID   *string
	Metadata   map[string]string
	DedupeKey  *string
}
