package com.cuso.mobile.view.home.inventory.procurement.location_management

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.StockLocationItemDto
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.InventoryViewModel

@Composable
fun AllStockLocationScreen(
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onItemClick: (StockLocationItemDto) -> Unit = {},
    onLocationClick: (StockLocationItemDto) -> Unit = {},
    onOptionsClick: (StockLocationItemDto) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val stockList by inventoryViewModel.stockLocationItems.collectAsState()
    val isLoading by inventoryViewModel.isLoadingStockLocations.collectAsState()
    val isLoadingMore by inventoryViewModel.isLoadingMoreStockLocations.collectAsState()
    val canLoadMore by inventoryViewModel.canLoadMoreStockLocations.collectAsState()
    val errorMessage by inventoryViewModel.stockLocationError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectAll by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        inventoryViewModel.fetchStockLocationItems()
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= stockList.size - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && canLoadMore && !isLoading && !isLoadingMore) {
            inventoryViewModel.loadMoreStockLocationItems()
        }
    }

    val totalStockSum = remember(stockList) {
        stockList.sumOf { it.totalStock.toLong() }
    }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar("All Stock Location", onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        inventoryViewModel.onStockLocationSearchQueryChanged(it)
                    },
                    placeholder = "Search Customers...",
                    showFilterIcon = true,
                    onFilterClick = { },
                    height = tokens.fieldHeight * 1.1f
                )

                HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 1.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppCheckbox(
                            checked = selectAll,
                            onCheckedChange = { checked ->
                                selectAll = checked
                                selectedItemIds = if (checked) stockList.map { it.id }.toSet() else emptySet()
                            }
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                        Text(
                            text = "Select All (${stockList.size} items)",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                    Text(
                        text = "Total: $totalStockSum Pcs",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }

                if (isLoading && stockList.isEmpty()) {
                    ListSkeleton()
                } else if (stockList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No stock locations found",
                            fontSize = tokens.bodyMedium,
                            color = mutedText
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = tokens.screenPadding * 1.5f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
                    ) {
                        itemsIndexed(stockList, key = { _, item -> item.id }) { _, item ->
                            val isChecked = selectedItemIds.contains(item.id)
                            StockLocationCardItem(
                                item = item,
                                isChecked = isChecked,
                                tokens = tokens,
                                onCheckedChange = { checked ->
                                    selectedItemIds = if (checked) selectedItemIds + item.id else selectedItemIds - item.id
                                    selectAll = selectedItemIds.size == stockList.size
                                },
                                onClick = {
                                    // Only allow navigation to details if locations exist
                                    if (item.locationCount > 0) {
                                        onItemClick(item)
                                    }
                                },
                                onLocationClick = { onLocationClick(item) },
                                onOptionsClick = { onOptionsClick(item) }
                            )
                        }

                        if (isLoadingMore) {
                            item {
                                ThreeDotLoading()
                            }
                        }
                    }
                }
            }

            DynamicIslandError(
                message = errorMessage,
                onDismiss = { }
            )
        }
    }
}

@Composable
private fun StockLocationCardItem(
    item: StockLocationItemDto,
    isChecked: Boolean,
    tokens: AppDesignTokens,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onLocationClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    val hasVariants = !item.variantLabel.isNullOrBlank() || !item.parentGroupId.isNullOrBlank()
    val categoryDisplay = item.category?.takeIf { it.isNotBlank() } ?: "APPAREL"
    val brandDisplay = item.brand?.takeIf { it.isNotBlank() } ?: "No Brand"
    val statusDisplay = item.status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    val hasLocations = item.locationCount > 0
    val titleTextColor = if (hasLocations) title_color else Color(0xFF9EAEC1)
    val skuTextColor = if (hasLocations) mutedText else Color(0xFFB0BDCD)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
            // Disable click on disabled/unlocated cards
            .clickable(enabled = hasLocations) { onClick() },
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckbox(checked = isChecked, onCheckedChange = onCheckedChange)
                Spacer(Modifier.width(tokens.extraPadding * 0.8f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                        .background(activity_purple_bg)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 2.dp)
                ) {
                    Text(categoryDisplay, fontSize = tokens.label, fontWeight = FontWeight.Bold, color = activity_purple)
                }

                Spacer(Modifier.width(6.dp))
                Text("•", color = mutedText, fontSize = tokens.label)
                Spacer(Modifier.width(6.dp))
                Text(brandDisplay, fontSize = tokens.caption, color = TextSecondary)

                Spacer(Modifier.weight(1f))

                val isActive = item.status.equals("active", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                        .background(if (isActive) greenBg else light_grey)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 3.dp)
                ) {
                    Text(
                        text = statusDisplay,
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = if (isActive) darkGreenBg else mutedText
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding * 0.4f))

                IconButton(onClick = onOptionsClick, modifier = Modifier.size(tokens.iconSize * 1.3f)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        tint = mutedText,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // Title styling based on location availability
            Text(
                text = item.name,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = titleTextColor
            )

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.sku,
                    fontSize = tokens.caption,
                    color = skuTextColor
                )
                if (hasVariants) {
                    Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                            .background(light_grey)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.variantLabel?.takeIf { it.isNotBlank() } ?: "Has Variants",
                            fontSize = tokens.label,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
            HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Stock", fontSize = tokens.label, color = mutedText)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${item.totalStock.toLong()} Piece",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Stock Location", fontSize = tokens.label, color = mutedText)
                    Spacer(Modifier.height(4.dp))

                    if (hasLocations) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                .border(1.dp, Primary, RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                .clickable { onLocationClick() }
                                .padding(horizontal = 12.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (item.locationCount == 1) "1 location" else "${item.locationCount} locations",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                .background(Primary)
                                .clickable { onLocationClick() }
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Add location",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    }
}