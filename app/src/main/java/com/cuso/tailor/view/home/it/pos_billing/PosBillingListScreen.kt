package com.cuso.tailor.view.home.it.pos_billing

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
import com.cuso.tailor.model.it.PosTerminalItemDto
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.ItViewModel

@Composable
fun TerminalFleetStatusScreen(
    viewModel: ItViewModel,
    onBack: () -> Unit,
    onTerminalClick: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    val terminals by viewModel.filteredPosTerminals.collectAsState()
    val metrics by viewModel.posTerminalMetrics.collectAsState()
    val searchQuery by viewModel.posSearchQuery.collectAsState()
    val isLoading by viewModel.isLoadingPosTerminals.collectAsState()
    val errorMessage by viewModel.posTerminalsError.collectAsState()
    val selectedStatus by viewModel.selectedPosStatus.collectAsState()

    val filterDrawerState = rememberFilterDrawerState()

    LaunchedEffect(Unit) {
        viewModel.fetchPosTerminalsList()
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
                    FilterOption("Warning", "Warning", selectedStatus.equals("Warning", ignoreCase = true))
                ),
                isMultiSelect = false
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = "Terminal Fleet Status",
                    onClose = onBack,
                    trailingContent = {
                        ExportButton(
                            onClick = {}
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
                    onQueryChange = viewModel::onPosSearchQueryChange,
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
                    // ─── 6 Metric Cards matching Screenshot 1 ───
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Row 1: TOTAL TERMINALS | ONLINE
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TerminalStatCard(
                                    label = "TOTAL TERMINALS",
                                    value = "${metrics?.totalTerminals ?: 0}",
                                    valueColor = title_color,
                                    badgeText = null,
                                    modifier = Modifier.weight(1f)
                                )
                                TerminalStatCard(
                                    label = "ONLINE",
                                    value = "${metrics?.online?.count ?: 0}",
                                    valueColor = greentext,
                                    badgeText = metrics?.online?.percentage,
                                    badgeBg = greenBg,
                                    badgeTextColor = greentext,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Row 2: OFFLINE | PAYMENT GATEWAYS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TerminalStatCard(
                                    label = "OFFLINE",
                                    value = "${metrics?.offline ?: 0}",
                                    valueColor = title_color,
                                    badgeText = null,
                                    modifier = Modifier.weight(1f)
                                )
                                TerminalStatCard(
                                    label = "PAYMENT GATEWAYS",
                                    value = "${metrics?.paymentGateways?.count ?: 0}",
                                    valueColor = Primary,
                                    badgeText = metrics?.paymentGateways?.badge,
                                    badgeBg = primary_light,
                                    badgeTextColor = Primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Row 3: PRINTER ISSUES | TOTAL ACTIVE ISSUES
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TerminalStatCard(
                                    label = "PRINTER ISSUES",
                                    value = "${metrics?.printerIssues ?: 0}",
                                    valueColor = title_color,
                                    badgeText = null,
                                    modifier = Modifier.weight(1f)
                                )
                                TerminalStatCard(
                                    label = "TOTAL ACTIVE ISSUES",
                                    value = "${metrics?.totalActiveIssues ?: 0}",
                                    valueColor = title_color,
                                    badgeText = null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(6.dp)) }

                    // ─── Terminal List Content ───
                    when {
                        isLoading && terminals.isEmpty() -> {
                            item { ListSkeleton() }
                        }

                        errorMessage != null && terminals.isEmpty() -> {
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
                                            onClick = { viewModel.fetchPosTerminalsList() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                        ) {
                                            Text("Retry", color = whiteBg)
                                        }
                                    }
                                }
                            }
                        }

                        terminals.isEmpty() -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No terminals found", color = mutedText, fontSize = 14.sp)
                                }
                            }
                        }

                        else -> {
                            items(
                                items = terminals,
                                key = { it.effectiveId }
                            ) { terminal ->
                                TerminalDataCard(
                                    terminal = terminal,
                                    onClick = { onTerminalClick(terminal.effectiveId) }
                                )
                            }
                        }
                    }

                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }

        // Filter Drawer
        FilterDrawer(
            state = filterDrawerState,
            sections = filterSections,
            onApply = { updatedSections ->
                val status = updatedSections.find { it.title == "Status" }
                    ?.options?.find { it.isSelected }?.id
                viewModel.onPosStatusFilterChange(status)
            },
            onClearAll = {
                viewModel.clearPosFilters()
            }
        )
    }
}

/**
 * Metric Card matching Screenshot 1 2x3 grid.
 */
@Composable
private fun TerminalStatCard(
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
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
 * Terminal Card matching Screenshot 1 visual layout using DataCard.
 */
@Composable
private fun TerminalDataCard(
    terminal: PosTerminalItemDto,
    onClick: () -> Unit
) {
    val isOnline = terminal.status.equals("Online", ignoreCase = true)
    val isWarning = terminal.status.equals("Warning", ignoreCase = true)
    val statusBg = when {
        isOnline -> greenBg
        isWarning -> orangeBg
        else -> redBg
    }
    val statusText = when {
        isOnline -> greentext
        isWarning -> orangeText
        else -> redText
    }

    val actions = remember(terminal) {
        listOf(
            MenuAction(label = "View Details", onClick = onClick),
            MenuAction(label = "Report", onClick = {}),
            MenuAction(label = "Restart", onClick = {})
        )
    }

    DataCard(
        item = terminal,
        title = terminal.branchName ?: "Store",
        code = terminal.posId.ifBlank { null },
        titleFontWeight = FontWeight.Bold,
        titleColor = title_color,
        topBadgeText = terminal.status.toTitleCase(),
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
                // Inner specs container: Counter | Printer | Terminal
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(cardBgLight)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(text = "Counter", fontSize = 10.sp, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = terminal.counterName ?: "Billing Counter",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(text = "Printer", fontSize = 10.sp, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            val isJammed = terminal.printerStatus.equals("JAMMED", ignoreCase = true)
                            Text(
                                text = terminal.printerStatus ?: "READY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isJammed) redText else greentext
                            )
                        }

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(text = "Terminal", fontSize = 10.sp, color = mutedText)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Terminal ${terminal.terminalId}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }
                    }
                }

                // Footer: Last Seen | Last Activity
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last Seen : ${terminal.lastActivityFormatted ?: "2 Mins Ago"}",
                        fontSize = 11.sp,
                        color = mutedText
                    )
                    Text(
                        text = "Last Activity : ${terminal.activeIssue ?: "None"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }
            }
        }
    )
}