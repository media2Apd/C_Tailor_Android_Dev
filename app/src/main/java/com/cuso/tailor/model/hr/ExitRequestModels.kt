package com.cuso.tailor.model.hr

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ExitRequestListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ExitRequestDto> = emptyList(),
    val pagination: ExitPaginationDto? = null
)

data class ExitRequestSingleResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ExitRequestDto? = null
)

data class ExitPaginationDto(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

data class ExitRequestDto(
    @SerializedName("_id")
    val id: String = "",
    val exitCode: String? = null,
    val organizationId: String? = null,
    val organizationMemberId: JsonElement? = null,
    val branchId: JsonElement? = null,
    val departmentId: JsonElement? = null,
    val designationId: JsonElement? = null,
    val exitType: String = "",
    val reason: String? = null,
    val resignationDate: String? = null,
    val requestedLastWorkingDate: String? = null,
    val approvedLastWorkingDate: String? = null,
    val noticePeriodDays: Int = 90,
    val noticePeriodWaived: Boolean = false,
    val noticePeriodWaivedDays: Int = 0,
    val initiatedBy: JsonElement? = null,
    val status: String = "Submitted",
    val submittedAt: String? = null,
    val approvedAt: String? = null,
    val rejectedAt: String? = null,
    val completedAt: String? = null,
    val rejectionReason: String? = null,
    val withdrawalReason: String? = null,
    val remarks: String? = null,
    val attachments: List<ExitAttachmentDto> = emptyList(),
    val clearanceItems: List<ExitClearanceItemDto> = emptyList(),
    val clearanceStatus: String? = null,
    val clearanceCompletedAt: String? = null,
    val clearanceRemarks: String? = null,
    val clearanceNotes: String? = null,
    val notes: String? = null,
    val settlementStatus: String? = null,
    val settlementAmount: Double? = null,
    val settlementDate: String? = null,
    val activityLog: List<ExitActivityLogDto> = emptyList(),
    val approvals: List<ExitApprovalDto> = emptyList(),
    val pendingTasks: List<ExitPendingTaskDto> = emptyList(),
    val isActive: Boolean = true,
    val createdBy: String? = null,
    val updatedBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ExitAttachmentDto(
    @SerializedName("_id")
    val id: String = "",
    val name: String = "",
    val url: String = "",
    val publicId: String? = null,
    val resourceType: String? = null
)

data class ExitClearanceItemDto(
    @SerializedName("_id")
    val id: String = "",
    val department: String = "",
    val departmentId: JsonElement? = null,
    val assignedTo: String? = null,
    val title: String = "",
    val description: String? = null,
    val assetId: String? = null,
    val status: String = "Pending",
    val clearedAt: String? = null,
    val remarks: String? = null,
    val attachments: List<ExitAttachmentDto> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ExitActivityLogDto(
    @SerializedName("_id")
    val id: String = "",
    val action: String = "",
    val description: String = "",
    val performedBy: JsonElement? = null,
    val performedAt: String? = null
)

data class ExitApprovalDto(
    @SerializedName("_id")
    val id: String = "",
    val stage: String = "Manager_Review",
    val approverId: JsonElement? = null,
    val status: String = "Pending",
    val comments: String? = null,
    val approvedAt: String? = null
)

data class ExitPendingTaskDto(
    @SerializedName("_id")
    val id: String = "",
    val title: String = "",
    val progress: Int = 0,
    val status: String = "Pending"
)

data class ExitApprovalDecisionRequest(
    val stage: String = "Manager_Review",
    val approverId: String? = null,
    val status: String,
    val comments: String? = null
)

data class ExitStatusUpdateRequest(
    val status: String,
    val comments: String? = null
)

data class ExitClearanceItemStatusRequest(
    val status: String
)

data class ExitClearanceFinalizeRequest(
    val status: String = "Clearance_Completed",
    val clearanceStatus: String = "Completed"
)

fun ExitClearanceItemDto.resolveDepartmentName(): String {
    if (department.isNotBlank()) return department
    return departmentId.extractEntityName("General")
}

data class ExitAssignedAssetListResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ExitAssignedAssetDto> = emptyList()
)

data class ExitAssignedAssetDto(
    @SerializedName("_id")
    val id: String = "",
    val assetId: JsonElement? = null,
    val assetName: String? = null,
    val name: String? = null,
    val assetTag: String? = null,
    val assetCode: String? = null,
    val serialNumber: String? = null,
    val status: String = "Assigned",
    val asset: JsonElement? = null,
    val currentAssignee: ExitAssetAssigneeDto? = null
)

data class ExitAssetAssigneeDto(
    val name: String? = null,
    val empId: String? = null,
    val department: String? = null,
    val userId: String? = null
)

data class ExitAssetAssignmentRequest(
    val assigneeName: String,
    val empId: String? = null,
    val department: String? = null,
    val userId: String? = null
)

fun ExitAssignedAssetDto.resolveDisplayName(): String {
    val nestedObj = asset?.takeIf { it.isJsonObject }?.asJsonObject
    val resolvedName = assetName?.takeIf { it.isNotBlank() }
        ?: name?.takeIf { it.isNotBlank() }
        ?: nestedObj?.get("assetName")?.takeIf { !it.isJsonNull }?.asString
        ?: nestedObj?.get("name")?.takeIf { !it.isJsonNull }?.asString
        ?: "Asset"
    val resolvedTag = assetTag?.takeIf { it.isNotBlank() }
        ?: assetCode?.takeIf { it.isNotBlank() }
        ?: serialNumber?.takeIf { it.isNotBlank() }
        ?: nestedObj?.get("assetTag")?.takeIf { !it.isJsonNull }?.asString
        ?: nestedObj?.get("serialNumber")?.takeIf { !it.isJsonNull }?.asString
        ?: assetId?.takeIf { it.isJsonPrimitive }?.asString
        ?: ""
    return if (resolvedTag.isNotBlank()) "Return: $resolvedName ($resolvedTag)" else "Return: $resolvedName"
}

fun ExitAssignedAssetDto.resolveAssetId(): String {
    if (id.isNotBlank()) return id
    val nestedId = asset?.takeIf { it.isJsonObject }?.asJsonObject?.get("_id")
        ?.takeIf { !it.isJsonNull }?.asString
    if (!nestedId.isNullOrBlank()) return nestedId
    return assetId.extractId()
}

// ─────────────────────────────────────────────
// Exit Settlement Models
// ─────────────────────────────────────────────

data class ExitSettlementResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ExitSettlementDto? = null
)

data class ExitSettlementCalculationResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: ExitSettlementCalculationDto? = null
)

