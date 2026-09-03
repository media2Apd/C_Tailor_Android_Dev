@file:Suppress(
    "UNUSED_VALUE",
    "unused_variable",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "VariableNeverRead",
    "unused"
)

package com.cuso.mobile.view.home.inventory.items.all_items

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.InventoryItem
import com.cuso.mobile.model.inventory.toHealthDisplay
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.blackTitle
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.AppErrorState
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.view.composable.SettingsTabs
import com.cuso.mobile.view.composable.TabItem
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.blurScrim
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val PurplePrimary = Color(0xFF5A4FE0)
private val PurpleLight = Color(0xFFEFEDFC)
private val GreenActive = Color(0xFF1FAA59)
private val GreenActiveBg = Color(0xFFE4F7EC)
private val DarkCardBg = Color(0xFF1B2437)
private val AmberText = Color(0xFFE8A33D)
private val RedText = Color(0xFFE5484D)
private val TextGray = Color(0xFF8A93A6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryViewOne(
    item: InventoryItem?,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onAdjustStock: (InventoryItem) -> Unit = {},
    onAdjustStockSubmit: (AdjustmentType, Double, String, String) -> Unit = { _, _, _, _ -> },
    onWarehouseTransfer: (InventoryItem) -> Unit = {},
    onReorderStock: (InventoryItem) -> Unit = {},
    onMarkInactive: (InventoryItem) -> Unit = {},
    onEdit: (InventoryItem) -> Unit = {},
    onShare: (InventoryItem) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Overview, 1 = Transactions
    var showAdjustStockSheet by remember { mutableStateOf(false) }
    var sheetBlur by remember { mutableStateOf(0.dp) }

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
                TitleBar("Item Details", onClose = onDismiss)
            }
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blurScrim(sheetBlur)
                ) {
                    when {
                        isLoading -> {
                            ListSkeleton()
                        }

                        errorMessage != null -> {
                            AppErrorState(
                                title = "Failed to load inventory",
                                message = "Something went wrong. Please check your connection and try again.",
                                onRetry = {  }
                            )
                        }

                        item != null -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = tokens.screenPadding)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        item.name,
                                        fontSize = tokens.h2,
                                        fontWeight = FontWeight.SemiBold,
                                        color = title_color
                                    )
                                    Spacer(Modifier.width(tokens.extraPadding - 2.dp))
                                    StatusBadge(
                                        active = item.status.equals("active", ignoreCase = true)
                                    )
                                    Spacer(Modifier.weight(1f))
                                    IconButton(
                                        onClick = { onEdit(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Edit,
                                            contentDescription = "Edit",
                                            tint = TextGray
                                        )
                                    }
                                    IconButton(
                                        onClick = { onShare(item) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Share,
                                            contentDescription = "Share",
                                            tint = TextGray
                                        )
                                    }
                                }

                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "SKU: ${item.sku} | Created on ${formatDate(item.createdAt)}",
                                    fontSize = tokens.caption,
                                    color = TextGray
                                )

                                Spacer(Modifier.height(tokens.extraPadding + 6.dp))
                                Button(
                                    onClick = { showAdjustStockSheet = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(tokens.buttonHeight),
                                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                                ) {
                                    Text(
                                        "Adjust Stock",
                                        fontWeight = FontWeight.Medium,
                                        color = whiteBg
                                    )
                                }

                                Spacer(Modifier.height(tokens.extraPadding + 6.dp))

                                SettingsTabs(
                                    tabs = inventoryTabs,
                                    selectedIndex = selectedTab,
                                    onTabSelected = { selectedTab = it }
                                )

                                Spacer(Modifier.height(tokens.extraPadding * 2))

                                if (selectedTab == 0) {
                                    OverviewContent(
                                        item = item,
                                        onAdjustStockClick = { showAdjustStockSheet = true },
                                        onWarehouseTransfer = onWarehouseTransfer,
                                        onReorderStock = onReorderStock,
                                        onMarkInactive = onMarkInactive
                                    )
                                } else {
                                    TransactionsPlaceholder()
                                }

                                Spacer(Modifier.height(tokens.extraPadding * 2 + 4.dp))
                            }
                        }
                    }
                }

                if (showAdjustStockSheet && item != null) {
                    AdjustStockSheet(
                        item = item,
                        onDismiss = {
                            showAdjustStockSheet = false
                            sheetBlur = 0.dp
                        },
                        onBlurScrimChange = { radius, _ ->
                            sheetBlur = radius
                        },
                        onSubmit = { type, quantity, reason, notes ->
                            showAdjustStockSheet = false
                            sheetBlur = 0.dp
                            onAdjustStockSubmit(type, quantity, reason, notes)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = Color(0xFFDCFCE7),
    textColor: Color = Color(0xFF10B981),
    dotColor: Color = textColor,
    cornerRadius: Dp = 20.dp,
    dotSize: Dp = 7.dp,
    showDot: Boolean = true
) {
    val tokens = LocalAppTokens.current
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
    modifier: Modifier = Modifier
) {
    StatusBadge(
        text = if (active) "Active" else "Inactive",
        bgColor = if (active) GreenActiveBg else Color(0xFFFBE9E9),
        textColor = if (active) GreenActive else RedText,
        showDot = false,
        modifier = modifier
    )
}

@Composable
private fun OverviewContent(
    item: InventoryItem,
    onAdjustStockClick: () -> Unit,
    onWarehouseTransfer: (InventoryItem) -> Unit,
    onReorderStock: (InventoryItem) -> Unit,
    onMarkInactive: (InventoryItem) -> Unit
) {
    val tokens = LocalAppTokens.current
    val health = item.toHealthDisplay()

    SectionHeader(icon = Icons.Outlined.Inventory2, title = "Item Details")
    Spacer(Modifier.height(tokens.extraPadding - 2.dp))
    InfoRow("Item Type", item.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
    DividerLine()
    InfoRow("Unit", item.unit)
    DividerLine()
    InfoRow("Current Stock", formatQty(item.currentStock))

    Spacer(Modifier.height(tokens.extraPadding * 2))
    SectionHeader(icon = Icons.Outlined.Sell, title = "Sales Information")
    Spacer(Modifier.height(tokens.extraPadding - 2.dp))
    InfoRow("Selling Price", formatCurrency(item.sellingPrice))

    Spacer(Modifier.height(tokens.extraPadding + 6.dp))
    InventoryHealthCard(
        isTracked = health.isTracked,
        statusLabel = item.stockStatus,
        totalStockValue = health.totalStockValue,
        available = health.available,
        reserved = health.reserved,
        wip = health.wip,
        incoming = health.incoming,
        lowThreshold = health.lowThreshold
    )

    Spacer(Modifier.height(tokens.extraPadding * 2))
    SectionHeader(icon = Icons.Outlined.ShoppingCart, title = "Inventory Actions")
    Spacer(Modifier.height(tokens.extraPadding))

    Button(
        onClick = onAdjustStockClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.buttonHeight),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = ButtonDefaults.buttonColors(containerColor = Primary)
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(tokens.iconSize), tint = whiteBg)
        Spacer(Modifier.width(tokens.extraPadding - 4.dp))
        Text("Adjust Stock", fontWeight = FontWeight.Medium, color = whiteBg)
    }
    Spacer(Modifier.height(tokens.extraPadding))
    OutlinedActionButton(
        label = "Warehouse Transfer",
        icon = Icons.Outlined.SwapHoriz,
        onClick = { onWarehouseTransfer(item) }
    )
    Spacer(Modifier.height(tokens.extraPadding))
    OutlinedActionButton(
        label = "Reorder Stock",
        icon = Icons.Outlined.Refresh,
        onClick = { onReorderStock(item) }
    )
    Spacer(Modifier.height(tokens.extraPadding))
    OutlinedActionButton(
        label = "Mark Inactive",
        icon = Icons.Outlined.Block,
        onClick = { onMarkInactive(item) },
        contentColor = redText,
        borderColor = redText
    )

    Spacer(Modifier.height(tokens.extraPadding * 2))
    QuickInsightCard(item = item)
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    val tokens = LocalAppTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(tokens.iconSize))
        Spacer(Modifier.width(tokens.extraPadding - 2.dp))
        Text(title, fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold, color = blackTitle)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.extraPadding),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextGray, fontSize = tokens.bodySmall)
        Text(value, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextGray)
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(color = Color(0xFFEFEFEF), thickness = 1.dp)
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
    lowThreshold: Double
) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(DarkCardBg)
            .padding(tokens.cardPadding - 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    "INVENTORY HEALTH",
                    color = TextGray,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        statusLabel.uppercase(),
                        color = whiteBg,
                        fontSize = tokens.h2,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(GreenActive)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(tokens.iconSize + 8.dp)
                    .clip(CircleShape)
                    .background(GreenActive),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = whiteBg,
                    modifier = Modifier.size(tokens.iconSize - 3.dp)
                )
            }
        }

        Spacer(Modifier.height(tokens.extraPadding + 8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            HealthMetric("Total Stock Value", formatCurrency(totalStockValue), Modifier.weight(1f))
            HealthMetric("Available", formatQty(available), Modifier.weight(1f))
        }
        Spacer(Modifier.height(tokens.extraPadding + 6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            HealthMetric("Reserved", formatQty(reserved), Modifier.weight(1f))
            HealthMetric("WIP", formatQty(wip), Modifier.weight(1f))
        }
        Spacer(Modifier.height(tokens.extraPadding + 6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            HealthMetric("Incoming", formatQty(incoming), Modifier.weight(1f), valueColor = AmberText)
            HealthMetric("Low Threshold", formatQty(lowThreshold), Modifier.weight(1f), valueColor = AmberText)
        }
    }
}

@Composable
private fun HealthMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = whiteBg
) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier) {
        Text(label, color = TextGray, fontSize = tokens.caption)
        Spacer(Modifier.height(4.dp))
        Text(value, color = valueColor, fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun OutlinedActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    contentColor: Color = Color(0xFF2B2F38),
    borderColor: Color = Color(0xFFE2E4E9)
) {
    val tokens = LocalAppTokens.current
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.buttonHeight),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(tokens.iconSize))
        Spacer(Modifier.width(tokens.extraPadding - 4.dp))
        Text(label, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun QuickInsightCard(item: InventoryItem) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius - 3.dp))
            .background(PurpleLight)
            .padding(tokens.extraPadding + 6.dp)
    ) {
        Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(tokens.iconSize + 2.dp))
        Spacer(Modifier.width(tokens.extraPadding))
        Column {
            Text("Quick Insight", fontWeight = FontWeight.SemiBold, fontSize = tokens.bodyMedium, color = title_color)
            Spacer(Modifier.height(4.dp))
            Text(
                "Demand for ${item.name} has been changing recently. Review reorder point and current stock to avoid stockouts.",
                fontSize = tokens.caption,
                color = Color(0xFF4A4A5A)
            )
        }
    }
}

@Composable
private fun TransactionsPlaceholder() {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = tokens.screenPadding * 3.75f),
        contentAlignment = Alignment.Center
    ) {
        Text("No transactions to show yet.", color = TextGray)
    }
}

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