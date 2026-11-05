@file:Suppress("UNUSED_PARAMETER", "AssignedValueIsNeverRead")
@file:OptIn(ExperimentalLayoutApi::class)

package com.cuso.tailor.view.home.it.integerations

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Power
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.it.IntegrationKind
import com.cuso.tailor.model.it.UnifiedIntegrationItem
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.SearchFilterBar
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.viewmodel.MarketingViewModel

// ─────────────────────────────────────────────────────────────────────────────
// FORM DATA
// ─────────────────────────────────────────────────────────────────────────────

data class IntegrationFormData(
    val type: String,
    val name: String,
    val category: String,
    val description: String,
    val baseUrl: String,
    val testPath: String,
    val authMethod: String,
    val headerName: String,
    val username: String,
    val secret: String
)

// ─────────────────────────────────────────────────────────────────────────────
// MAIN SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IntegrationManagementScreen(
    viewModel: MarketingViewModel,
    onClose: () -> Unit,
    onSyncAdPlatforms: (() -> Unit)? = null,
    onSaveIntegration: (IntegrationFormData) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val integrations by viewModel.filteredIntegrations.collectAsState()
    val allItems by viewModel.allIntegrations.collectAsState()
    val isLoading by viewModel.isIntegrationsLoading.collectAsState()

    val searchQuery by viewModel.integrationsSearchQuery.collectAsState()
    val selectedCategory by viewModel.integrationsCategoryFilter.collectAsState()
    val selectedStatus by viewModel.integrationsStatusFilter.collectAsState()

    var selectedItemForDialog by remember { mutableStateOf<UnifiedIntegrationItem?>(null) }
    var showAddMenu by remember { mutableStateOf(false) }
    var showAddFormDialog by remember { mutableStateOf<String?>(null) } // "external" or "cuso"
    var statusExpanded by remember { mutableStateOf(false) }

    var pageSize by remember { mutableIntStateOf(10) }
    var page by remember(searchQuery, selectedCategory, selectedStatus, pageSize) { mutableIntStateOf(0) }

    // Initial load
    LaunchedEffect(Unit) {
        viewModel.fetchAllIntegrations()
    }

    val connectedCount = remember(allItems) { allItems.count { it.status == "Connected" } }
    val needsAttentionCount = remember(allItems) {
        allItems.count { it.status == "Warning" || it.status == "Failed" }
    }
    val hasConnectedAdPlatform = remember(allItems) {
        allItems.any { it.kind == IntegrationKind.AD && it.status == "Connected" }
    }
    val categories = remember(allItems) {
        listOf("All") + allItems.map { it.category }.distinct()
    }
    val statusOptions = listOf(
        "All Statuses",
        "Connected",
        "Inactive",
        "Needs attention",
        "Unknown",
        "Connector not available",
        "Platform managed"
    )

    // Pagination values
    val total = integrations.size
    val lastPage = if (total == 0) 0 else (total - 1) / pageSize
    val safePage = page.coerceAtMost(lastPage)
    val startIndex = safePage * pageSize
    val endIndex = minOf(startIndex + pageSize, total)
    val pageItems = integrations.drop(startIndex).take(pageSize)

    val cardShape = RoundedCornerShape(tokens.cardCornerRadius)
    val controlShape = RoundedCornerShape(tokens.cardCornerRadius / 2)
    val cardBorder = BorderStroke(Dp.Hairline, sectionBorder)
    val gap = tokens.extraPadding

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
        ) {
            // ── Header card: title, subtitle and action buttons ──
            item {
                Surface(
                    color = whiteBg,
                    shape = cardShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(tokens.cardPadding)) {
                        Text(
                            text = "Integration Management",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        Spacer(Modifier.height(gap / 2))
                        Text(
                            text = "Manage connected services and integrations from one place.",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )

                        Spacer(Modifier.height(gap + gap / 2))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap),
                            verticalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            // Add integration with dropdown menu
                            Box {
                                Button(
                                    onClick = { showAddMenu = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                    shape = controlShape,
                                    contentPadding = PaddingValues(horizontal = tokens.screenPadding),
                                    modifier = Modifier.height(tokens.buttonHeight)
                                ) {
                                    Icon(
                                        Icons.Outlined.Add,
                                        contentDescription = null,
                                        tint = whiteBg,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                    Spacer(Modifier.width(gap / 2))
                                    Text(
                                        text = "Add integration",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = whiteBg,
                                        maxLines = 1
                                    )
                                }
                                DropdownMenu(
                                    expanded = showAddMenu,
                                    onDismissRequest = { showAddMenu = false },
                                    modifier = Modifier.background(whiteBg)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Connect an external API",
                                                fontSize = tokens.bodySmall,
                                                color = title_color
                                            )
                                        },
                                        onClick = {
                                            showAddMenu = false
                                            showAddFormDialog = "external"
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Create CUSO API / webhook",
                                                fontSize = tokens.bodySmall,
                                                color = title_color
                                            )
                                        },
                                        onClick = {
                                            showAddMenu = false
                                            showAddFormDialog = "cuso"
                                        }
                                    )
                                }
                            }

                            // Refresh status
                            OutlinedButton(
                                onClick = { viewModel.fetchAllIntegrations() },
                                shape = controlShape,
                                border = cardBorder,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = whiteBg,
                                    contentColor = title_color
                                ),
                                contentPadding = PaddingValues(horizontal = tokens.screenPadding),
                                modifier = Modifier.height(tokens.buttonHeight)
                            ) {
                                Icon(
                                    Icons.Outlined.Refresh,
                                    contentDescription = null,
                                    tint = title_color,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(gap / 2))
                                Text(
                                    text = "Refresh status",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color,
                                    maxLines = 1
                                )
                            }

                            // Sync ad platforms (disabled until an ad platform is connected)
                            Button(
                                onClick = { onSyncAdPlatforms?.invoke() },
                                enabled = hasConnectedAdPlatform && onSyncAdPlatforms != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primary_light,
                                    contentColor = Primary,
                                    disabledContainerColor = primary_light,
                                    disabledContentColor = iconMuted
                                ),
                                shape = controlShape,
                                contentPadding = PaddingValues(horizontal = tokens.screenPadding),
                                modifier = Modifier.height(tokens.buttonHeight)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ShowChart,
                                    contentDescription = null,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(gap / 2))
                                Text(
                                    text = "Sync ad platforms",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // ── Metric cards: stacked on phones, one row on tablets ──
            item {
                if (tokens.isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding)
                    ) {
                        IntegrationMetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Total integrations",
                            value = allItems.size.toString(),
                            icon = Icons.Outlined.Power,
                            iconTint = Primary
                        )
                        IntegrationMetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Connected",
                            value = connectedCount.toString(),
                            icon = Icons.Outlined.CheckCircle,
                            iconTint = greentext
                        )
                        IntegrationMetricCard(
                            modifier = Modifier.weight(1f),
                            label = "Needs attention",
                            value = needsAttentionCount.toString(),
                            icon = Icons.Outlined.ErrorOutline,
                            iconTint = yellowText
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)) {
                        IntegrationMetricCard(
                            modifier = Modifier.fillMaxWidth(),
                            label = "Total integrations",
                            value = allItems.size.toString(),
                            icon = Icons.Outlined.Power,
                            iconTint = Primary
                        )
                        IntegrationMetricCard(
                            modifier = Modifier.fillMaxWidth(),
                            label = "Connected",
                            value = connectedCount.toString(),
                            icon = Icons.Outlined.CheckCircle,
                            iconTint = greentext
                        )
                        IntegrationMetricCard(
                            modifier = Modifier.fillMaxWidth(),
                            label = "Needs attention",
                            value = needsAttentionCount.toString(),
                            icon = Icons.Outlined.ErrorOutline,
                            iconTint = yellowText
                        )
                    }
                }
            }

            // ── Connected services card ──
            item {
                Surface(
                    color = whiteBg,
                    shape = cardShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(tokens.cardPadding)) {
                        Text(
                            text = "Connected services",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        Spacer(Modifier.height(gap / 2))
                        Text(
                            text = "Live connections, platform-managed services and planned provider connectors.",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )

                        Spacer(Modifier.height(tokens.screenPadding))

                        SearchFilterBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.onIntegrationSearchQueryChange(it) },
                            placeholder = "Search integrations...",
                            isSearchBarAlone = true
                        )

                        Spacer(Modifier.height(tokens.screenPadding))

                        // Category chips wrap onto multiple lines
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap),
                            verticalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            categories.forEach { category ->
                                val isSelected = selectedCategory.equals(category, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) Primary else grey_border)
                                        .clickable { viewModel.onIntegrationCategoryFilterChange(category) }
                                        .padding(
                                            horizontal = tokens.screenPadding,
                                            vertical = 2.dp
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category,
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) whiteBg else TextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.screenPadding))

                        // Status filter using the shared FormDropdown
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Status:",
                                fontSize = tokens.bodySmall,
                                color = headerGrey
                            )
                            Spacer(Modifier.width(gap))
                            FormDropdown(
                                modifier = Modifier.width(tokens.imageDp * 3),
                                value = if (selectedStatus == "All") "All Statuses" else selectedStatus,
                                expanded = statusExpanded,
                                onExpandChange = { statusExpanded = it },
                                options = statusOptions,
                                onOptionSelected = { option ->
                                    viewModel.onIntegrationStatusFilterChange(
                                        if (option == "All Statuses") "All" else option
                                    )
                                }
                            )
                        }

                        Spacer(Modifier.height(tokens.screenPadding))

                        // Table with header, rows and pagination footer
                        IntegrationTableBox(
                            items = pageItems,
                            isLoading = isLoading,
                            total = total,
                            startIndex = startIndex,
                            endIndex = endIndex,
                            pageSize = pageSize,
                            onPageSizeChange = { pageSize = it },
                            hasPrevious = safePage > 0,
                            hasNext = safePage < lastPage,
                            onPrevious = { page = (safePage - 1).coerceAtLeast(0) },
                            onNext = { page = (safePage + 1).coerceAtMost(lastPage) },
                            onRowClick = { selectedItemForDialog = it }
                        )
                    }
                }
            }
        }
    }

    // Details dialog
    selectedItemForDialog?.let { item ->
        IntegrationDetailsDialog(
            item = item,
            onDismiss = { selectedItemForDialog = null },
            viewModel = viewModel
        )
    }

    // Add integration dialog
    showAddFormDialog?.let { type ->
        AddIntegrationFormDialog(
            type = type,
            onDismiss = { showAddFormDialog = null },
            onSave = { data ->
                onSaveIntegration(data)
                viewModel.fetchAllIntegrations()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

// Returns icon, icon tint and icon background for an integration kind
private fun integrationIcon(kind: IntegrationKind): Triple<ImageVector, Color, Color> = when (kind) {
    IntegrationKind.COURIER -> Triple(Icons.Outlined.LocalShipping, orangeText, orangeBg)
    IntegrationKind.AD -> Triple(Icons.Outlined.Cloud, Primary, primary_light)
    IntegrationKind.PLATFORM_SERVICE -> Triple(Icons.Outlined.Cloud, Primary, primary_light)
    else -> Triple(Icons.Outlined.Power, headerGrey, light_grey)
}

// ─────────────────────────────────────────────────────────────────────────────
// STATUS BADGE (wrapper around the shared StatusBadge)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IntegrationStatusBadge(status: String) {
    val tokens = LocalAppTokens.current

    val (bgColor, textColor) = when (status) {
        "Connected" -> greenBg to greentext
        "Warning" -> yellowBg to yellowText
        "Failed" -> redBg to redText
        "Platform managed" -> primary_light to Primary
        else -> grey_border to TextSecondary
    }

    StatusBadge(
        text = status,
        modifier = Modifier.height(tokens.fieldHeight * 0.55f),
        bgColor = bgColor,
        textColor = textColor,
        dotColor = if (bgColor == grey_border) mutedText else textColor,
        cornerRadius = tokens.cardCornerRadius
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// METRIC CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun IntegrationMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color
) {
    val tokens = LocalAppTokens.current
    val boxShape = RoundedCornerShape(tokens.cardCornerRadius / 2)

    Surface(
        modifier = modifier,
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.cardPadding, vertical = tokens.screenPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.iconSize + tokens.extraPadding)
                    .clip(boxShape)
                    .background(PanelBg)
                    .border(Dp.Hairline, grey_border, boxShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                )
            }
            Spacer(Modifier.width(tokens.extraPadding + tokens.extraPadding / 2))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = tokens.caption,
                    color = headerGrey
                )
                Text(
                    text = value,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TABLE (bordered box, horizontally scrollable, full names visible)
// ─────────────────────────────────────────────────────────────────────────────

private data class IntegrationTableColumns(
    val integration: Dp,
    val category: Dp,
    val status: Dp,
    val lastSync: Dp,
    val action: Dp,
    val total: Dp
)

@Composable
private fun rememberTableColumns(): IntegrationTableColumns {
    val tokens = LocalAppTokens.current
    val integration = tokens.imageDp * 6
    val category = tokens.imageDp * 2
    val status = tokens.imageDp * 3
    val lastSync = tokens.imageDp * 2
    val action = tokens.iconSize
    val total = integration + category + status + lastSync + action + tokens.screenPadding * 2
    return IntegrationTableColumns(integration, category, status, lastSync, action, total)
}

@Composable
private fun IntegrationTableBox(
    items: List<UnifiedIntegrationItem>,
    isLoading: Boolean,
    total: Int,
    startIndex: Int,
    endIndex: Int,
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRowClick: (UnifiedIntegrationItem) -> Unit
) {
    val tokens = LocalAppTokens.current
    val cols = rememberTableColumns()
    val scrollState = rememberScrollState()
    val boxShape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f)
    val rowVertical = tokens.extraPadding * 1.2f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(Dp.Hairline, sectionBorder, boxShape)
            .clip(boxShape)
            .background(whiteBg)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tableWidth = if (cols.total > maxWidth) cols.total else maxWidth

            Column(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .width(tableWidth)
            ) {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = tokens.screenPadding, vertical = rowVertical),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderCell("INTEGRATION", cols.integration)
                    TableHeaderCell("CATEGORY", cols.category)
                    TableHeaderCell("STATUS", cols.status)
                    TableHeaderCell("LAST SYNC", cols.lastSync)
                    Spacer(Modifier.width(cols.action))
                }

                HorizontalDivider(color = sectionBorder, thickness = Dp.Hairline)

                if (!isLoading) {
                    items.forEachIndexed { index, item ->
                        IntegrationRowItem(
                            item = item,
                            columns = cols,
                            rowVertical = rowVertical,
                            onClick = { onRowClick(item) }
                        )
                        if (index < items.lastIndex) {
                            HorizontalDivider(color = grey_border, thickness = Dp.Hairline)
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.cardPadding * 2),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.cardPadding * 2),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No integrations found",
                    fontSize = tokens.bodySmall,
                    color = headerGrey
                )
            }
        }

        HorizontalDivider(color = sectionBorder, thickness = Dp.Hairline)

        IntegrationPaginationFooter(
            total = total,
            startIndex = startIndex,
            endIndex = endIndex,
            pageSize = pageSize,
            onPageSizeChange = onPageSizeChange,
            hasPrevious = hasPrevious,
            hasNext = hasNext,
            onPrevious = onPrevious,
            onNext = onNext
        )
    }
}

