package com.example.petsocial.core.network.model.feed

data class PostResponse(
    val id: String,
    val author_user_id: String,
    val author_name: String,
    val pet_id: String,
    val pet_name: String,
    val pet_species: String,
    val body: String,
    val image_url: String?,
    val created_at: String,
    val updated_at: String
)
