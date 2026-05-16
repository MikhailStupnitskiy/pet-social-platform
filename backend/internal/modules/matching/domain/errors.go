package domain

import "errors"

var (
	ErrPetNotFound     = errors.New("pet not found")
	ErrInvalidSwipe    = errors.New("invalid swipe action")
	ErrPetAccessDenied = errors.New("pet access denied")
	ErrSamePetSwipe    = errors.New("cannot swipe same pet")
)
