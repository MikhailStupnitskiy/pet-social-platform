package com.example.petsocial.core.auth.api

import com.example.petsocial.core.auth.model.AuthResponse
import com.example.petsocial.core.auth.model.LoginRequest
import com.example.petsocial.core.auth.model.RegisterRequest
import com.example.petsocial.core.auth.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    @POST("v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @GET("v1/auth/me")
    suspend fun me(): UserResponse
}
