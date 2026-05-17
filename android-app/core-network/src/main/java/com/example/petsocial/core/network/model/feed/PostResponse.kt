package com.example.petsocial.core.network.model.feed

data class PostResponse(
    val id: String,
    val author_user_id: String,
    val author_name: String,
    val author_email: String = "",
    val author_city: String? = null,
    val author_bio: String? = null,
    val author_avatar_url: String? = null,
    val pet_id: String,
    val pet_name: String,
    val pet_species: String,
    val pet_breed: String? = null,
    val pet_sex: String? = null,
    val pet_birth_date: String? = null,
    val pet_bio: String? = null,
    val pet_photo_url: String? = null,
    val pet_personality_tags: List<String> = emptyList(),
    val pet_interests: List<String> = emptyList(),
    val body: String,
    val image_url: String?,
    val comments_count: Int = 0,
    val reactions_count: Int = 0,
    val my_reaction: String? = null,
    val reaction_counts: Map<String, Int> = emptyMap(),
    val created_at: String,
    val updated_at: String
)
