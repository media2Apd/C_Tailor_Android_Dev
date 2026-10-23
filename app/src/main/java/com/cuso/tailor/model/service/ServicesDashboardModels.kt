@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.model.service

import com.google.gson.annotations.SerializedName

//// ── Analytics / KPI Models ──
//data class ServicesAnalyticsResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String?,
//    @SerializedName("data") val data: ServicesAnalyticsData?
//)
//
//data class ServicesAnalyticsData(
//    @SerializedName("kpi") val kpi: ServicesKpis?
//)
//
//data class ServicesKpis(
//    @SerializedName("totalOrders") val totalOrders: ServiceKpiItem?,
//    @SerializedName("inWorkshop") val inWorkshop: ServiceKpiItem?,
//    @SerializedName("readyDelivery") val readyDelivery: ServiceKpiItem?,
//    @SerializedName("urgentJobs") val urgentJobs: ServiceKpiItem?,
//    @SerializedName("openComplaints") val openComplaints: ServiceKpiItem?,
//    @SerializedName("customerSatisfaction") val customerSatisfaction: ServiceKpiItem?
//)
//
//data class ServiceKpiItem(
//    @SerializedName("value") val value: Any?,
//    @SerializedName("trend") val trend: String?,
//    @SerializedName("trendUp") val trendUp: Boolean?,
//    @SerializedName("sparkline") val sparkline: List<Any?>?
//)
//
//// ── Charts Analytics Models ──
//data class ServicesChartsResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String?,
//    @SerializedName("data") val data: ServicesChartsData?
//)
//
//data class ServicesChartsData(
//    @SerializedName("statusPieChart") val statusPieChart: List<ServicePieChartItem>?,
//    @SerializedName("totalJobsCount") val totalJobsCount: Int?,
//    @SerializedName("lineChartData") val lineChartData: List<ServiceLineChartItem>?,
//    @SerializedName("monthComparison") val monthComparison: ServiceMonthComparison?
//)
//
//data class ServicePieChartItem(
//    @SerializedName("key") val key: String?,
//    @SerializedName("name") val name: String?,
//    @SerializedName("value") val value: Int?,
//    @SerializedName("percent") val percent: String?,
//    @SerializedName("color") val color: String?
//)
//
//data class ServiceLineChartItem(
//    @SerializedName("name") val name: String?,
//    @SerializedName("monthKey") val monthKey: String?,
//    @SerializedName("requests") val requests: Int?,
//    @SerializedName("completions") val completions: Int?
//)

data class ServiceMonthComparison(
    @SerializedName("currentMonth") val currentMonth: ServiceMonthData?,
    @SerializedName("previousMonth") val previousMonth: ServiceMonthData?
)

data class ServiceMonthData(
    @SerializedName("requests") val requests: Int?,
    @SerializedName("delivered") val delivered: Int?
)
//
//// ── Recent Service Requests Models ──
//data class RecentServiceRequestsResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String?,
//    @SerializedName("data") val data: List<RecentServiceRequestItem>?
//)
//
//data class RecentServiceRequestItem(
//    @SerializedName("_id") val id: String,
//    @SerializedName("serviceRequestCode") val serviceRequestCode: String?,
//    @SerializedName("primaryCategory") val primaryCategory: String?,
//    @SerializedName("customerSnapshot") val customerSnapshot: ServiceCustomerSnapshot?,
//    @SerializedName("grandTotal") val grandTotal: Double?,
//    @SerializedName("paymentStatus") val paymentStatus: String?,
//    @SerializedName("status") val status: String?,
//    @SerializedName("priority") val priority: String?,
//    @SerializedName("deliveryDate") val deliveryDate: String?,
//    @SerializedName("createdAt") val createdAt: String?
//)
//
//data class ServiceCustomerSnapshot(
//    @SerializedName("name") val name: String?,
//    @SerializedName("phone") val phone: String?
//)
//
//// ── Recent Complaints Models ──
//data class RecentComplaintsResponse(
//    @SerializedName("success") val success: Boolean,
//    @SerializedName("message") val message: String?,
//    @SerializedName("data") val data: List<RecentComplaintItem>?
//)
//
//data class RecentComplaintItem(
//    @SerializedName("_id") val id: String,
//    @SerializedName("complaintCode") val complaintCode: String?,
//    @SerializedName("customerSnapshot") val customerSnapshot: ServiceCustomerSnapshot?,
//    @SerializedName("complaintCategory") val complaintCategory: String?,
//    @SerializedName("severity") val severity: String?,
//    @SerializedName("complaintTitle") val complaintTitle: String?,
//    @SerializedName("status") val status: String?,
//    @SerializedName("createdAt") val createdAt: String?
//)

















