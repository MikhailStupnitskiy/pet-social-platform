package com.example.petsocial.core.network.model.matching

data class MatchingEventRequest(
    val source_pet_id: String,
    val target_pet_id: String,
    val event_type: String
)
