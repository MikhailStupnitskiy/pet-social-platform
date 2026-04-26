package com.example.petsocial.core.network.model.pets

data class PetResponse(
    val id: String,
    val owner_id: String,
    val name: String,
    val species: String,
    val breed: String?,
    val sex: String?,
    val birth_date: String?,
    val weight_kg: String?,
    val bio: String?,
    val is_active: Boolean,
    val created_at: String,
    val updated_at: String
)