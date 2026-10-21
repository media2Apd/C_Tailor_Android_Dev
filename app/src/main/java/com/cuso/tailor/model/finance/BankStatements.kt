package com.cuso.tailor.model.finance

import com.cuso.tailor.model.sales.PaginationInfo
import com.google.gson.annotations.SerializedName

// =============================================================
// 1. BANK ACCOUNTS VIEW ALL RESPONSE
// =============================================================

data class BankAccountListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<BankAccountItemDto> = emptyList()
)

data class BankAccountBranch(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

data class BankAccountChartOfAccount(
    @SerializedName("_id") val id: String = "",
    @SerializedName("accountName") val accountName: String = "",
    @SerializedName("accountCode") val accountCode: String = "",
    @SerializedName("accountType") val accountType: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("openingBalance") val openingBalance: Double = 0.0,
    @SerializedName("normalBalance") val normalBalance: String = "",
    @SerializedName("currency") val currency: String = "",
    @SerializedName("reconciliation") val reconciliation: Boolean = false,
    @SerializedName("status") val status: String = ""
)

data class BankAccountAddress(
    @SerializedName("countryCode") val countryCode: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("addressLine") val addressLine: String? = null
)

data class BankAccountContact(
    @SerializedName("contactPerson") val contactPerson: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("email") val email: String? = null
)

data class BankAccountUser(
    @SerializedName("_id") val id: String = "",
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = ""
)

data class BankAccountItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("branchId") val branchId: BankAccountBranch? = null,
    @SerializedName("chartOfAccountId") val chartOfAccountId: BankAccountChartOfAccount? = null,
    @SerializedName("accountType") val accountType: String = "",
    @SerializedName("accountDisplayName") val accountDisplayName: String = "",
    @SerializedName("bankName") val bankName: String = "",
    @SerializedName("accountNumber") val accountNumber: String = "",
    @SerializedName("accountNumberMasked") val accountNumberMasked: String = "",
    @SerializedName("currency") val currency: String = "",
    @SerializedName("iban") val iban: String? = null,
    @SerializedName("swiftBic") val swiftBic: String? = null,
    @SerializedName("routingIdentifierType") val routingIdentifierType: String? = null,
    @SerializedName("routingIdentifierValue") val routingIdentifierValue: String? = null,
    @SerializedName("bankBranchName") val bankBranchName: String? = null,
    @SerializedName("bankAddress") val bankAddress: BankAccountAddress? = null,
    @SerializedName("isDefault") val isDefault: Boolean = false,
    @SerializedName("overdraftLimit") val overdraftLimit: Double = 0.0,
    @SerializedName("contactDetails") val contactDetails: BankAccountContact? = null,
    @SerializedName("status") val status: String = "",
    @SerializedName("createdBy") val createdBy: BankAccountUser? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// =============================================================
// 2. BANK STATEMENTS VIEW ALL RESPONSE
// =============================================================

data class BankStatementListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<BankStatementItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: PaginationInfo? = null
)

data class BankStatementAccountRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("chartOfAccountId") val chartOfAccountId: String? = null,
    @SerializedName("accountDisplayName") val accountDisplayName: String = "",
    @SerializedName("bankName") val bankName: String = "",
    @SerializedName("accountNumberMasked") val accountNumberMasked: String = "",
    @SerializedName("currency") val currency: String = "INR"
)

data class BankStatementLineDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("transactionDate") val transactionDate: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("referenceNumber") val referenceNumber: String = "",
    @SerializedName("withdrawal") val withdrawal: Double = 0.0,
    @SerializedName("deposit") val deposit: Double = 0.0,
    @SerializedName("runningBalance") val runningBalance: Double? = null,
    @SerializedName("reconciliationStatus") val reconciliationStatus: String = "",
    @SerializedName("matchType") val matchType: String? = null,
    @SerializedName("matchedEntityType") val matchedEntityType: String? = null,
    @SerializedName("matchedEntityId") val matchedEntityId: String? = null,
    @SerializedName("reconciledAt") val reconciledAt: String? = null,
    @SerializedName("notes") val notes: String? = null
)

data class BankStatementItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("branchId") val branchId: BankAccountBranch? = null, // Changed from String? to BankAccountBranch?
    @SerializedName("bankAccountId") val bankAccountId: BankStatementAccountRef? = null,
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
    @SerializedName("lines") val lines: List<BankStatementLineDto> = emptyList(),
    @SerializedName("status") val status: String = "",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)
