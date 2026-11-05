package com.cuso.tailor.view.composable

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Design colors (light theme)
private val ScreenBackground = Color(0xFFF6F8FA)
private val IconTileBackground = Color(0xFFFFF1E0)
private val IconAccent = Color(0xFFB54708)
private val PrimaryText = Color(0xFF14181F)
private val SecondaryText = Color(0xFF5B6672)
private val CardBorder = Color(0xFFE1E6EB)
private val ButtonColor = Color(0xFF2F27CE)

@Composable
fun DeveloperModeBlockScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Rounded icon tile with the "</>" glyph
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(IconTileBackground, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            CodeIcon(modifier = Modifier.size(44.dp))
        }

        // Title and description
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Developer mode is on",
                color = PrimaryText,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "For your security, this app can't open while Developer Mode is enabled. " +
                        "Turn it off from Settings, then come back.",
                color = SecondaryText,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center
            )
        }

        // Steps card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StepRow(number = "1", text = "Open Settings")
                StepRow(number = "2", text = "Go to System › Developer options")
                StepRow(number = "3", text = "Switch Developer mode off")
            }
        }

        // Primary action: open the developer options screen
        Button(
            onClick = {
                try {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                } catch (_: Exception) {
                    // Fall back to the main Settings screen if developer options can't be opened
                    context.startActivity(
                        Intent(Settings.ACTION_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonColor,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Open Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Single numbered step inside the card
@Composable
private fun StepRow(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dark circle holding the step number
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(PrimaryText, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(text = text, color = PrimaryText, fontSize = 14.sp)
    }
}

// "</>" icon drawn on a canvas (24x24 design grid), so no extra icon dependency is needed
@Composable
private fun CodeIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 24f
        val stroke = Stroke(
            width = 1.8f * s,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Left chevron "<"
        val left = Path().apply {
            moveTo(8f * s, 7f * s)
            lineTo(3f * s, 12f * s)
            lineTo(8f * s, 17f * s)
        }
        // Right chevron ">"
        val right = Path().apply {
            moveTo(16f * s, 7f * s)
            lineTo(21f * s, 12f * s)
            lineTo(16f * s, 17f * s)
        }

        drawPath(left, IconAccent, style = stroke)
        drawPath(right, IconAccent, style = stroke)

        // Slash "/" in the middle
        drawLine(
            color = IconAccent,
            start = Offset(13.5f * s, 5f * s),
            end = Offset(10.5f * s, 19f * s),
            strokeWidth = 1.8f * s,
            cap = StrokeCap.Round
        )
    }
}