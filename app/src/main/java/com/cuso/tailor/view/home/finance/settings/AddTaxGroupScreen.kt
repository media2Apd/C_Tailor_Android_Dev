@file:Suppress("UNUSED_PARAMETER", "AssignedValueIsNeverRead", "SpellCheckingInspection")

package com.cuso.tailor.view.home.finance.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextArea
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.SheetValue
import com.cuso.tailor.view.composable.SmoothBottomSheet
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction
import com.cuso.tailor.view.composable.blurScrim
import java.util.UUID

// ---------------------------------------------------------------------------
// Data Models
// ---------------------------------------------------------------------------

data class TaxComponentEntry(
    val id: String = UUID.randomUUID().toString(),
    val componentName: String = "",
    val ratePercent: String = ""
)

data class AddTaxGroupFormState(
    var groupName: String = "",
    var groupCode: String = "",
    var taxType: String = "",
    var description: String = "",
    var components: List<TaxComponentEntry> = listOf(TaxComponentEntry())
)

private fun totalRate(components: List<TaxComponentEntry>): Int =
    components.sumOf { it.ratePercent.toIntOrNull() ?: 0 }

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun AddTaxGroupScreen(
    onClose: () -> Unit,
    onCreateGroup: (AddTaxGroupFormState) -> Unit,
    taxTypeOptions: List<String> = listOf("GST", "VAT", "Custom"),
    componentNameOptions: List<String> = listOf("CGST", "SGST", "IGST", "CESS")
) {
    val tokens = LocalAppTokens.current
    var form by remember { mutableStateOf(AddTaxGroupFormState()) }
    var taxTypeExpanded by remember { mutableStateOf(false) }

    // State to track dynamic blur radius from SmoothBottomSheet
    var backgroundBlurRadius by remember { mutableStateOf(0.dp) }

    // State for controlling the Smooth Bottom Sheet
    var sheetState by remember { mutableStateOf(SheetValue.Hidden) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ── Top Title Bar (Solid background + High zIndex to prevent scrim overlap) ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(10f),
            color = whiteBg,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TitleBar(title = "Add Tax Group", onClose = onClose)
                HorizontalDivider(color = grey_border, thickness = 2.dp)
            }
        }

        // ── Content Area with clipToBounds (Ensures bottom sheet scrim NEVER touches TitleBar) ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .blurScrim(backgroundBlurRadius)
                    .background(Color.Transparent),
                contentPadding = PaddingValues(
                    horizontal = tokens.screenPadding,
                    vertical = tokens.screenPadding
                )
            ) {
                item {
                    SectionHeader(number = 1, title = "Basic Information")
                    Spacer(Modifier.height(14.dp))

                    FormLabel("Tax Group Name", isRequired = true)
                    FormTextField(
                        value = form.groupName,
                        onValueChange = { form = form.copy(groupName = it) },
                        placeholder = "e.g. GST 18%"
                    )
                    Spacer(Modifier.height(14.dp))

                    FormLabel("Tax Group Code", isRequired = true)
                    FormTextField(
                        value = form.groupCode,
                        onValueChange = { form = form.copy(groupCode = it) },
                        placeholder = "e.g. GST_18"
                    )
                    Spacer(Modifier.height(14.dp))

                    FormLabel("Tax Type", isRequired = true)
                    FormDropdown(
                        value = form.taxType.ifEmpty { "Select tax type" },
                        expanded = taxTypeExpanded,
                        onExpandChange = { taxTypeExpanded = it },
                        options = taxTypeOptions,
                        onOptionSelected = { form = form.copy(taxType = it) }
                    )
                    Spacer(Modifier.height(14.dp))

                    FormLabel("Description")
                    FormTextArea(
                        value = form.description,
                        onValueChange = { form = form.copy(description = it) },
                        placeholder = "Description here..."
                    )

                    Spacer(Modifier.height(24.dp))
                    SectionHeader(number = 2, title = "Tax Components")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Add the individual taxes that make up this tax group.",
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(14.dp))
                }

                itemsIndexed(
                    items = form.components,
                    key = { _, item -> item.id }
                ) { index, component ->
                    TaxComponentCard(
                        index = index,
                        component = component,
                        componentNameOptions = componentNameOptions,
                        canDelete = form.components.size > 1,
                        onChange = { updated ->
                            form = form.copy(
                                components = form.components.toMutableList().also { it[index] = updated }
                            )
                        },
                        onDelete = {
                            form = form.copy(
                                components = form.components.filterIndexed { i, _ -> i != index }
                            )
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                // Open smooth bottom sheet in Half Screen mode
                                sheetState = SheetValue.Collapsed
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Add Tax Component",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    TotalTaxRateRow(total = totalRate(form.components))
                }

                item {
                    Spacer(Modifier.height(90.dp))
                }
            }

            // Screen-level bottom action buttons
            StepNavigationFab(
                showBack = true,
                onBack = onClose,
                backLabel = "Cancel",
                showBackArrow = false,
                showTrailingArrow = false,
                trailingAction = TrailingFabAction.Next(
                    label = "Create Group",
                    enabled = form.groupName.isNotBlank() && form.groupCode.isNotBlank() && form.taxType.isNotBlank(),
                    onClick = {
                        onCreateGroup(form)
                    }
                )
            )

            // Half-Page Smooth Bottom Sheet (Strictly bounded below TitleBar)
            AddTaxComponentSheet(
                sheetState = sheetState,
                onStateChange = { sheetState = it },
                componentNameOptions = componentNameOptions,
                onDismiss = { sheetState = SheetValue.Hidden },
                onAddComponent = { entry ->
                    form = form.copy(components = form.components + entry)
                    sheetState = SheetValue.Hidden
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Section Header Component
// ---------------------------------------------------------------------------

@Composable
private fun SectionHeader(number: Int, title: String) {
    val tokens = LocalAppTokens.current
    Column {
        Text(
            text = "Section $number — $title",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = title_color
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = grey_border)
    }
}

// ---------------------------------------------------------------------------
// Single Tax Component Card Component
// ---------------------------------------------------------------------------

@Composable
private fun TaxComponentCard(
    index: Int,
    component: TaxComponentEntry,
    componentNameOptions: List<String>,
    canDelete: Boolean,
    onChange: (TaxComponentEntry) -> Unit,
    onDelete: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(whiteBg)
            .border(1.dp, BorderGray, RoundedCornerShape(tokens.cardCornerRadius))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tax Component ${index + 1}",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (canDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove component",
                    tint = redText,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onDelete() }
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        FormLabel("Component Name", isRequired = true)
        FormDropdown(
            value = component.componentName.ifEmpty { "Select component" },
            expanded = expanded,
            onExpandChange = { expanded = it },
            options = componentNameOptions,
            onOptionSelected = { onChange(component.copy(componentName = it)) }
        )

        Spacer(Modifier.height(10.dp))
        FormLabel("Rate (%)", isRequired = true)
        FormTextField(
            value = component.ratePercent,
            onValueChange = { onChange(component.copy(ratePercent = it)) },
            keyboardType = KeyboardType.Number,
            placeholder = "e.g. 9"
        )
    }
}

// ---------------------------------------------------------------------------
// Total Tax Rate Summary Row Component
// ---------------------------------------------------------------------------

@Composable
private fun TotalTaxRateRow(total: Int) {
    val tokens = LocalAppTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(primary_light)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Total Tax Rate",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        Text(
            text = "$total%",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
    }
}

