package com.cuso.mobile.model.settings

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class SegmentDetailResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SegmentItem? = null
)

data class SegmentListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pagination") val pagination: PaginationDto? = null,
    @SerializedName("data") val data: List<SegmentItem> = emptyList()
)

data class PaginationDto(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class SegmentItem(
    @SerializedName("_id") val id: String,
    @SerializedName("organizationId") val organizationId: String,
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("code") val code: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("displayOrder") val displayOrder: Int = 0,
    @SerializedName("imageUrl") val imageUrl: String? = null,
    @SerializedName("imagePublicId") val imagePublicId: String? = null,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("createdBy") val createdBy: UserMetaDto? = null,
    @SerializedName("updatedBy") val updatedBy: UserMetaDto? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("__v") val v: Int? = null
)

@JsonAdapter(UserMetaDtoDeserializer::class)
data class UserMetaDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("firstName") val firstName: String? = null,
    @SerializedName("lastName") val lastName: String? = null,
    @SerializedName("memberId") val memberId: String? = null
)

class UserMetaDtoDeserializer : JsonDeserializer<UserMetaDto?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): UserMetaDto? {
        if (json == null || json.isJsonNull) return null

        return when {
            json.isJsonObject -> {
                val obj = json.asJsonObject
                UserMetaDto(
                    id = obj.get("_id")?.asString,
                    firstName = obj.get("firstName")?.asString,
                    lastName = obj.get("lastName")?.asString,
                    memberId = obj.get("memberId")?.asString
                )
            }
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                UserMetaDto(id = json.asString)
            }
            else -> null
        }
    }
}

data class CreateSegmentRequest(
    @SerializedName("name") val name: String,
    @SerializedName("displayName") val displayName: String = name,
    @SerializedName("code") val code: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("displayOrder") val displayOrder: Int = 0,
    @SerializedName("status") val status: String = "Active"
)

//data class UpdateSegmentRequest(
//    @SerializedName("name") val name: String,
//    @SerializedName("displayName") val displayName: String = name,
//    @SerializedName("code") val code: String,
//    @SerializedName("description") val description: String? = null,
//    @SerializedName("displayOrder") val displayOrder: Int = 0,
//    @SerializedName("status") val status: String = "Active"
//)

data class CreateSegmentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SegmentItem? = null
)

data class DeleteSegmentResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

//change status
data class ChangeSegmentStatusRequest(
    val status: String
)

data class ChangeSegmentStatusResponse(
    val success: Boolean,
    val message: String,
    val data: SegmentItem?
)