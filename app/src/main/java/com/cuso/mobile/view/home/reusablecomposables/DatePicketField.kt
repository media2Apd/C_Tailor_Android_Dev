//package com.cuso.mobile.view.home.reusablecomposables
//
//import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.DialogProperties
//import com.cuso.mobile.view.home.FormDateField
//import androidx.compose.ui.window.Dialog
//
//// ─────────────────────────────────────────────────────────────
//// DatePickerField - custom implementation, no Material3 stock
//// DatePicker, no MaterialTheme wrap, no animation. Guaranteed
//// identical behaviour/performance across every device.
//// ─────────────────────────────────────────────────────────────
//
//private data class DatePickerPalette(
//    val surface: Color,
//    val text: Color,
//    val subtext: Color,
//    val accent: Color,
//    val divider: Color,
//    val accentText: Color
//)
//
//private val LightDatePalette = DatePickerPalette(
//    surface = Color.White,
//    text = Color.Black,
//    subtext = Color(0xFF666666),
//    accent = Color(0xFF3B3BF9),
//    divider = Color(0xFFE0E0E0),
//    accentText = Color.White
//)
//
//private val DarkDatePalette = DatePickerPalette(
//    surface = Color(0xFF1E1E2E),
//    text = Color.White,
//    subtext = Color(0xFFAAAAAA),
//    accent = Color(0xFF7C7CFF),
//    divider = Color(0xFF3A3A4A),
//    accentText = Color.Black
//)
//
//private val DatePickerMonthNames = listOf(
//    "January", "February", "March", "April", "May", "June",
//    "July", "August", "September", "October", "November", "December"
//)
//
//@Composable
//fun DatePickerField(value: String, onDateSelected: (String) -> Unit) {
//    var showPicker by remember { mutableStateOf(false) }
//    FormDateField(value = value, onClick = { showPicker = true })
//
//    if (showPicker) {
//        val palette = if (isSystemInDarkTheme()) DarkDatePalette else LightDatePalette
//        CustomDatePickerDialog(
//            palette = palette,
//            initialDate = value,
//            onDismiss = { showPicker = false },
//            onConfirm = { day, month, year ->
//                onDateSelected(
//                    String.format(java.util.Locale.US, "%02d-%02d-%04d", day, month, year)
//                )
//                showPicker = false
//            }
//        )
//    }
//}
//
//@Composable
//private fun CustomDatePickerDialog(
//    palette: DatePickerPalette,
//    initialDate: String,
//    onDismiss: () -> Unit,
//    onConfirm: (day: Int, month: Int, year: Int) -> Unit
//) {
//    val today = remember { java.util.Calendar.getInstance() }
//    var displayMonth by remember { mutableIntStateOf(today.get(java.util.Calendar.MONTH)) }
//    var displayYear by remember { mutableIntStateOf(today.get(java.util.Calendar.YEAR)) }
//    var selectedDay by remember { mutableStateOf(today.get(java.util.Calendar.DAY_OF_MONTH)) }
//    var isManualEntry by remember { mutableStateOf(false) }
//    var manualText by remember { mutableStateOf(initialDate) }
//
//    Dialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(usePlatformDefaultWidth = false)
//    ) {
//        Surface(
//            modifier = Modifier.width(320.dp),
//            shape = RoundedCornerShape(24.dp),
//            color = palette.surface
//        ) {
//            Column(modifier = Modifier.padding(20.dp)) {
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text("Select date", color = palette.text, fontWeight = FontWeight.Medium)
//                    IconButton(onClick = { isManualEntry = !isManualEntry }) {
//                        Icon(
//                            imageVector = if (isManualEntry) Icons.Default.CalendarMonth else Icons.Default.Edit,
//                            contentDescription = "Toggle input mode",
//                            tint = palette.accent
//                        )
//                    }
//                }
//
//                Spacer(Modifier.height(16.dp))
//
//                if (isManualEntry) {
//                    OutlinedTextField(
//                        value = manualText,
//                        onValueChange = { manualText = it },
//                        label = { Text("dd-mm-yyyy", color = palette.subtext) },
//                        singleLine = true,
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedTextColor = palette.text,
//                            unfocusedTextColor = palette.text,
//                            focusedBorderColor = palette.accent,
//                            unfocusedBorderColor = palette.divider,
//                            cursorColor = palette.accent,
//                            focusedLabelColor = palette.accent,
//                            unfocusedLabelColor = palette.subtext
//                        ),
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                } else {
//                    DatePickerCalendarGrid(
//                        palette = palette,
//                        displayMonth = displayMonth,
//                        displayYear = displayYear,
//                        selectedDay = selectedDay,
//                        onDaySelected = { selectedDay = it },
//                        onPrevMonth = {
//                            if (displayMonth == 0) { displayMonth = 11; displayYear-- }
//                            else displayMonth--
//                        },
//                        onNextMonth = {
//                            if (displayMonth == 11) { displayMonth = 0; displayYear++ }
//                            else displayMonth++
//                        }
//                    )
//                }
//
//                Spacer(Modifier.height(16.dp))
//
//                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
//                    TextButton(onClick = onDismiss) {
//                        Text("Cancel", color = palette.accent)
//                    }
//                    Spacer(Modifier.width(4.dp))
//                    TextButton(onClick = {
//                        if (isManualEntry) {
//                            parseManualDatePicked(manualText)?.let { (d, m, y) -> onConfirm(d, m, y) }
//                        } else {
//                            onConfirm(selectedDay, displayMonth + 1, displayYear)
//                        }
//                    }) {
//                        Text("OK", color = palette.accent)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun DatePickerCalendarGrid(
//    palette: DatePickerPalette,
//    displayMonth: Int,
//    displayYear: Int,
//    selectedDay: Int,
//    onDaySelected: (Int) -> Unit,
//    onPrevMonth: () -> Unit,
//    onNextMonth: () -> Unit
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = "${DatePickerMonthNames[displayMonth]} $displayYear",
//            color = palette.text,
//            fontWeight = FontWeight.Medium
//        )
//        Row {
//            IconButton(onClick = onPrevMonth) {
//                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = palette.text)
//            }
//            IconButton(onClick = onNextMonth) {
//                Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = palette.text)
//            }
//        }
//    }
//
//    Spacer(Modifier.height(8.dp))
//
//    Row(modifier = Modifier.fillMaxWidth()) {
//        listOf("S", "M", "T", "W", "T", "F", "S").forEach { d ->
//            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
//                Text(d, color = palette.subtext, fontWeight = FontWeight.Medium)
//            }
//        }
//    }
//
//    Spacer(Modifier.height(4.dp))
//
//    val calendar = remember(displayMonth, displayYear) {
//        java.util.Calendar.getInstance().apply {
//            set(java.util.Calendar.YEAR, displayYear)
//            set(java.util.Calendar.MONTH, displayMonth)
//            set(java.util.Calendar.DAY_OF_MONTH, 1)
//        }
//    }
//    val firstDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
//    val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
//
//    val cells = remember(displayMonth, displayYear) {
//        buildList {
//            repeat(firstDayOfWeek) { add(null) }
//            for (d in 1..daysInMonth) add(d)
//        }
//    }
//
//    cells.chunked(7).forEach { week ->
//        Row(modifier = Modifier.fillMaxWidth()) {
//            week.forEach { day ->
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .aspectRatio(1f)
//                        .padding(2.dp)
//                        .then(
//                            if (day != null && day == selectedDay)
//                                Modifier.background(palette.accent, RoundedCornerShape(50))
//                            else Modifier
//                        )
//                        .clickable(enabled = day != null) { day?.let(onDaySelected) },
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (day != null) {
//                        Text(
//                            text = day.toString(),
//                            color = if (day == selectedDay) palette.accentText else palette.text
//                        )
//                    }
//                }
//            }
//            repeat(7 - week.size) {
//                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
//            }
//        }
//    }
//}
//
//private fun parseManualDatePicked(text: String): Triple<Int, Int, Int>? {
//    val parts = text.split("-")
//    if (parts.size != 3) return null
//    return try {
//        val day = parts[0].trim().toInt()
//        val month = parts[1].trim().toInt()
//        val year = parts[2].trim().toInt()
//        if (month !in 1..12 || day !in 1..31) return null
//        Triple(day, month, year)
//    } catch (e: NumberFormatException) {
//        null
//    }
//}