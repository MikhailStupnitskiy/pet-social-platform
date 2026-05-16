package com.example.petsocial.core.network.api

import com.example.petsocial.core.network.model.feed.ImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImagesApi {
    @Multipart
    @POST("v1/images")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): ImageUploadResponse
}
