package com.example.petsocial.core.network.model.feed

data class CommentResponse(
    val id: String,
    val post_id: String,
    val author_user_id: String,
    val author_name: String,
    val body: String,
    val created_at: String,
    val updated_at: String
)
