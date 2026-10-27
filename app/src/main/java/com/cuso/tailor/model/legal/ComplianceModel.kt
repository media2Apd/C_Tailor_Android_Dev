package com.cuso.tailor.model.legal

import com.google.gson.annotations.SerializedName

data class LegalComplianceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<LegalComplianceDto>?,
    @SerializedName("pagination") val pagination: PaginationDto?,
    @SerializedName("message") val message: String?
)

data class LegalComplianceDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("branchId") val branchId: BranchRef?,
    @SerializedName("complianceCode") val complianceCode: String?,
    @SerializedName("complianceName") val complianceName: String?,
    @SerializedName("complianceCategory") val complianceCategory: String?,
    @SerializedName("requirementType") val requirementType: String?,
    @SerializedName("regulationReference") val regulationReference: String?,
    @SerializedName("sourceAuthority") val sourceAuthority: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("jurisdiction") val jurisdiction: String?,
    @SerializedName("legalEntity") val legalEntity: String?,
    @SerializedName("applicability") val applicability: String?,
    @SerializedName("frequency") val frequency: String?,
    @SerializedName("effectiveDate") val effectiveDate: String?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("renewalReviewDate") val renewalReviewDate: String?,
    @SerializedName("nextDueDate") val nextDueDate: String?,
    @SerializedName("responsibleTeam") val responsibleTeam: String?,
    @SerializedName("complianceOwner") val complianceOwner: UserRef?,
    @SerializedName("reviewer") val reviewer: UserRef?,
    @SerializedName("riskLevel") val riskLevel: String?,
    @SerializedName("priority") val priority: String?,
    @SerializedName("requiredEvidence") val requiredEvidence: String?,
    @SerializedName("supportingDocuments") val supportingDocuments: List<LegalComplianceDocumentDto>?,
    @SerializedName("status") val status: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
)

data class BranchRef(
    @SerializedName("_id") val id: String
)

data class UserRef(
    @SerializedName("_id") val id: String,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?
) {
    val fullName: String
        get() = "${firstName.orEmpty()} ${lastName.orEmpty()}".trim().ifBlank { "-" }
}

data class LegalComplianceDocumentDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("public_id") val publicId: String?,
    @SerializedName("originalFilename") val originalFilename: String?,
    @SerializedName("uploadedAt") val uploadedAt: String?
)

data class PaginationDto(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int,
    @SerializedName("hasNextPage") val hasNextPage: Boolean,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean
)

/**
 * Top-level response wrapper for View-One Legal Compliance API
 */
data class LegalComplianceDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: LegalComplianceDetailDto?,
    @SerializedName("message") val message: String?
)

/**
 * Detailed DTO model representing a single Legal Compliance record
 */
data class LegalComplianceDetailDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("branchId") val branchId: BranchRef?,
    @SerializedName("complianceCode") val complianceCode: String?,
    @SerializedName("complianceName") val complianceName: String?,
    @SerializedName("complianceCategory") val complianceCategory: String?,
    @SerializedName("requirementType") val requirementType: String?,
    @SerializedName("regulationReference") val regulationReference: String?,
    @SerializedName("sourceAuthority") val sourceAuthority: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("jurisdiction") val jurisdiction: String?,
    @SerializedName("legalEntity") val legalEntity: String?,
    @SerializedName("applicability") val applicability: String?,
    @SerializedName("frequency") val frequency: String?,
    @SerializedName("effectiveDate") val effectiveDate: String?,
    @SerializedName("dueDate") val dueDate: String?,
    @SerializedName("renewalReviewDate") val renewalReviewDate: String?,
    @SerializedName("nextDueDate") val nextDueDate: String?,
    @SerializedName("responsibleTeam") val responsibleTeam: String?,
    @SerializedName("complianceOwner") val complianceOwner: UserRef?,
    @SerializedName("reviewer") val reviewer: UserRef?,
    @SerializedName("riskLevel") val riskLevel: String?,
    @SerializedName("priority") val priority: String?,
    @SerializedName("requiredEvidence") val requiredEvidence: String?,
    @SerializedName("supportingDocuments") val supportingDocuments: List<LegalComplianceDocumentDto>?,
    @SerializedName("evidenceVerifiedBy") val evidenceVerifiedBy: UserRef?,
    @SerializedName("evidenceVerifiedAt") val evidenceVerifiedAt: String?,
    @SerializedName("verificationNotes") val verificationNotes: String?,
    @SerializedName("activities") val activities: List<ComplianceActivityDto>?,
    @SerializedName("status") val status: String?,
    @SerializedName("lastCompletedAt") val lastCompletedAt: String?,
    @SerializedName("completedBy") val completedBy: UserRef?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("createdBy") val createdBy: UserRef?,
    @SerializedName("updatedBy") val updatedBy: UserRef?,
    @SerializedName("isDeleted") val isDeleted: Boolean?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("__v") val v: Int?
)

/**
 * Activity log entry tracking status changes and updates
 */
data class ComplianceActivityDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("activityType") val activityType: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("performedBy") val performedBy: UserRef?,
    @SerializedName("performedAt") val performedAt: String?
)

/**
 * Request payload for updating legal compliance status
 */
data class UpdateComplianceStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("comments") val comments: String? = null
)

/**
 * Response received when updating legal compliance status
 */
data class UpdateComplianceStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: LegalComplianceDetailDto?
)

data class DeleteComplianceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: LegalComplianceDetailDto?
)