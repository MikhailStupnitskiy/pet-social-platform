package com.example.petsocial.feature.feed

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.SectionHeader
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetInfo
import com.example.petsocial.core.designsystem.theme.PetOnSurface
import com.example.petsocial.core.designsystem.theme.PetOutline
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetPrimaryLight
import com.example.petsocial.core.designsystem.theme.PetSecondary
import com.example.petsocial.core.designsystem.theme.PetSurface
import com.example.petsocial.core.designsystem.theme.PetSurfaceMuted
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.feed.CommentResponse
import com.example.petsocial.core.network.model.feed.PostResponse
import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.ui.AuthenticatedImage
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.SuccessMessage

@Composable
fun FeedRoute(
    onProfileClick: () -> Unit,
    onMatchingClick: () -> Unit,
    onCareClick: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    FeedScreen(
        uiState = uiState,
        onProfileClick = onProfileClick,
        onMatchingClick = onMatchingClick,
        onCareClick = onCareClick,
        onBodyChanged = viewModel::onBodyChanged,
        onImageSelected = viewModel::onImageSelected,
        onClearSelectedImage = viewModel::clearSelectedImage,
        onSetActivePet = viewModel::setActivePet,
        onShowCreatePost = viewModel::showCreatePostSheet,
        onDismissCreatePost = viewModel::hideCreatePostSheet,
        onCreateClick = viewModel::createPost,
        onRetryClick = viewModel::load,
        onOpenCommentsClick = viewModel::toggleComments,
        onDismissComments = viewModel::closeComments,
        onCommentChanged = viewModel::onCommentChanged,
        onCreateCommentClick = viewModel::createComment,
        onReactionClick = viewModel::toggleReaction,
        onOpenPostProfile = viewModel::showPostProfile,
        onDismissPostProfile = viewModel::hidePostProfile,
        onStartEditPost = viewModel::startEditPost,
        onEditingPostBodyChanged = viewModel::onEditingPostBodyChanged,
        onEditingPostImageSelected = viewModel::onEditingPostImageSelected,
        onRemoveEditingPostImage = viewModel::removeEditingPostImage,
        onCancelEditPost = viewModel::cancelEditPost,
        onSavePost = viewModel::savePost,
        onDeletePost = viewModel::deletePost,
        onStartEditComment = viewModel::startEditComment,
        onEditingCommentChanged = viewModel::onEditingCommentChanged,
        onCancelEditComment = viewModel::cancelEditComment,
        onSaveComment = viewModel::saveComment,
        onDeleteComment = viewModel::deleteComment
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedScreen(
    uiState: FeedUiState,
    onProfileClick: () -> Unit,
    onMatchingClick: () -> Unit,
    onCareClick: () -> Unit,
    onBodyChanged: (String) -> Unit,
    onImageSelected: (android.net.Uri?) -> Unit,
    onClearSelectedImage: () -> Unit,
    onSetActivePet: (String) -> Unit,
    onShowCreatePost: () -> Unit,
    onDismissCreatePost: () -> Unit,
    onCreateClick: () -> Unit,
    onRetryClick: () -> Unit,
    onOpenCommentsClick: (String) -> Unit,
    onDismissComments: () -> Unit,
    onCommentChanged: (String, String) -> Unit,
    onCreateCommentClick: (String) -> Unit,
    onReactionClick: (PostResponse, String) -> Unit,
    onOpenPostProfile: (String) -> Unit,
    onDismissPostProfile: () -> Unit,
    onStartEditPost: (PostResponse) -> Unit,
    onEditingPostBodyChanged: (String) -> Unit,
    onEditingPostImageSelected: (android.net.Uri?) -> Unit,
    onRemoveEditingPostImage: () -> Unit,
    onCancelEditPost: () -> Unit,
    onSavePost: () -> Unit,
    onDeletePost: (String) -> Unit,
    onStartEditComment: (CommentResponse) -> Unit,
    onEditingCommentChanged: (String) -> Unit,
    onCancelEditComment: () -> Unit,
    onSaveComment: (String, String) -> Unit,
    onDeleteComment: (String, String) -> Unit
) {
    val createImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = onImageSelected
    )
    val editImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = onEditingPostImageSelected
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(2.dp)) }

        if (uiState.isLoading) {
            item { FullScreenLoading() }
            return@LazyColumn
        }

        item {
            FeedHeader(
                ownerName = uiState.ownerName,
                pets = uiState.pets,
                activePet = uiState.activePet,
                authToken = uiState.authToken,
                switchingPetId = uiState.switchingPetId,
                onProfileClick = onProfileClick,
                onPetClick = onSetActivePet,
                onCreatePostClick = onShowCreatePost
            )
        }

        item {
            ActionGrid(
                uiState = uiState,
                onMatchingClick = onMatchingClick,
                onCareClick = onCareClick,
                onProfileClick = onProfileClick
            )
        }

        uiState.errorMessage?.let { message ->
            item {
                ErrorMessage(message = message)
                TextButton(onClick = onRetryClick) {
                    Text("Повторить")
                }
            }
        }

        uiState.successMessage?.let { message ->
            item { SuccessMessage(message = message) }
        }

        item {
            SectionHeader(title = "Лента")
        }

        if (uiState.posts.isEmpty()) {
            item {
                EmptyFeedCard(onCreatePostClick = onShowCreatePost)
            }
        } else {
            items(uiState.posts, key = { it.id }) { post ->
                FeedPostCard(
                    post = post,
                    currentUserId = uiState.currentUserId,
                    authToken = uiState.authToken,
                    isReacting = post.id in uiState.reactingPostIds,
                    isDeleting = post.id in uiState.deletingPostIds,
                    onPostClick = { onOpenPostProfile(post.id) },
                    onEditPostClick = { onStartEditPost(post) },
                    onDeletePostClick = { onDeletePost(post.id) },
                    onCommentsClick = { onOpenCommentsClick(post.id) },
                    onReactionClick = { reactionType -> onReactionClick(post, reactionType) }
                )
            }
        }
    }

    if (uiState.isCreatePostSheetVisible) {
        ModalBottomSheet(onDismissRequest = onDismissCreatePost) {
            PostComposerSheet(
                title = uiState.activePet?.let { "Публикация от ${it.name}" } ?: "Новая публикация",
                body = uiState.body,
                selectedImageUri = uiState.selectedImageUri,
                existingImageUrl = null,
                authToken = uiState.authToken,
                isBusy = uiState.isCreating,
                submitText = "Опубликовать",
                onBodyChanged = onBodyChanged,
                onPickImage = {
                    createImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onClearImage = onClearSelectedImage,
                onCancel = onDismissCreatePost,
                onSubmit = onCreateClick
            )
        }
    }

    val editingPost = uiState.posts.firstOrNull { it.id == uiState.editingPostId }
    if (editingPost != null) {
        ModalBottomSheet(onDismissRequest = onCancelEditPost) {
            PostComposerSheet(
                title = "Редактировать пост",
                body = uiState.editingPostBody,
                selectedImageUri = uiState.editingPostImageUri,
                existingImageUrl = uiState.editingPostExistingImageUrl,
                authToken = uiState.authToken,
                isBusy = uiState.isUpdatingPost,
                submitText = "Сохранить",
                onBodyChanged = onEditingPostBodyChanged,
                onPickImage = {
                    editImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onClearImage = onRemoveEditingPostImage,
                onCancel = onCancelEditPost,
                onSubmit = onSavePost
            )
        }
    }

    val commentsPost = uiState.posts.firstOrNull { it.id == uiState.activeCommentsPostId }
    if (commentsPost != null) {
        ModalBottomSheet(onDismissRequest = onDismissComments) {
            CommentsSheet(
                post = commentsPost,
                currentUserId = uiState.currentUserId,
                comments = uiState.commentsByPost[commentsPost.id].orEmpty(),
                commentInput = uiState.commentInputs[commentsPost.id].orEmpty(),
                isCommentsLoading = commentsPost.id in uiState.commentsLoadingPostIds,
                isSubmittingComment = commentsPost.id in uiState.submittingCommentPostIds,
                editingCommentId = uiState.editingCommentId,
                editingCommentBody = uiState.editingCommentBody,
                busyCommentIds = uiState.busyCommentIds,
                onCommentChanged = { onCommentChanged(commentsPost.id, it) },
                onCreateCommentClick = { onCreateCommentClick(commentsPost.id) },
                onStartEditComment = onStartEditComment,
                onEditingCommentChanged = onEditingCommentChanged,
                onCancelEditComment = onCancelEditComment,
                onSaveComment = { commentId -> onSaveComment(commentsPost.id, commentId) },
                onDeleteComment = { commentId -> onDeleteComment(commentsPost.id, commentId) }
            )
        }
    }

    val profilePost = uiState.posts.firstOrNull { it.id == uiState.activeProfilePostId }
    if (profilePost != null) {
        ModalBottomSheet(onDismissRequest = onDismissPostProfile) {
            PostProfileSheet(
                post = profilePost,
                authToken = uiState.authToken
            )
        }
    }
}

@Composable
private fun FeedHeader(
    ownerName: String,
    pets: List<PetResponse>,
    activePet: PetResponse?,
    authToken: String?,
    switchingPetId: String?,
    onProfileClick: () -> Unit,
    onPetClick: (String) -> Unit,
    onCreatePostClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PetAvatar(
                imageUrl = activePet?.photo_url,
                authToken = authToken,
                contentDescription = activePet?.name,
                modifier = Modifier.clickable(onClick = onProfileClick),
                size = 54.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Добро пожаловать,", style = MaterialTheme.typography.bodyMedium, color = PetTextSecondary)
                Text(
                    text = ownerName.ifBlank { "PetSocial" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(onClick = onCreatePostClick, enabled = activePet != null) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Пост")
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = PetPrimary,
                modifier = Modifier.clickable(onClick = onProfileClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(activePet?.name ?: "Добавить питомца", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            pets.filterNot { it.id == activePet?.id }.forEach { pet ->
                PetSwitchChip(
                    label = pet.name,
                    selected = false,
                    enabled = switchingPetId == null,
                    isLoading = switchingPetId == pet.id,
                    onClick = { onPetClick(pet.id) }
                )
            }
            Surface(
                shape = CircleShape,
                color = PetSurface,
                border = BorderStroke(1.dp, PetOutline),
                modifier = Modifier
                    .size(42.dp)
                    .clickable(onClick = onProfileClick),
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = PetOnSurface)
                }
            }
        }
    }
}

@Composable
private fun PetSwitchChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (selected) PetPrimary else PetSurface,
        border = if (selected) null else BorderStroke(1.dp, PetOutline),
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
        shadowElevation = if (selected) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = if (selected) Color.White else PetPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (selected) Color.White else PetOnSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ActionGrid(
    uiState: FeedUiState,
    onMatchingClick: () -> Unit,
    onCareClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeedActionCard(
                title = "Найти пару",
                subtitle = "",
                icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = PetPrimary) },
                modifier = Modifier.weight(1f),
                onClick = onMatchingClick
            )
            FeedActionCard(
                title = "Расписание",
                subtitle = "",
                icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = PetSecondary) },
                modifier = Modifier.weight(1f),
                onClick = onCareClick
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeedActionCard(
                title = "Найти услугу",
                subtitle = "",
                icon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = PetInfo) },
                modifier = Modifier.weight(1f),
                onClick = onCareClick
            )
            FeedActionCard(
                title = "Питомцы",
                subtitle = "",
                icon = { Icon(Icons.Rounded.Add, contentDescription = null, tint = PetOnSurface) },
                modifier = Modifier.weight(1f),
                onClick = onProfileClick
            )
        }
    }
}

