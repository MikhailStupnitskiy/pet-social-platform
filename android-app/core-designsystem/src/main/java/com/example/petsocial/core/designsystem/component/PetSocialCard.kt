package com.example.petsocial.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PetSocialCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable (Modifier) -> Unit
) {
    Card(
        modifier = modifier
    ) {
        content(
            Modifier.padding(contentPadding)
        )
    }
}