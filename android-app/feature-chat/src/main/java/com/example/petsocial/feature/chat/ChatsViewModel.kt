package com.example.petsocial.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.common.result.AppError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.session.SessionEventBus
import com.example.petsocial.core.datastore.auth.TokenStorage

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val tokenStorage: TokenStorage,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState(isLoading = true))
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tokenStorage.token.collect { token ->
                _uiState.value = _uiState.value.copy(authToken = token)
            }
        }
    }

    private fun handleUnauthorized(error: AppError): Boolean {
        if (error is AppError.Unauthorized) {
            sessionEventBus.notifyUnauthorized()
            return true
        }

        return false
    }

    fun loadChats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = safeApiCall { repository.getChats() }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        chats = result.data
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }
}
