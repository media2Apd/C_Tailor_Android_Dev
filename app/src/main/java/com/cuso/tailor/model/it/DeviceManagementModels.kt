package com.cuso.tailor.model.it

import com.google.gson.annotations.SerializedName

/**
 * Top-level response for All Devices list.
 */
data class DeviceListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("metrics") val metrics: DeviceMetricsDto? = null,
    @SerializedName("devices") val devices: List<DeviceItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: DevicePaginationDto? = null,
    @SerializedName("message") val message: String? = null
)

/**
 * Top-level response for single Device Details.
 */
data class DeviceDetailResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: DeviceItemDto? = null,
    @SerializedName("message") val message: String? = null
)

data class DeviceMetricsDto(
    @SerializedName("totalDevices") val totalDevices: Int = 0,
    @SerializedName("online") val online: DeviceMetricCountDto? = null,
    @SerializedName("offline") val offline: DeviceMetricCountDto? = null,
    @SerializedName("maintenance") val maintenance: DeviceMetricCountDto? = null,
    @SerializedName("warning") val warning: DeviceMetricCountDto? = null
)

data class DeviceMetricCountDto(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("percentage") val percentage: String = "0%"
)

data class DevicePaginationDto(
    @SerializedName("currentPage") val currentPage: Int = 1,
    @SerializedName("totalPages") val totalPages: Int = 1,
    @SerializedName("totalRecords") val totalRecords: Int = 0
)

data class DeviceItemDto(
    @SerializedName("_id") val mongoId: String = "",
    @SerializedName("id") val id: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("deviceNo") val deviceNo: String = "",
    @SerializedName("deviceName") val deviceName: String = "",
    @SerializedName("deviceType") val deviceType: String = "",
    @SerializedName("ipAddress") val ipAddress: String? = null,
    @SerializedName("plainIp") val plainIp: String? = null,
    @SerializedName("serialNumber") val serialNumber: String = "",
    @SerializedName("status") val status: String = "Offline",
    @SerializedName("maintenanceStatus") val maintenanceStatus: String = "Normal",
    @SerializedName("manufacturer") val manufacturer: String? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("branchName") val branchName: String? = null,
    @SerializedName("lastSeen") val lastSeen: String? = null,
    @SerializedName("lastSeenFormatted") val lastSeenFormatted: String? = null,
    @SerializedName("liveDuration") val liveDuration: String? = null,
    @SerializedName("purchaseDate") val purchaseDate: String? = null,
    @SerializedName("warrantyExpiry") val warrantyExpiry: String? = null,
    @SerializedName("assignedTo") val assignedTo: DeviceAssignedToDto? = null,
    @SerializedName("hardwareSpecs") val hardwareSpecs: DeviceHardwareSpecsDto? = null,
    @SerializedName("timeTracking") val timeTracking: DeviceTimeTrackingDto? = null
) {
    val effectiveId: String
        get() = id.ifBlank { mongoId }
}

data class DeviceAssignedToDto(
    @SerializedName("name") val name: String = "",
    @SerializedName("assigneeType") val assigneeType: String = "",
    @SerializedName("assignedAt") val assignedAt: String? = null
)

data class DeviceHardwareSpecsDto(
    @SerializedName("processor") val processor: String? = null,
    @SerializedName("ram") val ram: String? = null,
    @SerializedName("storage") val storage: String? = null,
    @SerializedName("os") val os: String? = null
)

data class DeviceTimeTrackingDto(
    @SerializedName("isCurrentlyOnline") val isCurrentlyOnline: Boolean = false,
    @SerializedName("currentOnlineDuration") val currentOnlineDuration: String? = null,
    @SerializedName("currentOfflineDuration") val currentOfflineDuration: String? = null,
    @SerializedName("totalLifetimeUptime") val totalLifetimeUptime: String? = null
)