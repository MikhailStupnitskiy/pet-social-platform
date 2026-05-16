package domain

import "errors"

var (
	ErrChatNotFound   = errors.New("chat not found")
	ErrAccessDenied   = errors.New("chat access denied")
	ErrInvalidMessage = errors.New("invalid message")
)
