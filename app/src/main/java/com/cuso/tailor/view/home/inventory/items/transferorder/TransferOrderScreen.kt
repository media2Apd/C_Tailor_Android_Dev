@file:Suppress("unused", "AssignedValueIsNeverRead", "unusedVariable", "VariableNeverRead")

package com.cuso.tailor.view.home.inventory.items.transferorder

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.snapshotFlow
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
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.DecreaseStockRequest
import com.cuso.tailor.model.inventory.IncreaseStockRequest
import com.cuso.tailor.model.inventory.StockAdjustmentData
import com.cuso.tailor.model.inventory.TransferStockRequest
import com.cuso.tailor.model.inventory.WarehouseDropdownItem
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.textSubdued
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.ErrorMapper
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.SheetValue
import com.cuso.tailor.view.composable.ThreeDotLoading
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

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

    // API States from InventoryViewModel
    val adjustmentsList by viewModel.stockAdjustmentsList.collectAsState()
    val isLoadingAdjustments by viewModel.isLoadingAdjustments.collectAsState()
    val isLoadingMoreAdjustments by viewModel.isLoadingMoreAdjustments.collectAsState()
    val canLoadMoreAdjustments by viewModel.canLoadMoreAdjustments.collectAsState()

    val warehouseDropdown by viewModel.warehouseDropdown.collectAsState()
    val validReasons by viewModel.validAdjustmentReasons.collectAsState()

    val isSubmittingAdjustment by viewModel.isSubmittingAdjustment.collectAsState()
    val adjustmentSuccessMessage by viewModel.adjustmentSuccessMessage.collectAsState()
    val adjustmentErrorMessage by viewModel.adjustmentErrorMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var sheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var selectedAdjustment by remember { mutableStateOf<StockAdjustmentData?>(null) }

    // Scroll state for LazyColumn to support infinite scroll
    val listState = rememberLazyListState()

    // Fetch API Data on screen launch
    LaunchedEffect(Unit) {
        viewModel.clearAdjustmentAlerts()
        viewModel.fetchStockAdjustments(reset = true, adjustmentType = "transfer")
        viewModel.loadWarehouseDropdown()
        viewModel.fetchValidAdjustmentReasons()
        settingsViewModel.fetchBins(isRefresh = true)
    }

    // Scroll listener: triggers next page fetch only when crossing the bottom threshold
    LaunchedEffect(listState, canLoadMoreAdjustments, searchQuery) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            // Trigger when 2 items away from bottom
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 2)
        }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                if (isNearBottom &&
                    canLoadMoreAdjustments &&
                    !isLoadingMoreAdjustments &&
                    !isLoadingAdjustments &&
                    searchQuery.isBlank()
                ) {
                    viewModel.loadMoreStockAdjustments()
                }
            }
    }

    LaunchedEffect(adjustmentSuccessMessage) {
        if (!adjustmentSuccessMessage.isNullOrBlank()) {
            sheetState = SheetValue.Hidden
            viewModel.fetchStockAdjustments(reset = true, adjustmentType = "transfer")
        }
    }

    // Client-side search filter
    val filteredList = remember(adjustmentsList, searchQuery) {
        if (searchQuery.isBlank()) adjustmentsList
        else {
            adjustmentsList.filter { item ->
                item.itemName.contains(searchQuery, ignoreCase = true) ||
                        item.itemSku.contains(searchQuery, ignoreCase = true) ||
                        item.adjustmentCode.orEmpty().contains(searchQuery, ignoreCase = true) ||
                        item.originWarehouseName.contains(searchQuery, ignoreCase = true) ||
                        item.destinationWarehouseName.contains(searchQuery, ignoreCase = true) ||
                        (item.reason?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF9FAFB))) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Row(modifier = Modifier.fillMaxWidth()) {
                    TitleBar("Transfer Stock", onClose)
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
                    placeholder = "Search SKU, Item, Warehouse, Code...",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )

                HorizontalDivider(color = grey_border)

                if (isLoadingAdjustments && adjustmentsList.isEmpty()) {
                    ListSkeleton()
                } else if (!adjustmentErrorMessage.isNullOrBlank() && adjustmentsList.isEmpty()) {
                    AppErrorState(
                        title = "Failed to load stock adjustments",
                        message = adjustmentErrorMessage?.let { ErrorMapper.map(it) } ?: "Something went wrong. Please check your connection and try again.",
                        onRetry = {
                            viewModel.fetchStockAdjustments(reset = true, adjustmentType = "transfer")
                        }
                    )
                } else if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "No stock adjustments found" else "No matching adjustments found",
                            fontSize = tokens.bodyMedium,
                            color = mutedText
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        items(
                            items = filteredList,
                            key = { it.id.ifBlank { it.hashCode().toString() } }
                        ) { adjustment ->
                            AllOrdersItemCard(
                                adjustment = adjustment,
                                tokens = tokens,
                                onClick = {
                                    selectedAdjustment = adjustment
                                    sheetState = SheetValue.Expanded
                                }
                            )
                        }

                        // Three-dot loader is shown only while the next page request is in-flight
                        if (isLoadingMoreAdjustments) {
                            item(key = "pagination_threedot_loader") {
                                ThreeDotLoading(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                )
                            }
                        }

                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }
            }
        }

        DynamicIslandSuccess(
            message = adjustmentSuccessMessage,
            onDismiss = { viewModel.clearAdjustmentAlerts() }
        )

        DynamicIslandError(
            message = adjustmentErrorMessage?.takeIf { it.isNotBlank() && adjustmentsList.isNotEmpty() }?.let { ErrorMapper.map(it) },
            onDismiss = { viewModel.clearAdjustmentAlerts() }
        )
    }
}

