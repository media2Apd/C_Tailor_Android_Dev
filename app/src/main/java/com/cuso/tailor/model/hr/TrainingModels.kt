package com.cuso.tailor.model.hr

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/**
 * Training program data model.
 * Uses JsonElement for TrainerName to safely handle both populated objects and ID strings.
 */
data class TrainingProgramDto(
    @SerializedName("_id")
    val id: String = "",
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("programTitle")
    val programTitle: String = "",
    @SerializedName("programId")
    val programId: String = "",
    @SerializedName("trainingType")
    val trainingType: String = "",
    @SerializedName("durationMethod")
    val durationMethod: String? = "Hour",
    @SerializedName("duration")
    val duration: Double = 0.0,
    @SerializedName("modeOfDelivery")
    val modeOfDelivery: String = "",
    @SerializedName("TrainerName")
    val trainerNameElement: JsonElement? = null,
    @SerializedName("programDescription")
    val programDescription: String? = null,
    @SerializedName("isActive")
    val isActive: Boolean = true,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
) {
    /**
     * Extracts trainer ID safely regardless of whether backend returned an object or a plain string ID.
     */
    fun getTrainerId(): String {
        return if (trainerNameElement != null && trainerNameElement.isJsonObject) {
            trainerNameElement.asJsonObject.get("_id")?.asString.orEmpty()
        } else if (trainerNameElement != null && trainerNameElement.isJsonPrimitive) {
            trainerNameElement.asString.orEmpty()
        } else ""
    }

    /**
     * Extracts trainer's full name if populated, otherwise falls back to ID or default label.
     */
    fun getTrainerDisplayName(): String {
        return if (trainerNameElement != null && trainerNameElement.isJsonObject) {
            val obj = trainerNameElement.asJsonObject
            val first = obj.get("firstName")?.asString.orEmpty()
            val last = obj.get("lastName")?.asString.orEmpty()
            "$first $last".trim().ifBlank { "Assigned Trainer" }
        } else if (trainerNameElement != null && trainerNameElement.isJsonPrimitive) {
            trainerNameElement.asString.orEmpty()
        } else "Not Assigned"
    }
}

data class TrainingProgramPagination(
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("limit")
    val limit: Int = 10,
    @SerializedName("totalPages")
    val totalPages: Int = 1
)

data class TrainingProgramListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: List<TrainingProgramDto> = emptyList(),
    @SerializedName("pagination")
    val pagination: TrainingProgramPagination? = null
)

data class TrainingProgramSingleResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: TrainingProgramDto? = null
)

data class SimpleActionResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null
)

data class SaveTrainingProgramRequest(
    val programTitle: String,
    val programId: String,
    val trainingType: String,
    val durationMethod: String = "Hour",
    val duration: Double,
    val modeOfDelivery: String,
    val TrainerName: String,
    val programDescription: String = "",
    val isActive: Boolean = true
)