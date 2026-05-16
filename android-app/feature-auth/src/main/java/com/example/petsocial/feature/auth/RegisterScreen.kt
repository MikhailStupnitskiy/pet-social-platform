package com.example.petsocial.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RegisterRoute(
    registerAsHandler: Boolean = false,
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isRegistered) {
        if (uiState.isRegistered) {
            onRegisterSuccess()
            viewModel.onRegisterEventConsumed()
        }
    }

    LaunchedEffect(registerAsHandler) {
        if (registerAsHandler) {
            viewModel.onHandlerChanged(true)
        }
    }

    RegisterScreen(
        uiState = uiState,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onRepeatedPasswordChanged = viewModel::onRepeatedPasswordChanged,
        onHandlerChanged = viewModel::onHandlerChanged,
        onRegisterClick = viewModel::register,
        onLoginClick = onLoginClick
    )
}

@Composable
private fun RegisterScreen(
    uiState: RegisterUiState,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRepeatedPasswordChanged: (String) -> Unit,
    onHandlerChanged: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Создание аккаунта",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Email")
            },
            singleLine = true,
            enabled = !uiState.isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Пароль")
            },
            singleLine = true,
            enabled = !uiState.isLoading,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = uiState.repeatedPassword,
            onValueChange = onRepeatedPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Повторите пароль")
            },
            singleLine = true,
            enabled = !uiState.isLoading,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.isHandler,
                onCheckedChange = onHandlerChanged,
                enabled = !uiState.isLoading
            )
            Text("Зарегистрироваться как хэндлер")
        }

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Зарегистрироваться")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onLoginClick,
            enabled = !uiState.isLoading
        ) {
            Text("Уже есть аккаунт? Войти")
        }
    }
}
