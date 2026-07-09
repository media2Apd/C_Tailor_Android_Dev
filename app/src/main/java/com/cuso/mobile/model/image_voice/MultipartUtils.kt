package com.cuso.mobile.model.image_voice

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
//import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
//import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

//fun createPartFromString(value: String): RequestBody =
//    value.toRequestBody("text/plain".toMediaTypeOrNull())

/** Copies a content Uri into a temp cache file Retrofit/OkHttp can stream. */
fun Context.uriToTempFile(uri: Uri, prefix: String, suffix: String): File? {
    return try {
        val input = contentResolver.openInputStream(uri) ?: return null
        val file = File.createTempFile(prefix, suffix, cacheDir)
        input.use { inStream -> file.outputStream().use { out -> inStream.copyTo(out) } }
        file
    } catch (_: Exception) {
        null
    }
}

fun Context.createImageParts(
    uris: List<Uri>,
    partName: String = "designImages"
): List<MultipartBody.Part> {
    if (uris.isEmpty()) return emptyList()
    return uris.mapIndexedNotNull { index, uri ->
        val file = uriToTempFile(uri, "design_${index}_", ".jpg") ?: return@mapIndexedNotNull null
        val mediaType = (contentResolver.getType(uri) ?: "image/jpeg").toMediaTypeOrNull()
        MultipartBody.Part.createFormData(partName, file.name, file.asRequestBody(mediaType))
    }
}

fun Context.createVoiceNotePart(
    uri: Uri?,
    partName: String = "voiceNote"
): MultipartBody.Part? {
    if (uri == null) return null
    val file = uriToTempFile(uri, "voice_note_", ".m4a") ?: return null
    val mediaType = "audio/mp4".toMediaTypeOrNull()
    return MultipartBody.Part.createFormData(partName, file.name, file.asRequestBody(mediaType))
}