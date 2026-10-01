package com.cuso.tailor.view.home.inventory.approval

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.ActionDropdownMenu
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay

@Composable
fun ApprovalsListScreen(
    modifier: Modifier = Modifier,
    viewModel: InventoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onItemClick: (String) -> Unit = {},
    onFilterClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    // Observe approvals list from ViewModel
    val approvalsList by viewModel.approvalsList.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingApprovals.collectAsStateWithLifecycle()
    val errorMessage by viewModel.approvalsError.collectAsStateWithLifecycle()

    // Debounced search trigger
    LaunchedEffect(searchQuery) {
        delay(300)
        viewModel.fetchApprovals(page = 1, search = searchQuery.trim().ifBlank { null })
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                TitleBar(title = "Approvals", onClose = onBack)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search & Filter header row
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search SO number, customer...",
                onFilterClick = onFilterClick,
                height = tokens.fieldHeight,
                accentColor = Primary,
                borderColor = BorderGray,
                textSecondaryColor = iconMuted
            )

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            // Content loading & list display
            when {
                isLoading && approvalsList.isEmpty() -> {
                    ListSkeleton()
                }
                errorMessage != null && approvalsList.isEmpty() -> {
                    AppErrorState(
                        title = "Failed to load approvals",
                        message = errorMessage ?: "Unexpected error",
                        onRetry = { viewModel.fetchApprovals(page = 1) }
                    )
                }
                approvalsList.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No approvals found.", fontSize = tokens.bodySmall, color = iconMuted)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(approvalsList, key = { it.id }) { item ->
                            val (badgeBg, badgeText) = when (item.status.lowercase()) {
                                "approve", "approved" -> greenBg to greentext
                                "rejected" -> redBg to redText
                                else -> orangeBg to orangeText
                            }

                            // Dynamic card actions: changes between "Inactive" and "Active" based on isActive
                            val cardActions = listOf(
                                MenuAction(
                                    label = "View Details",
                                    icon = Icons.Outlined.Visibility,
                                    onClick = { onItemClick(item.id) }
                                ),
                                MenuAction(
                                    label = if (item.isActive) "Inactive" else "Active",
                                    onClick = {
                                        viewModel.toggleAutoReorderStatus(id = item.id)
                                    }
                                )
                            )

                            DataCard(
                                item = item,
                                onClick = { onItemClick(item.id) },
                                headerContent = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column {
                                            Text(
                                                text = item.requestId,
                                                fontSize = tokens.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = BluePrimary
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = item.supplier,
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = title_color
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                StatusBadge(
                                                    text = item.status,
                                                    bgColor = badgeBg,
                                                    textColor = badgeText,
                                                    showDot = false,
                                                    cornerRadius = 14.dp
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                ActionDropdownMenu(
                                                    actions = cardActions,
                                                    icon = Icons.Default.MoreVert
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "₹${item.totalAmount.toInt()}",
                                                fontSize = tokens.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = title_color
                                            )
                                        }
                                    }
                                },
                                content = {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            ApprovalInfoColumn(
                                                label = "Items",
                                                value = "${item.itemCount} Items",
                                                tokens = tokens,
                                                modifier = Modifier.weight(1f)
                                            )
                                            ApprovalInfoColumn(
                                                label = "Requested By",
                                                value = item.requestedBy,
                                                tokens = tokens,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            ApprovalInfoColumn(
                                                label = "Date",
                                                value = formatIsoDate(item.date),
                                                tokens = tokens,
                                                modifier = Modifier.weight(1f)
                                            )
                                            ApprovalInfoColumn(
                                                label = "Last Updated",
                                                value = formatIsoDateTime(item.lastUpdated),
                                                tokens = tokens,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovalInfoColumn(
    label: String,
    value: String,
    tokens: AppDesignTokens,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = tokens.caption,
            color = dataCardField
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = title_color
        )
    }
}

fun formatIsoDate(iso: String): String {
    if (iso.length < 10) return iso
    val parts = iso.substring(0, 10).split("-")
    if (parts.size != 3) return iso
    val monthName = when (parts[1]) {
        "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"
        "05" -> "May"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Aug"
        "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; "12" -> "Dec"
        else -> parts[1]
    }
    return "${parts[2]} $monthName ${parts[0]}"
}

fun formatIsoDateTime(iso: String): String {
    if (iso.length < 16) return iso
    val date = formatIsoDate(iso)
    val time = iso.substring(11, 16)
    return "$date, $time"
}