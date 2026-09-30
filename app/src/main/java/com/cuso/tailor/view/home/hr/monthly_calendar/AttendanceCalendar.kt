package com.cuso.tailor.view.home.hr.monthly_calendar

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class DayStatus(val color: Color, val label: String) {
    PRESENT(Color(0xFF10B981), "Present"),
    ABSENT(Color(0xFFEF4444), "Absent"),
    LATE(Color(0xFFF59E0B), "Late"),
    LEAVE(Color(0xFF3B82F6), "Leave"),
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
    hrViewModel: com.cuso.tailor.viewmodel.HrViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    var selectedView by remember { mutableStateOf("Calendar") }
    var monthDropdownOpen by remember { mutableStateOf(false) }

    // Dropdown Employee states
    var employeeDropdownOpen by remember { mutableStateOf(false) }
    val members by hrViewModel.members.collectAsState()
    var selectedEmployeeName by remember { mutableStateOf("") }
    var selectedEmployeeId by remember { mutableStateOf<String?>(null) }

    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    val monthFmt = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    // API attendance data
    val apiAttendanceList by hrViewModel.monthlyAttendance.collectAsState()
    val isLoading by hrViewModel.isLoadingMonthlyAttendance.collectAsState()

    // Load initial members list
    LaunchedEffect(Unit) {
        hrViewModel.fetchMembers(limit = 100)
    }

    // Auto-select first member
    LaunchedEffect(members) {
        if (members.isNotEmpty() && selectedEmployeeId == null) {
            val first = members.first()
            selectedEmployeeId = first._id
            selectedEmployeeName = "${first.firstName.orEmpty()} ${first.lastName.orEmpty()}".trim().ifBlank { first.memberId.orEmpty() }
        }
    }

    // Trigger API whenever selected employee or month changes
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

    // Convert API records into a map keyed by LocalDate
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

    // Build items for the List View
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
                val isLate = item.isLate

                val inPunch = item.punches.firstOrNull { it.type.equals("in", ignoreCase = true) }?.time
                val outPunch = item.punches.lastOrNull { it.type.equals("out", ignoreCase = true) }?.time

                val inFormatted = parseTimeToAmPm(inPunch)
                val outFormatted = parseTimeToAmPm(outPunch)

                val (statusText, statusColor) = when {
                    isLeave -> "Leave" to Color(0xFF3B82F6)
                    isAbsent -> "Absent" to Color(0xFFEF4444)
                    isLate -> "Late" to Color(0xFFF59E0B)
                    else -> "Present" to Color(0xFF2563EB)
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
                        statusColor = Color(0xFF64748B),
                        timeRangeText = "No punch records",
                        workedTimeText = "0m",
                        isWeekOff = true
                    )
                } else {
                    AttendanceListItem(
                        date = date,
                        dayOfMonth = day,
                        dayOfWeek = dayOfWeekStr,
                        statusText = "Absent",
                        statusColor = Color(0xFFEF4444),
                        timeRangeText = "No punch records",
                        workedTimeText = "0m",
                        isWeekOff = false
                    )
                }
            }
        }
    }

    val calendarDays = remember(currentMonth, records) { buildMonthCells(currentMonth, records) }

    // Summary calculation from API data
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
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Section Title & Description
            item {
                Column(
                    modifier = Modifier.padding(
                        horizontal = tokens.screenPadding,
                        vertical = 8.dp
                    )
                ) {
                    Text(
                        text = "Monthly Attendance Calendar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Visualize team performance and presence schedules.",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }
            }

            // View Switcher (List View / Calendar)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, grey_border, RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedView == "List View") Color(0xFFE0E7FF) else Color.Transparent)
                                .clickable { selectedView = "List View" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "List View",
                                fontSize = tokens.bodySmall,
                                fontWeight = if (selectedView == "List View") FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selectedView == "List View") Primary else headerGrey
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedView == "Calendar") Color(0xFFE0E7FF) else Color.Transparent)
                                .clickable { selectedView = "Calendar" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Calendar",
                                fontSize = tokens.bodySmall,
                                fontWeight = if (selectedView == "Calendar") FontWeight.SemiBold else FontWeight.Medium,
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
                        .padding(horizontal = tokens.screenPadding, vertical = 6.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Export action */ },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.5.dp, Primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteBg)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Export Report",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }
                }
            }

            // Month Dropdown & Employee Dropdown
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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

            // Conditional View Rendering: List View vs Calendar View
            if (selectedView == "List View") {
                item {
                    AttendanceListViewCard(
                        items = listItems,
                        horizontalPadding = tokens.screenPadding
                    )
                }
            } else {
                // Status Filters Container
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, grey_border),
                        color = whiteBg
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "STATUS FILTERS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = headerGrey
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Triple("Present", Color(0xFF10B981), Color(0xFFF0FDF4)),
                                    Triple("Absent", Color(0xFFEF4444), Color(0xFFFEF2F2)),
                                    Triple("Late", Color(0xFFF59E0B), Color(0xFFFFFBEB)),
                                    Triple("Leave", Color(0xFF3B82F6), Color(0xFFEFF6FF))
                                ).forEach { (label, dotColor, _) ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(1.dp, grey_border),
                                        color = whiteBg
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 5.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                            Spacer(Modifier.width(5.dp))
                                            Text(
                                                label,
                                                fontSize = 11.sp,
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

                // Calendar Grid
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding, vertical = 8.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        color = whiteBg
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFAFAFA))
                                    .padding(vertical = 8.dp)
                            ) {
                                daysOfWeek.forEach { day ->
                                    Text(
                                        text = day,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = headerGrey
                                    )
                                }
                            }
                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                            val rows = calendarDays.chunked(7)
                            rows.forEach { week ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    week.forEach { cell ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(76.dp)
                                                .background(
                                                    when {
                                                        cell.isHighlighted -> Color(0xFFEDE9FE)
                                                        cell.isWeekend && cell.isCurrentMonth -> Color(0xFFF8FAFC)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .border(0.5.dp, Color(0xFFE2E8F0))
                                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                // Top Row: Status Dot and Day Number
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (cell.status != DayStatus.NONE) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(7.dp)
                                                                .clip(CircleShape)
                                                                .background(cell.status.color)
                                                        )
                                                    } else {
                                                        Spacer(modifier = Modifier.size(7.dp))
                                                    }

                                                    Text(
                                                        text = cell.dayNumber,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (cell.isCurrentMonth) title_color else Color(0xFFCBD5E1),
                                                        textAlign = TextAlign.End
                                                    )
                                                }

                                                // Center Content
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .weight(1f),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val compactTimeStyle = TextStyle(
                                                        fontSize = 9.sp,
                                                        lineHeight = 9.5.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFF475569),
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
                                                                    fontWeight = FontWeight.SemiBold,
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

            // Employee Info Card
            item {
                val currentMember = remember(members, selectedEmployeeId) {
                    members.find { it._id == selectedEmployeeId }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, grey_border),
                    color = whiteBg
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val initials = remember(currentMember) {
                                "${currentMember?.firstName?.firstOrNull()?.uppercaseChar() ?: ' '}${currentMember?.lastName?.firstOrNull()?.uppercaseChar() ?: ' '}".trim().ifBlank { "?" }
                            }

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDE9FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    initials,
                                    color = Primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${currentMember?.firstName.orEmpty()} ${currentMember?.lastName.orEmpty()}".trim().ifBlank { selectedEmployeeName },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                                Text(
                                    currentMember?.departmentId?.name ?: "Department",
                                    fontSize = tokens.caption,
                                    color = headerGrey
                                )
                            }
                            StatusBadge(
                                text = currentMember?.memberId ?: "EMP",
                                bgColor = Color(0xFFEDE9FE),
                                textColor = Primary,
                                showDot = false
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = sectionBorder, thickness = 1.dp)
                        Spacer(Modifier.height(10.dp))

                        val shiftInfo = apiAttendanceList.firstOrNull()?.shiftId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Primary Shift", fontSize = tokens.caption, color = headerGrey)
                            Text(
                                "${shiftInfo?.name ?: "General"} (${shiftInfo?.startTime ?: "09:00"}–${shiftInfo?.endTime ?: "18:00"})",
                                fontSize = tokens.caption,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Monthly Summary Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, grey_border),
                    color = whiteBg
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Monthly Summary",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total Working Days",
                                fontSize = tokens.bodySmall,
                                color = TextPrimary
                            )
                            Text(
                                workingDays.toString(),
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = grey_border, thickness = 1.dp)
                        Spacer(Modifier.height(8.dp))

                        val summaryStats = listOf(
                            Triple("Present", present.toString(), Color(0xFF10B981)),
                            Triple("Absent", absent.toString(), Color(0xFFEF4444)),
                            Triple("Leave", leave.toString(), Color(0xFF3B82F6)),
                            Triple("Late Count", late.toString(), Color(0xFFF59E0B))
                        )

                        summaryStats.forEach { (label, count, dotColor) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(dotColor)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(label, fontSize = tokens.bodySmall, color = headerGrey)
                                }
                                Text(
                                    count,
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Presence Rate Progress Bar
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF8FAFC)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Presence Rate", fontSize = 11.sp, color = headerGrey)
                                    Text(
                                        String.format(Locale.ENGLISH, "%.1f%%", presenceRate * 100),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { presenceRate },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    color = Primary,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }
            }

            // Calendar Legend
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = tokens.screenPadding, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, grey_border),
                    color = whiteBg
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "CALENDAR LEGEND",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = headerGrey
                        )

                        listOf(
                            "On Time" to Color(0xFF10B981),
                            "Late Arrival" to Color(0xFFF59E0B),
                            "Absent" to Color(0xFFEF4444),
                            "Authorized Leave" to Color(0xFF3B82F6)
                        ).forEach { (legendText, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(7.dp).clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(legendText, fontSize = tokens.caption, color = headerGrey)
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
    horizontalPadding: androidx.compose.ui.unit.Dp
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        color = whiteBg
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Date Badge
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.dayOfMonth.toString(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = item.dayOfWeek,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Middle: Status & Punch Timings
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (item.isWeekOff) Icons.Default.NightsStay else Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = item.statusColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = item.statusText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = item.statusColor
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.timeRangeText,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Right: Worked Hours
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.workedTimeText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "worked",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Divider between rows
                if (index < items.lastIndex) {
                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }
        }
    }
}