@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.logistics.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalShipping
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
import com.cuso.tailor.model.logistics.DeliveryPerformanceItemDto
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.primary_light
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
fun DeliveryPerformanceReportScreen(
    onClose: () -> Unit,
    onViewDetail: (DeliveryPerformanceItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.deliveryPerfSummary.collectAsState()
    val pagingState by viewModel.deliveryPerfPaging.collectAsState()
    val filteredList by viewModel.filteredDeliveryPerfList.collectAsState()
    val searchQuery by viewModel.deliveryPerfSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Delivery Status",
                    options = listOf(
                        FilterOption("Assigned", "Assigned"),
                        FilterOption("In Transit", "In Transit"),
                        FilterOption("Delivered", "Delivered"),
                        FilterOption("Failed", "Failed")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Priority",
                    options = listOf(
                        FilterOption("Normal", "Normal"),
                        FilterOption("High", "High"),
                        FilterOption("Urgent", "Urgent")
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
        viewModel.loadDeliveryPerformanceReport()
    }

    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL DELIVERIES",
                value = (summary?.totalDeliveries ?: 0).toString(),
                subtitle = "All dispatched orders",
                icon = Icons.AutoMirrored.Filled.ListAlt,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "DELIVERY SLA RATE",
                value = summary?.successRate ?: "0%",
                subtitle = "${summary?.deliveredCount ?: 0} delivered successfully",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "ACTIVE IN-TRANSIT",
                value = (summary?.inTransitCount ?: 0).toString(),
                subtitle = "Out for delivery parcels",
                icon = Icons.Default.LocalShipping,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "FAILED ATTEMPTS",
                value = (summary?.failedAttemptsCount ?: 0).toString(),
                subtitle = "${summary?.trialsCompletedCount ?: 0} trials finished",
                icon = Icons.Default.ErrorOutline,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Delivery Performance Report",
        subtitle = null,
        sectionTitle = "Delivery Performance Ledger",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onDeliveryPerfSearchChange,
        searchPlaceholder = "Search by delivery, order or courier...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Delivery Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val priorities = updated.find { it.title == "Priority" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyDeliveryPerfFilters(statuses, priorities)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearDeliveryPerfFilters()
        },
        onLoadMore = { viewModel.loadDeliveryPerformanceReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Delivery performance report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Delivery performance report export started") },
        onClose = onClose,
        emptyMessage = "No delivery performance records found."
    ) { item ->
        val badgeTextColor = when {
            item.statusBadge.equals("Delivered", ignoreCase = true) -> greentext
            item.statusBadge.equals("Failed", ignoreCase = true) -> redText
            item.statusBadge.equals("In Transit", ignoreCase = true) -> yellowText
            else -> Primary
        }
        val badgeBgColor = when {
            item.statusBadge.equals("Delivered", ignoreCase = true) -> greenBg
            item.statusBadge.equals("Failed", ignoreCase = true) -> redBg
            item.statusBadge.equals("In Transit", ignoreCase = true) -> yellowBg
            else -> primary_light
        }
        val courierOrStaff = item.assignedTo.ifBlank { item.courierPartner }.ifBlank { "--" }

        DataCard(
            item = item,
            title = item.customerName.ifBlank { "--" },
            subtitle = "${item.deliveryNumber.ifBlank { "--" }}  •  ${item.orderCode.ifBlank { "--" }}",
            topBadgeText = item.statusBadge.ifBlank { "Assigned" },
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
                    ReportInlineMetricRow(label = "Garment / Item") {
                        Text(
                            text = item.garmentName.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Client Trial", item.trialCompleted.ifBlank { "--" }),
                                ReportGridCell("Courier / Staff", courierOrStaff),
                                ReportGridCell("Failure Note", item.failureReason.ifBlank { "None" })
                            ),
                            listOf(
                                ReportGridCell("Target / Promise Date", viewModel.formatDate(item.targetDate)),
                                ReportGridCell("Priority", item.priority.ifBlank { "Normal" })
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Created on ${viewModel.formatDate(item.createdAt)}")
                }
            }
        )
    }
}