data class ExitSettlementCalculationDto(
    val exitRequestId: String = "",
    val organizationMemberId: JsonElement? = null,
    val requestedLastWorkingDate: String? = null,
    val approvedLastWorkingDate: String? = null,
    val compensationSummary: ExitCompensationSummaryDto? = null,
    val earnings: List<ExitSettlementLineItemDto> = emptyList(),
    val deductions: List<ExitSettlementLineItemDto> = emptyList(),
    val loanDeductions: List<ExitSettlementLineItemDto> = emptyList(),
    val waivedLoans: List<ExitSettlementLineItemDto> = emptyList(),
    val reimbursements: List<ExitSettlementLineItemDto> = emptyList(),
    val pendingReimbursements: List<ExitSettlementLineItemDto> = emptyList(),
    val totalReimbursementAmount: Double = 0.0,
    val pendingReimbursementAmount: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val totalDeductions: Double = 0.0,
    val netSettlementAmount: Double = 0.0,
    val loanRecoveryCount: Int = 0,
    val calculationStatus: String? = null
)

data class ExitCompensationSummaryDto(
    val totalEarnings: Double = 0.0,
    val totalDeductions: Double = 0.0,
    val netSalary: Double = 0.0
)

data class ExitSettlementLineItemDto(
    @SerializedName("_id")
    val id: String? = null,
    val title: String? = null,
    val name: String? = null,
    val label: String? = null,
    val leaveType: String? = null,
    val description: String? = null,
    val days: Double? = null,
    val rate: Double? = null,
    val amount: Double = 0.0
)

