@file:Suppress("unused", "SpellCheckingInspection")

package com.cuso.tailor.view.home.finance.dashboard

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.finance.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.FinanceViewModel
import androidx.core.graphics.toColorInt
import com.cuso.tailor.view.composable.FinanceDashboardSkeleton

// =============================================================
// SCREEN (DYNAMIC API DATA WITH SMOOTH ANIMATIONS)
// =============================================================

@SuppressLint("DefaultLocale")
@Composable
fun FinanceOverviewScreen(
    viewModel: FinanceViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onViewDistribution: () -> Unit = {},
    onViewFunds: () -> Unit = {},
    onSummaryView: (String) -> Unit = {},
    onGoTo: (String) -> Unit = {},
    onAlertClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // Observe API States
    val kpiData by viewModel.kpiData.collectAsState()
    val cashFlowData by viewModel.cashFlowData.collectAsState()
    val distributionData by viewModel.distributionData.collectAsState()
    val salesOverview by viewModel.salesOverview.collectAsState()
    val purchaseOverview by viewModel.purchaseOverview.collectAsState()
    val ledgerOverview by viewModel.ledgerOverview.collectAsState()
    val expensesOverview by viewModel.expensesOverview.collectAsState()
    val fundsOverview by viewModel.fundsOverview.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val alertsList by viewModel.alertsList.collectAsState()
    val bankSnapshot by viewModel.bankSnapshot.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Trigger staggered entry animations when screen loads
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(title = "Finance Overview", onClose = onClose)
        HorizontalDivider(color = title_border)

        AnimatedContent(
            targetState = isLoading && kpiData == null,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "loading_state_transition"
        ) { loading ->
            if (loading) {
                FinanceDashboardSkeleton() // <-- Exact Finance Skeleton
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
                ) {
                    // Staggered Section 1: KPI Cards
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 50 }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)) {
                            SectionTitle("Key Metrics")

                            Row(
                                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
                            ) {
                                FinanceKpiCard(
                                    icon = Icons.Outlined.AccountBalanceWallet,
                                    iconBg = primary_light,
                                    iconTint = BluePrimary,
                                    title = "Total Receivables",
                                    value = "₹ ${formatAmount(kpiData?.totalReceivables?.amount ?: 0.0)}",
                                    trend = kpiData?.totalReceivables?.salesGrowth,
                                    modifier = Modifier.weight(1f)
                                )
                                FinanceKpiCard(
                                    icon = Icons.Outlined.ShoppingCart,
                                    iconBg = redBg,
                                    iconTint = redText,
                                    title = "Total Payables",
                                    value = "₹ ${formatAmount(kpiData?.totalPayables?.amount ?: 0.0)}",
                                    trend = kpiData?.totalPayables?.billingGrowth,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
                            ) {
                                FinanceKpiCard(
                                    icon = Icons.Outlined.AccountBalance,
                                    iconBg = primary_light,
                                    iconTint = BluePrimary,
                                    title = "Total Funds",
                                    value = "₹ ${formatAmount(kpiData?.totalFunds?.amount ?: 0.0)}",
                                    trend = kpiData?.totalFunds?.growth,
                                    modifier = Modifier.weight(1f)
                                )
                                FinanceKpiCard(
                                    icon = Icons.AutoMirrored.Outlined.TrendingUp,
                                    iconBg = greenBg,
                                    iconTint = darkGreenBg,
                                    title = "Monthly Collected",
                                    value = "₹ ${formatAmount(kpiData?.periodCollected?.amount ?: 0.0)}",
                                    trend = kpiData?.periodCollected?.growth,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
                            ) {
                                FinanceKpiCard(
                                    icon = Icons.Outlined.CreditCard,
                                    iconBg = orangeBg,
                                    iconTint = orangeText,
                                    title = "Monthly Expenses",
                                    value = "₹ ${formatAmount(kpiData?.periodExpenses?.amount ?: 0.0)}",
                                    trend = kpiData?.periodExpenses?.growth,
                                    modifier = Modifier.weight(1f)
                                )
                                FinanceKpiCard(
                                    icon = Icons.Default.Warning,
                                    iconBg = orangeBg,
                                    iconTint = orangeText,
                                    title = "Pending Approvals",
                                    value = "${kpiData?.pendingApprovals?.total ?: 0} Tasks",
                                    badge = "Outstanding",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            PendingActionsCard(
                                label = "Pending Approvals",
                                count = String.format("%02d", kpiData?.pendingApprovals?.total ?: 0)
                            )
                        }
                    }

                    // Staggered Section 2: Cash Flow Trends
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 60 }
                    ) {
                        CashFlowTrendsCard(cashFlowData = cashFlowData)
                    }

                    // Staggered Section 3: Finance Distribution
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 70 }
                    ) {
                        FinanceDistributionCard(
                            distributionData = distributionData,
                            onViewClick = onViewDistribution
                        )
                    }

                    // Staggered Section 4: Bank Snapshot
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { 80 }
                    ) {
                        BankSnapshotCard(
                            snapshot = bankSnapshot,
                            categories = expenseCategories,
                            onViewFunds = onViewFunds
                        )
                    }

                    // Staggered Section 5: Recent Transactions
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 90 }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                            SectionTitle("Recent Transactions")
                            RecentTransactionsCard(transactions = recentTransactions)
                        }
                    }

                    // Staggered Section 6: Summary Areas
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(900)) + slideInVertically(tween(900)) { 100 }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                            SectionTitle("Summary Areas")
                            SummaryAreaCard(
                                title = "Sales",
                                firstLabel = "Customers",
                                firstValue = "${salesOverview?.summary?.get("customers") ?: 0}",
                                secondLabel = "Sales Invoices",
                                secondValue = "${salesOverview?.summary?.get("salesInvoices") ?: 0}",
                                actionText = "Go to Sales",
                                onView = { onSummaryView("Sales") },
                                onGoTo = { onGoTo("Sales") }
                            )
                            SummaryAreaCard(
                                title = "Purchase",
                                firstLabel = "Suppliers",
                                firstValue = "${purchaseOverview?.summary?.get("suppliers") ?: 0}",
                                secondLabel = "Purchase Invoices",
                                secondValue = "${purchaseOverview?.summary?.get("purchaseInvoices") ?: 0}",
                                actionText = "Go to Purchase",
                                onView = { onSummaryView("Purchase") },
                                onGoTo = { onGoTo("Purchase") }
                            )
                            SummaryAreaCard(
                                title = "General Ledger",
                                firstLabel = "Chart of Accounts",
                                firstValue = "${ledgerOverview?.summary?.get("chartOfAccounts") ?: 0}",
                                secondLabel = "Journal Entries",
                                secondValue = "${ledgerOverview?.summary?.get("journalEntries") ?: 0}",
                                actionText = "Go to Ledger",
                                onView = { onSummaryView("General Ledger") },
                                onGoTo = { onGoTo("General Ledger") }
                            )
                            SummaryAreaCard(
                                title = "Expenses",
                                firstLabel = "Submitted",
                                firstValue = "${expensesOverview?.summary?.get("submitted") ?: 0}",
                                secondLabel = "Pending Claims",
                                secondValue = "${expensesOverview?.summary?.get("pendingClaims") ?: 0}",
                                actionText = "Go to Expenses",
                                onView = { onSummaryView("Expenses") },
                                onGoTo = { onGoTo("Expenses") }
                            )
                            SummaryAreaCard(
                                title = "Funds",
                                firstLabel = "Bank Accounts",
                                firstValue = "${fundsOverview?.summary?.get("bankAccounts") ?: 0}",
                                secondLabel = "Unreconciled",
                                secondValue = "${fundsOverview?.summary?.get("unreconciled") ?: 0}",
                                actionText = "Go to Funds",
                                onView = { onSummaryView("Funds") },
                                onGoTo = { onGoTo("Funds") }
                            )
                        }
                    }

                    // Staggered Section 7: Pending Actions & Alerts
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { 110 }
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                            SectionTitle("Pending Actions & Alerts")
                            alertsList.forEach { alert ->
                                AlertCard(
                                    title = alert.title,
                                    subtitle = alert.description,
                                    count = alert.count.toString(),
                                    badgeColorHex = alert.badgeColor,
                                    onClick = { onAlertClick(alert.title) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// ANIMATED KPI CARD COMPONENT
// =============================================================

@Composable
private fun FinanceKpiCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    value: String,
    trend: String? = null,
    badge: String? = null
) {
    val tokens = LocalAppTokens.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "kpi_card_scale"
    )

    FinanceCard(
        modifier = modifier
            .fillMaxHeight()
            .scale(scale),
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FinanceIconBox(icon = icon, bg = iconBg, tint = iconTint)
            when {
                trend != null -> Text(
                    text = "▲ $trend",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = greentext
                )
                badge != null -> Text(
                    text = badge,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = chartAmber
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(text = title, fontSize = tokens.bodySmall, color = mutedText, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
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
// ANIMATED CASH FLOW TRENDS CARD
// =============================================================

@Composable
private fun CashFlowTrendsCard(cashFlowData: CashFlowData?) {
    val tokens = LocalAppTokens.current
    val points = cashFlowData?.chartData.orEmpty()
    val inflowPoints = points.map { it.inflow.toFloat() }
    val outflowPoints = points.map { it.outflow.toFloat() }
    val maxVal = (inflowPoints + outflowPoints).maxOrNull()?.takeIf { it > 0 } ?: 1000f

    val inflowColor = BluePrimary
    val outflowColor = BluePrimary.copy(alpha = 0.4f)

    // Animated drawing progress from 0% to 100%
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "cash_flow_progress"
    )

    FinanceCard(contentPadding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardTitle("Cash Flow Trends")
            Text(
                text = cashFlowData?.summary?.status ?: "Surplus",
                fontSize = tokens.caption,
                fontWeight = FontWeight.SemiBold,
                color = if (cashFlowData?.summary?.status == "Surplus") greentext else redText
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendDot(color = inflowColor, label = "Inflow")
            LegendDot(color = outflowColor, label = "Outflow")
        }
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                for (i in 0..3) {
                    val y = h * i / 3f
                    drawLine(grey_border, Offset(0f, y), Offset(w, y), 1.dp.toPx())
                }

                if (points.isNotEmpty()) {
                    val slot = w / points.size
                    fun offsets(values: List<Float>) = values.mapIndexed { i, v ->
                        val animatedY = h * (1f - ((v * animatedProgress) / maxVal).coerceIn(0f, 1f))
                        Offset(slot * (i + 0.5f), animatedY)
                    }
                    if (outflowPoints.any { it > 0 }) {
                        drawPath(smoothPath(offsets(outflowPoints)), outflowColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                    }
                    if (inflowPoints.any { it > 0 }) {
                        drawPath(smoothPath(offsets(inflowPoints)), inflowColor, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            points.forEach { pt ->
                Text(
                    text = pt.periodLabel.take(3),
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
// ANIMATED FINANCE DISTRIBUTION
// =============================================================

@Composable
private fun FinanceDistributionCard(
    distributionData: FinanceDistributionData?,
    onViewClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val items = distributionData?.distribution.orEmpty()

    // Smooth animate bar width filling
    var animateBar by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateBar = true
    }
    val barProgress by animateFloatAsState(
        targetValue = if (animateBar) 1f else 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "distribution_bar_anim"
    )

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
            items.forEach { item ->
                val itemColor = parseColorSafely(item.color)
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(itemColor)
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
                            text = "${item.percentage.toInt()}% • ₹${formatAmount(item.amount)}",
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
            items.forEach { item ->
                val targetWeight = (item.percentage * barProgress).coerceAtLeast(0.01f)
                Box(
                    modifier = Modifier
                        .weight(targetWeight)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(parseColorSafely(item.color))
                )
            }
        }
    }
}

// =============================================================
// BANK SNAPSHOT CARD
// =============================================================

@Composable
private fun BankSnapshotCard(
    snapshot: BankSnapshotData?,
    categories: List<CategoryExpenseSlice>,
    onViewFunds: () -> Unit
) {
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
            text = "₹ ${formatAmount(snapshot?.totalBalance ?: 0.0)}",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            snapshot?.accounts.orEmpty().forEach { bank ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(parseColorSafely(bank.color)))
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
                        text = "₹ ${formatAmount(bank.balance)}",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

// =============================================================
// RECENT TRANSACTIONS CARD
// =============================================================

@Composable
private fun RecentTransactionsCard(transactions: List<RecentTransactionDto>) {
    FinanceCard(contentPadding = 0.dp) {
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No recent transactions found", color = mutedText)
            }
        } else {
            transactions.forEachIndexed { index, item ->
                TransactionRow(item)
                if (index < transactions.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = grey_border)
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(item: RecentTransactionDto) {
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
                    text = item.party.ifBlank { item.type },
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(6.dp))
                FinanceTag(text = item.type, bg = primary_light, fg = BluePrimary, shape = RoundedCornerShape(4.dp))
            }
            Spacer(Modifier.height(4.dp))
            Text(text = item.reference, fontSize = tokens.caption, color = mutedText)
            Spacer(Modifier.height(2.dp))
            Text(text = item.date.take(10), fontSize = tokens.caption, color = mutedText)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹ ${formatAmount(item.amount)}",
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
    "completed", "paid" -> greenBg to greentext
    "open", "sent" -> orangeBg to orangeText
    "pending", "draft" -> yellowBg to yellowText
    else -> light_grey to headerGrey
}

// =============================================================
// SUMMARY AREA CARD WITH TAP ANIMATION
// =============================================================

@Composable
private fun SummaryAreaCard(
    title: String,
    firstLabel: String,
    firstValue: String,
    secondLabel: String,
    secondValue: String,
    actionText: String,
    onView: () -> Unit,
    onGoTo: () -> Unit
) {
    val tokens = LocalAppTokens.current
    FinanceCard(contentPadding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardTitle(title)
            Box(modifier = Modifier.clickable { onView() }) {
                FinanceTag(text = "View", bg = primary_light, fg = BluePrimary, shape = RoundedCornerShape(4.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(text = firstLabel, fontSize = tokens.bodySmall, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Text(
            text = firstValue,
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Spacer(Modifier.height(10.dp))
        Text(text = secondLabel, fontSize = tokens.bodySmall, color = mutedText)
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = secondValue,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = "$actionText →",
                fontSize = tokens.bodySmall,
                color = BluePrimary,
                modifier = Modifier.clickable { onGoTo() }
            )
        }
    }
}

// =============================================================
// ALERT CARD COMPONENT WITH BOUNCE PRESS ANIMATION
// =============================================================

@Composable
private fun AlertCard(
    title: String,
    subtitle: String,
    count: String,
    badgeColorHex: String,
    onClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val parsedColor = parseColorSafely(badgeColorHex)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "alert_scale"
    )

    FinanceCard(
        contentPadding = 14.dp,
        modifier = Modifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(parsedColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = parsedColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = subtitle, fontSize = tokens.caption, color = mutedText, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            FinanceTag(text = count, bg = parsedColor.copy(alpha = 0.15f), fg = parsedColor, shape = RoundedCornerShape(4.dp))
        }
    }
}

// =============================================================
// SHARED COMPONENTS & HELPERS
// =============================================================

@Composable
private fun FinanceCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
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

@SuppressLint("DefaultLocale")
private fun formatAmount(amount: Double): String {
    return String.format("%,.0f", amount)
}

private fun parseColorSafely(hexColor: String): Color {
    return try {
        Color(hexColor.toColorInt())
    } catch (_: Exception) {
        BluePrimary
    }
}