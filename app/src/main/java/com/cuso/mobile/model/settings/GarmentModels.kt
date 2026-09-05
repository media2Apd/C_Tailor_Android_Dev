package com.cuso.mobile.model.settings

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class UpdateSegmentRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("displayName")
    val displayName: String = name,
    @SerializedName("code")
    val code: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("displayOrder")
    val displayOrder: Int = 1,
    @SerializedName("status")
    val status: String = "Active"
)

data class GarmentListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("pagination")
    val pagination: PaginationDto? = null,
    @SerializedName("data")
    val data: List<GarmentItem> = emptyList(),
    @SerializedName("message")
    val message: String? = null
)

data class ChangeGarmentStatusRequest(
    val status: String
)

data class ChangeGarmentStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: GarmentItem? = null
)

@JsonAdapter(ApplicableSegmentDeserializer::class)
data class ApplicableSegmentDto(
    @SerializedName("_id", alternate = ["id"])
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null
)

class ApplicableSegmentDeserializer : JsonDeserializer<ApplicableSegmentDto?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ApplicableSegmentDto? {
        if (json == null || json.isJsonNull) return null

        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val id = obj.get("_id")?.asString ?: obj.get("id")?.asString
                ApplicableSegmentDto(
                    id = id,
                    name = obj.get("name")?.takeIf { !it.isJsonNull }?.asString,
                    displayName = obj.get("displayName")?.takeIf { !it.isJsonNull }?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                ApplicableSegmentDto(id = json.asString)
            }
            else -> null
        }
    }
}

@JsonAdapter(MeasurementFieldDetailDeserializer::class)
data class MeasurementFieldDetailDto(
    @SerializedName("_id", alternate = ["id"])
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null,
    @SerializedName("inputType")
    val inputType: String? = null,
    @SerializedName("unit")
    val unit: String? = null
)

class MeasurementFieldDetailDeserializer : JsonDeserializer<MeasurementFieldDetailDto?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): MeasurementFieldDetailDto? {
        if (json == null || json.isJsonNull) return null

        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val id = obj.get("_id")?.asString ?: obj.get("id")?.asString
                MeasurementFieldDetailDto(
                    id = id,
                    name = obj.get("name")?.takeIf { !it.isJsonNull }?.asString,
                    displayName = obj.get("displayName")?.takeIf { !it.isJsonNull }?.asString,
                    inputType = obj.get("inputType")?.takeIf { !it.isJsonNull }?.asString,
                    unit = if (obj.has("unit") && !obj.get("unit").isJsonNull) obj.get("unit").asString else null
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                MeasurementFieldDetailDto(id = json.asString)
            }
            else -> null
        }
    }
}

data class GarmentMeasurementFieldItem(
    @SerializedName("fieldId", alternate = ["field", "fieldDetail"])
    val field: MeasurementFieldDetailDto? = null,
    @SerializedName("isRequired")
    val isRequired: Boolean = true,
    @SerializedName("displayOrder")
    val displayOrder: Int = 1,
    @SerializedName("_id", alternate = ["id"])
    val id: String? = null
)

data class GarmentItem(
    @SerializedName("_id", alternate = ["id"])
    val id: String,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("name")
    val name: String,
    @SerializedName("displayName")
    val displayName: String? = null,
    @SerializedName("code")
    val code: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("applicableSegments")
    val applicableSegments: List<ApplicableSegmentDto> = emptyList(),
    @SerializedName("measurementFields")
    val measurementFields: List<GarmentMeasurementFieldItem> = emptyList(),
    @SerializedName("baseStitchingCharge")
    val baseStitchingCharge: Double = 0.0,
    @SerializedName("isCustomStitchable")
    val isCustomStitchable: Boolean = true,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("imagePublicId")
    val imagePublicId: String? = null,
    @SerializedName("isSystemDefined")
    val isSystemDefined: Boolean = false,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("mappedCategoriesCount")
    val mappedCategoriesCount: Int = 0,
    @SerializedName("createdBy")
    val createdBy: UserMetaDto? = null,
    @SerializedName("updatedBy")
    val updatedBy: UserMetaDto? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("__v")
    val v: Int? = null
)

data class GarmentImageDto(
    @SerializedName("_id", alternate = ["id"])
    val id: String? = null,
    @SerializedName("url", alternate = ["imageUrl"])
    val url: String? = null,
    @SerializedName("isPrimary")
    val isPrimary: Boolean? = null
)

