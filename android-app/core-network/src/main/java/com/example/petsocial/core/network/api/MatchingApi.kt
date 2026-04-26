package com.example.petsocial.core.network.api

import com.example.petsocial.core.network.model.matching.MatchResponse
import com.example.petsocial.core.network.model.matching.RecommendationResponse
import com.example.petsocial.core.network.model.matching.SwipeRequest
import com.example.petsocial.core.network.model.matching.SwipeResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MatchingApi {

    @GET("v1/matching/recommendations")
    suspend fun getRecommendations(
        @Query("pet_id") petId: String
    ): List<RecommendationResponse>

    @POST("v1/matching/swipes")
    suspend fun swipe(
        @Body request: SwipeRequest
    ): SwipeResponse

    @GET("v1/matching/matches")
    suspend fun getMatches(
        @Query("pet_id") petId: String
    ): List<MatchResponse>
}