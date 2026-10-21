package com.cuso.tailor.model.finance

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
// 1. IMPORT STATEMENT REQUEST PAYLOAD
// ─────────────────────────────────────────────────────────────

data class ImportBankStatementLineRequest(
    @SerializedName("transactionDate") val transactionDate: String,
    @SerializedName("description") val description: String,
    @SerializedName("referenceNumber") val referenceNumber: String = "",
    @SerializedName("withdrawal") val withdrawal: Double = 0.0,
    @SerializedName("deposit") val deposit: Double = 0.0,
    @SerializedName("runningBalance") val runningBalance: Double? = null
)

data class ImportBankStatementRequest(
    @SerializedName("bankAccountId") val bankAccountId: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("openingBalance") val openingBalance: Double = 0.0,
    @SerializedName("closingBalance") val closingBalance: Double = 0.0,
    @SerializedName("lines") val lines: List<ImportBankStatementLineRequest>
)

// ─────────────────────────────────────────────────────────────
// 2. IMPORT STATEMENT RESPONSE BODY
// ─────────────────────────────────────────────────────────────

data class ImportBankStatementResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: ImportBankStatementData? = null
)

data class ImportBankStatementData(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("branchId") val branchId: BankAccountBranch? = null,
    @SerializedName("bankAccountId") val bankAccountId: BankStatementAccountRef? = null,
    @SerializedName("statementNumber") val statementNumber: String = "",
    @SerializedName("fileName") val fileName: String = "",
    @SerializedName("fileUrl") val fileUrl: String? = null,
    @SerializedName("importDate") val importDate: String = "",
    @SerializedName("openingBalance") val openingBalance: Double = 0.0,
    @SerializedName("closingBalance") val closingBalance: Double = 0.0,
    @SerializedName("totalDeposits") val totalDeposits: Double = 0.0,
    @SerializedName("totalWithdrawals") val totalWithdrawals: Double = 0.0,
    @SerializedName("lines") val lines: List<BankStatementLineDto> = emptyList(),
    @SerializedName("status") val status: String = "Uploaded",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)