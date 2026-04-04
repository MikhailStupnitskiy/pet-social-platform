# Architecture Overview
## Общая архитектура

Проект реализован как монорепозиторий (monorepo), включающий:

- android-app/ — мобильное приложение (Android)
- backend/ — серверная часть (API)
- docs/ — документация проекта

## Backend
### Архитектурный стиль
- Модульный монолит
- Разделение по доменным модулям:
auth
users/profile
pets
matching
chats
routine

### Слои
- transport — HTTP / WebSocket
- service (application) — бизнес-логика
- domain — сущности и интерфейсы
- repository — работа с БД
- platform — инфраструктура (config, DB, logger)

### Технологии
- Go
- PostgreSQL
- pgx (connection pool)
- chi (HTTP router)
- Docker

### База данных
- PostgreSQL
- управление схемой через SQL-миграции
- каждая миграция неизменяема после применения

## Mobile (Android)
### Архитектура
- Clean Architecture
- разделение на слои:
presentation
domain
data

### Модульность
- feature-based модули:
feature-auth
feature-profile
feature-pets
feature-matching
feature-chat
feature-routine
- core-модули:
core-network
core-database
core-ui
core-navigation
core-auth

### Технологии
- Kotlin
- Jetpack Compose
- Hilt (DI)
- Retrofit
- Room
- Coroutines / Flow

## API
- REST (JSON)
- JWT аутентификация (планируется)
- единый формат ошибок
- разделение health / readiness:
/health — сервис жив
/ready — сервис готов к работе (БД доступна)

## Конфигурация
- конфигурация через переменные окружения
- .env — локальная конфигурация (не хранится в репозитории)
- .env.example — пример конфигурации

## MVP функциональность

На первом этапе реализуются модули:

- auth
- profile
- pets
- matching
- chat
- routine

## Принципы
- разделение ответственности (Separation of Concerns)
- минимальная связанность модулей
- расширяемость архитектуры
- единый источник истины (backend API)
- все изменения БД через миграции