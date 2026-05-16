package com.example.petsocial.core.network.model.chat

data class ChatResponse(
    val id: String,
    val match_id: String?,
    val service_request_id: String?,
    val pet1_id: String,
    val pet2_id: String?,
    val client_user_id: String?,
    val handler_user_id: String?,
    val created_at: String
)
