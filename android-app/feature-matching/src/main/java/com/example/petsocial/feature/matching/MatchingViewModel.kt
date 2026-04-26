package com.example.petsocial.feature.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.network.api.PetsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MatchingViewModel @Inject constructor(
    private val repository: MatchingRepository,
    private val petsApi: PetsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchingUiState(isLoading = true))
    val uiState: StateFlow<MatchingUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                val pets = petsApi.getPets()
                val activePet = pets.firstOrNull { it.is_active }

                if (activePet == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        activePet = null,
                        recommendations = emptyList(),
                        matches = emptyList(),
                        errorMessage = "Сначала выберите активного питомца"
                    )
                    return@launch
                }

                val recommendations = repository.getRecommendations(activePet.id)
                val matches = repository.getMatches(activePet.id)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activePet = activePet,
                    recommendations = recommendations,
                    matches = matches
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Ошибка сервера: ${e.code()}"
                )
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Ошибка сети"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Неизвестная ошибка"
                )
            }
        }
    }

    fun like(targetPetId: String) {
        swipe(targetPetId = targetPetId, action = SwipeAction.Like)
    }

    fun pass(targetPetId: String) {
        swipe(targetPetId = targetPetId, action = SwipeAction.Pass)
    }

    private fun swipe(
        targetPetId: String,
        action: SwipeAction
    ) {
        val activePet = _uiState.value.activePet
        if (activePet == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Активный питомец не выбран"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isActionLoading = true,
                errorMessage = null,
                successMessage = null
            )

            try {
                val response = when (action) {
                    SwipeAction.Like -> repository.like(
                        sourcePetId = activePet.id,
                        targetPetId = targetPetId
                    )

                    SwipeAction.Pass -> repository.pass(
                        sourcePetId = activePet.id,
                        targetPetId = targetPetId
                    )
                }

                val updatedRecommendations = _uiState.value.recommendations
                    .filterNot { it.id == targetPetId }

                val updatedMatches = repository.getMatches(activePet.id)

                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    recommendations = updatedRecommendations,
                    matches = updatedMatches,
                    successMessage = if (response.is_match) {
                        "У вас новый match!"
                    } else {
                        when (action) {
                            SwipeAction.Like -> "Лайк отправлен"
                            SwipeAction.Pass -> "Анкета пропущена"
                        }
                    }
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    errorMessage = "Ошибка сервера: ${e.code()}"
                )
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    errorMessage = "Ошибка сети"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isActionLoading = false,
                    errorMessage = "Неизвестная ошибка"
                )
            }
        }
    }

    private enum class SwipeAction {
        Like,
        Pass
    }
}