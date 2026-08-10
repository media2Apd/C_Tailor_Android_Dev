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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.greentext
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.redBg
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.ScreenBreadcrumb
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.SheetValue
import com.cuso.mobile.view.composable.SmoothBottomSheet
import com.cuso.mobile.view.composable.blurScrim

// ── Design tokens ──
private val HrPrimary = Primary
private val CriticalRed = redBg
private val WarningOrange = Color(0xFFF59E0B)
private val HealthyGreen = greentext
private val TitleColor = title_color
private val MutedColor = mutedText
private val BorderColor = PrimaryBorder

// ── Data Models ──
private data class AttendanceStat(
    val label: String,
    val value: String,
    val valueColor: Color = TitleColor
)

private enum class AttendanceStatus { PRESENT, ABSENT, LATE, ON_LEAVE }

private data class AttendanceRecord(
    val id: String,
    val name: String,
    val empId: String,
    val designation: String,
    val status: AttendanceStatus,
    val checkIn: String,
    val checkOut: String,
    val workingHrs: String
)

private fun statusColor(status: AttendanceStatus): Color = when (status) {
    AttendanceStatus.PRESENT -> HealthyGreen
    AttendanceStatus.ABSENT -> CriticalRed
    AttendanceStatus.LATE -> WarningOrange
    AttendanceStatus.ON_LEAVE -> HrPrimary
}

private fun statusLabel(status: AttendanceStatus): String = when (status) {
    AttendanceStatus.PRESENT -> "Present"
    AttendanceStatus.ABSENT -> "Absent"
    AttendanceStatus.LATE -> "Late"
    AttendanceStatus.ON_LEAVE -> "On Leave"
}

private fun sampleAttendance(): List<AttendanceRecord> = listOf(
    AttendanceRecord("1", "John Anderson", "EMP-1018", "Senior Software Engineer", AttendanceStatus.PRESENT, "09:00 Am", "06:15 PM", "09h 13m"),
    AttendanceRecord("2", "John Anderson", "EMP-1018", "Senior Software Engineer", AttendanceStatus.ABSENT, "-", "-", "-"),
    AttendanceRecord("3", "John Anderson", "EMP-1018", "Senior Software Engineer", AttendanceStatus.LATE, "10:42 Am", "06:15 PM", "07h 00m"),
    AttendanceRecord("4", "John Anderson", "EMP-1018", "Senior Software Engineer", AttendanceStatus.PRESENT, "09:00 Am", "06:15 PM", "09h 13m")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    onClose: () -> Unit,
    onBreadCrumbClick: () -> Unit = {},
    onRecordClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var searchQuery by remember { mutableStateOf("") }
    var exportSheetState by remember { mutableStateOf(SheetValue.Hidden) }
    var exportBlur by remember { mutableStateOf(0.dp) }

    val allRecords = remember { sampleAttendance() }
    val filteredRecords = remember(searchQuery, allRecords) {
        if (searchQuery.isBlank()) allRecords
        else allRecords.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.empId.contains(searchQuery, ignoreCase = true)
        }
    }

    val stats = listOf(
        AttendanceStat("Present", "128", HealthyGreen),
        AttendanceStat("Absent", "6", CriticalRed),
        AttendanceStat("Late", "8"),
        AttendanceStat("On Leave", "12", HrPrimary)
    )

    Box(modifier = Modifier.fillMaxSize()) {
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

            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Box(modifier = Modifier.fillMaxSize().blurScrim(exportBlur)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().background(Color.Transparent),
                        contentPadding = PaddingValues(bottom = tokens.screenPadding * 1.5f)
                    ) {
                        // ── Breadcrumb + Export ──
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ScreenBreadcrumb(listOf("HR", "Attendance"), onClick = { onBreadCrumbClick() })
                                }
                                Row(
                                    modifier = Modifier
                                        .clickable { exportSheetState = SheetValue.Collapsed }
                                        .padding(horizontal = tokens.screenPadding / 2, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.FileUpload, contentDescription = "Export", tint = HrPrimary, modifier = Modifier.size(tokens.iconSize))
                                    Text("Export", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = HrPrimary)
                                }
                            }
                        }

                        // ── Stat grid ──
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                stats.chunked(2).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                        row.forEach { stat -> AttendanceStatCard(stat, Modifier.weight(1f)) }
                                    }
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                        }

                        // ── Search + filter ──
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(tokens.fieldHeight)
                                        .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                                        .padding(horizontal = tokens.screenPadding * 0.85f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = MutedColor, modifier = Modifier.size(tokens.iconSize))
                                    Spacer(Modifier.width(8.dp))
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = TextStyle(fontSize = tokens.bodyMedium, color = TitleColor),
                                        decorationBox = { inner ->
                                            if (searchQuery.isEmpty()) {
                                                Text("Search Employee Name or ID", fontSize = tokens.bodyMedium, color = MutedColor)
                                            }
                                            inner()
                                        }
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(tokens.fieldHeight)
                                        .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius / 1.5f))
                                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(tokens.cardCornerRadius / 1.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = TitleColor, modifier = Modifier.size(tokens.iconSize))
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }

                        // ── Section title ──
                        item {
                            Row(modifier = Modifier.fillMaxWidth().background(whiteBg).padding(horizontal = tokens.screenPadding, vertical = 10.dp)) {
                                Text("Today's Attendance", fontSize = tokens.bodyLarge,  color = TitleColor)
                            }
                            Spacer(Modifier.height(6.dp))
                        }

                        // ── List ──
                        items(filteredRecords, key = { it.id }) { record ->
                            AttendanceCard(record, onClick = { onRecordClick(record.id) })
                        }
                    }
                }

                SmoothBottomSheet(
                    state = exportSheetState,
                    onStateChange = { exportSheetState = it },
                    peekHeight = 340.dp,
                    topInset = 0.dp,
                    maxBlurRadius = 16.dp,
                    maxScrimAlpha = 0.45f,
                    scrollableContent = false,
                    onBlurScrimChange = { blur, _ -> exportBlur = blur },
                    onDismissRequest = { exportSheetState = SheetValue.Hidden }
                ) {
                    ExportAttendanceSheetContent(onDismiss = { exportSheetState = SheetValue.Hidden })
                }
            }
        }
    }
}

