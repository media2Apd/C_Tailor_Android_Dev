@file:Suppress("unused")

package com.cuso.tailor.model.logistics

import com.cuso.tailor.model.report.ReportPaginationDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =============================================================================
// 1. DELIVERY PERFORMANCE REPORT
// =============================================================================

@Serializable
data class DeliveryPerformanceReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<DeliveryPerformanceItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: DeliveryPerformanceSummaryDto? = null
)

@Serializable
data class DeliveryPerformanceItemDto(
    @SerialName("_id")
    val id: String = "",
    val deliveryNumber: String = "",
    val orderCode: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val garmentName: String = "",
    val statusBadge: String = "",
    val priority: String = "",
    val trialCompleted: String = "",
    val assignedTo: String = "",
    val courierPartner: String = "",
    val failureReason: String = "",
    val targetDate: String? = null,
    val createdAt: String? = null
)

@Serializable
data class DeliveryPerformanceSummaryDto(
    val totalDeliveries: Int = 0,
    val deliveredCount: Int = 0,
    val inTransitCount: Int = 0,
    val failedAttemptsCount: Int = 0,
    val trialsCompletedCount: Int = 0,
    val successRate: String = "0%"
)

// =============================================================================
// 2. SHIPMENT & TRACKING REPORT
// =============================================================================

@Serializable
data class ShipmentTrackingReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ShipmentTrackingItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: ShipmentTrackingSummaryDto? = null
)

@Serializable
data class ShipmentTrackingItemDto(
    @SerialName("_id")
    val id: String = "",
    val shipmentNumber: String = "",
    val trackingId: String = "",
    val orderCode: String = "",
    val courierPartner: String = "",
    val serviceType: String = "",
    val recipientName: String = "",
    val destination: String = "",
    val weight: String = "",
    val statusBadge: String = "",
    val currentLocation: String = "",
    val dispatchedAt: String? = null,
    val deliveredAt: String? = null,
    val createdAt: String? = null
)

@Serializable
data class ShipmentTrackingSummaryDto(
    val totalShipments: Int = 0,
    val inTransitCount: Int = 0,
    val outForDeliveryCount: Int = 0,
    val deliveredCount: Int = 0,
    val failedShipmentsCount: Int = 0,
    val totalWeightKg: Double = 0.0
)

// =============================================================================
// 3. RETURNS / RTO REPORT
// =============================================================================

@Serializable
data class ReturnsRtoReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ReturnsRtoItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: ReturnsRtoSummaryDto? = null
)

@Serializable
data class ReturnsRtoItemDto(
    @SerialName("_id")
    val id: String = "",
    val returnNumber: String = "",
    val orderCode: String = "",
    val customerName: String = "",
    val garmentName: String = "",
    val category: String = "",
    val priority: String = "",
    val reason: String = "",
    val statusBadge: String = "",
    val createdAt: String? = null
)

@Serializable
data class ReturnsRtoSummaryDto(
    val totalReturns: Int = 0,
    val alterationsCount: Int = 0,
    val damagedCount: Int = 0,
    val inProgressCount: Int = 0,
    val completedCount: Int = 0,
    val resolutionRate: String = "0%"
)

// =============================================================================
// 4. COD RECONCILIATION REPORT
// =============================================================================

@Serializable
data class CodReconciliationReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<CodReconciliationItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: CodReconciliationSummaryDto? = null
)

@Serializable
data class CodReconciliationItemDto(
    @SerialName("_id")
    val id: String = "",
    val shipmentId: String = "",
    val trackingId: String = "",
    val orderCode: String = "",
    val recipientName: String = "",
    val courierPartner: String = "",
    val destinationCity: String = "",
    val codAmount: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val netReceivable: Double = 0.0,
    val paymentStatus: String = "",
    val remittanceBadge: String = "",
    val isSettled: Boolean = false,
    val createdAt: String? = null
)

@Serializable
data class CodReconciliationSummaryDto(
    val totalCodOrders: Int = 0,
    val totalExpectedCodAmount: Double = 0.0,
    val collectedAmount: Double = 0.0,
    val pendingPayoutAmount: Double = 0.0,
    val totalDeliveryCharges: Double = 0.0
)