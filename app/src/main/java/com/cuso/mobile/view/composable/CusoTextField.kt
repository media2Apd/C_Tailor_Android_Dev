package com.cuso.mobile.view.composable

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.PrimaryTextColor
import com.cuso.mobile.ui.theme.TextLog
import com.cuso.mobile.ui.theme.blackTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CusoTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String = "",
    leadingIconVector: ImageVector? = null,
    leadingIconPainter: Painter? = null,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorText: String? = null,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val tokens = LocalAppTokens.current
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Dim the text a bit when the field is locked/read-only, same idea as the
    // old CardContentsForgotPassword did with Color(0xFF6B7280)
    val textColor = if (readOnly) Color(0xFF6B7280) else blackTitle

    Column(modifier = modifier.fillMaxWidth()) {
        // Render label if provided
        if (label != null) {
            Text(
                text = label,
                fontSize = tokens.bodySmall,
                color = TextLog,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Adaptive-height container (tokens.fieldHeight instead of a fixed 40dp)
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            BasicTextField(
                value = value,
                onValueChange = { if (!readOnly) onValueChange(it) },
                readOnly = readOnly,
                singleLine = true,
                textStyle = TextStyle(color = textColor, fontSize = tokens.bodySmall),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                interactionSource = interactionSource,
                keyboardOptions = keyboardOptions,
                visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                decorationBox = { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = value,
                        innerTextField = innerTextField,
                        enabled = !readOnly,
                        singleLine = true,
                        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                        interactionSource = interactionSource,
                        isError = isError,
                        placeholder = { Text(placeholder, color = PrimaryTextColor, fontSize = tokens.bodySmall) },
                        leadingIcon = if (leadingIconVector != null || leadingIconPainter != null) {
                            {
                                Box(modifier = Modifier.padding(start = 12.dp)) {
                                    if (leadingIconVector != null) {
                                        Icon(leadingIconVector, null, Modifier.size(tokens.iconSize), tint = PrimaryTextColor)
                                    } else if (leadingIconPainter != null) {
                                        Icon(leadingIconPainter, null, Modifier.size(tokens.iconSize), tint = PrimaryTextColor)
                                    }
                                }
                            }
                        } else null,
                        trailingIcon = if (isPassword) {
                            {
                                IconButton(
                                    onClick = { isPasswordVisible = !isPasswordVisible },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                }
                            }
                        } else null,
                        colors = customFieldOutlinedColors(),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        container = {
                            OutlinedTextFieldDefaults.Container(
                                enabled = !readOnly,
                                isError = isError,
                                interactionSource = interactionSource,
                                colors = customFieldOutlinedColors(),
                                shape = RoundedCornerShape(5.dp)
                            )
                        }
                    )
                }
            )
        }

        // Error message handling
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = Color.Red,
                fontSize = tokens.label,
                modifier = Modifier.padding(top = 2.dp, start = 4.dp)
            )
        }
    }
}