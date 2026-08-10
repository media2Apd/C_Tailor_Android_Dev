package com.cuso.mobile.view.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*

/**
 * Updated Accordion section:
 * 1. Trailing content (badges) appears next to the Title.
 * 2. Dropdown arrow stays at the far right.
 */
@Composable
fun AccordionSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    title: String,
    subtitle: String? = null,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    iconTint: Color = Color(0xFF5A57D6),
    trailing: @Composable (() -> Unit)? = null, // Used for the badge next to title
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    val arrowRotationByAnim by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "chevron_rotation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onHeaderClick() }
                .padding(horizontal = tokens.screenPadding, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Icon
            if (icon != null) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
            } else if (iconPainter != null) {
                Icon(iconPainter, null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
            }

            // Title Column
            Column {
                Text(
                    text = title,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF111827)
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(text = subtitle, fontSize = tokens.caption, color = TextSecondary)
                }
            }

            // --- Badge / Value next to Header ---
            if (trailing != null) {
                Spacer(Modifier.width(12.dp))
                trailing()
            }

            // Spacer to push the arrow to the far right
            Spacer(Modifier.weight(1f))

            // Dropdown Arrow Logic
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = TextSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(arrowRotationByAnim)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
                    .padding(bottom = 16.dp)
            ) {
                content()
            }
        }

        HorizontalDivider(
            color = PrimaryBorder,
            modifier = Modifier.padding(horizontal = tokens.screenPadding)
        )
    }
}