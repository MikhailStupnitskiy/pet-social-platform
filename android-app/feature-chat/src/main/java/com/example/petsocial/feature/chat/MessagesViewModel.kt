package com.example.petsocial.feature.chat

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
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState(isLoading = true))
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    private var currentChatId: String? = null

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
                errorMessage = "Чат не выбран"
            )
            return
        }

        if (text.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Введите сообщение"
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