@Composable
private fun FeedActionCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ProductCard(
        modifier = modifier
            .height(104.dp)
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(PetSurfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        if (subtitle.isNotBlank()) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = PetTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmptyFeedCard(onCreatePostClick: () -> Unit) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Text("Пока здесь тихо", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Опубликуйте первый пост от имени активного питомца или найдите новых друзей.", color = PetTextSecondary)
        Button(onClick = onCreatePostClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Новая публикация")
        }
    }
}

@Composable
private fun FeedPostCard(
    post: PostResponse,
    currentUserId: String,
    authToken: String?,
    isReacting: Boolean,
    isDeleting: Boolean,
    onPostClick: () -> Unit,
    onEditPostClick: () -> Unit,
    onDeletePostClick: () -> Unit,
    onCommentsClick: () -> Unit,
    onReactionClick: (String) -> Unit
) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPostClick),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PetAvatar(post.pet_photo_url, authToken, post.pet_name, size = 44.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(postTitle(post), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(postTime(post.created_at), style = MaterialTheme.typography.bodySmall, color = PetTextSecondary)
            }
            if (post.author_user_id == currentUserId) {
                IconButton(onClick = onEditPostClick) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = PetTextSecondary)
                }
                TextButton(onClick = onDeletePostClick, enabled = !isDeleting) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        if (!post.image_url.isNullOrBlank()) {
            AuthenticatedImage(
                imageUrl = post.image_url,
                authToken = authToken,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(post.body, style = MaterialTheme.typography.bodyMedium, color = PetOnSurface)
            ReactionRow(
                post = post,
                isReacting = isReacting,
                onReactionClick = onReactionClick,
                onCommentsClick = onCommentsClick
            )
        }
    }
}