@Composable
private fun AttendanceStatCard(stat: AttendanceStat, modifier: Modifier = Modifier) {
    val tokens = LocalAppTokens.current
    Column(modifier = modifier.background(whiteBg).padding(tokens.screenPadding * 0.85f)) {
        Text(stat.label, fontSize = tokens.caption, color = MutedColor)
        Spacer(Modifier.height(8.dp))
        Text(stat.value, fontSize = tokens.bodyLarge, color = stat.valueColor)
    }
}

@Composable
private fun AttendanceCard(record: AttendanceRecord, onClick: () -> Unit) {
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
            Text(record.name, fontSize = tokens.bodyMedium, color = TitleColor)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AttendanceBadge(statusLabel(record.status), statusColor(record.status))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MutedColor, modifier = Modifier.size(tokens.iconSize))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "ID: ${record.empId} · Designation: ${record.designation}",
            fontSize = tokens.caption,
            color = MutedColor
        )
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

@Composable
private fun AttendanceBadge(text: String, color: Color) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun ExportAttendanceSheetContent(onDismiss: () -> Unit) {
    val tokens = LocalAppTokens.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = tokens.screenPadding)) {
        Text("Export Report", fontSize = tokens.bodyLarge, fontWeight = FontWeight.Bold, color = TitleColor)
        Text("Choose an export option", fontSize = tokens.caption, color = MutedColor)
        Spacer(Modifier.height(12.dp))
        listOf(
            Triple(Icons.Default.PictureAsPdf, Color(0xFFEF4444), "Export as PDF"),
            Triple(Icons.Default.GridOn, Color(0xFF16A34A), "Export as Excel"),
            Triple(Icons.Default.Share, Color(0xFF3B82F6), "Share Report"),
            Triple(Icons.Default.Email, Color(0xFF9333EA), "Send by Email")
        ).forEach { (icon, color, label) ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onDismiss).padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(tokens.fieldHeight * 0.8f)
                            .background(color.copy(alpha = 0.12f), RoundedCornerShape(tokens.cardCornerRadius / 1.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(tokens.iconSize))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(label, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TitleColor)
                }
                Icon(Icons.Default.ChevronRight, null, tint = MutedColor, modifier = Modifier.size(tokens.iconSize))
            }
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(tokens.buttonHeight),
            shape = RoundedCornerShape(tokens.cardCornerRadius / 1.5f)
        ) {
            Text("Cancel", fontSize = tokens.bodyMedium, color = TitleColor, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(20.dp))
    }
}