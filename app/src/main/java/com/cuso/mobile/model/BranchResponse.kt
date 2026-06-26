package com.cuso.mobile.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class BranchListResponse(
    val success: Boolean,
    val data: List<BranchItem>
)

data class BranchItem(
    @SerializedName("_id") val id: String,
    @JsonAdapter(FlexibleOrganizationDeserializer::class)
    val organizationId: BranchOrganization? = null,
    val branchId: String = "",
    val name: String = "",
    val address: BranchListAddress = BranchListAddress(),
    val contactEmail: String = "",
    val contactMobile: String = "",
    val isMainBranch: Boolean = false,
    val status: String = "",
    val isDeleted: Boolean = false,
    @JsonAdapter(FlexibleCreatedByDeserializer::class)
    val createdBy: BranchCreatedBy? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val slug: String = "",
    @JsonAdapter(FlexibleBranchHeadDeserializer::class)
    val branchHead: BranchHead? = null,
    @JsonAdapter(FlexibleCreatedByDeserializer::class)
    val updatedBy: BranchCreatedBy? = null
)

// ── Generic helper: safely get string from JsonObject ──
private fun JsonElement?.safeString(key: String): String =
    this?.asJsonObject?.get(key)?.takeIf { it !is JsonNull }?.asString ?: ""

// ── organizationId ──
class FlexibleOrganizationDeserializer : JsonDeserializer<BranchOrganization?> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): BranchOrganization? {
        if (json is JsonNull) return null
        return if (json.isJsonObject)
            BranchOrganization(
                id   = json.safeString("_id"),
                name = json.safeString("name")
            )
        else BranchOrganization(id = json.asString, name = "")
    }
}

// ── createdBy / updatedBy ──
class FlexibleCreatedByDeserializer : JsonDeserializer<BranchCreatedBy?> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): BranchCreatedBy? {
        if (json is JsonNull) return null
        return if (json.isJsonObject)
            BranchCreatedBy(
                id        = json.safeString("_id"),
                firstName = json.safeString("firstName"),
                lastName  = json.safeString("lastName")
            )
        else BranchCreatedBy(id = json.asString, firstName = "", lastName = "")
    }
}

// ── branchHead ──
class FlexibleBranchHeadDeserializer : JsonDeserializer<BranchHead?> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): BranchHead? {
        if (json is JsonNull) return null
        return if (json.isJsonObject)
            BranchHead(
                id        = json.safeString("_id"),
                role      = json.safeString("role"),
                firstName = json.safeString("firstName"),
                lastName  = json.safeString("lastName"),
                status    = json.safeString("status")
            )
        else BranchHead(id = json.asString, role = "", firstName = "", lastName = "", status = "")
    }
}

data class BranchOrganization(
    @SerializedName("_id") val id: String,
    val name: String
)

data class BranchListAddress(
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val country: String? = null
)

data class BranchCreatedBy(
    @SerializedName("_id") val id: String,
    val firstName: String,
    val lastName: String
)

data class BranchHead(
    @SerializedName("_id") val id: String,
    val role: String,
    val firstName: String,
    val lastName: String,
    val status: String
)