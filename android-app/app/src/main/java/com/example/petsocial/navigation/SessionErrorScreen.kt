package com.example.petsocial.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.ScreenTitle

@Composable
fun SessionErrorScreen(
    message: String,
    onRetryClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ScreenTitle("Ошибка сессии")

        ErrorMessage(message = message)

        Button(
            onClick = onRetryClick
        ) {
            Text("Повторить")
        }

        Button(
            onClick = onLogoutClick
        ) {
            Text("Выйти")
        }
    }
}