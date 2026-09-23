@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.procurement.billslist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.outlinedButtonBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.ProcurementBillDetailData
import com.cuso.tailor.model.inventory.ProcurementBillDetailLine
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun PurchaseDetailScreen(
    billId: String,
    onClose: () -> Unit,
    onPreviewPdf: () -> Unit = {},
    onDownloadPdf: () -> Unit = {},
    onRecordPayment: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val billDetail by viewModel.selectedBillDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingBillDetail.collectAsStateWithLifecycle()
    val error by viewModel.billDetailError.collectAsStateWithLifecycle()

    val isActionInProgress by viewModel.isBillActionInProgress.collectAsStateWithLifecycle()
    val actionSuccessMessage by viewModel.billActionSuccessMessage.collectAsStateWithLifecycle()
    val actionErrorMessage by viewModel.billActionErrorMessage.collectAsStateWithLifecycle()

    var isPreviewMode by remember { mutableStateOf(false) }
    var showVoidDialog by remember { mutableStateOf(false) }

    LaunchedEffect(billId) {
        if (billId.isNotBlank()) {
            viewModel.fetchBillDetail(billId)
        }
    }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                TitleBar(if (isPreviewMode) "Bill Preview" else "Purchase Detail", onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                error != null -> {
                    AppErrorState(
                        title = "Failed to load bill details",
                        message = error ?: "Unknown error occurred",
                        onRetry = { viewModel.fetchBillDetail(billId) }
                    )
                }

                billDetail != null -> {
                    val detail = billDetail!!
                    val (statusBg, statusTextColor) = resolveBillStatusColors(detail.status)
                    val statusNormalized = detail.status.trim().lowercase()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = tokens.extraPadding * 1.2f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
                    ) {
                        // Section 1: Header and Action Buttons
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(0.dp),
                                colors = CardDefaults.cardColors(containerColor = whiteBg),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                            ) {
                                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = detail.billNumber.ifBlank { "-" },
                                            fontSize = tokens.h2,
                                            fontWeight = FontWeight.Bold,
                                            color = title_color
                                        )
                                        Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                                .background(statusBg)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(statusTextColor)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = detail.status.ifBlank { "-" }.replaceFirstChar { it.uppercase() },
                                                    fontSize = tokens.label,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = statusTextColor
                                                )
                                            }
                                        }
                                        Spacer(Modifier.weight(1f))
                                        IconButton(
                                            onClick = { },
                                            modifier = Modifier.size(tokens.iconSize * 1.3f)
                                        ) {
                                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = close_color)
                                        }
                                    }

                                    Spacer(Modifier.height(2.dp))
                                    val poNumber = detail.purchaseOrderId?.poNumber?.ifBlank { null }
                                        ?: detail.supplierBillReference?.ifBlank { null }
                                        ?: "-"
                                    Text(
                                        text = "PO Number - $poNumber",
                                        fontSize = tokens.caption,
                                        color = close_color
                                    )

                                    Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                                    // Dynamic Action Buttons Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            space = tokens.extraPadding * 0.6f,
                                            alignment = Alignment.CenterHorizontally
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Preview / Close Preview Button
                                        if (isPreviewMode) {
                                            Button(
                                                onClick = { isPreviewMode = false },
                                                colors = ButtonDefaults.buttonColors(containerColor = activity_purple_bg),
                                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.VisibilityOff,
                                                    contentDescription = null,
                                                    tint = Primary,
                                                    modifier = Modifier.size(tokens.iconSize * 0.85f)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = "Close Preview",
                                                    fontSize = tokens.caption,
                                                    color = Primary
                                                )
                                            }
                                        } else {
                                            OutlinedButton(
                                                onClick = {
                                                    isPreviewMode = true
                                                    onPreviewPdf()
                                                },
                                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                border = outlinedButtonBorder(enabled = true).copy(
                                                    brush = androidx.compose.ui.graphics.SolidColor(sectionBorder)
                                                ),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Visibility,
                                                    contentDescription = null,
                                                    tint = title_color,
                                                    modifier = Modifier.size(tokens.iconSize * 0.85f)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = "Preview PDF",
                                                    fontSize = tokens.caption,
                                                    color = title_color
                                                )
                                            }
                                        }

                                        // 2. Download Icon Button
                                        Box(
                                            modifier = Modifier
                                                .size(tokens.buttonHeight * 0.85f)
                                                .border(
                                                    width = 1.dp,
                                                    color = sectionBorder,
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f)
                                                )
                                                .clickable { onDownloadPdf() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Download,
                                                contentDescription = "Download",
                                                tint = title_color,
                                                modifier = Modifier.size(tokens.iconSize * 0.9f)
                                            )
                                        }

                                        // 3. Status Specific Action Buttons
                                        when (statusNormalized) {
                                            "draft" -> {
                                                // Send Button for Draft Status
                                                Button(
                                                    onClick = {
                                                        viewModel.sendBill(detail.id)
                                                    },
                                                    enabled = !isActionInProgress,
                                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                                                ) {
                                                    if (isActionInProgress) {
                                                        CircularProgressIndicator(
                                                            color = whiteBg,
                                                            strokeWidth = 2.dp,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    } else {
                                                        Text(
                                                            text = "Send",
                                                            fontSize = tokens.caption,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = whiteBg
                                                        )
                                                    }
                                                }
                                            }

                                            "sent", "pending", "open" -> {
                                                // Void Button
                                                OutlinedButton(
                                                    onClick = { showVoidDialog = true },
                                                    enabled = !isActionInProgress,
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                    border = outlinedButtonBorder(enabled = true).copy(
                                                        brush = androidx.compose.ui.graphics.SolidColor(sectionBorder)
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                                                ) {
                                                    Text(
                                                        text = "Void",
                                                        fontSize = tokens.caption,
                                                        color = title_color
                                                    )
                                                }

                                                // Record Payment Button
                                                Button(
                                                    onClick = onRecordPayment,
                                                    enabled = !isActionInProgress,
                                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                                                ) {
                                                    Text(
                                                        text = "Record Payment",
                                                        fontSize = tokens.caption,
                                                        color = whiteBg
                                                    )
                                                }
                                            }

                                            "void", "cancelled" -> {
                                                // Disabled Void Button
                                                OutlinedButton(
                                                    onClick = { },
                                                    enabled = false,
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                    border = outlinedButtonBorder(enabled = false).copy(
                                                        brush = androidx.compose.ui.graphics.SolidColor(sectionBorder.copy(alpha = 0.5f))
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                                                ) {
                                                    Text(
                                                        text = "Void",
                                                        fontSize = tokens.caption,
                                                        color = close_color.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }

                                            else -> {
                                                if (detail.balanceDue > 0.0) {
                                                    Button(
                                                        onClick = onRecordPayment,
                                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                        modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                                                    ) {
                                                        Text(
                                                            text = "Record Payment",
                                                            fontSize = tokens.caption,
                                                            color = whiteBg
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Preview Mode vs Standard Purchase Details
                        if (isPreviewMode) {
                            item {
                                BillPreviewReceiptCard(detail = detail, tokens = tokens)
                            }
                        } else {
                            // Section 2: PURCHASE DETAILS
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(0.dp),
                                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                                ) {
                                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                                        Text(
                                            text = "PURCHASE DETAILS",
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = title_color
                                        )
                                        Spacer(Modifier.height(tokens.extraPadding))

                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.weight(1.3f)) {
                                                Text("Vendor", fontSize = tokens.label, color = close_color)
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = detail.supplierSnapshot?.name?.ifBlank { null }
                                                        ?: detail.supplierId?.name?.ifBlank { null }
                                                        ?: "-",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = title_color
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Bill Date", fontSize = tokens.label, color = close_color)
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = formatBillDate(detail.billDate),
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = title_color
                                                )
                                            }
                                        }

                                        Spacer(Modifier.height(tokens.extraPadding))

                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.weight(1.3f)) {
                                                Text("Due Date", fontSize = tokens.label, color = close_color)
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = formatBillDate(detail.dueDate),
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = title_color
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Balance Due", fontSize = tokens.label, color = close_color)
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = "₹${formatIndianNumber(detail.balanceDue)}",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = title_color
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 3: ITEMS
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = tokens.screenPadding)
                                    ) {
                                        Text(
                                            text = "ITEMS",
                                            fontSize = tokens.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = title_color
                                        )
                                    }

                                    if (detail.lines.isEmpty()) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(0.dp),
                                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                                        ) {
                                            Text(
                                                text = "-",
                                                fontSize = tokens.caption,
                                                color = close_color,
                                                modifier = Modifier.padding(tokens.screenPadding)
                                            )
                                        }
                                    } else {
                                        detail.lines.forEach { line ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(0.dp),
                                                colors = CardDefaults.cardColors(containerColor = whiteBg),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(tokens.screenPadding)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = line.item?.name?.ifBlank { null }
                                                                ?: line.itemDescription?.ifBlank { null }
                                                                ?: "-",
                                                            fontSize = tokens.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = title_color
                                                        )
                                                        Text(
                                                            text = "Amount",
                                                            fontSize = tokens.label,
                                                            color = close_color
                                                        )
                                                    }

                                                    Spacer(Modifier.height(3.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "Account: ${line.expenseAccount?.accountName?.ifBlank { null } ?: line.lineType?.ifBlank { null } ?: "Product"}",
                                                            fontSize = tokens.caption,
                                                            color = close_color
                                                        )
                                                        Text(
                                                            text = "₹${formatIndianNumber(line.lineTotal)}",
                                                            fontSize = tokens.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = title_color
                                                        )
                                                    }

                                                    Spacer(Modifier.height(5.dp))
                                                    HorizontalDivider(color = grey_border, thickness = 2.dp)
                                                    Spacer(Modifier.height(5.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "Qty:  ${if (line.quantity % 1.0 == 0.0) line.quantity.toInt().toString() else line.quantity.toString()}",
                                                            fontSize = tokens.caption,
                                                            color = close_color
                                                        )
                                                        Text(
                                                            text = "Rate:  ₹${formatIndianNumber(line.rate)}",
                                                            fontSize = tokens.caption,
                                                            color = close_color
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Summary Card
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(0.dp),
                                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(tokens.screenPadding)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "Subtotal", fontSize = tokens.bodySmall, color = title_color)
                                                Text(
                                                    text = "₹${formatIndianNumber(detail.subtotal)}",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = title_color
                                                )
                                            }

                                            Spacer(Modifier.height(tokens.extraPadding))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "Due", fontSize = tokens.bodySmall, color = title_color)
                                                Text(
                                                    text = "₹${formatIndianNumber(detail.balanceDue)}",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = title_color
                                                )
                                            }

                                            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
                                            HorizontalDivider(color = grey_border, thickness = 2.dp)
                                            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Total",
                                                    fontSize = tokens.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = title_color
                                                )
                                                Text(
                                                    text = "₹${formatIndianNumber(detail.grandTotal)}",
                                                    fontSize = tokens.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Section 4: TRANSACTION HISTORY
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(0.dp),
                                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                                ) {
                                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (detail.amountPaid > 0) "1 shipment recorded" else "0 shipments recorded",
                                                fontSize = tokens.caption,
                                                color = close_color
                                            )
                                        }

                                        Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                                        if (detail.amountPaid > 0) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = if (detail.id.isNotBlank()) "TR-${detail.id.takeLast(6).uppercase()}" else "-",
                                                        fontSize = tokens.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Primary
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                                            .background(if (detail.balanceDue == 0.0) greenBg else activity_purple_bg)
                                                            .border(
                                                                width = 1.dp,
                                                                color = if (detail.balanceDue == 0.0) darkGreenBg.copy(alpha = 0.3f) else Primary.copy(alpha = 0.3f),
                                                                shape = RoundedCornerShape(tokens.cardCornerRadius * 2f)
                                                            )
                                                            .padding(horizontal = 10.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = if (detail.balanceDue == 0.0) "Success" else "Pending",
                                                            fontSize = tokens.label,
                                                            fontWeight = FontWeight.Medium,
                                                            color = if (detail.balanceDue == 0.0) darkGreenBg else Primary
                                                        )
                                                    }
                                                }

                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = formatBillDate(detail.updatedAt ?: detail.billDate),
                                                    fontSize = tokens.caption,
                                                    color = close_color
                                                )

                                                Spacer(Modifier.height(tokens.extraPadding))
                                                HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 0.8.dp)
                                                Spacer(Modifier.height(tokens.extraPadding))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Mode:  ${detail.paymentTermId?.name?.ifBlank { null } ?: "Cash"}",
                                                        fontSize = tokens.bodySmall,
                                                        color = close_color
                                                    )
                                                    Text(
                                                        text = "₹${formatIndianNumber(detail.amountPaid)}",
                                                        fontSize = tokens.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = title_color
                                                    )
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = tokens.extraPadding * 1.5f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "No payments recorded yet",
                                                    fontSize = tokens.caption,
                                                    color = close_color
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Top Alerts
            DynamicIslandSuccess(
                message = actionSuccessMessage,
                onDismiss = { viewModel.clearBillActionAlerts() }
            )

            DynamicIslandError(
                message = actionErrorMessage,
                onDismiss = { viewModel.clearBillActionAlerts() }
            )
        }
    }

    // Void Bill Dialog
    if (showVoidDialog && billDetail != null) {
        VoidBillDialog(
            tokens = tokens,
            isLoading = isActionInProgress,
            onDismiss = { showVoidDialog = false },
            onConfirmVoid = { reason ->
                viewModel.voidBill(
                    id = billDetail!!.id,
                    reason = reason,
                    onSuccess = {
                        showVoidDialog = false
                    }
                )
            }
        )
    }
}

