package com.example.petsocial.feature.routine

import com.example.petsocial.core.network.api.RoutineApi
import com.example.petsocial.core.network.model.routine.CompletionResponse
import com.example.petsocial.core.network.model.routine.CreateRoutineItemRequest
import com.example.petsocial.core.network.model.routine.RoutineItemResponse
import javax.inject.Inject

class DefaultRoutineRepository @Inject constructor(
    private val routineApi: RoutineApi
) : RoutineRepository {

    override suspend fun getRoutine(petId: String): List<RoutineItemResponse> {
        return routineApi.getRoutine(petId)
    }

    override suspend fun createRoutineItem(
        petId: String,
        title: String,
        category: String,
        scheduleTime: String?,
        notes: String?
    ): RoutineItemResponse {
        return routineApi.createRoutineItem(
            CreateRoutineItemRequest(
                pet_id = petId,
                title = title,
                category = category,
                schedule_time = scheduleTime,
                notes = notes
            )
        )
    }

    override suspend fun completeRoutineItem(id: String): CompletionResponse {
        return routineApi.completeRoutineItem(id)
    }
}