package com.cuso.tailor.model.legal

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class LegalRef(
    val id: String = "",
    val name: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val memberId: String = "",
    val contractType: String = "",
    val contractName: String = "",
    val contractNumber: String = ""
) {
    val displayName: String
        get() = when {
            name.isNotEmpty() -> name
            firstName.isNotEmpty() || lastName.isNotEmpty() -> "$firstName $lastName".trim()
            contractName.isNotEmpty() -> contractName
            memberId.isNotEmpty() -> memberId
            else -> id
        }
}

class FlexibleLegalRefAdapter : JsonDeserializer<LegalRef?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LegalRef? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                LegalRef(
                    id = obj.get("_id")?.asString.orEmpty(),
                    name = obj.get("name")?.asString.orEmpty(),
                    firstName = obj.get("firstName")?.asString.orEmpty(),
                    lastName = obj.get("lastName")?.asString.orEmpty(),
                    memberId = obj.get("memberId")?.asString.orEmpty(),
                    contractType = obj.get("contractType")?.asString.orEmpty(),
                    contractName = obj.get("contractName")?.asString.orEmpty(),
                    contractNumber = obj.get("contractNumber")?.asString.orEmpty()
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                LegalRef(id = json.asString)
            }
            else -> null
        }
    }
}

data class LegalDocumentItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("documentName") val documentName: String = "",
    @SerializedName("documentUrl") val documentUrl: String = "",
    @SerializedName("publicId") val publicId: String = "",
    @SerializedName("uploadedBy") val uploadedBy: String = "",
    @SerializedName("uploadedAt") val uploadedAt: String = ""
)

data class LegalActivityItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("activityType") val activityType: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("performedBy") val performedBy: String = "",
    @SerializedName("performedAt") val performedAt: String = ""
)

data class LegalEmployeeItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("legalRecordNumber") val legalRecordNumber: String = "",
    @JsonAdapter(FlexibleLegalRefAdapter::class)
    @SerializedName("employeeId") val employeeId: LegalRef? = null,
    @SerializedName("employeeCode") val employeeCode: String = "",
    @SerializedName("employmentType") val employmentType: String = "",
    @JsonAdapter(FlexibleLegalRefAdapter::class)
    @SerializedName("departmentId") val departmentId: LegalRef? = null,
    @JsonAdapter(FlexibleLegalRefAdapter::class)
    @SerializedName("branchId") val branchId: LegalRef? = null,
    @SerializedName("legalEntity") val legalEntity: String = "",
    @SerializedName("legalRecordType") val legalRecordType: String = "",
    @JsonAdapter(FlexibleLegalRefAdapter::class)
    @SerializedName("contractId") val contractId: LegalRef? = null,
    @SerializedName("labourDocumentType") val labourDocumentType: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("startDate") val startDate: String = "",
    @SerializedName("endDate") val endDate: String = "",
    @SerializedName("renewalDate") val renewalDate: String = "",
    @SerializedName("noticePeriod") val noticePeriod: String? = null,
    @JsonAdapter(FlexibleLegalRefAdapter::class)
    @SerializedName("hrOwner") val hrOwner: LegalRef? = null,
    @JsonAdapter(FlexibleLegalRefAdapter::class)
    @SerializedName("legalOwner") val legalOwner: LegalRef? = null,
    @SerializedName("riskLevel") val riskLevel: String = "Low",
    @SerializedName("status") val status: String = "Pending",
    @SerializedName("employmentContractDocuments") val employmentContractDocuments: List<LegalDocumentItem> = emptyList(),
    @SerializedName("labourDocuments") val labourDocuments: List<LegalDocumentItem> = emptyList(),
    @SerializedName("supportingDocuments") val supportingDocuments: List<LegalDocumentItem> = emptyList(),
    @SerializedName("activities") val activities: List<LegalActivityItem> = emptyList(),
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
)

data class LegalEmployeeListResponse(
    val success: Boolean,
    val data: List<LegalEmployeeItem> = emptyList(),
    val pagination: LegalPagination? = null
)

data class LegalEmployeeDetailResponse(
    val success: Boolean,
    val message: String? = null,
    val data: LegalEmployeeItem? = null
)

// ── Legal Employee Status Update ──
data class UpdateEmployeeStatusRequest(
    val status: String
//    val comments: String? = null
)