package com.cuso.tailor.view.home.security.api_logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    val logs by viewModel.filteredApiLogs.collectAsState()
    val isLoading by viewModel.isLoadingApiLogs.collectAsState()
    val searchQuery by viewModel.apiLogSearchQuery.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchApiLogs()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Title Bar
        TitleBar(
            title = "API Logs",
            onClose = onNavigateBack
        )

        // Search Filter Bar
        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { viewModel.onApiLogSearchQueryChange(it) },
            placeholder = "Search SO number, customer...",
            showFilterIcon = true,
            onFilterClick = {}
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
            }
        } else if (logs.isEmpty()) {
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    horizontal = tokens.screenPadding,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(logs, key = { it.id }) { logItem ->
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
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

                // Middle Container Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(cardBgLight)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Endpoint", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(2.dp))
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
                            Spacer(Modifier.height(2.dp))
                            Text(log.method, fontSize = tokens.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("CODE", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(2.dp))
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