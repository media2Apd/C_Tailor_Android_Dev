package com.cuso.tailor.model.report

import com.google.gson.annotations.SerializedName

data class MarketingReportPaginationDto(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 10,
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("hasNextPage") val hasNextPage: Boolean = false,
    @SerializedName("hasPreviousPage") val hasPreviousPage: Boolean = false
)

// =============================================================================
// CAMPAIGN PERFORMANCE & ROI REPORT
// GET /api/marketing/reports/campaign-performance?page=1&limit=10
// =============================================================================

data class CampaignPerformanceReportResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<Map<String, Any?>> = emptyList(),
    @SerializedName("pagination") val pagination: MarketingReportPaginationDto? = null,
    @SerializedName("summary") val summary: CampaignPerformanceReportSummaryDto? = null
)

data class CampaignPerformanceReportSummaryDto(
    @SerializedName("totalCampaigns") val totalCampaigns: Int = 0,
    @SerializedName("totalBudgetAllocated") val totalBudgetAllocated: Double = 0.0,
    @SerializedName("totalActualSpend") val totalActualSpend: Double = 0.0,
    @SerializedName("totalAttributedRevenue") val totalAttributedRevenue: Double = 0.0,
    @SerializedName("totalConversionsCount") val totalConversionsCount: Int = 0,
    @SerializedName("totalClicksCount") val totalClicksCount: Int = 0,
    @SerializedName("overallRoi") val overallRoi: String = "0%"
)

// =============================================================================
// MARKETING CHANNEL PERFORMANCE REPORT
// GET /api/marketing/reports/channel-performance?page=1&limit=10
// =============================================================================

data class ChannelPerformanceReportResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<Map<String, Any?>> = emptyList(),
    @SerializedName("pagination") val pagination: MarketingReportPaginationDto? = null,
    @SerializedName("summary") val summary: ChannelPerformanceReportSummaryDto? = null
)

data class ChannelPerformanceReportSummaryDto(
    @SerializedName("totalChannels") val totalChannels: Int = 0,
    @SerializedName("totalAcquiredCustomers") val totalAcquiredCustomers: Int = 0,
    @SerializedName("totalOrders") val totalOrders: Int = 0,
    @SerializedName("totalChannelRevenue") val totalChannelRevenue: Double = 0.0
)