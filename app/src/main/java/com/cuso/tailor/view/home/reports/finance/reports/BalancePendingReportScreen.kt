@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.finance.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
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
import com.cuso.tailor.model.finance.BalancePendingItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
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
fun BalancePendingReportScreen(
    onClose: () -> Unit,
    onViewDetail: (BalancePendingItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.balancePendingSummary.collectAsState()
    val pagingState by viewModel.balancePendingPagingState.collectAsState()
    val filteredList by viewModel.filteredBalancePendingList.collectAsState()
    val searchQuery by viewModel.balancePendingSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Invoice Status",
                    options = listOf(
                        FilterOption("Overdue", "Overdue"),
                        FilterOption("Pending", "Pending"),
                        FilterOption("Partially Paid", "Partially Paid")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Flags",
                    options = listOf(
                        FilterOption("overdue_only", "Overdue Invoices Only")
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
        viewModel.loadBalancePendingReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL PENDING",
                value = viewModel.formatCurrency(summary?.totalPendingAmount ?: 0.0),
                subtitle = "Receivable balance",
                icon = Icons.Default.AttachMoney,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "UNPAID INVOICES",
                value = (summary?.totalUnpaidInvoices ?: 0).toString(),
                subtitle = "Pending collection",
                icon = Icons.Default.Description,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Balance Pending Report",
        subtitle = null,
        sectionTitle = "Unpaid Invoices Register",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onBalancePendingSearchQueryChange,
        searchPlaceholder = "Search by invoice, customer or mobile...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.invoiceId.ifBlank { it.invoiceNumber } },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Invoice Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val overdueOnly = updated.find { it.title == "Flags" }
                ?.options?.any { it.id == "overdue_only" && it.isSelected } ?: false
            viewModel.applyBalancePendingFilters(statuses, overdueOnly)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearBalancePendingFilters()
        },
        onLoadMore = { viewModel.loadBalancePendingReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Balance pending report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Balance pending report export started") },
        onClose = onClose,
        emptyMessage = "No unpaid invoices with pending balance found."
    ) { item ->
        val isOverdue = item.daysOverdue > 0 || item.status.equals("Overdue", ignoreCase = true)
        val badgeTextColor = if (isOverdue) redText else yellowText
        val badgeBgColor = if (isOverdue) redBg else yellowBg
        val badgeLabel = item.status.ifBlank { if (isOverdue) "Overdue" else "Pending" }

        DataCard(
            item = item,
            title = item.customer.customerName.ifBlank { "--" },
            subtitle = item.invoiceNumber.ifBlank { "--" },
            topBadgeText = badgeLabel,
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
                    ReportInlineMetricRow(label = "Contact number") {
                        Text(
                            text = item.customer.mobileNumber.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Invoice Date", viewModel.formatDate(item.invoiceDate)),
                                ReportGridCell("Due Date", viewModel.formatDate(item.dueDate)),
                                ReportGridCell("Days Overdue", item.daysOverdue.toString())
                            ),
                            listOf(
                                ReportGridCell("Grand Total", viewModel.formatCurrency(item.grandTotal)),
                                ReportGridCell("Balance Due", viewModel.formatCurrency(item.balanceDue))
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Customer code: ${item.customer.customerCode.ifBlank { "--" }}")
                }
            }
        )
    }
}