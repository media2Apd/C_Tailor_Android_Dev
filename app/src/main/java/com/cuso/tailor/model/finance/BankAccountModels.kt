package com.cuso.tailor.model.finance

import com.google.gson.annotations.SerializedName
//
////data class BankAccountListResponse(
////    @SerializedName("success") val success: Boolean,
////    @SerializedName("message") val message: String?,
////    @SerializedName("data") val data: List<BankAccountItemDto> = emptyList()
////)
//
//data class BankAccountDetailResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String?,
//    @SerializedName("data") val data: BankAccountItemDto?
//)
////
////data class BankAccountItemDto(
////    @SerializedName("_id") val id: String = "",
////    @SerializedName("organizationId") val organizationId: String? = null,
////    @SerializedName("branchId") val branchId: BranchRefDto? = null,
////    @SerializedName("chartOfAccountId") val chartOfAccountId: ChartOfAccountItem? = null,
////    @SerializedName("accountType") val accountType: String = "Checking",
////    @SerializedName("accountDisplayName") val accountDisplayName: String = "",
////    @SerializedName("bankName") val bankName: String = "",
////    @SerializedName("accountNumber") val accountNumber: String = "",
////    @SerializedName("accountNumberMasked") val accountNumberMasked: String = "",
////    @SerializedName("currency") val currency: String = "INR",
////    @SerializedName("iban") val iban: String? = null,
////    @SerializedName("swiftBic") val swiftBic: String? = null,
////    @SerializedName("routingIdentifierType") val routingIdentifierType: String = "IFSC",
//
////    @SerializedName("isDefault") val isDefault: Boolean = false,
////    @SerializedName("overdraftLimit") val overdraftLimit: Double = 0.0,
////    @SerializedName("status") val status: String = "Active",
////    @SerializedName("createdAt") val createdAt: String? = null,
////    @SerializedName("updatedAt") val updatedAt: String? = null
////)
//
//data class CreateBankAccountRequest(
//    @SerializedName("accountDisplayName") val accountDisplayName: String,
//    @SerializedName("accountNumber") val accountNumber: String,
//    @SerializedName("accountType") val accountType: String,
//    @SerializedName("bankBranchName") val bankBranchName: String,
//    @SerializedName("bankName") val bankName: String,
//    @SerializedName("branchId") val branchId: String?,
//    @SerializedName("currency") val currency: String = "INR",
//    @SerializedName("isDefault") val isDefault: Boolean = false,
//    @SerializedName("overdraftLimit") val overdraftLimit: Double = 0.0,
//    @SerializedName("routingIdentifierType") val routingIdentifierType: String = "IFSC",
//    @SerializedName("routingIdentifierValue") val routingIdentifierValue: String,
//    @SerializedName("status") val status: String = "Active"
//)
//
///**
// * Response received when a bank account is archived
// * Endpoint: DELETE /api/finance/banking/accounts/{id} or POST /api/finance/banking/accounts/{id}/archive
// */
//data class ArchiveBankAccountResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String?,
//    @SerializedName("data") val data: BankAccountItemDto?
//)
//
///**
// * Response received when setting a bank account as default
// * Endpoint: POST /api/finance/banking/accounts/{id}/set-default
// */
//data class SetDefaultBankAccountResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String?,
//    @SerializedName("data") val data: BankAccountItemDto?
//)
//
//// ── View One Bank Statement Response ──
//data class BankStatementDetailResponse(
//    @SerializedName("success") val success: Boolean = false,
//    @SerializedName("message") val message: String = "",
//    @SerializedName("data") val data: BankStatementDetailData? = null
//)
//
//data class BankStatementDetailData(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("organizationId") val organizationId: String? = null,
//    @SerializedName("branchId") val branchId: String? = null,
//    @SerializedName("bankAccountId") val bankAccountId: StatementBankAccountDto? = null,
//    @SerializedName("statementNumber") val statementNumber: String = "",
//    @SerializedName("fileName") val fileName: String = "",
//    @SerializedName("fileUrl") val fileUrl: String? = null,
//    @SerializedName("importDate") val importDate: String = "",
//    @SerializedName("statementPeriodStart") val statementPeriodStart: String? = null,
//    @SerializedName("statementPeriodEnd") val statementPeriodEnd: String? = null,
//    @SerializedName("openingBalance") val openingBalance: Double = 0.0,
//    @SerializedName("closingBalance") val closingBalance: Double = 0.0,
//    @SerializedName("totalDeposits") val totalDeposits: Double = 0.0,
//    @SerializedName("totalWithdrawals") val totalWithdrawals: Double = 0.0,
//    @SerializedName("lines") val lines: List<StatementLineItemDto> = emptyList(),
//    @SerializedName("status") val status: String = "Uploaded",
//    @SerializedName("reconciledAt") val reconciledAt: String? = null,
//    @SerializedName("reconciledBy") val reconciledBy: String? = null,
//    @SerializedName("createdBy") val createdBy: StatementUserDto? = null,
//    @SerializedName("createdAt") val createdAt: String? = null,
//    @SerializedName("updatedAt") val updatedAt: String? = null
//)
//
//data class StatementBankAccountDto(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("chartOfAccountId") val chartOfAccountId: String? = null,
//    @SerializedName("accountDisplayName") val accountDisplayName: String = "",
//    @SerializedName("bankName") val bankName: String = "",
//    @SerializedName("accountNumberMasked") val accountNumberMasked: String = "",
//    @SerializedName("currency") val currency: String = "INR"
//)
//
//data class StatementLineItemDto(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("transactionDate") val transactionDate: String = "",
//    @SerializedName("description") val description: String = "",
//    @SerializedName("referenceNumber") val referenceNumber: String = "",
//    @SerializedName("withdrawal") val withdrawal: Double = 0.0,
//    @SerializedName("deposit") val deposit: Double = 0.0,
//    @SerializedName("runningBalance") val runningBalance: Double = 0.0,
//    @SerializedName("reconciliationStatus") val reconciliationStatus: String = "Unreconciled",
//    @SerializedName("matchType") val matchType: String? = null,
//    @SerializedName("matchedEntityType") val matchedEntityType: String? = null,
//    @SerializedName("matchedEntityId") val matchedEntityId: String? = null,
//    @SerializedName("notes") val notes: String = "",
//    @SerializedName("reconciledAt") val reconciledAt: String? = null,
//    @SerializedName("reconciledBy") val reconciledBy: String? = null
//)
//
//data class StatementUserDto(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("firstName") val firstName: String = "",
//    @SerializedName("lastName") val lastName: String = ""
//)
//
//// ── Manual Match Request ──
//data class ManualMatchRequest(
//    @SerializedName("statementLineId") val statementLineId: String,
//    @SerializedName("matchedEntityType") val matchedEntityType: String,
//    @SerializedName("matchedEntityId") val matchedEntityId: String,
//    @SerializedName("notes") val notes: String? = null
//)
//
//// ── Unmatch Request ──
//data class UnmatchStatementLineRequest(
//    @SerializedName("statementLineId") val statementLineId: String,
//    @SerializedName("notes") val notes: String = "Manual unmatch requested by accountant"
//)
//
//// ── Auto Reconciliation (RBS) Response ──
//data class AutoReconcileResponse(
//    @SerializedName("success") val success: Boolean = false,
//    @SerializedName("message") val message: String = "",
//    @SerializedName("data") val data: AutoReconcileData? = null
//)
//
//data class AutoReconcileData(
//    @SerializedName("statementId") val statementId: String = "",
//    @SerializedName("totalLinesCount") val totalLinesCount: Int = 0,
//    @SerializedName("matchedInThisRun") val matchedInThisRun: Int = 0,
//    @SerializedName("totalReconciledLines") val totalReconciledLines: Int = 0,
//    @SerializedName("status") val status: String = ""
//)









