package com.example.petsocial.feature.pets

import android.net.Uri
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
        bio: String?,
        photoUrl: String?,
        personalityTags: List<String>,
        interests: List<String>,
        healthNotes: String?,
        matchingGoal: String?
    ): PetResponse

    suspend fun uploadImage(uri: Uri): String

    suspend fun setActivePet(id: String)
}
