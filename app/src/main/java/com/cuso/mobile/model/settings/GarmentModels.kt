package com.cuso.mobile.model.settings

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

// ── Garment List Response (GET /api/sales/garments/view-all) ──
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

// ── Segment Reference Model inside Garment ──
@JsonAdapter(ApplicableSegmentDeserializer::class)
data class ApplicableSegmentDto(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null
)

// Deserializer supporting both String Segment ID and populated Segment Object
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
                ApplicableSegmentDto(
                    id = obj.get("_id")?.asString,
                    name = obj.get("name")?.asString,
                    displayName = obj.get("displayName")?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                ApplicableSegmentDto(id = json.asString)
            }
            else -> null
        }
    }
}

// ── Measurement Field Reference ──
@JsonAdapter(MeasurementFieldDetailDeserializer::class)
data class MeasurementFieldDetailDto(
    @SerializedName("_id")
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

// Deserializer supporting both String Field ID and populated Field Object
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
                MeasurementFieldDetailDto(
                    id = obj.get("_id")?.asString,
                    name = obj.get("name")?.asString,
                    displayName = obj.get("displayName")?.asString,
                    inputType = obj.get("inputType")?.asString,
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

// ── Measurement Field Mapping Entry in Garment ──
data class GarmentMeasurementFieldItem(
    @SerializedName("fieldId")
    val field: MeasurementFieldDetailDto? = null,
    @SerializedName("isRequired")
    val isRequired: Boolean = true,
    @SerializedName("displayOrder")
    val displayOrder: Int = 1,
    @SerializedName("_id")
    val id: String? = null
)

// ── Garment Item Model ──
data class GarmentItem(
    @SerializedName("_id")
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
    @SerializedName("isActive")
    val isActive: Boolean = true,
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

// ── Create/Update Garment Request ──
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
    @SerializedName("baseStitchingCharge")
    val baseStitchingCharge: Double = 0.0,
    @SerializedName("isCustomStitchable")
    val isCustomStitchable: Boolean = true
//    @SerializedName("isActive")
//    val isActive: Boolean = true
)

// ── Create Garment Response ──
data class CreateGarmentResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: GarmentItem? = null
)

// ── Garment Style List Response ──
data class GarmentStyleListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: PaginationInfo?,
    @SerializedName("data") val data: List<GarmentStyleItem>
)

data class PaginationInfo(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class GarmentStyleItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("sku") val sku: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("segmentId") val segment: StyleSegment? = null,
    @SerializedName("garmentId") val garment: StyleGarmentParent? = null,
    @SerializedName("styleTags") val styleTags: List<String> = emptyList(),
    @SerializedName("sleeveStyle") val sleeveStyle: String? = null,
    @SerializedName("measurementFields") val measurementFields: List<StyleMeasurementFieldEntry> = emptyList(),
    @SerializedName("customFields") val customFields: List<StyleCustomFieldEntry> = emptyList(),
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("stitchingCharge") val stitchingCharge: Double = 0.0,
    @SerializedName("isStitchingTaxable") val isStitchingTaxable: Boolean = false,
    @SerializedName("stitchingTaxGroupId") val stitchingTaxGroup: StyleTaxGroup? = null,
    @SerializedName("isCustomStitchable") val isCustomStitchable: Boolean = true,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

@JsonAdapter(StyleSegmentDeserializer::class)
data class StyleSegment(
    @SerializedName("_id") val id: String,
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
                val id = obj.get("_id")?.asString ?: return null
                StyleSegment(
                    id = id,
                    name = obj.get("name")?.asString,
                    displayName = obj.get("displayName")?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> StyleSegment(id = json.asString)
            else -> null
        }
    }
}

