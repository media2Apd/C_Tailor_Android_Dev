@file:Suppress("unused")
package com.cuso.tailor.model.marketing

import com.cuso.tailor.utils.currencySymbol
import com.google.gson.annotations.SerializedName

// =========================================================================
// 0. SHARED METRIC VALUE DTO (Handles both String & Numeric JSON values)
// =========================================================================

data class MetricValueDto(
    @SerializedName("value")
    private val rawValue: Any? = null,

    @SerializedName("raw")
    val raw: Double? = null,

    @SerializedName("subtitle")
    val subtitle: String? = null
) {
    /**
     * Clean, human-readable display string regardless of whether the API
     * returned a String ("${currencySymbol()}0.3L", "12.4K") or a Number (1, 95, 0).
     */
    val value: String
        get() = when (val v = rawValue) {
            null -> "--"
            is Number -> {
                if (v.toDouble() == v.toLong().toDouble()) v.toLong().toString()
                else v.toString()
            }
            else -> v.toString()
        }
}

// =========================================================================
// 1. MARKETING DASHBOARD SUMMARY
// GET /api/marketing/dashboard/summary?timeframe=this_week&trendRange=6M
// =========================================================================

data class MarketingDashboardSummaryResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: MarketingDashboardSummaryData? = null
)

data class MarketingDashboardSummaryData(
    @SerializedName("kpiRibbonRow1")
    val kpiRibbonRow1: SummaryKpiRow1Dto? = null,

    @SerializedName("kpiRibbonRow2")
    val kpiRibbonRow2: SummaryKpiRow2Dto? = null,

    @SerializedName("growthTrend")
    val growthTrend: List<GrowthTrendItemDto> = emptyList(),

    @SerializedName("conversionFunnel")
    val conversionFunnel: ConversionFunnelDto? = null
)

data class SummaryKpiRow1Dto(
    @SerializedName("marketingRevenue") val marketingRevenue: MetricValueDto? = null,
    @SerializedName("marketingSpend") val marketingSpend: MetricValueDto? = null,
    @SerializedName("roas") val roas: MetricValueDto? = null,
    @SerializedName("ordersFromMarketing") val ordersFromMarketing: MetricValueDto? = null,
    @SerializedName("newCustomers") val newCustomers: MetricValueDto? = null,
    @SerializedName("conversionRate") val conversionRate: MetricValueDto? = null
)

data class SummaryKpiRow2Dto(
    @SerializedName("returningCustomers") val returningCustomers: MetricValueDto? = null,
    @SerializedName("activeCampaigns") val activeCampaigns: MetricValueDto? = null,
    @SerializedName("avgOrderValue") val avgOrderValue: MetricValueDto? = null,
    @SerializedName("websiteTraffic") val websiteTraffic: MetricValueDto? = null,
    @SerializedName("costPerAcquisition") val costPerAcquisition: MetricValueDto? = null,
    @SerializedName("repeatPurchaseRate") val repeatPurchaseRate: MetricValueDto? = null
)

data class GrowthTrendItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("spend") val spend: Double = 0.0,
    @SerializedName("revenue") val revenue: Double = 0.0,
    @SerializedName("promotions") val promotions: Long = 0,
    @SerializedName("conversions") val conversions: Long = 0,
    @SerializedName("month") val month: String = "",
    @SerializedName("roas") val roas: Double = 0.0
)

data class ConversionFunnelDto(
    @SerializedName("stages") val stages: List<FunnelStageDto> = emptyList(),
    @SerializedName("summary") val summary: FunnelSummaryDto? = null
)

data class FunnelStageDto(
    @SerializedName("stage") val stage: String = "",
    @SerializedName("count") val count: Long = 0,
    @SerializedName("conversionRate") val conversionRate: Double = 0.0,
    @SerializedName("dropoffRate") val dropoffRate: Double = 0.0
)

