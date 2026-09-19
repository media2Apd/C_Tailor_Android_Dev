package com.cuso.tailor.view.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg

/**
 * Reusable collapsible accordion card for grouping form sections.
 * Supports an optional leading icon, smooth expand/collapse animations,
 * and adaptive design tokens.
 */
@Composable
fun FormAccordionCard(
    title: String,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tokens: AppDesignTokens = LocalAppTokens.current,
    containerColor: Color = whiteBg,
    elevation: Dp = 0.5.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "chevronRotation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onHeaderClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                    }
                    Text(
                        text = title,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = title_color
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = mutedText,
                    modifier = Modifier
                        .size(tokens.iconSize * 1.1f)
                        .rotate(arrowRotation)
                )
            }

            // Expandable Content Body
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = tween(250)) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(250)) + fadeOut(animationSpec = tween(150))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(tokens.extraPadding * 1.4f))
                    content()
                }
            }
        }
    }
}