package com.example.petsocial.core.network.model.notifications

data class NotificationResponse(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val entity_type: String?,
    val entity_id: String?,
    val metadata: Map<String, String> = emptyMap(),
    val read_at: String?,
    val created_at: String
)

data class UnreadCountResponse(
    val count: Int
)