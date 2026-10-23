@file:Suppress("AssignedValueIsNeverRead")
package com.cuso.tailor.view.home.legal.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.LegalDashboardSkeleton
import com.cuso.tailor.view.home.pdfgenerator.LegalDashboardExporter
import com.cuso.tailor.viewmodel.LegalViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExportFormatDialog(
    onDismiss: () -> Unit,
    onSelectFormat: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        title = {
            Text(
                text = "Export Dashboard Data",
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Choose the format you would like to download or share:",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )

                // 1. Excel Option
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
                    color = cardBgLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectFormat("excel") }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(greenBg, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("XLS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = greentext)
                        }
                        Column {
                            Text("Excel Spreadsheet (.csv)", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Opens directly in Microsoft Excel and Sheets", fontSize = tokens.label, color = mutedText)
                        }
                    }
                }

                // 2. PDF Option
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
                    color = cardBgLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectFormat("pdf") }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(redBg, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = redText)
                        }
                        Column {
                            Text("PDF Document (.pdf)", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Formatted for printing and PDF viewers", fontSize = tokens.label, color = mutedText)
                        }
                    }
                }

                // 3. Notepad / Text Option
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
                    color = cardBgLight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectFormat("txt") }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(cyanBg, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("TXT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = chartBlue)
                        }
                        Column {
                            Text("Notepad Text Document (.txt)", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Plain text table with all summary metrics", fontSize = tokens.label, color = mutedText)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontSize = tokens.bodySmall, color = TextSecondary)
            }
        }
    )
}

@Composable
fun LegalDashboardScreen(
    viewModel: LegalViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val dashboardData by viewModel.dashboardData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()

    var showExportDialog by remember { mutableStateOf(false) }

    if (isLoading && dashboardData == null) {
        LegalDashboardSkeleton()
        return
    }

    // Export Dialog Trigger
    if (showExportDialog) {
        ExportFormatDialog(
            onDismiss = { showExportDialog = false },
            onSelectFormat = { format ->
                showExportDialog = false
                dashboardData?.let { data ->
                    when (format) {
                        "excel" -> LegalDashboardExporter.exportToExcel(context, data)
                        "pdf"   -> LegalDashboardExporter.exportToPdf(context, data)
                        "txt"   -> LegalDashboardExporter.exportToText(context, data)
                    }
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background),
        contentPadding = PaddingValues(tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        // 1. Dashboard Header Section
        item {
            ExecutiveHeader(onExportClick = { showExportDialog = true })
        }

        // 2. Metrics Snapshot (Horizontally Scrollable Cards)
        item {
            MetricsSnapshotSection(kpis = dashboardData?.kpis)
        }

        // 3. Compliance & Risk Trend Chart Card
        item {
            ComplianceTrendCard(
                trendList = dashboardData?.trend.orEmpty(),
                selectedYear = selectedYear,
                onYearSelected = { year ->
                    viewModel.fetchDashboardSummary(year)
                }
            )        }

        // 4. Legal Health Status Card
        item {
            LegalHealthStatusCard(healthList = dashboardData?.health.orEmpty())
        }

        // 5. Risk Distribution Donut Chart Card
        item {
            RiskDistributionCard(riskDist = dashboardData?.riskDistribution)
        }

        // 6. Legal Categories Breakdown Card
        item {
            LegalCategoriesCard(
                categories = dashboardData?.categories.orEmpty(),
                total = dashboardData?.riskDistribution?.total ?: 0
            )
        }

        // 7. Critical Legal Items Table (Horizontally Scrollable)
        item {
            CriticalLegalItemsCard(items = dashboardData?.upcoming.orEmpty())
        }

        // 8. Approval Queue Table (Horizontally Scrollable)
        item {
            ApprovalQueueCard(approvals = dashboardData?.approvals.orEmpty())
        }
    }
}

// =============================================================================
// Header Section
// =============================================================================
@Composable
private fun ExecutiveHeader(onExportClick: () -> Unit) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Executive Dashboard",
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Good Morning, Admin! \uD83D\uDC4B",
                fontSize = tokens.caption,
                color = TextSecondary
            )
        }

        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
            color = whiteBg
        ) {
            Row(
                modifier = Modifier
                    .clickable { onExportClick() }
                    .padding(horizontal = tokens.extraPadding * 0.75f, vertical = tokens.extraPadding * 0.45f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = "Export",
                    tint = TextPrimary,
                    modifier = Modifier.size(tokens.iconSize * 0.85f)
                )
                Text(
                    text = "Export",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
        }
    }
}
// =============================================================================
// Metrics Snapshot Section (Horizontal Swipeable Row)
// =============================================================================
@Composable
private fun MetricsSnapshotSection(kpis: LegalKpis?) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "METRICS SNAPSHOT",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Bold,
                color = mutedText,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Swipe for more →",
                fontSize = tokens.caption,
                color = mutedText
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f),
            contentPadding = PaddingValues(end = tokens.screenPadding / 2)
        ) {
            item {
                MetricCardItem(
                    icon = Icons.Outlined.Description,
                    iconBg = cyanBg,
                    iconTint = chartBlue,
                    title = "Total Records",
                    value = formatInteger(kpis?.totalRecords?.value ?: 0.0),
                    change = kpis?.totalRecords?.change ?: 0.0,
                    lineColor = chartBlue
                )
            }
            item {
                MetricCardItem(
                    icon = Icons.Outlined.WarningAmber,
                    iconBg = redBg,
                    iconTint = redText,
                    title = "Critical Risks",
                    value = formatInteger(kpis?.criticalRisks?.value ?: 0.0),
                    change = kpis?.criticalRisks?.change ?: 0.0,
                    lineColor = redText
                )
            }
            item {
                MetricCardItem(
                    icon = Icons.Outlined.Description,
                    iconBg = yellowBg,
                    iconTint = orangeText,
                    title = "Expiring Contracts",
                    value = formatInteger(kpis?.expiringContracts?.value ?: 0.0),
                    change = kpis?.expiringContracts?.change ?: 0.0,
                    lineColor = chartAmber
                )
            }
            item {
                MetricCardItem(
                    icon = Icons.Outlined.WarningAmber,
                    iconBg = redBg,
                    iconTint = redText,
                    title = "Overdue Actions",
                    value = formatInteger(kpis?.overdueActions?.value ?: 0.0),
                    change = kpis?.overdueActions?.change ?: 0.0,
                    lineColor = redText
                )
            }
        }
    }
}

