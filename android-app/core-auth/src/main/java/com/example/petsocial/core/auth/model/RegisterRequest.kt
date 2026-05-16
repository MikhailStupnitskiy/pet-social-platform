package com.example.petsocial.core.auth.model

data class RegisterRequest(
    val email: String,
    val password: String,
    val is_handler: Boolean = false
)
