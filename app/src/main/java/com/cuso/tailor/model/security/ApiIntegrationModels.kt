@file:Suppress("unused")

package com.cuso.tailor.model.security

import com.google.gson.annotations.SerializedName

data class RateLimitDto(
    @SerializedName("total")
    val total: Int = 10000,
    @SerializedName("used")
    val used: Int = 0
)

data class PermissionScopeItemDto(
    @SerializedName("resource")
    val resource: String = "",
    @SerializedName("read")
    val read: Boolean = false,
    @SerializedName("create")
    val create: Boolean = false,
    @SerializedName("update")
    val update: Boolean = false,
    @SerializedName("delete")
    val delete: Boolean = false
)

data class IntegrationUserRefDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("firstName")
    val firstName: String = "",
    @SerializedName("lastName")
    val lastName: String = ""
)

data class ApiIntegrationItemDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("category")
    val category: String = "REST API",
    @SerializedName("authType")
    val authType: String = "API Key",
    @SerializedName("baseUrl")
    val baseUrl: String = "",
    @SerializedName("webhookUrl")
    val webhookUrl: String? = "",
    @SerializedName("events")
    val events: List<String> = emptyList(),
    @SerializedName("permissionsScope")
    val permissionsScope: List<PermissionScopeItemDto> = emptyList(),
    @SerializedName("status")
    val status: String = "Connected",
    @SerializedName("lastSync")
    val lastSync: String? = null,
    @SerializedName("latencyMs")
    val latencyMs: Int = 0,
    @SerializedName("deliverySuccessRate")
    val deliverySuccessRate: Int = 100,
    @SerializedName("rateLimit")
    val rateLimit: RateLimitDto = RateLimitDto(),
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

// Pagination wrapper for API Integration list
data class ApiIntegrationPaginationDto(
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("limit")
    val limit: Int = 10,
    @SerializedName("totalPages")
    val totalPages: Int = 1
)

// API Request & Response payloads
data class CreateApiIntegrationRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("category")
    val category: String = "REST API",
    @SerializedName("authType")
    val authType: String = "API Key",
    @SerializedName("baseUrl")
    val baseUrl: String,
    @SerializedName("permissionsScope")
    val permissionsScope: List<PermissionScopeItemDto>
)

data class ApiIntegrationListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<ApiIntegrationItemDto> = emptyList(),
    @SerializedName("pagination")
    val pagination: ApiIntegrationPaginationDto? = null
)

data class ApiIntegrationDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: ApiIntegrationItemDto? = null
)

data class CreateApiIntegrationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: ApiIntegrationItemDto? = null
)

data class ToggleApiIntegrationStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: ApiIntegrationItemDto? = null
)

data class UpdateApiIntegrationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: ApiIntegrationItemDto? = null
)

data class RevokeApiIntegrationResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: ApiIntegrationItemDto? = null
)

data class RevealedSecretDataDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("apiKey")
    val apiKey: String? = null,
    @SerializedName("secretKey")
    val secretKey: String? = null,
    @SerializedName("webhookSecret")
    val webhookSecret: String? = null
)

data class RevealSecretApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: RevealedSecretDataDto? = null
)