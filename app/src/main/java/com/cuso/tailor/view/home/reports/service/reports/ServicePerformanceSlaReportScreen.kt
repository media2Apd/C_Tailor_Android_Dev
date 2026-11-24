@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.service.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.ServicePerformanceSlaItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.home.reports.*
import com.cuso.tailor.viewmodel.ReportViewModel

@Composable
fun ServicePerformanceSlaReportScreen(
    onClose: () -> Unit,
    onViewDetail: (ServicePerformanceSlaItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.serviceSlaSummary.collectAsState()
    val pagingState by viewModel.serviceSlaPaging.collectAsState()
    val filteredList by viewModel.filteredServiceSlaList.collectAsState()
    val searchQuery by viewModel.serviceSlaSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "SLA Status",
                    options = listOf(
                        FilterOption("On Time", "On Time"),
                        FilterOption("Delayed", "Delayed"),
                        FilterOption("Breached", "Breached")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Order Type",
                    options = listOf(
                        FilterOption("New_Stitching", "New Stitching"),
                        FilterOption("Alteration", "Alteration")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }
    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { section -> section.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) { viewModel.loadServiceSlaReport() }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL ORDERS",
                value = (summary?.totalOrders ?: 0).toString(),
                subtitle = "${summary?.deliveredOrders ?: 0} delivered",
                icon = Icons.Default.Inventory2,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "ON-TIME DELIVERY RATE",
                value = summary?.onTimeDeliveryRate ?: "0%",
                subtitle = "${summary?.onTimeDeliveries ?: 0} on-time deliveries",
                subtitleColor = greentext,
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "DELAYED DELIVERIES",
                value = (summary?.delayedDeliveries ?: 0).toString(),
                subtitle = "Delivered after due date",
                subtitleColor = yellowText,
                icon = Icons.Default.Schedule,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "ACTIVE SLA BREACHES",
                value = (summary?.activeSlaBreaches ?: 0).toString(),
                subtitle = "Open orders past due",
                subtitleColor = redText,
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Service Performance & SLA Report",
        subtitle = null,
        sectionTitle = "Service Fulfilment Register",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onServiceSlaSearchChange,
        searchPlaceholder = "Search by order, customer or mobile...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.orderId },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val sla = updated.find { it.title == "SLA Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val types = updated.find { it.title == "Order Type" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyServiceSlaFilters(sla, types)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearServiceSlaFilters()
        },
        onLoadMore = { viewModel.loadServiceSlaReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Service SLA report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Service SLA report export started") },
        onClose = onClose,
        emptyMessage = "No service orders found."
    ) { item ->
        val badgeText = when {
            item.slaStatus.equals("On Time", true) -> greentext
            item.slaStatus.equals("Breached", true) -> redText
            else -> yellowText
        }
        val badgeBg = when {
            item.slaStatus.equals("On Time", true) -> greenBg
            item.slaStatus.equals("Breached", true) -> redBg
            else -> yellowBg
        }

        DataCard(
            item = item,
            title = item.customerName.ifBlank { "--" },
            subtitle = item.orderCode.ifBlank { "--" },
            topBadgeText = item.slaStatus.ifBlank { "--" },
            topBadgeTextColor = badgeText,
            topBadgeBgColor = badgeBg,
            topBadgeInline = true,
            actions = listOf(
                MenuAction(label = "View Details", icon = Icons.Default.Visibility, onClick = { onViewDetail(item) })
            ),
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Contact number") {
                        Text(
                            text = item.customerPhone.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Order Type", viewModel.formatStatusLabel(item.orderType)),
                                ReportGridCell("Priority", item.priority.ifBlank { "--" }),
                                ReportGridCell("Status", viewModel.formatStatusLabel(item.status))
                            ),
                            listOf(
                                ReportGridCell("Order Date", viewModel.formatDate(item.orderDate)),
                                ReportGridCell("Due Date", viewModel.formatDate(item.dueDate)),
                                ReportGridCell("Delivered At", viewModel.formatDate(item.deliveredAt))
                            )
                        )
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "SLA: ${item.slaStatus.ifBlank { "--" }}")
                }
            }
        )
    }
}