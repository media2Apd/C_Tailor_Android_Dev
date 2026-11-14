package com.cuso.tailor.model.report

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// =========================================================================
// COMMON PAGINATION & REFERENCE MODELS
// =========================================================================

data class ReportPaginationDto(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 1
)

data class HrNamedRefDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

data class HrMemberNameDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = ""
) {
    val fullName: String
        get() = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { "--" }
}

// =========================================================================
// 1. ATTENDANCE REGISTER & ANALYTICS MODELS
// =========================================================================

data class AttendanceAnalyticsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AttendanceAnalyticsData? = null
)

data class AttendanceAnalyticsData(
    @SerializedName("filterApplied") val filterApplied: AttendanceFilterAppliedDto? = null,
    @SerializedName("attendanceSummary") val attendanceSummary: AttendanceSummaryDto = AttendanceSummaryDto(),
    @SerializedName("statusDonutChart") val statusDonutChart: List<AttendanceStatusChartDto> = emptyList(),
    @SerializedName("workModeChart") val workModeChart: List<JsonElement> = emptyList(),
    @SerializedName("manualApprovalSummary") val manualApprovalSummary: ManualApprovalSummaryDto = ManualApprovalSummaryDto()
)

data class AttendanceFilterAppliedDto(
    @SerializedName("year") val year: Int = 0,
    @SerializedName("month") val month: Int = 0,
    @SerializedName("monthLabel") val monthLabel: String = ""
)

data class AttendanceSummaryDto(
    @SerializedName("totalRecords") val totalRecords: Int = 0,
    @SerializedName("presentCount") val presentCount: Int = 0,
    @SerializedName("absentCount") val absentCount: Int = 0,
    @SerializedName("onLeaveCount") val onLeaveCount: Int = 0,
    @SerializedName("halfDayLeaveCount") val halfDayLeaveCount: Int = 0,
    @SerializedName("holidayCount") val holidayCount: Int = 0,
    @SerializedName("weekOffCount") val weekOffCount: Int = 0,
    @SerializedName("attendanceRate") val attendanceRate: String = "0%",
    @SerializedName("lateCount") val lateCount: Int = 0,
    @SerializedName("earlyOutCount") val earlyOutCount: Int = 0,
    @SerializedName("missedCheckoutCount") val missedCheckoutCount: Int = 0,
    @SerializedName("manualEntryCount") val manualEntryCount: Int = 0,
    @SerializedName("totalWorkingMinutes") val totalWorkingMinutes: Int = 0,
    @SerializedName("totalOvertimeMinutes") val totalOvertimeMinutes: Int = 0,
    @SerializedName("totalLateMinutes") val totalLateMinutes: Int = 0
)

data class AttendanceStatusChartDto(
    @SerializedName("statusKey") val statusKey: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("count") val count: Int = 0,
    @SerializedName("percent") val percent: Double = 0.0,
    @SerializedName("color") val color: String = ""
)

data class ManualApprovalSummaryDto(
    @SerializedName("pending") val pending: Int = 0,
    @SerializedName("approved") val approved: Int = 0,
    @SerializedName("rejected") val rejected: Int = 0
)

data class AttendanceRecentResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<AttendanceRecordDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null
)

data class AttendanceRecordDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationMemberId") val organizationMemberId: String = "",
    @SerializedName("employeeName") val employeeName: String = "",
    @SerializedName("employeeCode") val employeeCode: String = "",
    @SerializedName("date") val date: String? = null,
    @SerializedName("firstIn") val firstIn: String? = null,
    @SerializedName("lastOut") val lastOut: String? = null,
    @SerializedName("totalWorkingMinutes") val totalWorkingMinutes: Int = 0,
    @SerializedName("totalBreakMinutes") val totalBreakMinutes: Int = 0,
    @SerializedName("overtimeMinutes") val overtimeMinutes: Int = 0,
    @SerializedName("workMode") val workMode: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("isLate") val isLate: Boolean = false,
    @SerializedName("lateMinutes") val lateMinutes: Int = 0,
    @SerializedName("isEarlyOut") val isEarlyOut: Boolean = false,
    @SerializedName("earlyOutMinutes") val earlyOutMinutes: Int = 0,
    @SerializedName("missedCheckout") val missedCheckout: Boolean = false,
    @SerializedName("manualEntry") val manualEntry: Boolean = false,
    @SerializedName("approvalStatus") val approvalStatus: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

