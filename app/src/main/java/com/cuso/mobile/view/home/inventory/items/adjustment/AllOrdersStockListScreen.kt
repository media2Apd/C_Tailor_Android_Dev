@file:Suppress("unused")

package com.cuso.mobile.view.home.inventory.items.adjustment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.DecreaseStockRequest
import com.cuso.mobile.model.inventory.IncreaseStockRequest
import com.cuso.mobile.model.inventory.StockSummaryItemDto
import com.cuso.mobile.model.inventory.TransferStockRequest
import com.cuso.mobile.model.inventory.WarehouseDropdownItem
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.badgeGrey
import com.cuso.mobile.ui.theme.disabled
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.light_blue
import com.cuso.mobile.ui.theme.light_blue_border
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.primary_light
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.sectionBorder
import com.cuso.mobile.ui.theme.textSubdued
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ErrorMapper
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.StatusBadge
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.InventoryViewModel
import com.cuso.mobile.viewmodel.SettingsViewModel

enum class AdjustmentType {
    Increase, Decrease, TransferStock
}

@Composable
fun AllOrdersStockListScreen(
    preselectedItemId: String? = null,
    initialAdjustmentType: AdjustmentType = AdjustmentType.TransferStock,
    onClose: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    val stockSummaryList by viewModel.stockSummaryList.collectAsState()
    val isLoadingSummary by viewModel.isLoadingStockSummary.collectAsState()
    val warehouseDropdown by viewModel.warehouseDropdown.collectAsState()
    val validReasons by viewModel.validAdjustmentReasons.collectAsState()

    val isSubmittingAdjustment by viewModel.isSubmittingAdjustment.collectAsState()
    val adjustmentSuccessMessage by viewModel.adjustmentSuccessMessage.collectAsState()
    val adjustmentErrorMessage by viewModel.adjustmentErrorMessage.collectAsState()
    val stockSummaryError by viewModel.stockSummaryError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var sheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var selectedStockItem by remember { mutableStateOf<StockSummaryItemDto?>(null) }
    var activeAdjustmentType by remember { mutableStateOf(initialAdjustmentType) }

    LaunchedEffect(Unit) {
        viewModel.clearAdjustmentAlerts()
        viewModel.clearStockSummaryAlerts()
        viewModel.fetchStockSummaryList()
        viewModel.loadWarehouseDropdown()
        viewModel.fetchValidAdjustmentReasons()
        settingsViewModel.fetchBins(isRefresh = true)
    }

    // Auto-open Bottom Sheet when preselectedItemId matches
    LaunchedEffect(stockSummaryList, preselectedItemId, initialAdjustmentType) {
        if (!preselectedItemId.isNullOrBlank() && stockSummaryList.isNotEmpty()) {
            val matchedItem = stockSummaryList.find { it.itemId == preselectedItemId }
            if (matchedItem != null) {
                selectedStockItem = matchedItem
                activeAdjustmentType = initialAdjustmentType
                sheetState = SheetValue.Expanded
            }
        }
    }

    LaunchedEffect(adjustmentSuccessMessage) {
        if (!adjustmentSuccessMessage.isNullOrBlank()) {
            sheetState = SheetValue.Hidden
            viewModel.fetchStockSummaryList()
        }
    }

    val filteredList = remember(stockSummaryList, searchQuery) {
        if (searchQuery.isBlank()) stockSummaryList
        else {
            stockSummaryList.filter { item ->
                item.product.contains(searchQuery, ignoreCase = true) ||
                        item.sku.contains(searchQuery, ignoreCase = true) ||
                        item.warehouse.contains(searchQuery, ignoreCase = true) ||
                        (item.variant?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "Stock Adjustments",
                    onClose = onClose
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Stock Adjustment....",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )

                HorizontalDivider(color = grey_border)

                if (isLoadingSummary && stockSummaryList.isEmpty()) {
                    ListSkeleton()
                } else if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No stock items found",
                            fontSize = tokens.bodyMedium,
                            color = mutedText
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        filteredList.forEach { item ->
                            StockAdjustmentCardItem(
                                stockItem = item,
                                tokens = tokens,
                                onAdjustClick = {
                                    selectedStockItem = item
                                    activeAdjustmentType = AdjustmentType.TransferStock
                                    sheetState = SheetValue.Expanded
                                }
                            )
                        }
                    }
                }
            }
        }

        SmoothBottomSheet(
            state = sheetState,
            onStateChange = { sheetState = it },
            title = "Adjust Stock",
            subtitle = selectedStockItem?.product ?: "Stock Adjustment",
            expandedFraction = 0.95f,
            collapsedFraction = 0.60f,
            maxBlurRadius = 16.dp,
            maxScrimAlpha = 0.45f,
            scrollableContent = true,
            collapsedCornerRadius = tokens.cardCornerRadius,
            sheetBackgroundColor = whiteBg,
            onDismissRequest = {
                viewModel.clearAdjustmentAlerts()
                sheetState = SheetValue.Hidden
            }
        ) {
            selectedStockItem?.let { item ->
                AdjustStockModalContent(
                    stockItem = item,
                    initialAdjustmentType = activeAdjustmentType,
                    warehouseList = warehouseDropdown,
                    reasonList = validReasons,
                    isSubmitting = isSubmittingAdjustment,
                    tokens = tokens,
                    settingsViewModel = settingsViewModel,
                    onDismiss = {
                        viewModel.clearAdjustmentAlerts()
                        sheetState = SheetValue.Hidden
                    },
                    onIncreaseStock = { req -> viewModel.submitIncreaseStock(req) },
                    onDecreaseStock = { req -> viewModel.submitDecreaseStock(req) },
                    onTransferStock = { req -> viewModel.submitStockTransfer(req) }
                )
            }
        }

        DynamicIslandSuccess(
            message = adjustmentSuccessMessage,
            onDismiss = { viewModel.clearAdjustmentAlerts() }
        )

        DynamicIslandError(
            message = (adjustmentErrorMessage ?: stockSummaryError)?.takeIf { it.isNotBlank() }?.let { ErrorMapper.map(it) },
            onDismiss = {
                viewModel.clearAdjustmentAlerts()
                viewModel.clearStockSummaryAlerts()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// DATA CARD COMPOSABLE
// ─────────────────────────────────────────────────────────────

@Composable
private fun StockAdjustmentCardItem(
    stockItem: StockSummaryItemDto,
    tokens: AppDesignTokens,
    onAdjustClick: () -> Unit
) {
    val unitLabel = stockItem.unit?.ifBlank { "pcs" } ?: "pcs"
    val displayProductName = stockItem.product.ifBlank { "Item Code: " + stockItem.itemId.take(8) }
    val displaySku = stockItem.sku.ifBlank { "—" }
    val warehouseName = stockItem.warehouse.ifBlank { "Main Warehouse" }
    val variantText = stockItem.variant?.ifBlank { "Standard" } ?: "Standard"

    val availableStock = stockItem.available.toInt()
    val reservedStock = stockItem.reserved.toInt()
    val reorderLevelText = if (stockItem.reorderLevel > 0) stockItem.reorderLevel.toInt().toString() else "—"

    val formattedDate = remember(stockItem.lastUpdated) {
        val rawDate = stockItem.lastUpdated?.take(10).orEmpty()
        val parts = rawDate.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else rawDate
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
    ) {
        DataCard(
            item = stockItem,
            showDivider = true,
            headerContent = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(
                            text = "SKU: $displaySku",
                            bgColor = primary_light,
                            textColor = Primary,
                            showDot = false,
                            cornerRadius = 6.dp
                        )
                        StatusBadge(
                            text = variantText,
                            bgColor = primary_light,
                            textColor = Primary,
                            showDot = false,
                            cornerRadius = 6.dp
                        )
                    }

                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(tokens.iconSize * 1.3f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = mutedText,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }
            },
            title = displayProductName,
            titleColor = Color(0xFF111827),
            titleFontWeight = FontWeight.SemiBold,
            content = {
                HorizontalDivider(color = grey_border)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Warehouse", fontSize = tokens.caption, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = warehouseName,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Available", fontSize = tokens.caption, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "$availableStock $unitLabel",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reserved", fontSize = tokens.caption, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "$reservedStock $unitLabel",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reorder Level", fontSize = tokens.caption, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = reorderLevelText,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF111827)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = grey_border)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (formattedDate.isNotBlank()) "Updated: $formattedDate" else "",
                            fontSize = tokens.caption,
                            color = title_color
                        )

                        Button(
                            onClick = onAdjustClick,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                            modifier = Modifier.height(tokens.buttonHeight * 0.82f)
                        ) {
                            Text(
                                text = "Adjust",
                                color = whiteBg,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        )
    }
    Spacer(Modifier.height(10.dp))
}

// ─────────────────────────────────────────────────────────────
// ADJUST STOCK MODAL CONTENT
// ─────────────────────────────────────────────────────────────

@Composable
fun AdjustStockModalContent(
    stockItem: StockSummaryItemDto,
    initialAdjustmentType: AdjustmentType = AdjustmentType.TransferStock,
    warehouseList: List<WarehouseDropdownItem>,
    reasonList: List<String>,
    isSubmitting: Boolean,
    tokens: AppDesignTokens,
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit,
    onIncreaseStock: (IncreaseStockRequest) -> Unit,
    onDecreaseStock: (DecreaseStockRequest) -> Unit,
    onTransferStock: (TransferStockRequest) -> Unit
) {
    var selectedAdjustmentType by remember(initialAdjustmentType) { mutableStateOf(initialAdjustmentType) }

    val defaultWarehouseId = stockItem.warehouseId.ifBlank { warehouseList.firstOrNull()?.value.orEmpty() }
    var originWarehouseId by remember(stockItem.warehouseId, warehouseList) { mutableStateOf(defaultWarehouseId) }
    var destinationWarehouseId by remember(warehouseList) {
        mutableStateOf(warehouseList.firstOrNull { it.value != defaultWarehouseId }?.value ?: warehouseList.firstOrNull()?.value.orEmpty())
    }

    var originWarehouseExpanded by remember { mutableStateOf(false) }
    var destWarehouseExpanded by remember { mutableStateOf(false) }

    val allBins by settingsViewModel.bins.collectAsState()

    val originWarehouseBins = remember(allBins, originWarehouseId) {
        allBins.filter { it.warehouseIdValue.isBlank() || it.warehouseIdValue == originWarehouseId }
            .ifEmpty { allBins }
    }

    val destWarehouseBins = remember(allBins, destinationWarehouseId) {
        allBins.filter { it.warehouseIdValue.isBlank() || it.warehouseIdValue == destinationWarehouseId }
            .ifEmpty { allBins }
    }

    var fromBinId by remember { mutableStateOf("") }
    var toBinId by remember { mutableStateOf("") }
    var fromBinExpanded by remember { mutableStateOf(false) }
    var toBinExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(originWarehouseBins, originWarehouseId) {
        if (fromBinId.isBlank() || originWarehouseBins.none { it.id == fromBinId }) {
            fromBinId = originWarehouseBins.firstOrNull { it.status.equals("active", true) }?.id
                ?: originWarehouseBins.firstOrNull()?.id.orEmpty()
        }
    }

    LaunchedEffect(destWarehouseBins, destinationWarehouseId) {
        if (toBinId.isBlank() || destWarehouseBins.none { it.id == toBinId }) {
            toBinId = destWarehouseBins.firstOrNull { it.status.equals("active", true) }?.id
                ?: destWarehouseBins.firstOrNull()?.id.orEmpty()
        }
    }

    var adjustmentQuantityText by remember { mutableStateOf("10") }
    var reason by remember(reasonList, selectedAdjustmentType) {
        mutableStateOf(reasonList.firstOrNull() ?: "Stock Rebalancing")
    }
    var reasonExpanded by remember { mutableStateOf(false) }
    var referenceNumber by remember { mutableStateOf("REF-${System.currentTimeMillis().toString().takeLast(6)}") }
    var handledBy by remember { mutableStateOf("Warehouse Staff") }

    val parsedQuantity = adjustmentQuantityText.toDoubleOrNull() ?: 0.0
    val originWarehouseName = warehouseList.find { it.value == originWarehouseId }?.label ?: stockItem.warehouse
    val destWarehouseName = warehouseList.find { it.value == destinationWarehouseId }?.label ?: "Destination Warehouse"

    val fromBinName = originWarehouseBins.find { it.id == fromBinId }?.name ?: if (fromBinId.isNotBlank()) fromBinId else "Select Bin"
    val toBinName = destWarehouseBins.find { it.id == toBinId }?.name ?: if (toBinId.isNotBlank()) toBinId else "Select Bin"

    val unit = stockItem.unit?.ifBlank { "pcs" } ?: "pcs"

    val currentStock = stockItem.available
    val reservedStock = stockItem.reserved
    val availableStock = (currentStock - reservedStock).coerceAtLeast(0.0)
    val isExceeded = parsedQuantity > availableStock && selectedAdjustmentType != AdjustmentType.Increase

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = tokens.screenPadding)
            .padding(bottom = 28.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Product Snapshot Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(light_blue, RoundedCornerShape(tokens.cardCornerRadius))
                .border(1.dp, light_blue_border, RoundedCornerShape(tokens.cardCornerRadius))
                .padding(tokens.screenPadding)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(whiteBg, CircleShape)
                        .border(1.dp, light_blue_border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shirts),
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stockItem.product.ifBlank { "Unnamed Item" },
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SKU", fontSize = tokens.caption, color = mutedText)
                            Text(
                                text = stockItem.sku.ifBlank { "—" },
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = textSubdued
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Variant", fontSize = tokens.caption, color = mutedText)
                            Text(
                                text = stockItem.variant ?: "Standard",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = textSubdued
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Triple Stat Metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMetricBox(
                label = "Current",
                value = currentStock.toInt().toString(),
                unit = unit,
                modifier = Modifier.weight(1f),
                tokens = tokens,
                isHighlighted = false
            )
            StatMetricBox(
                label = "Reserved",
                value = reservedStock.toInt().toString(),
                unit = unit,
                modifier = Modifier.weight(1f),
                tokens = tokens,
                isHighlighted = false
            )
            StatMetricBox(
                label = "Available",
                value = availableStock.toInt().toString(),
                unit = unit,
                modifier = Modifier.weight(1f),
                tokens = tokens,
                isHighlighted = true
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Adjustment Details",
            fontSize = tokens.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = title_color
        )

        Spacer(Modifier.height(12.dp))

        // Adjustment Type Segmented Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdjustmentTypePill("Increase", selectedAdjustmentType == AdjustmentType.Increase, tokens, Modifier.weight(1f)) {
                selectedAdjustmentType = AdjustmentType.Increase
            }
            AdjustmentTypePill("Decrease", selectedAdjustmentType == AdjustmentType.Decrease, tokens, Modifier.weight(1f)) {
                selectedAdjustmentType = AdjustmentType.Decrease
            }
            AdjustmentTypePill("Transfer\nStock", selectedAdjustmentType == AdjustmentType.TransferStock, tokens, Modifier.weight(1f)) {
                selectedAdjustmentType = AdjustmentType.TransferStock
            }
        }

        Spacer(Modifier.height(16.dp))

        val warehouseOptionNames = warehouseList.map { it.label }.ifEmpty { listOf("Main Warehouse") }

        if (selectedAdjustmentType == AdjustmentType.TransferStock) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CustomFormDropdown(
                        label = "From Warehouse",
                        value = originWarehouseName,
                        expanded = originWarehouseExpanded,
                        tokens = tokens,
                        onExpandChange = { originWarehouseExpanded = it },
                        options = warehouseOptionNames,
                        onOptionSelected = { selected ->
                            originWarehouseId = warehouseList.find { it.label == selected }?.value.orEmpty()
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    CustomFormDropdown(
                        label = "To Warehouse",
                        value = destWarehouseName,
                        expanded = destWarehouseExpanded,
                        tokens = tokens,
                        onExpandChange = { destWarehouseExpanded = it },
                        options = warehouseOptionNames,
                        onOptionSelected = { selected ->
                            destinationWarehouseId = warehouseList.find { it.label == selected }?.value.orEmpty()
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            val fromBinOptions = originWarehouseBins.map { it.name }.ifEmpty { listOf("Default Bin") }
            val toBinOptions = destWarehouseBins.map { it.name }.ifEmpty { listOf("Default Bin") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CustomFormDropdown(
                        label = "From Bin",
                        value = fromBinName,
                        expanded = fromBinExpanded,
                        tokens = tokens,
                        onExpandChange = { fromBinExpanded = it },
                        options = fromBinOptions,
                        onOptionSelected = { selected ->
                            fromBinId = originWarehouseBins.find { it.name == selected }?.id.orEmpty()
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    CustomFormDropdown(
                        label = "To Bin",
                        value = toBinName,
                        expanded = toBinExpanded,
                        tokens = tokens,
                        onExpandChange = { toBinExpanded = it },
                        options = toBinOptions,
                        onOptionSelected = { selected ->
                            toBinId = destWarehouseBins.find { it.name == selected }?.id.orEmpty()
                        }
                    )
                }
            }
        } else {
            val originBinOptions = originWarehouseBins.map { it.name }.ifEmpty { listOf("Default Bin") }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    CustomFormDropdown(
                        label = "Warehouse",
                        value = originWarehouseName,
                        expanded = originWarehouseExpanded,
                        tokens = tokens,
                        onExpandChange = { originWarehouseExpanded = it },
                        options = warehouseOptionNames,
                        onOptionSelected = { selected ->
                            originWarehouseId = warehouseList.find { it.label == selected }?.value.orEmpty()
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    CustomFormDropdown(
                        label = "Bin Location",
                        value = fromBinName,
                        expanded = fromBinExpanded,
                        tokens = tokens,
                        onExpandChange = { fromBinExpanded = it },
                        options = originBinOptions,
                        onOptionSelected = { selected ->
                            fromBinId = originWarehouseBins.find { it.name == selected }?.id.orEmpty()
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Quantity Input Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Adjustment Quantity",
                    fontSize = tokens.bodySmall,
                    color = textSubdued,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.fieldHeight * 1.15f)
                        .background(badgeGrey, RoundedCornerShape(10.dp))
                        .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = adjustmentQuantityText,
                            onValueChange = { adjustmentQuantityText = it },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = textSubdued
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = unit.uppercase(),
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = textSubdued
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Handled By",
                    fontSize = tokens.bodySmall,
                    color = textSubdued,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.fieldHeight * 1.15f)
                        .background(badgeGrey, RoundedCornerShape(10.dp))
                        .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = handledBy,
                        onValueChange = { handledBy = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = textSubdued
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (isExceeded) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = redText,
                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Exceeds available stock (${availableStock.toInt()} $unit)",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = redText
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        val reasonOptions = reasonList.ifEmpty {
            when (selectedAdjustmentType) {
                AdjustmentType.Increase -> listOf("Stock Count Correction", "Found Stock", "Supplier Return Reversed")
                AdjustmentType.Decrease -> listOf("Damaged Goods", "Inventory Loss", "Expired Stock", "Count Discrepancy")
                AdjustmentType.TransferStock -> listOf("Stock Rebalancing", "Branch Replenishment", "Inter-warehouse Transfer")
            }
        }

        CustomFormDropdown(
            label = "Reason for Adjustment",
            value = reason,
            expanded = reasonExpanded,
            tokens = tokens,
            onExpandChange = { reasonExpanded = it },
            options = reasonOptions,
            onOptionSelected = { reason = it }
        )

        Spacer(Modifier.height(14.dp))

        Text(text = "Reference Number", fontSize = tokens.bodySmall, color = textSubdued, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.fieldHeight * 1.15f)
                .background(badgeGrey, RoundedCornerShape(10.dp))
                .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = referenceNumber,
                onValueChange = { referenceNumber = it },
                textStyle = TextStyle(
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = textSubdued
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, sectionBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight)
            ) {
                Text("Cancel", color = title_color, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (parsedQuantity <= 0) return@Button

                when (selectedAdjustmentType) {
                    AdjustmentType.Increase -> {
                        val increaseReq = IncreaseStockRequest(
                            itemId = stockItem.itemId,
                            quantity = parsedQuantity,
                            reason = reason,
                            warehouseId = originWarehouseId.takeIf { it.isNotBlank() },
                            binId = fromBinId.takeIf { it.isNotBlank() },
                            referenceNumber = referenceNumber
                        )
                        onIncreaseStock(increaseReq)
                    }

                    AdjustmentType.Decrease -> {
                        val decreaseReq = DecreaseStockRequest(
                            itemId = stockItem.itemId,
                            quantity = parsedQuantity,
                            reason = reason,
                            warehouseId = originWarehouseId.takeIf { it.isNotBlank() },
                            binId = fromBinId.takeIf { it.isNotBlank() },
                            referenceNumber = referenceNumber
                        )
                        onDecreaseStock(decreaseReq)
                    }

                    AdjustmentType.TransferStock -> {
                        val transferReq = TransferStockRequest(
                            itemId = stockItem.itemId,
                            fromWarehouseId = originWarehouseId,
                            fromBinId = fromBinId.takeIf { it.isNotBlank() },
                            toWarehouseId = destinationWarehouseId,
                            toBinId = toBinId.takeIf { it.isNotBlank() },
                            quantity = parsedQuantity,
                            reason = reason
                        )
                        onTransferStock(transferReq)
                    }
                }
            },
            enabled = !isSubmitting && parsedQuantity > 0 && !isExceeded,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.buttonHeight * 1.1f)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = whiteBg, modifier = Modifier.size(tokens.iconSize))
            } else {
                Text(
                    text = "Save Adjustment",
                    color = whiteBg,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// REUSABLE SUB-COMPONENTS
// ─────────────────────────────────────────────────────────────

@Composable
private fun StatMetricBox(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier,
    tokens: AppDesignTokens,
    isHighlighted: Boolean
) {
    Box(
        modifier = modifier
            .background(if (isHighlighted) light_blue else badgeGrey, RoundedCornerShape(12.dp))
            .border(1.dp, if (isHighlighted) light_blue_border else sectionBorder, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = tokens.caption,
                color = if (isHighlighted) Primary else textSubdued,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlighted) Primary else title_color
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isHighlighted) Primary.copy(alpha = 0.8f) else mutedText,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun AdjustmentTypePill(
    label: String,
    isSelected: Boolean,
    tokens: AppDesignTokens,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(tokens.fieldHeight * 1.35f)
            .background(if (isSelected) primary_light else whiteBg, RoundedCornerShape(10.dp))
            .border(1.dp, if (isSelected) Primary else sectionBorder, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Primary else title_color,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp
        )
    }
}

@Composable
private fun CustomFormDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    tokens: AppDesignTokens,
    onExpandChange: (Boolean) -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    Column {
        Text(text = label, fontSize = tokens.bodySmall, color = textSubdued, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        FormDropdown(
            label = "",
            value = value,
            expanded = expanded,
            onExpandChange = onExpandChange,
            options = options,
            onOptionSelected = onOptionSelected
        )
    }
}