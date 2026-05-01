package com.example.petsocial.core.network.model.chat

data class MessageResponse(
    val id: String,
    val chat_id: String,
    val sender_user_id: String,
    val body: String,
    val created_at: String
)