package com.example.petsocial.core.network.model.matching

data class SwipeRequest(
    val source_pet_id: String,
    val target_pet_id: String,
    val action: String
)