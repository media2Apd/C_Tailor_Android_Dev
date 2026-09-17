package com.cuso.mobile.view.home.inventory.procurement.location_management

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.AssignStockLocationRequest
import com.cuso.mobile.model.inventory.StockLocationItemDto
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
import com.cuso.mobile.viewmodel.InventoryViewModel
import com.cuso.mobile.viewmodel.SettingsViewModel

@Composable
fun StockLocationFormScreen(
    item: StockLocationItemDto? = null,
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // API dropdown state collectors
    val warehouseDropdown by inventoryViewModel.warehouseDropdown.collectAsState()
    val floorDropdown by inventoryViewModel.floorDropdown.collectAsState()
    val sectionDropdown by inventoryViewModel.sectionDropdown.collectAsState()
    val rackDropdown by inventoryViewModel.rackDropdown.collectAsState()
    val binDropdown by inventoryViewModel.binDropdown.collectAsState()

    // API execution and item states
    val viewOneItem by inventoryViewModel.viewOneItem.collectAsState()
    val selectedStockLocationItem by inventoryViewModel.selectedStockLocationItem.collectAsState()
    val productCategories by settingsViewModel.productCategories.collectAsState()

    val isSubmitting by inventoryViewModel.isSubmittingStockLocation.collectAsState()
    val actionSuccessMessage by inventoryViewModel.stockLocationActionSuccess.collectAsState()
    val actionErrorMessage by inventoryViewModel.stockLocationActionError.collectAsState()

    // Determine the active item from either composable argument or ViewModel state
    val effectiveItem = item ?: selectedStockLocationItem

    // Safely extract the inventory item ID
    val targetItemId = remember(effectiveItem) {
        effectiveItem?.let { target ->
            val methods = target::class.java.methods
            listOf("getItemId", "getInventoryItemId", "getInventoryId", "get_id", "getId")
                .firstNotNullOfOrNull { methodName ->
                    methods.firstOrNull { it.name.equals(methodName, ignoreCase = true) }
                        ?.invoke(target)
                        ?.toString()
                        ?.takeIf { it.isNotBlank() }
                } ?: target.id
        }
    }

    // Warehouse selection state
    var selectedWarehouseName by remember { mutableStateOf("Select Warehouse") }
    var selectedWarehouseId by remember { mutableStateOf<String?>(null) }
    var warehouseDropdownExpanded by remember { mutableStateOf(false) }

    // Floor selection state
    var selectedFloorName by remember { mutableStateOf("Select Floor") }
    var selectedFloorId by remember { mutableStateOf<String?>(null) }
    var floorDropdownExpanded by remember { mutableStateOf(false) }

    // Section selection state
    var selectedSectionName by remember { mutableStateOf("Select Section") }
    var selectedSectionId by remember { mutableStateOf<String?>(null) }
    var sectionDropdownExpanded by remember { mutableStateOf(false) }

    // Rack selection state
    var selectedRackName by remember { mutableStateOf("Select Rack") }
    var selectedRackId by remember { mutableStateOf<String?>(null) }
    var rackDropdownExpanded by remember { mutableStateOf(false) }

    // Bin selection state
    var selectedBinName by remember { mutableStateOf("Select Bin") }
    var selectedBinId by remember { mutableStateOf<String?>(null) }
    var binDropdownExpanded by remember { mutableStateOf(false) }

    // Sequential cascading dependency rules
    val isFloorEnabled = selectedWarehouseId != null
    val isSectionEnabled = isFloorEnabled && selectedFloorId != null
    val isRackEnabled = isSectionEnabled && selectedSectionId != null
    val isBinEnabled = isRackEnabled && selectedRackId != null

    // Optional form details
    var stockCondition by remember { mutableStateOf("good") }
    var asOfDate by remember { mutableStateOf("2026-09-17") }
    var enableRotationTracking by remember { mutableStateOf(true) }

    // Initial data load
    LaunchedEffect(targetItemId) {
        inventoryViewModel.loadWarehouseDropdown()
        settingsViewModel.fetchProductCategories()

        if (!targetItemId.isNullOrBlank()) {
            inventoryViewModel.fetchInventoryViewOne(targetItemId)
        }
    }

    // Observe operation results
    LaunchedEffect(actionSuccessMessage) {
        actionSuccessMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            inventoryViewModel.clearStockLocationActionAlerts()
            inventoryViewModel.clearHierarchyDropdowns()
            inventoryViewModel.clearViewOneItem()
            onSave()
        }
    }

    LaunchedEffect(actionErrorMessage) {
        actionErrorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            inventoryViewModel.clearStockLocationActionAlerts()
        }
    }

    // Resolve variant label from API response
    val viewOneVariant = remember(viewOneItem) {
        viewOneItem?.let { vo ->
            val directLabel = vo::class.java.methods.firstOrNull {
                it.name in listOf("getVariantLabel", "getVariant")
            }?.invoke(vo)?.toString()

            if (!directLabel.isNullOrBlank()) return@let directLabel

            val selectionsMethod = vo::class.java.methods.firstOrNull { it.name == "getVariantSelections" }
            val selections = selectionsMethod?.invoke(vo) as? List<*>
            selections?.mapNotNull { selection ->
                selection?.let {
                    it::class.java.methods.firstOrNull { m -> m.name == "getValue" }?.invoke(it)?.toString()
                }
            }?.takeIf { it.isNotEmpty() }?.joinToString(" / ")
        }
    }

    // Resolve unit from API response
    val viewOneUnit = remember(viewOneItem) {
        viewOneItem?.let { vo ->
            vo::class.java.methods.firstOrNull { it.name == "getUnit" }?.invoke(vo)?.toString()
        }
    }

    // Resolve stock count from API response
    val viewOneStockQuantity = remember(viewOneItem) {
        viewOneItem?.let { vo ->
            val itemTotalStockObj = vo::class.java.methods.firstOrNull { it.name == "getItemTotalStock" }?.invoke(vo)
            if (itemTotalStockObj != null) {
                val totalQty = itemTotalStockObj::class.java.methods.firstOrNull {
                    it.name in listOf("getTotalQuantity", "getTotalAvailable")
                }?.invoke(itemTotalStockObj)

                when (totalQty) {
                    is Number -> return@let totalQty.toLong()
                    is String -> totalQty.toLongOrNull()?.let { return@let it }
                    else -> {}
                }
            }

            val directStock = vo::class.java.methods.firstOrNull {
                it.name in listOf("getTotalStock", "getStock", "getQuantity")
            }?.invoke(vo)

            when (directStock) {
                is Number -> directStock.toLong()
                is String -> directStock.toLongOrNull()
                else -> null
            }
        }
    }

    // Derived product metadata
    val productName = viewOneItem?.name ?: effectiveItem?.name ?: "Stock Item"
    val brandDisplay = viewOneItem?.brand?.takeIf { it.isNotBlank() } ?: effectiveItem?.brand?.takeIf { it.isNotBlank() } ?: "No Brand"
    val matchedCategory = productCategories.find { it.id == viewOneItem?.categoryId }?.name
    val categoryDisplay = matchedCategory ?: effectiveItem?.category ?: "Apparel"

    // Table display formatting
    val skuDisplay = viewOneItem?.sku ?: effectiveItem?.sku ?: "-"
    val variantDisplay = viewOneVariant?.takeIf { it.isNotBlank() } ?: effectiveItem?.variantLabel?.takeIf { it.isNotBlank() } ?: "-"
    val stockCount = viewOneStockQuantity ?: effectiveItem?.totalStock?.toLong() ?: 0L
    val unitDisplay = viewOneUnit?.takeIf { it.isNotBlank() } ?: "Pieces (Pcs)"
    val stockDisplay = "$stockCount $unitDisplay"

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
                    title = "Location Form",
                    onClose = {
                        inventoryViewModel.clearHierarchyDropdowns()
                        inventoryViewModel.clearViewOneItem()
                        onClose()
                    }
                )
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = tokens.screenPadding,
                    bottom = 80.dp // Offset to avoid overlapping with bottom floating action buttons
                ),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)
            ) {
                // Product summary header card
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
                            .padding(tokens.screenPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(tokens.iconSize * 2.4f)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize * 1.2f)
                            )
                        }

                        Spacer(modifier = Modifier.width(tokens.extraPadding * 1.2f))

                        Column {
                            Text(
                                text = productName,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$brandDisplay - $categoryDisplay",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Location hierarchy selection section
                item {
                    FormCardContainer(
                        tokens = tokens,
                        title = "Location",
                        trailingBadge = {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                                    .background(activity_purple_bg)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = selectedWarehouseName.takeIf { it != "Select Warehouse" } ?: "Warehouse",
                                    fontSize = tokens.label,
                                    fontWeight = FontWeight.Bold,
                                    color = activity_purple
                                )
                            }
                        }
                    ) {
                        // Level 1: Warehouse selection
                        FormLabel("Warehouse", isRequired = true)
                        FormDropdown(
                            value = selectedWarehouseName,
                            expanded = warehouseDropdownExpanded,
                            onExpandChange = { warehouseDropdownExpanded = it },
                            options = if (warehouseDropdown.isEmpty()) {
                                listOf("No Warehouses Available")
                            } else {
                                warehouseDropdown.map { it.label }
                            },
                            onOptionSelected = { label ->
                                selectedWarehouseName = label
                                val found = warehouseDropdown.find { it.label == label }
                                selectedWarehouseId = found?.value

                                // Reset downstream selections
                                selectedFloorName = "Select Floor"
                                selectedFloorId = null
                                selectedSectionName = "Select Section"
                                selectedSectionId = null
                                selectedRackName = "Select Rack"
                                selectedRackId = null
                                selectedBinName = "Select Bin"
                                selectedBinId = null

                                found?.value?.let { warehouseId ->
                                    inventoryViewModel.loadFloorDropdown(warehouseId)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        // Level 2 & Level 3: Floor and Section selections
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            // Level 2: Floor selection (requires Warehouse)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(if (isFloorEnabled) 1f else 0.45f)
                            ) {
                                FormLabel("Floor", isRequired = true)
                                FormDropdown(
                                    value = if (isFloorEnabled) selectedFloorName else "Select Warehouse first",
                                    expanded = if (isFloorEnabled) floorDropdownExpanded else false,
                                    onExpandChange = {
                                        if (isFloorEnabled) floorDropdownExpanded = it
                                    },
                                    options = when {
                                        !isFloorEnabled -> emptyList()
                                        floorDropdown.isEmpty() -> listOf("No Floors Available")
                                        else -> floorDropdown.map { it.name }
                                    },
                                    onOptionSelected = { name ->
                                        if (isFloorEnabled) {
                                            selectedFloorName = name
                                            val found = floorDropdown.find { it.name == name }
                                            selectedFloorId = found?.id

                                            // Reset downstream selections
                                            selectedSectionName = "Select Section"
                                            selectedSectionId = null
                                            selectedRackName = "Select Rack"
                                            selectedRackId = null
                                            selectedBinName = "Select Bin"
                                            selectedBinId = null

                                            found?.id?.let { floorId ->
                                                inventoryViewModel.loadSectionDropdown(floorId)
                                            }
                                        }
                                    }
                                )
                            }

                            // Level 3: Section selection (requires Floor)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(if (isSectionEnabled) 1f else 0.45f)
                            ) {
                                FormLabel("Section", isRequired = true)
                                FormDropdown(
                                    value = if (isSectionEnabled) selectedSectionName else "Select Floor first",
                                    expanded = if (isSectionEnabled) sectionDropdownExpanded else false,
                                    onExpandChange = {
                                        if (isSectionEnabled) sectionDropdownExpanded = it
                                    },
                                    options = when {
                                        !isSectionEnabled -> emptyList()
                                        sectionDropdown.isEmpty() -> listOf("No Sections Available")
                                        else -> sectionDropdown.map { it.name }
                                    },
                                    onOptionSelected = { name ->
                                        if (isSectionEnabled) {
                                            selectedSectionName = name
                                            val found = sectionDropdown.find { it.name == name }
                                            selectedSectionId = found?.id

                                            // Reset downstream selections
                                            selectedRackName = "Select Rack"
                                            selectedRackId = null
                                            selectedBinName = "Select Bin"
                                            selectedBinId = null

                                            found?.id?.let { sectionId ->
                                                inventoryViewModel.loadRackDropdown(sectionId)
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        // Level 4 & Level 5: Rack and Bin selections
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            // Level 4: Rack selection (requires Section)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(if (isRackEnabled) 1f else 0.45f)
                            ) {
                                FormLabel("Rack", isRequired = true)
                                FormDropdown(
                                    value = if (isRackEnabled) selectedRackName else "Select Section first",
                                    expanded = if (isRackEnabled) rackDropdownExpanded else false,
                                    onExpandChange = {
                                        if (isRackEnabled) rackDropdownExpanded = it
                                    },
                                    options = when {
                                        !isRackEnabled -> emptyList()
                                        rackDropdown.isEmpty() -> listOf("No Racks Available")
                                        else -> rackDropdown.map { it.name }
                                    },
                                    onOptionSelected = { name ->
                                        if (isRackEnabled) {
                                            selectedRackName = name
                                            val found = rackDropdown.find { it.name == name }
                                            selectedRackId = found?.id

                                            // Reset downstream selection
                                            selectedBinName = "Select Bin"
                                            selectedBinId = null

                                            found?.id?.let { rackId ->
                                                inventoryViewModel.loadBinDropdown(rackId)
                                            }
                                        }
                                    }
                                )
                            }

                            // Level 5: Bin selection (requires Rack)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(if (isBinEnabled) 1f else 0.45f)
                            ) {
                                FormLabel("Bin", isRequired = true)
                                FormDropdown(
                                    value = if (isBinEnabled) selectedBinName else "Select Rack first",
                                    expanded = if (isBinEnabled) binDropdownExpanded else false,
                                    onExpandChange = {
                                        if (isBinEnabled) binDropdownExpanded = it
                                    },
                                    options = when {
                                        !isBinEnabled -> emptyList()
                                        binDropdown.isEmpty() -> listOf("No Bins Available")
                                        else -> binDropdown.map { it.name }
                                    },
                                    onOptionSelected = { name ->
                                        if (isBinEnabled) {
                                            selectedBinName = name
                                            val found = binDropdown.find { it.name == name }
                                            selectedBinId = found?.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Stock summary table section
                item {
                    FormCardContainer(
                        tokens = tokens,
                        title = "Stock"
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Table header container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(light_blue_border, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SKU",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary,
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .padding(end = 8.dp),
                                        textAlign = TextAlign.Start
                                    )
                                    Text(
                                        text = "Variant",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary,
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .padding(end = 8.dp),
                                        textAlign = TextAlign.Start
                                    )
                                    Text(
                                        text = "Total Stock",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary,
                                        modifier = Modifier.weight(1.2f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Table row value container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(light_blue, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                    .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = skuDisplay,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Normal,
                                        color = title_color,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .padding(end = 8.dp),
                                        textAlign = TextAlign.Start
                                    )
                                    Text(
                                        text = variantDisplay,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Normal,
                                        color = title_color,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .padding(end = 8.dp),
                                        textAlign = TextAlign.Start
                                    )
                                    Text(
                                        text = stockDisplay,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Normal,
                                        color = title_color,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1.2f),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }

                // Optional inventory metadata section
                item {
                    FormCardContainer(tokens = tokens, title = "Optional Details") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("Stock Condition")
                                FormTextField(
                                    value = stockCondition,
                                    onValueChange = { stockCondition = it },
                                    placeholder = "e.g. good"
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                FormLabel("As of Date")
                                DatePickerField(
                                    value = asOfDate,
                                    onDateSelected = { asOfDate = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Stock Rotation",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = title_color
                                    )
                                    Text(
                                        text = "Enable Stock Rotation Tracking",
                                        fontSize = tokens.caption,
                                        color = mutedText
                                    )
                                }
                                MiniSwitch(
                                    checked = enableRotationTracking,
                                    onCheckedChange = { enableRotationTracking = it }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom floating action navigation bar
            StepNavigationFab(
                showBack = true,
                onBack = {
                    inventoryViewModel.clearHierarchyDropdowns()
                    inventoryViewModel.clearViewOneItem()
                    onClose()
                },
                backLabel = "Cancel",
                backEnabled = !isSubmitting,
                showBackArrow = false,
                showTrailingArrow = false,
                isLoading = isSubmitting,
                trailingAction = TrailingFabAction.Update(
                    label = "Save",
                    enabled = isBinEnabled && selectedBinId != null && !isSubmitting,
                    isLoading = isSubmitting,
                    onClick = {
                        val activeItemId = targetItemId
                        val activeBinId = selectedBinId

                        if (activeItemId.isNullOrBlank()) {
                            Toast.makeText(context, "Item ID is missing", Toast.LENGTH_SHORT).show()
                            return@Update
                        }

                        if (activeBinId.isNullOrBlank()) {
                            Toast.makeText(context, "Please select a Bin", Toast.LENGTH_SHORT).show()
                            return@Update
                        }

                        // Convert date string to YYYY-MM-DD format
                        val formattedDate = runCatching {
                            if (asOfDate.contains("/")) {
                                val parts = asOfDate.split("/")
                                if (parts.size == 3) {
                                    "${parts[2]}-${parts[1].padStart(2, '0')}-${parts[0].padStart(2, '0')}"
                                } else asOfDate
                            } else asOfDate
                        }.getOrDefault(asOfDate)

                        val requestPayload = AssignStockLocationRequest(
                            itemId = activeItemId,
                            binId = activeBinId,
                            quantity = stockCount.toDouble(),
                            reservedQuantity = 0.0,
                            stockCondition = stockCondition.trim().lowercase().ifBlank { "good" },
                            rotationMethod = if (enableRotationTracking) "fifo" else "none",
                            asOfDate = formattedDate
                        )

                        inventoryViewModel.assignStockLocation(requestPayload)
                    }
                )
            )
        }
    }
}

@Composable
private fun FormCardContainer(
    tokens: AppDesignTokens,
    title: String,
    trailingBadge: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
                trailingBadge?.invoke()
            }
            Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))
            HorizontalDivider(color = grey_border)
            Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))
            content()
        }
    }
}