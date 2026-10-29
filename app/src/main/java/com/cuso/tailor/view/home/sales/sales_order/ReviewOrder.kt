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
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.WarehouseDropdownItem
import com.cuso.tailor.model.sales.OrderOverviewData
import com.cuso.tailor.model.sales.OrderReviewData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.currencySymbol
import com.cuso.tailor.view.composable.CirculerProgressIndicatorReuse
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.OrderOverviewState
import com.cuso.tailor.viewmodel.OrderOverviewViewModel
import com.cuso.tailor.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun OrderOverviewScreen(
    orderId: String,
    showConvertToInvoice: Boolean = false,
    onConvertToInvoice: (OrderOverviewData) -> Unit = {},
    onClose: () -> Unit = {},
    onEditOrder: (OrderReviewData) -> Unit = {},
    onCreateNew: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val overViewViewModel: OrderOverviewViewModel = hiltViewModel()
    val viewModel: SalesViewModel = hiltViewModel(key = "order_overview_view_$orderId")
    val state by overViewViewModel.overviewState.collectAsStateWithLifecycle()
    val warehouseList by overViewViewModel.warehouseList.collectAsStateWithLifecycle()

    // Dialog state for cancelling order
    var showCancelModal by remember { mutableStateOf(false) }

    // Cancellation API states
    val isCancelling by viewModel.isCancellingOrder.collectAsStateWithLifecycle()
    val cancelSuccess by viewModel.cancelOrderSuccess.collectAsStateWithLifecycle()
    val cancelError by viewModel.cancelOrderError.collectAsStateWithLifecycle()

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Listen for cancel success
    LaunchedEffect(cancelSuccess) {
        cancelSuccess?.let {
            successMessage = it
            viewModel.clearCancelOrderAlerts()
        }
    }

    // Listen for cancel error
    LaunchedEffect(cancelError) {
        cancelError?.let {
            errorMessage = it
            viewModel.clearCancelOrderAlerts()
        }
    }

    LaunchedEffect(orderId) {
        overViewViewModel.fetchSalesOverview(orderId)
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
                                    onClick = { overViewViewModel.fetchSalesOverview(orderId) },
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
                            warehouseList = warehouseList,
                            context = context,
                            showConvertToInvoice = showConvertToInvoice, // <-- Pass செய்கிறோம்
                            onConvertToInvoice = { onConvertToInvoice(orderData) }, // <-- Pass செய்கிறோம்
                            onCancelOrder = { showCancelModal = true },
                            onPrintInvoice = { printReceipt(context, orderData) },
                            onEditOrder = {
                                onEditOrder(orderData.mapToOrderReviewData())
                            }
                        )
                    }
                }
            }
        }

        // Cancellation Reason Modal Dialog
        if (showCancelModal) {
            CancelOrderDialog(
                isLoading = isCancelling,
                onDismiss = {
                    if (!isCancelling) showCancelModal = false
                },
                onConfirmCancel = { reason ->
                    viewModel.cancelOrder(
                        orderId = orderId,
                        reason = reason,
                        onSuccess = {
                            showCancelModal = false
                        }
                    )
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// CANCELLATION REASON MODAL DIALOG
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun CancelOrderDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirmCancel: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var reasonText by remember { mutableStateOf("") }
    val isConfirmEnabled = reasonText.isNotBlank() && !isLoading

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading
        )
    ) {
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Cancel Order",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Please enter the reason for cancellation. Reason cannot be empty.",
                    fontSize = tokens.bodySmall,
                    color = mutedText
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = {
                        Text(
                            text = "Enter cancellation reason...",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    },
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = sectionBorder,
                        focusedTextColor = title_color,
                        unfocusedTextColor = title_color
                    ),
                    maxLines = 4,
                    enabled = !isLoading
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "Back",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                    }

                    Button(
                        onClick = { onConfirmCancel(reasonText.trim()) },
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = redText,
                            disabledContainerColor = redText.copy(alpha = 0.4f)
                        ),
                        enabled = isConfirmEnabled
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = whiteBg,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Confirm Cancel",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
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
    showConvertToInvoice: Boolean = false,
    onConvertToInvoice: () -> Unit = {},
    onCancelOrder: () -> Unit,
    onPrintInvoice: () -> Unit,
    onEditOrder: () -> Unit
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

    val isCancelled = orderData.status?.equals("cancelled", ignoreCase = true) == true ||
            orderData.order.status.equals("cancelled", ignoreCase = true)

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
                    val statusBg = if (isCancelled) redBg else greenBg
                    val statusColor = if (isCancelled) redText else darkGreenBg

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(statusBg)
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = status,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }

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

            // Action Buttons Row: [Print Invoice] [Edit Order]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPrintInvoice,
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = BorderStroke(1.5.dp, Primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text("Print Invoice", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onEditOrder,
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = BorderStroke(1.5.dp, Primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text("Edit Order", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }

            if (showConvertToInvoice && !isCancelled) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onConvertToInvoice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = "Convert to Invoice",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = whiteBg
                    )
                }
            }

            // Cancel Order Button
            if (!isCancelled) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onCancelOrder,
                    modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = BorderStroke(1.dp, redText),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = redText)
                ) {
                    Text("Cancel Order", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = redText)
                }
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
                        InnerProductRow("Unit Price", "${currencySymbol()}${formatIndianNumber(unitPrice)}")
                        InnerProductRow("Total", "${currencySymbol()}${formatIndianNumber(lineTotal)}", isBold = true)
                    }
                    if (index != items.lastIndex) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            val subtotal = orderData.subtotal ?: order.totalAmount
            val gstAmount = orderData.totalTax ?: (order.totalAmount * 0.05)
            val grandTotal = orderData.grandTotal ?: (subtotal + gstAmount)

            PriceSummaryRow("Subtotal", "${currencySymbol()}${formatIndianNumber(subtotal)}")
            PriceSummaryRow("GST", "${currencySymbol()}${formatIndianNumber(gstAmount)}")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Shipping Fee", fontSize = tokens.bodySmall, color = headerGrey)
                Text(
                    text = if ((orderData.deliveryCharge ?: 0.0) > 0.0) "${currencySymbol()}${formatIndianNumber(orderData.deliveryCharge!!)}" else "FREE",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = darkGreenBg
                )
            }

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = sectionBorder)
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Grand Total", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = title_color)
                Text("${currencySymbol()}${formatIndianNumber(grandTotal)}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
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
                ?: orderData.branchId?._id?.takeIf { it.isNotBlank() }

            val warehouseLabel = warehouseList.firstOrNull { it.value == targetWarehouseId }?.label
                ?: orderData.branchId?.name
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
                value = "${currencySymbol()}${formatIndianNumber(orderData.advanceAmountPaid ?: order.totalPaid)}",
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
                Column(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.iconSize * 0.85f)
                            .background(
                                color = if (step.isCompleted) activeColor else inactiveColor,
                                shape = CircleShape
                            )
                    )

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

