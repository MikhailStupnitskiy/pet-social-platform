package com.example.petsocial.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

fun bottomNavItemsFor(isHandler: Boolean): List<BottomNavItem> {
    return if (isHandler) {
        handlerBottomNavItems
    } else {
        ownerBottomNavItems
    }
}

private val ownerBottomNavItems = listOf(
    BottomNavItem(
        route = AppRoutes.Feed,
        title = "Лента",
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        route = AppRoutes.Matching,
        title = "Знакомства",
        icon = Icons.Default.Favorite
    ),
    BottomNavItem(
        route = AppRoutes.Chats,
        title = "Чаты",
        icon = Icons.Default.Chat
    ),
    BottomNavItem(
        route = AppRoutes.Care,
        title = "Уход",
        icon = Icons.Default.Task
    ),
    BottomNavItem(
        route = AppRoutes.Profile,
        title = "Профиль",
        icon = Icons.Default.Person
    )
)

private val handlerBottomNavItems = listOf(
    BottomNavItem(
        route = AppRoutes.Feed,
        title = "Лента",
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        route = AppRoutes.Handlers,
        title = "Работа",
        icon = Icons.Default.Work
    ),
    BottomNavItem(
        route = AppRoutes.Chats,
        title = "Чаты",
        icon = Icons.Default.Chat
    ),
    BottomNavItem(
        route = AppRoutes.Profile,
        title = "Профиль",
        icon = Icons.Default.Person
    )
)
