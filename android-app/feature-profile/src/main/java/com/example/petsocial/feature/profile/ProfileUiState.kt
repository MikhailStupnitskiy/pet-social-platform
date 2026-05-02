package com.example.petsocial.feature.profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isUnauthorized: Boolean = false,

    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val birthDate: String = "",
    val city: String = "",
    val bio: String = "",
    val avatarUrl: String = ""
)