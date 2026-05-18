package com.example.petsocial.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.session.SessionEventBus
import com.example.petsocial.core.datastore.auth.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val tokenStorage: TokenStorage,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState(isLoading = true))
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private var currentChatId: String? = null

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

    fun loadMessages(chatId: String) {
        currentChatId = chatId

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            when (val result = safeApiCall { repository.getMessages(chatId) }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        messages = result.data
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

    fun onMessageTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            messageText = value,
            errorMessage = null
        )
    }

    fun sendMessage() {
        val chatId = currentChatId
        val text = _uiState.value.messageText.trim()

        if (chatId.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "\u0427\u0430\u0442 \u043d\u0435 \u0432\u044b\u0431\u0440\u0430\u043d"
            )
            return
        }

        if (text.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSending = true,
                errorMessage = null
            )

            when (
                val result = safeApiCall {
                    repository.sendMessage(
                        chatId = chatId,
                        body = text
                    )
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        messageText = "",
                        messages = _uiState.value.messages + result.data
                    )
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }
}
