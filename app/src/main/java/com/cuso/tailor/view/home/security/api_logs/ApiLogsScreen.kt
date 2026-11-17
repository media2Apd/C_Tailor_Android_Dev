package com.cuso.tailor.view.home.security.api_logs

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
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.ApiLogItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun ApiLogsListScreen(
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val pagingState by viewModel.filteredApiLogsPagingState.collectAsState()
    val errorMessage by viewModel.apiLogsError.collectAsState()
    val searchQuery by viewModel.apiLogSearchQuery.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchApiLogs()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Page Header
        TitleBar(
            title = "API Logs",
            onClose = onNavigateBack
        )

        // Search Filter Bar
        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { viewModel.onApiLogSearchQueryChange(it) },
            placeholder = "Search endpoint, method, log ID...",
            showFilterIcon = true,
            onFilterClick = {}
        )

        when {
            errorMessage != null && pagingState.items.isEmpty() && !pagingState.isInitialLoading -> {
                AppErrorState(
                    title = "Failed to load API logs",
                    message = errorMessage?.ifBlank { "Unable to fetch API logs." }
                        ?: "Unable to fetch API logs.",
                    onRetry = { viewModel.fetchApiLogs() }
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
                        text = "No API logs found",
                        fontSize = tokens.bodyMedium,
                        color = mutedText
                    )
                }
            }

            else -> {
                PaginatedDataList(
                    state = pagingState,
                    onLoadMore = { viewModel.loadMoreApiLogs() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { logItem ->
                    ApiLogItemCard(
                        log = logItem,
                        onClick = { onNavigateToDetail(logItem.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiLogItemCard(
    log: ApiLogItemDto,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val actions = remember(log) {
        listOf(
            MenuAction(
                label = "View Detail",
                onClick = onClick
            )
        )
    }

    DataCard(
        item = log,
        title = log.integration?.name ?: "Ecommerce Platform",
        topBadgeText = log.displayStatus,
        topBadgeTextColor = if (log.isSuccess) greentext else redText,
        topBadgeBgColor = if (log.isSuccess) greenBg else redBg,
        topBadgeInline = true,
        actions = actions,
        onClick = { onClick() },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
            ) {
                // Timestamp Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIMESTAMP",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Text(
                        text = formatLogDateTime(log.timestamp ?: log.createdAt),
                        fontSize = tokens.bodySmall,
                        color = TextPrimary
                    )
                }

                // Middle Summary Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(cardBgLight)
                        .padding(
                            horizontal = tokens.cardPadding * 0.7f,
                            vertical = tokens.extraPadding
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Endpoint", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                            Text(
                                text = log.shortEndpoint,
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Method", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                            Text(log.method, fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("CODE", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                            Text(log.statusCode.toString(), fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Response Time
                Text(
                    text = "Response Time : ${log.responseTimeMs}ms",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    )
}

private fun formatLogDateTime(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "2025-08-21 14:32:18"
    return try {
        isoString.take(19).replace("T", " ")
    } catch (_: Exception) {
        "2025-08-21 14:32:18"
    }
}