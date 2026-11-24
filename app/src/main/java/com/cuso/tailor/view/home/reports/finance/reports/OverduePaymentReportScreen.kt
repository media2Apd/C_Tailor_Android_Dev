@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.finance.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Store
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
import com.cuso.tailor.model.finance.OverduePaymentItemDto
import com.cuso.tailor.ui.theme.TextPrimary
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
fun OverduePaymentReportScreen(
    onClose: () -> Unit,
    onViewDetail: (OverduePaymentItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.overduePaymentSummary.collectAsState()
    val pagingState by viewModel.overduePaymentPagingState.collectAsState()
    val filteredList by viewModel.filteredOverduePaymentList.collectAsState()
    val searchQuery by viewModel.overduePaymentSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Party Type",
                    options = listOf(
                        FilterOption("Receivable", "Customer (Receivable)"),
                        FilterOption("Payable", "Supplier (Payable)")
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
        viewModel.loadOverduePaymentReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "CUSTOMER OVERDUE (AR)",
                value = viewModel.formatCurrency(summary?.totalCustomerOverdueAmount ?: 0.0),
                subtitle = "${summary?.totalCustomerOverdueInvoices ?: 0} overdue invoices",
                icon = Icons.Default.Group,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "SUPPLIER OVERDUE (AP)",
                value = viewModel.formatCurrency(summary?.totalSupplierOverdueAmount ?: 0.0),
                subtitle = "${summary?.totalSupplierOverdueBills ?: 0} overdue bills",
                icon = Icons.Default.Store,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "NET OVERDUE BALANCE",
                value = viewModel.formatCurrency(summary?.netOverdueBalance ?: 0.0),
                subtitle = "AR Overdue minus AP Overdue",
                icon = Icons.Default.Balance,
                valueColor = TextPrimary,
                iconTint = purpleText,
                iconBgColor = purpleBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Overdue Payment",
        subtitle = "Actionable audit of customer receivables and supplier payables past their statutory due dates",
        sectionTitle = "Overdue Invoices & Bills Register",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onOverduePaymentSearchQueryChange,
        searchPlaceholder = "Search by invoice/bill, party or mobile...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id.ifBlank { it.documentId.ifBlank { it.documentNumber } } },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val partyTypes = updated.find { it.title == "Party Type" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyOverduePaymentFilters(partyTypes)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearOverduePaymentFilters()
        },
        onLoadMore = { viewModel.loadOverduePaymentReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Overdue payment report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Overdue payment report export started") },
        onClose = onClose,
        emptyMessage = "All invoices and supplier bills are settled or within statutory due dates."
    ) { item ->
        val partyName = item.partyName.ifBlank {
            item.customer?.customerName ?: item.supplier?.supplierName.orEmpty()
        }.ifBlank { "--" }
        val partyPhone = item.mobileNumber.ifBlank {
            item.customer?.mobileNumber ?: item.supplier?.mobileNumber.orEmpty()
        }.ifBlank { "--" }
        val badgeLabel = item.status.ifBlank { "Overdue" }

        DataCard(
            item = item,
            title = partyName,
            subtitle = item.documentNumber.ifBlank { "--" },
            topBadgeText = badgeLabel,
            topBadgeTextColor = redText,
            topBadgeBgColor = redBg,
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
                            text = partyPhone,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Due Date", viewModel.formatDate(item.dueDate)),
                                ReportGridCell("Days Overdue", item.daysOverdue.toString()),
                                ReportGridCell("Grand Total", viewModel.formatCurrency(item.grandTotal))
                            ),
                            listOf(
                                ReportGridCell("Party Type", item.partyType.ifBlank { "Receivable" }),
                                ReportGridCell("Balance Due", viewModel.formatCurrency(item.balanceDue))
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Party code: ${item.partyCode.ifBlank { "--" }}")
                }
            }
        )
    }
}