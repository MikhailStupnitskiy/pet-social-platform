package domain

import "errors"

var (
	ErrNotificationNotFound = errors.New("notification not found")
	ErrAccessDenied         = errors.New("notification access denied")
	ErrInvalidNotification  = errors.New("invalid notification")
)
