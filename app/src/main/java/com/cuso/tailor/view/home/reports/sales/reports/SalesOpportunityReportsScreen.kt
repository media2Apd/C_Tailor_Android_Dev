@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.sales.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WorkOutline
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
import com.cuso.tailor.model.report.OpportunityClosingAlertItemDto
import com.cuso.tailor.model.report.OpportunityMasterRegisterItemDto
import com.cuso.tailor.model.report.OpportunityStageFunnelItemDto
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
fun OpportunityMasterRegisterReportScreen(
    onClose: () -> Unit,
    onViewDetail: (OpportunityMasterRegisterItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.oppMasterSummary.collectAsState()
    val pagingState by viewModel.oppMasterPaging.collectAsState()
    val filteredList by viewModel.filteredOppMasterList.collectAsState()
    val searchQuery by viewModel.oppMasterSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Deal Status",
                    options = listOf(
                        FilterOption("Active", "Active"),
                        FilterOption("Won", "Won"),
                        FilterOption("Lost", "Lost")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) { filterSections.sumOf { s -> s.options.count { it.isSelected } } }

    LaunchedEffect(Unit) { viewModel.loadOppMasterReport(isLoadMore = false) }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL DEALS",
                value = (summary?.totalDealsCount ?: 0).toString(),
                subtitle = "${summary?.activeDealsCount ?: 0} active deals",
                icon = Icons.Default.WorkOutline,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "PIPELINE VALUE",
                value = viewModel.formatCurrency(summary?.totalPipelineValue ?: 0.0),
                subtitle = "Weighted: ${viewModel.formatCurrency(summary?.weightedForecastValue ?: 0.0)}",
                icon = Icons.Default.AttachMoney,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "WON DEALS",
                value = (summary?.wonDealsCount ?: 0).toString(),
                subtitle = "${viewModel.formatCurrency(summary?.wonDealsValue ?: 0.0)} won",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                subtitleColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "WIN RATE",
                value = summary?.winRate ?: "0%",
                subtitle = "${summary?.closingIn7DaysCount ?: 0} closing in 7d",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Opportunity Pipeline Master Register",
        sectionTitle = "Opportunity Deal Directory",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onOppMasterSearchChange,
        searchPlaceholder = "Search by code, deal name, client or stage...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Deal Status" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyOppMasterFilters(statuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearOppMasterFilters()
        },
        onLoadMore = { viewModel.loadOppMasterReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Opportunity register ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Opportunity register export started") },
        onClose = onClose,
        emptyMessage = "No opportunities found."
    ) { item ->
        val (statusText, statusBg) = when (item.status.lowercase()) {
            "won", "active" -> greentext to greenBg
            "lost" -> redText to redBg
            else -> yellowText to yellowBg
        }

        DataCard(
            item = item,
            title = item.name.ifBlank { item.opportunityCode },
            subtitle = "${item.opportunityCode}  •  ${item.clientName}",
            topBadgeText = item.status.ifBlank { "--" },
            topBadgeTextColor = statusText,
            topBadgeBgColor = statusBg,
            topBadgeInline = true,
            actions = listOf(MenuAction("View Details", Icons.Default.Visibility) { onViewDetail(item) }),
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Estimated Value") {
                        Text(
                            text = viewModel.formatCurrency(item.estimatedValue),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Stage", item.stageName.ifBlank { "--" }),
                                ReportGridCell("Probability", item.probability.ifBlank { "--" }),
                                ReportGridCell("Salesperson", item.salesperson.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Client Phone", item.clientPhone.ifBlank { "--" }),
                                ReportGridCell("Expected Close", viewModel.formatDate(item.expectedClosingDate)),
                                ReportGridCell("Sales Order", item.salesOrderCode ?: "--")
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Created on ${viewModel.formatDate(item.createdAt)}")
                }
            }
        )
    }
}

@Composable
fun OpportunityStageFunnelReportScreen(
    onClose: () -> Unit,
    onViewDetail: (OpportunityStageFunnelItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.oppFunnelSummary.collectAsState()
    val pagingState by viewModel.oppFunnelPaging.collectAsState()
    val filteredList by viewModel.filteredOppFunnelList.collectAsState()
    val searchQuery by viewModel.oppFunnelSearch.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadOppStageFunnelReport() }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "PIPELINE STAGES",
                value = (summary?.totalStagesCount ?: 0).toString(),
                subtitle = "Configured funnel steps",
                icon = Icons.Default.FilterList,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "TOTAL DEALS",
                value = (summary?.totalDeals ?: 0).toString(),
                subtitle = "Active in funnel",
                icon = Icons.Default.WorkOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "PIPELINE VALUE",
                value = viewModel.formatCurrency(summary?.totalPipelineValue ?: 0.0),
                subtitle = "Gross stage volume",
                icon = Icons.Default.AttachMoney,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "WEIGHTED FORECAST",
                value = viewModel.formatCurrency(summary?.totalWeightedValue ?: 0.0),
                subtitle = "Probability adjusted",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Opportunity Stage Funnel & Forecast",
        sectionTitle = "Funnel Stage Breakdown",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onOppFunnelSearchChange,
        searchPlaceholder = "Search by stage name or code...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = emptyList(),
        activeFilterCount = 0,
        onApplyFilters = {},
        onClearAllFilters = { viewModel.onOppFunnelSearchChange("") },
        onLoadMore = {},
        onShareClick = { DynamicIslandManager.showSuccess("Stage funnel report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Stage funnel report export started") },
        onClose = onClose,
        emptyMessage = "No stage funnel data found."
    ) { item ->
        DataCard(
            item = item,
            title = item.stageName.ifBlank { "--" },
            subtitle = "${item.stageCode}  •  Order #${item.displayOrder}",
            topBadgeText = item.percentOfPipeline,
            topBadgeTextColor = Primary,
            topBadgeBgColor = primary_light,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Stage Total Value") {
                        Text(
                            text = viewModel.formatCurrency(item.totalStageValue),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Deals Count", item.dealsCount.toString()),
                                ReportGridCell("Converted", item.convertedCount.toString()),
                                ReportGridCell("Probability", "${item.probability}%")
                            ),
                            listOf(
                                ReportGridCell("Stage Value", viewModel.formatCurrency(item.totalStageValue)),
                                ReportGridCell("Weighted", viewModel.formatCurrency(item.weightedValue)),
                                ReportGridCell("Pipeline Share", item.percentOfPipeline)
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Stage Code: ${item.stageCode}")
                }
            }
        )
    }
}

