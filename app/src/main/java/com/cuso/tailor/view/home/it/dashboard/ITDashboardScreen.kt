package com.cuso.tailor.view.home.it.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.it.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.viewmodel.ItViewModel

@Composable
fun ItDashboardScreen(
    onNavigateToRoute: (String) -> Unit = {},
    viewModel: ItViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val dashboardData by viewModel.dashboardData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        containerColor = Primary_background
    ) { paddingValues ->
        if (isLoading && dashboardData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (errorMessage != null && dashboardData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = errorMessage ?: "Failed to load dashboard",
                        fontSize = tokens.bodySmall,
                        color = redText
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.fetchDashboard() },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                    ) {
                        Text("Retry", fontSize = tokens.bodySmall, color = whiteBg)
                    }
                }
            }
        } else {
            val data = dashboardData ?: ItDashboardData()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = tokens.screenPadding, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ─── Header: Title & Last Updated Badge ───
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "IT Dashboard",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(greenBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(greentext)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "Last updated: ${viewModel.formatTimeAgo(data.lastUpdated)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = greentext
                            )
                        }
                    }
                }

                // ─── 2x3 Metric Cards Grid ───
                val kpis = data.kpis
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ItMetricCard(
                            title = "SYSTEMS",
                            value = kpis?.systems?.displayValue() ?: "N/A",
                            subText = kpis?.systems?.sub ?: "Operational",
                            dotColor = greentext,
                            modifier = Modifier.weight(1f)
                        )
                        ItMetricCard(
                            title = "DEVICES",
                            value = kpis?.devices?.displayValue() ?: "0",
                            subText = kpis?.devices?.sub ?: "0 offline",
                            dotColor = orangeText,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ItMetricCard(
                            title = "APPLICATIONS",
                            value = kpis?.applications?.displayValue() ?: "0",
                            subText = kpis?.applications?.sub ?: "Integrations",
                            dotColor = greentext,
                            modifier = Modifier.weight(1f)
                        )
                        ItMetricCard(
                            title = "NETWORK UPTIME",
                            value = kpis?.network?.displayValue() ?: "Not monitored",
                            subText = kpis?.network?.sub ?: "Telemetry",
                            dotColor = orangeText,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ItMetricCard(
                            title = "CRITICAL ALERTS",
                            value = kpis?.alerts?.displayValue() ?: "0",
                            valueColor = redText,
                            subText = kpis?.alerts?.sub?.take(18) ?: "Action required",
                            dotColor = redText,
                            modifier = Modifier.weight(1f)
                        )
                        ItMetricCard(
                            title = "OPEN TICKETS",
                            value = kpis?.tickets?.displayValue() ?: "0",
                            subText = kpis?.tickets?.sub ?: "0 SLA breached",
                            dotColor = greentext,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ─── Overall IT Health Banner ───
                val health = data.overallHealth
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Overall IT Health",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(greenBg)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${health?.percent ?: 0}% Healthy",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = greentext
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onNavigateToRoute("it_monitoring") }
                            ) {
                                Text(
                                    text = "View Monitoring",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Primary
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Text(
                            text = "Total Monitored: ${health?.total ?: 0}  •  Healthy: ${health?.healthy ?: 0}  •  Warning: ${health?.warning ?: 0}  •  Critical: ${health?.critical ?: 0}",
                            fontSize = 11.sp,
                            color = mutedText
                        )
                    }
                }

                // ─── Section Header: SYSTEM MODULES ───
                Text(
                    text = "SYSTEM MODULES",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )

                // ─── Core Health Cards (System Health, Device Health, Network Health, Integration Health) ───
                data.coreHealth.forEach { module ->
                    ItSystemModuleCard(
                        title = module.title,
                        badge = module.badge,
                        healthPercent = module.healthPercent,
                        stats = module.stats.map { it.label to it.isWarn },
                        buttonText = "View Details →",
                        icon = resolveModuleIcon(module.icon),
                        onButtonClick = { onNavigateToRoute(module.link ?: "") }
                    )
                }

                // ─── Module Overviews (Backup & Recovery, Integrations, POS & Billing, IT Assets) ───
                data.moduleOverviews.forEach { module ->
                    ItSystemModuleCard(
                        title = module.title,
                        badge = module.subtitle,
                        healthPercent = null,
                        stats = module.details.map { it.text to false },
                        buttonText = module.btnText,
                        icon = resolveModuleIcon(module.icon),
                        onButtonClick = { onNavigateToRoute(module.link ?: "") }
                    )
                }

                // ─── Integration Issues Card ───
                if (data.integrationIssues.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        color = whiteBg,
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(tokens.screenPadding),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Integration Issues",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = title_color
                                    )
                                    Text(
                                        text = "Requires technical review and resolution.",
                                        fontSize = 11.sp,
                                        color = mutedText
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(orangeBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${data.integrationIssueCount} Issues",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = orangeText
                                    )
                                }
                            }

                            data.integrationIssues.forEach { issue ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = cardBgLight,
                                    border = BorderStroke(1.dp, BorderGray)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = issue.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = title_color
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(orangeBg)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(orangeText)
                                                    )
                                                    Spacer(Modifier.width(4.dp))
                                                    Text(
                                                        text = "Warning",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = orangeText
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "${issue.reason} • ${issue.sub}",
                                            fontSize = 11.sp,
                                            color = mutedText
                                        )

                                        Spacer(Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onNavigateToRoute("it_integrations") },
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Review",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Primary
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = null,
                                                tint = Primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ─── IT Health Trend (7 Days) with Sparkline Chart ───
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "IT Health Trend (7 Days)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Text(
                                    text = "Past 7 days performance",
                                    fontSize = 11.sp,
                                    color = mutedText
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(greentext)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Healthy",
                                    fontSize = 11.sp,
                                    color = greentext,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Category Health Indicators Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TrendBadgePill("Network", "99.8%", greentext, Modifier.weight(1f))
                            TrendBadgePill("Servers", "98.2%", orangeText, Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TrendBadgePill("Applications", "99.5%", greentext, Modifier.weight(1f))
                            TrendBadgePill("Database", "99.9%", greentext, Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(8.dp))

                        // Custom 7-Day Trend Canvas Line Chart
                        ItSevenDayTrendChart(
                            points = listOf(96, 97, 96, 98, 97, 98, 97),
                            days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        )
                    }
                }

                // ─── Support & Triage Section ───
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Support & Triage",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                            Text(
                                text = "View All Tickets",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Primary,
                                modifier = Modifier.clickable { onNavigateToRoute("it_tickets") }
                            )
                        }

                        data.supportTriage.forEach { ticket ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = cardBgLight,
                                border = BorderStroke(1.dp, BorderGray)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${ticket.id} — ${ticket.title}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = title_color
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "${ticket.sub} • ${ticket.location}",
                                            fontSize = 11.sp,
                                            color = mutedText
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(greenBg)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = ticket.time,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = greentext
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ─── Recent IT Activity Section ───
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, BorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Recent IT Activity",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )

                        data.recentActivity.forEach { act ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    val dotColor = when (act.type.lowercase()) {
                                        "error" -> redText
                                        "success" -> greentext
                                        "warn" -> orangeText
                                        else -> Primary
                                    }
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 5.dp)
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = act.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = title_color
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "${act.location} • ${act.subtitle}",
                                            fontSize = 11.sp,
                                            color = mutedText
                                        )
                                    }
                                }

                                Text(
                                    text = act.time,
                                    fontSize = 11.sp,
                                    color = mutedText
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ─── Reusable Metric Card ───
@Composable
private fun ItMetricCard(
    title: String,
    value: String,
    subText: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
    valueColor: Color = title_color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = mutedText,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = subText,
                    fontSize = 10.sp,
                    color = mutedText,
                    maxLines = 1
                )
            }
        }
    }
}

