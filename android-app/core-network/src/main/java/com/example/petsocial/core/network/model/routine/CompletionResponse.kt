package com.example.petsocial.core.network.model.routine

data class CompletionResponse(
    val id: String,
    val routine_item_id: String,
    val completed_at: String
)