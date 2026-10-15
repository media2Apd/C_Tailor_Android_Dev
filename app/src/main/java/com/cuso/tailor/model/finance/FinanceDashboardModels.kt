package com.cuso.tailor.model.finance

import com.google.gson.annotations.SerializedName

// 1. KPI Stats Models
data class FinanceKpiResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: FinanceKpiData? = null
)

data class FinanceKpiData(
    @SerializedName("totalReceivables") val totalReceivables: KpiReceivables? = null,
    @SerializedName("totalPayables") val totalPayables: KpiPayables? = null,
    @SerializedName("totalFunds") val totalFunds: KpiFunds? = null,
    @SerializedName("periodCollected") val periodCollected: KpiCollected? = null,
    @SerializedName("periodExpenses") val periodExpenses: KpiExpenses? = null,
    @SerializedName("pendingApprovals") val pendingApprovals: KpiApprovals? = null
)

data class KpiReceivables(
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("overdueAmount") val overdueAmount: Double = 0.0,
    @SerializedName("overduePercentage") val overduePercentage: String? = null,
    @SerializedName("salesGrowth") val salesGrowth: String? = null,
    @SerializedName("trend") val trend: String? = null,
    @SerializedName("invoiceCount") val invoiceCount: Int = 0
)

data class KpiPayables(
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("overdueAmount") val overdueAmount: Double = 0.0,
    @SerializedName("overduePercentage") val overduePercentage: String? = null,
    @SerializedName("billingGrowth") val billingGrowth: String? = null,
    @SerializedName("trend") val trend: String? = null,
    @SerializedName("billCount") val billCount: Int = 0
)

data class KpiFunds(
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("periodNetCashFlow") val periodNetCashFlow: Double = 0.0,
    @SerializedName("growth") val growth: String? = null,
    @SerializedName("trend") val trend: String? = null,
    @SerializedName("accountsCount") val accountsCount: Int = 0
)

data class KpiCollected(
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("growth") val growth: String? = null,
    @SerializedName("trend") val trend: String? = null,
    @SerializedName("sparkline") val sparkline: List<Double> = emptyList()
)

data class KpiExpenses(
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("growth") val growth: String? = null,
    @SerializedName("trend") val trend: String? = null
)

data class KpiApprovals(
    @SerializedName("total") val total: Int = 0,
    @SerializedName("breakdown") val breakdown: Map<String, Int> = emptyMap()
)

// 2. Cash Flow Analytics Models
data class CashFlowResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: CashFlowData? = null
)

data class CashFlowData(
    @SerializedName("summary") val summary: CashFlowSummary? = null,
    @SerializedName("chartData") val chartData: List<CashFlowPoint> = emptyList()
)

data class CashFlowSummary(
    @SerializedName("totalInflow") val totalInflow: Double = 0.0,
    @SerializedName("totalOutflow") val totalOutflow: Double = 0.0,
    @SerializedName("netCashChange") val netCashChange: Double = 0.0,
    @SerializedName("status") val status: String? = null
)

data class CashFlowPoint(
    @SerializedName("periodKey") val periodKey: String = "",
    @SerializedName("periodLabel") val periodLabel: String = "",
    @SerializedName("inflow") val inflow: Double = 0.0,
    @SerializedName("outflow") val outflow: Double = 0.0,
    @SerializedName("netCashFlow") val netCashFlow: Double = 0.0
)

// 3. Finance Distribution Models
data class FinanceDistributionResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: FinanceDistributionData? = null
)

data class FinanceDistributionData(
    @SerializedName("totalPool") val totalPool: Double = 0.0,
    @SerializedName("distribution") val distribution: List<DistributionSlice> = emptyList()
)

data class DistributionSlice(
    @SerializedName("key") val key: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("actualFundsBalance") val actualFundsBalance: Double? = null,
    @SerializedName("percentage") val percentage: Float = 0f,
    @SerializedName("color") val color: String = "#1E1B4B"
)

// 4. Operations Overview Models (Sales, Purchase, Ledger, Expense, Funds)
data class OperationsOverviewResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: OperationsOverviewData? = null
)

data class OperationsOverviewData(
    @SerializedName("metrics") val metrics: List<OverviewMetricItem> = emptyList(),
    @SerializedName("summary") val summary: Map<String, Any> = emptyMap(),
    @SerializedName("navigationLink") val navigationLink: String? = null
)

data class OverviewMetricItem(
    @SerializedName("key") val key: String = "",
    @SerializedName("label") val label: String = "",
    @SerializedName("count") val count: Int? = null,
    @SerializedName("value") val value: Double? = null,
    @SerializedName("type") val type: String? = null
)

// 5. Recent Financial Transactions
data class RecentTransactionsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: RecentTransactionsData? = null
)

data class RecentTransactionsData(
    @SerializedName("transactions") val transactions: List<RecentTransactionDto> = emptyList()
)

data class RecentTransactionDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("date") val date: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("typeKey") val typeKey: String = "",
    @SerializedName("reference") val reference: String = "",
    @SerializedName("party") val party: String = "",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("status") val status: String = "",
    @SerializedName("rawStatus") val rawStatus: String = "",
    @SerializedName("badgeColor") val badgeColor: String = ""
)

// 6. Alerts & Pending Actions
data class DashboardAlertsResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: DashboardAlertsData? = null
)

data class DashboardAlertsData(
    @SerializedName("alerts") val alerts: List<DashboardAlertDto> = emptyList()
)

data class DashboardAlertDto(
    @SerializedName("key") val key: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("count") val count: Int = 0,
    @SerializedName("severity") val severity: String = "",
    @SerializedName("badgeColor") val badgeColor: String = "",
    @SerializedName("navigationLink") val navigationLink: String? = null
)

// 7. Bank Snapshot Models
data class BankSnapshotResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: BankSnapshotData? = null
)

data class BankSnapshotData(
    @SerializedName("totalBalance") val totalBalance: Double = 0.0,
    @SerializedName("accounts") val accounts: List<BankAccountSnapshotDto> = emptyList()
)

data class BankAccountSnapshotDto(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("balance") val balance: Double = 0.0,
    @SerializedName("currency") val currency: String = "INR",
    @SerializedName("color") val color: String = "#3B82F6"
)

// 8. Expense by Category Breakdown Models
data class ExpenseByCategoryResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ExpenseByCategoryData? = null
)

data class ExpenseByCategoryData(
    @SerializedName("totalExpenses") val totalExpenses: Double = 0.0,
    @SerializedName("categories") val categories: List<CategoryExpenseSlice> = emptyList()
)

data class CategoryExpenseSlice(
    @SerializedName("label") val label: String = "",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("percentage") val percentage: Float = 0f,
    @SerializedName("color") val color: String = "#3B82F6"
)