// ── Bank Account Models ──
data class BankAccountDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: BankAccountItemDto?
)

data class CreateBankAccountRequest(
    @SerializedName("accountDisplayName") val accountDisplayName: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("accountType") val accountType: String,
    @SerializedName("bankBranchName") val bankBranchName: String,
    @SerializedName("bankName") val bankName: String,
    @SerializedName("branchId") val branchId: String?,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("overdraftLimit") val overdraftLimit: Double = 0.0,
    @SerializedName("routingIdentifierType") val routingIdentifierType: String = "IFSC",
    @SerializedName("routingIdentifierValue") val routingIdentifierValue: String,
    @SerializedName("status") val status: String = "Active"
)

data class ArchiveBankAccountResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: BankAccountItemDto?
)

data class SetDefaultBankAccountResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: BankAccountItemDto?
)

// ── View One Bank Statement Models ──
data class BankStatementDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: BankStatementDetailData? = null
)

data class BankStatementDetailData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    // FIXED: branchId is an Object from API, not a String
    @SerializedName("branchId") val branchId: StatementBranchDto? = null,
    @SerializedName("bankAccountId") val bankAccountId: StatementBankAccountDto? = null,
    @SerializedName("statementNumber") val statementNumber: String = "",
    @SerializedName("fileName") val fileName: String = "",
    @SerializedName("fileUrl") val fileUrl: String? = null,
    @SerializedName("importDate") val importDate: String = "",
    @SerializedName("statementPeriodStart") val statementPeriodStart: String? = null,
    @SerializedName("statementPeriodEnd") val statementPeriodEnd: String? = null,
    @SerializedName("openingBalance") val openingBalance: Double = 0.0,
    @SerializedName("closingBalance") val closingBalance: Double = 0.0,
    @SerializedName("totalDeposits") val totalDeposits: Double = 0.0,
    @SerializedName("totalWithdrawals") val totalWithdrawals: Double = 0.0,
    @SerializedName("lines") val lines: List<StatementLineItemDto> = emptyList(),
    @SerializedName("status") val status: String = "Uploaded",
    @SerializedName("reconciledAt") val reconciledAt: String? = null,
    @SerializedName("reconciledBy") val reconciledBy: String? = null,
    @SerializedName("createdBy") val createdBy: StatementUserDto? = null,
    @SerializedName("updatedBy") val updatedBy: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// Branch object returned by populated API
