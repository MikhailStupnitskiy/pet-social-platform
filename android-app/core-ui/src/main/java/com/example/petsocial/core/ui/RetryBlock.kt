package com.example.petsocial.core.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RetryBlock(
    message: String,
    onRetryClick: () -> Unit
) {
    ErrorMessage(message = message)

    Spacer(modifier = Modifier.height(8.dp))

    Button(onClick = onRetryClick) {
        Text("Повторить")
    }
}