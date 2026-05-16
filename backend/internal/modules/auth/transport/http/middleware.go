package http

import (
	"net/http"
	"strings"

	"pet-social-platform/backend/internal/app/response"
	"pet-social-platform/backend/internal/modules/auth/domain"
	"pet-social-platform/backend/internal/modules/auth/service"
)

func AuthMiddleware(authService *service.Service) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			authHeader := strings.TrimSpace(r.Header.Get("Authorization"))
			if authHeader == "" {
				response.Unauthorized(w, "missing authorization header")
				return
			}

			const prefix = "Bearer "
			if !strings.HasPrefix(authHeader, prefix) {
				response.Unauthorized(w, "invalid authorization header")
				return
			}

			token := strings.TrimSpace(strings.TrimPrefix(authHeader, prefix))
			if token == "" {
				response.Unauthorized(w, "invalid authorization header")
				return
			}

			userID, err := authService.ParseToken(token)
			if err != nil {
				response.Unauthorized(w, domain.ErrUnauthorized.Error())
				return
			}

			ctx := WithUserID(r.Context(), userID)
			next.ServeHTTP(w, r.WithContext(ctx))
		})
	}
}
