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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.LowStockItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.util.Locale

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
    val isCreating by viewModel.isCreatingPO.collectAsStateWithLifecycle()

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dropdowns Expanded State
    var isSupplierDropdownExpanded by remember { mutableStateOf(false) }
    var isWarehouseDropdownExpanded by remember { mutableStateOf(false) }

    // 1. Supplier State
    var supplierName by remember { mutableStateOf("") }
    var selectedSupplierId by remember(initialItem) {
        mutableStateOf(initialItem?.preferredVendorId ?: "")
    }

    // 2. Warehouse State
    var warehouseName by remember(initialItem) {
        mutableStateOf(initialItem?.warehouseName ?: "")
    }
    var selectedWarehouseId by remember(initialItem) {
        mutableStateOf(initialItem?.warehouseId ?: "")
    }

    // 3. Reorder Quantity & Unit Price
    var reorderQuantity by remember(initialItem) {
        mutableStateOf(initialItem?.suggestedQty?.toInt()?.takeIf { it > 0 }?.toString() ?: "0")
    }
    val quantityUnit by remember(initialItem) {
        mutableStateOf(initialItem?.unit?.ifBlank { "Meters" } ?: "Meters")
    }

    var unitPrice by remember(initialItem) {
        mutableStateOf(initialItem?.costPrice?.toInt()?.takeIf { it > 0 }?.toString() ?: "500")
    }

    // 4. Delivery & Priority & Notes
    var expectedDelivery by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Normal") } // Normal, Urgent, Critical
    var additionalNotes by remember { mutableStateOf("") }

    // 5. Files Upload
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedFiles = selectedFiles + uris
        }
    }

    // Fetch initial dropdown data on launch
    LaunchedEffect(Unit) {
        viewModel.fetchSupplierDropdown()
        viewModel.loadWarehouseDropdown()
    }

    // If pre-filled supplier ID exists, map its display label
    LaunchedEffect(supplierList, selectedSupplierId) {
        if (selectedSupplierId.isNotBlank() && supplierName.isBlank()) {
            supplierList.firstOrNull { it.value == selectedSupplierId }?.let {
                supplierName = it.label
            }
        }
    }

    // Dynamic calculations
    val qtyVal = reorderQuantity.toDoubleOrNull() ?: 0.0
    val priceVal = unitPrice.toDoubleOrNull() ?: 0.0
    val totalOrderValue = qtyVal * priceVal

    Scaffold(
        containerColor = whiteBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                TitleBar(title = "Create Purchase Order", onClose = onClose)
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = tokens.screenPadding),
                contentPadding = PaddingValues(
                    top = tokens.extraPadding,
                    bottom = tokens.screenPadding * 2.5f
                ),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
            ) {
                // ── 1. Supplier & Warehouse (Dropdowns) ──
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Supplier",
                                value = supplierName.ifBlank { "Select supplier" },
                                expanded = isSupplierDropdownExpanded,
                                onExpandChange = { isSupplierDropdownExpanded = it },
                                options = supplierList.map { it.label }.ifEmpty {
                                    listOf("Select supplier")
                                },
                                onOptionSelected = { selectedLabel ->
                                    val selected = supplierList.firstOrNull { it.label == selectedLabel }
                                    supplierName = selectedLabel
                                    selectedSupplierId = selected?.value ?: ""
                                }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Warehouse",
                                value = warehouseName.ifBlank { "Select warehouse" },
                                expanded = isWarehouseDropdownExpanded,
                                onExpandChange = { isWarehouseDropdownExpanded = it },
                                options = warehouseList.map { it.label }.ifEmpty {
                                    listOf("Select warehouse")
                                },
                                onOptionSelected = { selectedLabel ->
                                    val selected = warehouseList.firstOrNull { it.label == selectedLabel }
                                    warehouseName = selectedLabel
                                    selectedWarehouseId = selected?.value ?: ""
                                }
                            )
                        }
                    }
                }

                // ── 2. Reorder Quantity & Unit Price ──
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        // Reorder Quantity Field
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Reorder Quantity")
                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                                border = BorderStroke(1.dp, grey_border),
                                color = whiteBg,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tokens.fieldHeight)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = reorderQuantity,
                                        onValueChange = { input ->
                                            reorderQuantity = input.filter { it.isDigit() || it == '.' }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        ),
                                        cursorBrush = SolidColor(Primary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = tokens.extraPadding)
                                    )

                                    // Unit Badge
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .background(light_grey)
                                            .padding(horizontal = tokens.extraPadding),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = quantityUnit,
                                            fontSize = tokens.caption,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Unit Price Field
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Unit Price")
                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                                border = BorderStroke(1.dp, grey_border),
                                color = light_blue,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tokens.fieldHeight)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .padding(horizontal = tokens.extraPadding),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "₹",
                                            fontSize = tokens.bodySmall,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    BasicTextField(
                                        value = unitPrice,
                                        onValueChange = { input ->
                                            unitPrice = input.filter { it.isDigit() || it == '.' }
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = TextStyle(
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        ),
                                        cursorBrush = SolidColor(Primary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = tokens.extraPadding)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── 3. Total Order Value Highlight Banner ──
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = CardDefaults.cardColors(containerColor = background_light_purple)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Order Value",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                                Text(
                                    text = "Excluding Taxes & Shipping",
                                    fontSize = tokens.caption,
                                    color = chartIndigo
                                )
                            }

                            Text(
                                text = "₹ ${String.format(Locale.US, "%,.0f", totalOrderValue)}",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                    }
                }

                // ── 4. Expected Delivery & Priority ──
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        // Expected Delivery with DatePickerField
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Expected Delivery")
                            DatePickerField(
                                value = expectedDelivery,
                                onDateSelected = { expectedDelivery = it }
                            )
                        }

                        // Priority (Normal / Urgent / Critical) Toggle
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Priority")
                            Surface(
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                                color = light_grey,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tokens.fieldHeight)
                                    .padding(3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("Normal", "Urgent", "Critical").forEach { opt ->
                                        val isSelected = priority == opt
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                                                .background(if (isSelected) whiteBg else Color.Transparent)
                                                .clickable { priority = opt },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = opt,
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isSelected) blackTitle else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 5. Additional Notes ──
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel("Additional Notes")
                        FormTextArea(
                            value = additionalNotes,
                            onValueChange = { additionalNotes = it },
                            placeholder = "Specific packaging or handling requirements...",
                            minLines = 4,
                            maxLines = 5,
                            borderColor = sectionBorder
                        )
                    }
                }

                // ── 6. Drag & Drop Specification Sheets Area ──
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(125.dp)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .border(
                                width = 1.dp,
                                color = sectionBorder,
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                            )
                            .background(cardBgLight)
                            .clickable { filePickerLauncher.launch("*/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudUpload,
                                contentDescription = null,
                                tint = headerGrey,
                                modifier = Modifier.size(tokens.iconSize * 1.5f)
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "Drag & Drop specification sheets",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = textSubdued
                            )

                            Spacer(Modifier.height(2.dp))

                            Text(
                                text = "Max 5MB · PDF, JPG, PNG",
                                fontSize = tokens.caption,
                                color = iconMuted
                            )
                        }
                    }

                    // Selected files preview list
                    if (selectedFiles.isNotEmpty()) {
                        Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            selectedFiles.forEach { uri ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                                        .background(light_blue)
                                        .padding(horizontal = tokens.extraPadding, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.InsertDriveFile,
                                            contentDescription = null,
                                            tint = Primary,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = uri.lastPathSegment ?: "document_sheet",
                                            fontSize = tokens.caption,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Remove",
                                        tint = redText,
                                        modifier = Modifier
                                            .size(tokens.iconSize)
                                            .clickable {
                                                selectedFiles = selectedFiles.filter { it != uri }
                                            }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── 7. Action Buttons (Cancel / Save & Order) ──
                item {
                    Spacer(Modifier.height(tokens.extraPadding))

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
                            border = BorderStroke(1.dp, grey_border),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = whiteBg,
                                contentColor = TextPrimary
                            )
                        ) {
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.Medium,
                                fontSize = tokens.bodyMedium
                            )
                        }

                        Button(
                            onClick = {
                                val targetItemId = initialItem?.itemId.orEmpty()
                                val parsedQty = reorderQuantity.toDoubleOrNull() ?: 1.0
                                val parsedRate = unitPrice.toDoubleOrNull() ?: 0.0

                                val formattedNotes = buildString {
                                    append("Priority: $priority")
                                    if (additionalNotes.isNotBlank()) {
                                        append("\n$additionalNotes")
                                    }
                                }

                                viewModel.createPurchaseOrder(
                                    supplierId = selectedSupplierId,
                                    warehouseId = selectedWarehouseId,
                                    itemId = targetItemId,
                                    qty = parsedQty,
                                    rate = parsedRate,
                                    eta = formatDateToIso(expectedDelivery),
                                    notes = formattedNotes,
                                    onSuccess = { po ->
                                        successMessage = "Purchase order ${po.poNumber} created successfully!"
                                        onCreateOrder()
                                    },
                                    onError = { err ->
                                        errorMessage = err
                                    }
                                )
                            },
                            enabled = !isCreating && selectedWarehouseId.isNotBlank(),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            if (isCreating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(tokens.iconSize),
                                    color = whiteBg,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Save & Order",
                                    color = whiteBg,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = tokens.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Dynamic Island Message Notifications ──
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

/**
 * Utility to convert "dd-MM-yyyy" from DatePickerField into ISO-8601 string required by the API.
 */
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