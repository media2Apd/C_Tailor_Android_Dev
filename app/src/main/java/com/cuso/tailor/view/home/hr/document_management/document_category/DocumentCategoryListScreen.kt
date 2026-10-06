@file:Suppress("AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.hr.document_management.document_category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.DocumentCategoryDtoCat
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel

@Composable
fun DocumentCategoryListScreen(
    viewModel: HrViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onNavigateToAddCategory: () -> Unit = {},
    onEditCategory: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    val categories by viewModel.documentCategoriesCat.collectAsState()
    val isLoading by viewModel.isLoadingCategoriesCat.collectAsState()

    // State to track which category is selected for deletion
    var categoryToDelete by remember { mutableStateOf<DocumentCategoryDtoCat?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchDocumentCategoriesCat()
    }

    val fabConfig = remember {
        FabConfig(
            label = "Add Document Category",
            icon = Icons.Default.Add,
            onClick = onNavigateToAddCategory
        )
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = fabConfig,
        fabVisible = !isLoading
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Top Title Bar
            TitleBar(
                title = "Document Categories",
                onClose = onClose
            )

            // Breadcrumb Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("HR", fontSize = tokens.label, color = close_color)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = close_color, modifier = Modifier.size(14.dp))
                Text("Training Management", fontSize = tokens.label, color = close_color)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = close_color, modifier = Modifier.size(14.dp))
            }

            Spacer(Modifier.height(4.dp))

            // Search Filter Bar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Programs, employee...",
                onFilterClick = {}
            )

            Spacer(Modifier.height(12.dp))

            // Category Items List
            if (isLoading) {
                ListSkeleton()
            } else {
                val filteredCategories = categories.filter {
                    it.categoryName.contains(searchQuery, ignoreCase = true) ||
                            (it.description ?: "").contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCategories, key = { it.id }) { category ->
                        DocumentCategoryItemCard(
                            category = category,
                            onEditClick = { onEditCategory(category.id) },
                            onDeleteClick = {
                                // Trigger delete confirmation dialog
                                categoryToDelete = category
                            }
                        )
                    }
                }
            }
        }
    }

    // ── Reusable DeleteModel Dialog ──
    categoryToDelete?.let { category ->
        DeleteModel(
            title = "Delete Document Category",
            message = "Are you sure you want to delete '${category.categoryName}'?\nThis action cannot be undone.",
            onDismiss = {
                categoryToDelete = null
            },
            onDelete = {
                val targetId = category.id
                categoryToDelete = null
                viewModel.deleteCategoryCat(
                    id = targetId,
                    onSuccess = {
                        DynamicIslandManager.showSuccess("Document category deleted successfully")
                    },
                    onError = { errorMessage ->
                        DynamicIslandManager.showError(errorMessage)
                    }
                )
            }
        )
    }
}

@Composable
fun DocumentCategoryItemCard(
    category: DocumentCategoryDtoCat,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val actions = remember(category.id) {
        listOf(
            MenuAction(
                label = "Edit",
                textColor = TextPrimary,
                tint = close_color,
                onClick = onEditClick
            ),
            MenuAction(
                label = "Delete",
                textColor = redText,
                tint = redText,
                onClick = onDeleteClick
            )
        )
    }

    DataCard(
        item = category,
        title = category.id.takeLast(3).uppercase(),
        titleColor = close_color,
        titleFontWeight = FontWeight.Normal,
        topBadgeText = if (category.validityRequired) "Valid" else "No Expiry",
        topBadgeBgColor = greenBg,
        topBadgeTextColor = complete_button_bg,
        topBadgeShowDot = false,
        topBadgeInline = true,
        topBadgeCornerRadius = tokens.cardCornerRadius,
        showHeaderDivider = false,
        actions = actions,
        onClick = { onEditClick() },
        content = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = category.categoryName,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = category.description.orEmpty().ifBlank { "-" },
                    fontSize = tokens.bodySmall,
                    color = close_color,
                    maxLines = 2
                )
            }
        }
    )
}