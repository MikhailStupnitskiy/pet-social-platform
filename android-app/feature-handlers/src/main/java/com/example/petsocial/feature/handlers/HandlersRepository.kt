package com.example.petsocial.feature.handlers

import com.example.petsocial.core.network.model.handlers.HandlerProfileResponse
import com.example.petsocial.core.network.model.handlers.HandlerReviewResponse
import com.example.petsocial.core.network.model.handlers.HandlerServiceResponse
import com.example.petsocial.core.network.model.handlers.ServiceRequestResponse

interface HandlersRepository {
    suspend fun getHandlers(city: String?, serviceType: String?, minRating: Double?): List<HandlerProfileResponse>
    suspend fun getMyProfile(): HandlerProfileResponse
    suspend fun saveMyProfile(
        displayName: String,
        city: String?,
        bio: String?,
        experienceYears: Int,
        conditions: String?,
        isActive: Boolean
    ): HandlerProfileResponse
    suspend fun createService(
        serviceType: String,
        title: String,
        description: String?,
        priceCents: Int,
        durationMinutes: Int?,
        isActive: Boolean
    ): HandlerServiceResponse
    suspend fun deleteService(id: String)
    suspend fun getRequests(role: String): List<ServiceRequestResponse>
    suspend fun createRequest(
        serviceId: String,
        petId: String,
        requestedDate: String,
        requestedTime: String,
        comment: String?
    ): ServiceRequestResponse
    suspend fun updateRequestStatus(id: String, status: String): ServiceRequestResponse
    suspend fun createReview(id: String, rating: Int, body: String?): HandlerReviewResponse
}
