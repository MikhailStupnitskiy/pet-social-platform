package com.example.petsocial.feature.profile

import android.net.Uri
import com.example.petsocial.core.network.model.profile.ProfileResponse
import com.example.petsocial.core.network.model.profile.ProfileStatsResponse
import com.example.petsocial.core.network.model.profile.PublicUserProfileResponse
import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.network.model.pets.PublicPetProfileResponse

interface ProfileRepository {

    suspend fun getMe(): ProfileResponse

    suspend fun getStats(): ProfileStatsResponse

    suspend fun getPets(): List<PetResponse>

    suspend fun getPublicProfile(userId: String): PublicUserProfileResponse

    suspend fun getPublicPet(petId: String): PublicPetProfileResponse

    suspend fun updateMe(
        name: String,
        birthDate: String?,
        city: String?,
        bio: String?,
        avatarUrl: String?
    ): ProfileResponse

    suspend fun createPet(
        name: String,
        species: String,
        breed: String?,
        sex: String?,
        birthDate: String?,
        bio: String?,
        photoUrl: String?,
        personalityTags: List<String>,
        interests: List<String>,
        healthNotes: String?,
        matchingGoal: String?
    ): PetResponse

    suspend fun updatePet(
        pet: PetResponse,
        name: String,
        species: String,
        breed: String?,
        sex: String?,
        birthDate: String?,
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
