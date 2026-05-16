package com.example.petsocial.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.petsocial.core.navigation.AppRoutes
import com.example.petsocial.core.navigation.bottomNavItemsFor
import com.example.petsocial.core.ui.PetSocialTopBar
import com.example.petsocial.feature.chat.ChatsRoute
import com.example.petsocial.feature.feed.FeedRoute
import com.example.petsocial.feature.handlers.HandlerProfileRoute
import com.example.petsocial.feature.handlers.HandlersRoute
import com.example.petsocial.feature.matching.MatchingRoute
import com.example.petsocial.feature.pets.PetsRoute
import com.example.petsocial.feature.profile.ProfileRoute
import com.example.petsocial.feature.routine.RoutineRoute

fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    isHandlerState: State<Boolean>,
    onCreateHandlerProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    listOf(
        AppRoutes.Profile,
        AppRoutes.Feed,
        AppRoutes.Matching,
        AppRoutes.Chats,
        AppRoutes.Care,
        AppRoutes.Handlers
    ).forEach { route ->
        composable(route) {
            MainScaffold(
                navController = navController,
                isHandler = isHandlerState.value,
                onCreateHandlerProfileClick = onCreateHandlerProfileClick,
                onLogoutClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    isHandler: Boolean,
    onCreateHandlerProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val currentRoute = navController.currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    Scaffold(
        topBar = {
            PetSocialTopBar(title = titleForRoute(currentRoute, isHandler))
        },
        bottomBar = {
            PetSocialBottomBar(navController = navController, isHandler = isHandler)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            when (currentRoute) {
                AppRoutes.Profile -> {
                    if (isHandler) {
                        HandlerProfileRoute(onLogoutClick = onLogoutClick)
                    } else {
                        OwnerProfileRoute(
                            onLogoutClick = onLogoutClick,
                            onCreateHandlerProfileClick = onCreateHandlerProfileClick
                        )
                    }
                }

                AppRoutes.Feed -> {
                    FeedRoute()
                }

                AppRoutes.Matching -> {
                    MatchingRoute()
                }

                AppRoutes.Chats -> {
                    ChatsRoute()
                }

                AppRoutes.Care -> {
                    CareRoute()
                }

                AppRoutes.Handlers -> {
                    HandlersRoute(isHandler = true)
                }
            }
        }
    }
}

@Composable
private fun OwnerProfileRoute(
    onLogoutClick: () -> Unit,
    onCreateHandlerProfileClick: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Профиль", "Питомцы")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> ProfileRoute(
                onLogoutClick = onLogoutClick,
                onUnauthorized = onLogoutClick,
                onCreateHandlerProfileClick = onCreateHandlerProfileClick
            )

            1 -> PetsRoute()
        }
    }
}

@Composable
private fun CareRoute() {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Рутина", "Хэндлеры")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> RoutineRoute()
            1 -> HandlersRoute(isHandler = false)
        }
    }
}

private fun titleForRoute(route: String?, isHandler: Boolean): String {
    return when (route) {
        AppRoutes.Profile -> "Профиль"
        AppRoutes.Feed -> "Лента"
        AppRoutes.Matching -> "Знакомства"
        AppRoutes.Chats -> "Чаты"
        AppRoutes.Care -> "Уход"
        AppRoutes.Handlers -> if (isHandler) "Работа" else "Услуги"
        else -> "PetSocial"
    }
}

@Composable
private fun PetSocialBottomBar(
    navController: NavHostController,
    isHandler: Boolean
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItemsFor(isHandler).forEach { item ->
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
