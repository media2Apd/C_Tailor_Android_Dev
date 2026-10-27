@file:Suppress("unused")
package com.cuso.tailor.model.legal

import com.google.gson.annotations.SerializedName

// ── Wrapper Responses ──

data class LegalCustomerListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<LegalCustomerItemDto>?,
    @SerializedName("pagination") val pagination: PaginationDto?,
    @SerializedName("message") val message: String?
)

data class LegalCustomerDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: LegalCustomerItemDto?,
    @SerializedName("message") val message: String?
)

data class LegalCustomerMutationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: LegalCustomerItemDto?
)

// ── Document DTO ──

data class LegalCustomerDocumentDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("documentName") val documentName: String? = null,
    @SerializedName("documentUrl") val documentUrl: String? = null,
    @SerializedName("publicId") val publicId: String? = null,
    @SerializedName("uploadedBy") val uploadedBy: FlexibleUserRef? = null,
    @SerializedName("uploadedAt") val uploadedAt: String? = null
)

// ── Main Customer Legal Item DTO ──

data class LegalCustomerItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("legalCustomerNumber") val legalCustomerNumber: String? = null,
    @SerializedName("customerId") val customerId: FlexibleRef? = null,
    @SerializedName("customerCode") val customerCode: String? = null,
    @SerializedName("legalMatterType") val legalMatterType: String? = null,
    @SerializedName("orderReference") val orderReference: String? = null,
    @SerializedName("invoiceReference") val invoiceReference: String? = null,
    @SerializedName("contractId") val contractId: FlexibleRef? = null,
    @SerializedName("consentType") val consentType: String? = null,
    @SerializedName("termsType") val termsType: String? = null,
    @SerializedName("cancellationTerms") val cancellationTerms: String? = null,
    @SerializedName("refundTerms") val refundTerms: String? = null,
    @SerializedName("effectiveDate") val effectiveDate: String? = null,
    @SerializedName("expiryDate") val expiryDate: String? = null,
    @SerializedName("disputeReference") val disputeReference: String? = null,
    @SerializedName("legalDescription") val legalDescription: String? = null,
    @SerializedName("riskLevel") val riskLevel: String? = null,
    @SerializedName("priority") val priority: String? = null,
    @SerializedName("countryName") val countryName: String? = null,
    @SerializedName("branchId") val branchId: FlexibleRef? = null,
    @SerializedName("legalOwner") val legalOwner: FlexibleUserRef? = null,
    @SerializedName("supportingDocuments") val supportingDocuments: List<LegalCustomerDocumentDto>? = null,
    @SerializedName("consentDocuments") val consentDocuments: List<LegalCustomerDocumentDto>? = null,
    @SerializedName("legalCorrespondence") val legalCorrespondence: List<LegalCustomerDocumentDto>? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("activities") val activities: List<LicenseActivityDto>? = null,
    @SerializedName("createdBy") val createdBy: FlexibleUserRef? = null,
    @SerializedName("updatedBy") val updatedBy: FlexibleUserRef? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("__v") val v: Int? = null
)












