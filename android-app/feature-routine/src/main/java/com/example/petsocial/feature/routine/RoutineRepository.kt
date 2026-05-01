package com.example.petsocial.feature.routine

import com.example.petsocial.core.network.model.routine.CompletionResponse
import com.example.petsocial.core.network.model.routine.RoutineItemResponse

interface RoutineRepository {

    suspend fun getRoutine(petId: String): List<RoutineItemResponse>

    suspend fun createRoutineItem(
        petId: String,
        title: String,
        category: String,
        scheduleTime: String?,
        notes: String?
    ): RoutineItemResponse

    suspend fun completeRoutineItem(id: String): CompletionResponse
}