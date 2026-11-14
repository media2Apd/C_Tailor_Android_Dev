package com.cuso.tailor.model.report

import com.google.gson.annotations.SerializedName

// =========================================================================
// 1. LEAD REPORTS (4 ENDPOINTS)
// =========================================================================

data class LeadMasterRegisterResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<LeadMasterRegisterItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: LeadMasterRegisterSummaryDto? = null
)

data class LeadMasterRegisterItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("leadCode") val leadCode: String = "",
    @SerializedName("enquiryDate") val enquiryDate: String? = null,
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("mobileNumber") val mobileNumber: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("leadSource") val leadSource: String = "",
    @SerializedName("customerType") val customerType: String = "",
    @SerializedName("enquiryType") val enquiryType: String = "",
    @SerializedName("priorityLevel") val priorityLevel: String = "",
    @SerializedName("leadStatus") val leadStatus: String = "",
    @SerializedName("isConverted") val isConverted: Boolean = false,
    @SerializedName("convertedAt") val convertedAt: String? = null,
    @SerializedName("budgetRange") val budgetRange: String = "",
    @SerializedName("appointmentStatus") val appointmentStatus: String = "",
    @SerializedName("nextFollowUpDate") val nextFollowUpDate: String? = null,
    @SerializedName("nextFollowUpTime") val nextFollowUpTime: String? = null,
    @SerializedName("preferredContactMethod") val preferredContactMethod: String = "",
    @SerializedName("salesperson") val salesperson: String = "",
    @SerializedName("branch") val branch: String = ""
)

data class LeadMasterRegisterSummaryDto(
    @SerializedName("totalLeads") val totalLeads: Int = 0,
    @SerializedName("convertedLeads") val convertedLeads: Int = 0,
    @SerializedName("activeLeads") val activeLeads: Int = 0,
    @SerializedName("lostLeads") val lostLeads: Int = 0,
    @SerializedName("totalEstimatedBudget") val totalEstimatedBudget: Double = 0.0,
    @SerializedName("conversionRate") val conversionRate: String = "0%"
)

data class LeadSourcePerformanceResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<LeadSourcePerformanceItemDto> = emptyList(),
    @SerializedName("summary") val summary: LeadSourcePerformanceSummaryDto? = null
)

data class LeadSourcePerformanceItemDto(
    @SerializedName("sourceName") val sourceName: String = "",
    @SerializedName("totalLeads") val totalLeads: Int = 0,
    @SerializedName("convertedLeads") val convertedLeads: Int = 0,
    @SerializedName("activeLeads") val activeLeads: Int = 0,
    @SerializedName("lostLeads") val lostLeads: Int = 0,
    @SerializedName("conversionRate") val conversionRate: String = "0%",
    @SerializedName("totalBudget") val totalBudget: Double = 0.0,
    @SerializedName("avgBudget") val avgBudget: Double = 0.0
)

data class LeadSourcePerformanceSummaryDto(
    @SerializedName("totalSourcesCount") val totalSourcesCount: Int = 0,
    @SerializedName("totalAllLeads") val totalAllLeads: Int = 0,
    @SerializedName("totalAllConverted") val totalAllConverted: Int = 0,
    @SerializedName("overallConversionRate") val overallConversionRate: String = "0%"
)

data class LeadOverdueFollowupsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<LeadOverdueFollowupItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: LeadOverdueFollowupsSummaryDto? = null
)

data class LeadOverdueFollowupItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("leadCode") val leadCode: String = "",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("mobileNumber") val mobileNumber: String = "",
    @SerializedName("leadStatus") val leadStatus: String = "",
    @SerializedName("priorityLevel") val priorityLevel: String = "",
    @SerializedName("leadSource") val leadSource: String = "",
    @SerializedName("enquiryType") val enquiryType: String = "",
    @SerializedName("nextFollowUpDate") val nextFollowUpDate: String? = null,
    @SerializedName("daysOverdue") val daysOverdue: Int = 0,
    @SerializedName("salesperson") val salesperson: String = "",
    @SerializedName("branch") val branch: String = ""
)

