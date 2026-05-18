package com.example.petsocial.core.network.model.profile

data class PublicUserProfileResponse(
    val user_id: String,
    val name: String,
    val birth_date: String? = null,
    val city: String? = null,
    val bio: String? = null,
    val avatar_url: String? = null,
    val stats: ProfileStatsResponse,
    val pets: List<PublicPetSummaryResponse> = emptyList(),
    val handler: PublicHandlerProfileResponse? = null,
    val created_at: String,
    val updated_at: String
)

data class PublicPetSummaryResponse(
    val id: String,
    val name: String,
    val species: String,
    val breed: String? = null,
    val sex: String? = null,
    val birth_date: String? = null,
    val bio: String? = null,
    val photo_url: String? = null,
    val personality_tags: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val matching_goal: String? = null,
    val is_active: Boolean,
    val created_at: String,
    val updated_at: String
)

data class PublicHandlerProfileResponse(
    val user_id: String,
    val display_name: String,
    val city: String? = null,
    val bio: String? = null,
    val avatar_url: String? = null,
    val experience_years: Int,
    val conditions: String? = null,
    val rating_avg: Double,
    val reviews_count: Int,
    val services: List<PublicHandlerServiceResponse> = emptyList()
)

data class PublicHandlerServiceResponse(
    val id: String,
    val service_type: String,
    val title: String,
    val description: String? = null,
    val price_cents: Int,
    val duration_minutes: Int? = null
)
