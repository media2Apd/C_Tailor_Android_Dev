package com.cuso.tailor.view.home.security.audit_logs

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
import com.cuso.tailor.model.security.AuditLogItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun AuditLogsListScreen(
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val pagingState by viewModel.filteredAuditLogsPagingState.collectAsState()
    val errorMessage by viewModel.auditLogsError.collectAsState()
    val searchQuery by viewModel.auditLogSearchQuery.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchAuditLogs()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        // Page Header
        TitleBar(
            title = "Audit Logs",
            onClose = onNavigateBack
        )

        // Search Filter Bar
        SearchFilterBar(
            query = searchQuery,
            onQueryChange = { viewModel.onAuditLogSearchQueryChange(it) },
            placeholder = "Search user, module, action...",
            showFilterIcon = true,
            onFilterClick = {}
        )

        when {
            errorMessage != null && pagingState.items.isEmpty() && !pagingState.isInitialLoading -> {
                AppErrorState(
                    title = "Failed to load audit logs",
                    message = errorMessage?.ifBlank { "Unable to fetch audit logs." }
                        ?: "Unable to fetch audit logs.",
                    onRetry = { viewModel.fetchAuditLogs() }
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
                        text = "No audit logs found",
                        fontSize = tokens.bodyMedium,
                        color = mutedText
                    )
                }
            }

            else -> {
                PaginatedDataList(
                    state = pagingState,
                    onLoadMore = { viewModel.loadMoreAuditLogs() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { logItem ->
                    AuditLogCard(
                        log = logItem,
                        onClick = { onNavigateToDetail(logItem.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(
    log: AuditLogItemDto,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val (textColor, bgColor) = log.displayActionBadgeColorPair

    val actions = remember(log) {
        listOf(
            MenuAction(
                label = "View Detail",
                onClick = onClick
            )
        )
    }

    val cardTitle = remember(log) {
        val name = log.recordName as String?
        val desc = log.description as String?
        when {
            !name.isNullOrBlank() -> name
            !desc.isNullOrBlank() -> if (desc.length > 30) desc.substring(0, 30) else desc
            else -> "Audit Log"
        }
    }

    val safeIp = remember(log.ipAddress) {
        val ip = log.ipAddress as String?
        if (ip != null && ip.trim().isNotEmpty()) {
            if (ip.length > 15) ip.substring(ip.length - 15) else ip
        } else {
            "::1"
        }
    }

    DataCard(
        item = log,
        title = cardTitle,
        topBadgeText = (log.action as String?) ?: "LOG",
        topBadgeTextColor = textColor,
        topBadgeBgColor = bgColor,
        topBadgeInline = true,
        actions = actions,
        onClick = { onClick() },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
            ) {
                // Member Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PERFORMED BY",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Text(
                        text = log.member?.name ?: "System",
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                            Text("Module", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                            Text(
                                text = (log.module as String?) ?: "—",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Action", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                            Text(
                                text = (log.action as String?) ?: "—",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("IP Address", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                            Text(
                                text = safeIp,
                                fontSize = tokens.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Timestamp Caption
                Text(
                    text = "Timestamp : ${formatLogDate(log.createdAt)}",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    )
}

private fun formatLogDate(isoString: Any?): String {
    val str = isoString as? String
    if (str == null || str.trim().isEmpty()) return "—"
    return try {
        val clean = if (str.length >= 19) str.substring(0, 19) else str
        clean.replace("T", " ")
    } catch (_: Exception) {
        "—"
    }
}