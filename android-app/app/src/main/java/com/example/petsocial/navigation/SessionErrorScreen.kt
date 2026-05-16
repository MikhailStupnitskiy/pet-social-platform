package com.example.petsocial.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetTextSecondary

@Composable
fun SessionErrorScreen(
    message: String,
    onRetryClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProductCard(modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Lock, contentDescription = null, tint = PetPrimary)
            Text("Сессия истекла", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(message.ifBlank { "Войдите снова, чтобы продолжить." }, color = PetTextSecondary)
            Button(onClick = onRetryClick, modifier = Modifier.fillMaxWidth()) {
                Text("Повторить")
            }
            TextButton(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth()) {
                Text("Войти снова")
            }
        }
    }
}
