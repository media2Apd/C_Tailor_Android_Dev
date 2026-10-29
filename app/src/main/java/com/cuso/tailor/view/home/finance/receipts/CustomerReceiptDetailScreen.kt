@file:Suppress("UNUSED_PARAMETER", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.finance.receipts

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.CustomerPaymentDetailData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.currencySymbol
import com.cuso.tailor.view.home.pdfgenerator.FinancePaymentReceiptPdfGenerator
import com.cuso.tailor.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun CustomerReceiptDetailScreen(
    paymentId: String,
    viewModel: FinanceViewModel = hiltViewModel(),
    onClose: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val receipt by viewModel.receiptDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingReceiptDetail.collectAsStateWithLifecycle()
    var showVoidDialog by remember { mutableStateOf(false) }

    // PDF Generator instance
    val pdfGenerator = remember { FinancePaymentReceiptPdfGenerator(context) }

    LaunchedEffect(paymentId) {
        viewModel.fetchReceiptDetail(paymentId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Top bar with title and close action
        Row(Modifier.fillMaxWidth()) {
            TitleBar(title = "Payment Receipt", onClose = onClose)
        }

        if (isLoading || receipt == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(tokens.screenPadding)
            ) {
                ListSkeleton()
            }
        } else {
            val data = receipt!!

            // Dynamic status color resolution
            val isVoid = data.status.equals("Void", ignoreCase = true)
            val isPaidOrCompleted = data.status.equals("Paid", ignoreCase = true) ||
                    data.status.equals("Completed", ignoreCase = true)

            val statusBgColor = when {
                isVoid -> redBg
                isPaidOrCompleted -> greenBg
                else -> light_grey
            }

            val statusTextColor = when {
                isVoid -> redText
                isPaidOrCompleted -> greentext
                else -> headerGrey
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // Top subtitle row: Receipt number, status badge and reference
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                        ) {
                            Text(
                                text = data.paymentNumber.ifBlank { "RCPT" },
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )

                            // Status badge component
                            StatusBadge(
                                text = data.status.uppercase().ifBlank { "PAID" },
                                bgColor = statusBgColor,
                                textColor = statusTextColor,
                                showDot = false,
                                cornerRadius = tokens.cardCornerRadius
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Reference - ${data.referenceNumber?.ifBlank { "N/A" } ?: "N/A"}",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                }

                // Action buttons: PDF, Print, Void Payment
                Surface(
                    color = whiteBg,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = tokens.extraPadding * 0.8f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ActionItemButton(
                            icon = Icons.Outlined.FileDownload,
                            text = "PDF",
                            tint = Primary,
                            onClick = {
                                pdfGenerator.downloadReceiptPdf(data)
                            }
                        )

                        VerticalDivider(
                            modifier = Modifier.height(20.dp),
                            color = sectionBorder,
                            thickness = 1.dp
                        )

                        ActionItemButton(
                            icon = Icons.Outlined.Print,
                            text = "Print",
                            tint = Primary,
                            onClick = {
                                pdfGenerator.printReceipt(data)
                            }
                        )

                        VerticalDivider(
                            modifier = Modifier.height(20.dp),
                            color = sectionBorder,
                            thickness = 1.dp
                        )

                        ActionItemButton(
                            icon = Icons.Outlined.Cancel,
                            text = if (isVoid) "Voided" else "Void Payment",
                            tint = if (isVoid) mutedText else redText,
                            onClick = {
                                if (!isVoid) showVoidDialog = true
                                else DynamicIslandManager.showSuccess("Payment is already voided")
                            }
                        )
                    }
                }

                // Document slip matching the exact image layout
                ReceiptDocumentSlip(
                    data = data,
                    statusBgColor = statusBgColor,
                    statusTextColor = statusTextColor
                )
            }
        }
    }

    // Confirmation dialog for voiding a payment
    if (showVoidDialog) {
        AlertDialog(
            onDismissRequest = { showVoidDialog = false },
            title = {
                Text(
                    text = "Void Payment Receipt",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to void this receipt? This will reverse ledger entries.",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showVoidDialog = false
                        viewModel.voidCustomerPayment(paymentId) {
                            DynamicIslandManager.showSuccess("Payment receipt voided successfully")
                        }
                    }
                ) {
                    Text("Void", color = redText, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVoidDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = whiteBg,
            shape = RoundedCornerShape(tokens.cardCornerRadius)
        )
    }
}

/**
 * Visual document slip matching the exact design from the screenshot.
 */
@SuppressLint("DefaultLocale")
@Composable
private fun ReceiptDocumentSlip(
    data: CustomerPaymentDetailData,
    statusBgColor: Color,
    statusTextColor: Color
) {
    val tokens = LocalAppTokens.current

    // Safe assignment to avoid smart-cast issues
    val invoice = data.invoice

    val formattedDate = remember(data.paymentDate) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(data.paymentDate)
            if (date != null) {
                SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date)
            } else data.paymentDate.take(10)
        } catch (_: Exception) {
            data.paymentDate.take(10)
        }
    }

    val appliedAmount = data.amount - data.unallocatedAmount

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── SECTION 1: HEADER (RELDA Logo, Status, Receipt & Invoice Numbers) ──
            SlipSection(background = whiteBg) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(tokens.fieldHeight)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(primary_light)
                                .border(
                                    1.dp,
                                    light_blue_border,
                                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "R",
                                color = Primary,
                                fontWeight = FontWeight.Medium,
                                fontSize = tokens.bodyLarge
                            )
                        }
                        Spacer(modifier = Modifier.width(tokens.extraPadding))
                        Column {
                            Text(
                                text = "RELDA",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Text(
                                text = "Billing receipt",
                                fontSize = tokens.bodySmall,
                                color = headerGrey
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(
                            text = data.status.uppercase().ifBlank { "PAID" },
                            bgColor = statusBgColor,
                            textColor = statusTextColor,
                            showDot = false,
                            cornerRadius = tokens.cardCornerRadius
                        )
                        Spacer(modifier = Modifier.height(tokens.extraPadding * 0.5f))
                        SlipLabel(text = "RECEIPT NO")
                        Text(
                            text = data.paymentNumber,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        if (invoice != null) {
                            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.4f))
                            SlipLabel(text = "INVOICE NO")
                            Text(
                                text = invoice.invoiceNumber,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            SlipDivider()

            // ── SECTION 2: BILL TO & SHIP / DEPOSIT TO (2 Columns) ──
            SlipSection(background = whiteBg) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    // Left Column (BILL TO)
                    Column(modifier = Modifier.weight(1f)) {
                        SlipLabel(text = "BILL TO")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data.customer?.fullName ?: "Customer",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = data.customer?.email ?: "N/A",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Phone: ${data.customer?.mobileNumber ?: "N/A"}",
                            fontSize = tokens.caption,
                            color = TextPrimary
                        )
                    }

                    // Right Column (DEPOSIT TO)
                    Column(modifier = Modifier.weight(1f)) {
                        SlipLabel(text = "DEPOSIT TO")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data.depositToAccount?.accountName ?: "Cash on Hand",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Account: ${data.depositToAccount?.accountCode ?: "100200"}",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mode: ${data.paymentMode}",
                            fontSize = tokens.caption,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = sectionBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // 2x2 Meta Grid (Date, Email, Mode, Phone)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    MetaCell(
                        label = "Invoice date",
                        value = formattedDate,
                        modifier = Modifier.weight(1f)
                    )
                    MetaCell(
                        label = "Email",
                        value = data.customer?.email ?: "N/A",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(tokens.extraPadding))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    MetaCell(
                        label = "Due date",
                        value = formattedDate,
                        modifier = Modifier.weight(1f)
                    )
                    MetaCell(
                        label = "Phone",
                        value = data.customer?.mobileNumber ?: "N/A",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SlipDivider()

            // ── SECTION 3: ITEMS / ALLOCATION CARD (Matching Item List Design) ──
            SlipSection(background = whiteBg) {
                SlipLabel(text = "ITEMS")
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                ItemAllocationCard(
                    title = if (data.isAdvancePayment) {
                        "Direct Customer Advance Payment"
                    } else {
                        invoice?.invoiceNumber?.let { "Payment for Invoice $it" } ?: "Invoice Settlement Payment"
                    },
                    sku = "SKU-RCPT • ${data.paymentNumber}",
                    amount = formatRupee(data.amount),
                    unitPrice = formatRupee(data.amount),
                    qty = "1",
                    tax = "0%"
                )
            }

            SlipDivider()

            // ── SECTION 4: FINANCIAL SUMMARY & GRAND TOTAL ──
            SlipSection(background = whiteBg) {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)) {
                    SummaryRow(label = "Subtotal", value = formatRupee(data.amount))
                    SummaryRow(
                        label = "Applied to Invoice",
                        value = formatRupee(appliedAmount)
                    )
                    SummaryRow(
                        label = "Unallocated Balance",
                        value = formatRupee(data.unallocatedAmount),
                        valueColor = if (data.unallocatedAmount > 0) greentext else TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))
                HorizontalDivider(color = sectionBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(tokens.extraPadding))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GRAND TOTAL",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.6.sp,
                        color = headerGrey
                    )
                    Text(
                        text = formatRupee(data.amount),
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding))

                // Payment Method Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                        .background(whiteBg)
                        .border(
                            1.dp,
                            sectionBorder,
                            RoundedCornerShape(tokens.cardCornerRadius * 0.4f)
                        )
                        .padding(horizontal = tokens.extraPadding, vertical = 6.dp)
                ) {
                    InlineLabelValue(label = "Payment method: ", value = data.paymentMode)
                }
            }

            SlipDivider()

            // ── SECTION 5: BANK DETAILS ──
            SlipSection(background = whiteBg) {
                SlipLabel(text = "BANK DETAILS")
                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.5f)) {
                    InlineLabelValue(
                        label = "Bank Name: ",
                        value = data.depositToAccount?.accountName ?: "Global Commerce Bank"
                    )
                    InlineLabelValue(
                        label = "Account No: ",
                        value = data.depositToAccount?.accountCode ?: "100200",
                        monospace = true
                    )
                    InlineLabelValue(
                        label = "IFSC/SWIFT: ",
                        value = data.referenceNumber?.ifBlank { "GCBUS33XXX" } ?: "GCBUS33XXX",
                        monospace = true
                    )
                }
            }

            SlipDivider()

            // ── SECTION 6: PAYMENT QR & TERMS & CONDITIONS ──
            SlipSection(background = whiteBg) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                    verticalAlignment = Alignment.Top
                ) {
                    // QR Box
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(whiteBg)
                            .border(
                                1.dp,
                                sectionBorder,
                                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                            )
                            .padding(tokens.extraPadding * 0.8f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "UPI / PAYMENT QR",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            color = headerGrey,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(tokens.extraPadding * 0.4f))
                        Icon(
                            imageVector = Icons.Outlined.QrCode2,
                            contentDescription = "Payment QR",
                            tint = TextPrimary,
                            modifier = Modifier.size(tokens.imageDp)
                        )
                    }

                    // Terms text
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TERMS & CONDITIONS",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Payment due within 30 days of invoice date. " +
                                    "Late fees may apply. Goods remain property " +
                                    "of Apex Global Solutions until paid in full.",
                            fontSize = tokens.caption,
                            lineHeight = (tokens.caption.value * 1.5f).sp,
                            color = headerGrey
                        )
                    }
                }
            }

            SlipDivider()

            // ── SECTION 7: CREATED WITH & AUTHORIZED SIGNATURE ──
            SlipSection(background = whiteBg) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Created with cuso invoice",
                        fontSize = tokens.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = iconMuted
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        HorizontalDivider(
                            modifier = Modifier.width(110.dp),
                            color = iconMuted,
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Authorized Signature",
                            fontSize = tokens.label,
                            color = headerGrey
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// Reusable Component Helpers
// =============================================================

@Composable
private fun SlipSection(
    background: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding),
        content = content
    )
}

