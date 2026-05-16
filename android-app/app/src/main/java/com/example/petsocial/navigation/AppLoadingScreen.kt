package com.example.petsocial.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetTextSecondary

@Composable
fun AppLoadingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.Favorite, contentDescription = null, tint = PetPrimary)
        Text("PetSocial", style = MaterialTheme.typography.headlineSmall)
        Text("Загружаем друзей рядом", color = PetTextSecondary)
        CircularProgressIndicator(color = PetPrimary, strokeWidth = 3.dp)
    }
}
