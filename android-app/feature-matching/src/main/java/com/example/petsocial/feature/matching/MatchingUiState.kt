package com.example.petsocial.feature.matching

import com.example.petsocial.core.network.model.matching.MatchResponse
import com.example.petsocial.core.network.model.matching.RecommendationResponse
import com.example.petsocial.core.network.model.pets.PetResponse

data class MatchingUiState(
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    val activePet: PetResponse? = null,
    val recommendations: List<RecommendationResponse> = emptyList(),
    val matches: List<MatchResponse> = emptyList()
)