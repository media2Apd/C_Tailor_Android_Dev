package com.cuso.mobile.view.home.inventory.bulk_items

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.view.home.subscriptions.WhiteCard
import com.cuso.mobile.viewmodel.InventoryViewModel
import com.cuso.mobile.viewmodel.SettingsViewModel

data class ComponentFormItem(
    val itemId: String,
    val name: String,
    val sku: String,
    val stockQty: Int,
    var requiredQty: Int,
    val unitCost: Double
)

@Composable
fun AddBulkItemScreen(
    editItemId: String? = null,
    viewModel: InventoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val isEdit = !editItemId.isNullOrBlank()
    val selectedItem by viewModel.selectedBulkItem.collectAsStateWithLifecycle()

    // ── Dynamic Dropdown States from API ──
    val categoryList by settingsViewModel.productCategories.collectAsStateWithLifecycle()
    val warehouseList by viewModel.warehouseDropdown.collectAsStateWithLifecycle()

    // Form fields
    var isActiveStatus by remember { mutableStateOf(true) }
    var itemName by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }

    // Category ID & Display Name
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryName by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var brand by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("set") }
    var unitExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    var sellingPrice by remember { mutableStateOf("") }
    var taxPercentage by remember { mutableStateOf("18") }
    var salesAccount by remember { mutableStateOf("Sales Revenue") }
    var salesAccountExpanded by remember { mutableStateOf(false) }
    var purchaseAccount by remember { mutableStateOf("Cost of Goods Sold (COGS)") }
    var purchaseAccountExpanded by remember { mutableStateOf(false) }

    var componentSearchQuery by remember { mutableStateOf("") }
    var components by remember {
        mutableStateOf(
            listOf(
                ComponentFormItem("6a8c3df68b122b97dd1a7666", "Italian Linen Fabric", "RM-LINEN-002", 50, 2, 180.00),
                ComponentFormItem("6a90381da781fc589e336266", "Zip Puller — Metal", "ZIP-M-01", 100, 4, 5.00)
            )
        )
    }

    var isInventoryTracked by remember { mutableStateOf(true) }
    var assemblyTypeIndex by remember { mutableIntStateOf(0) }

    // Warehouse ID & Display Name
    var selectedWarehouseId by remember { mutableStateOf<String?>(null) }
    var selectedWarehouseName by remember { mutableStateOf("") }
    var warehouseExpanded by remember { mutableStateOf(false) }

    var inventorySettingsExpanded by remember { mutableStateOf(true) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val computedCostPrice = remember(components) {
        components.sumOf { it.unitCost * it.requiredQty }
    }

    // ── Fetch Dropdowns on Screen Load ──
    LaunchedEffect(Unit) {
        settingsViewModel.fetchProductCategories()
        viewModel.loadWarehouseDropdown()
    }

    LaunchedEffect(editItemId) {
        if (!editItemId.isNullOrBlank()) {
            viewModel.fetchBulkItemDetail(editItemId)
        }
    }

    // Prepopulate form when editing
    LaunchedEffect(selectedItem, categoryList, warehouseList) {
        if (isEdit && selectedItem != null) {
            val item = selectedItem ?: return@LaunchedEffect
            itemName = item.name
            sku = item.sku
            description = item.description.orEmpty()
            brand = item.brand.orEmpty()
            unit = item.unit
            sellingPrice = item.sellingPrice.toString()
            taxPercentage = item.taxPercent.toString()
            isInventoryTracked = item.trackInventory
            assemblyTypeIndex = if (item.assemblyType.equals("Pre-assembled", ignoreCase = true)) 1 else 0

            // Match Category from List (ProductCategoryItem: name, id)
            item.categoryName?.let { catName ->
                selectedCategoryName = catName
                selectedCategoryId = categoryList.firstOrNull { it.name.equals(catName, ignoreCase = true) }?.id
            }

            // Match Warehouse from List (WarehouseDropdownItem: label, value)
            item.warehouseRestrictionName?.let { whName ->
                selectedWarehouseName = whName
                selectedWarehouseId = warehouseList.firstOrNull { it.label.equals(whName, ignoreCase = true) }?.value
            }
        }
    }

    fun handleSave() {
        val assemblyType = if (assemblyTypeIndex == 0) "On Order" else "Pre-assembled"
        val componentPairs = components.map { Pair(it.itemId, it.requiredQty) }

        if (isEdit && !editItemId.isNullOrBlank()) {
            viewModel.updateBulkItem(
                context = context,
                id = editItemId,
                name = itemName,
                sku = sku,
                description = description,
                categoryId = selectedCategoryId,                      // ✅ Selected Category ID
                brand = brand,
                unit = unit,
                costPrice = computedCostPrice,
                sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                taxPercent = taxPercentage.toDoubleOrNull() ?: 0.0,
                salesAccountId = null,
                purchaseAccountId = null,
                trackInventory = isInventoryTracked,
                assemblyType = assemblyType,
                warehouseRestrictionId = selectedWarehouseId,         // ✅ Selected Warehouse ID (value)
                components = componentPairs,
                imageUri = imageUri,
                onSuccess = onSaved
            )
        } else {
            viewModel.createBulkItem(
                context = context,
                name = itemName,
                sku = sku,
                description = description,
                categoryId = selectedCategoryId,                      // ✅ Selected Category ID
                brand = brand,
                unit = unit,
                costPrice = computedCostPrice,
                sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                taxPercent = taxPercentage.toDoubleOrNull() ?: 0.0,
                salesAccountId = null,
                purchaseAccountId = null,
                trackInventory = isInventoryTracked,
                assemblyType = assemblyType,
                warehouseRestrictionId = selectedWarehouseId,         // ✅ Selected Warehouse ID (value)
                components = componentPairs,
                imageUri = imageUri,
                onSuccess = onSaved
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                    TitleBar(if (isEdit) "Edit Bulk Item" else "Add Bulk Item", onClose)
                    HorizontalDivider(color = title_border)
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Basic Information", fontSize = tokens.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Status: Active", fontSize = tokens.bodySmall, color = TextLog)
                                Spacer(Modifier.width(6.dp))
                                MiniSwitch(checked = isActiveStatus, onCheckedChange = { isActiveStatus = it })
                            }
                        }
                        Spacer(Modifier.height(14.dp))

                        FormLabel("Item Name")
                        FormTextField(value = itemName, onValueChange = { itemName = it }, placeholder = "Starter Sewing Kit")

                        Spacer(Modifier.height(12.dp))
                        FormLabel("SKU")
                        FormTextField(value = sku, onValueChange = { sku = it }, placeholder = "CI-00003")

                        Spacer(Modifier.height(12.dp))
                        // ── Category Dropdown Connected to API ──
                        FormDropdown(
                            label = "Category",
                            value = selectedCategoryName.ifBlank { "Select Category" },
                            expanded = categoryExpanded,
                            onExpandChange = { categoryExpanded = it },
                            options = categoryList.map { it.name },
                            onOptionSelected = { chosenName ->
                                selectedCategoryName = chosenName
                                selectedCategoryId = categoryList.firstOrNull { it.name == chosenName }?.id
                            }
                        )

                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Brand")
                                FormTextField(value = brand, onValueChange = { brand = it }, placeholder = "Hari essential")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormDropdown(
                                    label = "Unit",
                                    value = unit,
                                    expanded = unitExpanded,
                                    onExpandChange = { unitExpanded = it },
                                    options = listOf("set", "piece", "box", "meter", "kg", "pack"),
                                    onOptionSelected = { unit = it }
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        FormLabel("Description")
                        FormTextArea(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Basic kit bundling thread, needle set and fabric scissors"
                        )
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
                        Spacer(Modifier.height(24.dp))
                        Text("Sales & Purchase Information", fontSize = tokens.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(14.dp))

                        Text("SALES INFORMATION", fontSize = tokens.label, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Selling Price")
                                FormTextField(value = sellingPrice, onValueChange = { sellingPrice = it }, placeholder = "450.00")
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Tax (%)")
                                FormTextField(value = taxPercentage, onValueChange = { taxPercentage = it }, placeholder = "18")
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        FormDropdown(
                            label = "Sales Account",
                            value = salesAccount,
                            expanded = salesAccountExpanded,
                            onExpandChange = { salesAccountExpanded = it },
                            options = listOf("Sales Revenue", "Other Income"),
                            onOptionSelected = { salesAccount = it }
                        )

                        Spacer(Modifier.height(18.dp))
                        Text("PURCHASE INFORMATION", fontSize = tokens.label, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormLabel("Cost Price")
                            Box(
                                modifier = Modifier
                                    .background(greenBg, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Auto-calculated", fontSize = tokens.label, color = darkGreenBg, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        FormTextField(value = "₹$computedCostPrice", onValueChange = {}, enabled = false)

                        Spacer(Modifier.height(12.dp))
                        FormDropdown(
                            label = "Purchase Account",
                            value = purchaseAccount,
                            expanded = purchaseAccountExpanded,
                            onExpandChange = { purchaseAccountExpanded = it },
                            options = listOf("Cost of Goods Sold (COGS)", "Raw Material Cost"),
                            onOptionSelected = { purchaseAccount = it }
                        )
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
                        Spacer(Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Components (Bill of Materials)", fontSize = tokens.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(light_grey, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("${components.size} items", fontSize = tokens.caption, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    SearchFilterBar(
                        query = componentSearchQuery,
                        onQueryChange = { componentSearchQuery = it },
                        placeholder = "Search to add component...",
                        isSearchBarAlone = true
                    )

                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
                        Spacer(Modifier.height(10.dp))
                        components.forEach { comp ->
                            ComponentFormCard(
                                comp = comp,
                                onQuantityChanged = { newQty ->
                                    components = components.map { if (it.itemId == comp.itemId) it.copy(requiredQty = newQty) else it }
                                },
                                onDelete = {
                                    components = components.filter { it.itemId != comp.itemId }
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                item {
                    AccordionSection(
                        iconPainter = painterResource(R.drawable.box),
                        title = "Inventory Settings",
                        expanded = inventorySettingsExpanded,
                        onHeaderClick = { inventorySettingsExpanded = !inventorySettingsExpanded }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Track Inventory", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("Monitor stock levels for this composite item", fontSize = tokens.caption, color = TextSecondary)
                            }
                            MiniSwitch(checked = isInventoryTracked, onCheckedChange = { isInventoryTracked = it })
                        }

                        Spacer(Modifier.height(14.dp))
                        Text("Assembly Type", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(light_grey, RoundedCornerShape(8.dp))
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (assemblyTypeIndex == 0) whiteBg else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { assemblyTypeIndex = 0 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("On Order", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = if (assemblyTypeIndex == 0) Primary else TextSecondary)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (assemblyTypeIndex == 1) whiteBg else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { assemblyTypeIndex = 1 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Pre-assembled", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = if (assemblyTypeIndex == 1) Primary else TextSecondary)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("‘On Order’ items are assembled only when a sales order is created.", fontSize = tokens.label, color = mutedText)

                        Spacer(Modifier.height(14.dp))
                        // ── Warehouse Restriction Dropdown Connected to API (using label & value) ──
                        FormDropdown(
                            label = "Warehouse Restriction",
                            value = selectedWarehouseName.ifBlank { "Select Warehouse" },
                            expanded = warehouseExpanded,
                            onExpandChange = { warehouseExpanded = it },
                            options = warehouseList.map { it.label },
                            onOptionSelected = { chosenLabel ->
                                selectedWarehouseName = chosenLabel
                                selectedWarehouseId = warehouseList.firstOrNull { it.label == chosenLabel }?.value
                            }
                        )
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            onBack = onClose,
            showBackArrow = false,
            trailingAction = TrailingFabAction.Update(
                label = if (isEdit) "Update Item" else "Save Item",
                onClick = { handleSave() }
            ),
            backWidthFraction = 0.25f,
            trailingWidthFraction = 0.35f
        )
    }
}

@Composable
fun ComponentFormCard(
    comp: ComponentFormItem,
    onQuantityChanged: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current

    WhiteCard(tokens = tokens, borderColor = Color.Transparent, padding = 12.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(yellowBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_desk),
                    contentDescription = null,
                    tint = yellowText,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(comp.name, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("SKU: ${comp.sku}", fontSize = tokens.caption, color = TextSecondary)
            }
            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = iconMuted, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = iconMuted,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onDelete() }
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).background(greentext, CircleShape))
                Spacer(Modifier.width(4.dp))
                Text("In Stock (${comp.stockQty})", fontSize = tokens.caption, color = darkGreenBg, fontWeight = FontWeight.Medium)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(light_grey, RoundedCornerShape(6.dp))
                        .border(1.dp, grey_border, RoundedCornerShape(6.dp))
                        .clickable { if (comp.requiredQty > 1) onQuantityChanged(comp.requiredQty - 1) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Text(
                    comp.requiredQty.toString(),
                    modifier = Modifier.padding(horizontal = 10.dp),
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Box(
                    modifier = Modifier
                        .background(light_grey, RoundedCornerShape(6.dp))
                        .border(1.dp, grey_border, RoundedCornerShape(6.dp))
                        .clickable { onQuantityChanged(comp.requiredQty + 1) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("+", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Cost", fontSize = tokens.label, color = iconMuted)
                Text("₹${"%.2f".format(comp.unitCost * comp.requiredQty)}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}
