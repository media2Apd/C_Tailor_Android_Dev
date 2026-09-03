@file:Suppress("unused")

package com.cuso.mobile.view.home.inventory.items.all_items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Warning
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
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.light_grey
import com.cuso.mobile.ui.theme.title_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AppErrorState
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay

private val InventoryBg = Color(0xFFF5F5F5)

/**
 * Returns badge foreground and background color based on stock status.
 */
private fun inventoryStatusColors(status: String?): Pair<Color, Color> {
    val safeStatus = status.orEmpty()
    return when {
        safeStatus.contains("In Stock", ignoreCase = true) && !safeStatus.contains("inactive", ignoreCase = true) ->
            Pair(Color(0xFF16A34A), Color(0xFFDCFCE7))

        safeStatus.contains("Out of Stock", ignoreCase = true) ->
            Pair(Color(0xFFDC2626), Color(0xFFFEE2E2))

        safeStatus.contains("draft", ignoreCase = true) ->
            Pair(Color(0xFFD97706), Color(0xFFFEF3C7))

        else ->
            Pair(Color(0xFF6B7280), light_grey)
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

    //  Guaranteed non-null list fallback
    val items: List<InventoryItem> = rawItems.orEmpty()

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Initial fetch
    LaunchedEffect(Unit) {
        inventoryViewModel.fetchInventoryItems()
    }

    // Debounced search that resets pagination and calls backend API
    LaunchedEffect(searchQuery) {
        delay(400)
        inventoryViewModel.fetchInventoryItems(search = searchQuery.trim().ifBlank { null })
    }

    // Detect when the user scrolls near the end (2 items buffer)
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            totalItemsNumber > 0 && lastVisibleItemIndex >= totalItemsNumber - 2
        }
    }

    // Trigger load more when reaching the end of list
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && canLoadMore && !isLoadingMore && !isLoading) {
            inventoryViewModel.loadMoreInventoryItems()
        }
    }

    // Handle view-one prefill for editing
    LaunchedEffect(viewOneItem) {
        viewOneItem?.let { item ->
            inventoryViewModel.populateFormForEdit(item)
            onEditItem()
            inventoryViewModel.clearViewOneItem()
        }
    }

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
            // ── Search & Filter ──
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
            // Loading initial page skeleton
            isLoading && items.isEmpty() -> {
                ListSkeleton()
            }

            // Error state
            errorMessage != null && items.isEmpty() -> {
                AppErrorState(
                    title = "Failed to load inventory screen",
                    message = "Something went wrong. Please check your connection and try again.",
                    onRetry = { inventoryViewModel.fetchInventoryItems(search = searchQuery.trim().ifBlank { null }) }
                )
            }

            // Empty state
            items.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(InventoryBg)
                        .padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFE7E5FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFF9B96F5),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No Items Found",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Start by adding your first inventory item",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF),
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

            // Paginated items list
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
                                val id = item._id.orEmpty()
                                if (id.isNotBlank()) id else "item_$index"
                            }
                        ) { _, item ->
                            // Safe handling of nullable model properties
                            val (badgeFg, badgeBg) = inventoryStatusColors(item.stockStatus)
                            val isTracking = item.trackInventory ?: false
                            val stockCount = item.currentStock ?: 0.0
                            val stockText = if (!isTracking) "—" else stockCount.toInt().toString()

                            val itemType = item.type.orEmpty().replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            }.ifBlank { "N/A" }
                            val price = item.sellingPrice ?: 0.0
                            val skuText = item.sku?.ifBlank { "—" } ?: "—"
                            val nameText = item.name.orEmpty().ifBlank { "Unnamed Item" }
                            val itemId = item._id.orEmpty()

                            DataCard(
                                item = item,
                                modifier = Modifier.animateItem(),
                                title = "$skuText • SKU",
                                subtitle = nameText,
                                topBadgeText = item.stockStatus.orEmpty(),
                                topBadgeTextColor = badgeFg,
                                topBadgeBgColor = badgeBg,
                                topBadgeInline = true,
                                footerAsRows = true,
                                footerFields = listOf(
                                    DataCardField(
                                        label = "Type",
                                        text = itemType
                                    ),
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
                                        onClick = { onViewItem(item) }
                                    )
                                )
                            )
                        }

                        // Animated pagination loading spinner at the bottom
                        item {
                            Column(Modifier.fillMaxWidth()) {
                                AnimatedVisibility(
                                    visible = isLoadingMore,
                                    enter = fadeIn() + slideInVertically { it / 2 },
                                    exit = fadeOut() + slideOutVertically { it / 2 }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = BluePrimary,
                                            strokeWidth = 2.5.dp
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}