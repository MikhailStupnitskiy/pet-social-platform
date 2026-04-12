package domain

import "time"

type Message struct {
	ID           string
	ChatID        string
	SenderUserID  string
	Body          string
	CreatedAt     time.Time
}