@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.inventory.bulk_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.BulkItemDoc
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun BulkListScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onItemClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onAddBulkClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    val bulkList by viewModel.bulkItems.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreBulk.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreBulk.collectAsStateWithLifecycle()
    val errorMessage by viewModel.bulkError.collectAsStateWithLifecycle()
    val successMessage by viewModel.bulkSuccessMessage.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.fetchBulkItems()
    }

    LaunchedEffect(listState, canLoadMore, searchQuery) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= (totalItems - 2)
        }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                if (isNearBottom && canLoadMore && !viewModel.isLoadingMoreBulk.value && !viewModel.isLoading.value && searchQuery.isBlank()) {
                    viewModel.loadMoreBulkItems()
                }
            }
    }

    val filteredList = remember(bulkList, searchQuery) {
        if (searchQuery.isBlank()) bulkList
        else bulkList.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                // நிரந்தர தீர்வு: Surface -> Column-க்குள் வரிசையாக வைப்பதால் எக்காரணத்தைக் கொண்டும் ஒன்றன் மேல் ஒன்று மறையாது
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TitleBar(
                            title = "All Bulk",
                            onClose = onClose
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = tokens.extraPadding * 0.4f)
                        ) {
                            SearchFilterBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                placeholder = "Search Bulk Items...",
                                onFilterClick = { }
                            )
                        }

                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 1.dp
                        )
                    }
                }
            }
        ) { paddingValues ->
            FabScaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                fab = FabConfig(
                    label = "Add Bulk Item",
                    icon = Icons.Default.Add,
                    onClick = onAddBulkClick,
                    endPadding = tokens.screenPadding,
                    bottomPadding = tokens.buttonHeight * 1.5f
                )
            ) {
                when {
                    isLoading && bulkList.isEmpty() -> {
                        ListSkeleton()
                    }

                    errorMessage != null && bulkList.isEmpty() -> {
                        AppErrorState(
                            title = "Failed to load bulk items",
                            message = errorMessage ?: "Something went wrong. Please check your connection.",
                            onRetry = { viewModel.fetchBulkItems() }
                        )
                    }

                    filteredList.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No matching bulk items found" else "No Bulk Items Yet",
                                fontSize = tokens.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = tokens.extraPadding)
                        ) {
                            items(
                                items = filteredList,
                                key = { it.id.ifBlank { it.hashCode().toString() } }
                            ) { item ->
                                BulkListItemCard(
                                    item = item,
                                    onClick = {
                                        if (item.id.isNotBlank()) {
                                            onItemClick(item.id)
                                        }
                                    },
                                    onEditClick = {
                                        if (item.id.isNotBlank()) {
                                            onEditClick(item.id)
                                        }
                                    },
                                    onDeleteClick = {
                                        viewModel.deleteBulkItem(item.id) {}
                                    }
                                )
                                Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                            }

                            if (isLoadingMore) {
                                item(key = "pagination_threedot_loader") {
                                    ThreeDotLoading(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = tokens.screenPadding)
                                    )
                                }
                            }

                            item {
                                Spacer(Modifier.height(tokens.buttonHeight * 2f))
                            }
                        }
                    }
                }
            }
        }

        DynamicIslandSuccess(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = successMessage,
            onDismiss = { viewModel.clearBulkSuccessMessage() }
        )

        DynamicIslandError(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = tokens.fieldHeight * 1.5f),
            message = errorMessage?.takeIf { bulkList.isNotEmpty() },
            onDismiss = { viewModel.clearBulkError() }
        )
    }
}

private data class BulkStatusTheme(
    val bg: Color,
    val text: Color,
    val dot: Color
)

private fun getBulkStatusTheme(status: String): BulkStatusTheme {
    return when (status.lowercase().trim()) {
        "healthy", "active", "in stock" -> BulkStatusTheme(
            bg = greenBg,
            text = greentext,
            dot = greentext
        )
        "low soon", "warning", "reorder" -> BulkStatusTheme(
            bg = yellowBg,
            text = yellowText,
            dot = yellowText
        )
        "out of stock", "inactive", "critical" -> BulkStatusTheme(
            bg = redBg,
            text = redText,
            dot = redText
        )
        else -> BulkStatusTheme(
            bg = yellowBg,
            text = yellowText,
            dot = yellowText
        )
    }
}

@Composable
fun BulkListItemCard(
    item: BulkItemDoc,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var isChecked by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val statusTheme = remember(item.status) { getBulkStatusTheme(item.status) }

    Surface(
        onClick = onClick,
        color = whiteBg,
        shadowElevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding * 0.9f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Primary,
                            uncheckedColor = dividerColor,
                            checkmarkColor = whiteBg
                        )
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                    Column {
                        Text(
                            text = item.name.ifBlank { "—" },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = tokens.bodyLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "SKU: ${item.sku.ifBlank { "—" }}",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }

                // Action Menu
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(tokens.fieldHeight * 0.6f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Actions",
                            tint = iconMuted,
                            modifier = Modifier.size(tokens.iconSize * 1.1f)
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
                            text = { Text("View", fontSize = tokens.bodyMedium, color = TextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Edit", fontSize = tokens.bodyMedium, color = TextPrimary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = TextLog,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEditClick()
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete", fontSize = tokens.bodyMedium, color = redText) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = redText,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.extraPadding * 0.4f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "STOCK ON HAND",
                        fontSize = tokens.label,
                        color = iconMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "%,.0f".format(item.stockOnHand),
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Column {
                    Text(
                        text = "REORDER POINT",
                        fontSize = tokens.label,
                        color = iconMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "%,.0f".format(item.reorderPoint),
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.extraPadding * 0.4f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Status", fontSize = tokens.bodySmall, color = TextSecondary)
                Box(
                    modifier = Modifier
                        .background(
                            color = statusTheme.bg,
                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                        )
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusTheme.dot, CircleShape)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = item.status.ifBlank { "Active" },
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = statusTheme.text
                        )
                    }
                }
            }
        }
    }
}