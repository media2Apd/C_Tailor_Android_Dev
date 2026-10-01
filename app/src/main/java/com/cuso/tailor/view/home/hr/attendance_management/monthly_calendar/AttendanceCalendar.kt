package com.cuso.tailor.view.home.hr.attendance_management.monthly_calendar

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.viewmodel.HrViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class DayStatus(val color: Color, val label: String) {
    PRESENT(complete_button_bg, "Present"),
    ABSENT(redText, "Absent"),
    LATE(orangeText, "Late"),
    LEAVE(BluePrimary, "Leave"),
    NONE(Color.Transparent, "")
}

data class AttendanceRecord(
    val status: DayStatus,
    val checkIn: String? = null,
    val checkOut: String? = null
)

data class CalendarDay(
    val date: LocalDate,
    val dayNumber: String,
    val status: DayStatus = DayStatus.NONE,
    val timeTop: String? = null,
    val timeBottom: String? = null,
    val statusText: String? = null,
    val isHighlighted: Boolean = false,
    val isCurrentMonth: Boolean = true,
    val isWeekend: Boolean = false
)

data class AttendanceListItem(
    val date: LocalDate,
    val dayOfMonth: Int,
    val dayOfWeek: String,
    val statusText: String,
    val statusColor: Color,
    val timeRangeText: String,
    val workedTimeText: String,
    val isWeekOff: Boolean = false
)

@RequiresApi(Build.VERSION_CODES.O)
fun parseTimeToHourMinute(isoString: String?): String? {
    if (isoString.isNullOrBlank()) return null
    return try {
        val instant = Instant.parse(isoString)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH).format(zonedDateTime)
    } catch (_: Exception) {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun parseTimeToAmPm(isoString: String?): String? {
    if (isoString.isNullOrBlank()) return null
    return try {
        val instant = Instant.parse(isoString)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH).format(zonedDateTime).lowercase()
    } catch (_: Exception) {
        null
    }
}

