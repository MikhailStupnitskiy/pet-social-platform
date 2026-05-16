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
    return if (isHandler) handlerBottomNavItems else ownerBottomNavItems
}

private val ownerBottomNavItems = listOf(
    BottomNavItem(AppRoutes.Feed, "Лента", Icons.Default.Home),
    BottomNavItem(AppRoutes.Matching, "Мэтчинг", Icons.Default.Favorite),
    BottomNavItem(AppRoutes.Chats, "Чаты", Icons.Default.Chat),
    BottomNavItem(AppRoutes.Care, "Уход", Icons.Default.Task),
    BottomNavItem(AppRoutes.Profile, "Профиль", Icons.Default.Person)
)

private val handlerBottomNavItems = listOf(
    BottomNavItem(AppRoutes.Feed, "Лента", Icons.Default.Home),
    BottomNavItem(AppRoutes.Handlers, "Работа", Icons.Default.Work),
    BottomNavItem(AppRoutes.Chats, "Чаты", Icons.Default.Chat),
    BottomNavItem(AppRoutes.Profile, "Профиль", Icons.Default.Person)
)
