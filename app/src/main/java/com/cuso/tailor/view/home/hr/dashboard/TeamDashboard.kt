@file:Suppress("unused", "UNUSED_PARAMETER","DEPRECATION", "AssignedValueIsNeverRead")

package com.cuso.tailor.view.home.hr.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*

@Composable
fun TeamOperationsDashboardScreen() {
    val tokens = LocalAppTokens.current
    var selectedTopTab by remember { mutableStateOf(1) } // 0: My Space, 1: Team, 2: Org
    var searchQuery by remember { mutableStateOf("") }
    var selectedProfileIndex by remember { mutableStateOf(0) }
    var selectedDetailTab by remember { mutableStateOf(4) } // 4: Activity

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentPadding = PaddingValues(tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)
    ) {
        // Top Space/Team/Org Segmented Control
        item {
            TopSegmentedSwitch(
                selectedIndex = selectedTopTab,
                onTabSelected = { selectedTopTab = it }
            )
        }

        // 6 Overview Counter Cards (2x3 Grid)
        item {
            OverviewCountersGrid()
        }

        // Search Bar and Quick Filters
        item {
            SearchAndFilterSection(
                searchQuery = searchQuery,
                onSearchChanged = { searchQuery = it }
            )
        }

        // Horizontal Profile Selection List
        item {
            HorizontalEmployeeSelector(
                selectedIndex = selectedProfileIndex,
                onSelected = { selectedProfileIndex = it }
            )
        }

        // Selected Employee Details & Attendance Card
        item {
            SelectedEmployeeCard(
                selectedTab = selectedDetailTab,
                onTabSelect = { selectedDetailTab = it }
            )
        }

        // Today's Team Status Table
        item {
            TodayTeamStatusCard()
        }

        // Weekly Attendance Matrix
        item {
            WeeklyAttendanceCard()
        }

        // Pending Team Requests Section
        item {
            PendingRequestsCard()
        }

        // Team Shift Distribution Donut Chart
        item {
            ShiftDistributionCard()
        }

        // Training and Doc Alerts Section
        item {
            TrainingDocAlertsCard()
        }

        // Leave Balance Visual Progress Indicators
        item {
            LeaveBalanceCard()
        }
    }
}

// =============================================================================
// Top Navigation Segmented Switch
// =============================================================================
@Composable
private fun TopSegmentedSwitch(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tokens = LocalAppTokens.current
    val tabs = listOf("My Space", "Team", "Org")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.buttonHeight)
            .background(color = light_blue_border.copy(alpha = 0.35f), shape = RoundedCornerShape(tokens.cardCornerRadius))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (isSelected) BluePrimary else Color.Transparent,
                        shape = RoundedCornerShape(tokens.cardCornerRadius - 4.dp)
                    )
                    .clickable { onTabSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) whiteBg else TextSecondary
                )
            }
        }
    }
}

