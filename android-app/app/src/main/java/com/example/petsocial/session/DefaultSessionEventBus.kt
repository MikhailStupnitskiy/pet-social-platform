package com.example.petsocial.session

import com.example.petsocial.core.common.session.SessionEvent
import com.example.petsocial.core.common.session.SessionEventBus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSessionEventBus @Inject constructor() : SessionEventBus {

    private val _events = MutableSharedFlow<SessionEvent>(
        extraBufferCapacity = 1
    )

    override val events: SharedFlow<SessionEvent> = _events

    override fun notifyUnauthorized() {
        _events.tryEmit(SessionEvent.Unauthorized)
    }
}