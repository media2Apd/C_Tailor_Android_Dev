package com.cuso.mobile.utils

// AppLoadingManager.kt

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppLoadingManager {
    private val _activeCallCount = MutableStateFlow(0)
    val isBusy: StateFlow<Boolean> = MutableStateFlow(false).apply {
    }.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val busyState: StateFlow<Boolean> = _isBusy.asStateFlow()

    @Synchronized
    fun start() {
        _activeCallCount.value += 1
        _isBusy.value = true
    }

    @Synchronized
    fun stop() {
        _activeCallCount.value = (_activeCallCount.value - 1).coerceAtLeast(0)
        _isBusy.value = _activeCallCount.value > 0
    }
}