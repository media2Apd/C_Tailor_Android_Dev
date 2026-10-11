package com.cuso.tailor.view.composable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.whiteBg

// ─────────────────────────────────────────────────────────────
// Reusable Settings Tabs Component
// ─────────────────────────────────────────────────────────────

/**
 * Data class for tab items.
 * icon -> use this if you have a Material ImageVector
 * iconPainter -> use this if you have a drawable resource (vector drawable xml etc.)
 * Both are optional; pass whichever one you have. If both are null, no icon is shown.
 */
data class TabItem(
    val label: String,
    val icon: ImageVector? = null,
    @param:DrawableRes val iconPainter: Int? = null,
    val badge: String? = null
)

/**
 * Reusable tabs component with a fixed height.
 *
 * Behavior:
 * - Few tabs: every tab gets an equal share of the full width.
 * - Many tabs: tabs keep their natural width and the row scrolls horizontally.
 * - The selected tab is scrolled into view automatically.
 */
@Composable
fun SettingsTabs(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    containerColor: Color = whiteBg,
    selectedBackgroundColor: Color = primary_light,
    selectedTextColor: Color = Primary,
    unselectedTextColor: Color = TextSecondary,
    selectedIconColor: Color = Primary,
    unselectedIconColor: Color = TextSecondary,
    borderColor: Color = grey_border,
    cornerRadius: Dp = 12.dp,
    selectedCornerRadius: Dp = 10.dp
) {
    if (tabs.isEmpty()) return

    // Inner spacing of the container and the gap between tabs
    val containerPadding = 4.dp
    val tabSpacing = 4.dp

    val containerShape = RoundedCornerShape(cornerRadius)
    val tabShape = RoundedCornerShape(selectedCornerRadius)

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Left offset and width (in px) of every tab inside the scrollable row
    val tabBounds = remember { mutableStateMapOf<Int, Pair<Int, Int>>() }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(containerShape)
            .background(containerColor, containerShape)
    ) {
        // Width each tab needs so that a small number of tabs fills the full width
        val equalTabWidth = (maxWidth - containerPadding * 2 - tabSpacing * (tabs.size - 1)) / tabs.size
        val viewportPx = with(density) { maxWidth.roundToPx() }

        // Keep the selected tab visible (centered when possible)
        val selectedBounds = tabBounds[selectedIndex]
        LaunchedEffect(selectedIndex, selectedBounds, viewportPx) {
            selectedBounds?.let { (left, width) ->
                val target = left - (viewportPx - width) / 2
                scrollState.animateScrollTo(target.coerceIn(0, scrollState.maxValue))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .horizontalScroll(scrollState)
                .padding(containerPadding),
            horizontalArrangement = Arrangement.spacedBy(tabSpacing)
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedIndex == index
                val tint = if (isSelected) selectedIconColor else unselectedIconColor

                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        // Equal share when few tabs, grows with content when many tabs
                        .widthIn(min = equalTabWidth)
                        .onGloballyPositioned { coordinates ->
                            tabBounds[index] = coordinates.positionInParent().x.toInt() to coordinates.size.width
                        }
                        .clip(tabShape)
                        .background(if (isSelected) selectedBackgroundColor else Color.Transparent, tabShape)
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Icon (ImageVector) takes priority, otherwise fall back to the drawable resource
                    when {
                        tab.icon != null -> {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = tint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        tab.iconPainter != null -> {
                            Icon(
                                painter = painterResource(id = tab.iconPainter),
                                contentDescription = tab.label,
                                tint = tint,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                    }

                    Text(
                        text = tab.label,
                        fontSize = 14.sp,
                        maxLines = 1,
                        softWrap = false,
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
}