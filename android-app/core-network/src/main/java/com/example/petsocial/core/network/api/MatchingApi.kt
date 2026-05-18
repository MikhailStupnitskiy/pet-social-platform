package com.example.petsocial.core.network.api

import com.example.petsocial.core.network.model.matching.MatchResponse
import com.example.petsocial.core.network.model.matching.MatchingEventRequest
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
        @Query("pet_id") petId: String,
        @Query("goal") goal: String,
        @Query("max_distance_meters") maxDistanceMeters: Int? = null,
        @Query("species") species: String? = null,
        @Query("breed") breed: String? = null,
        @Query("sex") sex: String? = null,
        @Query("age_min_months") ageMinMonths: Int? = null,
        @Query("age_max_months") ageMaxMonths: Int? = null,
        @Query("tags") tags: String? = null,
        @Query("interests") interests: String? = null,
        @Query("has_photo") hasPhoto: Boolean? = null
    ): List<RecommendationResponse>

    @POST("v1/matching/swipes")
    suspend fun swipe(
        @Body request: SwipeRequest
    ): SwipeResponse

    @GET("v1/matching/matches")
    suspend fun getMatches(
        @Query("pet_id") petId: String
    ): List<MatchResponse>

    @POST("v1/matching/events")
    suspend fun sendEvent(
        @Body request: MatchingEventRequest
    )
}
