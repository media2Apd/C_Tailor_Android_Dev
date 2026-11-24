@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.logistics.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.CodReconciliationItemDto
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
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun CodReconciliationReportScreen(
    onClose: () -> Unit,
    onViewDetail: (CodReconciliationItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.codReconSummary.collectAsState()
    val pagingState by viewModel.codReconPaging.collectAsState()
    val filteredList by viewModel.filteredCodReconList.collectAsState()
    val searchQuery by viewModel.codReconSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Settlement Status",
                    options = listOf(
                        FilterOption("settled", "Remitted / Settled"),
                        FilterOption("pending", "Pending Payout")
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
        viewModel.loadCodReconciliationReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL COD PARCELS",
                value = (summary?.totalCodOrders ?: 0).toString(),
                subtitle = "COD dispatches",
                icon = Icons.Default.CreditCard,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "TOTAL EXPECTED COD",
                value = viewModel.formatCurrency(summary?.totalExpectedCodAmount ?: 0.0),
                subtitle = "Dispatched cash value",
                icon = Icons.Default.Inventory2,
                valueColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "REMITTED / COLLECTED",
                value = viewModel.formatCurrency(summary?.collectedAmount ?: 0.0),
                subtitle = "Settled by couriers",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "PENDING COURIER PAYOUTS",
                value = viewModel.formatCurrency(summary?.pendingPayoutAmount ?: 0.0),
                subtitle = "Fees: ${viewModel.formatCurrency(summary?.totalDeliveryCharges ?: 0.0)}",
                icon = Icons.Default.AccessTime,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "COD Reconciliation Report",
        subtitle = null,
        sectionTitle = "COD Settlement Registry",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onCodReconSearchChange,
        searchPlaceholder = "Search by shipment, order, courier or recipient...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val settlements = updated.find { it.title == "Settlement Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyCodReconFilters(settlements)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearCodReconFilters()
        },
        onLoadMore = { viewModel.loadCodReconciliationReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("COD reconciliation report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("COD reconciliation report export started") },
        onClose = onClose,
        emptyMessage = "No COD reconciliation records found."
    ) { item ->
        val badgeTextColor = if (item.isSettled) greentext else yellowText
        val badgeBgColor = if (item.isSettled) greenBg else yellowBg
        val badgeLabel = item.remittanceBadge.ifBlank {
            if (item.isSettled) "Remitted / Settled" else "Pending Payout"
        }

        DataCard(
            item = item,
            title = item.recipientName.ifBlank { "--" },
            subtitle = "${item.shipmentId.ifBlank { "--" }}  •  ${item.orderCode.ifBlank { "--" }}",
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
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("COD AMOUNT", viewModel.formatCurrency(item.codAmount)),
                                ReportGridCell("FREIGHT", viewModel.formatCurrency(item.deliveryCharge)),
                                ReportGridCell("NET SETTLEMENT", viewModel.formatCurrency(item.netReceivable))
                            ),
                            listOf(
                                ReportGridCell("City", item.destinationCity.ifBlank { "--" }),
                                ReportGridCell("Courier Partner", item.courierPartner.ifBlank { "--" }),
                                ReportGridCell("Dispatch Date", viewModel.formatDate(item.createdAt))
                            ),
                            listOf(
                                ReportGridCell("Remittance Status", if (item.isSettled) "Settled" else "Pending"),
                                ReportGridCell("Order #", item.orderCode.ifBlank { "--" }),
                                ReportGridCell("Shipment #", item.shipmentId.ifBlank { "--" })
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Dispatched: ${viewModel.formatDate(item.createdAt)}")
                }
            }
        )
    }
}