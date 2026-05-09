package com.example.petsocial.feature.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
        onRetryClick = viewModel::load
    )
}

@Composable
private fun FeedScreen(
    uiState: FeedUiState,
    onBodyChanged: (String) -> Unit,
    onImageUrlChanged: (String) -> Unit,
    onCreateClick: () -> Unit,
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
                FeedPostCard(post = post)
            }
        }
    }
}

@Composable
private fun FeedPostCard(post: PostResponse) {
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
        }
    }
}
