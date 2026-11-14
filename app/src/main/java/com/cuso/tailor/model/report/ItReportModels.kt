package com.cuso.tailor.model.report

import com.google.gson.annotations.SerializedName

// =========================================================================
// 1. IT ASSET REPORT MODELS
// =========================================================================

data class ItAssetReportResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<ItAssetReportItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: ItAssetReportSummaryDto? = null
)

data class ItAssetReportItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("assetId") val assetId: String = "",
    @SerializedName("assetName") val assetName: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("makeModel") val makeModel: String = "",
    @SerializedName("condition") val condition: String = "",
    @SerializedName("statusBadge") val statusBadge: String = "",
    @SerializedName("cost") val cost: Double = 0.0,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("vendor") val vendor: String = "",
    @SerializedName("warrantyUntil") val warrantyUntil: String? = null,
    @SerializedName("isWarrantyExpired") val isWarrantyExpired: Boolean = false,
    @SerializedName("location") val location: String = "",
    @SerializedName("assignedTo") val assignedTo: String = "",
    @SerializedName("assignedOn") val assignedOn: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class ItAssetReportSummaryDto(
    @SerializedName("totalAssetsCount") val totalAssetsCount: Int = 0,
    @SerializedName("totalAssetValuation") val totalAssetValuation: Double = 0.0,
    @SerializedName("assignedAssetsCount") val assignedAssetsCount: Int = 0,
    @SerializedName("availableAssetsCount") val availableAssetsCount: Int = 0,
    @SerializedName("warrantyExpiredCount") val warrantyExpiredCount: Int = 0,
    @SerializedName("maintenanceDisposalCount") val maintenanceDisposalCount: Int = 0
)

// =========================================================================
// 2. IT SERVICE DESK REPORT MODELS
// =========================================================================

data class ItServiceDeskReportResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<ItServiceDeskReportItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: ItServiceDeskReportSummaryDto? = null
)

data class ItServiceDeskReportItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("ticketId") val ticketId: String = "",
    @SerializedName("subject") val subject: String = "",
    @SerializedName("category") val category: String = "",
    @SerializedName("priority") val priority: String = "",
    @SerializedName("statusBadge") val statusBadge: String = "",
    @SerializedName("requesterName") val requesterName: String = "",
    @SerializedName("requesterDept") val requesterDept: String = "",
    @SerializedName("technician") val technician: String = "",
    @SerializedName("slaStatus") val slaStatus: String = "",
    @SerializedName("targetResolutionTime") val targetResolutionTime: String? = null,
    @SerializedName("relatedAsset") val relatedAsset: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class ItServiceDeskReportSummaryDto(
    @SerializedName("totalTickets") val totalTickets: Int = 0,
    @SerializedName("openTicketsCount") val openTicketsCount: Int = 0,
    @SerializedName("criticalHighCount") val criticalHighCount: Int = 0,
    @SerializedName("slaBreachedCount") val slaBreachedCount: Int = 0,
    @SerializedName("resolvedTicketsCount") val resolvedTicketsCount: Int = 0,
    @SerializedName("slaComplianceRate") val slaComplianceRate: String = "0%"
)