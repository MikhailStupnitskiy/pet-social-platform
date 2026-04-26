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

sealed interface SessionUiState {
    data object Loading : SessionUiState
    data object AuthRequired : SessionUiState
    data object Authorized : SessionUiState

    data object LoggingOut : SessionUiState
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
            val token = tokenStorage.token.firstOrNull()

            _uiState.value = if (token.isNullOrBlank()) {
                SessionUiState.AuthRequired
            } else {
                SessionUiState.Authorized
            }
        }
    }

    fun onAuthSuccess() {
        _uiState.value = SessionUiState.Authorized
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = SessionUiState.LoggingOut
            authRepository.logout()
            _uiState.value = SessionUiState.AuthRequired
        }
    }
}