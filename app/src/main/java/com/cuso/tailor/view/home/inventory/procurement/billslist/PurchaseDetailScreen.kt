@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.procurement.billslist

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import com.cuso.tailor.view.home.pdfgenerator.ProcurementBillPdfGenerator
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun PurchaseDetailScreen(
    billId: String,
    onClose: () -> Unit,
    onDownloadPdf: () -> Unit = {},
    onRecordPayment: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current
    val billDetail by viewModel.selectedBillDetail.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingBillDetail.collectAsStateWithLifecycle()
    val error by viewModel.billDetailError.collectAsStateWithLifecycle()

    val isActionInProgress by viewModel.isBillActionInProgress.collectAsStateWithLifecycle()
    val actionSuccessMessage by viewModel.billActionSuccessMessage.collectAsStateWithLifecycle()
    val actionErrorMessage by viewModel.billActionErrorMessage.collectAsStateWithLifecycle()

    var isPreviewMode by remember { mutableStateOf(false) }
    var showVoidDialog by remember { mutableStateOf(false) }

    val pdfGenerator = remember(context) { ProcurementBillPdfGenerator(context) }

    val handleDownloadClick: () -> Unit = {
        val currentDetail = billDetail
        if (currentDetail != null) {
            pdfGenerator.downloadBillPdf(currentDetail)
        } else {
            onDownloadPdf()
        }
    }

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
                TitleBar(
                    title = if (isPreviewMode) "Bill Preview" else "Purchase Detail",
                    onClose = {
                        if (isPreviewMode) {
                            isPreviewMode = false
                        } else {
                            onClose()
                        }
                    }
                )
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
                        message = error ?: "-",
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
                        // Section 1: Header Card
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
                                            fontWeight = FontWeight.Medium,
                                            color = title_color
                                        )
                                        Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                                .background(statusBg)
                                                .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 2.dp)
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

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(
                                           tokens.extraPadding * 0.6f
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // In-place Toggle between Preview PDF and Close Preview
                                        if (isPreviewMode) {
                                            Button(
                                                onClick = { isPreviewMode = false },
                                                colors = ButtonDefaults.buttonColors(containerColor = background_light_purple),
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
                                                onClick = { isPreviewMode = true },
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

                                        // Header Download Action Button
                                        Box(
                                            modifier = Modifier
                                                .size(tokens.buttonHeight * 0.85f)
                                                .border(
                                                    width = 1.dp,
                                                    color = sectionBorder,
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f)
                                                )
                                                .clickable { handleDownloadClick() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Download,
                                                contentDescription = "Download",
                                                tint = title_color,
                                                modifier = Modifier.size(tokens.iconSize * 0.9f)
                                            )
                                        }

                                        when (statusNormalized) {
                                            "draft" -> {
                                                Button(
                                                    onClick = { viewModel.sendBill(detail.id) },
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
                                                OutlinedButton(
                                                    onClick = { },
                                                    enabled = false,
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                    border = outlinedButtonBorder(enabled = false).copy(
                                                        brush = androidx.compose.ui.graphics.SolidColor(sectionBorder)
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                                                ) {
                                                    Text(
                                                        text = "Void",
                                                        fontSize = tokens.caption,
                                                        color = close_color
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

                        // Section 2: Conditional Preview vs Detailed Sections
                        if (isPreviewMode) {
                            item {
                                Box(modifier = Modifier.padding(horizontal = tokens.screenPadding)) {
                                    BillPreviewReceiptCard(
                                        detail = detail,
                                        tokens = tokens,
                                        onDownloadPdf = handleDownloadClick
                                    )
                                }
                            }
                        } else {
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
                                            fontWeight = FontWeight.Medium,
                                            color = title_color
                                        )
                                        Spacer(Modifier.height(tokens.extraPadding))

                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.weight(1.3f)) {
                                                Text("Vendor", fontSize = tokens.bodySmall, color = close_color)
                                                Spacer(Modifier.height(2.dp))
                                                val rawVendorName = detail.supplierSnapshot?.name?.ifBlank { null }
                                                    ?: detail.supplierId?.name?.ifBlank { null }
                                                    ?: "-"

                                                val displayVendorName = if (!tokens.isTablet && rawVendorName.length > 24) {
                                                    "${rawVendorName.take(24)}..."
                                                } else {
                                                    rawVendorName
                                                }

                                                Text(
                                                    text = displayVendorName,
                                                    fontSize = tokens.bodySmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontWeight = FontWeight.Medium,
                                                    color = title_color
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Bill Date", fontSize = tokens.bodySmall, color = close_color)
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
                                                Text("Due Date", fontSize = tokens.bodySmall, color = close_color)
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = formatBillDate(detail.dueDate),
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = title_color
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Balance Due", fontSize = tokens.bodySmall, color = close_color)
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
                                                            fontWeight = FontWeight.Medium,
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
                                                            text = "Account: ${line.expenseAccount?.accountName?.ifBlank { null } ?: line.lineType?.ifBlank { null } ?: "-"}",
                                                            fontSize = tokens.caption,
                                                            color = close_color
                                                        )
                                                        Text(
                                                            text = "₹${formatIndianNumber(line.lineTotal)}",
                                                            fontSize = tokens.bodyMedium,
                                                            fontWeight = FontWeight.Medium,
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
                                                    fontWeight = FontWeight.Normal,
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
                                                    fontWeight = FontWeight.Normal,
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
                                                text = "Transaction History",
                                                fontSize = tokens.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = title_color
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
                                                        fontWeight = FontWeight.Medium,
                                                        color = Primary
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                                            .background(if (detail.balanceDue == 0.0) greenBg else background_light_purple)
                                                            .border(
                                                                width = 1.dp,
                                                                color = if (detail.balanceDue == 0.0) darkGreenBg.copy(alpha = 0.3f) else Primary.copy(alpha = 0.3f),
                                                                shape = RoundedCornerShape(tokens.cardCornerRadius * 2f)
                                                            )
                                                            .padding(horizontal = tokens.extraPadding, vertical = 2.dp)
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
                                                        text = "Mode:  ${detail.paymentTermId?.name?.ifBlank { null } ?: "-"}",
                                                        fontSize = tokens.bodySmall,
                                                        color = close_color
                                                    )
                                                    Text(
                                                        text = "₹${formatIndianNumber(detail.amountPaid)}",
                                                        fontSize = tokens.bodyMedium,
                                                        fontWeight = FontWeight.Medium,
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
            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
            color = whiteBg,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding * 1.25f)
            ) {
                Text(
                    text = "Void Bill",
                    fontSize = tokens.h2,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                Spacer(Modifier.height(tokens.extraPadding * 1.6f))

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
                            color = close_color
                        )
                    },
                    isError = isError,
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
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

                Spacer(Modifier.height(tokens.extraPadding * 2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
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

                    Spacer(Modifier.width(tokens.extraPadding * 1.2f))

                    Button(
                        onClick = {
                            if (reasonText.trim().isNotBlank()) {
                                onConfirmVoid(reasonText.trim())
                            } else {
                                isError = true
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = redText),
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
 * Pixel-perfect Billing Receipt Card using design palette colors and adaptive design tokens.
 */
@Composable
fun BillPreviewReceiptCard(
    detail: ProcurementBillDetailData,
    tokens: AppDesignTokens,
    modifier: Modifier = Modifier,
    onDownloadPdf: () -> Unit = {}
) {
    val (statusBg, statusTextColor) = resolveBillStatusColors(detail.status)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, sectionBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding * 1.25f)
        ) {
            val companyName = detail.companySnapshot?.name?.ifBlank { null } ?: "-"
            val companyInitial = if (companyName != "-") companyName.firstOrNull()?.uppercase() ?: "-" else "-"
            val orderId = detail.purchaseOrderId?.poNumber?.ifBlank { null }
                ?: detail.supplierBillReference?.ifBlank { null }
                ?: "-"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(tokens.cardHeight * 0.46f)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
                            .background(activity_purple_bg)
                            .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = companyInitial,
                            fontSize = tokens.h2,
                            fontWeight = FontWeight.Medium,
                            color = darkPurple
                        )
                    }

                    Spacer(Modifier.width(tokens.extraPadding * 1.2f))

                    Column {
                        Text(
                            text = companyName,
                            fontSize = tokens.h2,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Billing receipt",
                            fontSize = tokens.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 3f))
                            .background(statusBg)
                            .padding(horizontal = tokens.extraPadding * 1.2f, vertical = 2.dp)
                    ) {
                        Text(
                            text = detail.status.ifBlank { "-" },
                            fontSize = tokens.bodySmall,
                            color = statusTextColor
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Text(
                        text = "INVOICE NO",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = close_color,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = detail.billNumber.ifBlank { "-" },
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "ORDER ID",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = close_color,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = orderId,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 2f))
            HorizontalDivider(color = sectionBorder, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.6f))

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
            val billAddrLine = if (billAddrList.isNotEmpty()) billAddrList.joinToString(", ") else "-"
            val vendorPhone = billTo?.phone?.ifBlank { null } ?: "-"

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
            val shipAddrLine = if (shipAddrList.isNotEmpty()) {
                shipAddrList.joinToString(", ")
            } else {
                detail.companySnapshot?.address?.ifBlank { null } ?: "-"
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BILL TO",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = close_color,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = vendorName,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = blackTitle,
                        lineHeight = tokens.bodyMedium.value.sp * 1.15f
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = billAddrLine,
                        fontSize = tokens.bodySmall,
                        color = TextSecondary,
                        lineHeight = tokens.bodyMedium.value.sp * 1.15f
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Phone: $vendorPhone",
                        fontSize = tokens.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.width(tokens.screenPadding))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SHIP TO",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = close_color,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = warehouseName,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = blackTitle,
                        lineHeight = tokens.bodyMedium.value.sp * 1.15f
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = shipAddrLine,
                        fontSize = tokens.bodySmall,
                        color = TextSecondary,
                        lineHeight = tokens.bodyMedium.value.sp * 1.15f
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.6f))
            HorizontalDivider(color = sectionBorder, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.6f))

            val companyEmail = detail.companySnapshot?.email?.ifBlank { null } ?: "-"
            val companyPhone = detail.companySnapshot?.phone?.ifBlank { null } ?: "-"

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Invoice Date", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = close_color)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatBillDate(detail.billDate),
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                    Text("Due date", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = close_color)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatBillDate(detail.dueDate),
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                }

                Spacer(Modifier.width(tokens.screenPadding))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Phone", fontSize = tokens.bodySmall, color = close_color)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = companyPhone,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                    Text("Email", fontSize = tokens.bodySmall, color = close_color)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = companyEmail,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary,
                        lineHeight = tokens.bodyMedium.value.sp * 1.15f
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 2f))

            Text(
                text = "ITEMS",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = close_color,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(tokens.extraPadding))

            if (detail.lines.isEmpty()) {
                Text(
                    text = "-",
                    fontSize = tokens.bodySmall,
                    color = close_color,
                    modifier = Modifier.padding(vertical = tokens.extraPadding * 1.2f)
                )
            } else {
                detail.lines.forEach { line ->
                    ReceiptItemCard(line = line, tokens = tokens)
                    Spacer(Modifier.height(tokens.extraPadding))
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = sectionBorder, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.6f))

            ReceiptSummaryRow(
                label = "Subtotal",
                value = "₹${formatIndianNumber(detail.subtotal)}",
                textColor = TextPrimary,
                tokens = tokens
            )

            if (detail.totalDiscount > 0.0) {
                Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                ReceiptSummaryRow(
                    label = "Discount",
                    value = "-₹${formatIndianNumber(detail.totalDiscount)}",
                    textColor = greentext,
                    tokens = tokens
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))
            ReceiptSummaryRow(
                label = "Tax",
                value = "₹${formatIndianNumber(detail.totalTax)}",
                textColor = TextPrimary,
                tokens = tokens
            )

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
            HorizontalDivider(color = sectionBorder, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GRAND TOTAL",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "₹${formatIndianNumber(detail.grandTotal)}",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = blackTitle
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            val paymentMethod = detail.paymentTermId?.name?.ifBlank { null } ?: "-"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Payment method: ",
                        fontSize = tokens.bodySmall,
                        color = close_color
                    )
                    Text(
                        text = paymentMethod,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 2f))
            HorizontalDivider(color = sectionBorder, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.6f))

            Text(
                text = "BANK DETAILS",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = close_color,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            ReceiptDetailKeyVal("Bank Name:", "-", tokens = tokens)
            Spacer(Modifier.height(4.dp))
            ReceiptDetailKeyVal("Account No:", "-", tokens = tokens)
            Spacer(Modifier.height(4.dp))
            ReceiptDetailKeyVal("IFSC/SWIFT:", "-", tokens = tokens)

            Spacer(Modifier.height(tokens.extraPadding * 2f))
            HorizontalDivider(color = sectionBorder, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.6f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.cardHeight)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
                        .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
                        .padding(tokens.extraPadding * 0.8f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "UPI / PAYMENT QR",
                            fontSize = (tokens.label.value * 0.7f).sp,
                            fontWeight = FontWeight.Medium,
                            color = close_color,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        SimulatedQrCode(size = tokens.cardHeight * 0.54f, tint = TextPrimary)
                    }
                }

                Spacer(Modifier.width(tokens.screenPadding))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TERMS & CONDITIONS",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = detail.notes?.ifBlank { null } ?: "-",
                        fontSize = tokens.caption,
                        color = TextSecondary,
                        lineHeight = tokens.bodySmall.value.sp * 1.25f
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 2.8f))
            HorizontalDivider(color = sectionBorder, thickness = 1.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.6f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Created with cuso invoice",
                    fontSize = tokens.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = iconMuted
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .height(1.dp)
                            .background(iconMuted)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Authorized Signature",
                        fontSize = tokens.label,
                        color = close_color
                    )
                }
            }
        }
    }
}

