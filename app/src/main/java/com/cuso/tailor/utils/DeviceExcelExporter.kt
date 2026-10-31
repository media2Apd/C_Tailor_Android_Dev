package com.cuso.tailor.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.cuso.tailor.model.it.DeviceItemDto
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object  DeviceExcelExporter {

    /**
     * Generates a CSV file matching the exact Excel spreadsheet columns shown in the screenshot.
     * CSV files open natively in Microsoft Excel, Google Sheets, and LibreOffice.
     */
    fun exportDevicesToCsv(
        context: Context,
        devices: List<DeviceItemDto>,
        fileName: String = "Devices_${System.currentTimeMillis()}.csv"
    ): Boolean {
        return try {
            val headers = listOf(
                "S.No",
                "Device Name",
                "Device No",
                "Device Type",
                "Serial Number",
                "Branch",
                "Assigned To",
                "Status",
                "Last Seen",
                "Maintenance Status"
            )

            val stringBuilder = StringBuilder()
            // Add UTF-8 BOM so Excel opens special characters cleanly
            stringBuilder.append('\uFEFF')
            stringBuilder.append(headers.joinToString(","))
            stringBuilder.append("\n")

            devices.forEachIndexed { index, device ->
                val row = listOf(
                    (index + 1).toString(),
                    escapeCsv(device.deviceName),
                    escapeCsv(device.deviceNo.ifBlank { "--" }),
                    escapeCsv(device.deviceType),
                    escapeCsv(device.serialNumber.ifBlank { "--" }),
                    escapeCsv(device.branchName ?: "--"),
                    escapeCsv(device.assignedTo?.name ?: "Unassigned"),
                    escapeCsv(device.status),
                    escapeCsv(device.lastSeenFormatted ?: "Just now"),
                    escapeCsv(device.maintenanceStatus)
                )
                stringBuilder.append(row.joinToString(","))
                stringBuilder.append("\n")
            }

            val csvData = stringBuilder.toString().toByteArray(Charsets.UTF_8)

            // Save to Downloads folder
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri: Uri? = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
                    outputStream?.use { stream ->
                        stream.write(csvData)
                    }
                    shareFile(context, uri)
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { it.write(csvData) }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                shareFile(context, uri)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun escapeCsv(value: String): String {
        var str = value.trim()
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            str = str.replace("\"", "\"\"")
            return "\"$str\""
        }
        return str
    }

    private fun shareFile(context: Context, uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/csv")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(Intent.createChooser(shareIntent, "Open Exported File"))
        } catch (_: Exception) {
            // Handled if no viewer exists
        }
    }
}