package com.example.petsocial.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.component.MessageBubble
import com.example.petsocial.core.designsystem.component.PetChip
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.SearchField
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetOnPrimary
import com.example.petsocial.core.designsystem.theme.PetOnSurface
import com.example.petsocial.core.designsystem.theme.PetOutline
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetPrimaryLight
import com.example.petsocial.core.designsystem.theme.PetSecondary
import com.example.petsocial.core.designsystem.theme.PetSecondaryLight
import com.example.petsocial.core.designsystem.theme.PetSurface
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.chat.ChatResponse
import com.example.petsocial.core.network.model.chat.MessageResponse
import okhttp3.Headers

private const val CHAT_API_BASE_URL = "http://192.168.1.68:8080/"

private enum class ChatFilter(val label: String) {
    All("\u0412\u0441\u0435"),
    Pets("\u041f\u0438\u0442\u043e\u043c\u0446\u044b"),
    Services("\u0423\u0441\u043b\u0443\u0433\u0438")
}

@Composable
fun ChatsRoute(
    onUserProfileClick: (String) -> Unit = {},
    onPetProfileClick: (String) -> Unit = {},
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
            onUserProfileClick = onUserProfileClick,
            onPetProfileClick = onPetProfileClick,
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
            onUserProfileClick = onUserProfileClick,
            onPetProfileClick = onPetProfileClick,
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
    onUserProfileClick: (String) -> Unit,
    onPetProfileClick: (String) -> Unit,
    onRetryClick: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ChatFilter.All) }
    val filteredChats = uiState.chats
        .filter { chat ->
            when (filter) {
                ChatFilter.All -> true
                ChatFilter.Pets -> chat.isMatchChat
                ChatFilter.Services -> chat.isServiceChat
            }
        }
        .filter { chat ->
            query.isBlank() || listOf(
                chat.displayTitle,
                chat.displaySubtitle,
                chat.last_message.orEmpty(),
                chat.peer_name.orEmpty(),
                chat.owner_name.orEmpty(),
                chat.service_title.orEmpty()
            ).any { it.contains(query, ignoreCase = true) }
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
                text = "\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PetOnSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "\u041f\u043e\u0438\u0441\u043a \u043f\u043e \u0447\u0430\u0442\u0430\u043c"
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChatFilter.values().forEach { item ->
                    PetChip(
                        label = item.label,
                        selected = filter == item,
                        leading = item.leadingIcon(),
                        modifier = Modifier.clickable { filter = item }
                    )
                }
            }
        }

        if (uiState.isLoading) {
            item { ChatLoading() }
            return@LazyColumn
        }

        uiState.errorMessage?.let { message ->
            item {
                ProductCard(modifier = Modifier.fillMaxWidth()) {
                    ChatError(message)
                    Button(onClick = onRetryClick) { Text("\u041f\u043e\u0432\u0442\u043e\u0440\u0438\u0442\u044c") }
                }
            }
        }

        val newMatches = filteredChats.filter { it.is_new_match && it.isMatchChat }
        if (newMatches.isNotEmpty()) {
            item {
                Text(
                    text = "\u041d\u043e\u0432\u044b\u0435 \u043c\u044d\u0442\u0447\u0438",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PetOnSurface
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    newMatches.take(4).forEach { chat ->
                        Column(
                            modifier = Modifier
                                .width(72.dp)
                                .clickable { chat.openPublicProfile(onUserProfileClick, onPetProfileClick) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ChatAvatar(
                                imageUrl = chat.avatar_url,
                                authToken = uiState.authToken,
                                contentDescription = chat.displayTitle,
                                isService = chat.isServiceChat,
                                size = 58.dp
                            )
                            Text(
                                text = chat.displayTitle,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = PetOnSurface
                            )
                        }
                    }
                }
            }
        }

        if (filteredChats.isEmpty()) {
            item {
                ProductCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = emptyTitle(filter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PetOnSurface
                    )
                    Text(
                        text = "\u041a\u043e\u0433\u0434\u0430 \u043f\u043e\u044f\u0432\u044f\u0442\u0441\u044f \u043c\u044d\u0442\u0447\u0438 \u0438\u043b\u0438 \u0437\u0430\u044f\u0432\u043a\u0438 \u043d\u0430 \u0443\u0441\u043b\u0443\u0433\u0438, \u0447\u0430\u0442\u044b \u0431\u0443\u0434\u0443\u0442 \u0437\u0434\u0435\u0441\u044c.",
                        color = PetTextSecondary
                    )
                }
            }
        } else {
            items(filteredChats, key = { it.id }) { chat ->
                ChatRow(
                    chat = chat,
                    authToken = uiState.authToken,
                    onClick = { onChatClick(chat) },
                    onProfileClick = { chat.openPublicProfile(onUserProfileClick, onPetProfileClick) }
                )
            }
        }
    }
}

