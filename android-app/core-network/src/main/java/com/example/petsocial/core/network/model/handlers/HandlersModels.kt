package com.example.petsocial.core.network.model.handlers

data class HandlerProfileResponse(
    val user_id: String,
    val display_name: String,
    val city: String?,
    val bio: String?,
    val avatar_url: String?,
    val experience_years: Int,
    val conditions: String?,
    val is_active: Boolean,
    val latitude: String?,
    val longitude: String?,
    val rating_avg: Double,
    val reviews_count: Int,
    val services: List<HandlerServiceResponse>,
    val created_at: String,
    val updated_at: String
)

data class HandlerServiceResponse(
    val id: String,
    val handler_user_id: String,
    val service_type: String,
    val title: String,
    val description: String?,
    val price_cents: Int,
    val duration_minutes: Int?,
    val is_active: Boolean,
    val created_at: String,
    val updated_at: String
)

data class UpsertHandlerProfileRequest(
    val display_name: String,
    val city: String?,
    val bio: String?,
    val avatar_url: String?,
    val experience_years: Int,
    val conditions: String?,
    val is_active: Boolean,
    val latitude: String?,
    val longitude: String?
)

data class UpsertHandlerServiceRequest(
    val service_type: String,
    val title: String,
    val description: String?,
    val price_cents: Int,
    val duration_minutes: Int?,
    val is_active: Boolean
)

data class CreateServiceRequestRequest(
    val service_id: String,
    val pet_id: String,
    val requested_date: String,
    val requested_time: String,
    val comment: String?
)

data class UpdateServiceRequestStatusRequest(
    val status: String
)

data class ServiceRequestResponse(
    val id: String,
    val service_id: String,
    val client_user_id: String,
    val client_name: String,
    val handler_user_id: String,
    val handler_name: String,
    val pet_id: String,
    val pet_name: String,
    val service_type: String,
    val service_title: String,
    val requested_date: String,
    val requested_time: String,
    val comment: String?,
    val status: String,
    val review: HandlerReviewResponse?,
    val created_at: String,
    val updated_at: String
)

data class CreateHandlerReviewRequest(
    val rating: Int,
    val body: String?
)

data class HandlerReviewResponse(
    val id: String,
    val request_id: String,
    val handler_user_id: String,
    val client_user_id: String,
    val client_name: String,
    val rating: Int,
    val body: String?,
    val created_at: String
)
