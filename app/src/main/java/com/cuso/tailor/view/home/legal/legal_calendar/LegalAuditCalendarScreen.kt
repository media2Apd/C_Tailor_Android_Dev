@file:Suppress("unused")

package com.cuso.tailor.view.home.legal.legal_calendar

import android.content.ContentValues
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.legal.CalendarSummary
import com.cuso.tailor.model.legal.LegalCalendarEvent
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.LegalViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun AuditLegalCalendarScreen(
    onNavigateBack: () -> Unit,
    onScheduleClick: () -> Unit = {},
    onEventClick: (LegalCalendarEvent) -> Unit = {},
    viewModel: LegalViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val tokens = LocalAppTokens.current
    val calendarData by viewModel.calendarData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoadingCalendar.collectAsStateWithLifecycle()

    // ── Current viewing calendar month ──
    val calendar = remember { Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US) }
    var currentYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }

    // Today's date components
    val todayCal = remember { Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US) }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    // Currently clicked day inside the grid (defaults to today)
    var selectedDateKey by remember {
        mutableStateOf(String.format(Locale.US, "%04d-%02d-%02d", todayYear, todayMonth + 1, todayDay))
    }

    // ── Trigger API request whenever Month or Year changes ──
    LaunchedEffect(currentYear, currentMonth) {
        val (fromIso, toIso) = calculateGridDateRange(currentYear, currentMonth)
        viewModel.fetchLegalCalendar(fromIso, toIso)
    }

    // Events grouped by date string (yyyy-MM-dd)
    val eventsByDate = remember(calendarData) {
        val map = mutableMapOf<String, MutableList<LegalCalendarEvent>>()
        calendarData?.events.orEmpty().forEach { event ->
            val dateKey = event.date?.take(10)
            if (!dateKey.isNullOrBlank()) {
                map.getOrPut(dateKey) { mutableListOf() }.add(event)
            }
        }
        map
    }

    // Urgent action items from API
    val urgentEvents = calendarData?.urgent.orEmpty()
    val summary = calendarData?.summary ?: CalendarSummary()

    // Events matching the currently clicked calendar day
    val selectedDayEvents = eventsByDate[selectedDateKey].orEmpty()

    // Header title for month
    val monthTitle = remember(currentYear, currentMonth) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
        }
        SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TitleBar(title = "Audit Calendar Details", onClose = onNavigateBack)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding),
            contentPadding = PaddingValues(vertical = tokens.extraPadding)
        ) {
            // ── Top Action Buttons: Filter, Refresh, Export, Schedule ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter dropdown button
                    Surface(
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        border = BorderStroke(1.dp, sectionBorder),
                        color = whiteBg,
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "All",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = headerGrey,
                                modifier = Modifier.size(tokens.iconSize)
                            )
                        }
                    }

                    // Refresh Button
                    Button(
                        onClick = {
                            val (fromIso, toIso) = calculateGridDateRange(currentYear, currentMonth)
                            viewModel.fetchLegalCalendar(fromIso, toIso)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = whiteBg, contentColor = TextPrimary),
                        border = BorderStroke(1.dp, sectionBorder),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = "Refresh",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Export Button
                    Button(
                        onClick = {
                            val events = calendarData?.events.orEmpty()
                            if (events.isEmpty()) {
                                Toast.makeText(context, "No events to export", Toast.LENGTH_SHORT).show()
                            } else {
                                exportCalendarToCsv(context, events)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = whiteBg, contentColor = TextPrimary),
                        border = BorderStroke(1.dp, sectionBorder),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        Text(
                            text = "Export",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Schedule Button
                    Button(
                        onClick = onScheduleClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                        modifier = Modifier.height(tokens.buttonHeight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Schedule",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }
            }

            // ── Stat Summary Cards Row ──
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        StatCard(
                            title = "Today",
                            count = summary.today.toString(),
                            subtitle = "Scheduled today",
                            icon = Icons.Outlined.CalendarToday,
                            iconColor = iconMuted
                        )
                    }
                    item {
                        StatCard(
                            title = "This Week",
                            count = summary.thisWeek.toString(),
                            subtitle = "End of week",
                            icon = Icons.Outlined.AccessTime,
                            iconColor = iconMuted
                        )
                    }
                    item {
                        StatCard(
                            title = "Next 30D",
                            count = summary.next30Days.toString(),
                            subtitle = "Preparation",
                            icon = Icons.Default.RemoveRedEye,
                            iconColor = iconMuted
                        )
                    }
                }
            }

            // ── Secondary Stat Cards (Overdue & Upcoming) ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Overdue alert card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = redBg.copy(alpha = 0.35f)),
                        border = BorderStroke(1.dp, redBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(tokens.screenPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Overdue",
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = redText
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(redText, CircleShape)
                                    )
                                }
                                Text(
                                    text = summary.overdue.toString(),
                                    fontSize = tokens.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = redText
                                )
                                Text(
                                    text = "Immediate action",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = redText.copy(alpha = 0.8f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(tokens.fieldHeight)
                                    .background(redBg, RoundedCornerShape(tokens.cardCornerRadius * 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = redText,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                        }
                    }

                    // Upcoming card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = whiteBg),
                        border = BorderStroke(1.dp, sectionBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(tokens.screenPadding),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Upcoming",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = summary.upcoming.toString(),
                                    fontSize = tokens.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Near future",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = mutedText
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(tokens.fieldHeight)
                                    .background(Primary_background, RoundedCornerShape(tokens.cardCornerRadius * 0.7f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = iconMuted,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                            }
                        }
                    }
                }
            }

            // ── Urgent Action Required Section ──
            if (urgentEvents.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(redText, CircleShape)
                        )
                        Text(
                            text = "URGENT — ACTION REQUIRED",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = BluePrimary
                        )
                    }
                }

                items(urgentEvents) { event ->
                    val isDeadline = event.eventType.equals("Deadline", ignoreCase = true)
                    val cardBg = if (isDeadline) redBg.copy(alpha = 0.25f) else orangeBg.copy(alpha = 0.4f)
                    val cardBorder = if (isDeadline) redBg else orangeBg
                    val dotColor = if (isDeadline) redText else orangeText

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(tokens.cardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(tokens.screenPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(dotColor, CircleShape)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title ?: "Untitled",
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = listOfNotNull(event.moduleLabel, event.eventType).joinToString(" • "),
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Due: ${formatDateDisplay(event.date)}",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = dotColor
                                )
                            }
                            OutlinedButton(
                                onClick = { onEventClick(event) },
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                                border = BorderStroke(1.dp, sectionBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = "View",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // ── Dynamic Interactive Calendar Card ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = whiteBg),
                    border = BorderStroke(1.dp, sectionBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(tokens.screenPadding)
                    ) {
                        // Month Header with Prev, Next, and Today controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = monthTitle,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = BluePrimary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Previous Month
                                Surface(
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    border = BorderStroke(1.dp, sectionBorder),
                                    color = whiteBg,
                                    modifier = Modifier
                                        .height(30.dp)
                                        .clickable {
                                            if (currentMonth == 0) {
                                                currentMonth = 11
                                                currentYear -= 1
                                            } else {
                                                currentMonth -= 1
                                            }
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = "‹ Prev",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                // Next Month
                                Surface(
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    border = BorderStroke(1.dp, sectionBorder),
                                    color = whiteBg,
                                    modifier = Modifier
                                        .height(30.dp)
                                        .clickable {
                                            if (currentMonth == 11) {
                                                currentMonth = 0
                                                currentYear += 1
                                            } else {
                                                currentMonth += 1
                                            }
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = "Next ›",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                // Jump to Today
                                Surface(
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    border = BorderStroke(1.dp, sectionBorder),
                                    color = whiteBg,
                                    modifier = Modifier
                                        .height(30.dp)
                                        .clickable {
                                            currentYear = todayYear
                                            currentMonth = todayMonth
                                            selectedDateKey = String.format(Locale.US, "%04d-%02d-%02d", todayYear, todayMonth + 1, todayDay)
                                        }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    ) {
                                        Text(
                                            text = "Today",
                                            fontSize = tokens.caption,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(tokens.extraPadding))

                        // Day of week labels (Sun - Sat)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
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

                        Spacer(Modifier.height(8.dp))

                        // ── Calendar Grid Generation (6 rows x 7 columns) ──
                        val calendarDays = remember(currentYear, currentMonth) {
                            generateCalendarGrid(currentYear, currentMonth)
                        }

                        calendarDays.chunked(7).forEach { week ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                week.forEach { cell ->
                                    val dateKey = cell.formattedDateKey
                                    val dayEvents = eventsByDate[dateKey].orEmpty()
                                    val isSelected = selectedDateKey == dateKey
                                    val isCurrentMonth = cell.isCurrentMonth
                                    val isToday = cell.year == todayYear && cell.month == todayMonth && cell.day == todayDay

                                    // Indicator dot styling
                                    val hasHighRisk = dayEvents.any { it.riskLevel.equals("High", ignoreCase = true) }
                                    val hasEvents = dayEvents.isNotEmpty()

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                            .clickable { selectedDateKey = dateKey }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(
                                                    color = when {
                                                        isToday -> BluePrimary
                                                        isSelected -> primary_light
                                                        else -> Color.Transparent
                                                    },
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = cell.day.toString(),
                                                fontSize = tokens.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = when {
                                                    isToday -> whiteBg
                                                    isSelected -> BluePrimary
                                                    isCurrentMonth -> TextPrimary
                                                    else -> iconMuted
                                                }
                                            )
                                        }

                                        // Indicator Dot
                                        Spacer(Modifier.height(2.dp))
                                        if (hasEvents) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .background(
                                                        if (hasHighRisk) redText else orangeText,
                                                        CircleShape
                                                    )
                                            )
                                        } else {
                                            Spacer(Modifier.height(5.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // ── Bottom Event Detail Cards for Selected Date ──
                        Spacer(Modifier.height(tokens.extraPadding))
                        HorizontalDivider(color = light_grey, thickness = 1.dp)
                        Spacer(Modifier.height(tokens.extraPadding))

                        if (selectedDayEvents.isEmpty()) {
                            Text(
                                text = "No events scheduled for this day.",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.Medium,
                                color = mutedText,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedDayEvents.forEach { event ->
                                    val isHigh = event.riskLevel.equals("High", ignoreCase = true)
                                    val cardBg = if (isHigh) redBg.copy(alpha = 0.25f) else yellowBg
                                    val dotColor = if (isHigh) redText else yellowText

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onEventClick(event) },
                                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f),
                                        colors = CardDefaults.cardColors(containerColor = cardBg)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .background(dotColor, CircleShape)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${formatDisplayDateShort(event.date)}: ${event.title ?: "Event"}",
                                                    fontSize = tokens.bodySmall,
                                                    fontWeight = FontWeight.Medium,
                                                    color = TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                                                border = BorderStroke(1.dp, dotColor.copy(alpha = 0.5f)),
                                                color = whiteBg
                                            ) {
                                                Text(
                                                    text = event.moduleLabel ?: event.eventType ?: "Event",
                                                    fontSize = tokens.caption,
                                                    fontWeight = FontWeight.Medium,
                                                    color = dotColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

            item {
                Spacer(Modifier.height(tokens.extraPadding))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Stat Card Composable
// ─────────────────────────────────────────────────────────────

@Composable
private fun StatCard(
    title: String,
    count: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color
) {
    val tokens = LocalAppTokens.current

    Card(
        modifier = Modifier.width(115.dp),
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        border = BorderStroke(1.dp, sectionBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(tokens.iconSize)
                )
            }
            Text(
                text = count,
                fontSize = tokens.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = tokens.caption,
                fontWeight = FontWeight.Medium,
                color = mutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Calendar Calculation Helpers
// ─────────────────────────────────────────────────────────────

data class CalendarGridCell(
    val day: Int,
    val month: Int,
    val year: Int,
    val isCurrentMonth: Boolean,
    val formattedDateKey: String
)

/**
 * Generates exact 42 cells (6 rows x 7 columns) starting on preceding Sunday
 */
private fun generateCalendarGrid(year: Int, month: Int): List<CalendarGridCell> {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
    val daysBefore = firstDayOfWeek - Calendar.SUNDAY

    cal.add(Calendar.DAY_OF_MONTH, -daysBefore)

    val cells = mutableListOf<CalendarGridCell>()
    for (i in 0 until 42) {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val dateKey = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)

        cells.add(
            CalendarGridCell(
                day = d,
                month = m,
                year = y,
                isCurrentMonth = m == month,
                formattedDateKey = dateKey
            )
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return cells
}

/**
 * Calculates ISO "from" and "to" queries matching the API payload range
 */
private fun calculateGridDateRange(year: Int, month: Int): Pair<String, String> {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US)
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val daysBefore = firstDayOfWeek - Calendar.SUNDAY

    cal.add(Calendar.DAY_OF_MONTH, -daysBefore)
    val fromIso = String.format(Locale.US, "%04d-%02d-%02dT00:00:00.000Z", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))

    cal.add(Calendar.DAY_OF_MONTH, 41)
    val toIso = String.format(Locale.US, "%04d-%02d-%02dT00:00:00.000Z", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))

    return Pair(fromIso, toIso)
}

// ─────────────────────────────────────────────────────────────
// CSV Export & Formatting Helpers
// ─────────────────────────────────────────────────────────────

/**
 * Generates CSV content and launches Android Share Sheet to open with Excel / Sheets
 */
// ─────────────────────────────────────────────────────────────
// CSV Direct Download to Device Downloads Folder
// ─────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────
// 200% PERMANENT FIX: CSV Export, Download & Instant Open
// ─────────────────────────────────────────────────────────────

private fun exportCalendarToCsv(context: Context, events: List<LegalCalendarEvent>) {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val fileName = "Legal_Calendar_$timeStamp.csv"

    try {
        // 1. Build CSV content with UTF-8 BOM (\uFEFF) so Excel never shows "Corrupted file"
        val csvBuilder = StringBuilder()
        csvBuilder.append("\uFEFF") // UTF-8 BOM
        csvBuilder.append("Date,Title,Number,Module,Event Typ,Status,Risk,Due\r\n")

        events.forEach { event ->
            val formattedDate = formatExportDate(event.date)
            val title = escapeCsv(event.title ?: "")
            val number = escapeCsv(event.number ?: "")
            val module = escapeCsv(event.moduleLabel ?: event.module ?: "")
            val eventType = escapeCsv(event.eventType ?: "")
            val status = escapeCsv(event.status ?: "")
            val risk = escapeCsv(event.riskLevel ?: "")
            val due = escapeCsv(calculateDueStatus(event.date, event.status))

            csvBuilder.append("$formattedDate,$title,$number,$module,$eventType,$status,$risk,$due\r\n")
        }

        val csvBytes = csvBuilder.toString().toByteArray(StandardCharsets.UTF_8)

        // 2. Write file to internal cache directory (Supported by FileProvider without permission errors)
        val cacheFile = File(context.cacheDir, fileName)
        FileOutputStream(cacheFile).use { fos ->
            fos.write(csvBytes)
            fos.flush()
        }

        // 3. Save copy directly to device Downloads folder (MediaStore API)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val downloadUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (downloadUri != null) {
                resolver.openOutputStream(downloadUri)?.use { os ->
                    os.write(csvBytes)
                    os.flush()
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(downloadUri, contentValues, null, null)
            }
        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val downloadFile = File(downloadsDir, fileName)
            FileOutputStream(downloadFile).use { fos ->
                fos.write(csvBytes)
                fos.flush()
            }
        }

        Toast.makeText(context, "Saved to Downloads: $fileName", Toast.LENGTH_SHORT).show()

        // 4. Generate content URI from cache file (guaranteed to match FileProvider configuration)
        val contentUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        // 5. Open directly in Excel / Google Sheets
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "text/csv")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(viewIntent, "Open with Excel or Sheets").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(chooser)

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Export error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

/**
 * Formats ISO date to "dd-MMM-yy" (e.g., 01-Oct-26)
 */
private fun formatExportDate(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(rawDate) ?: return rawDate.take(10)
        SimpleDateFormat("dd-MMM-yy", Locale.US).format(date)
    } catch (_: Exception) {
        rawDate.take(10)
    }
}

/**
 * Calculates due column value matching the reference table logic:
 * - InActive status -> "InActive"
 * - 0 days diff -> "Due today"
 * - Negative diff -> "Nd overdue"
 * - Positive diff -> "Nd remaining"
 */
private fun calculateDueStatus(rawDate: String?, status: String?): String {
    if (status.equals("InActive", ignoreCase = true)) {
        return "InActive"
    }

    if (rawDate.isNullOrBlank()) return ""

    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val eventDateOnly = parser.parse(rawDate.take(10)) ?: return ""

        // Normalize today's date to midnight UTC
        val todayCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayDateOnly = todayCal.time

        val diffInMillis = eventDateOnly.time - todayDateOnly.time
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        when {
            diffInDays == 0L -> "Due today"
            diffInDays < 0L -> "${Math.abs(diffInDays)}d overdue"
            else -> "${diffInDays}d remaining"
        }
    } catch (_: Exception) {
        ""
    }
}

/**
 * Escapes quotes and wraps strings with commas or newlines to maintain valid CSV syntax
 */
private fun escapeCsv(value: String): String {
    val clean = value.trim()
    var escaped = clean.replace("\"", "\"\"")
    if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
        escaped = "\"$escaped\""
    }
    return escaped
}

private fun formatDateDisplay(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(raw) ?: return raw.take(10)
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(date)
    } catch (_: Exception) {
        raw.take(10)
    }
}

private fun formatDisplayDateShort(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(raw) ?: return raw.take(10)
        SimpleDateFormat("MMM dd", Locale.US).format(date)
    } catch (_: Exception) {
        raw.take(10)
    }
}