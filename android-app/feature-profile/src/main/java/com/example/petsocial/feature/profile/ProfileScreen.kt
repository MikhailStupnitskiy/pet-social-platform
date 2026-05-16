package com.example.petsocial.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.SuccessMessage
import com.example.petsocial.core.ui.ScreenTitle


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
    when {
        uiState.isLoading -> {
            FullScreenLoading()
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                ScreenTitle("Профиль")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.email,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Имя") },
                    singleLine = true,
                    enabled = !uiState.isSaving
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.birthDate,
                    onValueChange = onBirthDateChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Дата рождения YYYY-MM-DD") },
                    singleLine = true,
                    enabled = !uiState.isSaving
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.city,
                    onValueChange = onCityChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Город") },
                    singleLine = true,
                    enabled = !uiState.isSaving
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.bio,
                    onValueChange = onBioChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("О себе") },
                    enabled = !uiState.isSaving
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.avatarUrl,
                    onValueChange = onAvatarUrlChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Avatar URL") },
                    singleLine = true,
                    enabled = !uiState.isSaving
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ErrorMessage(message = uiState.errorMessage)

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRetryClick) {
                        Text("Повторить")
                    }
                }

                if (uiState.successMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    SuccessMessage(message = uiState.successMessage)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(8.dp))

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

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onCreateHandlerProfileClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving
                ) {
                    Text("Создать профиль хэндлера")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Выйти")
                }
            }
        }
    }
}
