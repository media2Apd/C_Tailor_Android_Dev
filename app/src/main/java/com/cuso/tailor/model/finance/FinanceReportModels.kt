@file:Suppress("unused")

package com.cuso.tailor.model.finance

import com.cuso.tailor.model.report.ReportPaginationDto
import kotlinx.serialization.Serializable

// =============================================================================
// SHARED FINANCE SUB-MODELS
// =============================================================================

@Serializable
data class FinanceCustomerSummaryDto(
    val customerId: String = "",
    val customerCode: String = "",
    val customerName: String = "",
    val mobileNumber: String = ""
)

@Serializable
data class FinanceSupplierSummaryDto(
    val supplierId: String = "",
    val supplierCode: String = "",
    val supplierName: String = "",
    val mobileNumber: String = ""
)

@Serializable
data class FinanceLinkedDocumentDto(
    val invoiceId: String? = null,
    val invoiceNumber: String? = null,
    val salesOrderId: String? = null,
    val orderCode: String? = null
)

@Serializable
data class FinanceLinkedOrderDto(
    val salesOrderId: String? = null,
    val orderCode: String? = null
)

@Serializable
data class FinanceAgingBucketsDto(
    val days1To30: Double = 0.0,
    val days31To60: Double = 0.0,
    val days61To90: Double = 0.0,
    val daysOver90: Double = 0.0
)

// =============================================================================
// 1. REVENUE & EXPENSE SUMMARY REPORT (FINANCIAL OVERVIEW & P&L)
// =============================================================================

@Serializable
data class RevenueExpenseSummaryResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<RevenueExpenseItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: RevenueExpenseSummaryDto? = null
)

@Serializable
data class RevenueExpenseItemDto(
    val accountId: String = "",
    val accountCode: String = "",
    val accountName: String = "",
    val accountType: String = "",
    val category: String = "",
    val netAmount: Double = 0.0,
    val currency: String = "INR"
)

@Serializable
data class RevenueExpenseSummaryDto(
    val totalRevenue: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMargin: String = "0%",
    val currency: String = "INR"
)

// =============================================================================
// 2. RECEIVABLES AGING REPORT (AR AGING)
// =============================================================================

@Serializable
data class ReceivablesAgingResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<ReceivablesAgingItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: ReceivablesAgingSummaryDto? = null
)

@Serializable
data class ReceivablesAgingItemDto(
    val customerId: String = "",
    val customerCode: String = "",
    val customerName: String = "",
    val mobileNumber: String = "",
    val status: String = "",
    val agingStatus: String = "",
    val totalReceivable: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val current: Double = 0.0,
    val days1To30: Double = 0.0,
    val days31To60: Double = 0.0,
    val days61To90: Double = 0.0,
    val daysOver90: Double = 0.0,
    val buckets: FinanceAgingBucketsDto? = null,
    val currency: String = "INR"
)

@Serializable
data class ReceivablesAgingSummaryDto(
    val totalReceivables: Double = 0.0,
    val totalCurrent: Double = 0.0,
    val totalOverdue: Double = 0.0,
    val buckets: FinanceAgingBucketsDto = FinanceAgingBucketsDto(),
    val currency: String = "INR",
    val asOfDate: String? = null
)

// =============================================================================
// 3. PAYABLES AGING REPORT (AP AGING)
// =============================================================================

@Serializable
data class PayablesAgingResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<PayablesAgingItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: PayablesAgingSummaryDto? = null
)

@Serializable
data class PayablesAgingItemDto(
    val supplierId: String = "",
    val supplierCode: String = "",
    val supplierName: String = "",
    val mobileNumber: String = "",
    val status: String = "",
    val agingStatus: String = "",
    val totalPayable: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val current: Double = 0.0,
    val days1To30: Double = 0.0,
    val days31To60: Double = 0.0,
    val days61To90: Double = 0.0,
    val daysOver90: Double = 0.0,
    val buckets: FinanceAgingBucketsDto? = null,
    val currency: String = "INR"
)

