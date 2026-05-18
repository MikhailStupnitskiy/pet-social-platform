package com.example.petsocial.core.network.model.matching

data class RecommendationResponse(
    val id: String,
    val owner_id: String,
    val name: String,
    val species: String,
    val breed: String?,
    val sex: String?,
    val birth_date: String?,
    val weight_kg: String?,
    val bio: String?,
    val photo_url: String?,
    val personality_tags: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val health_notes: String?,
    val matching_goal: String?,
    val search_radius_meters: Int = 3000,
    val latitude: String?,
    val longitude: String?,
    val distance_meters: Int?,
    val compatibility_score: Int = 0,
    val score_reasons: List<String> = emptyList(),
    val goal: String = "walk",
    val is_active: Boolean,
    val created_at: String,
    val updated_at: String
)
