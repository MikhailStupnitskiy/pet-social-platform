package com.example.petsocial.feature.routine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.core.designsystem.component.ChipTone
import com.example.petsocial.core.designsystem.component.ProductCard
import com.example.petsocial.core.designsystem.component.StatusChip
import com.example.petsocial.core.designsystem.theme.PetBackground
import com.example.petsocial.core.designsystem.theme.PetError
import com.example.petsocial.core.designsystem.theme.PetOnSurface
import com.example.petsocial.core.designsystem.theme.PetOutline
import com.example.petsocial.core.designsystem.theme.PetPrimary
import com.example.petsocial.core.designsystem.theme.PetSecondary
import com.example.petsocial.core.designsystem.theme.PetSecondaryLight
import com.example.petsocial.core.designsystem.theme.PetSurface
import com.example.petsocial.core.designsystem.theme.PetSurfaceMuted
import com.example.petsocial.core.designsystem.theme.PetTextSecondary
import com.example.petsocial.core.network.model.handlers.ServiceRequestResponse
import com.example.petsocial.core.network.model.routine.RoutineItemResponse
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale

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
        onRepeatRuleChanged = viewModel::onRepeatRuleChanged,
        onNotesChanged = viewModel::onNotesChanged,
        onSelectedDateChanged = viewModel::onSelectedDateChanged,
        onShowCreateSheet = viewModel::showCreateSheet,
        onDismissCreateSheet = viewModel::hideCreateSheet,
        onCreateClick = viewModel::createRoutineItem,
        onCompleteClick = viewModel::completeRoutineItem,
        onDeleteClick = viewModel::deleteRoutineItem,
        onRetryClick = viewModel::load
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineScreen(
    uiState: RoutineUiState,
    onTitleChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onScheduleTimeChanged: (String) -> Unit,
    onRepeatRuleChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSelectedDateChanged: (LocalDate) -> Unit,
    onShowCreateSheet: () -> Unit,
    onDismissCreateSheet: () -> Unit,
    onCreateClick: () -> Unit,
    onCompleteClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PetBackground)
    ) {
        if (uiState.isLoading) {
            RoutineLoading()
        } else {
            val events = uiState.eventsForSelectedDate()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    ScheduleHeader(
                        selectedDate = uiState.selectedDate,
                        week = uiState.selectedDate.weekDates(),
                        activePetName = uiState.activePet?.name,
                        onSelectedDateChanged = onSelectedDateChanged
                    )
                }

                uiState.errorMessage?.let { message ->
                    item {
                        MessageCard(message = message, isError = true, onRetryClick = onRetryClick)
                    }
                }

                uiState.successMessage?.let { message ->
                    item {
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            StatusChip(label = message, tone = ChipTone.Secondary)
                        }
                    }
                }

                item {
                    DaySummary(
                        date = uiState.selectedDate,
                        total = events.size,
                        routineCount = events.count { it is ScheduleEvent.Routine },
                        serviceCount = events.count { it is ScheduleEvent.Service }
                    )
                }

                if (events.isEmpty()) {
                    item {
                        EmptyDayCard(onShowCreateSheet = onShowCreateSheet)
                    }
                } else {
                    items(events, key = { it.id }) { event ->
                        when (event) {
                            is ScheduleEvent.Routine -> RoutineEventCard(
                                item = event.item,
                                isCompleted = routineOccurrenceKey(event.item.id, uiState.selectedDate) in uiState.completedRoutineOccurrences,
                                isCompleting = uiState.isCompleting,
                                isDeleting = uiState.isDeleting,
                                onCompleteClick = { onCompleteClick(event.item.id) },
                                onDeleteClick = { onDeleteClick(event.item.id) }
                            )

                            is ScheduleEvent.Service -> ServiceEventCard(request = event.request)
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = onShowCreateSheet,
                containerColor = PetPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
            }
        }

        if (uiState.isCreateSheetVisible) {
            ModalBottomSheet(onDismissRequest = onDismissCreateSheet) {
                CreateRoutineSheet(
                    uiState = uiState,
                    onTitleChanged = onTitleChanged,
                    onCategoryChanged = onCategoryChanged,
                    onScheduleTimeChanged = onScheduleTimeChanged,
                    onRepeatRuleChanged = onRepeatRuleChanged,
                    onNotesChanged = onNotesChanged,
                    onCreateClick = onCreateClick
                )
            }
        }
    }
}

@Composable
private fun RoutineLoading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ScheduleHeader(
    selectedDate: LocalDate,
    week: List<LocalDate>,
    activePetName: String?,
    onSelectedDateChanged: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
            .background(PetPrimary)
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Расписание",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = activePetName?.let { "Уход за $it" } ?: "Активный питомец не выбран",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Event, contentDescription = null, tint = Color.White)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "СЕГОДНЯ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.78f)
            )
            Text(
                text = selectedDate.fullRuDate(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            week.forEach { date ->
                WeekDayCell(
                    date = date,
                    selected = date == selectedDate,
                    onClick = { onSelectedDateChanged(date) }
                )
            }
        }
    }
}