data class ChangeGarmentCategoryStatusResponse(
    val success: Boolean,
    val message: String,
    val data: GarmentStyleItem
)
data class GarmentStyleItem(
    @SerializedName("_id", alternate = ["id"]) val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("segmentId", alternate = ["segment"]) val segment: StyleSegment? = null,
    @SerializedName("garmentId", alternate = ["garment"]) val garment: StyleGarmentParent? = null,
    @SerializedName("styleTags") val styleTags: List<String> = emptyList(),
    @SerializedName("sleeveStyle") val sleeveStyle: String? = null,
    @SerializedName("measurementFields") val measurementFields: List<StyleMeasurementFieldEntry> = emptyList(),
    @SerializedName("customFields") val customFields: List<StyleCustomFieldEntry> = emptyList(),
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("images") val images: List<GarmentImageDto> = emptyList(),
    @SerializedName("stitchingCharge") val stitchingCharge: Double = 0.0,
    @SerializedName("isStitchingTaxable") val isStitchingTaxable: Boolean = false,
    @SerializedName("stitchingTaxGroupId", alternate = ["stitchingTaxGroup", "taxGroup"]) val stitchingTaxGroup: StyleTaxGroup? = null,
    @SerializedName("isCustomStitchable") val isCustomStitchable: Boolean = true,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

@JsonAdapter(StyleSegmentDeserializer::class)
data class StyleSegment(
    @SerializedName("_id", alternate = ["id"]) val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null
)

class StyleSegmentDeserializer : JsonDeserializer<StyleSegment?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): StyleSegment? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val id = obj.get("_id")?.asString ?: obj.get("id")?.asString ?: return null
                StyleSegment(
                    id = id,
                    name = obj.get("name")?.takeIf { !it.isJsonNull }?.asString,
                    displayName = obj.get("displayName")?.takeIf { !it.isJsonNull }?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> StyleSegment(id = json.asString)
            else -> null
        }
    }
}

@JsonAdapter(StyleGarmentParentDeserializer::class)
data class StyleGarmentParent(
    @SerializedName("_id", alternate = ["id"]) val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("baseStitchingCharge") val baseStitchingCharge: Double = 0.0,
    @SerializedName("measurementFields") val measurementFields: List<StyleMeasurementFieldEntry> = emptyList()
)

class StyleGarmentParentDeserializer : JsonDeserializer<StyleGarmentParent?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): StyleGarmentParent? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                val id = obj.get("_id")?.asString ?: obj.get("id")?.asString ?: return null

                val measurementFieldsList: List<StyleMeasurementFieldEntry> = if (obj.has("measurementFields") && obj.get("measurementFields").isJsonArray) {
                    val fieldsArray = obj.getAsJsonArray("measurementFields")
                    context?.deserialize(fieldsArray, object : com.google.gson.reflect.TypeToken<List<StyleMeasurementFieldEntry>>() {}.type) ?: emptyList()
                } else {
                    emptyList()
                }

                StyleGarmentParent(
                    id = id,
                    name = obj.get("name")?.takeIf { !it.isJsonNull }?.asString,
                    displayName = obj.get("displayName")?.takeIf { !it.isJsonNull }?.asString,
                    baseStitchingCharge = obj.get("baseStitchingCharge")?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0,
                    measurementFields = measurementFieldsList
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> StyleGarmentParent(id = json.asString)
            else -> null
        }
    }
}

data class StyleMeasurementFieldEntry(
    @SerializedName("_id", alternate = ["id"]) val id: String? = null,
    @SerializedName("fieldId", alternate = ["field", "fieldDetail"]) val fieldDetail: StyleMeasurementFieldDetail? = null,
    @SerializedName("isRequired") val isRequired: Boolean = false,
    @SerializedName("displayOrder") val displayOrder: Int = 1
)

data class StyleMeasurementFieldDetail(
    @SerializedName("_id", alternate = ["id"]) val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("inputType") val inputType: String = "Number",
    @SerializedName("unit") val unit: String? = "inch",
    @SerializedName("options") val options: List<String> = emptyList()
)

data class StyleCustomFieldEntry(
    @SerializedName("_id", alternate = ["id"]) val id: String? = null,
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String
)

