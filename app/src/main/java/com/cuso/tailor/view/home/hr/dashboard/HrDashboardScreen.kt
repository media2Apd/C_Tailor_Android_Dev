@file:Suppress("unused", "AssignedValueIsNeverRead", "unusedVariable", "VariableNeverRead")

package com.cuso.tailor.view.home.hr.dashboard

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.CheckInOutTodayData
import com.cuso.tailor.model.hr.MyProfileData
import com.cuso.tailor.model.hr.OrgDashboardData
import com.cuso.tailor.model.hr.ShiftDetailData
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.DataCardProgressBar
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrDashboardViewModel
import com.cuso.tailor.viewmodel.HrViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HrDashboardScreen(
    onClose: () -> Unit,
    onNavigateToLeave: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToShift: () -> Unit,
    onNavigateToCreateFeed: () -> Unit = {},
    viewModel: HrDashboardViewModel = hiltViewModel(),
    hrViewModel: HrViewModel = hiltViewModel() // Injected HrViewModel for shift detail API
) {
    val tokens = LocalAppTokens.current

    var selectedSpace by remember { mutableStateOf("My Space") }
    val spaces = listOf("My Space", "Team", "Org")

    var selectedSection by remember { mutableStateOf("Overview") }
    val sections = listOf("Overview", "Dashboard")

    var selectedDashboardTab by remember { mutableStateOf("Profile") }
    val dashboardTabs = listOf(
        "Profile", "Feed","Approvals", "Leave", "Attendance", "Shift", "Files", "Time Logs", "Career History"
    )

    val todayAttendance by viewModel.todayAttendance.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()
    val teamData by viewModel.teamData.collectAsState()
    val orgData by viewModel.orgData.collectAsState()
    val shiftDetail by hrViewModel.shiftDetail.collectAsState()
    val attendanceHistory by viewModel.attendanceHistoryMonthToDate.collectAsState()

    // Sub-tab API States collected from ViewModel
    val feedList by viewModel.feedList.collectAsState()
    val legalFiles by viewModel.legalFiles.collectAsState()
    val designationDetail by viewModel.designationHistory.collectAsState()

    // Automatically trigger Shift and Career Designation APIs when respective tab is chosen
    LaunchedEffect(selectedDashboardTab, myProfile) {
        if (selectedDashboardTab == "Shift") {
            val shiftId = myProfile?.shiftId
            if (!shiftId.isNullOrBlank()) {
                hrViewModel.fetchShiftDetail(shiftId)
            }
        } else if (selectedDashboardTab == "Career History") {
            val desId = myProfile?.designationId?._id
            if (!desId.isNullOrBlank()) {
                viewModel.fetchCareerDesignationHistory(desId)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "HR Dashboard",
                onClose = onClose
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Space Selector Row (My Space / Team / Org)
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    spaces.forEach { space ->
                        val isSelected = selectedSpace == space
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(tokens.buttonHeight * 0.85f)
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                .background(if (isSelected) Primary else whiteBg)
                                .clickable { selectedSpace = space },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = space,
                                fontSize = tokens.bodySmall,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) whiteBg else TextSecondary
                            )
                        }
                    }
                }
            }

            // Render Content by Space Selection
            when (selectedSpace) {
                "My Space" -> {
                    MySpaceContent(
                        todayAttendance = todayAttendance,
                        myProfile = myProfile,
                        shiftDetail = shiftDetail,
                        orgData = orgData,
                        attendanceHistory = attendanceHistory,
                        feedList = feedList,
                        legalFiles = legalFiles,
                        designationDetail = designationDetail,
                        onFetchFeed = { viewModel.fetchEmployeeFeed() },
                        onPunch = { viewModel.performPunchAction() },
                        onNavigateToLeave = onNavigateToLeave,
                        onNavigateToAttendance = onNavigateToAttendance,
                        onNavigateToShift = onNavigateToShift,
                        onNavigateToCreateFeed = onNavigateToCreateFeed
                    )
                }
                "Team" -> {
                    TeamSpaceContent(teamData = teamData)
                }
                "Org" -> {
                    OrgSpaceContent(orgData = orgData)
                }
            }
        }
    }
}

// =========================================================================
// SUB-TAB 1: PROFILE CONTENT (/api/members/me)
// =========================================================================

@Composable
private fun ProfileSubTabContent(profile: MyProfileData?) {
    val tokens = LocalAppTokens.current
    val p = profile

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Identity Banner Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${p?.firstName?.take(1) ?: "N"}${p?.lastName?.take(1) ?: "S"}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = whiteBg
                            )
                        }
                        Column {
                            Text(
                                text = "${p?.firstName ?: "—"} ${p?.lastName ?: ""}",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                            Text(
                                text = "• ${p?.memberId ?: "—"}",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                            Text(
                                text = p?.departmentId?.name ?: "Management",
                                fontSize = tokens.label,
                                color = headerGrey
                            )
                        }
                    }

                    StatusBadge(
                        text = (p?.status ?: "Active").replaceFirstChar { it.uppercase() },
                        bgColor = greenBg,
                        textColor = greentext
                    )
                }

                // Profile Completion Progress Bar
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = light_grey,
                    border = BorderStroke(0.5.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Profile Completion", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                            Text("10%", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(sectionBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.10f)
                                    .background(Primary)
                            )
                        }
                    }
                }
            }
        }

        // Personal Information Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PERSONAL INFORMATION", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                    StatusBadge(text = "Verified Profile", bgColor = light_grey, textColor = TextSecondary)
                }

                InfoFieldGrid("First Name", p?.firstName ?: "—", "Last Name", p?.lastName ?: "—")
                InfoFieldGrid("Date of Birth", p?.dob?.take(10) ?: "—", "Gender", p?.gender ?: "—")
                InfoFieldGrid("Marital Status", p?.maritalStatus ?: "—", "Blood Group", p?.bloodGroup ?: "—")
                InfoFieldGrid("Nationality", p?.nationality ?: "—", "Language", p?.language ?: "—")
            }
        }

        // Employment Details Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("EMPLOYMENT DETAILS", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                    StatusBadge(text = "HR Controlled / Read-Only", bgColor = light_grey, textColor = TextSecondary)
                }

                InfoFieldGrid("Employee ID", p?.memberId ?: "—", "Designation", p?.designationId?.name ?: "—")
                InfoFieldGrid("Department", p?.departmentId?.name ?: "Management", "Employment Type", (p?.employmentType ?: "Full Time").replace("-", " "))
                InfoFieldGrid("Work Location / Branch", p?.branchId?.name ?: "Sample - Main Branch", "Joining Date", p?.doj?.take(10) ?: "—")
            }
        }

        // Contact Information Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CONTACT INFORMATION", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                    StatusBadge(text = "Self-Service Editable", bgColor = primary_light, textColor = Primary)
                }

                Text("Work Email (Read-Only)", fontSize = tokens.label, color = headerGrey)
                Text(p?.userId?.email ?: p?.workEmail ?: "—", fontSize = tokens.bodySmall, color = TextPrimary)

                Text("Primary Mobile", fontSize = tokens.label, color = headerGrey)
                Text(p?.userId?.mobile ?: p?.mobile ?: "—", fontSize = tokens.bodySmall, color = TextPrimary)
            }
        }
    }
}

// =========================================================================
// SUB-TAB 2: ATTENDANCE CONTENT (/api/hr/attendance/by-employee)
// =========================================================================

