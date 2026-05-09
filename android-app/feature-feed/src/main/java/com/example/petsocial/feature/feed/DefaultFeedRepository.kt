package com.example.petsocial.feature.feed

import com.example.petsocial.core.network.api.FeedApi
import com.example.petsocial.core.network.model.feed.CreatePostRequest
import com.example.petsocial.core.network.model.feed.PostResponse
import javax.inject.Inject

class DefaultFeedRepository @Inject constructor(
    private val feedApi: FeedApi
) : FeedRepository {

    override suspend fun getFeed(): List<PostResponse> {
        return feedApi.getFeed()
    }

    override suspend fun createPost(
        petId: String,
        body: String,
        imageUrl: String?
    ): PostResponse {
        return feedApi.createPost(
            CreatePostRequest(
                pet_id = petId,
                body = body,
                image_url = imageUrl
            )
        )
    }
}
