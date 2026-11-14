@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.security.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.report.SecurityIncidentReportItemDto
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.badgeGrey
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.home.reports.ReportCardFooterCaption
import com.cuso.tailor.view.home.reports.ReportDetailGridBox
import com.cuso.tailor.view.home.reports.ReportGridCell
import com.cuso.tailor.view.home.reports.ReportInfinitelyPaginatedScaffold
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun SecurityIncidentsReportScreen(
    onClose: () -> Unit,
    onViewDetail: (SecurityIncidentReportItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.securityIncidentSummary.collectAsState()
    val pagingState by viewModel.securityIncidentsPagingState.collectAsState()
    val filteredList by viewModel.filteredSecurityIncidentsList.collectAsState()
    val searchQuery by viewModel.securityIncidentSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Severity",
                    options = listOf(
                        FilterOption("LOW", "Low"),
                        FilterOption("MEDIUM", "Medium"),
                        FilterOption("HIGH", "High"),
                        FilterOption("CRITICAL", "Critical")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Status",
                    options = listOf(
                        FilterOption("open", "Open"),
                        FilterOption("resolved", "Resolved"),
                        FilterOption("closed", "Closed")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Alert Trigger",
                    options = listOf(
                        FilterOption("alerts_only", "Triggered Alerts Only")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }

    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { section -> section.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) {
        viewModel.loadSecurityIncidentsReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL",
                value = (summary?.totalIncidents ?: 0).toString(),
                subtitle = "All logs",
                icon = Icons.Default.Info,
                valueColor = TextPrimary,
                subtitleColor = mutedText,
                iconTint = mutedText,
                iconBgColor = badgeGrey
            ),
            ReportMetricSpec(
                label = "HIGH SEV",
                value = (summary?.criticalHighSeverity ?: 0).toString(),
                subtitle = "Critical",
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                subtitleColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "RESOLUTION",
                value = summary?.resolutionRate ?: "0%",
                subtitle = "Closed",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                subtitleColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "OPEN",
                value = (summary?.openUnresolved ?: 0).toString(),
                subtitle = "${summary?.alertTriggered ?: 0} alerts",
                icon = Icons.Default.NotificationsNone,
                valueColor = yellowText,
                subtitleColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Security Incidents & Alerts Report",
        subtitle = null,
        sectionTitle = "Incident Telemetry Ledger",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSecurityIncidentSearchQueryChange,
        searchPlaceholder = "Search by alert code, user, IP or event...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val severities = updated.find { it.title == "Severity" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val statuses = updated.find { it.title == "Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val alertsOnly = updated.find { it.title == "Alert Trigger" }
                ?.options?.any { it.id == "alerts_only" && it.isSelected } ?: false
            viewModel.applySecurityIncidentFilters(severities, statuses, alertsOnly)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearSecurityIncidentFilters()
        },
        onLoadMore = viewModel::loadMoreSecurityIncidents,
        onShareClick = { DynamicIslandManager.showSuccess("Security incidents report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Security incidents report export started") },
        onClose = onClose,
        emptyMessage = "No security incidents found."
    ) { item ->
        val isOpen = item.status.equals("open", ignoreCase = true)
        val badgeTextColor = if (isOpen) yellowText else greentext
        val badgeBgColor = if (isOpen) yellowBg else greenBg
        val severityColor = when (item.severity.uppercase()) {
            "CRITICAL", "HIGH" -> redText
            "MEDIUM" -> yellowText
            else -> TextPrimary
        }

        DataCard(
            item = item,
            title = item.alertCode.ifBlank { "--" },
            subtitle = "${item.eventType}  •  ${item.severity}",
            topBadgeText = item.status.uppercase(),
            topBadgeTextColor = badgeTextColor,
            topBadgeBgColor = badgeBgColor,
            topBadgeInline = true,
            actions = listOf(
                MenuAction(
                    label = "View Details",
                    icon = Icons.Default.Visibility,
                    onClick = { onViewDetail(item) }
                )
            ),
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Target Account", item.user.ifBlank { "--" }),
                                ReportGridCell("IP Address", item.ipAddress.ifBlank { "--" }),
                                ReportGridCell("Geo Location", item.location.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Event Type", item.eventType.ifBlank { "--" }),
                                ReportGridCell("Severity", item.severity.ifBlank { "--" }, valueColor = severityColor),
                                ReportGridCell("Device / Client", item.userAgent.ifBlank { "--" })
                            )
                        )
                    )

                    Spacer(androidx.compose.ui.Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Timestamp: ${viewModel.formatDate(item.createdAt)}")
                }
            }
        )
    }
}