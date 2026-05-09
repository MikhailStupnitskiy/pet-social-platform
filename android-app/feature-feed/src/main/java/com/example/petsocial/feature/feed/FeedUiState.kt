package com.example.petsocial.feature.feed

import com.example.petsocial.core.network.model.feed.PostResponse
import com.example.petsocial.core.network.model.pets.PetResponse

data class FeedUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val activePet: PetResponse? = null,
    val posts: List<PostResponse> = emptyList(),
    val body: String = "",
    val imageUrl: String = ""
)
