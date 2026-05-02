package com.example.petsocial.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petsocial.SessionUiState
import com.example.petsocial.SessionViewModel
import com.example.petsocial.core.navigation.AppRoutes
import com.example.petsocial.SessionEventsViewModel
import com.example.petsocial.core.common.session.SessionEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PetSocialAppRoot(
    sessionViewModel: SessionViewModel = hiltViewModel(),
    sessionEventsViewModel: SessionEventsViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val sessionState by sessionViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        sessionEventsViewModel.sessionEventBus.events.collectLatest { event ->
            when (event) {
                SessionEvent.Unauthorized -> {
                    sessionViewModel.logout()
                }
            }
        }
    }

    LaunchedEffect(sessionState) {
        when (sessionState) {
            SessionUiState.AuthRequired -> {
                navController.navigate(AppRoutes.Auth) {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }

            SessionUiState.Authorized -> {
                navController.navigate(AppRoutes.Profile) {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }

            SessionUiState.Loading,
            SessionUiState.LoggingOut -> {
                navController.navigate(AppRoutes.Loading) {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }

            is SessionUiState.Error -> {
                navController.navigate(AppRoutes.SessionError) {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.Loading
    ) {
        composable(AppRoutes.Loading) {
            AppLoadingScreen()
        }

        composable(AppRoutes.SessionError) {
            val state = sessionState

            SessionErrorScreen(
                message = if (state is SessionUiState.Error) {
                    state.message
                } else {
                    "Неизвестная ошибка"
                },
                onRetryClick = {
                    sessionViewModel.checkSession()
                },
                onLogoutClick = {
                    sessionViewModel.logout()
                }
            )
        }

        authGraph(
            onAuthSuccess = {
                sessionViewModel.onAuthSuccess()
            }
        )

        mainGraph(
            navController = navController,
            onLogoutClick = {
                sessionViewModel.logout()
            }
        )
    }
}