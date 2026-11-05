package com.cuso.tailor.model.it

import com.google.gson.annotations.SerializedName

// --- Courier Config Status ---
data class CourierConfigData(
    @SerializedName("shiprocketEnabled")
    val shiprocketEnabled: Boolean = false,
    @SerializedName("apiEmail")
    val apiEmail: String? = null,
    @SerializedName("pickupLocationName")
    val pickupLocationName: String? = null
)

data class CourierConfigResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("data")
    val data: CourierConfigData? = null
)

// --- External APIs ---
data class ExternalApiItemDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("category")
    val category: String = "Custom API",
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("baseUrl")
    val baseUrl: String = "",
    @SerializedName("testPath")
    val testPath: String? = "/",
    @SerializedName("authType")
    val authType: String = "API_KEY",
    @SerializedName("headerName")
    val headerName: String? = "X-API-Key",
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("lastTestedAt")
    val lastTestedAt: String? = null,
    @SerializedName("lastError")
    val lastError: String? = null,
    @SerializedName("hasCredentials")
    val hasCredentials: Boolean = false
)

data class ExternalApisResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("data")
    val data: List<ExternalApiItemDto> = emptyList()
)

// --- API Credentials ---
data class ApiCredentialItemDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("category")
    val category: String = "REST API",
    @SerializedName("authType")
    val authType: String = "API Key",
    @SerializedName("baseUrl")
    val baseUrl: String? = null,
    @SerializedName("webhookUrl")
    val webhookUrl: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("lastSync")
    val lastSync: String? = null
)

data class ApiCredentialsResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<ApiCredentialItemDto> = emptyList()
)

// --- Razorpay Integration ---
data class RazorpayIntegrationData(
    @SerializedName("provider")
    val provider: String? = "RAZORPAY",
    @SerializedName("isConfigured")
    val isConfigured: Boolean = false,
    @SerializedName("isEnabled")
    val isEnabled: Boolean = false,
    @SerializedName("status")
    val status: String? = "INACTIVE",
    @SerializedName("environment")
    val environment: String? = null,
    @SerializedName("keyIdHint")
    val keyIdHint: String? = null,
    @SerializedName("lastTestedAt")
    val lastTestedAt: String? = null,
    @SerializedName("lastError")
    val lastError: String? = null
)

data class RazorpayIntegrationResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("data")
    val data: RazorpayIntegrationData? = null
)

// --- Unified Integration Model for List Display ---
enum class IntegrationKind {
    AD,
    COURIER,
    PLANNED,
    RAZORPAY,
    EXTERNAL_API,
    API_CREDENTIAL,
    PLATFORM_SERVICE
}

data class UnifiedIntegrationItem(
    val id: String,
    val kind: IntegrationKind,
    val providerId: String,
    val name: String,
    val category: String,
    val description: String,
    val status: String,
    val accountId: String,
    val lastSync: String?,
    val syncStatus: String? = null,
    val externalConnection: ExternalApiItemDto? = null,
    val apiCredentialConnection: ApiCredentialItemDto? = null
)