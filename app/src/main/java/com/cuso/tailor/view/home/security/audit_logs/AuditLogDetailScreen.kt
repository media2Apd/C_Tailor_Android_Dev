package com.cuso.tailor.view.home.security.audit_logs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.AuditLogItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun AuditLogDetailScreen(
    logId: String,
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val logDetail by viewModel.selectedAuditLogDetail.collectAsState()
    val isLoading by viewModel.isLoadingAuditLogDetail.collectAsState()

    LaunchedEffect(logId) {
        viewModel.fetchAuditLogDetail(logId)
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = "Audit Log Detail",
                onClose = onNavigateBack
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary, strokeWidth = 2.dp)
            }
        } else if (logDetail == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Audit log detail not found",
                    fontSize = tokens.bodyMedium,
                    color = mutedText
                )
            }
        } else {
            val log = logDetail!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AuditSummaryCard(log = log)
            }
        }
    }
}

@Composable
private fun AuditSummaryCard(log: AuditLogItemDto) {
    val tokens = LocalAppTokens.current
    val (actionTextColor, actionBgColor) = log.displayActionBadgeColorPair

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Audit Summary",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
            }

            HorizontalDivider(color = grey_border)

            // Timestamp
            AuditDetailRow(
                label = "Timestamp",
                valueContent = {
                    Text(
                        text = formatAuditDetailDateTime(log.createdAt),
                        fontSize = tokens.bodySmall,
                        color = title_color
                    )
                }
            )

            HorizontalDivider(color = grey_border)

            // Performed By Member
            AuditDetailRow(
                label = "Member",
                valueContent = {
                    val memberName = log.member?.name?.takeIf { it.isNotBlank() } ?: "Unknown"
                    val memberId = log.member?.memberId?.takeIf { it.isNotBlank() } ?: "—"
                    Text(
                        text = "$memberName ($memberId)",
                        fontSize = tokens.bodySmall,
                        color = title_color,
                        fontWeight = FontWeight.Medium
                    )
                }
            )

            HorizontalDivider(color = grey_border)

            // Module
            AuditDetailRow(
                label = "Module",
                valueContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(cardBgLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = log.module.orEmpty().ifBlank { "—" },
                            fontSize = tokens.bodySmall,
                            color = title_color
                        )
                    }
                }
            )

            HorizontalDivider(color = grey_border)

            // Action
            AuditDetailRow(
                label = "Action",
                valueContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(actionBgColor)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = log.action.orEmpty().ifBlank { "—" },
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = actionTextColor
                        )
                    }
                }
            )

            HorizontalDivider(color = grey_border)

            // Record Name
            AuditDetailRow(
                label = "Record Name",
                valueContent = {
                    val record = log.recordName.orEmpty().ifBlank { "—" }
                    Text(
                        text = record,
                        fontSize = tokens.bodySmall,
                        color = title_color,
                        fontWeight = FontWeight.Medium
                    )
                }
            )

            HorizontalDivider(color = grey_border)

            // Description
            AuditDetailRow(
                label = "Description",
                valueContent = {
                    val desc = log.description.orEmpty().ifBlank { "—" }
                    Text(
                        text = desc,
                        fontSize = tokens.bodySmall,
                        color = title_color
                    )
                }
            )

            HorizontalDivider(color = grey_border)
// IP Address
            AuditDetailRow(
                label = "IP Address",
                valueContent = {
                    val ip = log.ipAddress.orEmpty().ifBlank { "—" }

                    Text(
                        text = ip,
                        fontSize = tokens.bodySmall,
                        color = title_color
                    )
                }
            )
        }
    }
}

@Composable
private fun AuditDetailRow(
    label: String,
    valueContent: @Composable () -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = tokens.bodySmall,
            color = mutedText
        )
        valueContent()
    }
}

private fun formatAuditDetailDateTime(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "—"
    return try {
        isoString.take(19).replace("T", " ")
    } catch (_: Exception) {
        "—"
    }
}