data class LeadOverdueFollowupsSummaryDto(
    @SerializedName("totalOverdueCount") val totalOverdueCount: Int = 0,
    @SerializedName("urgentPriorityCount") val urgentPriorityCount: Int = 0
)

data class LeadFollowupActivityResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<LeadFollowupActivityItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: LeadFollowupActivitySummaryDto? = null
)

data class LeadFollowupActivityItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("leadCode") val leadCode: String = "",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("activityType") val activityType: String = "",
    @SerializedName("channel") val channel: String = "",
    @SerializedName("outcome") val outcome: String = "",
    @SerializedName("notes") val notes: String = "",
    @SerializedName("leadStatus") val leadStatus: String = "",
    @SerializedName("salesperson") val salesperson: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class LeadFollowupActivitySummaryDto(
    @SerializedName("totalActivities") val totalActivities: Int = 0,
    @SerializedName("callsCount") val callsCount: Int = 0,
    @SerializedName("whatsAppCount") val whatsAppCount: Int = 0,
    @SerializedName("appointmentsCount") val appointmentsCount: Int = 0,
    @SerializedName("emailsCount") val emailsCount: Int = 0,
    @SerializedName("qualifiedCount") val qualifiedCount: Int = 0
)

// =========================================================================
// 2. OPPORTUNITY REPORTS (3 ENDPOINTS)
// =========================================================================

data class OpportunityMasterRegisterResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<OpportunityMasterRegisterItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: OpportunityMasterRegisterSummaryDto? = null
)

data class OpportunityMasterRegisterItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("opportunityCode") val opportunityCode: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("estimatedValue") val estimatedValue: Double = 0.0,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("stageName") val stageName: String = "",
    @SerializedName("probability") val probability: String = "",
    @SerializedName("clientName") val clientName: String = "",
    @SerializedName("clientPhone") val clientPhone: String = "",
    @SerializedName("salesperson") val salesperson: String = "",
    @SerializedName("expectedClosingDate") val expectedClosingDate: String? = null,
    @SerializedName("isConverted") val isConverted: Boolean = false,
    @SerializedName("salesOrderCode") val salesOrderCode: String? = null,
    @SerializedName("status") val status: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class OpportunityMasterRegisterSummaryDto(
    @SerializedName("totalDealsCount") val totalDealsCount: Int = 0,
    @SerializedName("totalPipelineValue") val totalPipelineValue: Double = 0.0,
    @SerializedName("weightedForecastValue") val weightedForecastValue: Double = 0.0,
    @SerializedName("wonDealsCount") val wonDealsCount: Int = 0,
    @SerializedName("wonDealsValue") val wonDealsValue: Double = 0.0,
    @SerializedName("activeDealsCount") val activeDealsCount: Int = 0,
    @SerializedName("closingIn7DaysCount") val closingIn7DaysCount: Int = 0,
    @SerializedName("winRate") val winRate: String = "0%"
)

data class OpportunityStageFunnelResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<OpportunityStageFunnelItemDto> = emptyList(),
    @SerializedName("summary") val summary: OpportunityStageFunnelSummaryDto? = null
)

data class OpportunityStageFunnelItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("dealsCount") val dealsCount: Int = 0,
    @SerializedName("convertedCount") val convertedCount: Int = 0,
    @SerializedName("stageName") val stageName: String = "",
    @SerializedName("stageCode") val stageCode: String = "",
    @SerializedName("probability") val probability: Int = 0,
    @SerializedName("displayOrder") val displayOrder: Int = 0,
    @SerializedName("totalStageValue") val totalStageValue: Double = 0.0,
    @SerializedName("weightedValue") val weightedValue: Double = 0.0,
    @SerializedName("percentOfPipeline") val percentOfPipeline: String = "0%"
)

data class OpportunityStageFunnelSummaryDto(
    @SerializedName("totalStagesCount") val totalStagesCount: Int = 0,
    @SerializedName("totalDeals") val totalDeals: Int = 0,
    @SerializedName("totalPipelineValue") val totalPipelineValue: Double = 0.0,
    @SerializedName("totalWeightedValue") val totalWeightedValue: Double = 0.0
)

