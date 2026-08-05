package com.cuso.mobile.view.home.reusablecomposables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.whiteBg

// ─────────────────────────────────────────────────────────────
// Reusable Settings Tabs Component - Add this to a separate file or at the top
// ─────────────────────────────────────────────────────────────

/**
 * Data class for tab items
 */
data class TabItem(
    val label: String,
    val icon: ImageVector,
    val badge: String? = null
)

/**
 * Reusable settings tabs component with customizable options
 */
@Composable
fun SettingsTabs(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = whiteBg,
    selectedBackgroundColor: Color = Color(0xFFEEF0FF),
    selectedTextColor: Color = Primary,
    unselectedTextColor: Color = TextSecondary,
    selectedIconColor: Color = Primary,
    unselectedIconColor: Color = TextSecondary,
    borderColor: Color = Color(0xFFE5E7EB),
    cornerRadius: Dp = 12.dp,
    selectedCornerRadius: Dp = 10.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(cornerRadius))
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            val isSelected = selectedIndex == index
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) selectedBackgroundColor else Color.Transparent,
                        RoundedCornerShape(selectedCornerRadius)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = tab.label,
                    tint = if (isSelected) selectedIconColor else unselectedIconColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = tab.label,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) selectedTextColor else unselectedTextColor
                )
                if (tab.badge != null) {
                    Spacer(Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) selectedTextColor else unselectedTextColor,
                                CircleShape
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tab.badge,
                            fontSize = 10.sp,
                            color = whiteBg,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}