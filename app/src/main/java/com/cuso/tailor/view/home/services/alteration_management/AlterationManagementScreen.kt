@file:Suppress("unused", "UNUSED_PARAMETER", "DEPRECATION", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.services.alteration_management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.AlterationItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.ServicesViewModel

@Composable
fun AlterationManagementScreen(
    viewModel: ServicesViewModel,
    onClose: () -> Unit,
    onItemClick: (AlterationItemDto) -> Unit,
    onCreateNewAlteration: () -> Unit,
    onBreadcrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val pagingState by viewModel.alterationsPagingState.collectAsState()
    val searchQuery by viewModel.alterationQuery.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAlterations()
    }

    Scaffold(
        topBar = {
            TitleBar(
                title = "Alteration Management",
                onClose = onClose
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Primary_background
    ) { padding ->
        FabScaffold(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            fab = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Primary_background)
            ) {
                // Search and Filter Row
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onAlterationSearch(it) },
                    placeholder = "Search Alteration...",
                    onFilterClick = { /* Handle Filter Dialog/Action */ }
                )

                if (!pagingState.isInitialLoading && pagingState.items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Alteration requests found",
                            fontSize = tokens.bodyMedium,
                            color = mutedText
                        )
                    }
                } else {
                    PaginatedDataList(
                        state = pagingState,
                        onLoadMore = { viewModel.loadMoreAlterations() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { item ->
                        AlterationDataCard(
                            item = item,
                            onClick = { onItemClick(item) },
                            onMenuClick = { /* Handle popup menu */ }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable card wrapping DataCard to render an alteration ticket item.
 */
@Composable
fun AlterationDataCard(
    item: AlterationItemDto,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val rawStatus = item.status?.lowercase() ?: "in progress"
    val (statusLabel, statusBg, statusText) = when {
        rawStatus.contains("pending") || rawStatus.contains("draft") -> {
            Triple("Pending", yellowBg, yellowText)
        }
        rawStatus.contains("completed") || rawStatus.contains("ready") -> {
            Triple("Completed", greenBg, greentext)
        }
        rawStatus.contains("cancel") || rawStatus.contains("reject") -> {
            Triple("Cancelled", redBg, redText)
        }
        else -> {
            Triple("In Progress", background_light_purple, BluePrimary)
        }
    }

    val typeText = if (item.isFreeCharge) "Free" else "Paid"

    DataCard(
        item = item,
        onClick = { onClick() },
        showDivider = false,
        eyebrowText = "${item.displayTicketCode}  •  Ticket ID",
        eyebrowColor = title_color,
        topBadgeText = statusLabel,
        topBadgeBgColor = statusBg,
        topBadgeTextColor = statusText,
        topBadgeShowDot = false,
        topBadgeInline = true,
        showActionsInHeader = true,
        actions = listOf(
            MenuAction(
                label = "View Details",
                tint = headerGrey,
                textColor = TextPrimary,
                onClick = onClick
            ),
            MenuAction(
                label = "Actions",
                tint = headerGrey,
                textColor = TextPrimary,
                onClick = onMenuClick
            )
        ),
        title = item.customerDisplayName,
        titleColor = textSubdued,
        titleFontWeight = FontWeight.Normal,
        subtitle = "Type: $typeText · Date: ${item.formattedDate}",
        footerAsRows = true,
        footerFields = listOf(
            DataCardField(
                label = "Order ID",
                text = item.orderIdDisplay,
                labelColor = iconMuted,
                textColor = textSubdued,
                asRow = true
            ),
            DataCardField(
                label = "Garment",
                text = item.garmentDisplay,
                labelColor = iconMuted,
                textColor = textSubdued,
                asRow = true
            ),
            DataCardField(
                label = "Tailor",
                text = item.tailorDisplay,
                labelColor = iconMuted,
                textColor = textSubdued,
                asRow = true
            )
        )
    )
}