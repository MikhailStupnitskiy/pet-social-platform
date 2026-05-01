package com.example.petsocial.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
        sessionViewModel = sessionViewModel,
        sessionState = sessionState
    )
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    sessionState: SessionUiState
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
            MainScaffold(
                navController = navController,
                sessionViewModel = sessionViewModel,
                sessionState = sessionState
            )
        }

        composable(AppRoutes.Pets) {
            MainScaffold(
                navController = navController,
                sessionViewModel = sessionViewModel,
                sessionState = sessionState
            )
        }

        composable(AppRoutes.Matching) {
            MainScaffold(
                navController = navController,
                sessionViewModel = sessionViewModel,
                sessionState = sessionState
            )
        }

        composable(AppRoutes.Chats) {
            MainScaffold(
                navController = navController,
                sessionViewModel = sessionViewModel,
                sessionState = sessionState
            )
        }

        composable(AppRoutes.Routine) {
            MainScaffold(
                navController = navController,
                sessionViewModel = sessionViewModel,
                sessionState = sessionState
            )
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    sessionState: SessionUiState
) {
    if (sessionState != SessionUiState.Authorized) {
        AppLoadingScreen()
        return
    }

    Scaffold(
        bottomBar = {
            PetSocialBottomBar(navController = navController)
        }
    ) { innerPadding ->
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            when (currentRoute) {
                AppRoutes.Profile -> {
                    ProfileRoute(
                        onLogoutClick = {
                            sessionViewModel.logout()
                        }
                    )
                }

                AppRoutes.Pets -> {
                    PetsRoute(
                        onBackClick = {
                            navController.navigate(AppRoutes.Profile) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                AppRoutes.Matching -> {
                    MatchingRoute(
                        onBackClick = {
                            navController.navigate(AppRoutes.Profile) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                AppRoutes.Chats -> {
                    ChatsRoute(
                        onBackClick = {
                            navController.navigate(AppRoutes.Profile) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                AppRoutes.Routine -> {
                    RoutineRoute(
                        onBackClick = {
                            navController.navigate(AppRoutes.Profile) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PetSocialBottomBar(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentDestination.isSelected(item.route),
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(AppRoutes.Profile) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(item.title)
                }
            )
        }
    }
}

private fun NavDestination?.isSelected(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
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