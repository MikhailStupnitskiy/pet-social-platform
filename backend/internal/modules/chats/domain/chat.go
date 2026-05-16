package domain

import "time"

type Chat struct {
	ID               string
	MatchID          *string
	ServiceRequestID *string
	Pet1ID           string
	Pet2ID           *string
	ClientUserID     *string
	HandlerUserID    *string
	CreatedAt        time.Time
}
