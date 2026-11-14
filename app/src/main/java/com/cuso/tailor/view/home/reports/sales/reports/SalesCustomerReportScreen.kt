@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.sales.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.StarOutline
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
import com.cuso.tailor.model.report.CustomerCreditReceivableItemDto
import com.cuso.tailor.model.report.CustomerDormantWinbackItemDto
import com.cuso.tailor.model.report.CustomerLtvRevenueItemDto
import com.cuso.tailor.model.report.CustomerMasterRegisterItemDto
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
import com.cuso.tailor.view.home.reports.ReportInlineMetricRow
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun CustomerMasterDirectoryReportScreen(
    onClose: () -> Unit,
    onViewDetail: (CustomerMasterRegisterItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.custMasterSummary.collectAsState()
    val pagingState by viewModel.custMasterPaging.collectAsState()
    val filteredList by viewModel.filteredCustMasterList.collectAsState()
    val searchQuery by viewModel.custMasterSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Customer Level",
                    options = listOf(
                        FilterOption("Regular", "Regular"),
                        FilterOption("VIP", "VIP"),
                        FilterOption("Elite", "Elite")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) { filterSections.sumOf { s -> s.options.count { it.isSelected } } }

    LaunchedEffect(Unit) { viewModel.loadCustMasterReport(isLoadMore = false) }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL CUSTOMERS",
                value = (summary?.totalCustomers ?: 0).toString(),
                subtitle = "Registered directory",
                icon = Icons.Default.Group,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "ACTIVE ACCOUNTS",
                value = (summary?.activeCustomers ?: 0).toString(),
                subtitle = "Verified clients",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "CORPORATE / VIP",
                value = "${summary?.corporateCount ?: 0} / ${summary?.vipEliteCount ?: 0}",
                subtitle = "Key account tiers",
                icon = Icons.Default.StarOutline,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "CREDIT LIMIT GIVEN",
                value = viewModel.formatCurrency(summary?.totalCreditLimitGiven ?: 0.0),
                subtitle = "Total credit line",
                icon = Icons.Default.AccountBalanceWallet,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Customer Master Directory Report",
        sectionTitle = "Registered Customer Accounts",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onCustMasterSearchChange,
        searchPlaceholder = "Search by name, code, phone or city...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val levels = updated.find { it.title == "Customer Level" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyCustMasterFilters(levels)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearCustMasterFilters()
        },
        onLoadMore = { viewModel.loadCustMasterReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Customer directory report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Customer directory report export started") },
        onClose = onClose,
        emptyMessage = "No customers found."
    ) { item ->
        val isActive = item.status.equals("active", ignoreCase = true)
        DataCard(
            item = item,
            title = item.fullName.ifBlank { "--" },
            subtitle = "${item.customerCode}  •  ${item.customerType}",
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
                                ReportGridCell("Mobile", item.mobileNumber.ifBlank { "--" }),
                                ReportGridCell("Tier", item.customerLevel.ifBlank { "--" }),
                                ReportGridCell("Contact", item.preferredContactMethod.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Credit Limit", viewModel.formatCurrency(item.creditLimit)),
                                ReportGridCell("Branch", item.branch.ifBlank { "--" }),
                                ReportGridCell("Location", item.location.ifBlank { "--" })
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Registered on ${viewModel.formatDate(item.createdAt)}")
                }
            }
        )
    }
}

