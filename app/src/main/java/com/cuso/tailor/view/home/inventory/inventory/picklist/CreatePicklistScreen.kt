@file:Suppress("unused","unusedVariable","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.inventory.picklist

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreatePicklistScreen(
    picklistId: String? = null,
    viewModel: InventoryViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onCancel: () -> Unit = {},
    onSuccessSaved: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isEditMode = !picklistId.isNullOrBlank()

    // Form field states
    var picklistNumberText by remember { mutableStateOf("Auto-generated on save") }

    var selectedDateText by remember {
        mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
    }

    var selectedAssigneeName by remember { mutableStateOf("") }
    var selectedAssigneeId by remember { mutableStateOf<String?>(null) }
    var assigneeDropdownExpanded by remember { mutableStateOf(false) }

    var selectedWarehouseName by remember { mutableStateOf("") }
    var selectedWarehouseId by remember { mutableStateOf<String?>(null) }
    var warehouseDropdownExpanded by remember { mutableStateOf(false) }

    var internalNotes by remember { mutableStateOf("") }

    var selectedSalesOrderCode by remember { mutableStateOf("") }
    var selectedSalesOrderId by remember { mutableStateOf<String?>(null) }
    var salesOrderDropdownExpanded by remember { mutableStateOf(false) }

    var loadedItems by remember { mutableStateOf<List<PicklistDetailLineItemDto>>(emptyList()) }

    // Observers from ViewModels
    val warehouseList by viewModel.warehouseDropdown.collectAsStateWithLifecycle()
    val isSaving by viewModel.isCreatingPicklist.collectAsStateWithLifecycle()
    val picklistDetail by viewModel.picklistDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by viewModel.isLoadingPicklistDetail.collectAsStateWithLifecycle()

    val memberList by hrViewModel.members.collectAsStateWithLifecycle()
    val readyOrders by viewModel.readyDeliveryOrders.collectAsStateWithLifecycle()

    // Initialize data on screen launch
    LaunchedEffect(Unit) {
        hrViewModel.fetchMembers(page = 1, limit = 100, status = "active")
        viewModel.fetchReadyForDeliveryOrders()
    }

    // Fetch picklist detail when opened in edit mode
    LaunchedEffect(picklistId) {
        if (isEditMode) {
            viewModel.fetchPicklistDetail(picklistId)
        }
    }

    // Prefill form in Edit Mode
    LaunchedEffect(picklistDetail) {
        picklistDetail?.let { detail ->
            if (isEditMode) {
                picklistNumberText = detail.picklistNumber.orEmpty().ifBlank { "Auto-generated on save" }

                detail.date?.let { rawDate ->
                    try {
                        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        val parsed = parser.parse(rawDate)
                        if (parsed != null) {
                            selectedDateText = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(parsed)
                        } else if (rawDate.length >= 10) {
                            val parts = rawDate.take(10).split("-")
                            if (parts.size == 3) selectedDateText = "${parts[2]}-${parts[1]}-${parts[0]}"
                        }
                    } catch (_: Exception) {
                        if (rawDate.length >= 10) {
                            val parts = rawDate.take(10).split("-")
                            if (parts.size == 3) selectedDateText = "${parts[2]}-${parts[1]}-${parts[0]}"
                        }
                    }
                }

                // Prefill Assignee
                selectedAssigneeName = detail.assignee?.fullName ?: "Unassigned"
                selectedAssigneeId = detail.assignee?.id

                // Prefill Location
                selectedWarehouseName = detail.warehouse?.name.orEmpty()
                selectedWarehouseId = detail.warehouse?.id

                // Prefill Notes
                internalNotes = detail.internalNotes.orEmpty()

                // Prefill Line Items
                loadedItems = detail.items
                detail.items.firstOrNull()?.salesOrder?.let { so ->
                    selectedSalesOrderCode = so.orderCode.orEmpty()
                    selectedSalesOrderId = so.id
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isEditMode) {
                viewModel.clearPicklistDetail()
            }
        }
    }

    // Dropdown string options
    val assigneeOptions = remember(memberList) {
        listOf("Unassigned") + memberList.map { "${it.firstName} ${it.lastName}".trim() }
    }

    val warehouseOptions = remember(warehouseList) {
        warehouseList.map { it.label }
    }

    val salesOrderOptions = remember(readyOrders, selectedSalesOrderCode) {
        val codes = readyOrders.mapNotNull { it.orderCode }
        if (selectedSalesOrderCode.isNotBlank() && selectedSalesOrderCode !in codes) {
            listOf(selectedSalesOrderCode) + codes
        } else {
            codes
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleBar(
                    title = if (isEditMode) "Edit Picklist" else "Create Picklist",
                    onClose = onClose
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = tokens.screenPadding,
                vertical = tokens.extraPadding
            ),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // Picklist Header & Inputs Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        CardHeaderTitle(title = "Picklist")

                        // 1. Picklist Auto Code
                        Column {
                            FormLabel(text = "Picklist", isRequired = false)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(tokens.fieldHeight)
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .background(lightGray)
                                    .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .padding(horizontal = tokens.cardPadding * 0.6f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = picklistNumberText,
                                    fontSize = tokens.bodySmall,
                                    color = mutedText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // 2. Date
                        Column {
                            FormLabel(text = "Date", isRequired = true)
                            DatePickerField(
                                value = selectedDateText,
                                onDateSelected = { selectedDateText = it }
                            )
                        }

                        // 3. Assignee
                        FormDropdown(
                            label = "Assignee",
                            value = selectedAssigneeName.ifBlank { "Select Assignee" },
                            expanded = assigneeDropdownExpanded,
                            onExpandChange = { assigneeDropdownExpanded = it },
                            options = assigneeOptions,
                            onOptionSelected = { selectedOption ->
                                selectedAssigneeName = selectedOption
                                if (selectedOption == "Unassigned") {
                                    selectedAssigneeId = null
                                } else {
                                    val matched = memberList.find { "${it.firstName} ${it.lastName}".trim() == selectedOption }
                                    selectedAssigneeId = matched?._id
                                }
                            },
                            isRequired = false
                        )

                        // 4. Location
                        FormDropdown(
                            label = "Location",
                            value = selectedWarehouseName.ifBlank { "Select Warehouse" },
                            expanded = warehouseDropdownExpanded,
                            onExpandChange = { warehouseDropdownExpanded = it },
                            options = warehouseOptions,
                            onOptionSelected = { selectedLabel ->
                                selectedWarehouseName = selectedLabel
                                selectedWarehouseId = warehouseList.find { it.label == selectedLabel }?.value
                            },
                            isRequired = true
                        )

                        // 5. Internal Notes
                        Column {
                            FormLabel(text = "Internal Notes", isRequired = false)
                            FormTextArea(
                                value = internalNotes,
                                onValueChange = { internalNotes = it },
                                placeholder = "",
                                minLines = 3,
                                maxLines = 5,
                                borderColor = grey_border,
                                focusedBorderColor = Primary,
                                textColor = TextPrimary,
                                placeholderColor = mutedText
                            )
                        }

                        // 6. Sales Order Dropdown
                        Column {
                            FormDropdown(
                                label = "Sales Order",
                                value = selectedSalesOrderCode.ifBlank { "Select Sales Order" },
                                expanded = salesOrderDropdownExpanded,
                                onExpandChange = {
                                    if (!isEditMode) salesOrderDropdownExpanded = it
                                },
                                options = salesOrderOptions,
                                onOptionSelected = { selectedCode ->
                                    selectedSalesOrderCode = selectedCode
                                    val matchedOrder = readyOrders.find { it.orderCode == selectedCode }
                                    selectedSalesOrderId = matchedOrder?.id

                                    // Map dispatch items to line items for instant display
                                    if (matchedOrder != null) {
                                        loadedItems = matchedOrder.dispatchItems.map { dispatchItem ->
                                            PicklistDetailLineItemDto(
                                                id = dispatchItem.id,
                                                itemName = dispatchItem.itemName,
                                                lineType = dispatchItem.lineType,
                                                requiresStock = false,
                                                qty = dispatchItem.quantity,
                                                qtyPicked = 0,
                                                qtyPacked = 0,
                                                status = "Pending",
                                                salesOrder = PicklistSalesOrderRef(
                                                    id = matchedOrder.id,
                                                    orderCode = matchedOrder.orderCode
                                                )
                                            )
                                        }
                                    }
                                },
                                isRequired = true
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Only \"Ready for Delivery\" orders are listed. Stock is picked from the bins where it is reserved.",
                                fontSize = tokens.caption,
                                color = mutedText,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }
                    }
                }
            }

            // Items List Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding)
                    ) {
                        CardHeaderTitle(title = "Items")

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        if (loadedItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = tokens.extraPadding * 2),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Select a sales order to load its items",
                                    fontSize = tokens.bodySmall,
                                    color = mutedText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                                loadedItems.forEachIndexed { index, itemLine ->
                                    CreatePicklistItemCard(
                                        item = itemLine,
                                        isEditMode = isEditMode,
                                        defaultOrderCode = selectedSalesOrderCode,
                                        onQtyPickedChange = { newPickedQty ->
                                            loadedItems = loadedItems.toMutableList().also { list ->
                                                list[index] = list[index].copy(qtyPicked = newPickedQty)
                                            }
                                        },
                                        onDelete = {
                                            loadedItems = loadedItems.toMutableList().also { list ->
                                                list.removeAt(index)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action Buttons
            item {
                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.5f))

                // Cancel Button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = BorderStroke(1.dp, sectionBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = whiteBg,
                        contentColor = TextPrimary
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(tokens.extraPadding * 0.8f))

                // Save or Update Button
                Button(
                    onClick = {
                        if (selectedWarehouseId.isNullOrBlank()) {
                            Toast.makeText(context, "Please select Location", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (selectedSalesOrderId.isNullOrBlank() && !isEditMode) {
                            Toast.makeText(context, "Please select a Sales Order", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (loadedItems.isEmpty()) {
                            Toast.makeText(context, "Please add at least one line item", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val isoDate = try {
                            val parts = selectedDateText.split("-")
                            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}"
                            else selectedDateText
                        } catch (_: Exception) {
                            selectedDateText
                        }

                        // Map line items to exact request payload format
                        val itemPayloads = loadedItems.map { lineItem ->
                            PicklistLineItemPayload(
                                soLineId = lineItem.id,
                                salesOrderId = lineItem.salesOrder?.id ?: selectedSalesOrderId,
                                qty = lineItem.qty,
                                qtyPicked = lineItem.qtyPicked,
                                lineType = lineItem.lineType,
                                itemName = lineItem.itemName
                            )
                        }

                        val request = CreatePicklistRequest(
                            date = isoDate,
                            assigneeId = selectedAssigneeId,
                            warehouseId = selectedWarehouseId!!,
                            internalNotes = internalNotes.ifBlank { null },
                            salesOrderId = selectedSalesOrderId,
                            items = itemPayloads
                        )

                        if (isEditMode) {
                            // Map to { _id, qtyPicked } matching the web payload exactly
                            val updateItems = loadedItems.mapNotNull { item ->
                                val itemId = item.id ?: return@mapNotNull null
                                UpdatePicklistItemPayload(
                                    id = itemId,
                                    qtyPicked = item.qtyPicked
                                )
                            }

                            viewModel.updatePicklist(
                                id = picklistId,
                                assigneeId = selectedAssigneeId,
                                internalNotes = internalNotes.ifBlank { null },
                                items = updateItems,
                                onSuccess = {
                                    Toast.makeText(context, "Picklist updated successfully", Toast.LENGTH_SHORT).show()
                                    onSuccessSaved()
                                }
                            )
                        } else {
                            // Full create payload
                            val createItemPayloads = loadedItems.map { lineItem ->
                                PicklistLineItemPayload(
                                    soLineId = lineItem.id,
                                    salesOrderId = lineItem.salesOrder?.id ?: selectedSalesOrderId,
                                    qty = lineItem.qty,
                                    qtyPicked = lineItem.qtyPicked,
                                    lineType = lineItem.lineType,
                                    itemName = lineItem.itemName
                                )
                            }

                            viewModel.createPicklist(
                                date = isoDate,
                                assigneeId = selectedAssigneeId,
                                warehouseId = selectedWarehouseId!!,
                                internalNotes = internalNotes.ifBlank { null },
                                salesOrderId = selectedSalesOrderId!!,
                                items = createItemPayloads,
                                onSuccess = {
                                    Toast.makeText(context, "Picklist created successfully", Toast.LENGTH_SHORT).show()
                                    onSuccessSaved()
                                }
                            )
                        }
                    },
                    enabled = !isSaving && !isLoadingDetail,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = disabled
                    )
                ) {
                    Text(
                        text = if (isSaving) "Saving..." else "Save",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding * 2))
            }
        }
    }
}

/**
 * Mobile-friendly Line Item Card:
 * - Create Mode: Both QTY and QTY PICK are disabled / read-only.
 * - Edit Mode: QTY is disabled / read-only; QTY PICK is editable (up to QTY).
 */
@Composable
private fun CreatePicklistItemCard(
    item: PicklistDetailLineItemDto,
    isEditMode: Boolean,
    defaultOrderCode: String,
    onQtyPickedChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val orderCode = item.salesOrder?.orderCode.orEmpty().ifBlank { defaultOrderCode.ifBlank { "SO-00" } }
    var qtyPickedString by remember(item.qtyPicked) { mutableStateOf(item.qtyPicked.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.extraPadding)
        ) {
            // Row 1: Item description and delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.itemName.orEmpty().ifBlank { "Item" },
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.lineType ?: "Custom Garment"} · no stock",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = orderCode,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(tokens.iconSize * 1.5f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Remove Item",
                        tint = redText,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding))

            // Row 2: Disabled QTY & Conditional QTY PICK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. QTY Box: ALWAYS DISABLED (in both Create and Edit mode)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "QTY",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(lightGray)
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .padding(horizontal = tokens.screenPadding * 0.5f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = item.qty.toString(),
                            fontSize = tokens.bodySmall,
                            color = mutedText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 2. QTY PICK Box: DISABLED in Create Mode, ENABLED in Edit Mode
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "QTY PICK",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = headerGrey
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.fieldHeight)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(if (isEditMode) whiteBg else lightGray)
                            .border(
                                1.dp,
                                if (isEditMode) Primary.copy(alpha = 0.6f) else grey_border,
                                RoundedCornerShape(tokens.cardCornerRadius * 0.4f)
                            )
                            .padding(horizontal = tokens.screenPadding * 0.5f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (isEditMode) {
                            BasicTextField(
                                value = qtyPickedString,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() }) {
                                        val entered = input.toIntOrNull() ?: 0
                                        // Bound quantity between 0 and maximum required QTY
                                        val valid = entered.coerceIn(0, item.qty)
                                        qtyPickedString = if (input.isEmpty()) "" else valid.toString()
                                        onQtyPickedChange(valid)
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = TextStyle(
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = item.qtyPicked.toString(),
                                fontSize = tokens.bodySmall,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardHeaderTitle(
    title: String
) {
    val tokens = LocalAppTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}