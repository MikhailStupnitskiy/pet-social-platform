package com.example.petsocial.feature.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.SectionHeader
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetOnSurface
import com.example.petsocial.core.designsystem.theme.PetOutline
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetPrimaryLight
import com.example.petsocial.core.designsystem.theme.PetSurface
import com.example.petsocial.core.designsystem.theme.PetSurfaceMuted
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.ui.AuthenticatedImage
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.SuccessMessage

@Composable
fun ProfileRoute(
    onLogoutClick: () -> Unit,
    onUnauthorized: () -> Unit,
    onCreateHandlerProfileClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isUnauthorized) {
        if (uiState.isUnauthorized) {
            onUnauthorized()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    ProfileScreen(
        uiState = uiState,
        onRetryClick = viewModel::loadProfile,
        onLogoutClick = onLogoutClick,
        onCreateHandlerProfileClick = onCreateHandlerProfileClick,
        onProfileClick = viewModel::showProfileEditor,
        onDismissProfileEditor = viewModel::hideProfileEditor,
        onProfileNameChanged = viewModel::onProfileNameChanged,
        onProfileCityChanged = viewModel::onProfileCityChanged,
        onProfileBioChanged = viewModel::onProfileBioChanged,
        onAvatarSelected = viewModel::onAvatarSelected,
        onClearAvatar = viewModel::clearSelectedAvatar,
        onSaveProfile = viewModel::saveProfile,
        onShowPets = viewModel::showPetsSheet,
        onDismissPets = viewModel::hidePetsSheet,
        onAddPetClick = viewModel::showCreatePetForm,
        onEditPetClick = viewModel::showEditPetForm,
        onCancelPetForm = viewModel::hidePetForm,
        onPetNameChanged = viewModel::onPetNameChanged,
        onPetSpeciesChanged = viewModel::onPetSpeciesChanged,
        onPetBreedChanged = viewModel::onPetBreedChanged,
        onPetSexChanged = viewModel::onPetSexChanged,
        onPetBirthDateChanged = viewModel::onPetBirthDateChanged,
        onPetBioChanged = viewModel::onPetBioChanged,
        onPetPhotoSelected = viewModel::onPetPhotoSelected,
        onClearPetPhoto = viewModel::clearSelectedPetPhoto,
        onPersonalityInputChanged = viewModel::onPersonalityInputChanged,
        onAddPersonalityTag = viewModel::addPersonalityTag,
        onRemovePersonalityTag = viewModel::removePersonalityTag,
        onInterestInputChanged = viewModel::onInterestInputChanged,
        onAddInterest = viewModel::addInterest,
        onRemoveInterest = viewModel::removeInterest,
        onPetHealthNotesChanged = viewModel::onPetHealthNotesChanged,
        onPetMatchingGoalChanged = viewModel::onPetMatchingGoalChanged,
        onSavePet = viewModel::savePet,
        onSetActivePet = viewModel::setActivePet
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    onRetryClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onCreateHandlerProfileClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDismissProfileEditor: () -> Unit,
    onProfileNameChanged: (String) -> Unit,
    onProfileCityChanged: (String) -> Unit,
    onProfileBioChanged: (String) -> Unit,
    onAvatarSelected: (Uri?) -> Unit,
    onClearAvatar: () -> Unit,
    onSaveProfile: () -> Unit,
    onShowPets: () -> Unit,
    onDismissPets: () -> Unit,
    onAddPetClick: () -> Unit,
    onEditPetClick: (PetResponse) -> Unit,
    onCancelPetForm: () -> Unit,
    onPetNameChanged: (String) -> Unit,
    onPetSpeciesChanged: (String) -> Unit,
    onPetBreedChanged: (String) -> Unit,
    onPetSexChanged: (String) -> Unit,
    onPetBirthDateChanged: (String) -> Unit,
    onPetBioChanged: (String) -> Unit,
    onPetPhotoSelected: (Uri?) -> Unit,
    onClearPetPhoto: () -> Unit,
    onPersonalityInputChanged: (String) -> Unit,
    onAddPersonalityTag: () -> Unit,
    onRemovePersonalityTag: (String) -> Unit,
    onInterestInputChanged: (String) -> Unit,
    onAddInterest: () -> Unit,
    onRemoveInterest: (String) -> Unit,
    onPetHealthNotesChanged: (String) -> Unit,
    onPetMatchingGoalChanged: (String) -> Unit,
    onSavePet: () -> Unit,
    onSetActivePet: (String) -> Unit
) {
    if (uiState.isLoading) {
        FullScreenLoading()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)
    ) {
        item {
            ProfileHero(
                uiState = uiState,
                onProfileClick = onProfileClick
            )
        }

        uiState.errorMessage?.let { message ->
            item {
                ErrorMessage(message = message)
                TextButton(onClick = onRetryClick) {
                    Text("Повторить")
                }
            }
        }

        uiState.successMessage?.let { message ->
            item { SuccessMessage(message = message) }
        }

        item {
            PetsPreviewSection(
                pet = uiState.activePet,
                authToken = uiState.authToken,
                onShowPets = onShowPets,
                onPetClick = onEditPetClick,
                onAddPetClick = onAddPetClick
            )
        }

        item {
            ProductCard(modifier = Modifier.fillMaxWidth()) {
                Text("Настройки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                SettingsAction(
                    icon = Icons.Rounded.Settings,
                    label = "Создать профиль хэндлера",
                    onClick = onCreateHandlerProfileClick
                )
                SettingsAction(
                    icon = Icons.Rounded.ExitToApp,
                    label = "Выйти",
                    isDestructive = true,
                    onClick = onLogoutClick
                )
            }
        }
    }

    if (uiState.isProfileEditorVisible) {
        ModalBottomSheet(onDismissRequest = onDismissProfileEditor) {
            ProfileEditSheet(
                uiState = uiState,
                onNameChanged = onProfileNameChanged,
                onCityChanged = onProfileCityChanged,
                onBioChanged = onProfileBioChanged,
                onAvatarSelected = onAvatarSelected,
                onClearAvatar = onClearAvatar,
                onCancel = onDismissProfileEditor,
                onSave = onSaveProfile
            )
        }
    }

    if (uiState.isPetsSheetVisible) {
        ModalBottomSheet(onDismissRequest = onDismissPets) {
            PetsSheet(
                uiState = uiState,
                onAddPetClick = onAddPetClick,
                onEditPetClick = onEditPetClick,
                onCancelPetForm = onCancelPetForm,
                onPetNameChanged = onPetNameChanged,
                onPetSpeciesChanged = onPetSpeciesChanged,
                onPetBreedChanged = onPetBreedChanged,
                onPetSexChanged = onPetSexChanged,
                onPetBirthDateChanged = onPetBirthDateChanged,
                onPetBioChanged = onPetBioChanged,
                onPetPhotoSelected = onPetPhotoSelected,
                onClearPetPhoto = onClearPetPhoto,
                onPersonalityInputChanged = onPersonalityInputChanged,
                onAddPersonalityTag = onAddPersonalityTag,
                onRemovePersonalityTag = onRemovePersonalityTag,
                onInterestInputChanged = onInterestInputChanged,
                onAddInterest = onAddInterest,
                onRemoveInterest = onRemoveInterest,
                onPetHealthNotesChanged = onPetHealthNotesChanged,
                onPetMatchingGoalChanged = onPetMatchingGoalChanged,
                onSavePet = onSavePet,
                onSetActivePet = onSetActivePet
            )
        }
    }
}

@Composable
private fun ProfileHero(
    uiState: ProfileUiState,
    onProfileClick: () -> Unit
) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onProfileClick),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Icon(Icons.Rounded.Settings, contentDescription = null, tint = PetOnSurface)
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ProfileAvatar(
                imageUrl = uiState.avatarUrl,
                authToken = uiState.authToken,
                contentDescription = uiState.name,
                size = 96.dp
            )
            Box(
                modifier = Modifier
                    .padding(top = 66.dp, start = 68.dp)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(PetPrimary)
                    .border(2.dp, PetSurface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Edit, contentDescription = null, tint = PetSurface, modifier = Modifier.size(15.dp))
            }
        }
        Text(
            text = uiState.name.ifBlank { "Профиль владельца" },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PetOnSurface
        )
        Text(
            text = uiState.city.ifBlank { uiState.email },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.bodyMedium,
            color = PetTextSecondary
        )
        if (uiState.bio.isNotBlank()) {
            Text(
                text = uiState.bio,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = PetOnSurface
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileStat(uiState.petsCount.toString(), "Питомцев")
            ProfileStat(uiState.matchesCount.toString(), "Мэтчей")
            ProfileStat(uiState.postsCount.toString(), "Постов")
        }
    }
}

