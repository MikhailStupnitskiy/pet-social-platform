package com.example.petsocial.feature.pets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petsocial.core.designsystem.component.ActionTile
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.component.PetImage
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.SectionHeader
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.SuccessMessage

@Composable
fun PetsRoute(
    viewModel: PetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPetsIfNeeded()
    }

    PetsScreen(
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onSpeciesChanged = viewModel::onSpeciesChanged,
        onBreedChanged = viewModel::onBreedChanged,
        onSexChanged = viewModel::onSexChanged,
        onBirthDateChanged = viewModel::onBirthDateChanged,
        onWeightKgChanged = viewModel::onWeightKgChanged,
        onBioChanged = viewModel::onBioChanged,
        onPhotoUrlChanged = viewModel::onPhotoUrlChanged,
        onPhotoSelected = viewModel::onPhotoSelected,
        onClearSelectedPhoto = viewModel::clearSelectedPhoto,
        onPersonalityTagsChanged = viewModel::onPersonalityTagsChanged,
        onInterestsChanged = viewModel::onInterestsChanged,
        onHealthNotesChanged = viewModel::onHealthNotesChanged,
        onMatchingGoalChanged = viewModel::onMatchingGoalChanged,
        onAddPetClick = viewModel::showAddPetForm,
        onCancelAddPetClick = viewModel::hideAddPetForm,
        onCreateClick = viewModel::createPet,
        onSetActiveClick = viewModel::setActivePet
    )
}

