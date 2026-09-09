@file:Suppress("unused")

package com.cuso.mobile.view.home.inventory.items.item_groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.ItemGroupDto
import com.cuso.mobile.ui.theme.BorderGray
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.Primary_background
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.background_light_purple
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.title_border
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
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AllItemGroupScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    onDismiss: () -> Unit = {},
    onAddItemGroup: () -> Unit = {},
    onView: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {}
) {
    // =========================================================================
    // DESIGN TOKENS & STATE COLLECTORS
    // =========================================================================
    val tokens = LocalAppTokens.current
    val uiState by viewModel.uiState.collectAsState()
    val deleteSuccessMessage by viewModel.deleteItemGroupSuccess.collectAsState()

    var displayedErrorMessage by remember { mutableStateOf<String?>(null) }

    // State for Delete Confirmation Dialog
    var itemGroupToDelete by remember { mutableStateOf<ItemGroupDto?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Synchronize ViewModel errors
    LaunchedEffect(uiState.errorMessage) {
        displayedErrorMessage = uiState.errorMessage
    }

    // Refresh list on initial screen load
    LaunchedEffect(Unit) {
        viewModel.refreshItemGroups()
    }

    // =========================================================================
    // ROOT UI WITH ADAPTIVE SCAFFOLD
    // =========================================================================
    FabScaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        fab = FabConfig(
            label = "Create item group",
            icon = Icons.Default.Add,
            onClick = onAddItemGroup
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // ── Main Screen Content ──
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Bar
                Row(modifier = Modifier.fillMaxWidth()) {
                    TitleBar(
                        title = "Item Group",
                        onClose = onDismiss
                    )
                }

                // Search Bar
                SearchFilterBar(
                    query = uiState.searchQuery,
                    onQueryChange = { query ->
                        viewModel.onSearchQueryChanged(query)
                    },
                    placeholder = "Search Item Group...",
                    accentColor = Primary,
                    borderColor = BorderGray,
                    textSecondaryColor = mutedText,
                    onFilterClick = { /* Optional Filter callback */ }
                )

                HorizontalDivider(color = title_border, thickness = 1.dp)

                // List, Skeleton Loading, or Empty View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when {
                        uiState.isLoading && uiState.filteredList.isEmpty() -> {
                            ListSkeleton()
                        }
                        uiState.filteredList.isEmpty() -> {
                            EmptyStateView(tokens = tokens)
                        }
                        else -> {
                            ItemGroupListView(
                                itemGroups = uiState.filteredList,
                                tokens = tokens,
                                onView = onView,
                                onEdit = onEdit,
                                onDelete = { group ->
                                    itemGroupToDelete = group
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // ── Delete Confirmation Dialog (DeleteModel) ──
            if (showDeleteDialog && itemGroupToDelete != null) {
                DeleteModel(
                    title = "Delete Item Group",
                    message = "Are you sure you want to delete \"${itemGroupToDelete?.name}\"?\nThis will remove the item group and its variants.",
                    onDismiss = {
                        showDeleteDialog = false
                        itemGroupToDelete = null
                    },
                    onDelete = {
                        itemGroupToDelete?.let { group ->
                            viewModel.deleteItemGroup(id = group.id)
                        }
                        showDeleteDialog = false
                        itemGroupToDelete = null
                    }
                )
            }

            // ── Dynamic Island Notifications ──
            DynamicIslandSuccess(
                message = deleteSuccessMessage,
                onDismiss = { viewModel.clearDeleteSuccessMessage() }
            )

            DynamicIslandError(
                message = displayedErrorMessage,
                onDismiss = { displayedErrorMessage = null }
            )
        }
    }
}

// =========================================================================
// ITEM GROUP LIST COMPOSABLE
// =========================================================================

@Composable
private fun ItemGroupListView(
    itemGroups: List<ItemGroupDto>,
    tokens: AppDesignTokens,
    onView: (String) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (group: ItemGroupDto) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = tokens.extraPadding)
    ) {
        items(items = itemGroups, key = { it.id }) { group ->
            // Extract formatted attribute summary
            val attributesSummary = group.variantAttributes
                .joinToString(", ") { it.name }
                .ifBlank { "No attributes" }

            // Extract formatted creation date
            val formattedDate = formatIsoDate(group.createdAt)

            DataCard(
                item = group,
                title = group.name,
                topBadgeText = "${group.variantCount} Items",
                topBadgeTextColor = Primary,
                topBadgeBgColor = background_light_purple,
                topBadgeInline = true,
                footerAsRows = false,
                footerFields = listOf(
                    DataCardField(
                        label = "",
                        text = attributesSummary,
                        labelColor = mutedText,
                        textColor = mutedText
                    ),
                    DataCardField(
                        label = "",
                        text = formattedDate,
                        labelColor = mutedText,
                        textColor = mutedText
                    )
                ),
                actions = listOf(
                    MenuAction(
                        label = "View",
                        icon = Icons.Filled.Visibility,
                        onClick = { onView(group.id) }
                    ),
                    MenuAction(
                        label = "Edit",
                        icon = Icons.Filled.Edit,
                        onClick = { onEdit(group.id) }
                    ),
                    MenuAction(
                        label = "Delete",
                        icon = Icons.Filled.Delete,
                        tint = redText,
                        textColor = redText,
                        onClick = { onDelete(group) }
                    )
                ),
                onClick = { onView(group.id) }
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding / 2))
        }
    }
}

// =========================================================================
// EMPTY STATE COMPOSABLE
// =========================================================================

@Composable
private fun EmptyStateView(tokens: AppDesignTokens) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No item groups found",
            fontSize = tokens.bodyMedium,
            color = TextSecondary
        )
    }
}

// =========================================================================
// HELPER FUNCTIONS
// =========================================================================

/**
 * Parses and formats ISO 8601 date strings to a readable format (e.g. "20 Jul 2026").
 */
private fun formatIsoDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(isoDate)
        date?.let { outputFormat.format(it) } ?: isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}