package main

import (
	"fmt"
	"log"
	"net/http"

	"pet-social-platform/backend/internal/app/router"
	"pet-social-platform/backend/internal/platform/config"
	"pet-social-platform/backend/internal/platform/postgres"
)

func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("failed to load config: %v", err)
	}

	dbPool, err := postgres.NewPool(cfg.PostgresDSN)
	if err != nil {
		log.Fatalf("failed to connect to postgres: %v", err)
	}
	defer dbPool.Close()

	r := router.New(router.Dependencies{
		DB: dbPool,
		JWTSecret: cfg.JWTSecret,
	})

	addr := fmt.Sprintf(":%s", cfg.HTTPPort)

	log.Printf("starting server on %s in %s mode", addr, cfg.AppEnv)

	if err := http.ListenAndServe(addr, r); err != nil {
		log.Fatal(err)
	}
}