data class OpportunityClosingAlertsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<OpportunityClosingAlertItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: OpportunityClosingAlertsSummaryDto? = null
)

data class OpportunityClosingAlertItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("opportunityCode") val opportunityCode: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("estimatedValue") val estimatedValue: Double = 0.0,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("stageName") val stageName: String = "",
    @SerializedName("probability") val probability: String = "",
    @SerializedName("clientName") val clientName: String = "",
    @SerializedName("salesperson") val salesperson: String = "",
    @SerializedName("expectedClosingDate") val expectedClosingDate: String? = null,
    @SerializedName("urgencyStatus") val urgencyStatus: String = "",
    @SerializedName("isOverdue") val isOverdue: Boolean = false
)

data class OpportunityClosingAlertsSummaryDto(
    @SerializedName("totalAlertDeals") val totalAlertDeals: Int = 0,
    @SerializedName("atRiskValue") val atRiskValue: Double = 0.0,
    @SerializedName("overdueDealsCount") val overdueDealsCount: Int = 0,
    @SerializedName("closingSoonCount") val closingSoonCount: Int = 0
)

// =========================================================================
// 3. CUSTOMER REPORTS (4 ENDPOINTS)
// =========================================================================

data class CustomerMasterRegisterResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<CustomerMasterRegisterItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: CustomerMasterRegisterSummaryDto? = null
)

data class CustomerMasterRegisterItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("customerCode") val customerCode: String = "",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("mobileNumber") val mobileNumber: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("customerType") val customerType: String = "",
    @SerializedName("customerLevel") val customerLevel: String = "",
    @SerializedName("creditLimit") val creditLimit: Double = 0.0,
    @SerializedName("creditPeriodDays") val creditPeriodDays: Int = 0,
    @SerializedName("preferredContactMethod") val preferredContactMethod: String = "",
    @SerializedName("location") val location: String = "",
    @SerializedName("branch") val branch: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class CustomerMasterRegisterSummaryDto(
    @SerializedName("totalCustomers") val totalCustomers: Int = 0,
    @SerializedName("activeCustomers") val activeCustomers: Int = 0,
    @SerializedName("corporateCount") val corporateCount: Int = 0,
    @SerializedName("vipEliteCount") val vipEliteCount: Int = 0,
    @SerializedName("totalCreditLimitGiven") val totalCreditLimitGiven: Double = 0.0
)

data class CustomerLtvRevenueResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<CustomerLtvRevenueItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: CustomerLtvRevenueSummaryDto? = null
)

data class CustomerLtvRevenueItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("customerCode") val customerCode: String = "",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("mobileNumber") val mobileNumber: String = "",
    @SerializedName("customerLevel") val customerLevel: String = "",
    @SerializedName("customerType") val customerType: String = "",
    @SerializedName("totalOrdersCount") val totalOrdersCount: Int = 0,
    @SerializedName("lifetimeSpend") val lifetimeSpend: Double = 0.0,
    @SerializedName("averageOrderValue") val averageOrderValue: Double = 0.0,
    @SerializedName("outstandingBalance") val outstandingBalance: Double = 0.0,
    @SerializedName("lastOrderDate") val lastOrderDate: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class CustomerLtvRevenueSummaryDto(
    @SerializedName("totalClientsTracked") val totalClientsTracked: Int = 0,
    @SerializedName("totalLtvRevenue") val totalLtvRevenue: Double = 0.0,
    @SerializedName("totalOutstandingDue") val totalOutstandingDue: Double = 0.0,
    @SerializedName("avgLtvPerClient") val avgLtvPerClient: Double = 0.0
)

data class CustomerCreditReceivablesResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<CustomerCreditReceivableItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: CustomerCreditReceivablesSummaryDto? = null
)

