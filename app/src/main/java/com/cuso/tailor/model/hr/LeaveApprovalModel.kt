package com.cuso.tailor.model.hr

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class LeaveRequestsResponse(
    val success: Boolean,
    val message: String,
    val data: List<LeaveRequestItemDto> = emptyList(),
    val pagination: LeavePaginationDto? = null
)

data class LeaveRequestItemDto(
    @SerializedName("_id")
    val id: String,
    val organizationId: String?,
    val organizationMemberId: LeaveMemberDto? = null,
    val leaveTypeId: LeaveTypeBriefDto?,
    val startDate: String?,
    val endDate: String?,
    val totalDays: Double?,
    val isHalfDay: Boolean = false,
    val halfDayType: String? = null,
    val reason: String? = null,
    val status: String = "pending",
    val approverId: String? = null,
    val approverNote: String? = null,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class LeaveTypeBriefDto(
    @SerializedName("_id")
    val id: String,
    val name: String?,
    val code: String?,
    val isPaid: Boolean?
)

data class LeaveMemberDto(
    @SerializedName("_id")
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val memberId: String? = null,
    val designation: String? = null,
    val department: String? = null
)

data class LeavePaginationDto(
    val page: Int = 1,
    val limit: Int = 10,
    val total: Int = 0,
    val totalPages: Int = 1
)

data class LeaveActionResponse(
    val success: Boolean,
    val message: String
)

// Request payload for updating leave status
data class UpdateLeaveStatusRequest(
    @SerializedName("status")
    val status: String, // "approved" or "rejected"
    @SerializedName("approverNote")
    val approverNote: String? = null
)

data class UpdateLeaveStatusResponse(
    val success: Boolean,
    val message: String,
    val data: JsonElement? = null // Avoids parsing failure for nested response objects
)

// Response wrapper for leave types
data class LeaveTypeResponse(
    val success: Boolean,
    val message: String,
    val data: List<LeaveTypeItemDto> = emptyList()
)

// Individual leave type model
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

// Request payload to submit a leave request
data class ApplyLeaveRequest(
    val organizationMemberId: String,
    val leaveTypeId: String,
    val startDate: String,
    val endDate: String,
    val totalDays: Double,
    val reason: String,
    val attachments: List<String> = emptyList()
)

// Response received after submitting leave request
data class ApplyLeaveResponse(
    val success: Boolean,
    val message: String
)

// Request data class
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

// Response data class with safe JsonElement parsing
data class CreateLeaveResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    // JsonElement safely handles String, Object, or null without throwing type mismatch errors
    @SerializedName("data")
    val data: JsonElement? = null
)

data class LeaveAttachmentDto(
    @SerializedName("url")
    val url: String,
    @SerializedName("name")
    val name: String? = null
)

