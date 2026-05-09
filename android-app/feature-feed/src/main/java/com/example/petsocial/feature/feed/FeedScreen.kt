package com.example.petsocial.feature.feed

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.network.model.feed.CommentResponse
import com.example.petsocial.core.network.model.feed.PostResponse
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.SectionTitle

@Composable
fun FeedRoute(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    FeedScreen(
        uiState = uiState,
        onBodyChanged = viewModel::onBodyChanged,
        onImageUrlChanged = viewModel::onImageUrlChanged,
        onCreateClick = viewModel::createPost,
        onRetryClick = viewModel::load,
        onToggleCommentsClick = viewModel::toggleComments,
        onCommentChanged = viewModel::onCommentChanged,
        onCreateCommentClick = viewModel::createComment,
        onReactionClick = viewModel::toggleReaction,
        onStartEditComment = viewModel::startEditComment,
        onEditingCommentChanged = viewModel::onEditingCommentChanged,
        onCancelEditComment = viewModel::cancelEditComment,
        onSaveComment = viewModel::saveComment,
        onDeleteComment = viewModel::deleteComment
    )
}

@Composable
private fun FeedScreen(
    uiState: FeedUiState,
    onBodyChanged: (String) -> Unit,
    onImageUrlChanged: (String) -> Unit,
    onCreateClick: () -> Unit,
    onRetryClick: () -> Unit,
    onToggleCommentsClick: (String) -> Unit,
    onCommentChanged: (String, String) -> Unit,
    onCreateCommentClick: (String) -> Unit,
    onReactionClick: (PostResponse, String) -> Unit,
    onStartEditComment: (CommentResponse) -> Unit,
    onEditingCommentChanged: (String) -> Unit,
    onCancelEditComment: () -> Unit,
    onSaveComment: (String, String) -> Unit,
    onDeleteComment: (String, String) -> Unit
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

        item {
            SectionTitle("Community feed")
        }

        if (uiState.activePet != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Posting as ${uiState.activePet.name}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        OutlinedTextField(
                            value = uiState.body,
                            onValueChange = onBodyChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("What happened today?") },
                            minLines = 3,
                            enabled = !uiState.isCreating
                        )
                        OutlinedTextField(
                            value = uiState.imageUrl,
                            onValueChange = onImageUrlChanged,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Image URL, optional") },
                            singleLine = true,
                            enabled = !uiState.isCreating
                        )
                        Button(
                            onClick = onCreateClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isCreating
                        ) {
                            if (uiState.isCreating) {
                                CircularProgressIndicator()
                            } else {
                                Text("Publish")
                            }
                        }
                    }
                }
            }
        } else {
            item {
                Text("Choose an active pet to publish posts.")
            }
        }

        if (uiState.errorMessage != null) {
            item {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onRetryClick) {
                    Text("Retry")
                }
            }
        }

        if (uiState.successMessage != null) {
            item {
                Text(
                    text = uiState.successMessage,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (uiState.posts.isEmpty()) {
            item {
                Text("No posts yet. The first pawprint is still up for grabs.")
            }
        } else {
            items(uiState.posts) { post ->
                FeedPostCard(
                    post = post,
                    currentUserId = uiState.currentUserId,
                    isExpanded = uiState.expandedPostId == post.id,
                    comments = uiState.commentsByPost[post.id].orEmpty(),
                    commentInput = uiState.commentInputs[post.id].orEmpty(),
                    isCommentsLoading = post.id in uiState.commentsLoadingPostIds,
                    isSubmittingComment = post.id in uiState.submittingCommentPostIds,
                    isReacting = post.id in uiState.reactingPostIds,
                    editingCommentId = uiState.editingCommentId,
                    editingCommentBody = uiState.editingCommentBody,
                    busyCommentIds = uiState.busyCommentIds,
                    onToggleCommentsClick = { onToggleCommentsClick(post.id) },
                    onCommentChanged = { onCommentChanged(post.id, it) },
                    onCreateCommentClick = { onCreateCommentClick(post.id) },
                    onReactionClick = { reactionType -> onReactionClick(post, reactionType) },
                    onStartEditComment = onStartEditComment,
                    onEditingCommentChanged = onEditingCommentChanged,
                    onCancelEditComment = onCancelEditComment,
                    onSaveComment = { commentId -> onSaveComment(post.id, commentId) },
                    onDeleteComment = { commentId -> onDeleteComment(post.id, commentId) }
                )
            }
        }
    }
}

@Composable
private fun FeedPostCard(
    post: PostResponse,
    currentUserId: String,
    isExpanded: Boolean,
    comments: List<CommentResponse>,
    commentInput: String,
    isCommentsLoading: Boolean,
    isSubmittingComment: Boolean,
    isReacting: Boolean,
    editingCommentId: String?,
    editingCommentBody: String,
    busyCommentIds: Set<String>,
    onToggleCommentsClick: () -> Unit,
    onCommentChanged: (String) -> Unit,
    onCreateCommentClick: () -> Unit,
    onReactionClick: (String) -> Unit,
    onStartEditComment: (CommentResponse) -> Unit,
    onEditingCommentChanged: (String) -> Unit,
    onCancelEditComment: () -> Unit,
    onSaveComment: (String) -> Unit,
    onDeleteComment: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = post.pet_name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${post.pet_species} by ${post.author_name.ifBlank { "Pet parent" }}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(post.body)
            if (!post.image_url.isNullOrBlank()) {
                Text(
                    text = "Photo: ${post.image_url}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            ReactionBar(
                post = post,
                isReacting = isReacting,
                onReactionClick = onReactionClick
            )

            TextButton(onClick = onToggleCommentsClick) {
                Text(
                    if (isExpanded) {
                        "Hide comments (${post.comments_count})"
                    } else {
                        "Comments (${post.comments_count})"
                    }
                )
            }

            if (isExpanded) {
                CommentsBlock(
                    currentUserId = currentUserId,
                    comments = comments,
                    commentInput = commentInput,
                    isCommentsLoading = isCommentsLoading,
                    isSubmittingComment = isSubmittingComment,
                    editingCommentId = editingCommentId,
                    editingCommentBody = editingCommentBody,
                    busyCommentIds = busyCommentIds,
                    onCommentChanged = onCommentChanged,
                    onCreateCommentClick = onCreateCommentClick,
                    onStartEditComment = onStartEditComment,
                    onEditingCommentChanged = onEditingCommentChanged,
                    onCancelEditComment = onCancelEditComment,
                    onSaveComment = onSaveComment,
                    onDeleteComment = onDeleteComment
                )
            }
        }
    }
}