@Composable
private fun AttendanceSubTabContent(
    todayAttendance: CheckInOutTodayData?,
    history: List<CheckInOutTodayData>
) {
    val tokens = LocalAppTokens.current
    val t = todayAttendance

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Today Status Box
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Attendance", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                    StatusBadge(
                        text = if (t?.checkedIn == true) "Checked In" else "Not Checked In",
                        bgColor = if (t?.checkedIn == true) greenBg else yellowBg,
                        textColor = if (t?.checkedIn == true) greentext else yellowText
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(modifier = Modifier.weight(1f), title = "Check-in", value = formatIsoTimeToAmPm(t?.firstIn), valueColor = TextPrimary)
                    MetricBox(modifier = Modifier.weight(1f), title = "Check-out", value = formatIsoTimeToAmPm(t?.lastOut), valueColor = TextPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(modifier = Modifier.weight(1f), title = "Working Time", value = "${t?.totalWorkingMinutes ?: 0}m", valueColor = Primary)
                    MetricBox(modifier = Modifier.weight(1f), title = "Break Time", value = "${t?.totalBreakMinutes ?: 0}m Elapsed", valueColor = yellowText)
                }
            }
        }

        // Attendance History Table (Month-to-Date)
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Attendance History (Month-to-Date)", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = whiteBg,
                    border = BorderStroke(0.8.dp, sectionBorder)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(light_grey)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("DATE & DAY", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = headerGrey)
                            Text("SHIFT", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = headerGrey)
                            Text("STATUS", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = headerGrey)
                        }

                        if (history.isEmpty()) {
                            Text(
                                text = "No records logged yet for this month",
                                fontSize = tokens.caption,
                                color = mutedText,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            history.forEachIndexed { idx, rec ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(rec.date?.take(10) ?: "—", fontSize = tokens.caption, color = TextPrimary)
                                    Text("General Shift", fontSize = tokens.caption, color = TextSecondary)
                                    StatusBadge(
                                        text = rec.status.uppercase(),
                                        bgColor = if (rec.status.equals("present", true)) greenBg else redBg,
                                        textColor = if (rec.status.equals("present", true)) greentext else redText
                                    )
                                }
                                if (idx < history.lastIndex) {
                                    HorizontalDivider(color = grey_border, thickness = 0.8.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// SUB-TAB 3: SHIFT CONTENT (MIRRORING SHIFTTAB.JSX & SCREENSHOTS 5, 6, 7)
// =========================================================================

data class ShiftScheduleDay(
    val dayKey: String,
    val dayLabel: String,
    val dateLabel: String,
    val isWorking: Boolean,
    val isToday: Boolean,
    val isPast: Boolean,
    val hours: String
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ShiftSubTabContent(
    shift: ShiftDetailData?,
    profile: MyProfileData? = null
) {
    val tokens = LocalAppTokens.current
    val s = shift
    val p = profile

    // Shift properties with fallbacks
    val shiftName = s?.name ?: "General Shift"
    val shiftCode = s?.shiftId ?: "SHIFT-001"
    val startTime = s?.startTime ?: "09:00 AM"
    val endTime = s?.endTime ?: "06:00 PM"
    val timingRange = "$startTime – $endTime"
    val breakDuration = s?.breakDuration ?: 0
    val breakLabel = if (breakDuration > 0) "${breakDuration} mins" else "No break"
    val branchName = p?.branchId?.name ?: "dummy branch"
    val effectiveFrom = p?.doj?.take(10) ?: "01 Nov 2026"
    val isActive = s?.status ?: true
    val graceMinutes = 10
    val fullDayMinutes = 480 // 8 hours

    // Next upcoming shift calculation
    val today = java.time.LocalDate.now()
    val tomorrow = today.plusDays(1)
    val nextShiftDateLabel = tomorrow.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.US))

    // Interactive 7-Day Week Offset State (0 = Current Week, -1 = Prev, +1 = Next)
    var weekOffset by remember { mutableIntStateOf(0) }

    val weekSchedule = remember(weekOffset, today) {
        val targetDate = today.plusWeeks(weekOffset.toLong())
        val monday = targetDate.minusDays(((targetDate.dayOfWeek.value - 1) % 7).toLong())
        (0..6).map { offset ->
            val date = monday.plusDays(offset.toLong())
            val dayOfWeek = date.dayOfWeek.value // 1 = Monday, 7 = Sunday
            val isSunday = dayOfWeek == 7
            val isCurrentToday = date.isEqual(today)
            val isPastDay = date.isBefore(today)

            ShiftScheduleDay(
                dayKey = date.dayOfWeek.name.take(3),
                dayLabel = date.dayOfWeek.name.take(3),
                dateLabel = date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.US)),
                isWorking = !isSunday,
                isToday = isCurrentToday,
                isPast = isPastDay,
                hours = if (!isSunday) "9h" else "0h"
            )
        }
    }

    val weekRangeLabel = remember(weekOffset, today) {
        val targetDate = today.plusWeeks(weekOffset.toLong())
        val monday = targetDate.minusDays(((targetDate.dayOfWeek.value - 1) % 7).toLong())
        val sunday = monday.plusDays(6)
        val mLabel = monday.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US))
        val sLabel = sunday.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US))
        "$mLabel – $sLabel" + if (weekOffset == 0) " (Current)" else ""
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Header Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Shift",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    StatusBadge(
                        text = if (isActive) "● Assigned Shift Active" else "● Shift Inactive",
                        bgColor = if (isActive) greenBg else light_grey,
                        textColor = if (isActive) greentext else TextSecondary
                    )
                }
                Text(
                    text = "View your assigned shift, weekly schedule and shift-change requests.",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )
            }
        }

        // 2. Current Assigned Shift Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = "Current Assigned Shift",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                            Text(
                                text = "Auto-created default shift",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                        }
                    }

                    StatusBadge(
                        text = "Default Policy",
                        bgColor = primary_light,
                        textColor = Primary
                    )
                }

                HorizontalDivider(color = grey_border, thickness = 0.8.dp)

                InfoFieldGrid("Shift Name", shiftName, "Shift Timing", timingRange)
                InfoFieldGrid("Shift Code", shiftCode, "Scheduled", "(9h scheduled)")
                InfoFieldGrid("Break Allocation", breakLabel, "Work Location", branchName)
                InfoFieldGrid("Effective From", effectiveFrom, "Weekly Off", "Sunday")

                // Shift Grace Callout Container
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = light_grey,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(15.dp))
                            Text(
                                text = "Grace period: ${graceMinutes} mins • Min full-day: ${fullDayMinutes / 60}h 00m",
                                fontSize = tokens.label,
                                color = TextPrimary
                            )
                        }
                        StatusBadge(
                            text = if (isActive) "Status: Active" else "Status: Inactive",
                            bgColor = if (isActive) greenBg else light_grey,
                            textColor = if (isActive) greentext else TextSecondary
                        )
                    }
                }
            }
        }

        // 3. Next Upcoming Shift Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(yellowBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = yellowText, modifier = Modifier.size(16.dp))
                        }
                        Column {
                            Text(
                                text = "Next Upcoming Shift",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                            Text(
                                text = "Scheduled sequence",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                        }
                    }
                    StatusBadge(text = "Tomorrow", bgColor = light_grey, textColor = TextSecondary)
                }

                Text(
                    text = nextShiftDateLabel,
                    fontSize = tokens.caption,
                    color = TextSecondary
                )
                Text(
                    text = timingRange,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
                Text(
                    text = "$shiftName • $branchName",
                    fontSize = tokens.caption,
                    color = headerGrey
                )

                // Expected Check-in Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = light_grey,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Expected check-in: before 09:10 AM",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                        Text(
                            text = "(Grace limit included)",
                            fontSize = tokens.label,
                            color = headerGrey
                        )
                    }
                }

                Text(
                    text = "View Weekly Schedule ⌵",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // 4. MY WEEKLY SCHEDULE Card (with interactive date switcher)
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "MY WEEKLY SCHEDULE",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Text(
                    text = "Continuous 7-day schedule distribution",
                    fontSize = tokens.label,
                    color = headerGrey
                )

                // Date Navigator Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "< Prev",
                        fontSize = tokens.caption,
                        color = TextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, sectionBorder, RoundedCornerShape(6.dp))
                            .clickable { weekOffset -= 1 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Text(
                        text = weekRangeLabel,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = "Next >",
                        fontSize = tokens.caption,
                        color = TextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, sectionBorder, RoundedCornerShape(6.dp))
                            .clickable { weekOffset += 1 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 7-Day Schedule Items List
                weekSchedule.forEach { day ->
                    val isSunday = !day.isWorking
                    val statusText = when {
                        isSunday -> "Weekly Off"
                        day.isPast -> "Completed"
                        else -> "Scheduled"
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSunday) light_grey.copy(alpha = 0.5f) else whiteBg,
                        border = BorderStroke(
                            width = if (day.isToday) 1.5.dp else 1.dp,
                            color = if (day.isToday) Primary else sectionBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = day.dayLabel,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (day.isToday) Primary else title_color
                                )
                                Text(
                                    text = day.dateLabel,
                                    fontSize = tokens.label,
                                    color = headerGrey
                                )
                            }

                            Text(
                                text = if (isSunday) "Weekly Off" else shiftName,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSunday) headerGrey else title_color
                            )

                            Text(
                                text = if (isSunday) "Rest Day" else timingRange,
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )

                            Text(
                                text = if (isSunday) "Holiday / Weekend" else "No scheduled break",
                                fontSize = tokens.label,
                                color = headerGrey
                            )

                            Spacer(Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(
                                    text = statusText,
                                    bgColor = when {
                                        isSunday -> light_grey
                                        day.isPast -> light_grey
                                        else -> greenBg
                                    },
                                    textColor = when {
                                        isSunday -> TextSecondary
                                        day.isPast -> TextSecondary
                                        else -> greentext
                                    }
                                )
                                Text(
                                    text = day.hours,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = headerGrey
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. MY SHIFT CHANGE REQUESTS Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MY SHIFT CHANGE REQUESTS",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                        StatusBadge(text = "0 Total", bgColor = primary_light, textColor = Primary)
                    }
                    Text(
                        text = "Recent 30 days",
                        fontSize = tokens.label,
                        color = headerGrey
                    )
                }

                Text(
                    text = "Submitted requests for shift adjustments and their current approval status.",
                    fontSize = tokens.caption,
                    color = headerGrey
                )

                // Dashed placeholder box matching screenshot
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                        .background(light_grey.copy(alpha = 0.3f))
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No shift change requests found.",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }
        }

        // 6. SHIFT POLICY RULES Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(primary_light),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = "SHIFT POLICY RULES",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                }

                ShiftPolicyRuleItem("Advance Notice", "Requests must be submitted at least 48 hours prior to the requested shift date.")
                ShiftPolicyRuleItem("Allowed Swaps", "Maximum of 3 shift change requests permitted per calendar month.")
                ShiftPolicyRuleItem("Break Timings", "Mandatory break interval is included for full working days.")
                ShiftPolicyRuleItem("Review Authority", "Shift adjustments are reviewed and sanctioned by the reporting manager / HR.")
            }
        }
    }
}

@Composable
private fun ShiftPolicyRuleItem(title: String, description: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(Primary)
        )
        Column {
            Text(
                text = "$title:",
                fontSize = tokens.caption,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )
            Text(
                text = description,
                fontSize = tokens.label,
                color = TextSecondary
            )
        }
    }
}

// =========================================================================
// SUB-TABS 4-8: APPROVALS, LEAVE, FILES, TIME LOGS, CAREER HISTORY
// =========================================================================

@Composable
private fun ApprovalsSubTabContent(
    pendingCount: Int = 1,
    history: List<CheckInOutTodayData> = emptyList()
) {
    val tokens = LocalAppTokens.current
    Surface(shape = RoundedCornerShape(tokens.cardCornerRadius), color = whiteBg, border = BorderStroke(1.dp, sectionBorder)) {
        Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("My Approvals", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
            Text("No pending requests awaiting approval.", fontSize = tokens.caption, color = mutedText)
        }
    }
}

@Composable
private fun LeaveSubTabContent(
    orgData: OrgDashboardData? = null,
    onNavigateToLeave: () -> Unit
) {
    val tokens = LocalAppTokens.current
    Surface(shape = RoundedCornerShape(tokens.cardCornerRadius), color = whiteBg, border = BorderStroke(1.dp, sectionBorder)) {
        Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("My Leave", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
            Text("Review entitlement and submit leave applications.", fontSize = tokens.caption, color = TextSecondary)
            Button(onClick = onNavigateToLeave, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text("Apply Leave", color = whiteBg, fontSize = tokens.bodySmall)
            }
        }
    }
}

// =========================================================================
// SUB-TAB 7: FILES CONTENT (MATCHING FILESTAB.JSX & SCREENSHOTS 8, 9)
// =========================================================================

