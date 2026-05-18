package com.example.petsocial.feature.matching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.network.api.PetsApi
import com.example.petsocial.core.datastore.auth.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.session.SessionEventBus

@HiltViewModel
class MatchingViewModel @Inject constructor(
    private val repository: MatchingRepository,
    private val petsApi: PetsApi,
    private val tokenStorage: TokenStorage,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchingUiState(isLoading = true))
    val uiState: StateFlow<MatchingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tokenStorage.token.collect { token ->
                _uiState.value = _uiState.value.copy(authToken = token)
            }
        }
    }

    private fun handleUnauthorized(error: AppError): Boolean {
        if (error is AppError.Unauthorized) {
            sessionEventBus.notifyUnauthorized()
            return true
        }

        return false
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            when (val petsResult = safeApiCall { petsApi.getPets() }) {
                is AppResult.Error -> {
                    if (handleUnauthorized(petsResult.error)) {
                        return@launch
                    }

                    _uiState.value = MatchingUiState(
                        isLoading = false,
                        errorMessage = petsResult.error.message
                    )
                }

                is AppResult.Success -> {
                    val activePet = petsResult.data.firstOrNull { it.is_active }

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

                    val recommendationsResult = safeApiCall {
                        repository.getRecommendations(
                            petId = activePet.id,
                            goal = _uiState.value.goal,
                            filters = _uiState.value.filters,
                            species = activePet.species
                        )
                    }

                    val matchesResult = safeApiCall {
                        repository.getMatches(activePet.id)
                    }

                    if (recommendationsResult is AppResult.Error) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = recommendationsResult.error.message
                        )
                        return@launch
                    }

                    if (matchesResult is AppResult.Error) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = matchesResult.error.message
                        )
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                            activePet = activePet,
                            recommendations = (recommendationsResult as AppResult.Success).data,
                            matches = (matchesResult as AppResult.Success).data,
                            locationMessage = locationMessageFor(activePet.latitude, activePet.longitude)
                        )
                }
            }
        }
    }

    fun selectGoal(goal: MatchingGoal) {
        if (_uiState.value.goal == goal) return
        _uiState.value = _uiState.value.copy(goal = goal, successMessage = null, errorMessage = null)
        load()
    }

    fun showFilters() {
        _uiState.value = _uiState.value.copy(isFilterSheetVisible = true)
    }

    fun hideFilters() {
        _uiState.value = _uiState.value.copy(isFilterSheetVisible = false)
    }

    fun updateFilters(filters: MatchingFilters) {
        _uiState.value = _uiState.value.copy(filters = filters, isFilterSheetVisible = false)
        load()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(filters = MatchingFilters(), isFilterSheetVisible = false)
        load()
    }

    fun updateActivePetLocation(latitude: Double, longitude: Double) {
        val activePet = _uiState.value.activePet ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshingLocation = true)
            when (val result = safeApiCall { repository.updatePetLocation(activePet.id, latitude, longitude) }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isRefreshingLocation = false,
                        activePet = result.data,
                        locationMessage = "Геопозиция обновлена"
                    )
                    load()
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) return@launch
                    _uiState.value = _uiState.value.copy(
                        isRefreshingLocation = false,
                        locationMessage = "Не удалось обновить геопозицию"
                    )
                }
            }
        }
    }

    fun markLocationDenied() {
        _uiState.value = _uiState.value.copy(locationMessage = "Геопозиция выключена, расстояние может быть неточным")
    }

    fun openProfile(targetPetId: String, onOpen: (String) -> Unit) {
        val sourcePetId = _uiState.value.activePet?.id
        if (sourcePetId != null) {
            viewModelScope.launch {
                safeApiCall { repository.sendProfileOpen(sourcePetId, targetPetId) }
            }
        }
        onOpen(targetPetId)
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

            val swipeResult = safeApiCall {
                when (action) {
                    SwipeAction.Like -> repository.like(
                        sourcePetId = activePet.id,
                        targetPetId = targetPetId
                    )

                    SwipeAction.Pass -> repository.pass(
                        sourcePetId = activePet.id,
                        targetPetId = targetPetId
                    )
                }
            }

            when (swipeResult) {
                is AppResult.Error -> {
                    if (handleUnauthorized(swipeResult.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isActionLoading = false,
                        errorMessage = swipeResult.error.message
                    )
                }

                is AppResult.Success -> {
                    val updatedRecommendations = _uiState.value.recommendations
                        .filterNot { it.id == targetPetId }

                    val matchesResult = safeApiCall {
                        repository.getMatches(activePet.id)
                    }

                    when (matchesResult) {
                        is AppResult.Error -> {
                            if (handleUnauthorized(matchesResult.error)) {
                                return@launch
                            }

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isActionLoading = false,
                                errorMessage = matchesResult.error.message
                            )
                        }

                        is AppResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isActionLoading = false,
                                recommendations = updatedRecommendations,
                                matches = matchesResult.data,
                                successMessage = if (swipeResult.data.is_match) {
                                    "У вас новый мэтч!"
                                } else {
                                    when (action) {
                                        SwipeAction.Like -> "Лайк отправлен"
                                        SwipeAction.Pass -> "Анкета пропущена"
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private enum class SwipeAction {
        Like,
        Pass
    }

    private fun locationMessageFor(latitude: String?, longitude: String?): String {
        return if (latitude.isNullOrBlank() || longitude.isNullOrBlank()) {
            "Разрешите геопозицию, чтобы видеть расстояние"
        } else {
            "Расстояние считается от текущей геопозиции"
        }
    }
}