@Composable
private fun TableHeaderCell(text: String, width: Dp) {
    val tokens = LocalAppTokens.current
    Text(
        text = text,
        fontSize = tokens.label,
        fontWeight = FontWeight.Medium,
        color = headerGrey,
        maxLines = 1,
        modifier = Modifier
            .width(width)
            .padding(end = tokens.extraPadding)
    )
}

@Composable
private fun IntegrationRowItem(
    item: UnifiedIntegrationItem,
    columns: IntegrationTableColumns,
    rowVertical: Dp,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val (icon, iconTint, iconBg) = integrationIcon(item.kind)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = tokens.screenPadding, vertical = rowVertical),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Identity: icon, name and full description
        Row(
            modifier = Modifier
                .width(columns.integration)
                .padding(end = tokens.extraPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.imageDp / 2 + tokens.extraPadding / 2)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius / 2))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }
            Spacer(Modifier.width(tokens.extraPadding + tokens.extraPadding / 2))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
                Text(
                    text = if (item.accountId.isNotBlank()) item.accountId else item.description,
                    fontSize = tokens.caption,
                    color = headerGrey
                )
            }
        }

        // Category
        Text(
            text = item.category,
            fontSize = tokens.caption,
            color = TextSecondary,
            modifier = Modifier
                .width(columns.category)
                .padding(end = tokens.extraPadding)
        )

        // Status
        Box(
            modifier = Modifier
                .width(columns.status)
                .padding(end = tokens.extraPadding),
            contentAlignment = Alignment.CenterStart
        ) {
            IntegrationStatusBadge(status = item.status)
        }

        // Last sync
        Text(
            text = item.lastSync ?: "Never",
            fontSize = tokens.caption,
            color = headerGrey,
            modifier = Modifier
                .width(columns.lastSync)
                .padding(end = tokens.extraPadding)
        )

        // Open details arrow
        Icon(
            imageVector = Icons.Outlined.NorthEast,
            contentDescription = "View details",
            tint = mutedText,
            modifier = Modifier.size(columns.action * 0.8f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PAGINATION FOOTER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun IntegrationPaginationFooter(
    total: Int,
    startIndex: Int,
    endIndex: Int,
    pageSize: Int,
    onPageSizeChange: (Int) -> Unit,
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val gap = tokens.extraPadding
    val controlShape = RoundedCornerShape(tokens.cardCornerRadius / 2)
    var pageSizeExpanded by remember { mutableStateOf(false) }

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding + tokens.extraPadding / 2),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.End),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        Text(
            text = if (total == 0) "Showing 0 of 0" else "Showing ${startIndex + 1} - $endIndex of $total",
            fontSize = tokens.caption,
            color = headerGrey,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        // Page size selector
        Box {
            Row(
                modifier = Modifier
                    .height(tokens.fieldHeight)
                    .clip(controlShape)
                    .background(whiteBg)
                    .border(Dp.Hairline, sectionBorder, controlShape)
                    .clickable { pageSizeExpanded = true }
                    .padding(horizontal = gap + gap / 2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = title_color,
                    modifier = Modifier.size(tokens.iconSize * 0.8f)
                )
                Spacer(Modifier.width(gap / 2))
                Text(
                    text = "$pageSize per page",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = title_color,
                    maxLines = 1
                )
            }
            DropdownMenu(
                expanded = pageSizeExpanded,
                onDismissRequest = { pageSizeExpanded = false },
                modifier = Modifier.background(whiteBg)
            ) {
                listOf(10, 25, 50).forEach { size ->
                    DropdownMenuItem(
                        text = { Text("$size per page", fontSize = tokens.bodySmall, color = title_color) },
                        onClick = {
                            onPageSizeChange(size)
                            pageSizeExpanded = false
                        }
                    )
                }
            }
        }

        // Previous / range / next
        Row(
            modifier = Modifier
                .height(tokens.fieldHeight)
                .clip(controlShape)
                .background(whiteBg)
                .border(Dp.Hairline, sectionBorder, controlShape),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PagerArrow(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                description = "Previous page",
                enabled = hasPrevious,
                onClick = onPrevious
            )
            Text(
                text = if (total == 0) "0 - 0" else "${startIndex + 1} - $endIndex",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = title_color,
                maxLines = 1
            )
            PagerArrow(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                description = "Next page",
                enabled = hasNext,
                onClick = onNext
            )
        }
    }
}

