package com.example.petsocial.feature.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetOnSurface
import com.example.petsocial.core.designsystem.theme.PetOutline
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetPrimaryLight
import com.example.petsocial.core.designsystem.theme.PetSurface
import com.example.petsocial.core.designsystem.theme.PetSurfaceMuted
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.notifications.NotificationResponse

@Composable
fun NotificationsRoute(
    onDestination: (NotificationDestination) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    NotificationsScreen(
        uiState = uiState,
        onRetryClick = viewModel::load,
        onMarkAllReadClick = viewModel::markAllRead,
        onNotificationClick = { notification ->
            viewModel.openNotification(notification, onDestination)
        }
    )
}

@Composable
private fun NotificationsScreen(
    uiState: NotificationsUiState,
    onRetryClick: () -> Unit,
    onMarkAllReadClick: () -> Unit,
    onNotificationClick: (NotificationResponse) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground)
    ) {
        when {
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Уведомления", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("События мэтчинга, чатов и услуг", color = PetTextSecondary)
                        }
                        TextButton(
                            onClick = onMarkAllReadClick,
                            enabled = !uiState.isSaving && uiState.notifications.any { it.read_at == null }
                        ) {
                            Text("Прочитать все")
                        }
                    }
                }

                uiState.errorMessage?.let { message ->
                    item {
                        ProductCard(modifier = Modifier.fillMaxWidth()) {
                            Text(message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = onRetryClick) {
                                Text("Повторить")
                            }
                        }
                    }
                }

                if (uiState.notifications.isEmpty()) {
                    item { EmptyNotificationsCard() }
                } else {
                    items(uiState.notifications, key = { it.id }) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = { onNotificationClick(notification) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationResponse,
    onClick: () -> Unit
) {
    val unread = notification.read_at == null
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (unread) PetSurface else PetSurfaceMuted,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (unread) PetPrimaryLight else PetOutline),
        tonalElevation = if (unread) 2.dp else 0.dp,
        shadowElevation = if (unread) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (unread) PetPrimaryLight else PetBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(notificationIcon(notification.type), contentDescription = null, tint = PetPrimary)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        notification.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (unread) FontWeight.Bold else FontWeight.SemiBold,
                        color = PetOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (unread) {
                        StatusChip(label = "Новое", tone = ChipTone.Info)
                    }
                }
                Text(
                    notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PetTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = PetTextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(notification.created_at.toDisplayDate(), style = MaterialTheme.typography.bodySmall, color = PetTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationsCard() {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Rounded.Notifications, contentDescription = null, tint = PetPrimary, modifier = Modifier.size(34.dp))
        Text("Уведомлений пока нет", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("Новые мэтчи, сообщения и статусы заявок появятся здесь.", color = PetTextSecondary)
    }
}

private fun notificationIcon(type: String): ImageVector {
    return when (type) {
        "match_created" -> Icons.Rounded.Favorite
        "message_received" -> Icons.Rounded.Mail
        "service_request_created", "service_request_status_changed" -> Icons.Rounded.WorkOutline
        else -> Icons.Rounded.Notifications
    }
}

private fun String.toDisplayDate(): String {
    val date = take(10)
    val time = substringAfter('T', "").take(5)
    if (date.length == 10 && time.length == 5) {
        return "${date.substring(8, 10)}.${date.substring(5, 7)} $time"
    }
    return take(16).replace('T', ' ')
}