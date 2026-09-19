@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused"
)

package com.cuso.tailor.view.home.inventory.items.all_items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.InventoryItem
import com.cuso.tailor.model.inventory.toHealthDisplay
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextLog
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.background_light_purple
import com.cuso.tailor.ui.theme.blackTitle
import com.cuso.tailor.ui.theme.complete_button_bg
import com.cuso.tailor.ui.theme.disabled
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.statLogoBg
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.AppErrorState
import com.cuso.tailor.view.composable.ListSkeleton
import com.cuso.tailor.view.composable.SettingsTabs
import com.cuso.tailor.view.composable.TabItem
import com.cuso.tailor.view.composable.TitleBar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryViewOne(
    item: InventoryItem?,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onAdjustStock: (InventoryItem) -> Unit = {},
    onWarehouseTransfer: (InventoryItem) -> Unit = {},
    onReorderStock: (InventoryItem) -> Unit = {},
    onMarkInactive: (InventoryItem) -> Unit = {},
    onEdit: (InventoryItem) -> Unit = {},
    onShare: (InventoryItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showStockNotAssignedAlert by remember { mutableStateOf(false) }

    // ── Direct API checks ──
    val rawStatus = item?.rawStockStatus
    val isTrackingDisabled = item?.trackInventory == false

    // If raw status is null/blank or inventory is NOT tracked, DISABLE Adjust Stock & Warehouse Transfer
    val isStockStatusNull = rawStatus.isNullOrBlank() || rawStatus.equals("null", ignoreCase = true) || isTrackingDisabled
    val isStockNotAssigned = !isStockStatusNull && rawStatus.contains("Stock Not Assigned", ignoreCase = true) == true
    val isActionButtonsEnabled = !isStockStatusNull

    // Show Dialog if stock is not assigned when user attempts to adjust or transfer
    if (showStockNotAssignedAlert) {
        StockNotAssignedDialog(
            onDismiss = { showStockNotAssignedAlert = false }
        )
    }

    val inventoryTabs = remember {
        listOf(
            TabItem(label = "Overview", icon = Icons.Outlined.Description),
            TabItem(label = "Transactions", icon = Icons.Outlined.SwapHoriz)
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TitleBar(title = "Item Details", onClose = onDismiss)
            }
        },
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Primary_background)
        ) {
            when {
                isLoading -> {
                    ListSkeleton()
                }

                errorMessage != null -> {
                    AppErrorState(
                        title = "Failed to load inventory",
                        message = errorMessage,
                        onRetry = { /* Retry callback */ }
                    )
                }

                item != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = tokens.screenPadding)
                    ) {
                        // ── Item Header ──
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.name,
                                fontSize = tokens.h2,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                            Spacer(Modifier.width(tokens.extraPadding - 2.dp))
                            StatusBadge(
                                active = item.status.equals("active", ignoreCase = true),
                                tokens = tokens
                            )
                            Spacer(Modifier.weight(1f))

                            IconButton(
                                onClick = { onEdit(item) },
                                modifier = Modifier.size(tokens.iconSize + 14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit",
                                    tint = mutedText,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }

                            IconButton(
                                onClick = { onShare(item) },
                                modifier = Modifier.size(tokens.iconSize + 14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Share",
                                    tint = mutedText,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "SKU: ${item.sku} | Created on ${formatDate(item.createdAt)}",
                            fontSize = tokens.caption,
                            color = mutedText
                        )

                        Spacer(Modifier.height(tokens.extraPadding + 6.dp))

                        // ── Primary Action Button (Adjust Stock) ──
                        Button(
                            onClick = {
                                if (isStockNotAssigned) {
                                    showStockNotAssignedAlert = true
                                } else {
                                    onAdjustStock(item)
                                }
                            },
                            enabled = isActionButtonsEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight),
                            shape = RoundedCornerShape(tokens.cardCornerRadius),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                disabledContainerColor = disabled,
                                contentColor = whiteBg,
                                disabledContentColor = whiteBg.copy(alpha = 0.6f)
                            )
                        ) {
                            Text(
                                text = "Adjust Stock",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding + 6.dp))

                        // ── Tabs Navigation ──
                        SettingsTabs(
                            tabs = inventoryTabs,
                            selectedIndex = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )

                        Spacer(Modifier.height(tokens.extraPadding * 2))

                        // ── Tab Content ──
                        if (selectedTab == 0) {
                            OverviewContent(
                                item = item,
                                tokens = tokens,
                                isActionButtonsEnabled = isActionButtonsEnabled,
                                onAdjustStockClick = {
                                    if (isStockNotAssigned) {
                                        showStockNotAssignedAlert = true
                                    } else {
                                        onAdjustStock(item)
                                    }
                                },
                                onWarehouseTransfer = {
                                    if (isStockNotAssigned) {
                                        showStockNotAssignedAlert = true
                                    } else {
                                        onWarehouseTransfer(item)
                                    }
                                },
                                onReorderStock = onReorderStock,
                                onMarkInactive = onMarkInactive
                            )
                        } else {
                            TransactionsPlaceholder(tokens = tokens)
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 2 + 4.dp))
                    }
                }
            }
        }
    }
}

