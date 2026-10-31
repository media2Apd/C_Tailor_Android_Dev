package com.cuso.tailor.view.home.it.all_devices

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.it.DeviceItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.ItViewModel
import androidx.compose.ui.platform.LocalContext
import com.cuso.tailor.utils.DeviceExcelExporter
import com.cuso.tailor.utils.DynamicIslandManager

@Composable
fun AllDeviceScreen(
    viewModel: ItViewModel,
    onBack: () -> Unit,
    onDeviceClick: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    val devices by viewModel.filteredDevices.collectAsState()
    val metrics by viewModel.deviceMetrics.collectAsState()
    val searchQuery by viewModel.deviceSearchQuery.collectAsState()
    val isLoading by viewModel.isLoadingDevices.collectAsState()
    val errorMessage by viewModel.devicesError.collectAsState()
    val selectedStatus by viewModel.selectedDeviceStatus.collectAsState()

    val filterDrawerState = rememberFilterDrawerState()

    LaunchedEffect(Unit) {
        viewModel.fetchDevicesList()
    }

    val activeFilterCount = remember(selectedStatus) {
        if (!selectedStatus.isNullOrBlank()) 1 else 0
    }

    val filterSections = remember(selectedStatus) {
        listOf(
            FilterSection(
                title = "Status",
                options = listOf(
                    FilterOption("Online", "Online", selectedStatus.equals("Online", ignoreCase = true)),
                    FilterOption("Offline", "Offline", selectedStatus.equals("Offline", ignoreCase = true)),
                    FilterOption("Maintenance", "Maintenance", selectedStatus.equals("Maintenance", ignoreCase = true)),
                    FilterOption("Warning", "Warning", selectedStatus.equals("Warning", ignoreCase = true))
                ),
                isMultiSelect = false
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0,0,0,0),
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = "All Devices",
                    onClose = onBack,
                    trailingContent = {
                        ExportButton(
                            onClick = {
                                if (devices.isEmpty()) {
                                    DynamicIslandManager.showError("No devices available to export")
                                } else {
                                    val success = DeviceExcelExporter.exportDevicesToCsv(
                                        context = context,
                                        devices = devices
                                    )
                                    if (success) {
                                        DynamicIslandManager.showSuccess("Exported to Downloads folder successfully")
                                    } else {
                                        DynamicIslandManager.showError("Failed to export devices")
                                    }
                                }
                            }
                        )
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search & Filter Bar
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = viewModel::onDeviceSearchQueryChange,
                    placeholder = "Search SO number, customer...",
                    filterCount = activeFilterCount,
                    onFilterClick = { filterDrawerState.open() },
                    accentColor = Primary,
                    borderColor = BorderGray,
                    height = tokens.fieldHeight
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // ─── Metric Cards Grid matching Screenshot 1 ───
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Row 1: TOTAL DEVICES | ONLINE
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DeviceStatCard(
                                    label = "TOTAL DEVICES",
                                    value = "${metrics?.totalDevices ?: 0}",
                                    valueColor = Primary,
                                    badgeText = null,
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                                DeviceStatCard(
                                    label = "ONLINE",
                                    value = "${metrics?.online?.count ?: 0}",
                                    valueColor = greentext,
                                    badgeText = metrics?.online?.percentage,
                                    badgeBg = greenBg,
                                    badgeTextColor = greentext,
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                            }

                            // Row 2: OFFLINE | MAINTENANCE
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                DeviceStatCard(
                                    label = "OFFLINE",
                                    value = "${metrics?.offline?.count ?: 0}",
                                    valueColor = title_color,
                                    badgeText = metrics?.offline?.percentage,
                                    badgeBg = redBg,
                                    badgeTextColor = redText,
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                                DeviceStatCard(
                                    label = "MAINTENANCE",
                                    value = "${metrics?.maintenance?.count ?: 0}",
                                    valueColor = Primary,
                                    badgeText = metrics?.maintenance?.percentage,
                                    badgeBg = primary_light,
                                    badgeTextColor = Primary,
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                            }

                            // Row 3: WARNING (Full Width)
                            DeviceStatCard(
                                label = "WARNING",
                                value = "${metrics?.warning?.count ?: 0}",
                                valueColor = redText,
                                badgeText = metrics?.warning?.percentage,
                                badgeBg = orangeBg,
                                badgeTextColor = orangeText,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item { Spacer(Modifier.height(4.dp)) }

                    // ─── Device List Content ───
                    when {
                        isLoading && devices.isEmpty() -> {
                            item {
                                ListSkeleton()
                            }
                        }

                        errorMessage != null && devices.isEmpty() -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = errorMessage ?: "Error", color = redText, fontSize = 13.sp)
                                        Spacer(Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.fetchDevicesList() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                        ) {
                                            Text("Retry", color = whiteBg)
                                        }
                                    }
                                }
                            }
                        }

                        devices.isEmpty() -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No devices found", color = mutedText, fontSize = 14.sp)
                                }
                            }
                        }

                        else -> {
                            items(
                                items = devices,
                                key = { it.effectiveId }
                            ) { device ->
                                DeviceDataCard(
                                    device = device,
                                    onClick = { onDeviceClick(device.effectiveId) }
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }

        // Side Filter Drawer
        FilterDrawer(
            state = filterDrawerState,
            sections = filterSections,
            onApply = { updatedSections ->
                val status = updatedSections.find { it.title == "Status" }
                    ?.options?.find { it.isSelected }?.id
                viewModel.onDeviceStatusFilterChange(status)
            },
            onClearAll = {
                viewModel.clearDeviceFilters()
            }
        )
    }
}

