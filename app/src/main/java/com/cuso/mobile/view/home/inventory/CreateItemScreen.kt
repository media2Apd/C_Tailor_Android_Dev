package com.cuso.mobile.view.home.inventory

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.cuso.mobile.model.inventory.ItemType
import com.cuso.mobile.viewmodel.CreateItemUiState
import com.cuso.mobile.viewmodel.InventoryViewModel
import com.cuso.mobile.viewmodel.ItemSection
import com.cuso.mobile.view.home.FormLabel
import com.cuso.mobile.view.home.FormTextField
import com.cuso.mobile.view.home.FormDropdown
import com.cuso.mobile.view.home.reusablecomposables.StepNavigationFab
import com.cuso.mobile.view.home.reusablecomposables.TrailingFabAction
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.ProfileViewModel

// ── Design tokens ──
private val AccentColor = Color(0xFF4F39F6)
private val BorderColor = Color(0xFFE3E4E8)
private val LabelColor = Color(0xFF6B7280)
private val TitleColor = Color(0xFF111827)
private val PlaceholderColor = Color(0xFF9CA3AF)
private val FieldShape = RoundedCornerShape(10.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    onDismiss: () -> Unit,
    onItemCreated: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsState()
    val planName = (profileState as? com.cuso.mobile.viewmodel.ProfileUiState.Success)
        ?.data?.organization?.plan?.name.orEmpty()
    val isStarterOrLight = planName.equals("Starter", ignoreCase = true) ||
            planName.equals("Light", ignoreCase = true)

    val context = LocalContext.current
    val formState by viewModel.createItemForm.collectAsState()
    val expandedSection by viewModel.expandedSection.collectAsState()
    val uiState by viewModel.createItemUiState.collectAsState()

    var unitExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var salesAccountExpanded by remember { mutableStateOf(false) }
    var purchaseAccountExpanded by remember { mutableStateOf(false) }
    var preferredVendorExpanded by remember { mutableStateOf(false) }

    val isEditMode = formState.itemId != null

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    LaunchedEffect(uiState) {
        if (uiState is CreateItemUiState.Success) {
            onItemCreated()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // ── Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) "Edit Item" else "Create Item",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TitleColor
                )
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = LabelColor,
                    modifier = Modifier.clickable {
                        viewModel.resetCreateItemForm()
                        onDismiss()
                    }
                )
            }
            HorizontalDivider(color = BorderColor)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 90.dp)
            ) {
                // ── Item Identity ──
                AccordionSection(
                    icon = Icons.Filled.Inventory2,
                    title = "Item Identity",
                    expanded = expandedSection == ItemSection.ITEM_IDENTITY,
                    onHeaderClick = { viewModel.toggleSection(ItemSection.ITEM_IDENTITY) }
                ) {
                    FormLabel("Item Type")
                    SegmentedToggle(
                        optionA = "Goods",
                        optionB = "Service",
                        selectedA = formState.itemType == ItemType.IN_HOUSE,
                        onSelectA = { viewModel.updateCreateItemForm { it.copy(itemType = ItemType.IN_HOUSE) } },
                        onSelectB = { viewModel.updateCreateItemForm { it.copy(itemType = ItemType.CLIENT) } }
                    )

                    Spacer(Modifier.height(16.dp))
                    FormLabel("Item Name")
                    FormTextField(
                        value = formState.name,
                        onValueChange = { newValue ->
                            viewModel.updateCreateItemForm { it.copy(name = newValue) }
                        },
                        placeholder = "e.g. Premium Woolen Fabric"
                    )

                    Spacer(Modifier.height(16.dp))
                    FormLabel("SKU")
                    FormTextField(
                        value = formState.sku,
                        onValueChange = { /* read only */ },
                        placeholder = "Auto-generated SKU",
                        enabled = false
                    )

                    if (!isEditMode) {
                        Spacer(Modifier.height(8.dp))
                        ToggleRow(
                            title = "Auto-generate SKU",
                            checked = formState.autoGenerateSku,
                            onCheckedChange = { checked -> viewModel.onAutoGenerateSkuToggle(checked) },
                            titleFirst = false
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Unit of Measure",
                        value = formState.unit.ifBlank { "Select Unit" },
                        expanded = unitExpanded,
                        onExpandChange = { unitExpanded = it },
                        options = listOf("Meter", "Piece", "Kg"),
                        onOptionSelected = { selectedUnit ->
                            viewModel.updateCreateItemForm { it.copy(unit = selectedUnit) }
                        }
                    )

                    Spacer(Modifier.height(16.dp))
                    ToggleRow(
                        title = "Returnable Item",
                        subtitle = "Customer can request return/refund",
                        checked = formState.returnable,
                        onCheckedChange = { isChecked ->
                            viewModel.updateCreateItemForm { it.copy(returnable = isChecked) }
                        },
                        titleFirst = true
                    )

                    Spacer(Modifier.height(16.dp))
                    FormLabel("Category")

                    FormTextField(
                        value = formState.category,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(category = v) } },
                        placeholder = "category",
                    )
                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Status",
                        value = formState.status.ifBlank { "active" },
                        expanded = statusExpanded,
                        onExpandChange = { statusExpanded = it },
                        options = listOf("active", "inactive", "draft"),
                        onOptionSelected = { selectedStatus ->
                            viewModel.updateCreateItemForm { it.copy(status = selectedStatus) }
                        }
                    )
                }

                // ── Product Images ──
                if (!isStarterOrLight) {
                    AccordionSection(
                        icon = Icons.Outlined.Image,
                        title = "Product Images",
                        expanded = expandedSection == ItemSection.PRODUCT_IMAGES,
                        onHeaderClick = { viewModel.toggleSection(ItemSection.PRODUCT_IMAGES) }
                    ) {
                        val displayImage = formState.imageUri ?: formState.existingImageUrl
                        if (displayImage != null) {
                            Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(FieldShape)) {
                                AsyncImage(
                                    model = displayImage,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.Black.copy(alpha = 0.55f))
                                        .clickable {
                                            viewModel.updateCreateItemForm {
                                                it.copy(imageUri = null, existingImageUrl = null)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Tap to replace image",
                                fontSize = 12.sp,
                                color = AccentColor,
                                modifier = Modifier.clickable { imagePickerLauncher.launch("image/*") }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth().height(140.dp).clip(FieldShape)
                                    .background(Color(0xFFF7F7FA)).border(1.dp, BorderColor, FieldShape)
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.CloudUpload, null, tint = LabelColor, modifier = Modifier.size(26.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Click to upload", fontSize = 13.sp, color = TitleColor)
                                }
                            }
                        }
                    }
                }

                // ── Physical Attributes ──
                if (!isStarterOrLight) {
                    AccordionSection(
                        icon = Icons.Filled.Straighten,
                        title = "Physical Attributes",
                        expanded = expandedSection == ItemSection.PHYSICAL_ATTRIBUTES,
                        onHeaderClick = { viewModel.toggleSection(ItemSection.PHYSICAL_ATTRIBUTES) }
                    ) {
                        FormLabel("Dimensions (LxWxH)")
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.weight(1f)) {
                                FormTextField(
                                    value = formState.length,
                                    onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(length = v) } },
                                    placeholder = "L",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                FormTextField(
                                    value = formState.width,
                                    onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(width = v) } },
                                    placeholder = "W",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                FormTextField(
                                    value = formState.height,
                                    onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(height = v) } },
                                    placeholder = "H",
                                    keyboardType = KeyboardType.Number
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        FormLabel("Weight (kg)")
                        FormTextField(
                            value = formState.weight,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(weight = v) } },
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(Modifier.height(16.dp))
                        FormLabel("Manufacturer")
                        FormTextField(
                            value = formState.manufacturer,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(manufacturer = v) } },
                            placeholder = "Brand Name"
                        )
                        Spacer(Modifier.height(16.dp))
                        FormLabel("Brand")
                        FormTextField(
                            value = formState.brand,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(brand = v) } },
                            placeholder = "e.g. Apple"
                        )
                    }
                }

                // ── Tax Information ──
                if (!isStarterOrLight) {
                    AccordionSection(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        title = "Tax Information",
                        expanded = expandedSection == ItemSection.TAX_INFO,
                        onHeaderClick = { viewModel.toggleSection(ItemSection.TAX_INFO) }
                    ) {
                        FormLabel("HSN Code")
                        FormTextField(
                            value = formState.hsnCode,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(hsnCode = v) } },
                            placeholder = "HSN",
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(Modifier.height(16.dp))
                        FormLabel("Tax Percentage (%)")
                        FormTextField(
                            value = formState.taxPercentage,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(taxPercentage = v) } },
                            placeholder = "0",
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(Modifier.height(16.dp))
                        ToggleRow(
                            title = "Price is Tax Inclusive",
                            checked = formState.taxInclusive,
                            onCheckedChange = { isChecked -> viewModel.updateCreateItemForm { it.copy(taxInclusive = isChecked) } },
                            titleFirst = false
                        )
                    }
                }

                // ── Sales Information ──
                AccordionSection(
                    icon = Icons.Filled.LocalOffer,
                    title = "Sales Information",
                    expanded = expandedSection == ItemSection.SALES_INFO,
                    onHeaderClick = { viewModel.toggleSection(ItemSection.SALES_INFO) }
                ) {
                    FormLabel("Selling Price")
                    FormTextField(
                        value = formState.sellingPrice,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(sellingPrice = v) } },
                        placeholder = "0.00",
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Sales Account",
                        value = formState.salesAccount.ifBlank { "General Revenue" },
                        expanded = salesAccountExpanded,
                        onExpandChange = { salesAccountExpanded = it },
                        options = listOf("General Revenue"),
                        onOptionSelected = { v -> viewModel.updateCreateItemForm { it.copy(salesAccount = v) } }
                    )
                    Spacer(Modifier.height(16.dp))
                    FormLabel("Sales Description")
                    AppTextArea(
                        value = formState.salesDescription,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(salesDescription = v) } }
                    )
                }

                // ── Purchase Information ──
                AccordionSection(
                    icon = Icons.Filled.ShoppingCart,
                    title = "Purchase Information",
                    expanded = expandedSection == ItemSection.PURCHASE_INFO,
                    onHeaderClick = { viewModel.toggleSection(ItemSection.PURCHASE_INFO) }
                ) {
                    FormLabel("Cost Price")
                    FormTextField(
                        value = formState.costPrice,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(costPrice = v) } },
                        placeholder = "0.00",
                        keyboardType = KeyboardType.Number
                    )
                    Spacer(Modifier.height(16.dp))
                    FormDropdown(
                        label = "Purchase Account",
                        value = formState.purchaseAccount.ifBlank { "Cost of Goods Sold" },
                        expanded = purchaseAccountExpanded,
                        onExpandChange = { purchaseAccountExpanded = it },
                        options = listOf("Cost of Goods Sold"),
                        onOptionSelected = { v -> viewModel.updateCreateItemForm { it.copy(purchaseAccount = v) } }
                    )
                    Spacer(Modifier.height(16.dp))
                    FormLabel("Preferred Vendor")
                    FormTextField(
                        value = formState.preferredVendor,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(preferredVendor = v) } },
                        placeholder = "0.00"
                    )
                    Spacer(Modifier.height(16.dp))
                    FormLabel("Purchase Description")
                    AppTextArea(
                        value = formState.purchaseDescription,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(purchaseDescription = v) } }
                    )
                }
            }
        }

        // ── Floating Footer ──
        StepNavigationFab(
            showBack = true,
            onBack = {
                viewModel.resetCreateItemForm()
                onDismiss()
            },
            backLabel = "Cancel",
            trailingAction = TrailingFabAction.Update(
                isLoading = uiState is CreateItemUiState.Loading,
                label = if (isEditMode) "Update Item" else "Save Item",
                enabled = uiState !is CreateItemUiState.Loading,
                onClick = { viewModel.createInventoryItem(context) }
            )
        )
    }
}

