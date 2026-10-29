@file:Suppress("DEPRECATION")

package com.cuso.tailor.view.home.logistics.order_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.SheetValue
import com.cuso.tailor.view.composable.SmoothBottomSheet
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun WarehouseAllocationBottomSheet(
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    orderId: String,
    orderCode: String,
    itemsSummary: String = "",
    viewModel: LogisticsViewModel,
    onBlurScrimChange: (Dp, Float) -> Unit = { _, _ -> }
) {
    val tokens = LocalAppTokens.current
    val allocationData by viewModel.warehouseAllocationData.collectAsState()
    val isLoading by viewModel.isLoadingWarehouses.collectAsState()
    val isSubmitting by viewModel.isSubmittingAction.collectAsState()

    var isAutoAllocate by remember { mutableStateOf(true) }
    var selectedWarehouseId by remember { mutableStateOf<String?>(null) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Fetch warehouse options when the sheet opens
    LaunchedEffect(sheetState, orderId) {
        if (sheetState != SheetValue.Hidden && orderId.isNotBlank()) {
            viewModel.fetchAvailableWarehouses(orderId)
        }
    }

    val warehouses = allocationData?.warehouses.orEmpty()

    // Automatically select the recommended or first in-stock warehouse when loaded
    LaunchedEffect(warehouses) {
        if (selectedWarehouseId == null && warehouses.isNotEmpty()) {
            val defaultChoice = warehouses.firstOrNull { it.isRecommended && !it.isOutOfStock }
                ?: warehouses.firstOrNull { !it.isOutOfStock }
                ?: warehouses.first()
            selectedWarehouseId = defaultChoice._id
        }
    }

    // Helper map to bridge FormDropdown String labels with WarehouseItemDto
    val warehouseOptionsList = remember(warehouses) {
        warehouses.map { wh ->
            if (!wh.code.isNullOrBlank()) "${wh.name} (${wh.code})" else wh.name.orEmpty()
        }
    }

    val selectedWarehouse = warehouses.find { it._id == selectedWarehouseId }
    val selectedDropdownLabel = remember(selectedWarehouse) {
        selectedWarehouse?.let { wh ->
            if (!wh.code.isNullOrBlank()) "${wh.name} (${wh.code})" else wh.name.orEmpty()
        } ?: ""
    }

    val displayPincode = allocationData?.deliveryPincode ?: "—"
    val displayOrderCode = allocationData?.orderId ?: orderCode

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        collapsedFraction = 0.88f,
        expandedFraction = 0.95f,
        dragCloseEnabled = true,
        scrollableContent = true,
        sheetBackgroundColor = whiteBg,
        onBlurScrimChange = onBlurScrimChange,
        headerContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Warehouse Allocation",
                    fontSize = tokens.caption,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "#$displayOrderCode",
                    fontSize = tokens.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info Cards: Delivery Pincode & Order ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Delivery Pincode Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = headerBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(activity_purple_bg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Delivery Pincode",
                                fontSize = tokens.label,
                                color = headerGrey,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = displayPincode,
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Order ID Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = headerBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(activity_purple_bg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Order ID",
                                fontSize = tokens.label,
                                color = headerGrey,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = displayOrderCode,
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Segmented Switch: Auto Allocate / Manual Selection
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(headerBg)
                    .border(BorderStroke(1.dp, sectionBorder), RoundedCornerShape(tokens.cardCornerRadius))
                    .padding(3.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Auto Allocate Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(if (isAutoAllocate) whiteBg else Color.Transparent)
                            .border(
                                width = if (isAutoAllocate) 1.dp else 0.dp,
                                color = if (isAutoAllocate) Primary else Color.Transparent,
                                shape = RoundedCornerShape(tokens.cardCornerRadius)
                            )
                            .clickable {
                                isAutoAllocate = true
                                val rec = warehouses.firstOrNull { it.isRecommended && !it.isOutOfStock }
                                    ?: warehouses.firstOrNull { !it.isOutOfStock }
                                if (rec != null) selectedWarehouseId = rec._id
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (isAutoAllocate) Primary else headerGrey,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Auto Allocate",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isAutoAllocate) Primary else headerGrey
                            )
                        }
                    }

                    // Manual Selection Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(if (!isAutoAllocate) whiteBg else Color.Transparent)
                            .border(
                                width = if (!isAutoAllocate) 1.dp else 0.dp,
                                color = if (!isAutoAllocate) Primary else Color.Transparent,
                                shape = RoundedCornerShape(tokens.cardCornerRadius)
                            )
                            .clickable { isAutoAllocate = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Manual selection",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (!isAutoAllocate) Primary else headerGrey
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                }
            } else if (isAutoAllocate) {
                // ==========================================
                // 1. AUTO ALLOCATE VIEW (LIST OF WAREHOUSES)
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Warehouse Name",
                        fontSize = tokens.caption,
                        color = headerGrey,
                        fontWeight = FontWeight.Medium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Text(
                            text = "Location",
                            fontSize = tokens.caption,
                            color = headerGrey,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Stock",
                            fontSize = tokens.caption,
                            color = headerGrey,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Zone/Distance",
                            fontSize = tokens.caption,
                            color = headerGrey,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                warehouses.forEach { wh ->
                    val isSelected = selectedWarehouseId == wh._id

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .clickable(enabled = !wh.isOutOfStock) { selectedWarehouseId = wh._id },
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        color = whiteBg,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Primary else sectionBorder
                        )
                    ) {
                        // Compact single row without excessive vertical padding and without nested stretching
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Section: Radio + Name/Location
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Radio Selection Indicator
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isSelected) 5.dp else 1.5.dp,
                                            color = if (isSelected) Primary else headerGrey,
                                            shape = CircleShape
                                        )
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = wh.name ?: wh.code ?: "",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = if (wh.isOutOfStock) headerGrey else TextPrimary
                                        )

//                                        if (wh.isRecommended) {
//                                            Box(
//                                                modifier = Modifier
//                                                    .clip(RoundedCornerShape(8.dp))
//                                                    .background(activity_purple_bg)
//                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
//                                            ) {
//                                                Text(
//                                                    text = "Recommended",
//                                                    fontSize = tokens.label,
//                                                    color = Primary,
//                                                    fontWeight = FontWeight.Medium
//                                                )
//                                            }
//                                        }
                                    }

                                    Text(
                                        text = wh.location ?: "—",
                                        fontSize = tokens.caption,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = headerGrey,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            // Right Section: Stock + Distance
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (wh.isOutOfStock) redText else greentext)
                                    )
                                    Text(
                                        text = if (wh.isOutOfStock) "Out of stock" else (wh.stock ?: "In Stock"),
                                        fontSize = tokens.caption,
                                        color = if (wh.isOutOfStock) redText else TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    text = wh.zoneDistanceKm ?: "—",
                                    fontSize = tokens.caption,
                                    color = if (isSelected) Primary else headerGrey,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else {
                // ========================================================
                // 2. MANUAL SELECTION VIEW (REUSING REUSABLE FormDropdown)
                // ========================================================
                FormDropdown(
                    label = "Warehouse",
                    value = selectedDropdownLabel.ifBlank { "Select a warehouse" },
                    expanded = isDropdownExpanded,
                    onExpandChange = { isDropdownExpanded = it },
                    options = warehouseOptionsList,
                    onOptionSelected = { selectedLabel ->
                        val matchingWarehouse = warehouses.find { wh ->
                            val label = if (!wh.code.isNullOrBlank()) "${wh.name} (${wh.code})" else wh.name.orEmpty()
                            label == selectedLabel
                        }
                        if (matchingWarehouse != null && !matchingWarehouse.isOutOfStock) {
                            selectedWarehouseId = matchingWarehouse._id
                        }
                        isDropdownExpanded = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            // Optimization Note Banner
            val warehouseNameForNote = selectedWarehouse?.name ?: "Selected Warehouse"
            Text(
                text = "Allocating $warehouseNameForNote will optimize delivery for tomorrow",
                fontSize = tokens.caption,
                color = headerGrey,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(4.dp))

            // Action Buttons: Cancel and Confirm Allocation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onStateChange(SheetValue.Hidden) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = tokens.bodySmall,
                        color = headerGrey,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        val targetWhId = selectedWarehouseId ?: warehouses.firstOrNull()?._id
                        if (targetWhId != null) {
                            viewModel.confirmWarehouseAllocation(
                                orderId = orderId,
                                warehouseId = targetWhId,
                                isAutoAllocate = isAutoAllocate,
                                onSuccess = {
                                    DynamicIslandManager.showSuccess("Warehouse allocated successfully")
                                    onStateChange(SheetValue.Hidden)
                                }
                            )
                        }
                    },
                    enabled = !isSubmitting && selectedWarehouseId != null,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = whiteBg,
                        disabledContainerColor = disabled
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = whiteBg,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "Confirm Allocation",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}