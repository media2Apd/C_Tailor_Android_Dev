package com.cuso.tailor.view.home.inventory.auto_reorder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.AutoReorderRuleItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay

@Composable
fun AutoReorderScreen(
    onBack: () -> Unit = {},
    onCreateNewItem: () -> Unit = {},
    onEditItem: (AutoReorderRuleItemDto) -> Unit = {},
    onFilterClick: () -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current

    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<AutoReorderRuleItemDto?>(null) }

    // Fetch initial dataset & handle search debounce
    var isInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(searchQuery) {
        if (!isInitialized) {
            isInitialized = true
            viewModel.fetchAutoReorderRules()
        } else {
            delay(400)
            viewModel.fetchAutoReorderRules(search = searchQuery.trim().ifBlank { null })
        }
    }

    val rules by viewModel.autoReorderRules.collectAsState()
    val isLoading by viewModel.isLoadingAutoReorder.collectAsState()
    val errorMessage by viewModel.autoReorderError.collectAsState()

    // Confirmation dialog for deleting rule
    itemToDelete?.let { rule ->
        DeleteModel(
            title = "Delete Auto Re-order Rule",
            message = "Are you sure you want to delete \"${rule.ruleName}\"? This action cannot be undone.",
            onDismiss = { itemToDelete = null },
            onDelete = {
                val targetId = rule.id
                itemToDelete = null
                viewModel.deleteAutoReorderRule(
                    id = targetId,
                    onSuccess = { msg -> DynamicIslandManager.showSuccess(msg) },
                    onError = { err -> DynamicIslandManager.showError(err) }
                )
            }
        )
    }

    FabScaffold(
        fab = FabConfig(
            label = "Create New Item",
            icon = Icons.Default.Add,
            onClick = onCreateNewItem
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Screen Header Bar
            TitleBar(
                title = "Auto Re-Orders",
                onClose = onBack
            )

            Spacer(Modifier.height(tokens.extraPadding * 0.6f))

            // Search Bar with Filter Trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search SO number, customer...",
                    onFilterClick = onFilterClick,
                    isSearchBarAlone = false
                )
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.6f))

            // UI State Content Handler
            when {
                isLoading && rules.isEmpty() -> {
                    ListSkeleton()
                }

                errorMessage != null && rules.isEmpty() -> {
                    AppErrorState(
                        title = "Failed to load Auto Re-orders",
                        message = errorMessage ?: "Something went wrong. Please check your connection.",
                        onRetry = { viewModel.fetchAutoReorderRules(search = searchQuery.trim().ifBlank { null }) }
                    )
                }

                rules.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No re-order rules found.",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = headerGrey
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(
                            items = rules,
                            key = { it.id }
                        ) { rule ->
                            val unitText = rule.itemId?.unit.orEmpty().ifBlank { "m" }
                            val triggerOperator = rule.triggerCondition?.operator ?: "<="
                            val triggerThreshold = rule.triggerCondition?.thresholdQty?.toInt() ?: 0
                            val triggerText = "Stock $triggerOperator $triggerThreshold $unitText"
                            val reorderQuantityText = "${rule.reorderQty?.toInt() ?: 0} $unitText"
                            val warehouseName = rule.warehouseId?.name ?: "Primary Hub"

                            val (badgeBg, badgeText) = if (rule.isActive) {
                                greenBg to greentext
                            } else {
                                redBg to redText
                            }

                            DataCard(
                                item = rule,
                                title = rule.ruleName,
                                subtitle = rule.itemId?.name ?: "Item Name",
                                titleColor = title_color,
                                titleFontWeight = FontWeight.Medium,
                                topBadgeText = if (rule.isActive) "Active" else "Inactive",
                                topBadgeInline = true,
                                topBadgeShowDot = true,
                                topBadgeBgColor = badgeBg,
                                topBadgeTextColor = badgeText,
                                topBadgeDotColor = badgeText,
                                showHeaderDivider = false,
                                actions = listOf(
                                    MenuAction(
                                        label = "Edit Item",
                                        icon = Icons.Default.Edit,
                                        onClick = { onEditItem(rule) }
                                    ),
                                    MenuAction(
                                        label = "Delete Item",
                                        icon = Icons.Outlined.DeleteOutline,
                                        onClick = { itemToDelete = rule }
                                    )
                                ),
                                content = {
                                    // Three-Column Metric Container matching the UI design
                                    Surface(
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                                        color = badgeGrey,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    horizontal = tokens.screenPadding * 0.8f,
                                                    vertical = tokens.extraPadding * 0.7f
                                                ),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Column 1: Warehouse
                                            Column(
                                                modifier = Modifier.weight(1.2f),
                                                horizontalAlignment = Alignment.Start,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "Warehouse",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Normal,
                                                    color = headerGrey
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = warehouseName,
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Column 2: Trigger Condition
                                            Column(
                                                modifier = Modifier.weight(1.2f),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "Trigger Condition",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Normal,
                                                    color = headerGrey
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = triggerText,
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }

                                            // Column 3: Reorder Qty
                                            Column(
                                                modifier = Modifier.weight(0.8f),
                                                horizontalAlignment = Alignment.End,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "Reorder Qty",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Normal,
                                                    color = headerGrey
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    text = reorderQuantityText,
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}