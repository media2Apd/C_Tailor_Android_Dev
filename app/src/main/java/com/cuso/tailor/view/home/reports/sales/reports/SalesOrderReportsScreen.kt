@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.sales.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
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
import com.cuso.tailor.model.report.BranchWiseSalesItemDto
import com.cuso.tailor.model.report.OrderDeliverySlaItemDto
import com.cuso.tailor.model.report.OrderMasterRegisterItemDto
import com.cuso.tailor.model.report.OrderProductStreamItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
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
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.home.reports.ReportCardFooterCaption
import com.cuso.tailor.view.home.reports.ReportDetailGridBox
import com.cuso.tailor.view.home.reports.ReportGridCell
import com.cuso.tailor.view.home.reports.ReportInfinitelyPaginatedScaffold
import com.cuso.tailor.view.home.reports.ReportInlineMetricRow
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun OrderMasterRegisterReportScreen(
    onClose: () -> Unit,
    onViewDetail: (OrderMasterRegisterItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.orderMasterSummary.collectAsState()
    val pagingState by viewModel.orderMasterPaging.collectAsState()
    val filteredList by viewModel.filteredOrderMasterList.collectAsState()
    val searchQuery by viewModel.orderMasterSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Payment Status",
                    options = listOf(
                        FilterOption("Paid", "Paid"),
                        FilterOption("Partial", "Partial"),
                        FilterOption("Pending", "Pending")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) { filterSections.sumOf { s -> s.options.count { it.isSelected } } }

    LaunchedEffect(Unit) { viewModel.loadOrderMasterReport(isLoadMore = false) }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL ORDERS",
                value = (summary?.totalOrdersCount ?: 0).toString(),
                subtitle = "${summary?.deliveredOrdersCount ?: 0} delivered",
                icon = Icons.Default.ShoppingBag,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "GROSS REVENUE",
                value = viewModel.formatCurrency(summary?.grossSalesRevenue ?: 0.0),
                subtitle = "Total booked sales",
                icon = Icons.Default.CurrencyRupee,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "ADVANCE COLLECTED",
                value = viewModel.formatCurrency(summary?.totalAdvanceCollected ?: 0.0),
                subtitle = "Received payments",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "BALANCE DUE",
                value = viewModel.formatCurrency(summary?.totalBalanceOutstanding ?: 0.0),
                subtitle = "Pending collection",
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Sales Order Master Register Report",
        sectionTitle = "Sales Order Ledger",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onOrderMasterSearchChange,
        searchPlaceholder = "Search by order code, client or item...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val payStatuses = updated.find { it.title == "Payment Status" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyOrderMasterFilters(payStatuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearOrderMasterFilters()
        },
        onLoadMore = { viewModel.loadOrderMasterReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Sales order report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Sales order report export started") },
        onClose = onClose,
        emptyMessage = "No sales orders found."
    ) { item ->
        val isPaid = item.paymentStatus.equals("paid", ignoreCase = true)
        DataCard(
            item = item,
            title = item.orderCode.ifBlank { "--" },
            subtitle = "${item.clientName}  •  ${item.itemPreview}",
            topBadgeText = viewModel.formatStatusLabel(item.status),
            topBadgeTextColor = if (isPaid) greentext else yellowText,
            topBadgeBgColor = if (isPaid) greenBg else yellowBg,
            topBadgeInline = true,
            actions = listOf(MenuAction("View Details", Icons.Default.Visibility) { onViewDetail(item) }),
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Grand Total") {
                        Text(
                            text = viewModel.formatCurrency(item.grandTotal),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Advance Paid", viewModel.formatCurrency(item.advancePaid), valueColor = greentext),
                                ReportGridCell("Balance Due", viewModel.formatCurrency(item.balanceDue), valueColor = if (item.balanceDue > 0) redText else greentext),
                                ReportGridCell("Payment", item.paymentStatus.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Order Type", item.orderType.ifBlank { "--" }),
                                ReportGridCell("Priority", item.priority.ifBlank { "--" }),
                                ReportGridCell("Due Date", viewModel.formatDate(item.dueDate))
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Order Date: ${viewModel.formatDate(item.orderDate)}  •  By ${item.salesperson}")
                }
            }
        )
    }
}

