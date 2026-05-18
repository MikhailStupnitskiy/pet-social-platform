package com.example.petsocial.feature.profile

import android.net.Uri
import com.example.petsocial.core.network.model.pets.PetResponse

data class ProfileUiState(
    val isLoading: Boolean = false,
    val isSavingProfile: Boolean = false,
    val isSavingPet: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isUnauthorized: Boolean = false,
    val authToken: String? = null,

    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val birthDate: String = "",
    val city: String = "",
    val bio: String = "",
    val avatarUrl: String = "",

    val petsCount: Int = 0,
    val matchesCount: Int = 0,
    val postsCount: Int = 0,
    val unreadNotificationsCount: Int = 0,
    val pets: List<PetResponse> = emptyList(),

    val isProfileEditorVisible: Boolean = false,
    val profileName: String = "",
    val profileCity: String = "",
    val profileBio: String = "",
    val selectedAvatarUri: Uri? = null,

    val isPetsSheetVisible: Boolean = false,
    val petFormMode: PetFormMode? = null,
    val editingPetId: String? = null,
    val petName: String = "",
    val petSpecies: String = "",
    val petBreed: String = "",
    val petSex: String = "",
    val petBirthDate: String = "",
    val petBio: String = "",
    val selectedPetPhotoUri: Uri? = null,
    val currentPetPhotoUrl: String? = null,
    val petPersonalityTags: List<String> = emptyList(),
    val petPersonalityInput: String = "",
    val petInterests: List<String> = emptyList(),
    val petInterestInput: String = "",
    val petHealthNotes: String = "",
    val petMatchingGoal: String = ""
) {
    val activePet: PetResponse?
        get() = pets.firstOrNull { it.is_active } ?: pets.firstOrNull()
}

enum class PetFormMode {
    Create,
    Edit
}