data class FunnelSummaryDto(
    @SerializedName("overallConversionRate") val overallConversionRate: String? = "0.00%",
    @SerializedName("visitorToOrderRate") val visitorToOrderRate: String? = "0.0%"
)

// =========================================================================
// 2. MARKETING CHANNEL OVERVIEW
// GET /api/marketing/channels/overview?days=30
// =========================================================================

data class ChannelOverviewResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ChannelOverviewData? = null
)

data class ChannelOverviewData(
    @SerializedName("kpiCards")
    val kpiCards: Map<String, MetricValueDto> = emptyMap(),

    @SerializedName("channelPerformanceMatrix")
    val channelPerformanceMatrix: List<ChannelMatrixItemDto> = emptyList()
)

data class ChannelMatrixItemDto(
    @SerializedName("channel") val channel: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("spend") val spend: String = "N/A",
    @SerializedName("impressions") val impressions: String = "N/A",
    @SerializedName("clicks") val clicks: String = "N/A",
    @SerializedName("ctr") val ctr: String = "N/A",
    @SerializedName("promotions") val promotions: String = "0",
    @SerializedName("qualLeads") val qualLeads: String = "0",
    @SerializedName("customers") val customers: String = "0",
    @SerializedName("conversions") val conversions: String = "0",
    @SerializedName("convRate") val convRate: String = "N/A",
    @SerializedName("cpl") val cpl: String = "N/A",
    @SerializedName("cac") val cac: String = "N/A",
    @SerializedName("revenue") val revenue: String = "N/A",
    @SerializedName("roas") val roas: String = "N/A",
    @SerializedName("roi") val roi: String = "N/A",
    @SerializedName("raw") val raw: ChannelRawMetricsDto? = null
)

data class ChannelRawMetricsDto(
    @SerializedName("spend") val spend: Double = 0.0,
    @SerializedName("rev") val rev: Double = 0.0,
    @SerializedName("conv") val conv: Long = 0,
    @SerializedName("prom") val prom: Long = 0,
    @SerializedName("cust") val cust: Long = 0,
    @SerializedName("qual") val qual: Long = 0
)

// =========================================================================
// 3. MARKETING SOCIAL OVERVIEW
// GET /api/marketing/social/overview?days=30
// =========================================================================

data class SocialOverviewResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SocialOverviewData? = null
)

data class SocialOverviewData(
    @SerializedName("kpiRow1")
    val kpiRow1: Map<String, MetricValueDto> = emptyMap(),

    @SerializedName("kpiRow2")
    val kpiRow2: Map<String, MetricValueDto> = emptyMap(),

    @SerializedName("posts")
    val posts: List<SocialPostDto> = emptyList(),

    @SerializedName("platformCards")
    val platformCards: List<SocialPlatformCardDto> = emptyList()
)

data class SocialPostDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("creativeCode") val creativeCode: String = "",
    @SerializedName("campaignCode") val campaignCode: String = "",
    @SerializedName("campaignName") val campaignName: String = "",
    @SerializedName("organizationId") val organizationId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("headline") val headline: String = "",
    @SerializedName("primaryText") val primaryText: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("platform") val platform: String = "Meta",
    @SerializedName("formatType") val formatType: String = "Ad",
    @SerializedName("mediaType") val mediaType: String = "video",
    @SerializedName("mediaThumbnailUrl") val mediaThumbnailUrl: String = "",
    @SerializedName("mediaUrl") val mediaUrl: String = "",
    @SerializedName("destinationUrl") val destinationUrl: String = "",
    @SerializedName("ctaType") val ctaType: String = "",
    @SerializedName("status") val status: String = "ACTIVE",
    @SerializedName("reach") val reach: Long = 0,
    @SerializedName("impressions") val impressions: Long = 0,
    @SerializedName("views") val views: Long = 0,
    @SerializedName("clicks") val clicks: Long = 0,
    @SerializedName("conversions") val conversions: Long = 0,
    @SerializedName("likes") val likes: Long = 0,
    @SerializedName("comments") val comments: Long = 0,
    @SerializedName("shares") val shares: Long = 0,
    @SerializedName("saves") val saves: Long = 0,
    @SerializedName("spend") val spend: Double = 0.0,
    @SerializedName("revenue") val revenue: Double = 0.0,
    @SerializedName("engagementRate") val engagementRate: Double = 0.0,
    @SerializedName("adCreatedAt") val adCreatedAt: String? = null,
    @SerializedName("lastSyncedAt") val lastSyncedAt: String? = null
)

