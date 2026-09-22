@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.safety_stock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.SafetyStockItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

private data class HealthTheme(
    val textColor: Color,
    val bgColor: Color,
    val progressColor: Color
)

private fun getHealthTheme(status: String): HealthTheme {
    return when (status.trim().lowercase()) {
        "safe", "healthy" -> HealthTheme(
            textColor = greentext,
            bgColor = greenBg,
            progressColor = Primary
        )
        "low", "warning" -> HealthTheme(
            textColor = yellowText,
            bgColor = yellowBg,
            progressColor = yellowText
        )
        "critical", "out of stock" -> HealthTheme(
            textColor = redText,
            bgColor = redBg,
            progressColor = redText
        )
        else -> HealthTheme(
            textColor = Primary,
            bgColor = light_blue,
            progressColor = Primary
        )
    }
}

@Composable
fun SafetyStockScreen(
    onClose: () -> Unit = {},
    onItemClick: (SafetyStockItemDto) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onFilterClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val safetyStockItems by viewModel.safetyStockList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingSafetyStock.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreSafetyStock.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreSafetyStock.collectAsStateWithLifecycle()
    val errorMessage by viewModel.safetyStockError.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    var isInitialized by remember { mutableStateOf(false) }

    // Initial load and debounced search query trigger
    LaunchedEffect(searchQuery) {
        if (!isInitialized) {
            isInitialized = true
            viewModel.fetchSafetyStock()
        } else {
            delay(400)
            viewModel.fetchSafetyStock(search = searchQuery.trim())
        }
    }

    // Scroll pagination threshold observer
    LaunchedEffect(listState, canLoadMore, searchQuery) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 2)
        }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                if (isNearBottom && canLoadMore && !viewModel.isLoadingMoreSafetyStock.value && !viewModel.isLoadingSafetyStock.value) {
                    viewModel.loadMoreSafetyStock(search = searchQuery.trim())
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = whiteBg
                ) {
                    TitleBar(
                        title = "Safety Stock",
                        onClose = onClose
                    )
                }
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Breadcrumb indicators
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = tokens.screenPadding, vertical = 6.dp)
                ) {
                    Text(text = "Inventory", fontSize = tokens.caption, color = mutedText)
                    Text(text = "  >  ", fontSize = tokens.caption, color = mutedText)
                    Text(text = "Alerts & Reorder", fontSize = tokens.caption, color = mutedText)
                    Text(text = "  >  ", fontSize = tokens.caption, color = mutedText)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(yellowBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Safety Stock",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = yellowText
                        )
                    }
                }

                // Search field container
                Box(
                    modifier = Modifier.padding(
                        horizontal = tokens.screenPadding,
                        vertical = tokens.extraPadding * 0.4f
                    )
                ) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = "Search Stock Items...",
                        onFilterClick = onFilterClick,
                        height = tokens.fieldHeight
                    )
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                when {
                    isLoading && safetyStockItems.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessage != null && safetyStockItems.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load safety stock",
                            message = errorMessage ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchSafetyStock(search = searchQuery.trim()) }
                        )
                    }

                    safetyStockItems.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "No safety stock records found." else "No matching items.",
                                color = mutedText,
                                fontSize = tokens.bodyMedium
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                            contentPadding = PaddingValues(
                                start = tokens.screenPadding,
                                end = tokens.screenPadding,
                                top = tokens.extraPadding,
                                bottom = tokens.screenPadding * 2
                            )
                        ) {
                            itemsIndexed(
                                items = safetyStockItems,
                                key = { index, item -> item.itemId.ifBlank { "item_$index" } }
                            ) { _, item ->
                                SafetyStockCard(
                                    item = item,
                                    onClick = { onItemClick(item) },
                                    onEditClick = { onEditClick(item.itemId) }
                                )
                            }

                            if (isLoadingMore) {
                                item(key = "safety_stock_loader") {
                                    ThreeDotLoading(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMessage?.takeIf { safetyStockItems.isNotEmpty() },
            onDismiss = { viewModel.clearSafetyStockAlerts() }
        )
    }
}

@Composable
fun SafetyStockCard(
    item: SafetyStockItemDto,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val theme = getHealthTheme(item.health)
    val displayUnit = item.unit?.ifBlank { "Meters" } ?: "Meters"
    var menuExpanded by remember { mutableStateOf(false) }

    val progressRatio = (item.healthPercent.toFloat() / 100f).coerceIn(0.08f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            // Header section: Product details and actions menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name.ifBlank { "Product" },
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.bgColor)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.health.ifBlank { "Safe" },
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Bold,
                        color = theme.textColor
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = iconMuted,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        offset = DpOffset(x = (-10).dp, y = 0.dp),
                        modifier = Modifier.background(
                            color = whiteBg,
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                        )
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Edit",
                                    fontSize = tokens.bodyMedium,
                                    color = TextPrimary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEditClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = buildString {
                    append("SKU: ${item.sku.ifBlank { "—" }}")
                    if (!item.variantLabel.isNullOrBlank()) {
                        append(" · Variant: ${item.variantLabel}")
                    }
                },
                fontSize = tokens.caption,
                color = mutedText
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding))

            // Inventory location and stock levels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "Warehouse",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.warehouseName ?: "Main Warehouse",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Stock Health",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.available.toInt()} $displayUnit",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Health progress indicator bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(dividerColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressRatio)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.progressColor)
                )
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding))
            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(tokens.extraPadding))

            // Threshold details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SAFETY LEVEL",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = mutedText,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.safetyStock.toInt()} $displayUnit",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "REORDER LEVEL",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = mutedText,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.reorderLevel.toInt()} $displayUnit",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}