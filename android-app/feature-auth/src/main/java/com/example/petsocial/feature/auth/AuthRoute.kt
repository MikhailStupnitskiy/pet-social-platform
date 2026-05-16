package com.example.petsocial.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

private enum class AuthScreen {
    Login,
    Register
}

@Composable
fun AuthRoute(
    startOnRegister: Boolean = false,
    registerAsHandler: Boolean = false,
    onAuthSuccess: () -> Unit
) {
    val currentScreen = remember {
        mutableStateOf(if (startOnRegister) AuthScreen.Register else AuthScreen.Login)
    }

    LaunchedEffect(startOnRegister) {
        if (startOnRegister) {
            currentScreen.value = AuthScreen.Register
        }
    }

    when (currentScreen.value) {
        AuthScreen.Login -> {
            LoginRoute(
                onLoginSuccess = onAuthSuccess,
                onRegisterClick = {
                    currentScreen.value = AuthScreen.Register
                }
            )
        }

        AuthScreen.Register -> {
            RegisterRoute(
                registerAsHandler = registerAsHandler,
                onRegisterSuccess = onAuthSuccess,
                onLoginClick = {
                    currentScreen.value = AuthScreen.Login
                }
            )
        }
    }
}
