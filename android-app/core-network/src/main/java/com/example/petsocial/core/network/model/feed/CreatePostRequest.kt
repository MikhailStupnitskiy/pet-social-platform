package com.example.petsocial.core.network.model.feed

data class CreatePostRequest(
    val pet_id: String,
    val body: String,
    val image_url: String? = null
)
