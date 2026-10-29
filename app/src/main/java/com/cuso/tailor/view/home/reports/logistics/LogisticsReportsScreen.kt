@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.logistics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Undo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import com.cuso.tailor.view.home.reports.ReportCategory
import com.cuso.tailor.view.home.reports.ReportItem
import com.cuso.tailor.view.home.reports.ReportsScreenTemplate

@Composable
fun LogisticsReportsScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {}
) {
    // Categories and reports configured for the Logistics & Delivery module
    val categories = remember {
        listOf(
            // Category 1: Delivery & Fulfillment SLA (Image 1)
            ReportCategory(
                id = "delivery_fulfillment_sla",
                title = "Delivery & Fulfillment SLA",
                reports = listOf(
                    ReportItem(
                        id = "log_delivery_performance",
                        title = "Delivery Performance Report",
                        description = "Audit on-time delivery rates, courier transit SLAs, trial fittings, and failed attempt dispositions",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "log_shipment_tracking",
                        title = "Shipment & Tracking Report",
                        description = "Live courier dispatches, AWB tracking logs, package dimensions, and multi-hub transit statuses",
                        isFavorite = true
                    )
                )
            ),

            // Category 2: Reverse Logistics & RTO (Image 2)
            ReportCategory(
                id = "reverse_logistics_rto",
                title = "Reverse Logistics & RTO",
                reports = listOf(
                    ReportItem(
                        id = "log_returns_rto",
                        title = "Returns / RTO Report",
                        description = "Monitor customer return requests, bespoke garment alterations, transit damages, and resolution cycles",
                        isFavorite = true
                    )
                )
            ),

            // Category 3: COD & Courier Finance (Image 3)
            ReportCategory(
                id = "cod_courier_finance",
                title = "COD & Courier Finance",
                reports = listOf(
                    ReportItem(
                        id = "log_cod_reconciliation",
                        title = "COD Reconciliation Report",
                        description = "Audit courier Cash-on-Delivery collections, delivery charge deductions, remitted funds, and payouts",
                        isFavorite = true
                    )
                )
            )
        )
    }

    // Filter drawer options configured for logistics reports
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Courier Partner",
                icon = Icons.Filled.Category,
                type = FilterSectionType.CHIP_ROW,
                isMultiSelect = true,
                options = listOf(
                    FilterOption("all", "All Couriers", isSelected = true),
                    FilterOption("bluedart", "Blue Dart"),
                    FilterOption("delhivery", "Delhivery"),
                    FilterOption("dtdc", "DTDC"),
                    FilterOption("self", "In-House Delivery")
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
        title = "Logistics & Delivery Reports",
        searchPlaceholder = "Search delivery SLAs, AWB shipments, RTO returns, or COD audits...",
        categories = categories,
        filterSections = filterSections,
        initialSelectedDateRange = "Last 30 Days",
        onClose = onClose,
        onReportClick = onReportClick,
        onExportExcel = { report -> /* Handle Excel export */ },
        onExportPdf = { report -> /* Handle PDF export */ }
    )
}