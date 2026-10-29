@file:Suppress("unused","AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.finance.funds.cashbooks

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CallMade
import androidx.compose.material.icons.automirrored.outlined.CallReceived
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.CashDrawerShiftItemDto
import com.cuso.tailor.model.finance.CloseCashShiftRequest
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.pdfgenerator.CashDrawerPdfGenerator
import com.cuso.tailor.viewmodel.CloseCashShiftState
import com.cuso.tailor.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

@Composable
fun CashBookDetailScreen(
    shiftId: String,
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val shift by viewModel.selectedCashShift.collectAsState()
    val isLoading by viewModel.isLoadingCashShifts.collectAsState()

    var showCloseShiftDialog by remember { mutableStateOf(false) }

    LaunchedEffect(shiftId) {
        viewModel.fetchCashShiftById(shiftId)
    }

    val pdfGenerator = remember { CashDrawerPdfGenerator(context) }

    // Close Shift Dialog
    if (showCloseShiftDialog && shift != null) {
        CloseCashDrawerShiftDialog(
            item = shift!!,
            viewModel = viewModel,
            onDismiss = { showCloseShiftDialog = false },
            onSuccess = {
                showCloseShiftDialog = false
                viewModel.fetchCashShiftById(shiftId)
                viewModel.fetchCashShifts()
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleBar(title = "Shift Details", onClose = onBack)
            }
        }
    ) { padding ->
        if (isLoading || shift == null) {
            ListSkeleton()
        } else {
            val item: CashDrawerShiftItemDto = shift!!
            val isClosed = item.status.equals("Closed", ignoreCase = true)
            val diff = item.discrepancyAmount

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // =============================================================
                // Header Card with Close Shift and Print Buttons
                // =============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
                ) {
                    Column(modifier = Modifier.padding(tokens.cardPadding * 0.75f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.shiftNumber,
                                        fontSize = tokens.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = TitleColor
                                    )
                                    Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                                    StatusBadge(
                                        text = if (isClosed) "Closed" else "Open",
                                        bgColor = if (isClosed) light_grey else greenBg,
                                        textColor = if (isClosed) headerGrey else greentext
                                    )
                                }
                                Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                                Text(
                                    text = "Shift ledger opened by ${item.cashierId?.fullName ?: "Cashier"}",
                                    fontSize = tokens.caption,
                                    color = mutedText
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Close Shift Button (Only visible when status is Open)
                                if (!isClosed) {
                                    Button(
                                        onClick = { showCloseShiftDialog = true },
                                        modifier = Modifier.height(tokens.buttonHeight * 0.9f),
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                        colors = ButtonDefaults.buttonColors(containerColor = redBg, disabledContainerColor = redBg),
                                        border = BorderStroke(1.dp, redText),
                                        contentPadding = PaddingValues(horizontal = 5.dp)
                                    ) {
                                        Text(
                                            text = "Close",
                                            fontSize = tokens.bodySmall,
                                            color = redText,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Print Ledger Button
                                OutlinedButton(
                                    onClick = { pdfGenerator.printShiftReport(item) },
                                    modifier = Modifier.height(tokens.buttonHeight * 0.9f),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(BorderGray)),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Print,
                                        contentDescription = "Print",
                                        tint = TitleColor,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Print",
                                        fontSize = tokens.bodySmall,
                                        color = TitleColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // =============================================================
                // 2x2 Mobile Grid: 4 Core Cash Drawer Metrics
                // =============================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    MetricBox(
                        title = "Opening Float",
                        amount = "${currencySymbol()}${formatAmount(item.openingBalance)}",
                        caption = "Starting drawer balance",
                        icon = Icons.Outlined.AccountBalanceWallet,
                        iconTint = headerGrey,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Cash Inflows (Sales)",
                        amount = "+${currencySymbol()}${formatAmount(item.totalCashCollected)}",
                        caption = "POS Receipts & Sales",
                        icon = Icons.AutoMirrored.Outlined.CallReceived,
                        iconTint = greentext,
                        amountColor = greentext,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    MetricBox(
                        title = "Cash Outflows",
                        amount = "-${currencySymbol()}${formatAmount(item.totalCashWithdrawn)}",
                        caption = "Expenses & Contra Deposits",
                        icon = Icons.AutoMirrored.Outlined.CallMade,
                        iconTint = orangeText,
                        amountColor = redText,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Expected In Drawer",
                        amount = "${currencySymbol()}${formatAmount(item.expectedClosingBalance)}",
                        caption = "Calculated running total",
                        icon = Icons.Outlined.Storefront,
                        iconTint = Primary,
                        amountColor = Primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // =============================================================
                // Drawer Reconciliation Summary
                // =============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
                ) {
                    Column(modifier = Modifier.padding(tokens.cardPadding * 0.75f)) {
                        Text(
                            text = "DRAWER RECONCILIATION SUMMARY",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = mutedText
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                        Text(
                            text = if (isClosed) "Shift closed at ${formatDateTime(item.closingTime)} by ${item.closedBy?.fullName ?: "Cashier"}" else "Shift is currently in session",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = tokens.extraPadding),
                            color = dividerColor
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Counted Cash", fontSize = tokens.label, color = mutedText)
                                Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                Text(
                                    text = "${currencySymbol()}${formatAmount(item.actualClosingBalance)}",
                                    fontSize = tokens.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = TitleColor
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Discrepancy", fontSize = tokens.label, color = mutedText)
                                Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                val diffPrefix = if (diff >= 0) "+${currencySymbol()}" else "-${currencySymbol()}"
                                val diffColor = when {
                                    diff > 0 -> greentext
                                    diff < 0 -> redText
                                    else -> TitleColor
                                }
                                Text(
                                    text = "$diffPrefix${formatAmount(abs(diff))}",
                                    fontSize = tokens.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = diffColor
                                )
                            }
                        }
                    }
                }

                // =============================================================
                // Shift Configuration Card
                // =============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.cardPadding * 0.75f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Text(
                            text = "Shift Configuration",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )

                        DetailRow("Branch:", item.branchId?.name ?: "Main Branch")
                        DetailRow(
                            "GL Cash Account:",
                            "${item.cashAccountId?.accountName ?: "Cash on Hand"} (${item.cashAccountId?.accountCode ?: "100200"})"
                        )
                        DetailRow("Cashier:", item.cashierId?.fullName ?: "—")
                        DetailRow("Session Opened:", formatDateTime(item.openingTime))
                    }
                }

                // =============================================================
                // Audit Notes & Activity Trail
                // =============================================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
                ) {
                    Column(modifier = Modifier.padding(tokens.cardPadding * 0.75f)) {
                        Text(
                            text = "Audit Notes & Activity Trail",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TitleColor
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(badgeGrey)
                                .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(tokens.cardPadding * 0.6f)
                        ) {
                            Text(
                                text = item.notes?.ifBlank { "No notes recorded for this shift." }
                                    ?: "No notes recorded for this shift.",
                                fontSize = tokens.bodySmall,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Created: ${formatDateTime(item.createdAt)}",
                                fontSize = tokens.label,
                                color = mutedText
                            )
                            Text(
                                text = "Record ID: ${item.id.takeLast(8)}",
                                fontSize = tokens.label,
                                color = mutedText
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Close Shift & Reconcile Cash Modal Dialog
 */
@Composable
fun CloseCashDrawerShiftDialog(
    item: CashDrawerShiftItemDto,
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    var physicalCount by remember { mutableStateOf("") }
    val todayFormatted = remember { SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date()) }
    var closeDate by remember { mutableStateOf(todayFormatted) }
    var auditNotes by remember { mutableStateOf("") }

    val closeState by viewModel.closeCashShiftState.collectAsState()
    val isSubmitting = closeState is CloseCashShiftState.Loading

    val netActivity = item.totalCashCollected - item.totalCashWithdrawn

    LaunchedEffect(closeState) {
        if (closeState is CloseCashShiftState.Error) {
            Toast.makeText(context, (closeState as CloseCashShiftState.Error).message, Toast.LENGTH_LONG).show()
            viewModel.resetCloseCashShiftState()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.cardPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // Header with Lock Icon & Shift ID
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }

                        Column {
                            Text(
                                text = "Close Shift & Reconcile Cash",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TitleColor
                            )
                            Text(
                                text = "Shift #${item.shiftNumber}",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close",
                            tint = close_color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = dividerColor)

                // Metric Banner: Opening Float / Net Activity / Expected Drawer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeGrey)
                        .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                        .padding(tokens.cardPadding * 0.6f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Opening Float", fontSize = tokens.label, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${currencySymbol()}${formatAmount(item.openingBalance)}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TitleColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Net Activity", fontSize = tokens.label, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            val netPrefix = if (netActivity >= 0) "+${currencySymbol()}" else "-${currencySymbol()}"
                            val netColor = if (netActivity >= 0) greentext else redText
                            Text(
                                text = "$netPrefix${formatAmount(abs(netActivity))}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = netColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Expected Drawer", fontSize = tokens.label, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${currencySymbol()}${formatAmount(item.expectedClosingBalance)}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TitleColor
                            )
                        }
                    }
                }

                // Input Row: Physical Cash Count & Close Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Physical Cash Count", isRequired = true)
                        FormTextField(
                            value = physicalCount,
                            onValueChange = { physicalCount = it },
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Decimal,
                            containerColor = whiteBg,
                            borderColor = BorderGray,
                            textColor = TitleColor
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Close Timestamp", isRequired = false)
                        DatePickerField(
                            value = closeDate,
                            onDateSelected = { closeDate = it }
                        )
                    }
                }

                // Audit Closing Notes
                Column {
                    FormLabel(text = "Audit Closing Notes", isRequired = false)
                    FormTextArea(
                        value = auditNotes,
                        onValueChange = { auditNotes = it },
                        placeholder = "Reason for variance, hand-over receiver name, denomination breakdown...",
                        minLines = 3,
                        maxLines = 4,
                        borderColor = BorderGray,
                        textColor = TitleColor
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                HorizontalDivider(color = dividerColor)

                // Actions: Cancel & Lock & Finalize Shift
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, BorderGray),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                    ) {
                        Text(
                            text = "Cancel",
                            color = TitleColor,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(Modifier.width(tokens.extraPadding))

                    Button(
                        onClick = {
                            val count = physicalCount.toDoubleOrNull()
                            if (count == null || count < 0.0) {
                                Toast.makeText(context, "Please enter a valid physical cash count", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val currentIsoTime = SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                Locale.US
                            ).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }.format(Date())

                            val request = CloseCashShiftRequest(
                                actualClosingBalance = count,
                                closingTime = currentIsoTime
                            )

                            viewModel.closeCashDrawerShift(item.id, request) { successMsg ->
                                Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
                                viewModel.resetCloseCashShiftState()
                                onSuccess()
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = redText
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        } else {
                            Text(
                                text = "Lock & Finalize Shift",
                                color = Color.White,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    modifier: Modifier = Modifier,
    title: String,
    amount: String,
    caption: String,
    icon: ImageVector,
    iconTint: Color,
    amountColor: Color = TitleColor
) {
    val tokens = LocalAppTokens.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(PrimaryBorder))
    ) {
        Column(
            modifier = Modifier.padding(tokens.cardPadding * 0.65f),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Normal,
                    color = mutedText
                )
                Box(
                    modifier = Modifier
                        .size(tokens.iconSize * 1.3f)
                        .background(badgeGrey, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(tokens.iconSize * 0.9f)
                    )
                }
            }
            Text(
                text = amount,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = amountColor
            )
            Text(
                text = caption,
                fontSize = tokens.label,
                color = mutedText
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.caption, color = mutedText)
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TitleColor
        )
    }
}

private fun formatAmount(amount: Double): String =
    String.format(Locale.US, "%,.2f", amount)

private fun formatDateTime(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US)
        parser.parse(iso)?.let { formatter.format(it) } ?: iso.take(16)
    } catch (_: Exception) {
        iso.take(16)
    }
}