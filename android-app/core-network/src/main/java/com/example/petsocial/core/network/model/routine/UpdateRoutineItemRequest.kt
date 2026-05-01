package com.example.petsocial.core.network.model.routine

data class UpdateRoutineItemRequest(
    val title: String,
    val category: String,
    val schedule_time: String? = null,
    val notes: String? = null,
    val is_enabled: Boolean
)