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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.SupplierDropdownItem
import com.cuso.tailor.model.inventory.VariantSelection
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.AccordionSection
import com.cuso.tailor.view.composable.AppRadioButton
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
import java.util.Locale

private val STANDARD_SIZES = listOf("XS", "S", "M", "L", "XL", "XXL", "XXXL", "Free Size")

private val UNIT_OPTIONS = listOf(
    "Pieces",
    "Meters",
    "Centimeters",
    "Inches",
    "Kilograms",
    "Grams",
    "Liters",
    "Milliliters",
    "Boxes",
    "Packs",
    "Pairs",
    "Dozens",
    "Rolls",
    "Bundles",
    "Sets"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val isEditable = !isViewOnly
    val context = LocalContext.current

    val profileState by profileViewModel.uiState.collectAsState()
    val planName = (profileState as? ProfileUiState.Success)
        ?.data?.organization?.plan?.name.orEmpty()
    val isStarterOrLight = planName.equals("Starter", ignoreCase = true) ||
            planName.equals("Light", ignoreCase = true) ||
            planName.equals("Plan not found", ignoreCase = true)

    val formState by viewModel.createItemForm.collectAsState()
    val expandedSection by viewModel.expandedSection.collectAsState()
    val uiState by viewModel.createItemUiState.collectAsState()

    val selectedItemDetail by viewModel.selectedItem.collectAsState()
    val viewOneItem by viewModel.viewOneItem.collectAsState()

    val hasParentGroup = !formState.parentGroupId.isNullOrBlank()
    val isGroupInheritedEditable = isEditable && !hasParentGroup
    val isGoods = !formState.itemType.equals("service", ignoreCase = true)

    // Local UI states for fields not yet present in CreateItemFormState
    var sacCode by remember { mutableStateOf("") }
    var fabric by remember { mutableStateOf(false) }
    var fabricType by remember { mutableStateOf("") }
    var openingStockRate by remember { mutableStateOf("") }
    var tagsText by remember { mutableStateOf("") }

    // Derive Size and Color directly from formState.variantSelections
    val selectedSize = remember(formState.variantSelections) {
        val rawSize = formState.variantSelections
            .find { it.name.equals("Size", ignoreCase = true) }
            ?.value
            .orEmpty()
        STANDARD_SIZES.find { it.equals(rawSize.trim(), ignoreCase = true) } ?: ""
    }

    val colorText = remember(formState.variantSelections) {
        formState.variantSelections
            .find { it.name.equals("Color", ignoreCase = true) }
            ?.value
            .orEmpty()
            .filter { !it.isDigit() }
    }

    // Local expansion state for Inventory Tracking section
    var inventorySectionExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(expandedSection) {
        inventorySectionExpanded = false
    }

    // Sync inventory tracking behavior with Item Type
    LaunchedEffect(formState.itemType) {
        if (formState.itemType.equals("service", ignoreCase = true)) {
            openingStockRate = ""
            viewModel.updateCreateItemForm {
                it.copy(
                    trackInventory = false,
                    isSerialTracked = false,
                    openingStock = ""
                )
            }
        } else {
            viewModel.updateCreateItemForm { it.copy(trackInventory = true) }
        }
    }

    // Category options
    val productCategories by settingsViewModel.productCategories.collectAsState()
    val categoryOptions = remember(productCategories) {
        productCategories.mapNotNull { it.name.takeIf { name -> name.isNotBlank() } }
    }
    val selectedCategoryDisplayName = remember(formState.category, productCategories) {
        val matchedCategory = productCategories.find {
            it.id.equals(formState.category, ignoreCase = true) ||
                    it.name.equals(formState.category, ignoreCase = true)
        }
        matchedCategory?.name ?: formState.category.ifBlank { "Select Category" }
    }

    // Item Group options
    val itemGroupState by viewModel.uiState.collectAsState()
    val itemGroupOptions = remember(itemGroupState.itemGroups) {
        itemGroupState.itemGroups.mapNotNull { it.name.takeIf { name -> name.isNotBlank() } }
    }
    val selectedItemGroupDisplayName = remember(formState.parentGroupId, itemGroupState.itemGroups) {
        itemGroupState.itemGroups.find { it.id == formState.parentGroupId }?.name
            ?: "Select Item Group (Optional)"
    }

    // Tax Group options
    val taxGroups by viewModel.taxGroups.collectAsState()
    val taxGroupOptions = remember(taxGroups) {
        taxGroups.mapNotNull { it.name.takeIf { name -> name.isNotBlank() } }
    }
    val selectedTaxGroupDisplayName = remember(formState.taxCategory, taxGroups) {
        taxGroups.find {
            it.id == formState.taxCategory || it.name.equals(formState.taxCategory, ignoreCase = true)
        }?.name ?: if (formState.taxCategory.isBlank() || formState.taxCategory == "GST 5%") {
            "Select Tax Group"
        } else {
            formState.taxCategory
        }
    }

    // Sales & Inventory account options
    val salesAccounts by salesFinanceViewModel.accountDropdownList.collectAsState()
    val salesAccountOptions = remember(salesAccounts) {
        salesAccounts.mapNotNull { it.accountName.takeIf { name -> name.isNotBlank() } }
    }
    val selectedSalesAccountDisplayName = remember(formState.salesAccount, salesAccounts) {
        salesAccounts.find { it.id == formState.salesAccount }?.accountName
            ?: formState.salesAccount.ifBlank { "Select Sales Account" }
    }
    val selectedInventoryAccountDisplayName = remember(formState.inventoryAccount, salesAccounts) {
        salesAccounts.find { it.id == formState.inventoryAccount }?.accountName
            ?: formState.inventoryAccount.ifBlank { "Select Inventory Account" }
    }

    // Purchase account options
    val purchaseAccounts by expenseFinanceViewModel.accountDropdownList.collectAsState()
    val purchaseAccountOptions = remember(purchaseAccounts) {
        purchaseAccounts.mapNotNull { it.accountName.takeIf { name -> name.isNotBlank() } }
    }
    val selectedPurchaseAccountDisplayName = remember(formState.purchaseAccount, purchaseAccounts) {
        purchaseAccounts.find { it.id == formState.purchaseAccount }?.accountName
            ?: formState.purchaseAccount.ifBlank { "Select Purchase Account" }
    }

    // Preferred Supplier options (combines dropdown endpoint and view-all list)
    val supplierDropdown by viewModel.supplierDropdown.collectAsState()
    val suppliersList by viewModel.suppliers.collectAsState()

    val resolvedSuppliers = remember(supplierDropdown, suppliersList) {
        val fromDropdown = supplierDropdown
            .filter { it.value.isNotBlank() && it.label.isNotBlank() }
        val fromViewAll = suppliersList
            .filter { it.id.isNotBlank() && it.name.isNotBlank() }
            .map { SupplierDropdownItem(value = it.id, label = it.name) }

        if (fromDropdown.isNotEmpty()) {
            (fromDropdown + fromViewAll).distinctBy { it.value }
        } else {
            fromViewAll
        }
    }

    val supplierOptions = remember(resolvedSuppliers) {
        resolvedSuppliers.map { it.label }
    }
    val selectedSupplierDisplayName = remember(formState.preferredVendor, resolvedSuppliers) {
        resolvedSuppliers.find { it.value == formState.preferredVendor }?.label
            ?: formState.preferredVendor.ifBlank { "Select Preferred Vendor" }
    }

    // Dropdown expansion states
    var unitExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var itemGroupExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var taxGroupExpanded by remember { mutableStateOf(false) }
    var salesAccountExpanded by remember { mutableStateOf(false) }
    var purchaseAccountExpanded by remember { mutableStateOf(false) }
    var inventoryAccountExpanded by remember { mutableStateOf(false) }
    var supplierExpanded by remember { mutableStateOf(false) }

    var currentErrorField by remember { mutableStateOf<String?>(null) }

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
        viewModel.fetchSuppliers(page = 1, limit = 50)
        salesFinanceViewModel.fetchChartOfAccountsDropdown(context = "sales_line")
        expenseFinanceViewModel.fetchChartOfAccountsDropdown(context = "expense_line")
    }

    // Fetch item details in edit/view mode or initialize clean defaults for create mode
    LaunchedEffect(itemId) {
        if (!itemId.isNullOrBlank()) {
            viewModel.updateCreateItemForm { it.copy(itemId = itemId) }
            viewModel.fetchInventoryViewOne(itemId)
            viewModel.fetchInventoryItemDetail(itemId)
        } else {
            viewModel.resetCreateItemForm()
            viewModel.updateCreateItemForm {
                it.copy(
                    barcode = "",
                    unit = "",
                    taxCategory = "",
                    weight = "",
                    reorderLevel = "0",
                    safetyStock = "0",
                    variantSelections = emptyList()
                )
            }
            sacCode = ""
            fabric = false
            fabricType = ""
            openingStockRate = ""
            tagsText = ""
        }
    }

    // Prefill form when item details arrive
    LaunchedEffect(viewOneItem, selectedItemDetail) {
        val currentItem: Any? = viewOneItem ?: selectedItemDetail
        if (!itemId.isNullOrBlank() && currentItem != null) {
            viewOneItem?.let { viewModel.populateFormForEdit(it) }

            val rawName = extractProperty(currentItem, "name")
            val rawSku = extractProperty(currentItem, "sku")
            val rawBarcode = extractProperty(currentItem, "barcode")
            val rawCategory = extractIdOrValue(currentItem, "categoryId", "category", "productCategory")
            val rawUnit = extractProperty(currentItem, "unit")
            val rawType = extractProperty(currentItem, "type", "itemType")
            val rawStatus = extractProperty(currentItem, "status")
            val rawCostPrice = extractProperty(currentItem, "costPrice")
            val rawSellingPrice = extractProperty(currentItem, "sellingPrice")
            val rawManufacturer = extractProperty(currentItem, "manufacturer")
            val rawBrand = extractProperty(currentItem, "brand")
            val rawHsnCode = extractProperty(currentItem, "hsnCode")
            val rawSacCode = extractProperty(currentItem, "sacCode")
            val rawFabric = extractProperty(currentItem, "fabric").equals("true", ignoreCase = true)
            val rawFabricType = extractProperty(currentItem, "fabricType")
            val rawTaxGroup = extractIdOrValue(currentItem, "taxGroupId", "taxCategory")
            val rawSalesAccount = extractIdOrValue(currentItem, "salesAccountId", "salesAccount")
            val rawPurchaseAccount = extractIdOrValue(currentItem, "purchaseAccountId", "purchaseAccount")
            val rawInventoryAccount = extractIdOrValue(currentItem, "inventoryAccountId", "inventoryAccount")
            val rawPreferredVendor = extractIdOrValue(currentItem, "preferredVendorId", "preferredVendor")
            val rawReturnable = extractProperty(currentItem, "returnable").equals("true", ignoreCase = true)
            val rawTrackInventory = extractProperty(currentItem, "trackInventory")
            val rawIsSerialTracked = extractProperty(currentItem, "isSerialTracked").equals("true", ignoreCase = true)
            val rawOpeningStock = extractProperty(currentItem, "openingStock")
            val rawOpeningStockRate = extractProperty(currentItem, "openingStockRate")
            val rawReorderLevel = extractProperty(currentItem, "reorderLevel")
            val rawSafetyStock = extractProperty(currentItem, "safetyStock")
            val rawParentGroupId = extractIdOrValue(currentItem, "parentGroupId").takeIf { it.isNotBlank() }

            val rawSize = extractVariantValue(currentItem, "size")
            val normalizedSize = STANDARD_SIZES.find { it.equals(rawSize.trim(), ignoreCase = true) } ?: ""
            val rawColor = extractVariantValue(currentItem, "color").filter { !it.isDigit() }
            val prefilledVariants = buildVariantSelections(normalizedSize, rawColor)

            if (rawSacCode.isNotBlank()) sacCode = sanitizeIntegerInput(rawSacCode)
            fabric = rawFabric
            if (rawFabricType.isNotBlank()) fabricType = rawFabricType
            if (rawOpeningStockRate.isNotBlank()) openingStockRate = sanitizeDecimalInput(rawOpeningStockRate)

            val rawTagsObj = getRawProperty(currentItem, "tags")
            if (rawTagsObj is Collection<*>) {
                tagsText = rawTagsObj.mapNotNull { it?.toString()?.trim() }
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
            }

            viewModel.updateCreateItemForm { current ->
                current.copy(
                    itemId = itemId,
                    name = current.name.ifBlank { rawName },
                    sku = current.sku.ifBlank { rawSku },
                    barcode = current.barcode.ifBlank { sanitizeIntegerInput(rawBarcode) },
                    category = rawCategory.ifBlank { current.category },
                    unit = current.unit.ifBlank { rawUnit.ifBlank { "Meters" } },
                    itemType = if (rawType.equals("service", ignoreCase = true)) "service" else "goods",
                    status = current.status.ifBlank { rawStatus.ifBlank { "active" } },
                    costPrice = current.costPrice.ifBlank { sanitizeDecimalInput(rawCostPrice) },
                    sellingPrice = current.sellingPrice.ifBlank { sanitizeDecimalInput(rawSellingPrice) },
                    manufacturer = current.manufacturer.ifBlank { rawManufacturer },
                    brand = current.brand.ifBlank { rawBrand },
                    hsnCode = current.hsnCode.ifBlank { sanitizeIntegerInput(rawHsnCode) },
                    taxCategory = rawTaxGroup.ifBlank { current.taxCategory },
                    salesAccount = current.salesAccount.ifBlank { rawSalesAccount },
                    purchaseAccount = current.purchaseAccount.ifBlank { rawPurchaseAccount },
                    inventoryAccount = current.inventoryAccount.ifBlank { rawInventoryAccount },
                    preferredVendor = current.preferredVendor.ifBlank { rawPreferredVendor },
                    returnable = rawReturnable,
                    trackInventory = if (rawTrackInventory.isNotBlank()) {
                        rawTrackInventory.equals("true", ignoreCase = true)
                    } else {
                        current.trackInventory
                    },
                    isSerialTracked = rawIsSerialTracked,
                    openingStock = current.openingStock.ifBlank { sanitizeDecimalInput(rawOpeningStock) },
                    reorderLevel = current.reorderLevel.ifBlank { sanitizeIntegerInput(rawReorderLevel) },
                    safetyStock = current.safetyStock.ifBlank { sanitizeIntegerInput(rawSafetyStock) },
                    parentGroupId = current.parentGroupId ?: rawParentGroupId,
                    variantSelections = if (prefilledVariants.isNotEmpty()) prefilledVariants else current.variantSelections,
                    autoGenerateSku = false
                )
            }
        }
    }

    // Inherit category, type, and unit when Item Group changes
    LaunchedEffect(formState.parentGroupId, itemGroupState.itemGroups) {
        val groupId = formState.parentGroupId ?: return@LaunchedEffect
        val selectedGroup = itemGroupState.itemGroups.find { it.id == groupId } ?: return@LaunchedEffect
        val groupCategory = extractIdOrValue(selectedGroup, "categoryId", "category")
        val groupType = extractProperty(selectedGroup, "type")
        val groupUnit = extractProperty(selectedGroup, "unit")

        viewModel.updateCreateItemForm { current ->
            current.copy(
                category = groupCategory.ifBlank { current.category },
                itemType = if (groupType.equals("service", ignoreCase = true)) "service" else current.itemType,
                unit = groupUnit.ifBlank { current.unit }
            )
        }
    }

    // Synchronize Category to ID when productCategories load
    LaunchedEffect(formState.category, productCategories) {
        if (formState.category.isNotBlank() && productCategories.isNotEmpty()) {
            val matchedCategory = productCategories.find {
                it.id.equals(formState.category, ignoreCase = true) ||
                        it.name.equals(formState.category, ignoreCase = true)
            }
            if (matchedCategory != null && formState.category != matchedCategory.id) {
                viewModel.updateCreateItemForm { it.copy(category = matchedCategory.id) }
            }
        }
    }

    // Auto-populate tax rate percentage when tax group is selected
    LaunchedEffect(formState.taxCategory, taxGroups) {
        val matchingGroup = taxGroups.find {
            it.id == formState.taxCategory || it.name.equals(formState.taxCategory, ignoreCase = true)
        }
        if (matchingGroup != null && formState.taxPercentage.isBlank()) {
            viewModel.updateCreateItemForm {
                it.copy(taxPercentage = sanitizeDecimalInput(matchingGroup.totalRate.toString()))
            }
        }
    }

    // Handle submit states
    LaunchedEffect(uiState) {
        when (uiState) {
            is CreateItemUiState.Success -> {
                DynamicIslandManager.showSuccess(
                    if (isEditMode) "Item updated successfully" else "Item created successfully"
                )
                delay(800)
                viewModel.resetCreateItemForm()
                onItemCreated()
            }
            is CreateItemUiState.Error -> {
                DynamicIslandManager.showError((uiState as CreateItemUiState.Error).message)
            }
            else -> Unit
        }
    }

    fun clearFieldError(fieldKey: String) {
        if (currentErrorField == fieldKey) {
            currentErrorField = null
        }
    }

    fun validateForm(): Boolean {
        if (isViewOnly) return true

        val sellingValue = formState.sellingPrice.toDoubleOrNull()
        val costValue = formState.costPrice.toDoubleOrNull()
        val taxValue = formState.taxPercentage.toDoubleOrNull()
        val openingStockValue = formState.openingStock.toDoubleOrNull()
        val openingRateValue = openingStockRate.toDoubleOrNull()

        val missingField: Pair<String, String>? = when {
            formState.name.isBlank() -> "itemName" to "Item name is required"
            formState.unit.isBlank() -> "unit" to "Unit of measure is required"
            formState.status.isBlank() -> "status" to "Status is required"
            !hasParentGroup && formState.category.isBlank() -> "category" to "Category is required"

            formState.length.isNotBlank() && formState.length.toDoubleOrNull() == null ->
                "length" to "Length must be a valid number"
            formState.width.isNotBlank() && formState.width.toDoubleOrNull() == null ->
                "width" to "Width must be a valid number"
            formState.height.isNotBlank() && formState.height.toDoubleOrNull() == null ->
                "height" to "Height must be a valid number"
            formState.weight.isNotBlank() && formState.weight.toDoubleOrNull() == null ->
                "weight" to "Weight must be a valid number"

            formState.taxPercentage.isNotBlank() && (taxValue == null || taxValue < 0.0 || taxValue > 100.0) ->
                "taxPercentage" to "Tax percentage must be between 0 and 100"

            formState.sellingPrice.isBlank() || sellingValue == null || sellingValue < 0.0 ->
                "sellingPrice" to "Valid selling price is required"
            !hasParentGroup && formState.salesAccount.isBlank() ->
                "salesAccount" to "Sales account is required"

            formState.costPrice.isBlank() || costValue == null || costValue < 0.0 ->
                "costPrice" to "Valid cost price is required"
            !hasParentGroup && formState.purchaseAccount.isBlank() ->
                "purchaseAccount" to "Purchase account is required"
            !hasParentGroup && formState.preferredVendor.isBlank() ->
                "preferredVendor" to "Preferred vendor is required"

            !hasParentGroup && formState.trackInventory && formState.inventoryAccount.isBlank() ->
                "inventoryAccount" to "Inventory account is required"
            formState.trackInventory && !formState.isSerialTracked &&
                    (formState.openingStock.isBlank() || openingStockValue == null || openingStockValue < 0.0) ->
                "openingStock" to "Valid opening stock is required"
            formState.trackInventory && !formState.isSerialTracked &&
                    (openingStockRate.isBlank() || openingRateValue == null || openingRateValue < 0.0) ->
                "openingStockRate" to "Valid opening stock rate is required"
            formState.reorderLevel.isNotBlank() && formState.reorderLevel.toIntOrNull() == null ->
                "reorderLevel" to "Reorder point must be a valid number"
            formState.safetyStock.isNotBlank() && formState.safetyStock.toIntOrNull() == null ->
                "safetyStock" to "Safety stock must be a valid number"

            else -> null
        }

        currentErrorField = missingField?.first

        if (missingField != null) {
            when (missingField.first) {
                "itemName", "unit", "status", "category" -> {
                    inventorySectionExpanded = false
                    if (expandedSection != ItemSection.ITEM_IDENTITY) {
                        viewModel.toggleSection(ItemSection.ITEM_IDENTITY)
                    }
                }
                "length", "width", "height", "weight" -> {
                    inventorySectionExpanded = false
                    if (expandedSection != ItemSection.PHYSICAL_ATTRIBUTES) {
                        viewModel.toggleSection(ItemSection.PHYSICAL_ATTRIBUTES)
                    }
                }
                "taxPercentage" -> {
                    inventorySectionExpanded = false
                    if (expandedSection != ItemSection.TAX_INFO) {
                        viewModel.toggleSection(ItemSection.TAX_INFO)
                    }
                }
                "sellingPrice", "salesAccount" -> {
                    inventorySectionExpanded = false
                    if (expandedSection != ItemSection.SALES_INFO) {
                        viewModel.toggleSection(ItemSection.SALES_INFO)
                    }
                }
                "costPrice", "purchaseAccount", "preferredVendor" -> {
                    inventorySectionExpanded = false
                    if (expandedSection != ItemSection.PURCHASE_INFO) {
                        viewModel.toggleSection(ItemSection.PURCHASE_INFO)
                    }
                }
                "inventoryAccount", "openingStock", "openingStockRate", "reorderLevel", "safetyStock" -> {
                    inventorySectionExpanded = true
                }
            }
            DynamicIslandManager.showError(missingField.second)
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

    val currentStockValue = remember(formState.openingStock, openingStockRate) {
        val qty = formState.openingStock.toDoubleOrNull() ?: 0.0
        val rate = openingStockRate.toDoubleOrNull() ?: 0.0
        String.format(Locale.US, "%.2f", qty * rate)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
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
                // 1. Item Identity Section
                AccordionSection(
                    iconPainter = painterResource(R.drawable.box),
                    title = "Item Identity",
                    expanded = expandedSection == ItemSection.ITEM_IDENTITY && !inventorySectionExpanded,
                    onHeaderClick = {
                        inventorySectionExpanded = false
                        viewModel.toggleSection(ItemSection.ITEM_IDENTITY)
                    }
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
                        selectedIndex = if (isGoods) 0 else 1,
                        onTabSelected = { index ->
                            if (isGroupInheritedEditable) {
                                viewModel.updateCreateItemForm {
                                    it.copy(itemType = if (index == 0) "goods" else "service")
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Item Name", isRequired = true)
                    FormTextField(
                        value = formState.name,
                        onValueChange = { newValue ->
                            clearFieldError("itemName")
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

                    if (isEditable) {
                        Spacer(Modifier.height(tokens.extraPadding))
                        ToggleRow(
                            title = "Auto-generate SKU",
                            checked = formState.autoGenerateSku,
                            enabled = true,
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
                        expanded = unitExpanded,
                        onExpandChange = { if (isGroupInheritedEditable) unitExpanded = it },
                        options = UNIT_OPTIONS,
                        onOptionSelected = { selectedUnit ->
                            clearFieldError("unit")
                            viewModel.updateCreateItemForm { it.copy(unit = selectedUnit) }
                        },
                        enabled = isGroupInheritedEditable,
                        isError = currentErrorField == "unit",
                        errorMessage = if (currentErrorField == "unit") "Unit of measure is required" else null
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
                    FormLabel("Fabric")
                    val fabricTabs = remember {
                        listOf(
                            TabItem(label = "No"),
                            TabItem(label = "Yes")
                        )
                    }
                    SettingsTabs(
                        tabs = fabricTabs,
                        selectedIndex = if (fabric) 1 else 0,
                        onTabSelected = { index ->
                            if (isGroupInheritedEditable) {
                                fabric = index == 1
                            }
                        }
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Fabric Type")
                    FormTextField(
                        value = fabricType,
                        onValueChange = { newValue ->
                            fabricType = newValue.filter { !it.isDigit() }
                        },
                        placeholder = "e.g. Cotton, Silk, Polyester",
                        enabled = isGroupInheritedEditable
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Category", isRequired = !hasParentGroup)
                    FormDropdown(
                        value = selectedCategoryDisplayName,
                        expanded = categoryExpanded,
                        onExpandChange = { if (isGroupInheritedEditable) categoryExpanded = it },
                        options = categoryOptions.ifEmpty { listOf("No Categories Available") },
                        onOptionSelected = { selectedCategoryName ->
                            val selectedCategoryId = productCategories
                                .find { it.name.equals(selectedCategoryName, ignoreCase = true) }
                                ?.id
                                ?: return@FormDropdown
                            clearFieldError("category")
                            viewModel.updateCreateItemForm { it.copy(category = selectedCategoryId) }
                        },
                        enabled = isGroupInheritedEditable,
                        isRequired = !hasParentGroup,
                        isError = currentErrorField == "category",
                        errorMessage = if (currentErrorField == "category") "Category is required" else null
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Item Group (Optional)")
                    FormDropdown(
                        value = selectedItemGroupDisplayName,
                        expanded = itemGroupExpanded,
                        onExpandChange = { if (isEditable) itemGroupExpanded = it },
                        options = listOf("None") + itemGroupOptions,
                        onOptionSelected = { selectedGroupName ->
                            val selectedGroupId = if (selectedGroupName == "None") {
                                null
                            } else {
                                itemGroupState.itemGroups
                                    .find { it.name.equals(selectedGroupName, ignoreCase = true) }
                                    ?.id
                            }
                            viewModel.updateCreateItemForm { it.copy(parentGroupId = selectedGroupId) }
                        },
                        enabled = isEditable
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Status", isRequired = true)
                    FormDropdown(
                        value = formState.status.ifBlank { "Select Status" },
                        expanded = statusExpanded,
                        onExpandChange = { if (isEditable) statusExpanded = it },
                        options = listOf("active", "inactive"),
                        onOptionSelected = { selectedStatus ->
                            clearFieldError("status")
                            viewModel.updateCreateItemForm { it.copy(status = selectedStatus) }
                        },
                        enabled = isEditable,
                        isError = currentErrorField == "status",
                        errorMessage = if (currentErrorField == "status") "Status is required" else null
                    )
                }

                // 2. Product Images Section
                if (!isStarterOrLight) {
                    AccordionSection(
                        iconPainter = painterResource(R.drawable.box),
                        title = "Product Images",
                        expanded = expandedSection == ItemSection.PRODUCT_IMAGES && !inventorySectionExpanded,
                        onHeaderClick = {
                            inventorySectionExpanded = false
                            viewModel.toggleSection(ItemSection.PRODUCT_IMAGES)
                        }
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
                            imagePreviewSize = 90.dp,
                            previewHeaderTitle = "ATTACHED IMAGE"
                        )
                    }
                }

                // 3. Physical Attributes Section
                if (!isStarterOrLight) {
                    AccordionSection(
                        iconPainter = painterResource(R.drawable.box),
                        title = "Physical Attributes",
                        expanded = expandedSection == ItemSection.PHYSICAL_ATTRIBUTES && !inventorySectionExpanded,
                        onHeaderClick = {
                            inventorySectionExpanded = false
                            viewModel.toggleSection(ItemSection.PHYSICAL_ATTRIBUTES)
                        }
                    ) {
                        FormLabel("Size")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.4f)
                        ) {
                            STANDARD_SIZES.forEach { sizeOption ->
                                val isSelected = selectedSize == sizeOption
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                        .clickable(enabled = isEditable) {
                                            val nextSize = if (isSelected) "" else sizeOption
                                            viewModel.updateCreateItemForm { current ->
                                                current.copy(
                                                    variantSelections = buildVariantSelections(nextSize, colorText)
                                                )
                                            }
                                        }
                                        .padding(end = tokens.extraPadding * 0.6f)
                                ) {
                                    AppRadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            if (isEditable) {
                                                val nextSize = if (isSelected) "" else sizeOption
                                                viewModel.updateCreateItemForm { current ->
                                                    current.copy(
                                                        variantSelections = buildVariantSelections(nextSize, colorText)
                                                    )
                                                }
                                            }
                                        },
                                        enabled = isEditable
                                    )
                                    Text(
                                        text = sizeOption,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                        color = if (isEditable) {
                                            if (isSelected) Primary else TextPrimary
                                        } else {
                                            TextSecondary
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Color")
                        FormTextField(
                            value = colorText,
                            onValueChange = { v ->
                                val cleanColor = v.filter { !it.isDigit() }
                                viewModel.updateCreateItemForm { current ->
                                    current.copy(
                                        variantSelections = buildVariantSelections(selectedSize, cleanColor)
                                    )
                                }
                            },
                            placeholder = "e.g. Red, Blue",
                            enabled = isEditable
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Dimensions (LxWxH)")
                        Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                            Box(Modifier.weight(1f)) {
                                FormTextField(
                                    value = formState.length,
                                    onValueChange = { v ->
                                        clearFieldError("length")
                                        viewModel.updateCreateItemForm {
                                            it.copy(length = sanitizeDecimalInput(v))
                                        }
                                    },
                                    placeholder = "L",
                                    enabled = isEditable,
                                    keyboardType = KeyboardType.Decimal,
                                    isError = currentErrorField == "length"
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                FormTextField(
                                    value = formState.width,
                                    onValueChange = { v ->
                                        clearFieldError("width")
                                        viewModel.updateCreateItemForm {
                                            it.copy(width = sanitizeDecimalInput(v))
                                        }
                                    },
                                    placeholder = "W",
                                    enabled = isEditable,
                                    keyboardType = KeyboardType.Decimal,
                                    isError = currentErrorField == "width"
                                )
                            }
                            Box(Modifier.weight(1f)) {
                                FormTextField(
                                    value = formState.height,
                                    onValueChange = { v ->
                                        clearFieldError("height")
                                        viewModel.updateCreateItemForm {
                                            it.copy(height = sanitizeDecimalInput(v))
                                        }
                                    },
                                    placeholder = "H",
                                    enabled = isEditable,
                                    keyboardType = KeyboardType.Decimal,
                                    isError = currentErrorField == "height"
                                )
                            }
                        }
                        if (currentErrorField in listOf("length", "width", "height")) {
                            Text(
                                text = "Dimensions must contain valid numbers",
                                fontSize = tokens.label,
                                color = redText,
                                modifier = Modifier.padding(top = tokens.extraPadding * 0.4f)
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Weight (kg)")
                        FormTextField(
                            value = formState.weight,
                            onValueChange = { v ->
                                clearFieldError("weight")
                                viewModel.updateCreateItemForm {
                                    it.copy(weight = sanitizeDecimalInput(v))
                                }
                            },
                            placeholder = "0.00",
                            enabled = isEditable,
                            keyboardType = KeyboardType.Decimal,
                            isError = currentErrorField == "weight",
                            errorMessage = if (currentErrorField == "weight") "Weight must be a valid number" else null
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Manufacturer")
                        FormTextField(
                            value = formState.manufacturer,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(manufacturer = v) } },
                            placeholder = "Manufacturer Name",
                            enabled = isGroupInheritedEditable
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Brand")
                        FormTextField(
                            value = formState.brand,
                            onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(brand = v) } },
                            placeholder = "e.g. Brand Name",
                            enabled = isGroupInheritedEditable
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Barcode")
                        FormTextField(
                            value = formState.barcode,
                            onValueChange = { v ->
                                viewModel.updateCreateItemForm {
                                    it.copy(barcode = sanitizeIntegerInput(v))
                                }
                            },
                            placeholder = "e.g. 8901234567890",
                            enabled = isEditable,
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                // 4. Tax Information Section
                if (!isStarterOrLight) {
                    AccordionSection(
                        iconPainter = painterResource(R.drawable.ic_transaction_sheet),
                        title = "Tax Information",
                        expanded = expandedSection == ItemSection.TAX_INFO && !inventorySectionExpanded,
                        onHeaderClick = {
                            inventorySectionExpanded = false
                            viewModel.toggleSection(ItemSection.TAX_INFO)
                        }
                    ) {
                        FormLabel(if (isGoods) "HSN Code" else "SAC Code")
                        FormTextField(
                            value = if (isGoods) formState.hsnCode else sacCode,
                            onValueChange = { v ->
                                val digitsOnly = sanitizeIntegerInput(v)
                                if (isGoods) {
                                    viewModel.updateCreateItemForm { it.copy(hsnCode = digitsOnly) }
                                } else {
                                    sacCode = digitsOnly
                                }
                            },
                            placeholder = if (isGoods) "e.g. 8471" else "e.g. 9983",
                            enabled = isGroupInheritedEditable,
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Tax Group")
                        FormDropdown(
                            value = selectedTaxGroupDisplayName,
                            expanded = taxGroupExpanded,
                            onExpandChange = { if (isGroupInheritedEditable) taxGroupExpanded = it },
                            options = taxGroupOptions.ifEmpty { listOf("No Tax Groups Available") },
                            onOptionSelected = { chosenGroupName ->
                                val selectedGroup = taxGroups.find { it.name == chosenGroupName }
                                    ?: return@FormDropdown
                                viewModel.updateCreateItemForm {
                                    it.copy(
                                        taxCategory = selectedGroup.id,
                                        taxPercentage = sanitizeDecimalInput(selectedGroup.totalRate.toString())
                                    )
                                }
                            },
                            enabled = isGroupInheritedEditable
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Tax Percentage (%)")
                        FormTextField(
                            value = formState.taxPercentage,
                            onValueChange = { v ->
                                clearFieldError("taxPercentage")
                                viewModel.updateCreateItemForm {
                                    it.copy(taxPercentage = sanitizeDecimalInput(v))
                                }
                            },
                            placeholder = "0",
                            enabled = isGroupInheritedEditable,
                            keyboardType = KeyboardType.Decimal,
                            isError = currentErrorField == "taxPercentage",
                            errorMessage = if (currentErrorField == "taxPercentage") "Tax percentage must be between 0 and 100" else null
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        ToggleRow(
                            title = "Price is Tax Inclusive",
                            checked = formState.taxInclusive,
                            enabled = isEditable,
                            onCheckedChange = { isChecked ->
                                viewModel.updateCreateItemForm { it.copy(taxInclusive = isChecked) }
                            },
                            titleFirst = false
                        )
                    }
                }

                // 5. Sales Information Section
                AccordionSection(
                    iconPainter = painterResource(R.drawable.ic_tag),
                    title = "Sales Information",
                    expanded = expandedSection == ItemSection.SALES_INFO && !inventorySectionExpanded,
                    onHeaderClick = {
                        inventorySectionExpanded = false
                        viewModel.toggleSection(ItemSection.SALES_INFO)
                    }
                ) {
                    FormLabel("Selling Price", isRequired = true)
                    FormTextField(
                        value = formState.sellingPrice,
                        onValueChange = { v ->
                            clearFieldError("sellingPrice")
                            viewModel.updateCreateItemForm {
                                it.copy(sellingPrice = sanitizeDecimalInput(v))
                            }
                        },
                        placeholder = "0.00",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Decimal,
                        isError = currentErrorField == "sellingPrice",
                        errorMessage = if (currentErrorField == "sellingPrice") "Valid selling price is required" else null
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Sales Account", isRequired = !hasParentGroup)
                    FormDropdown(
                        value = selectedSalesAccountDisplayName,
                        expanded = salesAccountExpanded,
                        onExpandChange = { if (isEditable) salesAccountExpanded = it },
                        options = salesAccountOptions.ifEmpty { listOf("No Sales Accounts Available") },
                        onOptionSelected = { chosenAccountName ->
                            val chosenAccountId = salesAccounts.find { it.accountName == chosenAccountName }?.id
                                ?: return@FormDropdown
                            clearFieldError("salesAccount")
                            viewModel.updateCreateItemForm { it.copy(salesAccount = chosenAccountId) }
                        },
                        enabled = isEditable,
                        isRequired = !hasParentGroup,
                        isError = currentErrorField == "salesAccount",
                        errorMessage = if (currentErrorField == "salesAccount") "Sales account is required" else null
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Sales Description")
                    FormTextArea(
                        value = formState.salesDescription,
                        enabled = isEditable,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(salesDescription = v) } }
                    )
                }

                // 6. Purchase Information Section
                AccordionSection(
                    iconPainter = painterResource(R.drawable.cart),
                    title = "Purchase Information",
                    expanded = expandedSection == ItemSection.PURCHASE_INFO && !inventorySectionExpanded,
                    onHeaderClick = {
                        inventorySectionExpanded = false
                        viewModel.toggleSection(ItemSection.PURCHASE_INFO)
                    }
                ) {
                    FormLabel("Cost Price", isRequired = true)
                    FormTextField(
                        value = formState.costPrice,
                        onValueChange = { v ->
                            clearFieldError("costPrice")
                            viewModel.updateCreateItemForm {
                                it.copy(costPrice = sanitizeDecimalInput(v))
                            }
                        },
                        placeholder = "0.00",
                        enabled = isEditable,
                        keyboardType = KeyboardType.Decimal,
                        isError = currentErrorField == "costPrice",
                        errorMessage = if (currentErrorField == "costPrice") "Valid cost price is required" else null
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Purchase Account", isRequired = !hasParentGroup)
                    FormDropdown(
                        value = selectedPurchaseAccountDisplayName,
                        expanded = purchaseAccountExpanded,
                        onExpandChange = { if (isEditable) purchaseAccountExpanded = it },
                        options = purchaseAccountOptions.ifEmpty { listOf("No Purchase Accounts Available") },
                        onOptionSelected = { chosenAccountName ->
                            val chosenAccountId = purchaseAccounts.find { it.accountName == chosenAccountName }?.id
                                ?: return@FormDropdown
                            clearFieldError("purchaseAccount")
                            viewModel.updateCreateItemForm { it.copy(purchaseAccount = chosenAccountId) }
                        },
                        enabled = isEditable,
                        isRequired = !hasParentGroup,
                        isError = currentErrorField == "purchaseAccount",
                        errorMessage = if (currentErrorField == "purchaseAccount") "Purchase account is required" else null
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Preferred Vendor", isRequired = !hasParentGroup)
                    FormDropdown(
                        value = selectedSupplierDisplayName,
                        expanded = supplierExpanded,
                        onExpandChange = { if (isEditable) supplierExpanded = it },
                        options = supplierOptions.ifEmpty { listOf("No Suppliers Available") },
                        onOptionSelected = { chosenSupplierLabel ->
                            val chosenSupplierValue = resolvedSuppliers.find { it.label == chosenSupplierLabel }?.value
                                ?: return@FormDropdown
                            clearFieldError("preferredVendor")
                            viewModel.updateCreateItemForm { it.copy(preferredVendor = chosenSupplierValue) }
                        },
                        enabled = isEditable,
                        isRequired = !hasParentGroup,
                        isError = currentErrorField == "preferredVendor",
                        errorMessage = if (currentErrorField == "preferredVendor") "Preferred vendor is required" else null
                    )

                    Spacer(Modifier.height(tokens.extraPadding))
                    FormLabel("Purchase Description")
                    FormTextArea(
                        value = formState.purchaseDescription,
                        enabled = isEditable,
                        onValueChange = { v -> viewModel.updateCreateItemForm { it.copy(purchaseDescription = v) } }
                    )
                }

                // 7. Inventory Tracking Section
                AccordionSection(
                    iconPainter = painterResource(R.drawable.box),
                    title = "Inventory Tracking",
                    expanded = inventorySectionExpanded,
                    onHeaderClick = {
                        inventorySectionExpanded = !inventorySectionExpanded
                    }
                ) {
                    ToggleRow(
                        title = if (formState.trackInventory) "TRACKING ON" else "TRACKING OFF",
                        subtitle = "Track stock levels for this item",
                        checked = formState.trackInventory,
                        enabled = isEditable && !isGoods,
                        onCheckedChange = { isChecked ->
                            viewModel.updateCreateItemForm { it.copy(trackInventory = isChecked) }
                        },
                        titleFirst = true
                    )

                    if (formState.trackInventory) {
                        Spacer(Modifier.height(tokens.extraPadding))
                        ToggleRow(
                            title = "Serial Number Tracking",
                            checked = formState.isSerialTracked,
                            enabled = isEditable,
                            onCheckedChange = { isChecked ->
                                if (isChecked) openingStockRate = ""
                                viewModel.updateCreateItemForm {
                                    it.copy(
                                        isSerialTracked = isChecked,
                                        openingStock = if (isChecked) "" else it.openingStock
                                    )
                                }
                            },
                            titleFirst = false
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Inventory Account", isRequired = !hasParentGroup)
                        FormDropdown(
                            value = selectedInventoryAccountDisplayName,
                            expanded = inventoryAccountExpanded,
                            onExpandChange = { if (isEditable) inventoryAccountExpanded = it },
                            options = salesAccountOptions.ifEmpty { listOf("No Inventory Accounts Available") },
                            onOptionSelected = { chosenAccountName ->
                                val chosenAccountId = salesAccounts.find { it.accountName == chosenAccountName }?.id
                                    ?: return@FormDropdown
                                clearFieldError("inventoryAccount")
                                viewModel.updateCreateItemForm { it.copy(inventoryAccount = chosenAccountId) }
                            },
                            enabled = isEditable,
                            isRequired = !hasParentGroup,
                            isError = currentErrorField == "inventoryAccount",
                            errorMessage = if (currentErrorField == "inventoryAccount") "Inventory account is required" else null
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                            Column(Modifier.weight(1f)) {
                                FormLabel("Opening Stock", isRequired = !formState.isSerialTracked)
                                FormTextField(
                                    value = formState.openingStock,
                                    onValueChange = { v ->
                                        clearFieldError("openingStock")
                                        viewModel.updateCreateItemForm {
                                            it.copy(openingStock = sanitizeDecimalInput(v))
                                        }
                                    },
                                    placeholder = "0",
                                    enabled = isEditable && !formState.isSerialTracked,
                                    keyboardType = KeyboardType.Decimal,
                                    isError = currentErrorField == "openingStock",
                                    errorMessage = if (currentErrorField == "openingStock") "Valid opening stock required" else null
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                FormLabel("Opening Stock Rate", isRequired = !formState.isSerialTracked)
                                FormTextField(
                                    value = openingStockRate,
                                    onValueChange = { v ->
                                        clearFieldError("openingStockRate")
                                        openingStockRate = sanitizeDecimalInput(v)
                                    },
                                    placeholder = "0.00",
                                    enabled = isEditable && !formState.isSerialTracked,
                                    keyboardType = KeyboardType.Decimal,
                                    isError = currentErrorField == "openingStockRate",
                                    errorMessage = if (currentErrorField == "openingStockRate") "Valid stock rate required" else null
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))
                        Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                            Column(Modifier.weight(1f)) {
                                FormLabel("Reorder Point")
                                FormTextField(
                                    value = formState.reorderLevel,
                                    onValueChange = { v ->
                                        clearFieldError("reorderLevel")
                                        viewModel.updateCreateItemForm {
                                            it.copy(reorderLevel = sanitizeIntegerInput(v))
                                        }
                                    },
                                    placeholder = "0",
                                    enabled = isEditable,
                                    keyboardType = KeyboardType.Number,
                                    isError = currentErrorField == "reorderLevel",
                                    errorMessage = if (currentErrorField == "reorderLevel") "Valid number required" else null
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                FormLabel("Safety Stock")
                                FormTextField(
                                    value = formState.safetyStock,
                                    onValueChange = { v ->
                                        clearFieldError("safetyStock")
                                        viewModel.updateCreateItemForm {
                                            it.copy(safetyStock = sanitizeIntegerInput(v))
                                        }
                                    },
                                    placeholder = "0",
                                    enabled = isEditable,
                                    keyboardType = KeyboardType.Number,
                                    isError = currentErrorField == "safetyStock",
                                    errorMessage = if (currentErrorField == "safetyStock") "Valid number required" else null
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))
                        FormLabel("Tags & Classification")
                        FormTextArea(
                            value = tagsText,
                            onValueChange = { tagsText = it },
                            enabled = isEditable,
                            placeholder = "Comma separated tags (e.g. silk, festive, new)",
                            minLines = 2,
                            maxLines = 4
                        )

                        Spacer(Modifier.height(tokens.extraPadding))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(whiteBg)
                                .border(
                                    width = 1.dp,
                                    color = sectionBorder,
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                                )
                                .padding(tokens.cardPadding * 0.6f)
                        ) {
                            Text(
                                text = "CURRENT STOCK VALUE",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = mutedText
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                            Text(
                                text = currentStockValue,
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Buttons
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
// INPUT SANITIZATION & VARIANT HELPERS
// =============================================================================

private fun buildVariantSelections(size: String, color: String): List<VariantSelection> {
    return buildList {
        if (size.isNotBlank()) add(VariantSelection(name = "Size", value = size.trim()))
        if (color.isNotBlank()) add(VariantSelection(name = "Color", value = color.trim()))
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

private fun sanitizeIntegerInput(input: String): String {
    return input.filter { it.isDigit() }
}

// =============================================================================
// PROPERTY REFLECTION HELPERS
// =============================================================================

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

private fun extractVariantValue(target: Any?, variantName: String): String {
    val selections = getRawProperty(target, "variantSelections") as? Collection<*> ?: return ""
    for (entry in selections) {
        val entryName = extractProperty(entry, "name")
        if (entryName.equals(variantName, ignoreCase = true)) {
            return extractProperty(entry, "value")
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
            Spacer(Modifier.width(tokens.extraPadding))
            Text(
                text = title,
                fontSize = tokens.bodySmall,
                color = if (enabled) TextSecondary else TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}