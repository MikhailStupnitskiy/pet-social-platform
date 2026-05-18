package com.example.petsocial.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService

object PetNotificationChannels {
    const val MATCHES = "matches"
    const val REMINDERS = "reminders"

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService<NotificationManager>() ?: return
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(MATCHES, "Мэтчи", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(REMINDERS, "Напоминания", NotificationManager.IMPORTANCE_HIGH)
            )
        )
    }
}