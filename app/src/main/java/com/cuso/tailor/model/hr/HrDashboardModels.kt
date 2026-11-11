@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

// =========================================================================
// 1. TODAY ATTENDANCE & PUNCHES (/api/hr/attendance/check-in-out/today)
// =========================================================================

data class CheckInOutTodayResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: CheckInOutTodayData? = null
)

data class CheckInOutTodayData(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("organizationMemberId") val organizationMemberId: String = "",
    @SerializedName("date") val date: String? = null,
    @SerializedName("shiftId") val shiftId: Any? = null,
    @SerializedName("punches") val punches: List<PunchItem> = emptyList(),
    @SerializedName("workMode") val workMode: String = "office",
    @SerializedName("totalWorkingMinutes") val totalWorkingMinutes: Int = 0,
    @SerializedName("totalBreakMinutes") val totalBreakMinutes: Int = 0,
    @SerializedName("overtimeMinutes") val overtimeMinutes: Int = 0,
    @SerializedName("missedCheckout") val missedCheckout: Boolean = false,
    @SerializedName("isHalfDay") val isHalfDay: Boolean = false,
    @SerializedName("isLate") val isLate: Boolean = false,
    @SerializedName("lateMinutes") val lateMinutes: Int = 0,
    @SerializedName("isEarlyOut") val isEarlyOut: Boolean = false,
    @SerializedName("earlyOutMinutes") val earlyOutMinutes: Int = 0,
    @SerializedName("status") val status: String = "absent",
    @SerializedName("firstIn") val firstIn: String? = null,
    @SerializedName("lastOut") val lastOut: String? = null,
    @SerializedName("checkedIn") val checkedIn: Boolean = false,
    @SerializedName("checkedOut") val checkedOut: Boolean = false,
    @SerializedName("lastPunchType") val lastPunchType: String? = null
)

data class PunchItem(
    @SerializedName("type") val type: String = "in",
    @SerializedName("time") val time: String? = null,
    @SerializedName("method") val method: String = "web",
    @SerializedName("ipAddress") val ipAddress: String? = null
)

// =========================================================================
// 2. MY PROFILE (/api/members/me)
// =========================================================================

data class MyProfileResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: MyProfileData? = null
)

data class MyProfileData(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("userId") val userId: UserContactRef? = null,
    @SerializedName("organizationId") val organizationId: OrgRefDto? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("branchId") val branchId: BranchRefDto? = null,
    @SerializedName("workingBranchId") val workingBranchId: String? = null,
    @SerializedName("departmentId") val departmentId: DepartmentRefDto? = null,
    @SerializedName("designationId") val designationId: DesignationRefDto? = null,
    @SerializedName("shiftId") val shiftId: String? = null,
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = "",
    @SerializedName("profilePicture") val profilePicture: String? = null,
    @SerializedName("employmentType") val employmentType: String? = "full-time",
    @SerializedName("status") val status: String? = "active",
    @SerializedName("doj") val doj: String? = null,
    @SerializedName("dob") val dob: String? = null,
    @SerializedName("joinedAt") val joinedAt: String? = null,
    @SerializedName("memberId") val memberId: String = "",
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("workEmail") val workEmail: String? = null,
    @SerializedName("personalMail") val personalMail: String? = null,
    @SerializedName("currentAddress") val currentAddress: String? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("maritalStatus") val maritalStatus: String? = null,
    @SerializedName("bloodGroup") val bloodGroup: String? = null,
    @SerializedName("nationality") val nationality: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("emergencyContactName") val emergencyContactName: String? = null,
    @SerializedName("emergencyContactMobile") val emergencyContactMobile: String? = null,
    @SerializedName("emergencyContactRelationship") val emergencyContactRelationship: String? = null
)

data class UserContactRef(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("email") val email: String? = null,
    @SerializedName("mobile") val mobile: String? = null
)

data class OrgRefDto(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("businessId") val businessId: String = ""
)

data class BranchRefDto(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("name") val name: String = ""
)

data class DepartmentRefDto(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("name") val name: String = ""
)

data class DesignationRefDto(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("name") val name: String = ""
)

// =========================================================================
// 3. MONTHLY ATTENDANCE VIEW (/api/hr/attendance/monthly-view)
// =========================================================================

data class MonthlyAttendanceViewResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: List<MonthlyAttendanceViewItem> = emptyList()
)

data class MonthlyAttendanceViewItem(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("date") val date: String? = null,
    @SerializedName("punches") val punches: List<PunchItem> = emptyList(),
    @SerializedName("totalWorkingMinutes") val totalWorkingMinutes: Int = 0,
    @SerializedName("totalBreakMinutes") val totalBreakMinutes: Int = 0,
    @SerializedName("isLate") val isLate: Boolean = false,
    @SerializedName("lateMinutes") val lateMinutes: Int = 0,
    @SerializedName("status") val status: String = "absent",
    @SerializedName("firstIn") val firstIn: String? = null,
    @SerializedName("lastOut") val lastOut: String? = null
)

