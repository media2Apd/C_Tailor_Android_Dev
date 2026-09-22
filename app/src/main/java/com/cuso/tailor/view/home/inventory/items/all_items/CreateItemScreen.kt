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

package com.cuso.tailor.view.home.inventory.items.all_items

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.ItemType
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.view.composable.AccordionSection
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.ImageUploadSection
import com.cuso.tailor.view.composable.SettingsTabs
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction
import com.cuso.tailor.view.home.sales.lead.MiniSwitch
import com.cuso.tailor.viewmodel.CreateItemUiState
import com.cuso.tailor.viewmodel.FinanceViewModel
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.ItemSection
import com.cuso.tailor.viewmodel.ProfileUiState
import com.cuso.tailor.viewmodel.ProfileViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateItemScreen(
    itemId: String? = null,
    onDismiss: () -> Unit,
    onItemCreated: () -> Unit,
    isViewOnly: Boolean = false,
    viewModel: InventoryViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    salesFinanceViewModel: FinanceViewModel = hiltViewModel(key = "sales_finance_vm"),
    expenseFinanceViewModel: FinanceViewModel = hiltViewModel(key = "expense_finance_vm")
) {
    val tokens = LocalAppTokens.current
    val fieldShape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f)
    val isEditable = !isViewOnly
    val context = LocalContext.current

    val profileState by profileViewModel.uiState.collectAsState()
    val planName = (profileState as? ProfileUiState.Success)
        ?.data?.organization?.plan?.name.orEmpty()
    val isStarterOrLight = planName.equals("Starter", ignoreCase = true) ||
            planName.equals("Light", ignoreCase = true) || planName.equals("Plan not found", ignoreCase = true)

    val formState by viewModel.createItemForm.collectAsState()
    val expandedSection by viewModel.expandedSection.collectAsState()
    val uiState by viewModel.createItemUiState.collectAsState()

    // Observe item detail from both viewOne and selectedItem
    val selectedItemDetail by viewModel.selectedItem.collectAsState()
    val viewOneItem by viewModel.viewOneItem.collectAsState()

    // ── 1. Category Options (Settings: name, id) ──
    val productCategories by settingsViewModel.productCategories.collectAsState()
    val categoryOptions = remember(productCategories) {
        productCategories.mapNotNull { it.name.takeIf { name -> name.isNotBlank() } }
    }
    val selectedCategoryDisplayName = remember(formState.category, productCategories) {
        productCategories.find { it.id == formState.category }?.name
            ?: formState.category.ifBlank { "Select Category" }
    }

    // ── 2. Item Group Options (Inventory: name, id) ──
    val itemGroupState by viewModel.uiState.collectAsState()
    val itemGroupOptions = remember(itemGroupState.itemGroups) {
        itemGroupState.itemGroups.mapNotNull { it.name.takeIf { name -> name.isNotBlank() } }
    }
    val selectedItemGroupDisplayName = remember(formState.parentGroupId, itemGroupState.itemGroups) {
        itemGroupState.itemGroups.find { it.id == formState.parentGroupId }?.name
            ?: "Select Item Group (Optional)"
    }

    // ── 3. Tax Group Options (Inventory: name, id) ──
    val taxGroups by viewModel.taxGroups.collectAsState()
    val taxGroupOptions = remember(taxGroups) {
        taxGroups.mapNotNull { it.name.takeIf { name -> name.isNotBlank() } }
    }
    val selectedTaxGroupDisplayName = remember(formState.taxCategory, taxGroups) {
        taxGroups.find { it.id == formState.taxCategory || it.name.equals(formState.taxCategory, ignoreCase = true) }?.name
            ?: formState.taxCategory.ifBlank { "Select Tax Group" }
    }

    // ── 4. Sales Account Options (AccountDropdownItem: accountName, id) ──
    val salesAccounts by salesFinanceViewModel.accountDropdownList.collectAsState()
    val salesAccountOptions = remember(salesAccounts) {
        salesAccounts.mapNotNull { it.accountName.takeIf { name -> name.isNotBlank() } }
    }
    val selectedSalesAccountDisplayName = remember(formState.salesAccount, salesAccounts) {
        salesAccounts.find { it.id == formState.salesAccount }?.accountName
            ?: formState.salesAccount.ifBlank { "Select Sales Account" }
    }

    // ── 5. Purchase Account Options (AccountDropdownItem: accountName, id) ──
    val purchaseAccounts by expenseFinanceViewModel.accountDropdownList.collectAsState()
    val purchaseAccountOptions = remember(purchaseAccounts) {
        purchaseAccounts.mapNotNull { it.accountName.takeIf { name -> name.isNotBlank() } }
    }
    val selectedPurchaseAccountDisplayName = remember(formState.purchaseAccount, purchaseAccounts) {
        purchaseAccounts.find { it.id == formState.purchaseAccount }?.accountName
            ?: formState.purchaseAccount.ifBlank { "Select Purchase Account" }
    }

    // ── 6. Preferred Supplier Options (SupplierDropdownItem: label, value) ──
    val supplierDropdown by viewModel.supplierDropdown.collectAsState()
    val supplierOptions = remember(supplierDropdown) {
        supplierDropdown.mapNotNull { it.label.takeIf { label -> label.isNotBlank() } }
    }
    val selectedSupplierDisplayName = remember(formState.preferredVendor, supplierDropdown) {
        supplierDropdown.find { it.value == formState.preferredVendor }?.label
            ?: formState.preferredVendor.ifBlank { "Select Preferred Vendor" }
    }

    // ── Dropdown Expansion States ──
    var unitExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var itemGroupExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var taxGroupExpanded by remember { mutableStateOf(false) }
    var salesAccountExpanded by remember { mutableStateOf(false) }
    var purchaseAccountExpanded by remember { mutableStateOf(false) }
    var supplierExpanded by remember { mutableStateOf(false) }

    var currentErrorField by remember { mutableStateOf<String?>(null) }
    var currentError by remember { mutableStateOf<String?>(null) }
    var successToastMessage by remember { mutableStateOf<String?>(null) }

    val isEditMode = formState.itemId != null || !itemId.isNullOrBlank()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // Load dropdown catalogs on screen launch
    LaunchedEffect(Unit) {
        settingsViewModel.fetchProductCategories()
        viewModel.loadItemGroups()
        viewModel.fetchTaxGroups()
        viewModel.fetchSupplierDropdown()
        salesFinanceViewModel.fetchChartOfAccountsDropdown(context = "sales_line")
        expenseFinanceViewModel.fetchChartOfAccountsDropdown(context = "expense_line")
    }

    // Fetch existing item details if an ID is passed in edit mode
    LaunchedEffect(itemId) {
        Log.d("CreateItemScreen", ">> Initializing CreateItemScreen with itemId: '$itemId'")
        if (!itemId.isNullOrBlank()) {
            viewModel.updateCreateItemForm { it.copy(itemId = itemId) }
            viewModel.fetchInventoryViewOne(itemId)
            viewModel.fetchInventoryItemDetail(itemId)
        } else {
            Log.d("CreateItemScreen", ">> itemId is null, resetting form for create mode")
            viewModel.resetCreateItemForm()
        }
    }

    // Prefill form completely when item details arrive from either API call
    LaunchedEffect(viewOneItem, selectedItemDetail) {
        val currentItem: Any? = viewOneItem ?: selectedItemDetail
        if (!itemId.isNullOrBlank() && currentItem != null && formState.name.isBlank()) {
            Log.d("CreateItemScreen", ">> Prefilling all form fields for item ID: '$itemId'")

            // Invoke ViewModel prefill logic if viewOne is available
            viewOneItem?.let { viewModel.populateFormForEdit(it) }

            // Extract all fields dynamically from the API response
            val rawName = extractProperty(currentItem, "name")
            val rawSku = extractProperty(currentItem, "sku")
            val rawBarcode = extractProperty(currentItem, "barcode")
            val rawCategory = extractProperty(currentItem, "categoryId", "category")
            val rawUnit = extractProperty(currentItem, "unit")
            val rawType = extractProperty(currentItem, "type", "itemType")
            val rawStatus = extractProperty(currentItem, "status")
            val rawCostPrice = extractProperty(currentItem, "costPrice")
            val rawSellingPrice = extractProperty(currentItem, "sellingPrice")
            val rawManufacturer = extractProperty(currentItem, "manufacturer")
            val rawBrand = extractProperty(currentItem, "brand")
            val rawHsnCode = extractProperty(currentItem, "hsnCode")
            val rawTaxGroup = extractProperty(currentItem, "taxGroupId", "taxCategory")
            val rawSalesAccount = extractProperty(currentItem, "salesAccountId", "salesAccount")
            val rawPurchaseAccount = extractProperty(currentItem, "purchaseAccountId", "purchaseAccount")
            val rawPreferredVendor = extractProperty(currentItem, "preferredVendorId", "preferredVendor")
            val rawReturnable = extractProperty(currentItem, "returnable").toBoolean()
            val rawReorderLevel = extractProperty(currentItem, "reorderLevel")
            val rawSafetyStock = extractProperty(currentItem, "safetyStock")
            val rawParentGroupId = extractProperty(currentItem, "parentGroupId").takeIf { it.isNotBlank() }

            viewModel.updateCreateItemForm { current ->
                current.copy(
                    itemId = itemId,
                    name = current.name.ifBlank { rawName },
                    sku = current.sku.ifBlank { rawSku },
                    barcode = current.barcode.ifBlank { rawBarcode.ifBlank { "000000000024" } },
                    category = current.category.ifBlank { rawCategory },
                    unit = current.unit.ifBlank { rawUnit.ifBlank { "Meters" } },
                    itemType = if (rawType.equals("service", ignoreCase = true)) ItemType.SERVICE else ItemType.GOODS,
                    status = current.status.ifBlank { rawStatus.ifBlank { "active" } },
                    costPrice = current.costPrice.ifBlank { rawCostPrice },
                    sellingPrice = current.sellingPrice.ifBlank { rawSellingPrice },
                    manufacturer = current.manufacturer.ifBlank { rawManufacturer },
                    brand = current.brand.ifBlank { rawBrand },
                    hsnCode = current.hsnCode.ifBlank { rawHsnCode },
                    taxCategory = current.taxCategory.ifBlank { rawTaxGroup },
                    salesAccount = current.salesAccount.ifBlank { rawSalesAccount },
                    purchaseAccount = current.purchaseAccount.ifBlank { rawPurchaseAccount },
                    preferredVendor = current.preferredVendor.ifBlank { rawPreferredVendor },
                    returnable = rawReturnable,
                    reorderLevel = current.reorderLevel.ifBlank { rawReorderLevel },
                    safetyStock = current.safetyStock.ifBlank { rawSafetyStock },
                    parentGroupId = current.parentGroupId ?: rawParentGroupId,
                    autoGenerateSku = false
                )
            }
        }
    }

    // Auto-populate tax rate percentage when tax group is selected or prefilled
    LaunchedEffect(formState.taxCategory, taxGroups) {
        val matchingGroup = taxGroups.find {
            it.id == formState.taxCategory || it.name.equals(formState.taxCategory, ignoreCase = true)
        }
        if (matchingGroup != null && formState.taxPercentage.isBlank()) {
            viewModel.updateCreateItemForm { it.copy(taxPercentage = matchingGroup.totalRate.toString()) }
        }
    }

    // Handle submit states and notifications
    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateItemUiState.Success -> {
                successToastMessage = if (isEditMode) "Item updated successfully" else "Item created successfully"
                delay(1200)
                viewModel.resetCreateItemForm()
                onItemCreated()
            }
            is CreateItemUiState.Error -> {
                currentError = (uiState as CreateItemUiState.Error).message
            }
            else -> Unit
        }
    }

    fun validateForm(): Boolean {
        if (isViewOnly) return true

        val missingField = when {
            formState.name.isBlank() -> "itemName" to "Item name is required"
            formState.unit.isBlank() -> "unit" to "Unit of measure is required"
            formState.category.isBlank() -> "category" to "Category is required"
            formState.sellingPrice.isBlank() -> "sellingPrice" to "Selling price is required"
            formState.costPrice.isBlank() -> "costPrice" to "Cost price is required"
            else -> null
        }

        currentErrorField = missingField?.first

        if (missingField != null) {
            when (missingField.first) {
                "itemName", "unit", "category" -> viewModel.toggleSection(ItemSection.ITEM_IDENTITY)
                "sellingPrice" -> viewModel.toggleSection(ItemSection.SALES_INFO)
                "costPrice" -> viewModel.toggleSection(ItemSection.PURCHASE_INFO)
            }
            currentError = missingField.second
            return false
        }
        return true
    }

    val screenTitle = when {
        isViewOnly -> "View Item"
        isEditMode -> "Edit Item"
        else -> "Create Item"
    }

    val handleDismiss = {
        if (uiState !is CreateItemUiState.Loading) {
            viewModel.resetCreateItemForm()
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBar(
                title = screenTitle,
                onClose = { handleDismiss() }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = tokens.buttonHeight * 2.2f)
            ) {
                // ── 1. Item Identity ──
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
                        selectedIndex = if (formState.itemType == ItemType.GOODS) 0 else 1,
                        onTabSelected = { index ->
                            if (isEditable) {
                                viewModel.updateCreateItemForm {
                                    it.copy(itemType = if (index == 0) ItemType.GOODS else ItemType.SERVICE)
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Item Name", isRequired = true)
                    FormTextField(
                        value = formState.name,
                        onValueChange = { newValue ->
                            viewModel.onItemNameChanged(newValue)
                        },
                        placeholder = "e.g. Rajasthani Silk Shirt - Gray / XL",
                        enabled = isEditable,
                        isError = currentErrorField == "itemName",
                        errorMessage = if (currentErrorField == "itemName") "Item name is required" else null
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("SKU", isRequired = !formState.autoGenerateSku)
                    FormTextField(
                        value = formState.sku,
                        onValueChange = { newSku ->
                            if (!formState.autoGenerateSku && isEditable) {
                                viewModel.updateCreateItemForm { it.copy(sku = newSku.uppercase()) }
                            }
                        },
                        placeholder = if (formState.autoGenerateSku) "Auto-generated SKU" else "e.g. RAJ-GRAY-XL",
                        enabled = isEditable && !formState.autoGenerateSku
                    )

                    if (!isEditMode && isEditable) {
                        Spacer(Modifier.height(8.dp))
                        ToggleRow(
                            title = "Auto-generate SKU",
                            checked = formState.autoGenerateSku,
                            enabled = isEditable,
                            onCheckedChange = { isChecked ->
                                viewModel.onAutoGenerateSkuToggle(isChecked)
                            },
                            titleFirst = false
                        )
                    }

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Unit of Measure", isRequired = true)
                    FormDropdown(
                        value = formState.unit.ifBlank { "Select Unit" },
                        expanded = if (isEditable) unitExpanded else false,
                        onExpandChange = { if (isEditable) unitExpanded = it },
                        options = listOf("Meter", "Meters", "Piece", "Pieces", "Kg"),
                        onOptionSelected = { selectedUnit ->
                            viewModel.updateCreateItemForm { it.copy(unit = selectedUnit) }
                        },
                        isError = currentErrorField == "unit",
                        errorMessage = if (currentErrorField == "unit") "Unit is required" else null
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
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

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Category", isRequired = true)
                    FormDropdown(
                        value = selectedCategoryDisplayName,
                        expanded = if (isEditable) categoryExpanded else false,
                        onExpandChange = { if (isEditable) categoryExpanded = it },
                        options = categoryOptions.ifEmpty { listOf("No Categories Available") },
                        onOptionSelected = { selectedCategoryName ->
                            val selectedCategoryId = productCategories
                                .find { it.name.equals(selectedCategoryName, ignoreCase = true) }
                                ?.id
                                .orEmpty()
                            viewModel.updateCreateItemForm { it.copy(category = selectedCategoryId) }
                        },
                        isRequired = true,
                        isError = currentErrorField == "category",
                        errorMessage = if (currentErrorField == "category") "Category is required" else null
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Item Group (Optional)")
                    FormDropdown(
                        value = selectedItemGroupDisplayName,
                        expanded = if (isEditable) itemGroupExpanded else false,
                        onExpandChange = { if (isEditable) itemGroupExpanded = it },
                        options = listOf("None") + itemGroupOptions,
                        onOptionSelected = { selectedGroupName ->
                            val selectedGroupId = if (selectedGroupName == "None") {
                                null
                            } else {
                                itemGroupState.itemGroups.find { it.name.equals(selectedGroupName, ignoreCase = true) }?.id
                            }
                            viewModel.updateCreateItemForm { it.copy(parentGroupId = selectedGroupId) }
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Status")
                    FormDropdown(
                        value = formState.status.ifBlank { "Select Status" },
                        expanded = if (isEditable) statusExpanded else false,
                        onExpandChange = { if (isEditable) statusExpanded = it },
                        options = listOf("active", "inactive", "draft"),
                        onOptionSelected = { selectedStatus ->
                            viewModel.updateCreateItemForm { it.copy(status = selectedStatus) }
                        }
                    )
                }

                // ── 2. Product Images ──
                if (!isStarterOrLight) {
                    AccordionSection(
                        iconPainter = painterResource(R.drawable.box),
                        title = "Product Images",
                        expanded = expandedSection == ItemSection.PRODUCT_IMAGES,
                        onHeaderClick = { viewModel.toggleSection(ItemSection.PRODUCT_IMAGES) }
                    ) {
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
                                }
                            },
                            onRemoveImage = {
                                if (isEditable) {
                                    viewModel.updateCreateItemForm {
                                        it.copy(imageUri = null, existingImageUrl = null)
                                    }
                                }
                            },
                            uploadBoxHeight = if (isEditable) 90.dp else 0.dp,
                            imagePreviewSize = 90.dp,
                            previewHeaderTitle = "ATTACHED IMAGE"
                        )
                    }
                }

                // ── 3. Physical Attributes ──
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
                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Weight (kg)")
                        FormTextField(
                            value = formState.weight,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(weight = v) } },
                            placeholder = "0.00",
                            enabled = isEditable,
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Manufacturer")
                        FormTextField(
                            value = formState.manufacturer,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(manufacturer = v) } },
                            placeholder = "Brand Name",
                            enabled = isEditable
                        )
                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Brand")
                        FormTextField(
                            value = formState.brand,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(brand = v) } },
                            placeholder = "e.g. Apple",
                            enabled = isEditable
                        )
                    }
                }

                // ── 4. Tax Information ──
                if (!isStarterOrLight) {
                    AccordionSection(
                        iconPainter = painterResource(R.drawable.ic_transaction_sheet),
                        title = "Tax Information",
                        expanded = expandedSection == ItemSection.TAX_INFO,
                        onHeaderClick = { viewModel.toggleSection(ItemSection.TAX_INFO) }
                    ) {
                        val isGoods = formState.itemType == ItemType.GOODS
                        FormLabel(if (isGoods) "HSN Code" else "SAC Code")
                        FormTextField(
                            value = formState.hsnCode,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(hsnCode = v) } },
                            placeholder = if (isGoods) "HSN" else "SAC",
                            enabled = isEditable,
                            keyboardType = KeyboardType.Number
                        )
                        Spacer(Modifier.height(tokens.extraPadding))

                        FormLabel("Tax Group")
                        FormDropdown(
                            value = selectedTaxGroupDisplayName,
                            expanded = if (isEditable) taxGroupExpanded else false,
                            onExpandChange = { if (isEditable) taxGroupExpanded = it },
                            options = taxGroupOptions.ifEmpty { listOf("No Tax Groups Available") },
                            onOptionSelected = { chosenGroupName ->
                                val selectedGroup = taxGroups.find { it.name == chosenGroupName }
                                if (selectedGroup != null) {
                                    viewModel.updateCreateItemForm {
                                        it.copy(
                                            taxCategory = selectedGroup.id,
                                            taxPercentage = selectedGroup.totalRate.toString()
                                        )
                                    }
                                }
                            }
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Tax Percentage (%)")
                        FormTextField(
                            value = formState.taxPercentage,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(taxPercentage = v) } },
                            placeholder = "0",
                            enabled = isEditable,
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        ToggleRow(
                            title = "Price is Tax Inclusive",
                            checked = formState.taxInclusive,
                            enabled = isEditable,
                            onCheckedChange = { isChecked -> viewModel.updateCreateItemForm { it.copy(taxInclusive = isChecked) } },
                            titleFirst = false
                        )
                    }
                }

                // ── 5. Sales Information ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_tag),
                    title = "Sales Information",
                    expanded = expandedSection == ItemSection.SALES_INFO,
                    onHeaderClick = { viewModel.toggleSection(ItemSection.SALES_INFO) }
                ) {
                    FormLabel("Selling Price", isRequired = true)
                    FormTextField(
                        value = formState.sellingPrice,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(sellingPrice = v) } },
                        placeholder = "Enter Selling Price",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Number,
                        isError = currentErrorField == "sellingPrice",
                        errorMessage = if (currentErrorField == "sellingPrice") "Selling price is required" else null
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Sales Account")
                    FormDropdown(
                        value = selectedSalesAccountDisplayName,
                        expanded = if (isEditable) salesAccountExpanded else false,
                        onExpandChange = { if (isEditable) salesAccountExpanded = it },
                        options = salesAccountOptions.ifEmpty { listOf("No Sales Accounts Available") },
                        onOptionSelected = { chosenAccountName ->
                            val chosenAccountId = salesAccounts.find { it.accountName == chosenAccountName }?.id
                                ?: chosenAccountName
                            viewModel.updateCreateItemForm { it.copy(salesAccount = chosenAccountId) }
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Sales Description")
                    FormTextArea(
                        value = formState.salesDescription,
                        enabled = isEditable,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(salesDescription = v) } }
                    )
                }

                // ── 6. Purchase Information ──
                AccordionSection(
                    iconPainter = painterResource(R.drawable.cart),
                    title = "Purchase Information",
                    expanded = expandedSection == ItemSection.PURCHASE_INFO,
                    onHeaderClick = { viewModel.toggleSection(ItemSection.PURCHASE_INFO) }
                ) {
                    FormLabel("Cost Price", isRequired = true)
                    FormTextField(
                        value = formState.costPrice,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(costPrice = v) } },
                        placeholder = "0.00",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Number,
                        isError = currentErrorField == "costPrice",
                        errorMessage = if (currentErrorField == "costPrice") "Cost price is required" else null
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Purchase Account")
                    FormDropdown(
                        value = selectedPurchaseAccountDisplayName,
                        expanded = if (isEditable) purchaseAccountExpanded else false,
                        onExpandChange = { if (isEditable) purchaseAccountExpanded = it },
                        options = purchaseAccountOptions.ifEmpty { listOf("No Purchase Accounts Available") },
                        onOptionSelected = { chosenAccountName ->
                            val chosenAccountId = purchaseAccounts.find { it.accountName == chosenAccountName }?.id
                                ?: chosenAccountName
                            viewModel.updateCreateItemForm { it.copy(purchaseAccount = chosenAccountId) }
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Preferred Vendor")
                    FormDropdown(
                        value = selectedSupplierDisplayName,
                        expanded = if (isEditable) supplierExpanded else false,
                        onExpandChange = { if (isEditable) supplierExpanded = it },
                        options = supplierOptions.ifEmpty { listOf("No Suppliers Available") },
                        onOptionSelected = { chosenSupplierLabel ->
                            val chosenSupplierValue = supplierDropdown.find { it.label == chosenSupplierLabel }?.value
                                ?: chosenSupplierLabel
                            viewModel.updateCreateItemForm { it.copy(preferredVendor = chosenSupplierValue) }
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Purchase Description")
                    FormTextArea(
                        value = formState.purchaseDescription,
                        enabled = isEditable,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(purchaseDescription = v) } }
                    )
                }
            }
        }

        // ── Floating Notifications ──
        DynamicIslandError(
            message = currentError,
            onDismiss = { currentError = null }
        )

        DynamicIslandSuccess(
            message = successToastMessage,
            onDismiss = { successToastMessage = null }
        )

        // ── Floating Action Buttons ──
        StepNavigationFab(
            showBack = true,
            onBack = { handleDismiss() },
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
                        if (validateForm() && uiState !is CreateItemUiState.Loading) {
                            if (isEditMode) {
                                viewModel.updateInventoryItem(context)
                            } else {
                                viewModel.createInventoryItem(context)
                            }
                        }
                    }
                )
            }
        )
    }
}

// =============================================================================
// PROPERTY REFLECTION HELPER
// =============================================================================

/**
 * Extracts string values from target models safely via getter methods or declared fields.
 */
private fun extractProperty(target: Any?, vararg candidateNames: String): String {
    if (target == null) return ""
    for (name in candidateNames) {
        try {
            val getterName = "get" + name.replaceFirstChar { it.uppercase() }
            val method = target.javaClass.methods.firstOrNull {
                it.name.equals(getterName, ignoreCase = true) || it.name.equals(name, ignoreCase = true)
            }
            val result = method?.invoke(target)?.toString()
            if (!result.isNullOrBlank()) return result
        } catch (_: Exception) {}
        try {
            val field = target.javaClass.declaredFields.firstOrNull { it.name.equals(name, ignoreCase = true) }
            field?.isAccessible = true
            val result = field?.get(target)?.toString()
            if (!result.isNullOrBlank()) return result
        } catch (_: Exception) {}
    }
    return ""
}

// =============================================================================
// TOGGLE ROW HELPER
// =============================================================================

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
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) TextPrimary else TextPrimary.copy(alpha = 0.6f)
                )
                subtitle?.let {
                    Text(
                        text = it,
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                }
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
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                color = if (enabled) TextSecondary else TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}