@Composable
private fun ChatRow(
    chat: ChatResponse,
    authToken: String?,
    onClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChatAvatar(
                imageUrl = chat.avatar_url,
                authToken = authToken,
                contentDescription = chat.displayTitle,
                modifier = Modifier.clickable(onClick = onProfileClick),
                isService = chat.isServiceChat,
                size = 54.dp
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = chat.displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PetOnSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (chat.unread_count > 0) {
                        StatusChip(label = chat.unread_count.toString(), tone = ChipTone.Primary)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChatTypeBadge(chat = chat)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = chat.displaySubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = PetTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = chat.last_message ?: if (chat.is_new_match) "\u042d\u0442\u043e \u043c\u044d\u0442\u0447. \u041d\u0430\u043f\u0438\u0448\u0438\u0442\u0435 \u043f\u0435\u0440\u0432\u044b\u043c." else "\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0439 \u043f\u043e\u043a\u0430 \u043d\u0435\u0442",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PetTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u0447\u0430\u0442",
                    tint = PetTextSecondary
                )
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
    onUserProfileClick: (String) -> Unit,
    onPetProfileClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground)
            .navigationBarsPadding()
    ) {
        ChatHeader(
            chat = chat,
            authToken = uiState.authToken,
            onBackClick = onBackClick,
            onProfileClick = { chat.openPublicProfile(onUserProfileClick, onPetProfileClick) }
        )

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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                uiState.errorMessage?.let { message ->
                    item { ChatError(message = message) }
                }

                if (uiState.messages.isEmpty()) {
                    item {
                        ProductCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "\u041d\u0430\u0447\u043d\u0438\u0442\u0435 \u0434\u0438\u0430\u043b\u043e\u0433",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PetOnSurface
                            )
                            Text(
                                text = if (chat.isServiceChat) {
                                    "\u041e\u0431\u0441\u0443\u0434\u0438\u0442\u0435 \u0434\u0435\u0442\u0430\u043b\u0438 \u0443\u0441\u043b\u0443\u0433\u0438, \u0432\u0440\u0435\u043c\u044f \u0438 \u043f\u0438\u0442\u043e\u043c\u0446\u0430."
                                } else {
                                    "\u041f\u0440\u0435\u0434\u043b\u043e\u0436\u0438\u0442\u0435 \u043f\u0440\u043e\u0433\u0443\u043b\u043a\u0443 \u0438\u043b\u0438 \u043f\u0440\u043e\u0441\u0442\u043e \u043f\u043e\u0437\u043d\u0430\u043a\u043e\u043c\u044c\u0442\u0435\u0441\u044c."
                                },
                                color = PetTextSecondary
                            )
                        }
                    }
                } else {
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageCard(message = message, chat = chat)
                    }
                }
            }
        }

        MessageInput(
            value = uiState.messageText,
            isSending = uiState.isSending,
            onValueChange = onMessageTextChanged,
            onSendClick = onSendClick
        )
    }
}

