@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.finance.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
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
import com.cuso.tailor.model.finance.PaymentCollectionItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.activity_purple_bg
import com.cuso.tailor.ui.theme.blueText
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
fun PaymentCollectionReportScreen(
    onClose: () -> Unit,
    onViewDetail: (PaymentCollectionItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.paymentCollectionSummary.collectAsState()
    val pagingState by viewModel.paymentCollectionPagingState.collectAsState()
    val filteredList by viewModel.filteredPaymentCollectionList.collectAsState()
    val searchQuery by viewModel.paymentCollectionSearchQuery.collectAsState()

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
                    title = "Status",
                    options = listOf(
                        FilterOption("Completed", "Completed"),
                        FilterOption("Pending", "Pending")
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
        viewModel.loadPaymentCollectionReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL COLLECTED",
                value = viewModel.formatCurrency(summary?.totalCollected ?: 0.0),
                subtitle = "Total incoming funds",
                icon = Icons.Default.CreditCard,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "TRANSACTIONS COUNT",
                value = (summary?.totalTransactions ?: 0).toString(),
                subtitle = "Total payment receipts",
                icon = Icons.Default.AttachMoney,
                valueColor = TextPrimary,
                iconTint = blueText,
                iconBgColor = activity_purple_bg
            ),
            ReportMetricSpec(
                label = "ADVANCE COLLECTED",
                value = viewModel.formatCurrency(summary?.advanceCollected ?: 0.0),
                subtitle = "Order advance payments",
                icon = Icons.Default.SouthWest,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "INVOICE SETTLEMENTS",
                value = viewModel.formatCurrency(summary?.invoicePaymentsCollected ?: 0.0),
                subtitle = "Direct invoice payments",
                icon = Icons.Default.Description,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Payment Collection Report",
        subtitle = null,
        sectionTitle = "Customer Collection Register",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onPaymentCollectionSearchQueryChange,
        searchPlaceholder = "Search by receipt, customer or mobile...",
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
            val statuses = updated.find { it.title == "Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyPaymentCollectionFilters(modes, statuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearPaymentCollectionFilters()
        },
        onLoadMore = { viewModel.loadPaymentCollectionReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Payment collection report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Payment collection report export started") },
        onClose = onClose,
        emptyMessage = "No payment collection receipts found."
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
                    val linkedDoc = item.linkedDocument?.invoiceNumber
                        ?: item.linkedDocument?.orderCode
                        ?: "--"
                    ReportCardFooterCaption(text = "Linked document: $linkedDoc")
                }
            }
        )
    }
}