// =============================================================================
// OVERVIEW TAB CONTENT
// =============================================================================

@Composable
private fun OverviewContent(
    item: InventoryItem,
    tokens: AppDesignTokens,
    isActionButtonsEnabled: Boolean = true,
    onAdjustStockClick: () -> Unit,
    onWarehouseTransfer: (InventoryItem) -> Unit,
    onReorderStock: (InventoryItem) -> Unit,
    onMarkInactive: (InventoryItem) -> Unit
) {
    val health = item.toHealthDisplay()

    // ── Item Details Section ──
    SectionHeader(
        icon = Icons.Outlined.Inventory2,
        title = "Item Details",
        tokens = tokens
    )
    Spacer(Modifier.height(tokens.extraPadding - 2.dp))
    InfoRow(
        label = "Item Type",
        value = item.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        tokens = tokens
    )
    DividerLine()
    InfoRow(
        label = "Unit",
        value = item.unit,
        tokens = tokens
    )
    DividerLine()
    InfoRow(
        label = "Current Stock",
        value = formatQty(item.currentStock),
        tokens = tokens
    )

    Spacer(Modifier.height(tokens.extraPadding * 2))

    // ── Sales Information Section ──
    SectionHeader(
        icon = Icons.Outlined.Sell,
        title = "Sales Information",
        tokens = tokens
    )
    Spacer(Modifier.height(tokens.extraPadding - 2.dp))
    InfoRow(
        label = "Selling Price",
        value = formatCurrency(item.sellingPrice),
        tokens = tokens
    )

    Spacer(Modifier.height(tokens.extraPadding + 6.dp))

    // ── Inventory Health Card ──
    InventoryHealthCard(
        isTracked = health.isTracked,
        statusLabel = item.stockStatus.uppercase(),
        totalStockValue = health.totalStockValue,
        available = health.available,
        reserved = health.reserved,
        wip = health.wip,
        incoming = health.incoming,
        lowThreshold = health.lowThreshold,
        tokens = tokens
    )

    Spacer(Modifier.height(tokens.extraPadding * 2))

    // ── Inventory Actions Section ──
    SectionHeader(
        icon = Icons.Outlined.ShoppingCart,
        title = "Inventory Actions",
        tokens = tokens
    )
    Spacer(Modifier.height(tokens.extraPadding))

    Button(
        onClick = onAdjustStockClick,
        enabled = isActionButtonsEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.buttonHeight),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            disabledContainerColor = disabled,
            contentColor = whiteBg,
            disabledContentColor = whiteBg.copy(alpha = 0.6f)
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.size(tokens.iconSize),
            tint = if (isActionButtonsEnabled) whiteBg else whiteBg.copy(alpha = 0.6f)
        )
        Spacer(Modifier.width(tokens.extraPadding - 4.dp))
        Text(
            text = "Adjust Stock",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }

    Spacer(Modifier.height(tokens.extraPadding))
    OutlinedActionButton(
        label = "Warehouse Transfer",
        icon = Icons.Outlined.SwapHoriz,
        enabled = isActionButtonsEnabled,
        onClick = { onWarehouseTransfer(item) },
        tokens = tokens
    )

    Spacer(Modifier.height(tokens.extraPadding))
    OutlinedActionButton(
        label = "Reorder Stock",
        icon = Icons.Outlined.Refresh,
        enabled = true,
        onClick = { onReorderStock(item) },
        tokens = tokens
    )

    Spacer(Modifier.height(tokens.extraPadding))
    OutlinedActionButton(
        label = "Mark Inactive",
        icon = Icons.Outlined.Block,
        enabled = true,
        onClick = { onMarkInactive(item) },
        contentColor = redText,
        borderColor = redText,
        tokens = tokens
    )

    Spacer(Modifier.height(tokens.extraPadding * 2))

    QuickInsightCard(item = item, tokens = tokens)
}

// =============================================================================
// SUB-COMPONENTS & CARDS
// =============================================================================

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    tokens: AppDesignTokens
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(tokens.iconSize)
        )
        Spacer(Modifier.width(tokens.extraPadding - 2.dp))
        Text(
            text = title,
            fontSize = tokens.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = blackTitle
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    tokens: AppDesignTokens
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = mutedText,
            fontSize = tokens.bodySmall
        )
        Text(
            text = value,
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(color = title_border, thickness = 2.dp)
}

