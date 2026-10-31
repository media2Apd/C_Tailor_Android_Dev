package com.cuso.tailor.view.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

const val SESSION_EXPIRED_DEFAULT_MESSAGE =
    "You were signed out after a period of inactivity. Please log in again to continue where you left off."

private data class SessionDialogColors(
    val card: Color,
    val border: Color,
    val title: Color,
    val body: Color,
    val button: Color,
    val buttonText: Color,
    val iconBg: Color,
    val icon: Color
)

private val LightColors = SessionDialogColors(
    card = Color(0xFFFFFFFF),
    border = Color(0xFFE3E7EE),
    title = Color(0xFF111827),
    body = Color(0xFF4B5563),
    button = Color(0xFF2F5BEA),
    buttonText = Color(0xFFFFFFFF),
    iconBg = Color(0xFFFEF3C7),
    icon = Color(0xFFB45309)
)

private val DarkColors = SessionDialogColors(
    card = Color(0xFF161B26),
    border = Color(0xFF2A3142),
    title = Color(0xFFF3F4F6),
    body = Color(0xFFA8B0C0),
    button = Color(0xFF7C96FF),
    buttonText = Color(0xFF0B0F17),
    iconBg = Color(0xFF3A2A10),
    icon = Color(0xFFFBBF24)
)

@Composable
fun SessionExpiredDialog(
    message: String,
    onLoginAgain: () -> Unit,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val c = if (darkTheme) DarkColors else LightColors

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = c.card,
            border = BorderStroke(1.dp, c.border),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp, top = 32.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Clock icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(c.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(28.dp)) {
                        val s = size.width / 24f
                        val stroke = Stroke(
                            width = 2f * s,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                        drawCircle(
                            color = c.icon,
                            radius = 9f * s,
                            center = Offset(12f * s, 12f * s),
                            style = stroke
                        )
                        val hands = Path().apply {
                            moveTo(12f * s, 7f * s)
                            lineTo(12f * s, 12f * s)
                            lineTo(15f * s, 14f * s)
                        }
                        drawPath(hands, color = c.icon, style = stroke)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Session expired",
                    color = c.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = message,
                    color = c.body,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onLoginAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = c.button,
                        contentColor = c.buttonText
                    )
                ) {
                    Text(
                        text = "Log in again",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}