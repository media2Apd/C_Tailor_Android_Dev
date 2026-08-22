package com.cuso.mobile.view.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
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
    onCameraClick: () -> Unit,
    onRemoveImage: (T) -> Unit,
    modifier: Modifier = Modifier,
    browseText: String = "Browse Files",
    cameraText: String = "Camera",
    @DrawableRes cameraIconRes: Int = R.drawable.camera,
    uploadBoxHeight: Dp = 100.dp,
    imagePreviewSize: Dp = 80.dp,
    previewHeaderTitle: String = "SELECTED IMAGES"
) {
    val tokens = LocalAppTokens.current

    Column(modifier = modifier.fillMaxWidth()) {
        // Dashed upload container
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
                // Browse Files Action
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

        // Preview of selected images
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
                items(selectedImages) { imageItem ->
                    Box(
                        modifier = Modifier
                            .size(imagePreviewSize)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius))
                            .background(Color(0xFFF3F4F6))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(tokens.cardCornerRadius)
                            )
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(imageItem),
                            contentDescription = "Design Reference Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Remove badge button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                                .background(Color(0xFFEF4444), CircleShape)
                                .clickable { onRemoveImage(imageItem) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove Image",
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}