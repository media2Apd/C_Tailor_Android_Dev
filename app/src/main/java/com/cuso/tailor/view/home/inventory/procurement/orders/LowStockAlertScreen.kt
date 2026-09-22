@file:Suppress("unused", "SpellCheckingInspection", "UNUSED_PARAMETER")

package com.cuso.tailor.view.home.inventory.procurement.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.LowStockItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

private data class SeverityTheme(
    val textColor: Color,
    val bgColor: Color,
    val progressColor: Color
)

private fun getSeverityTheme(severity: String): SeverityTheme {
    return when (severity.trim().lowercase()) {
        "urgent", "warning" -> SeverityTheme(
            textColor = yellowText,
            bgColor = yellowBg,
            progressColor = yellowText
        )
        "normal", "low", "good", "healthy" -> SeverityTheme(
            textColor = greentext,
            bgColor = greenBg,
            progressColor = greentext
        )
        // Default to Critical
        else -> SeverityTheme(
            textColor = redText,
            bgColor = redBg,
            progressColor = redText
        )
    }
}

@Composable
fun LowStockAlertCard(
    item: LowStockItemDto,
    onReorderClick: () -> Unit
) {
    val progressRatio = (item.stockUtilizationPercent.toFloat() / 100f).coerceIn(0f, 1f)
    val severityLabel = item.severity.ifBlank { "Critical" }
    val theme = getSeverityTheme(severityLabel)

    DataCard(
        item = item,
        showDivider = true,
        topBadgeShowDot = false,
        title = item.name,
        subtitle = "SKU: ${item.sku} · Variant: ${item.variantLabel ?: "-"}",
        topBadgeText = severityLabel.replaceFirstChar { it.uppercase() },
        topBadgeTextColor = theme.textColor,
        topBadgeBgColor = theme.bgColor,
        topBadgeDotColor = theme.bgColor,
        topBadgeInline = true,
        footerAsColumns = true,
        footerFields = listOf(
            DataCardField(
                label = "Warehouse",
                text = item.warehouseName ?: "Central Warehouse",
                valueFontWeight = FontWeight.SemiBold
            ),
            DataCardField(
                label = "Available",
                text = "${item.available.toInt()} ${item.unit ?: "pcs"}",
                textColor = theme.textColor,
                valueFontWeight = FontWeight.SemiBold
            )
        ),
        content = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 20.dp)
                ) {
                    DataCardProgressBar(
                        progress = progressRatio,
                        progressColor = theme.progressColor,
                        trackColor = Color(0xFFEDEDF2)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Reorder Level: ",
                            fontSize = 13.sp,
                            color = Color(0xFF9B9BA5)
                        )
                        Text(
                            text = "${item.reorderLevel.toInt()} ${item.unit ?: "pcs"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E2238)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onReorderClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Primary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Reorder →",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }
            }
        }
    )
}

@Composable
fun LowStockAlertsScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onReorderClick: (LowStockItemDto) -> Unit = {},
    onCreateNewItem: () -> Unit = {},
    onBreadcrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingLowStock.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreLowStock.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreLowStock.collectAsStateWithLifecycle()
    val errorMessage by viewModel.lowStockError.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchLowStockAlerts()
    }

    LaunchedEffect(listState, canLoadMore, searchQuery) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 2)
        }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                if (isNearBottom &&
                    canLoadMore &&
                    !viewModel.isLoadingMoreLowStock.value &&
                    !viewModel.isLoadingLowStock.value &&
                    searchQuery.isBlank()
                ) {
                    viewModel.loadMoreLowStockAlerts()
                }
            }
    }

    val filteredItems = remember(lowStockItems, searchQuery) {
        if (searchQuery.isBlank()) {
            lowStockItems
        } else {
            lowStockItems.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.sku.contains(searchQuery, ignoreCase = true) ||
                        (it.warehouseName?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = whiteBg
            ) {
                TitleBar("Low Stock Alerts", onClose = onClose)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { padding ->
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            fab = FabConfig(
                label = "Create New Item",
                icon = Icons.Default.Add,
                onClick = onCreateNewItem,
                endPadding = 16.dp,
                bottomPadding = 16.dp,
                draggable = true
            )
        ) {
            Column(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Stock Items...",
                    onFilterClick = { }
                )
                HorizontalDivider(color = dividerColor)

                when {
                    isLoading && lowStockItems.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessage != null && lowStockItems.isEmpty() -> {
                        AppErrorState(
                            title = "Unable to load stock alerts",
                            message = errorMessage ?: "Failed to connect to the server.",
                            onRetry = { viewModel.fetchLowStockAlerts() }
                        )
                    }

                    filteredItems.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "No low stock alerts found." else "No matching items.",
                                color = mutedText,
                                fontSize = tokens.bodyMedium
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
                        ) {
                            items(
                                items = filteredItems,
                                key = { it.itemId.ifBlank { it.hashCode().toString() } }
                            ) { item ->
                                LowStockAlertCard(
                                    item = item,
                                    onReorderClick = { onReorderClick(item) }
                                )
                            }

                            if (isLoadingMore) {
                                item(key = "pagination_threedot_loader") {
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
    }
}