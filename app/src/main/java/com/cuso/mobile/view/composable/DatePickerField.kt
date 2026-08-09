@file:Suppress(
    "UNUSED_VALUE", "SpellCheckingInspection", "GrazieInspection",
    "AssignedValueIsNeverRead", "unused_variable", "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)
package com.cuso.mobile.view.composable

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import java.util.*

private data class DatePickerPalette(
    val surface: Color,
    val text: Color,
    val subtext: Color,
    val accent: Color,
    val divider: Color,
    val accentText: Color,
    val fieldBackground: Color,
    val fieldBorder: Color
)

@Composable
fun DatePickerField(
    value: String,
    onDateSelected: (String) -> Unit,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    val tokens = LocalAppTokens.current
    var showPicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.6f)
            .then(
                if (isError) Modifier.border(1.dp, redtext, RoundedCornerShape(8.dp))
                else Modifier
            )
    ) {
        FormDateField(
            value = value,
            onClick = { if (enabled) showPicker = true }
        )

        if (!enabled) {
            Box(modifier = Modifier.matchParentSize().clickable(enabled = false) {})
        }
    }

    if (showPicker && enabled) {
        val palette = DatePickerPalette(
            surface = whiteBg,
            text = TextPrimary,
            subtext = TextSecondary,
            accent = Primary,
            divider = BorderGray,
            accentText = whiteBg,
            fieldBackground = Primary_background,
            fieldBorder = BorderGray
        )
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
                        if (isManualEntry) "Manual Entry" else " ",
                        color = palette.text,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = {
                        isManualEntry = !isManualEntry
                        manualError = null // switch aagum pothu error clear pannu
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
                                    manualError = null // type panna panna error clear pannu
                                }
                            },
                            placeholder = { Text("DD-MM-YYYY", color = palette.subtext) },
                            singleLine = true,
                            isError = manualError != null, // validation failed-na border RED aagum
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = DateTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = palette.accent,
                                unfocusedBorderColor = palette.fieldBorder,
                                errorBorderColor = redtext,
                                focusedTextColor = palette.text
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Error message text keela kaattu
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
 * Strict Validation: Thappana date type panna return NULL
 */
private fun validateAndParseDate(digits: String): Triple<Int, Int, Int>? {
    if (digits.length != 8) return null
    return try {
        val d = digits.substring(0, 2).toInt()
        val m = digits.substring(2, 4).toInt()
        val y = digits.substring(4, 8).toInt()

        // logic: strict check
        val cal = Calendar.getInstance()
        cal.setLenient(false) // Ithu thaan main! 31-02-2024 type panna error throw pannum
        cal.set(Calendar.YEAR, y)
        cal.set(Calendar.MONTH, m - 1)
        cal.set(Calendar.DAY_OF_MONTH, d)

        cal.time // Ithu exception throw panna date invalid

        if (y < 1900 || y > 2100) return null // Reasonable year range

        Triple(d, m, y)
    } catch (e: Exception) {
        null
    }
}

/**
 * Visual Transformation logic
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

@Composable
fun FormDateField(value: String, onClick: () -> Unit) {
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