@file:Suppress("unused", "UnusedVariable", "AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.security.api_integration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.ApiIntegrationItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun ApiIntegrationListScreen(
    viewModel: SecurityViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val integrations by viewModel.filteredIntegrations.collectAsState()
    val isLoading by viewModel.isLoadingIntegrations.collectAsState()
    val isRevoking by viewModel.isRevokingIntegration.collectAsState()
    val searchQuery by viewModel.integrationSearchQuery.collectAsState()

    var itemToRevoke by remember { mutableStateOf<ApiIntegrationItemDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchApiIntegrations()
    }

    FabScaffold(
        modifier = Modifier.fillMaxSize(),
        fab = FabConfig(
            label = "Add Integration",
            icon = Icons.Default.Add,
            onClick = onNavigateToAdd
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Primary_background)
        ) {
            // Title Header
            TitleBar(
                title = "API Integration",
                onClose = onNavigateBack
            )

            // Search Filter Bar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.onIntegrationSearchQueryChange(it) },
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
            } else if (integrations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No API integrations found",
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
                    items(integrations, key = { it.id }) { item ->
                        IntegrationListItemCard(
                            item = item,
                            onClick = { onNavigateToDetail(item.id) },
                            onEditClick = { onNavigateToEdit(item.id) },
                            onToggleStatus = {
                                viewModel.toggleApiIntegrationStatus(
                                    integrationId = item.id,
                                    onSuccess = { msg -> DynamicIslandManager.showSuccess(msg) },
                                    onError = { err -> DynamicIslandManager.showError(err) }
                                )
                            },
                            onRevokeClick = {
                                itemToRevoke = item
                            }
                        )
                    }
                }
            }
        }
    }

    // Revoke Confirmation Dialog
    // AFTER: Uses reusable DeleteModel component
    if (itemToRevoke != null) {
        DeleteModel(
            title = "Revoke Integration",
            message = "Are you sure you want to revoke \"${itemToRevoke!!.name}\"? All requests using these credentials will be permanently rejected.",
            checkboxText = "I confirm this revocation",
            cancelText = "No, Keep It",
            confirmText = "Yes, Revoke!",
            onDismiss = { itemToRevoke = null },
            onDelete = {
                viewModel.revokeApiIntegration(
                    integrationId = itemToRevoke!!.id,
                    onSuccess = { msg ->
                        DynamicIslandManager.showSuccess(msg)
                        itemToRevoke = null
                    },
                    onError = { err ->
                        DynamicIslandManager.showError(err)
                        itemToRevoke = null
                    }
                )
            }
        )
    }
}

@Composable
private fun IntegrationListItemCard(
    item: ApiIntegrationItemDto,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onRevokeClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val isConnected = item.status.equals("Connected", ignoreCase = true) || item.status.equals("Active", ignoreCase = true)

    // Dynamic actions list based on current status
    val actions = remember(item, isConnected) {
        listOf(
            MenuAction(
                label = "View Details",
                icon = Icons.Default.Visibility,
                onClick = onClick
            ),
            MenuAction(
                label = "Edit",
                icon = Icons.Default.Edit,
                onClick = onEditClick
            ),
            MenuAction(
                label = if (isConnected) "Disable" else "Enable",
                icon = if (isConnected) Icons.Default.PowerSettingsNew else Icons.Default.CheckCircle,
                textColor = if (isConnected) yellowText else greentext,
                tint = if (isConnected) yellowText else greentext,
                onClick = onToggleStatus
            ),
            MenuAction(
                label = "Revoke",
                icon = Icons.Default.Delete,
                textColor = redText,
                tint = redText,
                onClick = onRevokeClick
            )
        )
    }

    DataCard(
        item = item,
        title = item.name,
        topBadgeText = item.status,
        topBadgeTextColor = if (isConnected) greentext else redText,
        topBadgeBgColor = if (isConnected) greenBg else redBg,
        topBadgeInline = true,
        actions = actions,
        onClick = { onClick() },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // System Info Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SYSTEM",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = mutedText
                    )
                    Text(
                        text = "Ecommerce",
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                            Text("Type", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(2.dp))
                            Text(item.category, fontSize = tokens.bodySmall, color = TextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("API Version", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(2.dp))
                            Text("v1", fontSize = tokens.bodySmall, color = TextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("ENVIRONMENT", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(2.dp))
                            Text("Production", fontSize = tokens.bodySmall, color = TextPrimary)
                        }
                    }
                }

                // Last Sync caption
                Text(
                    text = "Last Sync : 2 Mins Ago",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
    )
}