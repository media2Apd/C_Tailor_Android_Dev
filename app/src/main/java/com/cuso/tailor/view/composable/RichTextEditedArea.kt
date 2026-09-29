package com.cuso.tailor.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*

/**
 * Reusable Rich Text Description Field with formatting toolbar (Bold, Italic, List, Link).
 */
@Composable
fun RichTextEditorArea(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String = "Detailed product details, styling tips, etc.",
    minHeight: Int = 130
) {
    val tokens = LocalAppTokens.current

    // Formatting button action helpers
    fun applySpanStyle(spanStyle: SpanStyle) {
        val selection = value.selection
        if (selection.collapsed) return

        val builder = AnnotatedString.Builder(value.annotatedString)
        builder.addStyle(spanStyle, selection.start, selection.end)
        onValueChange(
            value.copy(
                annotatedString = builder.toAnnotatedString()
            )
        )
    }

    fun insertBulletList() {
        val text = value.text
        val selection = value.selection
        val insertPos = selection.start.coerceIn(0, text.length)

        val before = text.substring(0, insertPos)
        val after = text.substring(insertPos)
        val bulletPrefix = if (before.isEmpty() || before.endsWith("\n")) "• " else "\n• "
        val newText = before + bulletPrefix + after
        val newCursorPos = insertPos + bulletPrefix.length

        onValueChange(
            TextFieldValue(
                text = newText,
                selection = TextRange(newCursorPos)
            )
        )
    }

    fun insertLink() {
        val selection = value.selection
        val text = value.text
        val selectedText = if (!selection.collapsed) {
            text.substring(selection.start, selection.end)
        } else {
            "link"
        }

        val linkMarkdown = "[$selectedText](https://)"
        val before = text.substring(0, selection.start)
        val after = text.substring(selection.end)
        val newText = before + linkMarkdown + after

        val builder = AnnotatedString.Builder(newText)
        val linkStart = before.length
        val linkEnd = linkStart + linkMarkdown.length
        builder.addStyle(
            SpanStyle(color = Primary, textDecoration = TextDecoration.Underline),
            linkStart,
            linkEnd
        )

        onValueChange(
            TextFieldValue(
                annotatedString = builder.toAnnotatedString(),
                selection = TextRange(linkEnd)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
    ) {
        // --- Toolbar Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bold Button
            Text(
                text = "B",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    applySpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                }
            )

            // Italic Button
            Text(
                text = "/",
                fontSize = tokens.bodyMedium,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    applySpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                }
            )

            // Bullet List Button
            Icon(
                imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                contentDescription = "Bullet List",
                tint = TextPrimary,
                modifier = Modifier
                    .size(tokens.iconSize * 1.05f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        insertBulletList()
                    }
            )

            // Link Button
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = "Insert Link",
                tint = TextPrimary,
                modifier = Modifier
                    .size(tokens.iconSize * 1.05f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        insertLink()
                    }
            )
        }

        HorizontalDivider(color = grey_border)

        // --- Text Input Area ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight.dp)
                .padding(14.dp),
            contentAlignment = Alignment.TopStart
        ) {
            if (value.text.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = tokens.bodyMedium,
                    color = mutedText
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                cursorBrush = SolidColor(Primary),
                textStyle = TextStyle(
                    fontSize = tokens.bodyMedium,
                    color = TextPrimary
                )
            )
        }
    }
}