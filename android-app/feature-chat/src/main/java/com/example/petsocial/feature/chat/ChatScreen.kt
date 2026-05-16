package com.example.petsocial.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.component.MessageBubble
import com.example.petsocial.core.designsystem.component.PetAvatar
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.SearchField
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.chat.ChatResponse
import com.example.petsocial.core.network.model.chat.MessageResponse
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.FullScreenLoading

@Composable
fun ChatsRoute(
    chatsViewModel: ChatsViewModel = hiltViewModel(),
    messagesViewModel: MessagesViewModel = hiltViewModel()
) {
    val selectedChat = remember { mutableStateOf<ChatResponse?>(null) }

    if (selectedChat.value == null) {
        val chatsUiState by chatsViewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            chatsViewModel.loadChats()
        }

        ChatsListScreen(
            uiState = chatsUiState,
            onChatClick = { chat -> selectedChat.value = chat },
            onRetryClick = chatsViewModel::loadChats
        )
    } else {
        val messagesUiState by messagesViewModel.uiState.collectAsState()
        val chat = selectedChat.value!!

        LaunchedEffect(chat.id) {
            messagesViewModel.loadMessages(chat.id)
        }

        MessagesScreen(
            chat = chat,
            uiState = messagesUiState,
            onMessageTextChanged = messagesViewModel::onMessageTextChanged,
            onSendClick = messagesViewModel::sendMessage,
            onBackClick = {
                selectedChat.value = null
                chatsViewModel.loadChats()
            }
        )
    }
}

@Composable
private fun ChatsListScreen(
    uiState: ChatsUiState,
    onChatClick: (ChatResponse) -> Unit,
    onRetryClick: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filteredChats = uiState.chats.filter { chat ->
        query.isBlank() ||
            chat.title.contains(query, ignoreCase = true) ||
            chat.subtitle.contains(query, ignoreCase = true) ||
            chat.last_message.orEmpty().contains(query, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Сообщения",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Поиск сообщений..."
            )
        }

        if (uiState.isLoading) {
            item { FullScreenLoading() }
            return@LazyColumn
        }

        uiState.errorMessage?.let { message ->
            item {
                ProductCard(modifier = Modifier.fillMaxWidth()) {
                    ErrorMessage(message)
                    Button(onClick = onRetryClick) { Text("Повторить") }
                }
            }
        }

        val newMatches = filteredChats.filter { it.is_new_match }
        if (newMatches.isNotEmpty()) {
            item {
                Text("Новые мэтчи", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    newMatches.take(4).forEach { chat ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PetAvatar(
                                imageUrl = chat.avatar_url,
                                contentDescription = chat.title,
                                size = 58.dp
                            )
                            Text(
                                text = chat.title,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (filteredChats.isEmpty()) {
            item {
                ProductCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Пока нет сообщений", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Когда у вас появятся мэтчи или заявки на услуги, чаты будут здесь.", color = PetTextSecondary)
                }
            }
        } else {
            items(filteredChats) { chat ->
                ChatRow(chat = chat, onClick = { onChatClick(chat) })
            }
        }
    }
}

@Composable
private fun ChatRow(
    chat: ChatResponse,
    onClick: () -> Unit
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PetAvatar(imageUrl = chat.avatar_url, contentDescription = chat.title, size = 52.dp)
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chat.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (chat.unread_count > 0) {
                        StatusChip(label = chat.unread_count.toString(), tone = ChipTone.Primary)
                    }
                }
                Text(text = chat.subtitle, color = PetTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = chat.last_message ?: if (chat.is_new_match) "Это мэтч! Напишите первым." else "Сообщений пока нет",
                    color = PetTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(onClick = onClick) {
                Text("Открыть")
            }
        }
    }
}

@Composable
private fun MessagesScreen(
    chat: ChatResponse,
    uiState: MessagesUiState,
    onMessageTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
            }
            PetAvatar(imageUrl = chat.avatar_url, contentDescription = chat.title, size = 44.dp)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(chat.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(chat.subtitle, style = MaterialTheme.typography.bodySmall, color = PetTextSecondary)
            }
        }

        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                uiState.errorMessage?.let { message ->
                    item { ErrorMessage(message = message) }
                }

                if (uiState.messages.isEmpty()) {
                    item {
                        ProductCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Начните диалог", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Предложите прогулку или обсудите детали заявки.", color = PetTextSecondary)
                        }
                    }
                } else {
                    items(uiState.messages) { message ->
                        MessageCard(message = message)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = uiState.messageText,
                onValueChange = onMessageTextChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Сообщение") },
                enabled = !uiState.isSending,
                singleLine = true
            )

            Button(
                onClick = onSendClick,
                enabled = !uiState.isSending,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Icon(Icons.Rounded.Send, contentDescription = "Отправить", tint = PetPrimary)
            }
        }
    }
}

@Composable
private fun MessageCard(message: MessageResponse) {
    MessageBubble(
        text = message.body,
        isMine = false,
        timestamp = message.created_at.take(16).replace("T", " ")
    )
}
