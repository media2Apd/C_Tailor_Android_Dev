package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.mobile.view.home.FormDateField
import com.cuso.mobile.view.home.LeadPrimary
import kotlin.collections.chunked
import kotlin.collections.forEach

private data class DatePickerPalette(
    val surface: Color,
    val text: Color,
    val subtext: Color,
    val accent: Color,
    val divider: Color,
    val accentText: Color,
    val fieldBackground: Color,   // ✅ NEW
    val fieldBorder: Color        // ✅ NEW
)

private val LightDatePalette = DatePickerPalette(
    surface = Color.White,
    text = Color.Black,
    subtext = Color(0xFF666666),
    accent = LeadPrimary,
    divider = Color(0xFFE0E0E0),
    accentText = Color.White,
    fieldBackground = Color.White,          // ✅ NEW
    fieldBorder = Color(0xFFE5E7EB)          // ✅ NEW
)

private val DarkDatePalette = DatePickerPalette(
    surface = Color(0xFF1E1E2E),
    text = Color.White,
    subtext = Color(0xFFAAAAAA),
    accent = Color(0xFF7C7CFF),
    divider = Color(0xFF3A3A4A),
    accentText = Color.Black,
    fieldBackground = Color(0xFF2A2A3D),    // ✅ NEW — subtle lighter shade than dialog bg
    fieldBorder = Color(0xFF3A3A4A)          // ✅ NEW — matches divider
)

private val DatePickerMonthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@Composable
fun DatePickerField(
    value: String,
    onDateSelected: (String) -> Unit,
    enabled: Boolean = true   // ✅ NEW — false = view mode, field can't be tapped/opened
) {
    var showPicker by remember { mutableStateOf(false) }

    // ✅ NEW — Box wraps FormDateField so we can dim it and block clicks when disabled,
    // without needing FormDateField itself to support an `enabled` parameter.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)   // visually looks disabled, like other fields
    ) {
        FormDateField(
            value = value,
            onClick = { if (enabled) showPicker = true }
        )

        if (!enabled) {
            // Transparent overlay that swallows any tap so the dialog never opens in view mode
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(enabled = false) {}
            )
        }
    }

    if (showPicker && enabled) {
        val palette = if (isSystemInDarkTheme()) DarkDatePalette else LightDatePalette
        CustomDatePickerDialog(
            palette = palette,
            initialDate = value,
            onDismiss = { showPicker = false },
            onConfirm = { day, month, year ->
                onDateSelected(
                    String.format(java.util.Locale.US, "%02d-%02d-%04d", day, month, year)
                )
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
    val today = remember { java.util.Calendar.getInstance() }
    var displayMonth by remember { mutableIntStateOf(today.get(java.util.Calendar.MONTH)) }
    var displayYear by remember { mutableIntStateOf(today.get(java.util.Calendar.YEAR)) }
    var selectedDay by remember { mutableStateOf(today.get(java.util.Calendar.DAY_OF_MONTH)) }
    var isManualEntry by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf(initialDate) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.width(320.dp),
            shape = RoundedCornerShape(24.dp),
            color = palette.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(palette.fieldBackground, RoundedCornerShape(8.dp))   // ✅ CHANGED
                        .border(1.dp, palette.fieldBorder, RoundedCornerShape(8.dp))     // ✅ CHANGED
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select date", color = palette.text, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    IconButton(
                        onClick = { isManualEntry = !isManualEntry },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isManualEntry) Icons.Default.CalendarMonth else Icons.Default.Edit,
                            contentDescription = "Toggle input mode",
                            tint = palette.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (isManualEntry) {
                    OutlinedTextField(
                        value = manualText,
                        onValueChange = { manualText = it },
                        label = { Text("dd-mm-yyyy", color = palette.subtext) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = palette.text,
                            unfocusedTextColor = palette.text,
                            focusedBorderColor = palette.accent,
                            unfocusedBorderColor = palette.divider,
                            cursorColor = palette.accent,
                            focusedLabelColor = palette.accent,
                            unfocusedLabelColor = palette.subtext
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
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

                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = palette.accent)
                    }
                    Spacer(Modifier.width(4.dp))
                    TextButton(onClick = {
                        if (isManualEntry) {
                            parseManualDatePicked(manualText)?.let { (d, m, y) -> onConfirm(d, m, y) }
                        } else {
                            onConfirm(selectedDay, displayMonth + 1, displayYear)
                        }
                    }) {
                        Text("OK", color = palette.accent)
                    }
                }
            }
        }
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${DatePickerMonthNames[displayMonth]} $displayYear",
            color = palette.text,
            fontWeight = FontWeight.Medium
        )
        Row {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = palette.text)
            }
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = palette.text)
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(d, color = palette.subtext, fontWeight = FontWeight.Medium)
            }
        }
    }

    Spacer(Modifier.height(4.dp))

    val calendar = remember(displayMonth, displayYear) {
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, displayYear)
            set(java.util.Calendar.MONTH, displayMonth)
            set(java.util.Calendar.DAY_OF_MONTH, 1)
        }
    }
    val firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)

    val cells = remember(displayMonth, displayYear) {
        buildList {
            repeat(firstDayOfWeek) { add(null) }
            for (d in 1..daysInMonth) add(d)
        }
    }

    cells.chunked(7).forEach { week ->
        Row(modifier = Modifier.fillMaxWidth()) {
            week.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .then(
                            if (day != null && day == selectedDay)
                                Modifier.background(palette.accent, RoundedCornerShape(50))
                            else Modifier
                        )
                        .clickable(enabled = day != null) { day?.let(onDaySelected) },
                    contentAlignment = Alignment.Center
                ) {
                    if (day != null) {
                        Text(
                            text = day.toString(),
                            color = if (day == selectedDay) palette.accentText else palette.text
                        )
                    }
                }
            }
            repeat(7 - week.size) {
                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
            }
        }
    }
}

private fun parseManualDatePicked(text: String): Triple<Int, Int, Int>? {
    val parts = text.split("-")
    if (parts.size != 3) return null
    return try {
        val day = parts[0].trim().toInt()
        val month = parts[1].trim().toInt()
        val year = parts[2].trim().toInt()
        if (month !in 1..12 || day !in 1..31) return null
        Triple(day, month, year)
    } catch (e: NumberFormatException) {
        null
    }
}