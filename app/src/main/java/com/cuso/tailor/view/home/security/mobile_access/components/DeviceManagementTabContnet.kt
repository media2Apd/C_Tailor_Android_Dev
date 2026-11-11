package com.cuso.tailor.view.home.security.mobile_access.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.MobileDeviceItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun DeviceManagementTabContent(
    viewModel: SecurityViewModel,
    onDeviceClick: (MobileDeviceItem) -> Unit
) {
    val tokens = LocalAppTokens.current
    val pagingState by viewModel.mobileDevicesPagingState.collectAsState()
    val searchQuery by viewModel.mobileDeviceSearch.collectAsState()

    val filterDrawerState = rememberFilterDrawerState()
    var deviceToBlock by remember { mutableStateOf<MobileDeviceItem?>(null) }

    val filterSections = remember {
        listOf(
            FilterSection(
                title = "Platform",
                options = listOf(FilterOption("iOS", "iOS"), FilterOption("Android", "Android")),
                isMultiSelect = false
            ),
            FilterSection(
                title = "Status",
                options = listOf(
                    FilterOption("approved", "Approved"),
                    FilterOption("pending", "Pending"),
                    FilterOption("blocked", "Blocked")
                ),
                isMultiSelect = false
            ),
            FilterSection(
                title = "Security",
                options = listOf(FilterOption("trusted", "Trusted"), FilterOption("untrusted", "Untrusted")),
                isMultiSelect = false
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { viewModel.setDeviceSearch(it) },
                placeholder = "Search SO number, customer...",
                showFilterIcon = true,
                filterCount = 0,
                onFilterClick = { filterDrawerState.open() }
            )

            if (!pagingState.isInitialLoading && pagingState.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No devices found", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Devices appear here when a user signs in from mobile app.", fontSize = tokens.caption, color = TextSecondary)
                    }
                }
            } else {
                PaginatedDataList(
                    state = pagingState,
                    onLoadMore = { viewModel.loadMoreMobileDevices() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding)
                ) { item ->
                    val canApprove = item.approvalStatus != "approved" && !item.isJailbrokenOrRooted
                    val canBlock = item.approvalStatus != "blocked"

                    val cardActions = mutableListOf<MenuAction>()
                    cardActions.add(MenuAction(label = "View", icon = Icons.Default.Visibility, onClick = { onDeviceClick(item) }))
                    if (canApprove) {
                        cardActions.add(MenuAction(label = "Approve", icon = Icons.Default.CheckCircle, onClick = { viewModel.approveMobileDevice(item.id) }))
                    }
                    if (canBlock) {
                        cardActions.add(MenuAction(label = "Block", icon = Icons.Default.Block, textColor = redText, tint = redText, onClick = { deviceToBlock = item }))
                    }

                    DataCard(
                        item = item,
                        title = item.deviceName,
                        topBadgeText = item.approvalStatus.replaceFirstChar { it.uppercase() },
                        topBadgeBgColor = if (item.approvalStatus == "approved") greenBg else redBg,
                        topBadgeTextColor = if (item.approvalStatus == "approved") greentext else redText,
                        actions = cardActions,
                        onClick = { onDeviceClick(item) },
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("USER", fontSize = tokens.label, color = mutedText)
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(item.userId?.fullName ?: "—", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        Text(item.userId?.role ?: "User", fontSize = tokens.caption, color = mutedText)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("PLATFORM", fontSize = tokens.label, color = mutedText)
                                    PlatformChip(item.platform)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(light_grey)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("VERSION (OS / APP)", fontSize = 10.sp, color = mutedText)
                                        Text("${item.osVersion} / ${item.appVersion}", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    }
                                    Column {
                                        Text("SECURITY", fontSize = 10.sp, color = mutedText)
                                        TrustBadge(item.trustStatus)
                                    }
                                }

                                Text(
                                    text = "LAST ACTIVE : ${item.lastActiveAt?.take(16)?.replace('T', ' ') ?: "Just now"}",
                                    fontSize = tokens.caption,
                                    color = mutedText
                                )
                            }
                        }
                    )
                }
            }
        }

        // Filter Sidebar
        FilterDrawer(
            state = filterDrawerState,
            title = "Filter Devices",
            sections = filterSections,
            onApply = { applied ->
                val platform = applied.find { it.title == "Platform" }?.options?.find { it.isSelected }?.id
                val approval = applied.find { it.title == "Status" }?.options?.find { it.isSelected }?.id
                val trust = applied.find { it.title == "Security" }?.options?.find { it.isSelected }?.id
                viewModel.applyDeviceFilters(platform, approval, trust)
            },
            onClearAll = { viewModel.applyDeviceFilters(null, null, null) }
        )

        // Confirm Block Modal
        deviceToBlock?.let { target ->
            AlertDialog(
                onDismissRequest = { deviceToBlock = null },
                title = { Text("Block Device", fontWeight = FontWeight.Bold, color = redText) },
                text = { Text("\"${target.deviceName}\" will be blocked and can no longer access the mobile ERP.", fontSize = tokens.bodySmall) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.blockMobileDevice(target.id) { deviceToBlock = null }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = redText)
                    ) { Text("Block Device", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { deviceToBlock = null }) { Text("Cancel", color = TextSecondary) }
                }
            )
        }
    }
}