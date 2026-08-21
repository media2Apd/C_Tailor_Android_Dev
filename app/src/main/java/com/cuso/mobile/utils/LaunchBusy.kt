package com.cuso.mobile.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun ViewModel.launchBusy(block: suspend () -> Unit): Job {
    return viewModelScope.launch {
        AppLoadingManager.start()
        try {
            block()
        } finally {
            AppLoadingManager.stop()
        }
    }
}