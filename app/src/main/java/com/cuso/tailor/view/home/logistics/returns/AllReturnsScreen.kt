package com.cuso.tailor.view.home.logistics.returns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.ReturnItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun LogisticsReturnScreen(
    onNavigateBack: () -> Unit,
    onCreateReturnClick: () -> Unit,
    onEditReturnClick: (String) -> Unit = {},
    onReturnClick: (ReturnItemDto) -> Unit,
    viewModel: LogisticsViewModel = viewModel()
) {
    val tokens = LocalAppTokens.current

    // Observe individual StateFlows from LogisticsViewModel
    val returnsList by viewModel.filteredReturns.collectAsState()
    val searchQuery by viewModel.returnSearchQuery.collectAsState()
    val isLoading by viewModel.isLoadingReturns.collectAsState()
    val errorMessage by viewModel.returnsError.collectAsState()
    val selectedStatus by viewModel.selectedReturnStatus.collectAsState()
    val selectedType by viewModel.selectedReturnType.collectAsState()
    val selectedPriority by viewModel.selectedReturnPriority.collectAsState()

    val filterDrawerState = rememberFilterDrawerState()

    // State holding the return item selected for deletion
    var itemToDelete by remember { mutableStateOf<ReturnItemDto?>(null) }

    // Fetch returns on initial screen launch
    LaunchedEffect(Unit) {
        viewModel.fetchReturns()
    }

    // Calculate active filter badge count
    val activeFilterCount = remember(selectedStatus, selectedType, selectedPriority) {
        listOfNotNull(selectedStatus, selectedType, selectedPriority).count()
    }

    // Configure filter categories for FilterDrawer
    val filterSections = remember(selectedStatus, selectedType, selectedPriority) {
        listOf(
            FilterSection(
                title = "Status",
                options = listOf(
                    FilterOption("Active", "Active", selectedStatus.equals("Active", ignoreCase = true)),
                    FilterOption("Pending_Review", "Pending Review", selectedStatus.equals("Pending_Review", ignoreCase = true)),
                    FilterOption("Approved", "Approved", selectedStatus.equals("Approved", ignoreCase = true)),
                    FilterOption("Rejected", "Rejected", selectedStatus.equals("Rejected", ignoreCase = true)),
                    FilterOption("Completed", "Completed", selectedStatus.equals("Completed", ignoreCase = true))
                ),
                isMultiSelect = false
            ),
            FilterSection(
                title = "Type",
                options = listOf(
                    FilterOption("Alteration", "Alteration", selectedType.equals("Alteration", ignoreCase = true)),
                    FilterOption("Exchange", "Exchange", selectedType.equals("Exchange", ignoreCase = true)),
                    FilterOption("Refund", "Refund", selectedType.equals("Refund", ignoreCase = true))
                ),
                isMultiSelect = false
            ),
            FilterSection(
                title = "Priority",
                options = listOf(
                    FilterOption("High", "High", selectedPriority.equals("High", ignoreCase = true)),
                    FilterOption("Medium", "Medium", selectedPriority.equals("Medium", ignoreCase = true)),
                    FilterOption("Low", "Low", selectedPriority.equals("Low", ignoreCase = true))
                ),
                isMultiSelect = false
            )
        )
    }

    val fabConfig = remember(onCreateReturnClick) {
        FabConfig(
            label = "Create Return",
            icon = Icons.Default.Add,
            onClick = onCreateReturnClick
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            modifier = Modifier.fillMaxSize(),
            fab = fabConfig,
            fabVisible = !filterDrawerState.isOpen
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TitleBar(title = "All Returns", onClose = onNavigateBack)
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Search & Filter Bar
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onReturnSearchQueryChange,
                        placeholder = "Search SO number, customer...",
                        filterCount = activeFilterCount,
                        onFilterClick = { filterDrawerState.open() },
                        accentColor = Primary,
                        borderColor = BorderGray,
                        height = tokens.fieldHeight
                    )
                    when {
                        isLoading && returnsList.isEmpty() -> {
                            ListSkeleton()
                        }

                        errorMessage != null && returnsList.isEmpty() -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(tokens.screenPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = errorMessage ?: "Failed to load returns",
                                        fontSize = tokens.bodySmall,
                                        color = redText
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = { viewModel.fetchReturns() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                                    ) {
                                        Text("Retry", fontSize = tokens.bodySmall, color = whiteBg)
                                    }
                                }
                            }
                        }

                        returnsList.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No returns found",
                                    fontSize = tokens.bodyMedium,
                                    color = mutedText
                                )
                            }
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = returnsList,
                                    key = { it.id.ifBlank { it.returnId } }
                                ) { item ->
                                    ReturnDataCard(
                                        returnItem = item,
                                        formattedDate = viewModel.formatReturnDateTime(item.createdAt),
                                        onClick = { onReturnClick(item) },
                                        onEditClick = { onEditReturnClick(item.id.ifBlank { item.returnId }) },
                                        onDeleteClick = { itemToDelete = item }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Side Filter Drawer
        FilterDrawer(
            state = filterDrawerState,
            sections = filterSections,
            onApply = { updatedSections ->
                val status = updatedSections.find { it.title == "Status" }
                    ?.options?.find { it.isSelected }?.id
                val type = updatedSections.find { it.title == "Type" }
                    ?.options?.find { it.isSelected }?.id
                val priority = updatedSections.find { it.title == "Priority" }
                    ?.options?.find { it.isSelected }?.id

                viewModel.onReturnStatusFilterChange(status)
                viewModel.onReturnTypeFilterChange(type)
                viewModel.onReturnPriorityFilterChange(priority)
            },
            onClearAll = {
                viewModel.clearReturnFilters()
            }
        )

        // Delete Confirmation Modal
        itemToDelete?.let { returnItem ->
            val returnCode = returnItem.returnId.ifBlank { returnItem.id }
            DeleteModel(
                title = "Delete Return Request",
                message = "You're going to delete the return request \"$returnCode\". Are you sure?",
                checkboxText = "I confirm this deletion",
                cancelText = "No, Keep It.",
                confirmText = "Yes, Delete!",
                onDismiss = { itemToDelete = null },
                onDelete = {
                    val targetId = returnItem.id.ifBlank { returnItem.returnId }
                    itemToDelete = null
                    viewModel.deleteReturnItem(
                        id = targetId,
                        onSuccess = { msg -> DynamicIslandManager.showSuccess(msg) },
                        onError = { err -> DynamicIslandManager.showError(err) }
                    )
                }
            )
        }
    }
}

