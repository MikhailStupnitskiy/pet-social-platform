package com.example.petsocial.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(
                isLoading = true
            )

            try {
                val profile = repository.getMe()

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
            } catch (e: HttpException) {
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    errorMessage = when (e.code()) {
                        401 -> "Сессия истекла. Войдите снова"
                        else -> "Ошибка сервера: ${e.code()}"
                    }
                )
            } catch (e: IOException) {
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    errorMessage = "Ошибка сети"
                )
            } catch (e: Exception) {
                _uiState.value = ProfileUiState(
                    isLoading = false,
                    errorMessage = "Неизвестная ошибка"
                )
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

            try {
                val updated = repository.updateMe(
                    name = state.name.trim(),
                    birthDate = state.birthDate.trim().ifBlank { null },
                    city = state.city.trim().ifBlank { null },
                    bio = state.bio.trim().ifBlank { null },
                    avatarUrl = state.avatarUrl.trim().ifBlank { null }
                )

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
            } catch (e: HttpException) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Ошибка сервера: ${e.code()}"
                )
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Ошибка сети"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Неизвестная ошибка"
                )
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401 -> "Сессия истекла. Войдите снова"
                    else -> "Ошибка сервера: ${e.code()}"
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isLoading = false,
                    errorMessage = message
                )
            }
        }
    }
}