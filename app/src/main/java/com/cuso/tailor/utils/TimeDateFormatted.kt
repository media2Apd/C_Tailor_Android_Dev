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

/**
 * Converts a date string coming from the date picker into the ISO 8601 format
 * expected by the backend, e.g. "2026-10-11T00:00:00.000Z".
 * Returns null if the input is blank or cannot be parsed.
 */
 fun toIsoDateTime(input: String?): String? {
    if (input.isNullOrBlank()) return null
    val value = input.trim()

    // Already in ISO format, send as is
    if (value.contains("T") && value.endsWith("Z")) return value

    val inputPatterns = listOf(
        "yyyy-MM-dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "dd MMM yyyy",
        "dd MMM, yyyy"
    )

    val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    for (pattern in inputPatterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.ENGLISH).apply {
                isLenient = false
                // Parse in UTC so the selected day does not shift because of the local timezone
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(value) ?: continue
            return outputFormat.format(date)
        } catch (_: Exception) {
            // Try the next pattern
        }
    }
    return null
}