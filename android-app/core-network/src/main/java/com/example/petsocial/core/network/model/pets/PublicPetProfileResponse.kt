package com.example.petsocial.core.network.model.pets

data class PublicOwnerSummaryResponse(
    val user_id: String,
    val name: String,
    val city: String? = null,
    val bio: String? = null,
    val avatar_url: String? = null
)

data class PublicPetProfileResponse(
    val id: String,
    val owner: PublicOwnerSummaryResponse,
    val name: String,
    val species: String,
    val breed: String? = null,
    val sex: String? = null,
    val birth_date: String? = null,
    val weight_kg: String? = null,
    val bio: String? = null,
    val photo_url: String? = null,
    val personality_tags: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val matching_goal: String? = null,
    val is_active: Boolean,
    val created_at: String,
    val updated_at: String
)