// =========================================================================
// 2. PAYROLL RUN AUDIT & ANALYTICS MODELS
// =========================================================================

data class PayrollRunAnalyticsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: PayrollRunAnalyticsData? = null
)

data class PayrollRunAnalyticsData(
    @SerializedName("filterApplied") val filterApplied: PayrollRunFilterAppliedDto? = null,
    @SerializedName("payrollRunSummary") val payrollRunSummary: PayrollRunSummaryDto = PayrollRunSummaryDto(),
    @SerializedName("statusChart") val statusChart: List<PayrollStatusChartDto> = emptyList(),
    @SerializedName("monthlyTrend") val monthlyTrend: List<PayrollMonthlyTrendDto> = emptyList(),
    @SerializedName("readinessSummary") val readinessSummary: PayrollReadinessDto = PayrollReadinessDto(),
    @SerializedName("lockSummary") val lockSummary: PayrollLockSummaryDto? = null
)

data class PayrollRunFilterAppliedDto(
    @SerializedName("payFrequency") val payFrequency: String = "All",
    @SerializedName("payrollMonth") val payrollMonth: String = "All",
    @SerializedName("payrollYear") val payrollYear: String = "All"
)

data class PayrollRunSummaryDto(
    @SerializedName("totalRuns") val totalRuns: Int = 0,
    @SerializedName("totalEmployees") val totalEmployees: Int = 0,
    @SerializedName("monthlySalaryEmployees") val monthlySalaryEmployees: Int = 0,
    @SerializedName("pieceRateEmployees") val pieceRateEmployees: Int = 0,
    @SerializedName("draftRuns") val draftRuns: Int = 0,
    @SerializedName("calculatingRuns") val calculatingRuns: Int = 0,
    @SerializedName("calculatedRuns") val calculatedRuns: Int = 0,
    @SerializedName("approvedRuns") val approvedRuns: Int = 0,
    @SerializedName("paidRuns") val paidRuns: Int = 0,
    @SerializedName("cancelledRuns") val cancelledRuns: Int = 0
)

data class PayrollStatusChartDto(
    @SerializedName("_id") val status: String = "",
    @SerializedName("count") val count: Int = 0
)

data class PayrollMonthlyTrendDto(
    @SerializedName("totalRuns") val totalRuns: Int = 0,
    @SerializedName("totalEmployees") val totalEmployees: Int = 0,
    @SerializedName("paidRuns") val paidRuns: Int = 0,
    @SerializedName("approvedRuns") val approvedRuns: Int = 0,
    @SerializedName("payrollYear") val payrollYear: Int = 0,
    @SerializedName("payrollMonth") val payrollMonth: Int = 0
)

data class PayrollReadinessDto(
    @SerializedName("missingSalaryAssignments") val missingSalaryAssignments: Int = 0,
    @SerializedName("pendingPieceRateApprovals") val pendingPieceRateApprovals: Int = 0,
    @SerializedName("missingBankDetails") val missingBankDetails: Int = 0
)

data class PayrollLockSummaryDto(
    @SerializedName("attendance") val attendance: PayrollLockStatDto = PayrollLockStatDto(),
    @SerializedName("leave") val leave: PayrollLockStatDto = PayrollLockStatDto(),
    @SerializedName("reimbursements") val reimbursements: PayrollLockStatDto = PayrollLockStatDto()
)

data class PayrollLockStatDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("locked") val locked: Int = 0,
    @SerializedName("unlocked") val unlocked: Int = 0
)

data class PayrollRunRecentResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<PayrollRunRecordDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null
)