@Composable
private fun PagerArrow(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .size(tokens.fieldHeight)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (enabled) title_color else mutedText.copy(alpha = 0.5f),
            modifier = Modifier.size(tokens.iconSize)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DETAILS DIALOG
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LabelValue(label: String, value: String, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier) {
        Text(text = label, fontSize = tokens.caption, color = headerGrey)
        Spacer(Modifier.height(tokens.extraPadding / 5))
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = title_color
        )
    }
}

@Composable
private fun InfoNoteBox(
    title: String,
    message: String,
    background: Color,
    titleColor: Color,
    messageColor: Color,
    borderColor: Color
) {
    val tokens = LocalAppTokens.current
    val shape = RoundedCornerShape(tokens.cardCornerRadius / 2)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, shape)
            .border(Dp.Hairline, borderColor, shape)
            .padding(tokens.screenPadding)
    ) {
        Text(
            text = title,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = titleColor
        )
        Spacer(Modifier.height(tokens.extraPadding / 2))
        Text(
            text = message,
            fontSize = tokens.caption,
            color = messageColor
        )
    }
}

@Composable
fun IntegrationDetailsDialog(
    item: UnifiedIntegrationItem,
    onDismiss: () -> Unit,
    viewModel: MarketingViewModel
) {
    val tokens = LocalAppTokens.current
    val (icon, iconTint, iconBg) = integrationIcon(item.kind)
    val gap = tokens.extraPadding
    val buttonShape = RoundedCornerShape(tokens.cardCornerRadius / 2)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(tokens.screenPadding)
                .widthIn(max = tokens.imageDp * 9)
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(Dp.Hairline, sectionBorder),
            shadowElevation = tokens.extraPadding
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(tokens.cardPadding)
            ) {
                // Header: icon, category, name and status badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.buttonHeight + gap)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius / 2))
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(tokens.iconSize + gap / 2)
                        )
                    }

                    Spacer(Modifier.width(gap + gap / 2))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item.category} integration",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                        Text(
                            text = item.name,
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }

                    Spacer(Modifier.width(gap))

                    Box(modifier = Modifier.align(Alignment.Top)) {
                        IntegrationStatusBadge(status = item.status)
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding))
                HorizontalDivider(color = grey_border, thickness = Dp.Hairline)
                Spacer(Modifier.height(tokens.screenPadding))

                Text(
                    text = item.description,
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )

                Spacer(Modifier.height(tokens.screenPadding))

                // Information grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PanelBg, RoundedCornerShape(tokens.cardCornerRadius / 2))
                        .padding(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(gap + gap / 2)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        LabelValue("Connection", item.status, Modifier.weight(1f))
                        LabelValue("Last sync", item.lastSync ?: "Never", Modifier.weight(1f))
                    }

                    if (item.kind == IntegrationKind.AD) {
                        LabelValue(
                            label = "Selected ad account",
                            value = if (item.accountId.isNotBlank()) item.accountId else "Not selected"
                        )
                    } else if (item.accountId.isNotBlank()) {
                        LabelValue(label = "Account / Endpoint", value = item.accountId)
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding))

                // Contextual actions based on integration kind
                when (item.kind) {
                    IntegrationKind.AD -> {
                        Button(
                            onClick = { /* Connect platform logic */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = buttonShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                        ) {
                            Icon(
                                Icons.Outlined.Power,
                                contentDescription = null,
                                tint = whiteBg,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(gap / 2))
                            Text(
                                text = "Connect ${item.name}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }

                        Spacer(Modifier.height(gap))

                        OutlinedButton(
                            onClick = { /* Reconnect logic */ },
                            shape = buttonShape,
                            border = BorderStroke(Dp.Hairline, sectionBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                        ) {
                            Text(
                                text = "Reconnect authorization",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }
                    }

                    IntegrationKind.COURIER -> {
                        Button(
                            onClick = { /* Open courier configuration */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = buttonShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                        ) {
                            Text(
                                text = "Configure ${item.name}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }

                    IntegrationKind.EXTERNAL_API -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            Button(
                                onClick = { /* Test connection */ },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = buttonShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight)
                            ) {
                                Text(
                                    text = "Test API",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = whiteBg
                                )
                            }

                            OutlinedButton(
                                onClick = { /* Remove integration */ },
                                shape = buttonShape,
                                border = BorderStroke(Dp.Hairline, redText),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(tokens.buttonHeight)
                            ) {
                                Text(
                                    text = "Remove",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = redText
                                )
                            }
                        }
                    }

                    IntegrationKind.PLANNED -> {
                        InfoNoteBox(
                            title = "Connector not available yet",
                            message = "This provider is in the integration catalog, but its authentication, account setup, and sync flow have not been implemented. No credentials have been collected or connection is being reported as active.",
                            background = PanelBg,
                            titleColor = title_color,
                            messageColor = TextSecondary,
                            borderColor = sectionBorder
                        )
                    }

                    IntegrationKind.PLATFORM_SERVICE -> {
                        InfoNoteBox(
                            title = "Managed by the CUSO platform",
                            message = "This service is configured at platform level and is not a tenant-owned connection.",
                            background = primary_light,
                            titleColor = Primary,
                            messageColor = Primary,
                            borderColor = light_blue_border
                        )
                    }

                    else -> {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = buttonShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                        ) {
                            Text(
                                text = "Close",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FORM WRAPPERS (built on the shared form components)
// ─────────────────────────────────────────────────────────────────────────────

// Label above a FormTextField with an optional helper line below
@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    filled: Boolean = false,
    secret: Boolean = false,
    helper: String? = null
) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier.fillMaxWidth()) {
        FormLabel(text = label)
        FormTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            containerColor = if (filled) light_grey else whiteBg,
            visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None
        )
        if (helper != null) {
            Spacer(Modifier.height(tokens.extraPadding / 2))
            Text(text = helper, fontSize = tokens.caption, color = iconMuted)
        }
    }
}

// FormDropdown that keeps its own expanded state
@Composable
private fun LabeledDropdown(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    FormDropdown(
        modifier = modifier,
        label = label,
        value = value,
        expanded = expanded,
        onExpandChange = { expanded = it },
        options = options,
        onOptionSelected = onSelect
    )
}

@Composable
private fun IntegrationFormDialogFrame(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(tokens.screenPadding)
                .widthIn(max = tokens.imageDp * 9)
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            shadowElevation = tokens.extraPadding
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(tokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding + tokens.extraPadding / 2)
            ) {
                // Title row with close icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = title_color,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = close_color,
                        modifier = Modifier
                            .size(tokens.iconSize)
                            .clickable { onDismiss() }
                    )
                }

                Text(
                    text = "Configure and manage this connection from IT Integrations.",
                    fontSize = tokens.caption,
                    color = headerGrey
                )

                content()
            }
        }
    }
}

