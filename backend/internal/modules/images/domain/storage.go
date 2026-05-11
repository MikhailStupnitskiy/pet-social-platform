package domain

import (
	"context"
	"io"
)

type Object struct {
	Content     io.ReadCloser
	ContentType string
	Size        int64
}

type Storage interface {
	Put(ctx context.Context, key string, content io.Reader, size int64, contentType string) error
	Get(ctx context.Context, key string) (*Object, error)
}
