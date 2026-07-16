package com.cuso.mobile.model.finance

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class JournalLine(
    @JsonAdapter(AccountIdDeserializer::class)
    @SerializedName("accountId")
    val accountId: ChartOfAccountItem?,
    @SerializedName("debit")
    val debit: Double,
    @SerializedName("credit")
    val credit: Double,
    @SerializedName("description")
    val description: String?,
    @SerializedName("_id")
    val id: String
)


class AccountIdDeserializer : JsonDeserializer<ChartOfAccountItem?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ChartOfAccountItem? {
        if (json == null || json.isJsonNull) return null

        return if (json.isJsonPrimitive) {
            // Create-journal-entry response shape: accountId is just the id string.
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
            // View/list endpoint shape: full populated account object.
            context?.deserialize(json, ChartOfAccountItem::class.java)
        }
    }
}
data class JournalEntryItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String,
    @SerializedName("branchId") val branchId: String?,
    @SerializedName("entryNumber") val entryNumber: String,
    @SerializedName("entryDate") val entryDate: String,
    @SerializedName("reference") val reference: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("isManual") val isManual: Boolean,
    @SerializedName("lines") val lines: List<JournalLine>,
    @SerializedName("status") val status: String,          // "Posted" | "Pending"
    @SerializedName("createdBy") val createdBy: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
) {
    // ── UI-friendly derived fields (screenshot shows one representative account) ──
    private val firstLine: JournalLine? get() = lines.firstOrNull()

    val primaryAccountName: String
        get() = firstLine?.accountId?.accountName ?: entryNumber

    val primaryAccountCode: String
        get() = firstLine?.accountId?.accountCode ?: "-"

    val primaryAccountType: String
        get() = firstLine?.accountId?.accountType ?: "-"

    val subAccountLabel: String
        get() = firstLine?.accountId?.category ?: "-"
}

data class JournalEntryPagination(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class JournalEntryListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: JournalEntryPagination,
    @SerializedName("data") val data: List<JournalEntryItem>
)


data class CreateJournalEntryRequest(
    @SerializedName("branchId") val branchId: String,
    @SerializedName("entryDate") val entryDate: String,
    @SerializedName("reference") val reference: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("status") val status: String,
    @SerializedName("lines") val lines: List<JournalEntryLineRequest>
)

data class JournalEntryLineRequest(
    @SerializedName("accountId") val accountId: String,
    @SerializedName("debit") val debit: Double,
    @SerializedName("credit") val credit: Double,
    @SerializedName("description") val description: String?
)

data class CreateJournalEntryResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: JournalEntryItem?
)