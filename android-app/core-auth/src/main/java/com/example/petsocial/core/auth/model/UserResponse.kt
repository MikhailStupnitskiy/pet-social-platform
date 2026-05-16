package com.example.petsocial.core.auth.model

data class UserResponse(
    val id: String,
    val email: String,
    val is_handler: Boolean = false
)