data class PayrollRunRecordDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("payFrequency") val payFrequency: String = "",
    @SerializedName("payrollMonth") val payrollMonth: Int = 0,
    @SerializedName("payrollYear") val payrollYear: Int = 0,
    @SerializedName("scope") val scope: PayrollScopeDto? = null,
    @SerializedName("dataLocks") val dataLocks: PayrollDataLocksDto? = null,
    @SerializedName("employeeCounts") val employeeCounts: PayrollEmployeeCountsDto = PayrollEmployeeCountsDto(),
    @SerializedName("readinessSnapshot") val readinessSnapshot: PayrollReadinessSnapshotDto? = null,
    @SerializedName("totalGrossSalary") val totalGrossSalary: Double = 0.0,
    @SerializedName("totalDeductions") val totalDeductions: Double = 0.0,
    @SerializedName("totalNetSalary") val totalNetSalary: Double = 0.0,
    @SerializedName("totalReimbursementAmount") val totalReimbursementAmount: Double = 0.0,
    @SerializedName("totalExpenseClaimAmount") val totalExpenseClaimAmount: Double = 0.0,
    @SerializedName("totalOtherPayables") val totalOtherPayables: Double = 0.0,
    @SerializedName("totalPayable") val totalPayable: Double = 0.0,
    @SerializedName("journalEntryId") val journalEntryId: String? = null,
    @SerializedName("status") val status: String = "",
    @SerializedName("createdBy") val createdBy: HrMemberNameDto? = null,
    @SerializedName("scopeKey") val scopeKey: String = "",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class PayrollScopeDto(
    @SerializedName("departmentIds") val departmentIds: List<HrNamedRefDto> = emptyList(),
    @SerializedName("employeeTypes") val employeeTypes: List<String> = emptyList(),
    @SerializedName("payMethods") val payMethods: List<String> = emptyList(),
    @SerializedName("includeNewJoiners") val includeNewJoiners: Boolean = true,
    @SerializedName("includeExitEmployees") val includeExitEmployees: Boolean = true
)

data class PayrollDataLocksDto(
    @SerializedName("attendance") val attendance: PayrollLockDetailDto? = null,
    @SerializedName("leave") val leave: PayrollLockDetailDto? = null,
    @SerializedName("reimbursements") val reimbursements: PayrollLockDetailDto? = null,
    @SerializedName("expenseClaims") val expenseClaims: PayrollLockDetailDto? = null
)

data class PayrollLockDetailDto(
    @SerializedName("locked") val locked: Boolean = false,
    @SerializedName("lockedAt") val lockedAt: String? = null,
    @SerializedName("lockedBy") val lockedBy: String? = null
)

data class PayrollEmployeeCountsDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("monthlySalary") val monthlySalary: Int = 0,
    @SerializedName("pieceRate") val pieceRate: Int = 0
)

data class PayrollReadinessSnapshotDto(
    @SerializedName("capturedAt") val capturedAt: String? = null,
    @SerializedName("missingSalaryAssignments") val missingSalaryAssignments: Int = 0,
    @SerializedName("pendingPieceRateApprovals") val pendingPieceRateApprovals: Int = 0,
    @SerializedName("missingBankDetails") val missingBankDetails: Int = 0
)

// =========================================================================
// 3. PIECE RATE MASTER & WAGE MODELS
// =========================================================================

data class PieceRateAnalyticsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: PieceRateAnalyticsData? = null
)

data class PieceRateAnalyticsData(
    @SerializedName("filterApplied") val filterApplied: AttendanceFilterAppliedDto? = null,
    @SerializedName("pieceRateSummary") val pieceRateSummary: PieceRateSummaryDto = PieceRateSummaryDto(),
    @SerializedName("statusDonutChart") val statusDonutChart: List<PieceRateStatusChartDto> = emptyList(),
    @SerializedName("workflowChart") val workflowChart: List<JsonElement> = emptyList()
)

