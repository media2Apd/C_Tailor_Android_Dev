package com.cuso.tailor.model.report

import com.google.gson.annotations.SerializedName

// =============================================================================
// CAMPAIGN PERFORMANCE & ROI REPORT
// GET /api/marketing/reports/campaign-performance?page=1&limit=10
// =============================================================================

data class CampaignPerformanceReportResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<CampaignPerformanceItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: CampaignPerformanceSummaryDto? = null,
)

// =============================================================================
// MARKETING CHANNEL PERFORMANCE REPORT
// GET /api/marketing/reports/channel-performance?page=1&limit=10
// =============================================================================

data class ChannelPerformanceReportResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<ChannelPerformanceItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: ChannelPerformanceSummaryDto? = null,
)
