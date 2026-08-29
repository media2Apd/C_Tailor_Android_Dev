package com.cuso.mobile.model.finance

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class ChartOfAccountsResponse(
    val success: Boolean,
    val data: List<ChartOfAccountItem> = emptyList(),
    val total: Int? = null
)

data class CreatedByRef(
    @SerializedName("_id") val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val memberId: String? = null
)

class CreatedByDeserializer : JsonDeserializer<CreatedByRef?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): CreatedByRef? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                CreatedByRef(
                    id = obj.get("_id")?.asString,
                    firstName = obj.get("firstName")?.asString,
                    lastName = obj.get("lastName")?.asString,
                    memberId = obj.get("memberId")?.asString
                )
            }
            json.isJsonPrimitive -> CreatedByRef(id = json.asString)
            else -> null
        }
    }
}

data class ChartOfAccountItem(
    @SerializedName("_id") val _id: String,
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("accountName") val accountName: String = "",
    @SerializedName("accountCode") val accountCode: String = "",
    @SerializedName("accountType") val accountType: String = "",
    @SerializedName("category") val category: String? = null,

    // Custom adapter handles both nested object and plain string ID
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

    // Handles both Object and String createdBy field from API
    @JsonAdapter(CreatedByDeserializer::class)
    @SerializedName("createdBy") val createdBy: CreatedByRef? = null,

    @SerializedName("isEditable") val isEditable: Boolean = true,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = "",

    @JsonAdapter(CreatedByDeserializer::class)
    @SerializedName("updatedBy") val updatedBy: CreatedByRef? = null
)

class ParentAccountDeserializer : JsonDeserializer<ChartOfAccountItem?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ChartOfAccountItem? {
        if (json == null || json.isJsonNull) return null

        return if (json.isJsonPrimitive) {
            // Minimal placeholder when API sends parentAccount as a string ID
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
            // Full nested object
            context?.deserialize(json, ChartOfAccountItem::class.java)
        }
    }
}

fun ChartOfAccountItem.indentLevel(): Int = when {
    parentAccount == null -> 0
    parentAccount.parentAccount == null -> 1
    else -> 2
}

// ═══════════════════════════════════════════════════
// Expenses Models
// ═══════════════════════════════════════════════════

data class ExpenseListResponse(
    val success: Boolean,
    val pagination: ExpensePagination,
    val data: List<ExpenseItem> = emptyList()
)

data class ExpensePagination(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
)

// ═══════════════════════════════════════════════════
// Expenses Models with Flexible ID / Object Deserializer
// ═══════════════════════════════════════════════════

data class ExpenseAccountRef(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("accountName") val accountName: String = "",
    @SerializedName("accountCode") val accountCode: String = ""
)
class ExpenseAccountRefDeserializer : JsonDeserializer<ExpenseAccountRef?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ExpenseAccountRef? {
        if (json == null || json.isJsonNull) return null
        return if (json.isJsonPrimitive) {
            // Handled when API sends plain string ID e.g. "6a8c1cc569c9cae2acc78155"
            ExpenseAccountRef(
                _id = json.asString,
                accountName = "",
                accountCode = ""
            )
        } else if (json.isJsonObject) {
            // Handled when API sends full populated object
            context?.deserialize(json, ExpenseAccountRef::class.java)
        } else {
            null
        }
    }
}

data class ExpenseFile(
    val url: String,
    val publicId: String,
    val uploadedAt: String,
    val _id: String
)

data class ExpenseItem(
    val _id: String,
    val organizationId: String? = null,
    val branch: String? = null,
    val expenseNumber: String = "",
    val expenseDate: String = "",

    @JsonAdapter(ExpenseAccountRefDeserializer::class)
    val accountId: ExpenseAccountRef? = null,

    val amount: Double = 0.0,

    @JsonAdapter(ExpenseAccountRefDeserializer::class)
    val paymentAccountId: ExpenseAccountRef? = null,

    val referenceNumber: String? = null,
    val notes: String? = null,
    val status: String = "Paid",
    val files: List<ExpenseFile> = emptyList(),
    val createdBy: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val __v: Int? = null
)

data class ExpenseViewOneResponse(
    val success: Boolean,
    val data: ExpenseItem
)

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
    val createdBy: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int? = null
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

data class CreatedChartOfAccountData(
    val _id: String,
    val organizationId: String,
    val accountName: String,
    val accountCode: String,
    val accountType: String,
    val parentAccount: String?,
    val level: Int,
    val isGroup: Boolean,
    val isSystemAccount: Boolean,
    val openingBalance: Double,
    val currency: String,
    val allowManualEntry: Boolean,
    val reconciliation: Boolean,
    val description: String?,
    val status: String,
    val createdBy: String? = null,
    val isEditable: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int? = null
)

data class CreateChartOfAccountResponse(
    val success: Boolean,
    val message: String? = null,
    val data: CreatedChartOfAccountData? = null
)


// Top-level API Response
data class AccountDropdownResponse(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: List<AccountDropdownItem> = emptyList()
)

// Individual Account Item in the data array
data class AccountDropdownItem(
    @SerializedName("_id")
    val id: String,

    @SerializedName("accountName")
    val accountName: String,

    @SerializedName("accountCode")
    val accountCode: String,

    @SerializedName("accountType")
    val accountType: String,

    @SerializedName("normalBalance")
    val normalBalance: String? = null,

    @SerializedName("currency")
    val currency: String? = null
) {
    // Helper property to display in dropdown UI (e.g. "100000 - Assets Group")
    val displayName: String
        get() = "$accountCode - $accountName"
}