/**
 * Dialog matching the Void Bill prompt.
 */
@Composable
fun VoidBillDialog(
    tokens: AppDesignTokens,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmVoid: (reason: String) -> Unit
) {
    var reasonText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(340.dp)
                .padding(tokens.screenPadding),
            shape = RoundedCornerShape(12.dp),
            color = whiteBg,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Void Bill",
                    fontSize = tokens.h2,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Reason for voiding",
                    fontSize = tokens.bodySmall,
                    color = close_color
                )

                Spacer(Modifier.height(6.dp))

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = {
                        reasonText = it
                        if (it.isNotBlank()) isError = false
                    },
                    placeholder = {
                        Text(
                            text = "Enter reason for voiding...",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    },
                    isError = isError,
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = if (isError) redText else sectionBorder,
                        focusedTextColor = title_color,
                        unfocusedTextColor = title_color
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Please enter a reason to void this bill",
                        fontSize = tokens.label,
                        color = redText
                    )
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        border = outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(sectionBorder)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodySmall,
                            color = title_color
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (reasonText.trim().isNotBlank()) {
                                onConfirmVoid(reasonText.trim())
                            } else {
                                isError = true
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "Void Bill",
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

/**
 * Pixel-perfect rendered Bill receipt matching Screenshots 1, 2, and 4.
 */
@Composable
fun BillPreviewReceiptCard(
    detail: ProcurementBillDetailData,
    tokens: AppDesignTokens,
    modifier: Modifier = Modifier
) {
    val (statusBg, statusTextColor) = resolveBillStatusColors(detail.status)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = androidx.compose.foundation.BorderStroke(0.75.dp, sectionBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding * 1.2f, vertical = tokens.screenPadding * 1.4f)
        ) {
            // Receipt Header: Bill title on left, Status & Bill number on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Bill",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                            .background(statusBg)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = detail.status.ifBlank { "-" }.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Bill number", fontSize = tokens.label, color = close_color)
                    Text(
                        text = detail.billNumber.ifBlank { "-" },
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.4f))

            // Bill to & Ship to Addresses
            val billTo = detail.supplierSnapshot
            val vendorName = billTo?.name?.ifBlank { null }
                ?: detail.supplierId?.name?.ifBlank { null }
                ?: "-"
            val billingAddr = billTo?.billingAddress
            val billAddrList = listOfNotNull(
                billingAddr?.flatNo?.ifBlank { null },
                billingAddr?.street?.ifBlank { null },
                billingAddr?.city?.ifBlank { null },
                billingAddr?.state?.ifBlank { null },
                billingAddr?.pincode?.ifBlank { null }
            )
            val cityStatePin = if (billAddrList.isNotEmpty()) billAddrList.joinToString(", ") else "-"
            val country = billingAddr?.country?.ifBlank { null }
            val phone = billTo?.phone?.ifBlank { null }

            Text("Bill to", fontSize = tokens.caption, color = close_color)
            Spacer(Modifier.height(3.dp))
            Text(vendorName, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
            Text(cityStatePin, fontSize = tokens.caption, color = title_color)
            if (!country.isNullOrBlank()) {
                Text(country, fontSize = tokens.caption, color = title_color)
            }
            Text(if (!phone.isNullOrBlank()) "Phone: $phone" else "Phone: -", fontSize = tokens.caption, color = title_color)

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            val warehouseName = detail.warehouseId?.name?.ifBlank { null }
                ?: detail.companySnapshot?.name?.ifBlank { null }
                ?: "-"
            val shipAddr = detail.supplierSnapshot?.shippingAddress
            val shipAddrList = listOfNotNull(
                shipAddr?.flatNo?.ifBlank { null },
                shipAddr?.street?.ifBlank { null },
                shipAddr?.city?.ifBlank { null },
                shipAddr?.state?.ifBlank { null },
                shipAddr?.pincode?.ifBlank { null }
            )
            val shipCityStatePin = if (shipAddrList.isNotEmpty()) {
                shipAddrList.joinToString(", ")
            } else {
                detail.companySnapshot?.address?.ifBlank { null } ?: "-"
            }
            val shipCountry = shipAddr?.country?.ifBlank { null }

            Text("Ship to", fontSize = tokens.caption, color = close_color)
            Spacer(Modifier.height(3.dp))
            Text(warehouseName, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
            Text(shipCityStatePin, fontSize = tokens.caption, color = title_color)
            if (!shipCountry.isNullOrBlank()) {
                Text(shipCountry, fontSize = tokens.caption, color = title_color)
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.4f))

            // Dates & Company Contacts Grid
            val companyEmail = detail.companySnapshot?.email?.ifBlank { null } ?: "-"
            val companyPhone = detail.companySnapshot?.phone?.ifBlank { null } ?: "-"

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bill date", fontSize = tokens.caption, color = close_color)
                    Spacer(Modifier.height(6.dp))
                    Text("Due date", fontSize = tokens.caption, color = close_color)
                }
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(formatBillDate(detail.billDate), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                    Spacer(Modifier.height(6.dp))
                    Text(formatBillDate(detail.dueDate), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Email", fontSize = tokens.caption, color = close_color)
                    Spacer(Modifier.height(6.dp))
                    Text("Phone", fontSize = tokens.caption, color = close_color)
                }
                Column(modifier = Modifier.weight(1.3f), horizontalAlignment = Alignment.End) {
                    Text(companyEmail, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                    Spacer(Modifier.height(6.dp))
                    Text(companyPhone, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.4f))
            HorizontalDivider(color = grey_border.copy(alpha = 0.8f), thickness = 0.8.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.4f))

            // Items Table Title
            Text("Items", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = title_color)
            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // Items Table Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(badgeGrey, RoundedCornerShape(2.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Item / Description", fontSize = tokens.label, color = close_color, modifier = Modifier.weight(2f))
                Text("Unit Price", fontSize = tokens.label, color = close_color, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Qty", fontSize = tokens.label, color = close_color, modifier = Modifier.weight(0.6f), textAlign = TextAlign.End)
                Text("Tax %", fontSize = tokens.label, color = close_color, modifier = Modifier.weight(0.6f), textAlign = TextAlign.End)
                Text("Total", fontSize = tokens.label, color = close_color, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
            }

            // Items Rows
            if (detail.lines.isEmpty()) {
                Text(
                    text = "-",
                    fontSize = tokens.caption,
                    color = close_color,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                detail.lines.forEach { line ->
                    ReceiptLineItemRow(line = line, tokens = tokens)
                    HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 0.75.dp)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.4f))

            // Subtotal, Tax, Due Breakdown
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", fontSize = tokens.caption, color = close_color)
                    Text(String.format(Locale.US, "₹%.2f", detail.subtotal), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tax", fontSize = tokens.caption, color = close_color)
                    Text(String.format(Locale.US, "₹%.2f", detail.totalTax), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                }
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Due", fontSize = tokens.caption, color = close_color)
                    Text(String.format(Locale.US, "₹%.2f", detail.balanceDue), fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.6f))

            // Grand Total
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text("Grand total", fontSize = tokens.caption, color = close_color)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = String.format(Locale.US, "₹%.2f", detail.grandTotal),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = title_color
                )
            }
        }
    }
}

