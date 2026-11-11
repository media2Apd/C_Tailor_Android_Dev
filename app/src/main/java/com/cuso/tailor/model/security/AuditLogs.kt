@file:Suppress("unused")

package com.cuso.tailor.model.security

import com.google.gson.annotations.SerializedName

data class AuditLogMemberDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("memberId")
    val memberId: String = "",
    @SerializedName("name")
    val name: String = ""
)

data class AuditLogItemDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("memberId")
    val member: AuditLogMemberDto? = null,
    @SerializedName("module")
    val module: String = "",
    @SerializedName("action")
    val action: String = "",
    @SerializedName("recordId")
    val recordId: String? = null,
    @SerializedName("recordName")
    val recordName: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("ipAddress")
    val ipAddress: String = "",
    @SerializedName("userAgent")
    val userAgent: String = "",
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    val displayActionBadgeColorPair: Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color>
        get() = when (action.uppercase()) {
            "CREATE" -> Pair(com.cuso.tailor.ui.theme.greentext, com.cuso.tailor.ui.theme.greenBg)
            "UPDATE" -> Pair(com.cuso.tailor.ui.theme.Primary, com.cuso.tailor.ui.theme.primary_light)
            "DELETE" -> Pair(com.cuso.tailor.ui.theme.redText, com.cuso.tailor.ui.theme.redBg)
            "REVEAL_CREDENTIALS" -> Pair(com.cuso.tailor.ui.theme.yellowText, com.cuso.tailor.ui.theme.yellowBg)
            else -> Pair(com.cuso.tailor.ui.theme.TextPrimary, com.cuso.tailor.ui.theme.light_grey)
        }
}

data class AuditLogPaginationDto(
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("limit")
    val limit: Int = 10,
    @SerializedName("totalPages")
    val totalPages: Int = 1,
    @SerializedName("hasNextPage")
    val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage")
    val hasPreviousPage: Boolean = false
)

data class AuditLogDataWrapper(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: List<AuditLogItemDto> = emptyList(),
    @SerializedName("pagination")
    val pagination: AuditLogPaginationDto? = null
)

data class AuditLogListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: AuditLogDataWrapper? = null
)

data class AuditLogDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: AuditLogItemDto? = null
)