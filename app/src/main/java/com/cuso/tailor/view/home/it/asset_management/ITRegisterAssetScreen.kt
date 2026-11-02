package com.cuso.tailor.view.home.it.asset_management

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.it.RegisterAssetRequest
import com.cuso.tailor.ui.theme.Primary
import com.cuso.tailor.ui.theme.Primary_background
import com.cuso.tailor.ui.theme.TextSecondary
import com.cuso.tailor.ui.theme.mutedText
import com.cuso.tailor.ui.theme.sectionBorder
import com.cuso.tailor.ui.theme.title_color
import com.cuso.tailor.ui.theme.whiteBg
import com.cuso.tailor.view.composable.DatePickerField
import com.cuso.tailor.view.composable.FormDropdown
import com.cuso.tailor.view.composable.FormLabel
import com.cuso.tailor.view.composable.FormTextField
import com.cuso.tailor.view.composable.TitleBar
import com.cuso.tailor.viewmodel.ItViewModel
import kotlin.random.Random

private fun convertDisplayDateToIso(dateStr: String): String? {
    if (dateStr.isBlank()) return null
    val parts = dateStr.split("-")
    return if (parts.size == 3) {
        "${parts[2]}-${parts[1]}-${parts[0]}"
    } else {
        dateStr
    }
}

