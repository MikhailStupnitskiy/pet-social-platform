package com.example.petsocial

import android.app.Application
import com.example.petsocial.core.network.api.NotificationsApi
import com.example.petsocial.notifications.NotificationSyncWorker
import com.example.petsocial.notifications.PetNotificationChannels
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PetSocialApp : Application() {

    @Inject lateinit var notificationsApi: NotificationsApi

    override fun onCreate() {
        super.onCreate()
        PetNotificationChannels.ensure(this)
        NotificationSyncWorker.configure(notificationsApi)
        NotificationSyncWorker.enqueue(this)
    }
}