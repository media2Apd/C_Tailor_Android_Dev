@file:Suppress(
    "UNUSED_VALUE",
    "AssignedValueIsNeverRead",
    "unusedvariable",
    "unused",
    "NAME_SHADOWING",
    "GrazieInspection",
    "SpellCheckingInspection", "VariableNeverRead"
)

package com.cuso.mobile.view.home.reports.finance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.view.composable.AccordionSection
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.R
import com.cuso.mobile.view.home.reports.inventory.ReportCountBadge
import com.cuso.mobile.view.home.reports.inventory.ReportListItem

@Composable
fun FinanceReportPage(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit,
    onReportClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val scrollState = rememberScrollState()
    var expandedSection by remember { mutableStateOf("Financial Statements") }

    Scaffold(
        topBar = {
            Column(
                Modifier.fillMaxWidth()
            ) {
                TitleBar("Finance", onClose = onClose)
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {

            AccordionSection(
                title = "Financial Statements",
                iconPainter = painterResource(R.drawable.box),
                expanded = expandedSection == "Financial Statements",
                onHeaderClick = { expandedSection = if (expandedSection == "Financial Statements") "" else "Financial Statements" },
                trailing = { ReportCountBadge(4) }
            ) {
                ReportListItem(
                    "Profit & Loss Report",
                    "View income, expenses, and net profit for the selected period",
                    initialFavorite = true,
                    onClick = { onReportClick("reports_finance_profit_and_loss_report") })
                ReportListItem(
                    "Balance Sheet", "View assets, liabilities, and equity at a specific date.",
                    initialFavorite = true,
                    onClick = { onReportClick("reports_finance_balance_sheet") })
                ReportListItem(
                    "Cash Flow Report", "Track cash inflows, outflows, and overall cash position.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_finance_cash_flow") })
                ReportListItem(
                    "Trial Balance",
                    "Verify debit and credit balances before financial statements.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_finance_trial_balance") })
            }

            AccordionSection(
                title = "Receivables & Payables",
                icon = Icons.Default.WarningAmber,
                expanded = expandedSection == "Receivables & Payables",
                onHeaderClick = { expandedSection = if (expandedSection == "Receivables & Payables") "" else "Receivables & Payables" },
                trailing = { ReportCountBadge(4) }
            ) {
                ReportListItem(
                    "Accounts Receivable Report", "Monitor outstanding customer payments.",
                    initialFavorite = true,
                    onClick = { onReportClick("reports_finance_accounts_receivable") })
                ReportListItem(
                    "Accounts Payable Report", "Track pending supplier payments.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_finance_accounts_payable") })
                ReportListItem(
                    "Customer Outstanding Report", "View unpaid customer balances.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_finance_customer_outstanding") })
                ReportListItem(
                    "Supplier Outstanding Report", "View unpaid supplier balances.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_finance_supplier_outstanding") })
            }

            AccordionSection(
                title = "Transaction Reports",
                icon = Icons.Default.Receipt,
                expanded = expandedSection == "Transaction Reports",
                onHeaderClick = { expandedSection = if (expandedSection == "Transaction Reports") "" else "Transaction Reports" },
                trailing = { ReportCountBadge(4) }
            ) {
                ReportListItem(
                    "Sales Invoice Report", "Review sales invoices and payment status.",
                    initialFavorite = true,
                    onClick = { onReportClick("reports_finance_sales_invoice") })
                ReportListItem(
                    "Purchase Invoice Report", "Review purchase invoices and supplier details.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_finance_purchase_invoice") })
                ReportListItem(
                    "Expense Report", "Monitor business expenses by category",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_finance_expense") })
                ReportListItem(
                    "Journal Entry Report", "Review accounting journal entries.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_finance_journal_entry") })
            }

            AccordionSection(
                title = "Payment Reports",
                iconPainter = painterResource(R.drawable.cart),
                expanded = expandedSection == "Payment Reports",
                onHeaderClick = { expandedSection = if (expandedSection == "Payment Reports") "" else "Payment Reports" },
                trailing = { ReportCountBadge(2) }
            ) {
                ReportListItem(
                    "Payment Report", "View all incoming and outgoing payments",
                    initialFavorite = true,
                    onClick = { onReportClick("reports_finance_payment") })
                ReportListItem(
                    "Payment Status Report", "Monitor paid, pending, and failed payments.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_finance_payment_status") })
            }

            AccordionSection(
                title = "Financial Analysis",
                icon = Icons.Default.BarChart,
                expanded = expandedSection == "Financial Analysis",
                onHeaderClick = { expandedSection = if (expandedSection == "Financial Analysis") "" else "Financial Analysis" },
                trailing = { ReportCountBadge(3) }
            ) {
                ReportListItem(
                    "Revenue Report", "Analyze revenue across selected periods.",
                    initialFavorite = true,
                    onClick = { onReportClick("reports_finance_revenue") })
                ReportListItem(
                    "Expense Analysis Report", "Compare expenses by category.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_finance_expense_analysis") })
                ReportListItem(
                    "Tax Summary Report", "Review collected and payable taxes.",
                    initialFavorite = false,
                    onClick = { onReportClick("reports_finance_tax_summary") })
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}