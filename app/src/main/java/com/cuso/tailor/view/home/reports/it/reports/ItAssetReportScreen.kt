@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.it.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.PersonOutline
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
import com.cuso.tailor.model.report.ItAssetReportItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.badgeGrey
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.mutedText
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
fun ItAssetReportScreen(
    onClose: () -> Unit,
    onViewDetail: (ItAssetReportItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.itAssetSummary.collectAsState()
    val pagingState by viewModel.itAssetsPagingState.collectAsState()
    val filteredList by viewModel.filteredItAssetsList.collectAsState()
    val searchQuery by viewModel.itAssetSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Asset Status",
                    options = listOf(
                        FilterOption("Available", "Available"),
                        FilterOption("Assigned", "Assigned"),
                        FilterOption("Maintenance", "Maintenance"),
                        FilterOption("Retired", "Retired")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Category",
                    options = listOf(
                        FilterOption("Laptop", "Laptop"),
                        FilterOption("Desktop", "Desktop"),
                        FilterOption("Printer", "Printer"),
                        FilterOption("Networking", "Networking"),
                        FilterOption("Software", "Software")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Warranty",
                    options = listOf(
                        FilterOption("expired_only", "Warranty Expired Only")
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
        viewModel.loadItAssetReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL ACTIVE ASSETS",
                value = (summary?.totalAssetsCount ?: 0).toString(),
                subtitle = "${summary?.availableAssetsCount ?: 0} available in pool",
                icon = Icons.Default.Computer,
                valueColor = TextPrimary,
                subtitleColor = mutedText,
                iconTint = mutedText,
                iconBgColor = badgeGrey
            ),
            ReportMetricSpec(
                label = "ASSET VALUATION",
                value = viewModel.formatCurrency(summary?.totalAssetValuation ?: 0.0),
                subtitle = "Total investment cost",
                icon = Icons.Default.CurrencyRupee,
                valueColor = greentext,
                subtitleColor = mutedText,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "ASSIGNED TO STAFF",
                value = (summary?.assignedAssetsCount ?: 0).toString(),
                subtitle = "In active deployment",
                icon = Icons.Default.PersonOutline,
                valueColor = purpleText,
                subtitleColor = mutedText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "WARRANTY EXPIRED",
                value = (summary?.warrantyExpiredCount ?: 0).toString(),
                subtitle = "${summary?.maintenanceDisposalCount ?: 0} maintenance/disposal",
                icon = Icons.Default.WarningAmber,
                valueColor = yellowText,
                subtitleColor = mutedText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "IT Asset Report",
        subtitle = "HARDWARE & LICENSE AUDIT",
        sectionTitle = "IT Asset & Hardware Directory",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onItAssetSearchQueryChange,
        searchPlaceholder = "Search by asset ID, name, category or vendor...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Asset Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val categories = updated.find { it.title == "Category" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val expiredOnly = updated.find { it.title == "Warranty" }
                ?.options?.any { it.id == "expired_only" && it.isSelected } ?: false
            viewModel.applyItAssetFilters(statuses, categories, expiredOnly)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearItAssetFilters()
        },
        onLoadMore = viewModel::loadMoreItAssets,
        onShareClick = { DynamicIslandManager.showSuccess("IT asset report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("IT asset report export started") },
        onClose = onClose,
        emptyMessage = "No IT assets found."
    ) { item ->
        val (statusTextColor, statusBgColor) = when (item.statusBadge.lowercase()) {
            "available", "active" -> greentext to greenBg
            "assigned" -> Primary to primary_light
            "maintenance", "disposal" -> yellowText to yellowBg
            else -> redText to redBg
        }

        DataCard(
            item = item,
            title = item.assetId.ifBlank { "--" },
            subtitle = "${item.assetName}  •  ${item.makeModel}",
            topBadgeText = item.statusBadge.ifBlank { "--" },
            topBadgeTextColor = statusTextColor,
            topBadgeBgColor = statusBgColor,
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
                    ReportInlineMetricRow(label = "Asset Value") {
                        Text(
                            text = viewModel.formatCurrency(item.cost),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("ASSET NAME", item.assetName.ifBlank { "--" }),
                                ReportGridCell("TYPE", item.type.ifBlank { "--" }),
                                ReportGridCell("CATEGORY", item.category.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("CONDITION", item.condition.ifBlank { "--" }),
                                ReportGridCell("CURRENT CUSTODIAN", item.assignedTo.ifBlank { "--" }),
                                ReportGridCell("LOCATION / DEPT", item.location.ifBlank { "--" })
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Warranty: ${viewModel.formatDate(item.warrantyUntil)}")
                }
            }
        )
    }
}