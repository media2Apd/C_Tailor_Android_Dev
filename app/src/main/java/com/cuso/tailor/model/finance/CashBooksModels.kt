package com.cuso.tailor.model.finance

import com.google.gson.annotations.SerializedName

data class CashDrawerShiftListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<CashDrawerShiftItemDto> = emptyList()
)

data class CashDrawerShiftDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: CashDrawerShiftItemDto?
)

data class CashDrawerShiftItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branchId: BranchRefDto? = null,
    @SerializedName("shiftNumber") val shiftNumber: String = "",
    @SerializedName("cashierId") val cashierId: CashierRefDto? = null,
    @SerializedName("cashAccountId") val cashAccountId: CashAccountRefDto? = null,
    @SerializedName("openingTime") val openingTime: String? = null,
    @SerializedName("closingTime") val closingTime: String? = null,
    @SerializedName("openingBalance") val openingBalance: Double = 0.0,
    @SerializedName("totalCashCollected") val totalCashCollected: Double = 0.0,
    @SerializedName("totalCashWithdrawn") val totalCashWithdrawn: Double = 0.0,
    @SerializedName("expectedClosingBalance") val expectedClosingBalance: Double = 0.0,
    @SerializedName("actualClosingBalance") val actualClosingBalance: Double = 0.0,
    @SerializedName("discrepancyAmount") val discrepancyAmount: Double = 0.0,
    @SerializedName("status") val status: String = "",
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("closedBy") val closedBy: CashierRefDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

//data class BranchRefDto(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("name") val name: String? = null
//)

data class CashierRefDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("memberId") val memberId: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { "—" }
}

data class CashAccountRefDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("accountName") val accountName: String? = null,
    @SerializedName("accountCode") val accountCode: String? = null,
    @SerializedName("currency") val currency: String? = null
)

/**
 * Payload sent to start/open a new cash drawer shift
 * Endpoint: POST /api/finance/cash-drawer/shifts/open
 */
data class OpenCashShiftRequest(
    @SerializedName("branchId") val branchId: String,
    @SerializedName("cashAccountId") val cashAccountId: String,
    @SerializedName("openingBalance") val openingBalance: Double,
    @SerializedName("openingTime") val openingTime: String,
    @SerializedName("notes") val notes: String = ""
)

data class OpenCashShiftResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: CashDrawerShiftItemDto?
)

/**
 * Payload sent to close and tally a cash drawer shift session
 * Endpoint: POST /api/finance/cash-drawer/shifts/close/{id}
 */
data class CloseCashShiftRequest(
    @SerializedName("actualClosingBalance") val actualClosingBalance: Double,
    @SerializedName("closingTime") val closingTime: String,
//    @SerializedName("closeNotes") val closeNotes: String = ""
)

data class CloseCashShiftResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: CashDrawerShiftItemDto?
)