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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.petsocial.core.network.model.pets.PetResponse

@Composable
fun PetsRoute(
    onBackClick: () -> Unit,
    viewModel: PetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPets()
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
        onSetActiveClick = viewModel::setActivePet,
        onBackClick = onBackClick
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
    onSetActiveClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Питомцы",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBackClick) {
                Text("Назад к профилю")
            }
        }

        if (uiState.isLoading) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
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

            Text(
                text = "Добавить питомца",
                style = MaterialTheme.typography.titleLarge
            )
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
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
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
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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