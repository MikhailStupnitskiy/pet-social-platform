package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"time"

	"pet-social-platform/backend/internal/app/router"
	imagesservice "pet-social-platform/backend/internal/modules/images/service"
	"pet-social-platform/backend/internal/platform/config"
	"pet-social-platform/backend/internal/platform/objectstorage"
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

	storageCtx, cancelStorage := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancelStorage()

	imageStorage, err := objectstorage.NewMinIOStorage(
		storageCtx,
		cfg.MinIO.Endpoint,
		cfg.MinIO.AccessKey,
		cfg.MinIO.SecretKey,
		cfg.MinIO.Bucket,
		cfg.MinIO.UseSSL,
	)
	if err != nil {
		log.Fatalf("failed to connect to minio: %v", err)
	}

	r := router.New(router.Dependencies{
		DB:           dbPool,
		JWTSecret:    cfg.JWTSecret,
		ImageService: imagesservice.New(imageStorage),
	})

	addr := fmt.Sprintf(":%s", cfg.HTTPPort)

	log.Printf("starting server on %s in %s mode", addr, cfg.AppEnv)

	if err := http.ListenAndServe(addr, r); err != nil {
		log.Fatal(err)
	}
}
