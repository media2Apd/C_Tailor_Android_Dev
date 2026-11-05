@file:Suppress("unused", "AssignedValueIsNeverRead", "UnusedVariable")
package com.cuso.tailor.view.home.security.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.security.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DateRangePickerField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.SecurityViewModel

@Composable
fun SecurityDashboardScreen(
    viewModel: SecurityViewModel,
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val isLoading by viewModel.isLoading.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val loginTrend by viewModel.loginTrend.collectAsState()
    val health by viewModel.securityHealth.collectAsState()
    val usersByRole by viewModel.usersByRole.collectAsState()
    val attackTypes by viewModel.attackTypes.collectAsState()
    val recentAlerts by viewModel.recentAlertsList.collectAsState()
    val activeSessions by viewModel.activeSessionsList.collectAsState()

    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val trendGranularity by viewModel.trendGranularity.collectAsState()
    val attackPeriod by viewModel.attackPeriod.collectAsState()

    Scaffold(
        topBar = {
            TitleBar(
                title = "Security Dashboard",
                subtitle = "Real-time threat & user activity metrics",
                onClose = onClose,
                trailingContent = {
                    IconButton(onClick = { viewModel.fetchAllDashboardData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = close_color,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }
            )
        },
        containerColor = Primary_background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.5f)
        ) {
            // ── 1. Date Range Picker Field ──
            item {
                DateRangePickerField(
                    startDate = startDate,
                    endDate = endDate,
                    onRangeSelected = { s, e -> viewModel.onDateRangeSelected(s, e) }
                )
            }

            // ── 2. Key Metrics ──
            item {
                Text(
                    text = "KEY METRICS",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Spacer(Modifier.height(tokens.extraPadding))

                val sum = summary
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        SecurityStatCard(
                            title = "Total Users",
                            value = (sum?.totalUsers?.value ?: 0).toString(),
                            trend = if (sum?.totalUsers?.trend == "new") "New" else "+8.7%",
                            isPositive = true,
                            icon = Icons.Default.People,
                            iconTint = Primary,
                            iconBg = primary_light,
                            modifier = Modifier.weight(1f)
                        )
                        SecurityStatCard(
                            title = "Active Users",
                            value = (sum?.activeUsers?.value ?: 0).toString(),
                            trend = if (sum?.activeUsers?.trend == "new") "New" else "+10.2%",
                            isPositive = true,
                            icon = Icons.Default.Bolt,
                            iconTint = greentext,
                            iconBg = greenBg,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                    ) {
                        SecurityStatCard(
                            title = "Login Attempts",
                            value = (sum?.loginAttempts?.value ?: 0).toString(),
                            trend = if (sum?.loginAttempts?.trend == "new") "New" else "-6.3%",
                            isPositive = false,
                            icon = Icons.AutoMirrored.Filled.Login,
                            iconTint = orangeText,
                            iconBg = orangeBg,
                            modifier = Modifier.weight(1f)
                        )
                        SecurityStatCard(
                            title = "Failed Logins",
                            value = (sum?.failedLogins?.value ?: 0).toString(),
                            trend = if (sum?.failedLogins?.trend == "new") "New" else "-12.4%",
                            isPositive = false,
                            icon = Icons.Default.Lock,
                            iconTint = redText,
                            iconBg = redBg,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    SecurityAlertsSummaryCard(
                        count = (sum?.securityAlerts?.value ?: 0).toInt(),
                        trend = if (sum?.securityAlerts?.trend == "new") "Active threats" else "-5.6% vs last month"
                    )
                }
            }

            // ── 3. Login Attempts Trend ──
            item {
                LoginAttemptsTrendWidget(
                    trendData = loginTrend,
                    selectedGranularity = trendGranularity,
                    onGranularitySelected = { viewModel.onTrendGranularityChanged(it) }
                )
            }

            // ── 4. Security Health ──
            item {
                SecurityHealthWidget(healthData = health)
            }

            // ── 5. Users by Role (Donut Chart) ──
            item {
                UsersByRoleWidget(rolesData = usersByRole)
            }

            // ── 6. Top Attack Types ──
            item {
                TopAttackTypesWidget(
                    data = attackTypes,
                    selectedPeriod = attackPeriod,
                    onPeriodChanged = { viewModel.onAttackPeriodChanged(it) }
                )
            }

            // ── 7. Recent Security Alerts ──
            item {
                RecentAlertsWidget(alerts = recentAlerts)
            }

            // ── 8. Active Sessions ──
            item {
                ActiveSessionsWidget(sessions = activeSessions)
            }

            item { Spacer(Modifier.height(tokens.screenPadding * 2)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// COMPONENT WIDGET IMPLEMENTATIONS
// ─────────────────────────────────────────────────────────────

@Composable
fun SecurityStatCard(
    title: String,
    value: String,
    trend: String,
    isPositive: Boolean,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Surface(
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding * 1.4f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.buttonHeight * 0.75f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(tokens.iconSize))
                }
                Text(
                    text = trend,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = if (isPositive) greentext else redText
                )
            }
            Spacer(Modifier.height(tokens.extraPadding))
            Text(
                text = title,
                fontSize = tokens.caption,
                color = mutedText
            )
            Text(
                text = value,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )
        }
    }
}

@Composable
fun SecurityAlertsSummaryCard(count: Int, trend: String) {
    val tokens = LocalAppTokens.current
    Surface(
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.extraPadding * 1.4f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(tokens.buttonHeight * 0.85f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(redBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = redText,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
                Spacer(Modifier.width(tokens.extraPadding))
                Column {
                    Text("Security Alerts", fontSize = tokens.caption, color = mutedText)
                    Text(count.toString(), fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold, color = title_color)
                }
            }
            Text(trend, fontSize = tokens.caption, color = redText, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun LoginAttemptsTrendWidget(
    trendData: LoginTrendData?,
    selectedGranularity: String,
    onGranularitySelected: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding * 1.5f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Login Attempts Trend",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                    Icon(Icons.Default.Info, contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize * 0.8f))
                }

                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        contentPadding = PaddingValues(horizontal = tokens.extraPadding, vertical = 0.dp),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder),
                        modifier = Modifier.height(tokens.buttonHeight * 0.75f)
                    ) {
                        Text(selectedGranularity.replaceFirstChar { it.uppercase() }, fontSize = tokens.caption, color = title_color)
                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(tokens.iconSize), tint = mutedText)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(whiteBg)
                    ) {
                        listOf("daily", "weekly", "monthly").forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.replaceFirstChar { it.uppercase() }, fontSize = tokens.bodySmall, color = title_color) },
                                onClick = {
                                    onGranularitySelected(item)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding))

            Row(horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.5f)) {
                LegendIndicator(label = "Successful Logins", color = chartEmerald)
                LegendIndicator(label = "Failed Logins", color = chartRose)
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.5f))

            val points = trendData?.points ?: emptyList()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.imageDp * 2.2f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val stepY = h / 4

                    for (i in 0..4) {
                        drawLine(
                            color = grey_border,
                            start = Offset(0f, i * stepY),
                            end = Offset(w, i * stepY),
                            strokeWidth = 1f
                        )
                    }

                    if (points.isNotEmpty()) {
                        val maxSuccess = points.maxOfOrNull { it.successful }?.coerceAtLeast(1L) ?: 1L
                        val maxFailed = points.maxOfOrNull { it.failed }?.coerceAtLeast(1L) ?: 1L
                        val maxOverall = maxOf(maxSuccess, maxFailed).toFloat()
                        val stepX = if (points.size > 1) w / (points.size - 1) else w / 2

                        // Successful Logins line
                        for (i in 0 until points.size - 1) {
                            val p1X = i * stepX
                            val p1Y = h - (points[i].successful.toFloat() / maxOverall) * (h * 0.8f)
                            val p2X = (i + 1) * stepX
                            val p2Y = h - (points[i + 1].successful.toFloat() / maxOverall) * (h * 0.8f)

                            drawLine(
                                color = chartEmerald,
                                start = Offset(p1X, p1Y),
                                end = Offset(p2X, p2Y),
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                            drawCircle(chartEmerald, radius = 6f, center = Offset(p1X, p1Y))
                        }
                        if (points.isNotEmpty()) {
                            val lastIdx = points.lastIndex
                            val lastX = if (points.size > 1) lastIdx * stepX else w / 2
                            val lastY = h - (points[lastIdx].successful.toFloat() / maxOverall) * (h * 0.8f)
                            drawCircle(chartEmerald, radius = 6f, center = Offset(lastX, lastY))
                        }

                        // Failed Logins line
                        for (i in 0 until points.size - 1) {
                            val p1X = i * stepX
                            val p1Y = h - (points[i].failed.toFloat() / maxOverall) * (h * 0.8f)
                            val p2X = (i + 1) * stepX
                            val p2Y = h - (points[i + 1].failed.toFloat() / maxOverall) * (h * 0.8f)

                            drawLine(
                                color = chartRose,
                                start = Offset(p1X, p1Y),
                                end = Offset(p2X, p2Y),
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                            drawCircle(chartRose, radius = 6f, center = Offset(p1X, p1Y))
                        }
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach {
                    Text(it.period, fontSize = tokens.label, color = mutedText)
                }
            }
        }
    }
}

