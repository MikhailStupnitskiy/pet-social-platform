package http

import (
	"bytes"
	"context"
	"io"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"net/textproto"
	"testing"

	authhttp "pet-social-platform/backend/internal/modules/auth/transport/http"
	"pet-social-platform/backend/internal/modules/images/domain"
	"pet-social-platform/backend/internal/modules/images/service"
)

func TestUploadRequiresAuth(t *testing.T) {
	handler := NewHandler(service.New(&handlerStorage{}))
	req := httptest.NewRequest(http.MethodPost, "/v1/images", nil)
	rec := httptest.NewRecorder()

	handler.Upload(rec, req)

	if rec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401, got %d", rec.Code)
	}
}

func TestGetRequiresAuth(t *testing.T) {
	handler := NewHandler(service.New(&handlerStorage{}))
	req := httptest.NewRequest(http.MethodGet, "/v1/images/image.png", nil)
	rec := httptest.NewRecorder()

	handler.Get(rec, req)

	if rec.Code != http.StatusUnauthorized {
		t.Fatalf("expected 401, got %d", rec.Code)
	}
}

func TestUploadStoresValidImage(t *testing.T) {
	storage := &handlerStorage{}
	handler := NewHandler(service.New(storage))
	req := multipartRequest(t, []byte{0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'}, "pet.png", "image/png")
	rec := httptest.NewRecorder()

	handler.Upload(rec, withUser(req))

	if rec.Code != http.StatusCreated {
		t.Fatalf("expected 201, got %d: %s", rec.Code, rec.Body.String())
	}
	if storage.key == "" {
		t.Fatal("expected storage key")
	}
	if storage.contentType != "image/png" {
		t.Fatalf("expected image/png, got %q", storage.contentType)
	}
}

func TestUploadRejectsMissingFile(t *testing.T) {
	handler := NewHandler(service.New(&handlerStorage{}))
	req := httptest.NewRequest(http.MethodPost, "/v1/images", bytes.NewReader(nil))
	req.Header.Set("Content-Type", "multipart/form-data")
	rec := httptest.NewRecorder()

	handler.Upload(rec, withUser(req))

	if rec.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", rec.Code)
	}
}

func TestUploadRejectsTooLargeFile(t *testing.T) {
	handler := NewHandler(service.New(&handlerStorage{}))
	req := multipartRequest(t, bytes.Repeat([]byte{1}, int(service.MaxImageBytes)+1), "pet.png", "image/png")
	rec := httptest.NewRecorder()

	handler.Upload(rec, withUser(req))

	if rec.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", rec.Code)
	}
}

func TestUploadRejectsUnsupportedType(t *testing.T) {
	handler := NewHandler(service.New(&handlerStorage{}))
	req := multipartRequest(t, []byte("plain text"), "pet.txt", "text/plain")
	rec := httptest.NewRecorder()

	handler.Upload(rec, withUser(req))

	if rec.Code != http.StatusBadRequest {
		t.Fatalf("expected 400, got %d", rec.Code)
	}
}

func multipartRequest(t *testing.T, content []byte, filename string, contentType string) *http.Request {
	t.Helper()

	var body bytes.Buffer
	writer := multipart.NewWriter(&body)
	header := make(textproto.MIMEHeader)
	header.Set("Content-Disposition", `form-data; name="file"; filename="`+filename+`"`)
	header.Set("Content-Type", contentType)
	part, err := writer.CreatePart(header)
	if err != nil {
		t.Fatalf("failed to create multipart part: %v", err)
	}
	if _, err := part.Write(content); err != nil {
		t.Fatalf("failed to write multipart part: %v", err)
	}
	if err := writer.Close(); err != nil {
		t.Fatalf("failed to close multipart writer: %v", err)
	}

	req := httptest.NewRequest(http.MethodPost, "/v1/images", &body)
	req.Header.Set("Content-Type", writer.FormDataContentType())
	return req
}

func withUser(req *http.Request) *http.Request {
	ctx := authhttp.WithUserID(req.Context(), "user-1")
	return req.WithContext(ctx)
}

type handlerStorage struct {
	key         string
	contentType string
}

func (s *handlerStorage) Put(_ context.Context, key string, content io.Reader, _ int64, contentType string) error {
	s.key = key
	s.contentType = contentType
	_, err := io.Copy(io.Discard, content)
	return err
}

func (s *handlerStorage) Get(context.Context, string) (*domain.Object, error) {
	return nil, domain.ErrImageNotFound
}