@Composable
private fun WeekDayCell(
    date: LocalDate,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(42.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, ruLocale).uppercase(ruLocale).take(2),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.7f)
        )
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (selected) Color.White else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) PetPrimary else Color.White.copy(alpha = 0.86f)
            )
        }
    }
}

@Composable
private fun DaySummary(
    date: LocalDate,
    total: Int,
    routineCount: Int,
    serviceCount: Int
) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = date.dayTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (total == 0) "Свободный день" else "$total событий: уход $routineCount, услуги $serviceCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = PetTextSecondary
                )
            }
            StatusChip(
                label = if (total == 0) "Пусто" else "На день",
                tone = if (total == 0) ChipTone.Neutral else ChipTone.Info
            )
        }
    }
}

@Composable
private fun RoutineEventCard(
    item: RoutineItemResponse,
    isCompleted: Boolean,
    isCompleting: Boolean,
    isDeleting: Boolean,
    onCompleteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val category = routineCategories.firstOrNull { it.value == item.category } ?: routineCategories.last()
    val isMedicine = item.category == "medicine"
    val background = when {
        isCompleted -> PetSurfaceMuted
        isMedicine -> Color(0xFFFFF3F3)
        else -> PetSurface
    }
    val border = when {
        isMedicine && !isCompleted -> BorderStroke(1.dp, Color(0xFFFFCACA))
        else -> BorderStroke(1.dp, PetOutline)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = background,
        border = border,
        tonalElevation = if (isCompleted) 0.dp else 1.dp,
        shadowElevation = if (isCompleted) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButtonCircle(
                completed = isCompleted,
                alert = isMedicine,
                enabled = !isCompleting && item.is_enabled && !isCompleted,
                onClick = onCompleteClick
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip(label = category.label, tone = if (isMedicine) ChipTone.Primary else ChipTone.Secondary)
                    if (item.repeat_rule != "none") {
                        StatusChip(label = repeatLabel(item.repeat_rule), tone = ChipTone.Info)
                    }
                    if (isCompleted) {
                        StatusChip(label = "Выполнено", tone = ChipTone.Secondary)
                    }
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) PetTextSecondary else PetOnSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                )
                Text(
                    text = listOfNotNull(item.schedule_time?.toShortTime(), item.notes?.takeIf { it.isNotBlank() })
                        .joinToString(" • ")
                        .ifBlank { "Время не указано" },
                    style = MaterialTheme.typography.bodySmall,
                    color = PetTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!isCompleted) {
                        TextButton(
                            onClick = onCompleteClick,
                            enabled = !isCompleting && item.is_enabled
                        ) {
                            Text("Выполнено", color = PetPrimary)
                        }
                    }
                    TextButton(
                        onClick = onDeleteClick,
                        enabled = !isDeleting
                    ) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (item.is_enabled) PetSurfaceMuted else Color(0xFFE8E4DD))
            )
        }
    }
}

@Composable
private fun IconButtonCircle(
    completed: Boolean,
    alert: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val tint = when {
        completed -> PetSecondary
        alert -> PetError
        else -> PetTextSecondary
    }
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (completed) Icons.Rounded.Check else if (alert) Icons.Rounded.Medication else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ServiceEventCard(request: ServiceRequestResponse) {
    val isCompleted = request.status == "completed"
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (isCompleted) PetSurfaceMuted else PetSurface,
        border = BorderStroke(1.dp, PetOutline),
        tonalElevation = if (isCompleted) 0.dp else 1.dp,
        shadowElevation = if (isCompleted) 0.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(PetSecondaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.WorkOutline, contentDescription = null, tint = PetSecondary, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusChip(label = "Услуга", tone = ChipTone.Info)
                    StatusChip(label = serviceTypeLabel(request.service_type), tone = ChipTone.Neutral)
                    if (isCompleted) {
                        StatusChip(label = "Завершена", tone = ChipTone.Secondary)
                    }
                }
                Text(
                    text = request.service_title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) PetTextSecondary else PetOnSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                )
                Text(
                    text = "${request.requested_time.toShortTime()} • ${request.handler_name} • ${request.pet_name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PetTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyDayCard(onShowCreateSheet: () -> Unit) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(18.dp)
    ) {
        Text("На этот день ничего нет", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Добавьте задачу ухода или запишитесь на услугу, чтобы событие появилось в расписании.", color = PetTextSecondary)
        Button(
            onClick = onShowCreateSheet,
            colors = ButtonDefaults.buttonColors(containerColor = PetPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Добавить задачу")
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    isError: Boolean,
    onRetryClick: () -> Unit
) {
    ProductCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(message, color = if (isError) MaterialTheme.colorScheme.error else PetOnSurface)
        if (isError) {
            Button(onClick = onRetryClick) {
                Text("Повторить")
            }
        }
    }
}

@Composable
private fun CreateRoutineSheet(
    uiState: RoutineUiState,
    onTitleChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onScheduleTimeChanged: (String) -> Unit,
    onRepeatRuleChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onCreateClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 680.dp)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Новая задача", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
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
        }
        item {
            ChipSelector(
                title = "Тип задачи",
                options = routineCategories,
                selected = uiState.category,
                onSelected = onCategoryChanged
            )
        }
        item {
            OutlinedTextField(
                value = uiState.scheduleTime,
                onValueChange = onScheduleTimeChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Время") },
                placeholder = { Text("08:00") },
                singleLine = true,
                enabled = !uiState.isCreating,
                shape = RoundedCornerShape(16.dp)
            )
        }
        item {
            ChipSelector(
                title = "Повторяемость",
                options = repeatOptions,
                selected = uiState.repeatRule,
                onSelected = onRepeatRuleChanged
            )
        }
        item {
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Заметки") },
                enabled = !uiState.isCreating,
                minLines = 2,
                shape = RoundedCornerShape(16.dp)
            )
        }
        item {
            Button(
                onClick = onCreateClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isCreating,
                colors = ButtonDefaults.buttonColors(containerColor = PetPrimary)
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                } else {
                    Text("Создать задачу")
                }
            }
        }
    }
}

