@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "Unused_parameter",
    "VariableNeverRead",
    "SameParameterValue",
    "unused",
    "unusedVariable"
)

package com.cuso.tailor.view.home.sales.sales_order

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.WarehouseDropdownItem
import com.cuso.tailor.model.sales.OrderOverviewData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.CirculerProgressIndicatorReuse
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.view.home.pdfgenerator.OrderReceiptPdfGenerator
import com.cuso.tailor.viewmodel.OrderOverviewState
import com.cuso.tailor.viewmodel.OrderOverviewViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun OrderOverviewScreen(
    orderId: String,
    onClose: () -> Unit = {},
    onEditOrder: (OrderReviewData) -> Unit = {},
    onCreateNew: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val viewModel: OrderOverviewViewModel = hiltViewModel(key = "order_overview_view_$orderId")
    val state by viewModel.overviewState.collectAsStateWithLifecycle()

    val warehouseList by viewModel.warehouseList.collectAsStateWithLifecycle()


    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orderId) {
        viewModel.fetchSalesOverview(orderId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(title = "Order Details", onClose = onClose)
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (val s = state) {
                    is OrderOverviewState.Loading, OrderOverviewState.Idle -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CirculerProgressIndicatorReuse()
                        }
                    }
                    is OrderOverviewState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Failed to load order",
                                    color = redText,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = s.message,
                                    color = mutedText,
                                    fontSize = tokens.bodySmall
                                )
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = { viewModel.fetchSalesOverview(orderId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                ) {
                                    Text("Retry", fontSize = tokens.bodySmall, color = whiteBg)
                                }
                            }
                        }
                    }
                    is OrderOverviewState.Success -> {
                        val orderData = s.data
                        OrderOverviewContent(
                            orderData = orderData,
                            warehouseList = warehouseList, // <-- Pass warehouseList here
                            context = context,
                            onCancelOrder = { errorMessage = "Order cancellation initiated" },
                            onPrintInvoice = { printReceipt(context, orderData) },
                            onTrackOrder = { successMessage = "Tracking details refreshed" }
                        )
                    }
                }
            }
        }

        DynamicIslandSuccess(
            modifier = Modifier.align(Alignment.TopCenter),
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// MAIN ORDER DETAILS CONTENT
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun OrderOverviewContent(
    orderData: OrderOverviewData,
    warehouseList: List<WarehouseDropdownItem>,
    context: Context,
    onCancelOrder: () -> Unit,
    onPrintInvoice: () -> Unit,
    onTrackOrder: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val order = orderData.order
    val customer = order.customerId
    val snapshot = orderData.customerSnapshot
    val items = orderData.items
    val status = orderData.status?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: " - "
    val isPaid = (orderData.balanceAmount ?: 0.0) <= 0.0
    val paymentType = orderData.paymentType?.replaceFirstChar { it.uppercase() }
        ?: if (isPaid) "Paid" else if ((orderData.advanceAmountPaid ?: 0.0) > 0.0) "Advance" else " - "

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.Transparent)
    ) {
        Spacer(Modifier.height(tokens.screenPadding * 0.5f))

        // --- 1. Order Top Header & Action Buttons ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = orderData.orderCode?.takeIf { it.isNotBlank() } ?: order.orderNumber.ifBlank { " - " },
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(greenBg)
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = status,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = darkGreenBg
                        )
                    }

                    // Payment Type Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(primary_light)
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = paymentType,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Action Buttons Row: [Print Invoice] [Track Order]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPrintInvoice,
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = BorderStroke(1.5.dp, Primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text("Print Invoice", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onTrackOrder,
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = BorderStroke(1.5.dp, Primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text("Track Order", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Cancel Order Button
            OutlinedButton(
                onClick = onCancelOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                border = BorderStroke(1.dp, redText),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = redText)
            ) {
                Text("Cancel Order", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = redText)
            }
        }

        DividerSection()

        // --- 2. Customer Information ---
        SectionContainer(title = "Customer Information") {
            val customerName = snapshot?.name?.takeIf { it.isNotBlank() }
                ?: customer?.name?.takeIf { it.isNotBlank() }
                ?: " - "

            val customerPhone = snapshot?.phone?.takeIf { it.isNotBlank() }
                ?: customer?.mobile?.takeIf { it.isNotBlank() }
                ?: " - "

            val customerEmail = snapshot?.email?.takeIf { it.isNotBlank() }
                ?: customer?.email?.takeIf { it.isNotBlank() }
                ?: " - "

            val addressObj = snapshot?.shippingAddress ?: snapshot?.billingAddress ?: customer?.address
            val customerAddress = listOfNotNull(
                snapshot?.shippingAddress?.flatNo ?: snapshot?.billingAddress?.flatNo,
                snapshot?.shippingAddress?.street ?: snapshot?.billingAddress?.street ?: customer?.address?.addressLine,
                snapshot?.shippingAddress?.areaZone ?: snapshot?.billingAddress?.areaZone,
                snapshot?.shippingAddress?.city ?: snapshot?.billingAddress?.city ?: customer?.address?.city,
                snapshot?.shippingAddress?.subdivisionName ?: snapshot?.billingAddress?.subdivisionName,
                snapshot?.shippingAddress?.pincode ?: snapshot?.billingAddress?.pincode
            ).filter { it.isNotBlank() }.joinToString(", ").ifBlank { " - " }

            KeyValueRow(label = "Name", value = customerName)
            KeyValueRow(label = "Phone", value = customerPhone)
            KeyValueRow(label = "Email", value = customerEmail)
            KeyValueRow(label = "Shipping Address", value = customerAddress)
        }

        DividerSection()

        // --- 3. Product Details ---
        SectionContainer(title = "Product Details") {
            if (items.isEmpty()) {
                Text(
                    text = "No items recorded in this order.",
                    fontSize = tokens.bodySmall,
                    color = mutedText
                )
            } else {
                items.forEachIndexed { index, item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(Primary_background)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val itemName = item.categoryName.takeIf { it.isNotBlank() && it != "Custom Garment" }
                            ?: item.itemDescription?.takeIf { it.isNotBlank() }
                            ?: "Item #${index + 1}"

                        val skuDisplay = item.customGarment?.designName?.takeIf { it.isNotBlank() }
                            ?: item.lineType?.replace("_", " ")?.takeIf { it.isNotBlank() }
                            ?: " - "
                        val quantityDisplay = "${item.quantityNumber ?: item.quantity} ${item.unit ?: "Piece"}"
                        val unitPrice = item.unitPrice ?: item.stitchingCharge
                        val lineTotal = item.lineTotal ?: (unitPrice * (item.quantityNumber ?: 1.0))

                        InnerProductRow("Product", itemName)
                        InnerProductRow("SKU", skuDisplay)
                        InnerProductRow("Quantity", quantityDisplay)
                        InnerProductRow("Unit Price", "₹${formatIndianNumber(unitPrice)}")
                        InnerProductRow("Total", "₹${formatIndianNumber(lineTotal)}", isBold = true)
                    }
                    if (index != items.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Subtotal, GST, Shipping Fee
            val subtotal = orderData.subtotal ?: order.totalAmount
            val gstAmount = orderData.totalTax ?: (order.totalAmount * 0.05)
            val grandTotal = orderData.grandTotal ?: (subtotal + gstAmount)

            PriceSummaryRow("Subtotal", "₹${formatIndianNumber(subtotal)}")
            PriceSummaryRow("GST", "₹${formatIndianNumber(gstAmount)}")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Shipping Fee", fontSize = tokens.bodySmall, color = headerGrey)
                Text(
                    text = if ((orderData.deliveryCharge ?: 0.0) > 0.0) "₹${formatIndianNumber(orderData.deliveryCharge!!)}" else "FREE",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = darkGreenBg
                )
            }

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = sectionBorder)
            Spacer(Modifier.height(6.dp))

            // Grand Total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Grand Total", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = title_color)
                Text("₹${formatIndianNumber(grandTotal)}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
            }
        }

        DividerSection()

        // --- 4. Order Activity Timeline ---
        SectionContainer(title = "Order Activity Timeline") {
            OrderTimelineView(orderData = orderData)
        }

        DividerSection()

        // --- 5. Shipping Details ---
        SectionContainer(title = "Shipping Details") {
            val targetWarehouseId = orderData.warehouseId?.takeIf { it.isNotBlank() }
                ?: orderData.branchId?.takeIf { it.isNotBlank() }

            val warehouseLabel = warehouseList.firstOrNull { it.value == targetWarehouseId }?.label
                ?: orderData.order.branch?.name
                ?: " - "

            KeyValueRow(label = "Warehouse", value = warehouseLabel)
            KeyValueRow(label = "Delivery Method", value = orderData.deliveryMethod?.replace("_", " ")?.ifBlank { " - " } ?: " - ")
            KeyValueRow(label = "Order Notes", value = orderData.orderNotes?.ifBlank { " - " } ?: " - ")
            KeyValueRow(label = "Est. Delivery Date", value = formatOverviewDate(orderData.dueDate))
        }

        DividerSection()

        // --- 6. Payment Details ---
        SectionContainer(title = "Payment Details") {
            val paymentModeDisplay = orderData.paymentMode?.takeIf { it.isNotBlank() } ?: " - "

            KeyValueRow(label = "Payment Type", value = orderData.paymentType?.replaceFirstChar { it.uppercase() } ?: " - ")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payment Status", fontSize = tokens.bodySmall, color = headerGrey)

                val (badgeBg, badgeTextColor) = when {
                    isPaid -> greenBg to darkGreenBg
                    (orderData.advanceAmountPaid ?: 0.0) > 0.0 -> yellowBg to yellowText
                    else -> redBg to redText
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isPaid) "Paid" else if ((orderData.advanceAmountPaid ?: 0.0) > 0.0) "Partial" else "Unpaid",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeTextColor
                    )
                }
            }

            KeyValueRow(label = "Payment Mode", value = paymentModeDisplay)
            KeyValueRow(label = "Payment Date", value = formatOverviewDate(orderData.createdAt ?: orderData.orderDate))
            KeyValueRow(
                label = "Amount Paid",
                value = "₹${formatIndianNumber(orderData.advanceAmountPaid ?: order.totalPaid)}",
                isValueBold = true
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────
// TIMELINE COMPONENT
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun OrderTimelineView(orderData: OrderOverviewData) {
    val tokens = LocalAppTokens.current
    val isCompleted = orderData.status.equals("completed", ignoreCase = true)
    val isInProduction = orderData.status.equals("in_production", ignoreCase = true) || isCompleted

    val steps = listOf(
        TimelineStep(
            title = "Order Placed",
            subtitle = formatOverviewDate(orderData.createdAt ?: orderData.orderDate),
            isCompleted = true
        ),
        TimelineStep(
            title = "Payment Status",
            subtitle = if ((orderData.advanceAmountPaid ?: 0.0) > 0) "Advance Received" else "Pending Payment",
            isCompleted = (orderData.advanceAmountPaid ?: 0.0) > 0
        ),
        TimelineStep(
            title = "Production Status",
            subtitle = orderData.status?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: " - ",
            isCompleted = isInProduction
        ),
        TimelineStep(
            title = "Expected Delivery",
            subtitle = formatOverviewDate(orderData.dueDate),
            isCompleted = isCompleted
        )
    )

    val activeColor = complete_button_bg
    val inactiveColor = sectionBorder

    Column(modifier = Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.Top
            ) {
                // Continuous Vertical Track & Circle Node
                Column(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Node Circle
                    Box(
                        modifier = Modifier
                            .size(tokens.iconSize * 0.85f)
                            .background(
                                color = if (step.isCompleted) activeColor else inactiveColor,
                                shape = CircleShape
                            )
                    )

                    // Connecting Line
                    if (index != steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .weight(1f)
                                .background(if (step.isCompleted) activeColor else inactiveColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title and Subtitle Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (index != steps.lastIndex) 26.dp else 4.dp)
                ) {
                    Text(
                        text = step.title,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color,
                        lineHeight = tokens.bodySmall * 1.25f
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = step.subtitle,
                        fontSize = tokens.caption,
                        color = headerGrey,
                        lineHeight = tokens.caption * 1.25f
                    )
                }
            }
        }
    }
}

