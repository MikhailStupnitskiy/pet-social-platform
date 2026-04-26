package com.example.petsocial.feature.pets

import com.example.petsocial.core.network.model.pets.PetResponse

interface PetsRepository {

    suspend fun getPets(): List<PetResponse>

    suspend fun createPet(
        name: String,
        species: String,
        breed: String?,
        sex: String?,
        birthDate: String?,
        weightKg: String?,
        bio: String?
    ): PetResponse

    suspend fun setActivePet(id: String)
}