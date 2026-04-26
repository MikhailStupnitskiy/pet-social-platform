package com.example.petsocial.feature.pets

import com.example.petsocial.core.network.api.PetsApi
import com.example.petsocial.core.network.model.pets.CreatePetRequest
import com.example.petsocial.core.network.model.pets.PetResponse
import javax.inject.Inject

class DefaultPetsRepository @Inject constructor(
    private val petsApi: PetsApi
) : PetsRepository {

    override suspend fun getPets(): List<PetResponse> {
        return petsApi.getPets()
    }

    override suspend fun createPet(
        name: String,
        species: String,
        breed: String?,
        sex: String?,
        birthDate: String?,
        weightKg: String?,
        bio: String?
    ): PetResponse {
        return petsApi.createPet(
            CreatePetRequest(
                name = name,
                species = species,
                breed = breed,
                sex = sex,
                birth_date = birthDate,
                weight_kg = weightKg,
                bio = bio
            )
        )
    }

    override suspend fun setActivePet(id: String) {
        petsApi.setActivePet(id)
    }
}