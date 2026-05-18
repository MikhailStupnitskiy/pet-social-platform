package com.example.petsocial.feature.matching

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.component.PetImage
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.SectionHeader
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetOnSurface
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetSecondary
import com.example.petsocial.core.designsystem.theme.PetSurface
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.matching.MatchResponse
import com.example.petsocial.core.network.model.matching.RecommendationResponse
import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.SuccessMessage

@Composable
fun MatchingRoute(
    onPetProfileClick: (String) -> Unit,
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
        onPetProfileClick = onPetProfileClick
    )
}

@Composable
private fun MatchingScreen(
    uiState: MatchingUiState,
    onLikeClick: (String) -> Unit,
    onPassClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onPetProfileClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        if (uiState.isLoading) {
            item { FullScreenLoading() }
            return@LazyColumn
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Найти пару",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.activePet?.let { "Ищем друзей для ${it.name}" } ?: "Выберите активного питомца",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PetTextSecondary
                    )
                }
                FilledIconButton(
                    onClick = { },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = PetSurface,
                        contentColor = PetOnSurface
                    )
                ) {
                    Icon(Icons.Rounded.Star, contentDescription = "Фильтры")
                }
            }
        }

        uiState.activePet?.let { pet ->
            item { ActivePetBlock(pet = pet) }
        }

        uiState.errorMessage?.let { message ->
            item {
                ProductCard(modifier = Modifier.fillMaxWidth()) {
                    ErrorMessage(message = message)
                    Button(onClick = onRetryClick) {
                        Text("Повторить")
                    }
                }
            }
        }

        uiState.successMessage?.let { message ->
            item { SuccessMessage(message = message) }
        }

        if (uiState.recommendations.isEmpty()) {
            item {
                ProductCard(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        tint = PetPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Новых кандидатов пока нет",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Попробуйте позже или расширьте параметры поиска.",
                        color = PetTextSecondary
                    )
                    OutlinedButton(onClick = onRetryClick) {
                        Text("Обновить")
                    }
                }
            }
        } else {
            item { SectionHeader(title = "Рекомендации") }
            items(uiState.recommendations) { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    isActionLoading = uiState.isActionLoading,
                    onOpenClick = { onPetProfileClick(recommendation.id) },
                    onLikeClick = { onLikeClick(recommendation.id) },
                    onPassClick = { onPassClick(recommendation.id) }
                )
            }
        }

        if (uiState.matches.isNotEmpty()) {
            item { SectionHeader(title = "Ваши мэтчи") }
            items(uiState.matches) { match ->
                MatchCard(
                    match = match,
                    activePetId = uiState.activePet?.id,
                    onPetProfileClick = onPetProfileClick
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ActivePetBlock(pet: PetResponse) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PetImage(
                imageUrl = pet.photo_url,
                contentDescription = pet.name,
                modifier = Modifier.size(56.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(text = pet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = pet.breed ?: pet.species, color = PetTextSecondary)
            }
            StatusChip(label = "Активен", tone = ChipTone.Secondary)
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: RecommendationResponse,
    isActionLoading: Boolean,
    onOpenClick: () -> Unit,
    onLikeClick: () -> Unit,
    onPassClick: () -> Unit
) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenClick),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(430.dp)
        ) {
            PetImage(
                imageUrl = recommendation.photo_url,
                contentDescription = recommendation.name,
                modifier = Modifier.matchParentSize(),
                cornerRadius = 16.dp
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.62f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.72f)
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recommendation.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = recommendation.breed ?: recommendation.species,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    StatusChip(
                        label = recommendation.distance_meters?.let { formatDistance(it) } ?: "Рядом",
                        tone = ChipTone.Neutral
                    )
                }
                Text(
                    text = recommendation.bio ?: "Любит прогулки, новые знакомства и спокойное общение.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    recommendation.personality_tags.take(3).ifEmpty {
                        listOf("Дружелюбный", "Активный")
                    }.forEach { tag ->
                        StatusChip(label = tag, tone = ChipTone.Neutral)
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = onPassClick,
                enabled = !isActionLoading,
                modifier = Modifier.size(60.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PetSurface,
                    contentColor = Color(0xFFE05252)
                )
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Пропустить")
            }
            Spacer(modifier = Modifier.size(18.dp))
            FilledIconButton(
                onClick = onOpenClick,
                modifier = Modifier.size(52.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PetSurface,
                    contentColor = PetSecondary
                )
            ) {
                Icon(Icons.Rounded.Star, contentDescription = "Подробнее")
            }
            Spacer(modifier = Modifier.size(18.dp))
            FilledIconButton(
                onClick = onLikeClick,
                enabled = !isActionLoading,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PetPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Rounded.Favorite, contentDescription = "Лайк")
            }
        }
    }
}

@Composable
private fun MatchCard(
    match: MatchResponse,
    activePetId: String?,
    onPetProfileClick: (String) -> Unit
) {
    val peerPetId = if (match.pet1_id == activePetId) match.pet2_id else match.pet1_id
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPetProfileClick(peerPetId) }
    ) {
        Text(text = "Это мэтч!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = "Чат создан: ${match.created_at.take(10)}", color = PetTextSecondary)
    }
}

private fun formatDistance(distanceMeters: Int): String {
    return if (distanceMeters >= 1000) {
        "${distanceMeters / 1000.0} км"
    } else {
        "$distanceMeters м"
    }
}
