package com.cuso.tailor.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.close_color
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg

@Composable
fun TitleBar(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    onClose: () -> Unit,
    // Optional Slot: Custom composable content (Badges, Buttons, Actions, etc.)
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = tokens.screenPadding, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically, // close button vertical center
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left: Title (row 1) + Subtitle (row 2)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = tokens.h1,
                fontWeight = FontWeight.Bold,
                color = title_color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = tokens.bodySmall,
                    color = title_color
                )
            }
        }

        // Right: Custom slot content + Close button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            trailingContent?.invoke(this)

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = close_color,
                modifier = Modifier
                    .size(if (tokens.isTablet) 28.dp else 24.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClose() }
            )
        }
    }
}