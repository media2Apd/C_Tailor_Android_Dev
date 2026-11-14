@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.sales.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Schedule
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
import com.cuso.tailor.model.report.QuotationDiscountLeakageItemDto
import com.cuso.tailor.model.report.QuotationExpiringAuditItemDto
import com.cuso.tailor.model.report.QuotationMasterRegisterItemDto
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
fun QuotationMasterRegisterReportScreen(
    onClose: () -> Unit,
    onViewDetail: (QuotationMasterRegisterItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.quoteMasterSummary.collectAsState()
    val pagingState by viewModel.quoteMasterPaging.collectAsState()
    val filteredList by viewModel.filteredQuoteMasterList.collectAsState()
    val searchQuery by viewModel.quoteMasterSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Quotation Status",
                    options = listOf(
                        FilterOption("Draft", "Draft"),
                        FilterOption("Sent", "Sent"),
                        FilterOption("Accepted", "Accepted"),
                        FilterOption("Converted", "Converted"),
                        FilterOption("Expired", "Expired")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) { filterSections.sumOf { s -> s.options.count { it.isSelected } } }

    LaunchedEffect(Unit) { viewModel.loadQuoteMasterReport(isLoadMore = false) }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL QUOTES",
                value = (summary?.totalQuotesCount ?: 0).toString(),
                subtitle = "${summary?.activeNegotiationCount ?: 0} active quotes",
                icon = Icons.Default.Description,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "GROSS VALUE",
                value = viewModel.formatCurrency(summary?.totalGrossValue ?: 0.0),
                subtitle = "Discount: ${viewModel.formatCurrency(summary?.totalDiscountGiven ?: 0.0)}",
                icon = Icons.Default.CurrencyRupee,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "CONVERTED ORDERS",
                value = (summary?.convertedToOrderCount ?: 0).toString(),
                subtitle = viewModel.formatCurrency(summary?.convertedValue ?: 0.0),
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "CONVERSION RATE",
                value = summary?.conversionRate ?: "0%",
                subtitle = "Active: ${viewModel.formatCurrency(summary?.activeNegotiationValue ?: 0.0)}",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Quotation Master Register Report",
        sectionTitle = "Issued Quotations Ledger",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onQuoteMasterSearchChange,
        searchPlaceholder = "Search by quote code, client or item...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Quotation Status" }?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyQuoteMasterFilters(statuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec -> sec.copy(options = sec.options.map { it.copy(isSelected = false) }) }
            viewModel.clearQuoteMasterFilters()
        },
        onLoadMore = { viewModel.loadQuoteMasterReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Quotation register ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Quotation register export started") },
        onClose = onClose,
        emptyMessage = "No quotations found."
    ) { item ->
        val (statusText, statusBg) = when (item.status.lowercase()) {
            "accepted", "converted" -> greentext to greenBg
            "expired", "rejected" -> redText to redBg
            else -> Primary to primary_light
        }

        DataCard(
            item = item,
            title = item.quotationCode.ifBlank { "--" },
            subtitle = "${item.clientName}  •  ${item.itemPreview}",
            topBadgeText = item.status.ifBlank { "--" },
            topBadgeTextColor = statusText,
            topBadgeBgColor = statusBg,
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
                                ReportGridCell("Subtotal", viewModel.formatCurrency(item.subtotal)),
                                ReportGridCell("Discount", viewModel.formatCurrency(item.totalDiscount)),
                                ReportGridCell("Approval", viewModel.formatStatusLabel(item.approvalStatus))
                            ),
                            listOf(
                                ReportGridCell("Client Phone", item.clientPhone.ifBlank { "--" }),
                                ReportGridCell("Salesperson", item.salesperson.ifBlank { "--" }),
                                ReportGridCell("Expiry Date", viewModel.formatDate(item.expiryDate))
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Quoted on ${viewModel.formatDate(item.quotationDate)}")
                }
            }
        )
    }
}

