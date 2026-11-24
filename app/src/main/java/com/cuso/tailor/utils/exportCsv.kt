package com.cuso.tailor.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val exportGson: Gson by lazy {
    GsonBuilder().serializeNulls().create()
}

private val mongoObjectIdRegex = Regex("^[a-fA-F0-9]{24}$")
private val isoDateFormats = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "yyyy-MM-dd'T'HH:mm:ssXXX"
)

/**
 * Automatically converts any list of DTOs/objects into a clean, human-readable CSV string.
 */
fun <T> convertAnyListToReadableCsv(items: List<T>): String {
    if (items.isEmpty()) return ""

    val flattenedRows = mutableListOf<Map<String, String>>()
    val orderedHeaders = LinkedHashSet<String>()

    items.forEach { item ->
        if (item != null) {
            val rowMap = linkedMapOf<String, String>()
            val tree = exportGson.toJsonTree(item)
            flattenJsonToReadableMap(
                element = tree,
                prefix = "",
                output = rowMap
            )
            orderedHeaders.addAll(rowMap.keys)
            flattenedRows.add(rowMap)
        }
    }

    if (orderedHeaders.isEmpty()) return ""

    val headersList = orderedHeaders.toList()
    val sb = StringBuilder()

    // Write CSV header row with readable column titles
    sb.append(headersList.joinToString(",") { escapeCsvCell(formatReadableHeader(it)) })
    sb.append("\r\n")

    // Write CSV data rows
    flattenedRows.forEach { row ->
        sb.append(headersList.joinToString(",") { key ->
            escapeCsvCell(row[key].orEmpty())
        })
        sb.append("\r\n")
    }

    return sb.toString()
}

/**
 * Automatically converts any list of report data into a readable CSV and saves it
 * directly into the device Downloads folder.
 */
fun <T> exportAnyDataToCsvDownloads(
    context: Context,
    coroutineScope: CoroutineScope,
    fileNamePrefix: String,
    items: List<T>,
    onFinish: () -> Unit = {}
) {
    if (items.isEmpty()) {
        DynamicIslandManager.showError("No records available to export")
        onFinish()
        return
    }

    coroutineScope.launch(Dispatchers.IO) {
        try {
            val csvContent = convertAnyListToReadableCsv(items)
            if (csvContent.isBlank()) {
                withContext(Dispatchers.Main) {
                    DynamicIslandManager.showError("No readable data found to export")
                    onFinish()
                }
                return@launch
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val cleanPrefix = fileNamePrefix
                .replace(Regex("[^a-zA-Z0-9_-]"), "_")
                .replace(Regex("_+"), "_")
                .trim('_')
                .ifBlank { "Report" }
            val fullFileName = "${cleanPrefix}_$timestamp.csv"

            // Include UTF-8 BOM so Excel renders currency symbols and special characters properly
            val utf8Bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
            val csvBytes = utf8Bom + csvContent.toByteArray(Charsets.UTF_8)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fullFileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IllegalStateException("Failed to create file in Downloads")

                try {
                    resolver.openOutputStream(uri)?.use { stream ->
                        stream.write(csvBytes)
                    } ?: throw IllegalStateException("Unable to open Downloads output stream")

                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                } catch (e: Exception) {
                    resolver.delete(uri, null, null)
                    throw e
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                val outFile = File(downloadsDir, fullFileName)
                FileOutputStream(outFile).use { stream ->
                    stream.write(csvBytes)
                }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(outFile.absolutePath),
                    arrayOf("text/csv"),
                    null
                )
            }

            withContext(Dispatchers.Main) {
                DynamicIslandManager.showSuccess("Downloaded to Downloads/$fullFileName")
                onFinish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                DynamicIslandManager.showError(e.message ?: "Failed to download CSV")
                onFinish()
            }
        }
    }
}

/**
 * Automatically converts any list of data to readable CSV and shares it using shareCsvFileSafely.
 */
fun <T> shareAnyDataAsCsv(
    context: Context,
    coroutineScope: CoroutineScope,
    fileNamePrefix: String,
    items: List<T>,
    onFinish: () -> Unit = {}
) {
    if (items.isEmpty()) {
        DynamicIslandManager.showError("No records available to share")
        onFinish()
        return
    }

    val csvContent = convertAnyListToReadableCsv(items)
    if (csvContent.isBlank()) {
        DynamicIslandManager.showError("No readable data found to share")
        onFinish()
        return
    }

    val cleanPrefix = fileNamePrefix
        .replace(Regex("[^a-zA-Z0-9_-]"), "_")
        .replace(Regex("_+"), "_")
        .trim('_')
        .ifBlank { "Report" }

    shareCsvFileSafely(
        context = context,
        coroutineScope = coroutineScope,
        fileName = cleanPrefix,
        csvContent = csvContent,
        onFinish = onFinish
    )
}

