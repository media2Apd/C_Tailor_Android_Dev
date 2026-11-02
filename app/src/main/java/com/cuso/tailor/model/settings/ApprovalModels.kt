package com.cuso.tailor.model.settings

import com.google.gson.annotations.SerializedName

data class ApprovalRulesResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<ApprovalRuleItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ApprovalPaginationDto? = null,
    @SerializedName("message") val message: String? = null
)

data class ApprovalRuleItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("entityType") val entityType: String = "",
    @SerializedName("priority") val priority: Int = 1,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("condition") val condition: ApprovalConditionDto? = null,
    @SerializedName("approverLevels") val approverLevels: List<ApproverLevelDto> = emptyList(),
    @SerializedName("createdBy") val createdBy: ApprovalUserDto? = null,
    @SerializedName("updatedBy") val updatedBy: ApprovalUserDto? = null,
    @SerializedName("createdAt") val createdAt: String = "",
    @SerializedName("updatedAt") val updatedAt: String = ""
)

data class ApprovalConditionDto(
    @SerializedName("field") val field: String = "",
    @SerializedName("operator") val operator: String = "",
    @SerializedName("value") val value: Any? = null
) {
    // Formats operator and value into readable text (e.g., "grandTotal > 10000")
    fun toReadableCriteria(): String {
        if (field.isBlank()) return "No criteria"
        val opSymbol = when (operator.lowercase()) {
            "gt" -> ">"
            "gte" -> ">="
            "lt" -> "<"
            "lte" -> "<="
            "eq" -> "="
            "ne" -> "!="
            else -> operator
        }
        return "$field $opSymbol ${value ?: ""}".trim()
    }
}

data class ApproverLevelDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("level") val level: Int = 1,
    @SerializedName("approverType") val approverType: String = "",
    @SerializedName("roleId") val roleId: String? = null,
    @SerializedName("approverId") val approverId: String? = null,
    @SerializedName("managerLevel") val managerLevel: Int? = null
)

data class ApprovalUserDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("firstName") val firstName: String = "",
    @SerializedName("lastName") val lastName: String = "",
    @SerializedName("memberId") val memberId: String = ""
) {
    val fullName: String
        get() = "$firstName $lastName".trim()
}

data class ApprovalPaginationDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

data class CreateApprovalRuleRequest(
    @SerializedName("name") val name: String,
    @SerializedName("entityType") val entityType: String,
    @SerializedName("priority") val priority: Int,
    @SerializedName("condition") val condition: ApprovalConditionDto?,
    @SerializedName("approverLevels") val approverLevels: List<CreateApproverLevelDto>
)

data class CreateApproverLevelDto(
    @SerializedName("level") val level: Int,
    @SerializedName("approverType") val approverType: String,
    @SerializedName("managerLevel") val managerLevel: Int? = null,
    @SerializedName("roleId") val roleId: String? = null,
    @SerializedName("approverId") val approverId: String? = null
)