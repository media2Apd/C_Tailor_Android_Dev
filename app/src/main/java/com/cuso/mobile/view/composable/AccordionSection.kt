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
 * An adaptive Accordion section with smooth expansion and icon rotation animations.
 */
@Composable
fun AccordionSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    iconPainter: androidx.compose.ui.graphics.painter.Painter? = null,
    title: String,
    subtitle: String? = null,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    iconTint: Color = Primary,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    // Animate the chevron rotation (0 to 180 degrees)
    val arrowRotationByAnim by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "chevron_rotation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onHeaderClick() }
                // Modern vertical padding with adaptive horizontal padding
                .padding(horizontal = tokens.screenPadding, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Logic
                if (icon != null) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                } else if (iconPainter != null) {
                    Icon(iconPainter, null, tint = iconTint, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                }

                // Title and Subtitle
                Column {
                    Text(
                        text = title,
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TitleColor
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Trailing / Arrow Logic
            Box(contentAlignment = Alignment.Center) {
                if (trailing != null) {
                    trailing()
                } else {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(arrowRotationByAnim) // Smooth rotation
                    )
                }
            }
        }

        // Expanded Content with smooth vertical slide + fade
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Align internal content padding with the header
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