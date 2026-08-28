@file:Suppress("SameParameterValue", "SameParameterValue","unused","unusedVariable")

package com.cuso.mobile.view.home.sales.payment_listing

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.blurScrim
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.view.composable.FormTextArea
import com.cuso.mobile.view.composable.FormTextField

// ─────────────────────────────────────────────────────────────────────────
// ADDITIONAL COLORS
// Existing theme colors (Primary, TextPrimary, mutedText, BorderGray,
// PanelBg, greentext/greenBg, redText/redBg, yellowText/yellowBg, whiteBg,
// close_color, title_color, title_font, blackTitle) are reused as-is from
// the theme file. Only the few tokens missing from the theme are added below.
// ─────────────────────────────────────────────────────────────────────────

private val LinkChipBg = Color(0xFFF0FDF4)
private val UpiChipBg = Color(0xFFDBEAFE)
private val UpiChipText = Color(0xFF1654E7)

// ─────────────────────────────────────────────────────────────────────────
// SAMPLE DATA MODELS (design-only placeholders)
// ─────────────────────────────────────────────────────────────────────────
private data class OrderLineItem(
    val name: String,
    val qty: Int,
    val rate: Int,
    val amount: Int
)

private data class PaymentHistoryEntry(
    val date: String,
    val method: String,
    val refId: String,
    val amount: Int
)

private val sampleLineItems = listOf(
    OrderLineItem("Cotton Saree", 3, 1200, 3600),
    OrderLineItem("Blouse Stitching", 3, 250, 750)
)

private val sampleHistory = listOf(
    PaymentHistoryEntry("12/02/2026", "UPI", "UPI452190", 1000),
    PaymentHistoryEntry("14/02/2026", "CASH", "CASH7712", 1000)
)

// ─────────────────────────────────────────────────────────────────────────
// PAYMENT INFORMATION SCREEN
// Scaffold's topBar reuses the shared TitleBar composable. The order
// reference / phone / date row sits inside the scrollable content.
// The inline Receive Payment card stays exactly as it was — its button
// now ALSO opens the ReceivePaymentSheet (SmoothBottomSheet) shown at the
// bottom of this file. Nothing existing was removed.
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun PaymentInformationScreen(
    onClose: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val subtotal = sampleLineItems.sumOf { it.amount }
    val tax = 0
    val totalAmount = subtotal + tax
    val amountPaid = 2000
    val balanceDue = totalAmount - amountPaid

    // ── NEW — bottom sheet state for the Receive Payment sheet ──
    var receiveSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var sheetBlur by remember { mutableStateOf(0.dp) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                TitleBar("Payment Information", onClose = onClose)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .blurScrim(sheetBlur)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp)
            ) {
                OrderReferenceRow(
                    orderRef = "ORD014 / Meena Textiles",
                    phone = "+91 98452 77654",
                    orderDate = "12/02/2026",
                    deliveryDate = "22/02/2026"
                )

                Spacer(Modifier.height(16.dp))

                OrderSummaryCard(
                    items = sampleLineItems,
                    subtotal = subtotal,
                    tax = tax,
                    totalAmount = totalAmount,
                    amountPaid = amountPaid,
                    balanceDue = balanceDue
                )

                Spacer(Modifier.height(20.dp))

                SendPaymentLinkCard(
                    balanceDue = balanceDue,
                    lastSentLabel = "Today • 10:42 AM"
                )

                Spacer(Modifier.height(20.dp))

                // ── Existing inline card — untouched. Its button now also ──
                // ── opens the ReceivePaymentSheet below.                  ──
                ReceivePaymentCard(
                    totalDue = balanceDue,
                    onReceivePaymentClick = { receiveSheetState = SheetValue.Collapsed }
                )

                Spacer(Modifier.height(24.dp))

                PaymentHistoryList(history = sampleHistory)

                Spacer(Modifier.height(24.dp))
            }
        }

        // ── NEW — Receive Payment bottom sheet, matches the screenshot ──
        ReceivePaymentSheet(
            orderRef = "ORD-88291",
            totalDue = balanceDue,
            sheetState = receiveSheetState,
            onStateChange = { receiveSheetState = it },
            onBlurScrimChange = { r, _ -> sheetBlur = r },
            onDismiss = { receiveSheetState = SheetValue.Hidden },
            onConfirm = { receiveSheetState = SheetValue.Hidden }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// SHARED SECTION HEADER
// Used by Order Summary, Send Payment Link, Receive Payment, Payment History
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
            title,
            fontSize = tokens.bodyLarge,
            color = TextPrimary
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
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding * 0.5f)
    ) {
        Text(
            orderRef,
            fontSize = tokens.bodyMedium,
            color = TitleColor
        )

        Spacer(Modifier.height(18.dp))

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
            .height(40.dp)
            .background(BorderGray)
    )
}

