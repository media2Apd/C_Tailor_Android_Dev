package com.cuso.mobile.view.home.inventory.procurement.location_management

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.computeHorizontalBounds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.R

@Composable
fun StockLocationDetailsScreen(
    onClose: () -> Unit,
    onNavigateToLocationForm: () -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "Location", "Variants")

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar("Stock Location  Details", onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(Modifier.height(10.dp))

            // Header Product Info Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(tokens.screenPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(tokens.iconSize * 2.5f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_horiz_3_lines),
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize * 1.3f)
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding * 1.2f))

                Column {
                    Text(
                        text = "Men Formal Shirt",
                        fontSize = tokens.bodyLarge,
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
            Spacer(Modifier.height(10.dp))

            // Tab Row
            AppUnderlineTabRow(
                tabs = tabTitles,
                selectedIndex = selectedTab,
                onTabSelected = { selectedTab = it }
            )
            Spacer(Modifier.height(10.dp))

            //  Tab Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
            ) {
                when (selectedTab) {
                    0 -> {
                        item { OverviewLocationTabContent(tokens) }
                    }
                    1 -> {
                        item { LocationBreakdownTabContent(tokens, onNavigateToLocationForm) }
                    }
                    2 -> {
                        item { VariantsLocationTabContent(tokens) }
                    }
                }
            }
        }
    }
}

// ── Tab 1: Overview ──
@Composable
private fun OverviewLocationTabContent(tokens: AppDesignTokens) {
    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)) {
        // Item Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = whiteBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(tokens.screenPadding)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Item Information", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                            .background(light_grey)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("GENERAL", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ITEM TYPE", fontSize = tokens.label, color = mutedText)
                        Text("Finished Goods", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PRIMARY UOM", fontSize = tokens.label, color = mutedText)
                        Text("Piece", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                Text("CATEGORY", fontSize = tokens.label, color = mutedText)
                Text("ABC Fashion - Apparel - Shirt - Formal", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)

                Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BRAND", fontSize = tokens.label, color = mutedText)
                        Text("ABC Fashion", fontSize = tokens.bodySmall, fontWeight = FontWeight.Medium, color = title_color)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("REORDER LEVEL", fontSize = tokens.label, color = mutedText)
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(yellowBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("50 piece", fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = yellowText)
                        }
                    }
                }
            }
        }

        // Tax Details Section
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
                    Text("Tax Details", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                            .background(greenBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("TAXATION", fontSize = tokens.label, fontWeight = FontWeight.Bold, color = darkGreenBg)
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(tokens.extraPadding)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(badgeGrey, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
                        Column {
                            Text("GST RATE", fontSize = tokens.label, color = mutedText)
                            Text("12%", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(badgeGrey, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                            .padding(tokens.extraPadding * 1.2f)
                    ) {
                        Column {
                            Text("HSN CODE", fontSize = tokens.label, color = mutedText)
                            Text("6205", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                        }
                    }
                }
            }
        }
    }
}

// ── Tab 2: Location ──
@Composable
private fun LocationBreakdownTabContent(
    tokens: AppDesignTokens,
    onNavigateToLocationForm: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)) {
        // Warehouse Card 1
        WarehouseLocationCard(
            tokens = tokens,
            name = "Chennai - Central Warehouse",
            address = "Central WH - Ground - Apparel - R12 - b04",
            isCentral = true,
            totalQty = "140 Piece",
            available = "133 Piece",
            reserved = "7 Piece",
            variants = listOf(
                "M - white - Regular" to "45",
                "L - white - Regular" to "40",
                "M - Bule - Regular" to "28",
                "XL - Bule - Slim" to "20"
            ),
            onClick = onNavigateToLocationForm
        )

        // Warehouse Card 2
        WarehouseLocationCard(
            tokens = tokens,
            name = "Chennai - T Nagar Showroom",
            address = "Showroom Store - Display - R12 - b04",
            isCentral = false,
            totalQty = "18 Piece",
            available = "17 Piece",
            reserved = "1 Piece",
            variants = listOf(
                "M - white - Regular" to "9",
                "L - white - Regular" to "8"
            ),
            onClick = onNavigateToLocationForm
        )
    }
}

@Composable
private fun WarehouseLocationCard(
    tokens: AppDesignTokens,
    name: String,
    address: String,
    isCentral: Boolean,
    totalQty: String,
    available: String,
    reserved: String,
    variants: List<Pair<String, String>>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
            // Header: Icon + Name + Tags
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(tokens.iconSize * 2f)
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                        .background(primary_light),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCentral) Icons.Outlined.Storefront else Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Spacer(Modifier.width(tokens.extraPadding))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(address, fontSize = tokens.label, color = mutedText)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TagPill(text = "Good", tokens = tokens)
                    TagPill(text = "FIFO", tokens = tokens)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
            HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            // Metrics Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Quantity", fontSize = tokens.label, color = mutedText)
                    Text(totalQty, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Available", fontSize = tokens.label, color = mutedText)
                    Text(available, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = complete_button_bg)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Reserved", fontSize = tokens.label, color = mutedText)
                    Text(reserved, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = yellowText)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            // Variant Breakdown List
            Text("Variant Breakdown", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = title_color)
            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                variants.forEach { (variantName, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(badgeGrey, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                            .padding(horizontal = tokens.extraPadding * 1.2f, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(variantName, fontSize = tokens.caption, color = TextSecondary)
                        Text(count, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                    }
                }
            }
        }
    }
}

// ── Tab 3: Variants ──
@Composable
private fun VariantsLocationTabContent(tokens: AppDesignTokens) {
    Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = tokens.extraPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("VARIANT ITEMS (2)", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = mutedText)
            Text("Total: 108 Pieces", fontSize = tokens.caption, color = mutedText)
        }

        listOf(
            Triple("M - white - Regular", "Size: Medium • Color: White", "SHIRT - MFS - WHT - M - R"),
            Triple("L - white - Regular", "Size: Large • Color: White", "SHIRT - MFS - WHT - L - R")
        ).forEach { (name, desc, sku) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(0.dp),
                colors = CardDefaults.cardColors(containerColor = whiteBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.padding(tokens.screenPadding)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(light_grey)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(name, fontSize = tokens.caption, fontWeight = FontWeight.SemiBold, color = title_color)
                        }

                        Spacer(Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                .background(greenBg)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Active", fontSize = tokens.label, fontWeight = FontWeight.Medium, color = darkGreenBg)
                        }

                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { }, modifier = Modifier.size(tokens.iconSize * 1.2f)) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize))
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(desc, fontSize = tokens.label, color = mutedText)

                    Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("SKU", fontSize = tokens.label, color = mutedText)
                            Text(sku, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = title_color)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL STOCK", fontSize = tokens.label, color = mutedText)
                            Text("54 Piece", fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagPill(text: String, tokens: AppDesignTokens) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
            .background(light_grey)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = tokens.label, color = TextSecondary)
    }
}