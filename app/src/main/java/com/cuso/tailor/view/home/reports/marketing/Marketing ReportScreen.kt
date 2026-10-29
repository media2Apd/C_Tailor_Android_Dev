@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.marketing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cuso.tailor.R
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import com.cuso.tailor.view.home.reports.ReportCategory
import com.cuso.tailor.view.home.reports.ReportItem
import com.cuso.tailor.view.home.reports.ReportsScreenTemplate

@Composable
fun MarketingReportsScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {}
) {
    // Categories and reports matching the marketing screenshots
    val categories = remember {
        listOf(
            // Category 1: Campaign Analytics & ROI
            ReportCategory(
                id = "campaign_analytics",
                title = "Campaign Analytics & ROI",
                reports = listOf(
                    ReportItem(
                        id = "mkt_campaign_roi",
                        title = "Campaign Performance & ROI Report",
                        description = "Chronological audit of active ad campaigns, budgets, actual spend, attributed revenues, ROAS, and ROI",
                        isFavorite = true
                    )
                )
            ),

            // Category 2: Acquisition & Channel Attribution
            ReportCategory(
                id = "channel_attribution",
                title = "Acquisition & Channel Attribution",
                reports = listOf(
                    ReportItem(
                        id = "mkt_channel_perf",
                        title = "Marketing Channel Performance Report",
                        description = "Analyze customer acquisition attribution across Paid Search, Paid Social, Organic, and average order values",
                        isFavorite = true
                    )
                )
            )
        )
    }

    // Filter drawer sections configured for marketing reports
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Channel",
                icon = Icons.Filled.Category,
                type = FilterSectionType.CHIP_ROW,
                isMultiSelect = true,
                options = listOf(
                    FilterOption("all", "All Channels", isSelected = true),
                    FilterOption("paid_search", "Paid Search"),
                    FilterOption("paid_social", "Paid Social"),
                    FilterOption("organic", "Organic")
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

    // Common reports template
    ReportsScreenTemplate(
        title = "Marketing Reports",
        searchPlaceholder = "Search campaigns, ROAS, channel attributions, or ad spend reports...",
        categories = categories,
        filterSections = filterSections,
        initialSelectedDateRange = "Last 30 Days",
        onClose = onClose,
        onReportClick = onReportClick,
        onExportExcel = { report -> /* Handle Excel export */ },
        onExportPdf = { report -> /* Handle PDF export */ }
    )
}