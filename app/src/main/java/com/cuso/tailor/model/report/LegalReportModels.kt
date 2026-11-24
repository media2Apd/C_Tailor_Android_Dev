
@file:Suppress("unused")

package com.cuso.tailor.model.report

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =============================================================================
// 1. CONTRACTS & EXPIRY AUDIT REPORT
// =============================================================================

@Serializable
data class ContractsExpiryReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ContractExpiryItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: ContractsExpirySummaryDto? = null
)

@Serializable
data class ContractExpiryItemDto(
    @SerialName("_id")
    val id: String = "",
    val contractNumber: String = "",
    val contractName: String = "",
    val contractType: String = "",
    val counterparty: String = "",
    val legalEntity: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val daysRemaining: String = "",
    val isExpired: Boolean = false,
    val contractValue: Double = 0.0,
    val currency: String = "INR",
    val renewalStatus: String = "",
    val renewalType: String = "",
    val riskLevel: String = "",
    val priority: String = "",
    val legalOwner: String = "",
    val createdAt: String? = null
)

@Serializable
data class ContractsExpirySummaryDto(
    val totalContracts: Int = 0,
    val totalPortfolioValue: Double = 0.0,
    val expiringIn30Days: Int = 0,
    val expiredContracts: Int = 0,
    val highRiskContracts: Int = 0,
    val pendingRenewals: Int = 0
)

// =============================================================================
// 2. COMPLIANCE & LICENSE AUDIT REPORT
// =============================================================================

@Serializable
data class ComplianceLicenseReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ComplianceLicenseItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: ComplianceLicenseSummaryDto? = null
)

@Serializable
data class ComplianceLicenseItemDto(
    @SerialName("_id")
    val id: String = "",
    val licenseCode: String = "",
    val licenseName: String = "",
    val licenseType: String = "",
    val licenseNumber: String = "",
    val issuingAuthority: String = "",
    val jurisdiction: String = "",
    val issueDate: String? = null,
    val expiryDate: String? = null,
    val daysRemaining: String = "",
    val isExpired: Boolean = false,
    val status: String = "",
    val renewalStatus: String = "",
    val riskLevel: String = "",
    val licenseOwner: String = "",
    val createdAt: String? = null
)

@Serializable
data class ComplianceLicenseSummaryDto(
    val totalLicenses: Int = 0,
    val activeLicenses: Int = 0,
    val expiringIn30Days: Int = 0,
    val expiredLicenses: Int = 0,
    val highRiskLicenses: Int = 0,
    val activeCompliancesTracked: Int = 0
)

// =============================================================================
// 3. LEGAL CASES & DISPUTES REPORT
// =============================================================================

@Serializable
data class LegalCasesDisputesReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<LegalCaseDisputeItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: LegalCasesDisputesSummaryDto? = null
)

@Serializable
data class LegalCaseDisputeItemDto(
    @SerialName("_id")
    val id: String = "",
    val caseCode: String = "",
    val disputeCode: String = "",
    val noticeCode: String = "",
    val title: String = "",
    val subject: String = "",
    val caseType: String = "",
    val partyInvolved: String = "",
    val jurisdiction: String = "",
    val hearingDate: String? = null,
    val deadlineDate: String? = null,
    val timelineAlert: String = "",
    val isOverdue: Boolean = false,
    val stage: String = "",
    val riskLevel: String = "",
    val legalCounsel: String = "",
    val status: String = "",
    val createdAt: String? = null
)

@Serializable
data class LegalCasesDisputesSummaryDto(
    val totalCases: Int = 0,
    val activeDisputes: Int = 0,
    val criticalHighRisk: Int = 0,
    val upcomingDeadlines: Int = 0,
    val resolvedCases: Int = 0
)