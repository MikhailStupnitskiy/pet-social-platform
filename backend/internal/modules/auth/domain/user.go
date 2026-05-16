package domain

import "time"

type User struct {
	ID           string
	Email        string
	PasswordHash string
	IsHandler    bool
	CreatedAt    time.Time
	UpdatedAt    time.Time
}
