package com.cuso.mobile.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.cuso.mobile.adaptive_screen.AppDesignTokens // டோக்கன்ஸை இம்போர்ட் செய்யவும்
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.close_color
import com.cuso.mobile.ui.theme.title_color
import com.cuso.mobile.ui.theme.whiteBg

@Composable
fun TitleBar(
    title: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(whiteBg)
            // 2. padding changes according to the screen size (16dp to 32dp)
            .padding(horizontal = tokens.screenPadding, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            // 3. replaces tokens.h1 instead of hardcoded values
            fontSize = tokens.h1,
            fontWeight = FontWeight.Bold,
            color = title_color
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = close_color,
            modifier = Modifier
                // 4. Also can change icon size
                .size(if (tokens.isTablet) 28.dp else 24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClose() }
        )
    }
}