@Composable
private fun ProfileAvatar(
    imageUrl: String?,
    authToken: String?,
    contentDescription: String?,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(PetSurfaceMuted)
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
            Icon(Icons.Rounded.Person, contentDescription = null, tint = PetPrimary, modifier = Modifier.size(size * 0.44f))
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
private fun PetsPreviewSection(
    pet: PetResponse?,
    authToken: String?,
    onShowPets: () -> Unit,
    onPetClick: (PetResponse) -> Unit,
    onAddPetClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = "Мои питомцы", action = "Все", modifier = Modifier.clickable(onClick = onShowPets))
        if (pet == null) {
            ProductCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAddPetClick)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(PetPrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = PetPrimary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Добавить питомца", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Профиль, фото и теги", style = MaterialTheme.typography.bodySmall, color = PetTextSecondary)
                    }
                }
            }
        } else {
            CompactPetCard(
                pet = pet,
                authToken = authToken,
                onClick = { onPetClick(pet) }
            )
        }
    }
}

@Composable
private fun CompactPetCard(
    pet: PetResponse,
    authToken: String?,
    onClick: () -> Unit
) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PetPhoto(
                imageUrl = pet.photo_url,
                authToken = authToken,
                contentDescription = pet.name,
                modifier = Modifier.size(64.dp),
                cornerRadius = 16.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    petSubtitle(pet),
                    style = MaterialTheme.typography.bodySmall,
                    color = PetTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(
                    label = pet.matching_goal ?: "В поиске пары",
                    tone = ChipTone.Secondary
                )
            }
            Icon(Icons.Rounded.Edit, contentDescription = null, tint = PetPrimary)
        }
    }
}

