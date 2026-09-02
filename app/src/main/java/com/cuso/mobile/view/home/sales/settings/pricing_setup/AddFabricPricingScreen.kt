package com.cuso.mobile.view.home.sales.settings.pricing_setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.DatePickerField
import com.cuso.mobile.view.composable.DynamicIslandError
import com.cuso.mobile.view.composable.DynamicIslandSuccess
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.home.sales.lead.MiniSwitch
//import com.cuso.mobile.viewmodel.SettingsViewModel

@Composable
fun AddFabricPriceScreen(
//    viewModel: SettingsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
    onSaveSuccess: () -> Unit = onClose
) {
    val tokens = LocalAppTokens.current

    var fabricName by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var fabricType by remember { mutableStateOf("") }
    var fabricColor by remember { mutableStateOf("") }

    var currentPrice by remember { mutableStateOf("") }
    var newSalesPrice by remember { mutableStateOf("") }
    var effectiveFromDate by remember { mutableStateOf("") }

    var isStatusActive by remember { mutableStateOf(true) }
    var pricePerMeter by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }

    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TitleBar(
                    title = "Add Fabric Price",
                    onClose = onClose
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = tokens.screenPadding, vertical = 8.dp)
                    .padding(bottom = 100.dp)
            ) {
                // Fabric Field
                FormLabel(text = "Fabric", isRequired = false)
                FormTextField(
                    value = fabricName,
                    onValueChange = { fabricName = it },
                    placeholder = "Linen Premium"
                )

                Spacer(Modifier.height(14.dp))

                // SKU Field
                FormLabel(text = "SKU", isRequired = false)
                FormTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    placeholder = "FAB-002"
                )

                Spacer(Modifier.height(14.dp))

                // Fabric Type Field
                FormLabel(text = "Fabric Type", isRequired = false)
                FormTextField(
                    value = fabricType,
                    onValueChange = { fabricType = it },
                    placeholder = "Linen"
                )

                Spacer(Modifier.height(14.dp))

                // Fabric Color Field
                FormLabel(text = "Fabric Color", isRequired = false)
                FormTextField(
                    value = fabricColor,
                    onValueChange = { fabricColor = it },
                    placeholder = "Blue"
                )

                Spacer(Modifier.height(14.dp))

                // Current Price and New Sales Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Current Price", isRequired = false)
                        FormTextField(
                            value = currentPrice,
                            onValueChange = { currentPrice = it },
                            placeholder = "—"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "New Sales Price", isRequired = false)
                        FormTextField(
                            value = newSalesPrice,
                            onValueChange = { newSalesPrice = it },
                            placeholder = "₹ 500",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Effective From Date Picker Field
                FormLabel(text = "Effective From", isRequired = false)
                DatePickerField(
                    value = effectiveFromDate,
                    onDateSelected = { selectedDate ->
                        effectiveFromDate = selectedDate
                    }
                )

                Spacer(Modifier.height(16.dp))

                // Status Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                    MiniSwitch(
                        checked = isStatusActive,
                        onCheckedChange = { isStatusActive = it }
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Price Per Meter and Unit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Price per Meter", isRequired = false)
                        FormTextField(
                            value = pricePerMeter,
                            onValueChange = { pricePerMeter = it },
                            placeholder = "₹ 600",
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel(text = "Unit", isRequired = false)
                        FormTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            placeholder = "Meter"
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "New price will be used for future quotations from the effective date.",
                    fontSize = 11.sp,
                    color = iconMuted
                )
            }
        }

        // Floating Bottom Navigation FAB
        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            showBackArrow = false,
            onBack = onClose,
            trailingAction = TrailingFabAction.Update(
                label = "Save Fabric Price",
                onClick = {
                    successMessage = "Fabric price saved successfully"
                    onSaveSuccess()
                }
            ),
            showTrailingArrow = false
        )

        DynamicIslandSuccess(
            message = successMessage,
            onDismiss = { successMessage = null }
        )

        DynamicIslandError(
            message = errorMessage,
            onDismiss = { errorMessage = null }
        )
    }
}