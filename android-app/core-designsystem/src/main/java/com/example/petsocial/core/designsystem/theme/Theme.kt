package com.example.petsocial.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme: ColorScheme = lightColorScheme(
    primary = PetPrimary,
    onPrimary = PetOnPrimary,
    secondary = PetSecondary,
    onSecondary = PetOnSecondary,
    background = PetBackground,
    onBackground = PetOnBackground,
    surface = PetSurface,
    onSurface = PetOnSurface,
    error = PetError,
    onError = PetOnError
)

private val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = PetPrimaryDark,
    onPrimary = PetOnPrimaryDark,
    secondary = PetSecondary,
    onSecondary = PetOnSecondary,
    background = PetBackgroundDark,
    onBackground = PetOnBackgroundDark,
    surface = PetSurfaceDark,
    onSurface = PetOnSurfaceDark,
    error = PetError,
    onError = PetOnError
)

@Composable
fun PetSocialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PetTypography,
        content = content
    )
}