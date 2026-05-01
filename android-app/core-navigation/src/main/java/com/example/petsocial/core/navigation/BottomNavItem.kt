package com.example.petsocial.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = AppRoutes.Profile,
        title = "Профиль",
        icon = Icons.Default.Person
    ),
    BottomNavItem(
        route = AppRoutes.Pets,
        title = "Питомцы",
        icon = Icons.Default.Pets
    ),
    BottomNavItem(
        route = AppRoutes.Matching,
        title = "Matching",
        icon = Icons.Default.Favorite
    ),
    BottomNavItem(
        route = AppRoutes.Chats,
        title = "Чаты",
        icon = Icons.Default.Chat
    ),
    BottomNavItem(
        route = AppRoutes.Routine,
        title = "Routine",
        icon = Icons.Default.Task
    )
)