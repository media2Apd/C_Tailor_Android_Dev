@file:Suppress("unused")

package com.cuso.tailor.model.legal

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

/**
 * List response wrapper for Legal Documents API
 */
data class LegalDocumentListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<LegalDocumentItemDto>?,
    @SerializedName("pagination") val pagination: PaginationDto?,
    @SerializedName("message") val message: String?
)

/**
 * Single view response wrapper for Legal Document API
 */
data class LegalDocumentDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: LegalDocumentDetailDto?,
    @SerializedName("message") val message: String?
)

/**
 * Delete response for Legal Document
 */
data class DeleteLegalDocumentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: JsonElement? = null
)

/**
 * Flexible User reference that safely parses either a raw String ID or a full user JSON object
 */
@JsonAdapter(FlexibleUserRefAdapter::class)
data class FlexibleUserRef(
    val id: String = "",
    val firstName: String? = null,
    val lastName: String? = null
) {
    val fullName: String
        get() = "${firstName.orEmpty()} ${lastName.orEmpty()}".trim().ifBlank { "-" }
}

class FlexibleUserRefAdapter : JsonDeserializer<FlexibleUserRef> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlexibleUserRef {
        if (json == null || json.isJsonNull) return FlexibleUserRef()
        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            FlexibleUserRef(
                id = obj.get("_id")?.asString.orEmpty(),
                firstName = obj.get("firstName")?.asString,
                lastName = obj.get("lastName")?.asString
            )
        } else if (json.isJsonPrimitive) {
            FlexibleUserRef(id = json.asString)
        } else {
            FlexibleUserRef()
        }
    }
}

/**
 * DTO representing an item in the Legal Documents List
 */
data class LegalDocumentItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("branchId") val branchId: BranchRef?,
    @SerializedName("documentName") val documentName: String?,
    @SerializedName("documentType") val documentType: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("documentNumber") val documentNumber: String?,
    @SerializedName("version") val version: String?,
    @SerializedName("relatedRecords") val relatedRecords: List<RelatedRecordDto>?,
    @SerializedName("issueDate") val issueDate: String?,
    @SerializedName("effectiveDate") val effectiveDate: String?,
    @SerializedName("expiryDate") val expiryDate: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("legalEntity") val legalEntity: String?,
    @SerializedName("confidentiality") val confidentiality: String?,
    @SerializedName("accessLevel") val accessLevel: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("versions") val versions: List<DocumentVersionDto>?,
    @SerializedName("activities") val activities: List<DocumentActivityDto>?,
    @SerializedName("status") val status: String?,
    @SerializedName("riskLevel") val riskLevel: String?,
    @SerializedName("createdBy") val createdBy: FlexibleUserRef?,
    @SerializedName("updatedBy") val updatedBy: FlexibleUserRef?,
    @SerializedName("isDeleted") val isDeleted: Boolean?,
    @SerializedName("access") val access: List<DocumentAccessDto>?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("__v") val v: Int?
)

/**
 * Detailed DTO representing a single Legal Document view
 */
data class LegalDocumentDetailDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String?,
    @SerializedName("branchId") val branchId: BranchRef?,
    @SerializedName("documentName") val documentName: String?,
    @SerializedName("documentType") val documentType: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("documentNumber") val documentNumber: String?,
    @SerializedName("version") val version: String?,
    @SerializedName("relatedRecords") val relatedRecords: List<RelatedRecordDto>?,
    @SerializedName("issueDate") val issueDate: String?,
    @SerializedName("effectiveDate") val effectiveDate: String?,
    @SerializedName("expiryDate") val expiryDate: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("legalEntity") val legalEntity: String?,
    @SerializedName("confidentiality") val confidentiality: String?,
    @SerializedName("accessLevel") val accessLevel: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("versions") val versions: List<DocumentVersionDto>?,
    @SerializedName("activities") val activities: List<DocumentActivityDto>?,
    @SerializedName("status") val status: String?,
    @SerializedName("riskLevel") val riskLevel: String?,
    @SerializedName("createdBy") val createdBy: FlexibleUserRef?,
    @SerializedName("updatedBy") val updatedBy: FlexibleUserRef?,
    @SerializedName("isDeleted") val isDeleted: Boolean?,
    @SerializedName("access") val access: List<DocumentAccessDto>?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("__v") val v: Int?
)

data class RelatedRecordDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("recordType") val recordType: String?,
    @SerializedName("recordId") val recordId: String?,
    @SerializedName("recordName") val recordName: String?
)

data class DocumentVersionDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("version") val version: String?,
    @SerializedName("files") val files: List<LegalComplianceDocumentDto>?,
    @SerializedName("uploadedBy") val uploadedBy: FlexibleUserRef?,
    @SerializedName("uploadedAt") val uploadedAt: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("isCurrentVersion") val isCurrentVersion: Boolean?
)

data class DocumentActivityDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("activityType") val activityType: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("performedBy") val performedBy: FlexibleUserRef?,
    @SerializedName("performedAt") val performedAt: String?
)

data class DocumentAccessDto(
    @SerializedName("_id") val id: String?,
    @SerializedName("roleName") val roleName: String?,
    @SerializedName("userOrRole") val userOrRole: String?,
    @SerializedName("accessType") val accessType: String?,
    @SerializedName("grantedBy") val grantedBy: String?,
    @SerializedName("accessDate") val accessDate: String?
)

/**
 * Request payload for updating legal document status
 */
data class UpdateDocumentStatusRequest(
    @SerializedName("status") val status: String,
    @SerializedName("comments") val comments: String? = null
)

/**
 * Response model received after updating legal document status
 */
data class UpdateDocumentStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: LegalDocumentDetailDto?
)