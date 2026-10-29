@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.inventory

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
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
fun InventoryReportsScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {}
) {
    // Categories and reports configured for the Inventory module
    val categories = remember {
        listOf(
            // Category 1: Stock Levels & Locations (Image 1)
            ReportCategory(
                id = "stock_levels_locations",
                title = "Stock Levels & Locations",
                reports = listOf(
                    ReportItem(
                        id = "inv_stock_summary",
                        title = "Stock Summary Report",
                        description = "Live inventory on-hand balances, reserved allocations, safety thresholds, and bin health",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "inv_low_stock_reorder",
                        title = "Low Stock & Reorder Action Report",
                        description = "Real-time replenishment alerts, automated reorder rule statuses, and shortage quantities",
                        isFavorite = true
                    )
                )
            ),

            // Category 2: Audit & Movement Ledgers (Image 2)
            ReportCategory(
                id = "audit_movement_ledgers",
                title = "Audit & Movement Ledgers",
                reports = listOf(
                    ReportItem(
                        id = "inv_stock_movement",
                        title = "Stock Movement Report",
                        description = "Chronological transaction audit of purchase receipts, sales issues, transfers, and adjustments",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "inv_material_consumption",
                        title = "Material Consumption Report",
                        description = "Track bespoke tailoring fabric utilization, job card consumptions, and overhead trims usage",
                        isFavorite = false
                    )
                )
            ),

            // Category 3: Asset & Cost Intelligence (Image 3)
            ReportCategory(
                id = "asset_cost_intelligence",
                title = "Asset & Cost Intelligence",
                reports = listOf(
                    ReportItem(
                        id = "inv_valuation_report",
                        title = "Inventory Valuation Report",
                        description = "Asset portfolio valuation at purchase cost, potential retail revenues, and gross margins",
                        isFavorite = true
                    )
                )
            )
        )
    }

    // Filter drawer options configured for inventory reports
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Stock Type",
                icon = Icons.Filled.Category,
                type = FilterSectionType.CHIP_ROW,
                isMultiSelect = true,
                options = listOf(
                    FilterOption("all", "All Items", isSelected = true),
                    FilterOption("fabrics", "Fabrics"),
                    FilterOption("trims", "Trims & Accessories"),
                    FilterOption("garments", "Finished Garments")
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
        title = "Inventory Reports",
        searchPlaceholder = "Search inventory, valuation, stock movements, or fabric reports...",
        categories = categories,
        filterSections = filterSections,
        initialSelectedDateRange = "Last 30 Days",
        onClose = onClose,
        onReportClick = onReportClick,
        onExportExcel = { report -> /* Handle Excel export */ },
        onExportPdf = { report -> /* Handle PDF export */ }
    )
}