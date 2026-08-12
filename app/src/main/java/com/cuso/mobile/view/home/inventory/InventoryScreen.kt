package com.cuso.mobile.view.home.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.DataCard
import com.cuso.mobile.view.composable.DataCardField
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.MenuAction
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.viewmodel.InventoryViewModel

private val InventoryBg = Color(0xFFF5F5F5)

// stockStatus badge colors — "Not Tracked" red/pink, "In Stock" green, "Low Stock" orange, fallback gray
private fun inventoryStatusColors(status: String?): Pair<Color, Color> {
    val safeStatus = status.orEmpty()   //   CHANGED: null-safe fallback
    return when {
        safeStatus.contains("In Stock", ignoreCase = true) && !safeStatus.contains("inactive", ignoreCase = true) ->
            Pair(Color(0xFF16A34A), Color(0xFFDCFCE7))

        safeStatus.contains("Out of Stock", ignoreCase = true) ->
            Pair(Color(0xFFDC2626), Color(0xFFFEE2E2))

        safeStatus.contains("draft", ignoreCase = true) ->
            Pair(Color(0xFFD97706), Color(0xFFFEF3C7))

        else ->
            Pair(Color(0xFF6B7280), Color(0xFFF3F4F6))
    }
}

@Composable
fun InventoryScreen(
    onClose: () -> Unit = {},
    onAddItem: () -> Unit = {},
    onViewItem: (InventoryItem) -> Unit = {},
    onEditItem: () -> Unit = {},   //   CHANGED — no item param needed; form is populated in the ViewModel before navigating
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    onBreadCrumbClick: () -> Unit ={}

) {
    val items by inventoryViewModel.inventoryItems.collectAsStateWithLifecycle()
    val isLoading by inventoryViewModel.isLoadingInventoryItems.collectAsStateWithLifecycle()
    val errorMessage by inventoryViewModel.inventoryError.collectAsStateWithLifecycle()
    val viewOneItem by inventoryViewModel.viewOneItem.collectAsStateWithLifecycle()   //   drives the Edit prefill flow

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        inventoryViewModel.fetchInventoryItems()
    }

    //   FIXED — once GetInventoryViewOne succeeds, push the response into the
    // create/edit form state, then navigate to CreateItemScreen (which reads
    // that same form state and renders in Edit mode).
    LaunchedEffect(viewOneItem) {
        viewOneItem?.let { item ->
            inventoryViewModel.populateFormForEdit(item)
            onEditItem()
            inventoryViewModel.clearViewOneItem()
        }
    }

    val filtered = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.sku.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleBar("All Items", onClose = onClose)

        }

        Column(
            Modifier
                .fillMaxWidth()
        ) {
            // ── Breadcrumb ──
            ScreenBreadcrumb(
                segments = listOf("Inventory", "All Items"),
                onClick = {onBreadCrumbClick()}
            )

            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                placeholder = "Search Customers...",
                accentColor = BluePrimary,
                borderColor = BorderGray,
                textSecondaryColor = TextSecondary,
                onFilterClick = { /* TODO: open filter drawer */ }
            )
        }
        HorizontalDivider(color = Color(0xFFF0F0F0))


        when {
            isLoading -> {
                ListSkeleton()
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(InventoryBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(errorMessage ?: "Failed to load items", color = Color.Red, fontSize = 13.sp)
                }
            }

            filtered.isEmpty() -> {
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
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFF9B96F5), modifier = Modifier.size(30.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("No Items Found", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF111827))
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
                        Icon(Icons.Default.Add, contentDescription = null, tint = whiteBg, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Item", color = whiteBg, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                        modifier = Modifier.fillMaxSize().background(Color.Transparent)
                    ) {
                        items(filtered, key = { it._id }) { item ->
                            val (badgeFg, badgeBg) = inventoryStatusColors(item.stockStatus)
                            val stockText = if (!item.trackInventory) "—" else item.currentStock.toInt().toString()

                            DataCard(
                                item = item,
                                title = "${item.sku} • SKU",
                                subtitle = item.name,
                                topBadgeText = item.stockStatus,
                                topBadgeTextColor = badgeFg,
                                topBadgeBgColor = badgeBg,
                                topBadgeInline = true,
                                footerAsRows = true,
                                footerFields = listOf(
                                    DataCardField(label = "Type", text = item.type.replaceFirstChar { it.uppercase() }),
                                    DataCardField(label = "Stock", text = stockText),
                                    DataCardField(label = "Selling Price", text = "₹${"%.2f".format(item.sellingPrice)}")
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
                                        onClick = { inventoryViewModel.onViewOneClicked(item._id) }   //   triggers GetInventoryViewOne → prefill → navigate
                                    ),
                                    MenuAction(
                                        label = "Delete",
                                        icon = Icons.Default.Delete,
                                        onClick = { onViewItem(item) }
                                    ),

                                    )
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}