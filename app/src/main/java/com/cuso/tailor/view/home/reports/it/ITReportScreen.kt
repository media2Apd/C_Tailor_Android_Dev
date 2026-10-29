@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.it

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Laptop
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
fun ItReportsScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {}
) {
    // Categories and reports configured for IT & Systems Governance module
    val categories = remember {
        listOf(
            // Category 1: Hardware & Asset Governance (Image 1)
            ReportCategory(
                id = "hardware_asset_governance",
                title = "Hardware & Asset Governance",
                reports = listOf(
                    ReportItem(
                        id = "it_asset_report",
                        title = "IT Asset Report",
                        description = "Complete inventory of laptops, servers, POS terminals, warranties, depreciation, and user assignments",
                        isFavorite = true
                    )
                )
            ),

            // Category 2: IT Helpdesk & SLA Operations (Image 2)
            ReportCategory(
                id = "it_helpdesk_sla",
                title = "IT Helpdesk & SLA Operations",
                reports = listOf(
                    ReportItem(
                        id = "it_service_desk_report",
                        title = "IT Service Desk Report",
                        description = "Chronological audit of support tickets, technician assignments, category volumes, and SLA breaches",
                        isFavorite = true
                    )
                )
            ),

            // Category 3: Cloud, Sync & Backup Reliability (Image 3)
            ReportCategory(
                id = "cloud_sync_backup",
                title = "Cloud, Sync & Backup Reliability",
                reports = listOf(
                    ReportItem(
                        id = "it_integration_sync_health",
                        title = "Integration & Sync Health Report",
                        description = "Monitor third-party channel syncs, marketing ad accounts, token expirations, and connection health",
                        isFavorite = true
                    ),
                    ReportItem(
                        id = "it_system_backup_health",
                        title = "System & Backup Health Report",
                        description = "Automated database snapshots, storage consumption, disaster recovery verification, and SLA success rates",
                        isFavorite = true
                    )
                )
            )
        )
    }

    // Filter drawer options configured for IT reports
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Asset / Ticket Status",
                icon = Icons.Filled.Category,
                type = FilterSectionType.CHIP_ROW,
                isMultiSelect = true,
                options = listOf(
                    FilterOption("all", "All Records", isSelected = true),
                    FilterOption("assigned", "Assigned Assets"),
                    FilterOption("in_repair", "Under Repair"),
                    FilterOption("active_sync", "Active Syncs")
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
        title = "IT & Systems Reports",
        searchPlaceholder = "Search IT assets, helpdesk tickets, sync channels, or backup audits...",
        categories = categories,
        filterSections = filterSections,
        initialSelectedDateRange = "Last 30 Days",
        onClose = onClose,
        onReportClick = onReportClick,
        onExportExcel = { report -> /* Handle Excel export */ },
        onExportPdf = { report -> /* Handle PDF export */ }
    )
}