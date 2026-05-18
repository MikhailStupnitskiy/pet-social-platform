package com.example.petsocial.feature.matching

import com.example.petsocial.core.network.model.matching.MatchResponse
import com.example.petsocial.core.network.model.matching.RecommendationResponse
import com.example.petsocial.core.network.model.pets.PetResponse

data class MatchingUiState(
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val isRefreshingLocation: Boolean = false,
    val isFilterSheetVisible: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val locationMessage: String? = null,
    val authToken: String? = null,

    val activePet: PetResponse? = null,
    val recommendations: List<RecommendationResponse> = emptyList(),
    val matches: List<MatchResponse> = emptyList(),
    val goal: MatchingGoal = MatchingGoal.Walk,
    val filters: MatchingFilters = MatchingFilters()
)

enum class MatchingGoal(val wireValue: String, val label: String) {
    Walk("walk", "Прогулка"),
    Breeding("breeding", "Разведение")
}

data class MatchingFilters(
    val maxDistanceMeters: Int? = 3000,
    val breed: String = "",
    val sex: String = "",
    val ageMinMonths: String = "",
    val ageMaxMonths: String = "",
    val tags: String = "",
    val interests: String = "",
    val hasPhotoOnly: Boolean = false
) {
    val activeCount: Int
        get() = listOf(
            maxDistanceMeters != null,
            breed.isNotBlank(),
            sex.isNotBlank(),
            ageMinMonths.isNotBlank(),
            ageMaxMonths.isNotBlank(),
            tags.isNotBlank(),
            interests.isNotBlank(),
            hasPhotoOnly
        ).count { it }
}
