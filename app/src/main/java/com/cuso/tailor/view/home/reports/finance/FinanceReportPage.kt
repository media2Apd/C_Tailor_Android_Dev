@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.finance

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import com.cuso.tailor.view.home.reports.ReportCategory
import com.cuso.tailor.view.home.reports.ReportItem
import com.cuso.tailor.view.home.reports.ReportsScreenTemplate

@Composable
fun FinanceReportsScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {}
) {
    // Categories and reports configured for the Finance module
    val categories = remember {
        listOf(
            // Category 1: Financial Overview & P&L (Image 1)
            ReportCategory(
                id = "financial_overview_pnl",
                title = "Financial Overview & P&L",
                reports = listOf(
                    ReportItem(
                        id = "fin_rev_exp_summary",
                        title = "Revenue & Expense Summary Report",
                        description = "Top-level financial KPIs (Total Revenue, Total Expense, Net Profit) and grouped account ledgers",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "fin_cash_flow_indirect",
                        title = "Cash Flow Statement (Indirect Method)",
                        description = "Tracks operating, investing, and financing cash movements across business periods",
                        isFavorite = true
                    )
                )
            ),

            // Category 2: Receivables & Payables Aging (Image 2)
            ReportCategory(
                id = "receivables_payables_aging",
                title = "Receivables & Payables Aging",
                reports = listOf(
                    ReportItem(
                        id = "fin_ar_aging",
                        title = "Receivables Aging Report (AR)",
                        description = "Buckets outstanding customer balances by overdue days (Current, 1-30, 31-60, 61-90, 90+)",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "fin_ap_aging",
                        title = "Payables Aging Report (AP)",
                        description = "Buckets outstanding supplier bills by overdue days to manage cash disbursements effectively",
                        isFavorite = false
                    )
                )
            ),

            // Category 3: Taxation & GST Compliance (Image 3)
            ReportCategory(
                id = "taxation_gst_compliance",
                title = "Taxation & GST Compliance",
                reports = listOf(
                    ReportItem(
                        id = "fin_gst_tax_summary",
                        title = "GST / Tax Summary Report",
                        description = "GSTR-3B audit register tracking output tax, input tax credit (ITC), and net tax payable",
                        isFavorite = true
                    )
                )
            ),

            // Category 4: Payments & Collections (Image 4)
            ReportCategory(
                id = "payments_collections",
                title = "Payments & Collections",
                reports = listOf(
                    ReportItem(
                        id = "fin_payment_collection",
                        title = "Payment Collection Report",
                        description = "Tracks customer payments, invoice settlements, and mode-wise breakdown (Cash, UPI, Card, Bank)",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "fin_advance_payment",
                        title = "Advance Payment Report",
                        description = "Monitors customer advances against sales orders and remaining unallocated balances",
                        isFavorite = false
                    ),
                    ReportItem(
                        id = "fin_payment_mode_breakdown",
                        title = "Payment Mode Breakdown Report",
                        description = "Comprehensive breakdown of all incoming collections and outgoing payments grouped by mode",
                        isFavorite = false
                    )
                )
            ),

            // Category 5: Outstanding & Overdue Audits (Image 5)
            ReportCategory(
                id = "outstanding_overdue_audits",
                title = "Outstanding & Overdue Audits",
                reports = listOf(
                    ReportItem(
                        id = "fin_balance_pending",
                        title = "Balance Pending Report",
                        description = "Lists all unpaid or partially paid sales invoices with pending balances due",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "fin_overdue_payment",
                        title = "Overdue Payment Report",
                        description = "Actionable audit of receivables and payables past their statutory due dates",
                        isFavorite = false
                    )
                )
            )
        )
    }

    // Filter drawer options configured for finance reports
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Accounting Type",
                icon = Icons.Filled.Category,
                type = FilterSectionType.CHIP_ROW,
                isMultiSelect = true,
                options = listOf(
                    FilterOption("all", "All Records", isSelected = true),
                    FilterOption("ar", "Receivables"),
                    FilterOption("ap", "Payables"),
                    FilterOption("tax", "Tax & GST")
                )
            ),
            FilterSection(
                title = "Favorite Status",
                icon = Icons.Filled.Star,
                type = FilterSectionType.CHIP_ROW,
                options = listOf(
                    FilterOption("all", "All", isSelected = true),
                    FilterOption("starred", "Starred Only")
                )
            )
        )
    }

    // Render using the common reusable reports template
    ReportsScreenTemplate(
        title = "Finance & Accounts Reports",
        searchPlaceholder = "Search finance reports by name, module or author...",
        categories = categories,
        filterSections = filterSections,
        initialSelectedDateRange = "Last 30 Days",
        onClose = onClose,
        onReportClick = onReportClick,
        onExportExcel = { report -> /* Handle Excel export */ },
        onExportPdf = { report -> /* Handle PDF export */ }
    )
}