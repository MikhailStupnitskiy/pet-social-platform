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
    val ownerName: String = "",
    val petsCount: Int = 0,
    val matchesCount: Int = 0,
    val postsCount: Int = 0,
    val pets: List<PetResponse> = emptyList(),
    val activePet: PetResponse? = null,
    val switchingPetId: String? = null,
    val posts: List<PostResponse> = emptyList(),
    val body: String = "",
    val selectedImageUri: Uri? = null,
    val isCreatePostSheetVisible: Boolean = false,
    val activeCommentsPostId: String? = null,
    val activeProfilePostId: String? = null,
    val editingPostId: String? = null,
    val editingPostBody: String = "",
    val editingPostImageUri: Uri? = null,
    val editingPostExistingImageUrl: String? = null,
    val isEditingPostImageRemoved: Boolean = false,
    val isUpdatingPost: Boolean = false,
    val commentsByPost: Map<String, List<CommentResponse>> = emptyMap(),
    val commentInputs: Map<String, String> = emptyMap(),
    val commentsLoadingPostIds: Set<String> = emptySet(),
    val submittingCommentPostIds: Set<String> = emptySet(),
    val reactingPostIds: Set<String> = emptySet(),
    val deletingPostIds: Set<String> = emptySet(),
    val editingCommentId: String? = null,
    val editingCommentBody: String = "",
    val busyCommentIds: Set<String> = emptySet()
)
