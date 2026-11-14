@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.it.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.report.ItServiceDeskReportItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.dividerColor
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.home.reports.ReportDetailGridBox
import com.cuso.tailor.view.home.reports.ReportGridCell
import com.cuso.tailor.view.home.reports.ReportInfinitelyPaginatedScaffold
import com.cuso.tailor.view.home.reports.ReportInlineMetricRow
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun ItServiceDeskReportScreen(
    onClose: () -> Unit,
    onViewTicketAudit: (ItServiceDeskReportItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.itServiceDeskSummary.collectAsState()
    val pagingState by viewModel.itServiceDeskPagingState.collectAsState()
    val filteredList by viewModel.filteredItServiceDeskList.collectAsState()
    val searchQuery by viewModel.itServiceDeskSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Ticket Status",
                    options = listOf(
                        FilterOption("Open", "Open"),
                        FilterOption("In Progress", "In Progress"),
                        FilterOption("Resolved", "Resolved"),
                        FilterOption("Closed", "Closed")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Priority",
                    options = listOf(
                        FilterOption("Low", "Low"),
                        FilterOption("Medium", "Medium"),
                        FilterOption("High", "High"),
                        FilterOption("Critical", "Critical")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "SLA Milestone",
                    options = listOf(
                        FilterOption("ON TRACK", "On Track"),
                        FilterOption("BREACHED", "Breached")
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
        viewModel.loadItServiceDeskReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL TICKETS",
                value = (summary?.totalTickets ?: 0).toString(),
                subtitle = "Logged requests",
                icon = Icons.Default.HeadsetMic,
                valueColor = TextPrimary,
                subtitleColor = mutedText,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "OPEN / IN PROGRESS",
                value = (summary?.openTicketsCount ?: 0).toString(),
                subtitle = "${summary?.criticalHighCount ?: 0} critical/high priority",
                icon = Icons.Default.Schedule,
                valueColor = yellowText,
                subtitleColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "SLA COMPLIANCE",
                value = summary?.slaComplianceRate ?: "0%",
                subtitle = "${summary?.resolvedTicketsCount ?: 0} resolved tickets",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                subtitleColor = mutedText,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "SLA BREACHED",
                value = (summary?.slaBreachedCount ?: 0).toString(),
                subtitle = "Overdue resolutions",
                icon = Icons.Default.ErrorOutline,
                valueColor = redText,
                subtitleColor = mutedText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "IT Service Desk Report",
        subtitle = null,
        sectionTitle = "Service Desk Ticket Registry",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onItServiceDeskSearchQueryChange,
        searchPlaceholder = "Search by ticket ID, subject, requester or tech...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Ticket Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val priorities = updated.find { it.title == "Priority" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val slaStatuses = updated.find { it.title == "SLA Milestone" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyItServiceDeskFilters(statuses, priorities, slaStatuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearItServiceDeskFilters()
        },
        onLoadMore = viewModel::loadMoreItServiceDesk,
        onShareClick = { DynamicIslandManager.showSuccess("IT service desk report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("IT service desk report export started") },
        onClose = onClose,
        emptyMessage = "No service desk tickets found."
    ) { item ->
        val (statusTextColor, statusBgColor) = when (item.statusBadge.lowercase()) {
            "open", "resolved", "closed" -> greentext to greenBg
            "in progress" -> yellowText to yellowBg
            else -> redText to redBg
        }
        val slaColor = if (item.slaStatus.contains("breach", ignoreCase = true)) redText else greentext
        val priorityColor = when (item.priority.lowercase()) {
            "critical", "high" -> redText
            "medium" -> yellowText
            else -> TextPrimary
        }

        DataCard(
            item = item,
            title = item.ticketId.ifBlank { "--" },
            subtitle = item.subject.ifBlank { "--" },
            topBadgeText = item.statusBadge.ifBlank { "--" },
            topBadgeTextColor = statusTextColor,
            topBadgeBgColor = statusBgColor,
            topBadgeInline = true,
            actions = listOf(
                MenuAction(
                    label = "View Ticket Audit",
                    icon = Icons.Default.Visibility,
                    onClick = { onViewTicketAudit(item) }
                )
            ),
            onClick = onViewTicketAudit,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Linked Asset") {
                        Text(
                            text = item.relatedAsset.ifBlank { "General IT Inquiry" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Category", item.category.ifBlank { "--" }),
                                ReportGridCell("SLA Milestone", item.slaStatus.ifBlank { "--" }, valueColor = slaColor),
                                ReportGridCell("Requester", item.requesterName.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Assigned Tech", item.technician.ifBlank { "--" }),
                                ReportGridCell("Priority", item.priority.ifBlank { "--" }, valueColor = priorityColor),
                                ReportGridCell("Department", item.requesterDept.ifBlank { "--" })
                            )
                        )
                    )

                    HorizontalDivider(color = dividerColor)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.5f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = mutedText,
                                modifier = Modifier.size(tokens.iconSize * 0.75f)
                            )
                            Text(
                                text = "Target SLA: ${viewModel.formatDate(item.targetResolutionTime)}",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }

                        Row(
                            modifier = Modifier.clickable { onViewTicketAudit(item) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                        ) {
                            Text(
                                text = "View ticket audit",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View ticket audit",
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize * 0.8f)
                            )
                        }
                    }
                }
            }
        )
    }
}