package com.example.petsocial.core.network.model.feed

data class UpdatePostRequest(
    val body: String,
    val image_url: String? = null
)