private fun flattenJsonToReadableMap(
    element: JsonElement?,
    prefix: String,
    output: MutableMap<String, String>
) {
    if (element == null || element.isJsonNull) {
        if (prefix.isNotBlank()) output[prefix] = "--"
        return
    }

    when {
        element.isJsonObject -> {
            val obj = element.asJsonObject
            obj.entrySet().forEach { (rawKey, value) ->
                if (rawKey == "_id" || rawKey == "__v") return@forEach

                val cleanKey = if (value.isJsonObject && rawKey.endsWith("Id") && rawKey.length > 2) {
                    rawKey.removeSuffix("Id")
                } else {
                    rawKey
                }

                val nextPrefix = if (prefix.isBlank()) cleanKey else "$prefix.$cleanKey"
                flattenJsonToReadableMap(value, nextPrefix, output)
            }
        }

        element.isJsonArray -> {
            val arrayText = formatJsonArrayReadable(element.asJsonArray)
            if (prefix.isNotBlank()) output[prefix] = arrayText
        }

        element.isJsonPrimitive -> {
            val primitive = element.asJsonPrimitive
            val rawValue = primitive.asString.trim()

            // Skip raw internal MongoDB IDs so exported CSV stays human-readable
            val isInternalIdKey = prefix.equals("id", ignoreCase = true) ||
                    prefix.endsWith(".id", ignoreCase = true) ||
                    prefix.endsWith("_id", ignoreCase = true)
            if (isInternalIdKey && mongoObjectIdRegex.matches(rawValue)) {
                return
            }

            if (prefix.isNotBlank()) {
                output[prefix] = formatPrimitiveReadable(primitive)
            }
        }
    }
}

private fun formatJsonArrayReadable(array: JsonArray): String {
    if (array.size() == 0) return "--"
    return array.mapNotNull { el ->
        when {
            el.isJsonNull -> null
            el.isJsonPrimitive -> formatPrimitiveReadable(el.asJsonPrimitive)
            el.isJsonObject -> extractReadableSummaryFromObject(el.asJsonObject)
            else -> null
        }
    }.filter { it.isNotBlank() }.joinToString("; ").ifBlank { "--" }
}

private fun extractReadableSummaryFromObject(obj: JsonObject): String {
    val preferredKeys = listOf("name", "fullName", "title", "label", "code", "orderCode", "sku")
    preferredKeys.forEach { key ->
        val candidate = obj.get(key)
        if (candidate != null && candidate.isJsonPrimitive) {
            val text = candidate.asString.trim()
            if (text.isNotBlank()) return text
        }
    }
    return obj.entrySet()
        .filter { (k, v) -> k != "_id" && k != "id" && v.isJsonPrimitive }
        .take(2)
        .joinToString(" - ") { (_, v) -> formatPrimitiveReadable(v.asJsonPrimitive) }
}

private fun formatPrimitiveReadable(primitive: JsonPrimitive): String {
    return when {
        primitive.isBoolean -> if (primitive.asBoolean) "Yes" else "No"
        primitive.isNumber -> {
            val bd = try {
                BigDecimal(primitive.asString).stripTrailingZeros()
            } catch (_: Exception) {
                null
            }
            if (bd != null) bd.toPlainString() else primitive.asString
        }
        else -> {
            val text = primitive.asString.trim()
            if (text.isBlank()) {
                "--"
            } else {
                formatIsoDateIfApplicable(text) ?: text
            }
        }
    }
}

private fun formatIsoDateIfApplicable(raw: String): String? {
    if (raw.length < 19 || !raw.contains("T")) return null
    for (pattern in isoDateFormats) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(raw) ?: continue
            val isMidnight = raw.contains("T00:00:00")
            val outPattern = if (isMidnight) "dd-MM-yyyy" else "dd-MM-yyyy hh:mm a"
            return SimpleDateFormat(outPattern, Locale.getDefault()).format(date)
        } catch (_: Exception) {
            // Try next ISO format
        }
    }
    return null
}

private fun formatReadableHeader(rawKey: String): String {
    return rawKey
        .replace(".", " ")
        .replace("_", " ")
        .replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            when (word.lowercase()) {
                "id" -> "ID"
                "sku" -> "SKU"
                "gst" -> "GST"
                "sla" -> "SLA"
                "cod" -> "COD"
                "rto" -> "RTO"
                "nps" -> "NPS"
                "ltv" -> "LTV"
                "roi" -> "ROI"
                "api" -> "API"
                "ip" -> "IP"
                else -> word.replaceFirstChar { ch -> ch.uppercaseChar() }
            }
        }
}

private fun escapeCsvCell(value: String): String {
    val needsQuotes = value.contains(',') ||
            value.contains('"') ||
            value.contains('\n') ||
            value.contains('\r')
    return if (needsQuotes) {
        "\"${value.replace("\"", "\"\"")}\""
    } else {
        value
    }
}