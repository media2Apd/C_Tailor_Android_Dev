package com.cuso.tailor.model.logistics

import com.google.gson.annotations.SerializedName

data class LogisticsDashboardResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: LogisticsDashboardData? = null,
    @SerializedName("message") val message: String? = null
)

data class LogisticsDashboardData(
    @SerializedName("kpiCards") val kpiCards: LogisticsKpiCards? = null,
    @SerializedName("shipmentsAndDeliveriesTrend") val shipmentsAndDeliveriesTrend: List<ShipmentDeliveryTrendItem> = emptyList(),
    @SerializedName("deliveryPerformance") val deliveryPerformance: DeliveryPerformanceMetrics? = null,
    @SerializedName("shipmentStatusBreakdown") val shipmentStatusBreakdown: ShipmentStatusBreakdown? = null,
    @SerializedName("shipmentsByCourierPartner") val shipmentsByCourierPartner: List<CourierPartnerItem> = emptyList(),
    @SerializedName("recentShipments") val recentShipments: List<RecentShipmentItem> = emptyList(),
    @SerializedName("codOrdersSummary") val codOrdersSummary: List<CodOrderSummaryItem> = emptyList()
)

data class LogisticsKpiCards(
    @SerializedName("totalOrders") val totalOrders: KpiMetricItem? = null,
    @SerializedName("deliveredOrders") val deliveredOrders: KpiMetricItem? = null,
    @SerializedName("inTransit") val inTransit: KpiMetricItem? = null,
    @SerializedName("pendingPickup") val pendingPickup: KpiMetricItem? = null,
    @SerializedName("returnOrders") val returnOrders: KpiMetricItem? = null,
    @SerializedName("codCollections") val codCollections: CodMetricItem? = null,
    @SerializedName("newCustomerSignups") val newCustomerSignups: KpiMetricItem? = null
)

data class KpiMetricItem(
    @SerializedName("value") val value: Int = 0,
    @SerializedName("comparison") val comparison: String = ""
)

data class CodMetricItem(
    @SerializedName("value") val value: String = "",
    @SerializedName("rawAmount") val rawAmount: Double = 0.0,
    @SerializedName("comparison") val comparison: String = ""
)

data class ShipmentDeliveryTrendItem(
    @SerializedName("month") val month: String = "",
    @SerializedName("shipments") val shipments: Int = 0,
    @SerializedName("deliveries") val deliveries: Int = 0,
    @SerializedName("returns") val returns: Int = 0
)

data class DeliveryPerformanceMetrics(
    @SerializedName("onTimeDeliveryRate") val onTimeDeliveryRate: PerformanceTargetItem? = null,
    @SerializedName("deliverySuccessRate") val deliverySuccessRate: PerformanceTargetItem? = null,
    @SerializedName("returnRate") val returnRate: PerformanceTargetItem? = null,
    @SerializedName("avgDeliveryTime") val avgDeliveryTime: PerformanceTargetItem? = null,
    @SerializedName("codCollectionRate") val codCollectionRate: PerformanceTargetItem? = null
)

data class PerformanceTargetItem(
    @SerializedName("target") val target: String = "",
    @SerializedName("actual") val actual: String = "",
    @SerializedName("status") val status: String = ""
)

data class ShipmentStatusBreakdown(
    @SerializedName("totalShipments") val totalShipments: Int = 0,
    @SerializedName("delivered") val delivered: StatusCountPercent? = null,
    @SerializedName("inTransit") val inTransit: StatusCountPercent? = null,
    @SerializedName("pendingPickup") val pendingPickup: StatusCountPercent? = null,
    @SerializedName("delayed") val delayed: StatusCountPercent? = null,
    @SerializedName("cancelled") val cancelled: StatusCountPercent? = null
)

data class StatusCountPercent(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("percent") val percent: String = ""
)

data class CourierPartnerItem(
    @SerializedName("courierName") val courierName: String = "",
    @SerializedName("count") val count: Int = 0,
    @SerializedName("percentage") val percentage: String = ""
)

data class RecentShipmentItem(
    @SerializedName("shipmentId") val shipmentId: String = "",
    @SerializedName("orderCode") val orderCode: String = "",
    @SerializedName("customer") val customer: String = "",
    @SerializedName("courierPartner") val courierPartner: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("pickupDate") val pickupDate: String = "",
    @SerializedName("expectedDelivery") val expectedDelivery: String = ""
)

data class CodOrderSummaryItem(
    @SerializedName("orderCode") val orderCode: String = "",
    @SerializedName("customer") val customer: String = "",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("codAmount") val codAmount: Double = 0.0,
    @SerializedName("collected") val collected: Double = 0.0,
    @SerializedName("status") val status: String = ""
)