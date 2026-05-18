package com.example.petsocial.core.network.model.routine

data class RoutineItemResponse(
    val id: String,
    val pet_id: String,
    val title: String,
    val category: String,
    val schedule_time: String?,
    val repeat_rule: String = "none",
    val notes: String?,
    val is_enabled: Boolean,
    val created_at: String,
    val updated_at: String
)
