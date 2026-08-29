@file:Suppress("UNUSED_PARAMETER")

package com.cuso.mobile.view.home.sales.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.FormLabel
import com.cuso.mobile.view.composable.FormTextArea
import com.cuso.mobile.view.composable.FormTextField
import com.cuso.mobile.view.composable.StepNavigationFab
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.TrailingFabAction
import com.cuso.mobile.view.home.sales.lead.MiniSwitch

@Composable
fun AddSegmentScreen(
    onClose: () -> Unit = {},
    onSubmit: (name: String, code: String, description: String, displayOrder: Int, isActive: Boolean) -> Unit = { _, _, _, _, _ -> }
) {
    val tokens = LocalAppTokens.current

    var segmentName by remember { mutableStateOf("") }
    var segmentCode by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var displayOrder by remember { mutableStateOf("5") }
    var isActiveStatus by remember { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    fun autoGenerateCode() {
        if (segmentName.isNotBlank()) {
            segmentCode = segmentName.trim().uppercase().replace(" ", "_")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = "Add Segment",
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

                // Segment Name Field
                Column {
                    FormLabel(text = "Segment Name", isRequired = true)
                    FormTextField(
                        value = segmentName,
                        onValueChange = { segmentName = it },
                        placeholder = "Corporate"
                    )
                }

                // Segment Code Field
                Column {
                    FormLabel(text = "Segment Code")
                    FormTextField(
                        value = segmentCode,
                        onValueChange = { segmentCode = it },
                        placeholder = "CORPORATE"
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Auto-generate from name",
                        color = Primary,
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { autoGenerateCode() }
                    )
                }

                // Description Field
                Column {
                    FormLabel(text = "Description")
                    FormTextArea(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Corporate and institutional garments",
                        minLines = 3,
                        maxLines = 5
                    )
                }

                // Display Order Field
                Column {
                    FormLabel(text = "Display Order")
                    FormTextField(
                        value = displayOrder,
                        onValueChange = { displayOrder = it },
                        placeholder = "5",
                        keyboardType = KeyboardType.Number
                    )
                }

                // Status Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Status",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Default active status",
                            fontSize = tokens.caption,
                            color = mutedText
                        )
                    }
                    MiniSwitch(
                        checked = isActiveStatus,
                        onCheckedChange = {isActiveStatus = it}
                    )
                }

                Spacer(Modifier.height(100.dp))
            }
        }

        // Floating Step Navigation FAB (Cancel / Create Segment)
        StepNavigationFab(
            showBack = true,
            onBack = onClose,
            backLabel = "Cancel",
            showBackArrow = false,
            showTrailingArrow = false,
            trailingAction = TrailingFabAction.Next(
                label = "Create Segment",
                onClick = {
                    onSubmit(
                        segmentName,
                        segmentCode,
                        description,
                        displayOrder.toIntOrNull() ?: 0,
                        isActiveStatus
                    )
                }
            )
        )
    }
}