@Composable
private fun InventoryHealthCard(
    isTracked: Boolean,
    statusLabel: String,
    totalStockValue: Double,
    available: Double,
    reserved: Double,
    wip: Double,
    incoming: Double,
    lowThreshold: Double,
    tokens: AppDesignTokens
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(statLogoBg)
            .padding(tokens.cardPadding - 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "INVENTORY HEALTH",
                    color = mutedText,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = statusLabel,
                        color = whiteBg,
                        fontSize = tokens.h2,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(complete_button_bg)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(tokens.iconSize + 8.dp)
                    .clip(CircleShape)
                    .background(complete_button_bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(tokens.iconSize - 3.dp)
                )
            }
        }

        Spacer(Modifier.height(tokens.extraPadding + 8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            HealthMetric("Total Stock Value", formatCurrency(totalStockValue), Modifier.weight(1f), tokens = tokens)
            HealthMetric("Available", formatQty(available), Modifier.weight(1f), tokens = tokens)
        }

        Spacer(Modifier.height(tokens.extraPadding + 6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            HealthMetric("Reserved", formatQty(reserved), Modifier.weight(1f), tokens = tokens)
            HealthMetric("WIP", formatQty(wip), Modifier.weight(1f), tokens = tokens)
        }

        Spacer(Modifier.height(tokens.extraPadding + 6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            HealthMetric("Incoming", formatQty(incoming), Modifier.weight(1f), valueColor = orangeText, tokens = tokens)
            HealthMetric("Low Threshold", formatQty(lowThreshold), Modifier.weight(1f), valueColor = orangeText, tokens = tokens)
        }
    }
}

@Composable
private fun HealthMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = whiteBg,
    tokens: AppDesignTokens
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = mutedText,
            fontSize = tokens.caption
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = tokens.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OutlinedActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    contentColor: Color = TextLog,
    borderColor: Color = BorderGray,
    tokens: AppDesignTokens
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.buttonHeight),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, if (enabled) borderColor else BorderGray.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = contentColor.copy(alpha = 0.4f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(tokens.iconSize),
            tint = if (enabled) contentColor else contentColor.copy(alpha = 0.4f)
        )
        Spacer(Modifier.width(tokens.extraPadding - 4.dp))
        Text(
            text = label,
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickInsightCard(
    item: InventoryItem,
    tokens: AppDesignTokens
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius - 3.dp))
            .background(background_light_purple)
            .padding(tokens.extraPadding + 6.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Lightbulb,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(tokens.iconSize + 2.dp)
        )
        Spacer(Modifier.width(tokens.extraPadding))
        Column {
            Text(
                text = "Quick Insight",
                fontWeight = FontWeight.SemiBold,
                fontSize = tokens.bodyMedium,
                color = title_color
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Demand for ${item.name} has been changing recently. Review reorder point and current stock to avoid stockouts.",
                fontSize = tokens.caption,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun TransactionsPlaceholder(tokens: AppDesignTokens) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.screenPadding * 3.75f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No transactions to show yet.",
            fontSize = tokens.bodyMedium,
            color = mutedText
        )
    }
}

// =============================================================================
// STATUS BADGES
// =============================================================================

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = greenBg,
    textColor: Color = greentext,
    dotColor: Color = textColor,
    cornerRadius: Dp = 20.dp,
    dotSize: Dp = 7.dp,
    showDot: Boolean = true,
    tokens: AppDesignTokens
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun StatusBadge(
    active: Boolean,
    modifier: Modifier = Modifier,
    tokens: AppDesignTokens
) {
    StatusBadge(
        text = if (active) "Active" else "Inactive",
        bgColor = if (active) greenBg else redBg,
        textColor = if (active) greentext else redText,
        showDot = false,
        tokens = tokens,
        modifier = modifier
    )
}

// =============================================================================
// HELPER FORMATTERS
// =============================================================================

private fun formatCurrency(value: Double): String {
    val format = NumberFormat.getNumberInstance(
        Locale.Builder().setLanguage("en").setRegion("IN").build()
    )
    format.maximumFractionDigits = 0
    return "\u20B9${format.format(value)}"
}

private fun formatQty(value: Double): String {
    val format = NumberFormat.getNumberInstance(
        Locale.Builder().setLanguage("en").setRegion("IN").build()
    )
    format.maximumFractionDigits = if (value % 1.0 == 0.0) 0 else 1
    return format.format(value)
}

private fun formatDate(iso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(iso)
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        formatter.format(date!!)
    } catch (e: Exception) {
        iso
    }
}