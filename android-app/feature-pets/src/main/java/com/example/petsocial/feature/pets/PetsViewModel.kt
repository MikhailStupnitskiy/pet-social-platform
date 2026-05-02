package com.example.petsocial.feature.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall

@HiltViewModel
class PetsViewModel @Inject constructor(
    private val repository: PetsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    fun loadPets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            when (val result = safeApiCall { repository.getPets() }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pets = result.data
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null, successMessage = null)
    }

    fun onSpeciesChanged(value: String) {
        _uiState.value = _uiState.value.copy(species = value, errorMessage = null, successMessage = null)
    }

    fun onBreedChanged(value: String) {
        _uiState.value = _uiState.value.copy(breed = value, errorMessage = null, successMessage = null)
    }

    fun onSexChanged(value: String) {
        _uiState.value = _uiState.value.copy(sex = value, errorMessage = null, successMessage = null)
    }

    fun onBirthDateChanged(value: String) {
        _uiState.value = _uiState.value.copy(birthDate = value, errorMessage = null, successMessage = null)
    }

    fun onWeightKgChanged(value: String) {
        _uiState.value = _uiState.value.copy(weightKg = value, errorMessage = null, successMessage = null)
    }

    fun onBioChanged(value: String) {
        _uiState.value = _uiState.value.copy(bio = value, errorMessage = null, successMessage = null)
    }

    fun createPet() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите имя питомца")
            return
        }

        if (state.species.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите вид питомца")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreating = true,
                errorMessage = null,
                successMessage = null
            )

            val result = safeApiCall {
                repository.createPet(
                    name = state.name.trim(),
                    species = state.species.trim(),
                    breed = state.breed.trim().ifBlank { null },
                    sex = state.sex.trim().ifBlank { null },
                    birthDate = state.birthDate.trim().ifBlank { null },
                    weightKg = state.weightKg.trim().ifBlank { null },
                    bio = state.bio.trim().ifBlank { null }
                )
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        name = "",
                        species = "",
                        breed = "",
                        sex = "",
                        birthDate = "",
                        weightKg = "",
                        bio = "",
                        successMessage = "Питомец создан"
                    )

                    loadPets()
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun setActivePet(id: String) {
        viewModelScope.launch {
            when (val result = safeApiCall { repository.setActivePet(id) }) {
                is AppResult.Success -> {
                    loadPets()
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun loadPetsIfNeeded() {
        val state = _uiState.value

        if (state.pets.isNotEmpty() || state.isLoading) {
            return
        }

        loadPets()
    }
}