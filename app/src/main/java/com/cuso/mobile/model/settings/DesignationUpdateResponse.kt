package com.cuso.mobile.model.settings
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName




//   For List & Create - Keep this
data class DesignationItem(
    @SerializedName("_id") val id: String = "",
    val name: String = "",
    val code: String = "",
    val description: String = "",
    val status: Boolean = true,
    val isDeleted: Boolean = false,
    @JsonAdapter(FlexibleDesignationPersonDeserializer::class)
    val createdBy: DesignationPerson? = null,
    @JsonAdapter(FlexibleDesignationPersonDeserializer::class)
    val deletedBy: DesignationPerson? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

////   For Update Response - Keep this
//data class DesignationData(
//    @SerializedName("_id") val id: String,
//    val name: String,
//    val organizationId: String,
//    val description: String?,
//    val code: String,
//    val status: Boolean,
//    val isDeleted: Boolean,
//    val deletedAt: String?,
//    val createdBy: String,  // ← String, not object
//    val createdAt: String,
//    val updatedAt: String,
//    val __v: Int,
//    val deletedBy: String?,
//    val updatedBy: String?
//)