package com.example.petsocial.feature.feed

import android.net.Uri
import com.example.petsocial.core.network.model.feed.CommentResponse
import com.example.petsocial.core.network.model.feed.PostResponse
import com.example.petsocial.core.network.model.pets.PetResponse

data class FeedUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val currentUserId: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val authToken: String? = null,
    val activePet: PetResponse? = null,
    val posts: List<PostResponse> = emptyList(),
    val body: String = "",
    val selectedImageUri: Uri? = null,
    val expandedPostId: String? = null,
    val commentsByPost: Map<String, List<CommentResponse>> = emptyMap(),
    val commentInputs: Map<String, String> = emptyMap(),
    val commentsLoadingPostIds: Set<String> = emptySet(),
    val submittingCommentPostIds: Set<String> = emptySet(),
    val reactingPostIds: Set<String> = emptySet(),
    val editingCommentId: String? = null,
    val editingCommentBody: String = "",
    val busyCommentIds: Set<String> = emptySet()
)
