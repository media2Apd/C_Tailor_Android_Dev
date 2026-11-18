@file:Suppress("unused")

package com.cuso.tailor.view.home.logistics.cod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.AllCourierItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.formatCurrency
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.formatIndianNumber
import com.cuso.tailor.viewmodel.LogisticsViewModel

@Composable
fun AllCodScreen(
    viewModel: LogisticsViewModel,
    onBack: () -> Unit,
    onCreateOrderClick: () -> Unit = {},
    onViewDeliveryDetail: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    val courierPagingState by viewModel.allCourierPagingState.collectAsState()
    val searchQuery by viewModel.allCourierSearchQuery.collectAsState()
    val errorMessage by viewModel.allCourierError.collectAsState()
    val selectedStatus by viewModel.selectedCourierStatus.collectAsState()

    val filterDrawerState = rememberFilterDrawerState()

    LaunchedEffect(Unit) {
        viewModel.fetchAllCourierList()
    }

    val activeFilterCount = remember(selectedStatus) {
        if (!selectedStatus.isNullOrBlank()) 1 else 0
    }

    val filterSections = remember(selectedStatus) {
        listOf(
            FilterSection(
                title = "Status",
                options = listOf(
                    FilterOption("Pending", "Pending", selectedStatus.equals("Pending", ignoreCase = true)),
                    FilterOption("Collected", "Collected", selectedStatus.equals("Collected", ignoreCase = true)),
                    FilterOption("In_Transit", "In Transit", selectedStatus.equals("In_Transit", ignoreCase = true)),
                    FilterOption("Delivered", "Delivered", selectedStatus.equals("Delivered", ignoreCase = true))
                ),
                isMultiSelect = false
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            modifier = Modifier.fillMaxSize(),
            fab = null,
            fabVisible = !filterDrawerState.isOpen
        ) {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = Color.Transparent,
                topBar = {
                    TitleBar(
                        title = "All Courier",
                        onClose = onBack
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    SearchFilterBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onAllCourierSearchQueryChange,
                        placeholder = "Search SO number, customer...",
                        filterCount = activeFilterCount,
                        onFilterClick = { filterDrawerState.open() },
                        accentColor = Primary,
                        borderColor = BorderGray,
                        height = tokens.fieldHeight
                    )

                    when {
                        errorMessage != null && courierPagingState.items.isEmpty() && !courierPagingState.isInitialLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(tokens.screenPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = errorMessage ?: "Failed to load couriers",
                                        fontSize = tokens.bodySmall,
                                        color = redText
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    Button(
                                        onClick = { viewModel.fetchAllCourierList() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                                    ) {
                                        Text("Retry", fontSize = tokens.bodySmall, color = whiteBg)
                                    }
                                }
                            }
                        }

                        !courierPagingState.isInitialLoading && courierPagingState.items.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No courier records found",
                                    fontSize = tokens.bodyMedium,
                                    color = mutedText
                                )
                            }
                        }

                        else -> {
                            PaginatedDataList(
                                state = courierPagingState,
                                onLoadMore = { viewModel.loadMoreAllCourierList() },
                                modifier = Modifier.fillMaxSize()
                            ) { item ->
                                AllCodCard(
                                    item = item,
                                    formattedDate = viewModel.formatCourierDate(item.deliveryDate),
                                    onClick = { onViewDeliveryDetail(item.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        FilterDrawer(
            state = filterDrawerState,
            sections = filterSections,
            onApply = { updatedSections ->
                val status = updatedSections.find { it.title == "Status" }
                    ?.options?.find { it.isSelected }?.id
                viewModel.onAllCourierStatusFilterChange(status)
            },
            onClearAll = {
                viewModel.clearAllCourierFilters()
            }
        )
    }
}

/**
 * Courier Card matching Screenshot 1 visual layout using DataCard
 */
@Composable
private fun AllCodCard(
    item: AllCourierItemDto,
    formattedDate: String,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val isPending = item.status.equals("Pending", ignoreCase = true)
    val statusBg = if (isPending) primary_light else greenBg
    val statusText = if (isPending) Primary else greentext

    val actions = remember(item) {
        listOf(
            MenuAction(label = "View Details", onClick = onClick),
            MenuAction(label = "Track Order", onClick = {}),
            MenuAction(label = "Download Slip", onClick = {})
        )
    }

    DataCard(
        item = item,
        eyebrowText = item.orderCode,
        eyebrowColor = mutedText,
        title = item.courierName,
        titleFontWeight = FontWeight.SemiBold,
        titleColor = title_color,
        topBadgeText = item.status.toTitleCase(),
        topBadgeBgColor = statusBg,
        topBadgeTextColor = statusText,
        topBadgeShowDot = false,
        topBadgeInline = true,
        actions = actions,
        onClick = { onClick() },
        content = {
            // Light grey container with COD Amount and Delivery Date
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(Primary_background)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "COD Amount",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "${formatCurrency()}${formatIndianNumber(item.codAmount)}",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                    }

                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Delivery Date",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = formattedDate,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }
            }
        }
    )
}