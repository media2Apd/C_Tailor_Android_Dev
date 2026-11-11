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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.cuso.tailor.view.composable.StatusBadge
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.HrDashboardViewModel
import com.cuso.tailor.viewmodel.HrViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HrDashboardScreen(
    onClose: () -> Unit,
    onNavigateToLeave: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToShift: () -> Unit,
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
        "Profile", "Approvals", "Leave", "Attendance", "Shift", "Files", "Time Logs", "Career History"
    )

    val todayAttendance by viewModel.todayAttendance.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState() // Collected logged-in user profile
    val teamData by viewModel.teamData.collectAsState()
    val orgData by viewModel.orgData.collectAsState()
    val shiftDetail by hrViewModel.shiftDetail.collectAsState() // Collected shift details
    val attendanceHistory by viewModel.attendanceHistoryMonthToDate.collectAsState() // Collected month-to-date attendance

    // Automatically trigger Shift API when Shift tab is chosen
    LaunchedEffect(selectedDashboardTab, myProfile) {
        if (selectedDashboardTab == "Shift") {
            val shiftId = myProfile?.shiftId
            if (!shiftId.isNullOrBlank()) {
                hrViewModel.fetchShiftDetail(shiftId)
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0,0,0,0),
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(
                title = "HR Portal",
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

            // Top Section Tabs (Rendered for My Space & Team)
            if (selectedSpace != "Org" && selectedSpace != "Team") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sections) { sec ->
                        val isSelected = selectedSection == sec
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
                                .background(if (isSelected) Primary else whiteBg)
                                .border(
                                    1.dp,
                                    if (isSelected) Primary else sectionBorder,
                                    RoundedCornerShape(tokens.cardCornerRadius * 0.6f)
                                )
                                .clickable { selectedSection = sec }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sec,
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) whiteBg else TextPrimary
                            )
                        }
                    }
                }

                // Sub Navigation Tabs if Dashboard is selected
                if (selectedSection == "Dashboard") {
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
                                    .clickable { selectedDashboardTab = tab }
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
                }
            }

            // Render Content by Space Selection
            when (selectedSpace) {
                "My Space" -> {
                    if (selectedSection == "Overview") {
                        MySpaceContent(
                            todayAttendance = todayAttendance,
                            onPunch = { viewModel.performPunchAction() },
                            onNavigateToLeave = onNavigateToLeave,
                            onNavigateToAttendance = onNavigateToAttendance,
                            onNavigateToShift = onNavigateToShift
                        )
                    } else {
                        // Switch content based on selected Dashboard sub-tab
                        when (selectedDashboardTab) {
                            "Profile" -> ProfileSubTabContent(profile = myProfile)
                            "Attendance" -> AttendanceSubTabContent(
                                todayAttendance = todayAttendance,
                                history = attendanceHistory
                            )
                            "Shift" -> ShiftSubTabContent(shift = shiftDetail)
                            "Approvals" -> ApprovalsSubTabContent()
                            "Leave" -> LeaveSubTabContent(onNavigateToLeave = onNavigateToLeave)
                            "Files" -> FilesSubTabContent()
                            "Time Logs" -> TimeLogsSubTabContent(todayAttendance = todayAttendance)
                            "Career History" -> CareerHistorySubTabContent(profile = myProfile)
                        }
                    }
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
// SUB-TAB 3: SHIFT CONTENT (/api/hr/shift/view-one/{id})
// =========================================================================

@Composable
private fun ShiftSubTabContent(shift: ShiftDetailData?) {
    val tokens = LocalAppTokens.current
    val s = shift

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                    Text("Current Assigned Shift", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                    StatusBadge(text = if (s?.status == true) "Active" else "Inactive", bgColor = greenBg, textColor = greentext)
                }

                InfoFieldGrid("Shift Name", s?.name ?: "", "Shift Timing", "${s?.startTime ?: ""} - ${s?.endTime ?: ""}")
                InfoFieldGrid("Shift Code", s?.shiftId ?: "", "Break Allocation", "${s?.breakDuration ?: 0} mins")
                InfoFieldGrid("Shift Type", s?.shiftType ?: "", "Working Days", s?.customWorkingDays?.joinToString(", ") ?: "Mon - Sat")
            }
        }

        // Shift Policy Rules
        Surface(
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = whiteBg,
            border = BorderStroke(1.dp, sectionBorder)
        ) {
            Column(
                modifier = Modifier.padding(tokens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("SHIFT POLICY RULES", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
                Text("• Advance Notice: Requests must be submitted at least 48 hours prior to shift.", fontSize = tokens.caption, color = TextSecondary)
                Text("• Allowed Swaps: Maximum of 3 shift adjustments permitted per month.", fontSize = tokens.caption, color = TextSecondary)
            }
        }
    }
}

// =========================================================================
// SUB-TABS 4-8: APPROVALS, LEAVE, FILES, TIME LOGS, CAREER HISTORY
// =========================================================================

@Composable
private fun ApprovalsSubTabContent() {
    val tokens = LocalAppTokens.current
    Surface(shape = RoundedCornerShape(tokens.cardCornerRadius), color = whiteBg, border = BorderStroke(1.dp, sectionBorder)) {
        Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("My Approvals", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
            Text("No pending requests awaiting approval.", fontSize = tokens.caption, color = mutedText)
        }
    }
}

@Composable
private fun LeaveSubTabContent(onNavigateToLeave: () -> Unit) {
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

@Composable
private fun FilesSubTabContent() {
    val tokens = LocalAppTokens.current
    Surface(shape = RoundedCornerShape(tokens.cardCornerRadius), color = whiteBg, border = BorderStroke(1.dp, sectionBorder)) {
        Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("My Files", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
            Text("No employee documents uploaded yet.", fontSize = tokens.caption, color = mutedText)
        }
    }
}

@Composable
private fun TimeLogsSubTabContent(todayAttendance: CheckInOutTodayData?) {
    val tokens = LocalAppTokens.current
    val t = todayAttendance
    Surface(shape = RoundedCornerShape(tokens.cardCornerRadius), color = whiteBg, border = BorderStroke(1.dp, sectionBorder)) {
        Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("PUNCH HISTORY", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
            if (t?.punches.isNullOrEmpty()) {
                Text("No punches recorded for today", fontSize = tokens.caption, color = mutedText)
            } else {
                t.punches.forEach { p ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatIsoTimeToAmPm(p.time), fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(if (p.type == "in") "Swipe In" else "Swipe Out", fontSize = tokens.caption, color = if (p.type == "in") greentext else redText)
                        Text(p.method.uppercase(), fontSize = tokens.label, color = headerGrey)
                    }
                }
            }
        }
    }
}

@Composable
private fun CareerHistorySubTabContent(profile: MyProfileData?) {
    val tokens = LocalAppTokens.current
    Surface(shape = RoundedCornerShape(tokens.cardCornerRadius), color = whiteBg, border = BorderStroke(1.dp, sectionBorder)) {
        Column(modifier = Modifier.padding(tokens.screenPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("My Career History", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
            Text("Joined on: ${profile?.doj?.take(10) ?: "—"}", fontSize = tokens.caption, color = TextSecondary)
            Text("Department: ${profile?.departmentId?.name ?: "Management"}", fontSize = tokens.caption, color = TextSecondary)
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

@Composable
private fun MySpaceContent(
    todayAttendance: CheckInOutTodayData?,
    onPunch: () -> Unit,
    onNavigateToLeave: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToShift: () -> Unit
) {
    val tokens = LocalAppTokens.current

    // Employee Check-In / Check-Out Punch Card
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
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(title_color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DK",
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = whiteBg
                )
            }

            Text(
                text = "DK",
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = title_color
            )
            Text(
                text = "Design Executive • EMP-0376",
                fontSize = tokens.caption,
                color = TextSecondary
            )

            val isCheckedIn = todayAttendance?.checkedIn == true
            StatusBadge(
                text = if (isCheckedIn) "In Office" else "Not Checked-in",
                bgColor = if (isCheckedIn) greenBg else light_grey,
                textColor = if (isCheckedIn) greentext else TextSecondary
            )

            Text(
                text = "First In: ${formatIsoTimeToAmPm(todayAttendance?.firstIn)} • Status: ${todayAttendance?.status?.uppercase() ?: "ABSENT"}",
                fontSize = tokens.caption,
                color = headerGrey
            )

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
                    text = if (isCheckedIn) "Check-out" else "Check-in",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = whiteBg
                )
            }
        }
    }

    // Today's Shift Card
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
                    text = "TODAY'S SHIFT",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                StatusBadge(
                    text = "Day Shift",
                    bgColor = primary_light,
                    textColor = Primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "09:00 AM – 06:00 PM",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                    Text(
                        text = "8h 00m • 1h Break",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                }
                TextButton(onClick = onNavigateToShift) {
                    Text("View Details", fontSize = tokens.caption, color = Primary)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp), tint = Primary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "EFFECTIVE",
                    value = "${todayAttendance?.totalWorkingMinutes ?: 0}m",
                    valueColor = TextPrimary
                )
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "LATE TIME",
                    value = "${todayAttendance?.lateMinutes ?: 0}m",
                    valueColor = if ((todayAttendance?.lateMinutes ?: 0) > 0) redText else greentext
                )
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "LAST CHECK-IN",
                    value = formatIsoTimeToAmPm(todayAttendance?.firstIn),
                    valueColor = TextPrimary
                )
            }
        }
    }

    // Today's Summary Card
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
                text = "TODAY'S SUMMARY",
                fontSize = tokens.caption,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            SummaryRow(label = "Effective Hours", value = "${(todayAttendance?.totalWorkingMinutes ?: 0) / 60}h ${(todayAttendance?.totalWorkingMinutes ?: 0) % 60}m")
            SummaryRow(label = "Late Time", value = "${todayAttendance?.lateMinutes ?: 0}m")
            SummaryRow(label = "Last Check-in", value = formatIsoTimeToAmPm(todayAttendance?.firstIn))
            SummaryRow(label = "Check-out", value = formatIsoTimeToAmPm(todayAttendance?.lastOut))
        }
    }

    // My Requests Card
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
                    color = Primary
                )
                Text(
                    text = "View All",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary,
                    modifier = Modifier.clickable { onNavigateToLeave() }
                )
            }

            RequestItemRow(
                icon = Icons.Default.Description,
                title = "Leave Request",
                subtitle = "Casual Leave",
                badgeText = "Pending",
                badgeBg = yellowBg,
                badgeTextCol = yellowText
            )
            RequestItemRow(
                icon = Icons.AutoMirrored.Filled.Assignment,
                title = "Regularization",
                subtitle = "30 May 2025",
                badgeText = "Approved",
                badgeBg = greenBg,
                badgeTextCol = greentext
            )
            RequestItemRow(
                icon = Icons.Default.Schedule,
                title = "Permission",
                subtitle = "Personal Outing",
                badgeText = "Rejected",
                badgeBg = redBg,
                badgeTextCol = redText
            )
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