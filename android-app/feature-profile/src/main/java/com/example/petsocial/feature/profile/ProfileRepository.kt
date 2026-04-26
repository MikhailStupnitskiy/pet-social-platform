package com.example.petsocial.feature.profile

import com.example.petsocial.core.network.model.profile.ProfileResponse

interface ProfileRepository {

    suspend fun getMe(): ProfileResponse

    suspend fun updateMe(
        name: String,
        birthDate: String?,
        city: String?,
        bio: String?,
        avatarUrl: String?
    ): ProfileResponse
}