private fun formatReceiptDate(isoDate: String?): String {
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
            SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(it)
        } ?: isoDate
    } catch (_: Exception) {
        isoDate
    }
}

// ─────────────────────────────────────────────────────────────────────────
// RECEIPT PRINT HELPER: POS THERMAL RECEIPT (EXACT MATCH TO ATTACHED IMAGE)
// ─────────────────────────────────────────────────────────────────────────
private fun printReceipt(context: Context, orderData: OrderOverviewData) {
    val html = generatePosReceiptHtml(orderData)
    val webView = WebView(context).apply {
        settings.javaScriptEnabled = false
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val orderNo = orderData.orderCode?.takeIf { it.isNotBlank() } ?: orderData.order.orderNumber
                val printAdapter = createPrintDocumentAdapter("Receipt_$orderNo")
                val printAttributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
                printManager?.print("Order Receipt", printAdapter, printAttributes)
            }
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
}

private fun generatePosReceiptHtml(orderData: OrderOverviewData): String {
    val order = orderData.order
    val orderNumber = orderData.orderCode?.takeIf { it.isNotBlank() } ?: order.orderNumber.ifBlank { " - " }
    val orderDate = formatReceiptDate(orderData.orderDate ?: order.orderDate)
    val deliveryDue = formatReceiptDate(orderData.dueDate ?: order.deliveryDate)

    val customerName = orderData.customerSnapshot?.name?.takeIf { it.isNotBlank() }
        ?: order.customerId?.name?.takeIf { it.isNotBlank() } ?: " - "

    val customerMobile = orderData.customerSnapshot?.phone?.takeIf { it.isNotBlank() }
        ?: order.customerId?.mobile?.takeIf { it.isNotBlank() } ?: " - "

    val branchName = orderData.branchId?.name ?: order.branch?.name ?: "Nexus - Main Branch"

    val subtotal = orderData.subtotal ?: order.totalAmount
    val totalTax = orderData.totalTax ?: (subtotal * 0.05)
    val cgst = totalTax / 2.0
    val sgst = totalTax / 2.0
    val grandTotal = orderData.grandTotal ?: (subtotal + totalTax)
    val totalPaid = orderData.advanceAmountPaid ?: order.totalPaid
    val balanceDue = orderData.balanceAmount ?: (grandTotal - totalPaid).coerceAtLeast(0.0)
    val paymentMode = orderData.paymentMode?.replaceFirstChar { it.uppercase() }?.takeIf { it.isNotBlank() } ?: "Cash"

    val itemsRows = StringBuilder()
    orderData.items.forEachIndexed { index, item ->
        val itemName = item.categoryName.takeIf { it.isNotBlank() && it != "Custom Garment" }
            ?: item.itemDescription?.takeIf { it.isNotBlank() }
            ?: "Item #${index + 1}"
        val qty = item.quantityNumber?.toInt() ?: item.quantity
        val unit = item.unit ?: "Pieces"
        val lineTotal = item.lineTotal ?: ((item.unitPrice ?: item.stitchingCharge) * qty)

        itemsRows.append(
            """
            <tr>
                <td style="padding: 6px 0; vertical-align: top; font-weight: bold; width: 45%;">${index + 1}. $itemName</td>
                <td style="padding: 6px 0; vertical-align: top; text-align: center; font-weight: bold; width: 30%;">
                    $qty<br/><span style="font-weight: normal; font-size: 11px;">$unit</span>
                </td>
                <td style="padding: 6px 0; vertical-align: top; text-align: right; font-weight: bold; width: 25%;">${currencySymbol()}${formatIndianNumber(lineTotal)}</td>
            </tr>
            """.trimIndent()
        )
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8">
        <style>
            @page {
                size: auto;
                margin: 0;
            }
            body {
                font-family: 'Courier New', Courier, monospace;
                color: #000;
                margin: 0;
                padding: 18px 24px;
                width: 320px;
                margin-left: auto;
                margin-right: auto;
                font-size: 13px;
                line-height: 1.4;
            }
            .center {
                text-align: center;
            }
            .bold {
                font-weight: bold;
            }
            .shop-title {
                font-size: 18px;
                font-weight: bold;
                letter-spacing: 1px;
                margin-top: 10px;
                margin-bottom: 4px;
            }
            .branch-title {
                font-size: 13px;
                font-weight: bold;
                margin-bottom: 4px;
            }
            .address-line {
                font-size: 11px;
                margin-bottom: 2px;
            }
            .dashed-line {
                border-top: 1.5px dashed #000;
                margin: 8px 0;
            }
            .dotted-line {
                border-top: 1.5px dotted #000;
                margin: 8px 0;
            }
            .solid-line {
                border-top: 1.5px solid #000;
                margin: 8px 0;
            }
            .row-split {
                display: flex;
                justify-content: space-between;
                margin: 3px 0;
            }
            table {
                width: 100%;
                border-collapse: collapse;
            }
            th {
                text-align: left;
                padding: 5px 0;
                font-size: 13px;
            }
            .logo-box {
                width: 70px;
                height: 70px;
                background-color: #1a1a1a;
                color: #fff;
                margin: 0 auto;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                font-family: sans-serif;
                font-size: 11px;
                font-weight: bold;
                letter-spacing: 1.5px;
            }
            .logo-icon {
                font-size: 14px;
                color: #cca461;
                margin-bottom: 2px;
            }
            .footer-text {
                margin-top: 14px;
                font-size: 11px;
            }
        </style>
    </head>
    <body>
        <div class="center">
            <div class="logo-box">
                <span class="logo-icon">&#10018;</span>
                <span>NEXUS</span>
                <span style="font-size: 7px; color: #999; letter-spacing: 0.5px;">APPAREL</span>
            </div>
            <div class="shop-title">NEXUS BOUTIQUE</div>
            <div class="branch-title">$branchName</div>
            <div class="address-line">Udhayam Colony, Anna Nagar, Chennai, Tamil Nadu - 600040</div>
            <div class="address-line bold">Ph: 919962376763</div>
        </div>

        <div class="dashed-line"></div>

        <div class="row-split">
            <span>Order No:</span>
            <span class="bold" style="font-size: 14px;">$orderNumber</span>
        </div>
        <div class="row-split">
            <span>Date:</span>
            <span>$orderDate</span>
        </div>
        <div class="row-split">
            <span>Delivery Due:</span>
            <span class="bold">$deliveryDue</span>
        </div>

        <div class="dotted-line"></div>

        <div class="row-split">
            <span>Customer:</span>
            <span class="bold">$customerName</span>
        </div>
        <div class="row-split">
            <span>Mobile:</span>
            <span>$customerMobile</span>
        </div>

        <div class="dashed-line"></div>

        <table>
            <thead>
                <tr style="border-bottom: 1.5px solid #000;">
                    <th style="width: 45%;">Item Description</th>
                    <th style="text-align: center; width: 30%;">Qty</th>
                    <th style="text-align: right; width: 25%;">Amount</th>
                </tr>
            </thead>
            <tbody>
                $itemsRows
            </tbody>
        </table>

        <div class="dashed-line"></div>

        <div class="row-split">
            <span>Subtotal:</span>
            <span>${currencySymbol()}${formatIndianNumber(subtotal)}</span>
        </div>
        <div class="row-split">
            <span>CGST 2.5%:</span>
            <span>+${currencySymbol()}${formatIndianNumber(cgst)}</span>
        </div>
        <div class="row-split">
            <span>SGST 2.5%:</span>
            <span>+${currencySymbol()}${formatIndianNumber(sgst)}</span>
        </div>

        <div class="solid-line"></div>

        <div class="row-split bold" style="font-size: 15px;">
            <span>GRAND TOTAL:</span>
            <span>${currencySymbol()}${formatIndianNumber(grandTotal)}</span>
        </div>

        <div class="dotted-line"></div>

        <div class="row-split bold">
            <span>Total Paid ($paymentMode):</span>
            <span>${currencySymbol()}${formatIndianNumber(totalPaid)}</span>
        </div>

        <div class="dashed-line"></div>

        <div class="row-split bold" style="font-size: 14px;">
            <span>BALANCE DUE:</span>
            <span>${currencySymbol()}${formatIndianNumber(balanceDue)}</span>
        </div>

        <div class="dashed-line"></div>

        <div class="center footer-text">
            <div class="bold" style="font-size: 12px; margin-bottom: 3px;">THANK YOU FOR YOUR BUSINESS!</div>
            <div>Please bring this receipt for trial / delivery.</div>
        </div>
    </body>
    </html>
    """.trimIndent()
}

// ─────────────────────────────────────────────────────────────────────────
// SAFE EXTENSION MAPPER: OrderOverviewData -> OrderReviewData
// ─────────────────────────────────────────────────────────────────────────
private fun OrderOverviewData.mapToOrderReviewData(): OrderReviewData {
    val editId = this.id.ifBlank { this.order._id }
    return OrderReviewData(
        orderId = editId,
        editOrderId = editId,
        customerId = this.customerSnapshot?.customerCode ?: this.customerId?._id ?: this.order.customerId?._id ?: "",
        customerCode = this.customerSnapshot?.customerCode ?: this.customerId?.customerCode ?: "",
        branchId = this.branchId?._id ?: this.order.branch?._id ?: "",
        fullName = this.customerSnapshot?.name ?: this.customerId?.fullName ?: this.order.customerId?.name ?: "",
        phone = this.customerSnapshot?.phone ?: this.customerId?.mobileNumber ?: this.order.customerId?.mobile ?: "",
        email = this.customerSnapshot?.email ?: this.customerId?.email ?: this.order.customerId?.email ?: "",
        flatNo = this.customerSnapshot?.billingAddress?.flatNo ?: "",
        street = this.customerSnapshot?.billingAddress?.street ?: "",
        areaZone = this.customerSnapshot?.billingAddress?.areaZone ?: "",
        city = this.customerSnapshot?.billingAddress?.city ?: this.order.customerId?.address?.city ?: "",
        pincode = this.customerSnapshot?.billingAddress?.pincode ?: "",
        orderDate = this.orderDate ?: this.order.orderDate ?: "",
        trialDate = this.trialDate ?: this.order.trialDate ?: "",
        deliveryDate = this.dueDate ?: this.order.deliveryDate ?: "",
        priority = this.priority ?: "",
        orderType = this.orderType ?: "",
        orderNotes = this.orderNotes ?: "",
        paymentMode = this.paymentMode ?: "",
        paymentType = this.paymentType ?: "",
        paymentTermId = this.paymentTermId?.id ?: "",
        salespersonId = this.salespersonId?.id ?: "",
        advanceAmountPaid = this.advanceAmountPaid ?: this.order.totalPaid,
        advanceAmountRequired = this.advanceAmountRequired ?: 0.0,
        deliveryMethod = this.deliveryMethod ?: "",
        deliveryCharge = this.deliveryCharge ?: 0.0,
        discount = this.totalDiscount ?: this.order.discount,
        paidSoFar = this.advanceAmountPaid ?: this.order.totalPaid,
        billingNotes = this.billingNotes ?: "",
        status = this.status ?: this.order.status,
        subtotal = this.subtotal ?: this.order.totalAmount,
        grandTotal = this.grandTotal ?: this.order.totalAmount,
        dynamicItems = emptyList()
    )
}