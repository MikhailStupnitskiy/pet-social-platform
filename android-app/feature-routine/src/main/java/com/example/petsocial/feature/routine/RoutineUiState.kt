package com.example.petsocial.feature.routine

import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.network.model.handlers.ServiceRequestResponse
import com.example.petsocial.core.network.model.routine.RoutineItemResponse
import java.time.LocalDate

data class RoutineUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isCompleting: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    val activePet: PetResponse? = null,
    val items: List<RoutineItemResponse> = emptyList(),
    val serviceRequests: List<ServiceRequestResponse> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val completedRoutineOccurrences: Set<String> = emptySet(),
    val isCreateSheetVisible: Boolean = false,

    val title: String = "",
    val category: String = "walk",
    val scheduleTime: String = "",
    val repeatRule: String = "none",
    val notes: String = ""
)