@Composable
private fun SettingsAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isDestructive) MaterialTheme.colorScheme.error else PetOnSurface)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, color = if (isDestructive) MaterialTheme.colorScheme.error else PetOnSurface)
        }
    }
}

@Composable
private fun ProfileEditSheet(
    uiState: ProfileUiState,
    onNameChanged: (String) -> Unit,
    onCityChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onAvatarSelected: (Uri?) -> Unit,
    onClearAvatar: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = onAvatarSelected
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Редактировать профиль", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(uiState.profileName, onNameChanged, Modifier.fillMaxWidth(), label = { Text("Имя") }, singleLine = true)
        OutlinedTextField(uiState.profileCity, onCityChanged, Modifier.fillMaxWidth(), label = { Text("Город") }, singleLine = true)
        OutlinedTextField(uiState.profileBio, onBioChanged, Modifier.fillMaxWidth(), label = { Text("О себе") }, minLines = 3)
        OutlinedButton(
            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSavingProfile
        ) {
            Text(if (uiState.selectedAvatarUri == null) "Выбрать фото" else "Заменить фото")
        }
        uiState.selectedAvatarUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            TextButton(onClick = onClearAvatar, enabled = !uiState.isSavingProfile) {
                Text("Убрать выбранное фото")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !uiState.isSavingProfile) {
                Text("Отмена")
            }
            Button(onClick = onSave, modifier = Modifier.weight(1f), enabled = !uiState.isSavingProfile) {
                if (uiState.isSavingProfile) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Сохранить")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun PetsSheet(
    uiState: ProfileUiState,
    onAddPetClick: () -> Unit,
    onEditPetClick: (PetResponse) -> Unit,
    onCancelPetForm: () -> Unit,
    onPetNameChanged: (String) -> Unit,
    onPetSpeciesChanged: (String) -> Unit,
    onPetBreedChanged: (String) -> Unit,
    onPetSexChanged: (String) -> Unit,
    onPetBirthDateChanged: (String) -> Unit,
    onPetBioChanged: (String) -> Unit,
    onPetPhotoSelected: (Uri?) -> Unit,
    onClearPetPhoto: () -> Unit,
    onPersonalityInputChanged: (String) -> Unit,
    onAddPersonalityTag: () -> Unit,
    onRemovePersonalityTag: (String) -> Unit,
    onInterestInputChanged: (String) -> Unit,
    onAddInterest: () -> Unit,
    onRemoveInterest: (String) -> Unit,
    onPetHealthNotesChanged: (String) -> Unit,
    onPetMatchingGoalChanged: (String) -> Unit,
    onSavePet: () -> Unit,
    onSetActivePet: (String) -> Unit
) {
    if (uiState.petFormMode != null) {
        PetForm(
            uiState = uiState,
            onCancel = onCancelPetForm,
            onPetNameChanged = onPetNameChanged,
            onPetSpeciesChanged = onPetSpeciesChanged,
            onPetBreedChanged = onPetBreedChanged,
            onPetSexChanged = onPetSexChanged,
            onPetBirthDateChanged = onPetBirthDateChanged,
            onPetBioChanged = onPetBioChanged,
            onPetPhotoSelected = onPetPhotoSelected,
            onClearPetPhoto = onClearPetPhoto,
            onPersonalityInputChanged = onPersonalityInputChanged,
            onAddPersonalityTag = onAddPersonalityTag,
            onRemovePersonalityTag = onRemovePersonalityTag,
            onInterestInputChanged = onInterestInputChanged,
            onAddInterest = onAddInterest,
            onRemoveInterest = onRemoveInterest,
            onPetHealthNotesChanged = onPetHealthNotesChanged,
            onPetMatchingGoalChanged = onPetMatchingGoalChanged,
            onSavePet = onSavePet
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Мои питомцы", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Button(onClick = onAddPetClick) {
                        Icon(Icons.Rounded.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Добавить")
                    }
                }
            }
            if (uiState.pets.isEmpty()) {
                item {
                    ProductCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Питомцев пока нет", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Добавьте первого питомца, чтобы искать друзей и публиковать посты.", color = PetTextSecondary)
                    }
                }
            } else {
                items(uiState.pets) { pet ->
                    FullPetCard(
                        pet = pet,
                        authToken = uiState.authToken,
                        onClick = { onEditPetClick(pet) },
                        onSetActiveClick = { onSetActivePet(pet.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FullPetCard(
    pet: PetResponse,
    authToken: String?,
    onClick: () -> Unit,
    onSetActiveClick: () -> Unit
) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box {
            PetPhoto(
                imageUrl = pet.photo_url,
                authToken = authToken,
                contentDescription = pet.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp),
                cornerRadius = 16.dp
            )
            StatusChip(
                label = if (pet.is_active) "Активен" else "Не активен",
                tone = if (pet.is_active) ChipTone.Secondary else ChipTone.Neutral,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            )
        }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(pet.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(petSubtitle(pet), color = PetTextSecondary)
                }
                Icon(Icons.Rounded.Edit, contentDescription = null, tint = PetPrimary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(label = pet.matching_goal ?: "В поиске друзей", tone = ChipTone.Primary)
                StatusChip(label = pet.health_notes ?: "Здоровье", tone = ChipTone.Info)
            }
            if (pet.personality_tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pet.personality_tags.forEach { tag ->
                        StatusChip(label = tag, tone = ChipTone.Neutral)
                    }
                }
            }
            if (!pet.is_active) {
                OutlinedButton(onClick = onSetActiveClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Сделать активным")
                }
            }
        }
    }
}

