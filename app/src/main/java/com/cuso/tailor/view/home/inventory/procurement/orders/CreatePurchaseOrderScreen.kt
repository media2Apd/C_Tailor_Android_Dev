@file:Suppress("UNUSED_PARAMETER", "unused", "AssignedValueIsNeverRead", "unusedVariable", "VariableNeverRead")

package com.cuso.tailor.view.home.inventory.procurement.orders

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.LowStockItemDto
import com.cuso.tailor.model.inventory.PurchaseOrderLinePayload
import com.cuso.tailor.model.inventory.SupplierDropdownItem
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.background_light_purple
import com.cuso.tailor.ui.theme.blackTitle
import com.cuso.tailor.ui.theme.cardBgLight
import com.cuso.tailor.ui.theme.chartIndigo
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.iconMuted
import com.cuso.tailor.ui.theme.light_blue
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.textSubdued
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.formatCurrency
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PO_TYPE_OPTIONS = listOf("Standard", "Urgent", "Critical")

private data class PoLineItemForm(
    val itemId: String = "",
    val itemName: String = "",
    val type: String = "Goods",
    val qty: String = "1",
    val unit: String = "Meters",
    val rate: String = "0",
    val taxGroupId: String = "",
    val taxGroupLabel: String = "",
    val taxPercent: Double = 0.0
) {
    val subtotal: Double
        get() = (qty.toDoubleOrNull() ?: 0.0) * (rate.toDoubleOrNull() ?: 0.0)

    val taxAmount: Double
        get() = (subtotal * taxPercent) / 100.0

    val total: Double
        get() = subtotal + taxAmount
}

