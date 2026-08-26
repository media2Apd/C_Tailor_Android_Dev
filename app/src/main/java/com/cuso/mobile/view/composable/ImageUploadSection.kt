package com.cuso.mobile.view.composable

import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.whiteBg

@Composable
fun <T> ImageUploadSection(
    selectedImages: List<T>,
    onBrowseClick: () -> Unit,
    onCameraClick: (() -> Unit)? = null,
    onRemoveImage: (T) -> Unit,
    modifier: Modifier = Modifier,
    browseText: String = "Browse Files",
    cameraText: String = "Camera",
    @DrawableRes cameraIconRes: Int = R.drawable.camera,
    uploadBoxHeight: Dp = 100.dp,
    imagePreviewSize: Dp = 86.dp,
    previewHeaderTitle: String = "ATTACHED FILES"
) {
    val tokens = LocalAppTokens.current
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Upload Action Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(uploadBoxHeight)
                .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius))
                .dashedBorder(
                    color = PrimaryBorder,
                    strokeWidth = 1.dp,
                    shape = RoundedCornerShape(tokens.cardCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Browse Action
                Text(
                    text = browseText,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { onBrowseClick() }
                        .padding(8.dp)
                )

                if (onCameraClick != null) {
                    Spacer(modifier = Modifier.width(24.dp))

                    // Camera Action
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
                            tint = Color(0xFF6B7280)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = cameraText,
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }

        // Preview row for all file types
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
                                .background(Color(0xFFEF4444))
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

// Helper methods for identifying file types and icons
private fun isDocumentFile(context: android.content.Context, item: Any?): Boolean {
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

private fun getDisplayName(context: android.content.Context, item: Any?): String {
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

private fun getDocumentIcon(extension: String): androidx.compose.ui.graphics.vector.ImageVector {
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