@Composable
private fun PetForm(
    uiState: ProfileUiState,
    onCancel: () -> Unit,
    onPetNameChanged: (String) -> Unit,
    onPetSpeciesChanged: (String) -> Unit,
    onPetBreedChanged: (String) -> Unit,
    onPetSexChanged: (String) -> Unit,
    onPetBirthDateChanged: (String) -> Unit,
    onPetBioChanged: (String) -> Unit,
    onPetPhotoSelected: (Uri?) -> Unit,
    onClearPetPhoto: () -> Unit,
    onPersonalityInputChanged: (String) -> Unit,
    onAddPersonalityTag: () -> Unit,
    onRemovePersonalityTag: (String) -> Unit,
    onInterestInputChanged: (String) -> Unit,
    onAddInterest: () -> Unit,
    onRemoveInterest: (String) -> Unit,
    onPetHealthNotesChanged: (String) -> Unit,
    onPetMatchingGoalChanged: (String) -> Unit,
    onSavePet: () -> Unit
) {
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = onPetPhotoSelected
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 680.dp)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                if (uiState.petFormMode == PetFormMode.Edit) "Редактировать питомца" else "Новый питомец",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        item { OutlinedTextField(uiState.petName, onPetNameChanged, Modifier.fillMaxWidth(), label = { Text("Имя") }, singleLine = true) }
        item { OutlinedTextField(uiState.petSpecies, onPetSpeciesChanged, Modifier.fillMaxWidth(), label = { Text("Вид") }, singleLine = true) }
        item { OutlinedTextField(uiState.petBreed, onPetBreedChanged, Modifier.fillMaxWidth(), label = { Text("Порода") }, singleLine = true) }
        item { OutlinedTextField(uiState.petSex, onPetSexChanged, Modifier.fillMaxWidth(), label = { Text("Пол") }, singleLine = true) }
        item { OutlinedTextField(uiState.petBirthDate, onPetBirthDateChanged, Modifier.fillMaxWidth(), label = { Text("Дата рождения YYYY-MM-DD") }, singleLine = true) }
        item { OutlinedTextField(uiState.petBio, onPetBioChanged, Modifier.fillMaxWidth(), label = { Text("Описание") }, minLines = 2) }
        item {
            OutlinedButton(
                onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSavingPet
            ) {
                Text(if (uiState.selectedPetPhotoUri == null && uiState.currentPetPhotoUrl.isNullOrBlank()) "Выбрать фото" else "Заменить фото")
            }
        }
        uiState.selectedPetPhotoUri?.let { uri ->
            item {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                TextButton(onClick = onClearPetPhoto, enabled = !uiState.isSavingPet) {
                    Text("Убрать выбранное фото")
                }
            }
        } ?: uiState.currentPetPhotoUrl?.let { imageUrl ->
            item {
                PetPhoto(
                    imageUrl = imageUrl,
                    authToken = uiState.authToken,
                    contentDescription = uiState.petName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    cornerRadius = 16.dp
                )
            }
        }
        item {
            ChipEditor(
                title = "Характер",
                input = uiState.petPersonalityInput,
                chips = uiState.petPersonalityTags,
                onInputChanged = onPersonalityInputChanged,
                onAdd = onAddPersonalityTag,
                onRemove = onRemovePersonalityTag
            )
        }
        item {
            ChipEditor(
                title = "Интересы",
                input = uiState.petInterestInput,
                chips = uiState.petInterests,
                onInputChanged = onInterestInputChanged,
                onAdd = onAddInterest,
                onRemove = onRemoveInterest
            )
        }
        item { OutlinedTextField(uiState.petMatchingGoal, onPetMatchingGoalChanged, Modifier.fillMaxWidth(), label = { Text("Цель поиска") }, singleLine = true) }
        item { OutlinedTextField(uiState.petHealthNotes, onPetHealthNotesChanged, Modifier.fillMaxWidth(), label = { Text("Здоровье") }, minLines = 2) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !uiState.isSavingPet) {
                    Text("Отмена")
                }
                Button(onClick = onSavePet, modifier = Modifier.weight(1f), enabled = !uiState.isSavingPet) {
                    if (uiState.isSavingPet) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text("Сохранить")
                }
            }
        }
    }
}

@Composable
private fun ChipEditor(
    title: String,
    input: String,
    chips: List<String>,
    onInputChanged: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChanged,
                modifier = Modifier.weight(1f),
                label = { Text("Добавить тег") },
                singleLine = true
            )
            IconButton(onClick = onAdd) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = PetPrimary)
            }
        }
        if (chips.isNotEmpty()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chips.forEach { chip ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = PetPrimaryLight,
                        modifier = Modifier.clickable { onRemove(chip) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(chip, style = MaterialTheme.typography.labelMedium, color = PetPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Rounded.Close, contentDescription = null, tint = PetPrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PetPhoto(
    imageUrl: String?,
    authToken: String?,
    contentDescription: String?,
    modifier: Modifier,
    cornerRadius: Dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
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
            Icon(Icons.Rounded.Pets, contentDescription = null, tint = PetPrimary, modifier = Modifier.size(32.dp))
        }
    }
}

private fun petSubtitle(pet: PetResponse): String {
    return listOfNotNull(
        pet.breed,
        pet.sex,
        pet.birth_date?.take(4)
    ).joinToString(", ").ifBlank { pet.species }
}