// ── Analytics KPI Response ──
data class ServicesAnalyticsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ServicesAnalyticsData?
)

data class ServicesAnalyticsData(
    @SerializedName("kpi") val kpi: ServicesKpis?
)

data class ServicesKpis(
    @SerializedName("totalOrders") val totalOrders: ServiceKpiItem?,
    @SerializedName("inWorkshop") val inWorkshop: ServiceKpiItem?,
    @SerializedName("readyDelivery") val readyDelivery: ServiceKpiItem?,
    @SerializedName("urgentJobs") val urgentJobs: ServiceKpiItem?,
    @SerializedName("openComplaints") val openComplaints: ServiceKpiItem?,
    @SerializedName("customerSatisfaction") val customerSatisfaction: ServiceKpiItem?
)

data class ServiceKpiItem(
    @SerializedName("value") val value: Any?,
    @SerializedName("trend") val trend: String?,
    @SerializedName("trendUp") val trendUp: Boolean?,
    @SerializedName("sparkline") val sparkline: List<Any?>?
)

// ── Charts Analytics Response ──
data class ServicesChartsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ServicesChartsData?
)

data class ServicesChartsData(
    @SerializedName("statusPieChart") val statusPieChart: List<ServicePieChartItem>?,
    @SerializedName("totalJobsCount") val totalJobsCount: Int?,
    @SerializedName("lineChartData") val lineChartData: List<ServiceLineChartItem>?
)

data class ServicePieChartItem(
    @SerializedName("key") val key: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("value") val value: Int?,
    @SerializedName("percent") val percent: String?,
    @SerializedName("color") val color: String?
)

data class ServiceLineChartItem(
    @SerializedName("name") val name: String?,
    @SerializedName("monthKey") val monthKey: String?,
    @SerializedName("requests") val requests: Int?,
    @SerializedName("completions") val completions: Int?
)

// ── Recent Service Requests ──
data class RecentServiceRequestsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<RecentServiceRequestItem>?
)

data class RecentServiceRequestItem(
    @SerializedName("_id") val id: String,
    @SerializedName("serviceRequestCode") val serviceRequestCode: String?,
    @SerializedName("primaryCategory") val primaryCategory: String?,
    @SerializedName("customerSnapshot") val customerSnapshot: ServiceCustomerSnapshot?,
    @SerializedName("status") val status: String?,
    @SerializedName("priority") val priority: String?
)

// ── Active Workshop Job Cards ──
data class RecentJobCardsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<RecentJobCardItem>?
)

data class RecentJobCardItem(
    @SerializedName("_id") val id: String,
    @SerializedName("jobCardCode") val jobCardCode: String?,
    @SerializedName("salesOrderId") val salesOrderId: JobCardOrderSnapshot?,
    @SerializedName("totalQuantity") val totalQuantity: Int?,
    @SerializedName("completedQuantity") val completedQuantity: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("currentStageName") val currentStageName: String?
)

data class JobCardOrderSnapshot(
    @SerializedName("_id") val id: String?,
    @SerializedName("orderCode") val orderCode: String?
)

// ── Complaints Queue ──
data class RecentComplaintsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<RecentComplaintItem>?
)

data class RecentComplaintItem(
    @SerializedName("_id") val id: String,
    @SerializedName("complaintCode") val complaintCode: String?,
    @SerializedName("customerSnapshot") val customerSnapshot: ServiceCustomerSnapshot?,
    @SerializedName("complaintTitle") val complaintTitle: String?,
    @SerializedName("severity") val severity: String?,
    @SerializedName("status") val status: String?
)

// ── Customer Reviews ──
data class RecentFeedbacksResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: List<RecentFeedbackItem>?
)

data class RecentFeedbackItem(
    @SerializedName("_id") val id: String,
    @SerializedName("feedbackCode") val feedbackCode: String?,
    @SerializedName("salesOrderId") val salesOrderId: FeedbackOrderSnapshot?,
    @SerializedName("customerSnapshot") val customerSnapshot: ServiceCustomerSnapshot?,
    @SerializedName("overallRating") val overallRating: Int?,
    @SerializedName("reviewTitle") val reviewTitle: String?
)

data class FeedbackOrderSnapshot(
    @SerializedName("_id") val id: String?,
    @SerializedName("orderCode") val orderCode: String?
)

data class ServiceCustomerSnapshot(
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String?
)