@Composable
fun SecurityHealthWidget(healthData: SecurityHealthData?) {
    val tokens = LocalAppTokens.current
    Surface(
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding * 1.5f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Security Health", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = title_color)
                    Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                    Icon(Icons.Default.Info, null, tint = mutedText, modifier = Modifier.size(tokens.iconSize * 0.8f))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.8f))
                        .background(greenBg)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 2.dp)
                ) {
                    Text(
                        text = "Overall: ${healthData?.overall ?: 0}%",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = greentext
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.4f))

            val items = healthData?.items ?: emptyList()
            items.forEach { item ->
                SecurityHealthRow(item)
                Spacer(Modifier.height(tokens.extraPadding))
            }
        }
    }
}

@Composable
fun SecurityHealthRow(item: SecurityHealthItemDto) {
    val tokens = LocalAppTokens.current
    val (statusBg, statusText) = when (item.status.lowercase()) {
        "excellent" -> greenBg to greentext
        "good" -> cyanBg to chartCyan
        "medium" -> yellowBg to yellowText
        else -> redBg to redText
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Primary, modifier = Modifier.size(tokens.iconSize * 0.9f))
                Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                Text(item.label, fontSize = tokens.bodySmall, color = title_color)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${item.percent}%", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                        .background(statusBg)
                        .padding(horizontal = tokens.extraPadding * 0.6f, vertical = 2.dp)
                ) {
                    Text(
                        text = item.status.replaceFirstChar { it.uppercase() },
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = statusText
                    )
                }
            }
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.5f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.extraPadding * 0.6f)
                .clip(CircleShape)
                .background(light_grey)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (item.percent / 100f).coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(Primary)
            )
        }
    }
}

