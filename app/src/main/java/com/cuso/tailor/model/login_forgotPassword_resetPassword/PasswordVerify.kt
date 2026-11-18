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
    val mustChangePassword: Boolean = false,
    val workspaceLocked: Boolean = false,
    val method: String? = null,
    val challengeToken: String? = null,
    val deviceId: String? = null
)

// Request payload for updating a temporary password
data class ChangeTemporaryPasswordRequest(
    val temporaryPassword: String,
    val newPassword: String,
    val confirmPassword: String,
    val challengeToken: String? = null
)

// Response payload returned after updating the temporary password
data class ChangeTemporaryPasswordResponse(
    val success: Boolean = false,
    val message: String = "",
    val requiresTwoFA: Boolean = false,
    val workspaceLocked: Boolean = false,
    val method: String? = null,
    val challengeToken: String? = null,
    val data: LoginData? = null
)

data class VerifyTwoFARequest(
    val challengeToken: String,
    val code: String
)

data class VerifyTwoFAResponse(
    val success: Boolean = false,
    val message: String = "",
    val refreshToken: String? = null,
    val mustChangePassword: Boolean = false,
    val workspaceLocked: Boolean = false,
    val challengeToken: String? = null,
    val data: LoginData? = null
)