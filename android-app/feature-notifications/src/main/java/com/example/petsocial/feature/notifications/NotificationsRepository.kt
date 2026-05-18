package com.example.petsocial.feature.notifications

import com.example.petsocial.core.network.model.notifications.NotificationResponse

interface NotificationsRepository {
    suspend fun getNotifications(unreadOnly: Boolean = false): List<NotificationResponse>
    suspend fun getUnreadCount(): Int
    suspend fun markRead(id: String): NotificationResponse
    suspend fun markAllRead()
}