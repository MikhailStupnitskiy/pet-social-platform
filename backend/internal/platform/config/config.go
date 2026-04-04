package config

import (
	"fmt"
	"os"

	"github.com/joho/godotenv"
)

type Config struct {
	AppEnv      string
	HTTPPort    string
	PostgresDSN string
}

func Load() (*Config, error) {
	_ = godotenv.Load("../.env")
	_ = godotenv.Load(".env")

	cfg := &Config{
		AppEnv:      getEnv("APP_ENV", "local"),
		HTTPPort:    getEnv("HTTP_PORT", "8080"),
		PostgresDSN: os.Getenv("POSTGRES_DSN"),
	}

	if cfg.PostgresDSN == "" {
		return nil, fmt.Errorf("POSTGRES_DSN is required")
	}

	return cfg, nil
}

func getEnv(key, fallback string) string {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}
	return value
}