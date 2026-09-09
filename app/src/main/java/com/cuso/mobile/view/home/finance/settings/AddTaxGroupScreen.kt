package com.cuso.mobile.view.home.finance.settings

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextArea
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.TitleBar
import java.util.UUID

// ---------------------------------------------------------------------------
// Data models
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
    var showAddComponentSheet by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(whiteBg)) {
        TitleBar(title = "Add Tax Group", onClose = onClose)
        HorizontalDivider(color = title_border)

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = tokens.screenPadding, vertical = tokens.screenPadding)
        ) {
            item {
                SectionHeader(number = 1, title = "Basic Information")
                Spacer(Modifier.height(14.dp))

                FormLabel("Tax Group Name", isRequired = true)
                FormTextField(
                    value = form.groupName,
                    onValueChange = { form = form.copy(groupName = it) }
                )
                Spacer(Modifier.height(14.dp))

                FormLabel("Tax Group Code", isRequired = true)
                FormTextField(
                    value = form.groupCode,
                    onValueChange = { form = form.copy(groupCode = it) }
                )
                Spacer(Modifier.height(14.dp))

                FormLabel("Tax Type", isRequired = true)
                FormDropdown(
                    label = "Tax Type",
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
                    onValueChange = { form = form.copy(description = it) }
                )

                Spacer(Modifier.height(24.dp))
                SectionHeader(number = 2, title = "Tax Components")
                Spacer(Modifier.height(4.dp))
                Text(
                    "Add the individual taxes that make up this tax group.",
                    fontSize = tokens.caption,
                    color = TextSecondary
                )
                Spacer(Modifier.height(14.dp))
            }

            itemsIndexed(form.components) { index, component ->
                TaxComponentCard(
                    index = index,
                    component = component,
                    componentNameOptions = componentNameOptions,
                    canDelete = form.components.size > 1,
                    onChange = { updated ->
                        form = form.copy(components = form.components.toMutableList().also { it[index] = updated })
                    },
                    onDelete = {
                        form = form.copy(components = form.components.filterIndexed { i, _ -> i != index })
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
                        ) { showAddComponentSheet = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Add Tax Component",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                }

                Spacer(Modifier.height(18.dp))

                TotalTaxRateRow(total = totalRate(form.components))
            }
        }

//        TaxFormBottomBar(
//            onCancel = onClose,
//            onConfirm = { onCreateGroup(form) },
//            confirmLabel = "Create Group"
//        )
    }

    if (showAddComponentSheet) {
        AddTaxComponentSheet(
            componentNameOptions = componentNameOptions,
            onDismiss = { showAddComponentSheet = false },
            onAddComponent = { entry ->
                form = form.copy(components = form.components + entry)
                showAddComponentSheet = false
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Section header with underline, e.g. "Section 1 — Basic Information"
// ---------------------------------------------------------------------------

@Composable
private fun SectionHeader(number: Int, title: String) {
    val tokens = LocalAppTokens.current
    Column {
        Text(
            "Section $number — $title",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = title_color
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = title_border)
    }
}

// ---------------------------------------------------------------------------
// Single tax component card ("Tax Component 1", delete icon, name + rate)
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
            .background(PanelBg)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tax Component ${index + 1}",
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (canDelete) {
                Icon(
                    Icons.Default.Delete,
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
            label = "Component Name",
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
            keyboardType = KeyboardType.Number
        )
    }
}

// ---------------------------------------------------------------------------
// Total tax rate summary row
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
        Text("Total Tax Rate", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
        Text("$total%", fontSize = tokens.h2, fontWeight = FontWeight.Bold, color = Primary)
    }
}

// ---------------------------------------------------------------------------
// "Add Tax Component" bottom sheet
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaxComponentSheet(
    componentNameOptions: List<String>,
    onDismiss: () -> Unit,
    onAddComponent: (TaxComponentEntry) -> Unit
) {
    val tokens = LocalAppTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var componentName by remember { mutableStateOf("") }
    var ratePercent by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = whiteBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
        ) {
            Text(
                "ADD TAX COMPONENT",
                fontSize = tokens.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))
            FormLabel("Component Name", isRequired = true)
            FormDropdown(
                label = "Component Name",
                value = componentName.ifEmpty { "Select component" },
                expanded = expanded,
                onExpandChange = { expanded = it },
                options = componentNameOptions,
                onOptionSelected = { componentName = it }
            )

            Spacer(Modifier.height(14.dp))
            FormLabel("Rate (%)", isRequired = true)
            FormTextField(
                value = ratePercent,
                onValueChange = { ratePercent = it },
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(20.dp))
//            TaxFormBottomBar(
//                onCancel = onDismiss,
//                onConfirm = {
//                    if (componentName.isNotBlank() && ratePercent.isNotBlank()) {
//                        onAddComponent(TaxComponentEntry(componentName = componentName, ratePercent = ratePercent))
//                    }
//                },
//                confirmLabel = "Add Component"
//            )
            Spacer(Modifier.height(12.dp))
        }
    }
}