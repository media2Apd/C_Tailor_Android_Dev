package com.cuso.mobile.view.home.inventory.procurement.location_management

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*

@Composable
fun StockLocationFormScreen(
    onClose: () -> Unit,
    onSave: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    // Location Fields
    var floor by remember { mutableStateOf("Ground/1st Floor") }
    var section by remember { mutableStateOf("Garments") }
    var rack by remember { mutableStateOf("R- 12") }
    var bin by remember { mutableStateOf("B-04") }

    // Optional Fields
    var stockCondition by remember { mutableStateOf("Goods") }
    var asOfDate by remember { mutableStateOf("02/09/2029") }
    var enableRotationTracking by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar("Location Form", onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(tokens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)
        ) {
            // Header Product Info Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.65f))
                        .padding(tokens.screenPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(tokens.iconSize * 2.4f)
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(primary_light),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize * 1.2f)
                        )
                    }

                    Spacer(Modifier.width(tokens.extraPadding * 1.2f))

                    Column {
                        Text(
                            text = "Men Formal Shirt",
                            fontSize = tokens.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = title_color
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "ABC Fashion - Apparel - Shirt - Formal",
                            fontSize = tokens.caption,
                            color = TextSecondary
                        )
                    }
                }
            }

            // ── Section 1: Location ──
            item {
                FormCardContainer(
                    tokens = tokens,
                    title = "Location",
                    trailingBadge = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                                .background(activity_purple_bg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Warehouse A", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = activity_purple)
                        }
                    }
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Floor")
                            FormTextField(value = floor, onValueChange = { floor = it }, placeholder = "")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Section")
                            FormTextField(value = section, onValueChange = { section = it }, placeholder = "")
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Rack")
                            FormTextField(value = rack, onValueChange = { rack = it }, placeholder = "")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Bin")
                            FormTextField(value = bin, onValueChange = { bin = it }, placeholder = "")
                        }
                    }
                }
            }

            // ── Section 2: Stock ──
            item {
                FormCardContainer(
                    tokens = tokens,
                    title = "Stock",
                    trailingBadge = {
                        Text("1 SKU item", fontSize = tokens.caption, color = mutedText)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(badgeGrey, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SKU", fontSize = tokens.label, color = mutedText)
                                Text("VARIANT", fontSize = tokens.label, color = mutedText)
                                Text("TOTAL STOCK", fontSize = tokens.label, color = mutedText)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SHIRT-MFS-WHT-M-R", fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                                Text("M - white - Regular", fontSize = tokens.caption, color = TextSecondary)
                                Text("54 Piece", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = title_color)
                            }

                            Spacer(Modifier.height(tokens.extraPadding))
                            HorizontalDivider(color = grey_border.copy(alpha = 0.6f), thickness = 0.8.dp)
                            Spacer(Modifier.height(tokens.extraPadding))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Item Status", fontSize = tokens.caption, color = mutedText)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                        .background(greenBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Active", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = darkGreenBg)
                                }
                            }
                        }
                    }
                }
            }

            // ── Section 3: Optional Details ──
            item {
                FormCardContainer(tokens = tokens, title = "Optional Details") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("Stock Condition")
                            FormTextField(value = stockCondition, onValueChange = { stockCondition = it }, placeholder = "")
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormLabel("As of Date")
                            DatePickerField(
                                value = asOfDate,
                                onDateSelected = { asOfDate = it }
                            )
                        }
                    }

                    Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                    // Stock Rotation Tracking Switch Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .background(whiteBg, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Stock Condition", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                                Text("Enable Stock Rotation Tracking", fontSize = tokens.caption, color = mutedText)
                            }
                            Switch(
                                checked = enableRotationTracking,
                                onCheckedChange = { enableRotationTracking = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = whiteBg, checkedTrackColor = Primary)
                            )
                        }
                    }
                }
            }

            // ── Section 4: Bottom Buttons ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = tokens.extraPadding),
                    horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)
                ) {
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = modelGray),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                    ) {
                        Text("Cancel", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = title_color)
                    }

                    Button(
                        onClick = onSave,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        modifier = Modifier.weight(1f).height(tokens.buttonHeight)
                    ) {
                        Text("Save", fontSize = tokens.bodySmall, fontWeight = FontWeight.SemiBold, color = whiteBg)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormCardContainer(
    tokens: AppDesignTokens,
    title: String,
    trailingBadge: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                trailingBadge?.invoke()
            }
            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
            content()
        }
    }
}