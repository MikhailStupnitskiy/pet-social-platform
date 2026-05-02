package com.example.petsocial.core.common.session

import kotlinx.coroutines.flow.SharedFlow

interface SessionEventBus {

    val events: SharedFlow<SessionEvent>

    fun notifyUnauthorized()
}