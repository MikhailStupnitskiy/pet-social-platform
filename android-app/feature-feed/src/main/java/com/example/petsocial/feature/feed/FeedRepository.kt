package com.example.petsocial.feature.feed

import com.example.petsocial.core.network.model.feed.PostResponse

interface FeedRepository {

    suspend fun getFeed(): List<PostResponse>

    suspend fun createPost(
        petId: String,
        body: String,
        imageUrl: String?
    ): PostResponse
}
