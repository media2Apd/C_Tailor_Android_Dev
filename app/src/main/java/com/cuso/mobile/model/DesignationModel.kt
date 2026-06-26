package com.cuso.mobile.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

// ──────────────────────────────────────────────────────────────
// 📋 List Response
// ──────────────────────────────────────────────────────────────
data class DesignationListResponse(
    val success: Boolean,
    val data: List<DesignationItem>
)

// ──────────────────────────────────────────────────────────────
// ➕ Create
// ──────────────────────────────────────────────────────────────
data class DesignationCreateRequest(
    val name: String,
    val code: String,
    val description: String
)

data class DesignationCreateResponse(
    val success: Boolean,
    val message: String,
    val data: DesignationItem?
)

// ──────────────────────────────────────────────────────────────
// ✏️ Update
// ──────────────────────────────────────────────────────────────
data class DesignationUpdateRequest(
    val name: String,
    val code: String,
    val description: String? = null,
    val status: Boolean = true
)

data class DesignationUpdateResponse(
    val success: Boolean,
    val message: String,
    val data: DesignationUpdateData?
)

data class DesignationUpdateData(
    @SerializedName("_id") val id: String,
    val name: String,
    val organizationId: String,
    val description: String?,
    val code: String,
    val status: Boolean,
    val isDeleted: Boolean,
    val deletedAt: String?,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    val deletedBy: String?,
    val updatedBy: String?
)

// ──────────────────────────────────────────────────────────────
// 🗑️ Delete
// ──────────────────────────────────────────────────────────────
data class DesignationDeleteResponse(
    val success: Boolean,
    val message: String
)



// ──────────────────────────────────────────────────────────────
// 👤 Person
// ──────────────────────────────────────────────────────────────
data class DesignationPerson(
    @SerializedName("_id") val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val memberId: String = ""
)

// ──────────────────────────────────────────────────────────────
// 🔧 Flexible Deserializer (handles both string and object)
// ──────────────────────────────────────────────────────────────
class FlexibleDesignationPersonDeserializer : JsonDeserializer<DesignationPerson?> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        ctx: JsonDeserializationContext
    ): DesignationPerson? {
        if (json is JsonNull) return null
        return if (json.isJsonObject) {
            val obj = json.asJsonObject
            DesignationPerson(
                id        = obj.get("_id")?.asString ?: "",
                firstName = obj.get("firstName")?.asString ?: "",
                lastName  = obj.get("lastName")?.asString ?: "",
                memberId  = obj.get("memberId")?.asString ?: ""
            )
        } else {
            // If it's just a string (ID only), create person with just the ID
            DesignationPerson(id = json.asString)
        }
    }
}