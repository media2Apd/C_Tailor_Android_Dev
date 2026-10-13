
@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.finance.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.BluePrimary
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.chartAmber
import com.cuso.tailor.ui.theme.chartBlue
import com.cuso.tailor.ui.theme.chartEmerald
import com.cuso.tailor.ui.theme.chartGreenDeep
import com.cuso.tailor.ui.theme.darkGreenBg
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.orangeBg
import com.cuso.tailor.ui.theme.orangeText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_border
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.view.composable.TitleBar

// =============================================================
// UI MODELS
// =============================================================

private data class FinanceKpi(
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val value: String,
    val trend: String? = null,
    val badge: String? = null
)

private data class DistributionItem(val label: String, val percent: Float, val amount: String, val color: Color)
private data class BankItem(val name: String, val amount: String, val color: Color)
private data class ExpenseCategory(val label: String, val percent: Float, val color: Color)
private data class TransactionItem(
    val name: String,
    val tag: String,
    val reference: String,
    val date: String,
    val amount: String,
    val status: String
)

private data class SummaryItem(
    val title: String,
    val firstLabel: String,
    val firstValue: String,
    val secondLabel: String,
    val secondValue: String,
    val actionText: String
)

private data class AlertItem(
    val icon: ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val title: String,
    val subtitle: String,
    val count: String,
    val countBg: Color,
    val countFg: Color
)

// =============================================================
// DATA
// =============================================================

private val kpiItems = listOf(
    FinanceKpi(Icons.Outlined.AccountBalanceWallet, primary_light, BluePrimary, "Total Receivables", "₹ 2.48 Cr", trend = "12.8%"),
    FinanceKpi(Icons.Outlined.ShoppingCart, redBg, redText, "Total Payables", "₹ 1.76 Cr", trend = "8.4%"),
    FinanceKpi(Icons.Outlined.AccountBalance, primary_light, BluePrimary, "Total Funds", "₹ 3.92 Cr", trend = "5.6%"),
    FinanceKpi(Icons.AutoMirrored.Outlined.TrendingUp, greenBg, darkGreenBg, "Monthly Collected", "₹ 1.82 Cr", trend = "15.3%"),
    FinanceKpi(Icons.Outlined.CreditCard, orangeBg, orangeText, "Monthly Expenses", "₹ 1.05 Cr", trend = "6.1%"),
    FinanceKpi(Icons.Default.Warning, orangeBg, orangeText, "Pending Approvals", "28 Tasks", badge = "Outstanding")
)

private val inflowPoints = listOf(1.2f, 1.8f, 1.1f, 2.2f, 1.6f, 1.8f)
private val outflowPoints = listOf(0.9f, 1.4f, 0.8f, 1.3f, 1.15f, 1.2f)
private val cashFlowXLabels = listOf("Jul 24", "", "Nov 24", "Mar 25", "May 25", "")
private val cashFlowPeriods = listOf("Last 6 Month", "Last 12 Month", "Last 24 Month")

private val distributionItems = listOf(
    DistributionItem("Receivables", 27f, "₹2.48 Cr", BluePrimary),
    DistributionItem("Payables", 19f, "₹1.76 Cr", redText),
    DistributionItem("Expenses", 11f, "₹1.05 Cr", chartAmber),
    DistributionItem("Funds / Cash", 43f, "₹3.92 Cr", greentext)
)

private val bankItems = listOf(
    BankItem("HDFC - Main Campus", "₹ 1.56 Cr", chartBlue),
    BankItem("ICICI - City Campus", "₹ 1.12 Cr", BluePrimary),
    BankItem("SBI - North", "₹ 78.40 L", greentext),
    BankItem("Cash In Hand", "₹ 78,000", greentext)
)

private val expenseCategories = listOf(
    ExpenseCategory("Salaries", 40f, chartBlue),
    ExpenseCategory("Administration", 25f, chartAmber),
    ExpenseCategory("Academics", 20f, chartEmerald),
    ExpenseCategory("Utilities", 15f, chartGreenDeep)
)

