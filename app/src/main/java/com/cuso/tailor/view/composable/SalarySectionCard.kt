@file:Suppress("UNUSED_VALUE", "SpellCheckingInspection", "DEPRECATION")

package com.cuso.tailor.view.composable

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.hr.SalaryComponentItem
import com.cuso.tailor.ui.theme.BorderGray
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.greenBg
import com.cuso.tailor.ui.theme.greentext
import com.cuso.tailor.ui.theme.grey_border
import com.cuso.tailor.ui.theme.light_grey
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.primary_light
import com.cuso.tailor.ui.theme.redBg
import com.cuso.tailor.ui.theme.redText
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.utils.DynamicIslandManager

/**
 * Common data model representing a line item in an Earnings or Deductions section.
 */
data class SalarySectionLineItem(
    val id: String = "",
    val name: String,
    val valueOrAmount: String,
    val isEarning: Boolean,
    val tag: String? = null
)

/**
 * Reusable Section Card for Earnings and Deductions.
 * Supports both Dropdown selection (from API components) and manual free-text entry.
 */
@SuppressLint("DefaultLocale")
@Composable
fun SalarySectionCard(
    title: String,
    isEarning: Boolean,
    items: List<SalarySectionLineItem>,
    modifier: Modifier = Modifier,
    availableComponents: List<SalaryComponentItem>? = null,
    onAddItem: (name: String, value: String, selectedComponent: SalaryComponentItem?) -> Unit,
    onDeleteItem: (Int) -> Unit
) {
    val tokens = LocalAppTokens.current

    var isAddingActive by remember { mutableStateOf(false) }
    var selectedComponent by remember { mutableStateOf<SalaryComponentItem?>(null) }
    var isComponentDropdownExpanded by remember { mutableStateOf(false) }

    var customNameText by remember { mutableStateOf("") }
    var valueOrAmountText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .padding(tokens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Section Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isEarning) greenBg else redBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEarning) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isEarning) greentext else redText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = title,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )
            }

            // Toggle inline addition form
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    isAddingActive = !isAddingActive
                    selectedComponent = null
                    customNameText = ""
                    valueOrAmountText = ""
                }
            ) {
                Icon(
                    imageVector = if (isAddingActive) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isAddingActive) "Cancel" else "Add Component",
                    fontSize = tokens.bodySmall,
                    color = Primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Inline Form Adder Panel ──
        AnimatedVisibility(
            visible = isAddingActive,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(light_grey)
                    .border(1.dp, grey_border, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select & Add to $title",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.SemiBold,
                    color = title_color
                )

                if (availableComponents != null) {
                    // Dropdown Mode
                    if (availableComponents.isEmpty()) {
                        Text(
                            text = "No components available to add.",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    } else {
                        Column {
                            FormLabel("Salary Component", isRequired = true)
                            FormDropdown(
                                value = selectedComponent?.name ?: "Select a component...",
                                expanded = isComponentDropdownExpanded,
                                onExpandChange = { isComponentDropdownExpanded = it },
                                options = availableComponents.map { it.name },
                                onOptionSelected = { selectedName ->
                                    val matched = availableComponents.find { it.name == selectedName }
                                    selectedComponent = matched
                                    isComponentDropdownExpanded = false
                                    valueOrAmountText = ""
                                }
                            )
                        }
                    }
                } else {
                    // Manual Text Input Mode
                    Column {
                        FormLabel("Component Name", isRequired = true)
                        FormTextField(
                            value = customNameText,
                            onValueChange = { customNameText = it },
                            placeholder = "e.g. Basic Pay",
                            containerColor = whiteBg,
                            borderColor = grey_border
                        )
                    }
                }

                // Amount / Value / Formula field
                Column {
                    FormLabel("Amount / Value / Formula", isRequired = true)
                    FormTextField(
                        value = valueOrAmountText,
                        onValueChange = { valueOrAmountText = it },
                        placeholder = "e.g. 5000 or Basic * 0.4",
                        containerColor = whiteBg,
                        borderColor = grey_border
                    )
                }

                Button(
                    onClick = {
                        val finalName = if (availableComponents != null) {
                            selectedComponent?.name.orEmpty()
                        } else {
                            customNameText.trim()
                        }

                        if (finalName.isBlank()) {
                            DynamicIslandManager.showError("Please specify a component name")
                            return@Button
                        }
                        if (valueOrAmountText.isBlank()) {
                            DynamicIslandManager.showError("Please enter an amount or formula")
                            return@Button
                        }

                        onAddItem(finalName, valueOrAmountText.trim(), selectedComponent)

                        // Reset
                        isAddingActive = false
                        selectedComponent = null
                        customNameText = ""
                        valueOrAmountText = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(tokens.buttonHeight * 0.9f),
                    shape = RoundedCornerShape(tokens.cardCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(
                        text = "Add Item",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = whiteBg
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // ── Render Line Items ──
        if (items.isEmpty() && !isAddingActive) {
            Text(
                text = "No components added yet.",
                fontSize = tokens.caption,
                color = mutedText,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEachIndexed { index, item ->
                    SalarySectionItemRow(
                        item = item,
                        onDeleteClick = { onDeleteItem(index) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Card Item row reusing DataCard styles.
 */
@Composable
private fun SalarySectionItemRow(
    item: SalarySectionLineItem,
    onDeleteClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val menuActions = remember {
        listOf(
            MenuAction(
                label = "Delete",
                icon = Icons.Outlined.Delete,
                textColor = redText,
                tint = redText,
                onClick = onDeleteClick
            )
        )
    }

    DataCard(
        item = item,
        title = item.name,
        titleFontWeight = FontWeight.Medium,
        titleColor = title_color,
        topBadgeText = if (item.isEarning) "Earning" else "Deduction",
        topBadgeTextColor = if (item.isEarning) Primary else redText,
        topBadgeBgColor = if (item.isEarning) primary_light else redBg,
        topBadgeDotColor = if (item.isEarning) Primary else redText,
        topBadgeInline = true,
        actions = menuActions,
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary tag (e.g. Fixed, Percentage)
                if (!item.tag.isNullOrBlank()) {
                    StatusBadge(
                        text = item.tag,
                        bgColor = light_grey,
                        textColor = TextSecondary,
                        showDot = false
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Formatted Amount / Formula Text
                Text(
                    text = item.valueOrAmount,
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (item.isEarning) Primary else redText
                )
            }
        }
    )
}