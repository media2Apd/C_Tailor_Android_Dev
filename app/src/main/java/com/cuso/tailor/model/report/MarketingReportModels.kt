@file:Suppress("unused")

package com.cuso.tailor.model.report

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =============================================================================
// 1. CAMPAIGN PERFORMANCE & ROI REPORT
// =============================================================================

@Serializable
data class CampaignPerformanceItemDto(
    @SerializedName("_id")
    @SerialName("_id")
    val id: String = "",
    @SerializedName("campaignName") val campaignName: String = "",
    @SerializedName("campaignCode") val campaignCode: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("platform") val platform: String = "",
    @SerializedName("channel") val channel: String = "",
    @SerializedName("budget") val budget: Double = 0.0,
    @SerializedName("actualSpend") val actualSpend: Double = 0.0,
    @SerializedName("attributedRevenue") val attributedRevenue: Double = 0.0,
    @SerializedName("roas") val roas: String = "0x",
    @SerializedName("roiPercent") val roiPercent: String = "0%",
    @SerializedName("roiValue") val roiValue: Double = 0.0,
    @SerializedName("conversionsCount") val conversionsCount: Int = 0,
    @SerializedName("clicksCount") val clicksCount: Int = 0,
    @SerializedName("createdAt") val createdAt: String? = null
)

@Serializable
data class CampaignPerformanceSummaryDto(
    @SerializedName("totalCampaigns") val totalCampaigns: Int = 0,
    @SerializedName("totalBudgetAllocated") val totalBudgetAllocated: Double = 0.0,
    @SerializedName("totalActualSpend") val totalActualSpend: Double = 0.0,
    @SerializedName("totalAttributedRevenue") val totalAttributedRevenue: Double = 0.0,
    @SerializedName("totalConversionsCount") val totalConversionsCount: Int = 0,
    @SerializedName("totalClicksCount") val totalClicksCount: Int = 0,
    @SerializedName("overallRoi") val overallRoi: String = "0%"
)

// =============================================================================
// 2. MARKETING CHANNEL PERFORMANCE REPORT
// =============================================================================

@Serializable
data class ChannelPerformanceItemDto(
    @SerializedName("_id")
    @SerialName("_id")
    val id: String = "",
    @SerializedName("channelName") val channelName: String = "",
    @SerializedName("channelCode") val channelCode: String = "",
    @SerializedName("channelType") val channelType: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("acquiredCustomers") val acquiredCustomers: Int = 0,
    @SerializedName("totalOrders") val totalOrders: Int = 0,
    @SerializedName("channelRevenue") val channelRevenue: Double = 0.0,
    @SerializedName("averageOrderValue") val averageOrderValue: Double = 0.0,
    @SerializedName("conversionRate") val conversionRate: String = "0%",
    @SerializedName("revenueSharePercent") val revenueSharePercent: String = "0%",
    @SerializedName("createdAt") val createdAt: String? = null
)

@Serializable
data class ChannelPerformanceSummaryDto(
    @SerializedName("totalChannels") val totalChannels: Int = 0,
    @SerializedName("totalAcquiredCustomers") val totalAcquiredCustomers: Int = 0,
    @SerializedName("totalOrders") val totalOrders: Int = 0,
    @SerializedName("totalChannelRevenue") val totalChannelRevenue: Double = 0.0
)
