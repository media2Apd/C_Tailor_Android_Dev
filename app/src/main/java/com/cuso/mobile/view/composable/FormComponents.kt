package com.cuso.mobile.view.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.grey_border
import com.cuso.mobile.ui.theme.light_grey
import com.cuso.mobile.ui.theme.redText
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.utils.AppLoadingManager

private val AccentColor = Color(0xFF3D3DFF)
private val BorderColor = Color(0xFFE3E4E8)
private val TitleColor = Color(0xFF111827)
private val LabelColor = Color(0xFF8A8A99)

@Suppress("UNUSED_PARAMETER")
@Composable
fun FormDropdown(
    label: String? = null,
    value: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    isRequired: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorMessage: String? = null,
    maxLines: Int = 1
) {
    val tokens = LocalAppTokens.current

    val isAppBusy by AppLoadingManager.busyState.collectAsState()
    val effectiveEnabled = enabled && !isAppBusy

    if (!label.isNullOrEmpty()) {
        FormLabel(label, isRequired)
    } else {
        Spacer(Modifier.height(tokens.screenPadding * 0.375f))
    }

    val density = LocalDensity.current
    var triggerWidthPx by remember { mutableIntStateOf(0) }

    // ── Rotation Animation for Arrow ──
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "DropdownArrowRotation"
    )

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates -> triggerWidthPx = coordinates.size.width }
                .height(tokens.fieldHeight)
                .background(
                    if (effectiveEnabled) whiteBg else light_grey,
                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                )
                .border(
                    1.dp,
                    if (isError) redText else grey_border,
                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                )
                .clickable(enabled = effectiveEnabled) { onExpandChange(!expanded) }
                .padding(horizontal = tokens.cardPadding * 0.6f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Max 1 Line & Ellipsis ──
            Text(
                text = value,
                fontSize = tokens.bodySmall,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
                color = when {
                    !effectiveEnabled -> Color(0xFF9CA3AF)
                    value == "Select an option" -> Color(0xFF9CA3AF)
                    else -> Color(0xFF374151)
                },
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(Modifier.width(tokens.cardPadding * 0.3f))

            // ── Dropdown Arrow Icon ──
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (effectiveEnabled) Color.Gray else Color(0xFFD1D5DB),
                modifier = Modifier.graphicsLayer { rotationZ = arrowRotation }
            )
        }

        if (effectiveEnabled) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandChange(false) },
                containerColor = whiteBg,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                modifier = Modifier
                    .width(with(density) { triggerWidthPx.toDp() })
                    .heightIn(max = tokens.fieldHeight * 4.5f)
            ) {
                options.forEach { option ->
                    Text(
                        text = option,
                        fontSize = tokens.bodyMedium,
                        color = Color(0xFF374151),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected(option)
                                onExpandChange(false)
                            }
                            .padding(
                                horizontal = tokens.cardPadding * 0.6f,
                                vertical = tokens.screenPadding * 0.5f
                            )
                    )
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    borderColor: Color = grey_border,               // Customizable border color
    textColor: Color = Color(0xFF374151),            // Customizable text color
    placeholderColor: Color = Color(0xFF9CA3AF),      // Customizable placeholder color
    containerColor: Color = whiteBg,                 // Customizable background container color
    keyboardCapitalization: androidx.compose.ui.text.input.KeyboardCapitalization =
        androidx.compose.ui.text.input.KeyboardCapitalization.None,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
) {
    val tokens = LocalAppTokens.current

    val isAppBusy by AppLoadingManager.busyState.collectAsState()
    val effectiveEnabled = enabled && !isAppBusy

    // Determine final border color based on error state or custom color
    val activeBorderColor = if (isError) redText else borderColor
    val activeContainerColor = if (effectiveEnabled) containerColor else light_grey

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(tokens.fieldHeight)
            .background(
                activeContainerColor,
                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            )
            .border(
                1.dp,
                activeBorderColor,
                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            )
            .padding(horizontal = tokens.cardPadding * 0.6f),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(
                text = placeholder,
                fontSize = tokens.bodySmall,
                color = placeholderColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = effectiveEnabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = keyboardCapitalization
            ),
            visualTransformation = visualTransformation,
            textStyle = TextStyle(
                fontSize = tokens.bodyMedium,
                color = if (effectiveEnabled) textColor else textColor.copy(alpha = 0.5f)
            )
        )
    }
    if (isError && !errorMessage.isNullOrBlank()) {
        Text(
            text = errorMessage,
            fontSize = tokens.label,
            color = redText,
            modifier = Modifier.padding(top = tokens.screenPadding * 0.25f, start = tokens.screenPadding * 0.25f)
        )
    }
}

@Composable
fun FormLabel(text: String?, isRequired: Boolean = false) {
    val tokens = LocalAppTokens.current
    Row {
        Text(
            text ?: "",
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151)
        )
        if (isRequired) {
            Text(
                text = " *",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color.Red
            )
        }
    }
    Spacer(Modifier.height(tokens.screenPadding * 0.375f))
}

@Composable
fun FormTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String = "",
    minLines: Int = 4,
    maxLines: Int = 6,
    borderColor: Color = BorderColor,
    focusedBorderColor: Color = AccentColor,
    textColor: Color = TitleColor,
    placeholderColor: Color = LabelColor
) {
    val tokens = LocalAppTokens.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        placeholder = { Text(placeholder, color = placeholderColor, fontSize = tokens.bodySmall) },
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.45f),
        textStyle = TextStyle(
            fontSize = tokens.bodyMedium,
            color = if (enabled) textColor else textColor.copy(alpha = 0.7f)
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            disabledTextColor = textColor.copy(alpha = 0.7f),
            unfocusedBorderColor = borderColor,
            focusedBorderColor = focusedBorderColor,
            disabledBorderColor = borderColor.copy(alpha = 0.5f),
            focusedContainerColor = whiteBg,
            unfocusedContainerColor = whiteBg,
            disabledContainerColor = Color(0xFFF9FAFB)
        ),
        modifier = modifier.fillMaxWidth()
    )
}