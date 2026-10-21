@file:Suppress(
    "UNUSED_VALUE", "SpellCheckingInspection", "GrazieInspection",
    "AssignedValueIsNeverRead", "unused_variable", "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)
package com.cuso.tailor.view.composable

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.utils.AppLoadingManager
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────
// PALETTE (shared by the date picker and the time picker)
// ─────────────────────────────────────────────────────────────

// Holds every color used by the picker dialogs.
// The values below are the exact colors from the light / dark design.
data class DatePickerPalette(
    val surface: Color,       // Dialog background
    val text: Color,          // Primary text (dates, hour/minute digits)
    val subtext: Color,       // Weekday letters, "SELECT DATE" label, unselected PM/AM
    val accent: Color,        // Selected day, selector, OK button
    val onAccent: Color,      // Text on top of accent
    val accentSoft: Color,    // Tint behind selected hour box / selected AM-PM / pencil button
    val onAccentSoft: Color,  // Text on top of accentSoft
    val link: Color,          // Cancel text, today ring, pencil icon
    val icon: Color,          // Chevrons, keyboard icon
    val divider: Color,       // Thin divider line
    val outline: Color,       // Borders (text field, AM/PM outline)
    val fieldFill: Color      // Unselected minute box, clock dial face
)

// Same palette for the time picker
typealias TimePickerPalette = DatePickerPalette

private val LightPickerPalette = DatePickerPalette(
    surface = Color(0xFFFFFFFF),
    text = Color(0xFF1F2430),
    subtext = Color(0xFF6B7280),
    accent = Color(0xFF4F46E5),
    onAccent = Color(0xFFFFFFFF),
    accentSoft = Color(0xFFE0E7FF),
    onAccentSoft = Color(0xFF3730A3),
    link = Color(0xFF4F46E5),
    icon = Color(0xFF374151),
    divider = Color(0xFFE5E7EB),
    outline = Color(0xFFD1D5DB),
    fieldFill = Color(0xFFF3F4F6)
)

private val DarkPickerPalette = DatePickerPalette(
    surface = Color(0xFF1E2028),
    text = Color(0xFFECEEF3),
    subtext = Color(0xFF9AA0AE),
    accent = Color(0xFF5B54EA),
    onAccent = Color(0xFFFFFFFF),
    accentSoft = Color(0xFF2B2E45),
    onAccentSoft = Color(0xFFC7D2FE),
    link = Color(0xFFA5B4FC),
    icon = Color(0xFFD1D5DB),
    divider = Color(0xFF2E313B),
    outline = Color(0xFF3A3E4C),
    fieldFill = Color(0xFF2A2D3A)
)

// Picks light or dark based on the app's current MaterialTheme surface,
// so it follows the app theme (not just the system setting).
@Composable
private fun rememberDatePickerPalette(): DatePickerPalette {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return if (isDark) DarkPickerPalette else LightPickerPalette
}

private val MonthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

private fun daysInMonth(year: Int, month: Int): Int =
    Calendar.getInstance().apply {
        clear()
        set(year, month, 1)
    }.getActualMaximum(Calendar.DAY_OF_MONTH)

// ─────────────────────────────────────────────────────────────
// DATE PICKER
// ─────────────────────────────────────────────────────────────

@Composable
fun DatePickerField(
    value: String,
    onDateSelected: (String) -> Unit,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    val tokens = LocalAppTokens.current
    var showPicker by remember { mutableStateOf(false) }

    // Combine the caller's enabled flag with the global "app busy" state,
    // so this field automatically disables whenever any API call
    // (create, update, etc.) is in flight app-wide.
    val isAppBusy by AppLoadingManager.busyState.collectAsState()
    val effectiveEnabled = enabled && !isAppBusy

    val palette = rememberDatePickerPalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (effectiveEnabled) 1f else 0.6f)
            .then(
                if (isError) Modifier.border(1.dp, redText, RoundedCornerShape(8.dp))
                else Modifier
            )
    ) {
        FormDateField(
            value = value,
            palette = palette,
            onClick = { if (effectiveEnabled) showPicker = true }
        )

        if (!effectiveEnabled) {
            // Invisible overlay that blocks clicks when the field is disabled
            Box(modifier = Modifier.matchParentSize().clickable(enabled = false) {})
        }
    }

    if (showPicker && effectiveEnabled) {
        CustomDatePickerDialog(
            palette = palette,
            initialDate = value,
            onDismiss = { showPicker = false },
            onConfirm = { day, month, year ->
                onDateSelected(String.format(Locale.US, "%02d-%02d-%04d", day, month, year))
                showPicker = false
            }
        )
    }
}

