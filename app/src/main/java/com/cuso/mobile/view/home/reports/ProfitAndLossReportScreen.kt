
@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable"
)
package com.cuso.mobile.view.home.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.blurScrim
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.redBg
import com.cuso.mobile.ui.theme.title_color

// ── Design tokens (screen-specific accent colors; sizing/spacing comes from LocalAppTokens) ──
private val ReportPrimary = Primary
private val CriticalRed = redBg
private val WarningOrange = Color(0xFFF59E0B)
private val HealthyGreen = greentext
private val TitleColor = title_color
private val MutedColor = mutedText
private val BorderColor = PrimaryBorder
private val NetProfitBg = Color(0xFFD1FAE5)

// ── Data Models ──
private data class PnlStat(
    val label: String,
    val value: String,
    val valueColor: Color = TitleColor
)

private data class PnlLine(
    val label: String,
    val value: String,
    val valueColor: Color = TitleColor
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitAndLossReportScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var exportSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var exportBlur by remember { mutableStateOf(0.dp) }

    var warehouseExpanded by remember { mutableStateOf(false) }
    var selectedWarehouse by remember { mutableStateOf("ABC International") }
    val warehouses = listOf("ABC International", "Chennai Central", "Metro Fabric World")

    val topStats = listOf(
        PnlStat("Revenue", "\u20B960,200", HealthyGreen),
        PnlStat("Expenses", "\u20B931,380", CriticalRed),
        PnlStat("Net Profit", "\u20B927,520", HealthyGreen),
        PnlStat("Profit Margin", "45.7%", ReportPrimary)
    )

    val revenueLines = listOf(
        PnlLine("Sales Revenue", "\u20B942,500"),
        PnlLine("Service Revenue", "\u20B912,200"),
        PnlLine("Interest Income", "\u20B93,000"),
        PnlLine("Other Income", "\u20B92,500")
    )

    val expenseLines = listOf(
        PnlLine("Advertising", "\u20B95,200"),
        PnlLine("Freight", "\u20B93,800"),
        PnlLine("Depreciation", "\u20B92,400"),
        PnlLine("Insurance", "\u20B91,800"),
        PnlLine("Office Supplies", "\u20B9950"),
        PnlLine("Rent", "\u20B96,500"),
        PnlLine("Maintenance", "\u20B92,100"),
        PnlLine("Travel", "\u20B93,400"),
        PnlLine("Utilities", "\u20B92,230"),
        PnlLine("Other Expenses", "\u20B93,000")
    )

    val summaryLines = listOf(
        PnlLine("Operating Profit", "\u20B928,820", HealthyGreen),
        PnlLine("Gross Margin", "52.1%"),
        PnlLine("Net Margin", "45.7%"),
        PnlLine("Compared With", "February 2026"),
        PnlLine("Growth", "+8.2%", HealthyGreen)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                    Column {
                        TitleBar("Profit & Loss Report", onClose = onClose)
                    }
                }
                HorizontalDivider(color = BorderColor)
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blurScrim(exportBlur)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent),
                        contentPadding = PaddingValues(bottom = tokens.screenPadding * 1.5f)
                    ) {
                        // ── Breadcrumb + Export ──
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically

                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ScreenBreadcrumb(
                                        listOf("Reports", "Finance", "Profit & Loss Report"),
                                        onClick = { onBreadCrumbClick() }
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .clickable { exportSheetState = SheetValue.Collapsed }
                                        .padding(horizontal = tokens.screenPadding / 2, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.FileUpload,
                                        contentDescription = "Export",
                                        tint = ReportPrimary,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                    Text(
                                        "Export",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ReportPrimary
                                    )
                                }
                            }
                        }

                        // ── Warehouse selector ──
                        item {
                            Column(Modifier.fillMaxWidth()) {
                                Row(Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
                                    Text(
                                        "Warehouse",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MutedColor
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(tokens.fieldHeight)
                                                .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                                                .border(
                                                    1.dp,
                                                    Color(0xFFE5E7EB),
                                                    RoundedCornerShape(tokens.cardCornerRadius / 1.5f)
                                                )
                                                .clickable { warehouseExpanded = true }
                                                .padding(horizontal = tokens.screenPadding * 0.85f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                selectedWarehouse,
                                                fontSize = tokens.bodyMedium,
                                                color = TitleColor
                                            )
                                            Icon(
                                                Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = MutedColor,
                                                modifier = Modifier.size(tokens.iconSize)
                                            )
                                        }
                                        DropdownMenu(
                                            expanded = warehouseExpanded,
                                            onDismissRequest = { warehouseExpanded = false }
                                        ) {
                                            warehouses.forEach { wh ->
                                                DropdownMenuItem(
                                                    text = { Text(wh) },
                                                    onClick = {
                                                        selectedWarehouse = wh
                                                        warehouseExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(tokens.fieldHeight)
                                            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                                            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(tokens.cardCornerRadius / 1.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.FilterList,
                                            contentDescription = "Filter",
                                            tint = TitleColor,
                                            modifier = Modifier.size(tokens.iconSize)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                        }

                        // ── Top stat grid (2x2) ──
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                topStats.chunked(2).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        row.forEach { stat ->
                                            PnlStatCard(stat, Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        // ── Statement title ──
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg)
                                    .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
                            ) {
                                Text(
                                    "Profit & Loss Statement",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TitleColor
                                )
                            }
                            Spacer(Modifier.height(5.dp))
                        }

                        // ── Revenue section ──
                        item {
                            PnlSectionHeading("Revenue")
                        }
                        items(revenueLines) { line ->
                            PnlLineRow(line)
                        }
                        item {
                            PnlTotalRow("Total Revenue & Gains", "\u20B960,200", HealthyGreen)
                            Spacer(Modifier.height(6.dp))
                        }

                        // ── Expenses section ──
                        item {
                            PnlSectionHeading("Expenses")
                        }
                        items(expenseLines) { line ->
                            PnlLineRow(line)
                        }
                        item {
                            PnlTotalRow("Total Expenses", "\u20B931,380", CriticalRed)
                            Spacer(Modifier.height(6.dp))
                        }

                        // ── Income Before Tax section ──
                        item {
                            PnlSectionHeading("Income Before Tax")
                            PnlLineRow(PnlLine("Income Before Tax", "\u20B928,820"))
                            PnlLineRow(PnlLine("Income Tax (4.5%)", "\u20B91,300"))
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = tokens.screenPadding)
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                    .background(NetProfitBg)
                                    .padding(horizontal = tokens.screenPadding, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Net Profit",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthyGreen
                                )
                                Text(
                                    "\u20B927,520",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthyGreen
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                        }

                        // ── Report Summary section ──
                        item {
                            PnlSectionHeading("Report Summary")
                        }
                        items(summaryLines) { line ->
                            PnlLineRow(line)
                        }
                        item { Spacer(Modifier.height(6.dp)) }

                        // ── Notes section ──
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Transparent)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(whiteBg)
                                        .padding(horizontal = tokens.screenPadding, vertical = 14.dp)

                                ) {
                                    Text(
                                        "Notes",
                                        fontSize = tokens.bodyMedium,
                                        color = TitleColor
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    Modifier.fillMaxWidth()
                                        .padding(horizontal = tokens.screenPadding, vertical = 14.dp)

                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                whiteBg,
                                                RoundedCornerShape(tokens.cardCornerRadius / 1.5f)
                                            )
                                            .border(
                                                1.dp,
                                                BorderColor,
                                                RoundedCornerShape(tokens.cardCornerRadius / 1.5f)
                                            )
                                            .padding(tokens.screenPadding * 0.85f)
                                    ) {
                                        Text(
                                            "Revenue increased due to seasonal demand in Q1. Marketing spend optimized by 12% vs. last month.",
                                            fontSize = tokens.bodySmall,
                                            color = MutedColor,
                                            lineHeight = tokens.bodySmall * 1.4f
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Export bottom sheet ──
                SmoothBottomSheet(
                    state = exportSheetState,
                    onStateChange = { exportSheetState = it },
                    peekHeight = 340.dp,
                    topInset = 0.dp,
                    maxBlurRadius = 16.dp,
                    maxScrimAlpha = 0.45f,
                    scrollableContent = false,
                    onBlurScrimChange = { blur, _ -> exportBlur = blur },
                    onDismissRequest = { exportSheetState = SheetValue.Hidden }
                ) {
                    ExportPnlSheetContent(onDismiss = { exportSheetState = SheetValue.Hidden })
                }
            }
        }
    }
}

@Composable
private fun PnlStatCard(stat: PnlStat, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = modifier
            .background(whiteBg)
            .padding(tokens.screenPadding * 0.85f)
    ) {
        Text(stat.label, fontSize = tokens.caption, color = MutedColor, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(stat.value, fontSize = tokens.bodySmall, color = stat.valueColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PnlSectionHeading(title: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = tokens.screenPadding, vertical = 10.dp)
    ) {
        Text(
            title,
            fontSize = tokens.bodyMedium,
            color = ReportPrimary
        )
    }
}

@Composable
private fun PnlLineRow(line: PnlLine) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.background(Color.Transparent)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(line.label, fontSize = tokens.bodySmall, color = MutedColor)
            Text(line.value, fontSize = tokens.bodySmall, color = line.valueColor)
        }
        HorizontalDivider(
            color = BorderColor,
            modifier = Modifier.padding(horizontal = tokens.screenPadding)
        )
    }
}

