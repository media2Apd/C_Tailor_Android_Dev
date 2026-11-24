@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.finance.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapVert
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
import com.cuso.tailor.model.finance.PaymentModeItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.purpleBg
import com.cuso.tailor.ui.theme.purpleText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
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
fun PaymentModeReportScreen(
    onClose: () -> Unit,
    onViewDetail: (PaymentModeItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.paymentModeSummary.collectAsState()
    val pagingState by viewModel.paymentModePagingState.collectAsState()
    val filteredList by viewModel.filteredPaymentModeList.collectAsState()
    val searchQuery by viewModel.paymentModeSearchQuery.collectAsState()

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
                )
            )
        )
    }

    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { section -> section.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) {
        viewModel.loadPaymentModeReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "INCOMING COLLECTIONS",
                value = viewModel.formatCurrency(summary?.totalIncomingCollections ?: 0.0),
                subtitle = "${summary?.totalIncomingTransactions ?: 0} incoming receipts",
                icon = Icons.Default.ArrowDownward,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "OUTGOING PAYMENTS",
                value = viewModel.formatCurrency(summary?.totalOutgoingPayments ?: 0.0),
                subtitle = "${summary?.totalOutgoingTransactions ?: 0} outgoing txns",
                icon = Icons.Default.ArrowUpward,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "NET CASH FLOW",
                value = viewModel.formatCurrency(summary?.netCashFlowFromPayments ?: 0.0),
                subtitle = "Net payment cash flow",
                icon = Icons.Default.SwapVert,
                valueColor = Primary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "TOTAL TRANSACTIONS",
                value = ((summary?.totalIncomingTransactions ?: 0) + (summary?.totalOutgoingTransactions ?: 0)).toString(),
                subtitle = "Across all payment modes",
                icon = Icons.Default.AccountBalance,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Payment Mode Breakdown Report",
        subtitle = "Channel-wise payment inflows and drawer deposit register",
        sectionTitle = "Payment Mode Register",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onPaymentModeSearchQueryChange,
        searchPlaceholder = "Search by receipt, mode or customer...",
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
            viewModel.applyPaymentModeFilters(modes)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearPaymentModeFilters()
        },
        onLoadMore = { viewModel.loadPaymentModeReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Payment mode report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Payment mode report export started") },
        onClose = onClose,
        emptyMessage = "No payment mode transactions found."
    ) { item ->
        DataCard(
            item = item,
            title = item.customer.customerName.ifBlank { "--" },
            subtitle = item.paymentNumber.ifBlank { "--" },
            topBadgeText = item.paymentMode.ifBlank { "Cash" },
            topBadgeTextColor = Primary,
            topBadgeBgColor = primary_light,
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
                                ReportGridCell("Payment Date", viewModel.formatDate(item.paymentDate)),
                                ReportGridCell("Amount", viewModel.formatCurrency(item.amount)),
                                ReportGridCell("Mode", item.paymentMode.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Reference / UTR", item.referenceNumber.ifBlank { "--" }),
                                ReportGridCell("Deposited To", item.depositedTo.ifBlank { "--" })
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