@Composable
fun ItRegisterAssetScreen(
    viewModel: ItViewModel,
    onNavigateBack: () -> Unit
) {
    val tokens = LocalAppTokens.current

    var assetName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Laptop") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var assetId by remember { mutableStateOf("IT-AST-8492") }
    var serialNumber by remember { mutableStateOf("") }
    var manufacturer by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("New") }

    var branchLocation by remember { mutableStateOf("") }
    var assignedDepartment by remember { mutableStateOf("") }
    var locationDetails by remember { mutableStateOf("") }

    var costText by remember { mutableStateOf("1200") }
    var vendor by remember { mutableStateOf("") }
    var purchaseDate by remember { mutableStateOf("23-10-2026") }
    var warrantyUntil by remember { mutableStateOf("31-10-2026") }

    val categoryList = remember {
        listOf("Laptop", "Desktop", "POS Terminal", "Printer", "Scanner", "Server", "Software License", "Other")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary_background)
    ) {
        TitleBar(
            title = "Register New Asset",
            subtitle = "Inventory",
            onClose = onNavigateBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = tokens.screenPadding, vertical = tokens.screenPadding / 2),
            verticalArrangement = Arrangement.spacedBy(tokens.screenPadding)
        ) {
            // Card 1: Asset Core Details
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(whiteBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                    .padding(tokens.screenPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
                    Column {
                        FormLabel(text = "Asset Name", isRequired = true)
                        FormTextField(
                            value = assetName,
                            onValueChange = { assetName = it },
                            placeholder = "e.g., MacBook Pro 16 - Engineering"
                        )
                    }

                    FormDropdown(
                        label = "Asset Category",
                        value = selectedCategory,
                        expanded = categoryExpanded,
                        onExpandChange = { categoryExpanded = it },
                        options = categoryList,
                        onOptionSelected = { selectedCategory = it },
                        isRequired = true
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormLabel(text = "Asset ID", isRequired = true)
                            Text(text = "Unique Code", fontSize = tokens.caption, color = mutedText)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                FormTextField(
                                    value = assetId,
                                    onValueChange = { assetId = it },
                                    placeholder = "IT-AST-8492"
                                )
                            }

                            Button(
                                onClick = {
                                    assetId = "AST-${Random.nextInt(1000, 9999)}"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                modifier = Modifier.height(tokens.fieldHeight)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 4)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = whiteBg,
                                        modifier = Modifier.size(tokens.iconSize)
                                    )
                                    Text(
                                        text = "Generate",
                                        fontSize = tokens.bodySmall,
                                        color = whiteBg,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        FormLabel(text = "Serial Number (S/N)", isRequired = true)
                        FormTextField(
                            value = serialNumber,
                            onValueChange = { serialNumber = it },
                            placeholder = "ENTER SERIAL NUMBER (S/N)"
                        )
                    }
                }
            }

            // Card 2: Manufacturer, Model, Condition
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(whiteBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                    .padding(tokens.screenPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Manufacturer")
                            FormTextField(
                                value = manufacturer,
                                onValueChange = { manufacturer = it },
                                placeholder = "e.g., Apple, Dell"
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Model")
                            FormTextField(
                                value = model,
                                onValueChange = { model = it },
                                placeholder = "e.g., M2 Max 2023"
                            )
                        }
                    }

                    Column {
                        FormLabel(text = "Condition", isRequired = true)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .background(Primary_background)
                                .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .padding(tokens.screenPadding / 6),
                            horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 6)
                        ) {
                            listOf("New", "Good", "Fair", "Repair").forEach { condition ->
                                val isSelected = selectedCondition == condition
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                        .background(if (isSelected) whiteBg else Primary_background)
                                        .clickable { selectedCondition = condition }
                                        .padding(vertical = tokens.screenPadding / 3),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = condition,
                                        fontSize = tokens.caption,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Primary else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 3: Assignment & Location
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(whiteBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                    .padding(tokens.screenPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 4)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Text(
                            text = "Assignment & Location",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    }

                    Column {
                        FormLabel(text = "Branch Location")
                        FormTextField(
                            value = branchLocation,
                            onValueChange = { branchLocation = it },
                            placeholder = "Select Branch (e.g., Chennai, HQ)"
                        )
                    }

                    Column {
                        FormLabel(text = "Assigned Department")
                        FormTextField(
                            value = assignedDepartment,
                            onValueChange = { assignedDepartment = it },
                            placeholder = "Select Department (e.g., IT, Engineering)"
                        )
                    }

                    Column {
                        FormLabel(text = "Location Details")
                        FormTextField(
                            value = locationDetails,
                            onValueChange = { locationDetails = it },
                            placeholder = "e.g., IT Office, Floor 3"
                        )
                    }
                }
            }

            // Card 4: Purchase & Warranty using DatePickerField
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                    .background(whiteBg)
                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius))
                    .padding(tokens.screenPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(tokens.screenPadding * 0.75f)) {
                    Text(
                        text = "Purchase & Warranty",
                        fontSize = tokens.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Cost")
                            FormTextField(
                                value = costText,
                                onValueChange = { costText = it },
                                keyboardType = KeyboardType.Number,
                                placeholder = "e.g., 1200",
                                trailingText = "INR"
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Vendor")
                            FormTextField(
                                value = vendor,
                                onValueChange = { vendor = it },
                                placeholder = "e.g., Tech Supplies"
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Purchase Date")
                            DatePickerField(
                                value = purchaseDate,
                                onDateSelected = { purchaseDate = it }
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel(text = "Warranty Until")
                            DatePickerField(
                                value = warrantyUntil,
                                onDateSelected = { warrantyUntil = it }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(tokens.screenPadding))
        }

        // Fixed Bottom Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(whiteBg)
                .border(1.dp, sectionBorder)
                .padding(tokens.screenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 2)
            ) {
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = whiteBg),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .height(tokens.buttonHeight)
                        .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = tokens.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }

                Button(
                    onClick = {
                        val req = RegisterAssetRequest(
                            assetName = assetName.trim(),
                            type = "Hardware",
                            category = selectedCategory,
                            serialNumber = serialNumber.trim(),
                            manufacturer = manufacturer.ifBlank { null },
                            model = model.ifBlank { null },
                            condition = selectedCondition,
                            cost = costText.toDoubleOrNull() ?: 0.0,
                            currency = "INR",
                            department = assignedDepartment.ifBlank { null },
                            locationDetails = locationDetails.ifBlank { null },
                            purchaseDate = convertDisplayDateToIso(purchaseDate),
                            branchName = branchLocation.ifBlank { null },
                            vendor = vendor.ifBlank { null },
                            warrantyUntil = convertDisplayDateToIso(warrantyUntil)
                        )

                        viewModel.registerAsset(
                            request = req,
                            onSuccess = { onNavigateBack() },
                            onError = { }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(tokens.buttonHeight)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(tokens.screenPadding / 4)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = whiteBg,
                            modifier = Modifier.size(tokens.iconSize)
                        )
                        Text(
                            text = "Register Asset",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = whiteBg
                        )
                    }
                }
            }
        }
    }
}