data class UiLegalDocument(
    val id: String,
    val name: String,
    val ext: String,
    val url: String,
    val refNo: String,
    val category: String,
    val uploadedAt: String,
    val expiryDate: String,
    val status: String
)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun FilesSubTabContent(
    legalRecords: List<com.cuso.tailor.model.hr.LegalEmployeeData> = emptyList()
) {
    val tokens = LocalAppTokens.current

    // Helper date formatter


    fun formatDisplayDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "—"
        return try {
            val instant = Instant.parse(dateStr)
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (_: Exception) {
            dateStr.take(10)
        }
    }

    // Helper file extension extractor
    fun getExtension(name: String): String {
        val ext = name.substringAfterLast('.', "FILE").uppercase()
        return if (ext.length > 4) ext.take(4) else ext
    }

    // Flatten document groups from all legal records
    val allDocuments = remember(legalRecords) {
        legalRecords.flatMap { rec ->
            val refNo = rec.legalRecordNumber.ifBlank { "" }
            val expiry = formatDisplayDate(rec.endDate)

            val contracts = rec.employmentContractDocuments.map { doc ->
                UiLegalDocument(
                    id = doc._id,
                    name = doc.documentName,
                    ext = getExtension(doc.documentName),
                    url = doc.documentUrl,
                    refNo = refNo,
                    category = "Employment Contract",
                    uploadedAt = formatDisplayDate(doc.uploadedAt),
                    expiryDate = expiry,
                    status = "Pending Review"
                )
            }
            val labours = rec.labourDocuments.map { doc ->
                UiLegalDocument(
                    id = doc._id,
                    name = doc.documentName,
                    ext = getExtension(doc.documentName),
                    url = doc.documentUrl,
                    refNo = refNo,
                    category = "Labour Document",
                    uploadedAt = formatDisplayDate(doc.uploadedAt),
                    expiryDate = expiry,
                    status = "Pending Review"
                )
            }
            val supportings = rec.supportingDocuments.map { doc ->
                UiLegalDocument(
                    id = doc._id,
                    name = doc.documentName,
                    ext = getExtension(doc.documentName),
                    url = doc.documentUrl,
                    refNo = refNo,
                    category = "Supporting Document",
                    uploadedAt = formatDisplayDate(doc.uploadedAt),
                    expiryDate = expiry,
                    status = "Pending Review"
                )
            }
            contracts + labours + supportings
        }
    }

    // Filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryTab by remember { mutableStateOf("All Documents") }

    var selectedStatus by remember { mutableStateOf("All Status") }
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }

    var selectedExpiry by remember { mutableStateOf("All") }
    var isExpiryDropdownExpanded by remember { mutableStateOf(false) }

    // Counts per category
    val contractCount = remember(allDocuments) { allDocuments.count { it.category == "Employment Contract" } }
    val labourCount = remember(allDocuments) { allDocuments.count { it.category == "Labour Document" } }
    val supportingCount = remember(allDocuments) { allDocuments.count { it.category == "Supporting Document" } }

    val categoryTabs = listOf(
        "All Documents (${allDocuments.size})",
        "Employment Contract ($contractCount)",
        "Labour Document ($labourCount)",
        "Supporting Document ($supportingCount)"
    )

    // Filtered documents list
    val filteredDocuments = remember(allDocuments, searchQuery, selectedCategoryTab, selectedStatus, selectedExpiry) {
        allDocuments.filter { doc ->
            val matchesCategory = when {
                selectedCategoryTab.startsWith("Employment Contract") -> doc.category == "Employment Contract"
                selectedCategoryTab.startsWith("Labour Document") -> doc.category == "Labour Document"
                selectedCategoryTab.startsWith("Supporting Document") -> doc.category == "Supporting Document"
                else -> true
            }

            val matchesSearch = searchQuery.isBlank() ||
                    doc.name.contains(searchQuery, ignoreCase = true) ||
                    doc.refNo.contains(searchQuery, ignoreCase = true)

            val matchesStatus = when (selectedStatus) {
                "Verified" -> doc.status.equals("Verified", ignoreCase = true)
                "Pending Review" -> doc.status.equals("Pending Review", ignoreCase = true)
                "Rejected" -> doc.status.equals("Rejected", ignoreCase = true)
                "Expired" -> doc.status.equals("Expired", ignoreCase = true)
                else -> true
            }

            val matchesExpiry = when (selectedExpiry) {
                "No Expiry" -> doc.expiryDate == "—"
                "Has Expiry" -> doc.expiryDate != "—"
                else -> true
            }

            matchesCategory && matchesSearch && matchesStatus && matchesExpiry
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Top Header Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "My Files",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Text(
                    text = "View your employee documents, upload requested files and track verification status.",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )
            }
        }

        // 2. Four KPI Summary Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FileStatCard(
                modifier = Modifier.weight(1f),
                label = "Total Documents",
                count = "${allDocuments.size}",
                note = "Across 3 categories",
                icon = Icons.Default.Description,
                iconBg = light_grey,
                iconTint = headerGrey,
                valueColor = title_color
            )
            FileStatCard(
                modifier = Modifier.weight(1f),
                label = "Verified",
                count = "0",
                note = "Approved by HR",
                icon = Icons.Default.CheckCircle,
                iconBg = greenBg,
                iconTint = greentext,
                valueColor = greentext
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FileStatCard(
                modifier = Modifier.weight(1f),
                label = "Pending Review",
                count = "${allDocuments.size}",
                note = "Awaiting verification",
                icon = Icons.Default.Schedule,
                iconBg = yellowBg,
                iconTint = yellowText,
                valueColor = yellowText
            )
            FileStatCard(
                modifier = Modifier.weight(1f),
                label = "Action Required",
                count = "0",
                note = "Requires re-upload",
                icon = Icons.Default.Warning,
                iconBg = redBg,
                iconTint = redText,
                valueColor = redText
            )
        }

        // 3. Category Tabs + Search + Dropdown Filters Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Pills (Horizontal Scrollable)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoryTabs) { tab ->
                        val isSelected = selectedCategoryTab == tab || (selectedCategoryTab == "All Documents" && tab.startsWith("All Documents"))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Primary else light_grey)
                                .clickable { selectedCategoryTab = tab }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = tab,
                                fontSize = tokens.caption,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) whiteBg else TextSecondary
                            )
                        }
                    }
                }

                HorizontalDivider(color = grey_border, thickness = 0.8.dp)

                // Search Input
                FormTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search document name or ID...",
                    containerColor = whiteBg
                )

                // Dropdown Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Verification:", fontSize = tokens.label, color = headerGrey)
                        Spacer(Modifier.height(4.dp))
                        FormDropdown(
                            value = selectedStatus,
                            expanded = isStatusDropdownExpanded,
                            onExpandChange = { isStatusDropdownExpanded = it },
                            options = listOf("All Status", "Verified", "Pending Review", "Rejected", "Expired"),
                            onOptionSelected = {
                                selectedStatus = it
                                isStatusDropdownExpanded = false
                            }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Expiry:", fontSize = tokens.label, color = headerGrey)
                        Spacer(Modifier.height(4.dp))
                        FormDropdown(
                            value = selectedExpiry,
                            expanded = isExpiryDropdownExpanded,
                            onExpandChange = { isExpiryDropdownExpanded = it },
                            options = listOf("All", "No Expiry", "Has Expiry", "Expiring Soon", "Expired"),
                            onOptionSelected = {
                                selectedExpiry = it
                                isExpiryDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // 4. Documents Table Card (Horizontal Scroll View matching Screenshot 9)
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column {
                val tableScrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(tableScrollState)
                ) {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .width(620.dp)
                            .background(light_grey)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DOCUMENT",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.SemiBold,
                            color = headerGrey,
                            modifier = Modifier.width(240.dp)
                        )
                        Text(
                            text = "CATEGORY",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.SemiBold,
                            color = headerGrey,
                            modifier = Modifier.width(150.dp)
                        )
                        Text(
                            text = "UPLOADED ON",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.SemiBold,
                            color = headerGrey,
                            modifier = Modifier.width(110.dp)
                        )
                        Text(
                            text = "EXPIRY DATE",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.SemiBold,
                            color = headerGrey,
                            modifier = Modifier.width(110.dp)
                        )
                    }

                    HorizontalDivider(color = sectionBorder, thickness = 0.8.dp)

                    // Table Data Rows
                    if (filteredDocuments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .width(620.dp)
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No documents match your filters.",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }
                    } else {
                        filteredDocuments.forEachIndexed { index, doc ->
                            Row(
                                modifier = Modifier
                                    .width(620.dp)
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Column 1: Document File + Extension Badge
                                Row(
                                    modifier = Modifier.width(240.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(greenBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = doc.ext,
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Bold,
                                            color = greentext
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = doc.name,
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.SemiBold,
                                            color = title_color,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = doc.refNo,
                                            fontSize = tokens.label,
                                            color = headerGrey
                                        )
                                    }
                                }

                                // Column 2: Category Badge
                                Box(modifier = Modifier.width(150.dp)) {
                                    val (catBg, catText) = when (doc.category) {
                                        "Employment Contract" -> Pair(primary_light, Primary)
                                        "Labour Document" -> Pair(Color(0xFFF3E8FF), Color(0xFF7C3AED))
                                        else -> Pair(light_grey, TextSecondary)
                                    }
                                    StatusBadge(
                                        text = doc.category,
                                        bgColor = catBg,
                                        textColor = catText
                                    )
                                }

                                // Column 3: Uploaded On
                                Text(
                                    text = doc.uploadedAt,
                                    fontSize = tokens.caption,
                                    color = TextPrimary,
                                    modifier = Modifier.width(110.dp)
                                )

                                // Column 4: Expiry Date
                                Text(
                                    text = doc.expiryDate,
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color,
                                    modifier = Modifier.width(110.dp)
                                )
                            }

                            if (index < filteredDocuments.lastIndex) {
                                HorizontalDivider(color = grey_border, thickness = 0.8.dp)
                            }
                        }
                    }
                }

                // Table Pagination Footer (Matching Screenshot 9)
                HorizontalDivider(color = sectionBorder, thickness = 0.8.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Showing ${filteredDocuments.size} of ${allDocuments.size} documents",
                        fontSize = tokens.label,
                        color = headerGrey
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = whiteBg,
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            Text(
                                text = "Previous",
                                fontSize = tokens.label,
                                color = mutedText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Medium,
                                color = whiteBg
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = whiteBg,
                            border = BorderStroke(1.dp, sectionBorder)
                        ) {
                            Text(
                                text = "Next",
                                fontSize = tokens.label,
                                color = mutedText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileStatCard(
    modifier: Modifier = Modifier,
    label: String,
    count: String,
    note: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    valueColor: Color
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = tokens.label,
                    color = headerGrey
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
                }
            }
            Text(
                text = count,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
            Text(
                text = note,
                fontSize = tokens.label,
                color = headerGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Reusable 2-column key-value grid for profile & shift details
@Composable
private fun InfoFieldGrid(label1: String, val1: String, label2: String, val2: String) {
    val tokens = LocalAppTokens.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label1, fontSize = tokens.label, color = headerGrey)
            Text(val1, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label2, fontSize = tokens.label, color = headerGrey)
            Text(val2, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
        }
    }
}

// =========================================================================
// ORGANIZATION (ORG) DASHBOARD VIEW - MATCHING MOBILE SCREENSHOT DESIGN
// =========================================================================

@Composable
private fun OrgSpaceContent(orgData: OrgDashboardData?) {
    val tokens = LocalAppTokens.current
    val overview = orgData?.overview

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top 6 KPI Stat Tiles (2 Columns x 3 Rows)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "Total Employees",
                value = "${overview?.totalEmployees ?: 0}",
                icon = Icons.Default.PeopleOutline,
                iconTint = Primary,
                iconBg = primary_light
            )
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "Active Employees",
                value = "${overview?.activeEmployees ?: 0}",
                icon = Icons.Default.Shield,
                iconTint = greentext,
                iconBg = greenBg
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "Departments",
                value = "${overview?.departments ?: 0}",
                icon = Icons.Default.Domain,
                iconTint = Primary,
                iconBg = primary_light
            )
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "New Hires (Month)",
                value = "${overview?.newHiresThisMonth ?: 0}",
                icon = Icons.Default.PersonAddAlt1,
                iconTint = yellowText,
                iconBg = yellowBg
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "Attrition Rate",
                value = "${overview?.estimatedMonthlyAttritionRate?.toInt() ?: 0}%",
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                iconTint = redText,
                iconBg = redBg
            )
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "Open Positions",
                value = "--",
                icon = Icons.Default.WorkOutline,
                iconTint = blueText,
                iconBg = primary_light
            )
        }

        // 2. Department-wise Headcount
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Department-wise Headcount",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = "View All Depts →",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }

                if (orgData?.departmentHeadcount.isNullOrEmpty()) {
                    Text(
                        text = "No department data available",
                        fontSize = tokens.caption,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    val maxHeadcount = (overview?.totalEmployees ?: 1).coerceAtLeast(1)

                    orgData.departmentHeadcount.forEach { dept ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = dept.departmentName,
                                fontSize = tokens.caption,
                                color = TextSecondary,
                                modifier = Modifier.width(90.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            val progress = (dept.employeeCount.toFloat() / maxHeadcount).coerceIn(0.05f, 1f)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(light_grey)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Primary)
                                )
                            }

                            Text(
                                text = "${dept.employeeCount}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color
                            )
                        }
                    }
                }
            }
        }

        // 3. Monthly Attrition & Hiring Trend
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Monthly Attrition & Hiring Trend",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(greentext, RoundedCornerShape(2.dp)))
                        Text("New Hires", fontSize = tokens.caption, color = TextSecondary)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(redText, RoundedCornerShape(2.dp)))
                        Text("Exits", fontSize = tokens.caption, color = TextSecondary)
                    }
                }

                // Trend status area
                val hasTrend = orgData?.monthlyHiringAttritionTrend?.months?.any { it.hires > 0 || it.exits > 0 } == true
                if (!hasTrend) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No trend data available",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        orgData.monthlyHiringAttritionTrend.months.takeLast(6).forEach { m ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${m.hires}", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = greentext)
                                Spacer(Modifier.height(4.dp))
                                Text(m.monthName.take(3), fontSize = tokens.caption, color = headerGrey)
                            }
                        }
                    }
                }
            }
        }

        // 4. Organisation Health Score
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Organisation Health Score",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Text(
                    text = "Global Health Index",
                    fontSize = tokens.caption,
                    color = headerGrey
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutScoreGauge(score = 0, size = 96.dp)
                }

                Text(
                    text = "Based on retention, performance parity, project completion times, and overall satisfaction.",
                    fontSize = tokens.caption,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 5. Gender Diversity Ratio
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Gender Diversity Ratio",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Donut Chart Canvas
                    Box(
                        modifier = Modifier.size(90.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = sectionBorder,
                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    // Diversity Breakdown Legend
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DiversityLegendRow(color = blueText, label = "Male: 0 (0%)")
                        DiversityLegendRow(color = redText, label = "Female: 0 (0%)")
                        DiversityLegendRow(color = yellowText, label = "Other: 0 (0%)")
                        val unspecifiedCount = overview?.totalEmployees ?: 0
                        DiversityLegendRow(color = mutedText, label = "Unspecified: $unspecifiedCount (100%)")
                    }
                }
            }
        }

        // 6. Top Performers This Quarter
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Top Performers This Quarter",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No performance data yet",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }
        }

        // 7. Upcoming Payroll Summary
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Payroll Summary",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = "Configure Payroll →",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }

                // Table Header Container
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = light_grey,
                    border = BorderStroke(0.5.dp, sectionBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("PAYROLL PERIOD", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = headerGrey)
                        Text("TOTAL COST / MO", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = headerGrey)
                        Text("EMPLOYEE COUNT", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = headerGrey)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data found",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }
        }

        // 8. Compliance & Policy Alerts
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Compliance & Policy Alerts",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No compliance alerts",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }
        }

        // 9. Budget Allocation by Department
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Budget Allocation by Department",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No budget data available",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }
        }

        // 10. Recent Organisation Announcements
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Recent Organisation Announcements",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active organization announcements",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }
        }

        // 11. Leave Balance (Org-wide)
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Leave Balance (Org-wide)",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = "View Details →",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = Primary
                    )
                }

                val leaveBal = orgData?.organizationLeaveBalance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TOTAL ENTITLEMENT", fontSize = tokens.label, color = headerGrey)
                        Text("${leaveBal?.totalEntitlement ?: 0} Days", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column {
                        Text("USED DAYS", fontSize = tokens.label, color = headerGrey)
                        Text("${leaveBal?.totalUsed ?: 0} Days", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = redText)
                    }
                    Column {
                        Text("AVAILABLE", fontSize = tokens.label, color = headerGrey)
                        Text("${leaveBal?.totalAvailable ?: 0} Days", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = greentext)
                    }
                }
            }
        }
    }
}