data class StatementBranchDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

data class StatementBankAccountDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("chartOfAccountId") val chartOfAccountId: String? = null,
    @SerializedName("accountDisplayName") val accountDisplayName: String = "",
    @SerializedName("bankName") val bankName: String = "",
    @SerializedName("accountNumberMasked") val accountNumberMasked: String = "",
    @SerializedName("currency") val currency: String = "INR"
)

data class StatementLineItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("transactionDate") val transactionDate: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("referenceNumber") val referenceNumber: String = "",
    @SerializedName("withdrawal") val withdrawal: Double = 0.0,
    @SerializedName("deposit") val deposit: Double = 0.0,
    // FIXED: runningBalance can be null in the API
    @SerializedName("runningBalance") val runningBalance: Double? = null,
    @SerializedName("reconciliationStatus") val reconciliationStatus: String = "Unreconciled",
    @SerializedName("matchType") val matchType: String? = null,
    @SerializedName("matchedEntityType") val matchedEntityType: String? = null,
    @SerializedName("matchedEntityId") val matchedEntityId: String? = null,
    @SerializedName("notes") val notes: String = "",
    @SerializedName("reconciledAt") val reconciledAt: String? = null,
    @SerializedName("reconciledBy") val reconciledBy: String? = null
)

data class StatementUserDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = ""
)

// ── Manual Match Request ──
data class ManualMatchRequest(
    @SerializedName("statementLineId") val statementLineId: String,
    @SerializedName("matchedEntityType") val matchedEntityType: String,
    @SerializedName("matchedEntityId") val matchedEntityId: String,
    @SerializedName("notes") val notes: String? = null
)

// ── Unmatch Request ──
data class UnmatchStatementLineRequest(
    @SerializedName("statementLineId") val statementLineId: String,
    @SerializedName("reason") val notes: String = "Manual unmatch requested by accountant"
)

// ── Auto Reconciliation (RBS) Response ──
data class AutoReconcileResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: AutoReconcileData? = null
)

data class AutoReconcileData(
    @SerializedName("statementId") val statementId: String = "",
    @SerializedName("totalLinesCount") val totalLinesCount: Int = 0,
    @SerializedName("matchedInThisRun") val matchedInThisRun: Int = 0,
    @SerializedName("totalReconciledLines") val totalReconciledLines: Int = 0,
    @SerializedName("status") val status: String = ""
)