package com.example.petsocial.core.network.api

import com.example.petsocial.core.network.model.feed.CreatePostRequest
import com.example.petsocial.core.network.model.feed.CommentResponse
import com.example.petsocial.core.network.model.feed.CreateCommentRequest
import com.example.petsocial.core.network.model.feed.PostResponse
import com.example.petsocial.core.network.model.feed.SetReactionRequest
import com.example.petsocial.core.network.model.feed.UpdateCommentRequest
import com.example.petsocial.core.network.model.feed.UpdatePostRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FeedApi {

    @GET("v1/feed/")
    suspend fun getFeed(
        @Query("limit") limit: Int = 30
    ): List<PostResponse>

    @POST("v1/feed/posts")
    suspend fun createPost(
        @Body request: CreatePostRequest
    ): PostResponse

    @GET("v1/feed/posts/{id}")
    suspend fun getPost(
        @Path("id") id: String
    ): PostResponse

    @PATCH("v1/feed/posts/{id}")
    suspend fun updatePost(
        @Path("id") id: String,
        @Body request: UpdatePostRequest
    ): PostResponse

    @DELETE("v1/feed/posts/{id}")
    suspend fun deletePost(
        @Path("id") id: String
    )

    @GET("v1/feed/posts/{id}/comments")
    suspend fun getComments(
        @Path("id") id: String
    ): List<CommentResponse>

    @POST("v1/feed/posts/{id}/comments")
    suspend fun createComment(
        @Path("id") id: String,
        @Body request: CreateCommentRequest
    ): CommentResponse

    @PATCH("v1/feed/posts/{id}/comments/{commentId}")
    suspend fun updateComment(
        @Path("id") id: String,
        @Path("commentId") commentId: String,
        @Body request: UpdateCommentRequest
    ): CommentResponse

    @DELETE("v1/feed/posts/{id}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("id") id: String,
        @Path("commentId") commentId: String
    )

    @PUT("v1/feed/posts/{id}/reaction")
    suspend fun setReaction(
        @Path("id") id: String,
        @Body request: SetReactionRequest
    ): PostResponse

    @DELETE("v1/feed/posts/{id}/reaction")
    suspend fun deleteReaction(
        @Path("id") id: String
    ): PostResponse
}
