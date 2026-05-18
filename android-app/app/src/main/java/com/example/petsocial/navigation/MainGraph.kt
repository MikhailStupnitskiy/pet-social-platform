package com.example.petsocial.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetPrimaryLight
import com.example.petsocial.core.designsystem.theme.PetSurface
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.navigation.AppRoutes
import com.example.petsocial.core.navigation.bottomNavItemsFor
import com.example.petsocial.core.ui.PetSocialTopBar
import com.example.petsocial.feature.chat.ChatsRoute
import com.example.petsocial.feature.feed.FeedRoute
import com.example.petsocial.feature.handlers.HandlerProfileRoute
import com.example.petsocial.feature.handlers.HandlersRoute
import com.example.petsocial.feature.matching.MatchingRoute
import com.example.petsocial.feature.profile.ProfileRoute
import com.example.petsocial.feature.profile.PublicPetProfileRoute
import com.example.petsocial.feature.profile.PublicUserProfileRoute
import com.example.petsocial.feature.routine.RoutineRoute
import com.example.petsocial.feature.notifications.NotificationsRoute
import com.example.petsocial.feature.notifications.NotificationDestination

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
        AppRoutes.Handlers,
        AppRoutes.Notifications
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

    composable(
        route = AppRoutes.PublicUser,
        arguments = listOf(navArgument(AppRoutes.PublicUserArg) { type = NavType.StringType })
    ) { entry ->
        val userId = entry.arguments?.getString(AppRoutes.PublicUserArg).orEmpty()
        PublicUserProfileRoute(
            userId = userId,
            onBack = { navController.popBackStack() },
            onPetClick = { petId -> navController.navigate(AppRoutes.publicPet(petId)) }
        )
    }

    composable(
        route = AppRoutes.PublicPet,
        arguments = listOf(navArgument(AppRoutes.PublicPetArg) { type = NavType.StringType })
    ) { entry ->
        val petId = entry.arguments?.getString(AppRoutes.PublicPetArg).orEmpty()
        PublicPetProfileRoute(
            petId = petId,
            onBack = { navController.popBackStack() },
            onOwnerClick = { userId -> navController.navigate(AppRoutes.publicUser(userId)) }
        )
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
        containerColor = PetBackground,
        topBar = {
            if (currentRoute != AppRoutes.Profile && currentRoute != AppRoutes.Feed && currentRoute != AppRoutes.Chats && currentRoute != AppRoutes.Notifications) {
                PetSocialTopBar(title = titleForRoute(currentRoute, isHandler))
            }
        },
        bottomBar = {
            PetSocialBottomBar(navController = navController, isHandler = isHandler)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                AppRoutes.Profile -> {
                    if (isHandler) {
                        HandlerProfileRoute(onLogoutClick = onLogoutClick)
                    } else {
                        OwnerProfileRoute(
                            onLogoutClick = onLogoutClick,
                            onCreateHandlerProfileClick = onCreateHandlerProfileClick,
                            onNotificationsClick = { navController.navigate(AppRoutes.Notifications) }
                        )
                    }
                }

                AppRoutes.Feed -> FeedRoute(
                    onProfileClick = {
                        navController.navigate(AppRoutes.Profile) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onMatchingClick = {
                        navController.navigate(AppRoutes.Matching) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onCareClick = {
                        navController.navigate(AppRoutes.Care) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onUserProfileClick = { userId -> navController.navigate(AppRoutes.publicUser(userId)) },
                    onPetProfileClick = { petId -> navController.navigate(AppRoutes.publicPet(petId)) }
                )
                AppRoutes.Matching -> MatchingRoute(
                    onPetProfileClick = { petId -> navController.navigate(AppRoutes.publicPet(petId)) }
                )
                AppRoutes.Chats -> ChatsRoute(
                    onUserProfileClick = { userId -> navController.navigate(AppRoutes.publicUser(userId)) },
                    onPetProfileClick = { petId -> navController.navigate(AppRoutes.publicPet(petId)) }
                )
                AppRoutes.Care -> CareRoute(
                    onUserProfileClick = { userId -> navController.navigate(AppRoutes.publicUser(userId)) },
                    onPetProfileClick = { petId -> navController.navigate(AppRoutes.publicPet(petId)) }
                )
                AppRoutes.Handlers -> HandlersRoute(
                    isHandler = true,
                    onUserProfileClick = { userId -> navController.navigate(AppRoutes.publicUser(userId)) },
                    onPetProfileClick = { petId -> navController.navigate(AppRoutes.publicPet(petId)) }
                )
                AppRoutes.Notifications -> NotificationsRoute(
                    onDestination = { destination ->
                        when (destination) {
                            NotificationDestination.Matching -> navController.navigate(AppRoutes.Matching) { launchSingleTop = true }
                            NotificationDestination.Chats -> navController.navigate(AppRoutes.Chats) { launchSingleTop = true }
                            NotificationDestination.Care -> navController.navigate(AppRoutes.Care) { launchSingleTop = true }
                            NotificationDestination.None -> Unit
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun OwnerProfileRoute(
    onLogoutClick: () -> Unit,
    onCreateHandlerProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    ProfileRoute(
        onLogoutClick = onLogoutClick,
        onUnauthorized = onLogoutClick,
        onCreateHandlerProfileClick = onCreateHandlerProfileClick,
        onNotificationsClick = onNotificationsClick
    )
}

@Composable
private fun CareRoute(
    onUserProfileClick: (String) -> Unit,
    onPetProfileClick: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Рутина", "Услуги")

    Column(modifier = Modifier.fillMaxSize()) {
        ProductTabRow(selectedTab = selectedTab, tabs = tabs) { selectedTab = it }

        when (selectedTab) {
            0 -> RoutineRoute()
            1 -> HandlersRoute(
                isHandler = false,
                onUserProfileClick = onUserProfileClick,
                onPetProfileClick = onPetProfileClick
            )
        }
    }
}

@Composable
private fun ProductTabRow(
    selectedTab: Int,
    tabs: List<String>,
    onSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = PetBackground,
        contentColor = PetPrimary
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onSelected(index) },
                text = { Text(title) },
                selectedContentColor = PetPrimary,
                unselectedContentColor = PetTextSecondary
            )
        }
    }
}

private fun titleForRoute(route: String?, isHandler: Boolean): String {
    if (route == AppRoutes.Chats) {
        return "\u0421\u043e\u043e\u0431\u0449\u0435\u043d\u0438\u044f"
    }

    return when (route) {
        AppRoutes.Profile -> "Профиль"
        AppRoutes.Feed -> "Лента"
        AppRoutes.Matching -> "Найти пару"
        AppRoutes.Chats -> "Сообщения"
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

    NavigationBar(
        containerColor = PetSurface,
        tonalElevation = 8.dp
    ) {
        bottomNavItemsFor(isHandler).forEach { item ->
            NavigationBarItem(
                selected = currentDestination.isSelected(item.route),
                onClick = {
                    navController.navigate(item.route) {
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
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PetPrimary,
                    selectedTextColor = PetPrimary,
                    indicatorColor = PetPrimaryLight,
                    unselectedIconColor = PetTextSecondary,
                    unselectedTextColor = PetTextSecondary
                )
            )
        }
    }
}

private fun NavDestination?.isSelected(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}