@Composable
fun OrderProductStreamReportScreen(
    onClose: () -> Unit,
    onViewDetail: (OrderProductStreamItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.productStreamSummary.collectAsState()
    val pagingState by viewModel.productStreamPaging.collectAsState()
    val filteredList by viewModel.filteredProductStreamList.collectAsState()
    val searchQuery by viewModel.productStreamSearch.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadProductStreamReport() }

    val totalRev = summary?.totalRevenue ?: 0.0
    val streamCount = summary?.streamsCount ?: 0
    val avgPerStream = if (streamCount > 0) totalRev / streamCount else 0.0

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "PRODUCT STREAMS",
                value = streamCount.toString(),
                subtitle = "Revenue categories",
                icon = Icons.Default.Category,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "TOTAL REVENUE",
                value = viewModel.formatCurrency(totalRev),
                subtitle = "Combined streams",
                icon = Icons.Default.CurrencyRupee,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "TOTAL ORDERS",
                value = (summary?.totalOrders ?: 0).toString(),
                subtitle = "Across streams",
                icon = Icons.Default.ShoppingBag,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "AVG / STREAM",
                value = viewModel.formatCurrency(avgPerStream),
                subtitle = "Stream yield",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Sales by Product Stream Report",
        sectionTitle = "Revenue Stream Breakdown",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onProductStreamSearchChange,
        searchPlaceholder = "Search by product stream...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.lineType },
        filterSections = emptyList(),
        activeFilterCount = 0,
        onApplyFilters = {},
        onClearAllFilters = { viewModel.onProductStreamSearchChange("") },
        onLoadMore = {},
        onShareClick = { DynamicIslandManager.showSuccess("Product stream report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Product stream report export started") },
        onClose = onClose,
        emptyMessage = "No product stream data found."
    ) { item ->
        DataCard(
            item = item,
            title = item.label.ifBlank { item.lineType },
            subtitle = viewModel.formatStatusLabel(item.lineType),
            topBadgeText = item.revenueShare,
            topBadgeTextColor = Primary,
            topBadgeBgColor = primary_light,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Stream Revenue") {
                        Text(
                            text = viewModel.formatCurrency(item.totalRevenue),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = greentext
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Orders Count", item.ordersCount.toString()),
                                ReportGridCell("Items Sold", item.itemsCount.toString()),
                                ReportGridCell("Revenue Share", item.revenueShare)
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Line Type: ${viewModel.formatStatusLabel(item.lineType)}")
                }
            }
        )
    }
}

@Composable
fun DeliverySlaReportScreen(
    onClose: () -> Unit,
    onViewDetail: (OrderDeliverySlaItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.deliverySlaSummary.collectAsState()
    val pagingState by viewModel.deliverySlaPaging.collectAsState()
    val filteredList by viewModel.filteredDeliverySlaList.collectAsState()
    val searchQuery by viewModel.deliverySlaSearch.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDeliverySlaReport(isLoadMore = false) }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL DELIVERED",
                value = (summary?.totalDeliveredCount ?: 0).toString(),
                subtitle = "Completed deliveries",
                icon = Icons.Default.LocalShipping,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "ON-TIME",
                value = (summary?.onTimeCount ?: 0).toString(),
                subtitle = "Within SLA target",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "DELAYED",
                value = (summary?.delayedCount ?: 0).toString(),
                subtitle = "SLA breached",
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "OTD RATE",
                value = summary?.otdRate ?: "100%",
                subtitle = "On-time performance",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Delivery SLA On-Time Performance",
        sectionTitle = "Delivered Order SLA Log",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onDeliverySlaSearchChange,
        searchPlaceholder = "Search by order code or client...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = emptyList(),
        activeFilterCount = 0,
        onApplyFilters = {},
        onClearAllFilters = { viewModel.onDeliverySlaSearchChange("") },
        onLoadMore = { viewModel.loadDeliverySlaReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Delivery SLA report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Delivery SLA report export started") },
        onClose = onClose,
        emptyMessage = "No delivered SLA records found."
    ) { item ->
        val isDelayed = item.delayDays > 0 || item.slaStatus.contains("delay", ignoreCase = true)
        DataCard(
            item = item,
            title = item.orderCode.ifBlank { "--" },
            subtitle = item.clientName.ifBlank { "--" },
            topBadgeText = item.slaStatus.ifBlank { "--" },
            topBadgeTextColor = if (isDelayed) redText else greentext,
            topBadgeBgColor = if (isDelayed) redBg else greenBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Due Date", viewModel.formatDate(item.dueDate)),
                                ReportGridCell("Delivered", viewModel.formatDate(item.deliveredDate)),
                                ReportGridCell("Delay Days", "${item.delayDays}d")
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Status: ${viewModel.formatStatusLabel(item.status)}")
                }
            }
        )
    }
}

