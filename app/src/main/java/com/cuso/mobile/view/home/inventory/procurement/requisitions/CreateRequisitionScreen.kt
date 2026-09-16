package com.cuso.mobile.view.home.inventory.procurement.requisitions

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*

data class FormItemEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String = "Cotton Linen",
    var category: String = "Clothing",
    var ordered: String = "50",
    var type: String = "Goods",
    var qtyReceived: String = "44",
    var unit: String = "Pcs",
    var rate: String = "440",
    var tax: String = "18",
    var total: String = "1540"
)

@Composable
fun CreateRequisitionScreen(
    onClose: () -> Unit,
    onSendForApproval: () -> Unit = {},
    onSaveDraft: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // Accordion Expand/Collapse States
    var isSupplierInfoExpanded by remember { mutableStateOf(false) }
    var isOrderDetailsExpanded by remember { mutableStateOf(false) }
    var isLogisticsExpanded by remember { mutableStateOf(false) }
    var isNotesExpanded by remember { mutableStateOf(false) }
    var isApproverExpanded by remember { mutableStateOf(false) }

    // Item Received Status Dropdown
    var itemStatus by remember { mutableStateOf("") }
    var isItemStatusExpanded by remember { mutableStateOf(false) }

    // Supplier Info
    var supplierName by remember { mutableStateOf("") }
    var supplierCode by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var billingAddress by remember { mutableStateOf("") }

    // Order Details
    var purchaseType by remember { mutableStateOf("") }
    var warehouse by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var eta by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("") }

    // Items List
    val itemsList = remember {
        mutableStateListOf(
            FormItemEntry(),
            FormItemEntry()
        )
    }

    //Image upload state
    // Inside CreateRequisitionScreen(...) function body:
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedFiles = selectedFiles + uris
        }
    }
    // Logistics
    var shippingMethod by remember { mutableStateOf("") }
    var shippingAddress by remember { mutableStateOf("") }
    var trackingNo by remember { mutableStateOf("") }

    // Notes
    var internalNotes by remember { mutableStateOf("") }

    // Approver
    var requestedBy by remember { mutableStateOf("") }
    var assignApprover by remember { mutableStateOf("") }

    // Summary
    var discount by remember { mutableStateOf("") }
    var shippingCost by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar("Create Purchase Request", onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)
        ) {
            // ── 1. Supplier Information ──
            item {
                FormAccordionCard(
                    tokens = tokens,
                    title = "Supplier Information",
                    expanded = isSupplierInfoExpanded,
                    onHeaderClick = { isSupplierInfoExpanded = !isSupplierInfoExpanded }
                ) {
                    FormLabel("Supplier Name")
                    FormTextField(value = supplierName, onValueChange = { supplierName = it }, placeholder = "Enter Supplier Name")

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Supplier Code")
                            FormTextField(value = supplierCode, onValueChange = { supplierCode = it }, placeholder = "Code")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Contact Person")
                            FormTextField(value = contactPerson, onValueChange = { contactPerson = it }, placeholder = "Contact Person")
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Email Address")
                    FormTextField(value = emailAddress, onValueChange = { emailAddress = it }, placeholder = "Email")

                    Spacer(Modifier.height(tokens.extraPadding))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Phone Number")
                            FormTextField(value = phoneNumber, onValueChange = { phoneNumber = it }, placeholder = "Phone")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Billing Address")
                            FormTextField(value = billingAddress, onValueChange = { billingAddress = it }, placeholder = "Address")
                        }
                    }
                }
            }

            // ── 2. Order Details ──
            item {
                FormAccordionCard(
                    tokens = tokens,
                    title = "Order Details",
                    expanded = isOrderDetailsExpanded,
                    onHeaderClick = { isOrderDetailsExpanded = !isOrderDetailsExpanded }
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Purchase Type")
                            FormTextField(value = purchaseType, onValueChange = { purchaseType = it }, placeholder = "Type")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Warehouse")
                            FormTextField(value = warehouse, onValueChange = { warehouse = it }, placeholder = "Warehouse")
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Department")
                    FormTextField(value = department, onValueChange = { department = it }, placeholder = "Department")

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("ETA")
                    FormTextField(value = eta, onValueChange = { eta = it }, placeholder = "ETA")

                    Spacer(Modifier.height(tokens.extraPadding))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Currency")
                            FormTextField(value = currency, onValueChange = { currency = it }, placeholder = "Currency")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Payment Terms")
                            FormTextField(value = paymentTerms, onValueChange = { paymentTerms = it }, placeholder = "Terms")
                        }
                    }
                }
            }

            // ── 3. Items Section ──
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Item", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)

                        // Status Dropdown Menu
                        Box {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                    .clickable { isItemStatusExpanded = true }
                                    .padding(horizontal = tokens.extraPadding, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(itemStatus, fontSize = tokens.caption, color = title_color, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = mutedText,
                                        modifier = Modifier.size(tokens.iconSize * 0.9f)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isItemStatusExpanded,
                                onDismissRequest = { isItemStatusExpanded = false },
                                containerColor = whiteBg
                            ) {
                                listOf("Received", "Pending", "Ordered", "In Transit").forEach { statusOption ->
                                    DropdownMenuItem(
                                        text = { Text(statusOption, fontSize = tokens.caption, color = title_color) },
                                        onClick = {
                                            itemStatus = statusOption
                                            isItemStatusExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    itemsList.forEachIndexed { index, item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = tokens.extraPadding),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.55f),
                            colors = CardDefaults.cardColors(containerColor = whiteBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(tokens.extraPadding * 1.4f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(tokens.iconSize * 2f)
                                            .background(disabled, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                    )
                                    Spacer(Modifier.width(tokens.extraPadding))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                                        Text(item.category, fontSize = tokens.caption, color = mutedText)
                                    }
                                    IconButton(
                                        onClick = { if (itemsList.size > 1) itemsList.removeAt(index) },
                                        modifier = Modifier.size(tokens.iconSize * 1.3f)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = redText, modifier = Modifier.size(tokens.iconSize))
                                    }
                                }

                                Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    MiniItemStat(tokens, "Ordered", item.ordered)
                                    MiniItemStat(tokens, "Type", item.type)
                                    MiniItemStat(tokens, "Qty(Re)", item.qtyReceived)
                                }
                                Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    MiniItemStat(tokens, "Unit", item.unit)
                                    MiniItemStat(tokens, "Rate", item.rate)
                                    MiniItemStat(tokens, "Tax %", item.tax)
                                }

                                Spacer(Modifier.height(tokens.extraPadding * 1.2f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("+ Add Serial Number", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = Primary)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total", fontSize = tokens.label, color = mutedText)
                                        Text(item.total, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                                    }
                                }
                            }
                        }
                    }

                    // Add Item Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Primary, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .clickable { itemsList.add(FormItemEntry()) }
                            .padding(vertical = tokens.extraPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize * 0.9f))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Item", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = Primary)
                        }
                    }
                }
            }

            // ── 4. Logistics ──
            item {
                FormAccordionCard(
                    tokens = tokens,
                    title = "Logistics",
                    expanded = isLogisticsExpanded,
                    onHeaderClick = { isLogisticsExpanded = !isLogisticsExpanded }
                ) {
                    FormLabel("Shipping Method")
                    FormTextField(value = shippingMethod, onValueChange = { shippingMethod = it }, placeholder = "")

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Shipping Address")
                    FormTextField(value = shippingAddress, onValueChange = { shippingAddress = it }, placeholder = "")

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Tracking/Reference No.")
                    FormTextField(value = trackingNo, onValueChange = { trackingNo = it }, placeholder = "")
                }
            }

            // ── 5. Attachments & Notes ──
            item {
                FormAccordionCard(
                    tokens = tokens,
                    title = "Attachments & Notes",
                    expanded = isNotesExpanded,
                    onHeaderClick = { isNotesExpanded = !isNotesExpanded }
                ) {
                    FormLabel("Internal Notes")
                    FormTextArea(
                        value = internalNotes,
                        onValueChange = { internalNotes = it },
                        placeholder = "Enter internal notes here..."
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 1.2f))
                    FormLabel("Files")
                    ImageUploadSection(
                        isImage = false,
                        selectedImages = selectedFiles,
                        onBrowseClick = { filePickerLauncher.launch("*/*") },
                        onRemoveImage = { uri ->
                            selectedFiles = selectedFiles.filter { it != uri }
                        },
                        documentUploadText = "Upload or drop files here",
                        uploadBoxHeight = 120.dp,
                        previewHeaderTitle = "ATTACHED FILES"
                    )
                }
            }

            // ── 6. Order Details & Approver ──
            item {
                FormAccordionCard(
                    tokens = tokens,
                    title = "Order Details",
                    expanded = isApproverExpanded,
                    onHeaderClick = { isApproverExpanded = !isApproverExpanded }
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Requested By")
                            FormTextField(value = requestedBy, onValueChange = { requestedBy = it }, placeholder = "")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Assign Approver")
                            FormTextField(value = assignApprover, onValueChange = { assignApprover = it }, placeholder = "")
                        }
                    }
                }
            }

            // ── 7. Order Summary ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                    colors = CardDefaults.cardColors(containerColor = whiteBg)
                ) {
                    Column(modifier = Modifier.padding(tokens.screenPadding)) {
                        Text("Order Summary", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)
                        Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = tokens.bodySmall, color = mutedText)
                            Text("10,000.00", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                        }
                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax (18%)", fontSize = tokens.bodySmall, color = mutedText)
                            Text("1,050.00", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                        }

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Discount")
                        FormTextField(value = discount, onValueChange = { discount = it }, placeholder = "0.00")

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Shipping Cost")
                        FormTextField(value = shippingCost, onValueChange = { shippingCost = it }, placeholder = "0.00")

                        Spacer(Modifier.height(tokens.extraPadding * 1.4f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                            Text("10,000.00", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = title_color)
                        }
                    }
                }
            }

            // ── 8. Bottom Buttons ──
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = modelGray),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                    }
                    Button(
                        onClick = onSaveDraft,
                        colors = ButtonDefaults.buttonColors(containerColor = modelGray),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                    ) {
                        Text("Save Draft", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding))

                Button(
                    onClick = onSendForApproval,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight)
                ) {
                    Text("Send For Approval", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = whiteBg)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE COLLAPSIBLE ACCORDION CARD
// ─────────────────────────────────────────────────────────────

@Composable
private fun FormAccordionCard(
    tokens: AppDesignTokens,
    title: String,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "chevronRotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onHeaderClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = mutedText,
                    modifier = Modifier
                        .size(tokens.iconSize * 1.1f)
                        .rotate(arrowRotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(250)) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(150))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(tokens.extraPadding * 1.4f))
                    content()
                }
            }
        }
    }
}

@Composable
private fun MiniItemStat(tokens: AppDesignTokens, label: String, value: String) {
    Column {
        Text(label, fontSize = tokens.label, color = mutedText)
        Spacer(Modifier.height(1.dp))
        Text(value, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
    }
}