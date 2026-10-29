@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.finance.purchase

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.PurchaseReturnItemPayload
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.CreatePurchaseReturnState
import com.cuso.tailor.viewmodel.FinanceViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Immutable UI state holder for return line items
data class ReturnRowState(
    val receiveItemId: String,
    val itemId: String,
    val name: String,
    val sku: String,
    val unit: String,
    val qtyReceived: Double,
    val availableQty: Double,
    val rate: Double,
    val taxPercent: Double,
    val isSerialTracked: Boolean,
    val availableSerials: List<String>,
    val returnQty: String = "0",
    val selectedSerials: Set<String> = emptySet(),
    val reason: String = ""
)

@Composable
fun CreatePurchaseReturnScreen(
    financeViewModel: FinanceViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onCreateReturn: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Observe data sources
    val suppliers by inventoryViewModel.suppliers.collectAsState()
    val purchaseOrders by inventoryViewModel.purchaseOrdersList.collectAsState()
    val grnReceives by financeViewModel.grnReceivesList.collectAsState()
    val returnableData by financeViewModel.returnableGrnData.collectAsState()
    val isLoadingReturnable by financeViewModel.isLoadingReturnableItems.collectAsState()
    val createReturnState by financeViewModel.createPurchaseReturnState.collectAsState()

    // Form Dropdown Selections
    var selectedSupplierId by remember { mutableStateOf("") }
    var selectedSupplierLabel by remember { mutableStateOf("") }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }

    var selectedPoId by remember { mutableStateOf("") }
    var selectedPoLabel by remember { mutableStateOf("") }
    var poDropdownExpanded by remember { mutableStateOf(false) }

    var selectedGrnId by remember { mutableStateOf("") }
    var selectedGrnLabel by remember { mutableStateOf("") }
    var grnDropdownExpanded by remember { mutableStateOf(false) }

    val todayFormatted = remember { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()) }
    var returnDate by remember { mutableStateOf(todayFormatted) }

    var returnReason by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    // Uploaded Evidence Files
    val attachedFiles = remember { mutableStateListOf<Uri>() }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        attachedFiles.addAll(uris)
    }

    // Editable Return Item Rows
    val returnItemRows = remember { mutableStateListOf<ReturnRowState>() }

    // Handle Create Purchase Return State Updates
    LaunchedEffect(createReturnState) {
        when (createReturnState) {
            is CreatePurchaseReturnState.Success -> {
                val msg = (createReturnState as CreatePurchaseReturnState.Success).message
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                financeViewModel.resetCreatePurchaseReturnState()
                onCreateReturn()
            }
            is CreatePurchaseReturnState.Error -> {
                val err = (createReturnState as CreatePurchaseReturnState.Error).message
                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                financeViewModel.resetCreatePurchaseReturnState()
            }
            else -> Unit
        }
    }

    // 1. Initial Load: Fetch Suppliers
    LaunchedEffect(Unit) {
        inventoryViewModel.fetchSuppliers(page = 1, limit = 500)
    }

    // 2. Supplier Selected -> Fetch Purchase Orders for that Supplier
    LaunchedEffect(selectedSupplierId) {
        if (selectedSupplierId.isNotBlank()) {
            selectedPoId = ""
            selectedPoLabel = ""
            selectedGrnId = ""
            selectedGrnLabel = ""
            returnItemRows.clear()
            financeViewModel.clearReturnCreationStates()
            inventoryViewModel.fetchAllPurchaseOrders(supplierId = selectedSupplierId)
        }
    }

    // 3. Purchase Order Selected -> Fetch GRN / Receives for that PO
    LaunchedEffect(selectedPoId) {
        if (selectedPoId.isNotBlank()) {
            selectedGrnId = ""
            selectedGrnLabel = ""
            returnItemRows.clear()
            financeViewModel.fetchReceivesByPo(poId = selectedPoId)
        }
    }

    // 4. GRN Selected -> Fetch Returnable Items
    LaunchedEffect(selectedGrnId) {
        if (selectedGrnId.isNotBlank()) {
            returnItemRows.clear()
            financeViewModel.fetchReturnableItemsByReceiveId(receiveId = selectedGrnId)
        }
    }

    // Populate editable rows when returnable items API responds
    LaunchedEffect(returnableData) {
        returnItemRows.clear()
        returnableData?.items?.forEach { item ->
            returnItemRows.add(
                ReturnRowState(
                    receiveItemId = item.receiveItemId,
                    itemId = item.itemId,
                    name = item.name,
                    sku = item.sku,
                    unit = item.unit.ifBlank { "Meters" },
                    qtyReceived = item.qtyReceived,
                    availableQty = item.availableQty,
                    rate = item.rate,
                    taxPercent = item.taxPercent,
                    isSerialTracked = item.isSerialTracked,
                    availableSerials = item.availableSerials,
                    returnQty = "0",
                    selectedSerials = emptySet(),
                    reason = ""
                )
            )
        }
    }

    // Calculation Totals
    val totalQty = returnItemRows.sumOf { it.returnQty.toDoubleOrNull() ?: 0.0 }
    val subtotal = returnItemRows.sumOf {
        val q = it.returnQty.toDoubleOrNull() ?: 0.0
        q * it.rate
    }
    val taxTotal = returnItemRows.sumOf {
        val q = it.returnQty.toDoubleOrNull() ?: 0.0
        (q * it.rate) * (it.taxPercent / 100.0)
    }
    val grandTotal = subtotal + taxTotal

    val isSubmitting = createReturnState is CreatePurchaseReturnState.Loading

    // Submission Handler matching exact multipart payload
    val onSubmitReturn: () -> Unit = {
        // Format returnDate to yyyy-MM-dd
        val parsedDate = try {
            val srcFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val targetFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            srcFormat.parse(returnDate)?.let { targetFormat.format(it) } ?: returnDate
        } catch (e: Exception) {
            returnDate
        }

        // Filter and map only items with return quantity > 0
        val payloadItems = returnItemRows
            .filter { (it.returnQty.toDoubleOrNull() ?: 0.0) > 0.0 }
            .map { row ->
                val rowQty = row.returnQty.toDoubleOrNull() ?: 0.0
                PurchaseReturnItemPayload(
                    receiveItemId = row.receiveItemId,
                    qty = if (rowQty % 1.0 == 0.0) rowQty.toInt().toDouble() else rowQty,
                    reason = row.reason.ifBlank { returnReason.trim() },
                    serialNumbers = if (row.isSerialTracked) row.selectedSerials.toList() else emptyList()
                )
            }

        val itemsJson = Gson().toJson(payloadItems)

        // Convert attached Uris to multipart parts
        val attachmentParts = attachedFiles.mapNotNull { uri ->
            uriToMultipartPart(context, uri, "attachments")
        }

        financeViewModel.createPurchaseReturn(
            poId = selectedPoId,
            receiveId = selectedGrnId,
            returnDate = parsedDate,
            reason = returnReason.trim(),
            itemsJson = itemsJson,
            attachmentParts = attachmentParts
        )
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = "Create Return Request",
                onClose = onBack
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Primary_background,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        border = BorderStroke(1.dp, sectionBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onSubmitReturn,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                        enabled = !isSubmitting && selectedSupplierId.isNotBlank() && selectedPoId.isNotBlank() && selectedGrnId.isNotBlank() && totalQty > 0 && returnReason.isNotBlank(),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(tokens.buttonHeight)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = whiteBg,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text("Create Return", fontSize = tokens.bodySmall, color = whiteBg, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // Top Notice
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = iconMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "RMA number is generated on save",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "Record items to be returned to a supplier against a goods receipt.",
                fontSize = tokens.caption,
                color = iconMuted
            )

            // ─────────────────────────────────────────────────────────────
            // 1. RETURN INFORMATION
            // ─────────────────────────────────────────────────────────────
            NumberedCard(stepNumber = "1", title = "Return Information") {
                val supplierOptions = remember(suppliers) {
                    suppliers.map { supplier ->
                        val sName = supplier.name.ifBlank { "Supplier" }
                        val sCode = supplier.supplierCode
                        if (sCode.isNotBlank()) "$sName ($sCode)" else sName
                    }
                }

                FormDropdown(
                    label = "Supplier",
                    isRequired = true,
                    value = selectedSupplierLabel.ifBlank { "Select supplier" },
                    expanded = supplierDropdownExpanded,
                    onExpandChange = { supplierDropdownExpanded = it },
                    options = supplierOptions,
                    onOptionSelected = { chosen ->
                        selectedSupplierLabel = chosen
                        val matched = suppliers.find {
                            val label = if (it.supplierCode.isNotBlank()) "${it.name} (${it.supplierCode})" else it.name
                            label == chosen
                        }
                        selectedSupplierId = matched?.id.orEmpty()
                    }
                )

                Spacer(Modifier.height(tokens.extraPadding))

                val poOptions = remember(purchaseOrders) {
                    purchaseOrders.mapNotNull { it.poNumber?.takeIf { no -> no.isNotBlank() } }
                }

                FormDropdown(
                    label = "Original Purchase Order",
                    isRequired = true,
                    value = selectedPoLabel.ifBlank { if (selectedSupplierId.isBlank()) "Select supplier first" else "Select purchase order" },
                    expanded = poDropdownExpanded,
                    onExpandChange = { if (selectedSupplierId.isNotBlank()) poDropdownExpanded = it },
                    options = poOptions,
                    onOptionSelected = { chosenPoNum ->
                        selectedPoLabel = chosenPoNum
                        val matchedPo = purchaseOrders.find { it.poNumber == chosenPoNum }
                        selectedPoId = matchedPo?.id.orEmpty()
                    }
                )

                Spacer(Modifier.height(tokens.extraPadding))

                val grnOptions = remember(grnReceives) {
                    grnReceives.mapNotNull { grn ->
                        val rNumber = grn.receiveNumber.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                        val rDate = grn.receiveDate.take(10)
                        if (rDate.isNotBlank()) "$rNumber • Recv: $rDate" else rNumber
                    }
                }

                FormDropdown(
                    label = "Goods Receipt Note (GRN)",
                    isRequired = true,
                    value = selectedGrnLabel.ifBlank { if (selectedPoId.isBlank()) "Select purchase order first" else "Select GRN" },
                    expanded = grnDropdownExpanded,
                    onExpandChange = { if (selectedPoId.isNotBlank()) grnDropdownExpanded = it },
                    options = grnOptions,
                    onOptionSelected = { chosenGrn ->
                        selectedGrnLabel = chosenGrn
                        val matchedGrn = grnReceives.find {
                            val label = if (it.receiveDate.isNotBlank()) "${it.receiveNumber} • Recv: ${it.receiveDate.take(10)}" else it.receiveNumber
                            label == chosenGrn
                        }
                        selectedGrnId = matchedGrn?.id.orEmpty()
                    }
                )

                Spacer(Modifier.height(tokens.extraPadding))

                Column {
                    FormLabel("Return Date", isRequired = true)
                    Spacer(Modifier.height(4.dp))
                    DatePickerField(
                        value = returnDate,
                        onDateSelected = { returnDate = it }
                    )
                }
            }

            // ─────────────────────────────────────────────────────────────
            // 2. RETURN REASON
            // ─────────────────────────────────────────────────────────────
            NumberedCard(stepNumber = "2", title = "Return Reason") {
                FormLabel("Reason for Return", isRequired = true)
                Spacer(Modifier.height(4.dp))
                FormTextArea(
                    value = returnReason,
                    onValueChange = { returnReason = it },
                    placeholder = "Describe the defect / reason for returning these items..."
                )

                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Used as the default reason for each item line.",
                    fontSize = tokens.label,
                    color = iconMuted
                )

                Spacer(Modifier.height(tokens.extraPadding))

                FormLabel("Remarks")
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = sectionBorder
                    ),
                    placeholder = {
                        Text(
                            text = "Any additional notes for the supplier (optional)",
                            fontSize = tokens.caption,
                            color = iconMuted
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(fontSize = tokens.bodySmall)
                )
            }

            // ─────────────────────────────────────────────────────────────
            // 3. ITEMS FOR RETURN (FIELDS FROM DESKTOP DESIGN)
            // ─────────────────────────────────────────────────────────────
            NumberedCard(
                stepNumber = "3",
                title = "Items for Return"
            ) {
                if (isLoadingReturnable) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
                    }
                } else if (returnItemRows.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = tokens.extraPadding * 2),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select a GRN to load its received items.",
                            fontSize = tokens.caption,
                            color = iconMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    returnItemRows.forEachIndexed { index, rowItem ->
                        val currentItemQty = rowItem.returnQty.toDoubleOrNull() ?: 0.0
                        val itemReturnValue = currentItemQty * rowItem.rate

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                            border = BorderStroke(1.dp, sectionBorder),
                            color = whiteBg
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                // ── Item Header with Serial Badge and Remove Action ──
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = rowItem.name,
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )

                                        if (rowItem.isSerialTracked) {
                                            Spacer(Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFFF1F5F9),
                                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                            ) {
                                                Text(
                                                    text = "Serial",
                                                    fontSize = tokens.label,
                                                    color = iconMuted,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    IconButton(
                                        onClick = { returnItemRows.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Remove Item",
                                            tint = iconMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // ── SKU, Received, Returnable ──
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("SKU", fontSize = tokens.label, color = iconMuted)
                                        Text(
                                            text = rowItem.sku.ifBlank { "—" },
                                            fontSize = tokens.caption,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("RECEIVED", fontSize = tokens.label, color = iconMuted)
                                        Text(
                                            text = "${formatDouble(rowItem.qtyReceived)} ${rowItem.unit}",
                                            fontSize = tokens.caption,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("RETURNABLE", fontSize = tokens.label, color = iconMuted)
                                        Text(
                                            text = "${formatDouble(rowItem.availableQty)} ${rowItem.unit}",
                                            fontSize = tokens.caption,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // ── Financials Row: Unit Cost, Tax, Return Value ──
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFEDF2F7))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("UNIT COST", fontSize = tokens.label, color = iconMuted)
                                            Text(
                                                text = "${currencySymbol()}${formatDouble(rowItem.rate)}",
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("TAX", fontSize = tokens.label, color = iconMuted)
                                            Text(
                                                text = "${formatDouble(rowItem.taxPercent)}%",
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("RETURN VALUE", fontSize = tokens.label, color = iconMuted)
                                            Text(
                                                text = "${currencySymbol()}${formatDouble(itemReturnValue)}",
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Primary
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(10.dp))

                                // ── Return Qty & Reason Fields ──
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Return Qty Input (Disabled when returnable quantity is 0)
                                    val isQtyEditable = rowItem.availableQty > 0
                                    Column(modifier = Modifier.width(110.dp)) {
                                        Text("RETURN QTY", fontSize = tokens.label, color = iconMuted)
                                        Spacer(Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isQtyEditable) Color(0xFFCBD5E1) else Color(0xFFE2E8F0)
                                            ),
                                            color = if (isQtyEditable) whiteBg else Color(0xFFF1F5F9)
                                        ) {
                                            BasicTextField(
                                                value = if (isQtyEditable) rowItem.returnQty else "0",
                                                onValueChange = { input ->
                                                    val sanitized = input.filter { it.isDigit() || it == '.' }
                                                    val num = sanitized.toDoubleOrNull() ?: 0.0
                                                    if (num <= rowItem.availableQty) {
                                                        val syncedSerials = if (rowItem.isSerialTracked) {
                                                            rowItem.availableSerials.take(num.toInt()).toSet()
                                                        } else {
                                                            rowItem.selectedSerials
                                                        }
                                                        returnItemRows[index] = rowItem.copy(
                                                            returnQty = sanitized,
                                                            selectedSerials = syncedSerials
                                                        )
                                                    }
                                                },
                                                enabled = isQtyEditable,
                                                textStyle = TextStyle(
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isQtyEditable) TextPrimary else iconMuted
                                                ),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                            )
                                        }
                                    }

                                    // Reason Input
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("REASON", fontSize = tokens.label, color = iconMuted)
                                        Spacer(Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                            color = whiteBg
                                        ) {
                                            BasicTextField(
                                                value = rowItem.reason,
                                                onValueChange = { newReason ->
                                                    returnItemRows[index] = rowItem.copy(reason = newReason)
                                                },
                                                textStyle = TextStyle(
                                                    fontSize = tokens.caption,
                                                    color = TextPrimary
                                                ),
                                                singleLine = true,
                                                decorationBox = { innerTextField ->
                                                    if (rowItem.reason.isEmpty()) {
                                                        Text("Optional", fontSize = tokens.caption, color = iconMuted)
                                                    }
                                                    innerTextField()
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }

                                // ── Selectable Serial Numbers Sub-Section ──
                                if (rowItem.isSerialTracked && rowItem.availableSerials.isNotEmpty()) {
                                    Spacer(Modifier.height(14.dp))
                                    val maxSelectable = rowItem.availableQty.toInt().coerceAtMost(rowItem.availableSerials.size)
                                    val isAllSelected = rowItem.selectedSerials.size >= maxSelectable && maxSelectable > 0

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Select serial numbers to return (${rowItem.selectedSerials.size}/$maxSelectable)",
                                            fontSize = tokens.caption,
                                            color = iconMuted
                                        )

                                        Text(
                                            text = if (isAllSelected) "Deselect all" else "Select all",
                                            fontSize = tokens.caption,
                                            color = Primary,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.clickable {
                                                val updatedSerials = if (isAllSelected) {
                                                    emptySet()
                                                } else {
                                                    rowItem.availableSerials.take(maxSelectable).toSet()
                                                }
                                                returnItemRows[index] = rowItem.copy(
                                                    selectedSerials = updatedSerials,
                                                    returnQty = updatedSerials.size.toString()
                                                )
                                            }
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    // Display serial items in 2-column paired rows
                                    val serialPairs = remember(rowItem.availableSerials) {
                                        rowItem.availableSerials.chunked(2)
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        serialPairs.forEach { pair ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                pair.forEach { serial ->
                                                    val isSelected = rowItem.selectedSerials.contains(serial)
                                                    Surface(
                                                        onClick = {
                                                            val currentSelected = rowItem.selectedSerials
                                                            val newSelected = if (isSelected) {
                                                                currentSelected - serial
                                                            } else {
                                                                if (currentSelected.size < rowItem.availableQty) {
                                                                    currentSelected + serial
                                                                } else {
                                                                    currentSelected
                                                                }
                                                            }
                                                            returnItemRows[index] = rowItem.copy(
                                                                selectedSerials = newSelected,
                                                                returnQty = newSelected.size.toString()
                                                            )
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(50),
                                                        color = if (isSelected) Color(0xFFEFF6FF) else whiteBg,
                                                        border = BorderStroke(
                                                            1.dp,
                                                            if (isSelected) Primary else Color(0xFFE2E8F0)
                                                        )
                                                    ) {
                                                        Text(
                                                            text = serial,
                                                            fontSize = tokens.caption,
                                                            color = if (isSelected) Primary else TextPrimary,
                                                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                                            textAlign = TextAlign.Center,
                                                            maxLines = 1,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 8.dp, vertical = 9.dp)
                                                        )
                                                    }
                                                }
                                                if (pair.size == 1) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Valuation uses the purchase rate from the GRN. Returnable qty already excludes quantities returned earlier.",
                        fontSize = tokens.label,
                        color = iconMuted,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ─────────────────────────────────────────────────────────────
            // 4. SUPPORTING EVIDENCE & ATTACHMENTS
            // ─────────────────────────────────────────────────────────────
            NumberedCard(
                stepNumber = "4",
                title = "Supporting Evidence & Attachments",
                headerBadge = "${attachedFiles.size} attached"
            ) {
                ImageUploadSection(
                    selectedImages = attachedFiles.toList(),
                    onBrowseClick = { filePicker.launch("*/*") },
                    onRemoveImage = { uri -> attachedFiles.remove(uri) },
                    maxFiles = 5,
                    title = "Upload Inspection Evidence",
                    subtitle = "Attach inspection photos, damaged-item proof, or supplier correspondence.",
                    supportedFormatsText = "Supports JPG, PNG, PDF · Max 5 files (Images auto-compressed to <10MB)",
                    browseText = "Browse files"
                )
            }

            // ─────────────────────────────────────────────────────────────
            // 5. RETURN SUMMARY
            // ─────────────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(modifier = Modifier.padding(tokens.extraPadding)) {
                    Text(
                        text = "Return Summary",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                    SummaryRow("Supplier", selectedSupplierLabel.substringBefore(" (").ifBlank { "—" })
                    SummaryRow("Purchase Order", selectedPoLabel.ifBlank { "—" })
                    SummaryRow("Goods Receipt", selectedGrnLabel.substringBefore(" •").ifBlank { "—" })
                    SummaryRow("Items Count", if (returnItemRows.isNotEmpty()) "${returnItemRows.size}" else "—")
                    SummaryRow("Total Return Qty", if (totalQty > 0) formatDouble(totalQty) else "—")

                    HorizontalDivider(color = sectionBorder, modifier = Modifier.padding(vertical = tokens.extraPadding * 0.6f))

                    SummaryRow("Item Return Subtotal", "${currencySymbol()}${formatDouble(subtotal)}")
                    SummaryRow("Tax", "${currencySymbol()}${formatDouble(taxTotal)}")

                    Spacer(Modifier.height(tokens.extraPadding * 0.6f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Return Total", fontSize = tokens.bodySmall, color = TextPrimary)
                        Text("${currencySymbol()}${formatDouble(grandTotal)}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = Primary)
                    }
                }
            }

            // ─────────────────────────────────────────────────────────────
            // 6. READINESS CHECKLIST
            // ─────────────────────────────────────────────────────────────
            val isStep1 = selectedSupplierId.isNotBlank()
            val isStep2 = selectedPoId.isNotBlank() && selectedGrnId.isNotBlank()
            val isStep3 = totalQty > 0
            val isStep4 = returnReason.isNotBlank()
            val completedChecks = listOf(isStep1, isStep2, isStep3, isStep4).count { it }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(modifier = Modifier.padding(tokens.extraPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Readiness Checklist",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF1F5F9)) {
                            Text(
                                text = "$completedChecks of 4",
                                fontSize = tokens.label,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                    ChecklistBulletItem(
                        title = "Supplier selected",
                        subtitle = selectedSupplierLabel.substringBefore(" (").ifBlank { null },
                        isChecked = isStep1
                    )

                    ChecklistBulletItem(
                        title = "PO & GRN linked",
                        subtitle = if (isStep2) "$selectedPoLabel & ${selectedGrnLabel.substringBefore(" •")}" else null,
                        isChecked = isStep2
                    )

                    ChecklistBulletItem(
                        title = "Return quantities entered",
                        subtitle = if (isStep3) "${formatDouble(totalQty)} items marked for return" else null,
                        isChecked = isStep3
                    )

                    ChecklistBulletItem(
                        title = "Return reason documented",
                        isChecked = isStep4
                    )
                }
            }

            Spacer(Modifier.height(tokens.screenPadding * 2))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE HELPER COMPOSABLES & UTILITIES
// ─────────────────────────────────────────────────────────────

@Composable
private fun NumberedCard(
    stepNumber: String,
    title: String,
    headerBadge: String? = null,
    actionButtonText: String? = null,
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFEEF2FF),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(stepNumber, fontSize = tokens.label, color = Primary, fontWeight = FontWeight.Medium)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                }

                if (actionButtonText != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFEEF2FF),
                        modifier = Modifier.clickable { }
                    ) {
                        Text(
                            text = actionButtonText,
                            fontSize = tokens.label,
                            color = Primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else if (headerBadge != null) {
                    Text(headerBadge, fontSize = tokens.label, color = iconMuted)
                }
            }

            HorizontalDivider(color = sectionBorder, modifier = Modifier.padding(vertical = tokens.extraPadding * 0.7f))
            content()
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.caption, color = iconMuted)
        Text(value, fontSize = tokens.caption, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ChecklistBulletItem(
    title: String,
    subtitle: String? = null,
    isChecked: Boolean
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (isChecked) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = darkGreenBg,
                modifier = Modifier.size(15.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(13.dp)
                    .border(1.5.dp, Color(0xFFCBD5E1), CircleShape)
            )
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                fontSize = tokens.caption,
                color = if (isChecked) TextPrimary else iconMuted,
                fontWeight = if (isChecked) FontWeight.Medium else FontWeight.Normal
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    fontSize = tokens.label,
                    color = iconMuted
                )
            }
        }
    }
}

private fun formatDouble(value: Double): String {
    return if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%,.0f", value)
    } else {
        String.format(Locale.US, "%,.2f", value)
    }
}

// Convert Android Uri into OkHttp MultipartBody.Part for file upload
private fun uriToMultipartPart(context: Context, uri: Uri, partName: String): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        var fileName = "attachment_${System.currentTimeMillis()}"

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex) ?: fileName
            }
        }

        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        MultipartBody.Part.createFormData(partName, fileName, requestBody)
    } catch (e: Exception) {
        null
    }
}