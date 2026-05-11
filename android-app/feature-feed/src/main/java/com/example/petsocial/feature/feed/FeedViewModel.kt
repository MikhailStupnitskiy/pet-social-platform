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
import com.example.petsocial.core.network.model.feed.CommentResponse
import com.example.petsocial.core.network.model.feed.PostResponse
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

            val currentUserId = when (val meResult = safeApiCall { authRepository.getMe() }) {
                is AppResult.Success -> meResult.data.id
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

            val activePet = when (val petsResult = safeApiCall { petsApi.getPets() }) {
                is AppResult.Success -> petsResult.data.firstOrNull { it.is_active }
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

            when (val feedResult = safeApiCall { repository.getFeed() }) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUserId = currentUserId,
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
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        body = "",
                        selectedImageUri = null,
                        successMessage = "Post published",
                        posts = listOf(result.data) + _uiState.value.posts
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
        if (state.expandedPostId == postId) {
            _uiState.value = state.copy(expandedPostId = null)
            return
        }

        _uiState.value = state.copy(expandedPostId = postId, errorMessage = null, successMessage = null)

        if (!state.commentsByPost.containsKey(postId)) {
            loadComments(postId)
        }
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
