package com.cuso.tailor.model.finance

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
    @SerializedName("accountType") val accountType: String? = "",
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

data class ExpenseSupplierRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("supplierName") val supplierName: String = "",
    @SerializedName("companyName") val companyName: String = ""
)

class ExpenseSupplierRefDeserializer : JsonDeserializer<ExpenseSupplierRef?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ExpenseSupplierRef? {
        if (json == null || json.isJsonNull) return null
        return if (json.isJsonPrimitive) {
            ExpenseSupplierRef(id = json.asString)
        } else if (json.isJsonObject) {
            val obj = json.asJsonObject
            ExpenseSupplierRef(
                id = obj.get("_id")?.asString ?: obj.get("id")?.asString ?: "",
                supplierName = obj.get("supplierName")?.asString ?: obj.get("name")?.asString ?: "",
                companyName = obj.get("companyName")?.asString ?: ""
            )
        } else {
            null
        }
    }
}

data class ExpenseBranchRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = ""
)

class ExpenseBranchRefDeserializer : JsonDeserializer<ExpenseBranchRef?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ExpenseBranchRef? {
        if (json == null || json.isJsonNull) return null
        return if (json.isJsonPrimitive) {
            ExpenseBranchRef(id = json.asString)
        } else if (json.isJsonObject) {
            val obj = json.asJsonObject
            ExpenseBranchRef(
                id = obj.get("_id")?.asString ?: obj.get("id")?.asString ?: "",
                name = obj.get("name")?.asString ?: ""
            )
        } else {
            null
        }
    }
}

data class ExpenseTaxGroupRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("groupName") val groupName: String = "",
    @SerializedName("totalRate") val totalRate: Double = 0.0
)

class ExpenseTaxGroupRefDeserializer : JsonDeserializer<ExpenseTaxGroupRef?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ExpenseTaxGroupRef? {
        if (json == null || json.isJsonNull) return null
        return if (json.isJsonPrimitive) {
            ExpenseTaxGroupRef(id = json.asString)
        } else if (json.isJsonObject) {
            val obj = json.asJsonObject
            ExpenseTaxGroupRef(
                id = obj.get("_id")?.asString ?: obj.get("id")?.asString ?: "",
                groupName = obj.get("groupName")?.asString ?: obj.get("name")?.asString ?: "",
                totalRate = obj.get("totalRate")?.asDouble ?: 0.0
            )
        } else {
            null
        }
    }
}

data class ExpenseLineItem(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("lineType") val lineType: String = "Service",

    @JsonAdapter(ExpenseAccountRefDeserializer::class)
    @SerializedName("expenseAccountId") val expenseAccountId: ExpenseAccountRef? = null,

    @SerializedName("itemDescription") val itemDescription: String = "",
    @SerializedName("quantity") val quantity: Double = 1.0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("lineTotal") val lineTotal: Double = 0.0,

    @JsonAdapter(ExpenseTaxGroupRefDeserializer::class)
    @SerializedName("taxGroupId") val taxGroupId: ExpenseTaxGroupRef? = null,

    @SerializedName("taxExemptionReason") val taxExemptionReason: String? = null,
    @SerializedName("taxAmount") val taxAmount: Double = 0.0
)

data class ExpenseFile(
    val url: String,
    val publicId: String,
    val uploadedAt: String,
    val _id: String
)

data class ExpenseItem(
    @SerializedName("_id") val _id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,

    @JsonAdapter(ExpenseBranchRefDeserializer::class)
    @SerializedName("branchId") val branchId: ExpenseBranchRef? = null,
    @SerializedName("branch") val branch: String? = null,

    @JsonAdapter(ExpenseSupplierRefDeserializer::class)
    @SerializedName("supplierId") val supplierId: ExpenseSupplierRef? = null,

    @SerializedName("expenseNumber") val expenseNumber: String = "",
    @SerializedName("expenseDate") val expenseDate: String = "",

    @JsonAdapter(ExpenseAccountRefDeserializer::class)
    @SerializedName("accountId") val accountId: ExpenseAccountRef? = null,

    @JsonAdapter(ExpenseAccountRefDeserializer::class)
    @SerializedName("paidFromAccountId") val paidFromAccountId: ExpenseAccountRef? = null,

    @JsonAdapter(ExpenseAccountRefDeserializer::class)
    @SerializedName("paymentAccountId") val paymentAccountId: ExpenseAccountRef? = null,

    @SerializedName("paymentMode") val paymentMode: String? = null,
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("taxTotal") val taxTotal: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("priceIncludesTax") val priceIncludesTax: Boolean = false,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("exchangeRate") val exchangeRate: Double = 1.0,

    @SerializedName("referenceNumber") val referenceNumber: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("status") val status: String = "Paid",

    @SerializedName("lines") val lines: List<ExpenseLineItem> = emptyList(),
    @SerializedName("files") val files: List<ExpenseFile> = emptyList(),

    @JsonAdapter(CreatedByDeserializer::class)
    @SerializedName("createdBy") val createdBy: CreatedByRef? = null,

    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = "",
    @SerializedName("__v") val __v: Int? = null
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

data class ExpenseVoucherLineJsonRequest(
    @SerializedName("lineType") val lineType: String = "Service",
    @SerializedName("expenseAccountId") val expenseAccountId: String,
    @SerializedName("itemDescription") val itemDescription: String,
    @SerializedName("quantity") val quantity: Double = 1.0,
    @SerializedName("rate") val rate: Double = 0.0,
    @SerializedName("taxGroupId") val taxGroupId: String? = null,
    @SerializedName("taxExemptionReason") val taxExemptionReason: String? = null
)

data class ExpenseVoucherAttachmentDto(
    @SerializedName("url") val url: String,
    @SerializedName("publicId") val publicId: String,
    @SerializedName("name") val name: String = ""
)

data class ExpenseVoucherJsonRequest(
    @SerializedName("expenseDate") val expenseDate: String,
    @SerializedName("paidFromAccountId") val paidFromAccountId: String,
    @SerializedName("paymentMode") val paymentMode: String,
    @SerializedName("referenceNumber") val referenceNumber: String = "",
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("supplierId") val supplierId: String? = null,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("exchangeRate") val exchangeRate: Double = 1.0,
    @SerializedName("priceIncludesTax") val priceIncludesTax: Boolean = false,
    @SerializedName("notes") val notes: String = "",
    @SerializedName("status") val status: String = "Draft",
    @SerializedName("lines") val lines: List<ExpenseVoucherLineJsonRequest> = emptyList(),
    @SerializedName("attachments") val attachments: List<ExpenseVoucherAttachmentDto> = emptyList()
)