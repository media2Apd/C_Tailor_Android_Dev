package com.cuso.tailor.view.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cuso.tailor.R
import com.cuso.tailor.adaptive_screen.getAdaptiveTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.lightGray
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.whiteBg

/**
 * Update screen.
 *
 * @param onUpdateClick  "Explore Updates" button click
 * @param onClose        pass a lambda to show the X button (top-right).
 *                       Keep it null for a hard force-update (no way to dismiss).
 */
@Composable
fun ForceUpdateBlockScreen(
    onUpdateClick: () -> Unit,
    onClose: (() -> Unit)? = null,
    widthSize: WindowWidthSizeClass = rememberWidthSizeClass()
) {
    val tokens = getAdaptiveTokens(widthSize)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(whiteBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = tokens.screenPadding)
            .padding(top = tokens.extraPadding, bottom = tokens.screenPadding)
    ) {
        // ---------- Scrollable content (keeps small screens safe) ----------
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                if (onClose != null) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(tokens.fieldHeight + 4.dp) // keeps touch target >= 44dp
                            .clip(CircleShape)
                            .background(lightGray)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextPrimary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(tokens.fieldHeight + 4.dp))
                }
            }

            // Illustration
            Image(
                painter = painterResource(id = R.drawable.update_screen),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tokens.imageDp * 3)
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding))

            // Title
            Text(
                text = "New Update!",
                color = Primary,
                fontSize = tokens.h1,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(tokens.extraPadding))

            // Description
            Text(
                text = "We’ve added new features and improvements to make your experience better than ever.",
                color = TextPrimary,
                fontSize = tokens.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = tokens.extraPadding)
            )

            Spacer(modifier = Modifier.height(tokens.screenPadding * 2))

            // What's New card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius + 5.dp))
                    .background(lightGray)
                    .padding(tokens.cardPadding)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                    Text(
                        text = "What’s New?",
                        color = Primary,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(tokens.screenPadding))

                WhatsNewItem(
                    title = "Faster performance",
                    subtitle = "Enjoy a smoother experience",
                    titleSize = tokens.bodyMedium,
                    subtitleSize = tokens.bodySmall
                )

                HorizontalDivider(
                    modifier = Modifier.padding(
                        horizontal = tokens.extraPadding,
                        vertical = tokens.screenPadding
                    ),
                    thickness = 1.dp,
                    color = sectionBorder
                )

                WhatsNewItem(
                    title = "Enhanced features",
                    subtitle = "Get more done with ease",
                    titleSize = tokens.bodyMedium,
                    subtitleSize = tokens.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(tokens.screenPadding))

        // ---------- Primary button ----------
        Button(
            onClick = onUpdateClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(tokens.buttonHeight + 8.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = whiteBg
            )
        ) {
            Text(
                text = "Updates Now",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.size(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(tokens.iconSize)
            )
        }
    }
}

@Composable
private fun WhatsNewItem(
    title: String,
    subtitle: String,
    titleSize: androidx.compose.ui.unit.TextUnit,
    subtitleSize: androidx.compose.ui.unit.TextUnit
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Primary)
        )
        Spacer(modifier = Modifier.size(10.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = titleSize,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = subtitleSize
            )
        }
    }
}

/** Fallback so you don't have to pass the size class from the Activity. */
@Composable
private fun rememberWidthSizeClass(): WindowWidthSizeClass {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp < 600 -> WindowWidthSizeClass.Compact
        widthDp < 840 -> WindowWidthSizeClass.Medium
        else -> WindowWidthSizeClass.Expanded
    }
}