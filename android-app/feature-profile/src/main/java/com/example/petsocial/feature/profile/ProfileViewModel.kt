package com.example.petsocial.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.common.session.SessionEventBus

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private fun handleUnauthorized(error: AppError): Boolean {
        if (error is AppError.Unauthorized) {
            sessionEventBus.notifyUnauthorized()
            return true
        }

        return false
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(
                isLoading = true
            )

            when (val result = safeApiCall { repository.getMe() }) {
                is AppResult.Success -> {
                    val profile = result.data

                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        userId = profile.user_id,
                        email = profile.email,
                        name = profile.name,
                        birthDate = profile.birth_date.orEmpty(),
                        city = profile.city.orEmpty(),
                        bio = profile.bio.orEmpty(),
                        avatarUrl = profile.avatar_url.orEmpty()
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(name = value, errorMessage = null, successMessage = null)
    }

    fun onBirthDateChanged(value: String) {
        _uiState.value = _uiState.value.copy(birthDate = value, errorMessage = null, successMessage = null)
    }

    fun onCityChanged(value: String) {
        _uiState.value = _uiState.value.copy(city = value, errorMessage = null, successMessage = null)
    }

    fun onBioChanged(value: String) {
        _uiState.value = _uiState.value.copy(bio = value, errorMessage = null, successMessage = null)
    }

    fun onAvatarUrlChanged(value: String) {
        _uiState.value = _uiState.value.copy(avatarUrl = value, errorMessage = null, successMessage = null)
    }

    fun saveProfile() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите имя")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                successMessage = null
            )

            when (
                val result = safeApiCall {
                    repository.updateMe(
                        name = state.name.trim(),
                        birthDate = state.birthDate.trim().ifBlank { null },
                        city = state.city.trim().ifBlank { null },
                        bio = state.bio.trim().ifBlank { null },
                        avatarUrl = state.avatarUrl.trim().ifBlank { null }
                    )
                }
            ) {
                is AppResult.Success -> {
                    val updated = result.data

                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        userId = updated.user_id,
                        email = updated.email,
                        name = updated.name,
                        birthDate = updated.birth_date.orEmpty(),
                        city = updated.city.orEmpty(),
                        bio = updated.bio.orEmpty(),
                        avatarUrl = updated.avatar_url.orEmpty(),
                        successMessage = "Профиль сохранён"
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }
}