// ─── Reusable System Module Card ───
@Composable
private fun ItSystemModuleCard(
    title: String,
    badge: String,
    healthPercent: String?,
    stats: List<Pair<String, Boolean>>,
    buttonText: String,
    icon: ImageVector,
    onButtonClick: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Icon + Title + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(cardBgLight)
                            .border(1.dp, BorderGray, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(greenBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = greentext
                    )
                }
            }

            // Health Status Headline
            if (!healthPercent.isNullOrBlank()) {
                Text(
                    text = healthPercent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
            }

            // Details/Stats row items
            stats.forEach { (label, isWarn) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isWarn) orangeText else greentext)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = if (isWarn) orangeText else mutedText
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Primary Blue Action Button
            Button(
                onClick = onButtonClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(
                    text = buttonText,
                    fontSize = tokens.bodySmall,
                    color = whiteBg,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TrendBadgePill(
    category: String,
    percentage: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = cardBgLight,
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = category, fontSize = 11.sp, color = title_color)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = percentage,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

// ─── Custom Canvas Line Chart for 7-Day Performance Trend ───
@Composable
private fun ItSevenDayTrendChart(
    points: List<Int>,
    days: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(horizontal = 10.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val spacing = width / (points.size - 1)

                val minVal = 94f
                val maxVal = 100f

                val path = Path()
                val coordinates = points.mapIndexed { idx, value ->
                    val x = idx * spacing
                    val normalizedY = (value - minVal) / (maxVal - minVal)
                    val y = height - (normalizedY * height * 0.8f) - (height * 0.1f)
                    Offset(x, y)
                }

                // Draw connecting path
                coordinates.forEachIndexed { i, offset ->
                    if (i == 0) path.moveTo(offset.x, offset.y)
                    else path.lineTo(offset.x, offset.y)
                }

                drawPath(
                    path = path,
                    color = Color(0xFF10B981),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw point circles
                coordinates.forEach { offset ->
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 4.dp.toPx(),
                        center = offset,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // X-Axis Day Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Text(
                    text = day,
                    fontSize = 10.sp,
                    color = mutedText
                )
            }
        }
    }
}

// ─── Module Icon Resolver ───
private fun resolveModuleIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "server" -> Icons.Outlined.Dns
        "smartphone", "devices" -> Icons.Outlined.Smartphone
        "network" -> Icons.Outlined.Hub
        "layouttemplate", "integrations", "linkicon" -> Icons.Outlined.IntegrationInstructions
        "database" -> Icons.Outlined.Storage
        "creditcard" -> Icons.Outlined.CreditCard
        "laptop" -> Icons.Outlined.Laptop
        else -> Icons.Outlined.Settings
    }
}