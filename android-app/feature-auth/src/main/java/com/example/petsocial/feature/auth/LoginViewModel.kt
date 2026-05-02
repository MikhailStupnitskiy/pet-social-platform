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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

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

    fun onLoginEventConsumed() {
        _uiState.value = _uiState.value.copy(
            isLoggedIn = false
        )
    }

    fun login() {
        val state = _uiState.value

        if (state.email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите email")
            return
        }

        if (state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Введите пароль")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (
                val result = safeApiCall {
                    authRepository.login(
                        email = state.email.trim(),
                        password = state.password
                    )
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                }

                is AppResult.Error -> {
                    val message = when (result.error) {
                        is AppError.Unauthorized -> "Неверный email или пароль"
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