@Composable
private fun FormActionRow(
    confirmLabel: String,
    confirmEnabled: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val shape = RoundedCornerShape(tokens.cardCornerRadius / 2)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onCancel,
            shape = shape,
            border = BorderStroke(Dp.Hairline, sectionBorder),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg),
            contentPadding = PaddingValues(horizontal = tokens.screenPadding),
            modifier = Modifier.height(tokens.buttonHeight)
        ) {
            Text(
                text = "Cancel",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = title_color
            )
        }
        Spacer(Modifier.width(tokens.extraPadding))
        Button(
            onClick = onConfirm,
            enabled = confirmEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                disabledContainerColor = disabled
            ),
            shape = shape,
            contentPadding = PaddingValues(horizontal = tokens.screenPadding),
            modifier = Modifier.height(tokens.buttonHeight)
        ) {
            Text(
                text = confirmLabel,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = whiteBg
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADD INTEGRATION FORM DIALOG
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AddIntegrationFormDialog(
    type: String, // "external" or "cuso"
    onDismiss: () -> Unit,
    onSave: (IntegrationFormData) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val isExternal = type == "external"
    val gap = tokens.extraPadding + tokens.extraPadding / 2
    val noteShape = RoundedCornerShape(tokens.cardCornerRadius / 2)

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var baseUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Custom API") }
    var testPath by remember { mutableStateOf("/") }
    var authMethod by remember { mutableStateOf(if (isExternal) "API key header" else "API Key") }
    var headerName by remember { mutableStateOf("X-API-Key") }
    var username by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var cusoType by remember { mutableStateOf("REST API") }

    val externalCategories = listOf(
        "Custom API", "Marketing", "Shipping", "Marketplace",
        "Payments", "Communication", "CRM", "Accounting"
    )
    val externalAuthMethods = listOf("API key header", "Bearer token", "Basic auth", "No authentication")
    val cusoTypes = listOf("REST API", "Webhook")
    val cusoAuthMethods = listOf("API Key", "Bearer Token", "HMAC Signature")

    IntegrationFormDialogFrame(
        title = if (isExternal) "Connect an external API" else "Create CUSO API / webhook",
        onDismiss = onDismiss
    ) {
        if (isExternal) {
            // Name and category sit side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                LabeledTextField(
                    label = "Integration name",
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.weight(1f)
                )
                LabeledDropdown(
                    label = "Category",
                    value = category,
                    options = externalCategories,
                    onSelect = { category = it },
                    modifier = Modifier.weight(1f)
                )
            }

            LabeledTextField(
                label = "Description",
                value = description,
                onValueChange = { description = it }
            )

            LabeledTextField(
                label = "API base URL",
                value = baseUrl,
                onValueChange = { baseUrl = it },
                placeholder = "https://api.example.com"
            )

            LabeledTextField(
                label = "Connection test path",
                value = testPath,
                onValueChange = { testPath = it },
                filled = true,
                helper = "A GET request is sent to this relative path; redirects are not followed."
            )

            LabeledDropdown(
                label = "Authentication",
                value = authMethod,
                options = externalAuthMethods,
                onSelect = { authMethod = it }
            )

            // Credential fields depend on the selected authentication method
            when (authMethod) {
                "API key header" -> {
                    LabeledTextField(
                        label = "Header name",
                        value = headerName,
                        onValueChange = { headerName = it },
                        filled = true
                    )
                    LabeledTextField(
                        label = "API key",
                        value = secret,
                        onValueChange = { secret = it },
                        secret = true
                    )
                }

                "Bearer token" -> {
                    LabeledTextField(
                        label = "Bearer token",
                        value = secret,
                        onValueChange = { secret = it },
                        secret = true
                    )
                }

                "Basic auth" -> {
                    LabeledTextField(
                        label = "Username",
                        value = username,
                        onValueChange = { username = it }
                    )
                    LabeledTextField(
                        label = "Password",
                        value = secret,
                        onValueChange = { secret = it },
                        secret = true
                    )
                }
            }

            // Security note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(yellowBg, noteShape)
                    .padding(gap)
            ) {
                Text(
                    text = "Credentials are encrypted at rest. Requests to private, local, or internal network addresses are blocked.",
                    fontSize = tokens.caption,
                    color = activity_brown
                )
            }

            FormActionRow(
                confirmLabel = "Save API",
                confirmEnabled = name.isNotBlank() && baseUrl.isNotBlank(),
                onCancel = onDismiss,
                onConfirm = {
                    onSave(
                        IntegrationFormData(
                            type = type,
                            name = name.trim(),
                            category = category,
                            description = description.trim(),
                            baseUrl = baseUrl.trim(),
                            testPath = testPath.trim(),
                            authMethod = authMethod,
                            headerName = headerName.trim(),
                            username = username.trim(),
                            secret = secret
                        )
                    )
                    onDismiss()
                }
            )
        } else {
            LabeledTextField(
                label = "Integration name",
                value = name,
                onValueChange = { name = it }
            )

            LabeledTextField(
                label = "Description",
                value = description,
                onValueChange = { description = it }
            )

            LabeledDropdown(
                label = "Type",
                value = cusoType,
                options = cusoTypes,
                onSelect = { cusoType = it }
            )

            LabeledTextField(
                label = "API base URL",
                value = baseUrl,
                onValueChange = { baseUrl = it },
                placeholder = "https://api.example.com"
            )

            LabeledDropdown(
                label = "Authentication method",
                value = authMethod,
                options = cusoAuthMethods,
                onSelect = { authMethod = it }
            )

            // Credential generation note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(activity_purple_bg, noteShape)
                    .border(Dp.Hairline, light_blue_border, noteShape)
                    .padding(gap)
            ) {
                Text(
                    text = "CUSO generates the API key and secret. They are masked by default and can be revealed from the integration details with an audit record.",
                    fontSize = tokens.caption,
                    color = darkPurple
                )
            }

            FormActionRow(
                confirmLabel = "Create credentials",
                confirmEnabled = name.isNotBlank() && baseUrl.isNotBlank(),
                onCancel = onDismiss,
                onConfirm = {
                    onSave(
                        IntegrationFormData(
                            type = cusoType,
                            name = name.trim(),
                            category = "Custom API",
                            description = description.trim(),
                            baseUrl = baseUrl.trim(),
                            testPath = "",
                            authMethod = authMethod,
                            headerName = "",
                            username = "",
                            secret = ""
                        )
                    )
                    onDismiss()
                }
            )
        }
    }
}