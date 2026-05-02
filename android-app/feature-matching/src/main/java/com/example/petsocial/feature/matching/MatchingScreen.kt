package com.example.petsocial.feature.matching

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.network.model.matching.MatchResponse
import com.example.petsocial.core.network.model.matching.RecommendationResponse
import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.ScreenTitle
import com.example.petsocial.core.ui.SectionTitle
import com.example.petsocial.core.ui.SuccessMessage

@Composable
fun MatchingRoute(
    onBackClick: () -> Unit,
    viewModel: MatchingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    MatchingScreen(
        uiState = uiState,
        onLikeClick = viewModel::like,
        onPassClick = viewModel::pass,
        onRetryClick = viewModel::load,
        onBackClick = onBackClick
    )
}

@Composable
private fun MatchingScreen(
    uiState: MatchingUiState,
    onLikeClick: (String) -> Unit,
    onPassClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenTitle("Matching")

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBackClick) {
                Text("Назад к профилю")
            }
        }

        if (uiState.isLoading) {
            item {
                FullScreenLoading()
            }
            return@LazyColumn
        }

        item {
            if (uiState.activePet != null) {
                ActivePetBlock(uiState.activePet)
            }
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

        if (uiState.successMessage != null) {
            item {
                SuccessMessage(message = uiState.successMessage)
            }
        }

        item {
            SectionTitle("Рекомендации")
        }

        if (uiState.recommendations.isEmpty()) {
            item {
                Text("Пока нет новых рекомендаций")
            }
        } else {
            items(uiState.recommendations) { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    isActionLoading = uiState.isActionLoading,
                    onLikeClick = {
                        onLikeClick(recommendation.id)
                    },
                    onPassClick = {
                        onPassClick(recommendation.id)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Matches")
        }

        if (uiState.matches.isEmpty()) {
            item {
                Text("Пока нет совпадений")
            }
        } else {
            items(uiState.matches) { match ->
                MatchCard(match)
            }
        }
    }
}

@Composable
private fun ActivePetBlock(
    pet: PetResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Активный питомец: ${pet.name}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Вид: ${pet.species}")
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: RecommendationResponse,
    isActionLoading: Boolean,
    onLikeClick: () -> Unit,
    onPassClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = recommendation.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Вид: ${recommendation.species}")

            if (!recommendation.breed.isNullOrBlank()) {
                Text("Порода: ${recommendation.breed}")
            }

            if (!recommendation.sex.isNullOrBlank()) {
                Text("Пол: ${recommendation.sex}")
            }

            if (!recommendation.birth_date.isNullOrBlank()) {
                Text("Дата рождения: ${recommendation.birth_date}")
            }

            if (!recommendation.weight_kg.isNullOrBlank()) {
                Text("Вес: ${recommendation.weight_kg} кг")
            }

            if (!recommendation.bio.isNullOrBlank()) {
                Text("Описание: ${recommendation.bio}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPassClick,
                    enabled = !isActionLoading
                ) {
                    Text("Пропустить")
                }

                Button(
                    onClick = onLikeClick,
                    enabled = !isActionLoading
                ) {
                    Text("Лайк")
                }
            }
        }
    }
}

@Composable
private fun MatchCard(
    match: MatchResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Match",
                style = MaterialTheme.typography.titleMedium
            )
            Text("ID: ${match.id}")
            Text("Pet 1: ${match.pet1_id}")
            Text("Pet 2: ${match.pet2_id}")
            Text("Создан: ${match.created_at}")
        }
    }
}