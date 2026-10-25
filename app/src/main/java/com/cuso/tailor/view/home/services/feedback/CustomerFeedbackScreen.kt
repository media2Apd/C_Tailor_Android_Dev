
@file:Suppress("unused")

package com.cuso.tailor.view.home.services.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.service.FeedbackItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.MenuAction
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
    val feedbacks by viewModel.feedbacksList.collectAsState()
    val searchQuery by viewModel.feedbackSearchQuery.collectAsState()
    val isLoading by viewModel.isLoadingFeedbacks.collectAsState()
    val errorMessage by viewModel.feedbacksError.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchFeedbacks()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            // 1. Reusable TitleBar (Close click triggers onBack)
            TitleBar(
                title = "Customer Feedback",
                onClose = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 2. Reusable SearchFilterBar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.onFeedbackSearch(it) },
                placeholder = "Search feedback, customer, order...",
                showFilterIcon = true,
                onFilterClick = { /* Open filter drawer/sheet */ }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (!errorMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Error loading feedback",
                        color = redText,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (feedbacks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No feedback records found",
                        color = mutedText,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = tokens.extraPadding * 0.5f)
                ) {
                    items(feedbacks) { item ->
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
 * Renders an individual Feedback card using the reusable DataCard
 */
@Composable
private fun FeedbackDataCardItem(
    item: FeedbackItemDto,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // NPS pill color strictly mapped to Colors.kt
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
        // Header title: Customer Name
        title = item.customerDisplayName,
        titleColor = title_color,
        titleFontWeight = FontWeight.Medium,
        // Subtitle: Feedback Code
        subtitle = item.displayCode,
        // Top right: NPS category badge & overflow menu
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
        // Content: Review title quote & 3-column stats box
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)) {
                // Review Title Quote
                if (!item.reviewTitle.isNullOrBlank()) {
                    Text(
                        text = "\"${item.reviewTitle}\"",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = textSubdued
                    )
                }

                // 3-Column Box (Rating | Channel | Order)
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
                            Spacer(Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = chartAmber,
                                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                                )
                                Spacer(Modifier.width(3.dp))
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
                            Spacer(Modifier.height(3.dp))
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
                            Spacer(Modifier.height(3.dp))
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