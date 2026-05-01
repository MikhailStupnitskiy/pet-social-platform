package com.example.petsocial.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.petsocial.core.navigation.AppRoutes
import com.example.petsocial.core.navigation.bottomNavItems
import com.example.petsocial.feature.chat.ChatsRoute
import com.example.petsocial.feature.matching.MatchingRoute
import com.example.petsocial.feature.pets.PetsRoute
import com.example.petsocial.feature.profile.ProfileRoute
import com.example.petsocial.feature.routine.RoutineRoute

fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    onLogoutClick: () -> Unit
) {
    composable(AppRoutes.Profile) {
        MainScaffold(
            navController = navController,
            onLogoutClick = onLogoutClick
        )
    }

    composable(AppRoutes.Pets) {
        MainScaffold(
            navController = navController,
            onLogoutClick = onLogoutClick
        )
    }

    composable(AppRoutes.Matching) {
        MainScaffold(
            navController = navController,
            onLogoutClick = onLogoutClick
        )
    }

    composable(AppRoutes.Chats) {
        MainScaffold(
            navController = navController,
            onLogoutClick = onLogoutClick
        )
    }

    composable(AppRoutes.Routine) {
        MainScaffold(
            navController = navController,
            onLogoutClick = onLogoutClick
        )
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        bottomBar = {
            PetSocialBottomBar(navController = navController)
        }
    ) { innerPadding ->
        val currentRoute = navController.currentBackStackEntryAsState()
            .value
            ?.destination
            ?.route

        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            when (currentRoute) {
                AppRoutes.Profile -> {
                    ProfileRoute(
                        onLogoutClick = onLogoutClick
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