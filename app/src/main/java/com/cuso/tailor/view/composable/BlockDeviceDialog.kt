package com.cuso.tailor.view.composable

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.textSubdued
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg

// Derived from theme colors (no hardcoded values)
private val Scrim = title_color.copy(alpha = 0.45f)
private val CardShadow = title_color.copy(alpha = 0.28f)
private val DangerShadow = redText.copy(alpha = 0.30f)

private val Inter = FontFamily.Default

@SuppressLint("RememberInComposition")
@Composable
fun BlockDeviceDialog(
    deviceName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Remove the system dim; we draw our own scrim so colors match the design
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(0f) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Scrim)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClick = onDismiss,
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 342.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = CardShadow,
                        spotColor = CardShadow,
                    )
                    .background(whiteBg, RoundedCornerShape(24.dp))
                    // swallow clicks so tapping the card doesn't dismiss
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null,
                        onClick = {},
                    )
                    .padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Icon badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(redBg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    BlockIcon()
                }

                // Title
                Text(
                    text = "Block Device?",
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .semantics { heading() },
                    fontFamily = Inter,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                )

                // Device chip
                Row(
                    modifier = Modifier
                        .background(light_grey, RoundedCornerShape(percent = 50))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PhoneIcon()
                    Text(
                        text = deviceName,
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                }

                // Description
                Text(
                    text = "This device will be blocked and can no longer access the mobile ERP. " +
                            "You can unblock it anytime from this screen.",
                    fontFamily = Inter,
                    fontSize = 14.sp,
                    lineHeight = 21.7.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, sectionBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = whiteBg,
                            contentColor = textSubdued,
                        ),
                    ) {
                        Text(
                            "Cancel",
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(14.dp),
                                ambientColor = DangerShadow,
                                spotColor = DangerShadow,
                            ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = redText,
                            contentColor = whiteBg,
                        ),
                    ) {
                        Text(
                            "Block Device",
                            fontFamily = Inter,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

/** Circle with a diagonal slash (24-unit viewBox scaled to 30dp, 2-unit stroke). */
@Composable
private fun BlockIcon() {
    Canvas(Modifier.size(30.dp)) {
        val u = size.width / 24f
        val stroke = Stroke(width = 2f * u, cap = StrokeCap.Round)
        drawCircle(
            color = redText,
            radius = 9f * u,
            center = Offset(12f * u, 12f * u),
            style = stroke,
        )
        drawLine(
            color = redText,
            start = Offset(5.6f * u, 5.6f * u),
            end = Offset(18.4f * u, 18.4f * u),
            strokeWidth = 2f * u,
            cap = StrokeCap.Round,
        )
    }
}

/** Small phone outline (24-unit viewBox scaled to 16dp). */
@Composable
private fun PhoneIcon() {
    Canvas(Modifier.size(16.dp)) {
        val u = size.width / 24f
        val sw = 2f * u
        drawRoundRect(
            color = textSubdued,
            topLeft = Offset(7f * u, 2f * u),
            size = Size(10f * u, 20f * u),
            cornerRadius = CornerRadius(2f * u, 2f * u),
            style = Stroke(width = sw),
        )
        drawLine(
            color = textSubdued,
            start = Offset(11f * u, 18f * u),
            end = Offset(13f * u, 18f * u),
            strokeWidth = sw,
            cap = StrokeCap.Round,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BlockDeviceDialogPreview() {
    BlockDeviceDialog(
        deviceName = "Chrome (Android Mobile)",
        onDismiss = {},
        onConfirm = {},
    )
}

/*
 * Usage:
 *
 * var showBlock by remember { mutableStateOf(false) }
 *
 * if (showBlock) {
 *     BlockDeviceDialog(
 *         deviceName = "Chrome (Android Mobile)",
 *         onDismiss = { showBlock = false },
 *         onConfirm = {
 *             showBlock = false
 *             viewModel.blockDevice(deviceId)
 *         },
 *     )
 * }
 */