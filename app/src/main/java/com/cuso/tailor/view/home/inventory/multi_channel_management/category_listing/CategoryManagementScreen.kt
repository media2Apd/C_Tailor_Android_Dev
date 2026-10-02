@file:Suppress("UNUSED_PARAMETER", "unused", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.inventory.multi_channel_management.category_listing

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.CategoryItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.InventoryViewModel
import com.cuso.tailor.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Category data model used across management screens.
 */
data class CategoryItemUi(
    val id: String,
    val name: String,
    val code: String,
    val parentCategory: String = "Fashion",
    val productsCount: Int = 100,
    val createdDate: String = "12 Jan 2026",
    val isActive: Boolean = true
)

// =============================================================================
// SCREEN 1: CATEGORY MANAGEMENT (LIST SCREEN)
// =============================================================================
// ── Default Category Filter Section (Same pattern as Lead Screen) ──
fun getDefaultCategoryFilterSections(): List<FilterSection> = listOf(
    FilterSection(
        title = "Status",
        type = FilterSectionType.CHIP_ROW,
        options = listOf(
            FilterOption("active", "Active", isSelected = false),
            FilterOption("inactive", "Inactive", isSelected = false)
        ),
        isMultiSelect = true
    )
)

// =============================================================================
// SCREEN 1: CATEGORY MANAGEMENT (LIST SCREEN - LEAD SCREEN PATTERN)
// =============================================================================
@Composable
fun CategoryManagementScreen(
    onClose: () -> Unit = {},
    onAddCategory: () -> Unit = {},
    onEditCategory: (CategoryItemDto) -> Unit = {},
    viewModel: InventoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    // State for tracking which category is pending deletion
    var categoryPendingDelete by remember { mutableStateOf<CategoryItemDto?>(null) }

    // ── Filter Drawer State & Sections (Identical to LeadScreenContent) ──
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections by remember {
        mutableStateOf(getDefaultCategoryFilterSections())
    }

    val categoriesList by viewModel.categoriesList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingCategories.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMoreCategories.collectAsStateWithLifecycle()
    val canLoadMore by viewModel.canLoadMoreCategories.collectAsStateWithLifecycle()
    val errorMessage by viewModel.categoriesError.collectAsStateWithLifecycle()

    // Observe product categories catalog from SettingsViewModel
    val productCategories by settingsViewModel.productCategories.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // Fetch both categories and the product categories catalog
    LaunchedEffect(Unit) {
        viewModel.fetchCategories(page = 1)
        settingsViewModel.fetchProductCategories()
    }

    // Scroll threshold observer for pagination (preventing infinite loops)
    LaunchedEffect(listState) {
        snapshotFlow {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= (totalItems - 2)
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && canLoadMore && !isLoading && !isLoadingMore) {
                    viewModel.loadMoreCategories()
                }
            }
    }

    // ── Calculate Active Filter Badge Count (Identical to LeadScreenContent) ──
    val activeFilterCount by remember(filterSections) {
        derivedStateOf {
            filterSections.sumOf { section ->
                section.options.count { it.isSelected }
            }
        }
    }

    // ── Filter Categories Matching Search and Selected Statuses ──
    val filteredCategories by remember(categoriesList, searchQuery, filterSections) {
        derivedStateOf {
            categoriesList.filter { item ->
                // Search query match (Category Name or Code)
                val matchesSearch = searchQuery.isBlank() ||
                        item.name.contains(searchQuery, ignoreCase = true) ||
                        (item.code?.contains(searchQuery, ignoreCase = true) == true)

                // Status filter match
                val selectedStatusOptions = filterSections.find { it.title == "Status" }
                    ?.options?.filter { it.isSelected }?.map { it.id.lowercase() } ?: emptyList()

                val itemStatus = item.status?.lowercase() ?: "inactive"
                val matchesStatus = selectedStatusOptions.isEmpty() ||
                        selectedStatusOptions.any { it == itemStatus }

                matchesSearch && matchesStatus
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Wrap with FabScaffold (Identical to LeadScreenContent structure)
        FabScaffold(
            fab = FabConfig(
                label = "Add Category",
                icon = Icons.Default.Add,
                onClick = onAddCategory,
                bottomPadding = 50.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Screen Title Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    TitleBar(
                        title = "Category Management",
                        onClose = onClose
                    )
                }
                HorizontalDivider(color = title_border)

                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search Bar & Filter Button with Badge Count
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                viewModel.onCategorySearchQueryChanged(it)
                            },
                            placeholder = "Search Categories...",
                            filterCount = activeFilterCount,
                            accentColor = BluePrimary,
                            borderColor = BorderGray,
                            textSecondaryColor = TextSecondary,
                            onFilterClick = { filterDrawerState.open() }
                        )

                        // Category Items Content Area
                        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            when {
                                isLoading && categoriesList.isEmpty() -> {
                                    ListSkeleton()
                                }

                                errorMessage != null && categoriesList.isEmpty() -> {
                                    AppErrorState(
                                        title = "Failed to load categories",
                                        message = errorMessage ?: "Unexpected error occurred",
                                        onRetry = { viewModel.fetchCategories(page = 1) }
                                    )
                                }

                                filteredCategories.isEmpty() -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(tokens.screenPadding * 2.5f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val hasFilters = filterSections.any { sec -> sec.options.any { it.isSelected } }
                                            Text(
                                                text = if (searchQuery.isNotBlank() || hasFilters) "No matching categories found" else "No Categories Found",
                                                fontSize = tokens.h2,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                text = if (searchQuery.isNotBlank() || hasFilters) "Try adjusting your search or filters" else "Start by adding your first category",
                                                fontSize = tokens.bodyMedium,
                                                color = mutedText
                                            )
                                            Spacer(Modifier.height(20.dp))
                                            Button(
                                                onClick = onAddCategory,
                                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                                contentPadding = PaddingValues(
                                                    horizontal = tokens.screenPadding,
                                                    vertical = tokens.screenPadding * 0.6f
                                                )
                                            ) {
                                                Text(
                                                    text = "Add Category",
                                                    fontSize = tokens.bodyMedium,
                                                    color = whiteBg,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    Column(modifier = Modifier.fillMaxSize()) {
                                        LazyColumn(
                                            state = listState,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                        ) {
                                            items(filteredCategories, key = { it.id }) { item ->
                                                val isActive = item.status.equals("active", ignoreCase = true)
                                                val (badgeBg, badgeTextColor) = if (isActive) {
                                                    greenBg to darkGreenBg
                                                } else {
                                                    light_grey to TextSecondary
                                                }

                                                val actions = listOf(
                                                    MenuAction("Edit", Icons.Default.Edit) { onEditCategory(item) },
                                                    MenuAction("Delete", Icons.Default.Delete, tint = redText, textColor = redText) {
                                                        categoryPendingDelete = item
                                                    }
                                                )

                                                DataCard(
                                                    item = item,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    title = item.name,
                                                    titleColor = TextPrimary,
                                                    titleFontWeight = FontWeight.Medium,
                                                    subtitle = item.code?.ifBlank { "—" } ?: "—",
                                                    topBadgeText = if (isActive) "Active" else "Inactive",
                                                    topBadgeBgColor = badgeBg,
                                                    topBadgeTextColor = badgeTextColor,
                                                    topBadgeDotColor = badgeTextColor,
                                                    topBadgeShowDot = true,
                                                    topBadgeInline = true,
                                                    showActionsInHeader = false,
                                                    actions = actions,
                                                    image = DataCardImage(
                                                        painter = painterResource(id = R.drawable.ic_shirts),
                                                        size = tokens.fieldHeight + 4.dp,
                                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                                                        backgroundColor = yellowBg,
                                                        tint = yellowText
                                                    ),
                                                    content = {
                                                        // Bottom Attributes Section
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                                                                .background(Primary_background)
                                                                .padding(
                                                                    vertical = tokens.extraPadding,
                                                                    horizontal = tokens.screenPadding * 0.9f
                                                                )
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                val parentCategoryDisplayName = remember(item.parentCategoryId, productCategories) {
                                                                    if (item.parentCategoryId.isNullOrBlank()) {
                                                                        "—"
                                                                    } else {
                                                                        productCategories.find { it.id == item.parentCategoryId }?.name ?: "—"
                                                                    }
                                                                }

                                                                Column(
                                                                    modifier = Modifier.weight(1.2f),
                                                                    horizontalAlignment = Alignment.Start
                                                                ) {
                                                                    Text(
                                                                        text = "Parent Category",
                                                                        fontSize = tokens.caption,
                                                                        fontWeight = FontWeight.Normal,
                                                                        color = iconMuted
                                                                    )
                                                                    Spacer(modifier = Modifier.height(2.dp))
                                                                    Text(
                                                                        text = parentCategoryDisplayName,
                                                                        fontSize = tokens.bodySmall,
                                                                        color = textSubdued
                                                                    )
                                                                }

                                                                Column(
                                                                    modifier = Modifier.weight(0.9f),
                                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                                ) {
                                                                    Text(
                                                                        text = "Products",
                                                                        fontSize = tokens.caption,
                                                                        fontWeight = FontWeight.Normal,
                                                                        color = iconMuted
                                                                    )
                                                                    Spacer(modifier = Modifier.height(2.dp))
                                                                    Text(
                                                                        text = item.productCount.toString(),
                                                                        fontSize = tokens.bodySmall,
                                                                        color = textSubdued
                                                                    )
                                                                }

                                                                Column(
                                                                    modifier = Modifier.weight(1.1f),
                                                                    horizontalAlignment = Alignment.End
                                                                ) {
                                                                    Text(
                                                                        text = "Created Date",
                                                                        fontSize = tokens.caption,
                                                                        fontWeight = FontWeight.Normal,
                                                                        color = iconMuted
                                                                    )
                                                                    Spacer(modifier = Modifier.height(2.dp))
                                                                    Text(
                                                                        text = formatIsoDate(item.createdAt),
                                                                        fontSize = tokens.bodySmall,
                                                                        color = textSubdued
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
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
                            }
                        }
                    }

                    // ── FilterDrawer Component (Exactly as placed in LeadScreenContent) ──
                    FilterDrawer(
                        state = filterDrawerState,
                        title = "Filters",
                        sections = filterSections,
                        onApply = { updatedSections ->
                            filterSections = updatedSections
                        },
                        onClearAll = {
                            filterSections = filterSections.map { section ->
                                section.copy(options = section.options.map { option -> option.copy(isSelected = false) })
                            }
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    categoryPendingDelete?.let { category ->
        DeleteModel(
            title = "Delete Category",
            message = "Are you sure you want to delete category \"${category.name}\"? This action cannot be undone.",
            onDismiss = { categoryPendingDelete = null },
            onDelete = {
                viewModel.deleteCategory(category.id) {
                    categoryPendingDelete = null
                }
            }
        )
    }
}


// =============================================================================
// HELPER DATE FORMATTER
// =============================================================================

private fun formatIsoDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "-"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val parsed = inputFormat.parse(isoDate)
        parsed?.let { outputFormat.format(it) } ?: isoDate.take(10)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}

// =============================================================================
// SCREEN 2: CREATE / EDIT CATEGORY (FORM SCREEN)
// =============================================================================

@Composable
fun CreateCategoryScreen(
    categoryId: String? = null,
    onClose: () -> Unit = {},
    onCancel: () -> Unit = {},
    onSavedSuccess: () -> Unit = {},
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val isEditMode = !categoryId.isNullOrBlank()

    // Form inputs state
    var categoryName by remember { mutableStateOf("") }
    var categoryCode by remember { mutableStateOf("") }
    var selectedParentCategoryName by remember { mutableStateOf("None (Top Level)") }
    var selectedParentCategoryId by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }

    var parentCategoryDropdownExpanded by remember { mutableStateOf(false) }

    // Observers from ViewModels
    val productCategories by settingsViewModel.productCategories.collectAsStateWithLifecycle()
    val isSaving by inventoryViewModel.isSavingCategory.collectAsStateWithLifecycle()
    val selectedCategoryDetail by inventoryViewModel.selectedCategoryDetail.collectAsStateWithLifecycle()
    val isLoadingDetail by inventoryViewModel.isLoadingCategoryDetail.collectAsStateWithLifecycle()

    // Fetch product categories catalog for the dropdown on screen launch
    LaunchedEffect(Unit) {
        settingsViewModel.fetchProductCategories()
    }

    // Load category details if in Edit Mode
    LaunchedEffect(categoryId) {
        if (isEditMode) {
            inventoryViewModel.fetchCategoryDetail(categoryId)
        } else {
            inventoryViewModel.clearSelectedCategoryDetail()
        }
    }

    // Prefill form inputs when detail response arrives
    LaunchedEffect(selectedCategoryDetail, productCategories) {
        selectedCategoryDetail?.let { detail ->
            categoryName = detail.name
            categoryCode = detail.code.orEmpty()
            description = detail.description.orEmpty()

            // Resolve parent category display name from ID
            val parentId = detail.parentCategoryId
            if (!parentId.isNullOrBlank()) {
                val matchedParent = productCategories.find { it.id == parentId }
                selectedParentCategoryName = matchedParent?.name ?: "None (Top Level)"
                selectedParentCategoryId = parentId
            } else {
                selectedParentCategoryName = "None (Top Level)"
                selectedParentCategoryId = null
            }
        }
    }

    // Build parent category options list
    val parentCategoryOptions = remember(productCategories) {
        listOf("None (Top Level)") + productCategories.map { it.name }.filter { it.isNotBlank() }
    }

    Scaffold(
        topBar = {
            Row(modifier = Modifier.fillMaxWidth()) {
                TitleBar(
                    title = if (isEditMode) "Edit Category" else "Create Category",
                    onClose = onClose
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent
    ) { innerPadding ->
        if (isLoadingDetail) {
            ListSkeleton()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Main Form Card Container
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.cardPadding)
                    ) {
                        // Section Header: Info Icon + Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(tokens.iconSize * 1.25f)
                                    .clip(CircleShape)
                                    .background(background_light_purple),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "i",
                                    color = Primary,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Text(
                                text = "Basic Information",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary
                            )
                        }

                        Spacer(modifier = Modifier.height(tokens.extraPadding))
                        HorizontalDivider(color = grey_border, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(tokens.extraPadding))

                        // 1. Category Name Field
                        FormLabel(text = "Category Name", isRequired = true)
                        FormTextField(
                            value = categoryName,
                            onValueChange = { categoryName = it },
                            placeholder = "e.g., Cotton Fabrics",
                            borderColor = sectionBorder
                        )

                        Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))

                        // 2. Category Code Field
                        FormLabel(text = "Category Code", isRequired = false)
                        FormTextField(
                            value = categoryCode,
                            onValueChange = { categoryCode = it },
                            placeholder = "E.G., FAB-COT",
                            borderColor = sectionBorder
                        )

                        Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))

                        // 3. Parent Category Selection Dropdown
                        FormDropdown(
                            label = "Parent Category",
                            value = selectedParentCategoryName,
                            expanded = parentCategoryDropdownExpanded,
                            onExpandChange = { parentCategoryDropdownExpanded = it },
                            options = parentCategoryOptions,
                            onOptionSelected = { optionName ->
                                selectedParentCategoryName = optionName
                                selectedParentCategoryId = if (optionName == "None (Top Level)") {
                                    null
                                } else {
                                    productCategories.find { it.name == optionName }?.id
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(tokens.extraPadding * 1.2f))

                        // 4. Description (Optional) TextArea
                        FormLabel(text = "Description (Optional)", isRequired = false)
                        FormTextArea(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = "Brief details about the textile category...",
                            minLines = 4,
                            maxLines = 5,
                            borderColor = sectionBorder,
                            focusedBorderColor = Primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding * 1.5f))

                // Bottom Action Buttons: Cancel and Save/Update Category
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = tokens.extraPadding),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
                        border = BorderStroke(1.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Reusable AppButton implementation
                    AppButton(
                        text = if (isEditMode) "Update Category" else "Save Category",
                        isLoading = isSaving,
                        enabled = categoryName.isNotBlank(),
                        modifier = Modifier.weight(1.1f),
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                if (isEditMode) {
                                    inventoryViewModel.updateCategory(
                                        id = categoryId,
                                        name = categoryName,
                                        code = categoryCode,
                                        parentCategoryId = selectedParentCategoryId,
                                        description = description,
                                        onSuccess = onSavedSuccess
                                    )
                                } else {
                                    inventoryViewModel.createCategory(
                                        name = categoryName,
                                        code = categoryCode,
                                        parentCategoryId = selectedParentCategoryId,
                                        description = description,
                                        onSuccess = onSavedSuccess
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}