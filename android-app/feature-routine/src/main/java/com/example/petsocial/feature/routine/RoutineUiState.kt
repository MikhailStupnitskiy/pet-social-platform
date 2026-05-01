package com.example.petsocial.feature.routine

import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.network.model.routine.RoutineItemResponse

data class RoutineUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isCompleting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    val activePet: PetResponse? = null,
    val items: List<RoutineItemResponse> = emptyList(),

    val title: String = "",
    val category: String = "",
    val scheduleTime: String = "",
    val notes: String = ""
)