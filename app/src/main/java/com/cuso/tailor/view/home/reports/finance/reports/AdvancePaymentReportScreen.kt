@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.finance.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MonetizationOn
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
import com.cuso.tailor.model.finance.AdvancePaymentItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.purpleBg
import com.cuso.tailor.ui.theme.purpleText
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
fun AdvancePaymentReportScreen(
    onClose: () -> Unit,
    onViewDetail: (AdvancePaymentItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.advancePaymentSummary.collectAsState()
    val pagingState by viewModel.advancePaymentPagingState.collectAsState()
    val filteredList by viewModel.filteredAdvancePaymentList.collectAsState()
    val searchQuery by viewModel.advancePaymentSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Payment Mode",
                    options = listOf(
                        FilterOption("Cash", "Cash"),
                        FilterOption("UPI", "UPI"),
                        FilterOption("Card", "Card"),
                        FilterOption("Bank Transfer", "Bank Transfer")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Flags",
                    options = listOf(
                        FilterOption("unallocated_only", "Unallocated Balance Only")
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
        viewModel.loadAdvancePaymentReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL ADVANCE COLLECTED",
                value = viewModel.formatCurrency(summary?.totalAdvanceCollected ?: 0.0),
                subtitle = "Total customer advances received",
                icon = Icons.Default.AccountBalanceWallet,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "UNALLOCATED",
                value = viewModel.formatCurrency(summary?.totalUnallocatedBalance ?: 0.0),
                subtitle = "Pending adjustment",
                icon = Icons.Default.ErrorOutline,
                valueColor = TextPrimary,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "TRANSACTIONS",
                value = (summary?.totalTransactions ?: 0).toString(),
                subtitle = "Advance receipts count",
                icon = Icons.Default.MonetizationOn,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Advance Payment Report",
        subtitle = null,
        sectionTitle = "Customer Advance Register",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onAdvancePaymentSearchQueryChange,
        searchPlaceholder = "Search by receipt, order or customer...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.paymentId },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val modes = updated.find { it.title == "Payment Mode" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val unallocatedOnly = updated.find { it.title == "Flags" }
                ?.options?.any { it.id == "unallocated_only" && it.isSelected } ?: false
            viewModel.applyAdvancePaymentFilters(modes, unallocatedOnly)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearAdvancePaymentFilters()
        },
        onLoadMore = { viewModel.loadAdvancePaymentReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Advance payment report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Advance payment report export started") },
        onClose = onClose,
        emptyMessage = "No advance payment receipts found."
    ) { item ->
        val isCompleted = item.status.equals("Completed", ignoreCase = true)
        val badgeTextColor = if (isCompleted) greentext else yellowText
        val badgeBgColor = if (isCompleted) greenBg else yellowBg

        DataCard(
            item = item,
            title = item.customer.customerName.ifBlank { "--" },
            subtitle = item.paymentNumber.ifBlank { "--" },
            topBadgeText = item.status.ifBlank { "Completed" },
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
                                ReportGridCell("Linked Order", item.linkedOrder?.orderCode?.ifBlank { "--" } ?: "--"),
                                ReportGridCell("Payment Date", viewModel.formatDate(item.paymentDate)),
                                ReportGridCell("Mode", item.paymentMode.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Advance Amt", viewModel.formatCurrency(item.amount)),
                                ReportGridCell("Unallocated Balance", viewModel.formatCurrency(item.unallocatedAmount))
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