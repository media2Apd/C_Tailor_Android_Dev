@file:Suppress("DEPRECATION")

package com.cuso.tailor.view.home.logistics.delivery.dispatch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.ShipmentItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun ShipmentListScreen(
    viewModel: LogisticsViewModel,
    onClose: () -> Unit,
    onShipmentClick: (ShipmentItemDto) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val shipments by viewModel.shipmentsList.collectAsState()
    val searchQuery by viewModel.shipmentSearchQuery.collectAsState()
    val isLoading by viewModel.isLoadingShipments.collectAsState()
    val errorMessage by viewModel.shipmentError.collectAsState()

    // Load shipments on initial composition
    LaunchedEffect(Unit) {
        viewModel.fetchShipments()
    }

    // Filter list based on orderId, shipmentId, or courier
    val filteredList = remember(shipments, searchQuery) {
        if (searchQuery.isBlank()) shipments
        else {
            shipments.filter { item ->
                (item.orderId?.contains(searchQuery, ignoreCase = true) == true) ||
                        (item.shipmentId?.contains(searchQuery, ignoreCase = true) == true) ||
                        (item.courier?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = "Shipments & Delivery",
                onClose = onClose
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Transparent)
        ) {
            // Search & Filter Component
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.onShipmentSearchQueryChange(it) },
                placeholder = "Search order, shipment ID, courier...",
                height = tokens.fieldHeight,
                showFilterIcon = true,
                filterCount = 0,
                onFilterClick = {
                    // Trigger filter drawer if needed
                }
            )

            // Content List
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    isLoading && shipments.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessage != null && shipments.isEmpty() -> {
                        Text(
                            text = errorMessage ?: "Failed to load shipments",
                            fontSize = tokens.bodyMedium,
                            color = redText,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    filteredList.isEmpty() -> {
                        Text(
                            text = "No shipments found",
                            fontSize = tokens.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = filteredList,
                                key = { it._id }
                            ) { item ->
                                ShipmentDataCard(
                                    item = item,
                                    onCardClick = { onShipmentClick(item) },
                                    onActionClick = {
                                        viewModel.markShipmentDispatched(
                                            shipmentId = item._id,
                                            onSuccess = {
                                                DynamicIslandManager.showSuccess("Shipment marked as dispatched")
                                            }
                                        )
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
 * Single Shipment Card using DataCard layout
 */
@Composable
fun ShipmentDataCard(
    item: ShipmentItemDto,
    onCardClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val orderIdText = item.orderId ?: "—"
    val shipmentIdText = item.shipmentId ?: "—"
    val courierText = item.courier ?: "Standard Courier"
    val statusText = item.status ?: "Ready"
    val actionText = item.action ?: "Mark Dispatched"

    val (statusBg, statusTextColor) = when (statusText.lowercase()) {
        "delivered", "shipped" -> Pair(greenBg, greentext)
        "ready" -> Pair(headerBg, headerGrey)
        "in_transit" -> Pair(activity_purple_bg, chartBlue)
        else -> Pair(light_grey, TextSecondary)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        color = whiteBg
    ) {
        DataCard(
            item = item,
            onClick = { onCardClick() },
            headerContent = {
                // Top Header Row: Order ID & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = orderIdText,
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )

                    StatusBadge(
                        text = statusText,
                        bgColor = statusBg,
                        textColor = statusTextColor,
                        cornerRadius = 14.dp,
                        showDot = false
                    )
                }
            },
            content = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Inner Details Container: Shipment ID & Courier Partner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                            .background(headerBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: Shipment ID
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "SHIPMENT ID",
                                    fontSize = tokens.label,
                                    color = headerGrey,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = shipmentIdText,
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Column 2: Courier Partner
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "COURIER",
                                    fontSize = tokens.label,
                                    color = headerGrey,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = courierText,
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Bottom Row: Action Button ("Mark Dispatched")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onActionClick,
                            modifier = Modifier.height(tokens.buttonHeight * 0.85f),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = whiteBg
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(
                                text = actionText,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        )
    }
}