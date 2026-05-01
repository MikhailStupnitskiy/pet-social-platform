package com.example.petsocial.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petsocial.SessionUiState
import com.example.petsocial.SessionViewModel
import com.example.petsocial.feature.auth.AuthRoute
import com.example.petsocial.feature.chat.ChatsRoute
import com.example.petsocial.feature.matching.MatchingRoute
import com.example.petsocial.feature.pets.PetsRoute
import com.example.petsocial.feature.profile.ProfileRoute
import com.example.petsocial.feature.routine.RoutineRoute

@Composable
fun PetSocialAppRoot(
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val sessionState by sessionViewModel.uiState.collectAsState()

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
        }
    }

    AppNavHost(
        navController = navController,
        sessionViewModel = sessionViewModel
    )
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    sessionViewModel: SessionViewModel
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Loading
    ) {
        composable(AppRoutes.Loading) {
            AppLoadingScreen()
        }

        composable(AppRoutes.Auth) {
            AuthRoute(
                onAuthSuccess = {
                    sessionViewModel.onAuthSuccess()
                }
            )
        }

        composable(AppRoutes.Profile) {
            ProfileRoute(
                onPetsClick = {
                    navController.navigate(AppRoutes.Pets)
                },
                onMatchingClick = {
                    navController.navigate(AppRoutes.Matching)
                },
                onChatsClick = {
                    navController.navigate(AppRoutes.Chats)
                },
                onRoutineClick = {
                    navController.navigate(AppRoutes.Routine)
                },
                onLogoutClick = {
                    sessionViewModel.logout()
                }
            )
        }

        composable(AppRoutes.Pets) {
            PetsRoute(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.Matching) {
            MatchingRoute(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.Chats) {
            ChatsRoute(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AppRoutes.Routine) {
            RoutineRoute(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

@Composable
private fun AppLoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}