@Composable
private fun ChatHeader(
    chat: ChatResponse,
    authToken: String?,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PetSurface)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "\u041d\u0430\u0437\u0430\u0434", tint = PetOnSurface)
        }
        ChatAvatar(
            imageUrl = chat.avatar_url,
            authToken = authToken,
            contentDescription = chat.displayTitle,
            modifier = Modifier.clickable(onClick = onProfileClick),
            isService = chat.isServiceChat,
            size = 46.dp
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
                .clickable(onClick = onProfileClick)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.headerTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PetOnSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                ChatTypeBadge(chat = chat)
            }
            Text(
                text = chat.headerSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PetTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MessageInput(
    value: String,
    isSending: Boolean,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PetSurface)
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0435") },
            enabled = !isSending,
            singleLine = true,
            shape = RoundedCornerShape(22.dp)
        )

        FilledIconButton(
            onClick = onSendClick,
            enabled = !isSending && value.isNotBlank(),
            modifier = Modifier.size(48.dp)
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = PetOnPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "\u041e\u0442\u043f\u0440\u0430\u0432\u0438\u0442\u044c",
                    tint = PetOnPrimary
                )
            }
        }
    }
}

@Composable
private fun MessageCard(message: MessageResponse, chat: ChatResponse) {
    MessageBubble(
        text = message.body,
        isMine = message.isMineIn(chat),
        timestamp = message.created_at.take(16).replace("T", " ")
    )
}

@Composable
private fun ChatAvatar(
    imageUrl: String?,
    authToken: String?,
    contentDescription: String?,
    isService: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val background = if (isService) PetSecondaryLight else PetPrimaryLight
    val tint = if (isService) PetSecondary else PetPrimary
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, PetOutline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            ChatAuthenticatedImage(
                imageUrl = imageUrl,
                authToken = authToken,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = if (isService) Icons.Rounded.WorkOutline else Icons.Rounded.Pets,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size * 0.45f)
            )
        }
    }
}

@Composable
private fun ChatLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ChatError(message: String) {
    Text(text = message, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun ChatAuthenticatedImage(
    imageUrl: String?,
    authToken: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUrl = remember(imageUrl) {
        imageUrl?.takeIf { it.isNotBlank() }?.let { url ->
            if (url.startsWith("http://") || url.startsWith("https://")) {
                url
            } else {
                CHAT_API_BASE_URL.trimEnd('/') + "/" + url.trimStart('/')
            }
        }
    }
    val model = remember(context, resolvedUrl, authToken) {
        resolvedUrl?.let { url ->
            val builder = ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
            if (!authToken.isNullOrBlank()) {
                builder.headers(
                    Headers.Builder()
                        .add("Authorization", "Bearer $authToken")
                        .build()
                )
            }
            builder.build()
        }
    }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )
}

@Composable
private fun ChatTypeBadge(chat: ChatResponse) {
    val background = if (chat.isServiceChat) PetSecondaryLight else PetPrimaryLight
    val foreground = if (chat.isServiceChat) PetSecondary else PetPrimary
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (chat.isServiceChat) Icons.Rounded.WorkOutline else Icons.Rounded.Pets,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (chat.isServiceChat) "\u0423\u0441\u043b\u0443\u0433\u0430" else "\u041f\u0438\u0442\u043e\u043c\u0435\u0446",
            style = MaterialTheme.typography.labelSmall,
            color = foreground,
            maxLines = 1
        )
    }
}

@Composable
private fun ChatFilter.leadingIcon(): (@Composable () -> Unit)? {
    return when (this) {
        ChatFilter.All -> null
        ChatFilter.Pets -> ({ Icon(Icons.Rounded.Pets, contentDescription = null, modifier = Modifier.size(16.dp)) })
        ChatFilter.Services -> ({ Icon(Icons.Rounded.WorkOutline, contentDescription = null, modifier = Modifier.size(16.dp)) })
    }
}

