package domain

import "time"

type Chat struct {
	ID        string
	MatchID   string
	Pet1ID    string
	Pet2ID    string
	CreatedAt time.Time
}