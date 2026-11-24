
@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.finance.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Percent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.RevenueExpenseItemDto
import com.cuso.tailor.ui.theme.Primary
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
fun RevenueExpenseSummaryReportScreen(
    onClose: () -> Unit,
    onViewDetail: (RevenueExpenseItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.revenueExpenseSummary.collectAsState()
    val pagingState by viewModel.revenueExpensePagingState.collectAsState()
    val filteredList by viewModel.filteredRevenueExpenseList.collectAsState()
    val searchQuery by viewModel.revenueExpenseSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Account Type",
                    options = listOf(
                        FilterOption("Income", "Income"),
                        FilterOption("Expense", "Expense")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Category",
                    options = listOf(
                        FilterOption("Cost of Goods Sold", "Cost of Goods Sold"),
                        FilterOption("Indirect Expense", "Indirect Expense"),
                        FilterOption("Direct Income", "Direct Income")
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
        viewModel.loadRevenueExpenseReport()
    }

    val metrics = remember(summary) {
        val netProfit = summary?.netProfit ?: 0.0
        val isProfit = netProfit >= 0.0
        listOf(
            ReportMetricSpec(
                label = "TOTAL REVENUE",
                value = viewModel.formatCurrency(summary?.totalRevenue ?: 0.0),
                subtitle = "Total income accounts",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                valueColor = greentext,
                subtitleColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "TOTAL EXPENSE",
                value = viewModel.formatCurrency(summary?.totalExpense ?: 0.0),
                subtitle = "COGS & indirect expenses",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                valueColor = redText,
                subtitleColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "NET PROFIT",
                value = viewModel.formatCurrency(netProfit),
                subtitle = if (isProfit) "Net surplus" else "Net deficit",
                icon = Icons.Default.AccountBalanceWallet,
                valueColor = if (isProfit) greentext else redText,
                subtitleColor = if (isProfit) greentext else redText,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "PROFIT MARGIN",
                value = summary?.profitMargin ?: "0%",
                subtitle = "Net margin percentage",
                icon = Icons.Default.Percent,
                valueColor = purpleText,
                subtitleColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Revenue & Expense Summary Report",
        subtitle = "Profit & Loss account breakdown across income and expense ledgers",
        sectionTitle = "Account Ledger Summary",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onRevenueExpenseSearchQueryChange,
        searchPlaceholder = "Search by account name or code...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.accountId },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val types = updated.find { it.title == "Account Type" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val categories = updated.find { it.title == "Category" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyRevenueExpenseFilters(types, categories)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearRevenueExpenseFilters()
        },
        onLoadMore = { viewModel.loadRevenueExpenseReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Revenue & expense report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Revenue & expense report export started") },
        onClose = onClose,
        emptyMessage = "No revenue or expense account records found."
    ) { item ->
        val isExpense = item.accountType.equals("Expense", ignoreCase = true)
        val badgeTextColor = if (isExpense) redText else greentext
        val badgeBgColor = if (isExpense) redBg else greenBg

        DataCard(
            item = item,
            title = item.accountName,
            subtitle = "Code: ${item.accountCode.ifBlank { "--" }}",
            topBadgeText = item.accountType.ifBlank { "Account" },
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
                    ReportInlineMetricRow(label = "Net Amount") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                .background(badgeBgColor)
                                .padding(
                                    horizontal = tokens.extraPadding * 0.8f,
                                    vertical = tokens.extraPadding * 0.25f
                                )
                        ) {
                            Text(
                                text = viewModel.formatCurrency(item.netAmount),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = badgeTextColor
                            )
                        }
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Account Code", item.accountCode.ifBlank { "--" }),
                                ReportGridCell("Category", item.category.ifBlank { "--" }),
                                ReportGridCell("Currency", item.currency.ifBlank { "INR" })
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Ledger type: ${item.accountType.ifBlank { "--" }}")
                }
            }
        )
    }
}