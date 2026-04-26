package com.example.petsocial.core.network.model.matching

data class MatchResponse(
    val id: String,
    val pet1_id: String,
    val pet2_id: String,
    val created_at: String
)