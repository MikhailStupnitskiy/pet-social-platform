package com.example.petsocial.core.common.session

sealed interface SessionEvent {

    data object Unauthorized : SessionEvent
}