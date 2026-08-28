package com.cuso.mobile.model.finance

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

// ─────────────────────────────────────────────────────────────
// Shared Deserializer for createdBy/updatedBy (Object or String)
// ─────────────────────────────────────────────────────────────

data class JournalCreatedByRef(
    @SerializedName("_id") val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val memberId: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").ifBlank { id ?: "-" }
}

class JournalCreatedByDeserializer : JsonDeserializer<JournalCreatedByRef?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): JournalCreatedByRef? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                JournalCreatedByRef(
                    id = obj.get("_id")?.asString,
                    firstName = obj.get("firstName")?.asString,
                    lastName = obj.get("lastName")?.asString,
                    memberId = obj.get("memberId")?.asString
                )
            }
            json.isJsonPrimitive -> JournalCreatedByRef(id = json.asString)
            else -> null
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Journal Line & Account Deserialization
// ─────────────────────────────────────────────────────────────

data class JournalLine(
    @JsonAdapter(AccountIdDeserializer::class)
    @SerializedName("accountId")
    val accountId: ChartOfAccountItem?,
    @SerializedName("debit")
    val debit: Double = 0.0,
    @SerializedName("credit")
    val credit: Double = 0.0,
    @SerializedName("description")
    val description: String? = null,
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
            // When accountId is just a string ID
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
            // When accountId is a fully populated JSON object
            context?.deserialize(json, ChartOfAccountItem::class.java)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Journal Entry List Response Models
// ─────────────────────────────────────────────────────────────

data class JournalEntryItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("entryNumber") val entryNumber: String = "",
    @SerializedName("entryDate") val entryDate: String = "",
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("referenceType") val referenceType: String? = null,
    @SerializedName("referenceId") val referenceId: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("isManual") val isManual: Boolean = false,
    @SerializedName("lines") val lines: List<JournalLine> = emptyList(),
    @SerializedName("status") val status: String = "",
    @JsonAdapter(JournalCreatedByDeserializer::class)
    @SerializedName("createdBy") val createdBy: JournalCreatedByRef? = null,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
) {
    private val firstLine: JournalLine? get() = lines.firstOrNull()

    val primaryAccountName: String
        get() = firstLine?.accountId?.accountName?.takeIf { it.isNotBlank() } ?: entryNumber

    val primaryAccountCode: String
        get() = firstLine?.accountId?.accountCode?.takeIf { it.isNotBlank() } ?: "-"

    val primaryAccountType: String
        get() = firstLine?.accountId?.accountType?.takeIf { it.isNotBlank() } ?: "-"
}

data class JournalEntryPagination(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 1
)

data class JournalEntryListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: JournalEntryPagination? = null,
    @SerializedName("data") val data: List<JournalEntryItem> = emptyList()
)

// ─────────────────────────────────────────────────────────────
// Create & Update Journal Entry Requests & Responses
// ─────────────────────────────────────────────────────────────

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

// ─────────────────────────────────────────────────────────────
// Ledger Models
// ─────────────────────────────────────────────────────────────

data class LedgerResponse(
    val success: Boolean,
    @JsonAdapter(LedgerDataDeserializer::class)
    val data: List<LedgerItem> = emptyList()
)

data class LedgerItem(
    @SerializedName("_id") val id: String,
    val date: String? = null,
    val account: String? = null,
    val code: String? = null,
    val journalNumber: String? = null,
    val reference: String? = null,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val balance: Double = 0.0
)

class LedgerDataDeserializer : JsonDeserializer<List<LedgerItem>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<LedgerItem> {
        if (json == null || json.isJsonNull) return emptyList()

        return when {
            json.isJsonArray -> {
                val list = mutableListOf<LedgerItem>()
                json.asJsonArray.forEach { elem ->
                    context?.deserialize<LedgerItem>(elem, LedgerItem::class.java)?.let { list.add(it) }
                }
                list
            }
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val arrayElem = obj.get("list")
                    ?: obj.get("entries")
                    ?: obj.get("ledger")
                    ?: obj.get("data")

                if (arrayElem != null && arrayElem.isJsonArray) {
                    val list = mutableListOf<LedgerItem>()
                    arrayElem.asJsonArray.forEach { elem ->
                        context?.deserialize<LedgerItem>(elem, LedgerItem::class.java)?.let { list.add(it) }
                    }
                    list
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Journal Entry Detail & Update Models
// ─────────────────────────────────────────────────────────────

data class JournalEntryDetailResponse(
    val success: Boolean,
    val data: JournalEntryDetailData? = null
)

data class JournalEntryDetailData(
    @SerializedName("_id") val id: String,
    val organizationId: String? = null,
    val branchId: String? = null,
    val entryNumber: String = "",
    val entryDate: String = "",
    val reference: String? = null,
    val notes: String? = null,
    val isManual: Boolean = false,
    val lines: List<JournalLineDetail> = emptyList(),
    val status: String = "",
    @JsonAdapter(JournalCreatedByDeserializer::class)
    val createdBy: JournalCreatedByRef? = null,
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
    val accountName: String = "",
    val accountCode: String = "",
    val accountType: String? = null
)

data class UpdateJournalEntryRequest(
    val branchId: String,
    val entryDate: String,
    val reference: String?,
    val notes: String?,
    val status: String,
    val lines: List<JournalEntryLineRequest>
)

data class UpdateJournalEntryResponse(
    val success: Boolean,
    val message: String? = null,
    val data: UpdateJournalEntryData? = null
)

data class UpdateJournalEntryData(
    val _id: String,
    val organizationId: String? = null,
    val branchId: String? = null,
    val entryNumber: String = "",
    val entryDate: String = "",
    val reference: String? = null,
    val notes: String? = null,
    val isManual: Boolean = false,
    val lines: List<UpdateJournalEntryLine> = emptyList(),
    val status: String = "",
    @JsonAdapter(JournalCreatedByDeserializer::class)
    val createdBy: JournalCreatedByRef? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @JsonAdapter(JournalCreatedByDeserializer::class)
    val updatedBy: JournalCreatedByRef? = null
)

data class UpdateJournalEntryLine(
    val accountId: String,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val description: String? = null,
    val _id: String? = null
)