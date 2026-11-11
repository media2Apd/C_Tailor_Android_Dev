package com.cuso.tailor.model.security

import com.google.gson.annotations.SerializedName

// --- Generic Wrappers ---
data class MobileDeviceListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<MobileDeviceItem> = emptyList(),
    @SerializedName("pagination") val pagination: PaginationData? = null
)

data class MobileAuthPolicyResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: MobileAuthPolicyData? = null
)

data class MobileDataSecurityPolicyResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: MobileDataSecurityPolicyData? = null
)

data class MobileSecurityAuditListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<MobileSecurityAuditItem> = emptyList(),
    @SerializedName("pagination") val pagination: PaginationData? = null
)

data class PaginationData(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1
)

// --- Device Management Model ---
data class MobileDeviceItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("userId") val userId: DeviceUserSummary? = null,
    @SerializedName("deviceName") val deviceName: String = "",
    @SerializedName("platform") val platform: String = "",
    @SerializedName("osVersion") val osVersion: String = "",
    @SerializedName("appVersion") val appVersion: String = "",
    @SerializedName("deviceIdLast4") val deviceIdLast4: String? = null,
    @SerializedName("approvalStatus") val approvalStatus: String = "pending", // approved, pending, blocked
    @SerializedName("trustStatus") val trustStatus: String = "trusted",     // trusted, untrusted
    @SerializedName("isJailbrokenOrRooted") val isJailbrokenOrRooted: Boolean = false,
    @SerializedName("mdmStatus") val mdmStatus: String = "unmanaged",
    @SerializedName("lastActiveAt") val lastActiveAt: String? = null,
    @SerializedName("lastLoginAt") val lastLoginAt: String? = null,
    @SerializedName("lastKnownIp") val lastKnownIp: String? = null,
    @SerializedName("registeredAt") val registeredAt: String? = null,
    @SerializedName("maskedDeviceId") val maskedDeviceId: String? = null
)

data class DeviceUserSummary(
    @SerializedName("_id") val id: String = "",
    @SerializedName("role") val role: String = "",
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = ""
) {
    val fullName: String get() = "$firstName $lastName".trim()
}

// --- Mobile Auth Policy Models ---
data class MobileAuthPolicyData(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("password") val password: PasswordPolicy = PasswordPolicy(),
    @SerializedName("otp") val otp: OtpPolicy = OtpPolicy(),
    @SerializedName("twoFA") val twoFA: TwoFAPolicy = TwoFAPolicy(),
    @SerializedName("biometric") val biometric: BiometricPolicy = BiometricPolicy()
)

data class PasswordPolicy(
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("minStrength") val minStrength: String = "strong",
    @SerializedName("minLength") val minLength: Int = 8,
    @SerializedName("expiryDays") val expiryDays: Int = 90,
    @SerializedName("failedAttemptLimit") val failedAttemptLimit: Int = 5,
    @SerializedName("lockDurationMinutes") val lockDurationMinutes: Int = 15
)

data class OtpPolicy(
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("channel") val channel: String = "sms_email",
    @SerializedName("expirySeconds") val expirySeconds: Int = 60,
    @SerializedName("maxAttempts") val maxAttempts: Int = 5,
    @SerializedName("resendLimit") val resendLimit: Int = 3,
    @SerializedName("resendCooldownSeconds") val resendCooldownSeconds: Int = 30
)

data class TwoFAPolicy(
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("methods") val methods: List<String> = listOf("otp"),
    @SerializedName("enforcement") val enforcement: String = "optional",
    @SerializedName("requireForSensitiveOps") val requireForSensitiveOps: Boolean = true
)

data class BiometricPolicy(
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("supportedMethods") val supportedMethods: List<String> = listOf("fingerprint", "face", "device_biometrics"),
    @SerializedName("requirePasscodeBackup") val requirePasscodeBackup: Boolean = true
)

// --- Mobile Data Security Models ---
data class MobileDataSecurityPolicyData(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("screenshotRestriction") val screenshotRestriction: ScreenshotRestriction = ScreenshotRestriction(),
    @SerializedName("downloadRestriction") val downloadRestriction: DownloadRestriction = DownloadRestriction(),
    @SerializedName("localEncryption") val localEncryption: LocalEncryption = LocalEncryption()
)

data class ScreenshotRestriction(
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("protectedContexts") val protectedContexts: List<String> = listOf(
        "Measurement Data Views",
        "Customer Photos & Profiles",
        "Payment Processing Screens"
    )
)

data class DownloadRestriction(
    @SerializedName("level") val level: String = "restrict_all"
)

data class LocalEncryption(
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("algorithm") val algorithm: String = "AES-256"
)

// --- Security Audit Log Model ---
data class MobileSecurityAuditItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("eventType") val eventType: String = "",
    @SerializedName("severity") val severity: String = "low", // low, medium, high
    @SerializedName("userId") val userId: DeviceUserSummary? = null,
    @SerializedName("userEmail") val userEmail: String? = null,
    @SerializedName("userName") val userName: String? = null,
    @SerializedName("deviceId") val deviceId: String? = null,
    @SerializedName("deviceModel") val deviceModel: String? = null,
    @SerializedName("osInfo") val osInfo: String? = null,
    @SerializedName("ipAddress") val ipAddress: String? = null,
    @SerializedName("mdmStatus") val mdmStatus: String? = null,
    @SerializedName("detailsSummary") val detailsSummary: String? = null,
    @SerializedName("status") val status: String = "unresolved",
    @SerializedName("timestamp") val timestamp: String? = null
)