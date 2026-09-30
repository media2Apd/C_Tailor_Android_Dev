package com.cuso.tailor.utils


import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun formatIsoToTime(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "--:--"
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = isoFormat.parse(isoString)
        val displayFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        date?.let { displayFormat.format(it) } ?: "--:--"
    } catch (_: Exception) {
        "--:--"
    }
}

fun formatMinutesToHours(minutes: Int): String {
    if (minutes <= 0) return "0.0 H"
    val hours = minutes / 60.0
    return String.format(Locale.US, "%.1f H", hours)
}


fun convertUiDateToApiDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputFormat.parse(dateStr)
        date?.let { outputFormat.format(it) } ?: dateStr
    } catch (_: Exception) {
        dateStr
    }
}

fun convert12HrTo24Hr(time12: String): String {
    return try {
        val inputFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val outputFormat = SimpleDateFormat("HH:mm", Locale.US)
        val date = inputFormat.parse(time12)
        date?.let { outputFormat.format(it) } ?: time12
    } catch (_: Exception) {
        time12
    }
}

fun format24HrTo12Hr(time24: String?): String {
    if (time24.isNullOrBlank()) return "--:--"
    return try {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.US)
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val date = inputFormat.parse(time24)
        date?.let { outputFormat.format(it) } ?: time24
    } catch (_: Exception) {
        time24
    }
}


fun convert24HrTo12Hr(time24: String?): String {
    if (time24.isNullOrBlank()) return "09:00 AM"
    return try {
        val inputFormat = SimpleDateFormat("HH:mm", Locale.US)
        val outputFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val date = inputFormat.parse(time24)
        date?.let { outputFormat.format(it) } ?: time24
    } catch (_: Exception) {
        time24
    }
}