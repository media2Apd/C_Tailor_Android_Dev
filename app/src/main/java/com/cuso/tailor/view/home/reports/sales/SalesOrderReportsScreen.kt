@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.sales

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import com.cuso.tailor.view.home.reports.ReportCategory
import com.cuso.tailor.view.home.reports.ReportItem
import com.cuso.tailor.view.home.reports.ReportsScreenTemplate
import com.cuso.tailor.R

@Composable
fun SalesOrderReportsScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {}
) {
    // Categories and reports configured for Sales & Order module
    val categories = remember {
        listOf(
            // Category 1: Lead Management
            ReportCategory(
                id = "leads",
                title = "Lead Management",
                reports = listOf(
                    ReportItem(
                        id = "lead_1",
                        title = "Lead Master Register Report",
                        description = "Comprehensive chronological audit of all prospect enquiries, channels, and conversion lifecycles",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "lead_2",
                        title = "Lead Source Performance & ROI Report",
                        description = "Channel-wise acquisition breakdown, marketing win rates, and pipeline revenue attribution",
                        isFavorite = false
                    ),
                    ReportItem(
                        id = "lead_3",
                        title = "Overdue Follow-ups Audit Report",
                        description = "Actionable alert register of missed client callbacks, days overdue, and lost business prevention",
                        isFavorite = false
                    ),
                    ReportItem(
                        id = "lead_4",
                        title = "Follow-Up Activity Register Report",
                        description = "Detailed interaction logs of sales staff phone calls, WhatsApp messages, and showroom trials",
                        isFavorite = false
                    )
                )
            ),

            // Category 2: Opportunity Management
            ReportCategory(
                id = "opportunities",
                title = "Opportunity Management",
                reports = listOf(
                    ReportItem(
                        id = "opp_1",
                        title = "Opportunity Master Register Report",
                        description = "Chronological audit of active deals, stage progression, probability forecasts, and order conversions",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "opp_2",
                        title = "Opportunity Stage Funnel & Forecast Report",
                        description = "Stage-wise deals concentration, conversion probabilities, and weighted pipeline revenue forecast",
                        isFavorite = false
                    ),
                    ReportItem(
                        id = "opp_3",
                        title = "Closing Soon & Overdue Deals Report",
                        description = "Actionable audit of deals with approaching decision deadlines and overdue negotiation targets",
                        isFavorite = false
                    )
                )
            ),

            // Category 3: Customer & CRM Intelligence
            ReportCategory(
                id = "customers",
                title = "Customer & CRM Intelligence",
                reports = listOf(
                    ReportItem(
                        id = "cust_1",
                        title = "Customer Master Directory Report",
                        description = "Comprehensive register of active clients, tiers, B2B credit terms, and communication profiles",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "cust_2",
                        title = "Customer Lifetime Value (LTV) Report",
                        description = "Ranking top spenders, lifetime revenue contribution, average order sizes, and VIP client values",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "cust_3",
                        title = "Customer Credit & Outstanding Dues Report",
                        description = "Audit B2B credit facility utilization, unpaid balances, and accounts receivable risks",
                        isFavorite = false
                    ),
                    ReportItem(
                        id = "cust_4",
                        title = "Dormant Customer Win-Back Report",
                        description = "Re-engage inactive clients who have not purchased in 90+ days to prevent customer churn",
                        isFavorite = false
                    )
                )
            ),

            // Category 4: Garment Measurements & Sizing
            ReportCategory(
                id = "measurements",
                title = "Garment Measurements & Sizing",
                reports = listOf(
                    ReportItem(
                        id = "meas_1",
                        title = "Customer Measurement Master Register",
                        description = "Chronological ledger of all body measurement profile cards recorded across garments",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "meas_2",
                        title = "Garment Demand & Segment Popularity Report",
                        description = "Analyze customer garment preferences, template distribution, and measurement profile share",
                        isFavorite = false
                    ),
                    ReportItem(
                        id = "meas_3",
                        title = "Stale / Outdated Measurements Audit Report",
                        description = "Identify customers whose physical body measurements have not been updated in 6+ months",
                        isFavorite = false
                    )
                )
            ),

            // Category 5: Sales Orders & Fulfillment
            ReportCategory(
                id = "orders",
                title = "Sales Orders & Fulfillment",
                reports = listOf(
                    ReportItem(
                        id = "ord_1",
                        title = "Sales Order Master Register Report",
                        description = "Chronological audit of bespoke tailoring bookings, retail sales, advance payments, and delivery statuses",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "ord_2",
                        title = "Sales by Product Stream Report",
                        description = "Analyze revenue attribution across bespoke tailoring labor, fabric materials, add-ons, and retail products",
                        isFavorite = false
                    ),
                    ReportItem(
                        id = "ord_3",
                        title = "On-Time Delivery (OTD) SLA Report",
                        description = "Audit promised deadlines against actual fulfillment timestamps to track workshop punctuality",
                        isFavorite = false
                    ),
                    ReportItem(
                        id = "ord_4",
                        title = "Branch-Wise Sales Comparison Report",
                        description = "Evaluating branch performance, revenue contribution share, and sales volume comparisons across locations",
                        isFavorite = true
                    )
                )
            ),

            // Category 6: Quotation & Estimates
            ReportCategory(
                id = "quotations",
                title = "Quotation & Estimates",
                reports = listOf(
                    ReportItem(
                        id = "quot_1",
                        title = "Quotation Master Register Report",
                        description = "Chronological audit of customer estimates, proposed valuations, discounts, and conversion lifecycles",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "quot_2",
                        title = "Discount Leakage & Margin Audit Report",
                        description = "Audit proposal price concessions, discount margins, manager approvals, and profit impact",
                        isFavorite = false
                    ),
                    ReportItem(
                        id = "quot_3",
                        title = "Expiring Quotations Action Report",
                        description = "Actionable pipeline alert of price proposals expiring within the next 7 days requiring sales callback",
                        isFavorite = false
                    )
                )
            )
        )
    }

    // Filter drawer options configured for this screen
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Module",
                icon = Icons.Filled.Category,
                type = FilterSectionType.CHIP_ROW,
                isMultiSelect = true,
                options = listOf(
                    FilterOption("all", "All Modules", isSelected = true),
                    FilterOption("leads", "Leads"),
                    FilterOption("orders", "Sales & Orders"),
                    FilterOption("crm", "CRM")
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

    // Call the common template
    ReportsScreenTemplate(
        title = "Sales & Order Reports",
        categories = categories,
        filterSections = filterSections,
        onClose = onClose,
        onReportClick = onReportClick,
        onExportExcel = { report -> /* Handle Excel export */ },
        onExportPdf = { report -> /* Handle PDF export */ }
    )
}