data class PieceRateSummaryDto(
    @SerializedName("totalPieceRates") val totalPieceRates: Int = 0,
    @SerializedName("activeCount") val activeCount: Int = 0,
    @SerializedName("scheduledCount") val scheduledCount: Int = 0,
    @SerializedName("expiredCount") val expiredCount: Int = 0,
    @SerializedName("inactiveCount") val inactiveCount: Int = 0,
    @SerializedName("totalRegularRate") val totalRegularRate: Double = 0.0,
    @SerializedName("totalOtRate") val totalOtRate: Double = 0.0,
    @SerializedName("averageRegularRate") val averageRegularRate: Double = 0.0,
    @SerializedName("averageOtRate") val averageOtRate: Double = 0.0,
    @SerializedName("highestRegularRate") val highestRegularRate: Double = 0.0,
    @SerializedName("highestOtRate") val highestOtRate: Double = 0.0
)

data class PieceRateStatusChartDto(
    @SerializedName("statusKey") val statusKey: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("count") val count: Int = 0,
    @SerializedName("percent") val percent: Double = 0.0,
    @SerializedName("averageRegularRate") val averageRegularRate: Double = 0.0,
    @SerializedName("averageOtRate") val averageOtRate: Double = 0.0,
    @SerializedName("color") val color: String = ""
)

data class PieceRateRecentResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<PieceRateRecordDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null
)

data class PieceRateRecordDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("workflowId") val workflowId: String = "",
    @SerializedName("workflowName") val workflowName: String = "",
    @SerializedName("garmentId") val garmentId: String? = null,
    @SerializedName("garmentName") val garmentName: String = "",
    @SerializedName("garmentCode") val garmentCode: String = "",
    @SerializedName("garmentCategoryId") val garmentCategoryId: String? = null,
    @SerializedName("garmentCategoryName") val garmentCategoryName: String = "",
    @SerializedName("regularRatePerPiece") val regularRatePerPiece: Double = 0.0,
    @SerializedName("otRatePerPiece") val otRatePerPiece: Double = 0.0,
    @SerializedName("effectiveFrom") val effectiveFrom: String? = null,
    @SerializedName("effectiveUntil") val effectiveUntil: String? = null,
    @SerializedName("status") val status: String = "",
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("createdBy") val createdBy: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

// =========================================================================
// 4. REIMBURSEMENT CLAIMS & SETTLEMENT MODELS
// =========================================================================

data class ReimbursementAnalyticsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ReimbursementAnalyticsData? = null
)

data class ReimbursementAnalyticsData(
    @SerializedName("filterApplied") val filterApplied: ReimbursementFilterAppliedDto? = null,
    @SerializedName("reimbursementSummary") val reimbursementSummary: ReimbursementSummaryDto = ReimbursementSummaryDto(),
    @SerializedName("statusChart") val statusChart: List<ReimbursementChartBucketDto> = emptyList(),
    @SerializedName("paymentStatusChart") val paymentStatusChart: List<ReimbursementChartBucketDto> = emptyList(),
    @SerializedName("monthlyTrend") val monthlyTrend: List<ReimbursementMonthlyTrendDto> = emptyList(),
    @SerializedName("claimTypeChart") val claimTypeChart: List<ReimbursementChartBucketDto> = emptyList()
)

data class ReimbursementFilterAppliedDto(
    @SerializedName("paymentMonth") val paymentMonth: String = "All",
    @SerializedName("status") val status: String = "All",
    @SerializedName("paymentStatus") val paymentStatus: String = "All",
    @SerializedName("branchId") val branchId: String = "All",
    @SerializedName("departmentId") val departmentId: String = "All",
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null
)

data class ReimbursementSummaryDto(
    @SerializedName("totalClaims") val totalClaims: Int = 0,
    @SerializedName("totalClaimAmount") val totalClaimAmount: Double = 0.0,
    @SerializedName("averageClaimAmount") val averageClaimAmount: Double = 0.0,
    @SerializedName("draftClaims") val draftClaims: Int = 0,
    @SerializedName("submittedClaims") val submittedClaims: Int = 0,
    @SerializedName("pendingApprovalClaims") val pendingApprovalClaims: Int = 0,
    @SerializedName("approvedClaims") val approvedClaims: Int = 0,
    @SerializedName("rejectedClaims") val rejectedClaims: Int = 0,
    @SerializedName("cancelledClaims") val cancelledClaims: Int = 0,
    @SerializedName("paymentPendingClaims") val paymentPendingClaims: Int = 0,
    @SerializedName("paidClaims") val paidClaims: Int = 0,
    @SerializedName("paidAmount") val paidAmount: Double = 0.0,
    @SerializedName("pendingPaymentAmount") val pendingPaymentAmount: Double = 0.0
)

