@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused"
)
@file:OptIn(ExperimentalStdlibApi::class)

package com.cuso.tailor.view.home.sales.quotation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.sales.QuotationItemDto
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.utils.currencySymbol
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.BranchUiState
import com.cuso.tailor.viewmodel.BranchViewModel
import com.cuso.tailor.viewmodel.QuotationDeleteUiState
import com.cuso.tailor.viewmodel.QuotationUiState
import com.cuso.tailor.viewmodel.QuotationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

// -------------------------------------------------------------
// UI Model for Quotation item
// -------------------------------------------------------------
data class PricingItem(
    val id: String,
    val title: String,
    val price: String,
    val isActive: Boolean,
    val status: String = "",
    val branchId: String = "",
    val applicableGarmentLabel: String = "Applicable Garment",
    val applicableGarmentValue: String,
    val fabricLabel: String = "N/A",
    val fabricPrice: Double = 0.0
)

private fun QuotationItemDto.toPricingItem(): PricingItem {
    val customGarmentItem = items.firstOrNull { it.lineType == "Custom_Garment" } ?: items.firstOrNull()
    val garmentName = customGarmentItem?.customGarment?.garmentName
        ?: customGarmentItem?.customGarment?.categoryDisplayName
        ?: customGarmentItem?.itemDescription
        ?: "Quotation"

    val customerName = customerSnapshot?.name
        ?: customerId?.fullName
        ?: quotationNumber
        ?: "Customer"

    return PricingItem(
        id = id,
        title = "$customerName - $garmentName",
        price = "${currencySymbol()}${String.format(java.util.Locale.US, "%.2f", grandTotal)}",
        isActive = status.equals("draft", ignoreCase = true).not(),
        status = status.replaceFirstChar { it.uppercase() },
        branchId = branchId ?: "",
        applicableGarmentValue = garmentName,
        fabricLabel = customGarmentItem?.customGarment?.fabricNotes ?: "N/A",
        fabricPrice = 0.0
    )
}

// -------------------------------------------------------------
// Quotation Filters Builder Function
// -------------------------------------------------------------
private fun buildQuotationFilterSections(branchOptions: List<FilterOption> = emptyList()): List<FilterSection> {
    return listOf(
        FilterSection(
            title = "Status",
            type = FilterSectionType.CHECKBOX_LIST,
            isMultiSelect = true,
            options = listOf(
                FilterOption("draft", "Draft"),
                FilterOption("pending_approval", "Pending Approval"),
                FilterOption("sent", "Sent"),
                FilterOption("accepted", "Accepted"),
                FilterOption("converted_to_order", "Converted To Order"),
                FilterOption("rejected", "Rejected"),
                FilterOption("expired", "Expired")
            )
        ),
        FilterSection(
            title = "Branch",
            type = FilterSectionType.CHECKBOX_LIST,
            isMultiSelect = true,
            options = if (branchOptions.isNotEmpty()) {
                branchOptions
            } else {
                listOf(FilterOption("default_branch", "Nexus Boutique - Main Branch"))
            }
        )
    )
}

