package com.cuso.tailor.model.settings

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

// =============================================================================
// 1. API RESPONSE WRAPPERS
// =============================================================================

data class DesignListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("pagination")
    val pagination: PaginationDto? = null,
    @SerializedName("data")
    val data: List<DesignItem> = emptyList(),
    @SerializedName("message")
    val message: String? = null
)

data class DesignDetailResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: DesignItem? = null,
    @SerializedName("message")
    val message: String? = null
)

data class ChangeDesignStatusResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: DesignItem? = null
)

data class DeleteDesignResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null
)

// =============================================================================
// 2. DESIGN CORE ITEM
// =============================================================================

data class DesignItem(
    @SerializedName("_id", alternate = ["id"])
    val id: String,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("name")
    val name: String,
    @SerializedName("designType")
    val designType: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("applicableGarments")
    val applicableGarments: List<ApplicableGarmentItem> = emptyList(),
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("imagePublicId")
    val imagePublicId: String? = null,
    @SerializedName("status")
    val status: String = "Active",
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

// =============================================================================
// 3. NESTED SUPPORTING MODELS (FIXED)
// =============================================================================

data class ApplicableGarmentItem(
    @SerializedName("_id", alternate = ["id"])
    val id: String? = null,
    @SerializedName("segmentId")
    val segmentId: DesignSegmentDto? = null,
    @SerializedName("garmentId")
    val garmentId: DesignGarmentDto? = null,
    @SerializedName("garmentCategoryId")
    val garmentCategoryId: DesignCategoryDto? = null,
    @SerializedName("allCategories")
    val allCategories: Boolean = true
)
data class ApplicableGarmentPayload(
    val segmentId: String,
    val garmentId: String,
    val garmentCategoryId: String? = null,
    val allCategories: Boolean = true
)

// ── Segment DTO & Deserializer ──
@JsonAdapter(DesignSegmentDeserializer::class)
data class DesignSegmentDto(
    @SerializedName("_id", alternate = ["id"])
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null,
    @SerializedName("code")
    val code: String? = null
)

class DesignSegmentDeserializer : JsonDeserializer<DesignSegmentDto?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DesignSegmentDto? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                DesignSegmentDto(
                    id = obj.get("_id")?.asString ?: obj.get("id")?.asString,
                    name = obj.get("name")?.takeIf { !it.isJsonNull }?.asString,
                    displayName = obj.get("displayName")?.takeIf { !it.isJsonNull }?.asString,
                    code = obj.get("code")?.takeIf { !it.isJsonNull }?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> DesignSegmentDto(id = json.asString)
            else -> null
        }
    }
}

// ── Garment DTO & Deserializer ──
@JsonAdapter(DesignGarmentDeserializer::class)
data class DesignGarmentDto(
    @SerializedName("_id", alternate = ["id"])
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null,
    @SerializedName("code")
    val code: String? = null
)

class DesignGarmentDeserializer : JsonDeserializer<DesignGarmentDto?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DesignGarmentDto? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                DesignGarmentDto(
                    id = obj.get("_id")?.asString ?: obj.get("id")?.asString,
                    name = obj.get("name")?.takeIf { !it.isJsonNull }?.asString,
                    displayName = obj.get("displayName")?.takeIf { !it.isJsonNull }?.asString,
                    code = obj.get("code")?.takeIf { !it.isJsonNull }?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> DesignGarmentDto(id = json.asString)
            else -> null
        }
    }
}

// ── Category DTO & Deserializer ──
@JsonAdapter(DesignCategoryDeserializer::class)
data class DesignCategoryDto(
    @SerializedName("_id", alternate = ["id"])
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null
)

class DesignCategoryDeserializer : JsonDeserializer<DesignCategoryDto?> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DesignCategoryDto? {
        if (json == null || json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                DesignCategoryDto(
                    id = obj.get("_id")?.asString ?: obj.get("id")?.asString,
                    name = obj.get("name")?.takeIf { !it.isJsonNull }?.asString,
                    displayName = obj.get("displayName")?.takeIf { !it.isJsonNull }?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> DesignCategoryDto(id = json.asString)
            else -> null
        }
    }
}

// =============================================================================
// 4. REQUEST PAYLOADS
// =============================================================================

data class ChangeDesignStatusRequest(
    @SerializedName("status")
    val status: String
)