// =========================================================================
// 4. MY SPACE DASHBOARD (/api/hr/myspace-dashboard)
// =========================================================================

data class MySpaceDashboardResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: MySpaceDashboardData? = null
)

data class MySpaceDashboardData(
    @SerializedName("currentMember") val currentMember: CurrentMemberDto? = null,
    @SerializedName("team") val team: MySpaceTeamDto? = null,
    @SerializedName("summary") val summary: MySpaceSummaryDto? = null,
    @SerializedName("attendance") val attendance: AttendanceSummaryDto? = null,
    @SerializedName("leave") val leave: LeaveSummaryDto? = null,
    @SerializedName("approvals") val approvals: ApprovalsSummaryDto? = null,
    @SerializedName("exitManagement") val exitManagement: ExitManagementDto? = null,
    @SerializedName("payroll") val payroll: PayrollSummaryDto? = null
)

data class CurrentMemberDto(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = "",
    @SerializedName("departmentId") val departmentId: String? = null,
    @SerializedName("designationId") val designationId: String? = null,
    @SerializedName("profilePicture") val profilePicture: String? = null
)

data class MySpaceTeamDto(
    @SerializedName("reporteeCount") val reporteeCount: Int = 0,
    @SerializedName("teamMemberCount") val teamMemberCount: Int = 0,
    @SerializedName("members") val members: List<CurrentMemberDto> = emptyList()
)

data class MySpaceSummaryDto(
    @SerializedName("totalEmployees") val totalEmployees: Int = 0,
    @SerializedName("activeEmployees") val activeEmployees: Int = 0,
    @SerializedName("presentToday") val presentToday: Int = 0,
    @SerializedName("onLeaveToday") val onLeaveToday: Int = 0,
    @SerializedName("pendingApprovals") val pendingApprovals: Int = 0
)

data class AttendanceSummaryDto(
    @SerializedName("presentToday") val presentToday: Int = 0
)

data class LeaveSummaryDto(
    @SerializedName("onLeaveToday") val onLeaveToday: Int = 0,
    @SerializedName("pendingApprovals") val pendingApprovals: Int = 0
)

data class ApprovalsSummaryDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("leaveApprovals") val leaveApprovals: Int = 0,
    @SerializedName("expenseApprovals") val expenseApprovals: Int = 0
)

data class ExitManagementDto(
    @SerializedName("count") val count: Int = 0
)

data class PayrollSummaryDto(
    @SerializedName("year") val year: Int = 2026,
    @SerializedName("month") val month: Int = 11,
    @SerializedName("totals") val totals: PayrollTotalsDto? = null
)

data class PayrollTotalsDto(
    @SerializedName("grossSalary") val grossSalary: Double = 0.0,
    @SerializedName("totalDeductions") val totalDeductions: Double = 0.0,
    @SerializedName("netSalary") val netSalary: Double = 0.0
)

// =========================================================================
// 5. TEAM DASHBOARD (/api/hr/team-dashboard)
// =========================================================================

data class TeamDashboardResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: TeamDashboardData? = null
)

data class TeamDashboardData(
    @SerializedName("manager") val manager: CurrentMemberDto? = null,
    @SerializedName("summary") val summary: TeamSummaryDto? = null,
    @SerializedName("teamMembers") val teamMembers: List<TeamMemberDto> = emptyList()
)

data class TeamSummaryDto(
    @SerializedName("totalTeamMembers") val totalTeamMembers: Int = 0,
    @SerializedName("activeMembers") val activeMembers: Int = 0,
    @SerializedName("inactiveMembers") val inactiveMembers: Int = 0
)

data class TeamMemberDto(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = "",
    @SerializedName("employeeCode") val employeeCode: String? = null,
    @SerializedName("memberId") val memberId: String? = null,
    @SerializedName("status") val status: String = "active",
    @SerializedName("departmentId") val departmentId: DepartmentRefDto? = null,
    @SerializedName("designationId") val designationId: DesignationRefDto? = null
)

// =========================================================================
// 6. ORG DASHBOARD (/api/hr/org-dashboard)
// =========================================================================

data class OrgDashboardResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: OrgDashboardData? = null
)

data class OrgDashboardData(
    @SerializedName("overview") val overview: OrgOverviewDto? = null,
    @SerializedName("departmentHeadcount") val departmentHeadcount: List<DepartmentHeadcountDto> = emptyList(),
    @SerializedName("genderDiversity") val genderDiversity: GenderDiversityDto? = null,
    @SerializedName("monthlyHiringAttritionTrend") val monthlyHiringAttritionTrend: MonthlyHiringAttritionTrendDto? = null,
    @SerializedName("organizationLeaveBalance") val organizationLeaveBalance: OrganizationLeaveBalanceDto? = null
)

