@file:Suppress("UNUSED_PARAMETER", "AssignedValueIsNeverRead", "SpellCheckingInspection")
package com.cuso.tailor.view.home.finance.settings

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.StepNavigationFab
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.view.composable.TrailingFabAction

// ---------------------------------------------------------------------------
// Data Models
// ---------------------------------------------------------------------------

/**
 * Represents an existing tax component fetched from the backend (equivalent to allTaxes in React).
 */
data class TaxRateItem(
    val id: String,
    val name: String,
    val rate: Double
)

/**
 * Form state matching the React component structure: { name: "", taxRateIds: [] }
 */
data class CreateTaxGroupFormState(
    val name: String = "",
    val selectedTaxIds: Set<String> = emptySet()
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun AddTaxGroupScreen(
    onClose: () -> Unit,
    allTaxes: List<TaxRateItem>,
    isLoading: Boolean = false,
    onCreateGroup: (name: String, taxRateIds: List<String>) -> Unit
) {
    val tokens = LocalAppTokens.current
    var formState by remember { mutableStateOf(CreateTaxGroupFormState()) }

    // Toggle tax ID in selection set (matches toggleTax in React)
    val toggleTax: (String) -> Unit = { taxId ->
        formState = formState.copy(
            selectedTaxIds = if (formState.selectedTaxIds.contains(taxId)) {
                formState.selectedTaxIds - taxId
            } else {
                formState.selectedTaxIds + taxId
            }
        )
    }

    // Calculate total rate for all selected components
    val totalRate = remember(formState.selectedTaxIds, allTaxes) {
        allTaxes
            .filter { it.id in formState.selectedTaxIds }
            .sumOf { it.rate }
    }

    // Validation rule: name must not be blank and at least one tax must be selected
    val isValid = formState.name.isNotBlank() && formState.selectedTaxIds.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // ── Top Title Bar ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(10f),
            color = whiteBg,
            shadowElevation = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TitleBar(title = "Create Tax Group", onClose = onClose)
                HorizontalDivider(color = grey_border, thickness = 1.dp)
            }
        }

        // ── Main Content Area ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentPadding = PaddingValues(
                    horizontal = tokens.screenPadding,
                    vertical = tokens.screenPadding
                )
            ) {
                // Group Name Section
                item {
                    FormLabel(text = "GROUP NAME", isRequired = true)
                    Spacer(Modifier.height(6.dp))
                    FormTextField(
                        value = formState.name,
                        onValueChange = { formState = formState.copy(name = it) },
                        placeholder = "e.g. GST 18% (Intra-state)"
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // Component Selection Header
                item {
                    Text(
                        text = "SELECT TAX COMPONENTS",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                }

                // Empty state fallback
                if (allTaxes.isEmpty()) {
                    item {
                        Text(
                            text = "No tax components found. Please create tax rates first.",
                            fontSize = tokens.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    // List of existing taxes with selection toggle
                    items(allTaxes, key = { it.id }) { tax ->
                        val isSelected = tax.id in formState.selectedTaxIds
                        TaxRateSelectCard(
                            tax = tax,
                            isSelected = isSelected,
                            onToggle = { toggleTax(tax.id) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                // Total Group Tax Rate Summary Card
                item {
                    Spacer(Modifier.height(16.dp))
                    TotalTaxRateCard(totalRate = totalRate)
                    Spacer(Modifier.height(100.dp)) // Space for bottom action bar
                }
            }

            // Bottom Navigation Actions
            StepNavigationFab(
                showBack = true,
                onBack = onClose,
                backLabel = "Cancel",
                showBackArrow = false,
                showTrailingArrow = false,
                trailingAction = TrailingFabAction.Next(
                    label = if (isLoading) "Saving..." else "Create Tax Group",
                    enabled = isValid && !isLoading,
                    onClick = {
                        if (isValid && !isLoading) {
                            onCreateGroup(
                                formState.name.trim(),
                                formState.selectedTaxIds.toList()
                            )
                        }
                    }
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Tax Rate Selectable Card Component
// ---------------------------------------------------------------------------

@Composable
private fun TaxRateSelectCard(
    tax: TaxRateItem,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val tokens = LocalAppTokens.current

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Primary else BorderGray,
        label = "cardBorderColor"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) primary_light else whiteBg,
        label = "cardBgColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(tokens.cardCornerRadius)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Primary,
                    checkmarkColor = Color.White
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = tax.name,
                fontSize = tokens.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }

        Text(
            text = "${tax.rate}%",
            fontSize = tokens.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
    }
}

// ---------------------------------------------------------------------------
// Total Group Tax Rate Card Component
// ---------------------------------------------------------------------------

@SuppressLint("DefaultLocale")
@Composable
private fun TotalTaxRateCard(totalRate: Double) {
    val tokens = LocalAppTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(tokens.cardCornerRadius))
            .background(primary_light)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TOTAL GROUP TAX RATE",
            fontSize = tokens.caption,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
        Text(
            text = String.format("%.2f%%", totalRate),
            fontSize = tokens.bodyLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Primary
        )
    }
}