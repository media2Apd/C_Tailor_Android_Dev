@file:Suppress("unused")

package com.cuso.tailor.view.composable

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.ui.theme.*

/**
 * Reusable image picker component.
 * Displays dashed "Upload Photo" box when empty.
 * Displays preview card with filename, size, and "Change" action when image is selected.
 */
@Composable
fun ReferenceImageUploadField(
    modifier: Modifier = Modifier,
    selectedImageUri: Uri? = null,
    onImageSelected: (Uri) -> Unit,
    label: String? = "Reference Image"
) {
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageSelected(uri)
        }
    }

    val fileDetails = remember(selectedImageUri) {
        selectedImageUri?.let { getUriFileDetails(context, it) }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (!label.isNullOrBlank()) {
            FormLabel(text = label)
        }

        if (selectedImageUri == null) {
            // ── State 1: Dashed Upload Box (Initial State) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .drawBehind {
                        val stroke = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawRoundRect(
                            color = Color(0xFFCBD5E1),
                            cornerRadius = CornerRadius(10.dp.toPx()),
                            style = stroke
                        )
                    }
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "Upload Photo",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Upload Photo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        } else {
            // ── State 2: Uploaded Preview Card ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = whiteBg,
                border = BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Thumbnail Icon Container
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "IMG",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }

                        // File Details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = fileDetails?.fileName ?: "attached_image.jpg",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = fileDetails?.fileMeta ?: "JPG",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Change Action Button
                    Text(
                        text = "Change",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary,
                        modifier = Modifier
                            .clickable { imagePickerLauncher.launch("image/*") }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

private data class UriFileDetails(
    val fileName: String,
    val fileMeta: String
)

private fun getUriFileDetails(context: Context, uri: Uri): UriFileDetails {
    var name = "image.jpg"
    var sizeBytes: Long = 0

    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
            if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
        }
    }

    val extension = name.substringAfterLast(".", "JPG").uppercase()
    val formattedSize = when {
        sizeBytes <= 0 -> ""
        sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
        else -> String.format("%.1f MB", sizeBytes.toDouble() / (1024 * 1024))
    }

    val meta = if (formattedSize.isNotBlank()) "$formattedSize • $extension" else extension
    return UriFileDetails(fileName = name, fileMeta = meta)
}