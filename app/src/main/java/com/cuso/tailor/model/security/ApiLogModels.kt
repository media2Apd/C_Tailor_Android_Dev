@file:Suppress("unused")

package com.cuso.tailor.model.security

import com.google.gson.annotations.SerializedName

data class ApiLogIntegrationRefDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("category")
    val category: String = ""
)

data class ApiLogMemberRefDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("firstName")
    val firstName: String = "",
    @SerializedName("lastName")
    val lastName: String = ""
)

data class ApiLogItemDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("integrationId")
    val integration: ApiLogIntegrationRefDto? = null,
    @SerializedName("logId")
    val logId: String = "",
    @SerializedName("method")
    val method: String = "GET",
    @SerializedName("endpoint")
    val endpoint: String = "",
    @SerializedName("statusCode")
    val statusCode: Int = 200,
    @SerializedName("statusGroup")
    val statusGroup: String = "success",
    @SerializedName("responseTimeMs")
    val responseTimeMs: Long = 0,
    @SerializedName("ipAddress")
    val ipAddress: String = "",
    @SerializedName("userAgent")
    val userAgent: String = "",
    @SerializedName("errorMessage")
    val errorMessage: String? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    val isSuccess: Boolean
        get() = statusGroup.equals("success", ignoreCase = true) || (statusCode in 200..299)

    val displayStatus: String
        get() = if (isSuccess) "Successful" else "Failed"

    // Extracts short endpoint path (e.g., "/orders", "/inventory", or path from full URL)
    val shortEndpoint: String
        get() = try {
            val path = if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
                java.net.URI(endpoint).path
            } else {
                endpoint
            }
            if (path.isNullOrBlank() || path == "/") "/orders" else path
        } catch (_: Exception) {
            "/orders"
        }
}

data class ApiLogPaginationDto(
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("limit")
    val limit: Int = 10,
    @SerializedName("totalPages")
    val totalPages: Int = 1
)

data class ApiLogListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<ApiLogItemDto> = emptyList(),
    @SerializedName("pagination")
    val pagination: ApiLogPaginationDto? = null
)

data class ApiLogDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: ApiLogItemDto? = null
)