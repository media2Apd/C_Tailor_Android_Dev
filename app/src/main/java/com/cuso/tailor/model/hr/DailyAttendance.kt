package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

data class AttendanceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<AttendanceDto>?
)

data class AttendanceDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("organizationMemberId") val organizationMember: OrganizationMemberDto?,
    @SerializedName("date") val date: String?,
    @SerializedName("shiftId") val shift: ShiftDto?,
    @SerializedName("workMode") val workMode: String?,
    @SerializedName("totalWorkingMinutes") val totalWorkingMinutes: Int = 0,
    @SerializedName("totalBreakMinutes") val totalBreakMinutes: Int = 0,
    @SerializedName("overtimeMinutes") val overtimeMinutes: Int = 0,
    @SerializedName("firstIn") val firstIn: String?,
    @SerializedName("lastOut") val lastOut: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("manualEntry") val manualEntry: Boolean = false,
    @SerializedName("isApproved") val isApproved: Boolean = false,
    @SerializedName("approvalStatus") val approvalStatus: String?
)

data class OrganizationMemberDto(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("memberId") val memberId: String?,
    @SerializedName("departmentId") val department: DepartmentDto?
)

data class DepartmentDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String?
)



// UI Model for Screen Consumption
data class AttendanceRecord(
    val id: String,
    val name: String,
    val empCode: String,
    val department: String,
    val shift: String,
    val inTime: String,
    val totalHours: String,
    val outTime: String,
    val status: String,
    val date: String,
    val approvalStatus: String
)


data class AttendanceApproveRequest(
    @SerializedName("attendanceId") val attendanceId: String,
    @SerializedName("isApproved") val isApproved: Boolean = true,
    @SerializedName("approvalStatus") val approvalStatus: String = "approved",
    @SerializedName("status") val status: String = "present"
)

data class AttendanceApproveResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

data class CreateManualAttendanceRequest(
    @SerializedName("organizationMemberId") val organizationMemberId: String,
    @SerializedName("date") val date: String,             // Format: "YYYY-MM-DD"
    @SerializedName("checkIn") val checkIn: String,       // Format: "HH:mm" (24-hr)
    @SerializedName("checkOut") val checkOut: String,     // Format: "HH:mm" (24-hr)
    @SerializedName("adminNote") val adminNote: String,
    @SerializedName("isApproved") val isApproved: Boolean = true
)

data class CreateManualAttendanceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: Any?
)