data class CustomerCreditReceivableItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("customerCode") val customerCode: String = "",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("mobileNumber") val mobileNumber: String = "",
    @SerializedName("customerType") val customerType: String = "",
    @SerializedName("customerLevel") val customerLevel: String = "",
    @SerializedName("creditLimit") val creditLimit: Double = 0.0,
    @SerializedName("creditPeriodDays") val creditPeriodDays: Int = 0,
    @SerializedName("unpaidOrdersCount") val unpaidOrdersCount: Int = 0,
    @SerializedName("outstandingBalance") val outstandingBalance: Double = 0.0,
    @SerializedName("utilizationPercentage") val utilizationPercentage: String = "0%",
    @SerializedName("oldestDueDate") val oldestDueDate: String? = null
)

data class CustomerCreditReceivablesSummaryDto(
    @SerializedName("accountsWithDuesCount") val accountsWithDuesCount: Int = 0,
    @SerializedName("totalReceivablesDue") val totalReceivablesDue: Double = 0.0,
    @SerializedName("totalCreditLimitAllocated") val totalCreditLimitAllocated: Double = 0.0
)

data class CustomerDormantWinbackResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<CustomerDormantWinbackItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: CustomerDormantWinbackSummaryDto? = null
)

data class CustomerDormantWinbackItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("customerCode") val customerCode: String = "",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("mobileNumber") val mobileNumber: String = "",
    @SerializedName("customerLevel") val customerLevel: String = "",
    @SerializedName("lastOrderDate") val lastOrderDate: String? = null,
    @SerializedName("daysInactive") val daysInactive: Int = 0,
    @SerializedName("lifetimeSpend") val lifetimeSpend: Double = 0.0,
    @SerializedName("totalOrdersCount") val totalOrdersCount: Int = 0
)

data class CustomerDormantWinbackSummaryDto(
    @SerializedName("dormantClientsCount") val dormantClientsCount: Int = 0,
    @SerializedName("dormantPastRevenue") val dormantPastRevenue: Double = 0.0,
    @SerializedName("inactivityThresholdDays") val inactivityThresholdDays: Int = 90
)

// =========================================================================
// 4. MEASUREMENT REPORTS (2 ENDPOINTS)
// =========================================================================

data class MeasurementMasterRegisterResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<MeasurementMasterRegisterItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: MeasurementMasterRegisterSummaryDto? = null
)

data class MeasurementMasterRegisterItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("customerCode") val customerCode: String = "",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("mobileNumber") val mobileNumber: String = "",
    @SerializedName("segmentName") val segmentName: String = "",
    @SerializedName("garmentName") val garmentName: String = "",
    @SerializedName("categoryName") val categoryName: String = "",
    @SerializedName("recordedFieldsCount") val recordedFieldsCount: String = "",
    @SerializedName("measuredAt") val measuredAt: String? = null,
    @SerializedName("recordedBy") val recordedBy: String = "",
    @SerializedName("status") val status: String = ""
)

data class MeasurementMasterRegisterSummaryDto(
    @SerializedName("totalCardsCount") val totalCardsCount: Int = 0,
    @SerializedName("activeCardsCount") val activeCardsCount: Int = 0,
    @SerializedName("uniqueClientsCount") val uniqueClientsCount: Int = 0
)

data class MeasurementStaleAuditResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<MeasurementStaleAuditItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: MeasurementStaleAuditSummaryDto? = null
)

data class MeasurementStaleAuditItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("customerCode") val customerCode: String = "",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("mobileNumber") val mobileNumber: String = "",
    @SerializedName("garmentName") val garmentName: String = "",
    @SerializedName("categoryName") val categoryName: String = "",
    @SerializedName("measuredAt") val measuredAt: String? = null,
    @SerializedName("monthsSinceMeasured") val monthsSinceMeasured: Int = 0,
    @SerializedName("status") val status: String = ""
)

data class MeasurementStaleAuditSummaryDto(
    @SerializedName("staleCardsCount") val staleCardsCount: Int = 0,
    @SerializedName("uniqueClientsCount") val uniqueClientsCount: Int = 0,
    @SerializedName("thresholdMonths") val thresholdMonths: Int = 6
)

// =========================================================================
// 5. SALES ORDER & BRANCH REPORTS (4 ENDPOINTS)
// =========================================================================

data class OrderMasterRegisterResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<OrderMasterRegisterItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: OrderMasterRegisterSummaryDto? = null
)