// ─────────────────────────────────────────────────────────────
// REAL DATA ITEM CARD
// ─────────────────────────────────────────────────────────────

@Composable
private fun AllOrdersItemCard(
    adjustment: StockAdjustmentData,
    tokens: AppDesignTokens,
    onClick: () -> Unit
) {
    val displayCode = adjustment.adjustmentCode?.ifBlank { adjustment.id.takeLast(6).uppercase() } ?: adjustment.id.takeLast(6).uppercase()
    val isTransfer = adjustment.type.equals("transfer", ignoreCase = true)
    val isIncrease = adjustment.type.equals("increase", ignoreCase = true)
    val isReversed = adjustment.isReversed

    val formattedDate = remember(adjustment.createdAt) {
        val rawDate = adjustment.createdAt?.take(10).orEmpty()
        val parts = rawDate.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else "—"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .clickable { onClick() }
            .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Code Badge
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEEF2FF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "#$displayCode",
                            color = Primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Status Badge (Completed / Reversed)
                    Box(
                        modifier = Modifier
                            .background(
                                if (isReversed) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                                RoundedCornerShape(30.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (isReversed) "Reversed" else "Completed",
                            color = if (isReversed) Color(0xFFDC2626) else Color(0xFF16A34A),
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

            // Item Name + Variant
            Text(
                text = adjustment.itemName,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )

            Spacer(Modifier.height(10.dp))

            // Warehouse Movement Route
            Box(
                modifier = Modifier
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                    .padding(horizontal = 5.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isIncrease) "Supplier / Stock In" else adjustment.originWarehouseName,
                        fontSize = tokens.caption,
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
                        text = if (isTransfer) adjustment.destinationWarehouseName else if (isIncrease) adjustment.originWarehouseName else "Stock Out",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = if (isTransfer) Primary else title_color
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // 3 Columns: QUANTITY | DATE | REASON
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "QUANTITY",
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${adjustment.quantity.toInt()}",
                        fontSize = tokens.bodySmall,
                        color = if (isIncrease) Color(0xFF16A34A) else if (isTransfer) Primary else Color(0xFFDC2626)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DATE",
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        fontSize = tokens.bodySmall,
                        color = title_color
                    )
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "HANDLED BY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9CA3AF),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = adjustment.handledByName,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color,
                        maxLines = 1
                    )
                }
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 8.dp)
}

// ─────────────────────────────────────────────────────────────
// ADJUST / TRANSFER STOCK MODAL CONTENT
// ─────────────────────────────────────────────────────────────

@Composable
fun AdjustStockModalContent(
    selectedAdjustment: StockAdjustmentData?,
    warehouseList: List<WarehouseDropdownItem>,
    reasonList: List<String>,
    isSubmitting: Boolean,
    tokens: AppDesignTokens,
    onDismiss: () -> Unit,
    onIncreaseStock: (IncreaseStockRequest) -> Unit,
    onDecreaseStock: (DecreaseStockRequest) -> Unit,
    onTransferStock: (TransferStockRequest) -> Unit
) {
    var selectedAdjustmentType by remember(selectedAdjustment) {
        mutableStateOf(
            when (selectedAdjustment?.type?.lowercase()) {
                "increase" -> AdjustmentType.Increase
                "decrease" -> AdjustmentType.Decrease
                else -> AdjustmentType.TransferStock
            }
        )
    }

    val defaultWarehouseId = selectedAdjustment?.origin?.warehouseId ?: warehouseList.firstOrNull()?.value.orEmpty()
    var originWarehouseId by remember(selectedAdjustment, warehouseList) { mutableStateOf(defaultWarehouseId) }
    var destinationWarehouseId by remember(selectedAdjustment, warehouseList) {
        mutableStateOf(selectedAdjustment?.destination?.warehouseId ?: warehouseList.firstOrNull { it.value != defaultWarehouseId }?.value ?: "")
    }

    var originWarehouseExpanded by remember { mutableStateOf(false) }
    var destWarehouseExpanded by remember { mutableStateOf(false) }

    var adjustmentQuantityText by remember(selectedAdjustment) {
        mutableStateOf(selectedAdjustment?.quantity?.toInt()?.toString() ?: "15")
    }
    var reason by remember(reasonList, selectedAdjustment) {
        mutableStateOf(selectedAdjustment?.reason ?: "Stock Rebalancing")
    }
    var reasonExpanded by remember { mutableStateOf(false) }
    var referenceNumber by remember(selectedAdjustment) {
        mutableStateOf(selectedAdjustment?.referenceNumber ?: "REF-${System.currentTimeMillis().toString().takeLast(6)}")
    }

    val parsedQuantity = adjustmentQuantityText.toDoubleOrNull() ?: 0.0
    val originWarehouseName = warehouseList.find { it.value == originWarehouseId }?.label ?: selectedAdjustment?.originWarehouseName ?: "Main Warehouse"
    val destWarehouseName = warehouseList.find { it.value == destinationWarehouseId }?.label ?: selectedAdjustment?.destinationWarehouseName ?: "Branch Warehouse"

    val unit = selectedAdjustment?.unit ?: "pcs"
    val warehouseOptionNames = warehouseList.map { it.label }.ifEmpty { listOf("Main Warehouse", "Branch Warehouse") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = tokens.screenPadding)
            .padding(bottom = 32.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Product Snapshot Card
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
                        text = selectedAdjustment?.itemName ?: "Select Item",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SKU", fontSize = 11.sp, color = mutedText)
                            Text(
                                text = selectedAdjustment?.itemSku ?: "—",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Variant", fontSize = 11.sp, color = mutedText)
                            Text(
                                text = selectedAdjustment?.itemVariant ?: "—",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Adjustment Type",
            fontSize = tokens.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = title_color
        )

        Spacer(Modifier.height(10.dp))

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

        // Quantity Input
        Text(text = "Adjustment Quantity", fontSize = tokens.bodySmall, color = textSubdued, fontWeight = FontWeight.Medium)
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
                    text = unit.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textSubdued
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Reason Dropdown
        val reasonOptions = reasonList.ifEmpty {
            listOf("Stock Rebalancing", "Stock Count Correction", "Damaged Goods", "Inter-warehouse Transfer", "Other")
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
                textStyle = TextStyle(fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(24.dp))

        // Action Buttons
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

            Button(
                onClick = {
                    val itemId = selectedAdjustment?.itemId ?: return@Button
                    if (parsedQuantity <= 0) return@Button

                    when (selectedAdjustmentType) {
                        AdjustmentType.Increase -> {
                            onIncreaseStock(
                                IncreaseStockRequest(
                                    itemId = itemId,
                                    quantity = parsedQuantity,
                                    reason = reason,
                                    warehouseId = originWarehouseId.takeIf { it.isNotBlank() },
                                    referenceNumber = referenceNumber
                                )
                            )
                        }
                        AdjustmentType.Decrease -> {
                            onDecreaseStock(
                                DecreaseStockRequest(
                                    itemId = itemId,
                                    quantity = parsedQuantity,
                                    reason = reason,
                                    warehouseId = originWarehouseId.takeIf { it.isNotBlank() },
                                    referenceNumber = referenceNumber
                                )
                            )
                        }
                        AdjustmentType.TransferStock -> {
                            onTransferStock(
                                TransferStockRequest(
                                    itemId = itemId,
                                    fromWarehouseId = originWarehouseId,
                                    toWarehouseId = destinationWarehouseId,
                                    quantity = parsedQuantity,
                                    reason = reason
                                )
                            )
                        }
                    }
                },
                enabled = !isSubmitting && parsedQuantity > 0 && selectedAdjustment?.itemId?.isNotBlank() == true,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = disabled),
                modifier = Modifier
                    .weight(1.5f)
                    .height(tokens.buttonHeight * 1.05f)
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
}

// ─────────────────────────────────────────────────────────────
// REUSABLE SUB-COMPONENTS
// ─────────────────────────────────────────────────────────────

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