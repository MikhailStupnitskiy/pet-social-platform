package com.example.petsocial.core.network.api

import com.example.petsocial.core.network.model.profile.ProfileResponse
import com.example.petsocial.core.network.model.profile.ProfileStatsResponse
import com.example.petsocial.core.network.model.profile.PublicUserProfileResponse
import com.example.petsocial.core.network.model.profile.UpdateProfileRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path


interface ProfileApi {

    @GET("v1/profile/me")
    suspend fun getMe(): ProfileResponse

    @GET("v1/profile/me/stats")
    suspend fun getStats(): ProfileStatsResponse

    @PATCH("v1/profile/me")
    suspend fun updateMe(
        @Body request: UpdateProfileRequest
    ): ProfileResponse

    @GET("v1/profile/{id}")
    suspend fun getPublicProfile(
        @Path("id") id: String
    ): PublicUserProfileResponse
}
