package com.example.petsocial.core.common.result

sealed interface AppError {

    val message: String

    data class Network(
        override val message: String = "Ошибка сети"
    ) : AppError

    data class Unauthorized(
        override val message: String = "Сессия истекла. Войдите снова"
    ) : AppError

    data class BadRequest(
        override val message: String = "Некорректные данные"
    ) : AppError

    data class Conflict(
        override val message: String = "Конфликт данных"
    ) : AppError

    data class Server(
        val code: Int,
        override val message: String = "Ошибка сервера"
    ) : AppError

    data class Unknown(
        override val message: String = "Неизвестная ошибка"
    ) : AppError
}