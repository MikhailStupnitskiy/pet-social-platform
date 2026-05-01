package com.example.petsocial.feature.chat

import com.example.petsocial.core.network.model.chat.ChatResponse

data class ChatsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val chats: List<ChatResponse> = emptyList()
)