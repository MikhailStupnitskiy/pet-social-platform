package com.example.petsocial.feature.feed

import com.example.petsocial.core.network.api.FeedApi
import com.example.petsocial.core.network.model.feed.CommentResponse
import com.example.petsocial.core.network.model.feed.CreateCommentRequest
import com.example.petsocial.core.network.model.feed.CreatePostRequest
import com.example.petsocial.core.network.model.feed.PostResponse
import com.example.petsocial.core.network.model.feed.SetReactionRequest
import com.example.petsocial.core.network.model.feed.UpdateCommentRequest
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

    override suspend fun getComments(postId: String): List<CommentResponse> {
        return feedApi.getComments(postId)
    }

    override suspend fun createComment(postId: String, body: String): CommentResponse {
        return feedApi.createComment(
            id = postId,
            request = CreateCommentRequest(body = body)
        )
    }

    override suspend fun updateComment(postId: String, commentId: String, body: String): CommentResponse {
        return feedApi.updateComment(
            id = postId,
            commentId = commentId,
            request = UpdateCommentRequest(body = body)
        )
    }

    override suspend fun deleteComment(postId: String, commentId: String) {
        feedApi.deleteComment(
            id = postId,
            commentId = commentId
        )
    }

    override suspend fun setReaction(postId: String, reactionType: String): PostResponse {
        return feedApi.setReaction(
            id = postId,
            request = SetReactionRequest(reaction_type = reactionType)
        )
    }

    override suspend fun deleteReaction(postId: String): PostResponse {
        return feedApi.deleteReaction(postId)
    }
}
