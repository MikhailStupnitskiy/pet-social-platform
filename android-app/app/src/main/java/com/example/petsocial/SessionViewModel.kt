package com.example.petsocial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.auth.data.AuthRepository
import com.example.petsocial.core.datastore.auth.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall

sealed interface SessionUiState {
    data object Loading : SessionUiState
    data object AuthRequired : SessionUiState
    data object Authorized : SessionUiState
    data object LoggingOut : SessionUiState

    data class Error(
        val message: String
    ) : SessionUiState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SessionUiState>(SessionUiState.Loading)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _uiState.value = SessionUiState.Loading

            val token = tokenStorage.token.firstOrNull()

            if (token.isNullOrBlank()) {
                _uiState.value = SessionUiState.AuthRequired
                return@launch
            }

            when (val result = safeApiCall { authRepository.getMe() }) {
                is AppResult.Success -> {
                    _uiState.value = SessionUiState.Authorized
                }

                is AppResult.Error -> {
                    when (result.error) {
                        is AppError.Unauthorized -> {
                            authRepository.logout()
                            _uiState.value = SessionUiState.AuthRequired
                        }

                        is AppError.Network -> {
                            _uiState.value = SessionUiState.Error(
                                message = "Не удалось проверить сессию. Проверьте подключение к сети."
                            )
                        }

                        else -> {
                            authRepository.logout()
                            _uiState.value = SessionUiState.AuthRequired
                        }
                    }
                }
            }
        }
    }

    fun onAuthSuccess() {
        checkSession()
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = SessionUiState.LoggingOut
            authRepository.logout()
            _uiState.value = SessionUiState.AuthRequired
        }
    }
}