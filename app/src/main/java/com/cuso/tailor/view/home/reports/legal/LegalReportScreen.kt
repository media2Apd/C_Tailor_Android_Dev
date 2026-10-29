@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.legal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import com.cuso.tailor.view.home.reports.ReportCategory
import com.cuso.tailor.view.home.reports.ReportItem
import com.cuso.tailor.view.home.reports.ReportsScreenTemplate

@Composable
fun LegalReportsScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {}
) {
    // Categories and reports configured for Audit & Legal module
    val categories = remember {
        listOf(
            // Category 1: Contracts & Agreements (Image 2)
            ReportCategory(
                id = "contracts_agreements",
                title = "Contracts & Agreements",
                reports = listOf(
                    ReportItem(
                        id = "leg_contracts_expiry",
                        title = "Contracts & Expiry Audit Report",
                        description = "Complete portfolio register of active agreements, counter-party commitments, renewal timelines, and financial exposures",
                        isFavorite = true
                    )
                )
            ),

            // Category 2: Statutory & Licenses (Image 1)
            ReportCategory(
                id = "statutory_licenses",
                title = "Statutory & Licenses",
                reports = listOf(
                    ReportItem(
                        id = "leg_compliance_license",
                        title = "Compliance & License Audit Report",
                        description = "Monitor operational corporate licenses, regulatory compliance deadlines, issuing authorities, and validity periods",
                        isFavorite = true
                    )
                )
            ),

            // Category 3: Litigation & Disputes (Image 3)
            ReportCategory(
                id = "litigation_disputes",
                title = "Litigation & Disputes",
                reports = listOf(
                    ReportItem(
                        id = "leg_cases_disputes",
                        title = "Legal Cases & Disputes Action Report",
                        description = "Audit active notices, commercial disputes, hearing schedules, jurisdiction risks, and counter-party claims",
                        isFavorite = true
                    )
                )
            )
        )
    }

    // Filter drawer options configured for legal & audit reports
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Legal Status",
                icon = Icons.Filled.Category,
                type = FilterSectionType.CHIP_ROW,
                isMultiSelect = true,
                options = listOf(
                    FilterOption("all", "All Records", isSelected = true),
                    FilterOption("active_contracts", "Active Contracts"),
                    FilterOption("expiring_soon", "Expiring < 30D"),
                    FilterOption("in_litigation", "Open Disputes")
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
        title = "Legal & Audit Reports",
        searchPlaceholder = "Search legal reports, compliance registers or dispute audits...",
        categories = categories,
        filterSections = filterSections,
        initialSelectedDateRange = "Last 30 Days",
        onClose = onClose,
        onReportClick = onReportClick,
        onExportExcel = { report -> /* Handle Excel export */ },
        onExportPdf = { report -> /* Handle PDF export */ }
    )
}