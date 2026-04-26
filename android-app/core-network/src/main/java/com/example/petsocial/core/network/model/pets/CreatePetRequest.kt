package com.example.petsocial.core.network.model.pets

data class CreatePetRequest(
    val name: String,
    val species: String,
    val breed: String? = null,
    val sex: String? = null,
    val birth_date: String? = null,
    val weight_kg: String? = null,
    val bio: String? = null
)