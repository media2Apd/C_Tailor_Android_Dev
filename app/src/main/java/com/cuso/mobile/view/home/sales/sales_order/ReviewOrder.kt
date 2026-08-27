@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "Unused_parameter",
    "VariableNeverRead",
    "SameParameterValue"
)

package com.cuso.mobile.view.home.sales.sales_order

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.sales.OrderOverviewData
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.CirculerProgressIndicatorReuse
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.home.formatIndianNumber
import com.cuso.mobile.view.home.pdfgenerator.OrderReceiptPdfGenerator
import com.cuso.mobile.viewmodel.OrderOverviewState
import com.cuso.mobile.viewmodel.OrderOverviewViewModel

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

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orderId) {
        viewModel.fetchSalesOverview(orderId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = whiteBg,
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
                                Text("Failed to load order", color = Color.Red, fontWeight = FontWeight.Bold)
                                Text(s.message, color = Color.Gray, fontSize = 13.sp)
                                Spacer(Modifier.height(12.dp))
                                Button(onClick = { viewModel.fetchSalesOverview(orderId) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    is OrderOverviewState.Success -> {
                        val orderData = s.data
                        OrderOverviewContent(
                            orderData = orderData,
                            context = context,
                            onCancelOrder = {
                                errorMessage = "Order cancellation initiated"
                            },
                            onPrintInvoice = {
                                printReceipt(context, orderData)
                            },
                            onTrackOrder = {
                                successMessage = "Tracking details refreshed"
                            }
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
    context: Context,
    onCancelOrder: () -> Unit,
    onPrintInvoice: () -> Unit,
    onTrackOrder: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val order = orderData.order
    val customer = order.customerId
    val items = orderData.items

    val status = order.status.ifBlank { "Shipped" }
    val paymentType = if (order.totalPaid > 0) "Prepaid" else "COD"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(whiteBg)
    ) {
        // --- 1. Order Top Header & Action Buttons ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ORD-${order.orderNumber}",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Status Badge (Shipped / Processing)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = status.replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF16A34A)
                        )
                    }

                    // Payment Badge (Prepaid / COD)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFEEF2FF))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = paymentType,
                            fontSize = 11.sp,
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
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.5.dp, Primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Text("Print Invoice", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onTrackOrder,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
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
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Text("Cancel Order", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
            }
        }

        DividerSection()

        // --- 2. Customer Information ---
        SectionContainer(title = "Customer Information") {
            KeyValueRow(label = "Name", value = customer?.name ?: "Rajesh Mehta")
            KeyValueRow(label = "Phone", value = customer?.mobile?.let { "+91 $it" } ?: "+91 98765 43210")
            KeyValueRow(label = "Email", value = customer?.email ?: "rajesh.mehta@gmail.com")
            KeyValueRow(
                label = "Shipping Address",
                value = listOfNotNull(customer?.address?.addressLine, customer?.address?.city, customer?.address?.addressLine   )
                    .joinToString(", ")
                    .ifBlank { "42 Marine Drive, Andheri West, Mumbai, Maharashtra 400058" }
            )
        }

        DividerSection()

        // --- 3. Product Details ---
        SectionContainer(title = "Product Details") {
            // Inner Product Card (Gray container)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val firstItem = items.firstOrNull()
                val totalQty = items.sumOf { it.quantity }.takeIf { it > 0 } ?: 120
                val unitPrice = firstItem?.stitchingCharge ?: 1500.0

                InnerProductRow("Product", firstItem?.categoryName ?: "Formal Shirts")
                InnerProductRow("SKU", "SKU-FS-001")
                InnerProductRow("Quantity", "$totalQty")
                InnerProductRow("Unit Price", "₹${formatIndianNumber(unitPrice)}")
                InnerProductRow("Total", "₹${formatIndianNumber(order.totalAmount)}", isBold = true)
            }

            Spacer(Modifier.height(14.dp))

            // Subtotal, GST, Shipping Fee
            val gstAmount = order.totalAmount * 0.18
            val grandTotal = order.totalAmount + gstAmount

            PriceSummaryRow("Subtotal", "₹${formatIndianNumber(order.totalAmount)}")
            PriceSummaryRow("GST (18%)", "₹${formatIndianNumber(gstAmount)}")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Shipping Fee", fontSize = tokens.bodySmall, color = Color(0xFF64748B))
                Text("FREE", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF16A34A))
            }

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(Modifier.height(6.dp))

            // Grand Total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Grand Total", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)
                Text("₹${formatIndianNumber(grandTotal)}", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Primary)
            }
        }

        DividerSection()

        // --- 4. Order Activity Timeline ---
        SectionContainer(title = "Order Activity Timeline") {
            OrderTimelineView()
        }

        DividerSection()

        // --- 5. Shipping Details ---
        SectionContainer(title = "Shipping Details") {
            KeyValueRow(label = "Warehouse", value = "MUM-N (Mumbai North)")
            KeyValueRow(label = "Courier Partner", value = "BlueDart Express")
            KeyValueRow(label = "AWB Tracking No.", value = "BD987654321")
            KeyValueRow(label = "Est. Delivery Date", value = "Aug 20, 2025")
        }

        DividerSection()

        // --- 6. Payment Details ---
        SectionContainer(title = "Payment Details") {
            KeyValueRow(label = "Payment Type", value = "Prepaid (Credit Card)")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Payment Status", fontSize = tokens.bodySmall, color = Color(0xFF64748B))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFDCFCE7))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text("Paid", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF16A34A))
                }
            }

            KeyValueRow(label = "Transaction Ref.", value = "TXN-2025-0850001")
            KeyValueRow(label = "Payment Date", value = "Aug 15, 2025")
            KeyValueRow(label = "Amount Paid", value = "₹${formatIndianNumber(order.totalAmount + (order.totalAmount * 0.18))}", isValueBold = true)
        }

        Spacer(Modifier.height(40.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────
// TIMELINE COMPONENT
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun OrderTimelineView() {
    val steps = listOf(
        TimelineStep("Order Placed", "Aug 15, 2025 - 10:30 AM", isCompleted = true),
        TimelineStep("Payment Confirmed", "Aug 15, 2025 - 10:45 AM", isCompleted = true),
        TimelineStep("Processing in Warehouse", "Aug 16, 2025 - 02:15 PM", isCompleted = true),
        TimelineStep("Shipped (MUM-N Warehouse)", "Aug 17, 2025 - 09:00 AM", isCompleted = true),
        TimelineStep("Out for Delivery", "Pending Courier Arrival", isCompleted = false)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Vertical Line & Dot
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (step.isCompleted) Color(0xFF10B981) else Color(0xFFE2E8F0))
                    )

                    if (index != steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(38.dp)
                                .background(if (step.isCompleted) Color(0xFF10B981) else Color(0xFFE2E8F0))
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Step Titles & Date
                Column(modifier = Modifier.padding(bottom = if (index != steps.lastIndex) 18.dp else 0.dp)) {
                    Text(
                        text = step.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = step.subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
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
            .padding(horizontal = tokens.screenPadding, vertical = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = tokens.bodyLarge,
            color = title_color
        )
        Spacer(Modifier.height(10.dp))
        HorizontalDivider(Modifier.background(grey_border))
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
            color = Color(0xFF64748B),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = if (isValueBold) FontWeight.Bold else FontWeight.Medium,
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B))
        Text(
            text = value,
            fontSize = 12.sp,
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
        Text(text = label, fontSize = tokens.bodySmall, color = Color(0xFF64748B))
        Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
    }
}

@Composable
private fun DividerSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(Color(0xFFF8FAFC))
    )
}

// ─────────────────────────────────────────────────────────────────────────
// RECEIPT PRINT HELPER
// ─────────────────────────────────────────────────────────────────────────
private fun printReceipt(context: Context, orderData: OrderOverviewData) {
    val pdfGenerator = OrderReceiptPdfGenerator(context)
    val receiptData = OrderReceiptPdfGenerator.OrderReceiptData(
        orderNumber = orderData.order.orderNumber,
        customerName = orderData.order.customerId?.name ?: "—",
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