// =========================================================================
// REUSABLE SUB-COMPONENTS FOR ORG DASHBOARD
// =========================================================================

@Composable
private fun OrgKpiTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color
) {
    val tokens = LocalAppTokens.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = tokens.caption,
                    color = headerGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Text(
                text = value,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = blackTitle
            )
        }
    }
}

@Composable
private fun DonutScoreGauge(
    score: Int,
    size: androidx.compose.ui.unit.Dp
) {
    val tokens = LocalAppTokens.current

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.toPx() - strokeWidth

            // Track arc
            drawArc(
                color = sectionBorder,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth)
            )

            // Value arc
            if (score > 0) {
                drawArc(
                    color = Primary,
                    startAngle = -90f,
                    sweepAngle = (score / 100f) * 360f,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Text(
            text = "$score",
            fontSize = tokens.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = title_color
        )
    }
}

@Composable
private fun DiversityLegendRow(
    color: Color,
    label: String
) {
    val tokens = LocalAppTokens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = tokens.caption,
            color = TextSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────
// (Existing MySpaceContent & TeamSpaceContent remain below)
// ─────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MySpaceContent(
    todayAttendance: CheckInOutTodayData?,
    myProfile: MyProfileData?,
    shiftDetail: ShiftDetailData?,
    orgData: OrgDashboardData?,
    attendanceHistory: List<CheckInOutTodayData>,
    feedList: List<com.cuso.tailor.model.hr.AnnouncementItemData> = emptyList(),
    legalFiles: List<com.cuso.tailor.model.hr.LegalEmployeeData> = emptyList(),
    designationDetail: com.cuso.tailor.model.hr.DesignationDetailData? = null,
    onFetchFeed: () -> Unit = {},
    onPunch: () -> Unit,
    onNavigateToLeave: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToShift: () -> Unit,
    onNavigateToCreateFeed: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val t = todayAttendance
    val p = myProfile
    val isCheckedIn = t?.checkedIn == true

    // ── Dynamic Live Seconds Timer ──
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isCheckedIn, t?.firstIn) {
        if (isCheckedIn && !t.firstIn.isNullOrBlank()) {
            try {
                val checkInEpoch = Instant.parse(t.firstIn).epochSecond
                while (true) {
                    val currentEpoch = Instant.now().epochSecond
                    elapsedSeconds = (currentEpoch - checkInEpoch).coerceAtLeast(0L)
                    kotlinx.coroutines.delay(1000L)
                }
            } catch (_: Exception) {
                elapsedSeconds = 0L
            }
        } else {
            elapsedSeconds = (t?.totalWorkingMinutes?.toLong() ?: 0L) * 60L
        }
    }

    val timerText = remember(elapsedSeconds) {
        val hours = elapsedSeconds / 3600
        val mins = (elapsedSeconds % 3600) / 60
        val secs = elapsedSeconds % 60
        String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
    }

    val initials = remember(p) {
        "${p?.firstName?.take(1) ?: "N"}${p?.lastName?.take(1) ?: "S"}".uppercase()
    }
    val fullName = remember(p) {
        "${p?.firstName ?: ""} ${p?.lastName ?: ""}".trim()
    }
    val roleMemberId = remember(p) {
        "${p?.role ?: "owner"} • ${p?.memberId ?: ""}"
    }

    var selectedSection by remember { mutableStateOf("Overview") }
    val sections = listOf("Overview", "Dashboard")

    var selectedDashboardTab by remember { mutableStateOf("Profile") }
    val dashboardTabs = listOf(
        "Profile", "Feed", "Approvals", "Leave", "Attendance", "Shift", "Files", "Time Logs", "Career History"
    )

    // Trigger API fetch immediately when switching to Feed
    LaunchedEffect(selectedDashboardTab) {
        if (selectedDashboardTab == "Feed") {
            onFetchFeed()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // 1. Profile & Live Punch Timer Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.imageDp * 1.1f)
                        .clip(CircleShape)
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }

                Text(
                    text = fullName,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
                Text(
                    text = roleMemberId,
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )

                StatusBadge(
                    text = if (isCheckedIn) "Checked-in" else "Checked-out",
                    bgColor = if (isCheckedIn) greenBg else primary_light,
                    textColor = if (isCheckedIn) greentext else Primary
                )

                Text(
                    text = timerText,
                    fontSize = tokens.h1,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )

                val lastTimestamp = if (isCheckedIn) t.firstIn else t?.lastOut
                Text(
                    text = "${if (isCheckedIn) "Checked-in" else "Checked-out"} at ${formatIsoTimeToAmPm(lastTimestamp)}",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = formatIsoDateToPretty(lastTimestamp ?: t?.date),
                    fontSize = tokens.caption,
                    color = headerGrey
                )

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = onPunch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(
                        imageVector = if (isCheckedIn) Icons.AutoMirrored.Filled.Logout else Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isCheckedIn) "Check Out" else "Check In",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = whiteBg
                    )
                }
            }
        }

        // 2. TODAY'S SHIFT Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S SHIFT",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Bold,
                        color = headerGrey
                    )
                    StatusBadge(
                        text = shiftDetail?.name ?: "General Shift",
                        bgColor = primary_light,
                        textColor = Primary
                    )
                }

                val shiftStart = shiftDetail?.startTime ?: "09:00 AM"
                val shiftEnd = shiftDetail?.endTime ?: "06:00 PM"
                Text(
                    text = "$shiftStart – $shiftEnd",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
                Text(
                    text = shiftDetail?.shiftType ?: "Standard Shift",
                    fontSize = tokens.bodySmall,
                    color = TextSecondary
                )

                Text(
                    text = "View Shift Details →",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier
                        .clickable { onNavigateToShift() }
                        .padding(top = 4.dp)
                )
            }
        }

        // 3. TODAY'S SUMMARY Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "TODAY'S SUMMARY",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Bold,
                    color = headerGrey
                )

                val workingMins = t?.totalWorkingMinutes ?: 0
                val lateMins = t?.lateMinutes ?: 0

                SummaryRow(
                    label = "Effective Hours",
                    value = String.format(Locale.US, "%02dh %02dm", workingMins / 60, workingMins % 60)
                )
                SummaryRow(
                    label = "Late Time",
                    value = String.format(Locale.US, "%02dh %02dm", lateMins / 60, lateMins % 60)
                )
                SummaryRow(
                    label = "Last Check-in",
                    value = formatIsoTimeToAmPm(t?.firstIn)
                )
                SummaryRow(
                    label = "Check-out",
                    value = formatIsoTimeToAmPm(t?.lastOut)
                )
            }
        }

        // 4. MY REQUESTS Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MY REQUESTS",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Bold,
                        color = headerGrey
                    )
                    Text(
                        text = "View All",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier.clickable { onNavigateToLeave() }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No requests submitted",
                        fontSize = tokens.bodySmall,
                        color = mutedText
                    )
                }
            }
        }

        // 5. Overview | Dashboard Tabs (Placed below My Requests)
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                sections.forEach { sec ->
                    val isSelected = selectedSection == sec
                    Column(
                        modifier = Modifier
                            .clickable { selectedSection = sec }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = sec,
                            fontSize = tokens.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Primary else TextSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .height(2.5.dp)
                                    .width(36.dp)
                                    .background(Primary)
                            )
                        }
                    }
                }
            }
        }

        // 6. Section Content Render
        if (selectedSection == "Overview") {
            OverviewSectionContent(
                orgData = orgData,
                onNavigateToLeave = onNavigateToLeave,
                onNavigateToAttendance = onNavigateToAttendance,
                onNavigateToShift = onNavigateToShift
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(dashboardTabs) { tab ->
                    val isSelected = selectedDashboardTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.45f))
                            .background(if (isSelected) primary_light else whiteBg)
                            .border(
                                1.dp,
                                if (isSelected) Primary else sectionBorder,
                                RoundedCornerShape(tokens.cardCornerRadius * 0.45f)
                            )
                            .clickable {
                                selectedDashboardTab = tab
                                if (tab == "Feed") {
                                    onFetchFeed()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = tokens.caption,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Primary else TextSecondary
                        )
                    }
                }
            }

            when (selectedDashboardTab) {
                "Profile" -> ProfileSubTabContent(profile = myProfile)
                "Feed" -> FeedSubTabContent(
                    feedItems = feedList,
                    onCreateFeedClick = onNavigateToCreateFeed
                )
                "Approvals" -> ApprovalsSubTabContent(
                    pendingCount = orgData?.overview?.totalEmployees ?: 1,
                    history = attendanceHistory
                )
                "Leave" -> LeaveSubTabContent(
                    orgData = orgData,
                    onNavigateToLeave = onNavigateToLeave
                )
                "Attendance" -> AttendanceSubTabContent(
                    todayAttendance = t,
                    history = attendanceHistory
                )
                "Shift" -> ShiftSubTabContent(shift = shiftDetail, profile = myProfile)
                "Files" -> FilesSubTabContent(legalRecords = legalFiles)
                "Time Logs" -> TimeLogsSubTabContent(
                    todayAttendance = t,
                    monthLogs = attendanceHistory
                )
                "Career History" -> CareerHistorySubTabContent(
                    profile = myProfile,
                    designationDetail = designationDetail
                )
            }
        }
    }
}

// =========================================================================
// SUB-TAB 2: FEED CONTENT (MATCHING EMPLOYEEFEED.JSX & SCREENSHOT 1)
// =========================================================================

