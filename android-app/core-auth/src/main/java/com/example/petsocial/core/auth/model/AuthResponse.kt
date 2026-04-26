package com.example.petsocial.core.auth.model

data class AuthResponse(
    val token: String,
    val user: UserResponse
)