/**
 * Line item component for individual items with adaptive styling.
 */
@Composable
private fun ReceiptItemCard(
    line: ProcurementBillDetailLine,
    tokens: AppDesignTokens
) {
    val itemName = line.item?.name?.ifBlank { null } ?: line.itemDescription?.ifBlank { null } ?: "-"
    val sku = line.item?.sku?.ifBlank { null }
    val subtitle = listOfNotNull(sku?.let { "SKU-$it" }, line.itemDescription?.takeIf { it != itemName && it.isNotBlank() }).joinToString(" • ")

    val taxPercentageText = if (line.taxBreakdown.isNotEmpty()) {
        val sumRate = line.taxBreakdown.sumOf { it.rate }
        if (sumRate % 1.0 == 0.0) "${sumRate.toInt()}%" else "$sumRate%"
    } else if (line.taxableAmount > 0.0 && line.totalTax > 0.0) {
        val calcRate = (line.totalTax / line.taxableAmount) * 100.0
        if (calcRate % 1.0 == 0.0) "${calcRate.toInt()}%" else String.format(Locale.US, "%.1f%%", calcRate)
    } else {
        "-"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
            .background(badgeGrey)
            .padding(tokens.extraPadding * 1.2f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = itemName,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = blackTitle
                    )
                    if (subtitle.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }
                Text(
                    text = "₹${formatIndianNumber(line.lineTotal)}",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = blackTitle
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))
            HorizontalDivider(color = sectionBorder.copy(alpha = 0.6f), thickness = 0.75.dp)
            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Unit: ", fontSize = tokens.caption, color = close_color)
                    Text("₹${formatIndianNumber(line.rate)}", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = blackTitle)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Qty: ", fontSize = tokens.caption, color = close_color)
                    Text(
                        text = if (line.quantity % 1.0 == 0.0) line.quantity.toInt().toString() else line.quantity.toString(),
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = blackTitle
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tax: ", fontSize = tokens.caption, color = close_color)
                    Text(taxPercentageText, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = blackTitle)
                }
            }
        }
    }
}