data class FeedCategoryOption(
    val label: String,
    val key: String
)
@SuppressLint("NewApi")
@Composable
private fun FeedSubTabContent(
    feedItems: List<com.cuso.tailor.model.hr.AnnouncementItemData>,
    onCreateFeedClick: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // State for Search and Categories
    var searchQuery by remember { mutableStateOf("") }
    var activeCategoryKey by remember { mutableStateOf("ALL") }

    val categories = listOf(
        FeedCategoryOption("All Updates", "ALL"),
        FeedCategoryOption("Company Announcements", "company_announcement"),
        FeedCategoryOption("General Updates", "general_update"),
        FeedCategoryOption("Events", "event"),
        FeedCategoryOption("Policy Updates", "policy_update"),
        FeedCategoryOption("Recognition", "recognition"),
        FeedCategoryOption("Other", "other")
    )

    // Helper to format ISO timestamp to time ago string
    fun formatDisplayTimeAgo(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Today, 10:04"
        return try {
            val instant = Instant.parse(isoString)
            val now = Instant.now()
            val diffMins = java.time.Duration.between(instant, now).toMinutes()
            when {
                diffMins < 60 -> "${if (diffMins <= 0) 1 else diffMins}m ago"
                diffMins < 1440 -> "Today, " + DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
                    .withZone(ZoneId.systemDefault()).format(instant)
                else -> DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
                    .withZone(ZoneId.systemDefault()).format(instant)
            }
        } catch (_: Exception) {
            "Today, 10:04"
        }
    }

    // Helper to normalize and match category keys
    fun normalizeCategory(raw: String): String {
        return raw.trim().lowercase().replace(Regex("[\\s-]+"), "_")
    }

    // Filter announcements by active tab and search query
    val filteredAnnouncements = remember(feedItems, activeCategoryKey, searchQuery) {
        val query = searchQuery.trim().lowercase()
        feedItems.filter { item ->
            val itemCatKey = normalizeCategory(item.postCategory)
            val matchesCategory = activeCategoryKey == "ALL" || itemCatKey == activeCategoryKey

            val author = "${item.createdBy?.firstName.orEmpty()} ${item.createdBy?.lastName.orEmpty()}".trim()
            val matchesSearch = query.isBlank() ||
                    item.title.lowercase().contains(query) ||
                    item.description.lowercase().contains(query) ||
                    author.lowercase().contains(query)

            matchesCategory && matchesSearch
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Top Header Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Employee Feed",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    StatusBadge(
                        text = "● Live Updates",
                        bgColor = greenBg,
                        textColor = greentext
                    )
                }
                Text(
                    text = "Stay updated with company announcements, HR notices, and workplace updates.",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )
            }
        }

        // 2. Search & Create Feed Action Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Input Box
                FormTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search feed...",
                    containerColor = whiteBg
                )

                // + Create Feed Primary Button
                Button(
                    onClick = onCreateFeedClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = whiteBg,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Create Feed",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = whiteBg
                    )
                }
            }
        }

        // 3. Category Tabs Strip (Scrollable)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSelected = activeCategoryKey == cat.key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Primary else light_grey)
                        .clickable { activeCategoryKey = cat.key }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = cat.label,
                        fontSize = tokens.caption,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) whiteBg else TextSecondary
                    )
                }
            }
        }

        // 4. Feed Announcement Cards List
        if (filteredAnnouncements.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(light_grey),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "No updates found",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = if (searchQuery.isNotBlank()) "No announcements match the keyword \"$searchQuery\"." else "There are no announcements posted in this category yet.",
                        fontSize = tokens.caption,
                        color = mutedText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            filteredAnnouncements.forEach { feed ->
                val author = "${feed.createdBy?.firstName.orEmpty()} ${feed.createdBy?.lastName.orEmpty()}".trim().ifBlank { "" }
                val role = "Executive Office"
                val initials = author.split(" ").map { it.take(1) }.take(2).joinToString("").uppercase().ifBlank { "" }
                val categoryName = feed.postCategory.replace("_", " ").ifBlank { "" }
                val audienceLabel = if (feed.audience?.type?.contains("Filtered", ignoreCase = true) == true) "Selected Employees" else "All Employees"
                val timeAgo = formatDisplayTimeAgo(feed.publishedAt ?: feed.createdAt)

                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    color = whiteBg,
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(tokens.screenPadding),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Author Row with Avatar, Role, Time and 3-Dots Menu
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(primary_light),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Primary
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = author,
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = title_color
                                        )
                                        Text(
                                            text = "• $role",
                                            fontSize = tokens.caption,
                                            color = headerGrey
                                        )
                                    }
                                    Text(
                                        text = timeAgo,
                                        fontSize = tokens.caption,
                                        color = headerGrey
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Actions",
                                tint = headerGrey,
                                modifier = Modifier
                                    .size(tokens.iconSize)
                                    .clickable { }
                            )
                        }

                        // Category Badge Pill
                        StatusBadge(
                            text = categoryName,
                            bgColor = primary_light,
                            textColor = Primary
                        )

                        // Announcement Title & Description
                        Text(
                            text = feed.title,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                        Text(
                            text = feed.description,
                            fontSize = tokens.bodySmall,
                            color = TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        HorizontalDivider(color = grey_border, thickness = 0.8.dp)

                        // Card Footer: View Details link and Target Audience
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View Details →",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = Primary,
                                modifier = Modifier.clickable { }
                            )
                            Text(
                                text = "Target: $audienceLabel",
                                fontSize = tokens.caption,
                                color = headerGrey
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TimeLogsSubTabContent(
    todayAttendance: CheckInOutTodayData?,
    monthLogs: List<CheckInOutTodayData>
) {
    val tokens = LocalAppTokens.current

    // Date Switcher State
    var selectedDateOffset by remember { mutableLongStateOf(0L) }
    val selectedLocalDate = remember(selectedDateOffset) {
        java.time.LocalDate.now().plusDays(selectedDateOffset)
    }
    val isTodaySelected = selectedDateOffset == 0L
    val displayDateHeader = remember(selectedLocalDate) {
        selectedLocalDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US))
    }

    // Resolve active record for selected day
    val activeRecord = remember(selectedLocalDate, monthLogs, todayAttendance) {
        val ymd = selectedLocalDate.toString()
        monthLogs.find { it.date?.take(10) == ymd } ?: if (isTodaySelected) todayAttendance else null
    }

    // Punches for the selected day
    val punches = activeRecord?.punches.orEmpty()
    val workingMins = activeRecord?.totalWorkingMinutes ?: 0
    val breakMins = activeRecord?.totalBreakMinutes ?: 0
    val grossMins = workingMins + breakMins
    val targetMinutes = 480 // 8 hours standard target

    // Helper formatter
    @SuppressLint("DefaultLocale")
    fun formatMinutesToDuration(mins: Int): String {
        val h = mins / 60
        val m = mins % 60
        return if (h > 0) "${h}h ${String.format("%02dm", m)}" else "${m} min"
    }

    val firstSwipeInFormatted = formatIsoTimeToAmPm(activeRecord?.firstIn)
    val lastSwipeOutFormatted = formatIsoTimeToAmPm(activeRecord?.lastOut)
    val isOpen = punches.lastOrNull()?.type.equals("in", ignoreCase = true)
    val isComplete = punches.isNotEmpty() && !isOpen && (punches.size % 2 == 0)

    // Interactive Dropdown States for Section 7
    var selectedRange by remember { mutableStateOf("Last 7 Days") }
    var isRangeDropdownExpanded by remember { mutableStateOf(false) }

    var selectedStatus by remember { mutableStateOf("All Statuses") }
    var isStatusDropdownExpanded by remember { mutableStateOf(false) }

    // Dynamic filtering of logs
    val filteredLogs = remember(monthLogs, selectedRange, selectedStatus) {
        val daysLimit = when (selectedRange) {
            "Last 7 Days" -> 7
            "Last 15 Days" -> 15
            "Last 30 Days" -> 30
            else -> 30
        }
        val cutoffDate = java.time.LocalDate.now().minusDays(daysLimit.toLong())

        monthLogs.filter { log ->
            val logDate = try {
                java.time.LocalDate.parse(log.date?.take(10) ?: "")
            } catch (_: Exception) {
                null
            }

            val matchesRange = logDate == null || !logDate.isBefore(cutoffDate)
            val matchesStatus = when (selectedStatus) {
                "Complete" -> !log.lastOut.isNullOrBlank()
                "Late" -> log.lateMinutes > 0
                "Incomplete" -> log.lastOut.isNullOrBlank()
                else -> true
            }

            matchesRange && matchesStatus
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Time Logs",
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )
            StatusBadge(
                text = "● Biometric Sync Active",
                bgColor = greenBg,
                textColor = greentext
            )
        }
        Text(
            text = "View your daily swipe-in, swipe-out and break records.",
            fontSize = tokens.caption,
            color = TextSecondary
        )

        // 2. Date Navigator Bar
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prev Day Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, sectionBorder, RoundedCornerShape(6.dp))
                            .clickable { selectedDateOffset -= 1 }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text("Prev Day", fontSize = tokens.caption, color = TextSecondary)
                    }

                    // Selected Date Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, sectionBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = headerGrey,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = displayDateHeader,
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                    }

                    // Next Day Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, sectionBorder, RoundedCornerShape(6.dp))
                            .clickable(enabled = !isTodaySelected) { if (!isTodaySelected) selectedDateOffset += 1 }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Next Day",
                            fontSize = tokens.caption,
                            color = if (isTodaySelected) mutedText else TextSecondary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = if (isTodaySelected) mutedText else TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sequence Status Badge
                    StatusBadge(
                        text = "● ${punches.size} Recorded Punches • " + when {
                            punches.isEmpty() -> "No Activity"
                            isComplete -> "Complete Sequence"
                            else -> "Incomplete Sequence"
                        },
                        bgColor = if (isComplete) greenBg else yellowBg,
                        textColor = if (isComplete) greentext else yellowText
                    )

                    Spacer(Modifier.weight(1f))

                    // Today Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isTodaySelected) primary_light else Primary)
                            .clickable(enabled = !isTodaySelected) { selectedDateOffset = 0L }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Today",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Medium,
                            color = if (isTodaySelected) Primary else whiteBg
                        )
                    }
                }
            }
        }

        // 3. Four KPI Summary Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimeLogKpiTile(
                modifier = Modifier.weight(1f),
                label = "FIRST SWIPE IN",
                value = firstSwipeInFormatted,
                sub = "Studio Terminal",
                icon = Icons.AutoMirrored.Filled.Login,
                iconBg = greenBg,
                iconTint = greentext
            )
            TimeLogKpiTile(
                modifier = Modifier.weight(1f),
                label = "LAST SWIPE OUT",
                value = if (lastSwipeOutFormatted != "--") lastSwipeOutFormatted else "— Missing —",
                sub = if (lastSwipeOutFormatted != "--") "End of scheduled shift" else "Awaiting swipe-out",
                icon = Icons.AutoMirrored.Filled.Logout,
                iconBg = primary_light,
                iconTint = Primary,
                valueColor = if (lastSwipeOutFormatted != "--") title_color else redText
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimeLogKpiTile(
                modifier = Modifier.weight(1f),
                label = "TOTAL BREAK TIME",
                value = "${breakMins} min",
                sub = "${if (breakMins > 0) "2" else "0"} breaks taken",
                icon = Icons.Default.FreeBreakfast,
                iconBg = yellowBg,
                iconTint = yellowText
            )
            TimeLogKpiTile(
                modifier = Modifier.weight(1f),
                label = "NET WORKING TIME",
                value = formatMinutesToDuration(workingMins),
                sub = "Target: 8h 00m • " + if (workingMins >= targetMinutes) "Target met" else "Under target",
                icon = Icons.Default.Schedule,
                iconBg = primary_light,
                iconTint = Primary
            )
        }

        // 4. Punch History Timeline Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PUNCH HISTORY",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                        Text(
                            text = "${punches.size} recorded swipe-in and swipe-out events for $displayDateHeader.",
                            fontSize = tokens.caption,
                            color = headerGrey
                        )
                    }
                }

                StatusBadge(
                    text = "Timezone: UTC+05:30",
                    bgColor = light_grey,
                    textColor = TextSecondary
                )

                if (punches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No punches recorded for this day.", fontSize = tokens.caption, color = mutedText)
                    }
                } else {
                    punches.forEachIndexed { index, p ->
                        val isSwipeIn = p.type.equals("in", ignoreCase = true)
                        val isFirst = index == 0
                        val isLast = index == punches.lastIndex

                        val title = when {
                            isFirst -> "First Check-In"
                            isLast && !isSwipeIn -> "Final Check-Out"
                            !isSwipeIn -> "Break Started"
                            else -> "Returned from Break"
                        }

                        val description = when {
                            isFirst -> "Started the working day • Studio Terminal"
                            isLast && !isSwipeIn -> "The working day ended • Studio Terminal"
                            !isSwipeIn -> "Employee left the active working session"
                            else -> "Working session resumed"
                        }

                        val status = if (isSwipeIn) "Verified" else "Completed"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Timeline Connecting Line & Dot
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isSwipeIn) greentext else yellowText)
                                )
                                if (index < punches.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.5.dp)
                                            .height(55.dp)
                                            .background(sectionBorder)
                                    )
                                }
                            }

                            // Timeline Card Container
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = light_grey.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, sectionBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = formatIsoTimeToAmPm(p.time),
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = title_color
                                            )
                                            StatusBadge(
                                                text = if (isSwipeIn) "Swipe In" else "Swipe Out",
                                                bgColor = if (isSwipeIn) greenBg else yellowBg,
                                                textColor = if (isSwipeIn) greentext else yellowText
                                            )
                                            Text(
                                                text = title,
                                                fontSize = tokens.caption,
                                                fontWeight = FontWeight.Medium,
                                                color = title_color
                                            )
                                        }

                                        StatusBadge(
                                            text = status,
                                            bgColor = if (isSwipeIn) greenBg else primary_light,
                                            textColor = if (isSwipeIn) greentext else Primary
                                        )
                                    }

                                    Text(
                                        text = description,
                                        fontSize = tokens.label,
                                        color = headerGrey
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Working Time Breakdown Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "WORKING TIME BREAKDOWN",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Text(
                    text = "Duration breakdown for today",
                    fontSize = tokens.label,
                    color = headerGrey
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("First Swipe In", fontSize = tokens.caption, color = headerGrey)
                    Text(firstSwipeInFormatted, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Final Swipe Out", fontSize = tokens.caption, color = headerGrey)
                    Text(lastSwipeOutFormatted, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gross Elapsed Time", fontSize = tokens.caption, color = headerGrey)
                    Text(formatMinutesToDuration(grossMins), fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
                }

                // Total Break Time Highlighted Box
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = yellowBg,
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Break Time", fontSize = tokens.caption, color = yellowText)
                        Text("- ${breakMins} min", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = yellowText)
                    }
                }

                HorizontalDivider(color = grey_border, thickness = 0.8.dp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Net Working Time:", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                    Text(formatMinutesToDuration(workingMins), fontSize = tokens.bodyLarge, fontWeight = FontWeight.SemiBold, color = Primary)
                }

                // Activity Distribution Bar
                Text(
                    text = "ACTIVITY DISTRIBUTION",
                    fontSize = tokens.label,
                    fontWeight = FontWeight.SemiBold,
                    color = headerGrey
                )
                val workPct = if (grossMins > 0) (workingMins.toFloat() / grossMins.toFloat()).coerceIn(0f, 1f) else 1f
                DataCardProgressBar(progress = workPct, progressColor = complete_button_bg)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(complete_button_bg))
                        Text("Working (${(workPct * 100).toInt()}%)", fontSize = tokens.label, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(yellowText))
                        Text("Breaks (${((1f - workPct) * 100).toInt()}%)", fontSize = tokens.label, color = TextSecondary)
                    }
                }

                // Shift Progress Status Box
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isComplete) greenBg else yellowBg,
                    border = BorderStroke(1.dp, if (isComplete) Color(0xFFBBF7D0) else Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isComplete) greentext else yellowText,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = if (isComplete) "Standard Day Shift Completed!" else "Shift in progress",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isComplete) greentext else yellowText
                            )
                            Text(
                                text = if (isComplete) "Punch policy compliant" else "A swipe-out is missing for this day",
                                fontSize = tokens.label,
                                color = if (isComplete) greentext else yellowText
                            )
                        }
                    }
                }
            }
        }

        // 6. Punch Policy & Rules Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "PUNCH POLICY & RULES",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Text(
                    text = "Company attendance guidelines",
                    fontSize = tokens.label,
                    color = headerGrey
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = light_grey,
                    border = BorderStroke(0.5.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("• Minimum 30 min meal break deducted after 6h of continuous work.", fontSize = tokens.caption, color = TextSecondary)
                        Text("• Grace time: 15 mins permissible for morning first swipe.", fontSize = tokens.caption, color = TextSecondary)
                        Text("• Any missing punch can be regularized within 48 hours via 'Request Correction'.", fontSize = tokens.caption, color = TextSecondary)
                    }
                }

                Text(
                    text = "Apply for Punch Regularization →",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.clickable { }
                )
            }
        }

        // 7. Recent Time Logs Table Card (Interactive Dropdowns & Filtered Month Logs)
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Text(
                        text = "RECENT TIME LOGS",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = "Overview of your past days punch activity and net work duration.",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Interactive Range Dropdown
                        FormDropdown(
                            modifier = Modifier.weight(1f),
                            value = selectedRange,
                            expanded = isRangeDropdownExpanded,
                            onExpandChange = { isRangeDropdownExpanded = it },
                            options = listOf("Last 7 Days", "Last 15 Days", "Last 30 Days"),
                            onOptionSelected = {
                                selectedRange = it
                                isRangeDropdownExpanded = false
                            }
                        )

                        // Interactive Status Dropdown
                        FormDropdown(
                            modifier = Modifier.weight(1f),
                            value = selectedStatus,
                            expanded = isStatusDropdownExpanded,
                            onExpandChange = { isStatusDropdownExpanded = it },
                            options = listOf("All Statuses", "Complete", "Late", "Incomplete"),
                            onOptionSelected = {
                                selectedStatus = it
                                isStatusDropdownExpanded = false
                            }
                        )
                    }
                }

                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(light_grey)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DATE", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = headerGrey, modifier = Modifier.weight(1.2f))
                    Text("FIRST SWIPE IN", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = headerGrey, modifier = Modifier.weight(1f))
                    Text("LAST SWIPE OUT", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = headerGrey, modifier = Modifier.weight(1f))
                }

                // Table Rows
                if (filteredLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No time logs found for this period.", fontSize = tokens.caption, color = mutedText)
                    }
                } else {
                    filteredLogs.forEachIndexed { idx, log ->
                        val dateFormatted = log.date?.take(10) ?: "—"
                        val isToday = dateFormatted == java.time.LocalDate.now().toString()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(dateFormatted, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
                                if (isToday) {
                                    StatusBadge(text = "Today", bgColor = primary_light, textColor = Primary)
                                }
                            }
                            Text(formatIsoTimeToAmPm(log.firstIn), fontSize = tokens.caption, color = TextSecondary, modifier = Modifier.weight(1f))
                            Text(formatIsoTimeToAmPm(log.lastOut), fontSize = tokens.caption, color = TextSecondary, modifier = Modifier.weight(1f))
                        }

                        if (idx < filteredLogs.lastIndex) {
                            HorizontalDivider(color = grey_border, thickness = 0.8.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeLogKpiTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    sub: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    valueColor: Color = title_color
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = tokens.label,
                    fontWeight = FontWeight.SemiBold,
                    color = headerGrey
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
                }
            }
            Text(
                text = value,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
            Text(
                text = sub,
                fontSize = tokens.label,
                color = headerGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CareerHistorySubTabContent(
    profile: MyProfileData?,
    designationDetail: com.cuso.tailor.model.hr.DesignationDetailData? = null
) {
    val tokens = LocalAppTokens.current
    val p = profile
    val des = designationDetail

    // Helper date formatter

    fun formatIsoDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "—"
        return try {
            val instant = Instant.parse(dateStr)
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        } catch (_: Exception) {
            dateStr.take(10)
        }
    }

    // Helper tenure calculator
    fun calculateTenure(doj: String?): String {
        if (doj.isNullOrBlank()) return "Less than a month"
        return try {
            val joinDate = java.time.LocalDate.parse(doj.take(10))
            val now = java.time.LocalDate.now()
            val period = java.time.Period.between(joinDate, now)
            when {
                period.years > 0 -> "${period.years} yr ${period.months} mos"
                period.months > 0 -> "${period.months} mos"
                else -> "Less than a month"
            }
        } catch (_: Exception) {
            "Less than a month"
        }
    }

    // Derived values
    val currentDesName = des?.name ?: p?.designationId?.name ?: "—"
    val departmentName = des?.departmentId?.name ?: p?.departmentId?.name ?: "Management"
    val branchName = p?.branchId?.name ?: "Sample - Main Branch"
    val employeeCode = p?.memberId ?: "SAM-001"
    val employeeName = "${p?.firstName ?: ""} ${p?.lastName ?: ""}".trim().ifBlank { "Nithishkumar S" }
    val formattedDoj = formatIsoDate(p?.doj ?: des?.createdAt)
    val totalTenure = calculateTenure(p?.doj ?: des?.createdAt)

    // History milestones extracted from API
    val historyList = des?.history.orEmpty()
    val totalMilestones = historyList.size + 1

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Header Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Career History",
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    StatusBadge(
                        text = "● Verified Record • $employeeCode",
                        bgColor = greenBg,
                        textColor = greentext
                    )
                }
                Text(
                    text = "View your employment journey, role changes and career milestones.",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )
            }
        }

        // 2. Current Employment Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Primary))
                        Text(
                            text = "CURRENT EMPLOYMENT",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.SemiBold,
                            color = title_color
                        )
                    }
                    StatusBadge(
                        text = "● Confirmed • Full Time",
                        bgColor = greenBg,
                        textColor = greentext
                    )
                }

                HorizontalDivider(color = grey_border, thickness = 0.8.dp)

                InfoFieldGrid("Employee Name", employeeName, "Employee ID", employeeCode)
                InfoFieldGrid("Current Designation", currentDesName, "Department", departmentName)
                InfoFieldGrid("Branch / Work Location", branchName, "Reporting Manager", "—")
                InfoFieldGrid("Date Joined", formattedDoj, "Total Tenure", totalTenure)
            }
        }

        // 3. Career Timeline Card (Timeline connecting dots)
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CAREER TIMELINE",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = "$totalMilestones Milestones",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                }
                Text(
                    text = "A chronological record of your role changes and employment milestones.",
                    fontSize = tokens.caption,
                    color = headerGrey
                )

                // Milestone 1: Current Active Role
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(greentext)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .height(130.dp)
                                .background(sectionBorder)
                        )
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Current Role — $currentDesName",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                                StatusBadge(text = "Current", bgColor = greenBg, textColor = greentext)
                            }
                            Text(
                                text = "Effective: ${formatIsoDate(des?.updatedAt)}",
                                fontSize = tokens.label,
                                color = headerGrey
                            )
                            if (historyList.isNotEmpty()) {
                                Text(
                                    text = "Previous: ${historyList.first().name} → New: $currentDesName",
                                    fontSize = tokens.label,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = des?.description?.ifBlank { "Mobile App Development" } ?: "Mobile App Development",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reference: REF-${des?.code ?: "MAD001"}",
                                    fontSize = tokens.label,
                                    color = headerGrey
                                )
                                Text(
                                    text = "View Details →",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary
                                )
                            }
                        }
                    }
                }

                // Milestone 2+: Employment Started / History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(headerGrey)
                    )

                    val initialRoleName = historyList.lastOrNull()?.name ?: currentDesName
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = whiteBg,
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Employment Started",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                                StatusBadge(text = "Onboarding Milestone", bgColor = light_grey, textColor = TextSecondary)
                            }
                            Text(
                                text = "Effective: $formattedDoj",
                                fontSize = tokens.label,
                                color = headerGrey
                            )
                            Text(
                                text = "Initial Designation: $initialRoleName",
                                fontSize = tokens.label,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Department: $departmentName  Location: $branchName",
                                fontSize = tokens.label,
                                color = headerGrey
                            )
                            Text(
                                text = "Joined $branchName as $initialRoleName.",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reference: REF-ONBOARD-$employeeCode",
                                    fontSize = tokens.label,
                                    color = headerGrey
                                )
                                Text(
                                    text = "View Details →",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Employment History Table Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Text(
                        text = "EMPLOYMENT HISTORY",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = title_color
                    )
                    Text(
                        text = "Official service records and assignments table.",
                        fontSize = tokens.caption,
                        color = headerGrey
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Showing $totalMilestones historical records",
                        fontSize = tokens.label,
                        color = headerGrey
                    )
                }

                // Table Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(light_grey)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("EFFECTIVE FROM", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = headerGrey, modifier = Modifier.weight(1f))
                    Text("EFFECTIVE TO", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = headerGrey, modifier = Modifier.weight(1f))
                    Text("DESIGNATION", fontSize = tokens.label, fontWeight = FontWeight.SemiBold, color = headerGrey, modifier = Modifier.weight(1.3f))
                }

                // Row 1: Current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(formatIsoDate(des?.updatedAt ?: p?.doj), fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color, modifier = Modifier.weight(1f))
                    Text("Present", fontSize = tokens.caption, color = TextSecondary, modifier = Modifier.weight(1f))
                    Text(currentDesName, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color, modifier = Modifier.weight(1.3f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                HorizontalDivider(color = grey_border, thickness = 0.8.dp)

                // Row 2: Past records from history array
                if (historyList.isNotEmpty()) {
                    historyList.forEach { hist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(formattedDoj, fontSize = tokens.caption, color = TextSecondary, modifier = Modifier.weight(1f))
                            Text(formatIsoDate(hist.changedAt), fontSize = tokens.caption, color = TextSecondary, modifier = Modifier.weight(1f))
                            Text(hist.name, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color, modifier = Modifier.weight(1.3f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        HorizontalDivider(color = grey_border, thickness = 0.8.dp)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(formattedDoj, fontSize = tokens.caption, color = TextSecondary, modifier = Modifier.weight(1f))
                        Text(formatIsoDate(des?.updatedAt), fontSize = tokens.caption, color = TextSecondary, modifier = Modifier.weight(1f))
                        Text(currentDesName, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color, modifier = Modifier.weight(1.3f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    HorizontalDivider(color = grey_border, thickness = 0.8.dp)
                }

                // Table Audit Footer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(light_grey.copy(alpha = 0.4f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Official HR Verification Status: Audit Compliant",
                        fontSize = tokens.label,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    Text(
                        text = "Last Updated by HR: ${formatIsoDate(des?.updatedAt ?: p?.doj)}",
                        fontSize = tokens.label,
                        color = headerGrey
                    )
                }
            }
        }
    }
}

// =========================================================================
// TEAM DASHBOARD VIEW - MATCHING EXACT MOBILE SCREENSHOT DESIGN
// =========================================================================

@Composable
private fun TeamSpaceContent(
    teamData: com.cuso.tailor.model.hr.TeamDashboardData?
) {
    val tokens = LocalAppTokens.current

    // State for employee search and filters
    var searchQuery by remember { mutableStateOf("") }
    var selectedDepartmentFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedMemberId by remember { mutableStateOf("") }
    var selectedProfileTab by remember { mutableStateOf("Activity") }
    val profileTabs = listOf("Activity", "Attendance", "Leave", "Shift", "Requests")

    val members = teamData?.teamMembers.orEmpty()
    val totalCount = teamData?.summary?.totalTeamMembers ?: members.size

    // Default select first member if available
    LaunchedEffect(members) {
        if (selectedMemberId.isEmpty() && members.isNotEmpty()) {
            selectedMemberId = members.first()._id
        }
    }

    // Filter members based on search query
    val filteredMembers = remember(members, searchQuery) {
        if (searchQuery.isBlank()) {
            members
        } else {
            val q = searchQuery.trim().lowercase()
            members.filter { m ->
                val fullName = "${m.firstName} ${m.lastName}".lowercase()
                val code = (m.employeeCode ?: m.memberId ?: "").lowercase()
                fullName.contains(q) || code.contains(q)
            }
        }
    }

    val selectedMember = members.find { it._id == selectedMemberId } ?: members.firstOrNull()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------------------------------------------------------------------
        // 1. Top 6 KPI Stat Tiles (2 Columns x 3 Rows)
        // ---------------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "Team Members",
                value = "$totalCount",
                icon = Icons.Default.PeopleOutline,
                iconTint = Primary,
                iconBg = primary_light
            )
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "In Office",
                value = "0",
                icon = Icons.Default.Domain,
                iconTint = greentext,
                iconBg = greenBg
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "On Leave",
                value = "0",
                icon = Icons.Default.CalendarToday,
                iconTint = yellowText,
                iconBg = yellowBg
            )
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "WFH",
                value = "0",
                icon = Icons.Default.Home,
                iconTint = blueText,
                iconBg = primary_light
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "Absent",
                value = "$totalCount",
                icon = Icons.Default.PersonOff,
                iconTint = redText,
                iconBg = redBg
            )
            OrgKpiTile(
                modifier = Modifier.weight(1f),
                label = "Late Today",
                value = "0",
                icon = Icons.Default.AccessTime,
                iconTint = yellowText,
                iconBg = yellowBg
            )
        }

        // ---------------------------------------------------------------------
        // 2. Search & Filter Bar
        // ---------------------------------------------------------------------
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Input Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.fieldHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(whiteBg)
                        .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = mutedText,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Spacer(Modifier.width(8.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = tokens.bodySmall,
                            color = TextPrimary
                        ),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search employee by name or ID...",
                                    fontSize = tokens.bodySmall,
                                    color = mutedText
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                // Filter Dropdowns Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.fieldHeight * 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, sectionBorder),
                        color = light_grey
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("All Departm...", fontSize = tokens.caption, color = TextPrimary)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(tokens.fieldHeight * 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, sectionBorder),
                        color = light_grey
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("All Status", fontSize = tokens.caption, color = TextPrimary)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(tokens.fieldHeight * 0.9f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, sectionBorder, RoundedCornerShape(8.dp))
                            .background(whiteBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Filters", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 3. Employee Carousel Horizontal Card Strip
        // ---------------------------------------------------------------------
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredMembers) { m ->
                val isSelected = m._id == selectedMemberId
                val initials = "${m.firstName.take(1)}${m.lastName.take(1)}".uppercase().ifBlank { "EM" }

                Surface(
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                    color = whiteBg,
                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) Primary else sectionBorder),
                    modifier = Modifier
                        .width(190.dp)
                        .clickable { selectedMemberId = m._id }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(primary_light),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "${m.firstName} ${m.lastName}",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = title_color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Team Member",
                                fontSize = tokens.label,
                                color = TextSecondary
                            )
                            StatusBadge(
                                text = "Absent",
                                bgColor = redBg,
                                textColor = redText
                            )
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 8. Selected Employee Detail Card
        // ---------------------------------------------------------------------
        if (selectedMember != null) {
            val initials = "${selectedMember.firstName.take(1)}${selectedMember.lastName.take(1)}".uppercase().ifBlank { "EM" }

            Surface(
                shape = RoundedCornerShape(tokens.cardCornerRadius),
                color = whiteBg,
                border = BorderStroke(1.dp, sectionBorder)
            ) {
                Column(
                    modifier = Modifier.padding(tokens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(primary_light),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initials, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = Primary)
                            }
                            Column {
                                Text(
                                    text = "${selectedMember.firstName} ${selectedMember.lastName}",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                                Text(
                                    text = "Team Member • ${selectedMember.employeeCode ?: selectedMember.memberId ?: "—"}",
                                    fontSize = tokens.caption,
                                    color = TextSecondary
                                )
                                Text(
                                    text = selectedMember.departmentId?.name ?: "Management",
                                    fontSize = tokens.label,
                                    color = headerGrey
                                )
                            }
                        }

                        StatusBadge(
                            text = "Absent",
                            bgColor = redBg,
                            textColor = redText
                        )
                    }

                    // 4 Metric Information Tiles (2x2 Grid)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = light_grey
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("CHECK-IN", fontSize = tokens.label, color = headerGrey)
                                Text("--", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = light_grey
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("SHIFT", fontSize = tokens.label, color = headerGrey)
                                Text("General Shift", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = light_grey
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("WORKING TODAY", fontSize = tokens.label, color = headerGrey)
                                Text("--", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = light_grey
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("DEPARTMENT", fontSize = tokens.label, color = headerGrey)
                                Text(
                                    text = selectedMember.departmentId?.name ?: "Management",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // Profile Underline Sub-Tabs
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        items(profileTabs) { tab ->
                            val isTabSelected = selectedProfileTab == tab
                            Column(
                                modifier = Modifier
                                    .clickable { selectedProfileTab = tab }
                                    .padding(bottom = 4.dp)
                            ) {
                                Text(
                                    text = tab,
                                    fontSize = tokens.caption,
                                    fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isTabSelected) Primary else TextSecondary
                                )
                                Spacer(Modifier.height(4.dp))
                                if (isTabSelected) {
                                    Box(
                                        modifier = Modifier
                                            .height(2.dp)
                                            .width(28.dp)
                                            .background(Primary)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = grey_border, thickness = 1.dp)

                    // Sub-Tab Content: Activity Timeline
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "TODAY'S ACTIVITY TIMELINE",
                            fontSize = tokens.caption,
                            fontWeight = FontWeight.Bold,
                            color = headerGrey
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No activity punches recorded for today",
                                fontSize = tokens.caption,
                                color = mutedText
                            )
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 4. Today's Team Status Table Card (Horizontally Scrollable)
        // ---------------------------------------------------------------------
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Today's Team Status",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                // Outer border container with scrollable viewport
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = whiteBg,
                    border = BorderStroke(0.8.dp, sectionBorder)
                ) {
                    val tableScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(tableScrollState)
                    ) {
                        // Table Header Row with aligned column widths
                        Row(
                            modifier = Modifier
                                .width(700.dp)
                                .background(light_grey)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EMPLOYEE",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = headerGrey,
                                modifier = Modifier.width(220.dp)
                            )
                            Text(
                                text = "STATUS",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = headerGrey,
                                modifier = Modifier.width(90.dp)
                            )
                            Text(
                                text = "CHECK-IN",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = headerGrey,
                                modifier = Modifier.width(90.dp)
                            )
                            Text(
                                text = "SHIFT",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = headerGrey,
                                modifier = Modifier.width(120.dp)
                            )
                            Text(
                                text = "WORKING HOURS",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = headerGrey,
                                modifier = Modifier.width(110.dp)
                            )
                        }

                        HorizontalDivider(color = sectionBorder, thickness = 0.8.dp)

                        // Table Data Rows
                        members.forEachIndexed { index, m ->
                            val initials = "${m.firstName.take(1)}${m.lastName.take(1)}".uppercase().ifBlank { "EM" }

                            Row(
                                modifier = Modifier
                                    .width(700.dp)
                                    .clickable { selectedMemberId = m._id }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Column 1: Employee Identity
                                Row(
                                    modifier = Modifier.width(220.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(primary_light),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Bold,
                                            color = Primary
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${m.firstName} ${m.lastName}",
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = title_color,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = m.employeeCode ?: m.memberId ?: "—",
                                            fontSize = tokens.label,
                                            color = headerGrey,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                // Column 2: Status
                                Box(modifier = Modifier.width(90.dp)) {
                                    StatusBadge(
                                        text = "Absent",
                                        bgColor = redBg,
                                        textColor = redText
                                    )
                                }

                                // Column 3: Check-in Time
                                Text(
                                    text = "--",
                                    fontSize = tokens.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.width(90.dp),
                                    maxLines = 1
                                )

                                // Column 4: Shift Name
                                Text(
                                    text = "General Shift",
                                    fontSize = tokens.bodySmall,
                                    color = TextPrimary,
                                    modifier = Modifier.width(120.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // Column 5: Working Hours
                                Text(
                                    text = "--",
                                    fontSize = tokens.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.width(110.dp),
                                    maxLines = 1
                                )
                            }

                            if (index < members.lastIndex) {
                                HorizontalDivider(color = grey_border, thickness = 0.8.dp)
                            }
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 5. Weekly Attendance Table Card (Horizontally Scrollable)
        // ---------------------------------------------------------------------
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Weekly Attendance",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                // Outer border container with scrollable viewport
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = whiteBg,
                    border = BorderStroke(0.8.dp, sectionBorder)
                ) {
                    val weeklyScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(weeklyScrollState)
                    ) {
                        // Table Header Row with aligned column widths
                        Row(
                            modifier = Modifier
                                .width(560.dp)
                                .background(light_grey)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EMPLOYEE",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = headerGrey,
                                modifier = Modifier.width(200.dp)
                            )
                            Text(
                                text = "MON",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = headerGrey,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "TUE",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = headerGrey,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "WED",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = headerGrey,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "THU",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = headerGrey,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "FRI",
                                fontSize = tokens.label,
                                fontWeight = FontWeight.Bold,
                                color = headerGrey,
                                modifier = Modifier.width(70.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        HorizontalDivider(color = sectionBorder, thickness = 0.8.dp)

                        // Table Data Rows
                        members.forEachIndexed { index, m ->
                            val initials = "${m.firstName.take(1)}${m.lastName.take(1)}".uppercase().ifBlank { "EM" }

                            Row(
                                modifier = Modifier
                                    .width(560.dp)
                                    .clickable { selectedMemberId = m._id }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Column 1: Employee Identity
                                Row(
                                    modifier = Modifier.width(200.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(primary_light),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = initials,
                                            fontSize = tokens.label,
                                            fontWeight = FontWeight.Bold,
                                            color = Primary
                                        )
                                    }
                                    Text(
                                        text = "${m.firstName} ${m.lastName}",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = title_color,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Column 2: MON
                                Box(
                                    modifier = Modifier.width(70.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(light_grey),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "--",
                                            fontSize = tokens.caption,
                                            color = mutedText
                                        )
                                    }
                                }

                                // Column 3: TUE (Absent Marker)
                                Box(
                                    modifier = Modifier.width(70.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(redBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "A",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Bold,
                                            color = redText
                                        )
                                    }
                                }

                                // Column 4: WED
                                Box(
                                    modifier = Modifier.width(70.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(light_grey),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "--",
                                            fontSize = tokens.caption,
                                            color = mutedText
                                        )
                                    }
                                }

                                // Column 5: THU
                                Box(
                                    modifier = Modifier.width(70.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(light_grey),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "--",
                                            fontSize = tokens.caption,
                                            color = mutedText
                                        )
                                    }
                                }

                                // Column 6: FRI
                                Box(
                                    modifier = Modifier.width(70.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(36.dp)
                                            .height(26.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(light_grey),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "--",
                                            fontSize = tokens.caption,
                                            color = mutedText
                                        )
                                    }
                                }
                            }

                            if (index < members.lastIndex) {
                                HorizontalDivider(color = grey_border, thickness = 0.8.dp)
                            }
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 6. Team Shift Distribution
        // ---------------------------------------------------------------------
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Team Shift Distribution",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Gauge with Total Count
                    Box(
                        modifier = Modifier.size(96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Primary,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalCount",
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                            Text(
                                text = "Total",
                                fontSize = tokens.label,
                                color = headerGrey
                            )
                        }
                    }

                    // Shift Legend
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Primary))
                        Text(
                            text = "General Shift (100%)",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // ---------------------------------------------------------------------
        // 7. Pending Team Requests
        // ---------------------------------------------------------------------
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pending Team Requests",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pending team requests",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }
            }
        }



        // ---------------------------------------------------------------------
        // 9. Leave Balance Card
        // ---------------------------------------------------------------------
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.screenPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Leave Balance",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )
                Text(
                    text = "View Details →",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun MetricBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    valueColor: Color
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
            .background(light_grey)
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                fontSize = tokens.caption,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = tokens.bodySmall, color = TextSecondary)
        Text(text = value, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun RequestItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String,
    badgeBg: Color,
    badgeTextCol: Color
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
            .background(light_grey)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
            Column {
                Text(text = title, fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = subtitle, fontSize = tokens.caption, color = TextSecondary)
            }
        }
        StatusBadge(text = badgeText, bgColor = badgeBg, textColor = badgeTextCol)
    }
}

@Composable
private fun OverviewSectionContent(
    orgData: OrgDashboardData?,
    onNavigateToLeave: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToShift: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val totalEmp = orgData?.overview?.totalEmployees ?: 4

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // 7 KPI Stat Tiles (Equal 2-Column Grid Alignment)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "Total Employees",
                value = "$totalEmp",
                subLabel = "Active workforce",
                subLabelColor = headerGrey,
                icon = Icons.Default.PeopleOutline
            )
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "Present Today",
                value = "0",
                subLabel = "0% of total",
                subLabelColor = greentext,
                icon = Icons.Default.CheckCircleOutline
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "On Leave",
                value = "0",
                subLabel = "0% of total",
                subLabelColor = redText,
                icon = Icons.Default.CalendarToday
            )
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "Pending Approvals",
                value = "0",
                subLabel = "Across modules",
                subLabelColor = TextSecondary,
                icon = Icons.Default.AssignmentTurnedIn
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "Payroll This Month",
                value = "₹ 0",
                subLabel = "Processed payout",
                subLabelColor = headerGrey,
                icon = Icons.Default.AccountBalanceWallet
            )
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "Training Due",
                value = "0",
                subLabel = "Employees",
                subLabelColor = greentext,
                icon = Icons.Default.School
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "Open Exit Cases",
                value = "1",
                subLabel = "In progress",
                subLabelColor = yellowText,
                icon = Icons.AutoMirrored.Filled.Logout
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // Reporting To Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("REPORTING TO", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = headerGrey)
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Not assigned", fontSize = tokens.bodySmall, color = mutedText)
                }
            }
        }

        // My Team Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MY TEAM", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = title_color)
                    Text("View All →", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = Primary)
                }
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No team members", fontSize = tokens.bodySmall, color = mutedText)
                }
            }
        }

        // My Reportees Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MY REPORTEES", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = title_color)
                    Text("View All →", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = Primary)
                }
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No reportees", fontSize = tokens.bodySmall, color = mutedText)
                }
            }
        }

        // HR Modules Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("HR MODULES", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = title_color)
                    Text("View All Modules →", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = Primary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = light_grey
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.PeopleOutline, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                            Text("4", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)
                            Text("Employee", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                            Text("Total Employees", fontSize = tokens.label, color = headerGrey)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = light_grey
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.Domain, contentDescription = null, tint = Primary, modifier = Modifier.size(tokens.iconSize))
                            Text("1", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = title_color)
                            Text("Organization", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                            Text("Departments", fontSize = tokens.label, color = headerGrey)
                        }
                    }
                }
            }
        }

        // Leave Balance & Utilization Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LEAVE BALANCE & UTILIZATION", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = headerGrey)
                    Text(
                        "View Balances →",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier.clickable { onNavigateToLeave() }
                    )
                }
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ⓘ No leave balance records assigned.", fontSize = tokens.bodySmall, color = mutedText)
                }
            }
        }

        // Weekly Attendance Calendar Strip
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("WEEKLY ATTENDANCE", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = headerGrey)
                    Text("This Week ⌵", fontSize = tokens.caption, color = TextSecondary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf("MON" to "9", "TUE" to "10", "WED" to "11", "THU" to "12", "FRI" to "13", "SAT" to "14", "SUN" to "15")
                    days.forEach { (day, num) ->
                        val isToday = day == "TUE"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    if (isToday) 1.5.dp else 0.dp,
                                    if (isToday) Primary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(day, fontSize = tokens.label, color = if (isToday) Primary else headerGrey)
                                Text(num, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isToday) redText else Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions Grid
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("QUICK ACTIONS", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = headerGrey)

                val actionsRow1 = listOf(
                    Triple("Apply Leave", Icons.Default.FlightTakeoff, redBg),
                    Triple("Regularize", Icons.Default.Autorenew, yellowBg),
                    Triple("Encashment", Icons.Default.Description, primary_light),
                    Triple("Muster", Icons.Default.PersonAddAlt1, greenBg)
                )
                val actionsRow2 = listOf(
                    Triple("Expense", Icons.Default.AccountBalanceWallet, redBg),
                    Triple("Raise Debit", Icons.AutoMirrored.Filled.ReceiptLong, yellowBg),
                    Triple("Requisition", Icons.Default.Build, yellowBg),
                    Triple("Helpdesk", Icons.Default.SupportAgent, primary_light)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    actionsRow1.forEach { (label, icon, bg) ->
                        QuickActionItem(label = label, icon = icon, bg = bg)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    actionsRow2.forEach { (label, icon, bg) ->
                        QuickActionItem(label = label, icon = icon, bg = bg)
                    }
                }
            }
        }

        // Company Announcements Card
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("COMPANY ANNOUNCEMENTS", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = title_color)
                    Text("View All →", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = Primary)
                }
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No announcements", fontSize = tokens.bodySmall, color = mutedText)
                }
            }
        }
    }
}

