package com.example.petsocial.feature.routine.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.petsocial.core.network.model.handlers.ServiceRequestResponse
import com.example.petsocial.core.network.model.routine.RoutineItemResponse
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

class ReminderScheduler(
    private val context: Context
) {
    fun schedule(items: List<RoutineItemResponse>, requests: List<ServiceRequestResponse>) {
        val manager = WorkManager.getInstance(context)
        val now = LocalDateTime.now()
        items.filter { it.is_enabled && !it.schedule_time.isNullOrBlank() }
            .forEach { item ->
                item.nextOccurrence(now)?.let { occurrence ->
                    enqueue(manager, "routine-${item.id}", item.title, "Скоро начнется задача ухода", occurrence)
                }
            }
        requests.filter { it.status == "accepted" }
            .forEach { request ->
                request.startDateTime()?.let { start ->
                    if (start.isAfter(now)) {
                        enqueue(manager, "service-${request.id}", request.service_title, "Услуга начнется через 15 минут", start)
                    }
                }
            }
    }

    private fun enqueue(manager: WorkManager, key: String, title: String, body: String, start: LocalDateTime) {
        val notifyAt = start.minusMinutes(15)
        val delayMillis = Duration.between(LocalDateTime.now(), notifyAt).toMillis()
        if (delayMillis <= 0) return
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("title" to title, "body" to body, "key" to key))
            .build()
        manager.enqueueUniqueWork("reminder-$key", ExistingWorkPolicy.REPLACE, request)
    }
}

private fun RoutineItemResponse.nextOccurrence(now: LocalDateTime): LocalDateTime? {
    val time = schedule_time?.toLocalTimeOrNull() ?: return null
    val createdDate = created_at.toLocalDateOrNull() ?: now.toLocalDate()
    var date = when (repeat_rule) {
        "daily" -> maxOf(createdDate, now.toLocalDate())
        "weekly" -> nextWeeklyDate(createdDate, now.toLocalDate())
        "monthly" -> nextMonthlyDate(createdDate, now.toLocalDate())
        else -> createdDate
    }
    var occurrence = LocalDateTime.of(date, time)
    if (!occurrence.isAfter(now)) {
        date = when (repeat_rule) {
            "daily" -> date.plusDays(1)
            "weekly" -> date.plusWeeks(1)
            "monthly" -> date.plusMonths(1)
            else -> return null
        }
        occurrence = LocalDateTime.of(date, time)
    }
    return occurrence
}

private fun nextWeeklyDate(created: LocalDate, current: LocalDate): LocalDate {
    var date = if (current.isAfter(created)) current else created
    while (date.dayOfWeek != created.dayOfWeek) date = date.plusDays(1)
    return date
}

private fun nextMonthlyDate(created: LocalDate, current: LocalDate): LocalDate {
    var date = LocalDate.of(current.year, current.month, minOf(created.dayOfMonth, current.lengthOfMonth()))
    if (date.isBefore(created)) date = created
    if (date.isBefore(current)) {
        val next = current.plusMonths(1)
        date = LocalDate.of(next.year, next.month, minOf(created.dayOfMonth, next.lengthOfMonth()))
    }
    return date
}

private fun ServiceRequestResponse.startDateTime(): LocalDateTime? {
    return try {
        val time = requested_time.toLocalTimeOrNull() ?: return null
        LocalDateTime.of(LocalDate.parse(requested_date), time)
    } catch (_: Exception) {
        null
    }
}

private fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(take(5)) }.getOrNull()

private fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        OffsetDateTime.parse(this).toLocalDate()
    } catch (_: DateTimeParseException) {
        runCatching { LocalDate.parse(take(10)) }.getOrNull()
    }
}