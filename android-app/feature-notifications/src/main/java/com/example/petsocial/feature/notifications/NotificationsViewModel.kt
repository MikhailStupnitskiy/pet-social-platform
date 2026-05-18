package com.example.petsocial.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.session.SessionEventBus
import com.example.petsocial.core.network.model.notifications.NotificationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = safeApiCall { repository.getNotifications() }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, notifications = result.data)
                }
                is AppResult.Error -> handleError(result.error, loading = true)
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            when (val result = safeApiCall { repository.markAllRead() }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        notifications = _uiState.value.notifications.map { it.copy(read_at = it.read_at ?: "read") }
                    )
                    load()
                }
                is AppResult.Error -> handleError(result.error, saving = true)
            }
        }
    }

    fun openNotification(notification: NotificationResponse, navigate: (NotificationDestination) -> Unit) {
        viewModelScope.launch {
            if (notification.read_at == null) {
                when (val result = safeApiCall { repository.markRead(notification.id) }) {
                    is AppResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            notifications = _uiState.value.notifications.map { item ->
                                if (item.id == notification.id) result.data else item
                            }
                        )
                    }
                    is AppResult.Error -> {
                        if (result.error is AppError.Unauthorized) {
                            sessionEventBus.notifyUnauthorized()
                            return@launch
                        }
                    }
                }
            }
            navigate(notification.destination())
        }
    }

    private fun handleError(error: AppError, loading: Boolean = false, saving: Boolean = false) {
        if (error is AppError.Unauthorized) {
            sessionEventBus.notifyUnauthorized()
            return
        }
        _uiState.value = _uiState.value.copy(
            isLoading = if (loading) false else _uiState.value.isLoading,
            isSaving = if (saving) false else _uiState.value.isSaving,
            errorMessage = error.message
        )
    }
}

sealed interface NotificationDestination {
    data object Matching : NotificationDestination
    data object Chats : NotificationDestination
    data object Care : NotificationDestination
    data object None : NotificationDestination
}

private fun NotificationResponse.destination(): NotificationDestination {
    return when (entity_type) {
        "chat" -> NotificationDestination.Chats
        "match" -> NotificationDestination.Matching
        "service_request" -> NotificationDestination.Care
        else -> when (type) {
            "message_received" -> NotificationDestination.Chats
            "match_created" -> NotificationDestination.Matching
            "service_request_created", "service_request_status_changed" -> NotificationDestination.Care
            else -> NotificationDestination.None
        }
    }
}