private data class TimelineStep(
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean
)

// ─────────────────────────────────────────────────────────────────────────
// REUSABLE ROW & SECTION HELPERS
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionContainer(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = tokens.screenPadding, vertical = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = tokens.bodyMedium,
            color = title_color,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(color = sectionBorder)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun KeyValueRow(
    label: String,
    value: String,
    isValueBold: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            color = headerGrey,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = if (isValueBold) FontWeight.Bold else FontWeight.Normal,
            color = title_color,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun InnerProductRow(
    label: String,
    value: String,
    isBold: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = headerGrey)
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = title_color
        )
    }
}

@Composable
private fun PriceSummaryRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = headerGrey)
        Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
    }
}

@Composable
private fun DividerSection() {
    Spacer(Modifier.height(10.dp))
}

private fun formatOverviewDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return " - "
    return try {
        val inputFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd"
        )
        var parsed: Date? = null
        for (pattern in inputFormats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                parsed = sdf.parse(isoDate)
                if (parsed != null) break
            } catch (_: Exception) {}
        }
        parsed?.let {
            SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(it)
        } ?: " - "
    } catch (_: Exception) {
        " - "
    }
}

// ─────────────────────────────────────────────────────────────────────────
// RECEIPT PRINT HELPER
// ─────────────────────────────────────────────────────────────────────────
private fun printReceipt(context: Context, orderData: OrderOverviewData) {
    val pdfGenerator = OrderReceiptPdfGenerator(context)
    val receiptData = OrderReceiptPdfGenerator.OrderReceiptData(
        orderNumber = orderData.order.orderNumber,
        customerName = orderData.order.customerId?.name ?: " - ",
        items = orderData.items.map {
            OrderReceiptPdfGenerator.OrderItem(
                quantity = it.quantity,
                name = it.categoryName,
                price = it.stitchingCharge,
                additionalCharge = it.additionalCharges.sumOf { add -> add.amount }
            )
        },
        otherCharges = orderData.order.summaryAdditionalCharges.sumOf { it.amount },
        totalAmount = orderData.order.totalAmount,
        paidAmount = orderData.order.totalPaid,
        balanceAmount = orderData.order.balanceAmount,
        deliveryDate = orderData.order.deliveryDate
    )
    pdfGenerator.printReceiptViaWebView(receiptData)
}