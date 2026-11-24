@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.finance.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
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
import com.cuso.tailor.model.finance.GstRateBreakdownItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.activity_purple_bg
import com.cuso.tailor.ui.theme.blueText
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.purpleBg
import com.cuso.tailor.ui.theme.purpleText
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.PagingUiState
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
fun GstSummaryReportScreen(
    onClose: () -> Unit,
    onViewDetail: (GstRateBreakdownItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val gstData by viewModel.gstSummaryData.collectAsState()
    val isLoading by viewModel.isLoadingGstSummary.collectAsState()
    val filteredList by viewModel.filteredGstRateBreakdownList.collectAsState()
    val searchQuery by viewModel.gstRateSearchQuery.collectAsState()

    val pagingState = remember(filteredList, isLoading) {
        PagingUiState(
            items = filteredList,
            isInitialLoading = isLoading,
            isLoadingMore = false,
            canLoadMore = false
        )
    }

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Tax Type",
                    options = listOf(
                        FilterOption("CGST", "CGST"),
                        FilterOption("SGST", "SGST"),
                        FilterOption("IGST", "IGST")
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
        viewModel.loadGstSummaryReport()
    }

    val summary = gstData?.summary
    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL OUTPUT TAX",
                value = viewModel.formatCurrency(summary?.totalOutputTax ?: 0.0),
                subtitle = "Collected on sales & returns",
                icon = Icons.Default.NorthEast,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "INPUT TAX CREDIT (ITC)",
                value = viewModel.formatCurrency(summary?.totalInputTaxCredit ?: 0.0),
                subtitle = "Eligible purchase & expense tax",
                icon = Icons.Default.SouthWest,
                valueColor = blueText,
                iconTint = blueText,
                iconBgColor = activity_purple_bg
            ),
            ReportMetricSpec(
                label = "NET TAX PAYABLE",
                value = viewModel.formatCurrency(summary?.netTaxPayable ?: 0.0),
                subtitle = "Payable to tax authority",
                icon = Icons.Default.Balance,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "ITC CARRY FORWARD",
                value = viewModel.formatCurrency(summary?.itcCarryForward ?: 0.0),
                subtitle = "Excess tax credit balance",
                icon = Icons.Default.Description,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            )
        )
    }

    val periodSubtitle = gstData?.period?.let { period ->
        if (period.from.isNotBlank() && period.to.isNotBlank()) "${period.from} - ${period.to}" else null
    }

    ReportInfinitelyPaginatedScaffold(
        title = "GST / Tax Summary Report",
        subtitle = periodSubtitle,
        sectionTitle = "Rate-Wise Tax Breakdown (GSTR-3B Register)",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onGstRateSearchQueryChange,
        searchPlaceholder = "Search by tax type or rate...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { "${it.taxName}_${it.taxType}_${it.rate}" },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val taxTypes = updated.find { it.title == "Tax Type" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyGstTaxTypeFilters(taxTypes)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearGstFilters()
        },
        onLoadMore = {},
        onShareClick = { DynamicIslandManager.showSuccess("GST summary report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("GST summary report export started") },
        onClose = onClose,
        emptyMessage = "No rate-wise GST tax breakdown records found."
    ) { item ->
        val displayTitle = item.taxName.ifBlank {
            "${item.taxType.ifBlank { "GST" }} ${item.rate}%"
        }

        DataCard(
            item = item,
            title = displayTitle,
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
                    ReportInlineMetricRow(label = "Net Tax Payable") {
                        Text(
                            text = viewModel.formatCurrency(item.netTaxPayable),
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportInlineMetricRow(label = "Rate") {
                        Text(
                            text = "${item.rate}%",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Outward Taxable", viewModel.formatCurrency(item.outwardTaxable)),
                                ReportGridCell("Output Tax", viewModel.formatCurrency(item.outputTax))
                            ),
                            listOf(
                                ReportGridCell("Inward Taxable", viewModel.formatCurrency(item.inwardTaxable)),
                                ReportGridCell("Input Tax Credit (ITC)", viewModel.formatCurrency(item.inputTaxCredit))
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "GSTR-3B tax rate register")
                }
            }
        )
    }
}