@Composable
fun CreatePurchaseOrderScreen(
    initialItem: LowStockItemDto? = null,
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCancel: () -> Unit = {},
    onCreateOrder: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val supplierDropdown by viewModel.supplierDropdown.collectAsStateWithLifecycle()
    val suppliersList by viewModel.suppliers.collectAsStateWithLifecycle()
    val warehouseList by viewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val warehouseUiState by viewModel.warehouseUiState.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val taxGroups by viewModel.taxGroups.collectAsStateWithLifecycle()
    val isCreating by viewModel.isSubmittingPO.collectAsStateWithLifecycle()
    val poErrorMessage by viewModel.purchaseOrdersError.collectAsStateWithLifecycle()

    LaunchedEffect(poErrorMessage) {
        poErrorMessage?.let {
            DynamicIslandManager.showError(it)
            viewModel.clearPOAlerts()
        }
    }

    // Resolved supplier dropdown options (combines dropdown endpoint and view-all list)
    val resolvedSuppliers = remember(supplierDropdown, suppliersList) {
        val fromDropdown = supplierDropdown.filter { it.value.isNotBlank() && it.label.isNotBlank() }
        val fromViewAll = suppliersList
            .filter { it.id.isNotBlank() && it.name.isNotBlank() }
            .map { SupplierDropdownItem(value = it.id, label = it.name) }
        if (fromDropdown.isNotEmpty()) {
            (fromDropdown + fromViewAll).distinctBy { it.value }
        } else {
            fromViewAll
        }
    }

    // Dropdown expansion states
    var isSupplierDropdownExpanded by remember { mutableStateOf(false) }
    var isWarehouseDropdownExpanded by remember { mutableStateOf(false) }
    var activeItemDropdownIndex by remember { mutableStateOf<Int?>(null) }
    var activeTaxDropdownIndex by remember { mutableStateOf<Int?>(null) }

    // Validation error key
    var errorField by remember { mutableStateOf<String?>(null) }

    // 1. Supplier states
    var supplierName by remember { mutableStateOf("") }
    var selectedSupplierId by remember(initialItem) {
        mutableStateOf(initialItem?.preferredVendorId ?: "")
    }
    var supplierCode by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var supplierGst by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("") }

    // 2. Warehouse & PO details states
    var warehouseName by remember(initialItem) {
        mutableStateOf(initialItem?.warehouseName ?: "")
    }
    var selectedWarehouseId by remember(initialItem) {
        mutableStateOf(initialItem?.warehouseId ?: "")
    }
    var shippingAddress by remember { mutableStateOf("") }

    val todayFormatted = remember {
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }
    var poDate by remember { mutableStateOf(todayFormatted) }
    var expectedDelivery by remember { mutableStateOf("") }
    var poType by remember { mutableStateOf("Standard") }

    // 3. Line items list
    var lineItems by remember(initialItem) {
        val initialQty = initialItem?.suggestedQty?.toInt()?.takeIf { it > 0 }?.toString() ?: "1"
        val initialRate = initialItem?.costPrice?.takeIf { it > 0 }?.toString() ?: "0"
        val initialUnit = initialItem?.unit?.ifBlank { "Meters" } ?: "Meters"
        val initialName = extractProperty(initialItem, "itemName", "name")
        mutableStateOf(
            listOf(
                PoLineItemForm(
                    itemId = initialItem?.itemId.orEmpty(),
                    itemName = initialName,
                    qty = initialQty,
                    unit = initialUnit,
                    rate = initialRate
                )
            )
        )
    }

    // 4. Summary adjustments
    var discountText by remember { mutableStateOf("0") }
    var shippingCostText by remember { mutableStateOf("0") }

    // 5. Logistics & notes states
    var shippingMethod by remember { mutableStateOf("") }
    var trackingNumber by remember { mutableStateOf("") }
    var transportName by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var freightTerms by remember { mutableStateOf("") }
    var additionalNotes by remember { mutableStateOf("") }

    // 6. Files upload
    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedFiles = selectedFiles + uris
        }
    }

    // Fetch catalog data on launch
    LaunchedEffect(Unit) {
        viewModel.fetchSupplierDropdown()
        viewModel.fetchSuppliers(page = 1, limit = 50)
        viewModel.loadWarehouseDropdown()
        viewModel.loadWarehouses()
        viewModel.fetchInventoryItems(page = 1, limit = 100)
        viewModel.fetchTaxGroups()
    }

    // Backfill supplier display name and metadata when suppliers arrive
    LaunchedEffect(resolvedSuppliers, suppliersList, selectedSupplierId) {
        if (selectedSupplierId.isNotBlank()) {
            if (supplierName.isBlank()) {
                resolvedSuppliers.firstOrNull { it.value == selectedSupplierId }?.let {
                    supplierName = it.label
                }
            }
            suppliersList.firstOrNull { it.id == selectedSupplierId }?.let { full ->
                supplierCode = full.supplierCode
                contactPerson = full.contact?.contactName.orEmpty()
                supplierGst = full.tax?.gstNumber.orEmpty()
                val termDays = full.payment?.paymentTerm ?: 0
                paymentTerms = if (termDays > 0) "Net $termDays" else "Immediate"
            }
        }
    }

    // Backfill warehouse shipping address when warehouse details arrive
    LaunchedEffect(warehouseUiState.warehouses, selectedWarehouseId) {
        if (selectedWarehouseId.isNotBlank()) {
            warehouseUiState.warehouses.firstOrNull { it.id == selectedWarehouseId }?.let { w ->
                shippingAddress = listOf(
                    w.address.address,
                    w.address.city,
                    w.address.state,
                    w.address.country,
                    w.address.pincode
                ).filter { it.isNotBlank() }.joinToString(", ")
            }
        }
    }

    // Backfill initial line item details when inventoryItems or taxGroups arrive
    LaunchedEffect(inventoryItems, taxGroups, initialItem) {
        val targetId = initialItem?.itemId.orEmpty()
        if (targetId.isNotBlank() && inventoryItems.isNotEmpty()) {
            val matchedItem = inventoryItems.firstOrNull {
                extractProperty(it, "_id", "id") == targetId
            }
            if (matchedItem != null) {
                val matchedName = extractProperty(matchedItem, "name")
                val matchedSku = extractProperty(matchedItem, "sku")
                val displayName = if (matchedSku.isNotBlank()) "$matchedName ($matchedSku)" else matchedName
                val matchedType = if (extractProperty(matchedItem, "type").equals("service", ignoreCase = true)) {
                    "Service"
                } else {
                    "Goods"
                }
                val matchedUnit = extractProperty(matchedItem, "unit").ifBlank { "Meters" }
                val matchedRate = extractProperty(matchedItem, "costPrice")
                val matchedTaxGroupId = extractIdOrValue(matchedItem, "taxGroupId")
                val matchedTaxGroup = taxGroups.firstOrNull { it.id == matchedTaxGroupId }

                lineItems = lineItems.mapIndexed { index, row ->
                    if (index == 0 && row.itemId == targetId) {
                        row.copy(
                            itemName = row.itemName.ifBlank { displayName },
                            type = matchedType,
                            unit = row.unit.ifBlank { matchedUnit },
                            rate = if (row.rate == "0" && matchedRate.isNotBlank()) sanitizeDecimalInput(matchedRate) else row.rate,
                            taxGroupId = row.taxGroupId.ifBlank { matchedTaxGroup?.id.orEmpty() },
                            taxGroupLabel = row.taxGroupLabel.ifBlank {
                                matchedTaxGroup?.let { "${it.name} (${it.totalRate}%)" }.orEmpty()
                            },
                            taxPercent = if (row.taxPercent == 0.0) matchedTaxGroup?.totalRate ?: 0.0 else row.taxPercent
                        )
                    } else {
                        row
                    }
                }
            }
        }
    }

    // Totals calculation
    val subtotalVal = remember(lineItems) { lineItems.sumOf { it.subtotal } }
    val taxVal = remember(lineItems) { lineItems.sumOf { it.taxAmount } }
    val discountVal = discountText.toDoubleOrNull() ?: 0.0
    val shippingCostVal = shippingCostText.toDoubleOrNull() ?: 0.0
    val totalOrderValue = (subtotalVal + taxVal - discountVal + shippingCostVal).coerceAtLeast(0.0)

    val itemOptionEntries = remember(inventoryItems) {
        inventoryItems.mapNotNull { item ->
            val id = extractProperty(item, "_id", "id")
            val name = extractProperty(item, "name")
            if (id.isBlank() || name.isBlank()) return@mapNotNull null
            val sku = extractProperty(item, "sku")
            val label = if (sku.isNotBlank()) "$name ($sku)" else name
            Triple(id, label, item)
        }
    }

    val taxGroupEntries = remember(taxGroups) {
        taxGroups.map { tax ->
            Triple(tax.id, "${tax.name} (${tax.totalRate}%)", tax.totalRate)
        }
    }

    fun validateForm(): Boolean {
        val firstInvalidItemIndex = lineItems.indexOfFirst {
            it.itemId.isBlank() || (it.qty.toDoubleOrNull() ?: 0.0) <= 0.0
        }

        val validationError: Pair<String, String>? = when {
            selectedWarehouseId.isBlank() -> "warehouse" to "Warehouse is required"
            selectedSupplierId.isBlank() -> "supplier" to "Supplier is required"
            poDate.isBlank() -> "poDate" to "PO Date is required"
            lineItems.isEmpty() -> "items" to "At least one item is required"
            firstInvalidItemIndex != -1 -> {
                val item = lineItems[firstInvalidItemIndex]
                if (item.itemId.isBlank()) {
                    "item_$firstInvalidItemIndex" to "Please select an item on row ${firstInvalidItemIndex + 1}"
                } else {
                    "qty_$firstInvalidItemIndex" to "Quantity must be greater than 0 on row ${firstInvalidItemIndex + 1}"
                }
            }
            else -> null
        }

        errorField = validationError?.first
        if (validationError != null) {
            DynamicIslandManager.showError(validationError.second)
            return false
        }
        return true
    }

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
                // 1. Supplier & Warehouse Dropdowns
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
                                options = resolvedSuppliers.map { it.label }.ifEmpty {
                                    listOf("No suppliers available")
                                },
                                onOptionSelected = { selectedLabel ->
                                    val selected = resolvedSuppliers.firstOrNull { it.label == selectedLabel }
                                        ?: return@FormDropdown
                                    supplierName = selected.label
                                    selectedSupplierId = selected.value
                                    if (errorField == "supplier") errorField = null
                                },
                                isRequired = true,
                                isError = errorField == "supplier",
                                errorMessage = if (errorField == "supplier") "Supplier is required" else null
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Warehouse",
                                value = warehouseName.ifBlank { "Select warehouse" },
                                expanded = isWarehouseDropdownExpanded,
                                onExpandChange = { isWarehouseDropdownExpanded = it },
                                options = warehouseList.map { it.label }.ifEmpty {
                                    listOf("No warehouses available")
                                },
                                onOptionSelected = { selectedLabel ->
                                    val selected = warehouseList.firstOrNull { it.label == selectedLabel }
                                        ?: return@FormDropdown
                                    warehouseName = selected.label
                                    selectedWarehouseId = selected.value
                                    if (errorField == "warehouse") errorField = null
                                },
                                isRequired = true,
                                isError = errorField == "warehouse",
                                errorMessage = if (errorField == "warehouse") "Warehouse is required" else null
                            )
                        }
                    }
                }

                // Auto-filled Supplier Information (when a supplier is selected)
                if (selectedSupplierId.isNotBlank()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Supplier Code")
                                FormTextField(
                                    value = supplierCode,
                                    onValueChange = {},
                                    enabled = false,
                                    placeholder = "-"
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Contact Person")
                                FormTextField(
                                    value = contactPerson,
                                    onValueChange = {},
                                    enabled = false,
                                    placeholder = "-"
                                )
                            }
                        }
                        Spacer(Modifier.height(tokens.extraPadding))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("GST/VAT Number")
                                FormTextField(
                                    value = supplierGst,
                                    onValueChange = {},
                                    enabled = false,
                                    placeholder = "-"
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Payment Terms")
                                FormTextField(
                                    value = paymentTerms,
                                    onValueChange = {},
                                    enabled = false,
                                    placeholder = "-"
                                )
                            }
                        }
                    }
                }

                // 2. PO Date, Expected Delivery (ETA) & PO Type
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("PO Date", isRequired = true)
                            DatePickerField(
                                value = poDate,
                                onDateSelected = {
                                    poDate = it
                                    if (errorField == "poDate") errorField = null
                                },
                                isError = errorField == "poDate"
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Expected Delivery (ETA)")
                            DatePickerField(
                                value = expectedDelivery,
                                onDateSelected = { expectedDelivery = it }
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel("PO Type")
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
                                PO_TYPE_OPTIONS.forEach { opt ->
                                    val isSelected = poType == opt
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                                            .background(if (isSelected) whiteBg else Color.Transparent)
                                            .clickable { poType = opt },
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

                // 3. Items Section (supports adding/removing multiple line items)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Items",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                .background(background_light_purple)
                                .clickable {
                                    lineItems = lineItems + PoLineItemForm()
                                }
                                .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding * 0.5f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Item",
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize * 0.9f)
                            )
                            Text(
                                text = "Add Item",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                    }
                }

                items(lineItems.size) { index ->
                    val rowItem = lineItems[index]
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder),
                        color = cardBgLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(tokens.extraPadding),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Item #${index + 1} (${rowItem.type})",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )

                                if (lineItems.size > 1) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Remove item",
                                        tint = redText,
                                        modifier = Modifier
                                            .size(tokens.iconSize)
                                            .clickable {
                                                lineItems = lineItems.filterIndexed { i, _ -> i != index }
                                            }
                                    )
                                }
                            }

                            FormDropdown(
                                label = "Item Details",
                                value = rowItem.itemName.ifBlank { "Select item" },
                                expanded = activeItemDropdownIndex == index,
                                onExpandChange = { open ->
                                    activeItemDropdownIndex = if (open) index else null
                                },
                                options = itemOptionEntries.map { it.second }.ifEmpty {
                                    listOf("No items available")
                                },
                                onOptionSelected = { selectedLabel ->
                                    val picked = itemOptionEntries.firstOrNull { it.second == selectedLabel }
                                        ?: return@FormDropdown
                                    val dto = picked.third
                                    val itemType = if (extractProperty(dto, "type").equals("service", ignoreCase = true)) {
                                        "Service"
                                    } else {
                                        "Goods"
                                    }
                                    val itemUnit = extractProperty(dto, "unit").ifBlank { "Meters" }
                                    val costPrice = sanitizeDecimalInput(extractProperty(dto, "costPrice")).ifBlank { "0" }
                                    val itemTaxGroupId = extractIdOrValue(dto, "taxGroupId")
                                    val matchedTax = taxGroups.firstOrNull { it.id == itemTaxGroupId }

                                    lineItems = lineItems.mapIndexed { i, current ->
                                        if (i == index) {
                                            current.copy(
                                                itemId = picked.first,
                                                itemName = picked.second,
                                                type = itemType,
                                                unit = itemUnit,
                                                rate = costPrice,
                                                taxGroupId = matchedTax?.id.orEmpty(),
                                                taxGroupLabel = matchedTax?.let { "${it.name} (${it.totalRate}%)" }.orEmpty(),
                                                taxPercent = matchedTax?.totalRate ?: 0.0
                                            )
                                        } else {
                                            current
                                        }
                                    }
                                    if (errorField == "item_$index") errorField = null
                                },
                                isRequired = true,
                                isError = errorField == "item_$index",
                                errorMessage = if (errorField == "item_$index") "Item is required" else null
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                            ) {
                                // Quantity + Unit badge
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel("Quantity", isRequired = true)
                                    Surface(
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                                        border = BorderStroke(
                                            1.dp,
                                            if (errorField == "qty_$index") redText else grey_border
                                        ),
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
                                                value = rowItem.qty,
                                                onValueChange = { input ->
                                                    val clean = sanitizeDecimalInput(input)
                                                    lineItems = lineItems.mapIndexed { i, current ->
                                                        if (i == index) current.copy(qty = clean) else current
                                                    }
                                                    if (errorField == "qty_$index") errorField = null
                                                },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .background(light_grey)
                                                    .padding(horizontal = tokens.extraPadding),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = rowItem.unit.ifBlank { "-" },
                                                    fontSize = tokens.caption,
                                                    color = TextSecondary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }

                                // Unit Price
                                Column(modifier = Modifier.weight(1f)) {
                                    FormLabel("Rate")
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
                                                    text = formatCurrency(),
                                                    fontSize = tokens.bodySmall,
                                                    color = TextSecondary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            BasicTextField(
                                                value = rowItem.rate,
                                                onValueChange = { input ->
                                                    val clean = sanitizeDecimalInput(input)
                                                    lineItems = lineItems.mapIndexed { i, current ->
                                                        if (i == index) current.copy(rate = clean) else current
                                                    }
                                                },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column(modifier = Modifier.weight(1.4f)) {
                                    FormDropdown(
                                        label = "Tax %",
                                        value = rowItem.taxGroupLabel.ifBlank { "Select tax" },
                                        expanded = activeTaxDropdownIndex == index,
                                        onExpandChange = { open ->
                                            activeTaxDropdownIndex = if (open) index else null
                                        },
                                        options = listOf("No Tax (0%)") + taxGroupEntries.map { it.second },
                                        onOptionSelected = { chosen ->
                                            if (chosen == "No Tax (0%)") {
                                                lineItems = lineItems.mapIndexed { i, current ->
                                                    if (i == index) {
                                                        current.copy(
                                                            taxGroupId = "",
                                                            taxGroupLabel = "No Tax (0%)",
                                                            taxPercent = 0.0
                                                        )
                                                    } else current
                                                }
                                            } else {
                                                val match = taxGroupEntries.firstOrNull { it.second == chosen }
                                                    ?: return@FormDropdown
                                                lineItems = lineItems.mapIndexed { i, current ->
                                                    if (i == index) {
                                                        current.copy(
                                                            taxGroupId = match.first,
                                                            taxGroupLabel = match.second,
                                                            taxPercent = match.third
                                                        )
                                                    } else current
                                                }
                                            }
                                        }
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Line Total",
                                        fontSize = tokens.caption,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${formatCurrency()} ${String.format(Locale.US, "%,.2f", rowItem.total)}",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Discount & Shipping Cost
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Discount")
                            FormTextField(
                                value = discountText,
                                onValueChange = { discountText = sanitizeDecimalInput(it) },
                                keyboardType = KeyboardType.Decimal,
                                placeholder = "0.00"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Shipping Cost")
                            FormTextField(
                                value = shippingCostText,
                                onValueChange = { shippingCostText = sanitizeDecimalInput(it) },
                                keyboardType = KeyboardType.Decimal,
                                placeholder = "0.00"
                            )
                        }
                    }
                }

                // 5. Total Order Value Highlight Banner (Preserved Design)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        colors = CardDefaults.cardColors(containerColor = background_light_purple)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subtotal",
                                    fontSize = tokens.caption,
                                    color = chartIndigo
                                )
                                Text(
                                    text = "${formatCurrency()} ${String.format(Locale.US, "%,.2f", subtotalVal)}",
                                    fontSize = tokens.caption,
                                    color = Primary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tax",
                                    fontSize = tokens.caption,
                                    color = chartIndigo
                                )
                                Text(
                                    text = "${formatCurrency()} ${String.format(Locale.US, "%,.2f", taxVal)}",
                                    fontSize = tokens.caption,
                                    color = Primary
                                )
                            }
                            HorizontalDivider(color = Primary.copy(alpha = 0.15f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Grand Total",
                                        fontSize = tokens.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Primary
                                    )
                                    Text(
                                        text = "Includes Tax, Discount & Shipping",
                                        fontSize = tokens.caption,
                                        color = chartIndigo
                                    )
                                }

                                Text(
                                    text = "${formatCurrency()} ${String.format(Locale.US, "%,.2f", totalOrderValue)}",
                                    fontSize = tokens.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary
                                )
                            }
                        }
                    }
                }

                // 6. Logistics Section
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Text(
                            text = "Logistics",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Column {
                            FormLabel("Shipping Method")
                            FormTextField(
                                value = shippingMethod,
                                onValueChange = { shippingMethod = it },
                                placeholder = "e.g. Road, Courier, Air"
                            )
                        }

                        Column {
                            FormLabel("Shipping Address")
                            FormTextArea(
                                value = shippingAddress,
                                onValueChange = {},
                                enabled = false,
                                placeholder = "Auto-filled from warehouse",
                                minLines = 2,
                                maxLines = 3,
                                borderColor = sectionBorder
                            )
                        }

                        Column {
                            FormLabel("Tracking / Reference No.")
                            FormTextField(
                                value = trackingNumber,
                                onValueChange = { trackingNumber = it },
                                placeholder = "Enter tracking number"
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Transport Company")
                                FormTextField(
                                    value = transportName,
                                    onValueChange = { transportName = it },
                                    placeholder = "Transport name"
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Vehicle Number")
                                FormTextField(
                                    value = vehicleNumber,
                                    onValueChange = { vehicleNumber = it.uppercase() },
                                    placeholder = "e.g. TN-01-AB-1234"
                                )
                            }
                        }

                        Column {
                            FormLabel("Freight Terms")
                            FormTextField(
                                value = freightTerms,
                                onValueChange = { freightTerms = it },
                                placeholder = "e.g. Paid, To Pay, Collect"
                            )
                        }
                    }
                }

                // 7. Internal Notes
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FormLabel("Internal Notes")
                        FormTextArea(
                            value = additionalNotes,
                            onValueChange = { additionalNotes = it },
                            placeholder = "Specific packaging or handling requirements...",
                            minLines = 3,
                            maxLines = 5,
                            borderColor = sectionBorder
                        )
                    }
                }

                // 8. Drag & Drop Specification Sheets Area (Preserved Design)
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

                            Spacer(Modifier.height(tokens.extraPadding * 0.4f))

                            Text(
                                text = "Drag & Drop specification sheets",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = textSubdued
                            )

                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))

                            Text(
                                text = "Max 5MB · PDF, JPG, PNG",
                                fontSize = tokens.caption,
                                color = iconMuted
                            )
                        }
                    }

                    if (selectedFiles.isNotEmpty()) {
                        Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)) {
                            selectedFiles.forEach { uri ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                                        .background(light_blue)
                                        .padding(
                                            horizontal = tokens.extraPadding,
                                            vertical = tokens.extraPadding * 0.6f
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.InsertDriveFile,
                                            contentDescription = null,
                                            tint = Primary,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                        Spacer(Modifier.width(tokens.extraPadding * 0.8f))
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

                // 9. Action Buttons (Cancel / Save & Order)
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
                                if (!validateForm() || isCreating) return@Button

                                val itemsPayload = lineItems.map { row ->
                                    PurchaseOrderLinePayload(
                                        itemId = row.itemId,
                                        qty = row.qty.toDoubleOrNull() ?: 1.0,
                                        rate = row.rate.toDoubleOrNull() ?: 0.0,
                                        taxGroupId = row.taxGroupId.ifBlank { null },
                                        taxPercent = row.taxPercent
                                    )
                                }

                                viewModel.createPurchaseOrderMultipart(
                                    context = context,
                                    warehouseId = selectedWarehouseId,
                                    supplierId = selectedSupplierId,
                                    poDate = formatDateToIso(poDate).orEmpty(),
                                    eta = formatDateToIso(expectedDelivery),
                                    poType = poType,
                                    currency = "INR",
                                    orderStatus = "Approved",
                                    shippingMethod = shippingMethod.trim(),
                                    transportName = transportName.trim(),
                                    vehicleNumber = vehicleNumber.trim(),
                                    trackingNumber = trackingNumber.trim(),
                                    freightTerms = freightTerms.trim(),
                                    internalNotes = additionalNotes.trim(),
                                    remarks = "",
                                    discount = discountVal,
                                    shippingCost = shippingCostVal,
                                    items = itemsPayload,
                                    attachmentUris = selectedFiles,
                                    onSuccess = { po ->
                                        DynamicIslandManager.showSuccess(
                                            "Purchase order ${po.poNumber} created successfully"
                                        )
                                        onCreateOrder()
                                    }
                                )
                            },
                            enabled = !isCreating,
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
    }
}

