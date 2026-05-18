package com.example.petsocial.core.network.api

import com.example.petsocial.core.network.model.routine.CompletionResponse
import com.example.petsocial.core.network.model.routine.CreateRoutineItemRequest
import com.example.petsocial.core.network.model.routine.RoutineItemResponse
import com.example.petsocial.core.network.model.routine.UpdateRoutineItemRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RoutineApi {

    @GET("v1/routine/")
    suspend fun getRoutine(
        @Query("pet_id") petId: String
    ): List<RoutineItemResponse>

    @POST("v1/routine/")
    suspend fun createRoutineItem(
        @Body request: CreateRoutineItemRequest
    ): RoutineItemResponse

    @PATCH("v1/routine/{id}")
    suspend fun updateRoutineItem(
        @Path("id") id: String,
        @Body request: UpdateRoutineItemRequest
    ): RoutineItemResponse

    @POST("v1/routine/{id}/complete")
    suspend fun completeRoutineItem(
        @Path("id") id: String
    ): CompletionResponse

    @DELETE("v1/routine/{id}")
    suspend fun deleteRoutineItem(
        @Path("id") id: String
    )
}
