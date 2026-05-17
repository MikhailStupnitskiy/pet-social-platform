package com.example.petsocial.feature.feed

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.auth.data.AuthRepository
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.session.SessionEventBus
import com.example.petsocial.core.datastore.auth.TokenStorage
import com.example.petsocial.core.network.api.PetsApi
import com.example.petsocial.core.network.api.ProfileApi
import com.example.petsocial.core.network.model.feed.CommentResponse
import com.example.petsocial.core.network.model.feed.PostResponse
import com.example.petsocial.core.network.model.profile.ProfileStatsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val petsApi: PetsApi,
    private val profileApi: ProfileApi,
    private val authRepository: AuthRepository,
    private val tokenStorage: TokenStorage,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tokenStorage.token.collect { token ->
                _uiState.value = _uiState.value.copy(authToken = token)
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            val currentUser = when (val meResult = safeApiCall { authRepository.getMe() }) {
                is AppResult.Success -> meResult.data
                is AppResult.Error -> {
                    if (handleUnauthorized(meResult.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = meResult.error.message
                    )
                    return@launch
                }
            }
            val currentUserId = currentUser.id

            val ownerName = when (val profileResult = safeApiCall { profileApi.getMe() }) {
                is AppResult.Success -> profileResult.data.name.ifBlank { currentUser.email }
                is AppResult.Error -> {
                    if (handleUnauthorized(profileResult.error)) {
                        return@launch
                    }
                    currentUser.email
                }
            }

            val stats = currentProfileStatsOrNull()

            val pets = when (val petsResult = safeApiCall { petsApi.getPets() }) {
                is AppResult.Success -> petsResult.data
                is AppResult.Error -> {
                    if (handleUnauthorized(petsResult.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = petsResult.error.message
                    )
                    return@launch
                }
            }
            val activePet = pets.firstOrNull { it.is_active }

            when (val feedResult = safeApiCall { repository.getFeed() }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUserId = currentUserId,
                        ownerName = ownerName,
                        petsCount = stats?.pets_count ?: 0,
                        matchesCount = stats?.matches_count ?: 0,
                        postsCount = stats?.posts_count ?: 0,
                        pets = pets,
                        activePet = activePet,
                        posts = feedResult.data
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(feedResult.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUserId = currentUserId,
                        ownerName = ownerName,
                        petsCount = stats?.pets_count ?: 0,
                        matchesCount = stats?.matches_count ?: 0,
                        postsCount = stats?.posts_count ?: 0,
                        pets = pets,
                        activePet = activePet,
                        errorMessage = feedResult.error.message
                    )
                }
            }
        }
    }

    fun onBodyChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            body = value,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            errorMessage = null,
            successMessage = null
        )
    }

    fun clearSelectedImage() {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = null,
            errorMessage = null,
            successMessage = null
        )
    }

    fun showCreatePostSheet() {
        _uiState.value = _uiState.value.copy(
            isCreatePostSheetVisible = true,
            errorMessage = null,
            successMessage = null
        )
    }

    fun hideCreatePostSheet() {
        if (_uiState.value.isCreating) {
            return
        }
        _uiState.value = _uiState.value.copy(
            isCreatePostSheetVisible = false,
            body = "",
            selectedImageUri = null
        )
    }

    fun setActivePet(petId: String) {
        val state = _uiState.value
        if (state.activePet?.id == petId || state.switchingPetId != null) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                switchingPetId = petId,
                errorMessage = null,
                successMessage = null
            )

            when (val result = safeApiCall { petsApi.setActivePet(petId) }) {
                is AppResult.Success -> {
                    val pets = _uiState.value.pets.map { pet ->
                        pet.copy(is_active = pet.id == petId)
                    }
                    _uiState.value = _uiState.value.copy(
                        switchingPetId = null,
                        pets = pets,
                        activePet = pets.firstOrNull { it.is_active }
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        switchingPetId = null,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun createPost() {
        val state = _uiState.value
        val activePet = state.activePet

        if (activePet == null) {
            _uiState.value = state.copy(errorMessage = "Choose an active pet before posting")
            return
        }
        if (state.body.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Write something for the post")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreating = true,
                errorMessage = null,
                successMessage = null
            )

            when (
                val result = safeApiCall {
                    val imageUrl = state.selectedImageUri?.let { uri ->
                        repository.uploadImage(uri)
                    }

                    repository.createPost(
                        petId = activePet.id,
                        body = state.body.trim(),
                        imageUrl = imageUrl
                    )
                }
            ) {
                is AppResult.Success -> {
                    val newPosts = listOf(result.data) + _uiState.value.posts
                    val stats = currentProfileStatsOrNull()
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        isCreatePostSheetVisible = false,
                        body = "",
                        selectedImageUri = null,
                        successMessage = "Post published",
                        posts = newPosts,
                        petsCount = stats?.pets_count ?: _uiState.value.petsCount,
                        matchesCount = stats?.matches_count ?: _uiState.value.matchesCount,
                        postsCount = stats?.posts_count ?: _uiState.value.postsCount
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun toggleComments(postId: String) {
        val state = _uiState.value
        if (state.activeCommentsPostId == postId) {
            _uiState.value = state.copy(activeCommentsPostId = null)
            return
        }

        _uiState.value = state.copy(activeCommentsPostId = postId, errorMessage = null, successMessage = null)

        if (!state.commentsByPost.containsKey(postId)) {
            loadComments(postId)
        }
    }

    fun closeComments() {
        _uiState.value = _uiState.value.copy(activeCommentsPostId = null)
    }

    fun showPostProfile(postId: String) {
        _uiState.value = _uiState.value.copy(activeProfilePostId = postId)
    }

    fun hidePostProfile() {
        _uiState.value = _uiState.value.copy(activeProfilePostId = null)
    }

    fun loadComments(postId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                commentsLoadingPostIds = _uiState.value.commentsLoadingPostIds + postId,
                errorMessage = null
            )

            when (val result = safeApiCall { repository.getComments(postId) }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        commentsLoadingPostIds = _uiState.value.commentsLoadingPostIds - postId,
                        commentsByPost = _uiState.value.commentsByPost + (postId to result.data)
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        commentsLoadingPostIds = _uiState.value.commentsLoadingPostIds - postId,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun onCommentChanged(postId: String, value: String) {
        _uiState.value = _uiState.value.copy(
            commentInputs = _uiState.value.commentInputs + (postId to value),
            errorMessage = null,
            successMessage = null
        )
    }

    fun createComment(postId: String) {
        val state = _uiState.value
        val body = state.commentInputs[postId].orEmpty().trim()
        if (body.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Write a comment first")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                submittingCommentPostIds = _uiState.value.submittingCommentPostIds + postId,
                errorMessage = null,
                successMessage = null
            )

            when (val result = safeApiCall { repository.createComment(postId, body) }) {
                is AppResult.Success -> {
                    val comments = _uiState.value.commentsByPost[postId].orEmpty() + result.data

                    _uiState.value = _uiState.value.copy(
                        submittingCommentPostIds = _uiState.value.submittingCommentPostIds - postId,
                        commentInputs = _uiState.value.commentInputs + (postId to ""),
                        commentsByPost = _uiState.value.commentsByPost + (postId to comments),
                        posts = incrementComments(postId)
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        submittingCommentPostIds = _uiState.value.submittingCommentPostIds - postId,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun startEditComment(comment: CommentResponse) {
        _uiState.value = _uiState.value.copy(
            editingCommentId = comment.id,
            editingCommentBody = comment.body,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onEditingCommentChanged(value: String) {
        _uiState.value = _uiState.value.copy(editingCommentBody = value)
    }

    fun cancelEditComment() {
        _uiState.value = _uiState.value.copy(
            editingCommentId = null,
            editingCommentBody = ""
        )
    }

    fun saveComment(postId: String, commentId: String) {
        val body = _uiState.value.editingCommentBody.trim()
        if (body.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Comment cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                busyCommentIds = _uiState.value.busyCommentIds + commentId,
                errorMessage = null,
                successMessage = null
            )

            when (val result = safeApiCall { repository.updateComment(postId, commentId, body) }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        busyCommentIds = _uiState.value.busyCommentIds - commentId,
                        editingCommentId = null,
                        editingCommentBody = "",
                        commentsByPost = replaceComment(postId, result.data)
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        busyCommentIds = _uiState.value.busyCommentIds - commentId,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun deleteComment(postId: String, commentId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                busyCommentIds = _uiState.value.busyCommentIds + commentId,
                errorMessage = null,
                successMessage = null
            )

            when (val result = safeApiCall { repository.deleteComment(postId, commentId) }) {
                is AppResult.Success -> {
                    val comments = _uiState.value.commentsByPost[postId].orEmpty()
                        .filterNot { it.id == commentId }

                    _uiState.value = _uiState.value.copy(
                        busyCommentIds = _uiState.value.busyCommentIds - commentId,
                        commentsByPost = _uiState.value.commentsByPost + (postId to comments),
                        posts = decrementComments(postId),
                        editingCommentId = if (_uiState.value.editingCommentId == commentId) null else _uiState.value.editingCommentId
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        busyCommentIds = _uiState.value.busyCommentIds - commentId,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun startEditPost(post: PostResponse) {
        _uiState.value = _uiState.value.copy(
            editingPostId = post.id,
            editingPostBody = post.body,
            editingPostImageUri = null,
            editingPostExistingImageUrl = post.image_url,
            isEditingPostImageRemoved = false,
            errorMessage = null,
            successMessage = null
        )
    }

    fun onEditingPostBodyChanged(value: String) {
        _uiState.value = _uiState.value.copy(editingPostBody = value)
    }

    fun onEditingPostImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(
            editingPostImageUri = uri,
            isEditingPostImageRemoved = false,
            errorMessage = null,
            successMessage = null
        )
    }

    fun removeEditingPostImage() {
        _uiState.value = _uiState.value.copy(
            editingPostImageUri = null,
            editingPostExistingImageUrl = null,
            isEditingPostImageRemoved = true,
            errorMessage = null,
            successMessage = null
        )
    }

    fun cancelEditPost() {
        if (_uiState.value.isUpdatingPost) {
            return
        }
        _uiState.value = _uiState.value.copy(
            editingPostId = null,
            editingPostBody = "",
            editingPostImageUri = null,
            editingPostExistingImageUrl = null,
            isEditingPostImageRemoved = false
        )
    }

    fun savePost() {
        val state = _uiState.value
        val postId = state.editingPostId ?: return
        val body = state.editingPostBody.trim()

        if (body.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Post text cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUpdatingPost = true,
                errorMessage = null,
                successMessage = null
            )

            when (
                val result = safeApiCall {
                    val imageUrl = state.editingPostImageUri?.let { uri ->
                        repository.uploadImage(uri)
                    } ?: state.editingPostExistingImageUrl

                    repository.updatePost(
                        postId = postId,
                        body = body,
                        imageUrl = imageUrl
                    )
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUpdatingPost = false,
                        editingPostId = null,
                        editingPostBody = "",
                        editingPostImageUri = null,
                        editingPostExistingImageUrl = null,
                        isEditingPostImageRemoved = false,
                        posts = replacePost(result.data),
                        successMessage = "Post updated"
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        isUpdatingPost = false,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                deletingPostIds = _uiState.value.deletingPostIds + postId,
                errorMessage = null,
                successMessage = null
            )

            when (val result = safeApiCall { repository.deletePost(postId) }) {
                is AppResult.Success -> {
                    val stats = currentProfileStatsOrNull()
                    _uiState.value = _uiState.value.copy(
                        deletingPostIds = _uiState.value.deletingPostIds - postId,
                        posts = _uiState.value.posts.filterNot { it.id == postId },
                        commentsByPost = _uiState.value.commentsByPost - postId,
                        activeCommentsPostId = _uiState.value.activeCommentsPostId.takeIf { it != postId },
                        activeProfilePostId = _uiState.value.activeProfilePostId.takeIf { it != postId },
                        editingPostId = _uiState.value.editingPostId.takeIf { it != postId },
                        petsCount = stats?.pets_count ?: _uiState.value.petsCount,
                        matchesCount = stats?.matches_count ?: _uiState.value.matchesCount,
                        postsCount = stats?.posts_count ?: _uiState.value.postsCount,
                        successMessage = "Post deleted"
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        deletingPostIds = _uiState.value.deletingPostIds - postId,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    fun toggleReaction(post: PostResponse, reactionType: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                reactingPostIds = _uiState.value.reactingPostIds + post.id,
                errorMessage = null,
                successMessage = null
            )

            val result = if (post.my_reaction == reactionType) {
                safeApiCall { repository.deleteReaction(post.id) }
            } else {
                safeApiCall { repository.setReaction(post.id, reactionType) }
            }

            when (result) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        reactingPostIds = _uiState.value.reactingPostIds - post.id,
                        posts = replacePost(result.data)
                    )
                }

                is AppResult.Error -> {
                    if (handleUnauthorized(result.error)) {
                        return@launch
                    }

                    _uiState.value = _uiState.value.copy(
                        reactingPostIds = _uiState.value.reactingPostIds - post.id,
                        errorMessage = result.error.message
                    )
                }
            }
        }
    }

    private fun handleUnauthorized(error: AppError): Boolean {
        if (error is AppError.Unauthorized) {
            sessionEventBus.notifyUnauthorized()
            return true
        }

        return false
    }

    private suspend fun currentProfileStatsOrNull(): ProfileStatsResponse? {
        return when (val statsResult = safeApiCall { profileApi.getStats() }) {
            is AppResult.Success -> statsResult.data
            is AppResult.Error -> {
                handleUnauthorized(statsResult.error)
                null
            }
        }
    }

    private fun replacePost(post: PostResponse): List<PostResponse> {
        return _uiState.value.posts.map { current ->
            if (current.id == post.id) post else current
        }
    }

    private fun incrementComments(postId: String): List<PostResponse> {
        return _uiState.value.posts.map { post ->
            if (post.id == postId) post.copy(comments_count = post.comments_count + 1) else post
        }
    }

    private fun decrementComments(postId: String): List<PostResponse> {
        return _uiState.value.posts.map { post ->
            if (post.id == postId) post.copy(comments_count = (post.comments_count - 1).coerceAtLeast(0)) else post
        }
    }

    private fun replaceComment(postId: String, comment: CommentResponse): Map<String, List<CommentResponse>> {
        val comments = _uiState.value.commentsByPost[postId].orEmpty().map { current ->
            if (current.id == comment.id) comment else current
        }

        return _uiState.value.commentsByPost + (postId to comments)
    }
}