private val transactionItems = listOf(
    TransactionItem("Rahul Sharma", "Invoice Received", "INV-2025-186", "30 May 2025", "₹ 68,250", "Open"),
    TransactionItem("Ananya Gupta", "Receipt Collected", "INV-2025-186", "29 May 2025", "₹ 1,25,000", "Completed"),
    TransactionItem("Bright Future Books", "Salary Payment", "INV-2025-186", "28 May 2025", "₹ 75,430", "Completed"),
    TransactionItem("Neha Verma", "Expense Reimb.", "INV-2025-186", "27 May 2025", "₹ 18,756", "Pending")
)

private val summaryItems = listOf(
    SummaryItem("Sales", "Total Sales", "₹ 4.82 Cr", "Pending Invoices", "12", "Go to Sales"),
    SummaryItem("Purchase", "Total Purchase", "₹ 2.14 Cr", "Pending POs", "8", "Go to Purchase"),
    SummaryItem("General Ledger", "Total Entries", "1,248", "Reconciled", "98%", "Go to Ledger"),
    SummaryItem("Expenses", "Total Expenses", "₹ 1.05 Cr", "Pending Claims", "24", "Go to Expenses"),
    SummaryItem("Funds", "Total Funds", "₹ 3.92 Cr", "Available Balance", "₹ 2.18 Cr", "Go to Funds")
)

private val alertItems = listOf(
    AlertItem(Icons.Default.Warning, redBg, redText, "Supplier Payments Due", "Due in next 7 days", "9", redBg, redText),
    AlertItem(Icons.Default.Schedule, orangeBg, orangeText, "Expense Claims", "Awaiting manager approval", "24", orangeBg, orangeText),
    AlertItem(Icons.Default.AccessTime, primary_light, BluePrimary, "Bank Reconciliation", "Unreconciled items remaining", "27", primary_light, BluePrimary)
)

// =============================================================
// SCREEN
// =============================================================

@Composable
fun FinanceOverviewScreen(
    onClose: () -> Unit = {},
    onViewDistribution: () -> Unit = {},
    onViewFunds: () -> Unit = {},
    onSummaryView: (String) -> Unit = {},
    onGoTo: (String) -> Unit = {},
    onAlertClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(title = "Finance Overview", onClose = onClose)
        HorizontalDivider(color = title_border)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
        ) {
            SectionTitle("Key Metrics")

            kpiItems.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
                ) {
                    pair.forEach { item ->
                        FinanceKpiCard(item = item, modifier = Modifier.weight(1f))
                    }
                }
            }

            PendingActionsCard(label = "Pending Actions", count = "08")

            CashFlowTrendsCard()

            FinanceDistributionCard(onViewClick = onViewDistribution)

            BankSnapshotCard(onViewFunds = onViewFunds)

            SectionTitle("Recent Transactions")
            RecentTransactionsCard()

            SectionTitle("Summary Areas")
            summaryItems.forEach { item ->
                SummaryAreaCard(
                    item = item,
                    onView = { onSummaryView(item.title) },
                    onGoTo = { onGoTo(item.title) }
                )
            }

            SectionTitle("Pending Actions & Alerts")
            alertItems.forEach { item ->
                AlertCard(item = item, onClick = { onAlertClick(item.title) })
            }
        }
    }
}

// =============================================================
// KEY METRICS
// =============================================================

