package com.cuso.tailor.model.hr

import com.google.gson.annotations.SerializedName

data class OrgStructureResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("data")
    val data: List<OrgMemberDto> = emptyList(),
    @SerializedName("message")
    val message: String? = null
)

data class OrgMemberDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("role")
    val role: String = "", // "owner", "custom", "manager", etc.
    @SerializedName("customRoleId")
    val customRoleId: String? = null,
    @SerializedName("branchId")
    val branchId: String? = null,
    @SerializedName("designationId")
    val designationId: OrgDesignationRef? = null,
    @SerializedName("firstName")
    val firstName: String = "",
    @SerializedName("lastName")
    val lastName: String = "",
    @SerializedName("memberId")
    val memberId: String? = null,
    @SerializedName("gender")
    val gender: String? = null,
    @SerializedName("departmentId")
    val departmentId: OrgDepartmentRef? = null,
    @SerializedName("reportingTo")
    val reportingTo: String? = null
) {
    val fullName: String get() = "$firstName $lastName".trim()

    val initials: String
        get() {
            val f = firstName.firstOrNull()?.uppercaseChar() ?: ' '
            val l = lastName.firstOrNull()?.uppercaseChar() ?: ' '
            return "$f$l".trim().ifBlank { "??" }
        }
}

data class OrgDesignationRef(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("name")
    val name: String = ""
)

data class OrgDepartmentRef(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("name")
    val name: String = ""
)