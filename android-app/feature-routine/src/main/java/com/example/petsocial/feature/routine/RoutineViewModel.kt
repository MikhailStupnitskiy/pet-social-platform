package com.example.petsocial.feature.routine

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.session.SessionEventBus
import com.example.petsocial.core.network.api.HandlersApi
import com.example.petsocial.core.network.api.PetsApi
import com.example.petsocial.feature.routine.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val repository: RoutineRepository,
    private val petsApi: PetsApi,
    private val handlersApi: HandlersApi,
    private val sessionEventBus: SessionEventBus,
    @ApplicationContext appContext: Context
) : ViewModel() {

    private val completionPrefs = appContext.getSharedPreferences(COMPLETION_PREFS_NAME, Context.MODE_PRIVATE)
    private val reminderScheduler = ReminderScheduler(appContext)

    private val _uiState = MutableStateFlow(
        RoutineUiState(
            isLoading = true,
            completedRoutineOccurrences = completionPrefs.getStringSet(COMPLETION_KEYS_PREF, emptySet()).orEmpty()
        )
    )
    val uiState: StateFlow<RoutineUiState> = _uiState.asStateFlow()

    private fun saveCompletedOccurrences(values: Set<String>) {
        completionPrefs.edit()
            .putStringSet(COMPLETION_KEYS_PREF, values)
            .apply()
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

                    val routineResult = safeApiCall { repository.getRoutine(activePet.id) }
                    val requestsResult = safeApiCall { handlersApi.getServiceRequests(role = "client") }

                    if (routineResult is AppResult.Error && handleUnauthorized(routineResult.error)) {
                        return@launch
                    }
                    if (requestsResult is AppResult.Error && handleUnauthorized(requestsResult.error)) {
                        return@launch
                    }

                    if (routineResult is AppResult.Error) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            activePet = activePet,
                            errorMessage = routineResult.error.message
                        )
                        return@launch
                    }

                    val routineItems = (routineResult as AppResult.Success).data
                    val serviceRequests = (requestsResult as? AppResult.Success)?.data.orEmpty()
                    reminderScheduler.schedule(routineItems, serviceRequests)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        activePet = activePet,
                        items = routineItems,
                        serviceRequests = serviceRequests
                    )
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

    fun onRepeatRuleChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            repeatRule = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onSelectedDateChanged(value: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = value, successMessage = null)
    }

    fun showCreateSheet() {
        _uiState.value = _uiState.value.copy(isCreateSheetVisible = true, errorMessage = null, successMessage = null)
    }

    fun hideCreateSheet() {
        _uiState.value = _uiState.value.copy(isCreateSheetVisible = false)
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
                        repeatRule = state.repeatRule,
                        notes = state.notes.trim().ifBlank { null }
                    )
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        title = "",
                        category = "walk",
                        scheduleTime = "",
                        repeatRule = "none",
                        notes = "",
                        isCreateSheetVisible = false,
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
        val selectedDate = _uiState.value.selectedDate
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCompleting = true,
                errorMessage = null,
                successMessage = null
            )

            when (val result = safeApiCall { repository.completeRoutineItem(id) }) {
                is AppResult.Success -> {
                    val completedOccurrences = _uiState.value.completedRoutineOccurrences + routineOccurrenceKey(id, selectedDate)
                    saveCompletedOccurrences(completedOccurrences)

                    _uiState.value = _uiState.value.copy(
                        isCompleting = false,
                        completedRoutineOccurrences = completedOccurrences,
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

    fun deleteRoutineItem(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                errorMessage = null,
                successMessage = null
            )

            when (val result = safeApiCall { repository.deleteRoutineItem(id) }) {
                is AppResult.Success -> {
                    val completedOccurrences = _uiState.value.completedRoutineOccurrences
                        .filterNot { it.startsWith("$id|") }
                        .toSet()
                    saveCompletedOccurrences(completedOccurrences)

                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        items = _uiState.value.items.filterNot { it.id == id },
                        completedRoutineOccurrences = completedOccurrences,
                        successMessage = "Задача удалена"
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }
}

fun routineOccurrenceKey(id: String, date: LocalDate): String = "$id|$date"

private const val COMPLETION_PREFS_NAME = "routine_completion_preferences"
private const val COMPLETION_KEYS_PREF = "completed_occurrence_keys"