@JsonAdapter(StyleGarmentParentDeserializer::class)
data class StyleGarmentParent(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("displayName") val displayName: String? = null,
    @SerializedName("baseStitchingCharge") val baseStitchingCharge: Double = 0.0
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
                val id = obj.get("_id")?.asString ?: return null
                StyleGarmentParent(
                    id = id,
                    name = obj.get("name")?.asString,
                    displayName = obj.get("displayName")?.asString,
                    baseStitchingCharge = obj.get("baseStitchingCharge")?.asDouble ?: 0.0
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> StyleGarmentParent(id = json.asString)
            else -> null
        }
    }
}

data class StyleMeasurementFieldEntry(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("fieldId") val fieldDetail: StyleMeasurementFieldDetail? = null,
    @SerializedName("isRequired") val isRequired: Boolean = false,
    @SerializedName("displayOrder") val displayOrder: Int = 1
)

data class StyleMeasurementFieldDetail(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("inputType") val inputType: String = "Number",
    @SerializedName("unit") val unit: String = "inch",
    @SerializedName("options") val options: List<String> = emptyList()
)

data class StyleCustomFieldEntry(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String
)

data class StyleTaxGroup(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("totalRate") val totalRate: Double
)

// ── Measurement Field Entry for Style Update ──
data class StyleMeasurementFieldEntryRequest(
    @SerializedName("fieldId") val fieldId: String,
    @SerializedName("isRequired") val isRequired: Boolean = false,
    @SerializedName("displayOrder") val displayOrder: Int = 1
)

// ── Create / Update Garment Category (Style) Request ──
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
//    @SerializedName("isActive") val isActive: Boolean = true
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
//    @SerializedName("isActive") val isActive: Boolean = true
)

// ── Single Garment Style Response (Create & Update) ──
data class GarmentStyleDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: GarmentStyleItem? = null
)

// ── Delete Garment Style Response ──
data class DeleteGarmentStyleResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)

//// ── Measurement Field Item Model ──
//data class MeasurementFieldSettingsItem(
//    @SerializedName("_id") val id: String,
//    @SerializedName("organizationId") val organizationId: String? = null,
//    @SerializedName("name") val name: String,
//    @SerializedName("displayName") val displayName: String? = null,
//    @SerializedName("code") val code: String? = null,
//    @SerializedName("description") val description: String? = null,
//    @SerializedName("inputType") val inputType: String = "Number",
//    @SerializedName("inputCount") val inputCount: Int = 1,
//    @SerializedName("subLabels") val subLabels: List<String> = emptyList(),
//    @SerializedName("unit") val unit: String? = "inch",
//    @SerializedName("minValue") val minValue: Double? = null,
//    @SerializedName("maxValue") val maxValue: Double? = null,
//    @SerializedName("options") val options: List<String> = emptyList(),
//    @SerializedName("isSystemDefined") val isSystemDefined: Boolean = false,
//    @SerializedName("isActive") val isActive: Boolean = true,
//    @SerializedName("createdAt") val createdAt: String? = null,
//    @SerializedName("updatedAt") val updatedAt: String? = null
//)
//
//// ── Measurement Field List Response ──
//data class MeasurementFieldListResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("pagination") val pagination: PaginationInfo? = null,
//    @SerializedName("data") val data: List<MeasurementFieldItem> = emptyList(),
//    @SerializedName("message") val message: String? = null
//)

// ── Create Measurement Field Request ──
data class CreateMeasurementFieldRequest(
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String = name,
    @SerializedName("code") val code: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("inputType") val inputType: String = "Number",
    @SerializedName("inputCount") val inputCount: Int = 1,
    @SerializedName("subLabels") val subLabels: List<String> = emptyList(),
    @SerializedName("unit") val unit: String? = "inch",
    @SerializedName("minValue") val minValue: Double? = null,
    @SerializedName("maxValue") val maxValue: Double? = null,
    @SerializedName("options") val options: List<String> = emptyList(),
//    @SerializedName("isActive") val isActive: Boolean = true
)

// ── Single Measurement Field Response ──
data class MeasurementFieldDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: MeasurementFieldItem? = null
)