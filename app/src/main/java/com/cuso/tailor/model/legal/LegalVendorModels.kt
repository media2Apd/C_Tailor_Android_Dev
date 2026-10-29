@file:Suppress("unused")

package com.cuso.tailor.model.legal

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

//// Generic Response Wrapper for Legal Vendor
//data class LegalVendorResponse<T>(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String? = null,
//    @SerializedName("data") val data: T? = null,
//    @SerializedName("pagination") val pagination: LegalVendorPaginationData? = null
//)
//
//// Pagination Model for Legal Vendor
//data class LegalVendorPaginationData(
//    @SerializedName("total") val total: Int = 0,
//    @SerializedName("page") val page: Int = 1,
//    @SerializedName("limit") val limit: Int = 20,
//    @SerializedName("totalPages") val totalPages: Int = 1,
//    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
//    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
//)
//
//// Flexible Reference Deserializer for Branch Reference
//class FlexibleLegalVendorBranchRefAdapter : JsonDeserializer<LegalVendorBranchRef> {
//    override fun deserialize(
//        json: JsonElement?,
//        typeOfT: Type?,
//        context: JsonDeserializationContext?
//    ): LegalVendorBranchRef {
//        if (json == null || json.isJsonNull) return LegalVendorBranchRef()
//        return if (json.isJsonObject) {
//            val obj = json.asJsonObject
//            LegalVendorBranchRef(
//                id = obj.get("_id")?.asString.orEmpty(),
//                name = obj.get("name")?.asString.orEmpty(),
//                branchCode = obj.get("branchCode")?.asString.orEmpty()
//            )
//        } else {
//            LegalVendorBranchRef(id = json.asString)
//        }
//    }
//}
//
//// Flexible Reference Deserializer for User Reference
//class FlexibleLegalVendorUserRefAdapter : JsonDeserializer<LegalVendorUserRef> {
//    override fun deserialize(
//        json: JsonElement?,
//        typeOfT: Type?,
//        context: JsonDeserializationContext?
//    ): LegalVendorUserRef {
//        if (json == null || json.isJsonNull) return LegalVendorUserRef()
//        return if (json.isJsonObject) {
//            val obj = json.asJsonObject
//            LegalVendorUserRef(
//                id = obj.get("_id")?.asString.orEmpty(),
//                firstName = obj.get("firstName")?.asString.orEmpty(),
//                lastName = obj.get("lastName")?.asString.orEmpty(),
//                memberId = obj.get("memberId")?.asString.orEmpty()
//            )
//        } else {
//            LegalVendorUserRef(id = json.asString)
//        }
//    }
//}
//
//// Flexible Reference Deserializer for Contract Reference
//class FlexibleLegalVendorContractRefAdapter : JsonDeserializer<LegalVendorContractRef> {
//    override fun deserialize(
//        json: JsonElement?,
//        typeOfT: Type?,
//        context: JsonDeserializationContext?
//    ): LegalVendorContractRef {
//        if (json == null || json.isJsonNull) return LegalVendorContractRef()
//        return if (json.isJsonObject) {
//            val obj = json.asJsonObject
//            LegalVendorContractRef(
//                id = obj.get("_id")?.asString.orEmpty(),
//                contractName = obj.get("contractName")?.asString.orEmpty(),
//                contractNumber = obj.get("contractNumber")?.asString.orEmpty(),
//                contractType = obj.get("contractType")?.asString.orEmpty()
//            )
//        } else {
//            LegalVendorContractRef(id = json.asString)
//        }
//    }
//}
//
//// Branch Reference Model
//data class LegalVendorBranchRef(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("name") val name: String = "",
//    @SerializedName("branchCode") val branchCode: String = ""
//)
//
//// User Reference Model
//data class LegalVendorUserRef(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("firstName") val firstName: String = "",
//    @SerializedName("lastName") val lastName: String = "",
//    @SerializedName("memberId") val memberId: String = ""
//) {
//    val fullName: String
//        get() = "$firstName $lastName".trim()
//}
//
//// Contract Reference Model
//data class LegalVendorContractRef(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("contractName") val contractName: String = "",
//    @SerializedName("contractNumber") val contractNumber: String = "",
//    @SerializedName("contractType") val contractType: String = ""
//)
//
//// Document Item Model for Legal Vendor
//data class LegalVendorDocumentItem(
//    @SerializedName("_id") val id: String? = null,
//    @SerializedName("documentUrl") val documentUrl: String = "",
//    @SerializedName("publicId") val publicId: String = "",
//    @SerializedName("documentType") val documentType: String = "",
//    @SerializedName("uploadedBy") val uploadedBy: String = "",
//    @SerializedName("uploadedAt") val uploadedAt: String = ""
//)
//
//// Activity Item Model for Legal Vendor
//data class LegalVendorActivityItem(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("action") val action: String = "",
//    @SerializedName("description") val description: String = "",
//    @SerializedName("performedBy") val performedBy: String = "",
//    @SerializedName("performedAt") val performedAt: String = ""
//)
//
//// Main Legal Vendor Item Model
//data class LegalVendorItem(
//    @SerializedName("_id") val id: String = "",
//    @SerializedName("organizationId") val organizationId: String = "",
//    @SerializedName("legalVendorNumber") val legalVendorNumber: String = "",
//    @SerializedName("vendor") val vendor: String = "",
//    @SerializedName("supplierType") val supplierType: String = "",
//    @SerializedName("country") val country: String = "",
//    @SerializedName("countryName") val countryName: String? = null,
//    @SerializedName("legalEntity") val legalEntity: String = "",
//    @JsonAdapter(FlexibleLegalVendorBranchRefAdapter::class)
//    @SerializedName("branchId") val branchId: LegalVendorBranchRef? = null,
//    @SerializedName("agreementType") val agreementType: String = "",
//    @SerializedName("agreementDescription") val agreementDescription: String = "",
//    @SerializedName("sla") val sla: String = "",
//    @SerializedName("serviceTerms") val serviceTerms: String = "",
//    @JsonAdapter(FlexibleLegalVendorContractRefAdapter::class)
//    @SerializedName("contractReference") val contractReference: LegalVendorContractRef? = null,
//    @SerializedName("paymentTerms") val paymentTerms: String = "",
//    @SerializedName("startDate") val startDate: String = "",
//    @SerializedName("endDate") val endDate: String = "",
//    @SerializedName("renewalNoticePeriod") val renewalNoticePeriod: Int = 0,
//    @SerializedName("renewalDate") val renewalDate: String = "",
//    @JsonAdapter(FlexibleLegalVendorUserRefAdapter::class)
//    @SerializedName("procurementOwner") val procurementOwner: LegalVendorUserRef? = null,
//    @JsonAdapter(FlexibleLegalVendorUserRefAdapter::class)
//    @SerializedName("legalOwner") val legalOwner: LegalVendorUserRef? = null,
//    @SerializedName("riskLevel") val riskLevel: String = "Low",
//    @SerializedName("priority") val priority: String = "Medium",
//    @SerializedName("status") val status: String = "Active",
//    @SerializedName("vendorAgreement") val vendorAgreement: List<LegalVendorDocumentItem> = emptyList(),
//    @SerializedName("slaDocuments") val slaDocuments: List<LegalVendorDocumentItem> = emptyList(),
//    @SerializedName("supportingDocuments") val supportingDocuments: List<LegalVendorDocumentItem> = emptyList(),
//    @SerializedName("activity") val activity: List<LegalVendorActivityItem> = emptyList(),
//    @SerializedName("createdAt") val createdAt: String = "",
//    @SerializedName("updatedAt") val updatedAt: String = ""
//) {
//    val displayCountry: String
//        get() = countryName ?: country
//}

