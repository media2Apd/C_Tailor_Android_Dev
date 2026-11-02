package com.cuso.tailor.model.marketing

import com.google.gson.annotations.SerializedName

data class CampaignsDashboardResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: CampaignsDashboardData? = null
)

data class CampaignsDashboardData(
    @SerializedName("campaigns")
    val campaigns: List<CampaignItemDto> = emptyList(),

    @SerializedName("dailyTrend")
    val dailyTrend: List<CampaignDailyTrendDto> = emptyList()
)

data class CampaignDailyTrendDto(
    @SerializedName("date") val date: String = "",
    @SerializedName("spend") val spend: Double = 0.0,
    @SerializedName("impressions") val impressions: Long = 0,
    @SerializedName("clicks") val clicks: Long = 0,
    @SerializedName("promotions") val promotions: Long = 0,
    @SerializedName("conversions") val conversions: Long = 0,
    @SerializedName("orders") val orders: Long = 0
)

data class PlatformPerformanceSummary(
    val platform: String,
    val spend: Double,
    val conversions: Long,
    val roi: Double,
    val leads: Long
)

data class CampaignStatusBreakdown(
    val status: String,
    val count: Int,
    val percentage: Int
)