@Composable
private fun PnlTotalRow(label: String, value: String, valueColor: Color) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = tokens.bodySmall,  color = TitleColor)
        Text(value, fontSize = tokens.bodySmall,  color = valueColor)
    }
}

@Composable
private fun ExportPnlSheetContent(onDismiss: () -> Unit) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
        Text("Export Report", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TitleColor)
        Text("Choose an export option", fontSize = tokens.caption, color = MutedColor)
        Spacer(Modifier.height(12.dp))

        ExportPnlOptionRow(Icons.Default.PictureAsPdf, Color(0xFFEF4444), "Export as PDF") { onDismiss() }
        ExportPnlOptionRow(Icons.Default.GridOn, Color(0xFF16A34A), "Export as Excel") { onDismiss() }
        ExportPnlOptionRow(Icons.Default.Share, Color(0xFF3B82F6), "Share Report") { onDismiss() }
        ExportPnlOptionRow(Icons.Default.Email, Color(0xFF9333EA), "Send by Email") { onDismiss() }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
            shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f)
        ) {
            Text("Cancel", fontSize = tokens.bodyMedium, color = TitleColor, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ExportPnlOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    label: String,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(tokens.fieldHeight * 0.8f)
                    .clip(RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(tokens.iconSize))
            }
            Spacer(Modifier.width(12.dp))
            Text(label, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TitleColor)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MutedColor, modifier = Modifier.size(tokens.iconSize))
    }
}