@Composable
fun OpportunityClosingAlertsReportScreen(
    onClose: () -> Unit,
    onViewDetail: (OpportunityClosingAlertItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.oppAlertsSummary.collectAsState()
    val pagingState by viewModel.oppAlertsPaging.collectAsState()
    val filteredList by viewModel.filteredOppAlertsList.collectAsState()
    val searchQuery by viewModel.oppAlertsSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Urgency",
                    options = listOf(FilterOption("overdue_only", "Overdue Deals Only")),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) { filterSections.sumOf { s -> s.options.count { it.isSelected } } }

    LaunchedEffect(Unit) { viewModel.loadOppClosingAlertsReport(isLoadMore = false) }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "ALERT DEALS",
                value = (summary?.totalAlertDeals ?: 0).toString(),
                subtitle = "Requiring attention",
                icon = Icons.Default.WarningAmber,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "AT-RISK VALUE",
                value = viewModel.formatCurrency(summary?.atRiskValue ?: 0.0),
                subtitle = "Deal value exposed",
                icon = Icons.Default.AttachMoney,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "OVERDUE DEALS",
                value = (summary?.overdueDealsCount ?: 0).toString(),
                subtitle = "Past expected close",
                icon = Icons.Default.Schedule,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "CLOSING SOON",
                value = (summary?.closingSoonCount ?: 0).toString(),
                subtitle = "Due within 7 days",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Closing Soon & Overdue Deals Report",
        sectionTitle = "Urgent Deal Watchlist",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onOppAlertsSearchChange,
        searchPlaceholder = "Search by deal, code or client...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val overdueOnly = updated.find { it.title == "Urgency" }?.options?.any { it.id == "overdue_only" && it.isSelected } ?: false
            viewModel.applyOppAlertsFilters(overdueOnly)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearOppAlertsFilters()
        },
        onLoadMore = { viewModel.loadOppClosingAlertsReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Closing alerts report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Closing alerts report export started") },
        onClose = onClose,
        emptyMessage = "No closing alert deals found."
    ) { item ->
        val badgeTextColor = if (item.isOverdue) redText else yellowText
        val badgeBgColor = if (item.isOverdue) redBg else yellowBg

        DataCard(
            item = item,
            title = item.name.ifBlank { item.opportunityCode },
            subtitle = "${item.opportunityCode}  •  ${item.clientName}",
            topBadgeText = item.urgencyStatus.ifBlank { "--" },
            topBadgeTextColor = badgeTextColor,
            topBadgeBgColor = badgeBgColor,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Deal Value") {
                        Text(
                            text = viewModel.formatCurrency(item.estimatedValue),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Stage", item.stageName.ifBlank { "--" }),
                                ReportGridCell("Probability", item.probability.ifBlank { "--" }),
                                ReportGridCell("Salesperson", item.salesperson.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Client", item.clientName.ifBlank { "--" }),
                                ReportGridCell("Status", item.urgencyStatus.ifBlank { "--" }, valueColor = badgeTextColor),
                                ReportGridCell("Expected Close", viewModel.formatDate(item.expectedClosingDate))
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Target close date: ${viewModel.formatDate(item.expectedClosingDate)}")
                }
            }
        )
    }
}