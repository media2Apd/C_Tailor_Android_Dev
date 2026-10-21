@file:Suppress("unused", "SpellCheckingInspection","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.finance.purchase

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.PurchaseReturnActionState
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PurchaseReturnDetailScreen(
    returnId: String,
    viewModel: FinanceViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onSettleRefund: (String) -> Unit = {},
    onViewPo: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val detail by viewModel.purchaseReturnDetail.collectAsState()
    val isLoading by viewModel.isLoadingReturnDetail.collectAsState()
    val errorMessage by viewModel.returnDetailError.collectAsState()
    val actionState by viewModel.returnActionState.collectAsState()

    // Dialog state controllers
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var showShipDialog by remember { mutableStateOf(false) }
    var showMarkReceivedDialog by remember { mutableStateOf(false) }
    var rejectionReason by remember { mutableStateOf("") }

    LaunchedEffect(returnId) {
        viewModel.fetchPurchaseReturnDetail(returnId)
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is PurchaseReturnActionState.Success -> {
                Toast.makeText(context, (actionState as PurchaseReturnActionState.Success).message, Toast.LENGTH_SHORT).show()
                viewModel.resetReturnActionState()
            }
            is PurchaseReturnActionState.Error -> {
                Toast.makeText(context, (actionState as PurchaseReturnActionState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetReturnActionState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = "Return Details",
                onClose = onBack
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { innerPadding ->
        if (isLoading) {
            ListSkeleton()
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage ?: "Failed to load details",
                    fontSize = tokens.bodySmall,
                    color = redText
                )
            }
        } else if (detail != null) {
            val data = detail!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // ─────────────────────────────────────────────────────────────
                // 1. TOP HEADER (RMA, STATUS, PO, DYNAMIC ACTION BUTTONS)
                // ─────────────────────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg
                ) {
                    Column(modifier = Modifier.padding(tokens.extraPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = data.rmaNumber.ifBlank { "—" },
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                                    StatusBadge(
                                        text = data.status.ifBlank { "Draft" },
                                        bgColor = when (data.status.lowercase()) {
                                            "approved" -> greenBg
                                            "rejected" -> redBg
                                            else -> light_grey
                                        },
                                        textColor = when (data.status.lowercase()) {
                                            "approved" -> darkGreenBg
                                            "rejected" -> redText
                                            else -> TextSecondary
                                        }
                                    )
                                }
                                Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                                Text(
                                    text = "Raised on ${formatDate(data.createdAt)} | ${data.po?.poNumber ?: "—"}",
                                    fontSize = tokens.caption,
                                    color = iconMuted
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            // ── DYNAMIC BUTTONS ──
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (data.status.lowercase()) {
                                    "draft" -> {
                                        OutlinedButton(
                                            onClick = {
                                                rejectionReason = ""
                                                showRejectDialog = true
                                            },
                                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                            border = BorderStroke(1.dp, redText),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            enabled = actionState !is PurchaseReturnActionState.Loading
                                        ) {
                                            Text("Reject", fontSize = tokens.bodySmall, color = redText, fontWeight = FontWeight.Medium)
                                        }

                                        Button(
                                            onClick = { showApproveDialog = true },
                                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                            colors = ButtonDefaults.buttonColors(containerColor = darkGreenBg),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            enabled = actionState !is PurchaseReturnActionState.Loading
                                        ) {
                                            Text("Approve", fontSize = tokens.bodySmall, color = whiteBg, fontWeight = FontWeight.Medium)
                                        }
                                    }

                                    "approved" -> {
                                        when (data.receiveStatus.lowercase()) {
                                            "not sent" -> {
                                                Button(
                                                    onClick = { showShipDialog = true },
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    enabled = actionState !is PurchaseReturnActionState.Loading
                                                ) {
                                                    Text("Ship Goods", fontSize = tokens.bodySmall, color = whiteBg, fontWeight = FontWeight.Medium)
                                                }
                                            }

                                            "in transit" -> {
                                                Button(
                                                    onClick = { showMarkReceivedDialog = true },
                                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                    enabled = actionState !is PurchaseReturnActionState.Loading
                                                ) {
                                                    Text("Mark Received", fontSize = tokens.bodySmall, color = whiteBg, fontWeight = FontWeight.Medium)
                                                }
                                            }

                                            "received" -> {
                                                if (data.refundStatus.equals("Not Refunded", ignoreCase = true)) {
                                                    OutlinedButton(
                                                        onClick = { onSettleRefund(data.id) },
                                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                        border = BorderStroke(1.dp, sectionBorder),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("Settle Refund", fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 2. RETURN VALUE SUMMARY
                // ─────────────────────────────────────────────────────────────
                val totalQty = data.items.sumOf { it.qty }
                SectionCard(title = "Return Value Summary") {
                    SummaryRow(label = "Total Return Qty", value = formatDouble(totalQty))
                    SummaryRow(label = "Item Return Subtotal", value = "₹${formatDouble(data.subtotal)}")
                    SummaryRow(label = "Tax", value = "₹${formatDouble(data.taxTotal)}")
                    HorizontalDivider(color = sectionBorder, modifier = Modifier.padding(vertical = tokens.extraPadding * 0.6f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Return",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "₹${formatDouble(data.grandTotal)}",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 3. RETURN PROGRESS (DYNAMIC STEPS)
                // ─────────────────────────────────────────────────────────────
                val isApproved = data.approvedAt != null || data.status.equals("Approved", ignoreCase = true)
                val isShipped = data.shippedAt != null || data.receiveStatus.equals("In Transit", ignoreCase = true) || data.receiveStatus.equals("Received", ignoreCase = true)
                val isSupplierReceived = data.supplierReceivedAt != null || data.receiveStatus.equals("Received", ignoreCase = true)
                val isRefundSettled = data.refundStatus.equals("Refunded", ignoreCase = true)

                val completedCount = listOf(isApproved, isShipped, isSupplierReceived, isRefundSettled).count { it }

                SectionCard(
                    title = "Return Progress",
                    headerBadge = "$completedCount/4"
                ) {
                    ProgressStepItem(title = "Return approved", isCompleted = isApproved)
                    ProgressStepItem(title = "Goods shipped", isCompleted = isShipped)
                    ProgressStepItem(title = "Received by supplier", isCompleted = isSupplierReceived)
                    ProgressStepItem(title = "Refund settled", isCompleted = isRefundSettled)
                }

                // ─────────────────────────────────────────────────────────────
                // 4. SUPPLIER CONTACT
                // ─────────────────────────────────────────────────────────────
                SectionCard(title = "Supplier Contact") {
                    Text(
                        text = data.supplier?.name?.ifBlank { "—" } ?: "—",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = data.supplier?.supplierCode?.ifBlank { "—" } ?: "—",
                        fontSize = tokens.label,
                        color = iconMuted
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.6f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = iconMuted,
                            modifier = Modifier.size(tokens.iconSize * 0.8f)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                        Text(
                            text = data.supplier?.contact?.phone?.ifBlank { "—" } ?: "—",
                            fontSize = tokens.caption,
                            color = TextPrimary
                        )
                    }
                    Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = iconMuted,
                            modifier = Modifier.size(tokens.iconSize * 0.8f)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                        Text(
                            text = data.supplier?.contact?.email?.ifBlank { "—" } ?: "—",
                            fontSize = tokens.caption,
                            color = TextPrimary
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 5. RETURN REQUEST INFORMATION
                // ─────────────────────────────────────────────────────────────
                SectionCard(
                    title = "Return Request Information",
                    headerBadge = "PROCUREMENT\nREFERENCE"
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoColumn(label = "RMA NUMBER", value = data.rmaNumber.ifBlank { "—" }, modifier = Modifier.weight(1f))
                        InfoColumn(
                            label = "SUPPLIER",
                            value = "${data.supplier?.name.orEmpty()}\n${data.supplier?.supplierCode.orEmpty()}".trim().ifBlank { "—" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "ORIGINAL PURCHASE ORDER", fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { data.po?.poNumber?.let(onViewPo) }
                            ) {
                                Text(
                                    text = data.po?.poNumber?.ifBlank { "—" } ?: "—",
                                    fontSize = tokens.caption,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.3f))
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize * 0.7f)
                                )
                            }
                        }
                        InfoColumn(label = "GOODS RECEIPT NOTE (GRN)", value = data.receive?.receiveNumber?.ifBlank { "—" } ?: "—", modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoColumn(label = "RETURN DATE", value = formatDate(data.returnDate), modifier = Modifier.weight(1f))
                        InfoColumn(label = "GRN DATE", value = formatDate(data.receive?.receiveDate), modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoColumn(label = "PO VALUE", value = "₹${formatDouble(data.po?.grandTotal ?: 0.0)}", modifier = Modifier.weight(1f))
                        InfoColumn(label = "SHIPPED ON", value = formatDate(data.shippedAt), modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "APPROVAL STATUS", fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                            StatusBadge(
                                text = data.status.ifBlank { "Draft" },
                                bgColor = if (data.status.equals("Approved", ignoreCase = true)) greenBg else light_grey,
                                textColor = if (data.status.equals("Approved", ignoreCase = true)) darkGreenBg else TextSecondary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "RECEIVE STATUS", fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                            StatusBadge(
                                text = data.receiveStatus.ifBlank { "Not Sent" },
                                bgColor = when (data.receiveStatus.lowercase()) {
                                    "received" -> greenBg
                                    "in transit" -> yellowBg
                                    else -> light_grey
                                },
                                textColor = when (data.receiveStatus.lowercase()) {
                                    "received" -> darkGreenBg
                                    "in transit" -> yellowText
                                    else -> TextSecondary
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "REFUND STATUS", fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                            StatusBadge(
                                text = data.refundStatus.ifBlank { "Not Refunded" },
                                bgColor = if (data.refundStatus.equals("Refunded", ignoreCase = true)) greenBg else light_grey,
                                textColor = if (data.refundStatus.equals("Refunded", ignoreCase = true)) darkGreenBg else TextSecondary
                            )
                        }
                        InfoColumn(
                            label = "GOODS RETURNED",
                            value = if (data.returned) "Yes — stock deducted" else "No",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 6. RETURN REASON
                // ─────────────────────────────────────────────────────────────
                SectionCard(title = "Return Reason") {
                    Text(text = "REASON FOR RETURN", fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        color = Primary_background
                    ) {
                        Text(
                            text = data.reason.ifBlank { "—" },
                            fontSize = tokens.bodySmall,
                            color = TextPrimary,
                            modifier = Modifier.padding(tokens.extraPadding)
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))
                    Text(text = "REMARKS", fontSize = tokens.label, color = iconMuted, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        color = Primary_background
                    ) {
                        Text(
                            text = data.remarks.ifBlank { "—" },
                            fontSize = tokens.bodySmall,
                            color = TextPrimary,
                            modifier = Modifier.padding(tokens.extraPadding)
                        )
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 7. SUPPORTING EVIDENCE
                // ─────────────────────────────────────────────────────────────
                SectionCard(
                    title = "Supporting Evidence",
                    headerBadge = "${data.attachments.size} file(s)"
                ) {
                    if (data.attachments.isEmpty()) {
                        Text(text = "No attachments provided", fontSize = tokens.caption, color = iconMuted)
                    } else {
                        data.attachments.forEach { attachment ->
                            val fileName = attachment.fileUrl.substringAfterLast("/").ifBlank { "attachment.png" }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = tokens.extraPadding * 0.5f),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                border = BorderStroke(1.dp, sectionBorder),
                                color = whiteBg
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(tokens.extraPadding),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = iconMuted,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                    Spacer(Modifier.width(tokens.extraPadding))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fileName,
                                            fontSize = tokens.bodySmall,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "View",
                                            fontSize = tokens.caption,
                                            color = Primary,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.clickable {
                                                // View attachment
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ─────────────────────────────────────────────────────────────
                // 8. ITEMS FOR RETURN (Balanced Tabular Grid & 2-Col Serials)
                // ─────────────────────────────────────────────────────────────
                SectionCard(
                    title = "Items for Return",
                    headerBadge = "${data.items.size} line item(s)"
                ) {
                    data.items.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.item?.name?.ifBlank { "Item #${idx + 1}" } ?: "Item #${idx + 1}",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = item.item?.sku?.ifBlank { "—" } ?: "—",
                                    fontSize = tokens.caption,
                                    color = iconMuted
                                )
                                Text(
                                    text = "${item.warehouse?.name.orEmpty()} / ${item.bin?.name.orEmpty()}".trim(' ', '/').ifBlank { "—" },
                                    fontSize = tokens.caption,
                                    color = iconMuted
                                )
                            }
                            Text(
                                text = "₹${formatDouble(item.total)}",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoColumn(label = "RETURN QTY", value = formatDouble(item.qty), modifier = Modifier.weight(1f))
                            InfoColumn(label = "UNIT COST", value = "₹${formatDouble(item.rate)}", modifier = Modifier.weight(1f))
                            InfoColumn(label = "TAX", value = "${formatDouble(item.taxPercent)}%", modifier = Modifier.weight(1f))
                            InfoColumn(label = "REASON", value = item.reason.ifBlank { "—" }, modifier = Modifier.weight(1.2f))
                        }

                        if (item.serialNumbers.isNotEmpty()) {
                            Spacer(Modifier.height(tokens.extraPadding))
                            Text(
                                text = "SERIAL / BATCH NUMBERS",
                                fontSize = tokens.label,
                                color = iconMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.5f))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                item.serialNumbers.chunked(2).forEach { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        pair.forEach { serial ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF8FAFC),
                                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 7.dp, horizontal = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = serial,
                                                        fontSize = tokens.label,
                                                        color = TextPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        if (idx != data.items.lastIndex) {
                            HorizontalDivider(
                                color = sectionBorder,
                                modifier = Modifier.padding(vertical = tokens.extraPadding)
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.6f))
                    Text(
                        text = "Stock for these lines was deducted from the original warehouse/bin when the goods were shipped.",
                        fontSize = tokens.caption,
                        color = iconMuted
                    )
                }

                // ─────────────────────────────────────────────────────────────
                // 9. RETURN ACTIVITY TIMELINE
                // ─────────────────────────────────────────────────────────────
                SectionCard(title = "Return Activity Timeline") {
                    TimelineItem(
                        title = "Return Request Created",
                        timestamp = formatDateTime(data.createdAt),
                        isCompleted = data.createdAt.isNotBlank(),
                        isLast = false
                    )
                    TimelineItem(
                        title = "Return Approved",
                        timestamp = formatDateTime(data.approvedAt),
                        isCompleted = data.approvedAt != null,
                        isLast = false
                    )
                    TimelineItem(
                        title = "Goods Shipped — Stock Deducted",
                        timestamp = formatDateTime(data.shippedAt),
                        isCompleted = data.shippedAt != null,
                        isLast = false
                    )
                    TimelineItem(
                        title = "Supplier Confirmed Receipt",
                        timestamp = formatDateTime(data.supplierReceivedAt),
                        isCompleted = data.supplierReceivedAt != null,
                        isLast = false
                    )
                    TimelineItem(
                        title = if (isRefundSettled) "Refund / Credit Settled" else "Pending: Refund / Credit Settlement",
                        timestamp = null,
                        isCompleted = isRefundSettled,
                        isLast = true
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CONFIRMATION DIALOGS
    // ─────────────────────────────────────────────────────────────

    // 1. APPROVE DIALOG
    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Approve Purchase Return", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium) },
            text = { Text("Are you sure you want to approve this return request?", fontSize = tokens.caption, color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showApproveDialog = false
                        viewModel.approvePurchaseReturn(returnId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = darkGreenBg)
                ) {
                    Text("Approve", color = whiteBg, fontSize = tokens.bodySmall)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showApproveDialog = false }) {
                    Text("Cancel", fontSize = tokens.bodySmall, color = TextPrimary)
                }
            }
        )
    }

    // 2. REJECT DIALOG (With Reason Input)
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Purchase Return", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium) },
            text = {
                Column {
                    Text("Please provide a reason for rejecting this return request:", fontSize = tokens.caption, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter rejection reason...", fontSize = tokens.caption, color = iconMuted) },
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionReason.isNotBlank()) {
                            showRejectDialog = false
                            viewModel.rejectPurchaseReturn(returnId, rejectionReason)
                        } else {
                            Toast.makeText(context, "Please enter a rejection reason", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = redText)
                ) {
                    Text("Reject", color = whiteBg, fontSize = tokens.bodySmall)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRejectDialog = false }) {
                    Text("Cancel", fontSize = tokens.bodySmall, color = TextPrimary)
                }
            }
        )
    }

    // 3. SHIP GOODS DIALOG
    if (showShipDialog) {
        AlertDialog(
            onDismissRequest = { showShipDialog = false },
            title = { Text("Ship Return Goods", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium) },
            text = { Text("Are you sure the items have been shipped back to the supplier? This will deduct the stock from the warehouse.", fontSize = tokens.caption, color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showShipDialog = false
                        viewModel.shipPurchaseReturnGoods(returnId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Ship Goods", color = whiteBg, fontSize = tokens.bodySmall)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showShipDialog = false }) {
                    Text("Cancel", fontSize = tokens.bodySmall, color = TextPrimary)
                }
            }
        )
    }

    // 4. MARK RECEIVED DIALOG
    if (showMarkReceivedDialog) {
        AlertDialog(
            onDismissRequest = { showMarkReceivedDialog = false },
            title = { Text("Confirm Supplier Receipt", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium) },
            text = { Text("Has the supplier confirmed receipt of the returned items?", fontSize = tokens.caption, color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showMarkReceivedDialog = false
                        viewModel.markPurchaseReturnReceived(returnId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = darkGreenBg)
                ) {
                    Text("Confirm Received", color = whiteBg, fontSize = tokens.bodySmall)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showMarkReceivedDialog = false }) {
                    Text("Cancel", fontSize = tokens.bodySmall, color = TextPrimary)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE HELPER COMPOSABLES
// ─────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    headerBadge: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (headerBadge != null) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = headerBadge,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = iconMuted,
                        textAlign = TextAlign.End
                    )
                }
            }
            HorizontalDivider(
                color = sectionBorder,
                modifier = Modifier.padding(vertical = tokens.extraPadding * 0.7f)
            )
            content()
        }
    }
}

@Composable
private fun InfoColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = tokens.label,
            color = iconMuted,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(tokens.extraPadding * 0.2f))
        Text(
            text = value,
            fontSize = tokens.caption,
            color = TextPrimary,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding * 0.25f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.caption, color = iconMuted)
        Text(text = value, fontSize = tokens.caption, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProgressStepItem(title: String, isCompleted: Boolean) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding * 0.35f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isCompleted) darkGreenBg else iconMuted,
            modifier = Modifier.size(tokens.iconSize * 0.9f)
        )
        Spacer(Modifier.width(tokens.extraPadding * 0.8f))
        Text(
            text = title,
            fontSize = tokens.caption,
            color = if (isCompleted) TextPrimary else iconMuted,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TimelineItem(
    title: String,
    timestamp: String?,
    isCompleted: Boolean,
    isLast: Boolean
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFE2E8F0))
                )
            }

            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) darkGreenBg else Color(0xFFCBD5E1))
            )
        }

        Spacer(Modifier.width(tokens.extraPadding * 0.8f))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else tokens.extraPadding * 1.2f)
        ) {
            Text(
                text = title,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = if (isCompleted) TextPrimary else iconMuted
            )
            if (!timestamp.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = timestamp,
                    fontSize = tokens.label,
                    color = iconMuted
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STRING / DATE HELPERS
// ─────────────────────────────────────────────────────────────

private fun formatDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = parser.parse(isoDate.take(10))
        date?.let { formatter.format(it) } ?: isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

private fun formatDateTime(isoDateTime: String?): String {
    if (isoDateTime.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val date = parser.parse(isoDateTime.take(19))
        date?.let { formatter.format(it) } ?: isoDateTime
    } catch (_: Exception) {
        isoDateTime
    }
}

private fun formatDouble(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%,.0f", value)
    } else {
        String.format(Locale.US, "%,.2f", value)
    }
}