@Composable
private fun HeaderInfoField(label: String, value: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier.padding(horizontal = tokens.screenPadding * 0.5f)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(label, fontSize = tokens.caption, color = mutedText)
        }
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(value, fontSize = tokens.bodySmall, color = TextPrimary)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// ORDER SUMMARY CARD
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun OrderSummaryCard(
    items: List<OrderLineItem>,
    subtotal: Int,
    tax: Int,
    totalAmount: Int,
    amountPaid: Int,
    balanceDue: Int
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        SectionHeader("Order Summary")

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
        ) {
            items.forEachIndexed { _, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(item.name, fontSize = tokens.bodyMedium, color = TextPrimary)
                        Text(
                            "Qty: ${item.qty} | Rate: ₹${item.rate}",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    }
                    Text(
                        "₹${formatAmount(item.amount)}",
                        fontSize = tokens.bodyMedium,
                        color = TextPrimary
                    )
                }
                HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 4.dp))
            }

            SummaryRow("Subtotal", "₹${formatAmount(subtotal)}")
            SummaryRow("Tax (0%)", "₹${formatAmount(tax)}")

            HorizontalDivider(color = BorderGray, modifier = Modifier.padding(vertical = 8.dp))

            SummaryRow("Total Amount", "₹${formatAmount(totalAmount)}", emphasize = true)
            SummaryRow("Amount Paid", "₹${formatAmount(amountPaid)}", valueColor = greentext)

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(redBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Balance Due", fontSize = tokens.bodyMedium, color = redText)
                Text(
                    "₹${formatAmount(balanceDue)}",
                    fontSize = tokens.bodyLarge,
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
    valueColor: Color = TextPrimary,
    emphasize: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = tokens.bodySmall,
            color = if (emphasize) TextPrimary else mutedText
        )
        Text(
            value,
            fontSize = tokens.bodySmall,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// SEND PAYMENT LINK CARD
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun SendPaymentLinkCard(
    balanceDue: Int,
    lastSentLabel: String
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
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
                    .padding(10.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_link_chain),
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "Send Payment Link",
                    fontSize = tokens.bodyMedium,
                    color = Primary
                )
                Text(
                    "Share a secure UPI payment link with your customer",
                    fontSize = tokens.bodySmall,
                    color = mutedText
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding)
        ) {
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LinkChipBg, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Balance Due", fontSize = tokens.bodySmall, color = mutedText)
                    Text(
                        "₹${formatAmount(balanceDue)}",
                        fontSize = tokens.bodyMedium,
                        color = redText
                    )
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Last Sent:", fontSize = tokens.bodySmall, color = greentext)
                    Text(lastSentLabel, fontSize = tokens.bodySmall, color = greentext)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor =darkGreenBg)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_whatsapp), contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("WhatsApp", color = whiteBg, fontSize = tokens.bodySmall)
                }
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(painter = painterResource(R.drawable.ic_mail), contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Send via Email", color = whiteBg, fontSize = tokens.bodySmall)
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "or",
                fontSize = tokens.caption,
                color = mutedText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Primary)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Primary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy Link", color = Primary, fontSize = tokens.bodySmall)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// RECEIVE PAYMENT — INLINE CARD (UNCHANGED from before — same fields,
