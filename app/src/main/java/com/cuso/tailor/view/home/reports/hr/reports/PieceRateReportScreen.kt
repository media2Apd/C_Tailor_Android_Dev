@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.hr.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Visibility
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
import com.cuso.tailor.model.report.PieceRateRecordDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeBg
import com.cuso.tailor.ui.theme.orangeText
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
fun PieceRateReportScreen(
    onClose: () -> Unit,
    onViewDetail: (PieceRateRecordDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val analytics by viewModel.pieceRateAnalytics.collectAsState()
    val pagingState by viewModel.pieceRatePagingState.collectAsState()
    val filteredList by viewModel.filteredPieceRateList.collectAsState()
    val searchQuery by viewModel.pieceRateSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Tariff Status",
                    options = listOf(
                        FilterOption("Active", "Active"),
                        FilterOption("Scheduled", "Scheduled"),
                        FilterOption("Expired", "Expired"),
                        FilterOption("Inactive", "Inactive")
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
        viewModel.loadPieceRateReport()
    }

    val summary = analytics?.pieceRateSummary
    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL PIECE RATES",
                value = (summary?.totalPieceRates ?: 0).toString(),
                subtitle = "Configured operations",
                icon = Icons.Default.ContentCut,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "ACTIVE TARIFFS",
                value = (summary?.activeCount ?: 0).toString(),
                subtitle = "In current workshop use",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "AVG REGULAR PIECE RATE",
                value = viewModel.formatCurrency(summary?.averageRegularRate ?: 0.0),
                subtitle = "Per operation unit",
                icon = Icons.Default.AttachMoney,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "PEAK OPERATION RATE",
                value = viewModel.formatCurrency(summary?.highestRegularRate ?: 0.0),
                subtitle = "Highest craft tariff",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                valueColor = orangeText,
                iconTint = orangeText,
                iconBgColor = orangeBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Piece Rate Master & Wage Report",
        subtitle = "Tariff Register",
        sectionTitle = "Piece Rate Tariff Schedule",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onPieceRateSearchQueryChange,
        searchPlaceholder = "Search by operation, garment or creator...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Tariff Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyPieceRateFilters(statuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearPieceRateFilters()
        },
        onLoadMore = viewModel::loadMorePieceRates,
        onShareClick = { DynamicIslandManager.showSuccess("Piece rate report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Piece rate report export started") },
        onClose = onClose,
        emptyMessage = "No piece rate tariffs found."
    ) { item ->
        val (statusTextColor, statusBgColor) = when (item.status.lowercase()) {
            "active" -> greentext to greenBg
            "scheduled" -> yellowText to yellowBg
            "inactive" -> redText to redBg
            else -> mutedText to grey_border
        }
        val effectiveDate = viewModel.formatDate(item.effectiveFrom)

        DataCard(
            item = item,
            title = item.workflowName,
            subtitle = "${item.garmentName}  •  ${item.garmentCategoryName}",
            topBadgeText = item.status,
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
                    ReportInlineMetricRow(label = "Regular Rate / Piece") {
                        Text(
                            text = viewModel.formatCurrency(item.regularRatePerPiece),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Operation", item.workflowName.ifBlank { "--" }),
                                ReportGridCell("Garment Type", item.garmentName.ifBlank { "--" }),
                                ReportGridCell("OT Rate / Pc", viewModel.formatCurrency(item.otRatePerPiece))
                            ),
                            listOf(
                                ReportGridCell("Effective Date", effectiveDate),
                                ReportGridCell("Category", item.garmentCategoryName.ifBlank { "--" }),
                                ReportGridCell("Approved By", item.createdBy.ifBlank { "--" })
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Effective from $effectiveDate")
                }
            }
        )
    }
}