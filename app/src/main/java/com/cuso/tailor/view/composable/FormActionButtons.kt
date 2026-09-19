package com.cuso.tailor.view.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.whiteBg

/**
 * Reusable 3-button action panel layout:
 * - Row with secondary buttons ("Cancel" & "Save Draft")
 * - Full-width primary action button ("Submit")
 */
@Composable
fun FormActionButtons(
    modifier: Modifier = Modifier,
    cancelText: String = "Cancel",
    draftText: String = "Save Draft",
    primaryText: String = "Submit Purchase Order",
    onCancel: () -> Unit = {},
    onSaveDraft: () -> Unit = {},
    onPrimaryClick: () -> Unit = {},
    showDraftButton: Boolean = true,
    showPrimaryButton: Boolean = true,
    isLoading: Boolean = false,
    isCancelEnabled: Boolean = true,
    isDraftEnabled: Boolean = true,
    isPrimaryEnabled: Boolean = true,
    buttonHeight: Dp? = null,
    cornerRadius: Dp = 14.dp,
    borderColor: Color = Color(0xFFCBD5E1),
    textColor: Color = Color(0xFF1E293B),
    primaryColor: Color = Primary
) {
    val tokens = LocalAppTokens.current
    val effectiveHeight = buttonHeight ?: tokens.buttonHeight
    val shape = RoundedCornerShape(cornerRadius)

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Top Row: Cancel and Save Draft Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
        ) {
            // Cancel Button
            OutlinedButton(
                onClick = onCancel,
                enabled = isCancelEnabled && !isLoading,
                shape = shape,
                border = BorderStroke(1.2.dp, borderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = whiteBg,
                    contentColor = textColor,
                    disabledContentColor = textColor.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(effectiveHeight)
            ) {
                Text(
                    text = cancelText,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            }

            // Save Draft Button (Optional visibility)
            if (showDraftButton) {
                OutlinedButton(
                    onClick = onSaveDraft,
                    enabled = isDraftEnabled && !isLoading,
                    shape = shape,
                    border = BorderStroke(1.2.dp, borderColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = whiteBg,
                        contentColor = textColor,
                        disabledContentColor = textColor.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(effectiveHeight)
                ) {
                    Text(
                        text = draftText,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }

        // Bottom Button: Full-Width Primary Action
        if (showPrimaryButton) {
            Spacer(Modifier.height(tokens.extraPadding))

            Button(
                onClick = onPrimaryClick,
                enabled = isPrimaryEnabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(effectiveHeight)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(tokens.iconSize),
                        color = whiteBg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = primaryText,
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = whiteBg
                    )
                }
            }
        }
    }
}