@Composable
private fun SlipDivider() {
    HorizontalDivider(color = sectionBorder, thickness = 1.dp)
}

@Composable
private fun SlipLabel(text: String) {
    val tokens = LocalAppTokens.current
    Text(
        text = text,
        fontSize = tokens.label,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.8.sp,
        color = iconMuted
    )
}

@Composable
private fun MetaCell(label: String, value: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier) {
        Text(text = label, fontSize = tokens.caption, color = mutedText)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

/**
 * Item Card exactly matching the visual layout of Item cards in the screenshot.
 */
@Composable
private fun ItemAllocationCard(
    title: String,
    sku: String,
    amount: String,
    unitPrice: String,
    qty: String,
    tax: String
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding)) {
            // Title & Total Amount row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = sku,
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }
                Spacer(modifier = Modifier.width(tokens.extraPadding))
                Text(
                    text = amount,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = grey_border, thickness = 1.dp)
            Spacer(modifier = Modifier.height(tokens.extraPadding))

            // Unit, Qty, Tax metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InlineLabelValue(label = "Unit: ", value = unitPrice)
                InlineLabelValue(label = "Qty: ", value = qty)
                InlineLabelValue(label = "Tax: ", value = tax)
            }
        }
    }
}

@Composable
private fun InlineLabelValue(
    label: String,
    value: String,
    monospace: Boolean = false
) {
    val tokens = LocalAppTokens.current
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = mutedText)) { append(label) }
            withStyle(
                SpanStyle(
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontFamily = if (monospace) FontFamily.Monospace else null
                )
            ) { append(value) }
        },
        fontSize = tokens.caption
    )
}

@Composable
private fun ActionItemButton(
    icon: ImageVector,
    text: String,
    tint: Color,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = tokens.extraPadding * 0.6f, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tint,
            modifier = Modifier.size(tokens.iconSize)
        )
        Text(
            text = text,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = tint
        )
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = TextSecondary)
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@SuppressLint("DefaultLocale")
private fun formatRupee(amount: Double): String = "${currencySymbol()}${String.format("%,.2f", amount)}"