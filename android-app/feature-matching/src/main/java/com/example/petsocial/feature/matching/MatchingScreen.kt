package com.example.petsocial.feature.matching

import android.annotation.SuppressLint
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.SectionHeader
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetOnSurface
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetSecondary
import com.example.petsocial.core.designsystem.theme.PetSurface
import com.example.petsocial.core.designsystem.theme.PetSurfaceMuted
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.matching.MatchResponse
import com.example.petsocial.core.network.model.matching.RecommendationResponse
import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.ui.AuthenticatedImage
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.SuccessMessage
import com.google.android.gms.location.LocationServices

@SuppressLint("MissingPermission")
@Composable
fun MatchingRoute(
    onPetProfileClick: (String) -> Unit,
    viewModel: MatchingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationClient = remember(context) { LocationServices.getFusedLocationProviderClient(context) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            if (
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                locationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.updateActivePetLocation(location.latitude, location.longitude)
                    } else {
                        viewModel.markLocationDenied()
                    }
                }
            }
        } else {
            viewModel.markLocationDenied()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(uiState.activePet?.id) {
        if (uiState.activePet == null) return@LaunchedEffect
        val hasLocationPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasLocationPermission) {
            locationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateActivePetLocation(location.latitude, location.longitude)
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    MatchingScreen(
        uiState = uiState,
        onGoalSelected = viewModel::selectGoal,
        onShowFilters = viewModel::showFilters,
        onHideFilters = viewModel::hideFilters,
        onApplyFilters = viewModel::updateFilters,
        onClearFilters = viewModel::clearFilters,
        onLikeClick = viewModel::like,
        onPassClick = viewModel::pass,
        onRetryClick = viewModel::load,
        onPetProfileClick = { id -> viewModel.openProfile(id, onPetProfileClick) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatchingScreen(
    uiState: MatchingUiState,
    onGoalSelected: (MatchingGoal) -> Unit,
    onShowFilters: () -> Unit,
    onHideFilters: () -> Unit,
    onApplyFilters: (MatchingFilters) -> Unit,
    onClearFilters: () -> Unit,
    onLikeClick: (String) -> Unit,
    onPassClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    onPetProfileClick: (String) -> Unit
) {
    if (uiState.isFilterSheetVisible) {
        FilterSheet(
            filters = uiState.filters,
            onDismiss = onHideFilters,
            onApply = onApplyFilters,
            onClear = onClearFilters
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        if (uiState.isLoading) {
            item { FullScreenLoading() }
            return@LazyColumn
        }

        item {
            HeaderBlock(
                activePet = uiState.activePet,
                authToken = uiState.authToken,
                goal = uiState.goal,
                activeFilters = uiState.filters.activeCount,
                locationMessage = uiState.locationMessage,
                isRefreshingLocation = uiState.isRefreshingLocation,
                onGoalSelected = onGoalSelected,
                onShowFilters = onShowFilters
            )
        }

        uiState.errorMessage?.let { message ->
            item {
                ProductCard(modifier = Modifier.fillMaxWidth()) {
                    ErrorMessage(message = message)
                    Button(onClick = onRetryClick) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Повторить")
                    }
                }
            }
        }

        uiState.successMessage?.let { message ->
            item { SuccessMessage(message = message) }
        }

        if (uiState.activePet == null) {
            item {
                EmptyState(
                    title = "Выберите активного питомца",
                    body = "Мэтчинг начнет подбирать анкеты после выбора активного питомца в профиле."
                )
            }
            return@LazyColumn
        }

        if (uiState.recommendations.isEmpty()) {
            item {
                EmptyState(
                    title = "Новых анкет пока нет",
                    body = "Попробуйте сменить цель поиска или ослабить фильтры."
                )
            }
        } else {
            item { SectionHeader(title = "Рекомендации", action = "${uiState.recommendations.size}") }
            items(uiState.recommendations, key = { it.id }) { recommendation ->
                RecommendationCard(
                    recommendation = recommendation,
                    authToken = uiState.authToken,
                    isActionLoading = uiState.isActionLoading,
                    onOpenClick = { onPetProfileClick(recommendation.id) },
                    onLikeClick = { onLikeClick(recommendation.id) },
                    onPassClick = { onPassClick(recommendation.id) }
                )
            }
        }

        if (uiState.matches.isNotEmpty()) {
            item { SectionHeader(title = "Ваши мэтчи") }
            items(uiState.matches, key = { it.id }) { match ->
                MatchCard(
                    match = match,
                    activePetId = uiState.activePet.id,
                    onPetProfileClick = onPetProfileClick
                )
            }
        }
    }
}

@Composable
private fun HeaderBlock(
    activePet: PetResponse?,
    authToken: String?,
    goal: MatchingGoal,
    activeFilters: Int,
    locationMessage: String?,
    isRefreshingLocation: Boolean,
    onGoalSelected: (MatchingGoal) -> Unit,
    onShowFilters: () -> Unit
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PetPhoto(
                imageUrl = activePet?.photo_url,
                authToken = authToken,
                contentDescription = activePet?.name,
                modifier = Modifier.size(58.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = activePet?.name ?: "Мэтчинг",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PetOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = activePet?.let { listOfNotNull(it.breed, it.species).joinToString(" · ") } ?: "Подбор питомцев",
                    color = PetTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            FilledIconButton(
                onClick = onShowFilters,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PetSurfaceMuted,
                    contentColor = PetPrimary
                )
            ) {
                Icon(Icons.Rounded.FilterList, contentDescription = "Фильтры")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MatchingGoal.values().forEach { item ->
                FilterChip(
                    selected = goal == item,
                    onClick = { onGoalSelected(item) },
                    label = { Text(item.label) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (item == MatchingGoal.Walk) Icons.Rounded.Search else Icons.Rounded.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = PetSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isRefreshingLocation) "Обновляем геопозицию..." else locationMessage ?: "Геопозиция нужна для расстояния",
                style = MaterialTheme.typography.bodySmall,
                color = PetTextSecondary,
                modifier = Modifier.weight(1f)
            )
            if (activeFilters > 0) {
                StatusChip(label = "Фильтры: $activeFilters", tone = ChipTone.Info)
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    recommendation: RecommendationResponse,
    authToken: String?,
    isActionLoading: Boolean,
    onOpenClick: () -> Unit,
    onLikeClick: () -> Unit,
    onPassClick: () -> Unit
) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenClick),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(430.dp)
        ) {
            PetPhoto(
                imageUrl = recommendation.photo_url,
                authToken = authToken,
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
                            0.58f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.76f)
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
                            text = listOfNotNull(recommendation.breed, recommendation.sex).joinToString(" · ")
                                .ifBlank { recommendation.species },
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        StatusChip(label = "${recommendation.compatibility_score}%", tone = ChipTone.Primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        StatusChip(
                            label = recommendation.distance_meters?.let(::formatDistance) ?: "Гео нет",
                            tone = ChipTone.Neutral
                        )
                    }
                }
                Text(
                    text = recommendation.bio ?: "Анкета без описания, но с подходящими параметрами для выбранной цели.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                FlowChips(
                    values = (recommendation.score_reasons + recommendation.personality_tags + recommendation.interests).distinct().take(4),
                    onDark = true
                )
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
                modifier = Modifier.size(58.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PetSurfaceMuted,
                    contentColor = Color(0xFFE05252)
                )
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Пропустить")
            }
            Spacer(modifier = Modifier.width(18.dp))
            FilledIconButton(
                onClick = onOpenClick,
                modifier = Modifier.size(52.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = PetSurfaceMuted,
                    contentColor = PetSecondary
                )
            ) {
                Icon(Icons.Rounded.Star, contentDescription = "Подробнее")
            }
            Spacer(modifier = Modifier.width(18.dp))
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
        Text(text = "Это мэтч", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = "Чат создан: ${match.created_at.take(10)}", color = PetTextSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    filters: MatchingFilters,
    onDismiss: () -> Unit,
    onApply: (MatchingFilters) -> Unit,
    onClear: () -> Unit
) {
    var draft by remember(filters) { mutableStateOf(filters) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Фильтры поиска", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Радиус: ${draft.maxDistanceMeters?.let(::formatDistance) ?: "без ограничения"}", color = PetTextSecondary)
            Slider(
                value = (draft.maxDistanceMeters ?: 30000).toFloat(),
                onValueChange = { draft = draft.copy(maxDistanceMeters = it.toInt()) },
                valueRange = 1000f..30000f,
                steps = 28
            )
            OutlinedTextField(
                value = draft.breed,
                onValueChange = { draft = draft.copy(breed = it) },
                label = { Text("Порода") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = draft.sex,
                onValueChange = { draft = draft.copy(sex = it) },
                label = { Text("Пол") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = draft.ageMinMonths,
                    onValueChange = { draft = draft.copy(ageMinMonths = it.filter(Char::isDigit)) },
                    label = { Text("Возраст от, мес.") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = draft.ageMaxMonths,
                    onValueChange = { draft = draft.copy(ageMaxMonths = it.filter(Char::isDigit)) },
                    label = { Text("до") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = draft.tags,
                onValueChange = { draft = draft.copy(tags = it) },
                label = { Text("Характер через запятую") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = draft.interests,
                onValueChange = { draft = draft.copy(interests = it) },
                label = { Text("Интересы через запятую") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = draft.hasPhotoOnly,
                    onCheckedChange = { draft = draft.copy(hasPhotoOnly = it) }
                )
                Text("Только анкеты с фото")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(onClick = onClear, modifier = Modifier.weight(1f)) {
                    Text("Сбросить")
                }
                Button(onClick = { onApply(draft) }, modifier = Modifier.weight(1f)) {
                    Text("Применить")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PetPhoto(
    imageUrl: String?,
    authToken: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
            .background(PetSurfaceMuted),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AuthenticatedImage(
                imageUrl = imageUrl,
                authToken = authToken,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.Pets,
                contentDescription = null,
                tint = PetPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Composable
private fun FlowChips(values: List<String>, onDark: Boolean = false) {
    if (values.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.take(3).forEach { value ->
            StatusChip(
                label = value,
                tone = if (onDark) ChipTone.Neutral else ChipTone.Info
            )
        }
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = Icons.Rounded.Favorite,
            contentDescription = null,
            tint = PetPrimary,
            modifier = Modifier.size(34.dp)
        )
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(text = body, color = PetTextSecondary)
    }
}

private fun formatDistance(distanceMeters: Int): String {
    return if (distanceMeters >= 1000) {
        val km = distanceMeters / 1000.0
        if (km >= 10) "${km.toInt()} км" else String.format("%.1f км", km)
    } else {
        "$distanceMeters м"
    }
}
