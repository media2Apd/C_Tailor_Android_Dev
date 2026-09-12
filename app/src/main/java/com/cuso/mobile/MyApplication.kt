package com.cuso.mobile

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        // Global message variable read by Compose UI
        var dynamicIslandMessage by mutableStateOf<String?>(null)
    }

    override fun onCreate() {
        super.onCreate()

        // Global Uncaught Exception Handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("GlobalCrashHandler", "Prevented crash on thread: ${thread.name}", throwable)

            // Trigger Dynamic Island message directly from MyApplication
            Handler(Looper.getMainLooper()).post {
                dynamicIslandMessage = "Data parsing issue prevented. Please try again."
            }
        }
    }
}