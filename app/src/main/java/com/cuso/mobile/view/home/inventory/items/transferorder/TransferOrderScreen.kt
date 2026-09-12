@file:Suppress("unused")

package com.cuso.mobile.view.home.inventory.items.transferorder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
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
import com.cuso.mobile.ui.theme.disabled
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.sectionBorder
import com.cuso.mobile.ui.theme.textSubdued
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.ErrorMapper
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.InventoryViewModel
import com.cuso.mobile.viewmodel.SettingsViewModel

enum class AdjustmentType {
    Increase, Decrease, TransferStock
}

@Composable
fun TransferOrdersStockListScreen(
    preselectedItemId: String? = null,
    isTransferMode: Boolean = false,
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

    LaunchedEffect(Unit) {
        viewModel.clearAdjustmentAlerts()
        viewModel.clearStockSummaryAlerts()
        viewModel.fetchStockSummaryList()
        viewModel.loadWarehouseDropdown()
        viewModel.fetchValidAdjustmentReasons()
        settingsViewModel.fetchBins(isRefresh = true)
    }

    LaunchedEffect(stockSummaryList, preselectedItemId) {
        if (!preselectedItemId.isNullOrBlank() && stockSummaryList.isNotEmpty()) {
            val matchedItem = stockSummaryList.find { it.itemId == preselectedItemId }
            if (matchedItem != null) {
                selectedStockItem = matchedItem
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

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                // Header Bar matching Screen 1 & 2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TitleBar("All Orders", onClose)
                }
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
                    placeholder = "Search Customers...",
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
                            AllOrdersItemCard(
                                stockItem = item,
                                tokens = tokens,
                                onClick = {
                                    selectedStockItem = item
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
            title = "Transfer Stock",
            subtitle = "Adjustment ID: ADJ-${selectedStockItem?.itemId?.takeLast(5) ?: "99231"}",
            expandedFraction = 0.95f,
            collapsedFraction = 0.65f,
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
// ALL ORDERS LIST CARD (Image 1 & 2 design)
// ─────────────────────────────────────────────────────────────

@Composable
private fun AllOrdersItemCard(
    stockItem: StockSummaryItemDto,
    tokens: AppDesignTokens,
    onClick: () -> Unit
) {
    val displayId = stockItem.itemId.takeLast(6).ifBlank { "123456" }
    val displayProductName = stockItem.product.ifBlank { "Linen Shirt" }
    val warehouseName = stockItem.warehouse.ifBlank { "North Hub" }
    val quantity = stockItem.available.toInt().toString()
    val formattedDate = remember(stockItem.lastUpdated) {
        val rawDate = stockItem.lastUpdated?.take(10).orEmpty()
        val parts = rawDate.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else "14/03/2026"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .clickable { onClick() }
            .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ID Badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ID: #$displayId",
                            color = Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Completed Badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Completed",
                            color = Color(0xFF16A34A),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = { onClick() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = mutedText,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Item Title
            Text(
                text = displayProductName,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )

            Spacer(Modifier.height(10.dp))

            // Warehouse Route Pill
            Box(
                modifier = Modifier
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = warehouseName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = mutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "South Dist.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // 3 Columns: QUANTITY | DATE | HANDLED BY
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "QUANTITY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = quantity,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "DATE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HANDLED BY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Hameed",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 8.dp)
}

// ─────────────────────────────────────────────────────────────
// ADJUST / TRANSFER STOCK MODAL CONTENT (Images 3 & 4 Design)
// ─────────────────────────────────────────────────────────────

@Composable
fun AdjustStockModalContent(
    stockItem: StockSummaryItemDto,
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
    var selectedAdjustmentType by remember { mutableStateOf(AdjustmentType.TransferStock) }

    val defaultWarehouseId = stockItem.warehouseId.ifBlank { warehouseList.firstOrNull()?.value.orEmpty() }
    var originWarehouseId by remember(stockItem.warehouseId, warehouseList) { mutableStateOf(defaultWarehouseId) }
    var destinationWarehouseId by remember(warehouseList) {
        mutableStateOf(warehouseList.firstOrNull { it.value != defaultWarehouseId }?.value ?: warehouseList.firstOrNull()?.value.orEmpty())
    }

    var originWarehouseExpanded by remember { mutableStateOf(false) }
    var destWarehouseExpanded by remember { mutableStateOf(false) }

    val allBins by settingsViewModel.bins.collectAsState()

    val originWarehouseBins = remember(allBins, originWarehouseId) {
        allBins.filter { it.warehouseIdValue.isBlank() || it.warehouseIdValue == originWarehouseId }.ifEmpty { allBins }
    }
    val destWarehouseBins = remember(allBins, destinationWarehouseId) {
        allBins.filter { it.warehouseIdValue.isBlank() || it.warehouseIdValue == destinationWarehouseId }.ifEmpty { allBins }
    }

    var fromBinId by remember { mutableStateOf("") }
    var toBinId by remember { mutableStateOf("") }

    LaunchedEffect(originWarehouseBins, originWarehouseId) {
        if (fromBinId.isBlank() || originWarehouseBins.none { it.id == fromBinId }) {
            fromBinId = originWarehouseBins.firstOrNull { it.status.equals("active", true) }?.id ?: originWarehouseBins.firstOrNull()?.id.orEmpty()
        }
    }

    LaunchedEffect(destWarehouseBins, destinationWarehouseId) {
        if (toBinId.isBlank() || destWarehouseBins.none { it.id == toBinId }) {
            toBinId = destWarehouseBins.firstOrNull { it.status.equals("active", true) }?.id ?: destWarehouseBins.firstOrNull()?.id.orEmpty()
        }
    }

    var adjustmentQuantityText by remember { mutableStateOf("900") }
    var reason by remember(reasonList, selectedAdjustmentType) {
        mutableStateOf("Stock Rebalancing")
    }
    var reasonExpanded by remember { mutableStateOf(false) }
    var referenceNumber by remember { mutableStateOf("AUDIT-2026-03") }
    var handledBy by remember { mutableStateOf("Warehouse Manager") }

    val parsedQuantity = adjustmentQuantityText.toDoubleOrNull() ?: 0.0
    val originWarehouseName = warehouseList.find { it.value == originWarehouseId }?.label ?: stockItem.warehouse.ifBlank { "Factory Warehouse" }
    val destWarehouseName = warehouseList.find { it.value == destinationWarehouseId }?.label ?: "Retail Store Chennai"

    val unit = stockItem.unit?.ifBlank { "M" } ?: "M"
    val currentStock = if (stockItem.available > 0) stockItem.available else 950.0
    val reservedStock = if (stockItem.reserved > 0) stockItem.reserved else 120.0
    val availableStock = (currentStock - reservedStock).coerceAtLeast(0.0)
    val isExceeded = parsedQuantity > availableStock && selectedAdjustmentType != AdjustmentType.Increase

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = tokens.screenPadding)
            .padding(bottom = 32.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Product Snapshot Card (Icon + Title + SKU + Variant + Location)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF6F8FF), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(whiteBg, CircleShape)
                        .border(1.dp, Color(0xFFE0E7FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shirts),
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stockItem.product.ifBlank { "Linen Shirt – Premium Blue" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SKU", fontSize = 11.sp, color = mutedText)
                            Text(
                                text = stockItem.sku.ifBlank { "FAB-ITL-220" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Variant", fontSize = 11.sp, color = mutedText)
                            Text(
                                text = stockItem.variant ?: "Sky Blue",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = originWarehouseName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // Triple Stat Metrics Row (Current, Reserved, Available)
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

        val warehouseOptionNames = warehouseList.map { it.label }.ifEmpty { listOf("Factory Warehouse", "Retail Store Chennai") }

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
        } else {
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
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
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
                                color = title_color
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (unit == "M") "METERS" else unit.uppercase(),
                            fontSize = 11.sp,
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
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
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
                            color = title_color
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
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Exceeds available stock (${availableStock.toInt()} $unit)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = redText
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Visual Transfer Diagram Card (Screen 3 & 4) ──
        if (selectedAdjustmentType == AdjustmentType.TransferStock) {
            TransferVisualizerCard(
                fromWarehouseName = originWarehouseName.take(12),
                fromStock = "${currentStock.toInt()} $unit",
                toWarehouseName = destWarehouseName.take(14),
                toStock = "120 $unit",
                transferAmount = "${if (parsedQuantity > 0) parsedQuantity.toInt() else 200} $unit",
                fromBalance = "${(currentStock - (if (parsedQuantity > 0) parsedQuantity else 200.0)).toInt().coerceAtLeast(0)} $unit",
                toBalance = "${(120 + (if (parsedQuantity > 0) parsedQuantity else 200.0)).toInt()} $unit"
            )
            Spacer(Modifier.height(16.dp))
        }

        // Reason for Adjustment Dropdown
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
                .background(Color(0xFFF9FAFB), RoundedCornerShape(10.dp))
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
                    color = title_color
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(18.dp))

        // ── Transfer Summary Dark Blue Card (Screenshot 4) ──
        if (selectedAdjustmentType == AdjustmentType.TransferStock) {
            TransferSummaryCard(
                movementText = "${String.format("%.2f", if (parsedQuantity > 0) parsedQuantity else 200.0)} $unit",
                originWarehouse = originWarehouseName,
                targetWarehouse = destWarehouseName
            )
            Spacer(Modifier.height(20.dp))
        }

        // Action Buttons (Cancel & Adjust Another)
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
                    .height(tokens.buttonHeight * 1.05f)
            ) {
                Text("Cancel", color = title_color, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = {
                    adjustmentQuantityText = "0"
                    referenceNumber = "REF-${System.currentTimeMillis().toString().takeLast(6)}"
                },
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, sectionBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.buttonHeight * 1.05f)
            ) {
                Text("Adjust Another", color = Primary, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Save Adjustment Full-Width Button
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
                .height(tokens.buttonHeight * 1.15f)
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
// TRANSFER VISUALIZER CARD (Screen 3 & 4 diagram)
// ─────────────────────────────────────────────────────────────

@Composable
private fun TransferVisualizerCard(
    fromWarehouseName: String,
    fromStock: String,
    toWarehouseName: String,
    toStock: String,
    transferAmount: String,
    fromBalance: String,
    toBalance: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(fromWarehouseName, fontSize = 11.sp, color = mutedText)
                    Spacer(Modifier.height(4.dp))
                    Text(fromStock, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = title_color)
                }

                // Transfer Pill in center
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Transfer $transferAmount", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(toWarehouseName, fontSize = 11.sp, color = mutedText)
                    Spacer(Modifier.height(4.dp))
                    Text(toStock, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = title_color)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFE5E7EB))
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("After Transfer Balance", fontSize = 11.sp, color = mutedText)

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Factory ", fontSize = 11.sp, color = mutedText)
                        Text(fromBalance, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = title_color)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Retail ", fontSize = 11.sp, color = mutedText)
                        Text(toBalance, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Primary)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TRANSFER SUMMARY DARK BLUE CARD (Screen 4)
// ─────────────────────────────────────────────────────────────

@Composable
private fun TransferSummaryCard(
    movementText: String,
    originWarehouse: String,
    targetWarehouse: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2E3BE8), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Transfer Summary",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Inventory Movement", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                Text(movementText, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Origin Warehouse", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                Text(originWarehouse, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Target Warehouse", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                Text(targetWarehouse, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Status", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Ready to Transfer",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
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
            .background(if (isHighlighted) Color(0xFFEEF2FF) else Color(0xFFF9FAFB), RoundedCornerShape(12.dp))
            .border(1.dp, if (isHighlighted) Color(0xFFC7D2FE) else sectionBorder, RoundedCornerShape(12.dp))
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
            .background(if (isSelected) Color(0xFFEEF2FF) else whiteBg, RoundedCornerShape(10.dp))
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