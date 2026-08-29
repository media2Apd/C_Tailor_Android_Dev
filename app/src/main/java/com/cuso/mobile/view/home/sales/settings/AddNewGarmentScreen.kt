@file:Suppress("UNUSED_PARAMETER","unusedVariable")

package com.cuso.mobile.view.home.sales.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.FormDropdown
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextArea
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction

@Composable
fun AddNewGarmentScreen(
    onClose: () -> Unit = {},
    onSubmit: (
        garmentName: String,
        displayName: String,
        segment: String,
        baseTemplate: String,
        description: String
    ) -> Unit = { _, _, _, _, _ -> }
) {
    val tokens = LocalAppTokens.current

    var garmentName by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Select Category") }
    var selectedSegment by remember { mutableStateOf("Select segment") }
    var selectedBaseTemplate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var segmentExpanded by remember { mutableStateOf(false) }
    var templateExpanded by remember { mutableStateOf(false) }

    val categoryOptions = listOf("Shirts", "Trousers", "Suits", "Kurtas", "Blazers", "Waistcoats")
    val segmentOptions = listOf("Men", "Women", "Kids")
    val templateOptions = listOf("None", "Standard Shirt", "Slim Fit Shirt", "Casual Shirt", "Formal Trouser")

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = "Add New Garment",
                    onClose = onClose
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = tokens.screenPadding)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // Garment Name Field
                Column {
                    FormLabel(text = "Garment Name", isRequired = true)
                    FormTextField(
                        value = garmentName,
                        onValueChange = { garmentName = it },
                        placeholder = "e.g. Suit 3-Piece"
                    )
                }

                // Display Name Field
                Column {
                    FormLabel(text = "Display Name", isRequired = true)
                    FormTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        placeholder = "e.g. Three Piece Suit"
                    )
                }

                // Category Dropdown
//                Column {
//                    FormDropdown(
//                        label = "Category",
//                        value = selectedCategory,
//                        expanded = categoryExpanded,
//                        onExpandChange = { categoryExpanded = it },
//                        options = categoryOptions,
//                        onOptionSelected = { selectedCategory = it },
//                        isRequired = true
//                    )
//                }

                // Segment Dropdown
                Column {
                    FormDropdown(
                        label = "Segment",
                        value = selectedSegment,
                        expanded = segmentExpanded,
                        onExpandChange = { segmentExpanded = it },
                        options = segmentOptions,
                        onOptionSelected = { selectedSegment = it },
                        isRequired = true
                    )
                }

                // Base Template Dropdown
                Column {
                    FormDropdown(
                        label = "Base Template (Optional)",
                        value = selectedBaseTemplate.ifEmpty { "e.g. Standard Shirt" },
                        expanded = templateExpanded,
                        onExpandChange = { templateExpanded = it },
                        options = templateOptions,
                        onOptionSelected = { selectedBaseTemplate = if (it == "None") "" else it }
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Copy measurements/settings from an existing garment type.",
                        fontSize = tokens.caption,
                        color = mutedText
                    )
                }

                // Description Field
                Column {
                    FormLabel(text = "Description")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Brief description of this garment type.",
                        minLines = 4,
                        maxLines = 6
                    )
                }

                Spacer(Modifier.height(100.dp))
            }
        }

        // Floating Step Navigation FAB (Cancel / Create Garment)
        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = "Create Garment",
                onClick = {
                    onSubmit(
                        garmentName,
                        displayName,
                        selectedSegment,
                        selectedBaseTemplate,
                        description
                    )
                }
            )
        )
    }
}