@Composable
private fun ChipSelector(
    title: String,
    options: List<Choice>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option.value == selected
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isSelected) PetPrimary else PetSurface,
                    border = if (isSelected) null else BorderStroke(1.dp, PetOutline),
                    modifier = Modifier.clickable { onSelected(option.value) }
                ) {
                    Text(
                        text = option.label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) Color.White else PetOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private sealed class ScheduleEvent(open val id: String, open val sortTime: String?) {
    data class Routine(val item: RoutineItemResponse) : ScheduleEvent("routine-${item.id}", item.schedule_time)
    data class Service(val request: ServiceRequestResponse) : ScheduleEvent("service-${request.id}", request.requested_time)
}

private data class Choice(val value: String, val label: String)

private fun RoutineUiState.eventsForSelectedDate(): List<ScheduleEvent> {
    val routineEvents = items
        .filter { it.isEnabledOn(selectedDate) }
        .map { ScheduleEvent.Routine(it) }
    val serviceEvents = serviceRequests
        .filter { it.pet_id == activePet?.id }
        .filter { it.requested_date == selectedDate.toString() }
        .map { ScheduleEvent.Service(it) }
    return (routineEvents + serviceEvents).sortedWith(
        compareBy<ScheduleEvent> { it.sortTime?.toShortTime().orEmpty().ifBlank { "99:99" } }
            .thenBy { it.id }
    )
}

private fun RoutineItemResponse.isEnabledOn(date: LocalDate): Boolean {
    if (!is_enabled) return false
    val createdDate = created_at.toLocalDateOrNull() ?: return true
    return when (repeat_rule) {
        "daily" -> !date.isBefore(createdDate)
        "weekly" -> !date.isBefore(createdDate) && date.dayOfWeek == createdDate.dayOfWeek
        "monthly" -> !date.isBefore(createdDate) && date.dayOfMonth == createdDate.dayOfMonth
        else -> date == createdDate
    }
}

private fun LocalDate.weekDates(): List<LocalDate> {
    val monday = minusDays((dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
    return (0L..6L).map { monday.plusDays(it) }
}

private fun LocalDate.fullRuDate(): String {
    val month = month.getDisplayName(TextStyle.FULL, ruLocale).replaceFirstChar { it.uppercase(ruLocale) }
    val day = dayOfWeek.getDisplayName(TextStyle.FULL, ruLocale).replaceFirstChar { it.uppercase(ruLocale) }
    return "$dayOfMonth $month, $day"
}

private fun LocalDate.dayTitle(): String {
    val month = month.getDisplayName(TextStyle.SHORT, ruLocale).replace(".", "")
    return "$dayOfMonth $month"
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        OffsetDateTime.parse(this).toLocalDate()
    } catch (_: DateTimeParseException) {
        runCatching { LocalDate.parse(take(10)) }.getOrNull()
    }
}

private fun String.toShortTime(): String {
    return take(5).ifBlank { this }
}

private fun repeatLabel(value: String): String {
    return repeatOptions.firstOrNull { it.value == value }?.label ?: "Не повторять"
}

private fun serviceTypeLabel(type: String): String {
    return when (type) {
        "walking" -> "Прогулки"
        "sitting" -> "Передержка"
        "training" -> "Тренировки"
        "grooming" -> "Груминг"
        else -> "Другое"
    }
}

private val ruLocale = Locale("ru", "RU")

private val routineCategories = listOf(
    Choice("walk", "Прогулка"),
    Choice("feeding", "Кормление"),
    Choice("medicine", "Лекарство"),
    Choice("grooming", "Груминг"),
    Choice("training", "Тренировка"),
    Choice("other", "Другое")
)

private val repeatOptions = listOf(
    Choice("none", "Не повторять"),
    Choice("daily", "Каждый день"),
    Choice("weekly", "Каждую неделю"),
    Choice("monthly", "Каждый месяц")
)
