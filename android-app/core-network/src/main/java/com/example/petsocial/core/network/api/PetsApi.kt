package com.example.petsocial.core.network.api

import com.example.petsocial.core.network.model.pets.CreatePetRequest
import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.network.model.pets.PublicPetProfileResponse
import com.example.petsocial.core.network.model.pets.UpdatePetRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface PetsApi {

    @GET("v1/pets/")
    suspend fun getPets(): List<PetResponse>

    @POST("v1/pets/")
    suspend fun createPet(
        @Body request: CreatePetRequest
    ): PetResponse

    @GET("v1/pets/{id}")
    suspend fun getPetById(
        @Path("id") id: String
    ): PetResponse

    @GET("v1/pets/public/{id}")
    suspend fun getPublicPetById(
        @Path("id") id: String
    ): PublicPetProfileResponse

    @PATCH("v1/pets/{id}")
    suspend fun updatePet(
        @Path("id") id: String,
        @Body request: UpdatePetRequest
    ): PetResponse

    @POST("v1/pets/{id}/set-active")
    suspend fun setActivePet(
        @Path("id") id: String
    )
}
