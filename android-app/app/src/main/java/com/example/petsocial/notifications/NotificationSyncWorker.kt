package com.example.petsocial.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.petsocial.R
import com.example.petsocial.core.network.api.NotificationsApi
import java.util.concurrent.TimeUnit

class NotificationSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        PetNotificationChannels.ensure(applicationContext)
        val api = workerFactoryNotificationsApi ?: return Result.success()

        return try {
            val items = api.getNotifications(limit = 20, unreadOnly = true)
                .filter { it.type == "match_created" }
            val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val shown = prefs.getStringSet(SHOWN_KEYS, emptySet()).orEmpty().toMutableSet()
            val newlyShown = mutableSetOf<String>()
            items.forEach { notification ->
                if (notification.id !in shown && showMatchNotification(notification.id, notification.title, notification.body)) {
                    newlyShown += notification.id
                }
            }
            if (newlyShown.isNotEmpty()) {
                prefs.edit().putStringSet(SHOWN_KEYS, shown + newlyShown).apply()
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun showMatchNotification(id: String, title: String, body: String): Boolean {
        if (!canPostNotifications()) return false
        val notification = NotificationCompat.Builder(applicationContext, PetNotificationChannels.MATCHES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        return try {
            NotificationManagerCompat.from(applicationContext).notify(id.hashCode(), notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val PREFS_NAME = "notification_sync_preferences"
        private const val SHOWN_KEYS = "shown_match_notification_ids"
        private const val WORK_NAME = "notification_sync_worker"

        @Volatile private var workerFactoryNotificationsApi: NotificationsApi? = null

        fun configure(notificationsApi: NotificationsApi) {
            workerFactoryNotificationsApi = notificationsApi
        }

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<NotificationSyncWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}