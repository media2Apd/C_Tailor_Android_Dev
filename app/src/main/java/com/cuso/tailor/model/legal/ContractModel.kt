package com.cuso.tailor.model.legal

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

// ── Root response ──
data class LegalContractsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<LegalContractDto>? = emptyList(),
    @SerializedName("pagination") val pagination: LegalPagination? = null
)

data class LegalPagination(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

// ── Contract entity ──

data class LegalContractDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    // Populated object in this endpoint, kept as JsonElement because other endpoints may return a plain id
    @SerializedName("branchId") val branchId: JsonElement? = null,
    @SerializedName("contractType") val contractType: String? = null,
    @SerializedName("contractName") val contractName: String? = null,
    @SerializedName("contractNumber") val contractNumber: String? = null,
    @SerializedName("contractDescription") val contractDescription: String? = null,
    @SerializedName("countryName") val countryName: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("legalEntity") val legalEntity: String? = null,
    @SerializedName("partyType") val partyType: String? = null,
    @SerializedName("primaryParty") val primaryParty: String? = null,
    @SerializedName("secondaryParty") val secondaryParty: String? = null,
    @SerializedName("internalBusinessOwner") val internalBusinessOwner: LegalUserRef? = null,
    @SerializedName("legalOwner") val legalOwner: LegalUserRef? = null,
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null,
    @SerializedName("renewalType") val renewalType: String? = null,
    @SerializedName("renewalNoticePeriod") val renewalNoticePeriod: Int = 0,
    @SerializedName("contractValue") val contractValue: Double = 0.0,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("paymentTerms") val paymentTerms: LegalPaymentTerms? = null,
    @SerializedName("keyTermsAndConditions") val keyTermsAndConditions: String? = null,
    @SerializedName("riskLevel") val riskLevel: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("confidentiality") val confidentiality: String? = null,
    @SerializedName("contractCategory") val contractCategory: String? = null,
    @SerializedName("mainDocuments") val mainDocuments: List<LegalDocumentDto>? = emptyList(),
    @SerializedName("supportingDocuments") val supportingDocuments: List<LegalDocumentDto>? = emptyList(),
    @SerializedName("__v") val version: Int? = 1,
    @SerializedName("renewalNumber") val renewalNumber: Int = 0,
    @SerializedName("renewalStatus") val renewalStatus: String? = null,
    @SerializedName("proposedStartDate") val proposedStartDate: String? = null,
    @SerializedName("proposedEndDate") val proposedEndDate: String? = null,
    @SerializedName("renewalValue") val renewalValue: Double = 0.0,
    @SerializedName("renewalTerms") val renewalTerms: String? = null,
    @SerializedName("renewalOwner") val renewalOwner: LegalUserRef? = null,
    @SerializedName("submittedAt") val submittedAt: String? = null,
    @SerializedName("approvedAt") val approvedAt: String? = null,
    @SerializedName("approvedBy") val approvedBy: LegalUserRef? = null,
    @SerializedName("rejectedAt") val rejectedAt: String? = null,
    @SerializedName("rejectedBy") val rejectedBy: LegalUserRef? = null,
    @SerializedName("approvalComments") val approvalComments: String? = null,
    @SerializedName("completedAt") val completedAt: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean = false,
    @SerializedName("createdBy") val createdBy: LegalUserRef? = null,
    @SerializedName("updatedBy") val updatedBy: LegalUserRef? = null,
    @SerializedName("activity") val activity: List<LegalActivityDto>? = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// ── Nested entities ──

/** User reference. The adapter accepts either a plain id string or a populated object. */
@JsonAdapter(LegalUserRefAdapter::class)
data class LegalUserRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null
) {
    val fullName: String
        get() = listOfNotNull(firstName, lastName).joinToString(" ").trim()
}

data class LegalPaymentTerms(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("code") val code: String? = null,
    @SerializedName("termType") val termType: String? = null,
    @SerializedName("dueDays") val dueDays: Int = 0,
    @SerializedName("description") val description: String? = null,
    @SerializedName("status") val status: String? = null
)

data class LegalDocumentDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("documentName") val documentName: String? = null,
    @SerializedName("documentUrl") val documentUrl: String? = null,
    @SerializedName("cloudinaryPublicId") val cloudinaryPublicId: String? = null,
    @SerializedName("version") val version: Int = 1
)

data class LegalActivityDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("action") val action: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("performedBy") val performedBy: LegalUserRef? = null,
    @SerializedName("performedAt") val performedAt: String? = null
)

// ── Adapter ──

private fun JsonObject.str(key: String): String? =
    get(key)?.takeIf { it.isJsonPrimitive }?.asString

class LegalUserRefAdapter : JsonDeserializer<LegalUserRef?> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LegalUserRef? = when {
        json.isJsonNull -> null
        json.isJsonPrimitive -> LegalUserRef(id = json.asString)
        json.isJsonObject -> json.asJsonObject.let {
            LegalUserRef(
                id = it.str("_id") ?: "",
                firstName = it.str("firstName"),
                lastName = it.str("lastName")
            )
        }
        else -> null
    }
}

/**
 * Request payload for renewing a contract
 */
data class RenewContractRequest(
    @SerializedName("proposedEndDate") val proposedEndDate: String,
    @SerializedName("proposedStartDate") val proposedStartDate: String,
    @SerializedName("renewalOwner") val renewalOwner: String,
    @SerializedName("renewalTerms") val renewalTerms: String,
    @SerializedName("renewalValue") val renewalValue: Double?
)

/**
 * API Response returned when renewal is initiated
 */
data class LegalRenewContractResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: LegalContractDto?
)

/**
 * Request body for updating the contract renewal status
 */
data class UpdateContractRenewalStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("approvalComments") val approvalComments: String? = null
)

/**
 * Response returned from PATCH /api/legal/legal-contract/renewal-status/{id}
 */
data class LegalRenewStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: LegalContractDto?
)

/**
 * Standard API Response for Legal Contract Creation
 * Endpoint: POST /api/legal/legal-contract/create
 */
data class LegalContractCreateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: LegalContractDto?
)

/**
 * Standard API Response for Legal Contract Update
 * Endpoint: PUT /api/legal/legal-contract/update/{id}
 */
data class LegalContractUpdateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: LegalContractDto?
)