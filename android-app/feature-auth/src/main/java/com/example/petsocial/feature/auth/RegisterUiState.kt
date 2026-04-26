package com.example.petsocial.feature.auth

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val repeatedPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false
)