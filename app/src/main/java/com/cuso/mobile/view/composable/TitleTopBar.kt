package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.close_color
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg

@Composable
fun TitleBar(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    // Optional Slot: You can pass any custom composable here (Text, Badge, Button, RadioButton, etc.)
    trailingContent: (@Composable RowScope.() -> Unit)? = null
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(whiteBg)
            .padding(horizontal = tokens.screenPadding, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Title text
        Text(
            text = title,
            fontSize = tokens.h1,
            fontWeight = FontWeight.Bold,
            color = title_color,
            modifier = Modifier.weight(1f, fill = false)
        )

        // Right side container (Custom Slot Content + Close Button)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Render custom content dynamically if provided
            trailingContent?.invoke(this)

            // Close button
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