fun formatMinutesToHours(minutes: Int): String {
    if (minutes <= 0) return "0m"
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun parseIsoToLocalDate(isoString: String): LocalDate? {
    return try {
        Instant.parse(isoString).atZone(ZoneId.systemDefault()).toLocalDate()
    } catch (_: Exception) {
        null
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun buildMonthCells(
    month: YearMonth,
    records: Map<LocalDate, AttendanceRecord>,
    today: LocalDate = LocalDate.now()
): List<CalendarDay> {
    val first = month.atDay(1)
    val leading = first.dayOfWeek.value - 1
    val total = ((leading + month.lengthOfMonth() + 6) / 7) * 7
    val start = first.minusDays(leading.toLong())

    return List(total) { i ->
        val date = start.plusDays(i.toLong())
        val inMonth = YearMonth.from(date) == month
        val rec = if (inMonth) records[date] else null

        CalendarDay(
            date = date,
            dayNumber = String.format(Locale.ENGLISH, "%02d", date.dayOfMonth),
            status = rec?.status ?: DayStatus.NONE,
            timeTop = rec?.checkIn,
            timeBottom = rec?.checkOut,
            statusText = if (rec != null && rec.status != DayStatus.PRESENT && rec.status != DayStatus.LATE) rec.status.label else null,
            isHighlighted = date == today,
            isCurrentMonth = inMonth,
            isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AttendanceCalendarScreen(
    onClose: () -> Unit = {},
    hrViewModel: HrViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var selectedView by remember { mutableStateOf("Calendar") }
    var monthDropdownOpen by remember { mutableStateOf(false) }

    var employeeDropdownOpen by remember { mutableStateOf(false) }
    val members by hrViewModel.members.collectAsState()
    var selectedEmployeeName by remember { mutableStateOf("") }
    var selectedEmployeeId by remember { mutableStateOf<String?>(null) }

    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    val monthFmt = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val apiAttendanceList by hrViewModel.monthlyAttendance.collectAsState()

    LaunchedEffect(Unit) {
        hrViewModel.fetchMembers(limit = 100)
    }

    LaunchedEffect(members) {
        if (members.isNotEmpty() && selectedEmployeeId == null) {
            val first = members.first()
            selectedEmployeeId = first._id
            selectedEmployeeName = "${first.firstName.orEmpty()} ${first.lastName.orEmpty()}".trim().ifBlank { first.memberId.orEmpty() }
        }
    }

    LaunchedEffect(selectedEmployeeId, currentMonth) {
        val empId = selectedEmployeeId
        if (!empId.isNullOrBlank()) {
            hrViewModel.fetchMonthlyAttendance(
                organizationMemberId = empId,
                month = currentMonth.monthValue,
                year = currentMonth.year
            )
        }
    }

    // Map API attendance records by date
    val records: Map<LocalDate, AttendanceRecord> = remember(apiAttendanceList) {
        apiAttendanceList.mapNotNull { item ->
            val localDate = parseIsoToLocalDate(item.date) ?: return@mapNotNull null
            val status = when (item.status?.lowercase()) {
                "present" -> if (item.isLate) DayStatus.LATE else DayStatus.PRESENT
                "late" -> DayStatus.LATE
                "absent" -> DayStatus.ABSENT
                "leave" -> DayStatus.LEAVE
                else -> DayStatus.NONE
            }

            val isAbsentOrLeave = status == DayStatus.ABSENT || status == DayStatus.LEAVE
            val inPunch = item.punches.firstOrNull { it.type.equals("in", ignoreCase = true) }?.time
            val outPunch = item.punches.lastOrNull { it.type.equals("out", ignoreCase = true) }?.time

            val checkIn = if (isAbsentOrLeave) null else parseTimeToHourMinute(inPunch)
            val checkOut = if (isAbsentOrLeave) null else parseTimeToHourMinute(outPunch)

            localDate to AttendanceRecord(
                status = status,
                checkIn = checkIn,
                checkOut = checkOut
            )
        }.toMap()
    }

    // Build items for the List View matching API records
    val listItems: List<AttendanceListItem> = remember(currentMonth, apiAttendanceList) {
        val daysInMonth = currentMonth.lengthOfMonth()
        val apiMap = apiAttendanceList.mapNotNull { item ->
            val date = parseIsoToLocalDate(item.date) ?: return@mapNotNull null
            date to item
        }.toMap()

        (1..daysInMonth).map { day ->
            val date = currentMonth.atDay(day)
            val item = apiMap[date]
            val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY
            val dayOfWeekStr = date.dayOfWeek.name.take(3)

            if (item != null) {
                val isAbsent = item.status.equals("absent", ignoreCase = true)
                val isLeave = item.status.equals("leave", ignoreCase = true)
                val isLate = item.isLate || item.status.equals("late", ignoreCase = true)
                val isPresent = item.status.equals("present", ignoreCase = true)

                val inPunch = item.punches.firstOrNull { it.type.equals("in", ignoreCase = true) }?.time
                val outPunch = item.punches.lastOrNull { it.type.equals("out", ignoreCase = true) }?.time

                val inFormatted = parseTimeToAmPm(inPunch)
                val outFormatted = parseTimeToAmPm(outPunch)

                val (statusText, statusColor) = when {
                    isLeave -> "Leave" to BluePrimary
                    isAbsent -> "Absent" to redText
                    isLate -> "Late" to orangeText
                    isPresent -> "Present" to complete_button_bg
                    else -> (item.status?.replaceFirstChar { it.uppercase() } ?: "—") to iconMuted
                }

                val timeRangeText = when {
                    isAbsent || isLeave -> "No punch records"
                    inFormatted != null && outFormatted != null -> "$inFormatted → $outFormatted"
                    inFormatted != null -> "$inFormatted → --:--"
                    else -> "No punch records"
                }

                AttendanceListItem(
                    date = date,
                    dayOfMonth = day,
                    dayOfWeek = dayOfWeekStr,
                    statusText = statusText,
                    statusColor = statusColor,
                    timeRangeText = timeRangeText,
                    workedTimeText = formatMinutesToHours(item.totalWorkingMinutes),
                    isWeekOff = false
                )
            } else {
                if (isSunday) {
                    AttendanceListItem(
                        date = date,
                        dayOfMonth = day,
                        dayOfWeek = dayOfWeekStr,
                        statusText = "Week Off",
                        statusColor = headerGrey,
                        timeRangeText = "No punch records",
                        workedTimeText = "0m",
                        isWeekOff = true
                    )
                } else {
                    AttendanceListItem(
                        date = date,
                        dayOfMonth = day,
                        dayOfWeek = dayOfWeekStr,
                        statusText = "—",
                        statusColor = iconMuted,
                        timeRangeText = "No punch records",
                        workedTimeText = "0m",
                        isWeekOff = false
                    )
                }
            }
        }
    }

    val calendarDays = remember(currentMonth, records) { buildMonthCells(currentMonth, records) }

    val monthRecords = remember(currentMonth, records) {
        records.filterKeys { YearMonth.from(it) == currentMonth }.values
    }
    val present = monthRecords.count { it.status == DayStatus.PRESENT }
    val late = monthRecords.count { it.status == DayStatus.LATE }
    val absent = monthRecords.count { it.status == DayStatus.ABSENT }
    val leave = monthRecords.count { it.status == DayStatus.LEAVE }
    val workingDays = calendarDays.count { it.isCurrentMonth && !it.isWeekend }
    val presenceRate = if (workingDays == 0) 0f else ((present + late).toFloat() / workingDays).coerceIn(0f, 1f)

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TitleBar(
                title = "Attendance Calendar",
                onClose = onClose
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = tokens.screenPadding)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(
                        horizontal = tokens.screenPadding,
                        vertical = tokens.extraPadding * 0.5f
                    )
                ) {
                    Text(
                        text = "Monthly Attendance Calendar",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Visualize team performance and presence schedules.",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Normal,
                        color = headerGrey
                    )
                }
            }

            // View Switcher (List View / Calendar)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.4f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                        .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
                        .background(whiteBg)
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                                .background(if (selectedView == "List View") primary_light else Color.Transparent)
                                .clickable { selectedView = "List View" }
                                .padding(vertical = tokens.extraPadding * 0.6f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "List View",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedView == "List View") Primary else headerGrey
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.7f))
                                .background(if (selectedView == "Calendar") primary_light else Color.Transparent)
                                .clickable { selectedView = "Calendar" }
                                .padding(vertical = tokens.extraPadding * 0.6f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Calendar",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedView == "Calendar") Primary else headerGrey
                            )
                        }
                    }
                }
            }

            // Export Report Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.4f)
                ) {
                    OutlinedButton(
                        onClick = { },
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
                        border = BorderStroke(1.dp, Primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(tokens.buttonHeight),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                        Text(
                            text = "Export Report",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }
                }
            }

            // Month & Employee Dropdown Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.4f),
                    verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                ) {
                    FormDropdown(
                        label = "Month",
                        value = currentMonth.format(monthFmt),
                        expanded = monthDropdownOpen,
                        onExpandChange = { monthDropdownOpen = it },
                        options = remember {
                            (-6L..6L).map { YearMonth.now().plusMonths(it).format(monthFmt) }
                        },
                        onOptionSelected = { currentMonth = YearMonth.parse(it, monthFmt) }
                    )

                    FormDropdown(
                        label = "Employee",
                        value = selectedEmployeeName.ifBlank { "Select Employee" },
                        expanded = employeeDropdownOpen,
                        onExpandChange = { employeeDropdownOpen = it },
                        options = members.map { member ->
                            "${member.firstName.orEmpty()} ${member.lastName.orEmpty()}".trim().ifBlank { member.memberId.orEmpty() }
                        },
                        onOptionSelected = { chosenName ->
                            selectedEmployeeName = chosenName
                            val matchedMember = members.find { member ->
                                val name = "${member.firstName.orEmpty()} ${member.lastName.orEmpty()}".trim().ifBlank { member.memberId.orEmpty() }
                                name.equals(chosenName, ignoreCase = true)
                            }
                            selectedEmployeeId = matchedMember?._id
                        }
                    )
                }
            }

            // View Section (List View vs Calendar Grid)
            if (selectedView == "List View") {
                item {
                    AttendanceListViewCard(
                        items = listItems,
                        horizontalPadding = tokens.screenPadding
                    )
                }
            } else {
                // Status Filter Legend Header
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.5f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = BorderStroke(1.dp, grey_border),
                        color = whiteBg
                    ) {
                        Column(modifier = Modifier.padding(tokens.extraPadding * 0.8f)) {
                            Text(
                                text = "STATUS FILTERS",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = headerGrey
                            )
                            Spacer(Modifier.height(tokens.extraPadding * 0.6f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                            ) {
                                listOf(
                                    Triple("Present", complete_button_bg, greenBg),
                                    Triple("Absent", redText, redBg),
                                    Triple("Late", orangeText, orangeBg),
                                    Triple("Leave", BluePrimary, activity_purple_bg)
                                ).forEach { (label, dotColor, _) ->
                                    Surface(
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 1.5f),
                                        border = BorderStroke(1.dp, grey_border),
                                        color = whiteBg
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = tokens.extraPadding * 0.8f,
                                                vertical = tokens.extraPadding * 0.4f
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = label,
                                                fontSize = tokens.label,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Calendar Grid Surface
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.5f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        border = BorderStroke(1.dp, sectionBorder),
                        color = whiteBg
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(headerBg)
                                    .padding(vertical = tokens.extraPadding * 0.5f)
                            ) {
                                daysOfWeek.forEach { day ->
                                    Text(
                                        text = day,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = headerGrey
                                    )
                                }
                            }
                            HorizontalDivider(color = sectionBorder, thickness = 1.dp)

                            val rows = calendarDays.chunked(7)
                            rows.forEach { week ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    week.forEach { cell ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(72.dp)
                                                .background(
                                                    when {
                                                        cell.isHighlighted -> background_light_purple
                                                        cell.isWeekend && cell.isCurrentMonth -> badgeGrey
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .border(0.5.dp, sectionBorder)
                                                .padding(horizontal = 3.dp, vertical = 3.dp)
                                        ) {
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (cell.status != DayStatus.NONE) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .clip(CircleShape)
                                                                .background(cell.status.color)
                                                        )
                                                    } else {
                                                        Spacer(modifier = Modifier.size(6.dp))
                                                    }

                                                    Text(
                                                        text = cell.dayNumber,
                                                        fontSize = tokens.caption,
                                                        fontWeight = FontWeight.Medium,
                                                        color = if (cell.isCurrentMonth) title_color else iconMuted,
                                                        textAlign = TextAlign.End
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val compactTimeStyle = TextStyle(
                                                        fontSize = tokens.label,
                                                        fontWeight = FontWeight.Normal,
                                                        color = textSubdued,
                                                        platformStyle = PlatformTextStyle(
                                                            includeFontPadding = false
                                                        )
                                                    )

                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        if (!cell.timeTop.isNullOrBlank()) {
                                                            Text(
                                                                text = cell.timeTop,
                                                                style = compactTimeStyle,
                                                                maxLines = 1
                                                            )
                                                        }

                                                        if (!cell.timeBottom.isNullOrBlank()) {
                                                            Text(
                                                                text = cell.timeBottom,
                                                                style = compactTimeStyle,
                                                                maxLines = 1
                                                            )
                                                        }

                                                        if (!cell.statusText.isNullOrBlank()) {
                                                            Text(
                                                                text = cell.statusText,
                                                                style = compactTimeStyle.copy(
                                                                    fontWeight = FontWeight.Medium,
                                                                    color = cell.status.color
                                                                ),
                                                                maxLines = 1
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Employee Summary Details Card
            item {
                val currentMember = remember(members, selectedEmployeeId) {
                    members.find { it._id == selectedEmployeeId }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.4f),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    border = BorderStroke(1.dp, grey_border),
                    color = whiteBg
                ) {
                    Column(modifier = Modifier.padding(tokens.extraPadding)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val initials = remember(currentMember) {
                                "${currentMember?.firstName?.firstOrNull()?.uppercaseChar() ?: ' '}${currentMember?.lastName?.firstOrNull()?.uppercaseChar() ?: ' '}".trim().ifBlank { "?" }
                            }

                            Box(
                                modifier = Modifier
                                    .size(tokens.buttonHeight)
                                    .clip(CircleShape)
                                    .background(background_light_purple),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = Primary,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = tokens.bodySmall
                                )
                            }
                            Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${currentMember?.firstName.orEmpty()} ${currentMember?.lastName.orEmpty()}".trim().ifBlank { selectedEmployeeName },
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                                Text(
                                    text = currentMember?.departmentId?.name ?: "Department",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Normal,
                                    color = headerGrey
                                )
                            }
                            StatusBadge(
                                text = currentMember?.memberId ?: "EMP",
                                bgColor = background_light_purple,
                                textColor = Primary,
                                showDot = false
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))
                        HorizontalDivider(color = sectionBorder, thickness = 1.dp)
                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                        val shiftInfo = apiAttendanceList.firstOrNull()?.shiftId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Primary Shift",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Normal,
                                color = headerGrey
                            )
                            Text(
                                text = "${shiftInfo?.name ?: "General"} (${shiftInfo?.startTime ?: "09:00"}–${shiftInfo?.endTime ?: "18:00"})",
                                fontSize = tokens.caption,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Monthly Counters Summary Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.4f),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    border = BorderStroke(1.dp, grey_border),
                    color = whiteBg
                ) {
                    Column(modifier = Modifier.padding(tokens.extraPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                            Spacer(Modifier.width(tokens.extraPadding * 0.5f))
                            Text(
                                text = "Monthly Summary",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total Working Days",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Normal,
                                color = TextPrimary
                            )
                            Text(
                                text = workingDays.toString(),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 0.6f))
                        HorizontalDivider(color = grey_border, thickness = 1.dp)
                        Spacer(Modifier.height(tokens.extraPadding * 0.6f))

                        val summaryStats = listOf(
                            Triple("Present", present.toString(), complete_button_bg),
                            Triple("Absent", absent.toString(), redText),
                            Triple("Leave", leave.toString(), BluePrimary),
                            Triple("Late Count", late.toString(), orangeText)
                        )

                        summaryStats.forEach { (label, count, dotColor) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = tokens.extraPadding * 0.2f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                    Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                                    Text(
                                        text = label,
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Normal,
                                        color = headerGrey
                                    )
                                }
                                Text(
                                    text = count,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.6f),
                            color = badgeGrey
                        ) {
                            Column(modifier = Modifier.padding(tokens.extraPadding * 0.7f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Presence Rate",
                                        fontSize = tokens.label,
                                        fontWeight = FontWeight.Normal,
                                        color = headerGrey
                                    )
                                    Text(
                                        text = String.format(Locale.ENGLISH, "%.1f%%", presenceRate * 100),
                                        fontSize = tokens.label,
                                        fontWeight = FontWeight.Medium,
                                        color = Primary
                                    )
                                }
                                Spacer(Modifier.height(tokens.extraPadding * 0.4f))
                                LinearProgressIndicator(
                                    progress = { presenceRate },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius)),
                                    color = Primary,
                                    trackColor = sectionBorder
                                )
                            }
                        }
                    }
                }
            }

            // Legend Info Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding * 0.4f),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    border = BorderStroke(1.dp, grey_border),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.extraPadding),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f)
                    ) {
                        Text(
                            text = "CALENDAR LEGEND",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Medium,
                            color = headerGrey
                        )

                        listOf(
                            "On Time" to complete_button_bg,
                            "Late Arrival" to orangeText,
                            "Absent" to redText,
                            "Authorized Leave" to BluePrimary
                        ).forEach { (legendText, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(Modifier.width(tokens.extraPadding * 0.6f))
                                Text(
                                    text = legendText,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Normal,
                                    color = headerGrey
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceListViewCard(
    items: List<AttendanceListItem>,
    horizontalPadding: Dp
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = tokens.extraPadding * 0.5f),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        border = BorderStroke(1.dp, sectionBorder),
        color = whiteBg
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = tokens.extraPadding,
                            vertical = tokens.extraPadding * 0.8f
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        color = badgeGrey
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.dayOfMonth.toString(),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = title_color
                            )
                            Text(
                                text = item.dayOfWeek,
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Normal,
                                color = iconMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(tokens.extraPadding * 0.8f))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusIcon = when {
                                item.isWeekOff -> Icons.Default.NightsStay
                                item.statusText == "—" -> Icons.Default.RemoveCircleOutline
                                else -> Icons.Default.CheckCircleOutline
                            }

                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = item.statusColor,
                                modifier = Modifier.size(tokens.iconSize * 0.8f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.statusText,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = item.statusColor
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.timeRangeText,
                            fontSize = tokens.caption,
                            color = headerGrey,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.workedTimeText,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "worked",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Normal,
                            color = iconMuted
                        )
                    }
                }

                if (index < items.lastIndex) {
                    HorizontalDivider(
                        color = grey_border,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = tokens.extraPadding)
                    )
                }
            }
        }
    }
}