package com.example.petsocial.feature.handlers

import com.example.petsocial.core.network.api.HandlersApi
import com.example.petsocial.core.network.model.handlers.CreateHandlerReviewRequest
import com.example.petsocial.core.network.model.handlers.CreateServiceRequestRequest
import com.example.petsocial.core.network.model.handlers.HandlerProfileResponse
import com.example.petsocial.core.network.model.handlers.HandlerReviewResponse
import com.example.petsocial.core.network.model.handlers.HandlerServiceResponse
import com.example.petsocial.core.network.model.handlers.ServiceRequestResponse
import com.example.petsocial.core.network.model.handlers.UpdateServiceRequestStatusRequest
import com.example.petsocial.core.network.model.handlers.UpsertHandlerProfileRequest
import com.example.petsocial.core.network.model.handlers.UpsertHandlerServiceRequest
import javax.inject.Inject

class DefaultHandlersRepository @Inject constructor(
    private val api: HandlersApi
) : HandlersRepository {

    override suspend fun getHandlers(city: String?, serviceType: String?, minRating: Double?): List<HandlerProfileResponse> {
        return api.getHandlers(city = city, serviceType = serviceType, minRating = minRating)
    }

    override suspend fun getMyProfile(): HandlerProfileResponse {
        return api.getMyHandlerProfile()
    }

    override suspend fun saveMyProfile(
        displayName: String,
        city: String?,
        bio: String?,
        experienceYears: Int,
        conditions: String?,
        isActive: Boolean
    ): HandlerProfileResponse {
        return api.upsertMyHandlerProfile(
            UpsertHandlerProfileRequest(
                display_name = displayName,
                city = city,
                bio = bio,
                avatar_url = null,
                experience_years = experienceYears,
                conditions = conditions,
                is_active = isActive,
                latitude = null,
                longitude = null
            )
        )
    }

    override suspend fun createService(
        serviceType: String,
        title: String,
        description: String?,
        priceCents: Int,
        durationMinutes: Int?,
        isActive: Boolean
    ): HandlerServiceResponse {
        return api.createService(
            UpsertHandlerServiceRequest(
                service_type = serviceType,
                title = title,
                description = description,
                price_cents = priceCents,
                duration_minutes = durationMinutes,
                is_active = isActive
            )
        )
    }

    override suspend fun deleteService(id: String) {
        api.deleteService(id)
    }

    override suspend fun getRequests(role: String): List<ServiceRequestResponse> {
        return api.getServiceRequests(role = role)
    }

    override suspend fun createRequest(
        serviceId: String,
        petId: String,
        requestedDate: String,
        requestedTime: String,
        comment: String?
    ): ServiceRequestResponse {
        return api.createServiceRequest(
            CreateServiceRequestRequest(
                service_id = serviceId,
                pet_id = petId,
                requested_date = requestedDate,
                requested_time = requestedTime,
                comment = comment
            )
        )
    }

    override suspend fun updateRequestStatus(id: String, status: String): ServiceRequestResponse {
        return api.updateServiceRequestStatus(
            id = id,
            request = UpdateServiceRequestStatusRequest(status = status)
        )
    }

    override suspend fun createReview(id: String, rating: Int, body: String?): HandlerReviewResponse {
        return api.createReview(
            id = id,
            request = CreateHandlerReviewRequest(rating = rating, body = body)
        )
    }
}
