@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.sales.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.report.MeasurementMasterRegisterItemDto
import com.cuso.tailor.model.report.MeasurementStaleAuditItemDto
import com.cuso.tailor.ui.theme.Primary
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
import com.cuso.tailor.view.home.reports.ReportCardFooterCaption
import com.cuso.tailor.view.home.reports.ReportDetailGridBox
import com.cuso.tailor.view.home.reports.ReportGridCell
import com.cuso.tailor.view.home.reports.ReportInfinitelyPaginatedScaffold
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun MeasurementMasterRegisterReportScreen(
    onClose: () -> Unit,
    onViewDetail: (MeasurementMasterRegisterItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.measMasterSummary.collectAsState()
    val pagingState by viewModel.measMasterPaging.collectAsState()
    val filteredList by viewModel.filteredMeasMasterList.collectAsState()
    val searchQuery by viewModel.measMasterSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Segment",
                    options = listOf(
                        FilterOption("Men", "Men"),
                        FilterOption("Women", "Women"),
                        FilterOption("Kids", "Kids")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) { filterSections.sumOf { s -> s.options.count { it.isSelected } } }

    LaunchedEffect(Unit) { viewModel.loadMeasMasterReport(isLoadMore = false) }

    val totalCards = summary?.totalCardsCount ?: 0
    val activeCards = summary?.activeCardsCount ?: 0
    val activePercent = if (totalCards > 0) "${activeCards * 100 / totalCards}%" else "0%"

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL CARDS",
                value = totalCards.toString(),
                subtitle = "Recorded profiles",
                icon = Icons.Default.Straighten,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "ACTIVE CARDS",
                value = activeCards.toString(),
                subtitle = "Current presets",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "UNIQUE CLIENTS",
                value = (summary?.uniqueClientsCount ?: 0).toString(),
                subtitle = "Measured customers",
                icon = Icons.Default.Group,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "ACTIVE RATIO",
                value = activePercent,
                subtitle = "Usable profiles",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Customer Measurement Master Register",
        sectionTitle = "Measurement Profile Directory",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onMeasMasterSearchChange,
        searchPlaceholder = "Search by customer, code or garment...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val segments = updated.find { it.title == "Segment" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyMeasMasterFilters(segments)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearMeasMasterFilters()
        },
        onLoadMore = { viewModel.loadMeasMasterReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Measurement register ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Measurement register export started") },
        onClose = onClose,
        emptyMessage = "No measurement records found."
    ) { item ->
        val isActive = item.status.equals("active", ignoreCase = true)
        DataCard(
            item = item,
            title = item.fullName.ifBlank { "--" },
            subtitle = "${item.customerCode}  •  ${item.segmentName} / ${item.garmentName}",
            topBadgeText = item.status.ifBlank { "--" },
            topBadgeTextColor = if (isActive) greentext else redText,
            topBadgeBgColor = if (isActive) greenBg else redBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Garment", item.garmentName.ifBlank { "--" }),
                                ReportGridCell("Category", item.categoryName.ifBlank { "--" }),
                                ReportGridCell("Parameters", item.recordedFieldsCount.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Mobile", item.mobileNumber.ifBlank { "--" }),
                                ReportGridCell("Recorded By", item.recordedBy.ifBlank { "--" }),
                                ReportGridCell("Measured On", viewModel.formatDate(item.measuredAt))
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Measured on ${viewModel.formatDate(item.measuredAt)}")
                }
            }
        )
    }
}

@Composable
fun MeasurementStaleAuditReportScreen(
    onClose: () -> Unit,
    onViewDetail: (MeasurementStaleAuditItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.measStaleSummary.collectAsState()
    val pagingState by viewModel.measStalePaging.collectAsState()
    val filteredList by viewModel.filteredMeasStaleList.collectAsState()
    val searchQuery by viewModel.measStaleSearch.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMeasStaleReport(isLoadMore = false) }

    val staleCount = summary?.staleCardsCount ?: 0
    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "STALE CARDS",
                value = staleCount.toString(),
                subtitle = "Needs re-measurement",
                icon = Icons.Default.WarningAmber,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "AFFECTED CLIENTS",
                value = (summary?.uniqueClientsCount ?: 0).toString(),
                subtitle = "Outdated profiles",
                icon = Icons.Default.Group,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "STALE THRESHOLD",
                value = "${summary?.thresholdMonths ?: 6} mos",
                subtitle = "Policy window",
                icon = Icons.Default.Schedule,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "AUDIT HEALTH",
                value = if (staleCount == 0) "Up to Date" else "Review",
                subtitle = "Measurement accuracy",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = if (staleCount == 0) greentext else redText,
                iconTint = if (staleCount == 0) greentext else redText,
                iconBgColor = if (staleCount == 0) greenBg else redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Stale Measurement Audit Report",
        sectionTitle = "Outdated Measurement Cards",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onMeasStaleSearchChange,
        searchPlaceholder = "Search by customer name or code...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = emptyList(),
        activeFilterCount = 0,
        onApplyFilters = {},
        onClearAllFilters = { viewModel.onMeasStaleSearchChange("") },
        onLoadMore = { viewModel.loadMeasStaleReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Stale measurement audit ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Stale measurement audit export started") },
        onClose = onClose,
        emptyMessage = "No stale measurement profiles found."
    ) { item ->
        DataCard(
            item = item,
            title = item.fullName.ifBlank { "--" },
            subtitle = "${item.customerCode}  •  ${item.garmentName}",
            topBadgeText = "${item.monthsSinceMeasured}m Old",
            topBadgeTextColor = yellowText,
            topBadgeBgColor = yellowBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Garment", item.garmentName.ifBlank { "--" }),
                                ReportGridCell("Category", item.categoryName.ifBlank { "--" }),
                                ReportGridCell("Mobile", item.mobileNumber.ifBlank { "--" })
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Last measured: ${viewModel.formatDate(item.measuredAt)}")
                }
            }
        )
    }
}