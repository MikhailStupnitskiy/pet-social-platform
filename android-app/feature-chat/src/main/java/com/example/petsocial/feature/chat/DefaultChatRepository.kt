package com.example.petsocial.feature.chat

import com.example.petsocial.core.network.api.ChatsApi
import com.example.petsocial.core.network.model.chat.ChatResponse
import com.example.petsocial.core.network.model.chat.MessageResponse
import com.example.petsocial.core.network.model.chat.SendMessageRequest
import javax.inject.Inject

class DefaultChatRepository @Inject constructor(
    private val chatsApi: ChatsApi
) : ChatRepository {

    override suspend fun getChats(): List<ChatResponse> {
        return chatsApi.getChats()
    }

    override suspend fun getMessages(chatId: String): List<MessageResponse> {
        return chatsApi.getMessages(chatId)
    }

    override suspend fun sendMessage(chatId: String, body: String): MessageResponse {
        return chatsApi.sendMessage(
            chatId = chatId,
            request = SendMessageRequest(body = body)
        )
    }
}