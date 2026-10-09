@file:Suppress("SameParameterValue", "unused", "unusedVariable")

package com.cuso.tailor.view.home.sales.payment_listing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.composable.SheetValue
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun PaymentInformationScreen(
    orderId: String,
    onClose: () -> Unit = {},
    viewModel: SalesViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val detailData by viewModel.selectedBillingDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingBillingDetail.collectAsStateWithLifecycle()
    val errorMessage by viewModel.billingDetailError.collectAsStateWithLifecycle()

    val recordSuccess by viewModel.recordPaymentSuccess.collectAsStateWithLifecycle()
    val recordError by viewModel.recordPaymentError.collectAsStateWithLifecycle()
    val isRecordingPayment by viewModel.isRecordingPayment.collectAsStateWithLifecycle()

    var receiveSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var sheetBlur by remember { mutableStateOf(0.dp) }

    LaunchedEffect(orderId) {
        if (orderId.isNotBlank()) {
            viewModel.fetchBillingPaymentDetail(orderId)
        }
    }

    LaunchedEffect(recordSuccess) {
        recordSuccess?.let { msg ->
            DynamicIslandManager.showSuccess(msg)
            viewModel.clearBillingAlerts()
            receiveSheetState = SheetValue.Hidden
        }
    }

    LaunchedEffect(recordError) {
        recordError?.let { err ->
            DynamicIslandManager.showError(err)
            viewModel.clearBillingAlerts()
        }
    }

    Scaffold(
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleBar(title = "Payment Information", onClose = onClose)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            when {
                isLoading && detailData == null -> {
                    ListSkeleton()
                }

                errorMessage != null && detailData == null -> {
                    AppErrorState(
                        title = "Failed to load payment details",
                        message = errorMessage ?: "Something went wrong",
                        onRetry = { viewModel.fetchBillingPaymentDetail(orderId) }
                    )
                }

                detailData != null -> {
                    val detail = detailData!!
                    val orderHeader = detail.orderHeader
                    val customer = detail.customer
                    val billing = detail.billingSummary
                    val items = detail.items
                    val history = detail.paymentHistory

                    val subtotal = billing?.subtotal ?: 0.0
                    val tax = billing?.totalTax ?: 0.0
                    val totalAmount = billing?.grandTotal ?: 0.0
                    val amountPaid = billing?.paidAmount ?: 0.0
                    val balanceDue = billing?.balanceDue ?: 0.0

                    val orderRefText = "${orderHeader?.orderCode ?: "ORD"} / ${customer?.name ?: "Customer"}"
                    val phoneText = customer?.phone?.takeIf { it.isNotBlank() } ?: "N/A"
                    val orderDateText = formatDisplayDate(orderHeader?.orderDate)
                    val deliveryDateText = formatDisplayDate(orderHeader?.dueDate)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .blurScrim(sheetBlur)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 14.dp)
                    ) {
                        // 1. Order Reference Row
                        OrderReferenceRow(
                            orderRef = orderRefText,
                            phone = phoneText,
                            orderDate = orderDateText,
                            deliveryDate = deliveryDateText
                        )

                        Spacer(Modifier.height(14.dp))

                        // 2. Order Summary Card
                        OrderSummaryCard(
                            items = items,
                            subtotal = subtotal,
                            tax = tax,
                            deliveryCharge = billing?.deliveryCharge ?: 0.0,
                            discount = billing?.totalDiscount ?: 0.0,
                            totalAmount = totalAmount,
                            amountPaid = amountPaid,
                            balanceDue = balanceDue
                        )

                        Spacer(Modifier.height(18.dp))


                        // 4. Inline Receive Payment Card
                        if (balanceDue <= 0.0) {
                            OrderFullyPaidCard()
                        } else {
                            // 3. Send Payment Link Card
                            SendPaymentLinkCard(
                                balanceDue = balanceDue,
                                orderCode = orderHeader?.orderCode ?: "",
                                customerPhone = phoneText
                            )

                            Spacer(Modifier.height(18.dp))

                            // 4. Inline Receive Payment Card
                            ReceivePaymentCard(
                                totalDue = balanceDue,
                                onReceivePaymentClick = { receiveSheetState = SheetValue.Collapsed }
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // 5. Payment History List
                        PaymentHistoryList(history = history)

                        Spacer(Modifier.height(30.dp))
                    }

                    // Bottom Sheet: Receive Payment
                    ReceivePaymentSheet(
                        orderRef = orderHeader?.orderCode ?: "",
                        totalDue = balanceDue,
                        sheetState = receiveSheetState,
                        isSubmitting = isRecordingPayment,
                        onStateChange = { receiveSheetState = it },
                        onBlurScrimChange = { r, _ -> sheetBlur = r },
                        onDismiss = { receiveSheetState = SheetValue.Hidden },
                        onConfirm = { req ->
                            // ViewModel call
                            viewModel.recordOrderPayment(
                                orderId = orderId,
                                request = req
                            )
                        }
                    )
                }
            }
        }
    }
}
@Composable
private fun OrderFullyPaidCard() {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding),
        shape = RoundedCornerShape(12.dp),
        color = activity_green_bg, // Theme-ல் உள்ள Light Green Bg
        border = BorderStroke(1.dp, greentext.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Green Check Icon
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Paid",
                tint = greentext,
                modifier = Modifier.size(tokens.iconSize * 1.5f)
            )

            Spacer(Modifier.height(8.dp))

            // Title: Order Fully Paid
            Text(
                text = "Order Fully Paid",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = activity_green,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "No pending balance due for this sales order.",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────
// SECTION HEADER
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = tokens.screenPadding, vertical = 10.dp)
    ) {
        Text(
            text = title,
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = title_color
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// ORDER REFERENCE ROW
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun OrderReferenceRow(
    orderRef: String,
    phone: String,
    orderDate: String,
    deliveryDate: String
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
    ) {
        Text(
            text = orderRef,
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = title_color
        )

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderInfoField("Phone", phone, Modifier.weight(1f))
            VerticalDivider()
            HeaderInfoField("Order Date", orderDate, Modifier.weight(1f))
            VerticalDivider()
            HeaderInfoField("Delivery Date", deliveryDate, Modifier.weight(1f))
        }
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(BorderGray)
    )
}