@Composable
private fun MetricCardItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    value: String,
    change: Double,
    lineColor: Color
) {
    val tokens = LocalAppTokens.current
    val isPositive = change >= 0

    Surface(
        modifier = Modifier.width(175.dp),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(iconBg, RoundedCornerShape(tokens.cardCornerRadius / 2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(tokens.iconSize * 0.9f)
                    )
                }
                Text(
                    text = title,
                    fontSize = tokens.caption,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = value,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (isPositive) greentext else redText,
                    modifier = Modifier.size(tokens.iconSize * 0.6f)
                )
                Text(
                    text = "${if (isPositive) "+" else ""}$change%",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPositive) greentext else redText
                )
                Text(
                    text = "vs last mo",
                    fontSize = tokens.label,
                    color = mutedText
                )
            }

            // Wave Line Graphic
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
            ) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.8f)
                    cubicTo(
                        size.width * 0.35f, size.height * 0.85f,
                        size.width * 0.65f, size.height * 0.1f,
                        size.width, size.height * 0.3f
                    )
                }
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

// =============================================================================
// Compliance & Risk Trend Chart Card (Dynamic Year Switching)
// =============================================================================
@Composable
private fun ComplianceTrendCard(
    trendList: List<LegalTrendItem>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    val tokens = LocalAppTokens.current
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Dynamically calculate current year and previous year
    val currentYear = remember { java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) }
    val lastYear = remember { currentYear - 1 }

    val selectedLabel = if (selectedYear == currentYear) "This Year" else "Last Year"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Interactive Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Compliance & Risk Trend",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Performance over current cycle",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }

                // Year Selector Dropdown
                Box {
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
                        color = cardBgLight,
                        modifier = Modifier.clickable { isDropdownExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(selectedLabel, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Year",
                                tint = TextSecondary,
                                modifier = Modifier.size(tokens.iconSize * 0.7f)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.background(whiteBg)
                    ) {
                        DropdownMenuItem(
                            text = { Text("This Year ($currentYear)", fontSize = tokens.bodySmall, color = TextPrimary) },
                            onClick = {
                                isDropdownExpanded = false
                                onYearSelected(currentYear)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Last Year ($lastYear)", fontSize = tokens.bodySmall, color = TextPrimary) },
                            onClick = {
                                isDropdownExpanded = false
                                onYearSelected(lastYear)
                            }
                        )
                    }
                }
            }

            // Legend Row
            Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(purpleText, CircleShape))
                    Text("Compliance Status (%)", fontSize = tokens.caption, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(greentext, CircleShape))
                    Text("Open Risks", fontSize = tokens.caption, color = TextSecondary)
                }
            }

            // Trend Chart Area with Dynamic Graph & Y-Axis Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val totalPoints = trendList.size.coerceAtLeast(1)
                        val stepX = if (totalPoints > 1) w / (totalPoints - 1) else w / 2

                        // Dashed Grid Lines (100%, 75%, 50%, 25%, 0%)
                        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        for (i in 0..4) {
                            val y = h * (i / 4f)
                            drawLine(
                                color = dividerColor,
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashedEffect
                            )
                        }

                        // Map percentage value to Canvas Y coordinate
                        fun toCanvasY(percentage: Double): Float {
                            val clamped = percentage.coerceIn(0.0, 100.0).toFloat()
                            return h - (clamped / 100f * h)
                        }

                        // Dynamic Compliance Points & Line
                        val compliancePoints = trendList.mapIndexedNotNull { index, item ->
                            item.compliance?.let { value ->
                                Offset(x = index * stepX, y = toCanvasY(value))
                            }
                        }

                        if (compliancePoints.isNotEmpty()) {
                            val compliancePath = Path().apply {
                                compliancePoints.forEachIndexed { idx, pt ->
                                    if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                                }
                            }
                            drawPath(
                                path = compliancePath,
                                color = purpleText,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                            compliancePoints.forEach { pt ->
                                drawCircle(color = whiteBg, radius = 4.5.dp.toPx(), center = pt)
                                drawCircle(color = purpleText, radius = 2.5.dp.toPx(), center = pt)
                            }
                        }

                        // Dynamic Open Risk Points & Line
                        val riskPoints = trendList.mapIndexedNotNull { index, item ->
                            item.openRisk?.let { value ->
                                Offset(x = index * stepX, y = toCanvasY(value))
                            }
                        }

                        if (riskPoints.isNotEmpty()) {
                            val riskPath = Path().apply {
                                riskPoints.forEachIndexed { idx, pt ->
                                    if (idx == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                                }
                            }
                            drawPath(
                                path = riskPath,
                                color = greentext,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                            riskPoints.forEach { pt ->
                                drawCircle(color = whiteBg, radius = 4.5.dp.toPx(), center = pt)
                                drawCircle(color = greentext, radius = 2.5.dp.toPx(), center = pt)
                            }
                        }
                    }
                }

                // Y-Axis Static Percentage Scale
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("100%", fontSize = tokens.label, color = mutedText)
                    Text("75%", fontSize = tokens.label, color = mutedText)
                    Text("50%", fontSize = tokens.label, color = mutedText)
                    Text("25%", fontSize = tokens.label, color = mutedText)
                    Text("0%", fontSize = tokens.label, color = mutedText)
                }
            }

            // Month Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 36.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trendList.forEach { item ->
                    Text(
                        text = item.month.orEmpty(),
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }
        }
    }
}