@Serializable
data class PayablesAgingSummaryDto(
    val totalPayables: Double = 0.0,
    val totalCurrent: Double = 0.0,
    val totalOverdue: Double = 0.0,
    val buckets: FinanceAgingBucketsDto = FinanceAgingBucketsDto(),
    val currency: String = "INR",
    val asOfDate: String? = null
)

// =============================================================================
// 4. GST / TAX SUMMARY REPORT
// =============================================================================

@Serializable
data class GstSummaryResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: GstSummaryDataDto? = null
)

@Serializable
data class GstSummaryDataDto(
    val period: GstPeriodDto = GstPeriodDto(),
    val currency: String = "INR",
    val summary: GstOverviewSummaryDto = GstOverviewSummaryDto(),
    val outwardSupplies: GstOutwardSuppliesDto = GstOutwardSuppliesDto(),
    val inwardSupplies: GstInwardSuppliesDto = GstInwardSuppliesDto(),
    val rateBreakdown: List<GstRateBreakdownItemDto> = emptyList()
)

@Serializable
data class GstPeriodDto(
    val from: String = "",
    val to: String = ""
)

@Serializable
data class GstOverviewSummaryDto(
    val totalOutputTax: Double = 0.0,
    val totalInputTaxCredit: Double = 0.0,
    val netTaxPayable: Double = 0.0,
    val itcCarryForward: Double = 0.0
)

@Serializable
data class GstOutwardSuppliesDto(
    val grossSalesTax: Double = 0.0,
    val creditNotesAdjustment: Double = 0.0,
    val netOutputTax: Double = 0.0
)

@Serializable
data class GstInwardSuppliesDto(
    val purchaseBillsTax: Double = 0.0,
    val expensesTax: Double = 0.0,
    val vendorCreditsAdjustment: Double = 0.0,
    val netEligibleITC: Double = 0.0
)

@Serializable
data class GstRateBreakdownItemDto(
    val taxName: String = "",
    val taxType: String = "",
    val rate: Double = 0.0,
    val outwardTaxable: Double = 0.0,
    val outputTax: Double = 0.0,
    val inwardTaxable: Double = 0.0,
    val inputTaxCredit: Double = 0.0,
    val netTaxPayable: Double = 0.0,
    val currency: String = "INR"
)

// =============================================================================
// 5. PAYMENT COLLECTION REPORT
// =============================================================================

@Serializable
data class PaymentCollectionReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<PaymentCollectionItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: PaymentCollectionSummaryDto? = null
)

@Serializable
data class PaymentCollectionItemDto(
    val paymentId: String = "",
    val paymentNumber: String = "",
    val paymentDate: String? = null,
    val customer: FinanceCustomerSummaryDto = FinanceCustomerSummaryDto(),
    val paymentMode: String = "",
    val referenceNumber: String = "",
    val amount: Double = 0.0,
    val currency: String = "INR",
    val isAdvancePayment: Boolean = false,
    val unallocatedAmount: Double = 0.0,
    val linkedDocument: FinanceLinkedDocumentDto? = null,
    val depositedTo: String = "",
    val status: String = ""
)

@Serializable
data class PaymentCollectionSummaryDto(
    val totalCollected: Double = 0.0,
    val totalTransactions: Int = 0,
    val advanceCollected: Double = 0.0,
    val invoicePaymentsCollected: Double = 0.0,
    val currency: String = "INR",
    val byPaymentMode: List<FinancePaymentModeStatDto> = emptyList()
)

@Serializable
data class FinancePaymentModeStatDto(
    val mode: String = "",
    val totalAmount: Double = 0.0,
    val count: Int = 0
)

// =============================================================================
// 6. ADVANCE PAYMENT REPORT
// =============================================================================

@Serializable
data class AdvancePaymentReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<AdvancePaymentItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: AdvancePaymentSummaryDto? = null
)

