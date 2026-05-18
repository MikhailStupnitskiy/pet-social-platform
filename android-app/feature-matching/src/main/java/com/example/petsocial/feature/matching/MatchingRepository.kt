package com.example.petsocial.feature.matching

import com.example.petsocial.core.network.model.matching.MatchResponse
import com.example.petsocial.core.network.model.matching.RecommendationResponse
import com.example.petsocial.core.network.model.matching.SwipeResponse
import com.example.petsocial.core.network.model.pets.PetResponse

interface MatchingRepository {

    suspend fun getRecommendations(
        petId: String,
        goal: MatchingGoal,
        filters: MatchingFilters,
        species: String?
    ): List<RecommendationResponse>

    suspend fun like(
        sourcePetId: String,
        targetPetId: String
    ): SwipeResponse

    suspend fun pass(
        sourcePetId: String,
        targetPetId: String
    ): SwipeResponse

    suspend fun getMatches(
        petId: String
    ): List<MatchResponse>

    suspend fun sendProfileOpen(
        sourcePetId: String,
        targetPetId: String
    )

    suspend fun updatePetLocation(
        petId: String,
        latitude: Double,
        longitude: Double
    ): PetResponse
}
