@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.logistics.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ContentCut
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
import com.cuso.tailor.model.logistics.ReturnsRtoItemDto
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
fun ReturnsRtoReportScreen(
    onClose: () -> Unit,
    onViewDetail: (ReturnsRtoItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.returnsRtoSummary.collectAsState()
    val pagingState by viewModel.returnsRtoPaging.collectAsState()
    val filteredList by viewModel.filteredReturnsRtoList.collectAsState()
    val searchQuery by viewModel.returnsRtoSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Category",
                    options = listOf(
                        FilterOption("Alteration", "Alteration"),
                        FilterOption("Damaged", "Damaged"),
                        FilterOption("RTO", "RTO")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Resolution Status",
                    options = listOf(
                        FilterOption("Pending Review", "Pending Review"),
                        FilterOption("In Progress", "In Progress"),
                        FilterOption("Completed", "Completed")
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
        viewModel.loadReturnsRtoReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL RETURN REQUESTS",
                value = (summary?.totalReturns ?: 0).toString(),
                subtitle = "All reverse dispatches",
                icon = Icons.AutoMirrored.Filled.Undo,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "BESPOKE ALTERATIONS",
                value = (summary?.alterationsCount ?: 0).toString(),
                subtitle = "Refit & resizing requests",
                subtitleColor = purpleText,
                icon = Icons.Default.ContentCut,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "DAMAGED PARCELS",
                value = (summary?.damagedCount ?: 0).toString(),
                subtitle = "Transit or fabric damages",
                subtitleColor = redText,
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "RESOLUTION RATE",
                value = summary?.resolutionRate ?: "0%",
                subtitle = "${summary?.completedCount ?: 0} returns resolved",
                subtitleColor = greentext,
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Returns / RTO Report",
        subtitle = null,
        sectionTitle = "Returns & Reverse Logistics Ledger",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onReturnsRtoSearchChange,
        searchPlaceholder = "Search by return code, order or customer...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val categories = updated.find { it.title == "Category" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val statuses = updated.find { it.title == "Resolution Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyReturnsRtoFilters(categories, statuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearReturnsRtoFilters()
        },
        onLoadMore = { viewModel.loadReturnsRtoReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Returns / RTO report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Returns / RTO report export started") },
        onClose = onClose,
        emptyMessage = "No return or RTO records found."
    ) { item ->
        val badgeTextColor = when {
            item.statusBadge.equals("Completed", ignoreCase = true) -> greentext
            item.statusBadge.contains("Pending", ignoreCase = true) -> yellowText
            else -> Primary
        }
        val badgeBgColor = when {
            item.statusBadge.equals("Completed", ignoreCase = true) -> greenBg
            item.statusBadge.contains("Pending", ignoreCase = true) -> yellowBg
            else -> primary_light
        }

        DataCard(
            item = item,
            title = item.customerName.ifBlank { "--" },
            subtitle = "${item.returnNumber.ifBlank { "--" }}  •  Order: ${item.orderCode.ifBlank { "--" }}",
            topBadgeText = item.statusBadge.ifBlank { "Pending Review" },
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
                    ReportInlineMetricRow(label = "Garment / Item") {
                        Text(
                            text = item.garmentName.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Category", item.category.ifBlank { "--" }),
                                ReportGridCell("Priority", item.priority.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Logged Date", viewModel.formatDate(item.createdAt)),
                                ReportGridCell("Issue / Refit Reason", item.reason.ifBlank { "--" })
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