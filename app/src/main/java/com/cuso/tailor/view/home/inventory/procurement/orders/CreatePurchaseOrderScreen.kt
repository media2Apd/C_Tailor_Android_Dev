@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable"
)

package com.cuso.tailor.view.home.inventory.procurement.orders

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.LowStockItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.util.Locale
import java.util.UUID

/**
 * Data model representing a Purchase Order line item card matching the design.
 */
data class PurchaseOrderItemEntry(
    val id: String = UUID.randomUUID().toString(),
    var itemId: String = "",
    var name: String = "Cotton Linen",
    var category: String = "Clothing",
    var orderedQty: String = "50",
    var type: String = "Goods",
    var qtyRe: String = "44",
    var unit: String = "Pcs",
    var rate: String = "440",
    var taxPercent: String = "18",
    var additionalNumber: String = "",
    var total: String = "1540"
)

@Composable
fun CreatePurchaseOrderScreen(
    initialItem: LowStockItemDto? = null,
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCancel: () -> Unit = {},
    onCreateOrder: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val supplierList by viewModel.supplierDropdown.collectAsStateWithLifecycle()
    val warehouseList by viewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val inventoryItemsList by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreatingPO.collectAsStateWithLifecycle()

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Section expansion states
    var isPoDetailsExpanded by remember { mutableStateOf(true) }
    var isSupplierInfoExpanded by remember { mutableStateOf(true) }
    var isLogisticsExpanded by remember { mutableStateOf(true) }
    var isNotesExpanded by remember { mutableStateOf(true) }

    // Dropdowns expanded states
    var isWarehouseDropdownExpanded by remember { mutableStateOf(false) }
    var isSupplierDropdownExpanded by remember { mutableStateOf(false) }
    var itemStatusExpanded by remember { mutableStateOf(false) }
    var selectedItemStatus by remember { mutableStateOf("Received") }

    // 1. PO Details State
    var warehouseName by remember(initialItem) {
        mutableStateOf(initialItem?.warehouseName ?: "")
    }
    var selectedWarehouseId by remember(initialItem) {
        mutableStateOf(initialItem?.warehouseId ?: "")
    }
    var poDate by remember { mutableStateOf("") }
    var eta by remember { mutableStateOf("") }
    var poType by remember { mutableStateOf("") }
    var referencePrLink by remember { mutableStateOf("") }

    // 2. Supplier Information State
    var supplierName by remember { mutableStateOf("") }
    var selectedSupplierId by remember(initialItem) {
        mutableStateOf(initialItem?.preferredVendorId ?: "")
    }
    var supplierCode by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var gstVatNumber by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("") }
    var creditPeriod by remember { mutableStateOf("") }

    // 3. Line Items State
    val itemsList = remember {
        mutableStateListOf(
            PurchaseOrderItemEntry(
                itemId = initialItem?.itemId ?: "",
                name = initialItem?.name ?: "",
                category = "",
                orderedQty = initialItem?.suggestedQty?.toInt()?.takeIf { it > 0 }?.toString() ?: "",
                type = "",
                qtyRe = initialItem?.available?.toInt()?.toString() ?: "",
                unit = initialItem?.unit ?: "",
                rate = initialItem?.costPrice?.toInt()?.takeIf { it > 0 }?.toString() ?: "",
                taxPercent = "",
                total = ""
            )
        )
    }

    // 4. Logistics State
    var shippingMethod by remember { mutableStateOf("") }
    var shippingAddress by remember { mutableStateOf("") }
    var trackingNo by remember { mutableStateOf("") }

    // 5. Notes & Attachments State
    var internalNotes by remember { mutableStateOf("") }
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedFiles = selectedFiles + uris
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchSupplierDropdown()
        viewModel.loadWarehouseDropdown()
        viewModel.fetchInventoryItems(page = 1, limit = 100)
    }

    // Dynamic Summary Calculations
    val calculatedSubtotal = itemsList.sumOf { item ->
        val qty = item.orderedQty.toDoubleOrNull() ?: 0.0
        val rate = item.rate.toDoubleOrNull() ?: 0.0
        qty * rate
    }.takeIf { it > 0 } ?: 10000.0

    val calculatedTax = itemsList.sumOf { item ->
        val qty = item.orderedQty.toDoubleOrNull() ?: 0.0
        val rate = item.rate.toDoubleOrNull() ?: 0.0
        val taxPercent = item.taxPercent.toDoubleOrNull() ?: 18.0
        (qty * rate) * (taxPercent / 100.0)
    }.takeIf { it > 0 } ?: 1050.0

    val calculatedGrandTotal = calculatedSubtotal.takeIf { true } ?: 10000.0

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                TitleBar("Create Purchase Order", onClose = onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = tokens.screenPadding * 2.5f),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
            ) {
                // ── 1. PO Details ──
                item {
                    Spacer(Modifier.height(tokens.screenPadding))

                    FormAccordionCard(
                        title = "PO Details",
                        expanded = isPoDetailsExpanded,
                        onHeaderClick = { isPoDetailsExpanded = !isPoDetailsExpanded }
                    ) {
                        FormDropdown(
                            label = "WAREHOUSE",
                            value = warehouseName.ifBlank { "Select Warehouse" },
                            expanded = isWarehouseDropdownExpanded,
                            onExpandChange = { isWarehouseDropdownExpanded = it },
                            options = warehouseList.map { it.label }.ifEmpty {
                                listOf("Alexander", "Factory Warehouse (Primary)", "Retail Warehouse")
                            },
                            onOptionSelected = { selectedLabel ->
                                val selected = warehouseList.firstOrNull { it.label == selectedLabel }
                                warehouseName = selectedLabel
                                selectedWarehouseId = selected?.value ?: selectedWarehouseId
                            }
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("PO DATE")
                                FormTextField(
                                    value = poDate,
                                    onValueChange = { poDate = it },
                                    placeholder = "1236547"
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("ETA")
                                FormTextField(
                                    value = eta,
                                    onValueChange = { eta = it },
                                    placeholder = "Alexander"
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel("PO TYPE")
                        FormTextField(
                            value = poType,
                            onValueChange = { poType = it },
                            placeholder = "Wedding Shirt"
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel("REFERENCE PR LINK")
                        FormTextField(
                            value = referencePrLink,
                            onValueChange = { referencePrLink = it },
                            placeholder = "Reference PR Link"
                        )
                    }
                }

                // ── 2. Supplier Information ──
                item {
                    FormAccordionCard(
                        title = "Supplier Information",
                        expanded = isSupplierInfoExpanded,
                        onHeaderClick = { isSupplierInfoExpanded = !isSupplierInfoExpanded }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "SUPPLIER NAME",
                                    value = supplierName.ifBlank { "Select Supplier" },
                                    expanded = isSupplierDropdownExpanded,
                                    onExpandChange = { isSupplierDropdownExpanded = it },
                                    options = supplierList.map { it.label }.ifEmpty {
                                        listOf("Alexander", "Global Textile Corp", "Sunrise Fabrics")
                                    },
                                    onOptionSelected = { selectedLabel ->
                                        val selected = supplierList.firstOrNull { it.label == selectedLabel }
                                        supplierName = selectedLabel
                                        selectedSupplierId = selected?.value ?: selectedSupplierId
                                    }
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("SUPPLIER CODE")
                                FormTextField(
                                    value = supplierCode,
                                    onValueChange = { supplierCode = it },
                                    placeholder = "Supplier code"
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel("CONTACT PERSON")
                        FormTextField(
                            value = contactPerson,
                            onValueChange = { contactPerson = it },
                            placeholder = "Contact Person"
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel("GST/VAT NUMBER")
                        FormTextField(
                            value = gstVatNumber,
                            onValueChange = { gstVatNumber = it },
                            placeholder = "Gst"
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("PAYMENT TERMS")
                                FormTextField(
                                    value = paymentTerms,
                                    onValueChange = { paymentTerms = it },
                                    placeholder = "Payment terms"
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("CREDIT PERIOD")
                                FormTextField(
                                    value = creditPeriod,
                                    onValueChange = { creditPeriod = it },
                                    placeholder = ""
                                )
                            }
                        }
                    }
                }

                // ── 3. Item Cards & Add Item ──
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding)
                    ) {
                        // Header with Filter Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Item",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )

                            Box {
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                    color = whiteBg,
                                    modifier = Modifier.clickable { itemStatusExpanded = !itemStatusExpanded }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedItemStatus,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF4B5563)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color(0xFF6B7280),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = itemStatusExpanded,
                                    onDismissRequest = { itemStatusExpanded = false }
                                ) {
                                    listOf("Received", "Pending", "Partial", "All").forEach { status ->
                                        DropdownMenuItem(
                                            text = { Text(status, fontSize = 13.sp) },
                                            onClick = {
                                                selectedItemStatus = status
                                                itemStatusExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Render Item Cards
                        itemsList.forEachIndexed { index, itemEntry ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = tokens.extraPadding),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                                colors = CardDefaults.cardColors(containerColor = whiteBg),
                                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Item Title Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(sectionBorder),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Checkroom,
                                                contentDescription = null,
                                                tint = headerGrey,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Spacer(Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = itemEntry.name,
                                                fontSize = tokens.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = title_color
                                            )
                                            Text(
                                                text = itemEntry.category,
                                                fontSize = tokens.caption,
                                                color = mutedText
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                if (itemsList.size > 1) {
                                                    itemsList.removeAt(index)
                                                }
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Delete Item",
                                                tint = Color(0xFF9CA3AF),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = Color(0xFFF1F3F5)
                                    )

                                    // Metrics Grid Row 1
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        MetricColumn("ORDERED", itemEntry.orderedQty, Modifier.weight(1f))
                                        MetricColumn("TYPE", itemEntry.type, Modifier.weight(1f))
                                        MetricColumn("QTY(RE)", itemEntry.qtyRe, Modifier.weight(1f))
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // Metrics Grid Row 2
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        MetricColumn("UNIT", itemEntry.unit, Modifier.weight(1f))
                                        MetricColumn("RATE", itemEntry.rate, Modifier.weight(1f))
                                        MetricColumn("TAX %", itemEntry.taxPercent, Modifier.weight(1f))
                                    }

                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = Color(0xFFF1F3F5)
                                    )

                                    // Item Footer
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "+ Additional Number",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Primary,
                                            modifier = Modifier.clickable { /* Handle additional numbering */ }
                                        )

                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = "TOTAL  ",
                                                fontSize = 11.sp,
                                                color = mutedText,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = itemEntry.total,
                                                fontSize = tokens.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = title_color
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Add Item Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .border(
                                    width = 1.dp,
                                    color = Primary,
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                )
                                .clickable {
                                    itemsList.add(
                                        PurchaseOrderItemEntry(
                                            name = "",
                                            category = "",
                                            orderedQty = "",
                                            type = "",
                                            qtyRe = "",
                                            unit = "",
                                            rate = "",
                                            taxPercent = "",
                                            total = ""
                                        )
                                    )
                                }
                                .padding(vertical = tokens.extraPadding * 1.1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize * 0.9f)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Add Item",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary
                                )
                            }
                        }
                    }
                }

                // ── 4. Logistics ──
                item {
                    FormAccordionCard(
                        title = "Logistics",
                        expanded = isLogisticsExpanded,
                        onHeaderClick = { isLogisticsExpanded = !isLogisticsExpanded }
                    ) {
                        FormLabel("Shipping Method")
                        FormTextField(
                            value = shippingMethod,
                            onValueChange = { shippingMethod = it },
                            placeholder = "-"
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel("Shipping Address")
                        FormTextField(
                            value = shippingAddress,
                            onValueChange = { shippingAddress = it },
                            placeholder = "-"
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel("Tracking/Reference No.")
                        FormTextField(
                            value = trackingNo,
                            onValueChange = { trackingNo = it },
                            placeholder = "-"
                        )
                    }
                }

                // ── 5. Attachments & Notes ──
                item {
                    FormAccordionCard(
                        title = "Attachments & Notes",
                        expanded = isNotesExpanded,
                        onHeaderClick = { isNotesExpanded = !isNotesExpanded }
                    ) {
                        FormLabel("Internal Notes")
                        FormTextArea(
                            value = internalNotes,
                            onValueChange = { internalNotes = it },
                            placeholder = "Enter internal notes..."
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
                            documentUploadText = "Upload or drag attachments here",
                            uploadBoxHeight = 120.dp,
                            previewHeaderTitle = "ATTACHED FILES"
                        )
                    }
                }

                // ── 6. Order Summary ──
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                        border = BorderStroke(1.dp, Color(0xFFF1F2F4))
                    ) {
                        Column(modifier = Modifier.padding(tokens.screenPadding)) {
                            Text(
                                text = "Order Summary",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )

                            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", fontSize = tokens.bodySmall, color = mutedText)
                                Text(
                                    text = String.format(Locale.US, "%,.2f", calculatedSubtotal),
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                            }

                            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Tax (18%)", fontSize = tokens.bodySmall, color = mutedText)
                                Text(
                                    text = String.format(Locale.US, "%,.2f", calculatedTax),
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                            }

                            Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Grand Total",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Text(
                                    text = String.format(Locale.US, "%,.2f", calculatedGrandTotal),
                                    fontSize = tokens.h2,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                            }
                        }
                    }
                }

                // ── 7. Bottom Action Buttons ──
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = title_color
                                )
                            ) {
                                Text("Cancel", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    successMessage = "PO Draft saved successfully"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = title_color
                                )
                            ) {
                                Text("Save Draft", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        Button(
                            onClick = {
                                val firstItem = itemsList.firstOrNull()
                                val parsedQty = firstItem?.orderedQty?.toDoubleOrNull() ?: 50.0
                                val parsedRate = firstItem?.rate?.toDoubleOrNull() ?: 440.0
                                val targetItemId = firstItem?.itemId?.ifBlank { null }
                                    ?: initialItem?.itemId
                                    ?: ""

                                viewModel.createPurchaseOrder(
                                    supplierId = selectedSupplierId.ifBlank { "" },
                                    warehouseId = selectedWarehouseId.ifBlank { "" },
                                    itemId = targetItemId,
                                    qty = parsedQty,
                                    rate = parsedRate,
                                    eta = formatDateToIso(eta.ifBlank { poDate }),
                                    notes = internalNotes,
                                    onSuccess = { po ->
                                        successMessage = "PO ${po.poNumber} created successfully!"
                                        onCreateOrder()
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                    }
                                )
                            },
                            enabled = !isCreating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight * 1.15f),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            )
                        ) {
                            if (isCreating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Submit Purchase Order",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }

        // ── Dynamic Island Notifications ──
        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8A8A99),
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value.ifBlank { "-" },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = title_color
        )
    }
}

private fun formatDateToIso(dateStr: String): String? {
    if (dateStr.isBlank()) return null
    return try {
        val parts = dateStr.trim().split("-", "/")
        if (parts.size == 3) {
            if (parts[0].length == 4) {
                "${parts[0]}-${parts[1].padStart(2, '0')}-${parts[2].padStart(2, '0')}T00:00:00.000Z"
            } else {
                "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}T00:00:00.000Z"
            }
        } else null
    } catch (_: Exception) {
        null
    }
}