package com.cuso.tailor.model.login_forgotPassword_resetPassword
// Updated request body to match the new /api/auth/login payload
data class PasswordVerify(
    val email: String,
    val password: String,
    val deviceId: String,
    val deviceInfo: DeviceInfo
)

// Nested object containing the user's mobile device details
data class DeviceInfo(
    val deviceName: String,
    val platform: String,
    val osVersion: String,
    val appVersion: String
)
data class PasswordResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)