fun ExitSettlementLineItemDto.resolveLabel(): String =
    title?.takeIf { it.isNotBlank() }
        ?: name?.takeIf { it.isNotBlank() }
        ?: label?.takeIf { it.isNotBlank() }
        ?: leaveType?.takeIf { it.isNotBlank() }
        ?: description?.takeIf { it.isNotBlank() }
        ?: "Item"

data class ExitSettlementDto(
    @SerializedName("_id")
    val id: String = "",
    val organizationId: String? = null,
    val organizationMemberId: JsonElement? = null,
    val exitRequestId: String? = null,
    val settlementNumber: String? = null,
    val employeeSnapshot: ExitSettlementEmployeeSnapshotDto? = null,
    val salaryDetails: ExitSettlementSalaryDetailsDto? = null,
    val leaveEncashments: List<ExitSettlementLineItemDto> = emptyList(),
    val expenseReimbursements: List<ExitSettlementLineItemDto> = emptyList(),
    val noticePeriod: ExitSettlementNoticePeriodDto? = null,
    val assetRecoveries: List<ExitSettlementLineItemDto> = emptyList(),
    val gratuity: ExitSettlementGratuityDto? = null,
    val deductions: ExitSettlementDeductionsDto? = null,
    val totals: ExitSettlementTotalsDto? = null,
    val currency: String = "INR",
    val settlementDate: String? = null,
    val paymentDate: String? = null,
    val paymentReference: String? = null,
    val paymentStatus: String = "Not_Started",
    val status: String = "Draft",
    val employeeSignature: ExitSettlementSignatureDto? = null,
    val hrSignature: ExitSettlementSignatureDto? = null,
    val financeSignature: ExitSettlementSignatureDto? = null,
    val remarks: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ExitSettlementEmployeeSnapshotDto(
    val employeeCode: String? = null,
    val employeeName: String? = null,
    val designation: String? = null,
    val department: String? = null,
    val joiningDate: String? = null,
    val lastWorkingDate: String? = null
)

data class ExitSettlementSalaryDetailsDto(
    val monthlyBasicSalary: Double = 0.0,
    val monthlyGrossSalary: Double = 0.0,
    val dailySalaryRate: Double = 0.0,
    val payableDays: Double = 0.0,
    val unpaidLeaveDays: Double = 0.0,
    val pendingSalary: Double = 0.0,
    val bonus: Double = 0.0,
    val incentives: Double = 0.0,
    val otherEarnings: Double = 0.0
)

data class ExitSettlementNoticePeriodDto(
    val applicableDays: Int = 0,
    val servedDays: Int = 0,
    val recoveryDays: Int = 0,
    val recoveryAmount: Double = 0.0
)

data class ExitSettlementGratuityDto(
    val eligible: Boolean = false,
    val amount: Double = 0.0,
    val adjustment: Double = 0.0,
    val remarks: String? = null
)

data class ExitSettlementDeductionsDto(
    val incomeTax: Double = 0.0,
    val providentFund: Double = 0.0,
    val esi: Double = 0.0,
    val loanOutstanding: Double = 0.0,
    val otherDeductions: Double = 0.0,
    val items: List<ExitSettlementLineItemDto> = emptyList()
)

data class ExitSettlementTotalsDto(
    val totalSalary: Double = 0.0,
    val totalLeaveEncashment: Double = 0.0,
    val totalReimbursements: Double = 0.0,
    val totalBonusAndIncentives: Double = 0.0,
    val totalGratuity: Double = 0.0,
    val totalOtherEarnings: Double = 0.0,
    val grossSettlement: Double = 0.0,
    val totalNoticeRecovery: Double = 0.0,
    val totalAssetRecovery: Double = 0.0,
    val totalDeductions: Double = 0.0,
    val finalPayableAmount: Double = 0.0
)

data class ExitSettlementSignatureDto(
    val signed: Boolean = false,
    val signedAt: String? = null
)

// Request payload for POST /api/hr/exit-settlement/create
data class CreateExitSettlementRequest(
    val exitRequestId: String,
    val organizationMemberId: String,
    val currency: String = "INR",
    val deductions: CreateExitSettlementDeductionsPayload = CreateExitSettlementDeductionsPayload(),
    val employeeSnapshot: CreateExitSettlementSnapshotPayload,
    val expenseReimbursements: List<ExitSettlementLineItemDto> = emptyList(),
    val remarks: String = "",
    val totals: CreateExitSettlementTotalsPayload
)

data class CreateExitSettlementDeductionsPayload(
    val items: List<ExitSettlementLineItemDto> = emptyList()
)

data class CreateExitSettlementSnapshotPayload(
    val name: String,
    val designationName: String,
    val departmentName: String
)

data class CreateExitSettlementTotalsPayload(
    val totalReimbursements: Double = 0.0,
    val grossSettlement: Double = 0.0,
    val totalDeductions: Double = 0.0,
    val finalPayableAmount: Double = 0.0
)

data class ExitClearanceProgress(
    val total: Int = 0,
    val cleared: Int = 0,
    val inProgress: Int = 0,
    val pending: Int = 0,
    val percentage: Int = 0
)

fun ExitRequestDto.toClearanceProgress(): ExitClearanceProgress {
    val totalCount = clearanceItems.size
    val clearedCount = clearanceItems.count {
        it.status.equals("Cleared", ignoreCase = true) ||
                it.status.equals("Completed", ignoreCase = true)
    }
    val inProgressCount = clearanceItems.count {
        it.status.equals("In_Progress", ignoreCase = true) ||
                it.status.equals("In Progress", ignoreCase = true)
    }
    val pendingCount = (totalCount - clearedCount - inProgressCount).coerceAtLeast(0)
    val percent = if (totalCount > 0) (clearedCount * 100) / totalCount else 0
    return ExitClearanceProgress(
        total = totalCount,
        cleared = clearedCount,
        inProgress = inProgressCount,
        pending = pendingCount,
        percentage = percent
    )
}

// Safe extraction helpers for populated objects vs raw IDs
fun JsonElement?.extractId(): String {
    if (this == null || isJsonNull) return ""
    return when {
        isJsonPrimitive -> asString
        isJsonObject -> asJsonObject.get("_id")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
        else -> ""
    }
}

fun JsonElement?.extractMemberName(): String {
    if (this == null || isJsonNull) return "-"
    if (isJsonPrimitive) return asString
    if (!isJsonObject) return "-"
    val obj = asJsonObject
    val fullName = obj.get("fullName")?.takeIf { !it.isJsonNull }?.asString
    if (!fullName.isNullOrBlank()) return fullName
    val first = obj.get("firstName")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
    val last = obj.get("lastName")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
    val combined = listOf(first, last).filter { it.isNotBlank() }.joinToString(" ")
    if (combined.isNotBlank()) return combined
    return obj.get("name")?.takeIf { !it.isJsonNull }?.asString ?: "-"
}

fun JsonElement?.extractEntityName(fallback: String = "-"): String {
    if (this == null || isJsonNull) return fallback
    if (isJsonPrimitive) return asString
    if (!isJsonObject) return fallback
    val obj = asJsonObject
    return obj.get("name")?.takeIf { !it.isJsonNull }?.asString
        ?: obj.get("departmentName")?.takeIf { !it.isJsonNull }?.asString
        ?: obj.get("designationName")?.takeIf { !it.isJsonNull }?.asString
        ?: fallback
}

fun JsonElement?.extractProfilePicture(): String? {
    if (this == null || isJsonNull || !isJsonObject) return null
    return asJsonObject.get("profilePicture")?.takeIf { !it.isJsonNull }?.asString
}

fun JsonElement?.extractDoj(): String? {
    if (this == null || isJsonNull || !isJsonObject) return null
    val obj = asJsonObject
    return obj.get("doj")?.takeIf { !it.isJsonNull }?.asString
        ?: obj.get("createdAt")?.takeIf { !it.isJsonNull }?.asString
}