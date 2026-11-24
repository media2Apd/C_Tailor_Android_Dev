@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.service.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.ComplaintEscalationItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.home.reports.*
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun ComplaintsEscalationReportScreen(
    onClose: () -> Unit,
    onViewDetail: (ComplaintEscalationItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.complaintsSummary.collectAsState()
    val pagingState by viewModel.complaintsPaging.collectAsState()
    val filteredList by viewModel.filteredComplaintsList.collectAsState()
    val searchQuery by viewModel.complaintsSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Severity",
                    options = listOf(
                        FilterOption("Critical", "Critical"),
                        FilterOption("High", "High"),
                        FilterOption("Medium", "Medium"),
                        FilterOption("Low", "Low")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Status",
                    options = listOf(
                        FilterOption("Open", "Open"),
                        FilterOption("In Progress", "In Progress"),
                        FilterOption("Resolved", "Resolved")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Flags",
                    options = listOf(FilterOption("breached_only", "SLA Breached Only")),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { section -> section.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) { viewModel.loadComplaintsReport() }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL COMPLAINTS",
                value = (summary?.totalComplaints ?: 0).toString(),
                subtitle = "${summary?.openComplaints ?: 0} currently open",
                icon = Icons.Default.Report,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "RESOLUTION RATE",
                value = summary?.resolutionRate ?: "0%",
                subtitle = "${summary?.resolvedComplaints ?: 0} resolved",
                subtitleColor = greentext,
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "SLA BREACHED",
                value = (summary?.slaBreachedCount ?: 0).toString(),
                subtitle = "Past resolution deadline",
                subtitleColor = redText,
                icon = Icons.Default.Timer,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "CRITICAL / HIGH",
                value = "${summary?.severityBreakdown?.critical ?: 0} / ${summary?.severityBreakdown?.high ?: 0}",
                subtitle = "Escalation priority",
                icon = Icons.Default.ErrorOutline,
                valueColor = yellowText,
                subtitleColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Complaints & Escalation Report",
        subtitle = null,
        sectionTitle = "Complaint Escalation Register",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onComplaintsSearchChange,
        searchPlaceholder = "Search by complaint, order or customer...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val sev = updated.find { it.title == "Severity" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val st = updated.find { it.title == "Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val breached = updated.find { it.title == "Flags" }
                ?.options?.any { it.id == "breached_only" && it.isSelected } ?: false
            viewModel.applyComplaintsFilters(sev, st, breached)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearComplaintsFilters()
        },
        onLoadMore = { viewModel.loadComplaintsReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Complaints report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Complaints report export started") },
        onClose = onClose,
        emptyMessage = "No complaints or escalations found."
    ) { item ->
        val isResolved = item.status.equals("Resolved", true)
        val badgeText = if (isResolved) greentext else if (item.isSlaBreached) redText else yellowText
        val badgeBg = if (isResolved) greenBg else if (item.isSlaBreached) redBg else yellowBg

        DataCard(
            item = item,
            title = item.customerName.ifBlank { "--" },
            subtitle = "${item.complaintCode.ifBlank { "--" }}  •  ${item.orderCode.ifBlank { "--" }}",
            topBadgeText = item.status.ifBlank { "Open" },
            topBadgeTextColor = badgeText,
            topBadgeBgColor = badgeBg,
            topBadgeInline = true,
            actions = listOf(
                MenuAction(label = "View Details", icon = Icons.Default.Visibility, onClick = { onViewDetail(item) })
            ),
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Severity") {
                        Text(
                            text = item.severity.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (item.severity.equals("Critical", true)) redText else TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Category", item.category.ifBlank { "--" }),
                                ReportGridCell("Assigned To", item.assignedTo.ifBlank { "Unassigned" }),
                                ReportGridCell("SLA", if (item.isSlaBreached) "Breached" else "Within SLA")
                            ),
                            listOf(
                                ReportGridCell("Logged On", viewModel.formatDate(item.createdAt)),
                                ReportGridCell("Resolved On", viewModel.formatDate(item.resolvedAt)),
                                ReportGridCell("Contact", item.customerPhone.ifBlank { "--" })
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = item.description.ifBlank { "No description provided" })
                }
            }
        )
    }
}