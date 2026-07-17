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


//ledger model

data class LedgerResponse(
    val success: Boolean,
    val data: List<LedgerItem> = emptyList()
)

data class LedgerItem(
    @SerializedName("_id") val id: String,
    val date: String,
    val account: String,
    val code: String,
    val journalNumber: String? = null,
    val reference: String? = null,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val balance: Double = 0.0
)

data class JournalEntryDetailResponse(
    val success: Boolean,
    val data: JournalEntryDetailData? = null
)

data class JournalEntryDetailData(
    @SerializedName("_id") val id: String,
    val organizationId: String? = null,
    val branchId: String? = null,
    val entryNumber: String,
    val entryDate: String,
    val reference: String? = null,
    val notes: String? = null,
    val isManual: Boolean = false,
    val lines: List<JournalLineDetail> = emptyList(),
    val status: String,
    val createdBy: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class JournalLineDetail(
    @SerializedName("_id") val id: String,
    val accountId: JournalLineAccountRef,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val description: String? = null
)

data class JournalLineAccountRef(
    @SerializedName("_id") val id: String,
    val accountName: String,
    val accountCode: String,
    val accountType: String? = null
    // rest of the account fields (category, level, isGroup, etc.) aren't needed
    // for the form — Gson silently ignores extra JSON fields, so no crash risk.
)

// Request body for PUT /api/finance/journal-entries/{id}
// Same shape as CreateJournalEntryRequest, minus the id (that goes in the path).
data class UpdateJournalEntryRequest(
    val branchId: String,
    val entryDate: String,
    val reference: String?,
    val notes: String?,
    val status: String,
    val lines: List<JournalEntryLineRequest>
)

// Response shape matches the JSON you pasted:
// { "success": true, "data": { ...JournalEntryDetailData... } }
// Reuses your existing JournalEntryDetailData model since the "data" object
// there is identical in shape to what getJournalEntryDetail() already returns.
data class UpdateJournalEntryResponse(
    val success: Boolean,
    val message: String? = null,
    val data: UpdateJournalEntryData? = null   // ✅ புதுசா தனி data class
)

data class UpdateJournalEntryData(
    val _id: String,
    val organizationId: String,
    val branchId: String,
    val entryNumber: String,
    val entryDate: String,
    val reference: String?,
    val notes: String?,
    val isManual: Boolean,
    val lines: List<UpdateJournalEntryLine>,
    val status: String,
    val createdBy: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val updatedBy: String?
)

data class UpdateJournalEntryLine(
    val accountId: String,   // ✅ String, object இல்ல — உங்க image-ல வந்த screenshot response அப்படி தான் இருக்கு
    val debit: Double,
    val credit: Double,
    val description: String?,
    val _id: String?
)