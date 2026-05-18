package domain

import "errors"

var (
	ErrPetNotFound     = errors.New("pet not found")
	ErrInvalidSwipe    = errors.New("invalid swipe action")
	ErrInvalidGoal     = errors.New("invalid matching goal")
	ErrInvalidEvent    = errors.New("invalid matching event")
	ErrPetAccessDenied = errors.New("pet access denied")
	ErrSamePetSwipe    = errors.New("cannot swipe same pet")
)
