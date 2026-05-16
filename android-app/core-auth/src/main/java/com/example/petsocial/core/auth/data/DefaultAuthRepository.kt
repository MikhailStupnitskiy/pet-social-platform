package com.example.petsocial.core.auth.data

import com.example.petsocial.core.auth.api.AuthApi
import com.example.petsocial.core.auth.model.LoginRequest
import com.example.petsocial.core.auth.model.RegisterRequest
import com.example.petsocial.core.auth.model.UserResponse
import com.example.petsocial.core.datastore.auth.TokenStorage
import javax.inject.Inject

class DefaultAuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    override suspend fun login(email: String, password: String): UserResponse {
        val response = authApi.login(
            LoginRequest(
                email = email,
                password = password
            )
        )

        tokenStorage.saveToken(response.token)

        return response.user
    }

    override suspend fun register(email: String, password: String, isHandler: Boolean): UserResponse {
        val response = authApi.register(
            RegisterRequest(
                email = email,
                password = password,
                is_handler = isHandler
            )
        )

        tokenStorage.saveToken(response.token)

        return response.user
    }

    override suspend fun getMe(): UserResponse {
        return authApi.me()
    }

    override suspend fun logout() {
        tokenStorage.clearToken()
    }
}
