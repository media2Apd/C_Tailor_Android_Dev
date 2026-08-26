@file:Suppress(
    "UNUSED_PARAMETER",
    "unused",
    "UNCHECKED_CAST",
    "DEPRECATION",
    "AssignedValueIsNeverRead",
    "GrazieInspection",
    "SpellCheckingInspection",
    "unusedvariable",
    "UNNECESSARY_SAFE_CALL"
)

package com.cuso.mobile.view.home.services.service_status.delay_rework

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*

private val ScreenBg = Color(0xFFF9FAFC)
private val InsightBg = Color(0xFFEFF6FF)
private val InsightBorder = Color(0xFFBFDBFE)
private val InsightBlue = Color(0xFF2563EB)
private val UrgentRed = Color(0xFFDC2626)
private val UrgentRedLight = Color(0xFFFFEBEE)
private val AmberWarn = Color(0xFFD97706)
private val AmberWarnLight = Color(0xFFFEF3C7)
private val ScheduledBlue = Color(0xFF4338CA)
private val ScheduledBlueLight = Color(0xFFEEF2FF)

/**
 * Separate screen for Delay & Rework Tracking under Service Status.
 */
@Composable
fun DelayReworkTrackingScreen(
    onClose: () -> Unit = {},
    onLogNewDelay: () -> Unit = {},
    onRegisterAlteration: () -> Unit = {},
    onCreateReworkOrder: () -> Unit = {},
    onViewLog: () -> Unit = {},
    onGenerateReport: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Service Status",
                fontSize = tokens.h2,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextPrimary
                )
            }
        }
        HorizontalDivider(color = BorderGray, thickness = 1.dp)

        // Main Scrollable Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp))
            { // Header Description
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Delay & Rework Tracking",
                        fontSize = tokens.h2,
                        color = TextPrimary
                    )
                    Text(
                        text = "Monitor workflow exceptions and manage garment alterations",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                    Spacer(Modifier.padding(bottom = 12.dp))
                }

                // Primary & Secondary Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onLogNewDelay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Log New Delay",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = whiteBg
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRegisterAlteration,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, Primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                        ) {
                            Text(
                                text = "Register Alteration",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = onCreateReworkOrder,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.5.dp, Primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                        ) {
                            Text(
                                text = "Create Rework Order",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(Modifier.padding(bottom = 12.dp))
                }


                // 2x2 Metrics Grid
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "Active Delays",
                            value = "12",
                            valueColor = redText,
                            icon = Icons.Outlined.WarningAmber,
                            iconColor = redText,
                            subtitle = "2 more than yesterday",
                            modifier = Modifier.weight(1f),
                            tokens = tokens
                        )
                        MetricStatCard(
                            title = "Pending Reworks",
                            value = "8",
                            valueColor = yellowText,
                            icon = Icons.Outlined.AccessTime,
                            iconColor = yellowText,
                            subtitle = "4 awaiting review",
                            modifier = Modifier.weight(1f),
                            tokens = tokens
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "Resolved Today",
                            value = "15",
                            valueColor = greentext,
                            icon = Icons.Outlined.Check,
                            iconColor = greentext,
                            subtitle = "> 80% of daily target",
                            modifier = Modifier.weight(1f),
                            tokens = tokens
                        )
                        MetricStatCard(
                            title = "Avg Resolution",
                            value = "1.5 Days",
                            valueColor = Primary,
                            icon = Icons.Outlined.NearMe,
                            iconColor = Primary,
                            subtitle = "~30min less than last week",
                            modifier = Modifier.weight(1f),
                            tokens = tokens
                        )
                    }
                }

                // Active Management Queue Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Active Management Queue",
                        fontSize = tokens.bodyMedium,
                        color = TextPrimary
                    )

                    // Search Bar with fixed stable height
                    // Search Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = whiteBg,
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = mutedText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search orders...",
                                        fontSize = tokens.bodySmall,
                                        color = mutedText
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Primary),
                                    textStyle = TextStyle(
                                        fontSize = tokens.bodySmall,
                                        color = TextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            // Queue Item 1
            QueueOrderCard(
                orderNumber = "#ORD-4452",
                customerName = "Sarah",
                statusTag = "Billing Issue",
                statusTagColor = UrgentRed,
                statusTagBg = UrgentRedLight,
                reason = "In-process mod failed",
                teamLetter = "A",
                teamName = "Team A — Cutting",
                teamColor = Primary,
                deadline = "Oct 24, 2023",
                badgeText = "• Urgent",
                badgeColor = UrgentRed,
                badgeBg = null,
                tokens = tokens
            )

            // Queue Item 2
            QueueOrderCard(
                orderNumber = "#ORD-4451",
                customerName = "Markus",
                statusTag = "Material shortage",
                statusTagColor = AmberWarn,
                statusTagBg = AmberWarnLight,
                reason = "Awaiting lining fabrics",
                teamLetter = "P",
                teamName = "Procurement",
                teamColor = Color(0xFF8B5CF6),
                deadline = "Oct 25, 2023",
                badgeText = "Scheduled",
                badgeColor = ScheduledBlue,
                badgeBg = ScheduledBlueLight,
                tokens = tokens
            )

            // Queue Item 3
            QueueOrderCard(
                orderNumber = "#ORD-4405",
                customerName = "Reno",
                statusTag = "Delayed/post",
                statusTagColor = UrgentRed,
                statusTagBg = UrgentRedLight,
                reason = "Incorrect hem length",
                teamLetter = "C",
                teamName = "Team C — Finishing",
                teamColor = Color(0xFF0288D1),
                deadline = "Oct 26, 2023",
                badgeText = "• Urgent",
                badgeColor = UrgentRed,
                badgeBg = null,
                tokens = tokens
            )

            Column(Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp))
            {

                // Recent Team Activity Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Team Activity",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "View Log",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary,
                            modifier = Modifier.clickable { onViewLog() }
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = whiteBg,
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            TeamActivityRow(
                                bulletColor = greentext,
                                title = "Team C resolved alteration for #ORD-4425",
                                timeAgo = "10 mins ago",
                                note = null,
                                tokens = tokens
                            )

                            HorizontalDivider(color = BorderGray.copy(alpha = 0.5f))

                            TeamActivityRow(
                                bulletColor = Primary,
                                title = "Procurement updated status for #ORD-4451: Fabrics scheduled for Friday arrival",
                                timeAgo = "",
                                note = "2 hours ago",
                                tokens = tokens
                            )

                            HorizontalDivider(color = BorderGray.copy(alpha = 0.5f))

                            TeamActivityRow(
                                bulletColor = redText,
                                title = "System Alert: #ORD-4302 has exceeded 48hr deadline",
                                timeAgo = "4 hours ago",
                                titleColor = redText,
                                timeAgoColor = redText,
                                note = null,
                                tokens = tokens
                            )
                        }
                    }
                    Spacer(Modifier.padding(bottom = 10.dp))
                }

                // Weekly Quality Insight Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = InsightBg,
                    border = BorderStroke(1.dp, InsightBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Weekly Quality Insight",
                                fontSize = tokens.bodyMedium,
                                color = darkPurple
                            )
                        }

                        Text(
                            text = "Delay issues are down by 21% this week after implementing the new measurement protocol in Team A.",
                            fontSize = tokens.bodySmall,
                            color = darkPurple,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Text(
                            text = "Generate Full Report",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary,
                            modifier = Modifier.clickable { onGenerateReport() }
                        )
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Sub-Components
// ─────────────────────────────────────────────────────────────

@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    valueColor: Color,
    icon: ImageVector,
    iconColor: Color,
    subtitle: String,
    modifier: Modifier = Modifier,
    tokens: AppDesignTokens
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = tokens.caption,
                    color = TextSecondary,
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = value,
                fontSize = 24.sp,
                color = valueColor
            )

            Text(
                text = subtitle,
                fontSize = tokens.caption,
                color = mutedText
            )
        }
    }
}