@Composable
private fun HeaderInfoField(label: String, value: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier.padding(horizontal = 6.dp)) {
        Text(label, fontSize = tokens.caption, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// ORDER SUMMARY CARD (API DATA)
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun OrderSummaryCard(
    items: List<BillingLineItemDto>,
    subtotal: Double,
    tax: Double,
    deliveryCharge: Double,
    discount: Double,
    totalAmount: Double,
    amountPaid: Double,
    balanceDue: Double
) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Order Summary")

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
        ) {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.itemName,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Qty: ${item.quantity} ${item.unit} | Rate: ₹${formatIndianNumber(item.unitPrice)}",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                    Text(
                        text = "₹${formatIndianNumber(item.lineTotal)}",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 3.dp))
            }

            SummaryRow("Subtotal", "₹${formatIndianNumber(subtotal)}")
            if (tax > 0.0) SummaryRow("Total Tax", "₹${formatIndianNumber(tax)}")
            if (discount > 0.0) SummaryRow("Discount", "-₹${formatIndianNumber(discount)}")
            if (deliveryCharge > 0.0) SummaryRow("Delivery Charge", "₹${formatIndianNumber(deliveryCharge)}")

            HorizontalDivider(color = dividerColor, modifier = Modifier.padding(vertical = 6.dp))

            SummaryRow("Total Amount", "₹${formatIndianNumber(totalAmount)}", valueColor = Primary)
            SummaryRow("Amount Paid", "₹${formatIndianNumber(amountPaid)}", valueColor = greentext)

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(redBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Balance Due", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = redText)
                Text(
                    text = "₹${formatIndianNumber(balanceDue)}",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = redText
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = tokens.bodySmall, color = mutedText)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

// ─────────────────────────────────────────────────────────────────────────
// SEND PAYMENT LINK CARD
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun SendPaymentLinkCard(
    balanceDue: Double,
    orderCode: String,
    customerPhone: String
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = tokens.screenPadding, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(primary_light)
                    .padding(8.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_link_chain),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "Send Payment Link",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Text(
                    text = "Share secure UPI link with your customer",
                    fontSize = tokens.caption,
                    color = mutedText
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
        ) {
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(greenBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Balance Due", fontSize = tokens.caption, color = TextSecondary)
                    Text(
                        text = "₹${formatIndianNumber(balanceDue)}",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = redText
                    )
                }
                Text("Ready to Send", fontSize = tokens.caption, color = greentext)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Order Link", "https://nexus.pay/order/$orderCode"))
                    Toast.makeText(context, "Payment link copied!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Primary)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Primary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy Payment Link", color = Primary, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// RECEIVE PAYMENT INLINE CARD
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun ReceivePaymentCard(
    totalDue: Double,
    onReceivePaymentClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Receive Payment")

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
        ) {
            Button(
                onClick = onReceivePaymentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = "Record Payment (₹${formatIndianNumber(totalDue)})",
                    color = whiteBg,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// PAYMENT HISTORY LIST (API DATA)
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun PaymentHistoryList(history: List<BillingHistoryItemDto>) {
    val tokens = LocalAppTokens.current

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Payment History (${history.size})")

        Spacer(Modifier.height(10.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No payment records found", fontSize = tokens.bodySmall, color = mutedText)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
            ) {
                history.forEachIndexed { idx, entry ->
                    PaymentHistoryRow(entry)
                    if (idx != history.lastIndex) HorizontalDivider(color = dividerColor)
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryRow(entry: BillingHistoryItemDto) {
    val tokens = LocalAppTokens.current

    val (chipBg, chipText) = when (entry.paymentMode.lowercase()) {
        "cash" -> greenBg to greentext
        "upi" -> primary_light to Primary
        else -> yellowBg to yellowText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = formatDisplayDate(entry.paymentDate),
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = entry.paymentNumber.ifBlank { entry.referenceNumber ?: "N/A" },
                fontSize = tokens.caption,
                color = mutedText
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(8.dp), color = chipBg) {
                Text(
                    text = entry.paymentMode,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = chipText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "₹${formatIndianNumber(entry.amount)}",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// RECEIVE PAYMENT BOTTOM SHEET (WITH API SUBMIT)
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun ReceivePaymentSheet(
    orderRef: String,
    totalDue: Double,
    sheetState: SheetValue,
    isSubmitting: Boolean = false,
    onStateChange: (SheetValue) -> Unit,
    onBlurScrimChange: (radius: androidx.compose.ui.unit.Dp, scrim: Float) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    onConfirm: (RecordOrderPaymentRequest) -> Unit
) {
    val tokens = LocalAppTokens.current
    var isFullAmount by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf(totalDue.toInt().toString()) }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var referenceNo by remember { mutableStateOf("") }
    val defaultDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    var completionDate by remember { mutableStateOf(defaultDate) }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(totalDue) {
        amountText = totalDue.toInt().toString()
    }

    LaunchedEffect(isFullAmount) {
        if (isFullAmount) amountText = totalDue.toInt().toString()
    }

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        collapsedFraction = 0.60f,
        topInset = 66.dp,
        onDismissRequest = onDismiss,
        onBlurScrimChange = onBlurScrimChange
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
        ) {
            Text(
                text = "RECEIVE PAYMENT",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            // Total Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(light_blue, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                    .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Due", fontSize = tokens.bodySmall, color = title_color)
                    Text("For Order #$orderRef", fontSize = tokens.caption, color = mutedText)
                }
                Text(
                    text = "₹${formatIndianNumber(totalDue)}",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }

            Spacer(Modifier.height(14.dp))

            FormLabel("Payment Type")
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PaymentTypeToggle(
                    label = "Full Amount",
                    selected = isFullAmount,
                    modifier = Modifier.weight(1f)
                ) { isFullAmount = true }
                PaymentTypeToggle(
                    label = "Partial Amount",
                    selected = !isFullAmount,
                    modifier = Modifier.weight(1f)
                ) { isFullAmount = false }
            }

            Spacer(Modifier.height(12.dp))

            FormLabel("Amount (₹)")
            Spacer(Modifier.height(4.dp))
//            OutlinedTextField(
//                value = amountText,
//                onValueChange = { if (!isFullAmount) amountText = it.filter { c -> c.isDigit() } },
//                readOnly = isFullAmount,
//                singleLine = true,
//                textStyle = LocalTextStyle.current.copy(fontSize = tokens.bodySmall),
//                modifier = Modifier.fillMaxWidth().height(tokens.fieldHeight),
//                shape = RoundedCornerShape(8.dp),
//                colors = OutlinedTextFieldDefaults.colors(
//                    unfocusedBorderColor = BorderGray,
//                    focusedBorderColor = Primary
//                )
//            )
            FormTextField(
                value = amountText,
                onValueChange = { if (!isFullAmount) amountText = it.filter { c -> c.isDigit() } },
                enabled = isFullAmount
            )

            Spacer(Modifier.height(12.dp))

            FormLabel("Payment Method")
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Cash", "UPI", "Card").forEach { method ->
                    val isSelected = selectedMethod == method
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, if (isSelected) Primary else BorderGray, RoundedCornerShape(8.dp))
                            .background(if (isSelected) primary_light else whiteBg)
                            .clickable { selectedMethod = method }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = method,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) Primary else TextPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    FormLabel("Reference No.")
                    Spacer(Modifier.height(4.dp))
                    FormTextField(
                        value = referenceNo,
                        onValueChange = { referenceNo = it },
                        placeholder = "e.g. UPI875421"
                    )
                }
                Column(Modifier.weight(1f)) {
                    FormLabel("Transaction Date")
                    Spacer(Modifier.height(4.dp))
                    DatePickerField(
                        value = completionDate,
                        onDateSelected = { completionDate = it }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            FormLabel("Notes")
            Spacer(Modifier.height(4.dp))
            FormTextArea(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Remarks about this payment"
            )

            Spacer(Modifier.height(18.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("Cancel", color = TextPrimary, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = {
                        val enteredAmount = amountText.toDoubleOrNull() ?: totalDue
                        val defaultNotes = "Balance payment received for order $orderRef"
                        val finalNotes = notes.trim().ifBlank { defaultNotes }
                        val isoDate = toIsoUtcDate(completionDate)

                        onConfirm(
                            RecordOrderPaymentRequest(
                                amount = enteredAmount,
                                notes = finalNotes,
                                paymentDate = isoDate,
                                paymentMode = selectedMethod
                            )
                        )
                    },
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = if (isSubmitting) "Saving..." else "Confirm Payment",
                        color = whiteBg,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
        }
    }
}
private fun toIsoUtcDate(rawDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(rawDate.take(10)) ?: Date()
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        isoFormat.format(date)
    } catch (_: Exception) {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        isoFormat.format(Date())
    }
}
@Composable
private fun PaymentTypeToggle(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, if (selected) Primary else BorderGray, RoundedCornerShape(8.dp))
            .background(if (selected) primary_light else whiteBg)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (selected) Primary else TextPrimary
        )
    }
}

private fun formatDisplayDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = parser.parse(raw.take(10)) ?: return raw.take(10)
        SimpleDateFormat("dd/MM/yyyy", Locale.US).format(date)
    } catch (_: Exception) {
        raw.take(10)
    }
}