@file:Suppress("unused")

package com.cuso.tailor.model.security

import com.google.gson.annotations.SerializedName

// =========================================================================
// NESTED BACKUP DOMAIN MODELS
// =========================================================================

data class BackupScheduleDto(
    @SerializedName("enabled")
    val enabled: Boolean = true,
    @SerializedName("frequency")
    val frequency: String = "",
    @SerializedName("time")
    val time: String = "",
    @SerializedName("timezone")
    val timezone: String = ""
)

data class BackupProtectionDto(
    @SerializedName("encryption")
    val encryption: Boolean = true,
    @SerializedName("compression")
    val compression: Boolean = true,
    @SerializedName("algorithm")
    val algorithm: String? = ""
)

data class BackupDestinationDto(
    @SerializedName("target")
    val target: String = "",
    @SerializedName("filename")
    val filename: String? = null,
    @SerializedName("gridFsFileId")
    val gridFsFileId: String? = null
)

data class BackupRetentionDto(
    @SerializedName("dailyDays")
    val dailyDays: Int = 30,
    @SerializedName("weeklyWeeks")
    val weeklyWeeks: Int = 12
)

data class BackupVerificationDto(
    @SerializedName("integrityCheck")
    val integrityCheck: String = "",
    @SerializedName("checksum")
    val checksum: String = "",
    @SerializedName("backupReadiness")
    val backupReadiness: String = "",
    @SerializedName("checksumSha256")
    val checksumSha256: String? = null
)

data class BackupExecutionMetricsDto(
    @SerializedName("startedAt")
    val startedAt: String? = null,
    @SerializedName("completedAt")
    val completedAt: String? = null,
    @SerializedName("durationMinutes")
    val durationMinutes: Int = 0,
    @SerializedName("durationFormatted")
    val durationFormatted: String = "",
    @SerializedName("initiatedBy")
    val initiatedBy: String = ""
)

data class BackupTimelineStepDto(
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
// BACKUP RECORD MODEL
// =========================================================================

data class BackupItemDto(
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("organizationId")
    val organizationId: String? = null,
    @SerializedName("backupId")
    val backupId: String = "",
    @SerializedName("restorePointId")
    val restorePointId: String? = null,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("description")
    val description: String = "",
    @SerializedName("source")
    val source: String = "",
    @SerializedName("sourceTarget")
    val sourceTarget: String = "",
    @SerializedName("type")
    val type: String = "",
    @SerializedName("sizeBytes")
    val sizeBytes: Long = 0L,
    @SerializedName("sizeFormatted")
    val sizeFormatted: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("schedule")
    val schedule: BackupScheduleDto = BackupScheduleDto(),
    @SerializedName("protection")
    val protection: BackupProtectionDto = BackupProtectionDto(),
    @SerializedName("destination")
    val destination: BackupDestinationDto = BackupDestinationDto(),
    @SerializedName("retention")
    val retention: BackupRetentionDto = BackupRetentionDto(),
    @SerializedName("verification")
    val verification: BackupVerificationDto = BackupVerificationDto(),
    @SerializedName("executionMetrics")
    val executionMetrics: BackupExecutionMetricsDto = BackupExecutionMetricsDto(),
    @SerializedName("timeline")
    val timeline: List<BackupTimelineStepDto> = emptyList(),
    @SerializedName("createdBy")
    val createdBy: String? = null,
    @SerializedName("updatedBy")
    val updatedBy: String? = null,
    @SerializedName("isDeleted")
    val isDeleted: Boolean = false,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

// =========================================================================
// API REQUEST & RESPONSE PAYLOADS
// =========================================================================

data class CreateBackupRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("source")
    val source: String = "",
    @SerializedName("sourceTarget")
    val sourceTarget: String = "",
    @SerializedName("type")
    val type: String = "",
    @SerializedName("size")
    val size: String = "",
    @SerializedName("destination")
    val destination: DestinationRequest = DestinationRequest(),
    @SerializedName("protection")
    val protection: ProtectionRequest = ProtectionRequest(),
    @SerializedName("retention")
    val retention: RetentionRequest = RetentionRequest(),
    @SerializedName("schedule")
    val schedule: ScheduleRequest = ScheduleRequest()
)

data class DestinationRequest(
    @SerializedName("target")
    val target: String = ""
)

data class ProtectionRequest(
    @SerializedName("encryption")
    val encryption: Boolean = true,
    @SerializedName("compression")
    val compression: Boolean = true
)

data class RetentionRequest(
    @SerializedName("dailyDays")
    val dailyDays: Int = 30,
    @SerializedName("weeklyWeeks")
    val weeklyWeeks: Int = 12
)

data class ScheduleRequest(
    @SerializedName("enabled")
    val enabled: Boolean = true,
    @SerializedName("frequency")
    val frequency: String = "",
    @SerializedName("time")
    val time: String = "",
    @SerializedName("timezone")
    val timezone: String = ""
)

data class BackupListApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("count")
    val count: Int = 0,
    @SerializedName("data")
    val data: List<BackupItemDto> = emptyList(),
    @SerializedName("message")
    val message: String? = null
)

data class BackupDetailApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: BackupItemDto? = null,
    @SerializedName("message")
    val message: String? = null
)

data class CreateBackupApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: BackupItemDto? = null
)

data class BackupAuditReportResponse(
    @SerializedName("reportTitle")
    val reportTitle: String = "",
    @SerializedName("backupId")
    val backupId: String = "",
    @SerializedName("restorePointId")
    val restorePointId: String? = null,
    @SerializedName("sourceCluster")
    val sourceCluster: String = "",
    @SerializedName("type")
    val type: String = "",
    @SerializedName("size")
    val size: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("verification")
    val verification: BackupVerificationDto = BackupVerificationDto(),
    @SerializedName("timelineAudit")
    val timelineAudit: List<BackupTimelineStepDto> = emptyList(),
    @SerializedName("executionMetrics")
    val executionMetrics: BackupExecutionMetricsDto = BackupExecutionMetricsDto(),
    @SerializedName("downloadedAt")
    val downloadedAt: String = ""
)

data class RunBackupApiResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: BackupItemDto? = null
)