// same layout, same tokens. Only new bit: accepts onReceivePaymentClick
// so its existing button can also trigger the new bottom sheet below.)
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun ReceivePaymentCard(
    totalDue: Int,
    onReceivePaymentClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var amountText by remember { mutableStateOf("$totalDue.00") }
    var paymentMode by remember { mutableStateOf("UPI") }
    var paymentModeExpanded by remember { mutableStateOf(false) }
    var referenceNo by remember { mutableStateOf("") }
    var transactionDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("Receive Payment")

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    FormLabel("Amount Received (₹)")
                    Spacer(Modifier.height(6.dp))
                    FormTextField(
                        value = amountText,
                        onValueChange = { amountText = it }
                    )
                }
                Column(Modifier.weight(1f)) {
                    FormLabel("Payment Mode")
                    FormDropdown(
                        value = paymentMode,
                        options = listOf("Cash", "UPI", "Card"),
                        expanded = paymentModeExpanded,
                        onExpandChange = { paymentModeExpanded = it },
                        onOptionSelected = { paymentMode = it; paymentModeExpanded = false }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    FormLabel("Reference No.")
                    Spacer(Modifier.height(6.dp))

                    FormTextField(
                        value = referenceNo,
                        onValueChange = { referenceNo = it },
                        placeholder = "UPI875421",
                    )
                }
                Column(Modifier.weight(1f)) {
                    FormLabel("Transaction Date")
                    Spacer(Modifier.height(6.dp))
                    DatePickerField(
                        value = transactionDate,
                        onDateSelected = { transactionDate = it }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            FormLabel("Notes")
            Spacer(Modifier.height(6.dp))
            FormTextArea(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Final balance cleared"
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = onReceivePaymentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    "Receive Payment",
                    color = whiteBg,
                    fontSize = tokens.bodyMedium
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// PAYMENT HISTORY LIST
// ─────────────────────────────────────────────────────────────────────────
@Composable
private fun PaymentHistoryList(history: List<PaymentHistoryEntry>) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth()
                .background(whiteBg)
        ) {
            SectionHeader("Payment History")
        }

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            history.forEachIndexed { idx, entry ->
                PaymentHistoryRow(entry)
                if (idx != history.lastIndex) HorizontalDivider(color = BorderGray)
            }
        }
    }
}