@Composable
private fun FinanceKpiCard(item: FinanceKpi, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    FinanceCard(modifier = modifier.fillMaxHeight(), contentPadding = 14.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FinanceIconBox(icon = item.icon, bg = item.iconBg, tint = item.iconTint)
            when {
                item.trend != null -> Text(
                    text = "▲ ${item.trend}",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = greentext
                )
                item.badge != null -> Text(
                    text = item.badge,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = chartAmber
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(text = item.title, fontSize = tokens.bodySmall, color = mutedText, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.value,
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun PendingActionsCard(label: String, count: String) {
    val tokens = LocalAppTokens.current
    FinanceCard(contentPadding = 14.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1.5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FinanceIconBox(icon = Icons.Default.Warning, bg = orangeBg, tint = orangeText)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = label,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1
                )
            }
            Text(
                text = count,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// =============================================================
// CASH FLOW TRENDS
// =============================================================

@Composable
private fun CashFlowTrendsCard() {
    val tokens = LocalAppTokens.current
    var expanded by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("Last 12 Month") }

    val inflowColor = BluePrimary
    val outflowColor = BluePrimary.copy(alpha = 0.4f)
    val yLabels = listOf("₹3.0 Cr", "₹2.0 Cr", "₹1.0 Cr", "")
    val maxValue = 3f

    FinanceCard(contentPadding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardTitle("Cash Flow Trends")
            Box {
                Row(
                    modifier = Modifier.clickable { expanded = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedPeriod.uppercase(),
                        fontSize = tokens.caption,
                        color = headerGrey,
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = headerGrey,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(whiteBg)
                ) {
                    cashFlowPeriods.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = tokens.bodySmall, color = TextPrimary) },
                            onClick = {
                                selectedPeriod = option
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendDot(color = inflowColor, label = "Inflow")
            LegendDot(color = outflowColor, label = "Outflow")
        }
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            Column(
                modifier = Modifier.width(48.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yLabels.forEach { Text(text = it, fontSize = tokens.label, color = mutedText) }
            }
            Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val w = size.width
                val h = size.height
                for (i in 0..3) {
                    val y = h * i / 3f
                    drawLine(grey_border, Offset(0f, y), Offset(w, y), 1.dp.toPx())
                }
                val slot = w / inflowPoints.size
                fun offsets(values: List<Float>) = values.mapIndexed { i, v ->
                    Offset(slot * (i + 0.5f), h * (1f - (v / maxValue).coerceIn(0f, 1f)))
                }
                drawPath(
                    smoothPath(offsets(outflowPoints)),
                    outflowColor,
                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    smoothPath(offsets(inflowPoints)),
                    inflowColor,
                    style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(start = 48.dp)) {
            cashFlowXLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = tokens.label,
                    color = headerGrey,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// =============================================================
// FINANCE DISTRIBUTION
// =============================================================

@Composable
private fun FinanceDistributionCard(onViewClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    FinanceCard(contentPadding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardTitle("Finance Distribution")
            LinkText(text = "View", onClick = onViewClick)
        }
        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            distributionItems.forEach { item ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(item.color)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.label,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${item.percent.toInt()}% • ${item.amount}",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            distributionItems.forEach { item ->
                Box(
                    modifier = Modifier
                        .weight(item.percent)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(item.color)
                )
            }
            Box(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(light_grey)
            )
        }
    }
}

// =============================================================
// BANK SNAPSHOT
// =============================================================

@Composable
private fun BankSnapshotCard(onViewFunds: () -> Unit) {
    val tokens = LocalAppTokens.current
    FinanceCard(contentPadding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardTitle("Bank Snapshot")
            LinkText(text = "View Funds", onClick = onViewFunds)
        }
        Spacer(Modifier.height(12.dp))
        Text(text = "Total Cash & Bank Balance", fontSize = tokens.caption, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Text(
            text = "₹ 3.92 Cr",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            bankItems.forEach { bank ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(bank.color))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = bank.name,
                        fontSize = tokens.bodySmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = bank.amount,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(text = "Expense by Category", fontSize = tokens.bodySmall, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            ExpenseDonut(segments = expenseCategories)
            Spacer(Modifier.width(32.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                expenseCategories.forEach { cat ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = cat.label,
                            fontSize = tokens.caption,
                            color = headerGrey,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "${cat.percent.toInt()}%", fontSize = tokens.caption, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseDonut(
    segments: List<ExpenseCategory>,
    diameter: Dp = 76.dp,
    strokeWidth: Dp = 16.dp
) {
    Canvas(modifier = Modifier.size(diameter)) {
        val strokePx = strokeWidth.toPx()
        val arcSize = Size(size.width - strokePx, size.height - strokePx)
        val topLeft = Offset(strokePx / 2f, strokePx / 2f)
        val total = segments.sumOf { it.percent.toDouble() }.toFloat()
        val gap = 3f
        var angle = -90f
        segments.forEach { seg ->
            val full = seg.percent / total * 360f
            drawArc(
                color = seg.color,
                startAngle = angle + gap / 2f,
                sweepAngle = (full - gap).coerceAtLeast(0f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokePx)
            )
            angle += full
        }
    }
}

// =============================================================
// RECENT TRANSACTIONS
// =============================================================

@Composable
private fun RecentTransactionsCard() {
    FinanceCard(contentPadding = 0.dp) {
        transactionItems.forEachIndexed { index, item ->
            TransactionRow(item)
            if (index < transactionItems.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = grey_border)
            }
        }
    }
}

@Composable
private fun TransactionRow(item: TransactionItem) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(6.dp))
                FinanceTag(text = item.tag, bg = primary_light, fg = BluePrimary, shape = RoundedCornerShape(4.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(text = item.reference, fontSize = tokens.caption, color = mutedText)
            Spacer(Modifier.height(2.dp))
            Text(text = item.date, fontSize = tokens.caption, color = mutedText)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.amount,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            val (bg, fg) = transactionStatusColors(item.status)
            FinanceTag(text = item.status, bg = bg, fg = fg, shape = RoundedCornerShape(50))
        }
    }
}

private fun transactionStatusColors(status: String): Pair<Color, Color> = when (status.lowercase()) {
    "completed" -> greenBg to greentext
    "open" -> orangeBg to orangeText
    "pending" -> yellowBg to yellowText
    else -> light_grey to headerGrey
}

// =============================================================
// SUMMARY AREAS
// =============================================================

@Composable
private fun SummaryAreaCard(item: SummaryItem, onView: () -> Unit, onGoTo: () -> Unit) {
    val tokens = LocalAppTokens.current
    FinanceCard(contentPadding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardTitle(item.title)
            Box(modifier = Modifier.clickable { onView() }) {
                FinanceTag(text = "View", bg = primary_light, fg = BluePrimary, shape = RoundedCornerShape(4.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(text = item.firstLabel, fontSize = tokens.bodySmall, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.firstValue,
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(Modifier.height(10.dp))
        Text(text = item.secondLabel, fontSize = tokens.bodySmall, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.secondValue,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "${item.actionText} →",
                fontSize = tokens.bodySmall,
                color = BluePrimary,
                modifier = Modifier.clickable { onGoTo() }
            )
        }
    }
}

// =============================================================
// PENDING ACTIONS & ALERTS
// =============================================================

@Composable
private fun AlertCard(item: AlertItem, onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    FinanceCard(contentPadding = 14.dp, modifier = Modifier.clickable { onClick() }) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(item.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = item.iconTint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = item.subtitle, fontSize = tokens.caption, color = mutedText, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            FinanceTag(text = item.count, bg = item.countBg, fg = item.countFg, shape = RoundedCornerShape(4.dp))
        }
    }
}

// =============================================================
// SHARED COMPONENTS
// =============================================================

@Composable
private fun FinanceCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
private fun SectionTitle(text: String) {
    val tokens = LocalAppTokens.current
    Text(
        text = text,
        fontSize = tokens.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
    )
}

@Composable
private fun CardTitle(text: String) {
    val tokens = LocalAppTokens.current
    Text(
        text = text,
        fontSize = tokens.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun LinkText(text: String, onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    Text(
        text = text,
        fontSize = tokens.bodySmall,
        fontWeight = FontWeight.Normal,
        color = BluePrimary,
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
private fun FinanceIconBox(icon: ImageVector, bg: Color, tint: Color, boxSize: Dp = 36.dp) {
    Box(
        modifier = Modifier.size(boxSize).clip(RoundedCornerShape(10.dp)).background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun FinanceTag(text: String, bg: Color, fg: Color, shape: Shape) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = tokens.label,
            fontWeight = FontWeight.Medium,
            color = fg,
            maxLines = 1
        )
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    val tokens = LocalAppTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(text = label, fontSize = tokens.caption, color = headerGrey)
    }
}

// =============================================================
// HELPERS
// =============================================================

private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
        val p0 = points[i - 1]
        val p1 = points[i]
        val cx = (p0.x + p1.x) / 2f
        path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
    }
    return path
}