@Serializable
data class AdvancePaymentItemDto(
    val paymentId: String = "",
    val paymentNumber: String = "",
    val paymentDate: String? = null,
    val customer: FinanceCustomerSummaryDto = FinanceCustomerSummaryDto(),
    val amount: Double = 0.0,
    val unallocatedAmount: Double = 0.0,
    val currency: String = "INR",
    val paymentMode: String = "",
    val referenceNumber: String = "",
    val linkedOrder: FinanceLinkedOrderDto? = null,
    val status: String = ""
)

@Serializable
data class AdvancePaymentSummaryDto(
    val totalAdvanceCollected: Double = 0.0,
    val totalUnallocatedBalance: Double = 0.0,
    val totalTransactions: Int = 0,
    val currency: String = "INR"
)

// =============================================================================
// 7. PAYMENT MODE BREAKDOWN REPORT
// =============================================================================

@Serializable
data class PaymentModeReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<PaymentModeItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: PaymentModeSummaryDto? = null
)

@Serializable
data class PaymentModeItemDto(
    val paymentId: String = "",
    val paymentNumber: String = "",
    val paymentDate: String? = null,
    val paymentMode: String = "",
    val amount: Double = 0.0,
    val referenceNumber: String = "",
    val customer: FinanceCustomerSummaryDto = FinanceCustomerSummaryDto(),
    val depositedTo: String = ""
)

@Serializable
data class PaymentModeSummaryDto(
    val totalIncomingCollections: Double = 0.0,
    val totalIncomingTransactions: Int = 0,
    val totalOutgoingPayments: Double = 0.0,
    val totalOutgoingTransactions: Int = 0,
    val netCashFlowFromPayments: Double = 0.0
)

// =============================================================================
// 8. BALANCE PENDING REPORT
// =============================================================================

@Serializable
data class BalancePendingReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<BalancePendingItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: BalancePendingSummaryDto? = null
)

@Serializable
data class BalancePendingItemDto(
    val invoiceId: String = "",
    val invoiceNumber: String = "",
    val invoiceDate: String? = null,
    val dueDate: String? = null,
    val daysOverdue: Int = 0,
    val customer: FinanceCustomerSummaryDto = FinanceCustomerSummaryDto(),
    val grandTotal: Double = 0.0,
    val balanceDue: Double = 0.0,
    val status: String = "",
    val currency: String = "INR"
)

@Serializable
data class BalancePendingSummaryDto(
    val totalPendingAmount: Double = 0.0,
    val totalUnpaidInvoices: Int = 0,
    val currency: String = "INR"
)

// =============================================================================
// 9. OVERDUE PAYMENT REPORT
// =============================================================================

@Serializable
data class OverduePaymentReportResponse(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<OverduePaymentItemDto> = emptyList(),
    val pagination: ReportPaginationDto? = null,
    val summary: OverduePaymentSummaryDto? = null
)

@Serializable
data class OverduePaymentItemDto(
    val id: String = "",
    val documentId: String = "",
    val documentNumber: String = "",
    val partyType: String = "",
    val partyName: String = "",
    val partyCode: String = "",
    val mobileNumber: String = "",
    val customer: FinanceCustomerSummaryDto? = null,
    val supplier: FinanceSupplierSummaryDto? = null,
    val dueDate: String? = null,
    val daysOverdue: Int = 0,
    val grandTotal: Double = 0.0,
    val balanceDue: Double = 0.0,
    val status: String = "",
    val currency: String = "INR"
)

@Serializable
data class OverduePaymentSummaryDto(
    val totalCustomerOverdueAmount: Double = 0.0,
    val totalCustomerOverdueInvoices: Int = 0,
    val totalSupplierOverdueAmount: Double = 0.0,
    val totalSupplierOverdueBills: Int = 0,
    val netOverdueBalance: Double = 0.0
)