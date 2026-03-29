Структура Android-части: 

android-app/
├── app
│
├── core
│   ├── core-common
│   ├── core-model
│   ├── core-ui
│   ├── core-designsystem
│   ├── core-navigation
│   ├── core-network
│   ├── core-database
│   ├── core-datastore
│   ├── core-auth
│   ├── core-testing
│   └── core-notification
│
├── feature-auth
├── feature-onboarding
├── feature-profile
├── feature-pets
├── feature-matching
├── feature-chat
├── feature-handlers
├── feature-routine
├── feature-feed
├── feature-subscription
├── feature-settings
└── feature-activity


Структура backend-части: 

backend/
├── cmd
│   ├── api
│   ├── worker
│   └── migrate
│
├── internal
│   ├── platform
│   │   ├── config
│   │   ├── logger
│   │   ├── postgres
│   │   ├── redis
│   │   ├── httpserver
│   │   ├── websocket
│   │   ├── storage
│   │   ├── auth
│   │   ├── queue
│   │   ├── clock
│   │   └── transaction
│   │
│   ├── modules
│   │   ├── auth
│   │   ├── users
│   │   ├── pets
│   │   ├── matching
│   │   ├── chats
│   │   ├── handlers
│   │   ├── deals
│   │   ├── routine
│   │   ├── feed
│   │   ├── subscription
│   │   ├── notifications
│   │   └── activity
│   │
│   └── app
│       ├── router
│       ├── middleware
│       └── bootstrap
│
├── pkg
│   └── shared
│
├── migrations
├── api
│   └── openapi
├── deployments
├── scripts
└── tests


Подробно можно ознакомиться [здесь][def]

[def]: https://miro.com/app/board/uXjVGqOWtUA=/