@Composable
private fun ReactionRow(
    post: PostResponse,
    isReacting: Boolean,
    onReactionClick: (String) -> Unit,
    onCommentsClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            reactionTypes.forEach { reaction ->
                ReactionButton(
                    icon = reaction.icon,
                    count = post.reaction_counts[reaction.type].orZero(),
                    selected = post.my_reaction == reaction.type,
                    enabled = !isReacting,
                    onClick = { onReactionClick(reaction.type) }
                )
            }
        }
        TextButton(onClick = onCommentsClick) {
            Text("💬", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.width(5.dp))
            Text(post.comments_count.toString())
        }
    }
}

@Composable
private fun ReactionButton(
    icon: String,
    count: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) PetPrimaryLight else PetSurface,
        border = BorderStroke(1.dp, if (selected) PetPrimary else PetOutline),
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(4.dp))
            Text(count.toString(), style = MaterialTheme.typography.labelMedium, color = PetTextSecondary)
        }
    }
}

@Composable
private fun PostComposerSheet(
    title: String,
    body: String,
    selectedImageUri: android.net.Uri?,
    existingImageUrl: String?,
    authToken: String?,
    isBusy: Boolean,
    submitText: String,
    onBodyChanged: (String) -> Unit,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    onCancel: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = body,
            onValueChange = onBodyChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Что интересного сегодня?") },
            minLines = 4,
            enabled = !isBusy
        )
        OutlinedButton(onClick = onPickImage, modifier = Modifier.fillMaxWidth(), enabled = !isBusy) {
            Text(if (selectedImageUri == null && existingImageUrl.isNullOrBlank()) "Добавить фото" else "Заменить фото")
        }
        when {
            selectedImageUri != null -> {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                TextButton(onClick = onClearImage, enabled = !isBusy) {
                    Text("Убрать фото")
                }
            }
            !existingImageUrl.isNullOrBlank() -> {
                AuthenticatedImage(
                    imageUrl = existingImageUrl,
                    authToken = authToken,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                TextButton(onClick = onClearImage, enabled = !isBusy) {
                    Text("Убрать фото")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !isBusy) {
                Text("Отмена")
            }
            Button(onClick = onSubmit, modifier = Modifier.weight(1f), enabled = !isBusy) {
                if (isBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(submitText)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun CommentsSheet(
    post: PostResponse,
    currentUserId: String,
    comments: List<CommentResponse>,
    commentInput: String,
    isCommentsLoading: Boolean,
    isSubmittingComment: Boolean,
    editingCommentId: String?,
    editingCommentBody: String,
    busyCommentIds: Set<String>,
    onCommentChanged: (String) -> Unit,
    onCreateCommentClick: () -> Unit,
    onStartEditComment: (CommentResponse) -> Unit,
    onEditingCommentChanged: (String) -> Unit,
    onCancelEditComment: () -> Unit,
    onSaveComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 680.dp)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Комментарии", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(postTitle(post), color = PetTextSecondary, style = MaterialTheme.typography.bodySmall)
        }

        if (isCommentsLoading) {
            item { CircularProgressIndicator() }
        } else if (comments.isEmpty()) {
            item {
                Text("Комментариев пока нет", color = PetTextSecondary)
            }
        } else {
            items(comments, key = { it.id }) { comment ->
                CommentRow(
                    currentUserId = currentUserId,
                    comment = comment,
                    isEditing = editingCommentId == comment.id,
                    editingCommentBody = editingCommentBody,
                    isBusy = comment.id in busyCommentIds,
                    onStartEditComment = onStartEditComment,
                    onEditingCommentChanged = onEditingCommentChanged,
                    onCancelEditComment = onCancelEditComment,
                    onSaveComment = onSaveComment,
                    onDeleteComment = onDeleteComment
                )
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentInput,
                    onValueChange = onCommentChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text("Ваш комментарий") },
                    minLines = 1,
                    enabled = !isSubmittingComment
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onCreateCommentClick,
                    enabled = !isSubmittingComment
                ) {
                    if (isSubmittingComment) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, tint = PetPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentRow(
    currentUserId: String,
    comment: CommentResponse,
    isEditing: Boolean,
    editingCommentBody: String,
    isBusy: Boolean,
    onStartEditComment: (CommentResponse) -> Unit,
    onEditingCommentChanged: (String) -> Unit,
    onCancelEditComment: () -> Unit,
    onSaveComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = PetSurfaceMuted
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(comment.author_name.ifBlank { "Владелец питомца" }, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            if (isEditing) {
                OutlinedTextField(
                    value = editingCommentBody,
                    onValueChange = onEditingCommentChanged,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onSaveComment(comment.id) }, enabled = !isBusy) {
                        Text("Сохранить")
                    }
                    TextButton(onClick = onCancelEditComment, enabled = !isBusy) {
                        Text("Отмена")
                    }
                }
            } else {
                Text(comment.body, style = MaterialTheme.typography.bodyMedium)
            }
            if (comment.author_user_id == currentUserId && !isEditing) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onStartEditComment(comment) }, enabled = !isBusy) {
                        Text("Изменить")
                    }
                    TextButton(onClick = { onDeleteComment(comment.id) }, enabled = !isBusy) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun PostProfileSheet(
    post: PostResponse,
    authToken: String?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 680.dp)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Профиль публикации", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            ProfileCard(
                title = post.pet_name,
                subtitle = petSubtitle(post),
                imageUrl = post.pet_photo_url,
                authToken = authToken,
                fallbackIcon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = PetPrimary) },
                body = post.pet_bio,
                chips = post.pet_personality_tags + post.pet_interests
            )
        }
        item {
            ProfileCard(
                title = post.author_name.ifBlank { "Владелец питомца" },
                subtitle = listOfNotNull(post.author_city, post.author_email.ifBlank { null }).joinToString(" · "),
                imageUrl = post.author_avatar_url,
                authToken = authToken,
                fallbackIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = PetPrimary) },
                body = post.author_bio,
                chips = emptyList()
            )
        }
    }
}

