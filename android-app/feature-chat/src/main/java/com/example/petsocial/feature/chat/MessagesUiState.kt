package com.example.petsocial.feature.chat

import com.example.petsocial.core.network.model.chat.MessageResponse

data class MessagesUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val authToken: String? = null,
    val messages: List<MessageResponse> = emptyList(),
    val messageText: String = ""
)
