package com.example.petsocial.feature.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.session.SessionEventBus
import com.example.petsocial.core.datastore.auth.TokenStorage
import com.example.petsocial.core.network.model.pets.PetResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val tokenStorage: TokenStorage,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tokenStorage.token.collect { token ->
                _uiState.value = _uiState.value.copy(authToken = token)
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            val profile = when (val result = safeApiCall { repository.getMe() }) {
                is AppResult.Success -> result.data
                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) return@launch
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.error.message)
                    return@launch
                }
            }

            val stats = when (val result = safeApiCall { repository.getStats() }) {
                is AppResult.Success -> result.data
                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) return@launch
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.error.message)
                    return@launch
                }
            }

            val pets = when (val result = safeApiCall { repository.getPets() }) {
                is AppResult.Success -> result.data
                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) return@launch
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.error.message)
                    return@launch
                }
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                userId = profile.user_id,
                email = profile.email,
                name = profile.name,
                birthDate = profile.birth_date.orEmpty(),
                city = profile.city.orEmpty(),
                bio = profile.bio.orEmpty(),
                avatarUrl = profile.avatar_url.orEmpty(),
                petsCount = stats.pets_count,
                matchesCount = stats.matches_count,
                postsCount = stats.posts_count,
                pets = pets
            )
        }
    }

    fun showProfileEditor() {
        val state = _uiState.value
        _uiState.value = state.copy(
            isProfileEditorVisible = true,
            profileName = state.name,
            profileCity = state.city,
            profileBio = state.bio,
            selectedAvatarUri = null,
            errorMessage = null,
            successMessage = null
        )
    }

    fun hideProfileEditor() {
        _uiState.value = _uiState.value.copy(
            isProfileEditorVisible = false,
            selectedAvatarUri = null,
            errorMessage = null
        )
    }

    fun onProfileNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(profileName = value, errorMessage = null, successMessage = null)
    }

    fun onProfileCityChanged(value: String) {
        _uiState.value = _uiState.value.copy(profileCity = value, errorMessage = null, successMessage = null)
    }

    fun onProfileBioChanged(value: String) {
        _uiState.value = _uiState.value.copy(profileBio = value, errorMessage = null, successMessage = null)
    }

    fun onAvatarSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedAvatarUri = uri, errorMessage = null, successMessage = null)
    }

    fun clearSelectedAvatar() {
        _uiState.value = _uiState.value.copy(selectedAvatarUri = null)
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.profileName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите имя")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingProfile = true, errorMessage = null, successMessage = null)

            when (
                val result = safeApiCall {
                    val avatarUrl = state.selectedAvatarUri?.let { repository.uploadImage(it) }
                        ?: state.avatarUrl.ifBlank { null }
                    repository.updateMe(
                        name = state.profileName.trim(),
                        birthDate = state.birthDate.ifBlank { null },
                        city = state.profileCity.trim().ifBlank { null },
                        bio = state.profileBio.trim().ifBlank { null },
                        avatarUrl = avatarUrl
                    )
                }
            ) {
                is AppResult.Success -> {
                    val profile = result.data
                    _uiState.value = _uiState.value.copy(
                        isSavingProfile = false,
                        isProfileEditorVisible = false,
                        selectedAvatarUri = null,
                        userId = profile.user_id,
                        email = profile.email,
                        name = profile.name,
                        birthDate = profile.birth_date.orEmpty(),
                        city = profile.city.orEmpty(),
                        bio = profile.bio.orEmpty(),
                        avatarUrl = profile.avatar_url.orEmpty(),
                        successMessage = "Профиль сохранён"
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) return@launch
                    _uiState.value = _uiState.value.copy(isSavingProfile = false, errorMessage = result.error.message)
                }
            }
        }
    }

    fun showPetsSheet() {
        _uiState.value = _uiState.value.copy(isPetsSheetVisible = true, errorMessage = null, successMessage = null)
    }

    fun hidePetsSheet() {
        _uiState.value = _uiState.value.copy(
            isPetsSheetVisible = false,
            petFormMode = null,
            editingPetId = null,
            errorMessage = null
        )
    }

    fun showCreatePetForm() {
        _uiState.value = resetPetForm(_uiState.value).copy(
            isPetsSheetVisible = true,
            petFormMode = PetFormMode.Create,
            errorMessage = null,
            successMessage = null
        )
    }

    fun showEditPetForm(pet: PetResponse) {
        _uiState.value = _uiState.value.copy(
            isPetsSheetVisible = true,
            petFormMode = PetFormMode.Edit,
            editingPetId = pet.id,
            petName = pet.name,
            petSpecies = normalizeSpecies(pet.species),
            petBreed = pet.breed.orEmpty(),
            petSex = normalizeSex(pet.sex.orEmpty()),
            petBirthDate = pet.birth_date.orEmpty(),
            petBio = pet.bio.orEmpty(),
            selectedPetPhotoUri = null,
            currentPetPhotoUrl = pet.photo_url,
            petPersonalityTags = pet.personality_tags.filter { it in allowedPersonalityTags },
            petPersonalityInput = "",
            petInterests = pet.interests.filter { it in allowedInterests },
            petInterestInput = "",
            petHealthNotes = pet.health_notes.orEmpty(),
            petMatchingGoal = pet.matching_goal.orEmpty(),
            errorMessage = null,
            successMessage = null
        )
    }

    fun hidePetForm() {
        _uiState.value = resetPetForm(_uiState.value).copy(errorMessage = null)
    }

    fun onPetNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(petName = value, errorMessage = null, successMessage = null)
    }

    fun onPetSpeciesChanged(value: String) {
        val state = _uiState.value
        val breed = if (state.petBreed in breedOptionsFor(value)) state.petBreed else ""
        _uiState.value = state.copy(
            petSpecies = value,
            petBreed = breed,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onPetBreedChanged(value: String) {
        _uiState.value = _uiState.value.copy(petBreed = value, errorMessage = null, successMessage = null)
    }

    fun onPetSexChanged(value: String) {
        _uiState.value = _uiState.value.copy(petSex = value, errorMessage = null, successMessage = null)
    }

    fun onPetBirthDateChanged(value: String) {
        _uiState.value = _uiState.value.copy(petBirthDate = value, errorMessage = null, successMessage = null)
    }

    fun onPetBioChanged(value: String) {
        _uiState.value = _uiState.value.copy(petBio = value, errorMessage = null, successMessage = null)
    }

    fun onPetPhotoSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedPetPhotoUri = uri, errorMessage = null, successMessage = null)
    }

    fun clearSelectedPetPhoto() {
        _uiState.value = _uiState.value.copy(selectedPetPhotoUri = null)
    }

    fun onPersonalityInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(petPersonalityInput = value)
    }

    fun addPersonalityTag() {
        val state = _uiState.value
        val tag = state.petPersonalityInput.trim()
        if (tag.isBlank() || state.petPersonalityTags.any { it.equals(tag, ignoreCase = true) }) return
        _uiState.value = state.copy(
            petPersonalityTags = state.petPersonalityTags + tag,
            petPersonalityInput = ""
        )
    }

    fun removePersonalityTag(tag: String) {
        _uiState.value = _uiState.value.copy(
            petPersonalityTags = _uiState.value.petPersonalityTags.filterNot { it == tag }
        )
    }

    fun togglePersonalityTag(tag: String) {
        if (tag !in allowedPersonalityTags) return
        val state = _uiState.value
        val tags = if (tag in state.petPersonalityTags) {
            state.petPersonalityTags - tag
        } else {
            state.petPersonalityTags + tag
        }
        _uiState.value = state.copy(petPersonalityTags = tags, errorMessage = null, successMessage = null)
    }

    fun onInterestInputChanged(value: String) {
        _uiState.value = _uiState.value.copy(petInterestInput = value)
    }

    fun addInterest() {
        val state = _uiState.value
        val tag = state.petInterestInput.trim()
        if (tag.isBlank() || state.petInterests.any { it.equals(tag, ignoreCase = true) }) return
        _uiState.value = state.copy(
            petInterests = state.petInterests + tag,
            petInterestInput = ""
        )
    }

    fun removeInterest(tag: String) {
        _uiState.value = _uiState.value.copy(
            petInterests = _uiState.value.petInterests.filterNot { it == tag }
        )
    }

    fun toggleInterest(tag: String) {
        if (tag !in allowedInterests) return
        val state = _uiState.value
        val tags = if (tag in state.petInterests) {
            state.petInterests - tag
        } else {
            state.petInterests + tag
        }
        _uiState.value = state.copy(petInterests = tags, errorMessage = null, successMessage = null)
    }

    fun onPetHealthNotesChanged(value: String) {
        _uiState.value = _uiState.value.copy(petHealthNotes = value, errorMessage = null, successMessage = null)
    }

    fun onPetMatchingGoalChanged(value: String) {
        _uiState.value = _uiState.value.copy(petMatchingGoal = value, errorMessage = null, successMessage = null)
    }

    fun savePet() {
        val state = _uiState.value
        if (state.petName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите имя питомца")
            return
        }
        if (state.petSpecies.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите вид питомца")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingPet = true, errorMessage = null, successMessage = null)

            when (
                val result = safeApiCall {
                    val photoUrl = state.selectedPetPhotoUri?.let { repository.uploadImage(it) }
                        ?: state.currentPetPhotoUrl
                    if (state.petFormMode == PetFormMode.Edit) {
                        val pet = state.pets.first { it.id == state.editingPetId }
                        repository.updatePet(
                            pet = pet,
                            name = state.petName.trim(),
                            species = state.petSpecies.trim(),
                            breed = state.petBreed.trim().ifBlank { null },
                            sex = state.petSex.trim().ifBlank { null },
                            birthDate = state.petBirthDate.trim().ifBlank { null },
                            bio = state.petBio.trim().ifBlank { null },
                            photoUrl = photoUrl,
                            personalityTags = state.petPersonalityTags,
                            interests = state.petInterests,
                            healthNotes = state.petHealthNotes.trim().ifBlank { null },
                            matchingGoal = state.petMatchingGoal.trim().ifBlank { null }
                        )
                    } else {
                        val hadActivePet = state.pets.any { it.is_active }
                        val created = repository.createPet(
                            name = state.petName.trim(),
                            species = state.petSpecies.trim(),
                            breed = state.petBreed.trim().ifBlank { null },
                            sex = state.petSex.trim().ifBlank { null },
                            birthDate = state.petBirthDate.trim().ifBlank { null },
                            bio = state.petBio.trim().ifBlank { null },
                            photoUrl = photoUrl,
                            personalityTags = state.petPersonalityTags,
                            interests = state.petInterests,
                            healthNotes = state.petHealthNotes.trim().ifBlank { null },
                            matchingGoal = state.petMatchingGoal.trim().ifBlank { null }
                        )
                        if (!hadActivePet) {
                            repository.setActivePet(created.id)
                        }
                        created
                    }
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = resetPetForm(_uiState.value).copy(
                        isSavingPet = false,
                        successMessage = if (state.petFormMode == PetFormMode.Edit) {
                            "Питомец сохранён"
                        } else {
                            "Питомец добавлен"
                        }
                    )
                    reloadPetsAndStats()
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) return@launch
                    _uiState.value = _uiState.value.copy(isSavingPet = false, errorMessage = result.error.message)
                }
            }
        }
    }

    fun setActivePet(id: String) {
        viewModelScope.launch {
            when (val result = safeApiCall { repository.setActivePet(id) }) {
                is AppResult.Success -> reloadPetsAndStats()
                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) return@launch
                    _uiState.value = _uiState.value.copy(errorMessage = result.error.message)
                }
            }
        }
    }

    private suspend fun reloadPetsAndStats() {
        val stats = when (val result = safeApiCall { repository.getStats() }) {
            is AppResult.Success -> result.data
            is AppResult.Error -> {
                if (handleUnauthorized(result.error)) return
                _uiState.value = _uiState.value.copy(errorMessage = result.error.message)
                return
            }
        }
        val pets = when (val result = safeApiCall { repository.getPets() }) {
            is AppResult.Success -> result.data
            is AppResult.Error -> {
                if (handleUnauthorized(result.error)) return
                _uiState.value = _uiState.value.copy(errorMessage = result.error.message)
                return
            }
        }
        _uiState.value = _uiState.value.copy(
            petsCount = stats.pets_count,
            matchesCount = stats.matches_count,
            postsCount = stats.posts_count,
            pets = pets
        )
    }

    private fun resetPetForm(state: ProfileUiState): ProfileUiState {
        return state.copy(
            petFormMode = null,
            editingPetId = null,
            petName = "",
            petSpecies = "",
            petBreed = "",
            petSex = "",
            petBirthDate = "",
            petBio = "",
            selectedPetPhotoUri = null,
            currentPetPhotoUrl = null,
            petPersonalityTags = emptyList(),
            petPersonalityInput = "",
            petInterests = emptyList(),
            petInterestInput = "",
            petHealthNotes = "",
            petMatchingGoal = ""
        )
    }

    private fun handleUnauthorized(error: AppError): Boolean {
        if (error is AppError.Unauthorized) {
            sessionEventBus.notifyUnauthorized()
            _uiState.value = _uiState.value.copy(isUnauthorized = true, isLoading = false)
            return true
        }

        return false
    }

    private fun normalizeSpecies(value: String): String {
        return when (value.lowercase()) {
            "dog", "собака" -> "Собака"
            "cat", "кошка" -> "Кошка"
            else -> value
        }
    }

    private fun normalizeSex(value: String): String {
        return when (value.lowercase()) {
            "male", "boy", "мальчик" -> "Мальчик"
            "female", "girl", "девочка" -> "Девочка"
            else -> value
        }
    }

    private fun breedOptionsFor(species: String): Set<String> {
        return when (species) {
            "Кошка" -> catBreedOptions
            else -> dogBreedOptions
        }
    }

    private companion object {
        val allowedPersonalityTags = setOf("Веселый", "Спокойный", "Активный", "Ласковый", "Игривый", "Общительный")
        val allowedInterests = setOf("Прогулки", "Бег", "Игры", "Парк", "Тренировки", "Путешествия")
        val dogBreedOptions = setOf(
            "Акита-ину",
            "Бигль",
            "Бордер-колли",
            "Вельш-корги",
            "Джек-рассел-терьер",
            "Золотистый ретривер",
            "Йоркширский терьер",
            "Лабрадор",
            "Мопс",
            "Немецкая овчарка",
            "Померанский шпиц",
            "Пудель",
            "Самоед",
            "Сиба-ину",
            "Такса",
            "Французский бульдог",
            "Хаски",
            "Чихуахуа",
            "Шпиц"
        )
        val catBreedOptions = setOf(
            "Абиссинская",
            "Бенгальская",
            "Британская короткошерстная",
            "Мейн-кун",
            "Невская маскарадная",
            "Ориентальная",
            "Персидская",
            "Русская голубая",
            "Сиамская",
            "Сибирская",
            "Сфинкс",
            "Шотландская вислоухая"
        )
    }
}
