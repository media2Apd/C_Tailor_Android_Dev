package com.cuso.mobile.view.home.inventory.procurement.purchase_order

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.PurchaseOrder
import com.cuso.mobile.model.inventory.ReceivePOItemRequest
import com.cuso.mobile.model.inventory.ReceivePurchaseOrderRequest
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.InventoryViewModel

@Composable
fun PODetailScreen(
    order: PurchaseOrder,
    viewModel: InventoryViewModel,
    onEdit: () -> Unit,
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val billConvertData by viewModel.billConvertDetail.collectAsState()
    val isSubmitting by viewModel.isSubmittingPO.collectAsState()
    val poSuccessMessage by viewModel.poSuccessMessage.collectAsState()
    val poError by viewModel.purchaseOrdersError.collectAsState()

    val requisitionDetail by viewModel.selectedRequisition.collectAsState()
    val isSubmittingReq by viewModel.isSubmittingRequisition.collectAsState()
    val reqSuccessMessage by viewModel.requisitionSuccessMessage.collectAsState()

    var showReceiveDialog by remember { mutableStateOf(false) }
    var showBillConvertDialog by remember { mutableStateOf(false) }
    var showApproveDialog by remember { mutableStateOf(false) }

    // Fetch live Requisition details if ID is present
    LaunchedEffect(order.id) {
        order.id?.let { viewModel.fetchRequisitionById(it) }
    }

    LaunchedEffect(poSuccessMessage) {
        poSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearPOAlerts()
        }
    }

    LaunchedEffect(reqSuccessMessage) {
        reqSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearRequisitionAlerts()
        }
    }

    LaunchedEffect(poError) {
        poError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearPOAlerts()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        TitleBar(
            title = "Purchase Detail",
            onClose = onClose
        )

        Column(
            modifier = Modifier.padding(horizontal = tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // Header Row: PO Number + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = requisitionDetail?.prNumber ?: order.poNumber ?: "PR-00021",
                            fontSize = tokens.h2,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusPill(status = requisitionDetail?.approvalStatus ?: order.orderStatus.ifBlank { "OPEN" })
                    }
                    Text(
                        text = "Purchase Orders / ${requisitionDetail?.prNumber ?: order.poNumber ?: "PR-00021"}",
                        fontSize = tokens.caption,
                        color = mutedText,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = iconMuted, modifier = Modifier.size(tokens.iconSize))
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(tokens.iconSize * 0.8f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { showApproveDialog = true },
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                    modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(tokens.iconSize * 0.8f))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve", fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        order.id?.let {
                            viewModel.fetchPOForBillConvert(it)
                            showBillConvertDialog = true
                        }
                    },
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    modifier = Modifier.weight(1.3f).height(tokens.buttonHeight)
                ) {
                    Text("Convert to Bill", fontSize = tokens.bodySmall, color = whiteBg, fontWeight = FontWeight.Bold)
                }
            }

            // 2x2 Metric Cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailMetricBox(icon = Icons.Default.DateRange, title = "Order Date", value = "Oct 24, 2023", modifier = Modifier.weight(1f))
                    DetailMetricBox(icon = Icons.Default.LocalShipping, title = "Exp. Delivery", value = "Oct 30, 2023", modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailMetricBox(icon = Icons.Default.Payment, title = "Advance Paid", value = "₹2,000.00", modifier = Modifier.weight(1f))
                    DetailMetricBox(icon = Icons.Default.AccountBalanceWallet, title = "Avail. Advance", value = "₹500.00", modifier = Modifier.weight(1f))
                }
            }

            // Vendor Details Card
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(tokens.cardPadding * 0.8f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Store, null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Vendor Details", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Text("✓ VERIFIED", fontSize = tokens.label, color = greentext, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Emily Johnson Vendor", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("452 Industrial Parkway, Suite 102\nNew Delhi, Delhi 110001", fontSize = tokens.bodySmall, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("✉ emily.j@rvendor.com", fontSize = tokens.bodySmall, color = TextSecondary)
                    Text("✆ +91 98765 43210", fontSize = tokens.bodySmall, color = TextSecondary)
                }
            }

            // Cost Breakdown Card
            val displaySubtotal = requisitionDetail?.estimatedSubtotal ?: order.subtotal ?: 25000.0
            val displayTax = requisitionDetail?.estimatedTax ?: order.taxTotal ?: 4500.0
            val displayGrandTotal = requisitionDetail?.estimatedTotal ?: order.grandTotal ?: 29500.0

            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(tokens.cardPadding * 0.8f)) {
                    Text("COST BREAKDOWN", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text("₹${displaySubtotal.toInt()}", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Tax (IGST 5%)", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text("₹${displayTax.toInt()}", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Shipping Charges", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text("₹${(order.shippingCost ?: 0.0).toInt()}", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = dividerColor)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Total", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("₹${displayGrandTotal.toInt()}", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }
            }
        }
    }

    // Approval Action Dialog
    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Approve Requisition", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to approve this purchase requisition?", fontSize = tokens.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        order.id?.let {
                            viewModel.actionRequisitionApproval(
                                id = it,
                                status = "Approved",
                                remarks = "Approved from detail screen"
                            )
                        }
                        showApproveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Approve")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showApproveDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Receive Dialog
    if (showReceiveDialog) {
        AlertDialog(
            onDismissRequest = { showReceiveDialog = false },
            title = { Text("Receive Purchase Order", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold) },
            text = { Text("Do you want to complete reception of items for ${order.poNumber}?", fontSize = tokens.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        val req = ReceivePurchaseOrderRequest(
                            poId = order.id ?: "",
                            warehouseId = "6a8d5643f685905f29057664",
                            subtotal = order.subtotal ?: 0.0,
                            taxTotal = order.taxTotal ?: 0.0,
                            grandTotal = order.grandTotal ?: 0.0,
                            items = order.items.map {
                                ReceivePOItemRequest(
                                    itemId = "6a8c3c548b122b97dd1a7634",
                                    poItemId = it.id ?: "",
                                    warehouseId = "6a8d5643f685905f29057664",
                                    binId = "6a8d58b1f685905f290576dc",
                                    qtyReceived = it.qty,
                                    rate = it.rate,
                                    taxPercent = it.taxPercent,
                                    subtotal = it.subtotal,
                                    taxAmount = it.taxAmount,
                                    total = it.total
                                )
                            }
                        )
                        viewModel.receivePurchaseOrder(req)
                        showReceiveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Confirm Receive")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showReceiveDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Bill Convert Dialog
    if (showBillConvertDialog && billConvertData != null) {
        AlertDialog(
            onDismissRequest = {
                showBillConvertDialog = false
                viewModel.clearBillConvertDetail()
            },
            title = { Text("Convert to Bill Summary", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("PO: ${billConvertData?.poNumber}", fontWeight = FontWeight.Bold, fontSize = tokens.bodyMedium)
                    Text("Supplier: ${billConvertData?.supplier?.name ?: "N/A"}", fontSize = tokens.bodySmall)
                    Text("Items Ready for Billing: ${billConvertData?.items?.size ?: 0}", fontSize = tokens.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showBillConvertDialog = false
                    viewModel.clearBillConvertDetail()
                }) { Text("Done") }
            }
        )
    }
}

@Composable
fun DetailMetricBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(tokens.cardPadding * 0.6f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(primary_light),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title.uppercase(), fontSize = tokens.label, color = TextSecondary, fontWeight = FontWeight.Medium)
                Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}