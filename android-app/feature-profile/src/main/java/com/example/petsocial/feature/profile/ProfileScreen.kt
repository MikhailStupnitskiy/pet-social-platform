package com.example.petsocial.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.designsystem.component.PetAvatar
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.SectionHeader
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
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
        onNameChanged = viewModel::onNameChanged,
        onBirthDateChanged = viewModel::onBirthDateChanged,
        onCityChanged = viewModel::onCityChanged,
        onBioChanged = viewModel::onBioChanged,
        onAvatarUrlChanged = viewModel::onAvatarUrlChanged,
        onSaveClick = viewModel::saveProfile,
        onRetryClick = viewModel::loadProfile,
        onLogoutClick = onLogoutClick,
        onCreateHandlerProfileClick = onCreateHandlerProfileClick
    )
}

@Composable
private fun ProfileScreen(
    uiState: ProfileUiState,
    onNameChanged: (String) -> Unit,
    onBirthDateChanged: (String) -> Unit,
    onCityChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onAvatarUrlChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onRetryClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onCreateHandlerProfileClick: () -> Unit
) {
    if (uiState.isLoading) {
        FullScreenLoading()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            ProductCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PetAvatar(imageUrl = uiState.avatarUrl, contentDescription = uiState.name, size = 88.dp)
                }
                Text(
                    text = uiState.name.ifBlank { "Профиль владельца" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = uiState.city.ifBlank { uiState.email },
                    style = MaterialTheme.typography.bodyMedium,
                    color = PetTextSecondary
                )
                if (uiState.bio.isNotBlank()) {
                    Text(text = uiState.bio, style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat("1", "Питомец")
                    ProfileStat("12", "Мэтчей")
                    ProfileStat("48", "Постов")
                }
            }
        }

        item { SectionHeader(title = "Редактировать профиль") }

        item {
            ProductCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(uiState.name, onNameChanged, Modifier.fillMaxWidth(), label = { Text("Имя") }, singleLine = true, enabled = !uiState.isSaving)
                OutlinedTextField(uiState.birthDate, onBirthDateChanged, Modifier.fillMaxWidth(), label = { Text("Дата рождения YYYY-MM-DD") }, singleLine = true, enabled = !uiState.isSaving)
                OutlinedTextField(uiState.city, onCityChanged, Modifier.fillMaxWidth(), label = { Text("Город") }, singleLine = true, enabled = !uiState.isSaving)
                OutlinedTextField(uiState.bio, onBioChanged, Modifier.fillMaxWidth(), label = { Text("О себе") }, minLines = 2, enabled = !uiState.isSaving)
                OutlinedTextField(uiState.avatarUrl, onAvatarUrlChanged, Modifier.fillMaxWidth(), label = { Text("Ссылка на аватар") }, singleLine = true, enabled = !uiState.isSaving)

                uiState.errorMessage?.let {
                    ErrorMessage(message = it)
                    TextButton(onClick = onRetryClick) {
                        Text("Повторить")
                    }
                }

                uiState.successMessage?.let { SuccessMessage(message = it) }

                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator()
                    } else {
                        Text("Сохранить")
                    }
                }
            }
        }

        item {
            ProductCard(modifier = Modifier.fillMaxWidth()) {
                Text("Настройки", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onCreateHandlerProfileClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Создать профиль хэндлера")
                }
                TextButton(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth()) {
                    Text("Выйти", color = PetPrimary)
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = PetTextSecondary)
    }
}
