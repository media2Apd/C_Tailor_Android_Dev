package com.cuso.mobile.utils

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

fun createPartFromString(value: String): RequestBody {
    return value.toRequestBody("text/plain".toMediaTypeOrNull())
}

fun uriToMultipartPart(context: Context, uri: Uri, partName: String = "images"): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val type = contentResolver.getType(uri) ?: "image/*"
        val inputStream = contentResolver.openInputStream(uri) ?: return null

        val tempFile = File.createTempFile("upload_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()

        val requestFile = tempFile.asRequestBody(type.toMediaTypeOrNull())
        MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}