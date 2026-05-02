package com.example.petsocial

import androidx.lifecycle.ViewModel
import com.example.petsocial.core.common.session.SessionEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionEventsViewModel @Inject constructor(
    val sessionEventBus: SessionEventBus
) : ViewModel()