package com.example.petsocial.core.network.model.routine

data class CreateRoutineItemRequest(
    val pet_id: String,
    val title: String,
    val category: String,
    val schedule_time: String? = null,
    val notes: String? = null
)