
@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.finance.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ShoppingBag
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
import com.cuso.tailor.model.finance.PayablesAgingItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.activity_purple_bg
import com.cuso.tailor.ui.theme.blueText
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.orangeBg
import com.cuso.tailor.ui.theme.orangeText
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
import com.cuso.tailor.view.home.reports.ReportCardFooterCaption
import com.cuso.tailor.view.home.reports.ReportDetailGridBox
import com.cuso.tailor.view.home.reports.ReportGridCell
import com.cuso.tailor.view.home.reports.ReportInfinitelyPaginatedScaffold
import com.cuso.tailor.view.home.reports.ReportInlineMetricRow
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun PayablesAgingReportScreen(
    onClose: () -> Unit,
    onViewDetail: (PayablesAgingItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.payablesAgingSummary.collectAsState()
    val pagingState by viewModel.payablesAgingPagingState.collectAsState()
    val filteredList by viewModel.filteredPayablesAgingList.collectAsState()
    val searchQuery by viewModel.payablesAgingSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Aging Status",
                    options = listOf(
                        FilterOption("Active", "Active"),
                        FilterOption("Follow-up", "Follow-up"),
                        FilterOption("Critical", "Critical")
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
        viewModel.loadPayablesAgingReport()
    }

    val metrics = remember(summary) {
        val buckets = summary?.buckets
        listOf(
            ReportMetricSpec(
                label = "TOTAL AP",
                value = viewModel.formatCurrency(summary?.totalPayables ?: 0.0),
                subtitle = "Total Outstanding",
                icon = Icons.Default.ShoppingBag,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "CURRENT",
                value = viewModel.formatCurrency(summary?.totalCurrent ?: 0.0),
                subtitle = "Not yet due",
                icon = Icons.Default.AccessTime,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "1 - 30 DAYS",
                value = viewModel.formatCurrency(buckets?.days1To30 ?: 0.0),
                subtitle = "Slightly overdue",
                icon = Icons.Default.AccessTime,
                valueColor = blueText,
                iconTint = blueText,
                iconBgColor = activity_purple_bg
            ),
            ReportMetricSpec(
                label = "31 - 60 DAYS",
                value = viewModel.formatCurrency(buckets?.days31To60 ?: 0.0),
                subtitle = "Requires follow-up",
                icon = Icons.Default.AccessTime,
                valueColor = orangeText,
                iconTint = orangeText,
                iconBgColor = orangeBg
            ),
            ReportMetricSpec(
                label = "61 - 90 DAYS",
                value = viewModel.formatCurrency(buckets?.days61To90 ?: 0.0),
                subtitle = "High risk",
                icon = Icons.Default.WarningAmber,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "90+ DAYS",
                value = viewModel.formatCurrency(buckets?.daysOver90 ?: 0.0),
                subtitle = "Critical overdue",
                icon = Icons.Default.Cancel,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Payables Aging Report (AP Aging)",
        subtitle = "6 Overdue Buckets",
        sectionTitle = "Supplier Outstanding & Aging Buckets",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onPayablesAgingSearchQueryChange,
        searchPlaceholder = "Search by supplier name, code or mobile...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.supplierId.ifBlank { it.supplierCode } },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Aging Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyPayablesAgingFilters(statuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearPayablesAgingFilters()
        },
        onLoadMore = { viewModel.loadPayablesAgingReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Payables aging report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Payables aging report export started") },
        onClose = onClose,
        emptyMessage = "There are no outstanding aging supplier records to display."
    ) { item ->
        val total = if (item.totalOutstanding > 0.0) item.totalOutstanding else item.totalPayable
        val d1To30 = item.buckets?.days1To30 ?: item.days1To30
        val d31To60 = item.buckets?.days31To60 ?: item.days31To60
        val d61To90 = item.buckets?.days61To90 ?: item.days61To90
        val dOver90 = item.buckets?.daysOver90 ?: item.daysOver90
        val badgeLabel = item.status.ifBlank { "Active" }

        DataCard(
            item = item,
            title = item.supplierName.ifBlank { "--" },
            subtitle = "Code: ${item.supplierCode.ifBlank { "--" }}",
            topBadgeText = badgeLabel,
            topBadgeTextColor = greentext,
            topBadgeBgColor = greenBg,
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
                    ReportInlineMetricRow(label = "Total Payables") {
                        Text(
                            text = viewModel.formatCurrency(total),
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("PHONE", item.mobileNumber.ifBlank { "--" }),
                                ReportGridCell("CURRENT", viewModel.formatCurrency(item.current)),
                                ReportGridCell("1 - 30 DAYS", viewModel.formatCurrency(d1To30))
                            ),
                            listOf(
                                ReportGridCell("31 - 60 DAYS", viewModel.formatCurrency(d31To60)),
                                ReportGridCell("61 - 90 DAYS", viewModel.formatCurrency(d61To90)),
                                ReportGridCell("90+ DAYS", viewModel.formatCurrency(dOver90))
                            )
                        )
                    )

                    ReportInlineMetricRow(label = "Status") {
                        Text(
                            text = item.agingStatus.ifBlank { badgeLabel },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "As of ${viewModel.formatDate(summary?.asOfDate)}")
                }
            }
        )
    }
}