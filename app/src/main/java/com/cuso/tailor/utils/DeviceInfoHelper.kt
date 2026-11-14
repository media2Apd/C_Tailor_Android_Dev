package com.cuso.tailor.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import com.cuso.tailor.model.login_forgotPassword_resetPassword.DeviceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceInfoHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Returns a unique ID for the app installation on this device (e.g., "9774d56d682e549c")
    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
    }

    // Builds the DeviceInfo object from system properties
    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceName = getDeviceName(),
            platform = getPlatform(),                       // Dynamically fetched (e.g., "Android")
            osVersion = Build.VERSION.RELEASE ?: "unknown", // e.g., "14"
            appVersion = getAppVersion()                    // e.g., "1.0.4"
        )
    }

    // Dynamically extracts the OS platform from the system http.agent without hardcoding
    private fun getPlatform(): String {
        val httpAgent = System.getProperty("http.agent").orEmpty()
        // Example httpAgent: "Dalvik/2.1.0 (Linux; U; Android 14; SM-S911B Build/...)"
        val insideParens = httpAgent.substringAfter("(", "").substringBefore(")", "")

        if (insideParens.isNotBlank()) {
            val tokens = insideParens.split(";").map { it.trim() }
            // Skip the kernel name (first token, e.g., "Linux") and single-letter flags (e.g., "U")
            val osToken = tokens.drop(1).firstOrNull { it.length > 1 }
            if (!osToken.isNullOrBlank()) {
                // Take only the OS name before the version number (e.g., "Android 14" -> "Android")
                return osToken.substringBefore(" ")
            }
        }

        // Fallback to JVM OS name property if http.agent is unavailable
        return System.getProperty("os.name") ?: "unknown"
    }

    // Combines manufacturer and model without duplicating the brand name
    private fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.trim()
        val model = Build.MODEL.trim()
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model.replaceFirstChar { it.uppercase() }
        } else {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        }
    }

    // Fetches the app's versionName from PackageManager safely across all Android versions
    private fun getAppVersion(): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "unknown"
        } catch (_: Exception) {
            "-"
        }
    }
}