@Composable
private fun ReceiptLineItemRow(
    line: ProcurementBillDetailLine,
    tokens: AppDesignTokens
) {
    val itemName = line.item?.name?.ifBlank { null } ?: line.itemDescription?.ifBlank { null } ?: "-"
    val sku = line.item?.sku

    val taxPercentageText = if (line.taxBreakdown.isNotEmpty()) {
        val sumRate = line.taxBreakdown.sumOf { it.rate }
        if (sumRate % 1.0 == 0.0) "${sumRate.toInt()}%" else "$sumRate%"
    } else if (line.taxableAmount > 0 && line.totalTax > 0) {
        val calcRate = (line.totalTax / line.taxableAmount) * 100
        if (calcRate % 1.0 == 0.0) "${calcRate.toInt()}%" else String.format(Locale.US, "%.1f%%", calcRate)
    } else {
        "-"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(2f)) {
            Text(
                text = itemName,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
            if (!sku.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "SKU: $sku",
                    fontSize = tokens.label,
                    color = close_color
                )
            }
        }

        Text(
            text = String.format(Locale.US, "₹%.2f", line.rate),
            fontSize = tokens.caption,
            color = title_color,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )

        Text(
            text = if (line.quantity % 1.0 == 0.0) line.quantity.toInt().toString() else line.quantity.toString(),
            fontSize = tokens.caption,
            color = title_color,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )

        Text(
            text = taxPercentageText,
            fontSize = tokens.caption,
            color = title_color,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )

        Text(
            text = String.format(Locale.US, "₹%.2f", line.lineTotal),
            fontSize = tokens.caption,
            fontWeight = FontWeight.Bold,
            color = title_color,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.End
        )
    }
}

private fun formatBillDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val parsed = inputFormat.parse(isoDate)
        parsed?.let { outputFormat.format(it) } ?: isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun resolveBillStatusColors(status: String): Pair<Color, Color> {
    return when (status.lowercase()) {
        "paid", "completed", "approved" -> greenBg to darkGreenBg
        "sent", "pending", "open" -> activity_purple_bg to Primary
        "void", "cancelled", "rejected" -> redBg to redText
        else -> light_grey to TextSecondary
    }
}