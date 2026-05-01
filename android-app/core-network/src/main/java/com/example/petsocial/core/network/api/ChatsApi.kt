package com.example.petsocial.core.network.api

import com.example.petsocial.core.network.model.chat.ChatResponse
import com.example.petsocial.core.network.model.chat.MessageResponse
import com.example.petsocial.core.network.model.chat.SendMessageRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatsApi {

    @GET("v1/chats/")
    suspend fun getChats(): List<ChatResponse>

    @GET("v1/chats/{id}/messages")
    suspend fun getMessages(
        @Path("id") chatId: String
    ): List<MessageResponse>

    @POST("v1/chats/{id}/messages")
    suspend fun sendMessage(
        @Path("id") chatId: String,
        @Body request: SendMessageRequest
    ): MessageResponse
}