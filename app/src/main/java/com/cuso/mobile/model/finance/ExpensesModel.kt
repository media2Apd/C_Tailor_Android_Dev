package com.cuso.mobile.model.finance

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type


data class ChartOfAccountsResponse(
    val success: Boolean,
    val data: List<ChartOfAccountItem>
)

data class ChartOfAccountItem(
    @SerializedName("_id") val _id: String,
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("accountName") val accountName: String = "",
    @SerializedName("accountCode") val accountCode: String = "",
    @SerializedName("accountType") val accountType: String = "",
    @SerializedName("category") val category: String? = null,

    // ✅ CHANGED — was a plain nested field before; now uses the custom
    // adapter so BOTH "parentAccount": {...} and "parentAccount": "id"
    // deserialize correctly without crashing.
    @JsonAdapter(ParentAccountDeserializer::class)
    @SerializedName("parentAccount") val parentAccount: ChartOfAccountItem? = null,

    @SerializedName("level") val level: Int = 0,
    @SerializedName("isGroup") val isGroup: Boolean = false,
    @SerializedName("isSystemAccount") val isSystemAccount: Boolean = false,
    @SerializedName("openingBalance") val openingBalance: Double = 0.0,
    @SerializedName("normalBalance") val normalBalance: String = "",
    @SerializedName("currency") val currency: String = "",
    @SerializedName("allowManualEntry") val allowManualEntry: Boolean = false,
    @SerializedName("reconciliation") val reconciliation: Boolean = false,
    @SerializedName("description") val description: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("createdBy") val createdBy: String? = null,
    @SerializedName("isEditable") val isEditable: Boolean = true,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = "",
    @SerializedName("updatedBy") val updatedBy: String? = null
)
class ParentAccountDeserializer : JsonDeserializer<ChartOfAccountItem?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ChartOfAccountItem? {
        if (json == null || json.isJsonNull) return null

        return if (json.isJsonPrimitive) {
            // Journal Entry shape: just the id string.
            // Build a minimal placeholder with only the id populated —
            // callers using parentAccount?._id (e.g. buildVisibleTree,
            // "is this a sub-account" checks) keep working as-is.
            ChartOfAccountItem(
                _id = json.asString,
                organizationId = "",
                accountName = "",
                accountCode = "",
                accountType = "",
                category = null,
                parentAccount = null,
                level = 0,
                isGroup = false,
                isSystemAccount = false,
                openingBalance = 0.0,
                normalBalance = "",
                currency = "",
                allowManualEntry = false,
                reconciliation = false,
                description = "",
                status = "",
                createdBy = null,
                isEditable = true,
                createdAt = "",
                updatedAt = "",
                updatedBy = null
            )
        } else {
            // Chart of Accounts shape: full nested object — deserialize normally.
            context?.deserialize(json, ChartOfAccountItem::class.java)
        }
    }
}


// ✅ FIXED — parentAccount.parentAccount is ChartOfAccountItem? now (not JsonElement?),
// so a plain null check is enough; .isJsonNull doesn't exist on this type.
fun ChartOfAccountItem.indentLevel(): Int = when {
    parentAccount == null -> 0
    parentAccount.parentAccount == null -> 1
    else -> 2
}


// ═══════════════════════════════════════════════════
// Expenses
// ═══════════════════════════════════════════════════

data class ExpenseListResponse(
    val success: Boolean,
    val pagination: ExpensePagination,
    val data: List<ExpenseItem>
)

data class ExpensePagination(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

data class ExpenseAccountRef(
    val _id: String,
    val accountName: String,
    val accountCode: String
)

data class ExpenseFile(
    val url: String,
    val publicId: String,
    val uploadedAt: String,
    val _id: String
)

data class ExpenseItem(
    val _id: String,
    val organizationId: String,
    val branch: String,
    val expenseNumber: String,
    val expenseDate: String,
    val accountId: ExpenseAccountRef,
    val amount: Double,
    val paymentAccountId: ExpenseAccountRef,
    val referenceNumber: String? = null,
    val notes: String? = null,
    val status: String,
    val files: List<ExpenseFile> = emptyList(),
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

// Single-expense detail response (view-one) — reuses ExpenseItem
data class ExpenseViewOneResponse(
    val success: Boolean,
    val data: ExpenseItem
)

// AFTER
// Create expense response — accountId / paymentAccountId come back as plain
// string IDs here (NOT populated objects like in list / view-one responses)
data class CreateExpenseData(
    val _id: String,
    val organizationId: String,
    val branch: String,
    val expenseNumber: String,
    val expenseDate: String,
    val accountId: String,
    val amount: Double,
    val paymentAccountId: String,
    val referenceNumber: String? = null,
    val notes: String? = null,
    val status: String,
    val files: List<ExpenseFile> = emptyList(),
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class CreateExpenseResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CreateExpenseData? = null
)

data class CreateChartOfAccountRequest(
    val accountName: String,
    val accountType: String,
    val description: String? = null,
    val parentAccount: String? = null
)

// Separate from ChartOfAccountItem because the CREATE endpoint returns
// parentAccount as a plain string id, not a nested object (unlike the
// GET /chart-of-accounts list endpoint, which nests the full parent object).
data class CreatedChartOfAccountData(
    val _id: String,
    val organizationId: String,
    val accountName: String,
    val accountCode: String,
    val accountType: String,
    val parentAccount: String?,   // ✅ String, not ChartOfAccountItem
    val level: Int,
    val isGroup: Boolean,
    val isSystemAccount: Boolean,
    val openingBalance: Double,
    val currency: String,
    val allowManualEntry: Boolean,
    val reconciliation: Boolean,
    val description: String?,
    val status: String,
    val createdBy: String,
    val isEditable: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)

data class CreateChartOfAccountResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CreatedChartOfAccountData? = null   // ✅ was ChartOfAccountItem — changed
)
