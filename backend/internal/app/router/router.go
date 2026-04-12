package router

import (
	"context"
	"net/http"
	"time"

	appmiddlewre "pet-social-platform/backend/internal/app/middleware"
	"pet-social-platform/backend/internal/app/response"

	authpostgres "pet-social-platform/backend/internal/modules/auth/repository/postgres"
	authservice "pet-social-platform/backend/internal/modules/auth/service"
	authhttp "pet-social-platform/backend/internal/modules/auth/transport/http"

	userspostgres "pet-social-platform/backend/internal/modules/users/repository/postgres"
	usersservice "pet-social-platform/backend/internal/modules/users/service"
	usershttp "pet-social-platform/backend/internal/modules/users/transport/http"

	petspostgres "pet-social-platform/backend/internal/modules/pets/repository/postgres"
	petsservice "pet-social-platform/backend/internal/modules/pets/service"
	petshttp "pet-social-platform/backend/internal/modules/pets/transport/http"

	matchingpostgres "pet-social-platform/backend/internal/modules/matching/repository/postgres"
	matchingservice "pet-social-platform/backend/internal/modules/matching/service"
	matchinghttp "pet-social-platform/backend/internal/modules/matching/transport/http"

	chatspostgres "pet-social-platform/backend/internal/modules/chats/repository/postgres"
	chatsservice "pet-social-platform/backend/internal/modules/chats/service"
	chatshttp "pet-social-platform/backend/internal/modules/chats/transport/http"

	"github.com/go-chi/chi/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

type Dependencies struct {
	DB *pgxpool.Pool
	JWTSecret string
}

func New(deps Dependencies) http.Handler {
	r := chi.NewRouter()

	r.Use(appmiddlewre.RequestID())
	r.Use(appmiddlewre.Recover())
	r.Use(appmiddlewre.Logging())

	r.NotFound(func(w http.ResponseWriter, r *http.Request) {
		response.NotFound(w, "route not found")
	})

	r.MethodNotAllowed(func(w http.ResponseWriter, r *http.Request) {
		response.Error(w, http.StatusMethodNotAllowed, "method_not_allowed", "method not allowed")
	})

	// auth init
	authRepo := authpostgres.New(deps.DB)
	jwtService := authservice.NewJWTService(deps.JWTSecret)
	authSvc := authservice.New(authRepo, jwtService)
	authHandler := authhttp.NewHandler(authSvc)

	// users init
	usersRepo := userspostgres.New(deps.DB)
	usersSvc := usersservice.New(usersRepo)
	usersHandler := usershttp.NewHandler(usersSvc)

	// pets init
	petsRepo := petspostgres.New(deps.DB)
	petsSvc := petsservice.New(petsRepo)
	petsHandler := petshttp.NewHandler(petsSvc)

	// chats init
	chatsRepo := chatspostgres.New(deps.DB)
	chatsSvc := chatsservice.New(chatsRepo)
	chatsHandler := chatshttp.NewHandler(chatsSvc)

	// matching init
	matchingRepo := matchingpostgres.New(deps.DB)
	matchingSvc := matchingservice.New(matchingRepo, chatsSvc)
	matchingHandler := matchinghttp.NewHandler(matchingSvc)

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

	r.Route("/v1/auth", func(r chi.Router) {
		r.Post("/register", authHandler.Register)
		r.Post("/login", authHandler.Login)

		r.Group(func(r chi.Router) {
			r.Use(authhttp.AuthMiddleware(authSvc))
			r.Get("/me", authHandler.Me)
		})
	})

	r.Route("/v1/profile", func(r chi.Router) {
		r.Use(authhttp.AuthMiddleware(authSvc))

		r.Get("/me", usersHandler.GetMe)
		r.Patch("/me", usersHandler.PatchMe)
	})

	r.Route("/v1/pets", func(r chi.Router) {
		r.Use(authhttp.AuthMiddleware(authSvc))

		r.Get("/", petsHandler.List)
		r.Post("/", petsHandler.Create)
		r.Get("/{id}", petsHandler.GetByID)
		r.Patch("/{id}", petsHandler.Patch)
		r.Post("/{id}/set-active", petsHandler.SetActive)
	})

	r.Route("/v1/matching", func(r chi.Router) {
		r.Use(authhttp.AuthMiddleware(authSvc))

		r.Get("/recommendations", matchingHandler.Recommendations)
		r.Post("/swipes", matchingHandler.Swipe)
		r.Get("/matches", matchingHandler.Matches)
	})

	r.Route("/v1/chats", func(r chi.Router) {
		r.Use(authhttp.AuthMiddleware(authSvc))

		r.Get("/", chatsHandler.ListChats)
		r.Get("/{id}/messages", chatsHandler.ListMessages)
		r.Post("/{id}/messages", chatsHandler.SendMessage)
	})

	return r
}