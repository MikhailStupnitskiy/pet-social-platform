package com.example.petsocial.feature.pets

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.petsocial.core.network.api.ImagesApi
import com.example.petsocial.core.network.api.PetsApi
import com.example.petsocial.core.network.model.pets.CreatePetRequest
import com.example.petsocial.core.network.model.pets.PetResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class DefaultPetsRepository @Inject constructor(
    private val petsApi: PetsApi,
    private val imagesApi: ImagesApi,
    @ApplicationContext private val context: Context
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
        bio: String?,
        photoUrl: String?,
        personalityTags: List<String>,
        interests: List<String>,
        healthNotes: String?,
        matchingGoal: String?
    ): PetResponse {
        return petsApi.createPet(
            CreatePetRequest(
                name = name,
                species = species,
                breed = breed,
                sex = sex,
                birth_date = birthDate,
                weight_kg = weightKg,
                bio = bio,
                photo_url = photoUrl,
                personality_tags = personalityTags,
                interests = interests,
                health_notes = healthNotes,
                matching_goal = matchingGoal
            )
        )
    }

    override suspend fun uploadImage(uri: Uri): String {
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("Cannot open selected image")
        val mediaType = resolver.getType(uri)?.toMediaTypeOrNull()
        val requestBody = bytes.toRequestBody(mediaType)
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = resolver.displayName(uri),
            body = requestBody
        )

        return imagesApi.uploadImage(part).image_url
    }

    override suspend fun setActivePet(id: String) {
        petsApi.setActivePet(id)
    }

    private fun android.content.ContentResolver.displayName(uri: Uri): String {
        val fallback = "image"
        return query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else fallback
            } else {
                fallback
            }
        } ?: fallback
    }
}
