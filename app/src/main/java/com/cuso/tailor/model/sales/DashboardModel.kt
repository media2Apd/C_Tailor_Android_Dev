@file:Suppress("unused")
package com.cuso.tailor.model.sales

import com.google.gson.annotations.SerializedName

// 1. KPI METRICS
data class KpiMetricsResponse(
    val success: Boolean,
    val message: String,
    val data: KpiMetricsData?
)

data class KpiMetricsData(
    val periodFilterApplied: PeriodFilterApplied?,
    val kpiCards: KpiCardsContainer?
)

data class PeriodFilterApplied(
    val period: String,
    val periodLabel: String,
    val startDate: String,
    val endDate: String
)

data class KpiCardsContainer(
    val totalRevenue: KpiCardMetric?,
    val salesOrderCount: KpiCardMetric?,
    val quotationsSentCount: KpiCardMetric?,
    val pendingReceivables: KpiCardMetric?,
    val activeCustomerCount: KpiCardMetric?,
    val conversionRate: KpiCardMetric?
)

data class KpiCardMetric(
    val value: Double?,
    val formattedValue: String?,
    val trend: String?,
    val trendUp: Boolean?,
    val sparkline: List<Double> = emptyList()
)

// 2. DASHBOARD CHARTS
data class DashboardChartsResponse(
    val success: Boolean,
    val message: String,
    val data: DashboardChartsData?
)

data class DashboardChartsData(
    val totalOrdersInMonth: Int = 0,
    val statusPieChart: List<ChartStatusSlice> = emptyList(),
    val revenueOrderTrend: List<MonthlyTrendPoint> = emptyList()
)

data class ChartStatusSlice(
    val statusKey: String,
    val name: String,
    val count: Int,
    val amount: Double,
    val percent: Int,
    val color: String
)

data class MonthlyTrendPoint(
    val name: String,
    val monthKey: String,
    val orders: Int,
    val revenue: Double,
    val collected: Double
)

// 3. EXECUTIVE ANALYTICS
data class ExecutiveAnalyticsResponse(
    val success: Boolean,
    val message: String,
    val data: ExecutiveAnalyticsData?
)

data class ExecutiveAnalyticsData(
    val salespersonLeaderboard: List<LeaderboardItem> = emptyList(),
    val revenueStreams: List<RevenueStreamItem> = emptyList(),
    val paymentModes: List<PaymentModeItem> = emptyList(),
    val receivables: List<ReceivablesItem> = emptyList()
)

data class LeaderboardItem(
    @SerializedName("_id") val id: String,
    val name: String,
    val totalOrders: Int,
    val totalRevenue: Double,
    val collectedAmount: Double,
    val balanceDue: Double
)

data class RevenueStreamItem(
    val type: String,
    val label: String,
    val revenue: Double,
    val itemsCount: Int,
    val percent: Int,
    val color: String
)

data class PaymentModeItem(
    val mode: String,
    val ordersCount: Int,
    val amount: Double
)

data class ReceivablesItem(
    val status: String,
    val ordersCount: Int,
    val balanceDue: Double,
    val collected: Double
)

// 4. OPPORTUNITY ANALYTICS
data class OpportunityAnalyticsResponse(
    val success: Boolean,
    val message: String,
    val data: OpportunityAnalyticsData?
)

data class OpportunityAnalyticsData(
    val kpi: OpportunityKpi?,
    val stageFunnel: List<StageFunnelItem> = emptyList()
)

data class OpportunityKpi(
    val totalPipelineValue: Double = 0.0,
    val weightedForecastValue: Double = 0.0,
    val activeDealsCount: Int = 0,
    val wonDealsCount: Int = 0,
    val winRate: String = "0%",
    val avgDealSize: Double = 0.0,
    val closingIn7DaysCount: Int = 0
)

data class StageFunnelItem(
    @SerializedName("_id") val id: String,
    val stageName: String,
    val stageCode: String,
    val probability: Int,
    val totalStageValue: Double,
    val weightedValue: Double,
    val color: String,
    val percentOfTotal: Int
)

// 5. QUOTATIONS ANALYTICS
data class QuotationAnalyticsResponse(
    val success: Boolean,
    val message: String,
    val data: QuotationAnalyticsData?
)

data class QuotationAnalyticsData(
    val pipelineSummary: QuotationPipelineSummary?,
    val statusDonutChart: List<QuotationDonutSlice> = emptyList()
)

data class QuotationPipelineSummary(
    val totalQuotesCount: Int = 0,
    val totalPipelineValue: Double = 0.0,
    val activeQuotesCount: Int = 0,
    val activeQuotesValue: Double = 0.0,
    val conversionRate: String = "0%",
    val avgQuoteValue: Double = 0.0,
    val expiringIn7DaysCount: Int = 0
)

data class QuotationDonutSlice(
    val statusKey: String,
    val name: String,
    val count: Int,
    val value: Double,
    val percent: Int,
    val color: String
)

// 6. LEADS ANALYTICS
data class LeadAnalyticsResponse(
    val success: Boolean,
    val message: String,
    val data: LeadAnalyticsData?
)

data class LeadAnalyticsData(
    val kpi: LeadKpi?,
    val statusDonutChart: List<LeadDonutSlice> = emptyList()
)

data class LeadKpi(
    val totalLeadsCount: Int = 0,
    val activeLeadsCount: Int = 0,
    val convertedCount: Int = 0,
    val conversionRate: String = "0%",
    val todayFollowUpsCount: Int = 0,
    val overdueFollowUpsCount: Int = 0
)

