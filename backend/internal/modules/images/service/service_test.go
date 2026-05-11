package service

import (
	"bytes"
	"context"
	"errors"
	"io"
	"testing"

	"pet-social-platform/backend/internal/modules/images/domain"
)

func TestUploadStoresValidPNG(t *testing.T) {
	storage := &fakeStorage{}
	svc := New(storage)
	content := []byte{0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n', 0, 0, 0, 0}

	imageURL, err := svc.Upload(context.Background(), UploadInput{
		Content:     bytes.NewReader(content),
		Size:        int64(len(content)),
		ContentType: "image/png",
		Filename:    "pet.png",
	})
	if err != nil {
		t.Fatalf("expected no error, got %v", err)
	}
	if imageURL == "" {
		t.Fatal("expected image url")
	}
	if storage.contentType != "image/png" {
		t.Fatalf("expected image/png, got %q", storage.contentType)
	}
	if !bytes.Equal(storage.content, content) {
		t.Fatal("stored content does not match input")
	}
}

func TestUploadRejectsMissingFile(t *testing.T) {
	svc := New(&fakeStorage{})

	if _, err := svc.Upload(context.Background(), UploadInput{}); !errors.Is(err, domain.ErrFileRequired) {
		t.Fatalf("expected ErrFileRequired, got %v", err)
	}
}

func TestUploadRejectsLargeFile(t *testing.T) {
	svc := New(&fakeStorage{})

	if _, err := svc.Upload(context.Background(), UploadInput{
		Content: bytes.NewReader([]byte{1}),
		Size:    MaxImageBytes + 1,
	}); !errors.Is(err, domain.ErrFileTooLarge) {
		t.Fatalf("expected ErrFileTooLarge, got %v", err)
	}
}

func TestUploadRejectsUnsupportedType(t *testing.T) {
	svc := New(&fakeStorage{})

	if _, err := svc.Upload(context.Background(), UploadInput{
		Content:     bytes.NewReader([]byte("plain text")),
		Size:        int64(len("plain text")),
		ContentType: "text/plain",
		Filename:    "pet.txt",
	}); !errors.Is(err, domain.ErrUnsupportedType) {
		t.Fatalf("expected ErrUnsupportedType, got %v", err)
	}
}

type fakeStorage struct {
	key         string
	content     []byte
	contentType string
}

func (f *fakeStorage) Put(_ context.Context, key string, content io.Reader, _ int64, contentType string) error {
	f.key = key
	f.contentType = contentType

	data, err := io.ReadAll(content)
	if err != nil {
		return err
	}
	f.content = data

	return nil
}

func (f *fakeStorage) Get(context.Context, string) (*domain.Object, error) {
	return nil, domain.ErrImageNotFound
}
