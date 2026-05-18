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
	PeerUserID       *string
	PeerPetID        *string
	Source           string
	PeerName         *string
	OwnerName        *string
	PetName          *string
	ServiceTitle     *string
	Title            string
	Subtitle         string
	AvatarURL        *string
	LastMessage      *string
	LastMessageAt    *time.Time
	UnreadCount      int
	IsNewMatch       bool
	CreatedAt        time.Time
}
