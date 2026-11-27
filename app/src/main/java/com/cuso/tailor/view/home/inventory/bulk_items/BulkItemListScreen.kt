@file:Suppress("UNUSED_VALUE", "SpellCheckingInspection","unusedVariable", "unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.bulk_items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

    val bulkPagingState by viewModel.bulkItemsPagingState.collectAsStateWithLifecycle()
    val errorMessage by viewModel.bulkError.collectAsStateWithLifecycle()

    // Filter Drawer State & Sections
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Assembly Type",
                    type = FilterSectionType.CHECKBOX_LIST,
                    options = listOf(
                        FilterOption("on_order", "On Order"),
                        FilterOption("pre_assembled", "Pre-assembled")
                    )
                ),
                FilterSection(
                    title = "Status",
                    type = FilterSectionType.CHECKBOX_LIST,
                    options = listOf(
                        FilterOption("active", "Active"),
                        FilterOption("inactive", "Inactive")
                    )
                )
            )
        )
    }

    val activeFilterCount by remember(filterSections) {
        derivedStateOf { filterSections.sumOf { sec -> sec.options.count { it.isSelected } } }
    }

    LaunchedEffect(Unit) { viewModel.fetchBulkItems() }

    // Client-side Filter layered over PagingUiState
    val filteredList by remember(bulkPagingState.items, searchQuery, filterSections) {
        derivedStateOf {
            val selectedAssembly = filterSections.find { it.title == "Assembly Type" }
                ?.options?.filter { it.isSelected }?.map { it.label.lowercase() } ?: emptyList()
            val selectedStatuses = filterSections.find { it.title == "Status" }
                ?.options?.filter { it.isSelected }?.map { it.label.lowercase() } ?: emptyList()

            bulkPagingState.items.filter { item ->
                val matchesSearch = searchQuery.isBlank() ||
                        item.name.contains(searchQuery, ignoreCase = true) ||
                        item.sku.contains(searchQuery, ignoreCase = true)

                val matchesAssembly = selectedAssembly.isEmpty() ||
                        selectedAssembly.any { item.assemblyType.contains(it, ignoreCase = true) }

                val matchesStatus = selectedStatuses.isEmpty() ||
                        selectedStatuses.any { item.status.contains(it, ignoreCase = true) }

                matchesSearch && matchesAssembly && matchesStatus
            }
        }
    }

    val displayBulkPagingState = remember(bulkPagingState, filteredList) {
        bulkPagingState.copy(items = filteredList)
    }

    Box(modifier = Modifier.fillMaxSize().background(Primary_background)) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TitleBar(title = "All Bulk", onClose = onClose)
                        Box(modifier = Modifier.fillMaxWidth().padding(bottom = tokens.extraPadding * 0.4f)) {
                            SearchFilterBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                placeholder = "Search Bulk Items...",
                                filterCount = activeFilterCount,
                                onFilterClick = { filterDrawerState.open() }
                            )
                        }
                        HorizontalDivider(color = dividerColor, thickness = 1.dp)
                    }
                }
            }
        ) { paddingValues ->
            FabScaffold(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                fab = FabConfig(
                    label = "Add Bulk Item",
                    icon = Icons.Default.Add,
                    onClick = onAddBulkClick,
                    endPadding = tokens.screenPadding,
                    bottomPadding = tokens.buttonHeight * 1.5f
                ),
                fabVisible = !displayBulkPagingState.isInitialLoading
            ) {
                when {
                    errorMessage != null && displayBulkPagingState.items.isEmpty() && !displayBulkPagingState.isInitialLoading -> {
                        AppErrorState(
                            title = "Failed to load bulk items",
                            message = errorMessage ?: "Something went wrong.",
                            onRetry = { viewModel.fetchBulkItems() }
                        )
                    }
                    !displayBulkPagingState.isInitialLoading && displayBulkPagingState.items.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (searchQuery.isNotBlank() || activeFilterCount > 0) "No matching bulk items found" else "No Bulk Items Yet",
                                fontSize = tokens.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        PaginatedDataList(
                            state = displayBulkPagingState,
                            onLoadMore = { viewModel.loadMoreBulkItems() },
                            modifier = Modifier.fillMaxSize()
                        ) { item ->
                            Column {
                                BulkListItemCard(
                                    item = item,
                                    onClick = { if (item.id.isNotBlank()) onItemClick(item.id) },
                                    onEditClick = { if (item.id.isNotBlank()) onEditClick(item.id) },
                                    onDeleteClick = { viewModel.deleteBulkItem(item.id) {} }
                                )
                                Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                            }
                        }
                    }
                }
            }
        }

        // Filter Bulk Items Drawer
        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Bulk Items",
            sections = filterSections,
            onApply = { updated -> filterSections = updated },
            onClearAll = {
                filterSections = filterSections.map { sec ->
                    sec.copy(options = sec.options.map { it.copy(isSelected = false) })
                }
            }
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
                    AppCheckbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it }
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