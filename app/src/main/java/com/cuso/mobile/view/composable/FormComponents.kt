package com.cuso.mobile.view.composable

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.whiteBg

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
    errorMessage: String? = null
) {
    val tokens = LocalAppTokens.current
    if (!label.isNullOrEmpty()) {
        FormLabel(label, isRequired)
    } else {
        Spacer(Modifier.height(tokens.screenPadding * 0.375f))
    }

    val density = LocalDensity.current
    var triggerWidthPx by remember { mutableIntStateOf(0) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates -> triggerWidthPx = coordinates.size.width }
                .height(tokens.fieldHeight)
                .background(
                    if (enabled) whiteBg else Color(0xFFF3F4F6),
                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                )
                .border(
                    1.dp,
                    if (isError) Color(0xFFEF4444) else Color(0xFFE5E7EB),
                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                )
                .clickable(enabled = enabled) { onExpandChange(true) }
                .padding(horizontal = tokens.cardPadding * 0.6f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                value,
                fontSize = tokens.bodySmall,
                color = when {
                    !enabled -> Color(0xFF9CA3AF)
                    value == "Select an option" -> Color(0xFF9CA3AF)
                    else -> Color(0xFF374151)
                }
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (enabled) Color.Gray else Color(0xFFD1D5DB)
            )
        }
        if (enabled) {
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
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    // ── NEW: needed for PAN (uppercase) / Aadhaar (spaced grouping) ──
    keyboardCapitalization: androidx.compose.ui.text.input.KeyboardCapitalization =
        androidx.compose.ui.text.input.KeyboardCapitalization.None,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
) {
    val tokens = LocalAppTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.fieldHeight)
            .background(
                if (enabled) whiteBg else Color(0xFFF3F4F6),
                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            )
            .border(
                1.dp,
                if (isError) Color(0xFFEF4444) else Color(0xFFE5E7EB),
                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            )
            .padding(horizontal = tokens.cardPadding * 0.6f),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(placeholder, fontSize = tokens.bodySmall, color = Color(0xFF9CA3AF))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = keyboardCapitalization
            ),
            visualTransformation = visualTransformation,
            textStyle = TextStyle(
                fontSize = tokens.bodyMedium,
                color = if (enabled) Color(0xFF374151) else Color(0xFF6B7280)
            )
        )
    }
    if (isError && !errorMessage.isNullOrBlank()) {
        Text(
            errorMessage,
            fontSize = tokens.label,
            color = Color(0xFFEF4444),
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
            color = Color.Gray
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

/*
package com.cuso.mobile.view.composable

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.redtext

// Holds all colors used by form fields (dropdown, text field, label).
// Built from MaterialTheme.colorScheme so it automatically switches
// between light mode and dark mode without any manual toggle logic.
private data class FormFieldPalette(
    val fieldBackground: Color,        // Background of an enabled field
    val fieldBackgroundDisabled: Color,// Background of a disabled field
    val fieldBorder: Color,            // Default border color
    val fieldBorderError: Color,       // Border color when isError = true
    val text: Color,                   // Primary text color (enabled)
    val textDisabled: Color,           // Text color when field is disabled
    val placeholderText: Color,        // Placeholder / muted text color
    val iconTint: Color,               // Icon color (enabled)
    val iconTintDisabled: Color,       // Icon color (disabled)
    val menuSurface: Color,            // Dropdown menu background
    val labelText: Color,              // Form label text color
    val errorText: Color               // Error message / required-asterisk color
)

// Builds a theme-aware palette. Re-evaluates automatically when the
// app theme (light/dark) changes since it reads from MaterialTheme.
@Composable
private fun rememberFormFieldPalette(): FormFieldPalette {
    val colorScheme = MaterialTheme.colorScheme
    return FormFieldPalette(
        fieldBackground = colorScheme.surface,
        fieldBackgroundDisabled = colorScheme.surfaceVariant,
        fieldBorder = colorScheme.outline,
        fieldBorderError = redtext,
        text = colorScheme.onSurface,
        textDisabled = colorScheme.onSurfaceVariant,
        placeholderText = colorScheme.onSurfaceVariant,
        iconTint = colorScheme.onSurfaceVariant,
        iconTintDisabled = colorScheme.outline,
        menuSurface = colorScheme.surface,
        labelText = colorScheme.onSurfaceVariant,
        errorText = redtext
    )
}

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
    errorMessage: String? = null
) {
    val tokens = LocalAppTokens.current
    val palette = rememberFormFieldPalette()

    if (!label.isNullOrEmpty()) {
        FormLabel(label, isRequired)
    } else {
        Spacer(Modifier.height(tokens.screenPadding * 0.375f))
    }

    val density = LocalDensity.current
    var triggerWidthPx by remember { mutableIntStateOf(0) }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates -> triggerWidthPx = coordinates.size.width }
                .height(tokens.fieldHeight)
                .background(
                    if (enabled) palette.fieldBackground else palette.fieldBackgroundDisabled,
                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                )
                .border(
                    1.dp,
                    if (isError) palette.fieldBorderError else palette.fieldBorder,
                    RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
                )
                .clickable(enabled = enabled) { onExpandChange(true) }
                .padding(horizontal = tokens.cardPadding * 0.6f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                value,
                fontSize = tokens.bodySmall,
                color = when {
                    !enabled -> palette.textDisabled
                    value == "Select an option" -> palette.placeholderText
                    else -> palette.text
                }
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = if (enabled) palette.iconTint else palette.iconTintDisabled
            )
        }
        if (enabled) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandChange(false) },
                containerColor = palette.menuSurface,
                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.4f),
                modifier = Modifier
                    .width(with(density) { triggerWidthPx.toDp() })
                    .heightIn(max = tokens.fieldHeight * 4.5f)
            ) {
                options.forEach { option ->
                    Text(
                        text = option,
                        fontSize = tokens.bodyMedium,
                        color = palette.text,
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
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    // ── NEW: needed for PAN (uppercase) / Aadhaar (spaced grouping) ──
    keyboardCapitalization: androidx.compose.ui.text.input.KeyboardCapitalization =
        androidx.compose.ui.text.input.KeyboardCapitalization.None,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None
) {
    val tokens = LocalAppTokens.current
    val palette = rememberFormFieldPalette()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.fieldHeight)
            .background(
                if (enabled) palette.fieldBackground else palette.fieldBackgroundDisabled,
                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            )
            .border(
                1.dp,
                if (isError) palette.fieldBorderError else palette.fieldBorder,
                RoundedCornerShape(tokens.cardCornerRadius * 0.5f)
            )
            .padding(horizontal = tokens.cardPadding * 0.6f),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(placeholder, fontSize = tokens.bodyMedium, color = palette.placeholderText)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = keyboardCapitalization
            ),
            visualTransformation = visualTransformation,
            textStyle = TextStyle(
                fontSize = tokens.bodyMedium,
                color = if (enabled) palette.text else palette.textDisabled
            )
        )
    }
    if (isError && !errorMessage.isNullOrBlank()) {
        Text(
            errorMessage,
            fontSize = tokens.label,
            color = palette.errorText,
            modifier = Modifier.padding(top = tokens.screenPadding * 0.25f, start = tokens.screenPadding * 0.25f)
        )
    }
}

@Composable
fun FormLabel(text: String?, isRequired: Boolean = false) {
    val tokens = LocalAppTokens.current
    val palette = rememberFormFieldPalette()

    Row {
        Text(
            text ?: "",
            fontSize = tokens.bodySmall,
            fontWeight = FontWeight.Medium,
            color = palette.labelText
        )
        if (isRequired) {
            Text(
                text = " *",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Medium,
                color = palette.errorText
            )
        }
    }
    Spacer(Modifier.height(tokens.screenPadding * 0.375f))
}
 */