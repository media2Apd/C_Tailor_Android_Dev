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
package com.cuso.mobile.view.home.hr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.redBg
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.TitleBar

// ── Design tokens ──
private val HrPrimary = Primary
private val CriticalRed = redText
private val WarningOrange = Color(0xFFF59E0B)
private val HealthyGreen = greentext
private val TitleColor = title_color
private val MutedColor = mutedText
private val BorderColor = PrimaryBorder
private val ActiveBg = Color(0xFFEDE9FE)

// ── Data Models ──
private data class InfoLine(val label: String, val value: String)

private data class TimelineEvent(
    val time: String,
    val label: String,
    val isMilestone: Boolean // true = filled blue dot (Checked In/Out), false = hollow grey dot (Break)
)

private enum class HistoryStatus { PRESENT, LATE, ABSENT }

private data class HistoryRecord(
    val date: String,
    val status: HistoryStatus,
    val checkIn: String,
    val checkOut: String,
    val workingHrs: String
)

private fun historyStatusColor(status: HistoryStatus): Color = when (status) {
    HistoryStatus.PRESENT -> HealthyGreen
    HistoryStatus.LATE -> WarningOrange
    HistoryStatus.ABSENT -> CriticalRed
}

private fun historyStatusLabel(status: HistoryStatus): String = when (status) {
    HistoryStatus.PRESENT -> "Present"
    HistoryStatus.LATE -> "Late"
    HistoryStatus.ABSENT -> "Absent"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceDetailScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onHistoryClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val attendanceInfo = listOf(
        InfoLine("Attendance Date", "30 Jul 2026"),
        InfoLine("Shift", "Morning Shift"),
        InfoLine("Check In", "9:02 AM"),
        InfoLine("Check Out", "06:18 PM"),
        InfoLine("Working Hours", "09h 16m"),
        InfoLine("Break Time", "45 mins"),
        InfoLine("Overtime", "30 mins")
    )

    val additionalInfo = listOf(
        InfoLine("Location", "Head Office"),
        InfoLine("Remarks", "Checked in on time")
    )

    val timeline = listOf(
        TimelineEvent("09:02 AM", "Checked In", isMilestone = true),
        TimelineEvent("12:35 PM", "Break Started", isMilestone = false),
        TimelineEvent("01:10 PM", "Break Ended", isMilestone = false),
        TimelineEvent("06:18 PM", "Checked Out", isMilestone = true)
    )

    val history = listOf(
        HistoryRecord("01 Jul 2026", HistoryStatus.PRESENT, "09:00 Am", "06:15 PM", "09h 13m"),
        HistoryRecord("02 Jul 2026", HistoryStatus.LATE, "10:00 Am", "06:15 PM", "09h 13m"),
        HistoryRecord("04 Jul 2026", HistoryStatus.ABSENT, "-", "-", "-")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = whiteBg) {
                Column { TitleBar("Attendance", onClose = onClose) }
            }
            HorizontalDivider(color = BorderColor)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Transparent),
            contentPadding = PaddingValues(bottom = tokens.screenPadding * 1.5f)
        ) {

            // ── Employee header ──
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("John Anderson", fontSize = tokens.bodyLarge, color = TitleColor)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(ActiveBg, RoundedCornerShape(999.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Active", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = HrPrimary)
                            }
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MutedColor, modifier = Modifier.size(tokens.iconSize))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("ID: EMP-1018 · Designation: Senior Software Engineer", fontSize = tokens.caption, color = MutedColor)
                }
            }

            // ── Attendance Information ──
            item { DetailSectionHeading("Attendance Information") }
            items(attendanceInfo) { line -> DetailLineRow(line) }
            item { Spacer(Modifier.height(6.dp)) }

            // ── Additional Information ──
            item { DetailSectionHeading("Additional Information") }
            items(additionalInfo) { line -> DetailLineRow(line) }
            item { Spacer(Modifier.height(6.dp)) }

            // ── Timeline ──
            item {
                DetailSectionHeading("Timeline")
                Column(modifier = Modifier.fillMaxWidth().background(Color.Transparent).padding(horizontal = tokens.screenPadding, vertical = 10.dp)) {
                    timeline.forEachIndexed { index, event ->
                        TimelineRow(event, isLast = index == timeline.lastIndex)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }

            // ── Attendance History ──
            item { DetailSectionHeading("Attendance History")
            Spacer(Modifier.height(5.dp))}
            items(history) { record ->
                HistoryCard(record, onClick = { onHistoryClick(record.date) })
            }
        }
    }
}

@Composable
private fun DetailSectionHeading(title: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
    ) {
        Text(title, fontSize = tokens.bodyLarge, color = TitleColor)
    }
}

@Composable
private fun DetailLineRow(line: InfoLine) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.background(Color.Transparent)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(line.label, fontSize = tokens.bodySmall, color = MutedColor)
            Text(line.value, fontSize = tokens.bodySmall, color = TitleColor)
        }
        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(horizontal = tokens.screenPadding))
    }
}

@Composable
private fun TimelineRow(event: TimelineEvent, isLast: Boolean) {
    val tokens = LocalAppTokens.current
    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // ── Dot (outer ring + inner filled dot) + connecting line ──
        Box(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight()
        ) {
            // ── Connector line — drawn full row height, centered under the dot ──
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 28.dp)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFE0E1F5))
                )
            }

            // ── Dot ──
            if (event.isMilestone) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD6DEFC)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(HrPrimary)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 2.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDDEF0)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9CA0C4))
                    )
                }
            }
        }
        Spacer(Modifier.width(14.dp))
        // ── Time + label ──
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 20.dp)) {
            Text(event.time, fontSize = tokens.bodySmall, color = TitleColor)
            Spacer(Modifier.height(2.dp))
            Text(event.label, fontSize = tokens.caption, color = MutedColor)
        }
    }
}

@Composable
private fun HistoryCard(record: HistoryRecord, onClick: () -> Unit) {
    val tokens = LocalAppTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(whiteBg)
            .clickable(onClick = onClick)
            .padding(horizontal = tokens.screenPadding, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(record.date, fontSize = tokens.bodyMedium, color = TitleColor)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .background(historyStatusColor(record.status).copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        historyStatusLabel(record.status),
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = historyStatusColor(record.status)
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MutedColor, modifier = Modifier.size(tokens.iconSize))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Check In", fontSize = tokens.caption, color = MutedColor)
                Spacer(Modifier.height(2.dp))
                Text(record.checkIn, fontSize = tokens.bodySmall, color = TitleColor)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Check Out", fontSize = tokens.caption, color = MutedColor)
                Spacer(Modifier.height(2.dp))
                Text(record.checkOut, fontSize = tokens.bodySmall, color = TitleColor)
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Working Hrs", fontSize = tokens.caption, color = MutedColor)
                Spacer(Modifier.height(2.dp))
                Text(record.workingHrs, fontSize = tokens.bodySmall, color = TitleColor)
            }
        }
    }
    HorizontalDivider(color = BorderColor)
}