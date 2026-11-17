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
    val success: Boolean = false,
    val message: String = "",
    val data: LoginData? = null,
    val requiresTwoFA: Boolean = false,
    val method: String? = null,
    val challengeToken: String? = null,
    val deviceId: String? = null
)

data class VerifyTwoFARequest(
    val challengeToken: String,
    val code: String
)

data class VerifyTwoFAResponse(
    val success: Boolean = false,
    val message: String = "",
    val refreshToken: String? = null,
    val data: LoginData? = null
)