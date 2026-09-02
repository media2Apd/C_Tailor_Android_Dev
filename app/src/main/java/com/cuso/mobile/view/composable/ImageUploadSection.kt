@file:Suppress("unused")
package com.cuso.mobile.view.composable

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.redText

@Composable
fun <T> ImageUploadSection(
    modifier: Modifier = Modifier,
    isImage: Boolean = true,
    selectedImages: List<T>,
    onBrowseClick: () -> Unit,
    onCameraClick: (() -> Unit)? = null,
    onRemoveImage: (T) -> Unit,
    browseText: String = "Browse Files",
    cameraText: String = "Camera",
    documentUploadText: String = "Drag and drop files here",
    @DrawableRes cameraIconRes: Int = R.drawable.ic_camera,
    @DrawableRes uploadIconRes: Int = R.drawable.ic_upload,
    uploadBoxHeight: Dp = if (isImage) 100.dp else 130.dp,
    imagePreviewSize: Dp = 86.dp,
    previewHeaderTitle: String = "ATTACHED FILES"
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Upload Action Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(uploadBoxHeight)
                .background(
                    color = Color(0xFFF1F5FD),
                    shape = RoundedCornerShape(tokens.cardCornerRadius)
                )
                .dashedBorder(
                    color = Primary,
                    strokeWidth = 1.dp,
                    shape = RoundedCornerShape(tokens.cardCornerRadius)
                )
                .then(
                    if (!isImage) Modifier.clickable { onBrowseClick() } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isImage) {
                // Image Upload Layout: Browse & Camera Options
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = browseText,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Primary,
                        modifier = Modifier
                            .clickable { onBrowseClick() }
                            .padding(8.dp)
                            .drawBehind {
                                val strokeWidthPx = 0.5.dp.toPx()
                                val verticalOffset = 2.dp.toPx()
                                val y = size.height - verticalOffset

                                drawLine(
                                    color = Primary,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = strokeWidthPx
                                )
                            }
                    )

                    if (onCameraClick != null) {
                        Spacer(modifier = Modifier.width(24.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onCameraClick() }
                                .padding(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(cameraIconRes),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = cameraText,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Primary
                            )
                        }
                    }
                }
            } else {
                // Document Upload Layout: Cloud Upload Card & Text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = Color.Black.copy(alpha = 0.05f),
                                spotColor = Color.Black.copy(alpha = 0.05f)
                            )
                            .background(Color.White, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(uploadIconRes),
                            contentDescription = "Upload Document",
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = documentUploadText,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF475569)
                    )
                }
            }
        }

        // Preview Row for Selected Files / Images
        if (selectedImages.isNotEmpty()) {
            Spacer(Modifier.height(tokens.extraPadding))
            Text(
                text = "$previewHeaderTitle (${selectedImages.size})",
                fontSize = tokens.caption,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(tokens.extraPadding))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(selectedImages) { item ->
                    val isDoc = isDocumentFile(context, item)
                    val fileName = getDisplayName(context, item)
                    val extension = getFileExtension(fileName)

                    Box(
                        modifier = Modifier
                            .size(imagePreviewSize)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(Color(0xFFF8FAFC))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(tokens.cardCornerRadius)
                            )
                    ) {
                        if (isDoc) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = getDocumentIcon(extension),
                                    contentDescription = null,
                                    tint = getDocumentColor(extension),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = fileName,
                                    fontSize = 9.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF334155),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(item),
                                contentDescription = "Attachment Preview",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Remove Button Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(redText)
                                .clickable { onRemoveImage(item) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Item",
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T> VoiceUploadSection(
    modifier: Modifier = Modifier,
    isRecording: Boolean,
    recordedAudios: List<T>,
    onRecordClick: () -> Unit,
    onRemoveAudio: (T) -> Unit,
    headerTitle: String = "RECORDED VOICE NOTES",
    recordBoxHeight: Dp = 120.dp
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    // Audio Playback State
    var currentlyPlayingItem by remember { mutableStateOf<T?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    fun stopAudio() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        isPlaying = false
        currentlyPlayingItem = null
    }

    fun togglePlayAudio(item: T) {
        if (currentlyPlayingItem == item && isPlaying) {
            stopAudio()
        } else {
            stopAudio()
            try {
                val player = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    when (item) {
                        is Uri -> setDataSource(context, item)
                        is String -> setDataSource(item)
                    }
                    prepare()
                }

                mediaPlayer = player
                currentlyPlayingItem = item
                isPlaying = true

                player.setOnCompletionListener {
                    stopAudio()
                }
                player.start()
            } catch (e: Exception) {
                e.printStackTrace()
                stopAudio()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopAudio()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Voice Recording Action Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(recordBoxHeight)
                .background(
                    color = Color(0xFFF1F5FD),
                    shape = RoundedCornerShape(tokens.cardCornerRadius)
                )
                .dashedBorder(
                    color = Primary,
                    strokeWidth = 1.dp,
                    shape = RoundedCornerShape(tokens.cardCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Record/Stop Action Button
                Button(
                    onClick = {
                        stopAudio()
                        onRecordClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) redText else Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isRecording) Color.White else Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isRecording) "Stop Recording" else "Start Recording",
                        color = if (isRecording) Color.White else Primary,
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isRecording) "Recording in progress..." else "Tap to record voice instructions",
                    fontSize = 12.sp,
                    color = if (isRecording) redText else Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Full Width Vertical List for Recorded Audio Items
        if (recordedAudios.isNotEmpty()) {
            Spacer(Modifier.height(tokens.extraPadding))
            Text(
                text = "$headerTitle (${recordedAudios.size})",
                fontSize = tokens.caption,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(tokens.extraPadding))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                recordedAudios.forEachIndexed { index, audioItem ->
                    val fileName = getDisplayName(context, audioItem)
                    val isItemPlaying = currentlyPlayingItem == audioItem && isPlaying

                    // Full-width Audio Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(Color(0xFFF8FAFC))
                            .border(
                                width = 1.dp,
                                color = if (isItemPlaying) Primary else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(tokens.cardCornerRadius)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Interactive Play/Pause/Waveform Button
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isItemPlaying) Primary else Primary.copy(alpha = 0.1f)
                                )
                                .clickable { togglePlayAudio(audioItem) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isItemPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isItemPlaying) "Pause" else "Play",
                                tint = if (isItemPlaying) Color.White else Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Audio Title / Details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (fileName == "File" || fileName.isBlank()) "Voice Note ${index + 1}" else fileName,
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1E293B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isItemPlaying) "Playing..." else "Recorded audio • Tap to play",
                                fontSize = 11.sp,
                                color = if (isItemPlaying) Primary else Color(0xFF16A34A),
                                fontWeight = FontWeight.Normal
                            )
                        }

                        // Remove Audio Button
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2))
                                .clickable {
                                    if (currentlyPlayingItem == audioItem) stopAudio()
                                    onRemoveAudio(audioItem)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete Voice Note",
                                tint = redText,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Shared Helper Methods
private fun isDocumentFile(context: Context, item: Any?): Boolean {
    if (item is Uri) {
        val type = context.contentResolver.getType(item)
        if (type != null) {
            return !type.startsWith("image/")
        }
        val name = getDisplayName(context, item).lowercase()
        return !name.endsWith(".jpg") && !name.endsWith(".jpeg") && !name.endsWith(".png") && !name.endsWith(".webp")
    }
    val path = item.toString().lowercase()
    return !path.endsWith(".jpg") && !path.endsWith(".jpeg") && !path.endsWith(".png") && !path.endsWith(".webp")
}

private fun getDisplayName(context: Context, item: Any?): String {
    if (item is Uri) {
        context.contentResolver.query(item, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                return cursor.getString(nameIndex)
            }
        }
    }
    return item?.toString()?.substringAfterLast("/") ?: "File"
}

private fun getFileExtension(fileName: String): String {
    return fileName.substringAfterLast('.', "").lowercase()
}

private fun getDocumentIcon(extension: String): ImageVector {
    return when (extension) {
        "pdf" -> Icons.Outlined.PictureAsPdf
        "doc", "docx" -> Icons.Outlined.Description
        "xls", "xlsx", "csv" -> Icons.Outlined.TableChart
        "zip", "rar" -> Icons.Outlined.FolderZip
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

private fun getDocumentColor(extension: String): Color {
    return when (extension) {
        "pdf" -> Color(0xFFE53935)
        "doc", "docx" -> Color(0xFF2563EB)
        "xls", "xlsx", "csv" -> Color(0xFF059669)
        "zip", "rar" -> Color(0xFFD97706)
        else -> Primary
    }
}