@Composable
private fun PetsScreen(
    uiState: PetsUiState,
    onNameChanged: (String) -> Unit,
    onSpeciesChanged: (String) -> Unit,
    onBreedChanged: (String) -> Unit,
    onSexChanged: (String) -> Unit,
    onBirthDateChanged: (String) -> Unit,
    onWeightKgChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onPhotoUrlChanged: (String) -> Unit,
    onPhotoSelected: (Uri?) -> Unit,
    onClearSelectedPhoto: () -> Unit,
    onPersonalityTagsChanged: (String) -> Unit,
    onInterestsChanged: (String) -> Unit,
    onHealthNotesChanged: (String) -> Unit,
    onMatchingGoalChanged: (String) -> Unit,
    onAddPetClick: () -> Unit,
    onCancelAddPetClick: () -> Unit,
    onCreateClick: () -> Unit,
    onSetActiveClick: (String) -> Unit
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = onPhotoSelected
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        if (uiState.isLoading) {
            item { FullScreenLoading() }
        } else {
            item { SectionHeader(title = "Мои питомцы") }

            uiState.errorMessage?.let { message ->
                item { ErrorMessage(message) }
            }
            uiState.successMessage?.let { message ->
                item { SuccessMessage(message) }
            }

            if (uiState.pets.isEmpty()) {
                item {
                    ProductCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Добавьте первого питомца",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Создайте профиль питомца, чтобы искать друзей, вести расписание и общаться.",
                            color = PetTextSecondary
                        )
                    }
                }
            } else {
                items(uiState.pets) { pet ->
                    PetCard(
                        pet = pet,
                        onSetActiveClick = { onSetActiveClick(pet.id) }
                    )
                }
            }

            if (uiState.isAddPetFormVisible) {
                item {
                    AddPetForm(
                        uiState = uiState,
                        onNameChanged = onNameChanged,
                        onSpeciesChanged = onSpeciesChanged,
                        onBreedChanged = onBreedChanged,
                        onSexChanged = onSexChanged,
                        onBirthDateChanged = onBirthDateChanged,
                        onWeightKgChanged = onWeightKgChanged,
                        onBioChanged = onBioChanged,
                        onPhotoUrlChanged = onPhotoUrlChanged,
                        onPickPhotoClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onClearSelectedPhoto = onClearSelectedPhoto,
                        onPersonalityTagsChanged = onPersonalityTagsChanged,
                        onInterestsChanged = onInterestsChanged,
                        onHealthNotesChanged = onHealthNotesChanged,
                        onMatchingGoalChanged = onMatchingGoalChanged,
                        onCancelClick = onCancelAddPetClick,
                        onCreateClick = onCreateClick
                    )
                }
            } else {
                item {
                    ActionTile(
                        title = "Добавить питомца",
                        subtitle = "Профиль, фото, теги",
                        icon = {
                            Icon(Icons.Rounded.Add, contentDescription = null, tint = PetPrimary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !uiState.isCreating, onClick = onAddPetClick)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddPetForm(
    uiState: PetsUiState,
    onNameChanged: (String) -> Unit,
    onSpeciesChanged: (String) -> Unit,
    onBreedChanged: (String) -> Unit,
    onSexChanged: (String) -> Unit,
    onBirthDateChanged: (String) -> Unit,
    onWeightKgChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onPhotoUrlChanged: (String) -> Unit,
    onPickPhotoClick: () -> Unit,
    onClearSelectedPhoto: () -> Unit,
    onPersonalityTagsChanged: (String) -> Unit,
    onInterestsChanged: (String) -> Unit,
    onHealthNotesChanged: (String) -> Unit,
    onMatchingGoalChanged: (String) -> Unit,
    onCancelClick: () -> Unit,
    onCreateClick: () -> Unit
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Новый питомец",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(uiState.name, onNameChanged, Modifier.fillMaxWidth(), label = { Text("Имя") }, singleLine = true)
        OutlinedTextField(uiState.species, onSpeciesChanged, Modifier.fillMaxWidth(), label = { Text("Вид, например dog") }, singleLine = true)
        OutlinedTextField(uiState.breed, onBreedChanged, Modifier.fillMaxWidth(), label = { Text("Порода") }, singleLine = true)
        OutlinedTextField(uiState.sex, onSexChanged, Modifier.fillMaxWidth(), label = { Text("Пол") }, singleLine = true)
        OutlinedTextField(uiState.birthDate, onBirthDateChanged, Modifier.fillMaxWidth(), label = { Text("Дата рождения YYYY-MM-DD") }, singleLine = true)
        OutlinedTextField(uiState.weightKg, onWeightKgChanged, Modifier.fillMaxWidth(), label = { Text("Вес, кг") }, singleLine = true)
        OutlinedButton(
            onClick = onPickPhotoClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isCreating
        ) {
            Text(if (uiState.selectedPhotoUri == null) "Выбрать фото с устройства" else "Заменить фото")
        }
        uiState.selectedPhotoUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            TextButton(
                onClick = onClearSelectedPhoto,
                enabled = !uiState.isCreating
            ) {
                Text("Удалить фото")
            }
        }
        OutlinedTextField(uiState.photoUrl, onPhotoUrlChanged, Modifier.fillMaxWidth(), label = { Text("Ссылка на фото (необязательно)") }, singleLine = true)
        OutlinedTextField(uiState.bio, onBioChanged, Modifier.fillMaxWidth(), label = { Text("Описание") }, minLines = 2)
        OutlinedTextField(uiState.personalityTags, onPersonalityTagsChanged, Modifier.fillMaxWidth(), label = { Text("Характер через запятую") }, singleLine = true)
        OutlinedTextField(uiState.interests, onInterestsChanged, Modifier.fillMaxWidth(), label = { Text("Интересы через запятую") }, singleLine = true)
        OutlinedTextField(uiState.healthNotes, onHealthNotesChanged, Modifier.fillMaxWidth(), label = { Text("Здоровье и особенности") }, minLines = 2)
        OutlinedTextField(uiState.matchingGoal, onMatchingGoalChanged, Modifier.fillMaxWidth(), label = { Text("Кого ищем") }, singleLine = true)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancelClick,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isCreating
            ) {
                Text("Отмена")
            }
            Button(
                onClick = onCreateClick,
                modifier = Modifier.weight(1f),
                enabled = !uiState.isCreating
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Создать")
                }
            }
        }
    }
}

@Composable
private fun PetCard(
    pet: PetResponse,
    onSetActiveClick: () -> Unit
) {
    ProductCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(0.dp)) {
        PetImage(
            imageUrl = pet.photo_url,
            contentDescription = pet.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            cornerRadius = 16.dp
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(pet.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = listOfNotNull(pet.breed, pet.sex, pet.birth_date?.take(4)).joinToString(", ").ifBlank { pet.species },
                        color = PetTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusChip(
                    label = if (pet.is_active) "Активен" else "Скрыт",
                    tone = if (pet.is_active) ChipTone.Secondary else ChipTone.Neutral
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(label = pet.matching_goal ?: "В поиске друзей", tone = ChipTone.Primary)
                StatusChip(label = pet.health_notes?.take(18) ?: "Здоровье", tone = ChipTone.Info)
            }

            if (pet.personality_tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pet.personality_tags.take(3).forEach { tag ->
                        StatusChip(label = tag, tone = ChipTone.Neutral)
                    }
                }
            }

            if (!pet.is_active) {
                Button(onClick = onSetActiveClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Сделать активным")
                }
            }
        }
    }
}
