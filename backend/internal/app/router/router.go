package router

import (
	"context"
	"net/http"
	"time"

	"pet-social-platform/backend/internal/app/middleware"
	"pet-social-platform/backend/internal/app/response"

	"github.com/go-chi/chi/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Dependencies struct {
	DB *pgxpool.Pool
}

func New(deps Dependencies) http.Handler {
	r := chi.NewRouter()

	r.Use(middleware.RequestID())
	r.Use(middleware.Recover())
	r.Use(middleware.Logging())

	r.NotFound(func(w http.ResponseWriter, r *http.Request) {
		response.NotFound(w, "route not found")
	})

	r.MethodNotAllowed(func(w http.ResponseWriter, r *http.Request) {
		response.Error(w, http.StatusMethodNotAllowed, "method_not_allowed", "method not allowed")
	})

	r.Get("/health", func(w http.ResponseWriter, r *http.Request) {
		response.JSON(w, http.StatusOK, map[string]string{
			"status": "ok",
		})
	})

	r.Get("/ready", func(w http.ResponseWriter, r *http.Request) {
		ctx, cancel := context.WithTimeout(r.Context(), 3*time.Second)
		defer cancel()

		if err := deps.DB.Ping(ctx); err != nil {
			response.ServiceUnavailable(w, "database is not available")
			return
		}

		response.JSON(w, http.StatusOK, map[string]string{
			"status": "ready",
		})
	})

	return r
}