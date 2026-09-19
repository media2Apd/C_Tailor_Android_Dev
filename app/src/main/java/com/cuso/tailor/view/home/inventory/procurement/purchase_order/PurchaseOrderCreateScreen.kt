@file:Suppress("SpellCheckingInspection", "unused", "AssignedValueIsNeverRead", "VariableNeverRead")

package com.cuso.tailor.view.home.inventory.procurement.purchase_order

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.CreateRequisitionRequest
import com.cuso.tailor.model.inventory.ItemRefDetail
import com.cuso.tailor.model.inventory.PurchaseOrder
import com.cuso.tailor.model.inventory.PurchaseOrderItem
import com.cuso.tailor.model.inventory.RequisitionItem
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.dividerColor
import com.cuso.tailor.ui.theme.title_border
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
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun POCreateScreen(
    viewModel: InventoryViewModel,
    poId: String? = null,
    existingPo: PurchaseOrder? = null,
    onDismiss: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val targetPoId = poId?.takeIf { it.isNotBlank() } ?: existingPo?.id?.takeIf { it.isNotBlank() }
    val isEditMode = !targetPoId.isNullOrBlank()

    val orderDetail by viewModel.purchaseOrderDetail.collectAsStateWithLifecycle()
    val isLoadingPODetail by viewModel.isLoadingPODetail.collectAsStateWithLifecycle()
    val poDetailError by viewModel.poDetailError.collectAsStateWithLifecycle()

    val isSubmittingRequisition by viewModel.isSubmittingRequisition.collectAsStateWithLifecycle()
    val isSubmittingPO by viewModel.isSubmittingPO.collectAsStateWithLifecycle()
    val isBusy = isSubmittingRequisition || isSubmittingPO

    val poSuccessMessage by viewModel.poSuccessMessage.collectAsStateWithLifecycle()
    val poErrorMessage by viewModel.purchaseOrdersError.collectAsStateWithLifecycle()
    val requisitionSuccessMessage by viewModel.requisitionSuccessMessage.collectAsStateWithLifecycle()
    val requisitionErrorMessage by viewModel.requisitionErrorMessage.collectAsStateWithLifecycle()

    val warehouseList by viewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val supplierList by viewModel.supplierDropdown.collectAsStateWithLifecycle()
    val inventoryItemsList by viewModel.inventoryItems.collectAsStateWithLifecycle()

    // Fetch dropdowns and PO View-One detail if in edit mode
    LaunchedEffect(targetPoId) {
        viewModel.loadWarehouseDropdown()
        viewModel.fetchSupplierDropdown()
        viewModel.fetchInventoryItems(page = 1, limit = 100)

        if (!targetPoId.isNullOrBlank()) {
            viewModel.fetchPurchaseOrderDetail(targetPoId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearPurchaseOrderDetail()
        }
    }

    LaunchedEffect(poSuccessMessage) {
        poSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearPOAlerts()
            onDismiss()
        }
    }

    LaunchedEffect(poErrorMessage) {
        poErrorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearPOAlerts()
        }
    }

    LaunchedEffect(poDetailError) {
        poDetailError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

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

    // Form fields state
    var selectedWarehouseId by remember { mutableStateOf("") }
    var warehouseName by remember { mutableStateOf("") }
    var warehouseExpanded by remember { mutableStateOf(false) }

    var poDate by remember { mutableStateOf("") }
    var eta by remember { mutableStateOf("") }
    var poType by remember { mutableStateOf("Standard") }
    var poTypeExpanded by remember { mutableStateOf(false) }
    var refPrLink by remember { mutableStateOf("") }

    var selectedSupplierId by remember { mutableStateOf("") }
    var supplierName by remember { mutableStateOf("") }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }
    var supplierCode by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var gstNumber by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("") }
    var creditPeriod by remember { mutableStateOf("") }

    var shippingMethod by remember { mutableStateOf("") }
    var shippingAddress by remember { mutableStateOf("") }
    var trackingNo by remember { mutableStateOf("") }
    var internalNotes by remember { mutableStateOf("") }

    var selectedFiles by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedFiles = selectedFiles + uris
        }
    }

    var isPoDetailsExpanded by remember { mutableStateOf(true) }
    var isSupplierExpanded by remember { mutableStateOf(true) }
    var isLogisticsExpanded by remember { mutableStateOf(true) }
    var isNotesExpanded by remember { mutableStateOf(true) }

    val itemsList = remember { mutableStateListOf(LineItemFormEntry()) }
    var isPrefilled by remember { mutableStateOf(false) }

    // Prefill from fetched PO View-One Detail
    LaunchedEffect(orderDetail) {
        val detail = orderDetail
        if (detail != null && !isPrefilled) {
            isPrefilled = true

            // Warehouse
            selectedWarehouseId = detail.warehouse?.id.orEmpty()
            warehouseName = detail.warehouse?.name.orEmpty()

            // PO General Info
            poDate = detail.poDate?.substringBefore("T").orEmpty()
            eta = detail.eta?.substringBefore("T").orEmpty()
            poType = detail.poType?.ifBlank { "Standard" } ?: "Standard"
            refPrLink = detail.requisitionId.orEmpty()

            // Supplier Information
            selectedSupplierId = detail.supplier?.id.orEmpty()
            supplierName = detail.supplier?.name.orEmpty()
            supplierCode = detail.supplier?.supplierCode.orEmpty()
            contactPerson = detail.supplier?.contact?.contactName.orEmpty()
            gstNumber = detail.supplier?.tax?.gstNumber.orEmpty()

            // Logistics
            shippingMethod = detail.shippingMethod.orEmpty()
            val whAddr = detail.warehouse?.address
            shippingAddress = listOfNotNull(
                whAddr?.address,
                whAddr?.city,
                whAddr?.state,
                whAddr?.pincode
            ).filter { it.isNotBlank() }.joinToString(", ")
            trackingNo = detail.trackingNumber.orEmpty()

            // Notes
            internalNotes = detail.internalNotes.orEmpty()

            // Line Items
            val fetchedItems = detail.items
            if (!fetchedItems.isNullOrEmpty()) {
                itemsList.clear()
                fetchedItems.forEach { poItem ->
                    val qtyVal = poItem.qty ?: 1.0
                    val qtyStr = if (qtyVal % 1.0 == 0.0) qtyVal.toLong().toString() else qtyVal.toString()
                    itemsList.add(
                        LineItemFormEntry(
                            itemId = poItem.itemRef?.id.orEmpty(),
                            name = poItem.itemRef?.name ?: "Item",
                            unit = poItem.itemRef?.unit ?: "pcs",
                            type = poItem.itemRef?.type ?: "Goods",
                            qty = qtyStr,
                            rate = poItem.rate?.toString() ?: "0",
                            taxPercent = poItem.taxPercent?.toString() ?: "0",
                            total = poItem.total?.toString() ?: "0"
                        )
                    )
                }
            }
        }
    }

    // Fallback prefill from existingPo if view-one response has not loaded
    LaunchedEffect(existingPo, warehouseList, supplierList) {
        if (existingPo != null && orderDetail == null && !isPrefilled) {
            val whId = when (val wh = existingPo.warehouseId) {
                is Map<*, *> -> wh["_id"]?.toString() ?: wh["id"]?.toString() ?: ""
                is String -> wh
                else -> ""
            }
            selectedWarehouseId = whId
            warehouseName = warehouseList.firstOrNull { it.value == whId }?.label ?: whId

            val supId = when (val sup = existingPo.supplierId) {
                is Map<*, *> -> sup["_id"]?.toString() ?: sup["id"]?.toString() ?: ""
                is String -> sup
                else -> ""
            }
            selectedSupplierId = supId
            supplierName = supplierList.firstOrNull { it.value == supId }?.label ?: supId

            poType = existingPo.poType.ifBlank { "Standard" }
            poDate = existingPo.poDate?.substringBefore("T").orEmpty()
            eta = existingPo.eta?.substringBefore("T").orEmpty()
            refPrLink = existingPo.requisitionId.orEmpty()

            if (existingPo.items.isNotEmpty()) {
                itemsList.clear()
                existingPo.items.forEach { poItem ->
                    val resolvedId = when (val itId = poItem.itemId) {
                        is ItemRefDetail -> itId.id
                        is Map<*, *> -> itId["_id"]?.toString() ?: itId["id"]?.toString() ?: ""
                        is String -> itId
                        else -> ""
                    }
                    val resolvedName = when (val itId = poItem.itemId) {
                        is ItemRefDetail -> itId.name
                        is Map<*, *> -> itId["name"]?.toString() ?: "Item"
                        else -> "Item"
                    }
                    val resolvedUnit = when (val itId = poItem.itemId) {
                        is ItemRefDetail -> itId.unit ?: "pcs"
                        is Map<*, *> -> itId["unit"]?.toString() ?: "pcs"
                        else -> "pcs"
                    }

                    itemsList.add(
                        LineItemFormEntry(
                            itemId = resolvedId,
                            name = resolvedName,
                            unit = resolvedUnit,
                            type = "Goods",
                            qty = poItem.qty.toInt().toString(),
                            rate = poItem.rate.toString(),
                            taxPercent = poItem.taxPercent.toString(),
                            total = poItem.total.toString()
                        )
                    )
                }
            }
        }
    }

    val calculatedSubtotal = itemsList.sumOf { item ->
        val q = item.qty.toDoubleOrNull() ?: 0.0
        val r = item.rate.toDoubleOrNull() ?: 0.0
        q * r
    }

    val calculatedTax = itemsList.sumOf { item ->
        val q = item.qty.toDoubleOrNull() ?: 0.0
        val r = item.rate.toDoubleOrNull() ?: 0.0
        val t = item.taxPercent.toDoubleOrNull() ?: 0.0
        (q * r) * (t / 100.0)
    }

    val calculatedGrandTotal = calculatedSubtotal + calculatedTax

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
                    title = if (isEditMode) "Update Purchase Order" else "Create Purchase Order",
                    onClose = onDismiss
                )
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        if (isEditMode && isLoadingPODetail && orderDetail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                ListSkeleton()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
            ) {
                // Section 1: PO Details
                FormAccordionCard(
                    title = "PO Details",
                    expanded = isPoDetailsExpanded,
                    onHeaderClick = { isPoDetailsExpanded = !isPoDetailsExpanded }
                ) {
                    FormDropdown(
                        label = "Warehouse",
                        value = warehouseName.ifBlank { "-" },
                        expanded = warehouseExpanded,
                        onExpandChange = { warehouseExpanded = it },
                        options = warehouseList.map { it.label },
                        onOptionSelected = { selectedLabel ->
                            val selected = warehouseList.firstOrNull { it.label == selectedLabel }
                            warehouseName = selectedLabel
                            selectedWarehouseId = selected?.value ?: ""
                        },
                        isRequired = true
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
                    FormTextField(
                        value = refPrLink,
                        onValueChange = { refPrLink = it },
                        placeholder = "-"
                    )
                }

                // Section 2: Supplier Information
                FormAccordionCard(
                    title = "Supplier Information",
                    expanded = isSupplierExpanded,
                    onHeaderClick = { isSupplierExpanded = !isSupplierExpanded }
                ) {
                    FormDropdown(
                        label = "Supplier Name",
                        value = supplierName.ifBlank { "-" },
                        expanded = supplierDropdownExpanded,
                        onExpandChange = { supplierDropdownExpanded = it },
                        options = supplierList.map { it.label },
                        onOptionSelected = { selectedLabel ->
                            val selected = supplierList.firstOrNull { it.label == selectedLabel }
                            supplierName = selectedLabel
                            selectedSupplierId = selected?.value ?: ""
                            supplierCode = selected?.value ?: ""
                        },
                        isRequired = true
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) {
                            Column {
                                FormLabel("Supplier Code")
                                FormTextField(
                                    value = supplierCode,
                                    onValueChange = { supplierCode = it },
                                    placeholder = "-"
                                )
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            Column {
                                FormLabel("Contact Person")
                                FormTextField(
                                    value = contactPerson,
                                    onValueChange = { contactPerson = it },
                                    placeholder = "-"
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    FormLabel("GST/VAT Number")
                    FormTextField(
                        value = gstNumber,
                        onValueChange = { gstNumber = it },
                        placeholder = "-"
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) {
                            Column {
                                FormLabel("Payment Terms")
                                FormTextField(
                                    value = paymentTerms,
                                    onValueChange = { paymentTerms = it },
                                    placeholder = "-"
                                )
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            Column {
                                FormLabel("Credit Period")
                                FormTextField(
                                    value = creditPeriod,
                                    onValueChange = { creditPeriod = it },
                                    placeholder = "-"
                                )
                            }
                        }
                    }
                }

                // Section 3: Reusable Dynamic Item Cards Section
                FormLineItemsSection(
                    itemsList = itemsList,
                    inventoryItemsList = inventoryItemsList,
                    horizontalPadding = true
                )

                // Section 4: Logistics
                FormAccordionCard(
                    title = "Logistics",
                    expanded = isLogisticsExpanded,
                    onHeaderClick = { isLogisticsExpanded = !isLogisticsExpanded }
                ) {
                    FormLabel("Shipping Method")
                    FormTextField(
                        value = shippingMethod,
                        onValueChange = { shippingMethod = it },
                        placeholder = "e.g., Express Courier"
                    )
                    Spacer(Modifier.height(8.dp))
                    FormLabel("Shipping Address")
                    FormTextField(
                        value = shippingAddress,
                        onValueChange = { shippingAddress = it },
                        placeholder = "Enter Full Address"
                    )
                    Spacer(Modifier.height(8.dp))
                    FormLabel("Tracking/Reference No.")
                    FormTextField(
                        value = trackingNo,
                        onValueChange = { trackingNo = it },
                        placeholder = "-"
                    )
                }

                // Section 5: Attachments & Notes
                FormAccordionCard(
                    title = "Attachments & Notes",
                    expanded = isNotesExpanded,
                    onHeaderClick = { isNotesExpanded = !isNotesExpanded }
                ) {
                    FormLabel("Internal Notes")
                    FormTextArea(
                        value = internalNotes,
                        onValueChange = { internalNotes = it },
                        placeholder = "Add notes for internal records..."
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

                // Section 6: Dynamic Order Summary
                Card(
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, BorderGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    Column(modifier = Modifier.padding(tokens.cardPadding * 0.8f)) {
                        Text(
                            text = "Order Summary",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = tokens.bodySmall, color = TextSecondary)
                            Text(
                                text = String.format(Locale.US, "₹ %,.2f", calculatedSubtotal),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Tax", fontSize = tokens.bodySmall, color = TextSecondary)
                            Text(
                                text = String.format(Locale.US, "₹ %,.2f", calculatedTax),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = dividerColor)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Grand Total", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = String.format(Locale.US, "₹ %,.2f", calculatedGrandTotal),
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                }

                // Bottom Actions: Update / Submit
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding)
                ) {
                    FormActionButtons(
                        cancelText = "Cancel",
                        draftText = "Save Draft",
                        primaryText = if (isEditMode) "Update Purchase Order" else "Submit Purchase Order",
                        onCancel = onDismiss,
                        onSaveDraft = {
                            val parsedRequisitionItems = itemsList.map { item ->
                                val qtyVal = item.qty.toDoubleOrNull() ?: 1.0
                                val rateVal = item.rate.toDoubleOrNull() ?: 0.0
                                val taxVal = item.taxPercent.toDoubleOrNull() ?: 0.0
                                val subtotalVal = qtyVal * rateVal
                                val taxAmtVal = subtotalVal * (taxVal / 100.0)

                                RequisitionItem(
                                    itemId = item.itemId.ifBlank { null },
                                    qty = qtyVal,
                                    unit = item.unit.ifBlank { "pcs" },
                                    type = item.type.ifBlank { "Goods" },
                                    rate = rateVal,
                                    taxPercent = taxVal,
                                    subtotal = subtotalVal,
                                    taxAmount = taxAmtVal,
                                    total = subtotalVal + taxAmtVal,
                                    status = "Pending"
                                )
                            }

                            val reqPayload = CreateRequisitionRequest(
                                supplierId = selectedSupplierId.ifBlank { "" },
                                warehouseId = selectedWarehouseId.ifBlank { "" },
                                department = "Production",
                                priority = poType.ifBlank { "Medium" },
                                requiredByDate = formatDateToIso(eta),
                                justification = internalNotes,
                                estimatedSubtotal = calculatedSubtotal,
                                estimatedTax = calculatedTax,
                                estimatedTotal = calculatedGrandTotal,
                                items = parsedRequisitionItems,
                                internalReference = trackingNo.trim().ifBlank { null }
                            )

                            viewModel.createRequisition(reqPayload)
                        },
                        onPrimaryClick = {
                            val parsedPoItems = itemsList.map { item ->
                                val qtyVal = item.qty.toDoubleOrNull() ?: 1.0
                                val rateVal = item.rate.toDoubleOrNull() ?: 0.0
                                val taxVal = item.taxPercent.toDoubleOrNull() ?: 0.0
                                val subtotalVal = qtyVal * rateVal
                                val taxAmtVal = subtotalVal * (taxVal / 100.0)

                                PurchaseOrderItem(
                                    id = null,
                                    itemId = item.itemId.ifBlank { "" },
                                    qty = qtyVal,
                                    receivedQty = 0.0,
                                    billedQty = 0.0,
                                    receiveStatus = "Not Received",
                                    billStatus = "Not Billed",
                                    rate = rateVal,
                                    taxPercent = taxVal,
                                    subtotal = subtotalVal,
                                    taxAmount = taxAmtVal,
                                    total = subtotalVal + taxAmtVal
                                )
                            }

                            val poPayload = PurchaseOrder(
                                id = targetPoId,
                                poNumber = orderDetail?.poNumber ?: existingPo?.poNumber,
                                requisitionId = refPrLink.trim().ifBlank { null },
                                supplierId = selectedSupplierId.ifBlank { null },
                                eta = formatDateToIso(eta),
                                currency = orderDetail?.currency ?: "INR",
                                warehouseId = selectedWarehouseId.ifBlank { null },
                                poType = poType.ifBlank { "Standard" },
                                items = parsedPoItems,
                                subtotal = calculatedSubtotal,
                                taxTotal = calculatedTax,
                                discount = orderDetail?.discount ?: 0.0,
                                shippingCost = orderDetail?.shippingCost ?: 0.0,
                                grandTotal = calculatedGrandTotal,
                                orderStatus = orderDetail?.orderStatus ?: existingPo?.orderStatus ?: "Draft",
                                poDate = formatDateToIso(poDate)
                            )

                            if (targetPoId != null) {
                                viewModel.updatePurchaseOrderDirect(targetPoId, poPayload) {
                                    onDismiss()
                                }
                            } else {
                                viewModel.createPurchaseOrderDirect(poPayload) {
                                    onDismiss()
                                }
                            }
                        },
                        isLoading = isBusy,
                        isCancelEnabled = !isBusy,
                        isDraftEnabled = !isBusy,
                        isPrimaryEnabled = !isBusy,
                        primaryColor = BluePrimary,
                        borderColor = BorderGray
                    )

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }
}

private fun formatDateToIso(rawDate: String): String {
    if (rawDate.isBlank()) return ""
    val inputPatterns = listOf("yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd", "dd MMM yyyy")

    for (pattern in inputPatterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
            val parsedDate = sdf.parse(rawDate.trim())
            if (parsedDate != null) {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'00:00:00.000'Z'", Locale.US)
                return isoFormat.format(parsedDate)
            }
        } catch (_: Exception) { }
    }
    return if (rawDate.contains("T")) rawDate else "${rawDate}T00:00:00.000Z"
}