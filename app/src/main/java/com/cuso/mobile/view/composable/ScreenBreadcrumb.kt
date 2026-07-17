package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CrumbInactive = Color(0xFF9A9AA8)
private val CrumbActive = Color(0xFF3A2FCB)
private val CrumbBg = Color(0xFFF7F7FA)

/**
 * Dynamic breadcrumb — pass ordered segments, e.g. listOf("Finance", "Trial Balance").
 * Whole row is clickable → opens the Modules bottom-sheet with the FIRST
 * segment's module (e.g. "Finance") auto-expanded and scrolled into view.
 */
@Composable
fun ScreenBreadcrumb(
    segments: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = CrumbBg
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        segments.forEachIndexed { index, label ->
            val isLast = index == segments.lastIndex
            Text(
                text = label,
                color = if (isLast) CrumbActive else CrumbInactive,
                fontSize = 13.sp,
                fontWeight = if (isLast) FontWeight.Medium else FontWeight.Normal
            )
            if (!isLast) {
                Text("  >  ", color = CrumbInactive, fontSize = 13.sp)
            }
        }
    }
}