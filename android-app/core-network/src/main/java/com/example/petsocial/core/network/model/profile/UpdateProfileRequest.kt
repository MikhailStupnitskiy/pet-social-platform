package com.example.petsocial.core.network.model.profile

data class UpdateProfileRequest(
    val name: String,
    val birth_date: String? = null,
    val city: String? = null,
    val bio: String? = null,
    val avatar_url: String? = null
)