@Composable
private fun AccordionSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (expanded) Color(0xFFF7F7FA) else Color.White)
                .clickable { onHeaderClick() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = AccentColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TitleColor)
            }
            Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = LabelColor)
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                content()
            }
        }
        HorizontalDivider(color = BorderColor)
    }
}

@Composable
private fun AppTextArea(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Write description...", color = PlaceholderColor) },
        shape = FieldShape,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = BorderColor,
            focusedBorderColor = AccentColor
        ),
        modifier = Modifier.fillMaxWidth().height(90.dp)
    )
}

@Composable
private fun SegmentedToggle(
    optionA: String, optionB: String,
    selectedA: Boolean,
    onSelectA: () -> Unit, onSelectB: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, FieldShape)
            .padding(4.dp)
    ) {
        SegmentedOption(optionA, selectedA, onSelectA, Modifier.weight(1f))
        SegmentedOption(optionB, !selectedA, onSelectB, Modifier.weight(1f))
    }
}

@Composable
private fun SegmentedOption(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(if (selected) AccentColor else Color.Transparent, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.White else LabelColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun ToggleRow(
    title: String, subtitle: String? = null,
    checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    titleFirst: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (titleFirst) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TitleColor)
                subtitle?.let { Text(it, fontSize = 12.sp, color = LabelColor) }
            }
            MiniSwitch(checked = checked, onCheckedChange = onCheckedChange)
        } else {
            MiniSwitch(checked = checked, onCheckedChange = onCheckedChange)
            Spacer(Modifier.width(10.dp))
            Text(title, fontSize = 13.sp, color = LabelColor)
        }
    }
}