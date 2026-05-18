package com.example.petsocial.core.network.api

import com.example.petsocial.core.network.model.notifications.NotificationResponse
import com.example.petsocial.core.network.model.notifications.UnreadCountResponse
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsApi {

    @GET("v1/notifications/")
    suspend fun getNotifications(
        @Query("limit") limit: Int = 50,
        @Query("unread_only") unreadOnly: Boolean = false
    ): List<NotificationResponse>

    @GET("v1/notifications/unread-count")
    suspend fun getUnreadCount(): UnreadCountResponse

    @PATCH("v1/notifications/{id}/read")
    suspend fun markRead(
        @Path("id") id: String
    ): NotificationResponse

    @POST("v1/notifications/read-all")
    suspend fun markAllRead()
}