data class OrderMasterRegisterItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("orderCode") val orderCode: String = "",
    @SerializedName("orderDate") val orderDate: String? = null,
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("clientName") val clientName: String = "",
    @SerializedName("clientPhone") val clientPhone: String = "",
    @SerializedName("itemPreview") val itemPreview: String = "",
    @SerializedName("orderType") val orderType: String = "",
    @SerializedName("priority") val priority: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("paymentStatus") val paymentStatus: String = "",
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("advancePaid") val advancePaid: Double = 0.0,
    @SerializedName("balanceDue") val balanceDue: Double = 0.0,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("salesperson") val salesperson: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class OrderMasterRegisterSummaryDto(
    @SerializedName("totalOrdersCount") val totalOrdersCount: Int = 0,
    @SerializedName("grossSalesRevenue") val grossSalesRevenue: Double = 0.0,
    @SerializedName("totalAdvanceCollected") val totalAdvanceCollected: Double = 0.0,
    @SerializedName("totalBalanceOutstanding") val totalBalanceOutstanding: Double = 0.0,
    @SerializedName("deliveredOrdersCount") val deliveredOrdersCount: Int = 0
)

data class OrderProductStreamResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<OrderProductStreamItemDto> = emptyList(),
    @SerializedName("summary") val summary: OrderProductStreamSummaryDto? = null
)

data class OrderProductStreamItemDto(
    @SerializedName("lineType") val lineType: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("totalRevenue") val totalRevenue: Double = 0.0,
    @SerializedName("itemsCount") val itemsCount: Int = 0,
    @SerializedName("ordersCount") val ordersCount: Int = 0,
    @SerializedName("revenueShare") val revenueShare: String = "0%"
)

data class OrderProductStreamSummaryDto(
    @SerializedName("totalRevenue") val totalRevenue: Double = 0.0,
    @SerializedName("totalOrders") val totalOrders: Int = 0,
    @SerializedName("streamsCount") val streamsCount: Int = 0
)

data class OrderDeliverySlaResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<OrderDeliverySlaItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: OrderDeliverySlaSummaryDto? = null
)

data class OrderDeliverySlaItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("orderCode") val orderCode: String = "",
    @SerializedName("clientName") val clientName: String = "",
    @SerializedName("dueDate") val dueDate: String? = null,
    @SerializedName("deliveredDate") val deliveredDate: String? = null,
    @SerializedName("slaStatus") val slaStatus: String = "",
    @SerializedName("delayDays") val delayDays: Int = 0,
    @SerializedName("status") val status: String = ""
)

data class OrderDeliverySlaSummaryDto(
    @SerializedName("totalDeliveredCount") val totalDeliveredCount: Int = 0,
    @SerializedName("onTimeCount") val onTimeCount: Int = 0,
    @SerializedName("delayedCount") val delayedCount: Int = 0,
    @SerializedName("otdRate") val otdRate: String = "100%"
)

data class BranchWiseSalesResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<BranchWiseSalesItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: BranchWiseSalesSummaryDto? = null
)

data class BranchWiseSalesItemDto(
    @SerializedName("branchId") val branchId: String = "",
    @SerializedName("branchName") val branchName: String = "",
    @SerializedName("branchCode") val branchCode: String = "",
    @SerializedName("totalOrders") val totalOrders: Int = 0,
    @SerializedName("totalRevenue") val totalRevenue: Double = 0.0,
    @SerializedName("averageOrderValue") val averageOrderValue: Double = 0.0,
    @SerializedName("paidRevenue") val paidRevenue: Double = 0.0,
    @SerializedName("pendingRevenue") val pendingRevenue: Double = 0.0,
    @SerializedName("revenueShare") val revenueShare: String = "0%",
    @SerializedName("currency") val currency: String = "INR"
)

data class BranchWiseSalesSummaryDto(
    @SerializedName("overallRevenue") val overallRevenue: Double = 0.0,
    @SerializedName("overallOrders") val overallOrders: Int = 0,
    @SerializedName("overallPaidRevenue") val overallPaidRevenue: Double = 0.0,
    @SerializedName("overallPendingRevenue") val overallPendingRevenue: Double = 0.0,
    @SerializedName("currency") val currency: String = "INR"
)

