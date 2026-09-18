package com.cuso.tailor.view.home.inventory.procurement.purchase_order

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.CreateRequisitionRequest
import com.cuso.tailor.model.inventory.PurchaseOrder
import com.cuso.tailor.model.inventory.PurchaseOrderItem
import com.cuso.tailor.model.inventory.RequisitionItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel

@Composable
fun POCreateScreen(
    viewModel: InventoryViewModel,
    existingPo: PurchaseOrder? = null,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val isSubmittingRequisition by viewModel.isSubmittingRequisition.collectAsState()
    val isSubmittingPO by viewModel.isSubmittingPO.collectAsState()
    val isBusy = isSubmittingRequisition || isSubmittingPO

    val requisitionSuccessMessage by viewModel.requisitionSuccessMessage.collectAsState()
    val requisitionErrorMessage by viewModel.requisitionErrorMessage.collectAsState()

    LaunchedEffect(requisitionSuccessMessage) {
        requisitionSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearRequisitionAlerts()
            onDismiss()
        }
    }

    LaunchedEffect(requisitionErrorMessage) {
        requisitionErrorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearRequisitionAlerts()
        }
    }

    // Form Field States
    var warehouse by remember { mutableStateOf("Chennai Central Warehouse") }
    var warehouseExpanded by remember { mutableStateOf(false) }

    var poDate by remember { mutableStateOf(existingPo?.poDate?.substringBefore("T") ?: "26-08-2026") }
    var eta by remember { mutableStateOf(existingPo?.eta?.substringBefore("T") ?: "15-09-2026") }
    var poType by remember { mutableStateOf(existingPo?.poType ?: "Standard") }
    var poTypeExpanded by remember { mutableStateOf(false) }
    var refPrLink by remember { mutableStateOf(existingPo?.requisitionId ?: "PR-2026-001") }

    // Supplier Information
    var supplierName by remember { mutableStateOf("Acme Industrial Supplies") }
    var supplierCode by remember { mutableStateOf("SUP-0012") }
    var contactPerson by remember { mutableStateOf("Rajesh Kumar") }
    var gstNumber by remember { mutableStateOf("33AAAAA0000A1Z5") }
    var paymentTerms by remember { mutableStateOf("Net 30") }
    var creditPeriod by remember { mutableStateOf("30 Days") }

    // Logistics & Notes
    var shippingMethod by remember { mutableStateOf("Express Courier") }
    var shippingAddress by remember { mutableStateOf("12 Industrial Estate Rd, Chennai") }
    var trackingNo by remember { mutableStateOf("TRK-990234") }
    var internalNotes by remember { mutableStateOf("Stock running low for upcoming production batch.") }

    // Section Toggle States
    var isPoDetailsExpanded by remember { mutableStateOf(true) }
    var isSupplierExpanded by remember { mutableStateOf(true) }
    var isLogisticsExpanded by remember { mutableStateOf(true) }
    var isNotesExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        TitleBar(
            title = if (existingPo != null) "Update Purchase Order" else "Create Purchase Order",
            onClose = onDismiss
        )

        Column(
            modifier = Modifier.padding(horizontal = tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // Section 1: PO Details
            ExpandableFormCard(
                icon = Icons.Default.Description,
                title = "PO Details",
                expanded = isPoDetailsExpanded,
                onToggle = { isPoDetailsExpanded = !isPoDetailsExpanded }
            ) {
                FormDropdown(
                    label = "Warehouse",
                    value = warehouse,
                    expanded = warehouseExpanded,
                    onExpandChange = { warehouseExpanded = it },
                    options = listOf("Chennai Central Warehouse", "Cold Storage Trichy", "Central Store"),
                    onOptionSelected = { warehouse = it }
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        Column {
                            FormLabel("PO Date")
                            DatePickerField(
                                value = poDate,
                                onDateSelected = { poDate = it }
                            )
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        Column {
                            FormLabel("ETA")
                            DatePickerField(
                                value = eta,
                                onDateSelected = { eta = it }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                FormDropdown(
                    label = "PO Type",
                    value = poType,
                    expanded = poTypeExpanded,
                    onExpandChange = { poTypeExpanded = it },
                    options = listOf("Standard", "Urgent", "Dropship"),
                    onOptionSelected = { poType = it }
                )
                Spacer(Modifier.height(8.dp))
                FormLabel("Reference PR Link")
                FormTextField(value = refPrLink, onValueChange = { refPrLink = it })
            }

            // Section 2: Supplier Information
            ExpandableFormCard(
                icon = Icons.Default.Business,
                title = "Supplier Information",
                expanded = isSupplierExpanded,
                onToggle = { isSupplierExpanded = !isSupplierExpanded }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        Column {
                            FormLabel("Supplier Name")
                            FormTextField(value = supplierName, onValueChange = { supplierName = it })
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        Column {
                            FormLabel("Supplier Code")
                            FormTextField(value = supplierCode, onValueChange = { supplierCode = it })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                FormLabel("Contact Person")
                FormTextField(value = contactPerson, onValueChange = { contactPerson = it })
                Spacer(Modifier.height(8.dp))
                FormLabel("GST/VAT Number")
                FormTextField(value = gstNumber, onValueChange = { gstNumber = it })
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        Column {
                            FormLabel("Payment Terms")
                            FormTextField(value = paymentTerms, onValueChange = { paymentTerms = it })
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        Column {
                            FormLabel("Credit Period")
                            FormTextField(value = creditPeriod, onValueChange = { creditPeriod = it })
                        }
                    }
                }
            }

            // Section 3: Item Cards Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Item", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = TextPrimary)
                Box(
                    modifier = Modifier
                        .background(primary_light, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Received ⌄", fontSize = tokens.label, color = BluePrimary, fontWeight = FontWeight.Bold)
                }
            }

            CreateItemPreviewCard()

            OutlinedButton(
                onClick = { /* Add item action */ },
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Item", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = Primary)
            }

            // Section 4: Logistics
            ExpandableFormCard(
                icon = Icons.Default.LocalShipping,
                title = "Logistics",
                expanded = isLogisticsExpanded,
                onToggle = { isLogisticsExpanded = !isLogisticsExpanded }
            ) {
                FormLabel("Shipping Method")
                FormTextField(value = shippingMethod, onValueChange = { shippingMethod = it }, placeholder = "e.g., Express Courier")
                Spacer(Modifier.height(8.dp))
                FormLabel("Shipping Address")
                FormTextField(value = shippingAddress, onValueChange = { shippingAddress = it }, placeholder = "Enter Full Address")
                Spacer(Modifier.height(8.dp))
                FormLabel("Tracking/Reference No.")
                FormTextField(value = trackingNo, onValueChange = { trackingNo = it })
            }

            // Section 5: Attachments & Notes
            ExpandableFormCard(
                icon = Icons.Default.AttachFile,
                title = "Attachments & Notes",
                expanded = isNotesExpanded,
                onToggle = { isNotesExpanded = !isNotesExpanded }
            ) {
                FormLabel("Internal Notes")
                FormTextArea(
                    value = internalNotes,
                    onValueChange = { internalNotes = it },
                    placeholder = "Add notes for internal records..."
                )
            }

            // Section 6: Order Summary
            Card(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(tokens.cardPadding * 0.8f)) {
                    Text("Order Summary", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Subtotal", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text("25,000.00", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Tax (18%)", fontSize = tokens.bodySmall, color = TextSecondary)
                        Text("4,500.00", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = dividerColor)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Grand Total", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("29,500.00", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }
            }

            // Bottom Actions: Cancel & Save Draft
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Text("Cancel", fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = {
                        val reqPayload = CreateRequisitionRequest(
                            supplierId = "6a8d3d30f7ca518822670495",
                            warehouseId = "6a8d5643f685905f29057664",
                            department = "Production",
                            priority = "High",
                            requiredByDate = "${eta}T00:00:00.000Z",
                            justification = internalNotes,
                            estimatedSubtotal = 25000.0,
                            estimatedTax = 4500.0,
                            estimatedTotal = 29500.0,
                            items = listOf(
                                RequisitionItem(
                                    itemId = "6a8c3c548b122b97dd1a7634",
                                    qty = 100.0,
                                    convertedQty = 0.0,
                                    unit = "PCS",
                                    type = "Goods",
                                    rate = 250.0,
                                    taxPercent = 18.0,
                                    subtotal = 25000.0,
                                    taxAmount = 4500.0,
                                    total = 29500.0,
                                    status = "Pending"
                                )
                            )
                        )
                        viewModel.createRequisition(reqPayload)
                    },
                    enabled = !isBusy,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                ) {
                    Text("Save Draft", fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            // Submit / Send For Approval Button
            Button(
                onClick = {
                    val poPayload = PurchaseOrder(
                        id = existingPo?.id,
                        poType = poType,
                        eta = "${eta}T00:00:00.000Z",
                        subtotal = 25000.0,
                        taxTotal = 4500.0,
                        grandTotal = 29500.0,
                        items = listOf(
                            PurchaseOrderItem(
                                itemId = "6a8c3c548b122b97dd1a7634",
                                qty = 100.0,
                                rate = 250.0,
                                taxPercent = 18.0,
                                subtotal = 25000.0,
                                taxAmount = 4500.0,
                                total = 29500.0
                            )
                        )
                    )

                    if (existingPo != null && existingPo.id != null) {
                        viewModel.updatePurchaseOrderDirect(existingPo.id, poPayload) {
                            onDismiss()
                        }
                    } else {
                        viewModel.createPurchaseOrderDirect(poPayload) {
                            onDismiss()
                        }
                    }
                },
                enabled = !isBusy,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.buttonHeight)
            ) {
                if (isBusy) {
                    CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(tokens.iconSize))
                } else {
                    Text(
                        text = if (existingPo != null) "Update Purchase Order" else "Submit Purchase Order",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = whiteBg
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableFormCard(
    icon: ImageVector,
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(tokens.cardPadding * 0.8f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun CreateItemPreviewCard() {
    val tokens = LocalAppTokens.current

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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(primary_light),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ShoppingBag, null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Cotton Linen", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Clothing", fontSize = tokens.caption, color = TextSecondary)
                    }
                }
                Icon(Icons.Default.DeleteOutline, null, tint = iconMuted, modifier = Modifier.size(tokens.iconSize))
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("ORDERED", fontSize = tokens.label, color = TextSecondary)
                    Text("50", fontSize = tokens.bodySmall, color = TextPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Text("TYPE", fontSize = tokens.label, color = TextSecondary)
                    Text("Goods", fontSize = tokens.bodySmall, color = TextPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Text("QTY(RE)", fontSize = tokens.label, color = TextSecondary)
                    Text("44", fontSize = tokens.bodySmall, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("UNIT", fontSize = tokens.label, color = TextSecondary)
                    Text("Pcs", fontSize = tokens.bodySmall, color = TextPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Text("RATE", fontSize = tokens.label, color = TextSecondary)
                    Text("440", fontSize = tokens.bodySmall, color = TextPrimary)
                }
                Column(Modifier.weight(1f)) {
                    Text("TAX %", fontSize = tokens.label, color = TextSecondary)
                    Text("18", fontSize = tokens.bodySmall, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("+ Additional Number", fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.SemiBold)
                Text("TOTAL  1540", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}