package com.cuso.tailor.model.legal

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

// ── Wrapper Responses ──

data class LegalLicenseListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<LegalLicenseItemDto>?,
    @SerializedName("pagination") val pagination: PaginationDto?,
    @SerializedName("message") val message: String?
)

data class LegalLicenseDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: LegalLicenseItemDto?,
    @SerializedName("message") val message: String?
)

data class LegalLicenseMutationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: LegalLicenseItemDto?
)

// ── Reusable Flexible References (Handles both raw String ID or populated Object) ──

@JsonAdapter(FlexibleRefAdapter::class)
data class FlexibleRef(
    val id: String = "",
    val name: String? = null
)

class FlexibleRefAdapter : JsonDeserializer<FlexibleRef> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlexibleRef {
        if (json == null || json.isJsonNull) return FlexibleRef()
        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            FlexibleRef(
                id = obj.get("_id")?.asString.orEmpty().ifBlank { obj.get("id")?.asString.orEmpty() },
                name = obj.get("name")?.asString
            )
        } else if (json.isJsonPrimitive) {
            FlexibleRef(id = json.asString)
        } else {
            FlexibleRef()
        }
    }
}

// ── Supporting Document & File DTOs ──

data class LicenseDocumentDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("public_id") val publicId: String? = null,
    @SerializedName("originalFilename") val originalFilename: String? = null,
    @SerializedName("uploadedAt") val uploadedAt: String? = null
)

data class LicenseActivityDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("activityType") val activityType: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("performedBy") val performedBy: FlexibleUserRef? = null,
    @SerializedName("performedAt") val performedAt: String? = null
)

// ── Main Legal License DTO ──

data class LegalLicenseItemDto(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branchId: FlexibleRef? = null,
    @SerializedName("licenseCode") val licenseCode: String? = null,
    @SerializedName("licenseType") val licenseType: String? = null,
    @SerializedName("licenseName") val licenseName: String? = null,
    @SerializedName("licenseNumber") val licenseNumber: String? = null,
    @SerializedName("issuingAuthority") val issuingAuthority: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("countryName") val countryName: String? = null,
    @SerializedName("region") val region: String? = null,
    @SerializedName("jurisdiction") val jurisdiction: String? = null,
    @SerializedName("legalEntity") val legalEntity: String? = null,
    @SerializedName("issueDate") val issueDate: String? = null,
    @SerializedName("effectiveDate") val effectiveDate: String? = null,
    @SerializedName("expiryDate") val expiryDate: String? = null,
    @SerializedName("renewalDate") val renewalDate: String? = null,
    @SerializedName("renewalNoticePeriod") val renewalNoticePeriod: Int? = null,
    @SerializedName("licenseOwner") val licenseOwner: FlexibleUserRef? = null,
    @SerializedName("responsibleTeam") val responsibleTeam: FlexibleRef? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("renewalStatus") val renewalStatus: String? = null,
    @SerializedName("riskLevel") val riskLevel: String? = null,
    @SerializedName("licenseCertificate") val licenseCertificate: List<LicenseDocumentDto>? = null,
    @SerializedName("supportingDocuments") val supportingDocuments: List<LicenseDocumentDto>? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("activities") val activities: List<LicenseActivityDto>? = null,
    @SerializedName("createdBy") val createdBy: FlexibleUserRef? = null,
    @SerializedName("updatedBy") val updatedBy: FlexibleUserRef? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean? = null,
    @SerializedName("renewalHistory") val renewalHistory: List<Any>? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("__v") val v: Int? = null
)

/**
 * Request payload for renewing a legal license
 */
data class RenewLicenseRequest(
    @SerializedName("renewalEffectiveDate") val renewalEffectiveDate: String,
    @SerializedName("renewalExpiryDate") val renewalExpiryDate: String,
    @SerializedName("renewalNotes") val renewalNotes: String? = null
)