data class OrgOverviewDto(
    @SerializedName("totalEmployees") val totalEmployees: Int = 0,
    @SerializedName("activeEmployees") val activeEmployees: Int = 0,
    @SerializedName("inactiveEmployees") val inactiveEmployees: Int = 0,
    @SerializedName("departments") val departments: Int = 0,
    @SerializedName("newHiresThisMonth") val newHiresThisMonth: Int = 0,
    @SerializedName("estimatedMonthlyAttritionRate") val estimatedMonthlyAttritionRate: Double = 0.0
)

data class DepartmentHeadcountDto(
    @SerializedName("departmentId") val departmentId: String = "",
    @SerializedName("departmentName") val departmentName: String = "",
    @SerializedName("employeeCount") val employeeCount: Int = 0
)

data class GenderDiversityDto(
    @SerializedName("totalActiveEmployees") val totalActiveEmployees: Int = 0,
    @SerializedName("records") val records: List<GenderRecordDto> = emptyList()
)

data class GenderRecordDto(
    @SerializedName("gender") val gender: String = "",
    @SerializedName("count") val count: Int = 0,
    @SerializedName("percentage") val percentage: Int = 0
)

data class MonthlyHiringAttritionTrendDto(
    @SerializedName("year") val year: Int = 2026,
    @SerializedName("months") val months: List<MonthTrendDto> = emptyList()
)

data class MonthTrendDto(
    @SerializedName("monthName") val monthName: String = "",
    @SerializedName("hires") val hires: Int = 0,
    @SerializedName("exits") val exits: Int = 0
)

data class OrganizationLeaveBalanceDto(
    @SerializedName("leaveYear") val leaveYear: Int = 2026,
    @SerializedName("totalEntitlement") val totalEntitlement: Int = 0,
    @SerializedName("totalCredited") val totalCredited: Int = 0,
    @SerializedName("totalUsed") val totalUsed: Int = 0,
    @SerializedName("totalPending") val totalPending: Int = 0,
    @SerializedName("totalAvailable") val totalAvailable: Int = 0,
    @SerializedName("employeeBalanceRecords") val employeeBalanceRecords: Int = 0
)

// ── Announcement / Feed Models ──
data class AnnouncementFeedResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<AnnouncementItemData> = emptyList()
)

data class AnnouncementItemData(
    val _id: String = "",
    val postCategory: String = "",
    val title: String = "",
    val description: String = "",
    val audience: AnnouncementAudience? = null,
    val status: String = "",
    val publishedAt: String? = null,
    val createdBy: AnnouncementCreator? = null,
    val createdAt: String? = null
)

data class AnnouncementAudience(
    val type: String = "",
    val departmentIds: List<String> = emptyList(),
    val branchIds: List<String> = emptyList()
)

data class AnnouncementCreator(
    val _id: String = "",
    val firstName: String = "",
    val lastName: String = ""
)

// ── Legal Employee / Files Models ──
data class LegalEmployeeResponse(
    val success: Boolean = false,
    val data: List<LegalEmployeeData> = emptyList()
)

data class LegalEmployeeData(
    val _id: String = "",
    val legalRecordNumber: String = "",
    val employeeCode: String = "",
    val employmentType: String = "",
    val status: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val employmentContractDocuments: List<LegalFileItem> = emptyList(),
    val supportingDocuments: List<LegalFileItem> = emptyList(),
    val labourDocuments: List<LegalFileItem> = emptyList()
)

data class LegalFileItem(
    val _id: String = "",
    val documentName: String = "",
    val documentUrl: String = "",
    val uploadedAt: String? = null
)

// ── Designation Career History Models ──
data class DesignationDetailResponse(
    val success: Boolean = false,
    val data: DesignationDetailData? = null
)

data class DesignationDetailData(
    val _id: String = "",
    val name: String = "",
    val code: String = "",
    val description: String = "",
    val status: String = "",
    val departmentId: DepartmentRef? = null,
    val history: List<DesignationHistoryEntry> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class DepartmentRef(
    val _id: String = "",
    val name: String = ""
)

data class DesignationHistoryEntry(
    val name: String = "",
    val code: String = "",
    val departmentId: String = "",
    val description: String = "",
    val status: String = "",
    val changedAt: String? = null
)

data class CreateAnnouncementRequest(
    val postCategory: String,
    val title: String,
    val description: String,
    val status: String, // "Draft" or "Published"
    val audience: AnnouncementAudiencePayload,
    val poll: AnnouncementPollPayload? = null
)

data class AnnouncementAudiencePayload(
    val type: String = "Filtered_Employees",
    val organizationMemberIds: List<String> = emptyList(),
    val employmentTypes: List<String> = emptyList(),
    val departmentIds: List<String> = emptyList(),
    val branchIds: List<String> = emptyList()
)

data class AnnouncementPollPayload(
    val question: String,
    val options: List<AnnouncementPollOptionPayload>,
    val allowMultipleSelections: Boolean,
    val closingDate: String? = null
)

data class AnnouncementPollOptionPayload(
    val optionText: String
)

data class AnnouncementCreateResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: AnnouncementItemData? = null
)