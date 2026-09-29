@file:Suppress("AssignedValueIsNeverRead")
package com.cuso.tailor.view.composable

import android.graphics.Color.parseColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController

/**
 * Reusable color field: shows a small color swatch + hex value + dropdown arrow,
 * and opens [ColorPickerDialog] on tap. Extracted from CreateOrderScreen so it can
 * be reused anywhere a color needs to be picked (order items, attribute values, etc.)
 */
@Composable
fun ColorPickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Select Color"
) {
    val tokens = LocalAppTokens.current
    var showColorPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(tokens.fieldHeight)
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
            .clickable { showColorPicker = true }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Small swatch preview of the currently selected color
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    runCatching {
                        Color(parseColor(if (value.startsWith("#")) value else "#$value"))
                    }.getOrDefault(Color.Transparent)
                )
                .border(1.dp, sectionBorder, RoundedCornerShape(4.dp))
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = value.ifBlank { placeholder },
            fontSize = tokens.bodyMedium,
            color = if (value.isBlank()) mutedText else title_color,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = headerGrey,
            modifier = Modifier.size(18.dp)
        )
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialHex = if (value.startsWith("#")) value else "#3B82F6",
            onDismiss = { showColorPicker = false },
            onConfirm = { hexColor ->
                onValueChange(hexColor)
                showColorPicker = false
            }
        )
    }
}

/**
 * Full color picker dialog: hue/saturation map + brightness + alpha sliders + hex preview.
 * Made public (was private inside CreateOrderScreen) so [ColorPickerField] and any other
 * screen can reuse it directly.
 */
@Composable
fun ColorPickerDialog(
    initialHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val tokens = LocalAppTokens.current
    val controller = rememberColorPickerController()
    var selectedHex by remember { mutableStateOf(initialHex.ifBlank { "#3B82F6" }) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = tokens.screenPadding),
            shape = RoundedCornerShape(tokens.cardCornerRadius),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(tokens.cardPadding),
                verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
            ) {
                Text("Choose Color", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = TextPrimary)

                HsvColorPicker(
                    modifier = Modifier.fillMaxWidth().height(260.dp).padding(tokens.extraPadding),
                    controller = controller,
                    initialColor = parseHexColorOrNull(selectedHex) ?: Primary,
                    onColorChanged = { envelope ->
                        val argb = envelope.color.toArgb()
                        val rgbHex = String.format("#%06X", 0xFFFFFF and argb)
                        selectedHex = rgbHex
                    }
                )

                BrightnessSlider(
                    modifier = Modifier.fillMaxWidth().height(35.dp),
                    controller = controller
                )

                AlphaSlider(
                    modifier = Modifier.fillMaxWidth().height(35.dp),
                    controller = controller
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(parseHexColorOrNull(selectedHex) ?: grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                    )
                    Text(selectedHex.uppercase(), fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.dp, grey_border)
                    ) {
                        Text("Cancel", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { onConfirm(selectedHex.uppercase()) },
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                    ) {
                        Text("Select", color = whiteBg, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/** Safely parses a hex string ("#RRGGBB" or "#AARRGGBB") into a Compose Color, or null if invalid. */
fun parseHexColorOrNull(hex: String): Color? {
    return try {
        val cleaned = hex.trim().removePrefix("#")
        if (cleaned.length != 6 && cleaned.length != 8) return null
        val colorLong = cleaned.toLong(16)
        if (cleaned.length == 6) Color(0xFF000000 or colorLong) else Color(colorLong)
    } catch (_: Exception) {
        null
    }
}