@Composable
private fun KpiStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subLabel: String? = null,
    subLabelColor: Color = greentext,
    icon: ImageVector
) {
    val tokens = LocalAppTokens.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(tokens.cardHeight * 0.95f), // Fixed equal height according to design token
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
        color = whiteBg,
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Title + Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = headerGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }

            // Bottom Section: Big Value + SubLabel (Always takes same vertical space)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = title_color
                )
                Text(
                    text = if (!subLabel.isNullOrBlank()) subLabel else " ", // Invisible space to preserve exact uniform height
                    fontSize = tokens.label,
                    color = subLabelColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    label: String,
    icon: ImageVector,
    bg: Color
) {
    val tokens = LocalAppTokens.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.width(68.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Primary, modifier = Modifier.size(tokens.iconSize * 1.1f))
        }
        Text(
            text = label,
            fontSize = tokens.label,
            color = title_color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@SuppressLint("NewApi")
private fun formatIsoDateToPretty(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "Today"
    return try {
        val instant = Instant.parse(isoString)
        val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy").withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (_: Exception) {
        isoString.take(10)
    }
}

@SuppressLint("NewApi")
private fun formatIsoTimeToAmPm(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "--"
    return try {
        val instant = Instant.parse(isoString)
        val formatter = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (_: Exception) {
        "--"
    }
}