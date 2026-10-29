@file:Suppress("unused", "RedundantVisibilityModifier")

package com.cuso.tailor.model.legal

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

// ── Flexible Reference Deserializers ──

class FlexibleLegalNoticeBranchRefAdapter : JsonDeserializer<LegalNoticeBranchRef> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LegalNoticeBranchRef {
        if (json == null || json.isJsonNull) return LegalNoticeBranchRef()
        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            LegalNoticeBranchRef(
                id = obj.get("_id")?.asString.orEmpty(),
                name = obj.get("name")?.asString.orEmpty(),
                branchCode = obj.get("branchCode")?.asString.orEmpty()
            )
        } else {
            LegalNoticeBranchRef(id = json.asString)
        }
    }
}

class FlexibleLegalNoticeUserRefAdapter : JsonDeserializer<LegalNoticeUserRef> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LegalNoticeUserRef {
        if (json == null || json.isJsonNull) return LegalNoticeUserRef()
        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            LegalNoticeUserRef(
                id = obj.get("_id")?.asString.orEmpty(),
                firstName = obj.get("firstName")?.asString.orEmpty(),
                lastName = obj.get("lastName")?.asString.orEmpty(),
                memberId = obj.get("memberId")?.asString.orEmpty()
            )
        } else {
            LegalNoticeUserRef(id = json.asString)
        }
    }
}

class FlexibleLegalNoticeContractRefAdapter : JsonDeserializer<LegalNoticeContractRef> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LegalNoticeContractRef {
        if (json == null || json.isJsonNull) return LegalNoticeContractRef()
        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            LegalNoticeContractRef(
                id = obj.get("_id")?.asString.orEmpty(),
                contractName = obj.get("contractName")?.asString.orEmpty(),
                contractNumber = obj.get("contractNumber")?.asString.orEmpty(),
                contractType = obj.get("contractType")?.asString.orEmpty()
            )
        } else {
            LegalNoticeContractRef(id = json.asString)
        }
    }
}

// Flexible Deserializer for Invoice: Handles both string ID and populated Object
class FlexibleLegalNoticeInvoiceRefAdapter : JsonDeserializer<LegalNoticeInvoiceRef> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LegalNoticeInvoiceRef {
        if (json == null || json.isJsonNull) return LegalNoticeInvoiceRef()
        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            LegalNoticeInvoiceRef(
                id = obj.get("_id")?.asString.orEmpty(),
                invoiceNumber = obj.get("invoiceNumber")?.asString.orEmpty(),
                currency = obj.get("currency")?.asString.orEmpty(),
                grandTotal = obj.get("grandTotal")?.asDouble ?: 0.0,
                status = obj.get("status")?.asString.orEmpty()
            )
        } else {
            LegalNoticeInvoiceRef(id = json.asString)
        }
    }
}

// Flexible Deserializer for Order: Handles both String ID and populated Object
class FlexibleLegalNoticeOrderRefAdapter : JsonDeserializer<LegalNoticeOrderRef> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LegalNoticeOrderRef {
        if (json == null || json.isJsonNull) return LegalNoticeOrderRef()
        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            LegalNoticeOrderRef(
                id = obj.get("_id")?.asString.orEmpty(),
                orderNumber = obj.get("orderNumber")?.asString.orEmpty(),
                salesOrderNumber = obj.get("salesOrderNumber")?.asString.orEmpty(),
                orderNo = obj.get("orderNo")?.asString.orEmpty()
            )
        } else {
            LegalNoticeOrderRef(id = json.asString)
        }
    }
}

data class LegalNoticeBranchRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("branchCode") val branchCode: String = ""
)

data class LegalNoticeUserRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = "",
    @SerializedName("memberId") val memberId: String = ""
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

data class LegalNoticeContractRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("contractName") val contractName: String = "",
    @SerializedName("contractNumber") val contractNumber: String = "",
    @SerializedName("contractType") val contractType: String = ""
)

data class LegalNoticeInvoiceRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("invoiceNumber") val invoiceNumber: String = "",
    @SerializedName("currency") val currency: String = "",
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("status") val status: String = ""
)

