
@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.marketing.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.report.CampaignPerformanceItemDto
import com.cuso.tailor.model.report.ChannelPerformanceItemDto
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
import com.cuso.tailor.view.home.reports.ReportDetailGridBox
import com.cuso.tailor.view.home.reports.ReportGridCell
import com.cuso.tailor.view.home.reports.ReportInfinitelyPaginatedScaffold
import com.cuso.tailor.view.home.reports.ReportInlineMetricRow
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

// =============================================================================
// 1. CAMPAIGN PERFORMANCE & ROI REPORT
// =============================================================================

@Composable
fun CampaignPerformanceReportScreen(
    onClose: () -> Unit,
    onViewDetail: (CampaignPerformanceItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.campaignPerfSummary.collectAsState()
    val pagingState by viewModel.campaignPerfPaging.collectAsState()
    val filteredList by viewModel.filteredCampaignPerfList.collectAsState()
    val searchQuery by viewModel.campaignPerfSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Status",
                    options = listOf(
                        FilterOption("Active", "Active"),
                        FilterOption("Paused", "Paused"),
                        FilterOption("Completed", "Completed")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Platform",
                    options = listOf(
                        FilterOption("Meta", "Meta"),
                        FilterOption("Google", "Google"),
                        FilterOption("Instagram", "Instagram")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { s -> s.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCampaignPerformanceReport(isLoadMore = false)
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL CAMPAIGNS",
                value = (summary?.totalCampaigns ?: 0).toString(),
                subtitle = "Active marketing initiatives",
                icon = Icons.Default.Share,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "TOTAL AD SPEND",
                value = viewModel.formatCurrency(summary?.totalActualSpend ?: 0.0),
                subtitle = "Budget: ${viewModel.formatCurrency(summary?.totalBudgetAllocated ?: 0.0)}",
                icon = Icons.Default.CurrencyRupee,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "ATTRIBUTED REVENUE",
                value = viewModel.formatCurrency(summary?.totalAttributedRevenue ?: 0.0),
                subtitle = "${summary?.totalConversionsCount ?: 0} orders generated",
                icon = Icons.Default.Inventory2,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "OVERALL CAMPAIGN ROI",
                value = summary?.overallRoi?.ifBlank { "0%" } ?: "0%",
                subtitle = "${summary?.totalClicksCount ?: 0} ad clicks tracked",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Campaign Performance & ROI Report",
        sectionTitle = "Campaign ROI Ledger",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onCampaignPerfSearchChange,
        searchPlaceholder = "Search by campaign name, code or platform...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id.ifBlank { it.campaignCode } },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Status" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val platforms = updated.find { it.title == "Platform" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyCampaignPerfFilters(statuses, platforms)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearCampaignPerfFilters()
        },
        onLoadMore = { viewModel.loadCampaignPerformanceReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Campaign ROI report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Campaign ROI report export started") },
        onClose = onClose,
        emptyMessage = "No campaign performance records found."
    ) { item ->
        val isActive = item.status.equals("active", ignoreCase = true)
        val isNegativeRoi = item.roiValue < 0 || item.roiPercent.startsWith("-")

        DataCard(
            item = item,
            title = item.campaignName.ifBlank { "--" },
            subtitle = item.campaignCode.ifBlank { "--" },
            topBadgeText = viewModel.formatStatusLabel(item.status),
            topBadgeTextColor = if (isActive) greentext else Primary,
            topBadgeBgColor = if (isActive) greenBg else primary_light,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Platform:") {
                        Text(
                            text = item.platform.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportInlineMetricRow(label = "Channel:") {
                        Text(
                            text = item.channel.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Budget (\u20B9)", viewModel.formatCurrency(item.budget)),
                                ReportGridCell("Spend (\u20B9)", viewModel.formatCurrency(item.actualSpend)),
                                ReportGridCell("Revenue (\u20B9)", viewModel.formatCurrency(item.attributedRevenue))
                            ),
                            listOf(
                                ReportGridCell("ROAS", item.roas.ifBlank { "0x" }),
                                ReportGridCell(
                                    label = "ROI %",
                                    value = item.roiPercent.ifBlank { "0%" },
                                    valueColor = if (isNegativeRoi) redText else greentext
                                ),
                                ReportGridCell("Orders / Clicks", "${item.conversionsCount} / ${item.clicksCount}")
                            )
                        )
                    )
                }
            }
        )
    }
}

// =============================================================================
// 2. MARKETING CHANNEL PERFORMANCE REPORT
// =============================================================================

@Composable
fun ChannelPerformanceReportScreen(
    onClose: () -> Unit,
    onViewDetail: (ChannelPerformanceItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.channelPerfSummary.collectAsState()
    val pagingState by viewModel.channelPerfPaging.collectAsState()
    val filteredList by viewModel.filteredChannelPerfList.collectAsState()
    val searchQuery by viewModel.channelPerfSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Status",
                    options = listOf(
                        FilterOption("Active", "Active"),
                        FilterOption("Inactive", "Inactive")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { s -> s.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) {
        viewModel.loadChannelPerformanceReport(isLoadMore = false)
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL CHANNELS",
                value = (summary?.totalChannels ?: 0).toString(),
                subtitle = "Active acquisition sources",
                icon = Icons.Default.Hub,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "ACQUIRED CUSTOMERS",
                value = (summary?.totalAcquiredCustomers ?: 0).toString(),
                subtitle = "Attributed signups",
                icon = Icons.Default.Group,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "TOTAL ORDERS",
                value = (summary?.totalOrders ?: 0).toString(),
                subtitle = "Channel conversions",
                icon = Icons.Default.ShoppingBag,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "CHANNEL REVENUE",
                value = viewModel.formatCurrency(summary?.totalChannelRevenue ?: 0.0),
                subtitle = "Gross attributed sales",
                icon = Icons.Default.CurrencyRupee,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Marketing Channel Performance Report",
        sectionTitle = "Channel Attribution Ledger",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onChannelPerfSearchChange,
        searchPlaceholder = "Search by channel name, code or type...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id.ifBlank { it.channelCode.ifBlank { it.channelName } } },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Status" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyChannelPerfFilters(statuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearChannelPerfFilters()
        },
        onLoadMore = { viewModel.loadChannelPerformanceReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Channel performance report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Channel performance report export started") },
        onClose = onClose,
        emptyMessage = "No channel performance records found."
    ) { item ->
        val isActive = item.status.equals("active", ignoreCase = true)

        DataCard(
            item = item,
            title = item.channelName.ifBlank { "--" },
            subtitle = "${item.channelCode.ifBlank { "--" }}  \u2022  ${item.channelType.ifBlank { "--" }}",
            topBadgeText = viewModel.formatStatusLabel(item.status),
            topBadgeTextColor = if (isActive) greentext else redText,
            topBadgeBgColor = if (isActive) greenBg else redBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Channel Revenue") {
                        Text(
                            text = viewModel.formatCurrency(item.channelRevenue),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = greentext
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Customers", item.acquiredCustomers.toString()),
                                ReportGridCell("Orders", item.totalOrders.toString()),
                                ReportGridCell("Conv. Rate", item.conversionRate.ifBlank { "0%" })
                            ),
                            listOf(
                                ReportGridCell("Avg Order Value", viewModel.formatCurrency(item.averageOrderValue)),
                                ReportGridCell("Revenue Share", item.revenueSharePercent.ifBlank { "0%" })
                            )
                        )
                    )
                }
            }
        )
    }
}