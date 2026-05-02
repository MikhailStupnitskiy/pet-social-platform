package com.example.petsocial.feature.pets

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
import com.example.petsocial.core.network.model.pets.PetResponse
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.SectionTitle
import com.example.petsocial.core.ui.SuccessMessage
import com.example.petsocial.core.designsystem.component.PetSocialCard

@Composable
fun PetsRoute(
    viewModel: PetsViewModel = hiltViewModel()
){
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
    onCreateClick: () -> Unit,
    onSetActiveClick: (String) -> Unit
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
        } else {
            items(uiState.pets) { pet ->
                PetCard(
                    pet = pet,
                    onSetActiveClick = {
                        onSetActiveClick(pet.id)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Добавить питомца")
        }

        item {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Имя") },
                singleLine = true,
                enabled = !uiState.isCreating
            )
        }

        item {
            OutlinedTextField(
                value = uiState.species,
                onValueChange = onSpeciesChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Вид, например dog") },
                singleLine = true,
                enabled = !uiState.isCreating
            )
        }

        item {
            OutlinedTextField(
                value = uiState.breed,
                onValueChange = onBreedChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Порода") },
                singleLine = true,
                enabled = !uiState.isCreating
            )
        }

        item {
            OutlinedTextField(
                value = uiState.sex,
                onValueChange = onSexChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Пол, например male/female") },
                singleLine = true,
                enabled = !uiState.isCreating
            )
        }

        item {
            OutlinedTextField(
                value = uiState.birthDate,
                onValueChange = onBirthDateChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Дата рождения YYYY-MM-DD") },
                singleLine = true,
                enabled = !uiState.isCreating
            )
        }

        item {
            OutlinedTextField(
                value = uiState.weightKg,
                onValueChange = onWeightKgChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Вес, например 12.5") },
                singleLine = true,
                enabled = !uiState.isCreating
            )
        }

        item {
            OutlinedTextField(
                value = uiState.bio,
                onValueChange = onBioChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Описание") },
                enabled = !uiState.isCreating
            )
        }

        if (uiState.errorMessage != null) {
            item {
                ErrorMessage(message = uiState.errorMessage)
            }
        }

        if (uiState.successMessage != null) {
            item {
                SuccessMessage(message = uiState.successMessage)
            }
        }

        item {
            Button(
                onClick = onCreateClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isCreating
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator()
                } else {
                    Text("Создать питомца")
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
    PetSocialCard(
        modifier = Modifier.fillMaxWidth()
    ) { contentModifier ->
        Column(
            modifier = contentModifier
        ) {
            Text(
                text = pet.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Вид: ${pet.species}")

            if (!pet.breed.isNullOrBlank()) {
                Text("Порода: ${pet.breed}")
            }

            if (!pet.sex.isNullOrBlank()) {
                Text("Пол: ${pet.sex}")
            }

            if (!pet.birth_date.isNullOrBlank()) {
                Text("Дата рождения: ${pet.birth_date}")
            }

            if (!pet.weight_kg.isNullOrBlank()) {
                Text("Вес: ${pet.weight_kg} кг")
            }

            if (!pet.bio.isNullOrBlank()) {
                Text("Описание: ${pet.bio}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (pet.is_active) {
                Text(
                    text = "Активный питомец",
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(onClick = onSetActiveClick) {
                    Text("Сделать активным")
                }
            }
        }
    }
}