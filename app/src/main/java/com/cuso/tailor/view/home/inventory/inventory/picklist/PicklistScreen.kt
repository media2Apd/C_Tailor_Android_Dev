package com.cuso.tailor.view.home.inventory.inventory.picklist
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.cuso.tailor.adaptive_screen.LocalAppTokens
//import com.cuso.tailor.model.inventory.PicklistItemDto
//import com.cuso.tailor.ui.theme.*
//import com.cuso.tailor.view.composable.*
//import com.cuso.tailor.viewmodel.InventoryViewModel
//import com.cuso.tailor.viewmodel.PicklistUiState
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.TimeZone
//
//@Composable
//fun PicklistScreen(
//    modifier: Modifier = Modifier,
//    viewModel: InventoryViewModel = hiltViewModel(),
//    onNavigateBack: () -> Unit = {},
//    onCreatePicklistClick: () -> Unit = {},
//    onPicklistItemClick: (PicklistItemDto) -> Unit = {}
//) {
//    val tokens = LocalAppTokens.current
//    val uiState by viewModel.picklistUiState.collectAsState()
//    val searchQuery by viewModel.picklistSearchQuery.collectAsState()
//    val filterDrawerState = rememberFilterDrawerState()
//
//    // Trigger initial load
//    LaunchedEffect(Unit) {
//        viewModel.fetchPicklists()
//    }
//
//    val filterSections = remember {
//        listOf(
//            FilterSection(
//                title = "Status",
//                options = listOf(
//                    FilterOption("yet_to_start", "Yet to Start"),
//                    FilterOption("in_progress", "In Progress"),
//                    FilterOption("picked", "Picked")
//                ),
//                isMultiSelect = false
//            )
//        )
//    }
//
//    FabScaffold(
//        modifier = modifier.fillMaxSize(),
//        fab = FabConfig(
//            label = "Picklist",
//            icon = Icons.Default.Add,
//            onClick = onCreatePicklistClick
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Transparent)
//        ) {
//            // Screen Header Bar
//            TitleBar(
//                title = "Picklist",
//                onClose = onNavigateBack
//            )
//
//            // Search and Filter Bar
//            SearchFilterBar(
//                query = searchQuery,
//                onQueryChange = viewModel::onPicklistSearchQueryChanged,
//                placeholder = "Search SO number, customer...",
//                showFilterIcon = true,
//                onFilterClick = { filterDrawerState.open() }
//            )
//
//            // Main Content Body
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//            ) {
//                when (val state = uiState) {
//                    is PicklistUiState.Loading -> {
//                        ListSkeleton()
//                    }
//
//                    // ── UPDATED: AppErrorState integrated with Retry ──
//                    is PicklistUiState.Error -> {
//                        AppErrorState(
//                            title = "Failed to load picklists",
//                            message = state.message.ifBlank { "Something went wrong while fetching picklist data. Please try again." },
//                            buttonText = "Retry",
//                            onRetry = {
//                                viewModel.fetchPicklists()
//                            }
//                        )
//                    }
//
//                    is PicklistUiState.Success -> {
//                        val picklistItems = state.items
//
//                        if (picklistItems.isEmpty()) {
//                            Box(
//                                modifier = Modifier.fillMaxSize(),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = "No picklists found",
//                                    fontSize = tokens.bodyMedium,
//                                    fontWeight = FontWeight.Medium,
//                                    color = mutedText
//                                )
//                            }
//                        } else {
//                            LazyColumn(
//                                modifier = Modifier.fillMaxSize(),
//                                contentPadding = PaddingValues(
//                                    horizontal = tokens.screenPadding,
//                                    vertical = 8.dp
//                                ),
//                                verticalArrangement = Arrangement.spacedBy(10.dp)
//                            ) {
//                                items(
//                                    items = picklistItems,
//                                    key = { it.id }
//                                ) { item ->
//                                    PicklistItemCard(
//                                        item = item,
//                                        onClick = { onPicklistItemClick(item) }
//                                    )
//                                }
//                            }
//                        }
//                    }
//
//                    is PicklistUiState.Idle -> Unit
//                }
//            }
//        }
//
//        // Drawer filter sheet
//        FilterDrawer(
//            state = filterDrawerState,
//            title = "Filters",
//            sections = filterSections,
//            onApply = { updatedSections ->
//                val selectedStatus = updatedSections
//                    .firstOrNull { it.title == "Status" }
//                    ?.options
//                    ?.firstOrNull { it.isSelected }
//                    ?.label
//                viewModel.onPicklistStatusFilterChanged(selectedStatus)
//            },
//            onClearAll = {
//                viewModel.onPicklistStatusFilterChanged(null)
//            }
//        )
//    }
//}
//
//@Composable
//private fun PicklistItemCard(
//    item: PicklistItemDto,
//    onClick: () -> Unit
//) {
//    val tokens = LocalAppTokens.current
//
//    // Status colors
//    val (statusBg, statusTextColor) = when (item.status?.lowercase()) {
//        "picked", "completed" -> Pair(greenBg, greentext)
//        "in progress" -> Pair(light_blue_border, Primary)
//        else -> Pair(orangeBg, orangeText)
//    }
//
//    val formattedDate = remember(item.date) {
//        formatIsoDate(item.date)
//    }
//
//    val menuActions = remember(item) {
//        listOf(
//            MenuAction(
//                label = "View",
//                textColor = TextPrimary,
//                onClick = onClick
//            ),
//            MenuAction(
//                label = "Delete",
//                textColor = redText,
//                onClick = { /* Action callback */ }
//            )
//        )
//    }
//
//    DataCard(
//        item = item,
//        title = item.assigneeId?.fullName ?: "Unassigned",
//        subtitle = item.picklistNumber.orEmpty(),
//        titleFontWeight = FontWeight.Medium,
//        titleColor = title_color,
//        topBadgeText = item.status ?: "Yet to Start",
//        topBadgeBgColor = statusBg,
//        topBadgeTextColor = statusTextColor,
//        topBadgeShowDot = false,
//        topBadgeInline = true,
//        actions = menuActions,
//        onClick = { onClick() },
//        showDivider = false,
//        content = {
//            Column(modifier = Modifier.fillMaxWidth()) {
//                // Inner Spec Box (Sales Order, Location, Item/Quantity)
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .border(
//                            width = 1.dp,
//                            color = BorderGray,
//                            shape = RoundedCornerShape(tokens.cardCornerRadius)
//                        ),
//                    color = Primary_background,
//                    shape = RoundedCornerShape(tokens.cardCornerRadius)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 14.dp, vertical = 10.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        // 1. Sales Order
//                        Column(
//                            horizontalAlignment = Alignment.Start,
//                            verticalArrangement = Arrangement.spacedBy(2.dp),
//                            modifier = Modifier.weight(1f)
//                        ) {
//                            Text(
//                                text = "Sales Order",
//                                fontSize = tokens.caption,
//                                fontWeight = FontWeight.Medium,
//                                color = mutedText,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                            Text(
//                                text = item.primarySalesOrderDisplay,
//                                fontSize = tokens.bodySmall,
//                                fontWeight = FontWeight.Medium,
//                                color = textSubdued,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                        }
//
//                        // 2. Location
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.spacedBy(2.dp),
//                            modifier = Modifier.weight(1.3f)
//                        ) {
//                            Text(
//                                text = "Location",
//                                fontSize = tokens.caption,
//                                fontWeight = FontWeight.Medium,
//                                color = mutedText,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                            Text(
//                                text = item.warehouseId?.name ?: "-",
//                                fontSize = tokens.bodySmall,
//                                fontWeight = FontWeight.Medium,
//                                color = textSubdued,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis,
//                                textAlign = TextAlign.Center
//                            )
//                        }
//
//                        // 3. Item/Quantity
//                        Column(
//                            horizontalAlignment = Alignment.End,
//                            verticalArrangement = Arrangement.spacedBy(2.dp),
//                            modifier = Modifier.weight(1.3f)
//                        ) {
//                            Text(
//                                text = "Item/Quantity",
//                                fontSize = tokens.caption,
//                                fontWeight = FontWeight.Medium,
//                                color = mutedText,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                            Text(
//                                text = "${item.totalItemsCount} items / ${item.totalQty} Qty",
//                                fontSize = tokens.bodySmall,
//                                fontWeight = FontWeight.Medium,
//                                color = textSubdued,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(10.dp))
//
//                // Date Footer Row
//                if (formattedDate.isNotBlank()) {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "Date : ",
//                            fontSize = tokens.caption,
//                            fontWeight = FontWeight.Medium,
//                            color = mutedText
//                        )
//                        Text(
//                            text = formattedDate,
//                            fontSize = tokens.caption,
//                            fontWeight = FontWeight.Medium,
//                            color = TextSecondary
//                        )
//                    }
//                }
//            }
//        }
//    )
//}
//
///**
// * Formats ISO date string to "MMM dd, yyyy"
// */
//private fun formatIsoDate(isoDate: String?): String {
//    if (isoDate.isNullOrBlank()) return ""
//    return try {
//        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
//            timeZone = TimeZone.getTimeZone("UTC")
//        }
//        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
//        val parsed = parser.parse(isoDate)
//        if (parsed != null) formatter.format(parsed) else isoDate
//    } catch (_: Exception) {
//        isoDate
//    }
//}