// ---------------------------------------------------------------------------
// Half-Page Add Tax Component Sheet using SmoothBottomSheet
// ---------------------------------------------------------------------------

@Composable
fun AddTaxComponentSheet(
    sheetState: SheetValue,
    onStateChange: (SheetValue) -> Unit,
    componentNameOptions: List<String>,
    onDismiss: () -> Unit,
    onBlurScrimChange: (blurRadius: Dp, scrimAlpha: Float) -> Unit = { _, _ -> }, // Added missing parameter
    onAddComponent: (TaxComponentEntry) -> Unit
) {
    val tokens = LocalAppTokens.current
    var componentName by remember { mutableStateOf("") }
    var ratePercent by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    // Reset input fields when sheet opens or closes
    LaunchedEffect(sheetState) {
        if (sheetState == SheetValue.Hidden) {
            componentName = ""
            ratePercent = ""
            expanded = false
        }
    }

    SmoothBottomSheet(
        state = sheetState,
        onStateChange = onStateChange,
        peekHeight = 360.dp,
        collapsedFraction = 0.52f, // Locks height to half page
        expandedFraction = 0.52f,  // Prevents expanding to full screen
        maxBlurRadius = 14.dp,
        onBlurScrimChange = onBlurScrimChange, // Connected correctly
        sheetBackgroundColor = Primary_background,
        scrollableContent = false,
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
                    .padding(bottom = 70.dp)
            ) {
                // Header Title
                Text(
                    text = "ADD TAX COMPONENT",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // Component Name Dropdown
                FormLabel("Component Name", isRequired = true)
                FormDropdown(
                    value = componentName.ifEmpty { "Select component" },
                    expanded = expanded,
                    onExpandChange = { expanded = it },
                    options = componentNameOptions,
                    onOptionSelected = { componentName = it }
                )

                Spacer(Modifier.height(14.dp))

                // Rate Percentage TextField
                FormLabel("Rate (%)", isRequired = true)
                FormTextField(
                    value = ratePercent,
                    onValueChange = { ratePercent = it },
                    keyboardType = KeyboardType.Number,
                    placeholder = "e.g. 9"
                )
            }

            // Bottom Navigation Buttons using StepNavigationFab
            StepNavigationFab(
                showBack = true,
                onBack = onDismiss,
                backLabel = "Cancel",
                showBackArrow = false,
                showTrailingArrow = false,
                trailingAction = TrailingFabAction.Next(
                    label = "Add Component",
                    enabled = componentName.isNotBlank() && ratePercent.isNotBlank(),
                    onClick = {
                        if (componentName.isNotBlank() && ratePercent.isNotBlank()) {
                            onAddComponent(
                                TaxComponentEntry(
                                    componentName = componentName.trim(),
                                    ratePercent = ratePercent.trim()
                                )
                            )
                        }
                    }
                )
            )
        }
    }
}