package com.cuso.tailor.model.settings

import com.cuso.tailor.model.logistics.PaginationMeta
import com.google.gson.annotations.SerializedName

data class LeavePolicyListResponse(
    val success: Boolean = false,
    val data: List<LeavePolicyItemDto> = emptyList(),
    val pagination: PaginationMeta? = null
)

data class LeavePolicyViewOneResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: LeavePolicyItemDto? = null
)

data class LeavePolicyActionResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: LeavePolicyItemDto? = null
)

data class LeavePolicyDeleteResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: Any? = null
)

data class LeavePolicyItemDto(
    @SerializedName("_id")
    val id: String = "",
    val organizationId: String? = null,
    val name: String = "",
    val code: String = "",
    val description: String? = null,
    val effectiveFrom: String? = null,
    val effectiveUntil: String? = null,
    val status: String = "active",
    val revision: Int = 0,
    val eligibility: PolicyEligibilityDto? = null,
    val leaveTypes: List<PolicyLeaveTypeItemDto> = emptyList(),
    val assignment: PolicyAssignmentDto? = null,
    val createdBy: Any? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val employeesAssigned: Int = 0
)

data class PolicyEligibilityDto(
    val employmentStatuses: List<String> = emptyList(),
    val departmentIds: List<Any> = emptyList(),
    val includeProbationers: Boolean = false,
    val autoEvaluateOnProbationClearance: Boolean = false,
    val designationIds: List<Any> = emptyList(),
    val grades: List<String> = emptyList(),
    val locationIds: List<String> = emptyList(),
    val minService: PolicyMinServiceDto? = null,
    val cohortSnapshot: PolicyCohortSnapshotDto? = null
)

data class PolicyMinServiceDto(
    val value: Int = 0,
    val unit: String = "days",
    val from: String = "joining_date"
)

data class PolicyCohortSnapshotDto(
    val count: Int = 0
)

data class PolicyLeaveTypeItemDto(
    @SerializedName("_id")
    val id: String = "",
    val leaveTypeId: Any? = null,
    val entitlement: PolicyEntitlementDto? = null,
    val defaultSnapshot: PolicyEntitlementDto? = null,
    val isOverridden: Boolean = false,
    val overrideReason: String? = null,
    val sortOrder: Int = 0
)

data class PolicyEntitlementDto(
    val quota: Double = 0.0,
    val unit: String = "days",
    val accrualFrequency: String = "yearly",
    val creditPerPeriod: Double? = null
)

data class PolicyAssignmentDto(
    val mode: String = "automatic",
    val priorityTier: Int = 2,
    val conflictResolution: String = "require_hr_review",
    val conflictDecisions: List<Any> = emptyList()
)

data class SaveLeavePolicyRequest(
    val name: String,
    val description: String?,
    val status: String,
    val effectiveFrom: String?,
    val effectiveUntil: String?,
    val eligibility: PolicyEligibilityDto,
    val leaveTypes: List<PolicyLeaveTypeItemDto>,
    val assignment: PolicyAssignmentDto
)