@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.security

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import com.cuso.tailor.view.home.reports.ReportCategory
import com.cuso.tailor.view.home.reports.ReportItem
import com.cuso.tailor.view.home.reports.ReportsScreenTemplate

@Composable
fun SecurityReportsScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onReportClick: (ReportItem) -> Unit = {}
) {
    // Categories and reports configured for the Security & Compliance module
    val categories = remember {
        listOf(
            // Category 1: Threat & Incident Management (Image 2)
            ReportCategory(
                id = "threat_incident_mgmt",
                title = "Threat & Incident Management",
                reports = listOf(
                    ReportItem(
                        id = "sec_incidents_alerts",
                        title = "Security Incidents & Alerts Report",
                        description = "Audit suspicious activities, unauthorized breaches, brute-force attempts, and malware alerts",
                        isFavorite = true
                    )
                )
            ),

            // Category 2: Identity & Access Audits (Image 1)
            ReportCategory(
                id = "identity_access_audits",
                title = "Identity & Access Audits",
                reports = listOf(
                    ReportItem(
                        id = "sec_access_permission",
                        title = "Access & Permission Activity Report",
                        description = "Monitor authentication histories, session escalations, failed logins, and suspicious IP traces",
                        isFavorite = true
                    )
                )
            ),

            // Category 3: API & Webhook Governance (Image 3)
            ReportCategory(
                id = "api_webhook_governance",
                title = "API & Webhook Governance",
                reports = listOf(
                    ReportItem(
                        id = "sec_api_integration_security",
                        title = "API & Integration Security Report",
                        description = "Monitor third-party API connections, token security health, rate-limit consumption, and webhook SLAs",
                        isFavorite = true
                    )
                )
            )
        )
    }

    // Filter drawer options configured for security reports
    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Severity Level",
                icon = Icons.Filled.Category,
                type = FilterSectionType.CHIP_ROW,
                isMultiSelect = true,
                options = listOf(
                    FilterOption("all", "All Severities", isSelected = true),
                    FilterOption("critical", "Critical"),
                    FilterOption("high", "High Risk"),
                    FilterOption("medium", "Medium"),
                    FilterOption("low", "Info / Low")
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
        title = "Security & Audit Reports",
        searchPlaceholder = "Search security audits, breach logs, or integration reports...",
        categories = categories,
        filterSections = filterSections,
        initialSelectedDateRange = "Last 30 Days",
        onClose = onClose,
        onReportClick = onReportClick,
        onExportExcel = { report -> /* Handle Excel export */ },
        onExportPdf = { report -> /* Handle PDF export */ }
    )
}