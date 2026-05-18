package com.example.petsocial.feature.matching

import com.example.petsocial.core.network.api.MatchingApi
import com.example.petsocial.core.network.api.PetsApi
import com.example.petsocial.core.network.model.matching.MatchingEventRequest
import com.example.petsocial.core.network.model.matching.MatchResponse
import com.example.petsocial.core.network.model.matching.RecommendationResponse
import com.example.petsocial.core.network.model.matching.SwipeRequest
import com.example.petsocial.core.network.model.matching.SwipeResponse
import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.network.model.pets.UpdatePetLocationRequest
import javax.inject.Inject

class DefaultMatchingRepository @Inject constructor(
    private val matchingApi: MatchingApi,
    private val petsApi: PetsApi
) : MatchingRepository {

    override suspend fun getRecommendations(
        petId: String,
        goal: MatchingGoal,
        filters: MatchingFilters,
        species: String?
    ): List<RecommendationResponse> {
        return matchingApi.getRecommendations(
            petId = petId,
            goal = goal.wireValue,
            maxDistanceMeters = filters.maxDistanceMeters,
            species = species,
            breed = filters.breed.ifBlank { null },
            sex = filters.sex.ifBlank { null },
            ageMinMonths = filters.ageMinMonths.toIntOrNull(),
            ageMaxMonths = filters.ageMaxMonths.toIntOrNull(),
            tags = filters.tags.ifBlank { null },
            interests = filters.interests.ifBlank { null },
            hasPhoto = filters.hasPhotoOnly.takeIf { it }
        )
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

    override suspend fun sendProfileOpen(
        sourcePetId: String,
        targetPetId: String
    ) {
        matchingApi.sendEvent(
            MatchingEventRequest(
                source_pet_id = sourcePetId,
                target_pet_id = targetPetId,
                event_type = "profile_open"
            )
        )
    }

    override suspend fun updatePetLocation(
        petId: String,
        latitude: Double,
        longitude: Double
    ): PetResponse {
        return petsApi.updatePetLocation(
            id = petId,
            request = UpdatePetLocationRequest(
                latitude = latitude.toString(),
                longitude = longitude.toString()
            )
        )
    }
}