/**
 * Return Card reusing DataCard matching the exact visual card layout.
 */
@Composable
private fun ReturnDataCard(
    returnItem: ReturnItemDto,
    formattedDate: String,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val customerName = returnItem.customer?.name
        ?: returnItem.orderId?.customerSnapshot?.name
        ?: "Customer"
    val orderCode = returnItem.orderCode.ifBlank { returnItem.orderId?.orderCode ?: "" }

    val isPendingOrActive = returnItem.status.contains("Pending", ignoreCase = true) ||
            returnItem.status.contains("Active", ignoreCase = true)
    val statusBg = if (isPendingOrActive) greenBg else primary_light
    val statusText = if (isPendingOrActive) greentext else Primary

    val priorityColor = when (returnItem.priority.uppercase()) {
        "HIGH", "URGENT" -> redText
        "MEDIUM" -> orangeText
        else -> TextSecondary
    }

    val actions = remember(returnItem) {
        listOf(
            MenuAction(label = "View Details", onClick = onClick),
            MenuAction(label = "Edit", onClick = onEditClick),
            MenuAction(label = "Delete", onClick = onDeleteClick)
        )
    }

    DataCard(
        item = returnItem,
        title = customerName,
        titleFontWeight = FontWeight.Medium,
        titleColor = title_color,
        topBadgeText = returnItem.status.replace("_", " ").toTitleCase(),
        topBadgeBgColor = statusBg,
        topBadgeTextColor = statusText,
        topBadgeShowDot = true,
        topBadgeInline = true,
        actions = actions,
        onClick = { onClick() },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Secondary order pill tag right below/near customer name
                if (orderCode.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-4).dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(primary_light)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = orderCode,
                            fontSize = tokens.caption,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Inner specs container: Return ID | Order ID | Type
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .background(cardBgLight)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Return ID",
                                fontSize = tokens.label,
                                color = mutedText
                            )
                            Text(
                                text = returnItem.returnId,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Order ID",
                                fontSize = tokens.label,
                                color = mutedText
                            )
                            Text(
                                text = orderCode.ifBlank { "-" },
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Type",
                                fontSize = tokens.label,
                                color = mutedText
                            )
                            Text(
                                text = returnItem.returnType,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Card Footer with Date and Priority
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Date : ",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                        Text(
                            text = formattedDate,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Priority : ",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                        Text(
                            text = returnItem.priority.uppercase(),
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = priorityColor
                        )
                    }
                }
            }
        }
    )
}