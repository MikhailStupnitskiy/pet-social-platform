package com.example.petsocial.feature.handlers

import com.example.petsocial.core.network.model.handlers.HandlerProfileResponse
import com.example.petsocial.core.network.model.handlers.HandlerServiceResponse
import com.example.petsocial.core.network.model.handlers.ServiceRequestResponse
import com.example.petsocial.core.network.model.pets.PetResponse

data class HandlersUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSubmitting: Boolean = false,
    val selectedTab: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val handlers: List<HandlerProfileResponse> = emptyList(),
    val clientRequests: List<ServiceRequestResponse> = emptyList(),
    val handlerRequests: List<ServiceRequestResponse> = emptyList(),
    val pets: List<PetResponse> = emptyList(),
    val activePet: PetResponse? = null,
    val myProfile: HandlerProfileResponse? = null,
    val cityFilter: String = "",
    val serviceTypeFilter: String = "",
    val minRatingFilter: String = "",
    val selectedService: HandlerServiceResponse? = null,
    val requestDate: String = "",
    val requestTime: String = "",
    val requestComment: String = "",
    val profileDisplayName: String = "",
    val profileCity: String = "",
    val profileBio: String = "",
    val profileExperienceYears: String = "0",
    val profileConditions: String = "",
    val profileIsActive: Boolean = true,
    val serviceType: String = "walking",
    val serviceTitle: String = "",
    val serviceDescription: String = "",
    val servicePriceRub: String = "",
    val serviceDurationMinutes: String = "",
    val serviceIsActive: Boolean = true,
    val reviewRequestId: String? = null,
    val reviewRating: String = "5",
    val reviewBody: String = ""
)
