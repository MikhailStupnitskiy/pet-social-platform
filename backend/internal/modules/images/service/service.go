package service

import (
	"bytes"
	"context"
	"io"
	"net/http"
	"strings"

	"pet-social-platform/backend/internal/modules/images/domain"

	"github.com/google/uuid"
)

const MaxImageBytes int64 = 5 * 1024 * 1024

type Service struct {
	storage domain.Storage
}

type UploadInput struct {
	Content     io.Reader
	Size        int64
	ContentType string
	Filename    string
}

func New(storage domain.Storage) *Service {
	return &Service{storage: storage}
}

func (s *Service) Upload(ctx context.Context, input UploadInput) (string, error) {
	if input.Content == nil {
		return "", domain.ErrFileRequired
	}
	if input.Size <= 0 {
		return "", domain.ErrFileRequired
	}
	if input.Size > MaxImageBytes {
		return "", domain.ErrFileTooLarge
	}

	buffer := make([]byte, 512)
	n, err := io.ReadFull(input.Content, buffer)
	if err != nil && err != io.ErrUnexpectedEOF && err != io.EOF {
		return "", err
	}
	buffer = buffer[:n]

	contentType, extension, ok := detectImageType(buffer)
	if !ok {
		return "", domain.ErrUnsupportedType
	}

	key := uuid.NewString() + extension
	reader := io.MultiReader(bytes.NewReader(buffer), input.Content)
	if err := s.storage.Put(ctx, key, reader, input.Size, contentType); err != nil {
		return "", err
	}

	return "/v1/images/" + key, nil
}

func (s *Service) Get(ctx context.Context, key string) (*domain.Object, error) {
	key = strings.TrimSpace(key)
	if key == "" {
		return nil, domain.ErrImageNotFound
	}

	return s.storage.Get(ctx, key)
}

func detectImageType(sniffed []byte) (string, string, bool) {
	contentType := http.DetectContentType(sniffed)
	if isWebP(sniffed) {
		contentType = "image/webp"
	}

	switch contentType {
	case "image/jpeg":
		return contentType, ".jpg", true
	case "image/png":
		return contentType, ".png", true
	case "image/webp":
		return contentType, ".webp", true
	default:
		return "", "", false
	}
}

func isWebP(value []byte) bool {
	return len(value) >= 12 &&
		string(value[0:4]) == "RIFF" &&
		string(value[8:12]) == "WEBP"
}
