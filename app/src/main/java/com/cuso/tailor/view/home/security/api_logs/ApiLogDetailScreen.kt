package com.cuso.tailor.view.home.security.api_logs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.ApiLogItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun ApiLogDetailScreen(
    logId: String,
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val logDetail by viewModel.selectedApiLogDetail.collectAsState()
    val isLoading by viewModel.isLoadingApiLogDetail.collectAsState()

    LaunchedEffect(logId) {
        viewModel.fetchApiLogDetail(logId)
    }

    Scaffold(
        containerColor = Primary_background,
        topBar = {
            TitleBar(
                title = "Log Detail",
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
                    text = "Log detail not found",
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
                // Request Summary Card matching your screenshot
                RequestSummaryCard(log = log)
            }
        }
    }
}

@Composable
private fun RequestSummaryCard(log: ApiLogItemDto) {
    val tokens = LocalAppTokens.current

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
            // Header: Request Summary
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Request Summary",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
            }

            HorizontalDivider(color = grey_border)

            // 1. Timestamp
            LogDetailRow(
                label = "Timestamp",
                valueContent = {
                    Text(
                        text = formatDetailDateTime(log.timestamp ?: log.createdAt),
                        fontSize = tokens.bodySmall,
                        color = title_color
                    )
                }
            )

            HorizontalDivider(color = grey_border)

            // 2. Integration
            LogDetailRow(
                label = "Integration",
                valueContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Primary)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = log.integration?.name ?: "Ecommerce Platform",
                            fontSize = tokens.bodySmall,
                            color = title_color
                        )
                    }
                }
            )

            HorizontalDivider(color = grey_border)

            // 3. Endpoint
            LogDetailRow(
                label = "Endpoint",
                valueContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(cardBgLight)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = log.shortEndpoint,
                            fontSize = tokens.bodySmall,
                            color = title_color
                        )
                    }
                }
            )

            HorizontalDivider(color = grey_border)

            // 4. HTTP Method
            LogDetailRow(
                label = "HTTP Method",
                valueContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(primary_light)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = log.method,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }
            )

            HorizontalDivider(color = grey_border)

            // 5. Status
            LogDetailRow(
                label = "Status",
                valueContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (log.isSuccess) complete_button_bg else redText)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = log.displayStatus,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (log.isSuccess) complete_button_bg else redText
                        )
                    }
                }
            )

            HorizontalDivider(color = grey_border)

            // 6. Response Code
            LogDetailRow(
                label = "Response Code",
                valueContent = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .background(cardBgLight)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = log.statusCode.toString(),
                            fontSize = tokens.bodySmall,
                            color = title_color
                        )
                    }
                }
            )

            HorizontalDivider(color = grey_border)

            // 7. Processing Time
            LogDetailRow(
                label = "Processing Time",
                valueContent = {
                    Text(
                        text = "${log.responseTimeMs}ms",
                        fontSize = tokens.bodySmall,
                        color = title_color
                    )
                }
            )
        }
    }
}

@Composable
private fun LogDetailRow(
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

private fun formatDetailDateTime(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "2025-08-21 14:31:05"
    return try {
        isoString.take(19).replace("T", " ")
    } catch (_: Exception) {
        "2025-08-21 14:31:05"
    }
}