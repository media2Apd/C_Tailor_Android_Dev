@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused",
    "SameParameterValue"
)

package com.cuso.mobile.view.home.inventory.items.all_items

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.ItemType
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.viewmodel.CreateItemUiState
import com.cuso.mobile.viewmodel.InventoryViewModel
import com.cuso.mobile.viewmodel.ItemSection
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.ProfileViewModel
import com.cuso.mobile.view.composable.AccordionSection
import com.cuso.mobile.view.composable.SettingsTabs
import com.cuso.mobile.view.composable.TabItem
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.ProfileUiState
import com.cuso.mobile.R
import com.cuso.mobile.view.composable.FormTextArea
import com.cuso.mobile.view.composable.ImageUploadSection

// ── Design tokens ──
private val AccentColor = Color(0xFF4F39F6)
private val BorderColor = Color(0xFFE3E4E8)
private val LabelColor = Color(0xFF6B7280)
private val TitleColor = Color(0xFF111827)
private val PlaceholderColor = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    onDismiss: () -> Unit,
    onItemCreated: () -> Unit,
    isViewOnly: Boolean = false, // Set to true for View Mode
    viewModel: InventoryViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val FieldShape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f)

    val isEditable = !isViewOnly

    val profileState by profileViewModel.uiState.collectAsState()
    val planName = (profileState as? ProfileUiState.Success)
        ?.data?.organization?.plan?.name.orEmpty()
    val isStarterOrLight = planName.equals("Starter", ignoreCase = true) ||
            planName.equals("Light", ignoreCase = true) || planName.equals("Plan not found", ignoreCase = true)

    val context = LocalContext.current
    val formState by viewModel.createItemForm.collectAsState()
    val expandedSection by viewModel.expandedSection.collectAsState()
    val uiState by viewModel.createItemUiState.collectAsState()

    var unitExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var salesAccountExpanded by remember { mutableStateOf(false) }
    var purchaseAccountExpanded by remember { mutableStateOf(false) }

    var currentErrorField by remember { mutableStateOf<String?>(null) }
    var currentError by remember { mutableStateOf<String?>(null) }

    fun customError(message: String) {
        currentError = message
    }
    val isEditMode = formState.itemId != null

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    LaunchedEffect(uiState) {
        if (uiState is CreateItemUiState.Success) {
            onItemCreated()
        }
    }

    fun validateForm(): Boolean {
        if (isViewOnly) return true

        val missingField = when {
            formState.name.isBlank() -> "itemName" to "Item name is required"
            formState.unit.isBlank() -> "unit" to "Unit of measure is required"
            formState.category.isBlank() -> "category" to "Category is required"
            formState.sellingPrice.isBlank() -> "sellingPrice" to "Selling price is required"
            formState.salesAccount.isBlank() -> "salesAccount" to "Sales account is required"
            formState.costPrice.isBlank() -> "costPrice" to "Cost price is required"
            formState.purchaseAccount.isBlank() -> "purchaseAccount" to "Purchase account is required"
            else -> null
        }

        currentErrorField = missingField?.first

        if (missingField != null) {
            when (missingField.first) {
                "itemName", "unit", "category" -> viewModel.toggleSection(ItemSection.ITEM_IDENTITY)
                "sellingPrice", "salesAccount" -> viewModel.toggleSection(ItemSection.SALES_INFO)
                "costPrice", "purchaseAccount" -> viewModel.toggleSection(ItemSection.PURCHASE_INFO)
            }
            customError(missingField.second)
            return false
        }
        return true
    }

    val screenTitle = when {
        isViewOnly -> "View Item"
        isEditMode -> "Edit Item"
        else -> "Create Item"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // ── Header ──
            TitleBar(screenTitle, onClose = onDismiss)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = tokens.buttonHeight * 2f)
            ) {
                // ── Item Identity ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.box),
                    title = "Item Identity",
                    expanded = expandedSection == ItemSection.ITEM_IDENTITY,
                    onHeaderClick = { viewModel.toggleSection(ItemSection.ITEM_IDENTITY) }
                ) {
                    FormLabel("Item Type")
                    val itemTypeTabs = remember {
                        listOf(
                            TabItem(label = "Goods"),
                            TabItem(label = "Service")
                        )
                    }
                    SettingsTabs(
                        tabs = itemTypeTabs,
                        selectedIndex = if (formState.itemType == ItemType.IN_HOUSE) 0 else 1,
                        onTabSelected = { index ->
                            if (isEditable) {
                                viewModel.updateCreateItemForm {
                                    it.copy(itemType = if (index == 0) ItemType.IN_HOUSE else ItemType.CLIENT)
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormLabel("Item Name")
                    FormTextField(
                        value = formState.name,
                        onValueChange = { newValue ->
                            viewModel.updateCreateItemForm { it.copy(name = newValue) }
                        },
                        placeholder = "e.g. Premium Woolen Fabric",
                        enabled = isEditable,
                        isError = currentErrorField == "itemName",
                        errorMessage = if (currentErrorField == "itemName") "Item name is required" else null
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormLabel("SKU")
                    FormTextField(
                        value = formState.sku,
                        onValueChange = { /* read only */ },
                        placeholder = "Auto-generated SKU",
                        enabled = false
                    )

                    if (!isEditMode && isEditable) {
                        Spacer(Modifier.height(8.dp))
                        ToggleRow(
                            title = "Auto-generate SKU",
                            checked = formState.autoGenerateSku,
                            enabled = isEditable,
                            onCheckedChange = { checked -> viewModel.onAutoGenerateSkuToggle(checked) },
                            titleFirst = false
                        )
                    }

                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormDropdown(
                        label = "Unit of Measure",
                        value = formState.unit.ifBlank { "Select Unit" },
                        expanded = if (isEditable) unitExpanded else false,
                        onExpandChange = { if (isEditable) unitExpanded = it },
                        options = listOf("Meter", "Piece", "Kg"),
                        onOptionSelected = { selectedUnit ->
                            viewModel.updateCreateItemForm { it.copy(unit = selectedUnit) }
                        },
                        isError = currentErrorField == "unit",
                        errorMessage = if (currentErrorField == "unit") "Unit is required" else null
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    ToggleRow(
                        title = "Returnable Item",
                        subtitle = "Customer can request return/refund",
                        checked = formState.returnable,
                        enabled = isEditable,
                        onCheckedChange = { isChecked ->
                            viewModel.updateCreateItemForm { it.copy(returnable = isChecked) }
                        },
                        titleFirst = true
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormLabel("Category")
                    FormTextField(
                        value = formState.category,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(category = v) } },
                        placeholder = "category",
                        enabled = isEditable,
                        isError = currentErrorField == "category",
                        errorMessage = if (currentErrorField == "category") "Category is required" else null
                    )

                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormDropdown(
                        label = "Status",
                        value = formState.status.ifBlank { "Select Status" },
                        expanded = if (isEditable) statusExpanded else false,
                        onExpandChange = { if (isEditable) statusExpanded = it },
                        options = listOf("active", "inactive", "draft"),
                        onOptionSelected = { selectedStatus ->
                            viewModel.updateCreateItemForm { it.copy(status = selectedStatus) }
                        }
                    )
                }

                // ── Product Images ──
                if (!isStarterOrLight) {
                    // ── Product Images ──
                    AccordionSection(
                        iconPainter = painterResource(R.drawable.box),
                        title = "Product Images",
                        expanded = expandedSection == ItemSection.PRODUCT_IMAGES,
                        onHeaderClick = { viewModel.toggleSection(ItemSection.PRODUCT_IMAGES) }
                    ) {
                        // Collect current image selection (Uri or remote URL string) into a list for ImageUploadSection
                        val selectedImagesList: List<Any> = remember(formState.imageUri, formState.existingImageUrl) {
                            listOfNotNull(formState.imageUri ?: formState.existingImageUrl)
                        }

                        ImageUploadSection(
                            isImage = true,
                            selectedImages = selectedImagesList,
                            browseText = if (isEditable) "Browse Image" else "View Only",
                            onBrowseClick = {
                                if (isEditable) {
                                    imagePickerLauncher.launch("image/*")
                                } },
                            onRemoveImage = {
                                if (isEditable) {
                                    viewModel.updateCreateItemForm {
                                        it.copy(imageUri = null, existingImageUrl = null)
                                    }
                                } },
                            uploadBoxHeight = if (isEditable) 90.dp else 0.dp,
                            imagePreviewSize = 90.dp,
                            previewHeaderTitle = "ATTACHED IMAGE"
                        )
                    }

                }

                // ── Physical Attributes ──
                if (!isStarterOrLight) {
                    AccordionSection(
                        iconPainter = painterResource(R.drawable.box),
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
                                    enabled = isEditable,
                                    keyboardType = KeyboardType.Number
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                FormTextField(
                                    value = formState.width,
                                    onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(width = v) } },
                                    placeholder = "W",
                                    enabled = isEditable,
                                    keyboardType = KeyboardType.Number
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                FormTextField(
                                    value = formState.height,
                                    onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(height = v) } },
                                    placeholder = "H",
                                    enabled = isEditable,
                                    keyboardType = KeyboardType.Number
                                )
                            }
                        }
                        Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                        FormLabel("Weight (kg)")
                        FormTextField(
                            value = formState.weight,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(weight = v) } },
                            placeholder = "0.00",
                            enabled = isEditable,
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                        FormLabel("Manufacturer")
                        FormTextField(
                            value = formState.manufacturer,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(manufacturer = v) } },
                            placeholder = "Brand Name",
                            enabled = isEditable
                        )
                        Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                        FormLabel("Brand")
                        FormTextField(
                            value = formState.brand,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(brand = v) } },
                            placeholder = "e.g. Apple",
                            enabled = isEditable
                        )
                    }
                }

                // ── Tax Information ──
                if (!isStarterOrLight) {
                    AccordionSection(
                        iconPainter = painterResource(R.drawable.ic_transaction_sheet),
                        title = "Tax Information",
                        expanded = expandedSection == ItemSection.TAX_INFO,
                        onHeaderClick = { viewModel.toggleSection(ItemSection.TAX_INFO) }
                    ) {
                        FormLabel("HSN Code")
                        FormTextField(
                            value = formState.hsnCode,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(hsnCode = v) } },
                            placeholder = "HSN",
                            enabled = isEditable,
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                        FormLabel("Tax Percentage (%)")
                        FormTextField(
                            value = formState.taxPercentage,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(taxPercentage = v) } },
                            placeholder = "0",
                            enabled = isEditable,
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                        ToggleRow(
                            title = "Price is Tax Inclusive",
                            checked = formState.taxInclusive,
                            enabled = isEditable,
                            onCheckedChange = { isChecked -> viewModel.updateCreateItemForm { it.copy(taxInclusive = isChecked) } },
                            titleFirst = false
                        )
                    }
                }

                // ── Sales Information ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_tag),
                    title = "Sales Information",
                    expanded = expandedSection == ItemSection.SALES_INFO,
                    onHeaderClick = { viewModel.toggleSection(ItemSection.SALES_INFO) }
                ) {
                    FormLabel("Selling Price")
                    FormTextField(
                        value = formState.sellingPrice,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(sellingPrice = v) } },
                        placeholder = "Enter Selling Price",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Number,
                        isError = currentErrorField == "sellingPrice",
                        errorMessage = if (currentErrorField == "sellingPrice") "Selling price is required" else null
                    )
                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormDropdown(
                        label = "Sales Account",
                        value = formState.salesAccount.ifBlank { "Select Sales Account" },
                        expanded = if (isEditable) salesAccountExpanded else false,
                        onExpandChange = { if (isEditable) salesAccountExpanded = it },
                        options = listOf("General Revenue"),
                        onOptionSelected = { v -> viewModel.updateCreateItemForm { it.copy(salesAccount = v) } },
                        isError = currentErrorField == "salesAccount",
                        errorMessage = if (currentErrorField == "salesAccount") "Sales account is required" else null
                    )
                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormLabel("Sales Description")
                    FormTextArea(
                        value = formState.salesDescription,
                        enabled = isEditable,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(salesDescription = v) } }
                    )
                }

                // ── Purchase Information ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.cart),
                    title = "Purchase Information",
                    expanded = expandedSection == ItemSection.PURCHASE_INFO,
                    onHeaderClick = { viewModel.toggleSection(ItemSection.PURCHASE_INFO) }
                ) {
                    FormLabel("Cost Price")
                    FormTextField(
                        value = formState.costPrice,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(costPrice = v) } },
                        placeholder = "0.00",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Number,
                        isError = currentErrorField == "costPrice",
                        errorMessage = if (currentErrorField == "costPrice") "Cost price is required" else null
                    )
                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormDropdown(
                        label = "Purchase Account",
                        value = formState.purchaseAccount.ifBlank { "Select Purchase Account" },
                        expanded = if (isEditable) purchaseAccountExpanded else false,
                        onExpandChange = { if (isEditable) purchaseAccountExpanded = it },
                        options = listOf("Cost of Goods Sold"),
                        onOptionSelected = { v -> viewModel.updateCreateItemForm { it.copy(purchaseAccount = v) } },
                        isError = currentErrorField == "purchaseAccount",
                        errorMessage = if (currentErrorField == "purchaseAccount") "Purchase account is required" else null
                    )
                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormLabel("Preferred Vendor")
                    FormTextField(
                        value = formState.preferredVendor,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(preferredVendor = v) } },
                        placeholder = "0.00",
                        enabled = isEditable
                    )
                    Spacer(Modifier.height(tokens.screenPadding * 0.8f))
                    FormLabel("Purchase Description")
                    FormTextArea(
                        value = formState.purchaseDescription,
                        enabled = isEditable,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(purchaseDescription = v) } }
                    )
                }
            }
        }

        DynamicIslandError(
            modifier = Modifier.align(Alignment.TopCenter),
            message = currentError,
            onDismiss = { currentError = null }
        )

        // ── Floating Footer ──
        StepNavigationFab(
            showBack = true,
            onBack = {
                viewModel.resetCreateItemForm()
                onDismiss()
            },
            showBackArrow = false,
            backLabel = if (isViewOnly) "Close" else "Cancel",
            trailingAction = if (isViewOnly) {
                null
            } else {
                TrailingFabAction.Update(
                    isLoading = uiState is CreateItemUiState.Loading,
                    label = if (isEditMode) "Update Item" else "Save Item",
                    enabled = uiState !is CreateItemUiState.Loading,
                    onClick = {
                        if (validateForm()) {
                            viewModel.createInventoryItem(context)
                        }
                    }
                )
            }
        )
    }
}



@Composable
private fun ToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    titleFirst: Boolean
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (titleFirst) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = if (enabled) TitleColor else TitleColor.copy(alpha = 0.6f))
                subtitle?.let { Text(it, fontSize = tokens.caption, color = LabelColor) }
            }
            MiniSwitch(
                checked = checked,
                onCheckedChange = { if (enabled) onCheckedChange(it) }
            )
        } else {
            MiniSwitch(
                checked = checked,
                onCheckedChange = { if (enabled) onCheckedChange(it) }
            )
            Spacer(Modifier.width(10.dp))
            Text(title, fontSize = tokens.bodySmall, color = if (enabled) LabelColor else LabelColor.copy(alpha = 0.6f))
        }
    }
}