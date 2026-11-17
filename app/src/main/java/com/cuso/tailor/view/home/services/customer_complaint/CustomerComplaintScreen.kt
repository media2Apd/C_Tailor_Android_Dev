@file:Suppress("unused")

package com.cuso.tailor.view.home.services.customer_complaint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.ComplaintItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.PaginatedDataList
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.ServicesViewModel

@Composable
fun ComplaintListScreen(
    viewModel: ServicesViewModel,
    onBack: () -> Unit,
    onComplaintClick: (String) -> Unit
) {
    val tokens: AppDesignTokens = LocalAppTokens.current
    val pagingState by viewModel.complaintsPagingState.collectAsState()
    val searchQuery by viewModel.complaintSearchQuery.collectAsState()
    val errorMessage by viewModel.complaintsError.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchComplaints()
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = "Complaint List",
                onClose = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
                .padding(padding)
        ) {
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.onComplaintSearch(it) },
                placeholder = "Search SO number, customer...",
                showFilterIcon = true,
                onFilterClick = { /* Open filter sheet */ }
            )

            when {
                !errorMessage.isNullOrBlank() && pagingState.items.isEmpty() && !pagingState.isInitialLoading -> {
                    AppErrorState(
                        title = "Failed to load complaints",
                        message = errorMessage ?: "Error loading complaints",
                        onRetry = { viewModel.fetchComplaints(searchQuery.ifBlank { null }) }
                    )
                }

                !pagingState.isInitialLoading && pagingState.items.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No complaints found",
                            color = mutedText,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                else -> {
                    PaginatedDataList(
                        state = pagingState,
                        onLoadMore = { viewModel.loadMoreComplaints() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { item ->
                        ComplaintDataCardItem(
                            item = item,
                            onClick = { onComplaintClick(item.id) }
                        )
                        HorizontalDivider(
                            color = dividerColor,
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reuses the DataCard component configured for the Complaint item layout.
 */
@Composable
private fun ComplaintDataCardItem(
    item: ComplaintItemDto,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val rawStatus = item.status?.lowercase() ?: "in progress"
    val (statusLabel, statusBg, statusText) = when {
        rawStatus.contains("resolve") || rawStatus.contains("close") -> {
            Triple("Resolved", greenBg, greentext)
        }
        rawStatus.contains("reject") || rawStatus.contains("cancel") -> {
            Triple("Cancelled", redBg, redText)
        }
        else -> {
            Triple("In Progress", yellowBg, yellowText)
        }
    }

    DataCard(
        item = item,
        onClick = { onClick() },
        showDivider = false,
        title = item.customerDisplayName,
        titleColor = title_color,
        titleFontWeight = FontWeight.Medium,
        subtitle = item.displayCode,
        topBadgeText = statusLabel,
        topBadgeBgColor = statusBg,
        topBadgeTextColor = statusText,
        topBadgeShowDot = false,
        topBadgeInline = true,
        actions = listOf(
            MenuAction(
                label = "View Details",
                tint = headerGrey,
                textColor = TextPrimary,
                onClick = onClick
            )
        ),
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                    .background(Primary_background)
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Contact
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Contact",
                            fontSize = tokens.caption,
                            color = mutedText,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                        Text(
                            text = item.contactPhone,
                            fontSize = tokens.bodySmall,
                            color = title_color,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Category
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text(
                            text = "Category",
                            fontSize = tokens.caption,
                            color = mutedText,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                        Text(
                            text = item.categoryDisplay,
                            fontSize = tokens.bodySmall,
                            color = title_color,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Complaint
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Complaint",
                            fontSize = tokens.caption,
                            color = mutedText,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                        Text(
                            text = item.complaintDisplay,
                            fontSize = tokens.bodySmall,
                            color = title_color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    )
}