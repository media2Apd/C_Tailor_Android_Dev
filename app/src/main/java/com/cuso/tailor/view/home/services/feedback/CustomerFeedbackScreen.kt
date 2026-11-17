@file:Suppress("unused")

package com.cuso.tailor.view.home.services.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.FeedbackItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.PaginatedDataList
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.ServicesViewModel

@Composable
fun CustomerFeedbackScreen(
    viewModel: ServicesViewModel,
    onBack: () -> Unit,
    onFeedbackClick: (String) -> Unit
) {
    val tokens: AppDesignTokens = LocalAppTokens.current
    val pagingState by viewModel.feedbacksPagingState.collectAsState()
    val searchQuery by viewModel.feedbackSearchQuery.collectAsState()
    val errorMessage by viewModel.feedbacksError.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchFeedbacks()
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = "Customer Feedback",
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
                onQueryChange = { viewModel.onFeedbackSearch(it) },
                placeholder = "Search feedback, customer, order...",
                showFilterIcon = true,
                onFilterClick = { /* Open filter drawer/sheet */ }
            )

            when {
                !errorMessage.isNullOrBlank() && pagingState.items.isEmpty() && !pagingState.isInitialLoading -> {
                    AppErrorState(
                        title = "Failed to load feedback",
                        message = errorMessage ?: "Error loading feedback",
                        onRetry = { viewModel.fetchFeedbacks(searchQuery.ifBlank { null }) }
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
                            text = "No feedback records found",
                            color = mutedText,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                else -> {
                    PaginatedDataList(
                        state = pagingState,
                        onLoadMore = { viewModel.loadMoreFeedbacks() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { item ->
                        FeedbackDataCardItem(
                            item = item,
                            onClick = { onFeedbackClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders an individual Feedback card using the reusable DataCard.
 */
@Composable
private fun FeedbackDataCardItem(
    item: FeedbackItemDto,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val (npsLabel, npsBg, npsText) = when (item.npsCategory?.lowercase()) {
        "promoter" -> Triple("Promoter", greenBg, greentext)
        "passive" -> Triple("Passive", yellowBg, yellowText)
        "detractor" -> Triple("Detractor", redBg, redText)
        else -> Triple(item.npsCategory ?: "Feedback", background_light_purple, BluePrimary)
    }

    DataCard(
        item = item,
        onClick = { onClick() },
        showDivider = false,
        title = item.customerDisplayName,
        titleColor = title_color,
        titleFontWeight = FontWeight.Medium,
        subtitle = item.displayCode,
        topBadgeText = npsLabel,
        topBadgeBgColor = npsBg,
        topBadgeTextColor = npsText,
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
            Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)) {
                if (!item.reviewTitle.isNullOrBlank()) {
                    Text(
                        text = "\"${item.reviewTitle}\"",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = textSubdued
                    )
                }

                // 3-Column Summary Box (Rating | Channel | Order ID)
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
                        // Rating Column
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Rating",
                                fontSize = tokens.caption,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = chartAmber,
                                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.3f))
                                Text(
                                    text = "${item.overallRating}/5",
                                    fontSize = tokens.bodySmall,
                                    color = title_color,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Channel Column
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text(
                                text = "Channel",
                                fontSize = tokens.caption,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                            Text(
                                text = item.channelDisplay,
                                fontSize = tokens.bodySmall,
                                color = title_color,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // Order Column
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Order ID",
                                fontSize = tokens.caption,
                                color = mutedText,
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.3f))
                            Text(
                                text = item.orderDisplay,
                                fontSize = tokens.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = title_color,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    )
}