@Composable
fun BranchWiseSalesReportScreen(
    onClose: () -> Unit,
    onViewDetail: (BranchWiseSalesItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.branchSalesSummary.collectAsState()
    val pagingState by viewModel.branchSalesPaging.collectAsState()
    val filteredList by viewModel.filteredBranchSalesList.collectAsState()
    val searchQuery by viewModel.branchSalesSearch.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadBranchWiseSalesReport(isLoadMore = false) }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "OVERALL REVENUE",
                value = viewModel.formatCurrency(summary?.overallRevenue ?: 0.0),
                subtitle = "All branches combined",
                icon = Icons.Default.CurrencyRupee,
                valueColor = Primary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "TOTAL ORDERS",
                value = (summary?.overallOrders ?: 0).toString(),
                subtitle = "Network volume",
                icon = Icons.Default.Storefront,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "PAID REVENUE",
                value = viewModel.formatCurrency(summary?.overallPaidRevenue ?: 0.0),
                subtitle = "Collected across branches",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "PENDING REVENUE",
                value = viewModel.formatCurrency(summary?.overallPendingRevenue ?: 0.0),
                subtitle = "Outstanding balance",
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Branch-Wise Sales Comparison Report",
        sectionTitle = "Branch Revenue Directory",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onBranchSalesSearchChange,
        searchPlaceholder = "Search by branch name or code...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.branchId },
        filterSections = emptyList(),
        activeFilterCount = 0,
        onApplyFilters = {},
        onClearAllFilters = { viewModel.onBranchSalesSearchChange("") },
        onLoadMore = { viewModel.loadBranchWiseSalesReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Branch sales report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Branch sales report export started") },
        onClose = onClose,
        emptyMessage = "No branch sales data found."
    ) { item ->
        DataCard(
            item = item,
            title = item.branchName.ifBlank { "--" },
            subtitle = "Code: ${item.branchCode}  •  ${item.totalOrders} Orders",
            topBadgeText = item.revenueShare,
            topBadgeTextColor = Primary,
            topBadgeBgColor = primary_light,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Branch Revenue") {
                        Text(
                            text = viewModel.formatCurrency(item.totalRevenue),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Orders", item.totalOrders.toString()),
                                ReportGridCell("Avg Order", viewModel.formatCurrency(item.averageOrderValue)),
                                ReportGridCell("Share", item.revenueShare)
                            ),
                            listOf(
                                ReportGridCell("Paid", viewModel.formatCurrency(item.paidRevenue), valueColor = greentext),
                                ReportGridCell("Pending", viewModel.formatCurrency(item.pendingRevenue), valueColor = if (item.pendingRevenue > 0) redText else greentext),
                                ReportGridCell("Currency", item.currency.ifBlank { "INR" })
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Branch Code: ${item.branchCode}")
                }
            }
        )
    }
}