@Composable
private fun ReceiptSummaryRow(
    label: String,
    value: String,
    textColor: Color,
    tokens: AppDesignTokens
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = TextSecondary)
        Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Normal, color = blackTitle)
    }
}

@Composable
private fun ReceiptDetailKeyVal(
    label: String,
    value: String,
    tokens: AppDesignTokens
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            color = TextSecondary,
            modifier = Modifier.width(90.dp)
        )
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Normal,
            color = TextPrimary
        )
    }
}

/**
 * Simulated QR barcode for preview rendering without external dependencies.
 */
@Composable
private fun SimulatedQrCode(
    size: Dp,
    tint: Color
) {
    Canvas(modifier = Modifier.size(size)) {
        val cellSize = this.size.width / 7f
        fun drawBlock(x: Int, y: Int, w: Int, h: Int) {
            drawRect(
                color = tint,
                topLeft = Offset(x * cellSize, y * cellSize),
                size = Size(w * cellSize, h * cellSize)
            )
        }

        drawBlock(0, 0, 3, 3)
        drawRect(whiteBg, topLeft = Offset(1 * cellSize, 1 * cellSize), size = Size(cellSize, cellSize))

        drawBlock(4, 0, 3, 3)
        drawRect(whiteBg, topLeft = Offset(5 * cellSize, 1 * cellSize), size = Size(cellSize, cellSize))

        drawBlock(0, 4, 3, 3)
        drawRect(whiteBg, topLeft = Offset(1 * cellSize, 5 * cellSize), size = Size(cellSize, cellSize))

        drawBlock(3, 1, 1, 1)
        drawBlock(1, 3, 1, 1)
        drawBlock(3, 3, 1, 1)
        drawBlock(5, 3, 1, 1)
        drawBlock(3, 5, 1, 1)
        drawBlock(4, 4, 1, 1)
        drawBlock(6, 5, 1, 2)
        drawBlock(4, 6, 2, 1)
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
        "sent", "pending", "open" -> background_light_purple to Primary
        "void", "cancelled", "rejected" -> redBg to redText
        else -> light_grey to TextSecondary
    }
}