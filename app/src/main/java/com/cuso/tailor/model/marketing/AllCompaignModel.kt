package com.cuso.tailor.model.marketing

import com.google.gson.annotations.SerializedName

// ==========================================
// 1. CAMPAIGN OVERVIEW TAB MODELS
// ==========================================
data class CampaignOverviewMetadataDto(
    @SerializedName("campaignCode") val campaignCode: String = "",
    @SerializedName("campaignName") val campaignName: String = "",
    @SerializedName("platform") val platform: String = "",
    @SerializedName("channelType") val channelType: String = "",
    @SerializedName("objective") val objective: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null,
    @SerializedName("lastSyncedAt") val lastSyncedAt: String? = null
)

data class CampaignOverviewDetailsDto(
    @SerializedName("campaignSpend") val campaignSpend: Double = 0.0,
    @SerializedName("budget") val budget: Double = 0.0,
    @SerializedName("remainingBudget") val remainingBudget: Double = 0.0,
    @SerializedName("marketingRevenue") val marketingRevenue: Double = 0.0,
    @SerializedName("campaignRoi") val campaignRoi: Double = 0.0,
    @SerializedName("roiPercent") val roiPercent: Double = 0.0,
    @SerializedName("impressions") val impressions: Long = 0L,
    @SerializedName("reach") val reach: Long = 0L,
    @SerializedName("clicks") val clicks: Long = 0L,
    @SerializedName("ctr") val ctr: Double = 0.0,
    @SerializedName("cpm") val cpm: Double = 0.0,
    @SerializedName("cpc") val cpc: Double = 0.0,
    @SerializedName("customerOrders") val customerOrders: Int = 0,
    @SerializedName("orderConversions") val orderConversions: Int = 0,
    @SerializedName("cpl") val cpl: Double = 0.0,
    @SerializedName("cpa") val cpa: Double = 0.0,
    @SerializedName("metadata") val metadata: CampaignOverviewMetadataDto? = null
)

data class CampaignOverviewTabContainer(
    @SerializedName("overview") val overview: CampaignOverviewDetailsDto? = null
)

data class CampaignOverviewResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: CampaignOverviewTabContainer? = null,
    @SerializedName("message") val message: String? = null
)

// ==========================================
// 2. CAMPAIGN PERFORMANCE TAB MODELS
// ==========================================
data class CampaignFunnelStageDto(
    @SerializedName("stage") val stage: String = "",
    @SerializedName("count") val count: Long = 0L,
    @SerializedName("percentage") val percentage: Double = 0.0
)

data class CampaignPerformanceTotalsDto(
    @SerializedName("spend") val spend: Double = 0.0,
    @SerializedName("revenue") val revenue: Double = 0.0,
    @SerializedName("roas") val roas: Double = 0.0
)

data class CampaignPerformanceData(
    @SerializedName("funnel") val funnel: List<CampaignFunnelStageDto> = emptyList(),
    @SerializedName("totals") val totals: CampaignPerformanceTotalsDto? = null
)

data class CampaignPerformanceResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: CampaignPerformanceData? = null,
    @SerializedName("message") val message: String? = null
)

// ==========================================
// 3. CAMPAIGN AUDIENCE TAB MODELS
// ==========================================
data class CampaignAudienceQuarterDto(
    @SerializedName("_id") val period: String = "",
    @SerializedName("spend") val spend: Double = 0.0,
    @SerializedName("promotions") val promotions: Int = 0,
    @SerializedName("conversions") val conversions: Int = 0
)

data class CampaignAudienceResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<CampaignAudienceQuarterDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

// ==========================================
// 4. CAMPAIGN CREATIVE TAB MODELS
// ==========================================
data class CampaignCreativeItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("creativeCode") val creativeCode: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("headline") val headline: String = "",
    @SerializedName("formatType") val formatType: String = "Ad",
    @SerializedName("ctaType") val ctaType: String = "",
    @SerializedName("impressions") val impressions: Long = 0L,
    @SerializedName("clicks") val clicks: Long = 0L,
    @SerializedName("promotions") val promotions: Int = 0,
    @SerializedName("revenue") val revenue: Double = 0.0,
    @SerializedName("roi") val roi: String? = null,
    @SerializedName("roas") val roas: String? = null,
    @SerializedName("ctr") val ctr: String? = null,
    @SerializedName("mediaType") val mediaType: String? = "video",
    @SerializedName("mediaThumbnailUrl") val mediaThumbnailUrl: String? = null,
    @SerializedName("status") val status: String = "Active"
)

data class CampaignCreativeResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<CampaignCreativeItemDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)

// ==========================================
// 5. CAMPAIGN TIMELINE TAB MODELS
// ==========================================
data class CampaignTimelineEventDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("eventTitle") val eventTitle: String = "",
    @SerializedName("eventSource") val eventSource: String = "",
    @SerializedName("eventTimestamp") val eventTimestamp: String = "",
    @SerializedName("isHighlighted") val isHighlighted: Boolean = false
)

data class CampaignTimelineResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: List<CampaignTimelineEventDto> = emptyList(),
    @SerializedName("message") val message: String? = null
)