// =============================================================================
// Overview 6 Counters Grid
// =============================================================================
@Composable
private fun OverviewCountersGrid() {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Group,
                iconBg = primary_light,
                iconColor = BluePrimary,
                value = "16",
                label = "Team Members"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Apartment,
                iconBg = greenBg,
                iconColor = greentext,
                value = "12",
                label = "In Office"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CalendarToday,
                iconBg = orangeBg,
                iconColor = orangeText,
                value = "2",
                label = "On Leave"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Home,
                iconBg = cyanBg,
                iconColor = chartBlue,
                value = "1",
                label = "WFH"
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.PersonOff,
                iconBg = redBg,
                iconColor = redText,
                value = "1",
                label = "Absent"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Schedule,
                iconBg = purpleBg,
                iconColor = purpleText,
                value = "3",
                label = "Pending Requests"
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    value: String,
    label: String
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Row(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(tokens.buttonHeight)
                    .background(iconBg, RoundedCornerShape(tokens.cardCornerRadius / 1.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }
            Column {
                Text(
                    text = value,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = label,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// =============================================================================
// Search & Filter Row
// =============================================================================
@Composable
private fun SearchAndFilterSection(
    searchQuery: String,
    onSearchChanged: (String) -> Unit
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding / 2)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(tokens.fieldHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = mutedText,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Text(
                        text = if (searchQuery.isEmpty()) "Search employee..." else searchQuery,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (searchQuery.isEmpty()) mutedText else TextPrimary
                    )
                }
            }

            Surface(
                modifier = Modifier.size(tokens.fieldHeight),
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Filter",
                        tint = TextSecondary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(label = "Dept:", value = "All")
            FilterChipItem(label = "Status:", value = "All")
            FilterChipItem(label = "Shift:", value = "All")
        }
    }
}

@Composable
private fun FilterChipItem(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Surface(
        shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f),
        color = whiteBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            Text(
                text = value,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

// =============================================================================
// Horizontal Employee Profile Selector
// =============================================================================
@Composable
private fun HorizontalEmployeeSelector(
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    val tokens = LocalAppTokens.current
    val employees = listOf(
        Triple("Anita Sharma", "UI Designer", "In Office"),
        Triple("Arun K", "Developer", "WFH"),
        Triple("Priya S", "UI Designer", "Late")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        employees.forEachIndexed { index, (name, role, status) ->
            val isSelected = selectedIndex == index
            Surface(
                modifier = Modifier
                    .width(135.dp)
                    .clickable { onSelected(index) },
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(tokens.imageDp / 1.25f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(BorderGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.BottomEnd)
                                .background(
                                    when (status) {
                                        "In Office" -> greentext
                                        "WFH" -> chartBlue
                                        else -> yellowText
                                    },
                                    CircleShape
                                )
                                .border(1.5.dp, whiteBg, CircleShape)
                        )
                    }

                    Text(
                        text = name,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = role,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    StatusBadge(status = status)
                }
            }
        }
    }
}

// =============================================================================
// Selected Employee Detailed Profile & Activity Card
// =============================================================================
@Composable
private fun SelectedEmployeeCard(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.buttonHeight)
                            .clip(CircleShape)
                            .background(BorderGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }
                    Column {
                        Text(
                            text = "Anita Sharma",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "UI Designer  |  EMP-0382",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "Design Department",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
                StatusBadge(status = "In Office")
            }

            // Stat Metric Pairs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricInfoBox(
                    modifier = Modifier.weight(1f),
                    title = "CHECK-IN",
                    value = "09:01 AM"
                )
                MetricInfoBox(
                    modifier = Modifier.weight(1f),
                    title = "SHIFT",
                    value = "09:00 AM - 06:00 PM"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricInfoBox(
                    modifier = Modifier.weight(1f),
                    title = "WORKING TODAY",
                    value = "6h 42m"
                )
                MetricInfoBox(
                    modifier = Modifier.weight(1f),
                    title = "ATTENDANCE THIS MONTH",
                    value = "96%"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricInfoBox(
                    modifier = Modifier.weight(1f),
                    title = "LEAVE BALANCE",
                    value = "8 / 12 Days"
                )
                MetricInfoBox(
                    modifier = Modifier.weight(1f),
                    title = "LATE COUNT (THIS MONTH)",
                    value = "1"
                )
            }

            // Internal Tabs
            val subTabs = listOf("Attendance", "Leave", "Shift", "Requests", "Activity")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                subTabs.forEachIndexed { index, title ->
                    val isTabActive = selectedTab == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onTabSelect(index) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isTabActive) BluePrimary else TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(36.dp)
                                .background(if (isTabActive) BluePrimary else Color.Transparent)
                        )
                    }
                }
            }

            // Activity Timeline Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Activity Timeline",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = BluePrimary
                )
                Text(
                    text = "Live Sync",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = mutedText
                )
            }

            TimelineEvent(
                dotColor = greentext,
                title = "Checked in",
                subtitle = "Checked in from Web App",
                time = "09:01 AM"
            )
            TimelineEvent(
                dotColor = orangeText,
                title = "Break Started",
                subtitle = "Lunch break break status",
                time = "01:10 PM"
            )
            TimelineEvent(
                dotColor = greentext,
                title = "Resumed Work",
                subtitle = "Returned from break",
                time = "02:04 PM"
            )

            // Monthly Mini Calendar Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance This Month",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = BluePrimary
                )
                Text(
                    text = "View Calendar",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = BluePrimary
                )
            }

            AttendanceMiniCalendar()
        }
    }
}

