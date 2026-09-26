package com.cuso.tailor.view.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.sectionBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionRowButtons(
    modifier: Modifier = Modifier,
    // Custom Label Parameters with Default Values
    editText: String = "Edit",
    previewPdfText: String = "Preview PDF",
    downloadDescription: String = "Download",
    convertToBillText: String = "Convert to Bill",
    // Actions & Callbacks
    onEditClick: () -> Unit,
    onPreviewPdfClick: () -> Unit,
    onDownloadClick: () -> Unit = {},
    onConvertToBillClick: () -> Unit,
    // States & Styles
    isConvertToBillEnabled: Boolean = true,
    buttonHeight: Dp = 40.dp,
    primaryColor: Color = Primary,
    borderColor: Color = sectionBorder,
    textColor: Color = Color(0xFF1E2238)
) {
    // Material 3-ன் 48.dp default minimum touch target-ஐ override செய்து exact 40.dp height வரவைக்க
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentEnforcement provides false
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Edit Button
            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = textColor
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                modifier = Modifier.height(buttonHeight)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = editText,
                    modifier = Modifier.size(15.dp),
                    tint = Color(0xFF475569)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = editText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // 2. Preview PDF Button
            OutlinedButton(
                onClick = onPreviewPdfClick,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = textColor
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(buttonHeight)
            ) {
                Text(
                    text = previewPdfText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Outlined.Visibility,
                    contentDescription = previewPdfText,
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF475569)
                )
            }

            // 3. Download Button (Square 40.dp x 40.dp)
            OutlinedButton(
                onClick = onDownloadClick,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = textColor
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(buttonHeight)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = downloadDescription,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF475569)
                )
            }

            // 4. Convert to Bill / Primary Action Button
            Button(
                onClick = onConvertToBillClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                ),
                enabled = isConvertToBillEnabled,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(buttonHeight)
                    .weight(1f)
            ) {
                Text(
                    text = convertToBillText,
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}