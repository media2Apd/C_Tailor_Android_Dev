@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.sales.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.report.LeadFollowupActivityItemDto
import com.cuso.tailor.model.report.LeadMasterRegisterItemDto
import com.cuso.tailor.model.report.LeadOverdueFollowupItemDto
import com.cuso.tailor.model.report.LeadSourcePerformanceItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.purpleBg
import com.cuso.tailor.ui.theme.purpleText
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
import com.cuso.tailor.view.home.reports.ReportInlineMetricRow
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun LeadMasterRegisterReportScreen(
    onClose: () -> Unit,
    onViewDetail: (LeadMasterRegisterItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.leadMasterSummary.collectAsState()
    val pagingState by viewModel.leadMasterPaging.collectAsState()
    val filteredList by viewModel.filteredLeadMasterList.collectAsState()
    val searchQuery by viewModel.leadMasterSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Lead Status",
                    options = listOf(
                        FilterOption("New", "New"),
                        FilterOption("Contacted", "Contacted"),
                        FilterOption("Qualified", "Qualified"),
                        FilterOption("Lost", "Lost")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Priority",
                    options = listOf(
                        FilterOption("Low", "Low"),
                        FilterOption("Medium", "Medium"),
                        FilterOption("High", "High"),
                        FilterOption("Urgent", "Urgent")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) { filterSections.sumOf { s -> s.options.count { it.isSelected } } }

    LaunchedEffect(Unit) { viewModel.loadLeadMasterReport(isLoadMore = false) }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL LEADS",
                value = (summary?.totalLeads ?: 0).toString(),
                subtitle = "${summary?.activeLeads ?: 0} active pipeline",
                icon = Icons.Default.Group,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "CONVERTED LEADS",
                value = (summary?.convertedLeads ?: 0).toString(),
                subtitle = "${summary?.conversionRate ?: "0%"} conversion",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                subtitleColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "LOST LEADS",
                value = (summary?.lostLeads ?: 0).toString(),
                subtitle = "Dropped enquiries",
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "EST. BUDGET POOL",
                value = viewModel.formatCurrency(summary?.totalEstimatedBudget ?: 0.0),
                subtitle = "Total declared value",
                icon = Icons.Default.CurrencyRupee,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Lead Master Register Report",
        sectionTitle = "Lead Enquiry Directory",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onLeadMasterSearchChange,
        searchPlaceholder = "Search by name, code, source or phone...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Lead Status" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val priorities = updated.find { it.title == "Priority" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyLeadMasterFilters(statuses, priorities)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearLeadMasterFilters()
        },
        onLoadMore = { viewModel.loadLeadMasterReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Lead master report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Lead master report export started") },
        onClose = onClose,
        emptyMessage = "No lead records found."
    ) { item ->
        val (statusText, statusBg) = when (item.leadStatus.lowercase()) {
            "qualified", "converted" -> greentext to greenBg
            "lost", "junk" -> redText to redBg
            "new" -> Primary to primary_light
            else -> yellowText to yellowBg
        }

        DataCard(
            item = item,
            title = item.fullName.ifBlank { "--" },
            subtitle = "${item.leadCode}  •  ${item.enquiryType}",
            topBadgeText = item.leadStatus.ifBlank { "--" },
            topBadgeTextColor = statusText,
            topBadgeBgColor = statusBg,
            topBadgeInline = true,
            actions = listOf(MenuAction("View Details", Icons.Default.Visibility) { onViewDetail(item) }),
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Source & Priority") {
                        Text(
                            text = "${item.leadSource}  •  ${item.priorityLevel}",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Mobile", item.mobileNumber.ifBlank { "--" }),
                                ReportGridCell("Salesperson", item.salesperson.ifBlank { "--" }),
                                ReportGridCell("Branch", item.branch.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Budget Range", item.budgetRange.ifBlank { "--" }),
                                ReportGridCell("Appointment", item.appointmentStatus.ifBlank { "--" }),
                                ReportGridCell("Channel", item.preferredContactMethod.ifBlank { "--" })
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Enquiry date: ${viewModel.formatDate(item.enquiryDate)}")
                }
            }
        )
    }
}

@Composable
fun LeadSourcePerformanceReportScreen(
    onClose: () -> Unit,
    onViewDetail: (LeadSourcePerformanceItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.leadSourceSummary.collectAsState()
    val pagingState by viewModel.leadSourcePaging.collectAsState()
    val filteredList by viewModel.filteredLeadSourceList.collectAsState()
    val searchQuery by viewModel.leadSourceSearch.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadLeadSourcePerformanceReport() }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "LEAD CHANNELS",
                value = (summary?.totalSourcesCount ?: 0).toString(),
                subtitle = "Active acquisition sources",
                icon = Icons.Default.Campaign,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "TOTAL LEADS",
                value = (summary?.totalAllLeads ?: 0).toString(),
                subtitle = "Across all channels",
                icon = Icons.Default.Group,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "CONVERTED",
                value = (summary?.totalAllConverted ?: 0).toString(),
                subtitle = "Won opportunities",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "OVERALL CONV. RATE",
                value = summary?.overallConversionRate ?: "0%",
                subtitle = "Channel efficiency",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Lead Source Performance & ROI Report",
        sectionTitle = "Source Conversion Breakdown",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onLeadSourceSearchChange,
        searchPlaceholder = "Search by source channel...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.sourceName },
        filterSections = emptyList(),
        activeFilterCount = 0,
        onApplyFilters = {},
        onClearAllFilters = { viewModel.onLeadSourceSearchChange("") },
        onLoadMore = {},
        onShareClick = { DynamicIslandManager.showSuccess("Lead source report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Lead source report export started") },
        onClose = onClose,
        emptyMessage = "No lead source metrics found."
    ) { item ->
        DataCard(
            item = item,
            title = item.sourceName.ifBlank { "--" },
            subtitle = "${item.totalLeads} total leads captured",
            topBadgeText = item.conversionRate,
            topBadgeTextColor = greentext,
            topBadgeBgColor = greenBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Total Pipeline Budget") {
                        Text(
                            text = viewModel.formatCurrency(item.totalBudget),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Total Leads", item.totalLeads.toString()),
                                ReportGridCell("Active", item.activeLeads.toString()),
                                ReportGridCell("Converted", item.convertedLeads.toString(), valueColor = greentext)
                            ),
                            listOf(
                                ReportGridCell("Lost", item.lostLeads.toString(), valueColor = redText),
                                ReportGridCell("Avg Budget", viewModel.formatCurrency(item.avgBudget)),
                                ReportGridCell("Conv. Rate", item.conversionRate)
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Acquisition Channel: ${item.sourceName}")
                }
            }
        )
    }
}

