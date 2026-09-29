package com.cuso.tailor.view.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.AppDesignTokens
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextPrimary
import com.cuso.tailor.ui.theme.headerGrey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.whiteBg

// Fixed size options -> shown as checkboxes only, never a free text field.
private val FIXED_SIZE_OPTIONS = listOf("S", "M", "L", "XL", "XXL", "XXXL")

// The only two attribute types the user can pick — no free text entry at all.
private val ATTRIBUTE_TYPE_OPTIONS = listOf("Size", "Color")

/**
 * Replaces the old free-text "Attribute Type" field with two checkboxes: Size / Color.
 * Unlike a radio group, BOTH can be checked at the same time — a product can have
 * Size variants, Color variants, or both together. Reuses the app's existing [AppCheckbox].
 */
@Composable
fun AttributeTypeSelector(
    selectedTypes: Set<String>,
    onTypeToggled: (type: String, isChecked: Boolean) -> Unit,
    tokens: AppDesignTokens
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        ATTRIBUTE_TYPE_OPTIONS.forEach { type ->
            val isChecked = selectedTypes.any { it.equals(type, ignoreCase = true) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onTypeToggled(type, !isChecked) }
            ) {
                AppCheckbox(
                    checked = isChecked,
                    onCheckedChange = { checked -> onTypeToggled(type, checked) }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = type,
                    fontSize = tokens.bodyMedium,
                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isChecked) TextPrimary else headerGrey
                )
            }
        }
    }
}

/**
 * Renders the "Values" input for one attribute row inside Attributes (Variants).
 *
 * - attributeType "Color"  -> a list of [ColorPickerField] rows (reused from CreateOrderScreen),
 *                              each with a remove button, plus "+ Add Color".
 * - attributeType "Size"   -> fixed checkbox list: S, M, L, XL, XXL, XXXL (reuses AppCheckbox,
 *                              no textfield at all).
 * - anything else          -> falls back to a simple free-text chip input.
 */
@Composable
fun AttributeValueSelector(
    attributeType: String,
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    tokens: AppDesignTokens
) {
    val normalizedType = attributeType.trim().lowercase()

    when {
        normalizedType == "color" -> ColorAttributeValues(values, onValuesChange, tokens)
        normalizedType == "size" -> SizeAttributeValues(values, onValuesChange, tokens)
        else -> FreeTextAttributeValues(values, onValuesChange, tokens)
    }
}

// ── Color attribute: one ColorPickerField per value, reused as-is ──
@Composable
private fun ColorAttributeValues(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    tokens: AppDesignTokens
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        values.forEachIndexed { index, colorHex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ColorPickerField(
                        value = colorHex,
                        onValueChange = { newHex ->
                            val updated = values.toMutableList()
                            updated[index] = newHex
                            onValuesChange(updated)
                        }
                    )
                }
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Color",
                    tint = redText,
                    modifier = Modifier
                        .size(tokens.iconSize * 0.9f)
                        .clickable {
                            onValuesChange(values.filterIndexed { i, _ -> i != index })
                        }
                )
            }
        }

        Text(
            text = "+ Add Color",
            fontSize = tokens.bodySmall,
            color = Primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
                onValuesChange(values + "#3B82F6")
            }
        )
    }
}

// ── Size attribute: fixed checkbox list only, no textfield ──
@Composable
private fun SizeAttributeValues(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    tokens: AppDesignTokens
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Two per row so it stays compact instead of a long vertical list.
        FIXED_SIZE_OPTIONS.chunked(3).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowOptions.forEach { size ->
                    val isChecked = values.contains(size)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                onValuesChange(
                                    if (isChecked) values - size else values + size
                                )
                            }
                            .padding(vertical = 6.dp)
                    ) {
                        // Reused checkbox component, same one used elsewhere in the app
                        AppCheckbox(
                            checked = isChecked,
                            onCheckedChange = {
                                onValuesChange(
                                    if (isChecked) values - size else values + size
                                )
                            }
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = size,
                            fontSize = tokens.bodyMedium,
                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isChecked) TextPrimary else headerGrey
                        )
                    }
                }
            }
        }
    }
}

// ── Fallback for any other attribute type: simple free text chip input ──
@Composable
private fun FreeTextAttributeValues(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    tokens: AppDesignTokens
) {
    var draftText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (values.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                values.forEachIndexed { index, valueText ->
                    Row(
                        modifier = Modifier
                            .background(whiteBg, RoundedCornerShape(20.dp))
                            .border(1.dp, sectionBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(valueText, fontSize = tokens.bodySmall, color = TextPrimary)
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = redText,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable {
                                    onValuesChange(values.filterIndexed { i, _ -> i != index })
                                }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = draftText,
            onValueChange = { draftText = it },
            placeholder = { Text("Type a value and press Enter", color = mutedText) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    val trimmed = draftText.trim()
                    if (trimmed.isNotBlank() && !values.contains(trimmed)) {
                        onValuesChange(values + trimmed)
                    }
                    draftText = ""
                }
            ),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = whiteBg,
                focusedContainerColor = whiteBg
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Cross-multiplies the values of each confirmed attribute (e.g. Size: [S, M, L], Color: [#RED, #BLUE])
 * into every combination, e.g. ["S / #RED", "S / #BLUE", "M / #RED", "M / #BLUE", ...].
 *
 * Pass a map of attributeType -> selected values. Only non-empty entries take part, so:
 * - Size only selected            -> combinations are just the size values
 * - Color only selected           -> combinations are just the color values
 * - Both Size and Color selected  -> every Size × Color pair
 *
 * Plain Kotlin, no Compose dependency, so it can be called from any ViewModel or Composable.
 */
fun generateAttributeCombinations(valuesByAttributeType: Map<String, List<String>>): List<List<String>> {
    var combinations = listOf<List<String>>()
    valuesByAttributeType.values.filter { it.isNotEmpty() }.forEach { values ->
        combinations = if (combinations.isEmpty()) {
            values.map { listOf(it) }
        } else {
            combinations.flatMap { combo -> values.map { value -> combo + value } }
        }
    }
    return combinations
}