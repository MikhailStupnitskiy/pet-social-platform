package com.example.petsocial.feature.routine

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
class RoutineViewModel @Inject constructor(
    private val repository: RoutineRepository,
    private val petsApi: PetsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineUiState(isLoading = true))
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()

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
                        items = emptyList(),
                        errorMessage = "Сначала выберите активного питомца"
                    )
                    return@launch
                }

                val items = repository.getRoutine(activePet.id)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activePet = activePet,
                    items = items
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

            try {
                repository.createRoutineItem(
                    petId = activePet.id,
                    title = state.title.trim(),
                    category = state.category.trim(),
                    scheduleTime = state.scheduleTime.trim().ifBlank { null },
                    notes = state.notes.trim().ifBlank { null }
                )

                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    title = "",
                    category = "",
                    scheduleTime = "",
                    notes = "",
                    successMessage = "Задача создана"
                )

                load()
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    errorMessage = "Ошибка сервера: ${e.code()}"
                )
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    errorMessage = "Ошибка сети"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    errorMessage = "Неизвестная ошибка"
                )
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

            try {
                repository.completeRoutineItem(id)

                _uiState.value = _uiState.value.copy(
                    isCompleting = false,
                    successMessage = "Задача выполнена"
                )
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isCompleting = false,
                    errorMessage = "Ошибка сервера: ${e.code()}"
                )
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isCompleting = false,
                    errorMessage = "Ошибка сети"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCompleting = false,
                    errorMessage = "Неизвестная ошибка"
                )
            }
        }
    }
}