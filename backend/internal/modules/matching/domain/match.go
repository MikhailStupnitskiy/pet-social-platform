package domain

import "time"

type Match struct {
	ID        string
	Pet1ID    string
	Pet2ID    string
	CreatedAt time.Time
}
