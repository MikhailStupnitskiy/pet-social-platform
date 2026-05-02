package com.example.petsocial.feature.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.network.model.routine.RoutineItemResponse
import com.example.petsocial.core.ui.ErrorMessage
import com.example.petsocial.core.ui.FullScreenLoading
import com.example.petsocial.core.ui.ScreenTitle
import com.example.petsocial.core.ui.SectionTitle
import com.example.petsocial.core.ui.SuccessMessage

@Composable
fun RoutineRoute(
    onBackClick: () -> Unit,
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
        onRetryClick = viewModel::load,
        onBackClick = onBackClick
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
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenTitle("Routine")

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onBackClick) {
                Text("Назад к профилю")
            }
        }

        if (uiState.isLoading) {
            item {
                FullScreenLoading()
            }
            return@LazyColumn
        }

        if (uiState.activePet != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Активный питомец: ${uiState.activePet.name}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Вид: ${uiState.activePet.species}")
                    }
                }
            }
        }

        if (uiState.errorMessage != null) {
            item {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = onRetryClick) {
                    Text("Повторить")
                }
            }
        }

        if (uiState.successMessage != null) {
            item {
                Text(
                    text = uiState.successMessage,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            SectionTitle("Задачи ухода")
        }

        if (uiState.items.isEmpty()) {
            item {
                Text("Пока нет задач ухода")
            }
        } else {
            items(uiState.items) { item ->
                RoutineItemCard(
                    item = item,
                    isCompleting = uiState.isCompleting,
                    onCompleteClick = {
                        onCompleteClick(item.id)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("Добавить задачу")
        }

        item {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название, например Morning walk") },
                singleLine = true,
                enabled = !uiState.isCreating
            )
        }

        item {
            OutlinedTextField(
                value = uiState.category,
                onValueChange = onCategoryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Категория, например walk/feed/medicine") },
                singleLine = true,
                enabled = !uiState.isCreating
            )
        }

        item {
            OutlinedTextField(
                value = uiState.scheduleTime,
                onValueChange = onScheduleTimeChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Время HH:MM:SS, например 08:00:00") },
                singleLine = true,
                enabled = !uiState.isCreating
            )
        }

        item {
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Заметки") },
                enabled = !uiState.isCreating
            )
        }

        item {
            Button(
                onClick = onCreateClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isCreating
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator()
                } else {
                    Text("Создать задачу")
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
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Категория: ${item.category}")

            if (!item.schedule_time.isNullOrBlank()) {
                Text("Время: ${item.schedule_time}")
            }

            if (!item.notes.isNullOrBlank()) {
                Text("Заметки: ${item.notes}")
            }

            Text(
                text = if (item.is_enabled) {
                    "Активна"
                } else {
                    "Отключена"
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCompleteClick,
                enabled = !isCompleting && item.is_enabled
            ) {
                Text("Выполнено")
            }
        }
    }
}