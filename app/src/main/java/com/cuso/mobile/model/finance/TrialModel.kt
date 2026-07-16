package com.cuso.mobile.model.finance

import com.google.gson.annotations.SerializedName

data class TrialBalanceItem(
    @SerializedName("accountId") val accountId: String,
    @SerializedName("account") val account: String,
    @SerializedName("code") val code: String,
    @SerializedName("accountType") val accountType: String,
    @SerializedName("debit") val debit: Double,
    @SerializedName("credit") val credit: Double
) {
    // net = debit - credit → positive = Debit balance (DR), negative = Credit balance (CR)
    val balance: Double
        get() = debit - credit

    val balanceLabel: String
        get() = if (balance >= 0) "DR" else "CR"

    val balanceAbs: Double
        get() = kotlin.math.abs(balance)
}

data class TrialBalanceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<TrialBalanceItem>
)