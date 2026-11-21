@file:Suppress("SpellCheckingInspection", "unused", "AssignedValueIsNeverRead", "VariableNeverRead")

package com.cuso.tailor.view.home.inventory.procurement.purchase_order

import android.net.Uri
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.ItemRefDetail
import com.cuso.tailor.model.inventory.PurchaseOrder
import com.cuso.tailor.model.inventory.PurchaseOrderLinePayload
import com.cuso.tailor.model.inventory.SupplierDropdownItem
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.dividerColor
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.utils.formatCurrency
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
import java.util.Date
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

    val isSubmittingPO by viewModel.isSubmittingPO.collectAsStateWithLifecycle()
    val isBusy = isSubmittingPO

    val poSuccessMessage by viewModel.poSuccessMessage.collectAsStateWithLifecycle()
    val poErrorMessage by viewModel.purchaseOrdersError.collectAsStateWithLifecycle()

    val warehouseList by viewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val warehouseUiState by viewModel.warehouseUiState.collectAsStateWithLifecycle()
    val supplierDropdown by viewModel.supplierDropdown.collectAsStateWithLifecycle()
    val suppliersList by viewModel.suppliers.collectAsStateWithLifecycle()
    val inventoryItemsList by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val taxGroups by viewModel.taxGroups.collectAsStateWithLifecycle()

    // Merge supplier dropdown and view-all suppliers so suppliers are always available
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

    // Fetch dropdowns and PO detail in edit mode
    LaunchedEffect(targetPoId) {
        viewModel.loadWarehouseDropdown()
        viewModel.loadWarehouses()
        viewModel.fetchSupplierDropdown()
        viewModel.fetchSuppliers(page = 1, limit = 50)
        viewModel.fetchInventoryItems(page = 1, limit = 100)
        viewModel.fetchTaxGroups()

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
            DynamicIslandManager.showSuccess(it)
            viewModel.clearPOAlerts()
            onDismiss()
        }
    }

    LaunchedEffect(poErrorMessage) {
        poErrorMessage?.let {
            DynamicIslandManager.showError(it)
            viewModel.clearPOAlerts()
        }
    }

    LaunchedEffect(poDetailError) {
        poDetailError?.let {
            DynamicIslandManager.showError(it)
        }
    }

    // Form fields state
    val todayFormatted = remember {
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    }
    var selectedWarehouseId by remember { mutableStateOf("") }
    var warehouseName by remember { mutableStateOf("") }
    var warehouseExpanded by remember { mutableStateOf(false) }

    var poDate by remember { mutableStateOf(if (isEditMode) "" else todayFormatted) }
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

    var shippingMethod by remember { mutableStateOf("") }
    var shippingAddress by remember { mutableStateOf("") }
    var trackingNo by remember { mutableStateOf("") }
    var transportName by remember { mutableStateOf("") }
    var vehicleNumber by remember { mutableStateOf("") }
    var freightTerms by remember { mutableStateOf("") }

    var internalNotes by remember { mutableStateOf("") }
    var discountText by remember { mutableStateOf("0") }
    var shippingCostText by remember { mutableStateOf("0") }

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

    // Backfill supplier metadata whenever selectedSupplierId or suppliersList updates
    LaunchedEffect(selectedSupplierId, suppliersList, resolvedSuppliers) {
        if (selectedSupplierId.isBlank()) return@LaunchedEffect
        if (supplierName.isBlank()) {
            resolvedSuppliers.firstOrNull { it.value == selectedSupplierId }?.let {
                supplierName = it.label
            }
        }
        suppliersList.firstOrNull { it.id == selectedSupplierId }?.let { s ->
            supplierCode = s.supplierCode
            contactPerson = s.contact?.contactName.orEmpty()
            gstNumber = s.tax?.gstNumber.orEmpty()
            val termDays = s.payment?.paymentTerm ?: 0
            paymentTerms = if (termDays > 0) "Net $termDays" else "Immediate"
        }
    }

    // Backfill warehouse shipping address whenever selectedWarehouseId updates
    LaunchedEffect(selectedWarehouseId, warehouseUiState.warehouses) {
        if (selectedWarehouseId.isBlank()) return@LaunchedEffect
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

    // Prefill from fetched PO View-One detail
    LaunchedEffect(orderDetail) {
        val detail = orderDetail
        if (detail != null && !isPrefilled) {
            isPrefilled = true

            selectedWarehouseId = detail.warehouse?.id.orEmpty()
            warehouseName = detail.warehouse?.name.orEmpty()

            poDate = detail.poDate?.substringBefore("T").orEmpty()
            eta = detail.eta?.substringBefore("T").orEmpty()
            poType = detail.poType?.ifBlank { "Standard" } ?: "Standard"
            refPrLink = detail.requisitionId.orEmpty()

            selectedSupplierId = detail.supplier?.id.orEmpty()
            supplierName = detail.supplier?.name.orEmpty()
            supplierCode = detail.supplier?.supplierCode.orEmpty()
            contactPerson = detail.supplier?.contact?.contactName.orEmpty()
            gstNumber = detail.supplier?.tax?.gstNumber.orEmpty()

            shippingMethod = detail.shippingMethod.orEmpty()
            transportName = detail.transportName.orEmpty()
            vehicleNumber = detail.vehicleNumber.orEmpty()
            trackingNo = detail.trackingNumber.orEmpty()
            freightTerms = detail.freightTerms.orEmpty()

            val whAddr = detail.warehouse?.address
            shippingAddress = listOfNotNull(
                whAddr?.address,
                whAddr?.city,
                whAddr?.state,
                whAddr?.country,
                whAddr?.pincode
            ).filter { it.isNotBlank() }.joinToString(", ")

            internalNotes = detail.internalNotes.orEmpty()
            discountText = detail.discount?.takeIf { it > 0 }?.toString() ?: "0"
            shippingCostText = detail.shippingCost?.takeIf { it > 0 }?.toString() ?: "0"

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
    LaunchedEffect(existingPo, warehouseList, resolvedSuppliers) {
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
            supplierName = resolvedSuppliers.firstOrNull { it.value == supId }?.label ?: supId

            poType = existingPo.poType.ifBlank { "Standard" }
            poDate = existingPo.poDate?.substringBefore("T").orEmpty()
            eta = existingPo.eta?.substringBefore("T").orEmpty()
            refPrLink = existingPo.requisitionId.orEmpty()
            shippingMethod = existingPo.shippingMethod.orEmpty()
            transportName = existingPo.transportName.orEmpty()
            vehicleNumber = existingPo.vehicleNumber.orEmpty()
            trackingNo = existingPo.trackingNumber.orEmpty()
            freightTerms = existingPo.freightTerms.orEmpty()
            internalNotes = existingPo.internalNotes.orEmpty()
            discountText = existingPo.discount.takeIf { it > 0 }?.toString() ?: "0"
            shippingCostText = existingPo.shippingCost.takeIf { it > 0 }?.toString() ?: "0"

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

    val discountVal = discountText.toDoubleOrNull() ?: 0.0
    val shippingCostVal = shippingCostText.toDoubleOrNull() ?: 0.0
    val calculatedGrandTotal = (calculatedSubtotal + calculatedTax - discountVal + shippingCostVal).coerceAtLeast(0.0)

    fun submitPurchaseOrder(targetOrderStatus: String) {
        val invalidIndex = itemsList.indexOfFirst {
            it.itemId.isBlank() || (it.qty.toDoubleOrNull() ?: 0.0) <= 0.0
        }

        val validationError = when {
            selectedWarehouseId.isBlank() -> "Warehouse is required"
            selectedSupplierId.isBlank() -> "Supplier is required"
            poDate.isBlank() -> "PO Date is required"
            itemsList.isEmpty() -> "At least one item is required"
            invalidIndex != -1 -> {
                if (itemsList[invalidIndex].itemId.isBlank()) {
                    "Please select an item on row ${invalidIndex + 1}"
                } else {
                    "Quantity must be greater than 0 on row ${invalidIndex + 1}"
                }
            }
            else -> null
        }

        if (validationError != null) {
            DynamicIslandManager.showError(validationError)
            return
        }

        val itemsPayload = itemsList.map { item ->
            val qtyVal = item.qty.toDoubleOrNull() ?: 1.0
            val rateVal = item.rate.toDoubleOrNull() ?: 0.0
            val taxPercentVal = item.taxPercent.toDoubleOrNull() ?: 0.0
            val matchedTaxGroupId = taxGroups.firstOrNull { it.totalRate == taxPercentVal }?.id

            PurchaseOrderLinePayload(
                itemId = item.itemId,
                qty = qtyVal,
                rate = rateVal,
                taxGroupId = matchedTaxGroupId,
                taxPercent = taxPercentVal
            )
        }

        val formattedPoDate = formatDateToIso(poDate)
        val formattedEta = formatDateToIso(eta).takeIf { it.isNotBlank() }

        if (targetPoId != null) {
            viewModel.updatePurchaseOrderMultipart(
                context = context,
                id = targetPoId,
                warehouseId = selectedWarehouseId,
                supplierId = selectedSupplierId,
                poDate = formattedPoDate,
                eta = formattedEta,
                poType = poType,
                currency = orderDetail?.currency ?: "INR",
                orderStatus = targetOrderStatus,
                shippingMethod = shippingMethod.trim(),
                transportName = transportName.trim(),
                vehicleNumber = vehicleNumber.trim(),
                trackingNumber = trackingNo.trim(),
                freightTerms = freightTerms.trim(),
                internalNotes = internalNotes.trim(),
                remarks = "",
                discount = discountVal,
                shippingCost = shippingCostVal,
                items = itemsPayload,
                requisitionId = refPrLink.trim().takeIf { it.isNotBlank() },
                attachmentUris = selectedFiles
            )
        } else {
            viewModel.createPurchaseOrderMultipart(
                context = context,
                warehouseId = selectedWarehouseId,
                supplierId = selectedSupplierId,
                poDate = formattedPoDate,
                eta = formattedEta,
                poType = poType,
                currency = "INR",
                orderStatus = targetOrderStatus,
                shippingMethod = shippingMethod.trim(),
                transportName = transportName.trim(),
                vehicleNumber = vehicleNumber.trim(),
                trackingNumber = trackingNo.trim(),
                freightTerms = freightTerms.trim(),
                internalNotes = internalNotes.trim(),
                remarks = "",
                discount = discountVal,
                shippingCost = shippingCostVal,
                items = itemsPayload,
                requisitionId = refPrLink.trim().takeIf { it.isNotBlank() },
                attachmentUris = selectedFiles
            )
        }
    }

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
                        value = warehouseName.ifBlank { "Select warehouse" },
                        expanded = warehouseExpanded,
                        onExpandChange = { warehouseExpanded = it },
                        options = warehouseList.map { it.label }.ifEmpty { listOf("No warehouses available") },
                        onOptionSelected = { selectedLabel ->
                            val selected = warehouseList.firstOrNull { it.label == selectedLabel }
                                ?: return@FormDropdown
                            warehouseName = selected.label
                            selectedWarehouseId = selected.value
                        },
                        isRequired = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Box(Modifier.weight(1f)) {
                            Column {
                                FormLabel("PO Date", isRequired = true)
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

                    Spacer(Modifier.height(tokens.extraPadding))

                    FormDropdown(
                        label = "PO Type",
                        value = poType,
                        expanded = poTypeExpanded,
                        onExpandChange = { poTypeExpanded = it },
                        options = listOf("Standard", "Urgent", "Critical"),
                        onOptionSelected = { poType = it }
                    )

                    if (refPrLink.isNotBlank()) {
                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Linked Requisition")
                        FormTextField(
                            value = refPrLink,
                            onValueChange = {},
                            enabled = false,
                            placeholder = "-"
                        )
                    }
                }

                // Section 2: Supplier Information
                FormAccordionCard(
                    title = "Supplier Information",
                    expanded = isSupplierExpanded,
                    onHeaderClick = { isSupplierExpanded = !isSupplierExpanded }
                ) {
                    FormDropdown(
                        label = "Supplier Name",
                        value = supplierName.ifBlank { "Select supplier" },
                        expanded = supplierDropdownExpanded,
                        onExpandChange = { supplierDropdownExpanded = it },
                        options = resolvedSuppliers.map { it.label }.ifEmpty { listOf("No suppliers available") },
                        onOptionSelected = { selectedLabel ->
                            val selected = resolvedSuppliers.firstOrNull { it.label == selectedLabel }
                                ?: return@FormDropdown
                            supplierName = selected.label
                            selectedSupplierId = selected.value
                        },
                        isRequired = true
                    )

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Box(Modifier.weight(1f)) {
                            Column {
                                FormLabel("Supplier Code")
                                FormTextField(
                                    value = supplierCode,
                                    onValueChange = {},
                                    enabled = false,
                                    placeholder = "-"
                                )
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            Column {
                                FormLabel("Contact Person")
                                FormTextField(
                                    value = contactPerson,
                                    onValueChange = {},
                                    enabled = false,
                                    placeholder = "-"
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Box(Modifier.weight(1f)) {
                            Column {
                                FormLabel("GST/VAT Number")
                                FormTextField(
                                    value = gstNumber,
                                    onValueChange = {},
                                    enabled = false,
                                    placeholder = "-"
                                )
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            Column {
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

                // Section 3: Line Items
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
                        placeholder = "e.g. Road, Courier, Air"
                    )
                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Shipping Address")
                    FormTextField(
                        value = shippingAddress,
                        onValueChange = {},
                        enabled = false,
                        placeholder = "Auto filled from warehouse"
                    )
                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Tracking/Reference No.")
                    FormTextField(
                        value = trackingNo,
                        onValueChange = { trackingNo = it },
                        placeholder = "Enter tracking number"
                    )
                    Spacer(Modifier.height(tokens.extraPadding))
                    Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Box(Modifier.weight(1f)) {
                            Column {
                                FormLabel("Transport Company")
                                FormTextField(
                                    value = transportName,
                                    onValueChange = { transportName = it },
                                    placeholder = "Transport name"
                                )
                            }
                        }
                        Box(Modifier.weight(1f)) {
                            Column {
                                FormLabel("Vehicle Number")
                                FormTextField(
                                    value = vehicleNumber,
                                    onValueChange = { vehicleNumber = it.uppercase() },
                                    placeholder = "e.g. TN-01-AB-1234"
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Freight Terms")
                    FormTextField(
                        value = freightTerms,
                        onValueChange = { freightTerms = it },
                        placeholder = "e.g. Paid, To Pay, Collect"
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
                        placeholder = "Enter internal notes (visible only to admin)"
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
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = tokens.bodySmall, color = TextSecondary)
                            Text(
                                text = String.format(Locale.US, "${formatCurrency()} %,.2f", calculatedSubtotal),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding * 0.6f))

                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Tax", fontSize = tokens.bodySmall, color = TextSecondary)
                            Text(
                                text = String.format(Locale.US, "${formatCurrency()} %,.2f", calculatedTax),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                            Box(Modifier.weight(1f)) {
                                Column {
                                    FormLabel("Discount")
                                    FormTextField(
                                        value = discountText,
                                        onValueChange = { discountText = sanitizeDecimalInput(it) },
                                        keyboardType = KeyboardType.Decimal,
                                        placeholder = "0"
                                    )
                                }
                            }
                            Box(Modifier.weight(1f)) {
                                Column {
                                    FormLabel("Shipping Cost")
                                    FormTextField(
                                        value = shippingCostText,
                                        onValueChange = { shippingCostText = sanitizeDecimalInput(it) },
                                        keyboardType = KeyboardType.Decimal,
                                        placeholder = "0"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding))
                        HorizontalDivider(color = dividerColor)
                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("Grand Total", fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(
                                text = String.format(Locale.US, "${formatCurrency()} %,.2f", calculatedGrandTotal),
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                        }
                    }
                }

                // Bottom Actions: Cancel / Save Draft / Submit
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
                            submitPurchaseOrder(targetOrderStatus = "Draft")
                        },
                        onPrimaryClick = {
                            val nextStatus = if (isEditMode) {
                                orderDetail?.orderStatus ?: existingPo?.orderStatus ?: "Approved"
                            } else {
                                "Approved"
                            }
                            submitPurchaseOrder(targetOrderStatus = nextStatus)
                        },
                        isLoading = isBusy,
                        isCancelEnabled = !isBusy,
                        isDraftEnabled = !isBusy,
                        isPrimaryEnabled = !isBusy,
                        primaryColor = BluePrimary,
                        borderColor = BorderGray
                    )

                    Spacer(Modifier.height(tokens.screenPadding))
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