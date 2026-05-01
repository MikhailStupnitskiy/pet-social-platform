package com.example.petsocial.feature.chat

import com.example.petsocial.core.network.model.chat.ChatResponse
import com.example.petsocial.core.network.model.chat.MessageResponse

interface ChatRepository {

    suspend fun getChats(): List<ChatResponse>

    suspend fun getMessages(chatId: String): List<MessageResponse>

    suspend fun sendMessage(chatId: String, body: String): MessageResponse
}