@Composable
fun LeadOverdueFollowupsReportScreen(
    onClose: () -> Unit,
    onViewDetail: (LeadOverdueFollowupItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.leadOverdueSummary.collectAsState()
    val pagingState by viewModel.leadOverduePaging.collectAsState()
    val filteredList by viewModel.filteredLeadOverdueList.collectAsState()
    val searchQuery by viewModel.leadOverdueSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Priority",
                    options = listOf(
                        FilterOption("Urgent", "Urgent"),
                        FilterOption("High", "High"),
                        FilterOption("Medium", "Medium"),
                        FilterOption("Low", "Low")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) { filterSections.sumOf { s -> s.options.count { it.isSelected } } }

    LaunchedEffect(Unit) { viewModel.loadLeadOverdueReport(isLoadMore = false) }

    val overdueCount = summary?.totalOverdueCount ?: 0
    val urgentCount = summary?.urgentPriorityCount ?: 0
    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL OVERDUE",
                value = overdueCount.toString(),
                subtitle = "Missed follow-ups",
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "URGENT PRIORITY",
                value = urgentCount.toString(),
                subtitle = "Immediate action",
                icon = Icons.Default.PhoneInTalk,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "STANDARD OVERDUE",
                value = (overdueCount - urgentCount).coerceAtLeast(0).toString(),
                subtitle = "Regular priority",
                icon = Icons.Default.Group,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "SLA HEALTH",
                value = if (overdueCount == 0) "Clear" else "Delayed",
                subtitle = "Follow-up adherence",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = if (overdueCount == 0) greentext else redText,
                iconTint = if (overdueCount == 0) greentext else redText,
                iconBgColor = if (overdueCount == 0) greenBg else redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Overdue Follow-ups Audit Report",
        sectionTitle = "Overdue Lead Follow-up Queue",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onLeadOverdueSearchChange,
        searchPlaceholder = "Search by lead name or code...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val priorities = updated.find { it.title == "Priority" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyLeadOverdueFilters(priorities)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearLeadOverdueFilters()
        },
        onLoadMore = { viewModel.loadLeadOverdueReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Overdue follow-ups report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Overdue follow-ups report export started") },
        onClose = onClose,
        emptyMessage = "No overdue follow-ups found."
    ) { item ->
        DataCard(
            item = item,
            title = item.fullName.ifBlank { "--" },
            subtitle = "${item.leadCode}  •  ${item.leadSource}",
            topBadgeText = "${item.daysOverdue}d Overdue",
            topBadgeTextColor = redText,
            topBadgeBgColor = redBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Mobile", item.mobileNumber.ifBlank { "--" }),
                                ReportGridCell("Priority", item.priorityLevel.ifBlank { "--" }),
                                ReportGridCell("Salesperson", item.salesperson.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Status", item.leadStatus.ifBlank { "--" }),
                                ReportGridCell("Enquiry", item.enquiryType.ifBlank { "--" }),
                                ReportGridCell("Due Date", viewModel.formatDate(item.nextFollowUpDate))
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Branch: ${item.branch.ifBlank { "--" }}")
                }
            }
        )
    }
}

