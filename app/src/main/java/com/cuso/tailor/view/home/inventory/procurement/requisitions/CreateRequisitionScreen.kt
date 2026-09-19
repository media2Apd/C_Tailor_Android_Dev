@file:Suppress("unused", "AssignedValueIsNeverRead", "VariableNeverRead")

package com.cuso.tailor.view.home.inventory.procurement.requisitions

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.CreateRequisitionRequest
import com.cuso.tailor.model.inventory.RequisitionItem
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormAccordionCard
import com.cuso.tailor.view.composable.FormActionButtons
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormLineItemsSection
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ImageUploadSection
import com.cuso.tailor.view.composable.LineItemFormEntry
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.DepartmentUiState
import com.cuso.tailor.viewmodel.DepartmentViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CreateRequisitionScreen(
    onClose: () -> Unit,
    editRequisitionId: String? = null,
    onSuccessSubmitted: () -> Unit = {},
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    departmentViewModel: DepartmentViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val isEditMode = !editRequisitionId.isNullOrBlank()

    val supplierList by inventoryViewModel.supplierDropdown.collectAsStateWithLifecycle()
    val warehouseList by inventoryViewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val inventoryItemsList by inventoryViewModel.inventoryItems.collectAsStateWithLifecycle()
    val departmentUiState by departmentViewModel.uiState.collectAsStateWithLifecycle()
    val selectedRequisitionDetail by inventoryViewModel.selectedRequisition.collectAsStateWithLifecycle()

    val departmentList = remember(departmentUiState) {
        (departmentUiState as? DepartmentUiState.Success)?.departments ?: emptyList()
    }

    var isSupplierInfoExpanded by remember { mutableStateOf(true) }
    var isOrderDetailsExpanded by remember { mutableStateOf(true) }
    var isLogisticsExpanded by remember { mutableStateOf(false) }
    var isNotesExpanded by remember { mutableStateOf(false) }
    var isApproverExpanded by remember { mutableStateOf(false) }

    var isSupplierDropdownExpanded by remember { mutableStateOf(false) }
    var isWarehouseDropdownExpanded by remember { mutableStateOf(false) }
    var isDepartmentDropdownExpanded by remember { mutableStateOf(false) }
    var isPriorityDropdownExpanded by remember { mutableStateOf(false) }

    val priorityOptions = remember { listOf("Low", "Medium", "High", "Urgent") }

    var selectedSupplierId by remember { mutableStateOf("") }
    var supplierName by remember { mutableStateOf("") }
    var supplierCode by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var billingAddress by remember { mutableStateOf("") }

    var purchaseType by remember { mutableStateOf("") }
    var selectedWarehouseId by remember { mutableStateOf("") }
    var warehouseName by remember { mutableStateOf("") }
    var selectedDepartmentId by remember { mutableStateOf("") }
    var departmentName by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var budgetCode by remember { mutableStateOf("") }

    var eta by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("INR") }
    var paymentTerms by remember { mutableStateOf("") }

    var approvalStatus by remember { mutableStateOf("Draft") }

    val itemsList = remember { mutableStateListOf(LineItemFormEntry()) }

    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedFiles = selectedFiles + uris
        }
    }

    var shippingMethod by remember { mutableStateOf("") }
    var shippingAddress by remember { mutableStateOf("") }
    var trackingNo by remember { mutableStateOf("") }
    var internalNotes by remember { mutableStateOf("") }

    var requestedByDate by remember { mutableStateOf("") }
    var assignApprover by remember { mutableStateOf("") }

    var discount by remember { mutableStateOf("0") }
    var shippingCost by remember { mutableStateOf("0") }

    LaunchedEffect(Unit) {
        inventoryViewModel.fetchSupplierDropdown()
        inventoryViewModel.loadWarehouseDropdown()
        inventoryViewModel.fetchInventoryItems(page = 1, limit = 100)
        departmentViewModel.loadDepartments()

        if (isEditMode) {
            inventoryViewModel.fetchRequisitionById(editRequisitionId)
        }
    }

    LaunchedEffect(selectedRequisitionDetail) {
        if (isEditMode && selectedRequisitionDetail != null) {
            val req = selectedRequisitionDetail!!

            selectedSupplierId = req.supplierId ?: ""
            supplierName = supplierList.firstOrNull { it.value == req.supplierId }?.label ?: req.supplierId ?: ""

            when (val wh = req.warehouseId) {
                is Map<*, *> -> {
                    selectedWarehouseId = wh["_id"]?.toString() ?: wh["id"]?.toString() ?: ""
                    warehouseName = wh["name"]?.toString() ?: ""
                }
                is String -> {
                    selectedWarehouseId = wh
                    warehouseName = warehouseList.firstOrNull { it.value == wh }?.label ?: wh
                }
                else -> {}
            }

            departmentName = req.department ?: ""
            selectedDepartmentId = departmentList.firstOrNull { it.name == req.department }?._id ?: ""
            priority = req.priority ?: "Medium"
            requestedByDate = req.requiredByDate?.take(10) ?: ""
            budgetCode = req.budgetCode ?: ""
            internalNotes = req.justification ?: ""
            trackingNo = req.internalReference ?: ""
            discount = req.discount?.toString() ?: "0"
            shippingCost = req.shippingCost?.toString() ?: "0"
            approvalStatus = req.approvalStatus ?: "Draft"

            if (req.items.isNotEmpty()) {
                itemsList.clear()
                req.items.forEach { lineItem ->
                    var parsedItemId = ""
                    var parsedItemName = ""
                    when (val iId = lineItem.itemId) {
                        is Map<*, *> -> {
                            parsedItemId = iId["_id"]?.toString() ?: iId["id"]?.toString() ?: ""
                            parsedItemName = iId["name"]?.toString() ?: ""
                        }
                        is String -> {
                            parsedItemId = iId
                            parsedItemName = lineItem.itemDisplayName
                        }
                        else -> {}
                    }

                    itemsList.add(
                        LineItemFormEntry(
                            itemId = parsedItemId,
                            name = parsedItemName.ifBlank { "Item" },
                            type = lineItem.type ?: "Goods",
                            qty = lineItem.qty.toInt().toString(),
                            unit = lineItem.unit ?: "Pieces (Pcs)",
                            rate = lineItem.rate.toString(),
                            taxPercent = lineItem.taxPercent.toString(),
                            total = lineItem.total.toString()
                        )
                    )
                }
            }
        }
    }

    val calculatedSubtotal = itemsList.sumOf { item ->
        val qty = item.qty.toDoubleOrNull() ?: 0.0
        val rate = item.rate.toDoubleOrNull() ?: 0.0
        qty * rate
    }

    val calculatedTax = itemsList.sumOf { item ->
        val qty = item.qty.toDoubleOrNull() ?: 0.0
        val rate = item.rate.toDoubleOrNull() ?: 0.0
        val taxPercent = item.taxPercent.toDoubleOrNull() ?: 0.0
        (qty * rate) * (taxPercent / 100.0)
    }

    val parsedDiscount = discount.toDoubleOrNull() ?: 0.0
    val parsedShipping = shippingCost.toDoubleOrNull() ?: 0.0
    val calculatedGrandTotal = (calculatedSubtotal + calculatedTax - parsedDiscount + parsedShipping).coerceAtLeast(0.0)

    val isPendingOrApproved = approvalStatus.equals("Pending Approval", ignoreCase = true) ||
            approvalStatus.equals("Approved", ignoreCase = true)

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
            ) {
                TitleBar(if (isEditMode) "Edit Purchase Request" else "Create Purchase Request", onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = tokens.screenPadding * 2f),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
        ) {
            // 1. Supplier Information (Full Width Edge-to-Edge)
            item {
                FormAccordionCard(
                    title = "Supplier Information",
                    expanded = isSupplierInfoExpanded,
                    onHeaderClick = { isSupplierInfoExpanded = !isSupplierInfoExpanded }
                ) {
                    FormDropdown(
                        label = "Supplier Name",
                        value = supplierName.ifBlank { "-" },
                        expanded = isSupplierDropdownExpanded,
                        onExpandChange = { isSupplierDropdownExpanded = it },
                        options = supplierList.map { it.label },
                        onOptionSelected = { selectedLabel ->
                            val selected = supplierList.firstOrNull { it.label == selectedLabel }
                            supplierName = selectedLabel
                            selectedSupplierId = selected?.value ?: ""
                            supplierCode = selected?.value ?: ""
                        },
                        isRequired = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Supplier Code")
                            FormTextField(
                                value = supplierCode,
                                onValueChange = { supplierCode = it },
                                placeholder = "-"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Contact Person")
                            FormTextField(
                                value = contactPerson,
                                onValueChange = { contactPerson = it },
                                placeholder = "-"
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    FormLabel("Email Address")
                    FormTextField(
                        value = emailAddress,
                        onValueChange = { emailAddress = it },
                        placeholder = "-",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Phone Number")
                            FormTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                placeholder = "-",
                                keyboardType = KeyboardType.Phone
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Billing Address")
                            FormTextField(
                                value = billingAddress,
                                onValueChange = { billingAddress = it },
                                placeholder = "-"
                            )
                        }
                    }
                }
            }

            // 2. Order Details (Full Width Edge-to-Edge)
            item {
                FormAccordionCard(
                    title = "Order Details",
                    expanded = isOrderDetailsExpanded,
                    onHeaderClick = { isOrderDetailsExpanded = !isOrderDetailsExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Purchase Type")
                            FormTextField(
                                value = purchaseType,
                                onValueChange = { purchaseType = it },
                                placeholder = "-"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Warehouse",
                                value = warehouseName.ifBlank { "-" },
                                expanded = isWarehouseDropdownExpanded,
                                onExpandChange = { isWarehouseDropdownExpanded = it },
                                options = warehouseList.map { it.label },
                                onOptionSelected = { selectedLabel ->
                                    val selected = warehouseList.firstOrNull { it.label == selectedLabel }
                                    warehouseName = selectedLabel
                                    selectedWarehouseId = selected?.value ?: ""
                                },
                                isRequired = true
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Department",
                                value = departmentName.ifBlank { "-" },
                                expanded = isDepartmentDropdownExpanded,
                                onExpandChange = { isDepartmentDropdownExpanded = it },
                                options = departmentList.map { it.name },
                                onOptionSelected = { selectedDeptName ->
                                    val selected = departmentList.firstOrNull { it.name == selectedDeptName }
                                    departmentName = selectedDeptName
                                    selectedDepartmentId = selected?._id ?: ""
                                },
                                isRequired = true
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormDropdown(
                                label = "Priority",
                                value = priority.ifBlank { "Medium" },
                                expanded = isPriorityDropdownExpanded,
                                onExpandChange = { isPriorityDropdownExpanded = it },
                                options = priorityOptions,
                                onOptionSelected = { selectedPriority ->
                                    priority = selectedPriority
                                },
                                isRequired = true
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("ETA")
                            DatePickerField(
                                value = eta,
                                onDateSelected = { eta = it }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Budget Code")
                            FormTextField(
                                value = budgetCode,
                                onValueChange = { budgetCode = it },
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
                            FormLabel("Currency")
                            FormTextField(
                                value = currency,
                                onValueChange = { currency = it },
                                placeholder = "e.g. INR"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Payment Terms")
                            FormTextField(
                                value = paymentTerms,
                                onValueChange = { paymentTerms = it },
                                placeholder = "-"
                            )
                        }
                    }
                }
            }

            // 3. Line Items Section (Padded items container)
            item {
                FormLineItemsSection(
                    itemsList = itemsList,
                    inventoryItemsList = inventoryItemsList,
                    horizontalPadding = true
                )
            }

            // 4. Logistics (Full Width Edge-to-Edge)
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

            // 5. Attachments & Notes (Full Width Edge-to-Edge)
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

            // 6. Approval Settings (Full Width Edge-to-Edge)
            item {
                FormAccordionCard(
                    title = "Approval Settings",
                    expanded = isApproverExpanded,
                    onHeaderClick = { isApproverExpanded = !isApproverExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Requested By Date")
                            DatePickerField(
                                value = requestedByDate,
                                onDateSelected = { requestedByDate = it }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Assign Approver")
                            FormTextField(
                                value = assignApprover,
                                onValueChange = { assignApprover = it },
                                placeholder = "-"
                            )
                        }
                    }
                }
            }

            // 7. Order Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                    colors = CardDefaults.cardColors(containerColor = whiteBg)
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
                                text = String.format(Locale.US, "₹ %,.2f", calculatedSubtotal),
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
                            Text("Estimated Tax", fontSize = tokens.bodySmall, color = mutedText)
                            Text(
                                text = String.format(Locale.US, "₹ %,.2f", calculatedTax),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel("Discount")
                        FormTextField(
                            value = discount,
                            onValueChange = { discount = it },
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Decimal
                        )

                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel("Shipping Cost")
                        FormTextField(
                            value = shippingCost,
                            onValueChange = { shippingCost = it },
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Decimal
                        )

                        Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Grand Total", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                            Text(
                                text = String.format(Locale.US, "₹ %,.2f", calculatedGrandTotal),
                                fontSize = tokens.h2,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }
            }

            // 8. Bottom Action Buttons
            item {
                fun buildRequest(): CreateRequisitionRequest {
                    val parsedItems = itemsList.map { item ->
                        val qtyVal = item.qty.toDoubleOrNull() ?: 1.0
                        val rateVal = item.rate.toDoubleOrNull() ?: 0.0
                        val taxVal = item.taxPercent.toDoubleOrNull() ?: 0.0
                        val itemSubtotal = qtyVal * rateVal
                        val itemTaxAmount = itemSubtotal * (taxVal / 100.0)
                        val itemTotal = itemSubtotal + itemTaxAmount

                        RequisitionItem(
                            itemId = item.itemId.ifBlank { null },
                            qty = qtyVal,
                            unit = item.unit.ifBlank { "-" },
                            type = item.type.ifBlank { "Goods" },
                            rate = rateVal,
                            taxPercent = taxVal,
                            subtotal = itemSubtotal,
                            taxAmount = itemTaxAmount,
                            total = itemTotal
                        )
                    }

                    val effectiveDate = requestedByDate.ifBlank { eta }

                    return CreateRequisitionRequest(
                        supplierId = selectedSupplierId.ifBlank { "" },
                        warehouseId = selectedWarehouseId.ifBlank { "" },
                        department = departmentName.ifBlank { "-" },
                        priority = priority.ifBlank { "Medium" },
                        requiredByDate = formatDateToYyyyMmDd(effectiveDate),
                        justification = internalNotes.ifBlank { "" },
                        items = parsedItems,
                        estimatedSubtotal = calculatedSubtotal,
                        estimatedTax = calculatedTax,
                        estimatedTotal = calculatedGrandTotal,
                        discount = parsedDiscount,
                        shippingCost = parsedShipping,
                        budgetCode = budgetCode.trim().ifBlank { null },
                        internalReference = trackingNo.trim().ifBlank { null }
                    )
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    FormActionButtons(
                        cancelText = "Cancel",
                        draftText = "Save Draft",
                        primaryText = "Send For Approval",
                        onCancel = onClose,
                        onSaveDraft = {
                            val request = buildRequest()
                            if (isEditMode) {
                                inventoryViewModel.updateRequisition(editRequisitionId, request) {
                                    onSuccessSubmitted()
                                }
                            } else {
                                inventoryViewModel.createRequisition(request) {
                                    onSuccessSubmitted()
                                }
                            }
                        },
                        onPrimaryClick = {
                            val request = buildRequest()
                            if (isEditMode) {
                                inventoryViewModel.updateAndSubmitRequisitionForApproval(
                                    editRequisitionId, request
                                ) {
                                    onSuccessSubmitted()
                                }
                            } else {
                                inventoryViewModel.createAndSubmitRequisitionForApproval(request) {
                                    onSuccessSubmitted()
                                }
                            }
                        },
                        showPrimaryButton = !isPendingOrApproved
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

private fun formatDateToYyyyMmDd(rawDate: String): String {
    if (rawDate.isBlank()) return ""
    val inputPatterns = listOf("yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd", "dd MMM yyyy")
    val targetFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    for (pattern in inputPatterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
            val parsedDate = sdf.parse(rawDate.trim())
            if (parsedDate != null) {
                return targetFormat.format(parsedDate)
            }
        } catch (_: Exception) { }
    }
    return rawDate.trim()
}