@Composable
fun QuotationDiscountLeakageReportScreen(
    onClose: () -> Unit,
    onViewDetail: (QuotationDiscountLeakageItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.quoteDiscountSummary.collectAsState()
    val pagingState by viewModel.quoteDiscountPaging.collectAsState()
    val filteredList by viewModel.filteredQuoteDiscountList.collectAsState()
    val searchQuery by viewModel.quoteDiscountSearch.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadQuoteDiscountReport(isLoadMore = false) }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "DISCOUNTED QUOTES",
                value = (summary?.discountedQuotesCount ?: 0).toString(),
                subtitle = "Margin concessions",
                icon = Icons.Default.Discount,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "DISCOUNT AMOUNT",
                value = viewModel.formatCurrency(summary?.totalDiscountAmount ?: 0.0),
                subtitle = "Total margin leakage",
                icon = Icons.Default.CurrencyRupee,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "GROSS BEFORE DISC.",
                value = viewModel.formatCurrency(summary?.totalGrossBeforeDiscount ?: 0.0),
                subtitle = "Base quote value",
                icon = Icons.Default.Description,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "AVG DISCOUNT",
                value = summary?.avgDiscountPercentage ?: "0%",
                subtitle = "${summary?.managerApprovedDiscounts ?: 0} manager approved",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Discount Leakage & Margin Audit",
        sectionTitle = "Discounted Quotation Ledger",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onQuoteDiscountSearchChange,
        searchPlaceholder = "Search by quote code or client...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = emptyList(),
        activeFilterCount = 0,
        onApplyFilters = {},
        onClearAllFilters = { viewModel.onQuoteDiscountSearchChange("") },
        onLoadMore = { viewModel.loadQuoteDiscountReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Discount leakage report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Discount leakage report export started") },
        onClose = onClose,
        emptyMessage = "No discounted quotations found."
    ) { item ->
        DataCard(
            item = item,
            title = item.quotationCode.ifBlank { "--" },
            subtitle = item.clientName.ifBlank { "--" },
            topBadgeText = "${item.discountPercentage} Off",
            topBadgeTextColor = redText,
            topBadgeBgColor = redBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Subtotal", viewModel.formatCurrency(item.subtotal)),
                                ReportGridCell("Discount", viewModel.formatCurrency(item.totalDiscount), valueColor = redText),
                                ReportGridCell("Net Total", viewModel.formatCurrency(item.grandTotal))
                            ),
                            listOf(
                                ReportGridCell("Approval", viewModel.formatStatusLabel(item.approvalStatus)),
                                ReportGridCell("Salesperson", item.salesperson.ifBlank { "--" }),
                                ReportGridCell("Date", viewModel.formatDate(item.quotationDate))
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Quoted on ${viewModel.formatDate(item.quotationDate)}")
                }
            }
        )
    }
}

@Composable
fun QuotationExpiringAuditReportScreen(
    onClose: () -> Unit,
    onViewDetail: (QuotationExpiringAuditItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.quoteExpiringSummary.collectAsState()
    val pagingState by viewModel.quoteExpiringPaging.collectAsState()
    val filteredList by viewModel.filteredQuoteExpiringList.collectAsState()
    val searchQuery by viewModel.quoteExpiringSearch.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadQuoteExpiringReport(isLoadMore = false) }

    val expiringCount = summary?.expiringCount ?: 0
    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "EXPIRING QUOTES",
                value = expiringCount.toString(),
                subtitle = "Approaching expiry",
                icon = Icons.Default.Schedule,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "AT-RISK VALUE",
                value = viewModel.formatCurrency(summary?.atRiskValue ?: 0.0),
                subtitle = "Unconverted pipeline",
                icon = Icons.Default.CurrencyRupee,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            ),
            ReportMetricSpec(
                label = "FOLLOW-UP ACTION",
                value = if (expiringCount > 0) "Required" else "None",
                subtitle = "Client outreach",
                icon = Icons.Default.WarningAmber,
                valueColor = if (expiringCount > 0) yellowText else greentext,
                iconTint = if (expiringCount > 0) yellowText else greentext,
                iconBgColor = if (expiringCount > 0) yellowBg else greenBg
            ),
            ReportMetricSpec(
                label = "PIPELINE HEALTH",
                value = if (expiringCount == 0) "Healthy" else "At Risk",
                subtitle = "Quotation SLA",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = if (expiringCount == 0) greentext else redText,
                iconTint = if (expiringCount == 0) greentext else redText,
                iconBgColor = if (expiringCount == 0) greenBg else redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Expiring Quotation Action Report",
        sectionTitle = "Expiring Quotations Watchlist",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onQuoteExpiringSearchChange,
        searchPlaceholder = "Search by quote code or client...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = emptyList(),
        activeFilterCount = 0,
        onApplyFilters = {},
        onClearAllFilters = { viewModel.onQuoteExpiringSearchChange("") },
        onLoadMore = { viewModel.loadQuoteExpiringReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Expiring quotes report ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Expiring quotes report export started") },
        onClose = onClose,
        emptyMessage = "No expiring quotations found."
    ) { item ->
        DataCard(
            item = item,
            title = item.quotationCode.ifBlank { "--" },
            subtitle = item.clientName.ifBlank { "--" },
            topBadgeText = "${item.daysRemaining}d Left",
            topBadgeTextColor = if (item.daysRemaining <= 2) redText else yellowText,
            topBadgeBgColor = if (item.daysRemaining <= 2) redBg else yellowBg,
            topBadgeInline = true,
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Quote Value") {
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
                                ReportGridCell("Client Phone", item.clientPhone.ifBlank { "--" }),
                                ReportGridCell("Salesperson", item.salesperson.ifBlank { "--" }),
                                ReportGridCell("Status", item.status.ifBlank { "--" })
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Expires on ${viewModel.formatDate(item.expiryDate)}")
                }
            }
        )
    }
}