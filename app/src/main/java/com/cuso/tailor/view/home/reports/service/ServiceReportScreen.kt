@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.service

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import com.cuso.tailor.view.home.reports.ReportCategory
import com.cuso.tailor.view.home.reports.ReportItem
import com.cuso.tailor.view.home.reports.ReportsScreenTemplate

@Composable
fun ServiceReportsScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {}
) {
    // Categories and reports configured for the Customer Service & Support module
    val categories = remember {
        listOf(
            // Category 1: Customer Feedback & NPS (Image 3)
            ReportCategory(
                id = "customer_feedback_nps",
                title = "Customer Feedback & NPS",
                reports = listOf(
                    ReportItem(
                        id = "srv_feedback_nps",
                        title = "Customer Feedback & NPS Summary Report",
                        description = "Detailed audit of customer satisfaction ratings, Net Promoter Score (NPS), and review remarks",
                        isFavorite = true
                    )
                )
            ),

            // Category 2: Complaints & Escalations (Image 2)
            ReportCategory(
                id = "complaints_escalations",
                title = "Complaints & Escalations",
                reports = listOf(
                    ReportItem(
                        id = "srv_complaints_escalation",
                        title = "Complaints & Escalation Report",
                        description = "Tracking customer grievances, defect categories, SLA breaches, and resolution workflows",
                        isFavorite = true
                    )
                )
            ),

            // Category 3: Service Performance & Fulfillment (Image 1)
            ReportCategory(
                id = "service_performance_fulfillment",
                title = "Service Performance & Fulfillment",
                reports = listOf(
                    ReportItem(
                        id = "srv_performance_sla",
                        title = "Service Performance & SLA Report",
                        description = "Auditing on-time delivery rates, workshop punctuality, and deadline fulfillments",
                        isFavorite = true
                    )
                )
            )
        )
    }

    // Filter drawer options configured for service reports
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Resolution Status",
                icon = Icons.Filled.Category,
                type = FilterSectionType.CHIP_ROW,
                isMultiSelect = true,
                options = listOf(
                    FilterOption("all", "All Statuses", isSelected = true),
                    FilterOption("open", "Open Grievances"),
                    FilterOption("in_progress", "Under Review"),
                    FilterOption("resolved", "Resolved")
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
        title = "Customer Service Reports",
        searchPlaceholder = "Search service reports by name, module or author...",
        categories = categories,
        filterSections = filterSections,
        initialSelectedDateRange = "Last 30 Days",
        onClose = onClose,
        onReportClick = onReportClick,
        onExportExcel = { report -> /* Handle Excel export */ },
        onExportPdf = { report -> /* Handle PDF export */ }
    )
}