@Composable
private fun ProfileCard(
    title: String,
    subtitle: String,
    imageUrl: String?,
    authToken: String?,
    fallbackIcon: @Composable () -> Unit,
    body: String?,
    chips: List<String>
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(PetSurfaceMuted),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AuthenticatedImage(
                        imageUrl = imageUrl,
                        authToken = authToken,
                        contentDescription = title,
                        modifier = Modifier.matchParentSize()
                    )
                } else {
                    fallbackIcon()
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (subtitle.isNotBlank()) {
                    Text(subtitle, color = PetTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (!body.isNullOrBlank()) {
            Text(body, color = PetOnSurface, style = MaterialTheme.typography.bodyMedium)
        }
        if (chips.isNotEmpty()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEach { chip ->
                    StatusChip(label = chip, tone = ChipTone.Neutral)
                }
            }
        }
    }
}

@Composable
private fun PetAvatar(
    imageUrl: String?,
    authToken: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(PetPrimaryLight)
            .border(1.dp, PetOutline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AuthenticatedImage(
                imageUrl = imageUrl,
                authToken = authToken,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Icon(Icons.Default.Favorite, contentDescription = null, tint = PetPrimary, modifier = Modifier.size(size * 0.45f))
        }
    }
}

private data class ReactionSpec(
    val type: String,
    val icon: String
)

private val reactionTypes = listOf(
    ReactionSpec("like", "♡"),
    ReactionSpec("love", "♥"),
    ReactionSpec("funny", "☺"),
    ReactionSpec("support", "+")
)

private fun Int?.orZero(): Int = this ?: 0

private fun postTitle(post: PostResponse): String {
    val author = post.author_name.ifBlank { "Владелец" }
    return "$author и ${post.pet_name}"
}

private fun postTime(value: String): String {
    return value.take(10).ifBlank { "сейчас" }
}

private fun petSubtitle(post: PostResponse): String {
    return listOfNotNull(
        post.pet_species.takeIf { it.isNotBlank() },
        post.pet_breed,
        post.pet_sex,
        post.pet_birth_date?.take(4)
    ).joinToString(" · ")
}
