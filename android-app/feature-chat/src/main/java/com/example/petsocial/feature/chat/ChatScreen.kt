package com.example.petsocial.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.network.model.chat.ChatResponse
import com.example.petsocial.core.network.model.chat.MessageResponse
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.ScreenTitle

@Composable
fun ChatsRoute(
    chatsViewModel: ChatsViewModel = hiltViewModel(),
    messagesViewModel: MessagesViewModel = hiltViewModel()
) {
    val selectedChatId = remember {
        mutableStateOf<String?>(null)
    }

    if (selectedChatId.value == null) {
        val chatsUiState by chatsViewModel.uiState.collectAsState()

        LaunchedEffect(Unit) {
            chatsViewModel.loadChats()
        }

        ChatsListScreen(
            uiState = chatsUiState,
            onChatClick = { chat ->
                selectedChatId.value = chat.id
            },
            onRetryClick = chatsViewModel::loadChats
        )
    } else {
        val messagesUiState by messagesViewModel.uiState.collectAsState()
        val chatId = selectedChatId.value.orEmpty()

        LaunchedEffect(chatId) {
            messagesViewModel.loadMessages(chatId)
        }

        MessagesScreen(
            chatId = chatId,
            uiState = messagesUiState,
            onMessageTextChanged = messagesViewModel::onMessageTextChanged,
            onSendClick = messagesViewModel::sendMessage,
            onBackClick = {
                selectedChatId.value = null
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (uiState.isLoading) {
            item {
                FullScreenLoading()
            }
            return@LazyColumn
        }

        if (uiState.errorMessage != null) {
            item {
                ErrorMessage(message = uiState.errorMessage)

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = onRetryClick) {
                    Text("Повторить")
                }
            }
        }

        if (uiState.chats.isEmpty()) {
            item {
                Text("Пока нет чатов. Чат появится после match.")
            }
        } else {
            items(uiState.chats) { chat ->
                ChatCard(
                    chat = chat,
                    onClick = {
                        onChatClick(chat)
                    }
                )
            }
        }
    }
}

@Composable
private fun ChatCard(
    chat: ChatResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ScreenTitle("Чат")

            Text("ID: ${chat.id}")
            Text("Match: ${chat.match_id}")
            Text("Pet 1: ${chat.pet1_id}")
            Text("Pet 2: ${chat.pet2_id}")
            Text("Создан: ${chat.created_at}")
        }
    }
}

@Composable
private fun MessagesScreen(
    chatId: String,
    uiState: MessagesUiState,
    onMessageTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        ScreenTitle("Чат")

        Text(
            text = chatId,
            style = MaterialTheme.typography.bodySmall
        )

        TextButton(onClick = onBackClick) {
            Text("Назад к чатам")
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.errorMessage != null) {
                    item {
                        ErrorMessage(message = uiState.errorMessage)
                    }
                }

                if (uiState.messages.isEmpty()) {
                    item {
                        Text("Сообщений пока нет")
                    }
                } else {
                    items(uiState.messages) { message ->
                        MessageCard(message)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.messageText,
                onValueChange = onMessageTextChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Сообщение") },
                enabled = !uiState.isSending
            )

            Button(
                onClick = onSendClick,
                enabled = !uiState.isSending
            ) {
                Text("Отпр.")
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: MessageResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "От: ${message.sender_user_id}",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = message.created_at,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}