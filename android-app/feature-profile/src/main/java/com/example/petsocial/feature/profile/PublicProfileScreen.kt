package com.example.petsocial.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.SectionHeader
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetOnSurface
import com.example.petsocial.core.designsystem.theme.PetOutline
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetPrimaryLight
import com.example.petsocial.core.designsystem.theme.PetSurfaceMuted
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.pets.PublicPetProfileResponse
import com.example.petsocial.core.network.model.profile.PublicHandlerProfileResponse
import com.example.petsocial.core.network.model.profile.PublicPetSummaryResponse
import com.example.petsocial.core.network.model.profile.PublicUserProfileResponse
import com.example.petsocial.core.ui.AuthenticatedImage
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.FullScreenLoading

@Composable
fun PublicUserProfileRoute(
    userId: String,
    onBack: () -> Unit,
    onPetClick: (String) -> Unit,
    viewModel: PublicUserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    PublicUserProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = { viewModel.load(userId) },
        onPetClick = onPetClick
    )
}

@Composable
fun PublicPetProfileRoute(
    petId: String,
    onBack: () -> Unit,
    onOwnerClick: (String) -> Unit,
    viewModel: PublicPetProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(petId) {
        viewModel.load(petId)
    }

    PublicPetProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = { viewModel.load(petId) },
        onOwnerClick = onOwnerClick
    )
}

@Composable
private fun PublicUserProfileScreen(
    uiState: PublicUserProfileUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onPetClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DetailTopRow(title = "Profile", onBack = onBack) }

        if (uiState.isLoading) {
            item { FullScreenLoading() }
            return@LazyColumn
        }

        uiState.errorMessage?.let { message ->
            item { DetailError(message = message, onRetry = onRetry) }
            return@LazyColumn
        }

        val profile = uiState.profile ?: return@LazyColumn
        item {
            PublicUserHero(
                profile = profile,
                authToken = uiState.authToken
            )
        }

        profile.handler?.let { handler ->
            item { PublicHandlerCard(handler = handler) }
        }

        item {
            SectionHeader(title = "Pets")
        }

        if (profile.pets.isEmpty()) {
            item {
                ProductCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No public pets yet", color = PetTextSecondary)
                }
            }
        } else {
            items(profile.pets, key = { it.id }) { pet ->
                PublicPetSummaryCard(
                    pet = pet,
                    authToken = uiState.authToken,
                    onClick = { onPetClick(pet.id) }
                )
            }
        }
    }
}

@Composable
private fun PublicPetProfileScreen(
    uiState: PublicPetProfileUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOwnerClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { DetailTopRow(title = "Pet", onBack = onBack) }

        if (uiState.isLoading) {
            item { FullScreenLoading() }
            return@LazyColumn
        }

        uiState.errorMessage?.let { message ->
            item { DetailError(message = message, onRetry = onRetry) }
            return@LazyColumn
        }

        val pet = uiState.pet ?: return@LazyColumn
        item {
            PublicPetHero(
                pet = pet,
                authToken = uiState.authToken
            )
        }
        item {
            PublicPetInfoCard(pet = pet)
        }
        item {
            OwnerSummaryCard(
                pet = pet,
                authToken = uiState.authToken,
                onClick = { onOwnerClick(pet.owner.user_id) }
            )
        }
    }
}

@Composable
private fun DetailTopRow(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DetailError(
    message: String,
    onRetry: () -> Unit
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        ErrorMessage(message = message)
        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
private fun PublicUserHero(
    profile: PublicUserProfileResponse,
    authToken: String?
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            RoundImage(
                imageUrl = profile.avatar_url,
                authToken = authToken,
                contentDescription = profile.name,
                size = 104
            ) {
                Icon(Icons.Rounded.Person, contentDescription = null, tint = PetPrimary, modifier = Modifier.size(46.dp))
            }
        }
        Text(
            text = profile.name.ifBlank { "PetSocial user" },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = listOfNotNull(profile.city, profile.birth_date?.take(4)).joinToString(" · "),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = PetTextSecondary
        )
        profile.bio?.takeIf { it.isNotBlank() }?.let { bio ->
            Text(bio, color = PetOnSurface)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat(profile.stats.pets_count.toString(), "Pets")
            ProfileStat(profile.stats.matches_count.toString(), "Matches")
            ProfileStat(profile.stats.posts_count.toString(), "Posts")
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = PetTextSecondary)
    }
}