@Composable
private fun ReactionBar(
    post: PostResponse,
    isReacting: Boolean,
    onReactionClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Reactions: ${post.reactions_count}",
            style = MaterialTheme.typography.bodySmall
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            reactionTypes.forEach { reactionType ->
                val count = post.reaction_counts[reactionType].orZero()
                val selected = post.my_reaction == reactionType
                val label = "$reactionType $count"

                if (selected) {
                    Button(
                        onClick = { onReactionClick(reactionType) },
                        enabled = !isReacting
                    ) {
                        Text(label)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onReactionClick(reactionType) },
                        enabled = !isReacting
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentsBlock(
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
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isCommentsLoading) {
            CircularProgressIndicator()
        } else if (comments.isEmpty()) {
            Text(
                text = "No comments yet",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            comments.forEach { comment ->
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

        OutlinedTextField(
            value = commentInput,
            onValueChange = onCommentChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Add a comment") },
            minLines = 2,
            enabled = !isSubmittingComment
        )

        Button(
            onClick = onCreateCommentClick,
            enabled = !isSubmittingComment,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSubmittingComment) {
                CircularProgressIndicator()
            } else {
                Text("Send comment")
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
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = comment.author_name.ifBlank { "Pet parent" },
                style = MaterialTheme.typography.labelMedium
            )

            if (isEditing) {
                OutlinedTextField(
                    value = editingCommentBody,
                    onValueChange = onEditingCommentChanged,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isBusy
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onSaveComment(comment.id) },
                        enabled = !isBusy
                    ) {
                        Text("Save")
                    }
                    TextButton(
                        onClick = onCancelEditComment,
                        enabled = !isBusy
                    ) {
                        Text("Cancel")
                    }
                }
            } else {
                Text(comment.body)
            }

            if (comment.author_user_id == currentUserId && !isEditing) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { onStartEditComment(comment) },
                        enabled = !isBusy
                    ) {
                        Text("Edit")
                    }
                    TextButton(
                        onClick = { onDeleteComment(comment.id) },
                        enabled = !isBusy
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

private val reactionTypes = listOf("like", "love", "funny", "support")

private fun Int?.orZero(): Int = this ?: 0
