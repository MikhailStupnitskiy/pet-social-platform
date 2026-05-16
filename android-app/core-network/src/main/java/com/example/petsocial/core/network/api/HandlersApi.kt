package com.example.petsocial.core.network.api

import com.example.petsocial.core.network.model.handlers.CreateHandlerReviewRequest
import com.example.petsocial.core.network.model.handlers.CreateServiceRequestRequest
import com.example.petsocial.core.network.model.handlers.HandlerProfileResponse
import com.example.petsocial.core.network.model.handlers.HandlerReviewResponse
import com.example.petsocial.core.network.model.handlers.HandlerServiceResponse
import com.example.petsocial.core.network.model.handlers.ServiceRequestResponse
import com.example.petsocial.core.network.model.handlers.UpdateServiceRequestStatusRequest
import com.example.petsocial.core.network.model.handlers.UpsertHandlerProfileRequest
import com.example.petsocial.core.network.model.handlers.UpsertHandlerServiceRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface HandlersApi {

    @GET("v1/handlers/")
    suspend fun getHandlers(
        @Query("city") city: String? = null,
        @Query("service_type") serviceType: String? = null,
        @Query("min_rating") minRating: Double? = null,
        @Query("active_only") activeOnly: Boolean = true
    ): List<HandlerProfileResponse>

    @GET("v1/handlers/{id}")
    suspend fun getHandler(
        @Path("id") id: String
    ): HandlerProfileResponse

    @GET("v1/handlers/me")
    suspend fun getMyHandlerProfile(): HandlerProfileResponse

    @PUT("v1/handlers/me")
    suspend fun upsertMyHandlerProfile(
        @Body request: UpsertHandlerProfileRequest
    ): HandlerProfileResponse

    @POST("v1/handlers/me/services")
    suspend fun createService(
        @Body request: UpsertHandlerServiceRequest
    ): HandlerServiceResponse

    @PATCH("v1/handlers/me/services/{id}")
    suspend fun updateService(
        @Path("id") id: String,
        @Body request: UpsertHandlerServiceRequest
    ): HandlerServiceResponse

    @DELETE("v1/handlers/me/services/{id}")
    suspend fun deleteService(
        @Path("id") id: String
    )

    @GET("v1/service-requests/")
    suspend fun getServiceRequests(
        @Query("role") role: String = "client",
        @Query("status") status: String? = null
    ): List<ServiceRequestResponse>

    @POST("v1/service-requests/")
    suspend fun createServiceRequest(
        @Body request: CreateServiceRequestRequest
    ): ServiceRequestResponse

    @PATCH("v1/service-requests/{id}/status")
    suspend fun updateServiceRequestStatus(
        @Path("id") id: String,
        @Body request: UpdateServiceRequestStatusRequest
    ): ServiceRequestResponse

    @POST("v1/service-requests/{id}/review")
    suspend fun createReview(
        @Path("id") id: String,
        @Body request: CreateHandlerReviewRequest
    ): HandlerReviewResponse
}
