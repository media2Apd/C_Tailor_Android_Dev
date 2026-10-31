package com.cuso.tailor.model.it

import com.google.gson.annotations.SerializedName

/**
 * Top-level response for the Backup Dashboard API.
 */
data class BackupDashboardResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("metrics") val metrics: BackupMetricsDto? = null,
    @SerializedName("jobs") val jobs: List<BackupJobItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: BackupPaginationDto? = null
)

/**
 * Summary metrics shown in the top cards on the list screen.
 */
data class BackupMetricsDto(
    @SerializedName("totalJobs") val totalJobs: Int = 0,
    @SerializedName("successful") val successful: Int = 0,
    @SerializedName("failed") val failed: Int = 0,
    @SerializedName("running") val running: Int = 0,
    @SerializedName("nextBackup") val nextBackup: String? = null
)

/**
 * Represents each backup job item in the list screen.
 */
data class BackupJobItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("branchId") val branchId: String? = null,
    @SerializedName("jobName") val jobName: String = "",
    @SerializedName("sourceEntity") val sourceEntity: String? = null,
    @SerializedName("backupType") val backupType: String? = "Full",
    @SerializedName("environment") val environment: String? = "Production",
    @SerializedName("storageMedium") val storageMedium: String? = "Local",
    @SerializedName("retentionDays") val retentionDays: Int = 0,
    @SerializedName("scheduleCron") val scheduleCron: String? = null,
    @SerializedName("lastBackupAt") val lastBackupAt: String? = null,
    @SerializedName("lastBackupSize") val lastBackupSize: String? = null,
    @SerializedName("lastBackupSizeBytes") val lastBackupSizeBytes: Long = 0,
    @SerializedName("status") val status: String = "Successful",
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("nextRunAt") val nextRunAt: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
) {
    val effectiveId: String
        get() = id
}

data class BackupPaginationDto(
    @SerializedName("currentPage") val currentPage: Int = 1,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("totalRecords") val totalRecords: Int = 0
)

/**
 * Top-level response for single Backup Snapshot / Detail endpoint.
 */
data class BackupDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: BackupSnapshotDetailDto? = null
)

/**
 * Nested job details returned inside snapshot detail.
 */
data class NestedBackupJobDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("environment") val environment: String? = null,
    @SerializedName("retentionDays") val retentionDays: Int? = null,
    @SerializedName("scheduleCron") val scheduleCron: String? = null,
    @SerializedName("nextRunAt") val nextRunAt: String? = null
)

/**
 * Complete snapshot detail data object for the detail screen.
 */
data class BackupSnapshotDetailDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String? = null,
    @SerializedName("backupJobId") val backupJobId: NestedBackupJobDto? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("sourceEntity") val sourceEntity: String? = null,
    @SerializedName("backupType") val backupType: String? = "Full",
    @SerializedName("environment") val environment: String? = "Production",
    @SerializedName("branchLevel") val branchLevel: String? = "Global/HQ",
    @SerializedName("storageMedium") val storageMedium: String? = "Cloud",
    @SerializedName("storageAllocationPercent") val storageAllocationPercent: Int = 100,
    @SerializedName("sizeBytes") val sizeBytes: Long = 0,
    @SerializedName("formattedSize") val formattedSize: String? = null,
    @SerializedName("durationFormatted") val durationFormatted: String? = null,
    @SerializedName("recordsProcessed") val recordsProcessed: Long = 0,
    @SerializedName("status") val status: String = "Successful",
    @SerializedName("healthStatus") val healthStatus: String? = "Healthy",
    @SerializedName("verificationStatus") val verificationStatus: String? = "Verified",
    @SerializedName("storagePathOrKey") val storagePathOrKey: String? = null,
    @SerializedName("checksum") val checksum: String? = null,
    @SerializedName("backupId") val backupId: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

/**
 * Advanced options object sent in the restore request payload.
 */
data class RestoreAdvancedOptionsDto(
    @SerializedName("verifyIntegrityAfterRestore") val verifyIntegrityAfterRestore: Boolean = true,
    @SerializedName("createSafetySnapshot") val createSafetySnapshot: Boolean = true
)

/**
 * Request payload sent to POST /api/it/backups/restore.
 */
data class ExecuteRestoreBackupRequest(
    @SerializedName("backupId") val backupId: String,
    @SerializedName("restoreDestination") val restoreDestination: String,
    @SerializedName("restoreType") val restoreType: String,
    @SerializedName("selectedSchemas") val selectedSchemas: List<String> = emptyList(),
    @SerializedName("advancedOptions") val advancedOptions: RestoreAdvancedOptionsDto = RestoreAdvancedOptionsDto(),
    @SerializedName("userAcknowledgedOverwrite") val userAcknowledgedOverwrite: Boolean = true
)

/**
 * Response received from POST /api/it/backups/restore.
 */
data class ExecuteRestoreBackupResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("restoreId") val restoreId: String? = null,
    @SerializedName("estimatedDuration") val estimatedDuration: String? = null,
    @SerializedName("status") val status: String? = null
)