package com.example.petsocial.core.network.model.profile

data class ProfileResponse(
    val user_id: String,
    val email: String,
    val name: String,
    val birth_date: String?,
    val city: String?,
    val bio: String?,
    val avatar_url: String?,
    val created_at: String,
    val updated_at: String
)