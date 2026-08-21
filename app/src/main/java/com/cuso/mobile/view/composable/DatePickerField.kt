@file:Suppress(
    "UNUSED_VALUE", "SpellCheckingInspection", "GrazieInspection",
    "AssignedValueIsNeverRead", "unused_variable", "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)
package com.cuso.mobile.view.composable

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.utils.AppLoadingManager
import com.cuso.mobile.view.home.LeadPrimary
import java.util.*

// Holds all the colors used across the date picker UI.
// Built dynamically from MaterialTheme.colorScheme so it automatically
// adapts to light mode / dark mode without any manual switching logic.
 data class DatePickerPalette(
    val surface: Color,
    val text: Color,
    val subtext: Color,
    val accent: Color,
    val divider: Color,
    val accentText: Color,
    val fieldBackground: Color,
    val fieldBorder: Color
)

// Builds a theme-aware palette. Called from composables so it re-evaluates
// automatically whenever the app switches between light and dark theme.
@Composable
private fun rememberDatePickerPalette(): DatePickerPalette {
    val colorScheme = MaterialTheme.colorScheme
    return DatePickerPalette(
        surface = colorScheme.surface,
        text = colorScheme.onSurface,
        subtext = colorScheme.onSurfaceVariant,
        accent = colorScheme.primary,
        divider = colorScheme.outlineVariant,
        accentText = colorScheme.onPrimary,
        fieldBackground = colorScheme.surfaceVariant,
        fieldBorder = colorScheme.outline
    )
}

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
                if (isError) Modifier.border(1.dp, redtext, RoundedCornerShape(8.dp))
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
    var displayMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var displayYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var selectedDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }

    var isManualEntry by remember { mutableStateOf(false) }
    var manualDigits by remember { mutableStateOf(initialDate.filter { it.isDigit() }) }

    // --- Error State ---
    var manualError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.width(320.dp),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            color = palette.surface,
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(tokens.cardPadding)) {
                // Header & Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.fieldHeight)
                        .background(palette.fieldBackground, RoundedCornerShape(8.dp))
                        .border(1.dp, palette.fieldBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isManualEntry) "Manual Entry" else "Select Date",
                        color = palette.text,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = {
                        isManualEntry = !isManualEntry
                        manualError = null // Clear any error when switching modes
                    }) {
                        Icon(
                            imageVector = if (isManualEntry) Icons.Default.CalendarMonth else Icons.Default.Edit,
                            contentDescription = null,
                            tint = palette.accent,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                }

                Spacer(Modifier.height(tokens.screenPadding))

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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.accent,
                                unfocusedBorderColor = palette.fieldBorder,
                                errorBorderColor = redtext,
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
                                color = redtext,
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
                        onDaySelected = { selectedDay = it },
                        onPrevMonth = {
                            if (displayMonth == 0) { displayMonth = 11; displayYear-- }
                            else displayMonth--
                        },
                        onNextMonth = {
                            if (displayMonth == 11) { displayMonth = 0; displayYear++ }
                            else displayMonth++
                        }
                    )
                }

                Spacer(Modifier.height(tokens.screenPadding))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = palette.subtext, fontSize = tokens.bodyMedium)
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
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("OK", color = palette.accentText, fontSize = tokens.bodyMedium)
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
// Now takes the theme-aware palette instead of hardcoded hex colors,
// so it automatically matches light/dark mode.
// The clickable field shown on the form (before the picker dialog opens).
// Uses static hardcoded colors (not theme-aware) as before.
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
            .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
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
            Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = Color.Gray,
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
    onDaySelected: (Int) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${monthNames[displayMonth]} $displayYear", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Row {
            IconButton(onClick = onPrevMonth) { Icon(Icons.Default.ChevronLeft, null, tint = palette.text) }
            IconButton(onClick = onNextMonth) { Icon(Icons.Default.ChevronRight, null, tint = palette.text) }
        }
    }

    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(d, color = palette.subtext, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, displayYear)
        set(Calendar.MONTH, displayMonth)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Build the full grid: leading blanks for offset, then all days of the month
    val days = buildList {
        repeat(firstDay) { add(null) }
        for (i in 1..maxDays) add(i)
    }

    days.chunked(7).forEach { week ->
        Row(modifier = Modifier.fillMaxWidth()) {
            week.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (day == selectedDay) palette.accent else Color.Transparent)
                        .clickable(enabled = day != null) { day?.let(onDaySelected) },
                    contentAlignment = Alignment.Center
                ) {
                    if (day != null) {
                        Text(day.toString(), color = if (day == selectedDay) palette.accentText else palette.text)
                    }
                }
            }
            if (week.size < 7) repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

//TIME PICKER FIELD

