@file:Suppress("unused", "UNUSED_PARAMETER","DEPRECATION", "AssignedValueIsNeverRead")

package com.cuso.tailor.model.hr

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// Root response matching: { "success": true, "data": { "data": [...], "total": 2 }, "pagination": {...} }
data class LeaveRequestsResponse(
    val success: Boolean,
    val message: String? = null,
    val data: LeaveRequestsContainerDto? = null,
    val pagination: LeavePaginationDto? = null
)

data class LeaveRequestsContainerDto(
    val data: List<LeaveRequestItemDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val isPaginated: Boolean = false
)

data class LeaveAttachmentDto(
    val fileUrl: String? = null,
    val publicId: String? = null,
    val originalName: String? = null
)

data class LeaveRequestItemDto(
    @SerializedName("_id")
    val id: String,
    val organizationId: String?,
    val organizationMemberId: LeaveMemberDto? = null,
    val leaveTypeId: LeaveTypeBriefDto?,
    val leavePolicyId: String? = null,
    val leaveYear: String? = null,
    val startDate: String?,
    val endDate: String?,
    val totalDays: Double?,
    val isHalfDay: Boolean = false,
    val halfDayType: String? = null,
    val reason: String? = null,
    val emergencyContact: String? = null,
    val status: String = "Pending",
    val approvalRemarks: String? = null,
    val approvedAt: String? = null,
    val rejectedAt: String? = null,
    val cancelledAt: String? = null,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val attachment: LeaveAttachmentDto? = null
)

data class LeaveTypeBriefDto(
    @SerializedName("_id")
    val id: String,
    @SerializedName("leaveTypeName")
    val name: String?,
    @SerializedName("shortCode")
    val code: String? = null,
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("colorCode")
    val colorCode: String? = null,
    @SerializedName("compensationType")
    val compensationType: String? = null
)

data class LeaveMemberDto(
    @SerializedName("_id")
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val memberId: String? = null,
    val employeeCode: String? = null,
    val designationId: String? = null,
    val departmentId: String? = null,
    val workMobile: String? = null,
    val personalMail: String? = null,
    val profilePicture: String? = null
)

data class LeavePaginationDto(
    val totalPages: Int? = null,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

// Request payload matching { "status": "Approved" } or { "status": "Rejected", "approvalRemarks": "..." }
data class UpdateLeaveStatusRequest(
    @SerializedName("status")
    val status: String, // Sends "Approved" or "Rejected"
    @SerializedName("rejectionRemarks")
    val approvalRemarks: String? = null
)

// Response model matching: { "success": true, "message": "...", "data": { ... } }
data class UpdateLeaveStatusResponse(
    val success: Boolean,
    val message: String? = null,
    val data: LeaveRequestItemDto? = null
)

data class LeaveTypeResponse(
    val success: Boolean,
    val message: String,
    val data: List<LeaveTypeItemDto> = emptyList()
)

data class LeaveTypeItemDto(
    @SerializedName("_id")
    val id: String,
    val organizationId: String?,
    val name: String,
    val code: String,
    val color: String?,
    val isPaid: Boolean,
    val maxDaysPerYear: Int?,
    val maxConsecutiveDays: Int?,
    val carryForward: Boolean?,
    val maxCarryForward: Int?,
    val requiresApproval: Boolean?,
    val minNoticeDays: Int?,
    val allowHalfDay: Boolean?,
    val applicableGender: String?,
    val availableDuringProbation: Boolean?,
    val requiresAttachment: Boolean?,
    val attachmentThresholdDays: Int?,
    val isActive: Boolean
)

data class CreateLeaveRequest(
    @SerializedName("organizationMemberId")
    val organizationMemberId: String,
    @SerializedName("leaveTypeId")
    val leaveTypeId: String,
    @SerializedName("startDate")
    val startDate: String,
    @SerializedName("endDate")
    val endDate: String,
    @SerializedName("totalDays")
    val totalDays: Int,
    @SerializedName("reason")
    val reason: String,
    @SerializedName("isHalfDay")
    val isHalfDay: Boolean = false,
    @SerializedName("halfDayType")
    val halfDayType: String? = null,
    @SerializedName("emergencyContact")
    val emergencyContact: String? = null
)

data class CreateLeaveResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    val data: JsonElement? = null
)