// =============================================================================
// Legal Health Status Progress Card
// =============================================================================
@Composable
private fun LegalHealthStatusCard(healthList: List<LegalHealthItem>) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Legal Health Status",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f),
                    color = greenBg
                ) {
                    Text(
                        text = "Overall Good",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = greentext,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            healthList.forEach { item ->
                val percent = item.percent ?: 0
                val barColor = when (item.status) {
                    "Healthy"   -> greentext
                    "Review"    -> chartBlue
                    "Attention" -> chartAmber
                    else        -> redText
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.label.orEmpty(), fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("$percent%", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            HealthStatusTag(status = item.status.orEmpty())
                        }
                    }

                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = barColor,
                        trackColor = cardBgLight
                    )
                }
            }
        }
    }
}

@Composable
private fun HealthStatusTag(status: String) {
    val tokens = LocalAppTokens.current
    val (bg, fg) = when (status) {
        "Healthy"   -> Pair(greenBg, greentext)
        "Review"    -> Pair(cyanBg, chartBlue)
        "Attention" -> Pair(yellowBg, orangeText)
        else        -> Pair(redBg, redText)
    }

    Surface(shape = RoundedCornerShape(tokens.cardCornerRadius / 2), color = bg) {
        Text(
            text = status,
            fontSize = tokens.label,
            fontWeight = FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

// =============================================================================
// Risk Distribution Donut Chart Card (Spacious, No Overlap)
// =============================================================================
@Composable
private fun RiskDistributionCard(riskDist: LegalRiskDistribution?) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Risk Distribution",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Centered Donut Chart with roomy interior
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 18.dp.toPx()
                    drawArc(greentext, -90f, 160f, false, style = Stroke(stroke, cap = StrokeCap.Butt))
                    drawArc(purpleText, 70f, 65f, false, style = Stroke(stroke, cap = StrokeCap.Butt))
                    drawArc(chartBlue, 135f, 50f, false, style = Stroke(stroke, cap = StrokeCap.Butt))
                    drawArc(redText, 185f, 45f, false, style = Stroke(stroke, cap = StrokeCap.Butt))
                    drawArc(chartAmber, 230f, 40f, false, style = Stroke(stroke, cap = StrokeCap.Butt))
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${riskDist?.total ?: 0}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "TOTAL RECORDS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = mutedText,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Legend Rows
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                riskDist?.items?.forEach { item ->
                    val dotColor = when (item.key) {
                        "healthy"  -> greentext
                        "review"   -> chartAmber
                        "highRisk" -> redText
                        "pending"  -> chartBlue
                        else       -> purpleText
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                            Text(item.label.orEmpty(), fontSize = tokens.bodySmall, color = TextPrimary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${item.count ?: 0}", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("(${item.percent ?: 0.0}%)", fontSize = tokens.caption, color = mutedText)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// Legal Categories Breakdown Card
// =============================================================================
@Composable
private fun LegalCategoriesCard(categories: List<LegalCategoryItem>, total: Int) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Legal Categories",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius / 2),
                    color = cardBgLight
                ) {
                    Text(
                        text = "By Records",
                        fontSize = tokens.label,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            categories.forEach { cat ->
                val recordCount = cat.records ?: 0
                val safeTotal = if (total > 0) total else 1
                val percent = ((recordCount.toFloat() / safeTotal) * 100).toInt()

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cat.label.orEmpty(), fontSize = tokens.bodySmall, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("$percent%", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("($recordCount)", fontSize = tokens.caption, color = mutedText)
                        }
                    }
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = BluePrimary,
                        trackColor = cardBgLight
                    )
                }
            }
        }
    }
}

// =============================================================================
// Critical Legal Items (Horizontally Scrollable Table)
// =============================================================================
@Composable
private fun CriticalLegalItemsCard(items: List<LegalUpcomingItem>) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(modifier = Modifier.padding(vertical = tokens.extraPadding)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.extraPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Critical Legal Items", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Needs review & action", fontSize = tokens.caption, color = mutedText)
                }
                Text("View All →", fontSize = tokens.bodySmall, color = BluePrimary, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontally Scrollable Table with Fixed 700dp Width
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.width(700.dp)) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBgLight)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RECORD / ITEM", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(220.dp))
                        Text("OWNER", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                        Text("DUE DATE", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(110.dp))
                        Text("PRIORITY", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(100.dp))
                        Text("STATUS", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(140.dp))
                    }

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    // Data Rows
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title ?: item.number.orEmpty(),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(220.dp)
                            )
                            Text(
                                text = item.owner ?: "—",
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(130.dp)
                            )
                            Text(
                                text = formatDate(item.date),
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.width(110.dp)
                            )
                            Box(modifier = Modifier.width(100.dp)) {
                                PriorityBadge(priority = item.riskLevel ?: "Medium")
                            }
                            Box(modifier = Modifier.width(140.dp)) {
                                StatusBadgePill(status = item.eventType ?: item.status ?: "In Progress")
                            }
                        }
                        HorizontalDivider(color = cardBgLight, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

// =============================================================================
// Approval Queue (Horizontally Scrollable Table)
// =============================================================================
@Composable
private fun ApprovalQueueCard(approvals: List<LegalApprovalItem>) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(modifier = Modifier.padding(vertical = tokens.extraPadding)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.extraPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Approval Queue", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${approvals.size} pending decision items", fontSize = tokens.caption, color = mutedText)
                }
                Text("View All →", fontSize = tokens.bodySmall, color = BluePrimary, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontally Scrollable Table with Fixed 680dp Width
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.width(680.dp)) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBgLight)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("REQUEST / ITEM", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(230.dp))
                        Text("REQUESTED BY", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(190.dp))
                        Text("DUE DATE", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                        Text("STATUS", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = TextSecondary, maxLines = 1, softWrap = false, modifier = Modifier.width(130.dp))
                    }

                    HorizontalDivider(color = dividerColor, thickness = 1.dp)

                    // Data Rows
                    approvals.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title ?: item.requestNumber.orEmpty(),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(230.dp)
                            )
                            Text(
                                text = "${item.requestedBy.orEmpty()} • ${item.moduleLabel ?: "Contracts"}",
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(190.dp)
                            )
                            Text(
                                text = formatDate(item.dueDate),
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.width(130.dp)
                            )
                            Box(modifier = Modifier.width(130.dp)) {
                                ApprovalStatusBadge(status = item.state ?: item.stage ?: "Pending")
                            }
                        }
                        HorizontalDivider(color = cardBgLight, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

// =============================================================================
// Helper Badges & Formatters
// =============================================================================
@Composable
private fun PriorityBadge(priority: String) {
    val tokens = LocalAppTokens.current
    val (bg, fg) = when (priority.lowercase()) {
        "high"   -> Pair(redBg, redText)
        "medium" -> Pair(yellowBg, orangeText)
        else     -> Pair(greenBg, greentext)
    }

    Surface(shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f), color = bg) {
        Text(
            text = priority,
            fontSize = tokens.label,
            fontWeight = FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun StatusBadgePill(status: String) {
    val tokens = LocalAppTokens.current
    val (bg, fg) = when (status.lowercase()) {
        "expiry", "expiring soon" -> Pair(yellowBg, orangeText)
        "active", "in progress"   -> Pair(greenBg, greentext)
        "renewal", "pending"      -> Pair(cyanBg, chartBlue)
        else                      -> Pair(cardBgLight, TextSecondary)
    }

    Surface(shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f), color = bg) {
        Text(
            text = status,
            fontSize = tokens.label,
            fontWeight = FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ApprovalStatusBadge(status: String) {
    val tokens = LocalAppTokens.current
    val (bg, fg) = when (status.lowercase()) {
        "approved"  -> Pair(greenBg, greentext)
        "in review" -> Pair(primary_light, BluePrimary)
        else        -> Pair(yellowBg, orangeText)
    }

    Surface(shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f), color = bg) {
        Text(
            text = status,
            fontSize = tokens.label,
            fontWeight = FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

private fun formatInteger(value: Double): String {
    return String.format(Locale.getDefault(), "%,d", value.toLong())
}

private fun formatDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "—"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(isoDate) ?: return isoDate.take(10)
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        formatter.format(date)
    } catch (_: Exception) {
        isoDate.take(10)
    }
}