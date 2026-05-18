package com.example.petsocial.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.session.SessionEventBus
import com.example.petsocial.core.datastore.auth.TokenStorage
import com.example.petsocial.core.network.model.pets.PublicPetProfileResponse
import com.example.petsocial.core.network.model.profile.PublicUserProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class PublicUserProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authToken: String? = null,
    val profile: PublicUserProfileResponse? = null
)

data class PublicPetProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authToken: String? = null,
    val pet: PublicPetProfileResponse? = null
)

@HiltViewModel
class PublicUserProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val tokenStorage: TokenStorage,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicUserProfileUiState(isLoading = true))
    val uiState: StateFlow<PublicUserProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tokenStorage.token.collect { token ->
                _uiState.value = _uiState.value.copy(authToken = token)
            }
        }
    }

    fun load(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = safeApiCall { repository.getPublicProfile(userId) }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        profile = result.data
                    )
                }
                is AppResult.Error -> handleError(result.error)
            }
        }
    }

    private fun handleError(error: AppError) {
        if (error is AppError.Unauthorized) {
            sessionEventBus.notifyUnauthorized()
            return
        }
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = error.message
        )
    }
}

@HiltViewModel
class PublicPetProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val tokenStorage: TokenStorage,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublicPetProfileUiState(isLoading = true))
    val uiState: StateFlow<PublicPetProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tokenStorage.token.collect { token ->
                _uiState.value = _uiState.value.copy(authToken = token)
            }
        }
    }

    fun load(petId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = safeApiCall { repository.getPublicPet(petId) }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pet = result.data
                    )
                }
                is AppResult.Error -> handleError(result.error)
            }
        }
    }

    private fun handleError(error: AppError) {
        if (error is AppError.Unauthorized) {
            sessionEventBus.notifyUnauthorized()
            return
        }
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = error.message
        )
    }
}
