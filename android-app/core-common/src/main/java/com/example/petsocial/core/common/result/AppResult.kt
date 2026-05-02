package com.example.petsocial.core.common.result

sealed interface AppResult<out T> {

    data class Success<T>(
        val data: T
    ) : AppResult<T>

    data class Error(
        val error: AppError
    ) : AppResult<Nothing>
}