@Composable
private fun QueueOrderCard(
    orderNumber: String,
    customerName: String,
    statusTag: String,
    statusTagColor: Color,
    statusTagBg: Color,
    reason: String,
    teamLetter: String,
    teamName: String,
    teamColor: Color,
    deadline: String,
    badgeText: String,
    badgeColor: Color,
    badgeBg: Color?,
    tokens: AppDesignTokens
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = whiteBg,
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title & Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$orderNumber • $customerName",
                    fontSize = tokens.bodySmall,
                    color = TextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusTagBg
                ) {
                    Text(
                        text = statusTag,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = statusTagColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = BorderGray.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))

            // Reason Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Reason: ",
                    fontSize = tokens.caption,
                    color = mutedText
                )
                Text(
                    text = reason,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(6.dp))

            // Team Row with Centered Avatar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Team: ",
                    fontSize = tokens.caption,
                    color = mutedText
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(teamColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = teamLetter,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = teamColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 10.sp
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = teamName,
                    fontSize = tokens.caption,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(10.dp))

            // Deadline & Action Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Deadline: ",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                    Text(
                        text = deadline,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (badgeBg != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = badgeText,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Options",
                        tint = mutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamActivityRow(
    bulletColor: Color,
    title: String,
    timeAgo: String,
    titleColor: Color = TextPrimary,
    timeAgoColor: Color = mutedText,
    note: String? = null,
    tokens: AppDesignTokens
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(bulletColor)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Normal,
                    color = titleColor,
                    lineHeight = 20.sp
                )
                if (!note.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = note,
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }
        }

        if (timeAgo.isNotBlank()) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = timeAgo,
                fontSize = tokens.caption,
                color = timeAgoColor,
                fontWeight = FontWeight.Normal
            )
        }
    }
}