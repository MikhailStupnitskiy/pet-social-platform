package com.example.petsocial.feature.feed

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.petsocial.core.network.api.FeedApi
import com.example.petsocial.core.network.model.feed.CommentResponse
import com.example.petsocial.core.network.model.feed.CreateCommentRequest
import com.example.petsocial.core.network.model.feed.CreatePostRequest
import com.example.petsocial.core.network.model.feed.PostResponse
import com.example.petsocial.core.network.model.feed.SetReactionRequest
import com.example.petsocial.core.network.model.feed.UpdateCommentRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class DefaultFeedRepository @Inject constructor(
    private val feedApi: FeedApi,
    @ApplicationContext private val context: Context
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

    override suspend fun uploadImage(uri: Uri): String {
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("Cannot open selected image")
        val mediaType = resolver.getType(uri)?.toMediaTypeOrNull()
        val requestBody = bytes.toRequestBody(mediaType)
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = resolver.displayName(uri),
            body = requestBody
        )

        return feedApi.uploadImage(part).image_url
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

    private fun android.content.ContentResolver.displayName(uri: Uri): String {
        val fallback = "image"
        return query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else fallback
            } else {
                fallback
            }
        } ?: fallback
    }
}