// Request Payload Model for Legal Vendor Create and Update
data class LegalVendorRequest(
    @SerializedName("vendor") val vendor: String,
    @SerializedName("supplierType") val supplierType: String,
    @SerializedName("countryName") val countryName: String,
    @SerializedName("legalEntity") val legalEntity: String,
    @SerializedName("branchId") val branchId: String,
    @SerializedName("agreementType") val agreementType: String,
    @SerializedName("agreementDescription") val agreementDescription: String,
    @SerializedName("sla") val sla: String,
    @SerializedName("serviceTerms") val serviceTerms: String,
    @SerializedName("contractReference") val contractReference: String? = null,
    @SerializedName("paymentTerms") val paymentTerms: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String,
    @SerializedName("renewalNoticePeriod") val renewalNoticePeriod: Int,
    @SerializedName("renewalDate") val renewalDate: String,
    @SerializedName("procurementOwner") val procurementOwner: String,
    @SerializedName("legalOwner") val legalOwner: String,
    @SerializedName("riskLevel") val riskLevel: String,
    @SerializedName("priority") val priority: String,
    @SerializedName("status") val status: String,
    @SerializedName("id") val id: String? = null
)



// Generic Response Container
data class LegalVendorResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: T? = null,
    @SerializedName("pagination") val pagination: LegalVendorPaginationData? = null
)

// Pagination Model
data class LegalVendorPaginationData(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

// Flexible Reference Deserializers
class FlexibleLegalVendorBranchRefAdapter : JsonDeserializer<LegalVendorBranchRef> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LegalVendorBranchRef {
        if (json == null || json.isJsonNull) return LegalVendorBranchRef()
        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            LegalVendorBranchRef(
                id = obj.get("_id")?.asString.orEmpty(),
                name = obj.get("name")?.asString.orEmpty(),
                branchCode = obj.get("branchCode")?.asString.orEmpty()
            )
        } else {
            LegalVendorBranchRef(id = json.asString)
        }
    }
}

