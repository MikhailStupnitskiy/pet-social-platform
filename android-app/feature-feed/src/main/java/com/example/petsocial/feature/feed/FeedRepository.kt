package com.example.petsocial.feature.feed

import android.net.Uri
import com.example.petsocial.core.network.model.feed.PostResponse
import com.example.petsocial.core.network.model.feed.CommentResponse

interface FeedRepository {

    suspend fun getFeed(): List<PostResponse>

    suspend fun createPost(
        petId: String,
        body: String,
        imageUrl: String?
    ): PostResponse

    suspend fun uploadImage(uri: Uri): String

    suspend fun getComments(postId: String): List<CommentResponse>

    suspend fun createComment(postId: String, body: String): CommentResponse

    suspend fun updateComment(postId: String, commentId: String, body: String): CommentResponse

    suspend fun deleteComment(postId: String, commentId: String)

    suspend fun setReaction(postId: String, reactionType: String): PostResponse

    suspend fun deleteReaction(postId: String): PostResponse
}
