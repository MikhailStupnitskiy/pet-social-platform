package com.example.petsocial.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

private enum class AuthScreen {
    Login,
    Register
}

@Composable
fun AuthRoute(
    onAuthSuccess: () -> Unit
) {
    val currentScreen = remember {
        mutableStateOf(AuthScreen.Login)
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
                onRegisterSuccess = onAuthSuccess,
                onLoginClick = {
                    currentScreen.value = AuthScreen.Login
                }
            )
        }
    }
}