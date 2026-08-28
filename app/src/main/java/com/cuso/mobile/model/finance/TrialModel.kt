package com.cuso.mobile.model.finance

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────
// Trial Balance — API Response
// Matches: { "success": true, "data": { "list": [...], "totals": {...} } }
// ─────────────────────────────────────────────────────────────

data class TrialBalanceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: TrialBalanceData? = null
)

data class TrialBalanceData(
    @SerializedName("list") val list: List<TrialBalanceItem> = emptyList(),
    @SerializedName("totals") val totals: TrialBalanceTotals? = null
)

data class TrialBalanceTotals(
    @SerializedName("totalDebit") val totalDebit: Double = 0.0,
    @SerializedName("totalCredit") val totalCredit: Double = 0.0
)

data class TrialBalanceItem(
    @SerializedName("_id") val id: String,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("accountCode") val accountCode: String? = null,
    @SerializedName("accountType") val accountType: String,
    @SerializedName("totalDebit") val totalDebit: Double = 0.0,
    @SerializedName("totalCredit") val totalCredit: Double = 0.0,
    @SerializedName("balance") val netBalance: Double = 0.0
) {
    // Backward compatibility helpers for UI
    val accountId: String get() = id
    val account: String get() = accountName
    val code: String get() = accountCode ?: "-"
    val debit: Double get() = totalDebit
    val credit: Double get() = totalCredit

    val balanceLabel: String
        get() = if (netBalance >= 0) "DR" else "CR"

    val balanceAbs: Double
        get() = kotlin.math.abs(netBalance)
}