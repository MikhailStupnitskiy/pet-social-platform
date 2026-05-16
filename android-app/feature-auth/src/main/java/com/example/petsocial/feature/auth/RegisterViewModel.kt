package com.example.petsocial.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.common.result.safeApiCall

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            errorMessage = null
        )
    }

    fun onPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            errorMessage = null
        )
    }

    fun onRepeatedPasswordChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            repeatedPassword = value,
            errorMessage = null
        )
    }

    fun onHandlerChanged(value: Boolean) {
        _uiState.value = _uiState.value.copy(
            isHandler = value,
            errorMessage = null
        )
    }

    fun onRegisterEventConsumed() {
        _uiState.value = _uiState.value.copy(
            isRegistered = false
        )
    }

    fun register() {
        val state = _uiState.value

        if (state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите email")
            return
        }

        if (state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите пароль")
            return
        }

        if (state.password.length < 6) {
            _uiState.value = state.copy(errorMessage = "Пароль должен быть не короче 6 символов")
            return
        }

        if (state.password != state.repeatedPassword) {
            _uiState.value = state.copy(errorMessage = "Пароли не совпадают")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (
                val result = safeApiCall {
                    authRepository.register(
                        email = state.email.trim(),
                        password = state.password,
                        isHandler = state.isHandler
                    )
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistered = true
                    )
                }

                is AppResult.Error -> {
                    val message = when (result.error) {
                        is AppError.Conflict -> "Пользователь с таким email уже существует"
                        is AppError.BadRequest -> "Некорректные данные"
                        else -> result.error.message
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = message
                    )
                }
            }
        }
    }
}