data class StyleTaxGroup(
    @SerializedName("_id", alternate = ["id"]) val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("totalRate") val totalRate: Double
)

data class StyleMeasurementFieldEntryRequest(
    @SerializedName("fieldId") val fieldId: String,
    @SerializedName("isRequired") val isRequired: Boolean = false,
    @SerializedName("displayOrder") val displayOrder: Int = 1
)

data class CreateGarmentRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("displayName")
    val displayName: String = name,
    @SerializedName("code")
    val code: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("applicableSegments")
    val applicableSegments: List<String> = emptyList(),
    @SerializedName("isCustomStitchable")
    val isCustomStitchable: Boolean = true,
    @SerializedName("status")
    val status: String = "Active"
)

data class CreateGarmentResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: GarmentItem? = null
)

data class GarmentStyleListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: PaginationDto?,
    @SerializedName("data") val data: List<GarmentStyleItem>
)

data class CreateGarmentStyleRequest(
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String = name,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("segmentId") val segmentId: String? = null,
    @SerializedName("garmentId") val garmentId: String? = null,
    @SerializedName("measurementFields") val measurementFields: List<StyleMeasurementFieldEntryRequest> = emptyList(),
    @SerializedName("styleTags") val styleTags: List<String> = emptyList(),
    @SerializedName("sleeveStyle") val sleeveStyle: String? = null,
    @SerializedName("stitchingCharge") val stitchingCharge: Double = 0.0,
    @SerializedName("isStitchingTaxable") val isStitchingTaxable: Boolean = false,
    @SerializedName("stitchingTaxGroupId") val stitchingTaxGroupId: String? = null,
    @SerializedName("isCustomStitchable") val isCustomStitchable: Boolean = true,
    @SerializedName("status") val status: String = "Active"
)

data class UpdateGarmentStyleRequest(
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String = name,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("segmentId") val segmentId: String? = null,
    @SerializedName("garmentId") val garmentId: String? = null,
    @SerializedName("measurementFields") val measurementFields: List<StyleMeasurementFieldEntryRequest> = emptyList(),
    @SerializedName("styleTags") val styleTags: List<String> = emptyList(),
    @SerializedName("sleeveStyle") val sleeveStyle: String? = null,
    @SerializedName("stitchingCharge") val stitchingCharge: Double = 0.0,
    @SerializedName("isStitchingTaxable") val isStitchingTaxable: Boolean = false,
    @SerializedName("stitchingTaxGroupId") val stitchingTaxGroupId: String? = null,
    @SerializedName("isCustomStitchable") val isCustomStitchable: Boolean = true,
    @SerializedName("status") val status: String? = null
)

data class GarmentStyleDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: GarmentStyleItem? = null
)

data class DeleteGarmentStyleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)

data class CreateMeasurementFieldRequest(
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String = name,
    @SerializedName("code") val code: String,
    @SerializedName("groupName") val groupName: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("inputType") val inputType: String = "Number",
    @SerializedName("inputCount") val inputCount: Int = 1,
    @SerializedName("subLabels") val subLabels: List<String> = emptyList(),
    @SerializedName("unit") val unit: String? = "inch",
    @SerializedName("minValue") val minValue: Double? = null,
    @SerializedName("maxValue") val maxValue: Double? = null,
    @SerializedName("options") val options: List<String> = emptyList(),
    @SerializedName("status") val status: String = "Active"
)

data class MeasurementFieldDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: MeasurementFieldItem? = null
)

//view one
data class GarmentDetailResponse(
    val success: Boolean,
    val data: GarmentDetail
)

data class GarmentDetail(
    val _id: String,
    val name: String,
    val displayName: String?,
    val code: String,
    val description: String?,
    val baseStitchingCharge: Double,
    val status: String,
    val applicableSegments: List<ApplicableSegment>
)

data class ApplicableSegment(
    val _id: String,
    val name: String,
    val displayName: String
)
//update garment basic price
/**
 * Request model to update only the basic pricing and status of a garment
 */
data class UpdateGarmentBasicPriceRequest(
    @SerializedName("baseStitchingCharge") val baseStitchingCharge: Double,
    @SerializedName("isActive") val isActive: Boolean
)

/**
 * Main response wrapper for the update API
 */
data class UpdateGarmentBasicPriceResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: GarmentItem?
)