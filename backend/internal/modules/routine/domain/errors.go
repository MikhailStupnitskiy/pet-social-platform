package domain

import "errors"

var (
	ErrRoutineItemNotFound = errors.New("routine item not found")
	ErrPetAccessDenied     = errors.New("pet access denied")
	ErrInvalidRepeatRule   = errors.New("invalid repeat rule")
)