@Composable
fun UsersByRoleWidget(rolesData: UsersByRoleData?) {
    val tokens = LocalAppTokens.current
    val roleColors = listOf(chartEmerald, chartBlue, chartAmber, chartRose, chartCyan, chartViolet)

    Surface(
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding * 1.5f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Users by Role", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = title_color)
                Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                Icon(Icons.Default.Info, null, tint = mutedText, modifier = Modifier.size(tokens.iconSize * 0.8f))
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.5f))

            val roles = rolesData?.roles ?: emptyList()
            val total = rolesData?.total ?: 0L

            // Donut Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.imageDp * 2.6f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(tokens.imageDp * 2.4f)) {
                    val strokeWidth = 36f
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(diameter, diameter)

                    var startAngle = -90f
                    if (roles.isEmpty()) {
                        drawArc(
                            color = light_grey,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(strokeWidth)
                        )
                    } else {
                        roles.forEachIndexed { index, role ->
                            val sweep = (role.percent.toFloat() / 100f) * 360f
                            val color = roleColors[index % roleColors.size]
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(strokeWidth)
                            )
                            startAngle += sweep
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = total.toString(),
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = "Total Users",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.5f))

            // Roles Breakdown Legend List
            Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f)) {
                roles.forEachIndexed { index, role ->
                    val color = roleColors[index % roleColors.size]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(tokens.extraPadding * 0.8f)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                            Text(
                                text = role.name.replaceFirstChar { it.uppercase() },
                                fontSize = tokens.bodySmall,
                                color = title_color
                            )
                        }
                        Text(
                            text = "${role.count} (${role.percent}%)",
                            fontSize = tokens.bodySmall,
                            color = mutedText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopAttackTypesWidget(
    data: TopAttackTypesData?,
    selectedPeriod: String,
    onPeriodChanged: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding * 1.5f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Top Attack Types", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = title_color)
                    Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                    Icon(Icons.Default.Info, null, tint = mutedText, modifier = Modifier.size(tokens.iconSize * 0.8f))
                }

                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        contentPadding = PaddingValues(horizontal = tokens.extraPadding, vertical = 0.dp),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, sectionBorder),
                        modifier = Modifier.height(tokens.buttonHeight * 0.75f)
                    ) {
                        val displayPeriod = when (selectedPeriod) {
                            "this_month" -> "This Month"
                            "last_month" -> "Last Month"
                            else -> "This Year"
                        }
                        Text(displayPeriod, fontSize = tokens.caption, color = title_color)
                        Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(tokens.iconSize), tint = mutedText)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(whiteBg)
                    ) {
                        listOf("this_month" to "This Month", "last_month" to "Last Month", "this_year" to "This Year").forEach { (id, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontSize = tokens.bodySmall, color = title_color) },
                                onClick = {
                                    onPeriodChanged(id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.4f))

            val items = data?.items ?: emptyList()
            items.forEach { item ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.label, fontSize = tokens.bodySmall, color = title_color)
                        Text("${item.count} (${item.percent}%)", fontSize = tokens.caption, color = mutedText)
                    }
                    Spacer(Modifier.height(tokens.extraPadding * 0.5f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.extraPadding * 0.6f)
                            .clip(CircleShape)
                            .background(light_grey)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = (item.percent.toFloat() / 100f).coerceIn(0f, 1f))
                                .clip(CircleShape)
                                .background(chartIndigo)
                        )
                    }
                }
                Spacer(Modifier.height(tokens.extraPadding))
            }
        }
    }
}

