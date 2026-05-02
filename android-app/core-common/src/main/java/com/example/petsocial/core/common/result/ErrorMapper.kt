package com.example.petsocial.core.common.result

import retrofit2.HttpException
import java.io.IOException

fun Throwable.toAppError(): AppError {
    return when (this) {
        is IOException -> {
            AppError.Network()
        }

        is HttpException -> {
            when (code()) {
                400 -> AppError.BadRequest()
                401 -> AppError.Unauthorized()
                409 -> AppError.Conflict()
                in 500..599 -> AppError.Server(
                    code = code(),
                    message = "Ошибка сервера: ${code()}"
                )
                else -> AppError.Server(
                    code = code(),
                    message = "Ошибка сервера: ${code()}"
                )
            }
        }

        else -> {
            AppError.Unknown()
        }
    }
}