@file:Suppress("DEPRECATION")

package com.cuso.tailor.view.home.logistics.order_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.LogisticsActivityLogDto
import com.cuso.tailor.model.logistics.LogisticsOrderDetailData
import com.cuso.tailor.model.logistics.SalesOrderItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.SheetValue
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.blurScrim
import com.cuso.tailor.viewmodel.LogisticsViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@Composable
fun LogisticsOrderDetailScreen(
    orderId: String,
    viewModel: LogisticsViewModel,
    onClose: () -> Unit,
    onCancelOrderClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val orderDetail by viewModel.orderDetail.collectAsState()
    val isLoading by viewModel.isLoadingDetail.collectAsState()
    val errorMessage by viewModel.detailError.collectAsState()

    // Bottom Sheet Control States
    var allocateSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var updateStatusSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var bottomSheetBlur by remember { mutableStateOf(0.dp) }

    LaunchedEffect(orderId) {
        if (orderId.isNotBlank()) {
            viewModel.fetchOrderDetail(orderId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .blurScrim(bottomSheetBlur),
            topBar = {
                TitleBar(
                    title = "Order Details",
                    onClose = onClose
                )
            },
            bottomBar = {
                // Fixed Bottom Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg)
                        .border(BorderStroke(1.dp, sectionBorder))
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Update Status Button
                        Button(
                            onClick = { updateStatusSheetState = SheetValue.Collapsed },
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = grey_border,
                                contentColor = TextPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(
                                text = "Update Status",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Cancel Order Button
                        Button(
                            onClick = onCancelOrderClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = grey_border,
                                contentColor = TextPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(
                                text = "Cancel Order",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Primary Allocate Button
                    Button(
                        onClick = { allocateSheetState = SheetValue.Collapsed },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            contentColor = whiteBg
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Allocate",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Primary_background)
            ) {
                when {
                    isLoading && orderDetail == null -> {
                        ListSkeleton()
                    }

                    errorMessage != null && orderDetail == null -> {
                        Text(
                            text = errorMessage ?: "Failed to load order details",
                            fontSize = tokens.bodyMedium,
                            color = redText,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    orderDetail != null -> {
                        val detail = orderDetail!!
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = tokens.screenPadding,
                                vertical = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item { OrderHeaderSectionCard(detail) }
                            item { OrderTimelineSectionCard(detail) }
                            item { OrderItemsSectionCard(detail) }
                            item { CustomerDetailsSectionCard(detail) }
                            item { AllocationDetailsSectionCard(detail) }
                            item { PaymentSummarySectionCard(detail) }
                            item { ActivityLogSectionCard(detail) }
                            item { Spacer(Modifier.height(10.dp)) }
                        }
                    }
                }
            }
        }

        // --- Warehouse Allocation BottomSheet ---
        orderDetail?.let { detail ->
            val orderCode = detail.order?.orderCode ?: detail.`package`?.packageNumber ?: "ORD-00000"
            val pincode = detail.order?.customerSnapshot?.shippingAddress?.pincode ?: "600040"
            val itemsSummary = detail.order?.items?.firstOrNull()?.itemDescription ?: "Garments"

            val targetOrderId = detail.order?._id ?: detail.`package`?._id ?: orderId

            WarehouseAllocationBottomSheet(
                sheetState = allocateSheetState,
                onStateChange = { allocateSheetState = it },
                orderId = targetOrderId,
                orderCode = orderCode,
                viewModel = viewModel,
                onBlurScrimChange = { blur, _ -> bottomSheetBlur = blur }
            )

            // --- Update Order Status BottomSheet ---
            val currentStatus = detail.shipment?.status ?: detail.`package`?.status ?: "Packed"
            UpdateOrderStatusBottomSheet(
                sheetState = updateStatusSheetState,
                onStateChange = { updateStatusSheetState = it },
                orderId = orderId,
                orderCode = orderCode,
                currentStatus = currentStatus,
                viewModel = viewModel,
                onBlurScrimChange = { blur, _ -> bottomSheetBlur = blur }
            )
        }
    }
}

// =========================================================================
// REUSABLE SINGLE CARD CONTAINER
// =========================================================================

@Composable
fun OrderSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.cardPadding),
            content = content
        )
    }
}

// =========================================================================
// SECTION CARDS (REUSING OrderSectionCard)
// =========================================================================

@Composable
private fun OrderHeaderSectionCard(detail: LogisticsOrderDetailData) {
    val tokens = LocalAppTokens.current
    val order = detail.order
    val pkg = detail.`package`
    val shipment = detail.shipment

    val orderCode = order?.orderCode ?: pkg?.packageNumber ?: "—"
    val status = shipment?.status ?: pkg?.status ?: "In Transit"
    val paymentMode = order?.paymentMode ?: "Prepaid"
    val dateDisplay = formatIsoDate(pkg?.createdAt ?: order?.dueDate)

    val formattedTotal = remember(order?.grandTotal) {
        formatCurrency(order?.grandTotal ?: 0.0)
    }

    OrderSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Order ID",
                    fontSize = tokens.caption,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = orderCode,
                    fontSize = tokens.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(activity_purple_bg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(chartBlue)
                    )
                    Text(
                        text = status,
                        fontSize = tokens.caption,
                        color = chartBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (dateDisplay.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Created: $dateDisplay",
                fontSize = tokens.caption,
                color = headerGrey,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(headerBg)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Grand Total",
                        fontSize = tokens.caption,
                        color = headerGrey,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formattedTotal,
                        fontSize = tokens.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(activity_purple_bg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = paymentMode,
                        fontSize = tokens.caption,
                        color = chartIndigo,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderTimelineSectionCard(detail: LogisticsOrderDetailData) {
    val tokens = LocalAppTokens.current
    val timeline = detail.logistics?.timeline
    val pkg = detail.`package`
    val currentStatus = (detail.shipment?.status ?: detail.logistics?.status ?: "Created").lowercase()

    OrderSectionCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(activity_purple_bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = chartIndigo,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "Order Timeline",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(16.dp))

        TimelineStepRow(
            icon = Icons.Default.Check,
            iconTint = greentext,
            iconBg = greenBg,
            lineColor = darkGreenBg,
            title = "Created",
            subtitle = "Order placed by customer",
            timestamp = formatShortDateTime(timeline?.createdAt ?: pkg?.createdAt),
            isLast = false
        )

        TimelineStepRow(
            icon = Icons.Default.Check,
            iconTint = greentext,
            iconBg = greenBg,
            lineColor = darkGreenBg,
            title = "Allocated",
            subtitle = detail.allocationDetails?.warehouseName ?: "Assigned to Central Hub",
            timestamp = formatShortDateTime(detail.allocationDetails?.allocatedTime),
            isLast = false
        )

        TimelineStepRow(
            icon = Icons.Default.Check,
            iconTint = greentext,
            iconBg = greenBg,
            lineColor = darkGreenBg,
            title = "Picked",
            subtitle = "Picker processed items",
            timestamp = formatShortDateTime(timeline?.pickedAt),
            isLast = false
        )

        val isPacked = currentStatus.contains("pack") || currentStatus.contains("ship") || currentStatus.contains("deliver")
        TimelineStepRow(
            icon = Icons.Default.Inventory,
            iconTint = if (isPacked) whiteBg else headerGrey,
            iconBg = if (isPacked) chartBlue else grey_border,
            lineColor = if (isPacked) chartBlue else sectionBorder,
            title = "Packed (In Progress)",
            subtitle = "Secure packaging confirmed",
            timestamp = formatShortDateTime(timeline?.packedAt),
            isLast = false
        )

        val isShipped = currentStatus.contains("ship") || currentStatus.contains("deliver")
        TimelineStepRow(
            icon = Icons.Default.LocalShipping,
            iconTint = if (isShipped) greentext else headerGrey,
            iconBg = if (isShipped) greenBg else grey_border,
            lineColor = if (isShipped) darkGreenBg else sectionBorder,
            title = "Shipped",
            subtitle = "Carrier handover awaiting",
            timestamp = if (isShipped) formatShortDateTime(timeline?.inTransitAt) else "Pending",
            isLast = false
        )

        val isDelivered = currentStatus.contains("deliver")
        TimelineStepRow(
            icon = Icons.Default.Home,
            iconTint = if (isDelivered) greentext else headerGrey,
            iconBg = if (isDelivered) greenBg else grey_border,
            lineColor = sectionBorder,
            title = "Delivered",
            subtitle = "Expected arrival at destination",
            timestamp = if (isDelivered) formatShortDateTime(timeline?.deliveredAt) else "Est. ${formatShortDateTime(pkg?.estimatedDelivery)}",
            isLast = true
        )
    }
}

@Composable
private fun TimelineStepRow(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    lineColor: Color,
    title: String,
    subtitle: String,
    timestamp: String,
    isLast: Boolean
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(12.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .background(lineColor)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontSize = tokens.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    fontSize = tokens.caption,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = timestamp,
                fontSize = tokens.caption,
                color = headerGrey,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun OrderItemsSectionCard(detail: LogisticsOrderDetailData) {
    val tokens = LocalAppTokens.current
    val items = detail.order?.items.orEmpty()
    val grandTotal = detail.order?.grandTotal ?: 0.0

    OrderSectionCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(activity_purple_bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = chartIndigo,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "Order Items",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(14.dp))

        items.forEachIndexed { index, item ->
            OrderItemRow(item)
            if (index < items.lastIndex) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = title_border, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(headerBg)
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Subtotal",
                        fontSize = tokens.caption,
                        color = headerGrey,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatCurrency(grandTotal),
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tax (0%)",
                        fontSize = tokens.caption,
                        color = headerGrey,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatCurrency(0.0),
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(color = title_border, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Amount",
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatCurrency(grandTotal),
                        fontSize = tokens.bodyMedium,
                        color = chartIndigo,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderItemRow(item: SalesOrderItemDto) {
    val tokens = LocalAppTokens.current
    val title = item.itemDescription ?: item.customGarment?.categoryDisplayName ?: "Custom Garment"
    val sku = "SKU: ${item.sacCode ?: item._id.takeLast(6).uppercase(LocalLocale.current.platformLocale)}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = sku,
                fontSize = tokens.caption,
                color = headerGrey,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Qty: ${item.quantity} • Rate: ${formatCurrency(item.unitPrice)}",
                fontSize = tokens.caption,
                color = headerGrey,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = formatCurrency(item.lineTotal),
            fontSize = tokens.bodySmall,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CustomerDetailsSectionCard(detail: LogisticsOrderDetailData) {
    val tokens = LocalAppTokens.current
    val customer = detail.order?.customerSnapshot
    val address = customer?.shippingAddress?.fullDisplayAddress()
        ?: detail.order?.formattedAddress
        ?: "No shipping address provided"

    OrderSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CUSTOMER DETAILS",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = chartIndigo
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(activity_purple_bg)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Verified User",
                    fontSize = tokens.label,
                    color = chartBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(activity_purple_bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = chartBlue,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = customer?.name ?: "Customer",
                    fontSize = tokens.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = customer?.phone ?: "—",
                    fontSize = tokens.caption,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = headerGrey,
                modifier = Modifier.size(16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Shipping Address",
                    fontSize = tokens.caption,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = address,
                    fontSize = tokens.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AllocationDetailsSectionCard(detail: LogisticsOrderDetailData) {
    val tokens = LocalAppTokens.current
    val allocation = detail.allocationDetails
    val warehouseName = allocation?.warehouseName ?: allocation?.warehouse ?: "Main Warehouse"
    val allocatedTime = formatShortDateTime(allocation?.allocatedTime)

    OrderSectionCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(activity_purple_bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = chartIndigo,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "Allocation Details",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Warehouse",
                    fontSize = tokens.caption,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = warehouseName,
                    fontSize = tokens.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Allocated Time",
                    fontSize = tokens.caption,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = allocatedTime,
                    fontSize = tokens.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PaymentSummarySectionCard(detail: LogisticsOrderDetailData) {
    val tokens = LocalAppTokens.current
    val paymentMode = detail.order?.paymentMode ?: "Cash / Card"

    OrderSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(greenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = greentext,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "Payment Summary",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(greenBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Captured",
                    fontSize = tokens.label,
                    color = greentext,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Method:",
                    fontSize = tokens.caption,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Transaction ID:",
                    fontSize = tokens.caption,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = paymentMode,
                    fontSize = tokens.bodySmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "TXN_${detail.order?._id?.takeLast(8)?.uppercase(Locale.getDefault()) ?: "—"}",
                    fontSize = tokens.caption,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ActivityLogSectionCard(detail: LogisticsOrderDetailData) {
    val tokens = LocalAppTokens.current
    val activityLogs = detail.logistics?.activityLog.orEmpty()
    var isHistoryExpanded by remember { mutableStateOf(false) }

    val displayedLogs = if (isHistoryExpanded) activityLogs else activityLogs.take(3)

    OrderSectionCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(activity_purple_bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = chartIndigo,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "Activity Log",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Spacer(Modifier.height(14.dp))

        if (displayedLogs.isEmpty()) {
            Text(
                text = "No activity logs available",
                fontSize = tokens.caption,
                color = headerGrey,
                fontWeight = FontWeight.Medium
            )
        } else {
            displayedLogs.forEach { log ->
                ActivityLogItemRow(log)
                Spacer(Modifier.height(10.dp))
            }
        }

        if (activityLogs.size > 3) {
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = { isHistoryExpanded = !isHistoryExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                border = BorderStroke(1.dp, sectionBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    text = if (isHistoryExpanded) "Show Less" else "View Full History",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ActivityLogItemRow(log: LogisticsActivityLogDto) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(chartBlue)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = log.action ?: "Activity",
                fontSize = tokens.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            if (!log.note.isNullOrBlank()) {
                Text(
                    text = log.note,
                    fontSize = tokens.caption,
                    color = headerGrey,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = formatShortDateTime(log.timestamp),
                fontSize = tokens.caption,
                color = headerGrey,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// =========================================================================
// UTILITY DATE & CURRENCY FORMATTERS
// =========================================================================

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount)
}

private fun formatIsoDate(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(isoString) ?: return isoString
        val formatter = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        formatter.format(date)
    } catch (_: Exception) {
        isoString
    }
}

private fun formatShortDateTime(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(isoString) ?: return isoString
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        formatter.format(date)
    } catch (_: Exception) {
        isoString
    }
}