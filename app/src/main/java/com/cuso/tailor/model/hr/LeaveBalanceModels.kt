package com.cuso.tailor.model.hr

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Response wrapper for GET /api/hr/leave-balance/view-all
 */
data class LeaveBalanceListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<LeaveBalanceItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: LeaveBalancePaginationDto? = null
)

data class LeaveBalancePaginationDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: LeaveBalancePageInfoDto? = null,
    @SerializedName("totalPages") val totalPages: Int? = null,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

data class LeaveBalancePageInfoDto(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 100,
    @SerializedName("skip") val skip: Int = 0,
    @SerializedName("isPaginated") val isPaginated: Boolean = true
)

/**
 * Individual leave balance item supporting all backend field variations
 */
data class LeaveBalanceItemDto(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("id") val idField: String? = null,
    @SerializedName("organizationMemberId") val organizationMemberId: JsonElement? = null,
    @SerializedName("employeeName") val employeeName: String? = null,
    @SerializedName("employeeCode") val employeeCode: String? = null,
    @SerializedName("leaveTypeId") val leaveTypeId: JsonElement? = null,
    @SerializedName("leaveType") val leaveType: String? = null,
    @SerializedName("totalEntitlement") val totalEntitlement: Double? = null,
    @SerializedName("credited") val credited: Double? = null,
    @SerializedName("allocated") val allocated: Double? = null,
    @SerializedName("entitlement") val entitlement: Double? = null,
    @SerializedName("used") val used: Double? = null,
    @SerializedName("consumedDays") val consumedDays: Double? = null,
    @SerializedName("remaining") val remaining: Double? = null,
    @SerializedName("balance") val balance: Double? = null,
    @SerializedName("available") val available: Double? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("status") val status: String? = null
) {
    val resolvedId: String
        get() = _id ?: idField.orEmpty()
}

// ── Safe Extraction Helpers ──

fun LeaveBalanceItemDto.extractAllocatedDays(): Double {
    return totalEntitlement ?: credited ?: allocated ?: entitlement ?: 0.0
}

fun LeaveBalanceItemDto.extractUsedDays(): Double {
    return used ?: consumedDays ?: 0.0
}

fun LeaveBalanceItemDto.extractRemainingDays(): Double {
    return remaining ?: balance ?: available ?: (extractAllocatedDays() - extractUsedDays()).coerceAtLeast(0.0)
}

fun LeaveBalanceItemDto.extractLeaveTypeId(): String {
    val el = leaveTypeId ?: return ""
    return when {
        el.isJsonPrimitive -> el.asString
        el.isJsonObject -> el.asJsonObject.get("_id")?.asString
            ?: el.asJsonObject.get("id")?.asString.orEmpty()
        else -> ""
    }
}

fun LeaveBalanceItemDto.extractLeaveTypeName(leaveTypesMap: Map<String, String> = emptyMap()): String {
    val el = leaveTypeId
    if (el != null && el.isJsonObject) {
        val obj = el.asJsonObject
        val name = obj.get("name")?.takeIf { !it.isJsonNull }?.asString
            ?: obj.get("leaveType")?.takeIf { !it.isJsonNull }?.asString
            ?: obj.get("title")?.takeIf { !it.isJsonNull }?.asString
        if (!name.isNullOrBlank()) return name
    }
    val typeId = extractLeaveTypeId()
    return leaveTypesMap[typeId] ?: leaveType?.takeIf { it.isNotBlank() } ?: "Annual Leave"
}

fun LeaveBalanceItemDto.extractEmployeeName(): String {
    val el = organizationMemberId
    if (el != null && el.isJsonObject) {
        val obj = el.asJsonObject
        val first = obj.get("firstName")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
        val last = obj.get("lastName")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
        val full = "$first $last".trim()
        if (full.isNotBlank()) return full

        val alt = obj.get("fullName")?.takeIf { !it.isJsonNull }?.asString
            ?: obj.get("name")?.takeIf { !it.isJsonNull }?.asString
        if (!alt.isNullOrBlank()) return alt
    }
    return employeeName?.takeIf { it.isNotBlank() } ?: "Employee"
}

fun LeaveBalanceItemDto.extractEmployeeCode(): String {
    val el = organizationMemberId
    if (el != null && el.isJsonObject) {
        val obj = el.asJsonObject
        val code = obj.get("employeeCode")?.takeIf { !it.isJsonNull }?.asString
            ?: obj.get("memberId")?.takeIf { !it.isJsonNull }?.asString
        if (!code.isNullOrBlank()) return code
    }
    return employeeCode.orEmpty()
}

// ── Dashboard Computed UI State Models ──

data class LeaveDashboardMetrics(
    val totalLeaves: Int = 0,
    val approvedCount: Int = 0,
    val pendingCount: Int = 0,
    val rejectedCount: Int = 0
)

data class LeaveUtilizationUiItem(
    val name: String,
    val used: Int,
    val total: Int,
    val remaining: Int,
    val progress: Float
)

data class MonthlyUsageTrendUiItem(
    val monthLabel: String,
    val count: Int,
    val heightFraction: Float,
    val isCurrentMonth: Boolean
)