@Composable
private fun CustomDatePickerDialog(
    palette: DatePickerPalette,
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (day: Int, month: Int, year: Int) -> Unit
) {
    val tokens = LocalAppTokens.current
    val today = remember { Calendar.getInstance() }

    // If the field already has a valid date (DD-MM-YYYY), open on that date.
    val initial = remember(initialDate) {
        validateAndParseDate(initialDate.filter { it.isDigit() })
    }

    var displayMonth by remember {
        mutableIntStateOf(initial?.second?.minus(1) ?: today.get(Calendar.MONTH))
    }
    var displayYear by remember {
        mutableIntStateOf(initial?.third ?: today.get(Calendar.YEAR))
    }
    var selectedDay by remember {
        mutableIntStateOf(initial?.first ?: today.get(Calendar.DAY_OF_MONTH))
    }

    var isManualEntry by remember { mutableStateOf(false) }
    var manualDigits by remember { mutableStateOf(initialDate.filter { it.isDigit() }) }

    // --- Error State ---
    var manualError by remember { mutableStateOf<String?>(null) }

    // Moves the calendar by one month and keeps the selected day valid
    // (e.g. 31 Jan -> Feb becomes 28/29 Feb instead of an invalid date).
    fun shiftMonth(delta: Int) {
        var m = displayMonth + delta
        var y = displayYear
        if (m < 0) { m = 11; y-- } else if (m > 11) { m = 0; y++ }
        displayMonth = m
        displayYear = y
        selectedDay = selectedDay.coerceAtMost(daysInMonth(y, m))
    }

    val headerText = remember(displayYear, displayMonth, selectedDay) {
        val c = Calendar.getInstance().apply {
            clear()
            set(displayYear, displayMonth, selectedDay)
        }
        SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(c.time)
    }

    val todayDay: Int? =
        if (displayYear == today.get(Calendar.YEAR) && displayMonth == today.get(Calendar.MONTH))
            today.get(Calendar.DAY_OF_MONTH)
        else null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.width(342.dp),
            shape = RoundedCornerShape(28.dp),
            color = palette.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Header: small label + big selected date + edit/calendar toggle
                Text(
                    text = if (isManualEntry) "ENTER DATE" else "SELECT DATE",
                    color = palette.subtext,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isManualEntry) "Type a date" else headerText,
                        color = palette.text,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = {
                            isManualEntry = !isManualEntry
                            manualError = null // Clear any error when switching modes
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(palette.accentSoft, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isManualEntry) Icons.Default.CalendarMonth else Icons.Default.Edit,
                            contentDescription = if (isManualEntry) "Switch to calendar" else "Switch to text input",
                            tint = palette.link,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
                    color = palette.divider
                )

                if (isManualEntry) {
                    Column {
                        OutlinedTextField(
                            value = manualDigits,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                if (filtered.length <= 8) {
                                    manualDigits = filtered
                                    manualError = null // Clear error while typing
                                }
                            },
                            placeholder = { Text("DD-MM-YYYY", color = palette.subtext) },
                            singleLine = true,
                            isError = manualError != null, // Border turns red when validation fails
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = DateTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.accent,
                                unfocusedBorderColor = palette.outline,
                                errorBorderColor = redText,
                                focusedTextColor = palette.text,
                                unfocusedTextColor = palette.text,
                                cursorColor = palette.accent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Show error message below the field, or the format hint if no error
                        if (manualError != null) {
                            Text(
                                text = manualError!!,
                                color = redText,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        } else {
                            Text(
                                "Format: DD-MM-YYYY",
                                fontSize = tokens.label,
                                color = palette.subtext,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }
                } else {
                    DatePickerCalendarGrid(
                        palette = palette,
                        displayMonth = displayMonth,
                        displayYear = displayYear,
                        selectedDay = selectedDay,
                        todayDay = todayDay,
                        onDaySelected = { selectedDay = it },
                        onPrevMonth = { shiftMonth(-1) },
                        onNextMonth = { shiftMonth(1) }
                    )
                }

                Spacer(Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            "Cancel",
                            color = palette.link,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (isManualEntry) {
                                if (manualDigits.length < 8) {
                                    manualError = "Incomplete date"
                                } else {
                                    val result = validateAndParseDate(manualDigits)
                                    if (result != null) {
                                        onConfirm(result.first, result.second, result.third)
                                    } else {
                                        manualError = "Invalid date entered" // Example: 32-13-2024
                                    }
                                }
                            } else {
                                onConfirm(selectedDay, displayMonth + 1, displayYear)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.accent,
                            contentColor = palette.onAccent
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 28.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            "OK",
                            color = palette.onAccent,
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Strict validation: returns null if the entered date is invalid.
 */
private fun validateAndParseDate(digits: String): Triple<Int, Int, Int>? {
    if (digits.length != 8) return null
    return try {
        val d = digits.substring(0, 2).toInt()
        val m = digits.substring(2, 4).toInt()
        val y = digits.substring(4, 8).toInt()

        // Strict check: disallow rollover dates (e.g. 32 Jan -> Feb 1)
        val cal = Calendar.getInstance()
        cal.isLenient = false
        cal.set(Calendar.YEAR, y)
        cal.set(Calendar.MONTH, m - 1)
        cal.set(Calendar.DAY_OF_MONTH, d)

        cal.time // Triggers an exception if the date is invalid

        if (y !in 1900..2100) return null // Reasonable year range

        Triple(d, m, y)
    } catch (e: Exception) {
        null
    }
}

/**
 * Visual transformation that inserts dashes as the user types digits (DD-MM-YYYY).
 */
class DateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0, 8) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 3) out += "-"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset + 1
                if (offset <= 8) return offset + 2
                return out.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                if (offset <= 10) return offset - 2
                return trimmed.length
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

// The clickable field shown on the form (before the picker dialog opens).
// Uses the app's static colors (not the picker palette), unchanged from before.
@Composable
fun FormDateField(
    value: String,
    onClick: () -> Unit,
    palette: DatePickerPalette = rememberDatePickerPalette()
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(whiteBg, RoundedCornerShape(8.dp))
            .border(1.dp, grey_border, RoundedCornerShape(8.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value.ifEmpty { "dd-mm-yyyy" },
            fontSize = 14.sp,
            color = if (value.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF374151),
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(R.drawable.ic_calendar_2),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DatePickerCalendarGrid(
    palette: DatePickerPalette,
    displayMonth: Int,
    displayYear: Int,
    selectedDay: Int,
    todayDay: Int?,
    onDaySelected: (Int) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    // Month title + prev/next chevrons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${MonthNames[displayMonth]} $displayYear",
            color = palette.text,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Row {
            IconButton(onClick = onPrevMonth, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = palette.icon)
            }
            IconButton(onClick = onNextMonth, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = palette.icon)
            }
        }
    }

    Spacer(Modifier.height(4.dp))

    // Weekday labels
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(d, color = palette.subtext, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    val calendar = Calendar.getInstance().apply {
        clear()
        set(displayYear, displayMonth, 1)
    }
    val firstDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Build the full grid: leading blanks for offset, then all days of the month
    val days = buildList<Int?> {
        repeat(firstDay) { add(null) }
        for (i in 1..maxDays) add(i)
    }

    days.chunked(7).forEach { week ->
        Row(modifier = Modifier.fillMaxWidth()) {
            week.forEach { day ->
                val isSelected = day == selectedDay
                val isToday = day != null && day == todayDay && !isSelected

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) palette.accent else Color.Transparent)
                        .then(
                            if (isToday) Modifier.border(1.5.dp, palette.link, CircleShape)
                            else Modifier
                        )
                        .clickable(enabled = day != null) { day?.let(onDaySelected) },
                    contentAlignment = Alignment.Center
                ) {
                    if (day != null) {
                        Text(
                            text = day.toString(),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                isSelected -> palette.onAccent
                                isToday -> palette.link
                                else -> palette.text
                            }
                        )
                    }
                }
            }
            if (week.size < 7) repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TIME PICKER
// ─────────────────────────────────────────────────────────────

// Reads "hh:mm AM/PM" (or "HH:mm") into a 24-hour hour + minute.
// Falls back to 10:53 when the value is empty or unreadable.
private fun parseTimeOrDefault(value: String): Pair<Int, Int> {
    if (value.isEmpty()) return Pair(10, 53)

    val hour24 = try {
        val hour = value.substringBefore(":").trim().toInt()
        when {
            value.contains("PM", ignoreCase = true) && hour != 12 -> hour + 12
            value.contains("AM", ignoreCase = true) && hour == 12 -> 0
            else -> hour
        }
    } catch (_: Exception) { 10 }

    val minute = try {
        val parts = value.split(":")
        if (parts.size >= 2) parts[1].take(2).toInt() else 0
    } catch (_: Exception) { 53 }

    return Pair(hour24.coerceIn(0, 23), minute.coerceIn(0, 59))
}

@Composable
fun TimePickerField(
    value: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    // Allows callers to explicitly disable this field (e.g. based on
    // form validation state), in addition to the automatic global busy check below.
    enabled: Boolean = true
) {
    val tokens = LocalAppTokens.current
    var showPicker by remember { mutableStateOf(false) }

    // Combine the caller's enabled flag with the global "app busy" state,
    // so this field automatically disables whenever any API call
    // (create, update, etc.) is in flight app-wide.
    val isAppBusy by AppLoadingManager.busyState.collectAsState()
    val effectiveEnabled = enabled && !isAppBusy

    val palette = rememberDatePickerPalette()

    // This field row uses the app's static colors (not the picker palette), unchanged from before
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (effectiveEnabled) 1f else 0.6f) // visually dim when disabled
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .border(1.dp, PrimaryBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .clickable(enabled = effectiveEnabled) { showPicker = true } // block click when disabled
            .padding(
                horizontal = tokens.cardPadding * 0.6f,
                vertical = tokens.screenPadding * 0.375f
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value.ifEmpty { "Select Time" },
            fontSize = tokens.bodyMedium,
            color = if (value.isEmpty()) Color(0xFF9CA3AF) else Color(0xFF374151)
        )
        Icon(
            Icons.Default.Schedule,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(tokens.iconSize)
        )
    }

    if (showPicker && effectiveEnabled) { // dialog only opens when enabled
        val initial = remember(value) { parseTimeOrDefault(value) }

        CustomTimePickerDialog(
            palette = palette,
            initialHour = initial.first,
            initialMinute = initial.second,
            onDismiss = { showPicker = false },
            onConfirm = { hour24, minute ->
                val displayHour = when {
                    hour24 == 0 -> 12
                    hour24 > 12 -> hour24 - 12
                    else -> hour24
                }
                val amPm = if (hour24 < 12) "AM" else "PM"
                onTimeSelected(String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm))
                showPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTimePickerDialog(
    palette: TimePickerPalette,
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour24: Int, minute: Int) -> Unit
) {
    val tokens = LocalAppTokens.current
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )
    var showDial by remember { mutableStateOf(true) }

    val colors = TimePickerDefaults.colors(
        // Clock dial
        clockDialColor = palette.fieldFill,
        clockDialSelectedContentColor = palette.onAccent,
        clockDialUnselectedContentColor = palette.text,
        selectorColor = palette.accent,
        containerColor = palette.surface,
        // AM / PM toggle
        periodSelectorBorderColor = palette.outline,
        periodSelectorSelectedContainerColor = palette.accentSoft,
        periodSelectorUnselectedContainerColor = Color.Transparent,
        periodSelectorSelectedContentColor = palette.onAccentSoft,
        periodSelectorUnselectedContentColor = palette.subtext,
        // Hour / minute boxes
        timeSelectorSelectedContainerColor = palette.accentSoft,
        timeSelectorUnselectedContainerColor = palette.fieldFill,
        timeSelectorSelectedContentColor = palette.onAccentSoft,
        timeSelectorUnselectedContentColor = palette.text
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.width(342.dp),
            shape = RoundedCornerShape(28.dp),
            color = palette.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "SELECT TIME",
                    color = palette.subtext,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Spacer(Modifier.height(14.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (showDial) {
                        TimePicker(state = state, colors = colors)
                    } else {
                        TimeInput(state = state, colors = colors)
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Keyboard <-> dial toggle
                    IconButton(onClick = { showDial = !showDial }) {
                        Icon(
                            imageVector = if (showDial) Icons.Default.Keyboard else Icons.Default.Schedule,
                            contentDescription = if (showDial) "Switch to keyboard input" else "Switch to clock",
                            tint = palette.icon
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                "Cancel",
                                color = palette.link,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { onConfirm(state.hour, state.minute) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.accent,
                                contentColor = palette.onAccent
                            ),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 28.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text(
                                "OK",
                                color = palette.onAccent,
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}