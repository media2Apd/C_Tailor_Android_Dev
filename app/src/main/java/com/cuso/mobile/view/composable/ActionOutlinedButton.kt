package com.cuso.mobile.view.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.whiteBg

// Reusable outlined action button component
@Composable
fun AddActionOutlinedButton(
    modifier: Modifier = Modifier,
    text: String = "Add Field",
    onClick: () -> Unit,
    icon: ImageVector? = Icons.Default.Add,
    enabled: Boolean = true,
    borderColor: Color = Primary,
    contentColor: Color = Primary,
    containerColor: Color = whiteBg
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.2.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        modifier = modifier.heightIn(min = 34.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) contentColor else contentColor.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            color = if (enabled) contentColor else contentColor.copy(alpha = 0.4f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}