@Composable
fun CustomerLtvRevenueReportScreen(
    onClose: () -> Unit,
    onViewDetail: (CustomerLtvRevenueItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.custLtvSummary.collectAsState()
    val pagingState by viewModel.custLtvPaging.collectAsState()
    val filteredList by viewModel.filteredCustLtvList.collectAsState()
    val searchQuery by viewModel.custLtvSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Customer Level",
                    options = listOf(
                        FilterOption("Regular", "Regular"),
                        FilterOption("VIP", "VIP"),
                        FilterOption("Elite", "Elite")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) { filterSections.sumOf { s -> s.options.count { it.isSelected } } }

    LaunchedEffect(Unit) { viewModel.loadCustLtvReport(isLoadMore = false) }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "CLIENTS TRACKED",
                value = (summary?.totalClientsTracked ?: 0).toString(),
                subtitle = "Revenue ranking",
                icon = Icons.Default.Group,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "TOTAL LTV REVENUE",
                value = viewModel.formatCurrency(summary?.totalLtvRevenue ?: 0.0),
                subtitle = "Cumulative spend",
                icon = Icons.Default.CurrencyRupee,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "AVG LTV / CLIENT",
                value = viewModel.formatCurrency(summary?.avgLtvPerClient ?: 0.0),
                subtitle = "Mean customer value",
                icon = Icons.Default.StarOutline,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "OUTSTANDING DUE",
                value = viewModel.formatCurrency(summary?.totalOutstandingDue ?: 0.0),
                subtitle = "Pending collection",
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Customer Lifetime Value & Revenue",
        sectionTitle = "LTV & Spend Ranking",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onCustLtvSearchChange,
        searchPlaceholder = "Search by customer name or code...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val levels = updated.find { it.title == "Customer Level" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyCustLtvFilters(levels)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearCustLtvFilters()
        },
        onLoadMore = { viewModel.loadCustLtvReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Customer LTV report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Customer LTV report export started") },
        onClose = onClose,
        emptyMessage = "No customer LTV records found."
    ) { item ->
        DataCard(
            item = item,
            title = item.fullName.ifBlank { "--" },
            subtitle = "${item.customerCode}  •  ${item.customerLevel}",
            topBadgeText = "${item.totalOrdersCount} Orders",
            topBadgeTextColor = Primary,
            topBadgeBgColor = primary_light,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Lifetime Spend") {
                        Text(
                            text = viewModel.formatCurrency(item.lifetimeSpend),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = greentext
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Orders", item.totalOrdersCount.toString()),
                                ReportGridCell("Avg Order", viewModel.formatCurrency(item.averageOrderValue)),
                                ReportGridCell("Outstanding", viewModel.formatCurrency(item.outstandingBalance), valueColor = if (item.outstandingBalance > 0) redText else greentext)
                            ),
                            listOf(
                                ReportGridCell("Mobile", item.mobileNumber.ifBlank { "--" }),
                                ReportGridCell("Type", item.customerType.ifBlank { "--" }),
                                ReportGridCell("Last Order", viewModel.formatDate(item.lastOrderDate))
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Customer since ${viewModel.formatDate(item.createdAt)}")
                }
            }
        )
    }
}

@Composable
fun CustomerCreditReceivablesReportScreen(
    onClose: () -> Unit,
    onViewDetail: (CustomerCreditReceivableItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.custCreditSummary.collectAsState()
    val pagingState by viewModel.custCreditPaging.collectAsState()
    val filteredList by viewModel.filteredCustCreditList.collectAsState()
    val searchQuery by viewModel.custCreditSearch.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadCustCreditReport(isLoadMore = false) }

    val dueTotal = summary?.totalReceivablesDue ?: 0.0
    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "ACCOUNTS WITH DUES",
                value = (summary?.accountsWithDuesCount ?: 0).toString(),
                subtitle = "Pending settlements",
                icon = Icons.Default.Group,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "RECEIVABLES DUE",
                value = viewModel.formatCurrency(dueTotal),
                subtitle = "Unpaid order balance",
                icon = Icons.Default.CurrencyRupee,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "CREDIT ALLOCATED",
                value = viewModel.formatCurrency(summary?.totalCreditLimitAllocated ?: 0.0),
                subtitle = "Approved facility",
                icon = Icons.Default.AccountBalanceWallet,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "COLLECTION STATUS",
                value = if (dueTotal > 0) "Follow-up" else "Settled",
                subtitle = "Credit risk audit",
                icon = Icons.Default.WarningAmber,
                valueColor = if (dueTotal > 0) yellowText else greentext,
                iconTint = if (dueTotal > 0) yellowText else greentext,
                iconBgColor = if (dueTotal > 0) yellowBg else greenBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Customer Credit & Receivable Audit",
        sectionTitle = "Outstanding Credit Accounts",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onCustCreditSearchChange,
        searchPlaceholder = "Search by customer name or code...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = emptyList(),
        activeFilterCount = 0,
        onApplyFilters = {},
        onClearAllFilters = { viewModel.onCustCreditSearchChange("") },
        onLoadMore = { viewModel.loadCustCreditReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Credit receivables report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Credit receivables report export started") },
        onClose = onClose,
        emptyMessage = "No outstanding credit receivables found."
    ) { item ->
        DataCard(
            item = item,
            title = item.fullName.ifBlank { "--" },
            subtitle = "${item.customerCode}  •  ${item.customerLevel}",
            topBadgeText = "${item.unpaidOrdersCount} Unpaid",
            topBadgeTextColor = redText,
            topBadgeBgColor = redBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Outstanding Balance") {
                        Text(
                            text = viewModel.formatCurrency(item.outstandingBalance),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = redText
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Credit Limit", viewModel.formatCurrency(item.creditLimit)),
                                ReportGridCell("Utilization", item.utilizationPercentage.ifBlank { "0%" }),
                                ReportGridCell("Terms", "${item.creditPeriodDays} days")
                            ),
                            listOf(
                                ReportGridCell("Mobile", item.mobileNumber.ifBlank { "--" }),
                                ReportGridCell("Type", item.customerType.ifBlank { "--" }),
                                ReportGridCell("Oldest Due", viewModel.formatDate(item.oldestDueDate))
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Oldest due date: ${viewModel.formatDate(item.oldestDueDate)}")
                }
            }
        )
    }
}

