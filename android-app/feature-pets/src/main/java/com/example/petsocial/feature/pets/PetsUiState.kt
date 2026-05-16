package com.example.petsocial.feature.pets

import android.net.Uri
import com.example.petsocial.core.network.model.pets.PetResponse

data class PetsUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isAddPetFormVisible: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val pets: List<PetResponse> = emptyList(),

    val name: String = "",
    val species: String = "",
    val breed: String = "",
    val sex: String = "",
    val birthDate: String = "",
    val weightKg: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val selectedPhotoUri: Uri? = null,
    val personalityTags: String = "",
    val interests: String = "",
    val healthNotes: String = "",
    val matchingGoal: String = ""
)
