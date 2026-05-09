package com.example.petsocial.core.network.api

import com.example.petsocial.core.network.model.feed.CreatePostRequest
import com.example.petsocial.core.network.model.feed.PostResponse
import com.example.petsocial.core.network.model.feed.UpdatePostRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
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
}
