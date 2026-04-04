package middleware

import (
	"log"
	"net/http"

	"pet-social-platform/backend/internal/app/response"
)

func Recover() func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			defer func() {
				if rec := recover(); rec != nil {
					log.Printf("panic recovered: %v", rec)
					response.InternalServerError(w)
				}
			}()

			next.ServeHTTP(w, r)
		})
	}
}