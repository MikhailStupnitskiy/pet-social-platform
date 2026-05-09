package com.example.petsocial.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petsocial.core.common.result.AppError
import com.example.petsocial.core.common.result.AppResult
import com.example.petsocial.core.common.result.safeApiCall
import com.example.petsocial.core.common.session.SessionEventBus
import com.example.petsocial.core.network.api.PetsApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository,
    private val petsApi: PetsApi,
    private val sessionEventBus: SessionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

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

    fun onImageUrlChanged(value: String) {
        _uiState.value = _uiState.value.copy(
            imageUrl = value,
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
                    repository.createPost(
                        petId = activePet.id,
                        body = state.body.trim(),
                        imageUrl = state.imageUrl.trim().ifBlank { null }
                    )
                }
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        body = "",
                        imageUrl = "",
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

    private fun handleUnauthorized(error: AppError): Boolean {
        if (error is AppError.Unauthorized) {
            sessionEventBus.notifyUnauthorized()
            return true
        }

        return false
    }
}