data class LegalNoticeOrderRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("orderNumber") val orderNumber: String = "",
    @SerializedName("salesOrderNumber") val salesOrderNumber: String = "",
    @SerializedName("orderNo") val orderNo: String = ""
) {
    val displayOrderNumber: String
        get() = orderNumber.ifBlank { salesOrderNumber.ifBlank { orderNo.ifBlank { if (id.isNotBlank()) "#${id.takeLast(6)}" else "-" } } }
}

data class LegalNoticeDocumentItem(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("documentUrl") val documentUrl: String = "",
    @SerializedName("publicId") val publicId: String = "",
    @SerializedName("documentType") val documentType: String = "",
    @JsonAdapter(FlexibleLegalNoticeUserRefAdapter::class)
    @SerializedName("uploadedBy") val uploadedBy: LegalNoticeUserRef? = null,
    @SerializedName("uploadedAt") val uploadedAt: String = ""
)

data class LegalNoticeActivityItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("action") val action: String = "",
    @SerializedName("description") val description: String = "",
    @JsonAdapter(FlexibleLegalNoticeUserRefAdapter::class)
    @SerializedName("performedBy") val performedBy: LegalNoticeUserRef? = null,
    @SerializedName("performedAt") val performedAt: String = ""
)

// Main Legal Notice Item DTO
data class LegalNoticeItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("caseNumber") val caseNumber: String = "",
    @SerializedName("caseType") val caseType: String = "",
    @SerializedName("caseTitle") val caseTitle: String = "",
    @SerializedName("caseDescription") val caseDescription: String = "",
    @SerializedName("primaryParty") val primaryParty: String = "",
    @SerializedName("partyType") val partyType: String = "",

    // Flexible deserializer handles both String ID and populated Object
    @JsonAdapter(FlexibleLegalNoticeOrderRefAdapter::class)
    @SerializedName("relatedOrder") val relatedOrder: LegalNoticeOrderRef? = null,

    // Flexible deserializer handles both String ID and populated Object
    @JsonAdapter(FlexibleLegalNoticeInvoiceRefAdapter::class)
    @SerializedName("relatedInvoice") val relatedInvoice: LegalNoticeInvoiceRef? = null,

    @JsonAdapter(FlexibleLegalNoticeContractRefAdapter::class)
    @SerializedName("relatedContract") val relatedContract: LegalNoticeContractRef? = null,
    @SerializedName("relatedRecord") val relatedRecord: String? = null,
    @SerializedName("riskLevel") val riskLevel: String = "Low",
    @SerializedName("country") val country: String = "",
    @SerializedName("countryName") val countryName: String? = null,
    @JsonAdapter(FlexibleLegalNoticeBranchRefAdapter::class)
    @SerializedName("branchId") val branchId: LegalNoticeBranchRef? = null,
    @SerializedName("nextDeadline") val nextDeadline: String = "",
    @SerializedName("priority") val priority: String = "Medium",
    @SerializedName("jurisdiction") val jurisdiction: String = "",
    @JsonAdapter(FlexibleLegalNoticeUserRefAdapter::class)
    @SerializedName("legalOwner") val legalOwner: LegalNoticeUserRef? = null,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("caseStatus") val caseStatus: String? = null,
    @SerializedName("caseDocuments") val caseDocuments: List<LegalNoticeDocumentItem> = emptyList(),
    @SerializedName("supportingEvidence") val supportingEvidence: List<LegalNoticeDocumentItem> = emptyList(),
    @SerializedName("notes") val notes: String = "",
    @SerializedName("activity") val activity: List<LegalNoticeActivityItem> = emptyList(),
    @JsonAdapter(FlexibleLegalNoticeUserRefAdapter::class)
    @SerializedName("createdBy") val createdBy: LegalNoticeUserRef? = null,
    @JsonAdapter(FlexibleLegalNoticeUserRefAdapter::class)
    @SerializedName("updatedBy") val updatedBy: LegalNoticeUserRef? = null,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
) {
    val displayCountry: String
        get() = countryName ?: country
}

data class LegalNoticeListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<LegalNoticeItem>? = null,
    @SerializedName("pagination") val pagination: LegalVendorPaginationData? = null
)

data class LegalNoticeDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: LegalNoticeItem? = null
)

data class UpdateNoticeStatusRequest(
    @SerializedName("caseStatus") val caseStatus: String? = null
)