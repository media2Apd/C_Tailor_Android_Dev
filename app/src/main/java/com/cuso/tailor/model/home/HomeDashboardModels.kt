package com.cuso.tailor.model.home

import com.google.gson.annotations.SerializedName

// ── 1. KPI Summary Models ──
data class HomeKpiResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: HomeKpiData? = null
)

data class HomeKpiData(
    @SerializedName("kpis") val kpis: HomeKpisGroup? = null
)

data class HomeKpisGroup(
    @SerializedName("salesRevenue") val salesRevenue: KpiMetricItem? = null,
    @SerializedName("totalOrders") val totalOrders: KpiMetricItem? = null,
    @SerializedName("netProfit") val netProfit: KpiMetricItem? = null,
    @SerializedName("receivables") val receivables: KpiMetricItem? = null,
    @SerializedName("inventoryValue") val inventoryValue: KpiMetricItem? = null,
    @SerializedName("deliverySuccess") val deliverySuccess: KpiMetricItem? = null
)

data class KpiMetricItem(
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("growth") val growth: String? = null,
    @SerializedName("trendUp") val trendUp: Boolean? = null,
    @SerializedName("percentage") val percentage: String? = null,
    @SerializedName("subLabel") val subLabel: String? = null,
    @SerializedName("overdueLabel") val overdueLabel: String? = null,
    @SerializedName("reorderLabel") val reorderLabel: String? = null,
    @SerializedName("comparisonLabel") val comparisonLabel: String? = null
)

// ── 2. Revenue Trend Models ──
data class RevenueTrendResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: RevenueTrendData? = null
)

data class RevenueTrendData(
    @SerializedName("summary") val summary: RevenueTrendSummary? = null,
    @SerializedName("chartData") val chartData: List<RevenueTrendPoint> = emptyList()
)

data class RevenueTrendSummary(
    @SerializedName("totalCurrentRevenue") val totalCurrentRevenue: Double = 0.0,
    @SerializedName("totalPreviousRevenue") val totalPreviousRevenue: Double = 0.0,
    @SerializedName("overallGrowth") val overallGrowth: String? = null
)

data class RevenueTrendPoint(
    @SerializedName("month") val month: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("currentRevenue") val currentRevenue: Double = 0.0,
    @SerializedName("growth") val growth: String? = null
)

// ── 3. Marketing Performance Models ──
data class HomeMarketingResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: HomeMarketingData? = null
)

data class HomeMarketingData(
    @SerializedName("headerStats") val headerStats: MarketingHeaderStats? = null,
    @SerializedName("channels") val channels: List<MarketingChannelItem> = emptyList()
)

data class MarketingHeaderStats(
    @SerializedName("roi") val roi: String? = "0.0x",
    @SerializedName("leadsGenerated") val leadsGenerated: Int = 0,
    @SerializedName("badgeText") val badgeText: String? = null
)

data class MarketingChannelItem(
    @SerializedName("channel") val channel: String = "",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("barProgress") val barProgress: Float = 0f,
    @SerializedName("color") val color: String = "#4F46E5"
)

// ── 4. Donut Chart Models (Orders, Stock, Attendance, Services) ──
data class DonutSectionResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: DonutSectionData? = null
)

data class DonutSectionData(
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
    @SerializedName("totalOrders") val totalOrders: Int? = null,
    @SerializedName("totalSKUs") val totalSKUs: Int? = null,
    @SerializedName("totalEmployees") val totalEmployees: Int? = null,
    @SerializedName("totalRequests") val totalRequests: Int? = null,
    @SerializedName("unit") val unit: String? = null,
    @SerializedName("segments") val segments: List<DonutSliceItem> = emptyList()
)

data class DonutSliceItem(
    @SerializedName("status") val status: String = "",
    @SerializedName("count") val count: Float = 0f,
    @SerializedName("color") val color: String = "#3B82F6",
    @SerializedName("percentage") val percentage: Int = 0
)

// ── 5. Top Products Leaderboard Models ──
data class TopProductsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: TopProductsData? = null
)

data class TopProductsData(
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
    @SerializedName("products") val products: List<LeaderboardProductItem> = emptyList()
)

data class LeaderboardProductItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("orders") val orders: Int = 0,
    @SerializedName("revenue") val revenue: Double = 0.0,
    @SerializedName("growth") val growth: String = ""
)

// ── 6. Action Required Models ──
data class HomeActionRequiredResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: HomeActionRequiredData? = null
)

data class HomeActionRequiredData(
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String = "",
    @SerializedName("actions") val actions: List<HomeActionTaskItem> = emptyList()
)

data class HomeActionTaskItem(
    @SerializedName("module") val module: String = "",
    @SerializedName("actionRequired") val actionRequired: String = "",
    @SerializedName("count") val count: Int = 0,
    @SerializedName("priority") val priority: String = "Medium",
    @SerializedName("priorityColor") val priorityColor: String = "#F59E0B",
    @SerializedName("due") val due: String = "Today",
    @SerializedName("navigationLink") val navigationLink: String? = null
)