data class LeadDonutSlice(
    val statusKey: String,
    val name: String,
    val count: Int,
    val percent: Int,
    val color: String
)

// 7. LEAD FOLLOW-UP ANALYTICS
data class FollowUpAnalyticsResponse(
    val success: Boolean,
    val message: String,
    val data: FollowUpAnalyticsData?
)

data class FollowUpAnalyticsData(
    val kpi: FollowUpKpi?,
    val channelDonutChart: List<ChannelDonutSlice> = emptyList()
)

data class FollowUpKpi(
    val totalInteractions: Int = 0,
    val callsCount: Int = 0,
    val whatsAppCount: Int = 0,
    val appointmentsCount: Int = 0,
    val emailsCount: Int = 0,
    val todayUpcomingTasks: Int = 0
)

data class ChannelDonutSlice(
    val typeKey: String,
    val name: String,
    val count: Int,
    val percent: Int,
    val color: String
)

// 8. CUSTOMER ANALYTICS
data class CustomerAnalyticsResponse(
    val success: Boolean,
    val message: String,
    val data: CustomerAnalyticsData?
)

data class CustomerAnalyticsData(
    val kpi: CustomerKpi?,
    val customerLevelPie: List<CustomerLevelSlice> = emptyList(),
    val contactMethods: List<CustomerContactMethod> = emptyList(),
    val customerTypes: CustomerTypesSummary?
)

data class CustomerKpi(
    val totalCustomers: Int = 0,
    val activeCustomers: Int = 0,
    val thisMonthOnboarded: Int = 0,
    val momGrowth: String = "0%",
    val trendUp: Boolean = true,
    val sparkline: List<Double> = emptyList(),
    val vipEliteRatio: String = "0%",
    val corporateRatio: String = "0%"
)

data class CustomerLevelSlice(
    val levelKey: String,
    val name: String,
    val count: Int,
    val percent: Int,
    val color: String
)

data class CustomerContactMethod(
    val method: String,
    val count: Int,
    val percent: Int
)

data class CustomerTypesSummary(
    val individual: Int = 0,
    val corporate: Int = 0
)

// 9. RECENT SALES ORDERS
data class RecentOrdersResponse(
    val success: Boolean,
    val message: String,
    val data: List<RecentOrderItem> = emptyList()
)

data class RecentOrderItem(
    @SerializedName("_id") val id: String,
    val orderCode: String,
    val orderDate: String?,
    val dueDate: String?,
    val orderType: String?,
    val priority: String?,
    val status: String?,
    val financials: OrderFinancials?,
    val customer: OrderCustomerRef?,
    val salespersonName: String? = null
)

data class OrderFinancials(
    val currency: String?,
    val grandTotal: Double?,
    val advancePaid: Double?,
    val balanceDue: Double?
)

data class OrderCustomerRef(
    val name: String?,
    val phone: String?,
    val code: String?
)

// 10. RECENT QUOTATIONS
data class RecentQuotationsResponse(
    val success: Boolean,
    val message: String,
    val data: List<RecentQuotationItem> = emptyList()
)

data class RecentQuotationItem(
    @SerializedName("_id") val id: String,
    val quotationCode: String,
    val quotationDate: String?,
    val expiryDate: String?,
    val grandTotal: Double?,
    val currency: String?,
    val status: String?,
    val customer: QuotationCustomerRef?
)

data class QuotationCustomerRef(
    val name: String?,
    val phone: String?
)

// 11. RECENT CUSTOMERS
data class RecentCustomersResponse(
    val success: Boolean,
    val message: String,
    val data: List<RecentCustomerItem> = emptyList()
)

data class RecentCustomerItem(
    @SerializedName("_id") val id: String,
    val customerCode: String,
    val fullName: String,
    val mobileNumber: String,
    val email: String,
    val customerType: String,
    val status: String
)

// 12. RECENT OPPORTUNITIES
data class RecentOpportunitiesResponse(
    val success: Boolean,
    val message: String,
    val data: List<RecentOpportunityItem> = emptyList()
)

data class RecentOpportunityItem(
    @SerializedName("_id") val id: String,
    val opportunityCode: String,
    val name: String,
    val estimatedValue: Double?,
    val stage: OpportunityStageRef?,
    val clientName: String?,
    val clientPhone: String?,
    val expectedClosingDate: String?
)

data class OpportunityStageRef(
    val name: String,
    val percentage: Int
)

// 13. RECENT LEADS
data class RecentLeadsResponse(
    val success: Boolean,
    val message: String,
    val data: List<RecentLeadItem> = emptyList()
)

data class RecentLeadItem(
    @SerializedName("_id") val id: String,
    val leadCode: String,
    val fullName: String,
    val mobileNumber: String,
    val leadSource: String?,
    val priorityLevel: String?,
    val leadStatus: String?
)

// 14. RECENT FOLLOW-UPS
data class RecentFollowUpsResponse(
    val success: Boolean,
    val message: String,
    val data: List<RecentFollowUpItem> = emptyList()
)

data class RecentFollowUpItem(
    @SerializedName("_id") val id: String,
    val leadCode: String?,
    val clientName: String?,
    val clientPhone: String?,
    val followUpType: String?,
    val nextFollowUpDate: String?,
    val nextFollowUpTime: String?,
    val loggedBy: String?
)