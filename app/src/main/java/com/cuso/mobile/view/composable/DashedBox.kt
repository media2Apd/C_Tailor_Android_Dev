@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead",
    "unused_variable",
    "unused_parameter",
    "UnusedMaterial3ScaffoldPaddingParameter"
)
package com.cuso.mobile.view.composable

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.home.LeadPrimary
import com.cuso.mobile.view.home.LeadPrimarySoft

// DashedBorderUploadBox.kt

fun Modifier.dashedBorder(
    color: Color,
    shape: Shape = RoundedCornerShape(12.dp),
    strokeWidth: Dp = 1.5.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp,
    cornerRadius: Dp = 12.dp
): Modifier = this.drawBehind {
    val strokeWidthPx = strokeWidth.toPx()
    val halfStroke = strokeWidthPx / 2f

    val stroke = Stroke(
        width = strokeWidthPx,
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f
        )
    )

    drawRoundRect(
        color = color,
        topLeft = Offset(halfStroke, halfStroke),
        size = Size(
            width = size.width - strokeWidthPx,
            height = size.height - strokeWidthPx
        ),
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}

@Composable
fun DashedUploadBox(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.AddAPhoto,
    title: String = "Drag & drop photos or files here",
    subtitle: String = "Supports JPG, PNG, PDF (Max 10MB)",
    actionText: String = "Browse Files",
    borderColor: Color = Color(0xFF5B4FE9),
    onBrowseClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .dashedBorder(color = borderColor)
            .background(Color(0xFFF7F8FA), RoundedCornerShape(12.dp))
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = borderColor,
            modifier = Modifier.size(28.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1A1A1A)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9CA3AF)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = actionText,
            style = MaterialTheme.typography.bodyMedium,
            color = borderColor,
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onBrowseClick() }
        )
    }
}

@Composable
fun rememberFilePickerLauncher(
    onFileSelected: (Uri) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) { }

            onFileSelected(it)
        }
    }

    return {
        launcher.launch(
            arrayOf("image/jpeg", "image/png", "application/pdf")
        )
    }
}

@Composable
fun SelectableChipRow(
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    multiSelect: Boolean = true,
    selectedColor: Color = LeadPrimary,
    selectedBackground: Color = LeadPrimarySoft,
    unselectedBorderColor: Color = grey_border,
    unselectedTextColor: Color = Color(0xFF374151)
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { option ->
            val isSelected = selectedOptions.contains(option)

            Box(
                modifier = Modifier
                    .border(
                        1.dp,
                        if (isSelected) selectedColor else unselectedBorderColor,
                        RoundedCornerShape(50.dp)
                    )
                    .background(
                        if (isSelected) selectedBackground else whiteBg,
                        RoundedCornerShape(50.dp)
                    )
                    .clickable {
                        val updated = when {
                            !multiSelect -> listOf(option)
                            isSelected -> selectedOptions.filter { it != option }
                            else -> selectedOptions + option
                        }
                        onSelectionChange(updated)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = selectedColor
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = option,
                        fontSize = 13.sp,
                        color = if (isSelected) selectedColor else unselectedTextColor,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}