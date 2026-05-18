package com.example.petsocial.feature.notifications

import com.example.petsocial.core.network.api.NotificationsApi
import com.example.petsocial.core.network.model.notifications.NotificationResponse
import javax.inject.Inject

class DefaultNotificationsRepository @Inject constructor(
    private val api: NotificationsApi
) : NotificationsRepository {

    override suspend fun getNotifications(unreadOnly: Boolean): List<NotificationResponse> {
        return api.getNotifications(unreadOnly = unreadOnly)
    }

    override suspend fun getUnreadCount(): Int {
        return api.getUnreadCount().count
    }

    override suspend fun markRead(id: String): NotificationResponse {
        return api.markRead(id)
    }

    override suspend fun markAllRead() {
        api.markAllRead()
    }
}