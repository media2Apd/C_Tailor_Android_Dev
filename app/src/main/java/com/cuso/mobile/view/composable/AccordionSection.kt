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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.Primary
import com.cuso.mobile.ui.theme.PrimaryBorder
import com.cuso.mobile.ui.theme.TextSecondary
import com.cuso.mobile.ui.theme.title_color

/**
 * Adaptive Accordion section:
 * - Adapts icon sizes, chevrons, and spacing automatically across phones and tablets.
 * - Trailing content (badges/switches) stays responsive.
 * - If showArrow is false, trailing content moves to the far right.
 */
@Composable
fun AccordionSection(
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    title: String,
    subtitle: String? = null,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    iconTint: Color = Primary,
    trailing: @Composable (() -> Unit)? = null,
    showArrow: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalAppTokens.current

    val arrowRotationByAnim by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "chevron_rotation"
    )

    val trailingReplacesArrow = trailing != null && !showArrow

    // Adaptive icon dimension calculation
    val leadingIconSize = tokens.iconSize * 1.1f
    val arrowIconSize = if (tokens.isTablet) 28.dp else 24.dp
    Spacer(Modifier.height(tokens.screenPadding))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onHeaderClick() }
                .padding(
                    horizontal = tokens.screenPadding,
                    vertical = tokens.extraPadding * 1.2f
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Adaptive Leading Icon
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(leadingIconSize)
                )
                Spacer(Modifier.width(tokens.extraPadding))
            } else if (iconPainter != null) {
                Icon(
                    painter = iconPainter,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(leadingIconSize)
                )
                Spacer(Modifier.width(tokens.extraPadding))
            }

            // Title & Subtitle Column
            Column {
                Text(
                    text = title,
                    fontSize = tokens.bodyLarge,
                    color = title_color
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                }
            }

            // Inline Badge / Switch next to title
            if (trailing != null && !trailingReplacesArrow) {
                Spacer(Modifier.width(tokens.extraPadding))
                trailing()
            }

            Spacer(Modifier.weight(1f))

            // Far-right element: Replaced trailing widget OR Adaptive Chevron
            if (trailingReplacesArrow) {
                trailing.invoke()
            } else if (showArrow) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(arrowIconSize)
                        .rotate(arrowRotationByAnim)
                )
            }
        }

        // Expanded Content Area
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding)
                    .padding(bottom = tokens.extraPadding * 1.2f)
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