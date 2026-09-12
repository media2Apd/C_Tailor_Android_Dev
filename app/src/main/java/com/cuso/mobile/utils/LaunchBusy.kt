package com.cuso.mobile.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Global safe coroutine launcher for all ViewModels.
 * Intercepts uncaught runtime exceptions and prevents app process termination.
 */
fun ViewModel.launchBusy(
    onError: (Throwable) -> Unit = {},
    block: suspend CoroutineScope.() -> Unit
): Job {
    // Global exception interceptor for ViewModel coroutines
    val safeExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        onError(throwable)
    }

    return viewModelScope.launch(safeExceptionHandler) {
        block()
    }
}