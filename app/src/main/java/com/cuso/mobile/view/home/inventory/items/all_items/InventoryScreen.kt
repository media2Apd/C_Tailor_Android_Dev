@file:Suppress("unused")

package com.cuso.mobile.view.home.inventory.items.all_items

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
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.ui.theme.BluePrimary
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextPrimary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.background_light_purple
import com.cuso.mobile.ui.theme.darkGreenBg
import com.cuso.mobile.ui.theme.greenBg
import com.cuso.mobile.ui.theme.lightGray
import com.cuso.mobile.ui.theme.light_grey
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.redBg
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.ui.theme.yellowBg
import com.cuso.mobile.ui.theme.yellowText
import com.cuso.mobile.view.composable.AppErrorState
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.DeleteModel
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.ThreeDotLoading
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.InventoryViewModel
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
    // Observe state from ViewModel
    val rawItems by inventoryViewModel.inventoryItems.collectAsStateWithLifecycle()
    val isLoading by inventoryViewModel.isLoadingInventoryItems.collectAsStateWithLifecycle()
    val isLoadingMore by inventoryViewModel.isLoadingMoreInventoryItems.collectAsStateWithLifecycle()
    val canLoadMore by inventoryViewModel.canLoadMoreInventoryItems.collectAsStateWithLifecycle()
    val errorMessage by inventoryViewModel.inventoryError.collectAsStateWithLifecycle()
    val viewOneItem by inventoryViewModel.viewOneItem.collectAsStateWithLifecycle()

    // Dialog & Notification States
    var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }
    var successToastMessage by remember { mutableStateOf<String?>(null) }
    var errorToastMessage by remember { mutableStateOf<String?>(null) }

    val items: List<InventoryItem> = rawItems
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Single unified initial fetch & debounced search (prevents double initialization)
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

    // Scroll listener using snapshotFlow: only fires when the user crosses the bottom threshold
    LaunchedEffect(listState, canLoadMore, searchQuery) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            // Trigger when 2 items away from bottom
            totalItemsNumber > 0 && lastVisibleItemIndex >= (totalItemsNumber - 2)
        }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                if (isNearBottom &&
                    canLoadMore &&
                    !inventoryViewModel.isLoadingMoreInventoryItems.value &&
                    !inventoryViewModel.isLoadingInventoryItems.value &&
                    searchQuery.isBlank()
                ) {
                    inventoryViewModel.loadMoreInventoryItems()
                }
            }
    }

    // Prefill form for editing
    LaunchedEffect(viewOneItem) {
        viewOneItem?.let { item ->
            inventoryViewModel.populateFormForEdit(item)
            onEditItem()
            inventoryViewModel.clearViewOneItem()
        }
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { item ->
        val itemName = item.name.orEmpty().ifBlank { "this item" }
        DeleteModel(
            title = "Delete Product",
            message = "Are you sure you want to delete \"$itemName\"? It can be restored within 7 days, after which it is permanently removed.",
            onDismiss = { itemToDelete = null },
            onDelete = {
                val itemId = item._id
                if (itemId.isNotBlank()) {
                    inventoryViewModel.deleteInventoryItem(
                        itemId = itemId,
                        onSuccess = {
                            successToastMessage = "Item deleted. It can be restored within 7 days, after which it is permanently removed."
                            itemToDelete = null
                        }
                    )
                } else {
                    itemToDelete = null
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleBar("All Items", onClose = onClose)
            }

            Column(Modifier.fillMaxWidth()) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Items...",
                    accentColor = BluePrimary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { }
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

                items.isEmpty() -> {
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
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No Items Found",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Start by adding your first inventory item",
                            fontSize = 13.sp,
                            color = mutedText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = onAddItem,
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Add Item",
                                color = whiteBg,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                                items = items,
                                key = { index, item ->
                                    val id = item._id
                                    if (id.isNotBlank()) id else "item_$index"
                                }
                            ) { _, item ->
                                val (badgeFg, badgeBg) = inventoryStatusColors(item.stockStatus)
                                val isTracking = item.trackInventory
                                val stockCount = item.currentStock
                                val stockText = if (!isTracking) "—" else stockCount.toInt().toString()

                                val itemType = item.type.orEmpty().replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase() else it.toString()
                                }.ifBlank { "N/A" }
                                val price = item.sellingPrice
                                val skuText = item.sku.ifBlank { "—" }
                                val nameText = item.name.orEmpty().ifBlank { "Unnamed Item" }
                                val itemId = item._id

                                DataCard(
                                    item = item,
                                    modifier = Modifier.animateItem(),
                                    smalltitle = "$skuText • SKU",
                                    subtitle = nameText,
                                    topBadgeText = item.stockStatus.orEmpty(),
                                    topBadgeTextColor = badgeFg,
                                    topBadgeBgColor = badgeBg,
                                    topBadgeInline = true,
                                    footerAsRows = true,
                                    footerFields = listOf(
                                        DataCardField(label = "Type", text = itemType),
                                        DataCardField(label = "Stock", text = stockText),
                                        DataCardField(
                                            label = "Selling Price",
                                            text = "₹${"%.2f".format(price)}"
                                        )
                                    ),
                                    actions = listOf(
                                        MenuAction(
                                            label = "View",
                                            icon = Icons.Default.Visibility,
                                            onClick = { onViewItem(item) }
                                        ),
                                        MenuAction(
                                            label = "Edit",
                                            icon = Icons.Default.Edit,
                                            onClick = {
                                                if (itemId.isNotBlank()) {
                                                    inventoryViewModel.onViewOneClicked(itemId)
                                                }
                                            }
                                        ),
                                        MenuAction(
                                            label = "Delete",
                                            icon = Icons.Default.Delete,
                                            onClick = {
                                                itemToDelete = item
                                            }
                                        )
                                    )
                                )
                            }

                            // Render ThreeDotLoading ONLY when next page is actively loading
                            if (isLoadingMore) {
                                item(key = "pagination_threedot_loader") {
                                    ThreeDotLoading(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp)
                                    )
                                }
                            }

                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }

        // ── Dynamic Island Notifications ──
        DynamicIslandSuccess(
            message = successToastMessage,
            onDismiss = { successToastMessage = null }
        )

        DynamicIslandError(
            message = errorToastMessage ?: errorMessage,
            onDismiss = {
                errorToastMessage = null
                inventoryViewModel.clearInventoryError()
            }
        )
    }
}