data class SocialPlatformCardDto(
    @SerializedName("platform") val platform: String = "",
    @SerializedName("followers") val followers: String = "0",
    @SerializedName("growth") val growth: String = "0%",
    @SerializedName("engagementRate") val engagementRate: String = "0%"
)

// =========================================================================
// 4. MARKETING WEB ANALYTICS OVERVIEW
// GET /api/marketing/web-analytics/overview?timeframe=30D
// =========================================================================

data class WebAnalyticsOverviewResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: WebAnalyticsOverviewData? = null
)

data class WebAnalyticsOverviewData(
    @SerializedName("kpiRibbonRow1")
    val kpiRibbonRow1: Map<String, MetricValueDto> = emptyMap(),

    @SerializedName("kpiRibbonRow2")
    val kpiRibbonRow2: Map<String, MetricValueDto> = emptyMap(),

    @SerializedName("conversionSummary")
    val conversionSummary: WebConversionSummaryDto? = null,

    @SerializedName("trafficTrend")
    val trafficTrend: List<WebTrafficTrendItemDto> = emptyList(),

    @SerializedName("sources")
    val sources: List<WebSourceItemDto> = emptyList(),

    @SerializedName("devices")
    val devices: List<WebDeviceItemDto> = emptyList(),

    @SerializedName("allPages")
    val allPages: List<WebPageItemDto> = emptyList(),

    @SerializedName("lastSynced")
    val lastSynced: String? = null
)

data class WebConversionSummaryDto(
    @SerializedName("pageViews") val pageViews: String = "0",
    @SerializedName("clicks") val clicks: String = "0",
    @SerializedName("totalConversions") val totalConversions: String = "0",
    @SerializedName("conversionRate") val conversionRate: String = "0%",
    @SerializedName("promotions") val promotions: String = "0",
    @SerializedName("customers") val customers: String = "0",
    @SerializedName("attributedRevenue") val attributedRevenue: String = "${currencySymbol()}0.0L",
    @SerializedName("revenuePerUser") val revenuePerUser: String = "${currencySymbol()}0"
)

data class WebTrafficTrendItemDto(
    @SerializedName("date") val date: String = "",
    @SerializedName("users") val users: Long = 0,
    @SerializedName("sessions") val sessions: Long = 0,
    @SerializedName("pageViews") val pageViews: Long = 0,
    @SerializedName("clicks") val clicks: Long = 0
)

data class WebSourceItemDto(
    @SerializedName("source") val source: String = "",
    @SerializedName("medium") val medium: String = "",
    @SerializedName("channel") val channel: String = "",
    @SerializedName("campaign") val campaign: String = "",
    @SerializedName("users") val users: Long = 0,
    @SerializedName("sessions") val sessions: Long = 0,
    @SerializedName("pageViews") val pageViews: Long = 0,
    @SerializedName("clicks") val clicks: Long = 0,
    @SerializedName("conversions") val conversions: Long = 0
)

data class WebDeviceItemDto(
    @SerializedName("deviceType") val deviceType: String = "",
    @SerializedName("users") val users: Long = 0,
    @SerializedName("sessions") val sessions: Long = 0,
    @SerializedName("clicks") val clicks: Long = 0,
    @SerializedName("conversions") val conversions: Long = 0
)