@Composable
private fun PublicHandlerCard(handler: PublicHandlerProfileResponse) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Star, contentDescription = null, tint = PetPrimary)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(handler.display_name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "${handler.rating_avg} (${handler.reviews_count}) · ${handler.experience_years} years",
                    color = PetTextSecondary
                )
            }
        }
        handler.bio?.takeIf { it.isNotBlank() }?.let { Text(it) }
        handler.conditions?.takeIf { it.isNotBlank() }?.let { Text(it, color = PetTextSecondary) }
        handler.services.take(4).forEach { service ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(service.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(formatPrice(service.price_cents), color = PetPrimary)
            }
        }
    }
}

@Composable
private fun PublicPetSummaryCard(
    pet: PublicPetSummaryResponse,
    authToken: String?,
    onClick: () -> Unit
) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SquareImage(
                imageUrl = pet.photo_url,
                authToken = authToken,
                contentDescription = pet.name,
                size = 70
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(petSubtitle(pet.species, pet.breed, pet.sex, pet.birth_date), color = PetTextSecondary)
                ChipRow(pet.personality_tags + pet.interests + listOfNotNull(pet.matching_goal))
            }
        }
    }
}

@Composable
private fun PublicPetHero(
    pet: PublicPetProfileResponse,
    authToken: String?
) {
    ProductCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(PetSurfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            if (!pet.photo_url.isNullOrBlank()) {
                AuthenticatedImage(
                    imageUrl = pet.photo_url,
                    authToken = authToken,
                    contentDescription = pet.name,
                    modifier = Modifier.matchParentSize()
                )
            } else {
                Icon(Icons.Rounded.Pets, contentDescription = null, tint = PetPrimary, modifier = Modifier.size(64.dp))
            }
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(pet.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(petSubtitle(pet.species, pet.breed, pet.sex, pet.birth_date), color = PetTextSecondary)
            ChipRow(pet.personality_tags + pet.interests + listOfNotNull(pet.matching_goal))
        }
    }
}

@Composable
private fun PublicPetInfoCard(pet: PublicPetProfileResponse) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(title = "About")
        pet.bio?.takeIf { it.isNotBlank() }?.let { Text(it) }
        InfoLine(label = "Species", value = pet.species)
        InfoLine(label = "Breed", value = pet.breed)
        InfoLine(label = "Sex", value = pet.sex)
        InfoLine(label = "Birth date", value = pet.birth_date)
        InfoLine(label = "Weight", value = pet.weight_kg?.let { "$it kg" })
        InfoLine(label = "Looking for", value = pet.matching_goal)
    }
}

@Composable
private fun OwnerSummaryCard(
    pet: PublicPetProfileResponse,
    authToken: String?,
    onClick: () -> Unit
) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        SectionHeader(title = "Owner")
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundImage(
                imageUrl = pet.owner.avatar_url,
                authToken = authToken,
                contentDescription = pet.owner.name,
                size = 58
            ) {
                Icon(Icons.Rounded.Person, contentDescription = null, tint = PetPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.owner.name.ifBlank { "Pet owner" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(pet.owner.city ?: "City not specified", color = PetTextSecondary)
            }
        }
        pet.owner.bio?.takeIf { it.isNotBlank() }?.let { Text(it) }
    }
}

@Composable
private fun InfoLine(label: String, value: String?) {
    if (value.isNullOrBlank()) {
        return
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = PetTextSecondary)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ChipRow(labels: List<String>) {
    val filtered = labels.map { it.trim() }.filter { it.isNotBlank() }.take(8)
    if (filtered.isEmpty()) {
        return
    }
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filtered.forEach { label ->
            StatusChip(label = label, tone = ChipTone.Neutral)
        }
    }
}

@Composable
private fun RoundImage(
    imageUrl: String?,
    authToken: String?,
    contentDescription: String?,
    size: Int,
    fallback: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(PetPrimaryLight)
            .border(1.dp, PetOutline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AuthenticatedImage(
                imageUrl = imageUrl,
                authToken = authToken,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize()
            )
        } else {
            fallback()
        }
    }
}

@Composable
private fun SquareImage(
    imageUrl: String?,
    authToken: String?,
    contentDescription: String?,
    size: Int
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PetSurfaceMuted),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AuthenticatedImage(
                imageUrl = imageUrl,
                authToken = authToken,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Icon(Icons.Rounded.Favorite, contentDescription = null, tint = PetPrimary, modifier = Modifier.size(30.dp))
        }
    }
}

private fun petSubtitle(
    species: String,
    breed: String?,
    sex: String?,
    birthDate: String?
): String {
    return listOfNotNull(
        species.takeIf { it.isNotBlank() },
        breed,
        sex,
        birthDate?.take(4)
    ).joinToString(" · ")
}

private fun formatPrice(priceCents: Int): String {
    return "${priceCents / 100} RUB"
}
