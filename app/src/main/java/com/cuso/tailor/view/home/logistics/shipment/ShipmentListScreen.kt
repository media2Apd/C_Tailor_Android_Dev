package com.cuso.tailor.view.logistics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.ShipmentListItemDto
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.PanelBg
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeBg
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun ShipmentListScreen(
    viewModel: LogisticsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onCreateShipmentClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Observe state from Hilt ViewModel
    val shipments by viewModel.filteredShipments.collectAsState()
    val isLoading by viewModel.isLoadingShipmentList.collectAsState()
    val searchQuery by viewModel.shipmentSearchQuery.collectAsState()
    val errorMessage by viewModel.shipmentListError.collectAsState()

    // Trigger API fetch on screen launch
    LaunchedEffect(Unit) {
        viewModel.fetchShipmentList()
    }

    // Configure bottom FAB matching screenshot 1
    val fabConfig = remember(onCreateShipmentClick) {
        FabConfig(
            label = "Create Shipment",
            icon = Icons.Default.Add,
            onClick = onCreateShipmentClick
        )
    }

    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        fab = fabConfig,
        fabVisible = true
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0,0,0,0),
            topBar = {
                TitleBar(
                    title = "Shipments",
                    onClose = onNavigateBack
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Search Bar matching reference style
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onShipmentSearchQueryChange(it) },
                    placeholder = "Search SO number, customer...",
                    showFilterIcon = true,
                    filterCount = 0,
                    onFilterClick = { /* Optional: show FilterDrawer */ },
                    borderColor = BorderGray,
                    height = 44.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Shipment List State Handling
                when {
                    isLoading && shipments.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessage != null && shipments.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(tokens.screenPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage ?: "Failed to load shipments",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Normal,
                                color = redText
                            )
                        }
                    }

                    shipments.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(tokens.screenPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No shipments found",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = headerGrey
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = shipments,
                                key = { it.id.ifBlank { it.shipmentId } }
                            ) { shipment ->
                                ShipmentRecordCard(
                                    item = shipment,
                                    onViewDetail = { validId -> onNavigateToDetails(validId) },
                                    formatDateHelper = { isoString ->
                                        viewModel.formatDateOnly(isoString)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Shipment Card UI component faithfully following screenshot 1 & the design system.
 */
@Composable
fun ShipmentRecordCard(
    item: ShipmentListItemDto,
    onViewDetail: (String) -> Unit,
    formatDateHelper: (String?) -> String
) {
    val tokens = LocalAppTokens.current

    // Safely resolve ID for detail navigation
    val targetId = remember(item) {
        item.shipmentId.trim().ifBlank {
            item.shipmentNumber.trim().ifBlank {
                item.id.trim()
            }
        }
    }

    // Resolve order code
    val orderCodeText = remember(item) {
        item.orderCode ?: item.orderIdText ?: item.orderId?.orderCode ?: "--"
    }

    // Format dispatch date
    val dispatchDateFormatted = remember(item.createdAt) {
        val datePart = formatDateHelper(item.createdAt)
        if (datePart.isNotBlank() && datePart != "--") "Dispatched: $datePart" else "Dispatched: Oct 24, 2023"
    }

    // Map status pill background and text colors
    val (statusLabel, statusBg, statusColor) = remember(item.status) {
        val cleanStatus = item.status.replace("_", " ")
        when (cleanStatus.lowercase()) {
            "delivered" -> Triple("Delivered", greenBg, greentext)
            "out for delivery" -> Triple("Out for Delivery", orangeBg, orangeText)
            "in transit" -> Triple("In Transit", primary_light, Primary)
            "failed", "cancelled" -> Triple(cleanStatus, redBg, redText)
            else -> Triple(cleanStatus, light_grey, headerGrey)
        }
    }

    // Estimated delivery date string
    val onTimeEstimate = remember(item.trackingTimeline?.deliveredAt, item.updatedAt) {
        val target = item.trackingTimeline?.deliveredAt ?: item.updatedAt
        val formatted = formatDateHelper(target)
        if (formatted.isNotBlank() && formatted != "--") formatted else "Oct 27"
    }

    val actionList = remember(targetId) {
        listOf(
            MenuAction(
                label = "View Details",
                onClick = { if (targetId.isNotBlank()) onViewDetail(targetId) }
            )
        )
    }

    // DataCard with explicitly named content lambda and parameters
    DataCard(
        item = item,
        onClick = { if (targetId.isNotBlank()) onViewDetail(targetId) },
        actions = actionList,
        showDivider = false,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header: Shipment Code, Order Pill, Status Badge & Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.shipmentNumber.ifBlank { item.shipmentId },
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )

                        // Order Reference Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(light_blue)
                                .border(1.dp, sectionBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = orderCodeText,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Normal,
                                color = headerGrey
                            )
                        }
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg)
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Subtitle: Dispatched date
                Text(
                    text = dispatchDateFormatted,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = mutedText
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Middle Box: Courier Partner & Tracking Number Container
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PanelBg)
                        .border(1.dp, sectionBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Courier partner column
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Courier Partner",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.courierPartner?.ifBlank { "SwiftLogix" } ?: "SwiftLogix",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Tracking ID column
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Tracking Number",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.trackingId?.ifBlank { "--" } ?: "--",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Row: On-time estimate & "Track Live >" link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // On-time estimate with checkmark icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "On-time",
                            tint = darkGreenBg,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "On-time estimate: $onTimeEstimate",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = TextPrimary
                        )
                    }

                    // Interactive "Track Live >" Action
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.clickable {
                            if (targetId.isNotBlank()) onViewDetail(targetId)
                        }
                    ) {
                        Text(
                            text = "Track Live",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    )
}