private val ChatResponse.isServiceChat: Boolean
    get() = source == "service_request" || service_request_id != null

private val ChatResponse.isMatchChat: Boolean
    get() = !isServiceChat

private val ChatResponse.displayTitle: String
    get() = when {
        isServiceChat -> peer_name?.takeIf { it.isNotBlank() } ?: title
        else -> pet_name?.takeIf { it.isNotBlank() } ?: title
    }

private val ChatResponse.headerTitle: String
    get() {
        val petName = pet_name?.takeIf { it.isNotBlank() }
        val ownerName = owner_name?.takeIf { it.isNotBlank() }
        val peerName = peer_name?.takeIf { it.isNotBlank() }

        return listOfNotNull(petName, ownerName ?: peerName)
            .distinct()
            .joinToString(" \u0438 ")
            .ifBlank { displayTitle }
    }

private val ChatResponse.displaySubtitle: String
    get() = when {
        isServiceChat -> listOfNotNull(
            service_title?.takeIf { it.isNotBlank() },
            pet_name?.takeIf { it.isNotBlank() }?.let { "\u043f\u0438\u0442\u043e\u043c\u0435\u0446: $it" }
        ).joinToString(" \u00b7 ").ifBlank { subtitle }
        else -> listOfNotNull(
            owner_name?.takeIf { it.isNotBlank() }?.let { "\u0432\u043b\u0430\u0434\u0435\u043b\u0435\u0446: $it" },
            subtitle.takeIf { it.isNotBlank() }
        ).joinToString(" \u00b7 ").ifBlank { subtitle }
    }

private val ChatResponse.headerSubtitle: String
    get() = when {
        isServiceChat -> listOfNotNull(
            service_title?.takeIf { it.isNotBlank() },
            owner_name?.takeIf { it.isNotBlank() }?.let { "\u043a\u043b\u0438\u0435\u043d\u0442: $it" }
        ).joinToString(" \u00b7 ").ifBlank { subtitle }
        else -> listOfNotNull(
            owner_name?.takeIf { it.isNotBlank() }?.let { "\u0432\u043b\u0430\u0434\u0435\u043b\u0435\u0446: $it" },
            subtitle.takeIf { it.isNotBlank() }
        ).joinToString(" \u00b7 ").ifBlank { subtitle }
    }

private fun MessageResponse.isMineIn(chat: ChatResponse): Boolean {
    val peerUserId = chat.peer_user_id
    if (!peerUserId.isNullOrBlank()) {
        return sender_user_id != peerUserId
    }

    return is_mine
}

private fun emptyTitle(filter: ChatFilter): String {
    return when (filter) {
        ChatFilter.All -> "\u041f\u043e\u043a\u0430 \u043d\u0435\u0442 \u0441\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u0439"
        ChatFilter.Pets -> "\u041f\u043e\u043a\u0430 \u043d\u0435\u0442 \u0447\u0430\u0442\u043e\u0432 \u0441 \u043f\u0438\u0442\u043e\u043c\u0446\u0430\u043c\u0438"
        ChatFilter.Services -> "\u041f\u043e\u043a\u0430 \u043d\u0435\u0442 \u0447\u0430\u0442\u043e\u0432 \u043f\u043e \u0443\u0441\u043b\u0443\u0433\u0430\u043c"
    }
}

private fun ChatResponse.openPublicProfile(
    onUserProfileClick: (String) -> Unit,
    onPetProfileClick: (String) -> Unit
) {
    val peerPetId = peer_pet_id
    val peerUserId = peer_user_id
    val fallbackPetId = pet2_id

    when {
        !peerPetId.isNullOrBlank() -> onPetProfileClick(peerPetId)
        !peerUserId.isNullOrBlank() -> onUserProfileClick(peerUserId)
        !fallbackPetId.isNullOrBlank() -> onPetProfileClick(fallbackPetId)
    }
}
