package domain

import "time"

type Completion struct {
	ID            string
	RoutineItemID string
	CompletedAt   time.Time
}