@file:Suppress("unused")
package com.cuso.tailor.view.composable

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun <T> ImageUploadSection(
    modifier: Modifier = Modifier,
    selectedImages: List<T>,
    onBrowseClick: () -> Unit,
    onRemoveImage: (T) -> Unit,
    maxFiles: Int = 5,
    title: String = "Media Upload",
    subtitle: String = "Add your documents here, and you can upload up to $maxFiles files max",
    supportedFormatsText: String = "Only support .jpg, .png and .svg and zip files",
    onClose: (() -> Unit)? = null,
    isImage: Boolean = true,
    browseText: String? = null,
    onCameraClick: (() -> Unit)? = null,
    documentUploadText: String? = null,
    uploadBoxHeight: Dp? = null,
    imagePreviewSize: Dp? = null,
    previewHeaderTitle: String? = null
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val displayTitle = previewHeaderTitle ?: title
    val buttonText = browseText ?: "Browse files"

    // Track simulated upload progress for newly picked items
    val uploadProgressMap = remember { mutableStateMapOf<T, Float>() }
    val pausedItems = remember { mutableStateMapOf<T, Boolean>() }

    // Start progress simulation for newly added items
    LaunchedEffect(selectedImages) {
        selectedImages.forEach { item ->
            if (!uploadProgressMap.containsKey(item)) {
                uploadProgressMap[item] = 0f
                coroutineScope.launch {
                    val progressAnim = Animatable(0f)
                    while (progressAnim.value < 1f) {
                        if (pausedItems[item] != true) {
                            progressAnim.animateTo(
                                targetValue = (progressAnim.value + 0.15f).coerceAtMost(1f),
                                animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                            )
                            uploadProgressMap[item] = progressAnim.value
                        }
                        delay(200)
                    }
                    uploadProgressMap[item] = 1f
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
    ) {
        // ── Header (Title, Subtitle & Dismiss Icon) ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {

                Text(
                    text = subtitle,
                    fontSize = tokens.caption,
                    color = close_color
                )
            }

            if (onClose != null) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextPrimary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }
            }
        }

        Spacer(Modifier.height(tokens.extraPadding))

        // ── Dashed Upload Box ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .dashedBorder(
                    color = Primary,
                    strokeWidth = 1.5.dp,
                    cornerRadius = tokens.cardCornerRadius * 0.7f
                )
                .background(
                    color = primary_light.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.7f)
                )
                .then(
                    if (uploadBoxHeight != null && uploadBoxHeight > 0.dp) Modifier.height(uploadBoxHeight)
                    else Modifier
                )
                .padding(vertical = tokens.extraPadding * 1.5f, horizontal = tokens.screenPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Folder upload icon with circular arrow badge
                Box(
                    modifier = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Upward upload arrow badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(whiteBg.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                Text(
                    text = "Drag your file(s) to start uploading",
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                Spacer(Modifier.height(tokens.extraPadding * 0.6f))

                // OR separator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(180.dp)
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = grey_border,
                        thickness = 1.dp
                    )
                    Text(
                        text = "  OR  ",
                        fontSize = tokens.caption,
                        color = mutedText,
                        fontWeight = FontWeight.Medium
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = grey_border,
                        thickness = 1.dp
                    )
                }

                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                // Outlined "Browse files" button
                OutlinedButton(
                    onClick = onBrowseClick,
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    border = BorderStroke(1.5.dp, Primary),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = whiteBg,
                        contentColor = Primary
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }
            }
        }

        Spacer(Modifier.height(tokens.extraPadding * 0.5f))

        // ── Supported Format Footer ──
        Text(
            text = supportedFormatsText,
            fontSize = tokens.caption,
            color = mutedText,
            modifier = Modifier.padding(start = 2.dp)
        )

        Spacer(Modifier.height(tokens.extraPadding))

        // ── Uploaded / In-Progress Files List ──
        if (selectedImages.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                selectedImages.forEach { item ->
                    val progress = uploadProgressMap[item] ?: 1f
                    val (fileName, fileSize) = getFileDetails(context, item)

                    if (progress < 1f) {
                        // ── Card 1: Uploading in Progress ──
                        UploadingProgressCard(
                            progress = progress,
                            isPaused = pausedItems[item] == true,
                            onTogglePause = {
                                pausedItems[item] = !(pausedItems[item] ?: false)
                            },
                            onCancel = {
                                uploadProgressMap.remove(item)
                                onRemoveImage(item)
                            }
                        )
                    } else {
                        // ── Card 2: Upload Completed File ──
                        CompletedFileCard(
                            fileName = fileName,
                            fileSize = fileSize,
                            onRemove = {
                                uploadProgressMap.remove(item)
                                onRemoveImage(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card displayed while file upload is in progress.
 */
@Composable
private fun UploadingProgressCard(
    progress: Float,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onCancel: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val progressPercent = (progress * 100).toInt()
    val secondsRemaining = ((1f - progress) * 30).toInt().coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
            .padding(tokens.screenPadding * 0.9f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isPaused) "Paused" else "Uploading...",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$progressPercent% • $secondsRemaining seconds remaining",
                    fontSize = tokens.caption,
                    color = mutedText
                )
            }

            // Pause and Cancel icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume Button
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .border(1.dp, BorderGray, CircleShape)
                        .clickable { onTogglePause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Red Cancel Button
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(redBg)
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = redText,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Linear Progress Bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Primary,
            trackColor = grey_border
        )
    }
}

/**
 * Card displayed once the file has uploaded successfully.
 */
@Composable
private fun CompletedFileCard(
    fileName: String,
    fileSize: String,
    onRemove: () -> Unit
) {
    val tokens = LocalAppTokens.current
    val extension = fileName.substringAfterLast('.', "").uppercase()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.6f))
            .padding(horizontal = tokens.screenPadding * 0.9f, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // File Type Badge Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFF7ED)), // Light yellow/orange folder background
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                    if (extension.isNotBlank()) {
                        Text(
                            text = extension.take(4),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text = fileName,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = fileSize,
                    fontSize = tokens.caption,
                    color = mutedText
                )
            }
        }

        // Circular Remove Button
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(1.dp, BorderGray, CircleShape)
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove File",
                tint = mutedText,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * Extracts the file display name and readable size from Uri.
 */
private fun getFileDetails(context: Context, item: Any?): Pair<String, String> {
    var name = "file"
    var size = "Unknown size"
    if (item is Uri) {
        try {
            context.contentResolver.query(item, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: "file"
                    if (sizeIndex >= 0) {
                        val bytes = cursor.getLong(sizeIndex)
                        size = formatBytes(bytes)
                    }
                }
            }
        } catch (_: Exception) {
            name = item.lastPathSegment ?: "file"
        }
    } else {
        name = item?.toString()?.substringAfterLast("/") ?: "file"
    }
    return Pair(name, size)
}

/**
 * Formats raw bytes to human-readable format like '5.3MB'.
 */
private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> String.format(Locale.US, "%.1fMB", mb)
        else -> String.format(Locale.US, "%.0fKB", kb)
    }
}
