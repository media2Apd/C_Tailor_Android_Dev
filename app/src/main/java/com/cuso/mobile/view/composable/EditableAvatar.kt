package com.cuso.mobile.view.composable

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cuso.mobile.ui.theme.whiteBg

@Composable
fun EditableAvatar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUri: Uri? = null,
    imageUrl: String? = null,
    initials: String? = null,
    isUploading: Boolean = false,
    isReadOnly: Boolean = false,
    avatarSize: Dp = 80.dp,
    backgroundColor: Color = Color.Gray,
    badgeIcon: ImageVector = Icons.Default.Edit,
    badgeIconTint: Color = Color(0xFF1E2238),
    badgeBgColor: Color = whiteBg,
    badgeBorderColor: Color = Color(0xFF1E2238)
) {
    Box(
        modifier = modifier.size(avatarSize),
        contentAlignment = Alignment.Center
    ) {
        // ── Main Circular Avatar ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable(enabled = !isReadOnly) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            when {
                isUploading -> {
                    CirculerProgressIndicatorSmall()
                }
                imageUri != null -> {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }
                !imageUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                }
                !initials.isNullOrBlank() && initials != "?" -> {
                    Text(
                        text = initials,
                        color = whiteBg,
                        fontSize = (avatarSize.value * 0.32f).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Upload Photo",
                        tint = whiteBg,
                        modifier = Modifier.size(avatarSize * 0.45f)
                    )
                }
            }
        }

        // ── Bottom-End Floating Badge ──
        if (!isReadOnly) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(avatarSize * 0.32f)
                    .clip(CircleShape)
                    .background(badgeBgColor)
                    .border(1.5.dp, badgeBorderColor, CircleShape)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = badgeIcon,
                    contentDescription = "Edit Photo",
                    tint = badgeIconTint,
                    modifier = Modifier.size(avatarSize * 0.16f)
                )
            }
        }
    }
}
