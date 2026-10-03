package com.cuso.tailor.model.hr

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Model representing an assigned training program.
 * Uses JsonElement for trainingProgramId and organizationMemberIds to safely handle
 * both populated objects and ID strings from the backend.
 */
data class AssignedTrainingProgramDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("trainingProgramId")
    val trainingProgramElement: JsonElement? = null,
    @SerializedName("departmentIds")
    val departmentIds: List<String> = emptyList(),
    @SerializedName("organizationMemberIds")
    val organizationMembersElement: JsonElement? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("endDate")
    val endDate: String? = null,
    @SerializedName("mode")
    val mode: String = "Virtual",
    @SerializedName("notes")
    val notes: String? = null,
    @SerializedName("status")
    val status: String = "Assigned",
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    fun getProgramId(): String {
        return if (trainingProgramElement != null && trainingProgramElement.isJsonObject) {
            trainingProgramElement.asJsonObject.get("_id")?.asString.orEmpty()
        } else if (trainingProgramElement != null && trainingProgramElement.isJsonPrimitive) {
            trainingProgramElement.asString.orEmpty()
        } else ""
    }

    fun getProgramTitle(): String {
        return if (trainingProgramElement != null && trainingProgramElement.isJsonObject) {
            trainingProgramElement.asJsonObject.get("programTitle")?.asString.orEmpty()
                .ifBlank { "Training Program" }
        } else "Training Program"
    }

    fun getTrainingType(): String {
        return if (trainingProgramElement != null && trainingProgramElement.isJsonObject) {
            trainingProgramElement.asJsonObject.get("trainingType")?.asString.orEmpty()
                .ifBlank { "Technical" }
        } else "Technical"
    }

    fun getPrimaryMemberName(): String {
        if (organizationMembersElement != null && organizationMembersElement.isJsonArray) {
            val firstMember = organizationMembersElement.asJsonArray.firstOrNull()
            if (firstMember != null && firstMember.isJsonObject) {
                val first = firstMember.asJsonObject.get("firstName")?.asString.orEmpty()
                val last = firstMember.asJsonObject.get("lastName")?.asString.orEmpty()
                return "$first $last".trim().ifBlank { "Trainee Employee" }
            }
        }
        return "Trainee Employee"
    }

    fun getMemberIds(): List<String> {
        val list = mutableListOf<String>()
        if (organizationMembersElement != null && organizationMembersElement.isJsonArray) {
            organizationMembersElement.asJsonArray.forEach { elem ->
                if (elem.isJsonObject) {
                    elem.asJsonObject.get("_id")?.asString?.let { list.add(it) }
                } else if (elem.isJsonPrimitive) {
                    list.add(elem.asString)
                }
            }
        }
        return list
    }
}

data class AssignedTrainingPagination(
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("limit")
    val limit: Int = 10,
    @SerializedName("totalPages")
    val totalPages: Int = 1
)

data class AssignedTrainingListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<AssignedTrainingProgramDto> = emptyList(),
    @SerializedName("pagination")
    val pagination: AssignedTrainingPagination? = null
)

data class AssignedTrainingSingleResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: AssignedTrainingProgramDto? = null
)

data class SaveAssignedTrainingRequest(
    val trainingProgramId: String,
    val departmentIds: List<String> = emptyList(),
    val organizationMemberIds: List<String> = emptyList(),
    val startDate: String,
    val endDate: String,
    val mode: String,
    val notes: String = "",
    val status: String = "Assigned"
)