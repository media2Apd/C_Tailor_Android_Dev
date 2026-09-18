@file:Suppress("unused", "AssignedValueIsNeverUsed")

package com.cuso.tailor.model.settings

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class WorkPricingResponseForChangeStatus(
    val success: Boolean,
    val message: String,
    val data: WorkPricingItem
)

/**
 * Use this for the "Get All" list API
 */
data class WorkPricingListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<WorkPricingItem>,
    @SerializedName("pagination") val pagination: PaginationMetadata?
)

/**
 * Request model for creating or updating Work Pricing.
 */
data class WorkPricingRequest(
    @SerializedName("workType") val workType: String,
    @SerializedName("segmentId") val segmentId: String,
    @SerializedName("basePrice") val basePrice: Double,
    @SerializedName("status") val status: String? = null,
    @SerializedName("isTaxable") val isTaxable: Boolean = false,
    @SerializedName("garmentId") val garmentId: String? = null,
    @SerializedName("garmentCategoryId") val garmentCategoryId: String? = null,
    @SerializedName("taxGroupId") val taxGroupId: String? = null,
    @SerializedName("incomeAccount") val incomeAccount: String? = null
)

/**
 * Helper model for income account inside WorkPricingDetail
 */
@JsonAdapter(WorkPricingIncomeAccountDeserializer::class)
data class WorkPricingIncomeAccount(
    @SerializedName("_id", alternate = ["id"]) val id: String,
    @SerializedName("accountName") val accountName: String? = null,
    @SerializedName("accountCode") val accountCode: String? = null
) {
    val displayName: String
        get() = if (!accountCode.isNullOrBlank()) "$accountCode - $accountName" else (accountName ?: "")
}

/**
 * Response model for Work Pricing operations.
 */
data class WorkPricingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: WorkPricingDetail?,
    @SerializedName("pagination") val pagination: PaginationMetadata?
)

data class PaginationMetadata(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class WorkPricingItem(
    @SerializedName("_id") val id: String,
    @SerializedName("workType") val workType: String,
    @SerializedName("basePrice") val basePrice: Double,
    @SerializedName("status") val status: String,
    @SerializedName("isTaxable") val isTaxable: Boolean,
    @SerializedName("segmentId") val segmentId: WorkPricingSegment?,
    @SerializedName("garmentId") val garment: WorkPricingGarment?,
    @SerializedName("taxGroupId") val taxGroup: WorkPricingTaxGroup?
)

data class WorkPricingSegment(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String?
)

data class WorkPricingTaxGroup(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("totalRate") val totalRate: Double
)

/**
 * Main response wrapper for the Work Pricing View One API
 */
data class WorkPricingDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: WorkPricingDetail
)

data class WorkPricingDetail(
    @SerializedName("_id") val id: String,
    @SerializedName("workType") val workType: String,
    @SerializedName("basePrice") val basePrice: Double,
    @SerializedName("status") val status: String,
    @SerializedName("isTaxable") val isTaxable: Boolean,

    // UI looks for detail.segment
    @SerializedName("segmentId") val segment: WorkPricingSegment?,
    // UI looks for detail.garment
    @SerializedName("garmentId") val garment: WorkPricingGarment?,
    // UI looks for detail.garmentCategory
    @SerializedName("garmentCategoryId") val garmentCategory: WorkPricingCategory?,

    @SerializedName("taxGroupId") val taxGroup: WorkPricingTaxGroup?,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("incomeAccount") val incomeAccount: WorkPricingIncomeAccount? = null
)

data class WorkPricingGarment(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String
)

data class WorkPricingCategory(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String?
)

data class WorkPricingCreatedData(
    @SerializedName("_id") val id: String,
    @SerializedName("segmentId") val segmentId: String?,
    @SerializedName("garmentId") val garmentId: String?,
    @SerializedName("garmentCategoryId") val garmentCategoryId: String?
)

class WorkPricingSegmentDeserializer : JsonDeserializer<WorkPricingSegment?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): WorkPricingSegment? {
        if (json == null || json.isJsonNull) return null
        return if (json.isJsonPrimitive) {
            WorkPricingSegment(id = json.asString, name = "", displayName = null)
        } else {
            val obj = json.asJsonObject
            WorkPricingSegment(
                id = obj.get("_id")?.asString.orEmpty(),
                name = obj.get("name")?.asString.orEmpty(),
                displayName = obj.get("displayName")?.takeIf { !it.isJsonNull }?.asString
            )
        }
    }
}

class WorkPricingGarmentDeserializer : JsonDeserializer<WorkPricingGarment?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): WorkPricingGarment? {
        if (json == null || json.isJsonNull) return null
        return if (json.isJsonPrimitive) {
            WorkPricingGarment(id = json.asString, name = "")
        } else {
            val obj = json.asJsonObject
            WorkPricingGarment(
                id = obj.get("_id")?.asString.orEmpty(),
                name = obj.get("name")?.asString.orEmpty()
            )
        }
    }
}

class WorkPricingCategoryDeserializer : JsonDeserializer<WorkPricingCategory?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): WorkPricingCategory? {
        if (json == null || json.isJsonNull) return null
        return if (json.isJsonPrimitive) {
            WorkPricingCategory(id = json.asString, name = "", displayName = null)
        } else {
            val obj = json.asJsonObject
            WorkPricingCategory(
                id = obj.get("_id")?.asString.orEmpty(),
                name = obj.get("name")?.asString.orEmpty(),
                displayName = obj.get("displayName")?.takeIf { !it.isJsonNull }?.asString
            )
        }
    }
}

class WorkPricingTaxGroupDeserializer : JsonDeserializer<WorkPricingTaxGroup?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): WorkPricingTaxGroup? {
        if (json == null || json.isJsonNull) return null
        return if (json.isJsonPrimitive) {
            WorkPricingTaxGroup(id = json.asString, name = "", totalRate = 0.0)
        } else {
            val obj = json.asJsonObject
            WorkPricingTaxGroup(
                id = obj.get("_id")?.asString.orEmpty(),
                name = obj.get("name")?.asString.orEmpty(),
                totalRate = obj.get("totalRate")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0
            )
        }
    }
}

class WorkPricingIncomeAccountDeserializer : JsonDeserializer<WorkPricingIncomeAccount?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): WorkPricingIncomeAccount? {
        if (json == null || json.isJsonNull) return null
        return if (json.isJsonPrimitive) {
            WorkPricingIncomeAccount(id = json.asString, accountName = "", accountCode = "")
        } else {
            val obj = json.asJsonObject
            WorkPricingIncomeAccount(
                id = obj.get("_id")?.asString ?: obj.get("id")?.asString.orEmpty(),
                accountName = obj.get("accountName")?.takeIf { !it.isJsonNull }?.asString,
                accountCode = obj.get("accountCode")?.takeIf { !it.isJsonNull }?.asString
            )
        }
    }
}