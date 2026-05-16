package com.example.petsocial.feature.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.SectionHeader
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.routine.RoutineItemResponse
import com.example.petsocial.core.ui.FullScreenLoading

@Composable
fun RoutineRoute(
    viewModel: RoutineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    RoutineScreen(
        uiState = uiState,
        onTitleChanged = viewModel::onTitleChanged,
        onCategoryChanged = viewModel::onCategoryChanged,
        onScheduleTimeChanged = viewModel::onScheduleTimeChanged,
        onNotesChanged = viewModel::onNotesChanged,
        onCreateClick = viewModel::createRoutineItem,
        onCompleteClick = viewModel::completeRoutineItem,
        onRetryClick = viewModel::load
    )
}

@Composable
private fun RoutineScreen(
    uiState: RoutineUiState,
    onTitleChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onScheduleTimeChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onCreateClick: () -> Unit,
    onCompleteClick: (String) -> Unit,
    onRetryClick: () -> Unit
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (uiState.isLoading) {
            item { FullScreenLoading() }
            return@LazyColumn
        }

        item {
            SectionHeader(title = "Расписание")
        }

        uiState.activePet?.let { pet ->
            item {
                ProductCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Активный питомец",
                        style = MaterialTheme.typography.labelLarge,
                        color = PetTextSecondary
                    )
                    Text(
                        text = pet.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = pet.species,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PetTextSecondary
                    )
                }
            }
        }

        uiState.errorMessage?.let { message ->
            item {
                ProductCard(modifier = Modifier.fillMaxWidth()) {
                    Text(message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onRetryClick) {
                        Text("Повторить")
                    }
                }
            }
        }

        uiState.successMessage?.let { message ->
            item {
                StatusChip(label = message, tone = ChipTone.Secondary)
            }
        }

        item {
            SectionHeader(title = "Задачи ухода")
        }

        if (uiState.items.isEmpty()) {
            item {
                ProductCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Пока нет задач",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Добавьте прогулку, кормление или лекарство, чтобы держать уход под контролем.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PetTextSecondary
                    )
                }
            }
        } else {
            items(uiState.items, key = { it.id }) { item ->
                RoutineItemCard(
                    item = item,
                    isCompleting = uiState.isCompleting,
                    onCompleteClick = { onCompleteClick(item.id) }
                )
            }
        }

        item {
            SectionHeader(title = "Добавить задачу")
        }

        item {
            ProductCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Название") },
                    placeholder = { Text("Вечерняя прогулка") },
                    singleLine = true,
                    enabled = !uiState.isCreating,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = uiState.category,
                    onValueChange = onCategoryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Категория") },
                    placeholder = { Text("walk, feed, medicine") },
                    singleLine = true,
                    enabled = !uiState.isCreating,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = uiState.scheduleTime,
                    onValueChange = onScheduleTimeChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Время") },
                    placeholder = { Text("08:00:00") },
                    singleLine = true,
                    enabled = !uiState.isCreating,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = onNotesChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Заметки") },
                    enabled = !uiState.isCreating,
                    shape = RoundedCornerShape(16.dp)
                )
                Button(
                    onClick = onCreateClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isCreating,
                    colors = ButtonDefaults.buttonColors(containerColor = PetPrimary)
                ) {
                    if (uiState.isCreating) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Создать задачу")
                    }
                }
            }
        }
    }
}

@Composable
private fun RoutineItemCard(
    item: RoutineItemResponse,
    isCompleting: Boolean,
    onCompleteClick: () -> Unit
) {
    ProductCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PetTextSecondary
                )
            }
            StatusChip(
                label = if (item.is_enabled) "Активна" else "Отключена",
                tone = if (item.is_enabled) ChipTone.Secondary else ChipTone.Neutral
            )
        }

        item.schedule_time?.takeIf { it.isNotBlank() }?.let { time ->
            Text("Время: $time", style = MaterialTheme.typography.bodyMedium)
        }
        item.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            Text(notes, style = MaterialTheme.typography.bodyMedium, color = PetTextSecondary)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = onCompleteClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCompleting && item.is_enabled,
            colors = ButtonDefaults.buttonColors(containerColor = PetPrimary)
        ) {
            Text("Выполнено")
        }
    }
}
