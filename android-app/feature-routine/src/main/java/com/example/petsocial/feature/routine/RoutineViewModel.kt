package com.example.petsocial.feature.routine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.network.api.PetsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.session.SessionEventBus

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val repository: RoutineRepository,
    private val petsApi: PetsApi,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineUiState(isLoading = true))
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()

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
                    _uiState.value = _uiState.value.copy(
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
                            items = emptyList(),
                            errorMessage = "Сначала выберите активного питомца"
                        )
                        return@launch
                    }

                    when (val routineResult = safeApiCall { repository.getRoutine(activePet.id) }) {
                        is AppResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                activePet = activePet,
                                items = routineResult.data
                            )
                        }

                        is AppResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                activePet = activePet,
                                errorMessage = routineResult.error.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            title = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onCategoryChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            category = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onScheduleTimeChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            scheduleTime = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onNotesChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            notes = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun createRoutineItem() {
        val state = _uiState.value
        val activePet = state.activePet

        if (activePet == null) {
            _uiState.value = state.copy(errorMessage = "Активный питомец не выбран")
            return
        }

        if (state.title.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите название задачи")
            return
        }

        if (state.category.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите категорию")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreating = true,
                errorMessage = null,
                successMessage = null
            )

            when (
                val result = safeApiCall {
                    repository.createRoutineItem(
                        petId = activePet.id,
                        title = state.title.trim(),
                        category = state.category.trim(),
                        scheduleTime = state.scheduleTime.trim().ifBlank { null },
                        notes = state.notes.trim().ifBlank { null }
                    )
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        title = "",
                        category = "",
                        scheduleTime = "",
                        notes = "",
                        successMessage = "Задача создана"
                    )

                    load()
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

    fun completeRoutineItem(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCompleting = true,
                errorMessage = null,
                successMessage = null
            )

            when (val result = safeApiCall { repository.completeRoutineItem(id) }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCompleting = false,
                        successMessage = "Задача выполнена"
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCompleting = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }
}