class FlexibleLegalVendorUserRefAdapter : JsonDeserializer<LegalVendorUserRef> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LegalVendorUserRef {
        if (json == null || json.isJsonNull) return LegalVendorUserRef()
        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            LegalVendorUserRef(
                id = obj.get("_id")?.asString.orEmpty(),
                firstName = obj.get("firstName")?.asString.orEmpty(),
                lastName = obj.get("lastName")?.asString.orEmpty(),
                memberId = obj.get("memberId")?.asString.orEmpty()
            )
        } else {
            LegalVendorUserRef(id = json.asString)
        }
    }
}

class FlexibleLegalVendorContractRefAdapter : JsonDeserializer<LegalVendorContractRef> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): LegalVendorContractRef {
        if (json == null || json.isJsonNull) return LegalVendorContractRef()
        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            LegalVendorContractRef(
                id = obj.get("_id")?.asString.orEmpty(),
                contractName = obj.get("contractName")?.asString.orEmpty(),
                contractNumber = obj.get("contractNumber")?.asString.orEmpty(),
                contractType = obj.get("contractType")?.asString.orEmpty()
            )
        } else {
            LegalVendorContractRef(id = json.asString)
        }
    }
}

data class LegalVendorBranchRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("branchCode") val branchCode: String = ""
)

data class LegalVendorUserRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = "",
    @SerializedName("memberId") val memberId: String = ""
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

data class LegalVendorContractRef(
    @SerializedName("_id") val id: String = "",
    @SerializedName("contractName") val contractName: String = "",
    @SerializedName("contractNumber") val contractNumber: String = "",
    @SerializedName("contractType") val contractType: String = ""
)

data class LegalVendorDocumentItem(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("documentUrl") val documentUrl: String = "",
    @SerializedName("publicId") val publicId: String = "",
    @SerializedName("documentType") val documentType: String = "",
    @SerializedName("uploadedBy") val uploadedBy: String = "",
    @SerializedName("uploadedAt") val uploadedAt: String = ""
)

data class LegalVendorActivityItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("action") val action: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("performedBy") val performedBy: String = "",
    @SerializedName("performedAt") val performedAt: String = ""
)

// Main Legal Vendor DTO
data class LegalVendorItem(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("legalVendorNumber") val legalVendorNumber: String = "",
    @SerializedName("vendor") val vendor: String = "",
    @SerializedName("supplierType") val supplierType: String = "",
    @SerializedName("country") val country: String = "",
    @SerializedName("countryName") val countryName: String? = null,
    @SerializedName("legalEntity") val legalEntity: String = "",
    @JsonAdapter(FlexibleLegalVendorBranchRefAdapter::class)
    @SerializedName("branchId") val branchId: LegalVendorBranchRef? = null,
    @SerializedName("agreementType") val agreementType: String = "",
    @SerializedName("agreementDescription") val agreementDescription: String = "",
    @SerializedName("sla") val sla: String = "",
    @SerializedName("serviceTerms") val serviceTerms: String = "",
    @JsonAdapter(FlexibleLegalVendorContractRefAdapter::class)
    @SerializedName("contractReference") val contractReference: LegalVendorContractRef? = null,
    @SerializedName("paymentTerms") val paymentTerms: String = "",
    @SerializedName("startDate") val startDate: String = "",
    @SerializedName("endDate") val endDate: String = "",
    @SerializedName("renewalNoticePeriod") val renewalNoticePeriod: Int = 0,
    @SerializedName("renewalDate") val renewalDate: String = "",
    @JsonAdapter(FlexibleLegalVendorUserRefAdapter::class)
    @SerializedName("procurementOwner") val procurementOwner: LegalVendorUserRef? = null,
    @JsonAdapter(FlexibleLegalVendorUserRefAdapter::class)
    @SerializedName("legalOwner") val legalOwner: LegalVendorUserRef? = null,
    @SerializedName("riskLevel") val riskLevel: String = "Low",
    @SerializedName("priority") val priority: String = "Medium",
    @SerializedName("status") val status: String = "Active",
    @SerializedName("vendorAgreement") val vendorAgreement: List<LegalVendorDocumentItem> = emptyList(),
    @SerializedName("slaDocuments") val slaDocuments: List<LegalVendorDocumentItem> = emptyList(),
    @SerializedName("supportingDocuments") val supportingDocuments: List<LegalVendorDocumentItem> = emptyList(),
    @SerializedName("activity") val activity: List<LegalVendorActivityItem> = emptyList(),
    @JsonAdapter(FlexibleLegalVendorUserRefAdapter::class)
    @SerializedName("createdBy") val createdBy: LegalVendorUserRef? = null,
    @JsonAdapter(FlexibleLegalVendorUserRefAdapter::class)
    @SerializedName("updatedBy") val updatedBy: LegalVendorUserRef? = null,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
) {
    val displayCountry: String
        get() = countryName ?: country
}

// Concrete List Response Wrapper to resolve type inference
data class LegalVendorListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<LegalVendorItem>? = null,
    @SerializedName("pagination") val pagination: LegalVendorPaginationData? = null
)

// Concrete Detail Response Wrapper
data class LegalVendorDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: LegalVendorItem? = null
)

// Status Update Request Model
data class UpdateVendorStatusRequest(
    @SerializedName("status") val status: String
)