@Composable
private fun PaymentHistoryRow(entry: PaymentHistoryEntry) {
    val tokens = LocalAppTokens.current
    val (chipBg, chipText) = if (entry.method == "UPI") UpiChipBg to UpiChipText else yellowBg to yellowText

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(entry.date, fontSize = tokens.bodySmall, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text("ID: ${entry.refId}", fontSize = tokens.caption, color = mutedText)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(chipBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(entry.method, fontSize = tokens.label, color = chipText)
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "₹${formatAmount(entry.amount)}",
                fontSize = tokens.bodyMedium,
                color = TextPrimary
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
// NEW — RECEIVE PAYMENT BOTTOM SHEET (matches the screenshot)
// Purely additive: SmoothBottomSheet with Total card, Payment Type toggle,
// Amount field, Payment Method icon row, Reference/Completion Date, Notes,
// Cancel / Confirm Payment. Nothing above this was removed to add it.
// ─────────────────────────────────────────────────────────────────────────
@Composable
fun ReceivePaymentSheet(
    orderRef: String,
    totalDue: Int,
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    onBlurScrimChange: (radius: androidx.compose.ui.unit.Dp, scrim: Float) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var isFullAmount by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf("$totalDue.00") }
    var selectedMethod by remember { mutableStateOf("Cash") }
    var referenceNo by remember { mutableStateOf("") }
    var completionDate by remember { mutableStateOf("06-07-2026") }
    var notes by remember { mutableStateOf("") }

    LaunchedEffect(isFullAmount) {
        if (isFullAmount) amountText = "$totalDue.00"
    }

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        collapsedFraction = 0.55f,
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
                "RECEIVE PAYMENT",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 0.5.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            // Total card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(light_blue, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                    .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total", fontSize = tokens.bodyMedium, color = TitleColor)
                    Text("For Order #$orderRef", fontSize = tokens.caption, color = mutedText)
                }
                Text(
                    "₹${formatAmount(totalDue)}",
                    fontSize = tokens.h2,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            Spacer(Modifier.height(18.dp))

            FormLabel("Payment Type")
            Spacer(Modifier.height(8.dp))
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

            Spacer(Modifier.height(16.dp))

            FormLabel("Amount (₹)")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { if (!isFullAmount) amountText = it },
                readOnly = isFullAmount,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = tokens.bodySmall),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderGray,
                    focusedBorderColor = Primary,
                    disabledBorderColor = BorderGray,
                    disabledTextColor = TextPrimary,
                    disabledContainerColor = PanelBg
                )
            )

            Spacer(Modifier.height(16.dp))

            FormLabel("Payment Method")
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PaymentMethodOption(
                    label = "Cash",
                    icon = R.drawable.revenue,
                    selected = selectedMethod == "Cash",
                    modifier = Modifier.weight(1f)
                ) { selectedMethod = "Cash" }
                PaymentMethodOption(
                    label = "UPI",
                    icon = R.drawable.ic_qr,
                    selected = selectedMethod == "UPI",
                    modifier = Modifier.weight(1f)
                ) { selectedMethod = "UPI" }
                PaymentMethodOption(
                    label = "Card",
                    icon = R.drawable.ic_credit,
                    selected = selectedMethod == "Card",
                    modifier = Modifier.weight(1f)
                ) { selectedMethod = "Card" }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    FormLabel("Reference No. (Opt)")
                    Spacer(Modifier.height(6.dp))
                    FormTextField(
                        value = referenceNo,
                        onValueChange = { referenceNo = it },
                        placeholder = "UPI875421",
                    )
                }
                Column(Modifier.weight(1f)) {
                    FormLabel("Completion Date")
                    Spacer(Modifier.height(6.dp))
                    DatePickerField(
                        value = completionDate,
                        onDateSelected = { completionDate = it }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            FormLabel("Notes")
            Spacer(Modifier.height(6.dp))
            FormTextArea(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Add internal remarks about this transaction"
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Text("Cancel", color = TextPrimary, fontSize = tokens.bodyMedium)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(painterResource(R.drawable.ic_tick), contentDescription = null, tint = whiteBg, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Confirm Payment", color = whiteBg, fontSize = tokens.bodySmall)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
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
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = if (selected) Primary else BorderGray,
                shape = RoundedCornerShape(10.dp)
            )
            .background(if (selected) light_blue else whiteBg)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = tokens.bodySmall,
            color = if (selected) Primary else TextPrimary
        )
    }
}

@Composable
private fun PaymentMethodOption(
    label: String,
    icon: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp ,
                color = if (selected) Primary else BorderGray,
                shape = RoundedCornerShape(10.dp)
            )
            .background(if (selected) light_blue else whiteBg)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (selected) Primary else mutedText,
            modifier = Modifier.size(tokens.iconSize)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (selected) Primary else TextPrimary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────
private fun formatAmount(value: Int): String {
    val s = value.toString()
    if (s.length <= 3) return s
    val last3 = s.takeLast(3)
    val rest = s.dropLast(3)
    val grouped = rest.reversed().chunked(2).joinToString(",").reversed()
    return "$grouped,$last3"
}

// ─────────────────────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PaymentInformationScreenPreview() {
    PaymentInformationScreen()
}