@Composable
private fun MetricInfoBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f),
        color = cardBgLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = mutedText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun TimelineEvent(
    dotColor: Color,
    title: String,
    subtitle: String,
    time: String
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .background(dotColor, CircleShape)
            )
            Column {
                Text(
                    text = title,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }
        Text(
            text = time,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

@Composable
private fun AttendanceMiniCalendar() {
    val tokens = LocalAppTokens.current
    val days = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            days.forEach { day ->
                Text(
                    text = day,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Calendar Week Samples
        val sampleGrid = listOf(
            listOf("27", "28", "29", "30", "1", "2", "3"),
            listOf("4", "5", "6", "7", "8", "9", "10"),
            listOf("11", "12", "13", "14", "15", "16", "17"),
            listOf("18", "19", "20", "21", "22", "23", "24"),
            listOf("25", "26", "27", "28", "29", "30", "31")
        )

        sampleGrid.forEachIndexed { rowIndex, week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                week.forEach { dateText ->
                    val isPresent = rowIndex in 1..3 && dateText.toIntOrNull() != null && dateText != "8" && dateText != "13" && dateText != "22" && dateText != "24"
                    val isLate = dateText == "8" || dateText == "13" || dateText == "22"
                    val isBlackSelected = dateText == "24"
                    val isMuted = rowIndex == 0 && dateText.toInt() > 15

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .background(
                                color = when {
                                    isBlackSelected -> TextPrimary
                                    isLate -> orangeBg
                                    isPresent -> greenBg
                                    else -> Color.Transparent
                                },
                                shape = RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dateText,
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                isBlackSelected -> whiteBg
                                isLate -> orangeText
                                isPresent -> greentext
                                isMuted -> mutedText
                                else -> TextSecondary
                            }
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// Today's Team Status Table
// =============================================================================
@Composable
private fun TodayTeamStatusCard() {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Today's Team Status",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Daily attendance check & hours",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
                Text(
                    text = "View All",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = BluePrimary
                )
            }

            HorizontalDivider(color = dividerColor)

            // Header Row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Employee", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.weight(1.3f))
                Text("Status", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.weight(1f))
                Text("Check-in", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.weight(0.9f))
                Text("Shift", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.weight(0.8f))
            }

            // Row Items
            TeamStatusRowItem("Anita Sharma", "UI Designer", "In Office", "09:01 AM", "09:00 AM")
            TeamStatusRowItem("Arun K", "Developer", "WFH", "09:05 AM", "09:00 AM")
            TeamStatusRowItem("Priya S", "UI Designer", "Late", "09:28 AM", "09:00 AM")
            TeamStatusRowItem("Rahul M", "Developer", "Absent", "--", "09:00 AM")
            TeamStatusRowItem("Sneha L", "UI Designer", "On Leave", "--", "09:00 AM")
            TeamStatusRowItem("Karan P", "Developer", "WFH", "09:07 AM", "09:00 AM")
        }
    }
}

@Composable
private fun TeamStatusRowItem(
    name: String,
    role: String,
    status: String,
    checkIn: String,
    shift: String
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1.3f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(BorderGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(name, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(role, fontSize = tokens.label, fontWeight = FontWeight.Medium, color = TextSecondary)
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            StatusBadge(status = status)
        }

        Text(
            text = checkIn,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(0.9f)
        )

        Text(
            text = shift,
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.weight(0.8f)
        )
    }
}

// =============================================================================
// Weekly Attendance Matrix
// =============================================================================
@Composable
private fun WeeklyAttendanceCard() {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Attendance",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = BluePrimary
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = primary_light
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("This Week", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary)
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(tokens.iconSize))
                    }
                }
            }

            // Days Header
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Employee", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, modifier = Modifier.weight(1.2f))
                listOf("Mon", "Tue", "Wed", "Thu", "Fri").forEach { day ->
                    Text(day, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }

            WeeklyAttendanceRow("Anita", listOf("P", "P", "P", "P", "P"))
            WeeklyAttendanceRow("Arun", listOf("WFH", "P", "WFH", "P", "P"))
            WeeklyAttendanceRow("Priya", listOf("P", "Late", "P", "P", "Late"))
            WeeklyAttendanceRow("Rahul", listOf("P", "P", "A", "P", "A"))
            WeeklyAttendanceRow("Sneha", listOf("L", "L", "P", "L", "L"))
        }
    }
}

@Composable
private fun WeeklyAttendanceRow(name: String, statuses: List<String>) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1.2f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(BorderGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            }
            Text(name, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
        }

        statuses.forEach { st ->
            val bg = when (st) {
                "P" -> greenBg
                "WFH" -> cyanBg
                "Late" -> orangeBg
                "A" -> redBg
                else -> purpleBg // "L"
            }
            val fg = when (st) {
                "P" -> greentext
                "WFH" -> chartBlue
                "Late" -> orangeText
                "A" -> redText
                else -> purpleText
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .height(24.dp)
                    .background(bg, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(st, fontSize = tokens.label, fontWeight = FontWeight.Medium, color = fg)
            }
        }
    }
}

// =============================================================================
// Pending Requests Card
// =============================================================================
@Composable
private fun PendingRequestsCard() {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pending Team Requests", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = BluePrimary)
                Text("View All", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary)
            }

            RequestItemCard(
                name = "Priya S",
                desc = "Leave Request | 02 - 03 Jun 2025"
            )
            RequestItemCard(
                name = "Rahul M",
                desc = "Regularization | 30 May 2025"
            )
        }
    }
}