// =========================================================================
// 6. QUOTATION REPORTS (3 ENDPOINTS)
// =========================================================================

data class QuotationMasterRegisterResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<QuotationMasterRegisterItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: QuotationMasterRegisterSummaryDto? = null
)

data class QuotationMasterRegisterItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("quotationCode") val quotationCode: String = "",
    @SerializedName("quotationDate") val quotationDate: String? = null,
    @SerializedName("expiryDate") val expiryDate: String? = null,
    @SerializedName("clientName") val clientName: String = "",
    @SerializedName("clientPhone") val clientPhone: String = "",
    @SerializedName("itemPreview") val itemPreview: String = "",
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("totalDiscount") val totalDiscount: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("status") val status: String = "",
    @SerializedName("approvalStatus") val approvalStatus: String = "",
    @SerializedName("convertedOrderCode") val convertedOrderCode: String? = null,
    @SerializedName("salesperson") val salesperson: String = "",
    @SerializedName("createdAt") val createdAt: String? = null
)

data class QuotationMasterRegisterSummaryDto(
    @SerializedName("totalQuotesCount") val totalQuotesCount: Int = 0,
    @SerializedName("totalGrossValue") val totalGrossValue: Double = 0.0,
    @SerializedName("totalDiscountGiven") val totalDiscountGiven: Double = 0.0,
    @SerializedName("convertedToOrderCount") val convertedToOrderCount: Int = 0,
    @SerializedName("convertedValue") val convertedValue: Double = 0.0,
    @SerializedName("activeNegotiationCount") val activeNegotiationCount: Int = 0,
    @SerializedName("activeNegotiationValue") val activeNegotiationValue: Double = 0.0,
    @SerializedName("conversionRate") val conversionRate: String = "0%"
)

data class QuotationDiscountLeakageResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<QuotationDiscountLeakageItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: QuotationDiscountLeakageSummaryDto? = null
)

data class QuotationDiscountLeakageItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("quotationCode") val quotationCode: String = "",
    @SerializedName("clientName") val clientName: String = "",
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("totalDiscount") val totalDiscount: Double = 0.0,
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("discountPercentage") val discountPercentage: String = "0%",
    @SerializedName("approvalStatus") val approvalStatus: String = "",
    @SerializedName("salesperson") val salesperson: String = "",
    @SerializedName("quotationDate") val quotationDate: String? = null
)

data class QuotationDiscountLeakageSummaryDto(
    @SerializedName("discountedQuotesCount") val discountedQuotesCount: Int = 0,
    @SerializedName("totalDiscountAmount") val totalDiscountAmount: Double = 0.0,
    @SerializedName("totalGrossBeforeDiscount") val totalGrossBeforeDiscount: Double = 0.0,
    @SerializedName("managerApprovedDiscounts") val managerApprovedDiscounts: Int = 0,
    @SerializedName("avgDiscountPercentage") val avgDiscountPercentage: String = "0%"
)

data class QuotationExpiringAuditResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: List<QuotationExpiringAuditItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: ReportPaginationDto? = null,
    @SerializedName("summary") val summary: QuotationExpiringAuditSummaryDto? = null
)

data class QuotationExpiringAuditItemDto(
    @SerializedName("_id") val id: String = "",
    @SerializedName("quotationCode") val quotationCode: String = "",
    @SerializedName("clientName") val clientName: String = "",
    @SerializedName("clientPhone") val clientPhone: String = "",
    @SerializedName("grandTotal") val grandTotal: Double = 0.0,
    @SerializedName("expiryDate") val expiryDate: String? = null,
    @SerializedName("daysRemaining") val daysRemaining: Int = 0,
    @SerializedName("status") val status: String = "",
    @SerializedName("salesperson") val salesperson: String = ""
)

data class QuotationExpiringAuditSummaryDto(
    @SerializedName("expiringCount") val expiringCount: Int = 0,
    @SerializedName("atRiskValue") val atRiskValue: Double = 0.0
)