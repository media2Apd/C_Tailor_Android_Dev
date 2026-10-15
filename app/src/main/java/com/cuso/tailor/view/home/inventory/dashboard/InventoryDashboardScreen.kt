package com.cuso.tailor.view.home.inventory.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.viewmodel.InventoryViewModel

@Composable
fun InventoryDashboardScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    val summary by viewModel.dashboardSummary.collectAsState()
    val stockHealth by viewModel.stockHealth.collectAsState()
    val stockTrends by viewModel.stockTrends.collectAsState()
    val categories by viewModel.categoryBreakdown.collectAsState()
    val fastMoving by viewModel.fastMovingProducts.collectAsState()
    val warehouseHealth by viewModel.warehouseHealthList.collectAsState()
    val slowMoving by viewModel.slowMovingProducts.collectAsState()
    val isLoading by viewModel.isDashboardLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchDashboardOverview()
    }

    Scaffold(
        containerColor = Primary_background
    ) { padding ->
        if (isLoading && summary == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Primary,
                    strokeWidth = 2.5.dp
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                contentPadding = PaddingValues(top = tokens.extraPadding, bottom = tokens.cardPadding)
            ) {
                // Top Header: Overview Title & Live Sync Badge
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overview",
                            fontSize = tokens.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                .background(greenBg)
                                .border(1.dp, darkGreenBg.copy(alpha = 0.25f), RoundedCornerShape(tokens.cardCornerRadius))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(chartEmerald, CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Live sync",
                                color = chartGreenDeep,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Adaptive Metric Summary Cards Grid
                item {
                    val metricCards = listOf(
                        MetricCardUiData(
                            title = "Total Products",
                            value = summary?.totalProducts?.formattedValue?.toString() ?: "0",
                            changePercent = summary?.totalProducts?.changePercent ?: 0.0,
                            isPositive = (summary?.totalProducts?.changePercent ?: 0.0) >= 0,
                            icon = Icons.Outlined.Inventory2,
                            iconTint = chartIndigo,
                            iconBg = primary_light,
                            sparkline = summary?.totalProducts?.sparkline ?: listOf(10.0, 15.0, 25.0)
                        ),
                        MetricCardUiData(
                            title = "Inventory Value",
                            value = summary?.inventoryValue?.displayValue ?: "₹${summary?.inventoryValue?.value ?: 0}",
                            changePercent = summary?.inventoryValue?.changePercent ?: 0.0,
                            isPositive = (summary?.inventoryValue?.changePercent ?: 0.0) >= 0,
                            icon = Icons.Outlined.TrendingUp,
                            iconTint = chartEmerald,
                            iconBg = greenBg,
                            sparkline = summary?.inventoryValue?.sparkline ?: listOf(2.0, 2.2, 2.5)
                        ),
                        MetricCardUiData(
                            title = "Low Stock Items",
                            value = summary?.lowStockItems?.formattedValue?.toString() ?: "0",
                            changePercent = summary?.lowStockItems?.changePercent ?: 0.0,
                            isPositive = false,
                            icon = Icons.Outlined.WarningAmber,
                            iconTint = orangeText,
                            iconBg = orangeBg,
                            sparkline = summary?.lowStockItems?.sparkline ?: listOf(180.0, 162.0)
                        ),
                        MetricCardUiData(
                            title = "Out of Stock",
                            value = summary?.outOfStock?.formattedValue?.toString() ?: "0",
                            changePercent = summary?.outOfStock?.changePercent ?: 0.0,
                            isPositive = false,
                            icon = Icons.Outlined.Block,
                            iconTint = redText,
                            iconBg = redBg,
                            sparkline = summary?.outOfStock?.sparkline ?: listOf(30.0, 37.0)
                        ),
                        MetricCardUiData(
                            title = "Pending POs",
                            value = summary?.pendingPOs?.formattedValue?.toString() ?: "0",
                            changePercent = summary?.pendingPOs?.changePercent ?: 0.0,
                            isPositive = (summary?.pendingPOs?.changePercent ?: 0.0) >= 0,
                            icon = Icons.Outlined.ShoppingBag,
                            iconTint = chartBlue,
                            iconBg = activity_purple_bg,
                            sparkline = summary?.pendingPOs?.sparkline ?: listOf(32.0, 24.0)
                        ),
                        MetricCardUiData(
                            title = "Inventory Accuracy",
                            value = summary?.inventoryAccuracy?.formattedValue?.toString() ?: "0%",
                            changePercent = summary?.inventoryAccuracy?.changePercent ?: 0.0,
                            isPositive = (summary?.inventoryAccuracy?.changePercent ?: 0.0) >= 0,
                            icon = Icons.Outlined.Verified,
                            iconTint = chartViolet,
                            iconBg = purpleBg,
                            sparkline = summary?.inventoryAccuracy?.sparkline ?: listOf(95.0, 98.6)
                        )
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        metricCards.chunked(tokens.gridColumns).forEach { rowCards ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                            ) {
                                rowCards.forEach { card ->
                                    SummaryMetricCard(
                                        modifier = Modifier.weight(1f),
                                        data = card
                                    )
                                }
                                // Placeholder if row items are less than grid columns
                                repeat(tokens.gridColumns - rowCards.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Inventory Value & Stock Trends Chart
                item {
                    InventoryValueStockCard(points = stockTrends?.points ?: emptyList())
                }

                // Warehouse Health Section
                item {
                    WarehouseHealthSectionCard(warehouses = warehouseHealth)
                }

                // Stock Health Donut Chart Section
                item {
                    StockHealthSectionCard(stockHealth = stockHealth)
                }

                // Inventory Categories Breakdown Section
                item {
                    InventoryCategoriesCard(categories = categories)
                }

                // Fast Moving Products Section
                item {
                    FastMovingSectionCard(products = fastMoving)
                }

                // Slow Moving / Dead Stock Section
                item {
                    SlowMovingSectionCard(products = slowMoving)
                }
            }
        }
    }
}

// =============================================================================
// DATA HOLDER FOR METRIC CARD
// =============================================================================
data class MetricCardUiData(
    val title: String,
    val value: String,
    val changePercent: Double,
    val isPositive: Boolean,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color,
    val sparkline: List<Double>
)

// =============================================================================
// SUB-COMPONENTS
// =============================================================================

@Composable
fun SummaryMetricCard(
    modifier: Modifier = Modifier,
    data: MetricCardUiData
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBorder))
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(data.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = null,
                        tint = data.iconTint,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                MiniSparkline(
                    data = data.sparkline,
                    lineColor = data.iconTint,
                    modifier = Modifier
                        .width(52.dp)
                        .height(24.dp)
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = data.title,
                fontSize = tokens.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = data.value,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))

            val trendColor = if (data.isPositive) greentext else redText
            val trendIcon = if (data.isPositive) "↑" else "↓"
            Text(
                text = "$trendIcon ${kotlin.math.abs(data.changePercent)}% vs last month",
                fontSize = tokens.label,
                color = trendColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MiniSparkline(
    data: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) return
    Canvas(modifier = modifier) {
        val minVal = data.minOrNull() ?: 0.0
        val maxVal = data.maxOrNull() ?: 1.0
        val range = (maxVal - minVal).coerceAtLeast(0.0001)

        val path = Path()
        val stepX = size.width / (data.size - 1)

        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = size.height - (((value - minVal) / range) * size.height).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun InventoryValueStockCard(points: List<StockTrendPoint>) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBorder))
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Inventory Value & Stock",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(tokens.iconSize),
                        tint = iconMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "This Year ▾",
                        fontSize = tokens.caption,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Legends
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = Primary, text = "Value (₹)", isCircle = true)
                ChartLegendItem(color = chartCyan, text = "Incoming", isCircle = false)
                ChartLegendItem(color = chartBlue, text = "Outgoing", isCircle = false)
            }

            Spacer(Modifier.height(16.dp))

            // Bar Trends Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.cardHeight)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val months = if (points.isNotEmpty()) points.map { it.month } else listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Oct", "Dec")
                    months.take(8).forEachIndexed { index, m ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .fillMaxHeight(0.35f + (index % 4) * 0.15f)
                                        .background(chartCyan, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .fillMaxHeight(0.25f + (index % 3) * 0.15f)
                                        .background(chartBlue, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = m,
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChartLegendItem(color: Color, text: String, isCircle: Boolean) {
    val tokens = LocalAppTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(if (isCircle) 8.dp else 10.dp)
                .background(color, if (isCircle) CircleShape else RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = tokens.caption,
            color = headerGrey,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun WarehouseHealthSectionCard(warehouses: List<WarehouseHealthItem>) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBorder))
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Warehouse Health",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Text(
                    text = "${warehouses.size} Locations",
                    fontSize = tokens.bodySmall,
                    color = BluePrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(12.dp))

            warehouses.forEach { wh ->
                val barColor = when (wh.color.lowercase()) {
                    "emerald", "green" -> chartEmerald
                    "orange", "amber" -> chartAmber
                    "blue" -> chartBlue
                    else -> redText
                }

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = wh.name,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TitleColor
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = wh.location,
                                fontSize = tokens.bodySmall,
                                color = mutedText,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${wh.percent.toInt()}% ",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = barColor
                            )
                            Text(
                                text = wh.status,
                                fontSize = tokens.bodySmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(lightGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((wh.percent.toFloat() / 100f).coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StockHealthSectionCard(stockHealth: StockHealthData?) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBorder))
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding)) {
            Text(
                text = "Stock Health",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )

            Spacer(Modifier.height(14.dp))

            // Donut Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                val segments = stockHealth?.segments ?: emptyList()
                Canvas(modifier = Modifier.size(120.dp)) {
                    var startAngle = -90f
                    val strokeWidth = 14.dp.toPx()

                    if (segments.isEmpty()) {
                        drawArc(
                            color = BorderGray,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth)
                        )
                    } else {
                        segments.forEach { seg ->
                            val sweepAngle = (seg.percent.toFloat() / 100f) * 360f
                            val parsedColor = try {
                                Color(android.graphics.Color.parseColor(seg.color))
                            } catch (_: Exception) {
                                chartEmerald
                            }

                            drawArc(
                                color = parsedColor,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += sweepAngle
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stockHealth?.total ?: 0}",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Total Items",
                        fontSize = tokens.label,
                        color = mutedText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Segments
            stockHealth?.segments?.forEach { seg ->
                val parsedColor = try {
                    Color(android.graphics.Color.parseColor(seg.color))
                } catch (_: Exception) {
                    chartEmerald
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(parsedColor, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = seg.label,
                            fontSize = tokens.bodySmall,
                            color = textSubdued,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row {
                        Text(
                            text = "${seg.count}",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = " (${seg.percent}%)",
                            fontSize = tokens.caption,
                            color = mutedText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryCategoriesCard(categories: List<CategoryBreakdownItem>) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBorder))
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding)) {
            Text(
                text = "Inventory Categories",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Primary
            )

            Spacer(Modifier.height(12.dp))

            categories.forEach { cat ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = cat.name,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = textSubdued
                        )
                        Text(
                            text = "${cat.count} (${cat.percent}%)",
                            fontSize = tokens.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(lightGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((cat.percent.toFloat() / 100f).coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(BluePrimary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FastMovingSectionCard(products: List<FastMovingProductItem>) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBorder))
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fast Moving Products",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Text(
                    text = "View all",
                    fontSize = tokens.bodySmall,
                    color = BluePrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Product", fontSize = tokens.caption, color = mutedText, fontWeight = FontWeight.Medium)
                Text("Current Stock / Usage", fontSize = tokens.caption, color = mutedText, fontWeight = FontWeight.Medium)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = dividerColor
            )

            products.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeGrey),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!item.image.isNullOrBlank()) {
                                AsyncImage(
                                    model = item.image,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Layers,
                                    contentDescription = null,
                                    tint = iconMuted,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = item.name,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.sku,
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.currentStock,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "↑ ${item.monthlyUsage}",
                            fontSize = tokens.caption,
                            color = greentext,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SlowMovingSectionCard(products: List<SlowMovingProductItem>) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBorder))
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Slow Moving / Dead Stock",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
                Text(
                    text = "View all",
                    fontSize = tokens.bodySmall,
                    color = BluePrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("PRODUCT", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                Text("DAYS IDLE / VALUE", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
                Text("ACTION", fontSize = tokens.label, color = mutedText, fontWeight = FontWeight.Medium)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = dividerColor
            )

            products.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left
                    Row(
                        modifier = Modifier.weight(1.3f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(redBg.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!item.image.isNullOrBlank()) {
                                AsyncImage(
                                    model = item.image,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Print,
                                    contentDescription = null,
                                    tint = redText,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = item.name,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.sku,
                                fontSize = tokens.label,
                                color = mutedText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Middle
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = item.daysIdle,
                            fontSize = tokens.caption,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = item.stockValue,
                            fontSize = tokens.caption,
                            color = mutedText,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Right (Badge Action)
                    val isDisposal = item.recommendation.contains("Disposal", ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDisposal) redBg else yellowBg)
                            .border(
                                1.dp,
                                if (isDisposal) redText.copy(alpha = 0.4f) else yellowText.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = item.recommendation,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = if (isDisposal) redText else yellowText
                        )
                    }
                }
            }
        }
    }
}