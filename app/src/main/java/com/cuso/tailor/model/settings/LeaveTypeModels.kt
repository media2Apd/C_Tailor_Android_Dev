@file:Suppress("unused", "AssignedValueIsNeverRead", "UnusedVariable")
package com.cuso.tailor.model.settings

import com.google.gson.annotations.SerializedName

data class LeaveTypeApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("pagination") val pagination: LeaveTypePagination? = null
)

data class LeaveTypeListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<LeaveTypeItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: LeaveTypePagination? = null
)

data class LeaveTypeViewOneResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: LeaveTypeItemDto? = null
)

data class LeaveTypeActionResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: LeaveTypeItemDto? = null
)

data class LeaveTypeDeleteResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null
)

data class LeaveTypePagination(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

data class LeaveTypeItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("leaveTypeName") val leaveTypeName: String = "",
    @SerializedName("shortCode") val shortCode: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("compensationType") val compensationType: String = "paid",
    @SerializedName("category") val category: String = "Regular Leave",
    @SerializedName("colorCode") val colorCode: String = "#6366F1",
    @SerializedName("effectiveFrom") val effectiveFrom: String? = null,
    @SerializedName("effectiveUntil") val effectiveUntil: String? = null,
    @SerializedName("isOpenEnded") val isOpenEnded: Boolean = true,
    @SerializedName("version") val version: String = "v1.0",
    @SerializedName("status") val status: String = "active",
    @SerializedName("allocationMethod") val allocationMethod: String = "fixed_annual",
    @SerializedName("daysPerYear") val daysPerYear: Int = 0,
    @SerializedName("leaveYearCycle") val leaveYearCycle: String = "calendar_year",
    @SerializedName("maxAccumulatedBalance") val maxAccumulatedBalance: Int = 0,
    @SerializedName("prorateForMidYearJoiners") val prorateForMidYearJoiners: Boolean = false,
    @SerializedName("eligibility") val eligibility: LeaveEligibilityDto = LeaveEligibilityDto(),
    @SerializedName("usageRules") val usageRules: LeaveUsageRulesDto = LeaveUsageRulesDto(),
    @SerializedName("carryForward") val carryForward: LeaveCarryForwardDto = LeaveCarryForwardDto(),
    @SerializedName("encashment") val encashment: LeaveEncashmentDto = LeaveEncashmentDto(),
    @SerializedName("approval") val approval: LeaveApprovalDto = LeaveApprovalDto(),
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class LeaveEligibilityDto(
    @SerializedName("audienceScope") val audienceScope: String = "all_employees",
    @SerializedName("employmentClassifications") val employmentClassifications: List<String> = emptyList(),
    @SerializedName("allDepartments") val allDepartments: Boolean = true,
    @SerializedName("departments") val departments: List<String> = emptyList(),
    @SerializedName("locations") val locations: List<String> = emptyList(),
    @SerializedName("availableDuringProbation") val availableDuringProbation: Boolean = false,
    @SerializedName("minServiceRequirement") val minServiceRequirement: MinServiceRequirementDto = MinServiceRequirementDto()
)

data class MinServiceRequirementDto(
    @SerializedName("value") val value: Int = 0,
    @SerializedName("unit") val unit: String = "months"
)

data class LeaveUsageRulesDto(
    @SerializedName("allowHalfDay") val allowHalfDay: Boolean = true,
    @SerializedName("allowHourly") val allowHourly: Boolean = false,
    @SerializedName("minimumDuration") val minimumDuration: Double = 0.5,
    @SerializedName("maxConsecutiveDays") val maxConsecutiveDays: Int = 0,
    @SerializedName("advanceNoticeDays") val advanceNoticeDays: Int = 0,
    @SerializedName("allowEmergencyWaiver") val allowEmergencyWaiver: Boolean = false,
    @SerializedName("allowNegativeBalance") val allowNegativeBalance: Boolean = false,
    @SerializedName("includeInterveningWeekends") val includeInterveningWeekends: Boolean = false,
    @SerializedName("requireSupportingDocuments") val requireSupportingDocuments: Boolean = false,
    @SerializedName("documentRequiredAfterDays") val documentRequiredAfterDays: Int = 0
)

data class LeaveCarryForwardDto(
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("maxDays") val maxDays: Int = 0,
    @SerializedName("expiryAfterMonths") val expiryAfterMonths: Int = 0,
    @SerializedName("excessTreatment") val excessTreatment: String = "expire"
)

data class LeaveEncashmentDto(
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("rateCalculation") val rateCalculation: String = "basic_per_30",
    @SerializedName("annualCeilingDays") val annualCeilingDays: Int = 0
)

data class LeaveApprovalDto(
    @SerializedName("requiresApproval") val requiresApproval: Boolean = true,
    @SerializedName("chain") val chain: List<ApprovalChainNodeDto> = emptyList(),
    @SerializedName("postFactoApproval") val postFactoApproval: PostFactoApprovalDto = PostFactoApprovalDto(),
    @SerializedName("hrHeadSignOff") val hrHeadSignOff: HrHeadSignOffDto = HrHeadSignOffDto()
)

data class ApprovalChainNodeDto(
    @SerializedName("level") val level: Int = 1,
    @SerializedName("approverRole") val approverRole: String = "reporting_manager",
    @SerializedName("_id") val id: String? = null
)

data class PostFactoApprovalDto(
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("withinHours") val withinHours: Int = 48
)

data class HrHeadSignOffDto(
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("whenDurationExceedsDays") val whenDurationExceedsDays: Int = 5
)