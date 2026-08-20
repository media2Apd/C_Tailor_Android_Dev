package com.cuso.mobile.view.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.mutedText

private val CrumbInactive = mutedText
private val CrumbActive = Primary

/**
 * Dynamic breadcrumb — pass ordered segments, e.g. listOf("Finance", "Trial Balance").
 * Whole row is clickable → opens the Modules bottom-sheet with the FIRST
 * segment's module (e.g. "Finance") auto-expanded and scrolled into view.
 */
@Composable
fun ScreenBreadcrumb(
    segments: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = tokens.screenPadding, vertical = 12.dp)
    ) {
        segments.forEachIndexed { index, label ->
            val isLast = index == segments.lastIndex
            Text(
                text = label,
                color = if (isLast) CrumbActive else CrumbInactive,
                fontSize = tokens.caption,
                fontWeight = if (isLast) FontWeight.Medium else FontWeight.Normal
            )
            if (!isLast) {
                Text("  >  ", color = CrumbInactive, fontSize = tokens.caption)
            }
        }
    }
}