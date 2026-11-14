@file:Suppress("unused")

package com.cuso.tailor.view.home.reports.hr.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.purpleBg
import com.cuso.tailor.ui.theme.purpleText
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.yellowBg
import com.cuso.tailor.ui.theme.yellowText
import com.cuso.tailor.utils.DynamicIslandManager
import com.cuso.tailor.view.composable.DataCard
import com.cuso.tailor.view.composable.FilterOption
import com.cuso.tailor.view.composable.FilterSection
import com.cuso.tailor.view.composable.MenuAction
import com.cuso.tailor.view.home.reports.ReportCardFooterCaption
import com.cuso.tailor.view.home.reports.ReportDetailGridBox
import com.cuso.tailor.view.home.reports.ReportGridCell
import com.cuso.tailor.view.home.reports.ReportInfinitelyPaginatedScaffold
import com.cuso.tailor.view.home.reports.ReportInlineMetricRow
import com.cuso.tailor.view.home.reports.ReportMetricSpec
import com.cuso.tailor.viewmodel.ReportViewModel
import com.cuso.tailor.model.report.AttendanceRecordDto

@Composable
fun AttendanceReportScreen(
    onClose: () -> Unit,
    onViewDetail: (AttendanceRecordDto) -> Unit = {},
    viewModel: ReportViewModel = hiltViewModel()
) {
    val tokens = LocalAppTokens.current
    val analytics by viewModel.attendanceAnalytics.collectAsState()
    val pagingState by viewModel.attendancePagingState.collectAsState()
    val filteredList by viewModel.filteredAttendanceList.collectAsState()
    val searchQuery by viewModel.attendanceSearchQuery.collectAsState()

    var filterSections by remember {
        mutableStateOf(
            listOf(
                FilterSection(
                    title = "Attendance Status",
                    options = listOf(
                        FilterOption("present", "Present"),
                        FilterOption("absent", "Absent"),
                        FilterOption("on_leave", "On Leave"),
                        FilterOption("half_day_leave", "Half Day")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Work Mode",
                    options = listOf(
                        FilterOption("office", "Office"),
                        FilterOption("remote", "Remote"),
                        FilterOption("field", "Field")
                    ),
                    isMultiSelect = true
                ),
                FilterSection(
                    title = "Flags",
                    options = listOf(
                        FilterOption("late_only", "Late Arrivals Only")
                    ),
                    isMultiSelect = true
                )
            )
        )
    }

    val activeFilterCount = remember(filterSections) {
        filterSections.sumOf { section -> section.options.count { it.isSelected } }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAttendanceReport()
    }

    val summary = analytics?.attendanceSummary
    val metrics = remember(summary) {
        listOf(
            ReportMetricSpec(
                label = "TOTAL PUNCH LOGS",
                value = (summary?.totalRecords ?: 0).toString(),
                subtitle = "In selected period",
                icon = Icons.Default.Group,
                valueColor = TextPrimary,
                iconTint = Primary,
                iconBgColor = primary_light
            ),
            ReportMetricSpec(
                label = "ATTENDANCE RATE",
                value = summary?.attendanceRate ?: "0%",
                subtitle = "${summary?.presentCount ?: 0} Present records",
                icon = Icons.Default.CheckCircleOutline,
                valueColor = greentext,
                subtitleColor = greentext,
                iconTint = greentext,
                iconBgColor = greenBg
            ),
            ReportMetricSpec(
                label = "LATE ARRIVALS",
                value = (summary?.lateCount ?: 0).toString(),
                subtitle = "${summary?.totalLateMinutes ?: 0} total late mins",
                icon = Icons.Default.WarningAmber,
                valueColor = yellowText,
                subtitleColor = yellowText,
                iconTint = yellowText,
                iconBgColor = yellowBg
            ),
            ReportMetricSpec(
                label = "OVERTIME ACCUMULATION",
                value = viewModel.formatHoursFromMinutesZeroAllowed(summary?.totalOvertimeMinutes ?: 0),
                subtitle = "Overtime worked",
                icon = Icons.Default.AccessTime,
                valueColor = purpleText,
                subtitleColor = purpleText,
                iconTint = purpleText,
                iconBgColor = purpleBg
            )
        )
    }

    ReportInfinitelyPaginatedScaffold(
        title = "Attendance Register & Analytics Report",
        subtitle = analytics?.filterApplied?.monthLabel?.takeIf { it.isNotBlank() },
        sectionTitle = "Attendance Punch Ledger",
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onAttendanceSearchQueryChange,
        searchPlaceholder = "Search by employee or code...",
        metrics = metrics,
        pagingState = pagingState,
        filteredItems = filteredList,
        itemKey = { it.id },
        filterSections = filterSections,
        activeFilterCount = activeFilterCount,
        onApplyFilters = { updated ->
            filterSections = updated
            val statuses = updated.find { it.title == "Attendance Status" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val modes = updated.find { it.title == "Work Mode" }
                ?.options?.filter { it.isSelected }?.map { it.id }?.toSet() ?: emptySet()
            val lateOnly = updated.find { it.title == "Flags" }
                ?.options?.any { it.id == "late_only" && it.isSelected } ?: false
            viewModel.applyAttendanceFilters(statuses, modes, lateOnly)
        },
        onClearAllFilters = {
            filterSections = filterSections.map { sec ->
                sec.copy(options = sec.options.map { it.copy(isSelected = false) })
            }
            viewModel.clearAttendanceFilters()
        },
        onLoadMore = viewModel::loadMoreAttendanceRecords,
        onShareClick = { DynamicIslandManager.showSuccess("Attendance report link ready to share") },
        onExportClick = { DynamicIslandManager.showSuccess("Attendance report export started") },
        onClose = onClose,
        emptyMessage = "No attendance records found."
    ) { item ->
        val isPresent = item.status.equals("present", ignoreCase = true)
        val badgeTextColor = if (isPresent) greentext else redText
        val badgeBgColor = if (isPresent) greenBg else redBg
        val formattedDate = viewModel.formatDate(item.date)
        val empCode = item.employeeCode.ifBlank { "--" }

        DataCard(
            item = item,
            title = item.employeeName,
            subtitle = "$empCode  •  Date: $formattedDate",
            topBadgeText = item.status.uppercase(),
            topBadgeTextColor = badgeTextColor,
            topBadgeBgColor = badgeBgColor,
            topBadgeInline = true,
            actions = listOf(
                MenuAction(
                    label = "View Details",
                    icon = Icons.Default.Visibility,
                    onClick = { onViewDetail(item) }
                )
            ),
            onClick = onViewDetail,
            content = {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    ReportInlineMetricRow(label = "Attendance Flag") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.6f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.isLate && item.lateMinutes > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                        .background(redBg)
                                        .padding(
                                            horizontal = tokens.extraPadding * 0.8f,
                                            vertical = tokens.extraPadding * 0.25f
                                        )
                                ) {
                                    Text(
                                        text = "Late by ${item.lateMinutes}m",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = redText
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                    .background(grey_border)
                                    .padding(
                                        horizontal = tokens.extraPadding * 0.8f,
                                        vertical = tokens.extraPadding * 0.25f
                                    )
                            ) {
                                Text(
                                    text = item.workMode.uppercase().ifBlank { "--" },
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    ReportDetailGridBox(
                        rows = listOf(
                            listOf(
                                ReportGridCell("First Punch In", viewModel.formatTime(item.firstIn)),
                                ReportGridCell("Last Punch Out", viewModel.formatTime(item.lastOut)),
                                ReportGridCell("Work Hours", viewModel.formatMinutesToHours(item.totalWorkingMinutes))
                            ),
                            listOf(
                                ReportGridCell("Overtime", viewModel.formatMinutesToHours(item.overtimeMinutes)),
                                ReportGridCell("Mode", item.workMode.uppercase().ifBlank { "--" }),
                                ReportGridCell("Date", formattedDate)
                            )
                        )
                    )

                    Spacer(Modifier.height(tokens.extraPadding * 0.2f))
                    ReportCardFooterCaption(text = "Logged on ${viewModel.formatDate(item.createdAt)}")
                }
            }
        )
    }
}