@Composable
fun LeadFollowupActivityReportScreen(
    onClose: () -> Unit,
    onViewDetail: (LeadFollowupActivityItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.leadActivitySummary.collectAsState()
    val pagingState by viewModel.leadActivityPaging.collectAsState()
    val filteredList by viewModel.filteredLeadActivityList.collectAsState()
    val searchQuery by viewModel.leadActivitySearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Channel",
                    options = listOf(
                        FilterOption("Call", "Call"),
                        FilterOption("Whatsapp", "WhatsApp"),
                        FilterOption("Appointment", "Appointment"),
                        FilterOption("Email", "Email")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) { filterSections.sumOf { s -> s.options.count { it.isSelected } } }

    LaunchedEffect(Unit) { viewModel.loadLeadActivityReport(isLoadMore = false) }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL ACTIVITIES",
                value = (summary?.totalActivities ?: 0).toString(),
                subtitle = "Logged interactions",
                icon = Icons.Default.EventAvailable,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "CALLS / WHATSAPP",
                value = "${summary?.callsCount ?: 0} / ${summary?.whatsAppCount ?: 0}",
                subtitle = "${summary?.emailsCount ?: 0} emails sent",
                icon = Icons.Default.PhoneInTalk,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "APPOINTMENTS",
                value = (summary?.appointmentsCount ?: 0).toString(),
                subtitle = "Scheduled visits",
                icon = Icons.Default.EventAvailable,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "QUALIFIED",
                value = (summary?.qualifiedCount ?: 0).toString(),
                subtitle = "Progressed leads",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Follow-up Activity Register Report",
        sectionTitle = "Interaction & Activity Log",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onLeadActivitySearchChange,
        searchPlaceholder = "Search by lead, code or channel...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val channels = updated.find { it.title == "Channel" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyLeadActivityFilters(channels)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearLeadActivityFilters()
        },
        onLoadMore = { viewModel.loadLeadActivityReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Follow-up activity report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Follow-up activity report export started") },
        onClose = onClose,
        emptyMessage = "No follow-up activities recorded."
    ) { item ->
        DataCard(
            item = item,
            title = item.fullName.ifBlank { "--" },
            subtitle = "${item.leadCode}  •  ${item.channel}",
            topBadgeText = item.activityType.ifBlank { "--" },
            topBadgeTextColor = Primary,
            topBadgeBgColor = primary_light,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Channel", item.channel.ifBlank { "--" }),
                                ReportGridCell("Outcome", item.outcome.ifBlank { "--" }),
                                ReportGridCell("Salesperson", item.salesperson.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Lead Status", item.leadStatus.ifBlank { "--" }),
                                ReportGridCell("Notes", item.notes.ifBlank { "--" }),
                                ReportGridCell("Date", viewModel.formatDate(item.createdAt))
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Logged on ${viewModel.formatDate(item.createdAt)}")
                }
            }
        )
    }
}