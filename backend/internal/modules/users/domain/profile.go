package domain

import "time"

type Profile struct {
	UserID    string
	Email     string
	Name      string
	BirthDate *time.Time
	City      *string
	Bio       *string
	AvatarURL *string
	CreatedAt time.Time
	UpdatedAt time.Time
}