@Composable
private fun RequestItemCard(name: String, desc: String) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f),
        color = cardBgLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(BorderGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(name, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text(desc, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = yellowBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, yellowText.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "Pending",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = yellowText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// =============================================================================
// Shift Distribution Donut Chart Card
// =============================================================================
@Composable
private fun ShiftDistributionCard() {
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
            Text(
                text = "Team Shift Distribution",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = BluePrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 12.dp.toPx()
                        // 10 General (chartBlue), 4 Morning (greentext), 2 Evening (chartAmber)
                        drawArc(
                            color = chartBlue,
                            startAngle = -90f,
                            sweepAngle = 225f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        drawArc(
                            color = greentext,
                            startAngle = 135f,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        drawArc(
                            color = chartAmber,
                            startAngle = 225f,
                            sweepAngle = 45f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("16", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                        Text("Total", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = TextSecondary)
                    }
                }

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LegendItem(dotColor = chartBlue, label = "General Shift", count = "10")
                    LegendItem(dotColor = greentext, label = "Morning Shift", count = "4")
                    LegendItem(dotColor = chartAmber, label = "Evening Shift", count = "2")
                }
            }
        }
    }
}

@Composable
private fun LegendItem(dotColor: Color, label: String, count: String) {
    val tokens = LocalAppTokens.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
        Text(
            text = "$label ($count)",
            fontSize = tokens.caption,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

// =============================================================================
// Training & Document Alerts Card
// =============================================================================
@Composable
private fun TrainingDocAlertsCard() {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Training & Doc Alerts", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = BluePrimary)
                Text("View All", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary)
            }

            AlertNotificationRow(
                icon = Icons.Outlined.School,
                title = "Workplace Safety Training",
                dueDate = "Due on 05 Jun 2025",
                badgeText = "Overdue",
                isOverdue = true
            )

            AlertNotificationRow(
                icon = Icons.Outlined.Shield,
                title = "Cyber Security Awareness",
                dueDate = "Due on 10 Jun 2025",
                badgeText = "Due Soon",
                isOverdue = false
            )
        }
    }
}

@Composable
private fun AlertNotificationRow(
    icon: ImageVector,
    title: String,
    dueDate: String,
    badgeText: String,
    isOverdue: Boolean
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f),
        color = if (isOverdue) redBg.copy(alpha = 0.3f) else yellowBg.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isOverdue) redBg else yellowBg)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (isOverdue) redBg else yellowBg, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isOverdue) redText else orangeText,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Column {
                    Text(title, fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = TextPrimary)
                    Text(dueDate, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isOverdue) redBg else yellowBg
            ) {
                Text(
                    text = badgeText,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.Medium,
                    color = if (isOverdue) redText else orangeText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// =============================================================================
// Leave Balance Progress Card
// =============================================================================
@Composable
private fun LeaveBalanceCard() {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        color = whiteBg
    ) {
        Column(
            modifier = Modifier.padding(tokens.extraPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("LEAVE BALANCE", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = BluePrimary)
                Text("View Details →", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = BluePrimary)
            }

            LeaveProgressBarItem(
                title = "Casual Leave",
                countText = "8 / 12 Days",
                progress = 8f / 12f,
                barColor = redText
            )

            LeaveProgressBarItem(
                title = "Annual Leave",
                countText = "12 / 18 Days",
                progress = 12f / 18f,
                barColor = greentext
            )
        }
    }
}

@Composable
private fun LeaveProgressBarItem(
    title: String,
    countText: String,
    progress: Float,
    barColor: Color
) {
    val tokens = LocalAppTokens.current

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(countText, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = TextSecondary)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = BorderGray
        )
    }
}

// =============================================================================
// Shared Generic Status Badge
// =============================================================================
@Composable
private fun StatusBadge(status: String) {
    val tokens = LocalAppTokens.current

    val (bg, fg, dot) = when (status) {
        "In Office" -> Triple(greenBg, greentext, greentext)
        "WFH" -> Triple(cyanBg, chartBlue, chartBlue)
        "Late" -> Triple(orangeBg, orangeText, orangeText)
        "Absent" -> Triple(redBg, redText, redText)
        "On Leave" -> Triple(purpleBg, purpleText, purpleText)
        else -> Triple(BorderGray, TextSecondary, TextSecondary)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(dot, CircleShape)
            )
            Text(
                text = status,
                fontSize = tokens.label,
                fontWeight = FontWeight.Medium,
                color = fg
            )
        }
    }
}