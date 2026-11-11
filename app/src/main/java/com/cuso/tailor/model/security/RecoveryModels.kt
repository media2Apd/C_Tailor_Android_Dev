@file:Suppress("unused")

package com.cuso.tailor.model.security

import com.google.gson.annotations.SerializedName

// =========================================================================
// RECOVERY NESTED MODELS
// =========================================================================

data class RecoveryDestinationDto(
    @SerializedName("type")
    val type: String = "",
    @SerializedName("targetEnvironment")
    val targetEnvironment: String = ""
)

data class RecoveryOptionsDto(
    @SerializedName("overwriteExisting")
    val overwriteExisting: Boolean = false,
    @SerializedName("verifyDataAfterRestore")
    val verifyDataAfterRestore: Boolean = true,
    @SerializedName("restartServices")
    val restartServices: Boolean = false
)

data class RecoveryProgressDto(
    @SerializedName("percentage")
    val percentage: Int = 0,
    @SerializedName("currentStepText")
    val currentStepText: String = "",
    @SerializedName("estimatedTimeRemaining")
    val estimatedTimeRemaining: String = ""
)

data class RecoveryVerificationDto(
    @SerializedName("dataIntegrity")
    val dataIntegrity: String = "",
    @SerializedName("postRestore")
    val postRestore: String = "",
    @SerializedName("serviceHealth")
    val serviceHealth: String = ""
)

data class RecoveryTimelineStepDto(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("time")
    val time: String = "",
    @SerializedName("status")
    val status: String = ""
)

// =========================================================================
// RECOVERY INITIATION REQUEST & ITEM MODEL
// =========================================================================

data class InitiateRecoveryRequest(
    @SerializedName("backupId")
    val backupId: String,
    @SerializedName("destinationLocation")
    val destinationLocation: String,
    @SerializedName("destinationType")
    val destinationType: String,
    @SerializedName("overwriteAcknowledged")
    val overwriteAcknowledged: Boolean,
    @SerializedName("overwriteExisting")
    val overwriteExisting: Boolean,
    @SerializedName("restartServices")
    val restartServices: Boolean,
    @SerializedName("verifyDataAfterRestore")
    val verifyDataAfterRestore: Boolean
)

data class RecoveryItemDto(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("recoveryId")
    val recoveryId: String = "",
    @SerializedName("backupId")
    val backupId: String = "",
    @SerializedName("backupSource")
    val backupSource: String = "",
    @SerializedName("sourceTarget")
    val sourceTarget: String = "",
    @SerializedName("backupType")
    val backupType: String = "",
    @SerializedName("backupSizeFormatted")
    val backupSizeFormatted: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("startedAt")
    val startedAt: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("destination")
    val destination: RecoveryDestinationDto = RecoveryDestinationDto(),
    @SerializedName("options")
    val options: RecoveryOptionsDto = RecoveryOptionsDto(),
    @SerializedName("progress")
    val progress: RecoveryProgressDto = RecoveryProgressDto(),
    @SerializedName("verification")
    val verification: RecoveryVerificationDto = RecoveryVerificationDto(),
    @SerializedName("timeline")
    val timeline: List<RecoveryTimelineStepDto> = emptyList()
)

// API Responses
data class InitiateRecoveryApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: RecoveryItemDto? = null
)

data class RecoveryDetailApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: RecoveryItemDto? = null
)