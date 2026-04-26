package com.example.petsocial.feature.matching

import com.example.petsocial.core.network.model.matching.MatchResponse
import com.example.petsocial.core.network.model.matching.RecommendationResponse
import com.example.petsocial.core.network.model.matching.SwipeResponse

interface MatchingRepository {

    suspend fun getRecommendations(
        petId: String
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
}