data class ReimbursementChartBucketDto(
    @SerializedName("_id") val key: String = "",
    @SerializedName("count") val count: Int = 0,
    @SerializedName("totalAmount") val totalAmount: Double = 0.0
)

data class ReimbursementMonthlyTrendDto(
    @SerializedName("totalClaims") val totalClaims: Int = 0,
    @SerializedName("totalClaimAmount") val totalClaimAmount: Double = 0.0,
    @SerializedName("paidAmount") val paidAmount: Double = 0.0,
    @SerializedName("paymentMonth") val paymentMonth: String = ""
)

data class ReimbursementRecentResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<ReimbursementRecordDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null
)

data class ReimbursementRecordDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("payrollRunId") val payrollRunId: String? = null,
    @SerializedName("payrollLockedAt") val payrollLockedAt: String? = null,
    @SerializedName("organizationMemberId") val organizationMemberId: HrMemberNameDto? = null,
    @SerializedName("branchId") val branchId: HrNamedRefDto? = null,
    @SerializedName("departmentId") val departmentId: HrNamedRefDto? = null,
    @SerializedName("designationId") val designationId: HrNamedRefDto? = null,
    @SerializedName("claimType") val claimType: String = "",
    @SerializedName("expenseDate") val expenseDate: String? = null,
    @SerializedName("paymentMonth") val paymentMonth: String = "",
    @SerializedName("businessPurpose") val businessPurpose: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("expenseItems") val expenseItems: List<ReimbursementExpenseItemDto> = emptyList(),
    @SerializedName("supportingDocuments") val supportingDocuments: List<ReimbursementDocumentDto> = emptyList(),
    @SerializedName("totalClaimAmount") val totalClaimAmount: Double = 0.0,
    @SerializedName("status") val status: String = "",
    @SerializedName("activity") val activity: List<ReimbursementActivityDto> = emptyList(),
    @SerializedName("approvedBy") val approvedBy: JsonElement? = null,
    @SerializedName("approvedAt") val approvedAt: String? = null,
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("paymentStatus") val paymentStatus: String = "",
    @SerializedName("paymentDate") val paymentDate: String? = null,
    @SerializedName("paymentReference") val paymentReference: String? = null,
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("updatedBy") val updatedBy: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    fun resolveApproverName(): String {
        val element = approvedBy ?: return "--"
        return when {
            element.isJsonPrimitive -> element.asString.ifBlank { "--" }
            element.isJsonObject -> {
                val obj = element.asJsonObject
                val first = obj.get("firstName")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
                val last = obj.get("lastName")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
                val full = listOf(first, last).filter { it.isNotBlank() }.joinToString(" ")
                full.ifBlank { obj.get("name")?.takeIf { !it.isJsonNull }?.asString ?: "--" }
            }
            else -> "--"
        }
    }
}

data class ReimbursementExpenseItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("expenseType") val expenseType: String = "",
    @SerializedName("expenseDate") val expenseDate: String? = null,
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("description") val description: String? = null,
    @SerializedName("receipt") val receipt: ReimbursementReceiptDto? = null
)

data class ReimbursementReceiptDto(
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("publicId") val publicId: String? = null
)

data class ReimbursementDocumentDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("documentName") val documentName: String = "",
    @SerializedName("fileName") val fileName: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("publicId") val publicId: String = ""
)

data class ReimbursementActivityDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("activityType") val activityType: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("performedBy") val performedBy: String = "",
    @SerializedName("performedAt") val performedAt: String? = null
)