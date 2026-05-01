package com.example.petsocial.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.petsocial.core.navigation.AppRoutes
import com.example.petsocial.feature.auth.AuthRoute

fun NavGraphBuilder.authGraph(
    onAuthSuccess: () -> Unit
) {
    composable(AppRoutes.Auth) {
        AuthRoute(
            onAuthSuccess = onAuthSuccess
        )
    }
}