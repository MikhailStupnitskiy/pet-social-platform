package domain

import "time"

type RoutineItem struct {
	ID           string
	PetID        string
	Title        string
	Category     string
	ScheduleTime *string
	Notes        *string
	IsEnabled    bool
	CreatedAt    time.Time
	UpdatedAt    time.Time
}