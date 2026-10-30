package com.cuso.tailor.view.home.logistics.order_tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.logistics.TrackingEventItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FilterDrawer
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.rememberFilterDrawerState
import com.cuso.tailor.viewmodel.LogisticsViewModel

/**
 * Mobile Tracking Events Screen displaying desktop fields:
 * Shipment ID, Date, Location, Updated By, Status, and Tracking ID inside DataCard.
 * Strictly adheres to:
 * - Max font size = tokens.bodyMedium
 * - Max font weight = FontWeight.Medium
 * - Colors sourced strictly from com.cuso.tailor.ui.theme.*
 */
@Composable
fun TrackingEventsListScreen(
    viewModel: LogisticsViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onViewEventDetail: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val trackingEvents by viewModel.filteredTrackingEvents.collectAsState()
    val searchQuery by viewModel.trackingSearchQuery.collectAsState()
    val isLoading by viewModel.isLoadingTrackingEvents.collectAsState()
    val selectedStatus by viewModel.selectedTrackingStatus.collectAsState()

    val filterDrawerState = rememberFilterDrawerState()

    // Load events on screen enter
    LaunchedEffect(Unit) {
        viewModel.fetchTrackingEvents()
    }

    val filterSections = remember(selectedStatus) {
        listOf(
            FilterSection(
                title = "Status",
                options = listOf(
                    FilterOption("out_for_delivery", "Out_for_Delivery", selectedStatus?.equals("Out_for_Delivery", true) == true),
                    FilterOption("in_transit", "In_Transit", selectedStatus?.equals("In_Transit", true) == true),
                    FilterOption("delivered", "Delivered", selectedStatus?.equals("Delivered", true) == true),
                    FilterOption("created", "Created", selectedStatus?.equals("Created", true) == true)
                ),
                isMultiSelect = false
            )
        )
    }

    val activeFilterCount = if (!selectedStatus.isNullOrBlank()) 1 else 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = "Tracking Events",
                    subtitle = "Logistics • Order Tracking",
                    onClose = onNavigateBack
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Transparent)
            ) {
                // Reusable Search & Filter Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onTrackingSearchQueryChange(it) },
                    placeholder = "Search Shipment ID, Location, Tracking ID...",
                    showFilterIcon = true,
                    filterCount = activeFilterCount,
                    onFilterClick = { filterDrawerState.open() },
                    accentColor = Primary,
                    borderColor = BorderGray,
                    textSecondaryColor = mutedText,
                    height = tokens.fieldHeight
                )

                when {
                    isLoading && trackingEvents.isEmpty() -> {
                        ListSkeleton()
                    }

                    trackingEvents.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tracking events found",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = headerGrey
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = tokens.screenPadding,
                                vertical = 8.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = trackingEvents,
                                key = { it.id }
                            ) { item ->
                                TrackingEventCardItem(
                                    item = item,
                                    formattedDate = viewModel.formatTrackingDate(item.date),
                                    onViewDetailClick = { validId ->
                                        onViewEventDetail(validId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Reusable Filter Drawer
        FilterDrawer(
            state = filterDrawerState,
            title = "Filters",
            sections = filterSections,
            onApply = { updatedSections ->
                val statusOption = updatedSections
                    .find { it.title == "Status" }
                    ?.options
                    ?.find { it.isSelected }

                viewModel.onTrackingStatusFilterChange(statusOption?.label)
            },
            onClearAll = {
                viewModel.clearTrackingFilters()
            }
        )
    }
}

/**
 * Mobile DataCard mapping the 5 desktop columns:
 * Shipment ID (Primary title), Status Badge, Location, Updated By, Tracking ID, and Date.
 */
@Composable
private fun TrackingEventCardItem(
    item: TrackingEventItemDto,
    formattedDate: String,
    onViewDetailClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    // Resolve status color pairs from theme palette
    val (badgeBg, badgeText) = when (item.status.lowercase()) {
        "out_for_delivery", "out for delivery" -> orangeBg to orangeText
        "in_transit", "in transit" -> yellowBg to yellowText
        "delivered" -> greenBg to greentext
        else -> primary_light to Primary
    }

    val targetId = remember(item.id, item.shipmentId) {
        item.id.trim().ifBlank { item.shipmentId.trim() }
    }

    DataCard(
        item = item,
        modifier = modifier,
        title = item.shipmentId,
        titleColor = Primary, // Blue/Purple identifier color from desktop image
        titleFontWeight = FontWeight.Medium,
        topBadgeText = item.status.replace("_", " "),
        topBadgeInline = true,
        topBadgeShowDot = false,
        topBadgeBgColor = badgeBg,
        topBadgeTextColor = badgeText,
        topBadgeCornerRadius = tokens.cardCornerRadius,
        onClick = {
            if (targetId.isNotBlank()) {
                onViewDetailClick(targetId)
            }
        },
        actions = listOf(
            MenuAction(
                label = "View Details",
                textColor = title_color,
                onClick = {
                    if (targetId.isNotBlank()) {
                        onViewDetailClick(targetId)
                    }
                }
            )
        ),
        content = {
            // Middle Container holding Location, Updated By, and Tracking ID
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(PanelBg)
                    .border(
                        width = 1.dp,
                        color = sectionBorder,
                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                    )
                    .padding(
                        horizontal = tokens.extraPadding,
                        vertical = tokens.extraPadding
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column 1: Location
                    Column(
                        modifier = Modifier.weight(1.1f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                    ) {
                        Text(
                            text = "Location",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.location.ifBlank { "Primary Facility" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Column 2: Updated By
                    Column(
                        modifier = Modifier.weight(0.9f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                    ) {
                        Text(
                            text = "Updated By",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.updatedBy.ifBlank { "Admin" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Column 3: Tracking ID
                    Column(
                        modifier = Modifier.weight(1.2f),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.25f)
                    ) {
                        Text(
                            text = "Tracking ID",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Normal,
                            color = headerGrey,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.trackingId ?: "—",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(tokens.extraPadding * 0.7f))

            // Bottom Row: Date display and Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = headerGrey,
                        modifier = Modifier.size(tokens.iconSize * 0.8f)
                    )
                    Text(
                        text = "Date: ",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                    Text(
                        text = formattedDate,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = {
                        if (targetId.isNotBlank()) {
                            onViewDetailClick(targetId)
                        }
                    },
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = whiteBg,
                        disabledContainerColor = disabled,
                        disabledContentColor = whiteBg
                    ),
                    contentPadding = PaddingValues(
                        horizontal = tokens.extraPadding * 1.4f,
                        vertical = 0.dp
                    ),
                    modifier = Modifier.height(tokens.buttonHeight * 0.85f)
                ) {
                    Text(
                        text = "View Status",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }
        }
    )
}