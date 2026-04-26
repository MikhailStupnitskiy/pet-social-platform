package com.example.petsocial.feature.profile

import com.example.petsocial.core.network.api.ProfileApi
import com.example.petsocial.core.network.model.profile.ProfileResponse
import com.example.petsocial.core.network.model.profile.UpdateProfileRequest
import javax.inject.Inject

class DefaultProfileRepository @Inject constructor(
    private val profileApi: ProfileApi
) : ProfileRepository {

    override suspend fun getMe(): ProfileResponse {
        return profileApi.getMe()
    }

    override suspend fun updateMe(
        name: String,
        birthDate: String?,
        city: String?,
        bio: String?,
        avatarUrl: String?
    ): ProfileResponse {
        return profileApi.updateMe(
            UpdateProfileRequest(
                name = name,
                birth_date = birthDate,
                city = city,
                bio = bio,
                avatar_url = avatarUrl
            )
        )
    }
}