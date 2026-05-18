package com.example.petsocial.feature.notifications

import com.example.petsocial.core.network.model.notifications.NotificationResponse

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val notifications: List<NotificationResponse> = emptyList(),
    val errorMessage: String? = null
)