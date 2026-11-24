
@file:Suppress("unused")

package com.cuso.tailor.model.service

import com.cuso.tailor.model.report.ReportPaginationDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =============================================================================
// 1. CUSTOMER FEEDBACK & NPS REPORT
// =============================================================================

@Serializable
data class CustomerFeedbackSummaryResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<CustomerFeedbackReportItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: CustomerFeedbackSummaryDto? = null
)

// NOTE: item fields are inferred because the sample response had an empty data list
@Serializable
data class CustomerFeedbackReportItemDto(
    @SerialName("_id")
    val id: String = "",
    val reviewCode: String = "",
    val orderCode: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val overallRating: Double = 0.0,
    val fittingRating: Double = 0.0,
    val stitchingRating: Double = 0.0,
    val deliveryRating: Double = 0.0,
    val staffRating: Double = 0.0,
    val npsScore: Int = 0,
    val npsCategory: String = "",
    val comment: String = "",
    val status: String = "",
    val createdAt: String? = null
)

@Serializable
data class CustomerFeedbackSummaryDto(
    val totalReviews: Int = 0,
    val averageOverallRating: Double = 0.0,
    val averageFittingRating: Double = 0.0,
    val averageStitchingRating: Double = 0.0,
    val averageDeliveryRating: Double = 0.0,
    val averageStaffRating: Double = 0.0,
    val npsSummary: NpsSummaryDto = NpsSummaryDto(),
    val grievances: FeedbackGrievanceDto = FeedbackGrievanceDto()
)

@Serializable
data class NpsSummaryDto(
    val netPromoterScore: Double = 0.0,
    val promoters: Int = 0,
    val passives: Int = 0,
    val detractors: Int = 0,
    val totalNpsRated: Int = 0
)

@Serializable
data class FeedbackGrievanceDto(
    val totalNegativeReviews: Int = 0,
    val pendingResolution: Int = 0
)

// =============================================================================
// 2. COMPLAINTS & ESCALATION REPORT
// =============================================================================

@Serializable
data class ComplaintsEscalationResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ComplaintEscalationItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: ComplaintsEscalationSummaryDto? = null
)

// NOTE: item fields are inferred because the sample response had an empty data list
@Serializable
data class ComplaintEscalationItemDto(
    @SerialName("_id")
    val id: String = "",
    val complaintCode: String = "",
    val orderCode: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val category: String = "",
    val severity: String = "",
    val status: String = "",
    val assignedTo: String = "",
    val isSlaBreached: Boolean = false,
    val description: String = "",
    val createdAt: String? = null,
    val resolvedAt: String? = null
)

@Serializable
data class ComplaintsEscalationSummaryDto(
    val totalComplaints: Int = 0,
    val openComplaints: Int = 0,
    val resolvedComplaints: Int = 0,
    val resolutionRate: String = "0%",
    val slaBreachedCount: Int = 0,
    val severityBreakdown: ComplaintSeverityBreakdownDto = ComplaintSeverityBreakdownDto()
)

@Serializable
data class ComplaintSeverityBreakdownDto(
    val critical: Int = 0,
    val high: Int = 0
)

// =============================================================================
// 3. SERVICE PERFORMANCE & SLA REPORT
// =============================================================================

@Serializable
data class ServicePerformanceSlaResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ServicePerformanceSlaItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: ServicePerformanceSlaSummaryDto? = null
)

@Serializable
data class ServicePerformanceSlaItemDto(
    val orderId: String = "",
    val orderCode: String = "",
    val orderType: String = "",
    val priority: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val orderDate: String? = null,
    val dueDate: String? = null,
    val deliveredAt: String? = null,
    val status: String = "",
    val slaStatus: String = ""
)

@Serializable
data class ServicePerformanceSlaSummaryDto(
    val totalOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val onTimeDeliveries: Int = 0,
    val delayedDeliveries: Int = 0,
    val onTimeDeliveryRate: String = "0%",
    val activeSlaBreaches: Int = 0
)