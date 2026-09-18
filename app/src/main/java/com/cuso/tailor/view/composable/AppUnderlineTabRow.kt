package com.cuso.tailor.view.composable

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import androidx.compose.material3.SecondaryScrollableTabRow


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
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = tokens.screenPadding,
            containerColor = whiteBg,
            contentColor = Primary,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedIndex),
                    color = Primary,
                    height = indicatorHeight
                )
            },
            divider = {
                HorizontalDivider(color = dividerColor)
            },
            modifier = modifier
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = {
                        onTabSelected(index)
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = tokens.bodySmall,
                            fontWeight = if (selectedIndex == index) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                            color = if (selectedIndex == index) {
                                Primary
                            } else {
                                close_color
                            },
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                )
            }
        }
    } else {
        SecondaryTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = whiteBg,
            contentColor = Primary,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedIndex),
                    color = Primary,
                    height = indicatorHeight
                )
            },
            divider = {
                HorizontalDivider(color = dividerColor)
            },
            modifier = modifier
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = {
                        onTabSelected(index)
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = tokens.bodySmall,
                            fontWeight = if (selectedIndex == index) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                            color = if (selectedIndex == index) {
                                Primary
                            } else {
                                close_color
                            }
                        )
                    }
                )
            }
        }
    }
}