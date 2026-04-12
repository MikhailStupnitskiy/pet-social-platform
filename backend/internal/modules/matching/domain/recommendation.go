package domain

import "time"

type Recommendation struct {
	ID        string
	OwnerID   string
	Name      string
	Species   string
	Breed     *string
	Sex       *string
	BirthDate *time.Time
	WeightKg  *string
	Bio       *string
	IsActive  bool
	CreatedAt time.Time
	UpdatedAt time.Time
}