@Composable
fun RecentAlertsWidget(alerts: List<SecurityAlertItemDto>) {
    val tokens = LocalAppTokens.current
    Surface(
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding * 1.5f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent Security Alerts", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = title_color)
                    Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                    Icon(Icons.Default.Info, null, tint = mutedText, modifier = Modifier.size(tokens.iconSize * 0.8f))
                }
                Text("View all", fontSize = tokens.caption, color = Primary, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(grey_border)
                    .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding * 0.6f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ALERT", fontSize = tokens.label, color = mutedText, modifier = Modifier.weight(1.4f))
                Text("SEVERITY", fontSize = tokens.label, color = mutedText, modifier = Modifier.weight(0.9f))
                Text("LOCATION", fontSize = tokens.label, color = mutedText, modifier = Modifier.weight(1.1f))
                Text("STATUS", fontSize = tokens.label, color = mutedText, modifier = Modifier.weight(0.8f))
            }

            HorizontalDivider(color = sectionBorder, thickness = 1.dp)

            if (alerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No recent alerts", fontSize = tokens.bodySmall, color = mutedText)
                }
            } else {
                alerts.forEach { alert ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.extraPadding, vertical = tokens.extraPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.4f)) {
                            Text(alert.alertId, fontSize = tokens.label, color = mutedText)
                            Text(alert.type, fontSize = tokens.bodySmall, color = title_color, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        // Severity Tag
                        val (_, sevText) = when (alert.severity.lowercase()) {
                            "high" -> redBg to redText
                            "medium" -> yellowBg to yellowText
                            else -> greenBg to greentext
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .wrapContentWidth(Alignment.Start)
                        ) {
                            Text(
                                text = alert.severity.replaceFirstChar { it.uppercase() },
                                fontSize = tokens.label,
                                color = sevText,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Location
                        Row(
                            modifier = Modifier.weight(1.1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = mutedText, modifier = Modifier.size(tokens.iconSize * 0.7f))
                            Spacer(Modifier.width(tokens.extraPadding * 0.3f))
                            Text(alert.location, fontSize = tokens.caption, color = title_color, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        // Status Tag
                        val (stBg, stText) = when (alert.status.lowercase()) {
                            "open" -> redBg to redText
                            "investigating" -> orangeBg to orangeText
                            else -> greenBg to greentext
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.8f)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .background(stBg)
                                .padding(horizontal = tokens.extraPadding * 0.6f, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = alert.status.replaceFirstChar { it.uppercase() },
                                fontSize = tokens.label,
                                color = stText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    HorizontalDivider(color = grey_border, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun ActiveSessionsWidget(sessions: List<ActiveSessionItemDto>) {
    val tokens = LocalAppTokens.current
    Surface(
        color = whiteBg,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(tokens.extraPadding * 1.5f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Active Sessions", fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = title_color)
                Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                Icon(Icons.Default.Info, null, tint = mutedText, modifier = Modifier.size(tokens.iconSize * 0.8f))
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(tokens.screenPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active sessions", fontSize = tokens.bodySmall, color = mutedText)
                }
            } else {
                sessions.forEach { session ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = tokens.extraPadding * 0.8f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(session.name, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                            Text(
                                text = "${session.role.replaceFirstChar { it.uppercase() }} • ${session.ipAddress}",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .background(greenBg)
                                .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(tokens.extraPadding * 0.6f)
                                    .clip(CircleShape)
                                    .background(greentext)
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.4f))
                            Text("Active", fontSize = tokens.label, color = greentext, fontWeight = FontWeight.Medium)
                        }
                    }
                    HorizontalDivider(color = grey_border, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun LegendIndicator(label: String, color: Color) {
    val tokens = LocalAppTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(tokens.extraPadding * 0.8f)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(tokens.extraPadding * 0.5f))
        Text(label, fontSize = tokens.caption, color = mutedText)
    }
}