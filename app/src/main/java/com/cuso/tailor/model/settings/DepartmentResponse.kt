// DepartmentModels.kt
package com.cuso.tailor.model.settings

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

// Safe JSON field reader helper
private fun JsonObject.safeDeptString(key: String): String {
    val el = get(key) ?: return ""
    return if (el.isJsonNull || !el.isJsonPrimitive) "" else el.asString
}

// ─── CreatedBy DTO ───
data class CreatedByDto(
    val _id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val memberId: String = ""
)

// ─── Department Head DTO ───
data class DepartmentHeadDto(
    @SerializedName("_id") val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val memberId: String = "",
    val profilePicture: String? = null
)

// ─── Deserializers for polymorphic API fields ───
class FlexibleDepartmentHeadDeserializer : JsonDeserializer<DepartmentHeadDto?> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        ctx: JsonDeserializationContext
    ): DepartmentHeadDto? {
        if (json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                DepartmentHeadDto(
                    id = obj.safeDeptString("_id"),
                    firstName = obj.safeDeptString("firstName"),
                    lastName = obj.safeDeptString("lastName"),
                    memberId = obj.safeDeptString("memberId"),
                    profilePicture = obj.get("profilePicture")
                        ?.takeIf { !it.isJsonNull && it.isJsonPrimitive }
                        ?.asString
                )
            }
            json.isJsonPrimitive -> DepartmentHeadDto(id = json.asString)
            else -> null
        }
    }
}

class FlexibleDepartmentStatusDeserializer : JsonDeserializer<Boolean> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        ctx: JsonDeserializationContext
    ): Boolean {
        if (json.isJsonNull || !json.isJsonPrimitive) return true
        val prim = json.asJsonPrimitive
        return when {
            prim.isBoolean -> prim.asBoolean
            prim.isString -> prim.asString.equals("Active", ignoreCase = true) ||
                    prim.asString.equals("true", ignoreCase = true)
            else -> true
        }
    }
}

class FlexibleDepartmentOrgDeserializer : JsonDeserializer<String> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        ctx: JsonDeserializationContext
    ): String {
        if (json.isJsonNull) return ""
        return when {
            json.isJsonObject -> json.asJsonObject.safeDeptString("_id")
            json.isJsonPrimitive -> json.asString
            else -> ""
        }
    }
}

class FlexibleDepartmentCreatedByDeserializer : JsonDeserializer<CreatedByDto?> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        ctx: JsonDeserializationContext
    ): CreatedByDto? {
        if (json.isJsonNull) return null
        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                CreatedByDto(
                    _id = obj.safeDeptString("_id"),
                    firstName = obj.safeDeptString("firstName"),
                    lastName = obj.safeDeptString("lastName"),
                    memberId = obj.safeDeptString("memberId")
                )
            }
            json.isJsonPrimitive -> CreatedByDto(_id = json.asString)
            else -> null
        }
    }
}

// ─── DepartmentItem ───
data class DepartmentItem(
    val _id: String = "",
    val name: String = "",
    @JsonAdapter(FlexibleDepartmentOrgDeserializer::class)
    val organizationId: String = "",
    @JsonAdapter(FlexibleDepartmentOrgDeserializer::class)
    val branchId: String? = null,
    @SerializedName("departmentHeadId")
    @JsonAdapter(FlexibleDepartmentHeadDeserializer::class)
    val departmentHead: DepartmentHeadDto? = null,
    val description: String? = null,
    @JsonAdapter(FlexibleDepartmentStatusDeserializer::class)
    val status: Boolean = true,
    val isDefault: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    @JsonAdapter(FlexibleDepartmentCreatedByDeserializer::class)
    val createdBy: CreatedByDto? = null,
    @JsonAdapter(FlexibleDepartmentCreatedByDeserializer::class)
    val updatedBy: CreatedByDto? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    @SerializedName("employeeCount", alternate = ["totalEmployees"])
    val totalEmployees: Int = 0
) {
    val id: String get() = _id
    val departmentHeadId: String? get() = departmentHead?.id?.takeIf { it.isNotBlank() }
}

data class DepartmentPagination(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

// ─── Department Response ───
data class DepartmentResponse(
    val success: Boolean = false,
    val data: List<DepartmentItem> = emptyList(),
    val pagination: DepartmentPagination? = null
)

// ─── Department Create ───
data class DepartmentCreateRequest(
    val name: String,
    val description: String,
    val departmentHeadId: String? = null,
    val status: String = "Active"
)

data class DepartmentCreateResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: DepartmentItem? = null
)

// ─── Department Update ───
data class DepartmentUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val departmentHeadId: String? = null,
    val status: String? = null
)

data class DepartmentUpdateResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: DepartmentItem? = null
)