/**
 * Metric stat card matching Screenshot 1 (Total Devices, Online, Offline, etc.).
 */
@Composable
private fun DeviceStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color,
    badgeText: String? = null,
    badgeBg: Color = greenBg,
    badgeTextColor: Color = greentext
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )

                if (!badgeText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = badgeTextColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Device Item Card matching Screenshot 1 visual layout using DataCard.
 */
@Composable
private fun DeviceDataCard(
    device: DeviceItemDto,
    onClick: () -> Unit
) {
    val isOnline = device.status.equals("Online", ignoreCase = true)
    val statusBg = if (isOnline) greenBg else redBg
    val statusText = if (isOnline) greentext else redText

    val actions = remember(device) {
        listOf(
            MenuAction(label = "View Details", onClick = onClick),
            MenuAction(label = "Edit Device", onClick = {}),
            MenuAction(label = "Assign", onClick = {})
        )
    }

    DataCard(
        item = device,
        title = device.deviceType,
        code = device.serialNumber.ifBlank { null },
        titleFontWeight = FontWeight.Bold,
        titleColor = title_color,
        topBadgeText = device.status.toTitleCase(),
        topBadgeBgColor = statusBg,
        topBadgeTextColor = statusText,
        topBadgeShowDot = true,
        topBadgeInline = true,
        actions = actions,
        onClick = { onClick() },
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Branch Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Branch", fontSize = 12.sp, color = mutedText)
                    Text(
                        text = device.branchName ?: "--",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }

                // Assigned To
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Assigned To", fontSize = 12.sp, color = mutedText)
                    Text(
                        text = device.assignedTo?.name ?: "Unassigned",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }

                // Inner specs container: Device | Device No | Serial Number
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(cardBgLight)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(text = "Device", fontSize = 10.sp, color = mutedText)
                            Text(
                                text = device.deviceName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(text = "Device No", fontSize = 10.sp, color = mutedText)
                            Text(
                                text = device.deviceNo.ifBlank { "--" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(text = "Serial Number", fontSize = 10.sp, color = mutedText)
                            Text(
                                text = device.serialNumber.ifBlank { "--" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }
                    }
                }

                // Footer: Last Seen | Maintenance
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last Seen : ${device.lastSeenFormatted ?: "Just now"}",
                        fontSize = 11.sp,
                        color = mutedText
                    )
                    Text(
                        text = "Maintenance : ${device.maintenanceStatus}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (device.maintenanceStatus.equals("Overdue", ignoreCase = true)) redText else title_color
                    )
                }
            }
        }
    )
}