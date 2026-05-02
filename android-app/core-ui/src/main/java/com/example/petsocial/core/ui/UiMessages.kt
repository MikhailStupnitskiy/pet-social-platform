package com.example.petsocial.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ErrorMessage(
    message: String
) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
fun SuccessMessage(
    message: String
) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.primary
    )
}