@Composable
fun CustomerDormantWinbackReportScreen(
    onClose: () -> Unit,
    onViewDetail: (CustomerDormantWinbackItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.custDormantSummary.collectAsState()
    val pagingState by viewModel.custDormantPaging.collectAsState()
    val filteredList by viewModel.filteredCustDormantList.collectAsState()
    val searchQuery by viewModel.custDormantSearch.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadCustDormantReport(isLoadMore = false) }

    val dormantCount = summary?.dormantClientsCount ?: 0
    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "DORMANT CLIENTS",
                value = dormantCount.toString(),
                subtitle = "Inactive accounts",
                icon = Icons.Default.Group,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "PAST REVENUE",
                value = viewModel.formatCurrency(summary?.dormantPastRevenue ?: 0.0),
                subtitle = "Win-back opportunity",
                icon = Icons.Default.CurrencyRupee,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "INACTIVITY THRESHOLD",
                value = "${summary?.inactivityThresholdDays ?: 90} days",
                subtitle = "Audit window",
                icon = Icons.Default.Schedule,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "RETENTION HEALTH",
                value = if (dormantCount == 0) "Healthy" else "Outreach",
                subtitle = "Win-back campaign",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = if (dormantCount == 0) greentext else yellowText,
                iconTint = if (dormantCount == 0) greentext else yellowText,
                iconBgColor = if (dormantCount == 0) greenBg else yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Dormant Customer Win-back Report",
        sectionTitle = "Inactive Customer Roster",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onCustDormantSearchChange,
        searchPlaceholder = "Search by customer name or code...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = emptyList(),
        activeFilterCount = 0,
        onApplyFilters = {},
        onClearAllFilters = { viewModel.onCustDormantSearchChange("") },
        onLoadMore = { viewModel.loadCustDormantReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Dormant win-back report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Dormant win-back report export started") },
        onClose = onClose,
        emptyMessage = "No dormant customers found."
    ) { item ->
        DataCard(
            item = item,
            title = item.fullName.ifBlank { "--" },
            subtitle = "${item.customerCode}  •  ${item.customerLevel}",
            topBadgeText = "${item.daysInactive}d Inactive",
            topBadgeTextColor = yellowText,
            topBadgeBgColor = yellowBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Mobile", item.mobileNumber.ifBlank { "--" }),
                                ReportGridCell("Orders", item.totalOrdersCount.toString()),
                                ReportGridCell("Past Spend", viewModel.formatCurrency(item.lifetimeSpend))
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Last order: ${viewModel.formatDate(item.lastOrderDate)}")
                }
            }
        )
    }
}