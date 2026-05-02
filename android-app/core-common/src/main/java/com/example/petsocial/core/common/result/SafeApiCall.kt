package com.example.petsocial.core.common.result

suspend inline fun <T> safeApiCall(
    crossinline block: suspend () -> T
): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (e: Throwable) {
        AppResult.Error(e.toAppError())
    }
}