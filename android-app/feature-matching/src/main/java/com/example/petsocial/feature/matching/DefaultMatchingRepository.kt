package com.example.petsocial.feature.matching

import com.example.petsocial.core.network.api.MatchingApi
import com.example.petsocial.core.network.model.matching.MatchResponse
import com.example.petsocial.core.network.model.matching.RecommendationResponse
import com.example.petsocial.core.network.model.matching.SwipeRequest
import com.example.petsocial.core.network.model.matching.SwipeResponse
import javax.inject.Inject

class DefaultMatchingRepository @Inject constructor(
    private val matchingApi: MatchingApi
) : MatchingRepository {

    override suspend fun getRecommendations(
        petId: String
    ): List<RecommendationResponse> {
        return matchingApi.getRecommendations(petId)
    }

    override suspend fun like(
        sourcePetId: String,
        targetPetId: String
    ): SwipeResponse {
        return matchingApi.swipe(
            SwipeRequest(
                source_pet_id = sourcePetId,
                target_pet_id = targetPetId,
                action = "like"
            )
        )
    }

    override suspend fun pass(
        sourcePetId: String,
        targetPetId: String
    ): SwipeResponse {
        return matchingApi.swipe(
            SwipeRequest(
                source_pet_id = sourcePetId,
                target_pet_id = targetPetId,
                action = "pass"
            )
        )
    }

    override suspend fun getMatches(
        petId: String
    ): List<MatchResponse> {
        return matchingApi.getMatches(petId)
    }
}