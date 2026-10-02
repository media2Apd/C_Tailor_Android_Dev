@file:Suppress("unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.items.all_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.model.inventory.InventoryItem
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.background_light_purple
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.lightGray
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.DataCardField
import com.cuso.tailor.view.composable.DeleteModel
import com.cuso.tailor.view.composable.DynamicIslandError
import com.cuso.tailor.view.composable.DynamicIslandSuccess
import com.cuso.tailor.view.composable.FabConfig
import com.cuso.tailor.view.composable.FabScaffold
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.FilterSectionType
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.ThreeDotLoading
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Returns badge foreground and background color based on stock status.
 */
private fun inventoryStatusColors(status: String?): Pair<Color, Color> {
    val safeStatus = status.orEmpty()
    return when {
        safeStatus.contains("In Stock", ignoreCase = true) && !safeStatus.contains("inactive", ignoreCase = true) ->
            Pair(darkGreenBg, greenBg)

        safeStatus.contains("Stock Not Assigned", ignoreCase = true) ->
            Pair(redText, redBg)

        safeStatus.contains("draft", ignoreCase = true) ->
            Pair(yellowText, yellowBg)

        else ->
            Pair(TextSecondary, light_grey)
    }
}

@Composable
fun InventoryScreen(
    onClose: () -> Unit = {},
    onAddItem: () -> Unit = {},
    onViewItem: (InventoryItem) -> Unit = {},
    onEditItem: () -> Unit = {},
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    onBreadCrumbClick: () -> Unit = {}
) {
    val rawItems by inventoryViewModel.inventoryItems.collectAsStateWithLifecycle()
    val isLoading by inventoryViewModel.isLoadingInventoryItems.collectAsStateWithLifecycle()
    val isLoadingMore by inventoryViewModel.isLoadingMoreInventoryItems.collectAsStateWithLifecycle()
    val canLoadMore by inventoryViewModel.canLoadMoreInventoryItems.collectAsStateWithLifecycle()
    val errorMessage by inventoryViewModel.inventoryError.collectAsStateWithLifecycle()
    val viewOneItem by inventoryViewModel.viewOneItem.collectAsStateWithLifecycle()

    var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }
    var successToastMessage by remember { mutableStateOf<String?>(null) }
    var errorToastMessage by remember { mutableStateOf<String?>(null) }

    val items: List<InventoryItem> = rawItems
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // ── Filter Drawer State & Sections (As per Image) ──
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Type",
                    type = FilterSectionType.CHECKBOX_LIST,
                    options = listOf(
                        FilterOption(id = "goods", label = "Goods"),
                        FilterOption(id = "service", label = "Service")
                    )
                ),
                FilterSection(
                    title = "Stock Status",
                    type = FilterSectionType.CHECKBOX_LIST,
                    options = listOf(
                        FilterOption(id = "in_stock", label = "In Stock"),
                        FilterOption(id = "low_stock", label = "Low Stock"),
                        FilterOption(id = "out_of_stock", label = "Out of Stock"),
                        FilterOption(id = "not_tracked", label = "Not Tracked")
                    )
                )
            )
        )
    }

    val activeFilterCount by remember(filterSections) {
        derivedStateOf { filterSections.sumOf { sec -> sec.options.count { it.isSelected } } }
    }

    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(searchQuery) {
        if (!isInitialized) {
            isInitialized = true
            inventoryViewModel.fetchInventoryItems()
        } else {
            delay(400)
            inventoryViewModel.fetchInventoryItems(search = searchQuery.trim().ifBlank { null })
        }
    }

    // Scroll pagination
    LaunchedEffect(listState, canLoadMore, searchQuery) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            totalItemsNumber > 0 && lastVisibleItemIndex >= (totalItemsNumber - 2)
        }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                if (isNearBottom && canLoadMore && !inventoryViewModel.isLoadingMoreInventoryItems.value &&
                    !inventoryViewModel.isLoadingInventoryItems.value && searchQuery.isBlank()
                ) {
                    inventoryViewModel.loadMoreInventoryItems()
                }
            }
    }

    // ── Filter logic applying Search + Drawer filters ──
    val filteredItems by remember(items, searchQuery, filterSections) {
        derivedStateOf {
            val selectedTypes = filterSections.find { it.title == "Type" }
                ?.options?.filter { it.isSelected }?.map { it.label.lowercase() } ?: emptyList()
            val selectedStatuses = filterSections.find { it.title == "Stock Status" }
                ?.options?.filter { it.isSelected }?.map { it.label.lowercase() } ?: emptyList()

            items.filter { item ->
                val matchesSearch = searchQuery.isBlank() ||
                        item.name.contains(searchQuery, ignoreCase = true) ||
                        item.sku.contains(searchQuery, ignoreCase = true)

                val matchesType = selectedTypes.isEmpty() || selectedTypes.any { it.equals(item.type, ignoreCase = true) }

                val itemStockStatus = item.stockStatus?.lowercase().orEmpty()
                val matchesStatus = selectedStatuses.isEmpty() || selectedStatuses.any { status ->
                    when (status) {
                        "in stock" -> itemStockStatus.contains("in stock", ignoreCase = true)
                        "low stock" -> itemStockStatus.contains("low", ignoreCase = true)
                        "out of stock" -> itemStockStatus.contains("out of stock", ignoreCase = true) || itemStockStatus.contains("stock not assigned", ignoreCase = true)
                        "not tracked" -> !item.trackInventory
                        else -> itemStockStatus.contains(status, ignoreCase = true)
                    }
                }

                matchesSearch && matchesType && matchesStatus
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleBar(title = "All Items", onClose = onClose)
            }

            Column(Modifier.fillMaxWidth()) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Items...",
                    filterCount = activeFilterCount,
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { filterDrawerState.open() }
                )
            }
            HorizontalDivider(color = title_border)

            when {
                isLoading && items.isEmpty() -> {
                    ListSkeleton()
                }

                errorMessage != null && items.isEmpty() -> {
                    AppErrorState(
                        title = "Failed to load inventory screen",
                        message = "Something went wrong. Please check your connection and try again.",
                        onRetry = { inventoryViewModel.fetchInventoryItems(search = searchQuery.trim().ifBlank { null }) }
                    )
                }

                filteredItems.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(lightGray)
                            .padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(background_light_purple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Primary, modifier = Modifier.size(30.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || activeFilterCount > 0) "No Matching Items" else "No Items Found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || activeFilterCount > 0) "Try adjusting your search or filters" else "Start by adding your first inventory item",
                            fontSize = 13.sp,
                            color = mutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    FabScaffold(
                        modifier = Modifier.fillMaxSize(),
                        fab = FabConfig(
                            label = "Add Item",
                            icon = Icons.Default.Add,
                            onClick = onAddItem
                        )
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Transparent)
                        ) {
                            itemsIndexed(
                                items = filteredItems,
                                key = { index, item -> item._id.ifBlank { "item_$index" } }
                            ) { _, item ->
                                val (badgeFg, badgeBg) = inventoryStatusColors(item.stockStatus)
                                val stockText = if (!item.trackInventory) "—" else item.currentStock.toInt().toString()
                                val itemType = item.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.ifBlank { "N/A" }

                                DataCard(
                                    item = item,
                                    modifier = Modifier.animateItem(),
                                    smalltitle = "${item.sku.ifBlank { "—" }} • SKU",
                                    subtitle = item.name.ifBlank { "Unnamed Item" },
                                    topBadgeText = item.stockStatus,
                                    topBadgeTextColor = badgeFg,
                                    topBadgeBgColor = badgeBg,
                                    topBadgeInline = true,
                                    footerAsRows = true,
                                    footerFields = listOf(
                                        DataCardField(label = "Type", text = itemType),
                                        DataCardField(label = "Stock", text = stockText),
                                        DataCardField(label = "Selling Price", text = "₹${"%.2f".format(item.sellingPrice)}")
                                    ),
                                    actions = listOf(
                                        MenuAction(label = "View", icon = Icons.Default.Visibility, onClick = { onViewItem(item) }),
                                        MenuAction(label = "Edit", icon = Icons.Default.Edit, onClick = {
                                            if (item._id.isNotBlank()) inventoryViewModel.onViewOneClicked(item._id)
                                        }),
                                        MenuAction(label = "Delete", icon = Icons.Default.Delete, onClick = { itemToDelete = item })
                                    )
                                )
                            }

                            if (isLoadingMore) {
                                item(key = "pagination_threedot_loader") {
                                    ThreeDotLoading(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
                                }
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }

        // ── Filter Items Drawer ──
        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Items",
            sections = filterSections,
            onApply = { updated -> filterSections = updated },
            onClearAll = {
                filterSections = filterSections.map { sec ->
                    sec.copy(options = sec.options.map { it.copy(isSelected = false) })
                }
            }
        )

        DynamicIslandSuccess(message = successToastMessage, onDismiss = { successToastMessage = null })
        DynamicIslandError(message = errorToastMessage ?: errorMessage, onDismiss = { errorToastMessage = null })
    }
}