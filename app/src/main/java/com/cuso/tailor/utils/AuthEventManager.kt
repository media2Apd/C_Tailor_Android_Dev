package com.cuso.tailor.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthEventManager {
    private val _sessionExpiredEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val sessionExpiredEvent = _sessionExpiredEvent.asSharedFlow()

    fun onSessionExpired(message: String = "Session expired. Please log in again.") {
        _sessionExpiredEvent.tryEmit(message)
    }
    fun emitSessionExpired(msg: String) { _sessionExpiredEvent.tryEmit(msg) }
}