@file:Suppress("DEPRECATION", "AssignedValueIsNeverRead", "unused")

package com.cuso.tailor.view.home.logistics.delivery_management.dispatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
fun DispatchListScreen(
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
    var selectedShipment by remember {
        mutableStateOf<ShipmentItemDto?>(null)
    }

    var showDispatchDialog by remember {
        mutableStateOf(false)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        topBar = {
            TitleBar(
                title = "Shipments & Delivery",
                onClose = onClose
            )
        },
        containerColor = Color.Transparent
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(paddingValues)
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

            when {
                    isLoading && shipments.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessage != null && shipments.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage ?: "Failed to load shipments",
                                fontSize = tokens.bodyMedium,
                                color = redText
                            )
                        }
                    }

                    filteredList.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No shipments found",
                                fontSize = tokens.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                                .background(Color.Transparent)
                        ) {
                            items(
                                items = filteredList,
                                key = { it._id }
                            ) { item ->
                                ShipmentDataCard(
                                    item = item,
                                    onCardClick = { onShipmentClick(item) },
                                    onActionClick = {
                                        selectedShipment = item
                                        showDispatchDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
        }
    }
    if (showDispatchDialog && selectedShipment != null) {
        DispatchConfirmationDialog(
            item = selectedShipment!!,
            onDismiss = {
                showDispatchDialog = false
                selectedShipment = null
            },
            onConfirm = {
                val shipment = selectedShipment ?: return@DispatchConfirmationDialog

                viewModel.markShipmentDispatched(
                    shipmentId = shipment._id,
                    onSuccess = {
                        showDispatchDialog = false
                        selectedShipment = null

                        DynamicIslandManager.showSuccess(
                            "Shipment marked as dispatched"
                        )
                    }
                )
            }
        )
    }
}

@Composable
fun DispatchConfirmationDialog(
    item: ShipmentItemDto,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Confirm Dispatch",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF172033)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Text(
                    text = "Review the shipment details before final authorization.",
                    fontSize = tokens.bodySmall,
                    color = Color(0xFF64748B)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    DispatchDetailRow("Order ID", item.orderId ?: "—")
                    HorizontalDivider(color = Color(0xFFE8EDF3))
                    DispatchDetailRow("Shipment ID", item.shipmentId ?: "—")
                    HorizontalDivider(color = Color(0xFFE8EDF3))
                    DispatchDetailRow("Courier Name", item.courier ?: "—")
                    HorizontalDivider(color = Color(0xFFE8EDF3))
                    DispatchDetailRow("Ready Time", "—")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = "Once dispatched, this action cannot be undone.",
                        color = Color.Red,
                        fontSize = tokens.label
                    )
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Text("Confirm Dispatch")
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

@Composable
private fun DispatchDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = mutedText,
            fontSize = 14.sp
        )

        Text(
            text = value,
            color = TitleColor  ,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium
        )
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
    val isReady = statusText.equals("Ready", ignoreCase = true)
    val actionText = if (isReady) "Mark Dispatched" else "Dispatched"

    val (statusBg, statusTextColor) = when (statusText.lowercase()) {
        "delivered", "shipped" -> Pair(greenBg, greentext)
        "ready" -> Pair(headerBg, headerGrey)
        "in_transit" -> Pair(activity_purple_bg, chartBlue)
        else -> Pair(light_grey, TextSecondary)
    }
    var selectedShipment by remember {
        mutableStateOf<ShipmentItemDto?>(null)
    }

    var showDispatchDialog by remember {
        mutableStateOf(false)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
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
                            onClick = {
                                if (isReady) {
                                    onActionClick()
                                }
                            },
                            enabled = isReady,
                            modifier = Modifier.height(tokens.buttonHeight * 0.85f),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isReady) Primary else light_grey,
                                contentColor = if (isReady) whiteBg else TextSecondary,
                                disabledContainerColor = light_grey,
                                disabledContentColor = TextSecondary
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 0.dp
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                disabledElevation = 0.dp
                            )
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