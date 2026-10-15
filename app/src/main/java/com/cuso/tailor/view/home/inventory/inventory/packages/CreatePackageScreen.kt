@file:Suppress("unused","unusedVariable","AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.inventory.packages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.CreatePackageItemRequest
import com.cuso.tailor.model.inventory.CreatePackageRequest
import com.cuso.tailor.model.inventory.PicklistDetailLineItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.toIsoDateTime
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel

@Composable
fun CreatePackageScreen(
    packageId: String? = null,
    viewModel: InventoryViewModel,
    onClose: () -> Unit = {},
    onPackageSaved: () -> Unit = {},
    onPackageCreated: () -> Unit = onPackageSaved
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val isEditMode = !packageId.isNullOrBlank()

    // Observe ViewModel States
    val allPicklists by viewModel.picklists.collectAsStateWithLifecycle()
    val picklistDetail by viewModel.picklistDetail.collectAsStateWithLifecycle()
    val packageDetail by viewModel.packageDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by viewModel.isLoadingPackageDetail.collectAsStateWithLifecycle()
    val isCreatingPackage by viewModel.isCreatingPackage.collectAsStateWithLifecycle()
    val isUpdatingPackage by viewModel.isUpdatingPackage.collectAsStateWithLifecycle()
    val createPackageError by viewModel.createPackageError.collectAsStateWithLifecycle()
    val packageDetailError by viewModel.packageDetailError.collectAsStateWithLifecycle()

    // Form fields
    var packageNumberText by remember { mutableStateOf("Auto-generated on save") }
    var salesOrderCodeText by remember { mutableStateOf("") }
    var estimatedDeliveryDate by remember { mutableStateOf("") }
    var selectedPicklistDisplay by remember { mutableStateOf("") }
    var selectedPicklistId by remember { mutableStateOf<String?>(null) }
    var isPicklistDropdownExpanded by remember { mutableStateOf(false) }

    val packQuantities = remember { mutableStateMapOf<String, Int>() }

    val isConverted = remember(packageDetail?.status) {
        packageDetail?.status.equals("Converted", ignoreCase = true)
    }
    val isReadyForShipment = remember(packageDetail?.status) {
        packageDetail?.status.equals("Ready for Shipment", ignoreCase = true)
    }

    val isEditable = remember(isEditMode, isReadyForShipment, isConverted) {
        if (!isEditMode) true
        else isReadyForShipment && !isConverted
    }

    LaunchedEffect(packageId) {
        if (isEditMode && packageId != null) {
            viewModel.fetchPackageDetail(packageId)
        } else {
            viewModel.fetchPicklists(page = 1, limit = 50)
        }
    }

    LaunchedEffect(packageDetail) {
        if (isEditMode && packageDetail != null) {
            val pkg = packageDetail!!
            packageNumberText = pkg.packageNumber ?: "PCK-00"
            salesOrderCodeText = pkg.salesOrderCode
            estimatedDeliveryDate = pkg.createdAt?.take(10).orEmpty()
        }
    }

    val eligiblePicklists = remember(allPicklists) {
        allPicklists.filter { pl ->
            if (pl.status.equals("Converted", ignoreCase = true)) return@filter false
            pl.items.any { item -> (item.qtyPicked - item.qtyPacked) > 0 }
        }
    }

    val picklistDropdownOptions = remember(eligiblePicklists) {
        if (eligiblePicklists.isEmpty()) {
            listOf("No picklists with picked items")
        } else {
            eligiblePicklists.map { "${it.picklistNumber ?: "PL"} - ${it.warehouse?.name ?: "Warehouse"}" }
        }
    }

    LaunchedEffect(selectedPicklistId) {
        if (!isEditMode) {
            selectedPicklistId?.let { id ->
                viewModel.fetchPicklistDetail(id)
            }
        }
    }

    LaunchedEffect(picklistDetail) {
        if (!isEditMode) {
            packQuantities.clear()
            picklistDetail?.items?.forEach { item ->
                val itemId = item.id ?: return@forEach
                val available = (item.qtyPicked - item.qtyPacked).coerceAtLeast(0)
                packQuantities[itemId] = available
            }
        }
    }

    LaunchedEffect(createPackageError, packageDetailError) {
        val errorMsg = createPackageError ?: packageDetailError
        errorMsg?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearCreatePackageError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        if (isEditMode && isLoadingDetail && packageDetail == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(
                    title = if (isEditMode) "Edit Package" else "Create Package",
                    onClose = {
                        viewModel.clearPackageDetail()
                        onClose()
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Warning Banner if status is Converted / Locked
                    if (isEditMode && !isEditable) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = redText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Status: ${packageDetail?.status ?: "Converted"}. This package is locked and cannot be edited.",
                                        color = redText,
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // ── Card 1: Package Header Info ──
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            color = whiteBg,
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Assignment,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Package",
                                        fontSize = tokens.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Primary
                                    )
                                }

                                // 1. Package Name/Code
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FormLabel("Package")
                                    FormTextField(
                                        value = packageNumberText,
                                        onValueChange = {},
                                        enabled = false,
                                        containerColor = Primary_background
                                    )
                                }

                                // 2. Sales Order (Only visible in Edit Mode)
                                if (isEditMode) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        FormLabel("Sales Order")
                                        FormTextField(
                                            value = salesOrderCodeText,
                                            onValueChange = {},
                                            enabled = false,
                                            containerColor = Primary_background
                                        )
                                    }
                                }

                                // 3. Estimated Delivery Date (Disabled when not editable)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FormLabel("Estimated Delivery")
                                    DatePickerField(
                                        value = estimatedDeliveryDate,
                                        enabled = isEditable,
                                        onDateSelected = { estimatedDeliveryDate = it }
                                    )
                                }

                                // 4. Picklists Dropdown (Only visible in Create Mode)
                                if (!isEditMode) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        FormLabel("Picklists", isRequired = true)
                                        FormDropdown(
                                            value = selectedPicklistDisplay.ifBlank {
                                                if (eligiblePicklists.isEmpty()) "No picklists with picked items"
                                                else "Select Picklist"
                                            },
                                            expanded = isPicklistDropdownExpanded,
                                            onExpandChange = {
                                                if (eligiblePicklists.isNotEmpty()) isPicklistDropdownExpanded = it
                                            },
                                            options = picklistDropdownOptions,
                                            onOptionSelected = { selected ->
                                                selectedPicklistDisplay = selected
                                                val index = picklistDropdownOptions.indexOf(selected)
                                                if (index != -1 && index < eligiblePicklists.size) {
                                                    selectedPicklistId = eligiblePicklists[index].id
                                                }
                                            }
                                        )

                                        Text(
                                            text = "Only picklists with picked, not-yet-packed quantity are listed. One package is created per sales order.",
                                            fontSize = tokens.caption,
                                            color = close_color,
                                            lineHeight = tokens.caption.times(1.3f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Card 2: Items Section ──
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            color = whiteBg,
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Category,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Items",
                                        fontSize = tokens.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Primary
                                    )
                                }

                                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                                if (isEditMode) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "ITEM", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = close_color)
                                        Text(text = "QTY", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = close_color)
                                    }

                                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                                    val editItems = packageDetail?.items.orEmpty()
                                    editItems.forEachIndexed { index, innerItem ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = innerItem.itemName ?: "Custom Garment",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${innerItem.qty}",
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = TextPrimary
                                            )
                                        }

                                        if (index < editItems.lastIndex) {
                                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Items can't be changed after packing.",
                                        fontSize = tokens.caption,
                                        color = close_color
                                    )
                                } else {
                                    val detailItems = picklistDetail?.items.orEmpty()

                                    if (selectedPicklistId.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 36.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Select a picklist to load its picked items",
                                                fontSize = tokens.bodySmall,
                                                color = close_color,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else if (detailItems.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 36.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No picked items available in this picklist",
                                                fontSize = tokens.bodySmall,
                                                color = close_color,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        detailItems.forEachIndexed { index, item ->
                                            val itemId = item.id.orEmpty()
                                            val availableQty = (item.qtyPicked - item.qtyPacked).coerceAtLeast(0)
                                            val currentPackQty = packQuantities[itemId] ?: availableQty

                                            PackageLineItemCard(
                                                item = item,
                                                picklistNumber = picklistDetail?.picklistNumber ?: "PL",
                                                availableQty = availableQty,
                                                packQty = currentPackQty,
                                                onQtyChange = { newQty: Int ->
                                                    packQuantities[itemId] = newQty.coerceIn(0, availableQty)
                                                }
                                            )

                                            if (index < detailItems.lastIndex) {
                                                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Floating Action Buttons (StepNavigationFab) ──
            val isActionLoading = isCreatingPackage || isUpdatingPackage

            StepNavigationFab(
                showBack = true,
                backLabel = if (isEditMode && !isEditable) "Close" else "Cancel",
                onBack = {
                    viewModel.clearPackageDetail()
                    onClose()
                },
                showBackArrow = false,
                showTrailingArrow = false,
                isLoading = isActionLoading,
                trailingAction = if (isEditMode && !isEditable) {
                    null // Hide save button completely if converted/locked
                } else {
                    TrailingFabAction.Update(
                        label = if (isEditMode) "Save Changes" else "Create Package",
                        isLoading = isActionLoading,
                        enabled = !isActionLoading && isEditable && if (isEditMode) true else selectedPicklistId != null,
                        onClick = {
                            if (isEditMode) {

                                val isoDelivery = toIsoDateTime(estimatedDeliveryDate)
                                if (estimatedDeliveryDate.isNotBlank() && isoDelivery == null) {
//                                    Toast.makeText(context, "Invalid estimated delivery date", Toast.LENGTH_SHORT).show()
                                    return@Update
                                }

                                viewModel.updatePackage(
                                    packageId = packageId,
                                    estimatedDelivery = isoDelivery,
                                    onSuccess = {
//                                        Toast.makeText(context, "Package updated successfully!", Toast.LENGTH_SHORT).show()
                                        viewModel.clearPackageDetail()
                                        onPackageSaved()
                                    }
                                )
                            } else {
                                val picklistId = selectedPicklistId
                                if (picklistId.isNullOrBlank()) {
                                    Toast.makeText(context, "Please select a picklist", Toast.LENGTH_SHORT).show()
                                    return@Update
                                }

                                val itemsToSubmit = packQuantities.mapNotNull { (itemId, qty) ->
                                    if (qty > 0) CreatePackageItemRequest(picklistItemId = itemId, qty = qty)
                                    else null
                                }

                                if (itemsToSubmit.isEmpty()) {
//                                    Toast.makeText(context, "Please pack at least 1 item", Toast.LENGTH_SHORT).show()
                                    return@Update
                                }

                                val isoDelivery = toIsoDateTime(estimatedDeliveryDate)
                                if (estimatedDeliveryDate.isNotBlank() && isoDelivery == null) {
//                                    Toast.makeText(context, "Invalid estimated delivery date", Toast.LENGTH_SHORT).show()
                                    return@Update
                                }

                                viewModel.createPackage(
                                    request = CreatePackageRequest(
                                        picklistId = listOf(picklistId),
                                        estimatedDelivery = isoDelivery,
                                        items = itemsToSubmit
                                    ),
                                    onSuccess = {
//                                        Toast.makeText(context, "Package created successfully!", Toast.LENGTH_SHORT).show()
                                        onPackageSaved()
                                    }
                                )
                            }
                        }
                    )
                },
                backWidthFraction = if (isEditMode && !isEditable) 0.95f else 0.28f,
                trailingWidthFraction = if (isEditMode && !isEditable) 0f else 0.42f
            )
        }
    }
}

/**
 * Mobile-friendly Card Layout for a Picklist Line Item
 */
@Composable
private fun PackageLineItemCard(
    item: PicklistDetailLineItemDto,
    picklistNumber: String,
    availableQty: Int,
    packQty: Int,
    onQtyChange: (Int) -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.itemName ?: "Custom Garment",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = item.salesOrder?.orderCode ?: "-",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = BluePrimary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Picklist: $picklistNumber",
                    fontSize = tokens.caption,
                    color = close_color
                )
                Text(
                    text = "Available: $availableQty Qty",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            // Pack Qty Counter Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pack Qty:",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = close_color
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, light_blue_border),
                    color = cardBgLight
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { onQtyChange(packQty - 1) },
                            enabled = packQty > 0,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("-", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        Text(
                            text = packQty.toString(),
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        IconButton(
                            onClick = { onQtyChange(packQty + 1) },
                            enabled = packQty < availableQty,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("+", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}