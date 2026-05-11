package domain

import "errors"

var (
	ErrFileRequired    = errors.New("image file is required")
	ErrFileTooLarge    = errors.New("image file is too large")
	ErrUnsupportedType = errors.New("unsupported image type")
	ErrImageNotFound   = errors.New("image not found")
)
