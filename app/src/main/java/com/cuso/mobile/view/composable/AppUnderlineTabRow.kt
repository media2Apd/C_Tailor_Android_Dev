package com.cuso.mobile.view.composable

import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*

/**
 * Clean, enterprise-grade reusable Underline Tab Row.
 *
 * @param tabs List of tab title strings
 * @param selectedIndex Currently selected tab index
 * @param onTabSelected Callback invoked when a tab is clicked
 * @param indicatorHeight Height of the bottom underline indicator (default 2.5.dp)
 * @param isScrollable True for scrollable tabs, false for equal-width fixed tabs
 */
@Composable
fun AppUnderlineTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isScrollable: Boolean = false,
    indicatorHeight: Dp = 2.5.dp
) {
    val tokens = LocalAppTokens.current

    if (isScrollable) {
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = tokens.screenPadding,
            containerColor = whiteBg,
            contentColor = Primary,
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = Primary,
                        height = indicatorHeight
                    )
                }
            },
            divider = { HorizontalDivider(color = dividerColor) },
            modifier = modifier
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            fontSize = tokens.bodySmall,
                            fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedIndex == index) Primary else close_color
                        )
                    }
                )
            }
        }
    } else {
        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = whiteBg,
            contentColor = Primary,
            indicator = { tabPositions ->
                if (selectedIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = Primary,
                        height = indicatorHeight
                    )
                }
            },
            divider = { HorizontalDivider(color = dividerColor) },
            modifier = modifier
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            fontSize = tokens.bodySmall,
                            fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedIndex == index) Primary else close_color
                        )
                    }
                )
            }
        }
    }
}