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
	Title            string
	Subtitle         string
	AvatarURL        *string
	LastMessage      *string
	LastMessageAt    *time.Time
	UnreadCount      int
	IsNewMatch       bool
	CreatedAt        time.Time
}