// Holds all colors used by the time picker UI.
// Built from MaterialTheme.colorScheme so it automatically switches
// between light mode and dark mode without any manual toggle logic.
data class TimePickerPalette(
    val surface: Color,          // Dialog/background surface color
    val text: Color,             // Primary text color
    val subtext: Color,          // Secondary/muted text color
    val accent: Color,           // Primary accent color (selected state, buttons)
    val accentSoft: Color,       // Soft/light version of accent (highlight band behind selected wheel item)
    val onAccent: Color,         // Text/icon color on top of the accent color
    val fieldBackground: Color,  // Background of the clickable field / dialog
    val fieldBorder: Color       // Border color of the clickable field
)

// Builds a theme-aware palette. Re-evaluates automatically when the
// app theme (light/dark) changes since it reads from MaterialTheme.
@Composable
private fun rememberTimePickerPalette(): TimePickerPalette {
    val colorScheme = MaterialTheme.colorScheme
    return TimePickerPalette(
        surface = colorScheme.surface,
        text = colorScheme.onSurface,
        subtext = colorScheme.onSurfaceVariant,
        accent = colorScheme.primary,
        accentSoft = colorScheme.primaryContainer,
        onAccent = colorScheme.onPrimary,
        fieldBackground = colorScheme.surface,
        fieldBorder = colorScheme.outline
    )
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    value: String,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    // NEW: allows callers to explicitly disable this field (e.g. based on
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

    val palette = rememberTimePickerPalette()

    var selectedHour by remember(value) {
        mutableIntStateOf(
            if (value.isNotEmpty()) {
                try {
                    val hourStr = value.substringBefore(":").trim()
                    val hour = hourStr.toInt()
                    if (value.contains("PM", ignoreCase = true) && hour != 12) hour + 12
                    else if (value.contains("AM", ignoreCase = true) && hour == 12) 0
                    else hour
                } catch (_: Exception) { 10 }
            } else 10
        )
    }

    var selectedMinute by remember(value) {
        mutableIntStateOf(
            if (value.isNotEmpty()) {
                try {
                    val parts = value.split(":")
                    if (parts.size >= 2) {
                        parts[1].take(2).toInt()
                    } else 0
                } catch (_: Exception) { 53 }
            } else 53
        )
    }

    var isAm by remember(value) {
        mutableStateOf(
            if (value.isNotEmpty()) {
                !value.contains("PM", ignoreCase = true)
            } else true
        )
    }

    // This field row is back to static hardcoded colors (not theme-aware)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (effectiveEnabled) 1f else 0.6f) // NEW: visually dim when disabled
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .border(1.dp, PrimaryBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .clickable(enabled = effectiveEnabled) { showPicker = true } // NEW: block click when disabled
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

    if (showPicker && effectiveEnabled) { // NEW: dialog only opens when enabled
        AlertDialog(
            onDismissRequest = { showPicker = false },
            containerColor = whiteBg,
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            title = {
                Text(
                    "Appointment Time",
                    fontSize = tokens.h2,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            },
            text = {
                CustomTimePicker(
                    palette = palette,
                    hour = selectedHour,
                    minute = selectedMinute,
                    isAm = isAm,
                    onHourChange = { selectedHour = it },
                    onMinuteChange = { selectedMinute = it },
                    onAmPmChange = { isAm = it }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val displayHour = when {
                            selectedHour == 0 -> 12
                            selectedHour > 12 -> selectedHour - 12
                            else -> selectedHour
                        }
                        val amPm = if (isAm) "AM" else "PM"
                        val formattedTime = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm)
                        onTimeSelected(formattedTime)
                        showPicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = LeadPrimary
                    )
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPicker = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF6B7280)
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CustomTimePicker(
    palette: TimePickerPalette,
    hour: Int,
    minute: Int,
    isAm: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onAmPmChange: (Boolean) -> Unit
) {
    val tokens = LocalAppTokens.current
    val itemHeight = tokens.fieldHeight
    val wheelHeight = itemHeight * 4.5f
    val rowHeight = itemHeight * 6.3f

    val hourOptions = (1..12).toList()
    val minuteOptions = (0..59).map { String.format("%02d", it) }

    val hourRepeatCount = 1000
    val minuteRepeatCount = 1000
    val totalHourItems = hourOptions.size * hourRepeatCount
    val totalMinuteItems = minuteOptions.size * minuteRepeatCount

    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val displayMinute = String.format("%02d", minute)

    val initialHourIndex = remember {
        (hourRepeatCount / 2) * hourOptions.size + hourOptions.indexOf(displayHour).coerceAtLeast(0)
    }
    val initialMinuteIndex = remember {
        (minuteRepeatCount / 2) * minuteOptions.size + minuteOptions.indexOf(displayMinute).coerceAtLeast(0)
    }

    val hourScrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialHourIndex)
    val minuteScrollState = rememberLazyListState(initialFirstVisibleItemIndex = initialMinuteIndex)

    val hourCenterIndex by remember {
        derivedStateOf {
            val layoutInfo = hourScrollState.layoutInfo
            val center = layoutInfo.viewportEndOffset / 2
            val visibleItems = layoutInfo.visibleItemsInfo
            val closest = visibleItems.minByOrNull {
                val itemCenter = (it.offset + it.size / 2)
                kotlin.math.abs(itemCenter - center)
            }
            closest?.index ?: 0
        }
    }

    val minuteCenterIndex by remember {
        derivedStateOf {
            val layoutInfo = minuteScrollState.layoutInfo
            val center = layoutInfo.viewportEndOffset / 2
            val visibleItems = layoutInfo.visibleItemsInfo
            val closest = visibleItems.minByOrNull {
                val itemCenter = (it.offset + it.size / 2)
                kotlin.math.abs(itemCenter - center)
            }
            closest?.index ?: 0
        }
    }

    LaunchedEffect(hourCenterIndex) {
        if (hourCenterIndex in 0 until totalHourItems) {
            val newHour = hourOptions[hourCenterIndex % hourOptions.size]
            val hour24 = when {
                newHour == 12 && isAm -> 0
                newHour == 12 && !isAm -> 12
                !isAm -> newHour + 12
                else -> newHour
            }
            onHourChange(hour24)
        }
    }

    LaunchedEffect(minuteCenterIndex) {
        if (minuteCenterIndex in 0 until totalMinuteItems) {
            onMinuteChange(minuteOptions[minuteCenterIndex % minuteOptions.size].toInt())
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .padding(vertical = tokens.screenPadding * 0.5f),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- Hour wheel ---
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Hour", fontSize = tokens.caption, color = palette.subtext, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(tokens.screenPadding * 0.25f))

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(wheelHeight)) {
                // Highlight band behind the centered/selected item
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .align(Alignment.Center)
                        .background(
                            palette.accentSoft,
                            RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                        )
                )

                LazyColumn(
                    state = hourScrollState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                        lazyListState = hourScrollState
                    )
                ) {
                    items(totalHourItems) { i ->
                        val h = hourOptions[i % hourOptions.size]
                        val isSelected = i == hourCenterIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .padding(horizontal = tokens.screenPadding * 0.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", h),
                                fontSize = if (isSelected) tokens.h1 else tokens.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) palette.accent else palette.subtext
                            )
                        }
                    }
                }
            }
        }

        Text(":", fontSize = tokens.h1, fontWeight = FontWeight.Bold, color = palette.text, modifier = Modifier.padding(horizontal = tokens.screenPadding * 0.25f))

        // --- Minute wheel ---
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Minute", fontSize = tokens.caption, color = palette.subtext, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(tokens.screenPadding * 0.25f))

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(wheelHeight)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .align(Alignment.Center)
                        .background(
                            palette.accentSoft,
                            RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                        )
                )

                LazyColumn(
                    state = minuteScrollState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    flingBehavior = androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior(
                        lazyListState = minuteScrollState
                    )
                ) {
                    items(totalMinuteItems) { i ->
                        val m = minuteOptions[i % minuteOptions.size]
                        val isSelected = i == minuteCenterIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeight)
                                .padding(horizontal = tokens.screenPadding * 0.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = m,
                                fontSize = if (isSelected) tokens.h1 else tokens.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) palette.accent else palette.subtext
                            )
                        }
                    }
                }
            }
        }

        // --- AM/PM toggle ---
        Column(
            modifier = Modifier
                .weight(0.8f)
                .padding(start = tokens.screenPadding * 0.5f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("AM/PM", fontSize = tokens.caption, color = palette.subtext, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(tokens.screenPadding * 0.25f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(wheelHeight)
                    .padding(vertical = tokens.screenPadding * 1.25f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(if (isAm) palette.accent else Color.Transparent)
                        .clickable { onAmPmChange(true) }
                        .padding(tokens.screenPadding * 0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "AM",
                        fontSize = tokens.bodyMedium,
                        fontWeight = if (isAm) FontWeight.Bold else FontWeight.Normal,
                        color = if (isAm) palette.onAccent else palette.subtext
                    )
                }

                Spacer(Modifier.height(tokens.screenPadding * 0.5f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(if (!isAm) palette.accent else Color.Transparent)
                        .clickable { onAmPmChange(false) }
                        .padding(tokens.screenPadding * 0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "PM",
                        fontSize = tokens.bodyMedium,
                        fontWeight = if (!isAm) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isAm) palette.onAccent else palette.subtext
                    )
                }
            }
        }
    }
}