// -------------------------------------------------------------
// Quotation Screen
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationScreen(
    onClose: () -> Unit = {},
    onAddNe: () -> Unit = {},
    onView: (String) -> Unit = {},
    onEdit: (String) -> Unit = {},
    onBreadCrumbClick: () -> Unit = {},
    quotationViewModel: QuotationViewModel = hiltViewModel(),
    branchViewModel: BranchViewModel = hiltViewModel()
) {
    val quotationState by quotationViewModel.uiState.collectAsStateWithLifecycle()
    val deleteState by quotationViewModel.deleteState.collectAsStateWithLifecycle()
    val isLoadingMore by quotationViewModel.isLoadingMore.collectAsStateWithLifecycle()
    val canLoadMore by quotationViewModel.canLoadMore.collectAsStateWithLifecycle()

    // Fetch branches from BranchViewModel API
    val branchState by branchViewModel.uiState.collectAsStateWithLifecycle()
    val branches = (branchState as? BranchUiState.Success)?.branches ?: emptyList()

    val listState = rememberLazyListState()
    var searchQuery by remember { mutableStateOf("") }
    var quotationToDelete by remember { mutableStateOf<PricingItem?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Filter drawer state
    val filterDrawerState = rememberFilterDrawerState()
    var filterSections: List<FilterSection> by remember {
        mutableStateOf<List<FilterSection>>(buildQuotationFilterSections())
    }

    // Populate branch filter options dynamically when branches load from API
    LaunchedEffect(branches) {
        if (branches.isNotEmpty()) {
            filterSections = filterSections.map { section: FilterSection ->
                if (section.title.equals("Branch", ignoreCase = true)) {
                    val previouslySelected: Set<String> = section.options
                        .filter { it.isSelected }
                        .map { it.id }
                        .toSet()

                    val dynamicBranchOptions = branches.map { b ->
                        val label = b.name?.takeIf { it.isNotBlank() } ?: "Main Branch"
                        FilterOption(
                            id = b.id,
                            label = label,
                            isSelected = b.id in previouslySelected
                        )
                    }
                    section.copy(options = dynamicBranchOptions)
                } else {
                    section
                }
            }
        }
    }

    // Count of currently active filters for the badge
    val activeFilterCount by remember(filterSections) {
        derivedStateOf {
            filterSections.sumOf { section: FilterSection ->
                section.options.count { it.isSelected }
            }
        }
    }

    val allItems: List<PricingItem> = when (val state = quotationState) {
        is QuotationUiState.Success -> state.quotations.map { it.toPricingItem() }
        else -> emptyList()
    }

    // Filter list based on selected statuses and branches
    val filteredItems by remember(allItems, filterSections) {
        derivedStateOf {
            val selectedStatuses: List<String> = filterSections
                .find { it.title.equals("Status", ignoreCase = true) }
                ?.options
                ?.filter { it.isSelected }
                ?.map { it.label.lowercase().replace(" ", "_") }
                ?: emptyList()

            val selectedBranchIds: List<String> = filterSections
                .find { it.title.equals("Branch", ignoreCase = true) }
                ?.options
                ?.filter { it.isSelected }
                ?.map { it.id }
                ?: emptyList()

            allItems.filter { item ->
                val normalizedStatus = item.status.lowercase().replace(" ", "_")
                val matchesStatus = selectedStatuses.isEmpty() ||
                        selectedStatuses.any { normalizedStatus.contains(it) || it.contains(normalizedStatus) }

                val matchesBranch = selectedBranchIds.isEmpty() ||
                        (item.branchId.isNotBlank() && item.branchId in selectedBranchIds)

                matchesStatus && matchesBranch
            }
        }
    }

    // Infinite scroll trigger
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val total = info.totalItemsCount
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 3
        }
            .distinctUntilChanged()
            .collect { nearEnd ->
                if (nearEnd && canLoadMore && !isLoadingMore) {
                    quotationViewModel.loadMoreQuotations()
                }
            }
    }

    // Initial API loads
    LaunchedEffect(Unit) {
        quotationViewModel.loadQuotations()
        branchViewModel.loadBranches()
    }

    // Search query with debounce
    LaunchedEffect(searchQuery) {
        delay(400)
        quotationViewModel.searchQuotations(searchQuery)
    }

    LaunchedEffect(deleteState) {
        when (val state = deleteState) {
            is QuotationDeleteUiState.Success -> {
                successMsg = "Quotation deleted successfully"
                quotationViewModel.resetDeleteState()
            }
            is QuotationDeleteUiState.Error -> {
                errorMsg = state.message
                quotationViewModel.resetDeleteState()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            fab = FabConfig(
                label = "Add New Quotation",
                icon = Icons.Default.Add,
                onClick = onAddNe,
                bottomPadding = 24.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                // Top Title Bar
                Column(modifier = Modifier.fillMaxWidth()) {
                    TitleBar(title = "Quotation List", onClose = onClose)
                    Spacer(Modifier.height(8.dp))
                }

                HorizontalDivider(color = title_border)

                // Main Content container hosting Search, List, and FilterDrawer below TitleBar
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = "Search Customers...",
                            filterCount = activeFilterCount,
                            accentColor = BluePrimary,
                            borderColor = BorderGray,
                            textSecondaryColor = TextSecondary,
                            onFilterClick = { filterDrawerState.open() }
                        )

                        when {
                            quotationState is QuotationUiState.Loading -> {
                                ListSkeleton()
                            }

                            quotationState is QuotationUiState.Error -> {
                                AppErrorState(
                                    title = "Failed to load Quotation",
                                    message = "Something went wrong. Please check your connection and try again.",
                                    onRetry = { quotationViewModel.refresh() }
                                )
                            }

                            filteredItems.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No quotations found", color = Color(0xFF9CA3AF))
                                }
                            }

                            else -> {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    items(filteredItems, key = { it.id }) { item ->
                                        val (badgeBg, badgeText) = when (item.status.lowercase()) {
                                            "accepted", "approved" -> greenBg to greentext
                                            "rejected", "cancelled" -> redBg to redText
                                            "sent" -> primary_light to BluePrimary
                                            "draft" -> yellowBg to yellowText
                                            else -> primary_light to BluePrimary
                                        }

                                        val menuActions = listOf(
                                            MenuAction("View", Icons.Default.Visibility, onClick = { onView(item.id) }),
                                            MenuAction("Edit", Icons.Default.Edit, onClick = { onEdit(item.id) }),
                                            MenuAction(
                                                "Delete",
                                                Icons.Default.Delete,
                                                tint = redText,
                                                textColor = redText,
                                                onClick = { quotationToDelete = item }
                                            )
                                        )

                                        DataCard(
                                            item = item,
                                            topBadgeText = item.status,
                                            showActionsInHeader = true,
                                            topBadgeTextColor = badgeText,
                                            topBadgeBgColor = badgeBg,
                                            topBadgeCornerRadius = 20.dp,
                                            topBadgeInline = true,
                                            topBadgeShowDot = true,
                                            title = item.title,
                                            content = {
                                                val tokens = LocalAppTokens.current
                                                Column {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = item.price,
                                                            fontSize = tokens.bodySmall,
                                                            color = TextPrimary
                                                        )
                                                        ActionDropdownMenu(icon = Icons.Default.MoreVert, actions = menuActions)
                                                    }
                                                    Spacer(Modifier.height(8.dp))
                                                    Text(
                                                        text = item.applicableGarmentLabel,
                                                        fontSize = tokens.bodySmall,
                                                        color = mutedText
                                                    )
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        text = item.applicableGarmentValue,
                                                        fontSize = tokens.bodySmall,
                                                        color = TextPrimary
                                                    )
                                                }
                                            },
                                            onClick = { onView(item.id) }
                                        )
                                    }

                                    if (isLoadingMore) {
                                        item {
                                            ThreeDotLoading()
                                        }
                                    }

                                    item { Spacer(modifier = Modifier.height(90.dp)) }
                                }
                            }
                        }
                    }

                    // Slide-over Filter Drawer
                    FilterDrawer(
                        state = filterDrawerState,
                        title = "Filter Quotations",
                        sections = filterSections,
                        onApply = { updatedSections: List<FilterSection> ->
                            filterSections = updatedSections
                        },
                        onClearAll = {
                            filterSections = filterSections.map { section: FilterSection ->
                                section.copy(
                                    options = section.options.map { option: FilterOption ->
                                        option.copy(isSelected = false)
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }

        // Delete Alert Dialog
        if (quotationToDelete != null) {
            DeleteModel(
                title = "Delete Quotation",
                message = "Are you sure you want to delete this quotation? This action cannot be undone.",
                onDismiss = { quotationToDelete = null },
                onDelete = {
                    quotationViewModel.deleteQuotation(quotationToDelete!!.id)
                    quotationToDelete = null
                }
            )
        }
    }
}