private fun sanitizeDecimalInput(input: String): String {
    val builder = StringBuilder()
    var hasDecimalPoint = false
    for (ch in input) {
        when {
            ch.isDigit() -> builder.append(ch)
            ch == '.' && !hasDecimalPoint -> {
                if (builder.isEmpty()) builder.append('0')
                builder.append('.')
                hasDecimalPoint = true
            }
        }
    }
    return builder.toString()
}

private fun extractProperty(target: Any?, vararg candidateNames: String): String {
    if (target == null) return ""
    for (name in candidateNames) {
        val raw = getRawProperty(target, name)
        if (raw != null) {
            val str = raw.toString()
            if (str.isNotBlank()) return str
        }
    }
    return ""
}

private fun extractIdOrValue(target: Any?, vararg candidateNames: String): String {
    if (target == null) return ""
    for (name in candidateNames) {
        val value = getRawProperty(target, name) ?: continue
        when (value) {
            is String -> {
                if (value.isNotBlank()) return value
            }
            else -> {
                val idValue = extractProperty(value, "_id", "id", "value")
                if (idValue.isNotBlank()) return idValue

                val nameValue = extractProperty(value, "name", "label")
                if (nameValue.isNotBlank()) return nameValue
            }
        }
    }
    return ""
}

private fun getRawProperty(target: Any?, name: String): Any? {
    if (target == null) return null
    try {
        val getterName = "get" + name.replaceFirstChar { it.uppercase() }
        val method = target.javaClass.methods.firstOrNull {
            it.name.equals(getterName, ignoreCase = true) || it.name.equals(name, ignoreCase = true)
        }
        if (method != null) {
            return method.invoke(target)
        }
    } catch (_: Exception) {}
    try {
        val field = target.javaClass.declaredFields.firstOrNull { it.name.equals(name, ignoreCase = true) }
        field?.isAccessible = true
        return field?.get(target)
    } catch (_: Exception) {}
    return null
}

/**
 * Converts "dd-MM-yyyy" from DatePickerField into ISO-8601 string required by the API.
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