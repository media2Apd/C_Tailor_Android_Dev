@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.logistics.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
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
import com.cuso.tailor.model.logistics.ShipmentTrackingItemDto
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
fun ShipmentTrackingReportScreen(
    onClose: () -> Unit,
    onViewDetail: (ShipmentTrackingItemDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val summary by viewModel.shipmentTrackingSummary.collectAsState()
    val pagingState by viewModel.shipmentTrackingPaging.collectAsState()
    val filteredList by viewModel.filteredShipmentTrackingList.collectAsState()
    val searchQuery by viewModel.shipmentTrackingSearch.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Shipment Status",
                    options = listOf(
                        FilterOption("Created", "Created"),
                        FilterOption("In Transit", "In Transit"),
                        FilterOption("Out for Delivery", "Out for Delivery"),
                        FilterOption("Delivered", "Delivered"),
                        FilterOption("Failed", "Failed")
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
        viewModel.loadShipmentTrackingReport()
    }

    val metrics = remember(summary) {
        val totalWeight = summary?.totalWeightKg ?: 0.0
        val weightLabel = if (totalWeight % 1.0 == 0.0) "${totalWeight.toInt()} kg" else "$totalWeight kg"
        listOf(
            ReportMetricSpec(
                label = "TOTAL SHIPMENTS",
                value = (summary?.totalShipments ?: 0).toString(),
                subtitle = "$weightLabel total weight",
                icon = Icons.Default.Inventory2,
                valueColor = TextPrimary,
                iconTint = purpleText,
                iconBgColor = purpleBg
            ),
            ReportMetricSpec(
                label = "ACTIVE IN-TRANSIT",
                value = (summary?.inTransitCount ?: 0).toString(),
                subtitle = "${summary?.outForDeliveryCount ?: 0} out for delivery",
                subtitleColor = yellowText,
                icon = Icons.Default.LocalShipping,
                valueColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "DELIVERED PARCELS",
                value = (summary?.deliveredCount ?: 0).toString(),
                subtitle = "Successfully received",
                subtitleColor = greentext,
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "FAILED DISPATCHES",
                value = (summary?.failedShipmentsCount ?: 0).toString(),
                subtitle = "Carrier exceptions",
                subtitleColor = redText,
                icon = Icons.Default.WarningAmber,
                valueColor = redText,
                iconTint = redText,
                iconBgColor = redBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Shipment & Tracking Report",
        subtitle = null,
        sectionTitle = "Shipment & Tracking Directory",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onShipmentTrackingSearchChange,
        searchPlaceholder = "Search by AWB, shipment, order or recipient...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Shipment Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            viewModel.applyShipmentTrackingFilters(statuses)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearShipmentTrackingFilters()
        },
        onLoadMore = { viewModel.loadShipmentTrackingReport(isLoadMore = true) },
        onShareClick = { DynamicIslandManager.showSuccess("Shipment tracking report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Shipment tracking report export started") },
        onClose = onClose,
        emptyMessage = "No shipment tracking records found."
    ) { item ->
        val badgeTextColor = when {
            item.statusBadge.equals("Delivered", ignoreCase = true) ||
                    item.statusBadge.equals("Created", ignoreCase = true) -> greentext
            item.statusBadge.equals("Failed", ignoreCase = true) -> redText
            item.statusBadge.contains("Transit", ignoreCase = true) -> yellowText
            else -> Primary
        }
        val badgeBgColor = when {
            item.statusBadge.equals("Delivered", ignoreCase = true) ||
                    item.statusBadge.equals("Created", ignoreCase = true) -> greenBg
            item.statusBadge.equals("Failed", ignoreCase = true) -> redBg
            item.statusBadge.contains("Transit", ignoreCase = true) -> yellowBg
            else -> primary_light
        }

        DataCard(
            item = item,
            title = "${item.recipientName.ifBlank { "--" }}  •  ${item.shipmentNumber.ifBlank { "--" }}",
            subtitle = "AWB: ${item.trackingId.ifBlank { "--" }}",
            topBadgeText = item.statusBadge.ifBlank { "Created" },
            topBadgeTextColor = badgeTextColor,
            topBadgeBgColor = badgeBgColor,
            topBadgeInline = true,
            actions = listOf(
                MenuAction(
                    label = "View Full Trail",
                    icon = Icons.Default.Visibility,
                    onClick = { onViewDetail(item) }
                )
            ),
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Courier Partner") {
                        Text(
                            text = item.courierPartner.ifBlank { "--" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("Destination", item.destination.ifBlank { "--" }),
                                ReportGridCell("Order #", item.orderCode.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Live Hub Location", item.currentLocation.ifBlank { "--" }),
                                ReportGridCell("Weight", item.weight.ifBlank { "--" })
                            ),
                            listOf(
                                ReportGridCell("Dispatched Date", viewModel.formatDate(item.dispatchedAt)),
                                ReportGridCell("Milestone", item.statusBadge.ifBlank { "--" })
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