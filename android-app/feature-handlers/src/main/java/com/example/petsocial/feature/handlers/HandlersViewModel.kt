package com.example.petsocial.feature.handlers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.session.SessionEventBus
import com.example.petsocial.core.network.api.PetsApi
import com.example.petsocial.core.network.model.handlers.HandlerServiceResponse
import com.example.petsocial.core.network.model.handlers.ServiceRequestResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HandlersViewModel @Inject constructor(
    private val repository: HandlersRepository,
    private val petsApi: PetsApi,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(HandlersUiState(isLoading = true))
    val uiState: StateFlow<HandlersUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, successMessage = null)

            val petsResult = safeApiCall { petsApi.getPets() }
            val handlersResult = safeApiCall {
                val state = _uiState.value
                repository.getHandlers(
                    city = state.cityFilter.trim().ifBlank { null },
                    serviceType = state.serviceTypeFilter.trim().ifBlank { null },
                    minRating = state.minRatingFilter.toDoubleOrNull()
                )
            }
            val clientRequestsResult = safeApiCall { repository.getRequests("client") }
            val handlerRequestsResult = safeApiCall { repository.getRequests("handler") }
            val myProfileResult = safeApiCall { repository.getMyProfile() }

            if (listOf(petsResult, handlersResult, clientRequestsResult, handlerRequestsResult).anyUnauthorized()) {
                sessionEventBus.notifyUnauthorized()
                return@launch
            }

            val error = listOf(petsResult, handlersResult, clientRequestsResult, handlerRequestsResult)
                .firstOrNull { it is AppResult.Error } as? AppResult.Error
            if (error != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.error.message)
                return@launch
            }

            val pets = (petsResult as AppResult.Success).data
            val handlers = (handlersResult as AppResult.Success).data
            val clientRequests = (clientRequestsResult as AppResult.Success).data
            val handlerRequests = (handlerRequestsResult as AppResult.Success).data
            val myProfile = (myProfileResult as? AppResult.Success)?.data

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                handlers = handlers,
                clientRequests = clientRequests,
                handlerRequests = handlerRequests,
                pets = pets,
                activePet = pets.firstOrNull { it.is_active } ?: pets.firstOrNull(),
                myProfile = myProfile
            ).withProfileForm(myProfile)
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index, errorMessage = null, successMessage = null)
    }

    fun onCityFilterChanged(value: String) = update { copy(cityFilter = value) }
    fun onServiceTypeFilterChanged(value: String) = update { copy(serviceTypeFilter = value) }
    fun onMinRatingFilterChanged(value: String) = update { copy(minRatingFilter = value) }
    fun onRequestDateChanged(value: String) = update { copy(requestDate = value) }
    fun onRequestTimeChanged(value: String) = update { copy(requestTime = value) }
    fun onRequestCommentChanged(value: String) = update { copy(requestComment = value) }
    fun onProfileDisplayNameChanged(value: String) = update { copy(profileDisplayName = value) }
    fun onProfileCityChanged(value: String) = update { copy(profileCity = value) }
    fun onProfileBioChanged(value: String) = update { copy(profileBio = value) }
    fun onProfileExperienceChanged(value: String) = update { copy(profileExperienceYears = value) }
    fun onProfileConditionsChanged(value: String) = update { copy(profileConditions = value) }
    fun onProfileActiveChanged(value: Boolean) = update { copy(profileIsActive = value) }
    fun onServiceTypeChanged(value: String) = update { copy(serviceType = value) }
    fun onServiceTitleChanged(value: String) = update { copy(serviceTitle = value) }
    fun onServiceDescriptionChanged(value: String) = update { copy(serviceDescription = value) }
    fun onServicePriceChanged(value: String) = update { copy(servicePriceRub = value) }
    fun onServiceDurationChanged(value: String) = update { copy(serviceDurationMinutes = value) }
    fun onServiceActiveChanged(value: Boolean) = update { copy(serviceIsActive = value) }
    fun onReviewRatingChanged(value: String) = update { copy(reviewRating = value) }
    fun onReviewBodyChanged(value: String) = update { copy(reviewBody = value) }

    fun selectService(service: HandlerServiceResponse) {
        _uiState.value = _uiState.value.copy(
            selectedService = service,
            selectedTab = 0,
            errorMessage = null,
            successMessage = null
        )
    }

    fun startReview(request: ServiceRequestResponse) {
        if (request.review != null) {
            _uiState.value = _uiState.value.copy(
                reviewRequestId = null,
                reviewBody = "",
                successMessage = "Отзыв уже оставлен",
                errorMessage = null
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            selectedTab = 1,
            reviewRequestId = request.id,
            reviewRating = "5",
            reviewBody = "",
            errorMessage = null,
            successMessage = null
        )
    }

    fun search() {
        load()
    }

    fun saveProfile() {
        val state = _uiState.value
        val experience = state.profileExperienceYears.toIntOrNull()
        if (state.profileDisplayName.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите имя хэндлера")
            return
        }
        if (experience == null || experience < 0) {
            _uiState.value = state.copy(errorMessage = "Опыт должен быть неотрицательным числом")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, successMessage = null)
            when (
                val result = safeApiCall {
                    repository.saveMyProfile(
                        displayName = state.profileDisplayName.trim(),
                        city = state.profileCity.trim().ifBlank { null },
                        bio = state.profileBio.trim().ifBlank { null },
                        experienceYears = experience,
                        conditions = state.profileConditions.trim().ifBlank { null },
                        isActive = state.profileIsActive
                    )
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        myProfile = result.data,
                        successMessage = "Профиль хэндлера сохранён"
                    ).withProfileForm(result.data)
                    load()
                }

                is AppResult.Error -> handleError(result.error, saving = true)
            }
        }
    }

    fun createService() {
        val state = _uiState.value
        val priceRub = state.servicePriceRub.toIntOrNull()
        val duration = state.serviceDurationMinutes.trim().ifBlank { null }?.toIntOrNull()
        if (state.serviceTitle.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите название услуги")
            return
        }
        if (priceRub == null || priceRub < 0) {
            _uiState.value = state.copy(errorMessage = "Цена должна быть числом")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, successMessage = null)
            when (
                val result = safeApiCall {
                    repository.createService(
                        serviceType = state.serviceType.trim(),
                        title = state.serviceTitle.trim(),
                        description = state.serviceDescription.trim().ifBlank { null },
                        priceCents = priceRub * 100,
                        durationMinutes = duration,
                        isActive = state.serviceIsActive
                    )
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        serviceTitle = "",
                        serviceDescription = "",
                        servicePriceRub = "",
                        serviceDurationMinutes = "",
                        successMessage = "Услуга добавлена"
                    )
                    load()
                }

                is AppResult.Error -> handleError(result.error, saving = true)
            }
        }
    }

    fun deleteService(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, successMessage = null)
            when (val result = safeApiCall { repository.deleteService(id) }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, successMessage = "Услуга отключена")
                    load()
                }

                is AppResult.Error -> handleError(result.error, saving = true)
            }
        }
    }

    fun createRequest() {
        val state = _uiState.value
        val service = state.selectedService
        val pet = state.activePet
        if (service == null) {
            _uiState.value = state.copy(errorMessage = "Выберите услугу")
            return
        }
        if (pet == null) {
            _uiState.value = state.copy(errorMessage = "Сначала добавьте или выберите питомца")
            return
        }
        if (state.requestDate.isBlank() || state.requestTime.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Укажите дату и время")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null, successMessage = null)
            when (
                val result = safeApiCall {
                    repository.createRequest(
                        serviceId = service.id,
                        petId = pet.id,
                        requestedDate = state.requestDate.trim(),
                        requestedTime = state.requestTime.trim(),
                        comment = state.requestComment.trim().ifBlank { null }
                    )
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        selectedService = null,
                        requestDate = "",
                        requestTime = "",
                        requestComment = "",
                        successMessage = "Заявка создана"
                    )
                    load()
                }

                is AppResult.Error -> handleError(result.error, submitting = true)
            }
        }
    }

    fun updateRequestStatus(id: String, status: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null, successMessage = null)
            when (val result = safeApiCall { repository.updateRequestStatus(id, status) }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSubmitting = false, successMessage = "Статус обновлён")
                    load()
                }

                is AppResult.Error -> handleError(result.error, submitting = true)
            }
        }
    }

    fun submitReview() {
        val state = _uiState.value
        val requestID = state.reviewRequestId
        val rating = state.reviewRating.toIntOrNull()
        if (requestID == null || rating == null) {
            _uiState.value = state.copy(errorMessage = "Выберите заявку и оценку")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null, successMessage = null)
            when (
                val result = safeApiCall {
                    repository.createReview(
                        id = requestID,
                        rating = rating,
                        body = state.reviewBody.trim().ifBlank { null }
                    )
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        reviewRequestId = null,
                        reviewRating = "5",
                        reviewBody = "",
                        successMessage = "Отзыв отправлен"
                    )
                    load()
                }

                is AppResult.Error -> handleError(result.error, submitting = true)
            }
        }
    }

    private fun update(block: HandlersUiState.() -> HandlersUiState) {
        _uiState.value = _uiState.value.block().copy(errorMessage = null, successMessage = null)
    }

    private fun handleError(error: AppError, saving: Boolean = false, submitting: Boolean = false) {
        if (error is AppError.Unauthorized) {
            sessionEventBus.notifyUnauthorized()
            return
        }
        _uiState.value = _uiState.value.copy(
            isSaving = if (saving) false else _uiState.value.isSaving,
            isSubmitting = if (submitting) false else _uiState.value.isSubmitting,
            errorMessage = error.message
        )
    }

    private fun List<AppResult<*>>.anyUnauthorized(): Boolean {
        return any { result -> result is AppResult.Error && result.error is AppError.Unauthorized }
    }
}

private fun HandlersUiState.withProfileForm(profile: com.example.petsocial.core.network.model.handlers.HandlerProfileResponse?): HandlersUiState {
    if (profile == null) {
        return this
    }
    return copy(
        profileDisplayName = profile.display_name,
        profileCity = profile.city.orEmpty(),
        profileBio = profile.bio.orEmpty(),
        profileExperienceYears = profile.experience_years.toString(),
        profileConditions = profile.conditions.orEmpty(),
        profileIsActive = profile.is_active
    )
}