data class WebPageItemDto(
    @SerializedName("path") val path: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("pageViews") val pageViews: Long = 0,
    @SerializedName("clicks") val clicks: Long = 0,
    @SerializedName("users") val users: Long = 0,
    @SerializedName("sessions") val sessions: Long = 0,
    @SerializedName("engRate") val engRate: String = "0%",
    @SerializedName("avgTime") val avgTime: String = "0m 00s",
    @SerializedName("bounce") val bounce: String = "0%",
    @SerializedName("exit") val exit: String = "0.0%",
    @SerializedName("conversions") val conversions: String = "0",
    @SerializedName("convRate") val convRate: String = "0%"
)

// =========================================================================
// 5. MARKETING WEB ANALYTICS TRAFFIC ENGAGEMENT
// GET /api/marketing/web-analytics/traffic-engagement?timeframe=30D
// =========================================================================

data class TrafficEngagementResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: TrafficEngagementData? = null
)

data class TrafficEngagementData(
    @SerializedName("kpiRow1")
    val kpiRow1: Map<String, MetricValueDto> = emptyMap(),

    @SerializedName("kpiRow2")
    val kpiRow2: Map<String, MetricValueDto> = emptyMap(),

    @SerializedName("trafficTrend")
    val trafficTrend: List<WebTrafficTrendItemDto> = emptyList(),

    @SerializedName("newVsReturning")
    val newVsReturning: TrafficNewVsReturningDto? = null,

    @SerializedName("devicePerformance")
    val devicePerformance: List<WebDeviceItemDto> = emptyList(),

    @SerializedName("topPages")
    val topPages: List<TrafficTopPageDto> = emptyList()
)

data class TrafficNewVsReturningDto(
    @SerializedName("newUsers") val newUsers: TrafficUserCountRatioDto? = null,
    @SerializedName("returningUsers") val returningUsers: TrafficUserCountRatioDto? = null
)

data class TrafficUserCountRatioDto(
    @SerializedName("count") val count: String = "0",
    @SerializedName("percentage") val percentage: Double = 0.0
)

data class TrafficTopPageDto(
    @SerializedName("path") val path: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("pageViews") val pageViews: Long = 0,
    @SerializedName("clicks") val clicks: Long = 0,
    @SerializedName("sessions") val sessions: Long = 0
)

// =========================================================================
// 6. MARKETING CUSTOMER OVERVIEW
// GET /api/marketing/customers/overview?days=30
// =========================================================================

data class CustomerOverviewResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: CustomerOverviewData? = null
)

data class CustomerOverviewData(
    @SerializedName("kpiRow1")
    val kpiRow1: Map<String, MetricValueDto> = emptyMap(),

    @SerializedName("kpiRow2")
    val kpiRow2: Map<String, MetricValueDto> = emptyMap(),

    @SerializedName("newVsReturning")
    val newVsReturning: CustomerNewVsReturningBreakdownDto? = null,

    @SerializedName("customerRetention")
    val customerRetention: CustomerRetentionDto? = null,

    @SerializedName("monthlyTrend")
    val monthlyTrend: List<CustomerMonthlyTrendDto> = emptyList()
)

data class CustomerNewVsReturningBreakdownDto(
    @SerializedName("new") val newSegment: CustomerSegmentMetricDto? = null,
    @SerializedName("returning") val returningSegment: CustomerSegmentMetricDto? = null
)

data class CustomerSegmentMetricDto(
    @SerializedName("count") val count: Long = 0,
    @SerializedName("percentage") val percentage: Double = 0.0,
    @SerializedName("revenue") val revenue: Double = 0.0,
    @SerializedName("aov") val aov: Double = 0.0
)

data class CustomerRetentionDto(
    @SerializedName("retentionRate") val retentionRate: String = "0%",
    @SerializedName("repeatCustomerRate") val repeatCustomerRate: String = "0%",
    @SerializedName("churnRate") val churnRate: String = "0%",
    @SerializedName("atRiskCustomers") val atRiskCustomers: Long = 0,
    @SerializedName("churnedCustomers") val churnedCustomers: Long = 0
)

data class CustomerMonthlyTrendDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("totalCustomers") val totalCustomers: Long = 0
)

// =========================================================================
// 7. MARKETING CAMPAIGNS LEDGER
// GET /api/marketing/campaigns
// =========================================================================

data class CampaignsListResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<CampaignItemDto> = emptyList()
)

data class CampaignItemDto(
    @SerializedName("campaignCode") val campaignCode: String = "",
    @SerializedName("campaignName") val campaignName: String = "",
    @SerializedName("externalCampaignId") val externalCampaignId: String = "",
    @SerializedName("platform") val platform: String = "Meta",
    @SerializedName("startDate") val startDate: String = "",
    @SerializedName("endDate") val endDate: String = "",
    @SerializedName("status") val status: String = "Active",
    @SerializedName("spend") val spend: Double = 0.0,
    @SerializedName("spendValue") val spendValue: Double = 0.0,
    @SerializedName("impressions") val impressions: Long = 0,
    @SerializedName("reach") val reach: Long = 0,
    @SerializedName("clicks") val clicks: Long = 0,
    @SerializedName("ctr") val ctr: Double = 0.0,
    @SerializedName("cpa") val cpa: Double = 0.0,
    @SerializedName("promotions") val promotions: Long = 0,
    @SerializedName("customerOrders") val customerOrders: Long = 0,
    @SerializedName("conversions") val conversions: Long = 0,
    @SerializedName("revenue") val revenue: Double = 0.0,
    @SerializedName("revenueValue") val revenueValue: Double = 0.0,
    @SerializedName("roas") val roas: String = "0x",
    @SerializedName("roi") val roi: String = "—"
) {
    val calculatedRoas: Double
        get() = if (spendValue > 0.0) revenueValue / spendValue else 0.0
}

// =========================================================================
// 8. MARKETING CALENDAR VIEW
// GET /api/marketing/calendar/view?year=2026&month=10
// =========================================================================

data class MarketingCalendarResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: MarketingCalendarData? = null
)

data class MarketingCalendarData(
    @SerializedName("kpiCards")
    val kpiCards: CalendarKpiCardsDto? = null,

    @SerializedName("calendarMatrix")
    val calendarMatrix: CalendarMatrixDto = CalendarMatrixDto(),

    @SerializedName("lastSynced")
    val lastSynced: String? = null
)

data class CalendarKpiCardsDto(
    @SerializedName("totalActivities") val totalActivities: Int = 0,
    @SerializedName("scheduled") val scheduled: Int = 0,
    @SerializedName("active") val active: Int = 0,
    @SerializedName("completed") val completed: Int = 0,
    @SerializedName("emailActivities") val emailActivities: Int = 0,
    @SerializedName("campaignAds") val campaignAds: Int = 0
)

data class CalendarMatrixDto(
    @SerializedName("year") val year: Int = 2026,
    @SerializedName("month") val month: Int = 10,
    @SerializedName("monthName") val monthName: String = "",
    @SerializedName("days") val days: List<CalendarDayDto> = emptyList()
)

data class CalendarDayDto(
    @SerializedName("dayNumber") val dayNumber: Int = 1,
    @SerializedName("date") val date: String = "",
    @SerializedName("isHighVolume") val isHighVolume: Boolean = false,
    @SerializedName("activitiesCount") val activitiesCount: Int = 0,
    @SerializedName("activities") val activities: List<CalendarActivityDto> = emptyList()
)

data class CalendarActivityDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("activityType") val activityType: String = "Advertising",
    @SerializedName("platform") val platform: String = "Meta",
    @SerializedName("channel") val channel: String = "Paid Social",
    @SerializedName("status") val status: String = "Active",
    @SerializedName("startDate") val startDate: String = "",
    @SerializedName("endDate") val endDate: String = "",
    @SerializedName("color") val color: String? = null,
    @SerializedName("badgeBg") val badgeBg: String? = null,
    @SerializedName("isMultiDay") val isMultiDay: Boolean = false
)