package com.cuso.tailor.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

fun shareCsvFileSafely(
    context: Context,
    coroutineScope: CoroutineScope,
    fileName: String,
    csvContent: String,
    onFinish: () -> Unit = {}
) {
    // 1. Background Thread (Dispatchers.IO) - prevents ANR freeze
    coroutineScope.launch(Dispatchers.IO) {
        try {
            val cachePath = File(context.cacheDir, "shared_files")
            if (!cachePath.exists()) {
                cachePath.mkdirs()
            }
            val file = File(cachePath, "$fileName.csv")
            FileOutputStream(file).use { outputStream ->
                outputStream.write(csvContent.toByteArray())
            }

            // 2. Generate URI via FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // 3. Launch Chooser safely on Main Thread with Read Permission
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    // Prevents "Permission Denial" in Android 10+
                    clipData = ClipData.newRawUri("", contentUri)
                }

                val chooser = Intent.createChooser(intent, "Share Record").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                onFinish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onFinish()
            }
        }
    }
}