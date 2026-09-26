package com.cuso.tailor.view.home.inventory.pricing_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.tailor.adaptive_screen.LocalAppTokens
import com.cuso.tailor.model.inventory.*
import com.cuso.tailor.ui.theme.*
import com.cuso.tailor.view.composable.*
import com.cuso.tailor.view.home.sales.lead.MiniSwitch

@Composable
fun NewPriceListScreen(
    onClose: () -> Unit = {},
    onSave: (PriceListFormState) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    var form by remember { mutableStateOf(PriceListFormState()) }
    var itemsList by remember { mutableStateOf(sampleLineItems()) }
    var itemSearch by remember { mutableStateOf("") }

    var basicExpanded by remember { mutableStateOf(true) }
    var typeExpanded by remember { mutableStateOf(true) }
    var schemeExpanded by remember { mutableStateOf(true) }
    var ruleExpanded by remember { mutableStateOf(true) }
    var currencyExpanded by remember { mutableStateOf(true) }

    val isAllItems = form.priceListType == PriceListType.ALL_ITEMS

    Box(modifier = Modifier.fillMaxSize().background(Primary_background)) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TitleBar(title = "New Price List", onClose = onClose)
                    HorizontalDivider(color = title_border)
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // SECTION 1: BASIC DETAILS
                item {
                    AccordionSection(
                        icon = Icons.Default.Menu,
                        title = "1. Basic Details",
                        expanded = basicExpanded,
                        onHeaderClick = { basicExpanded = !basicExpanded }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(whiteBg)
                                .padding(vertical = tokens.extraPadding)
                        ) {
                            FormLabel("Price List Name", isRequired = true)
                            FormTextField(
                                value = form.name,
                                onValueChange = { form = form.copy(name = it) },
                                placeholder = "e.g. EU Wholesale 2026"
                            )

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Transaction Type")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(light_grey, RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            ) {
                                PriceListTransactionType.entries.forEach { tType ->
                                    val isSelected = form.transactionType == tType
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) whiteBg else Color.Transparent)
                                            .clickable { form = form.copy(transactionType = tType) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tType.label,
                                            fontSize = tokens.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Primary else TextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            FormLabel("Description")
                            FormTextArea(
                                value = form.description,
                                onValueChange = { form = form.copy(description = it) },
                                placeholder = "Describe the scope or purpose of this price list..."
                            )
                        }
                    }
                }

                // SECTION 2: PRICE LIST TYPE
                item {
                    AccordionSection(
                        icon = Icons.Default.ShoppingBag,
                        title = "2. Price List Type",
                        expanded = typeExpanded,
                        onHeaderClick = { typeExpanded = !typeExpanded }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(whiteBg)
                                .padding(vertical = tokens.extraPadding)
                        ) {
                            // Option A: All Items
                            val isAllSelected = form.priceListType == PriceListType.ALL_ITEMS
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                    .border(
                                        width = if (isAllSelected) 2.dp else 1.dp,
                                        color = if (isAllSelected) Primary else sectionBorder,
                                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                                    )
                                    .background(if (isAllSelected) primary_light else whiteBg)
                                    .clickable { form = form.copy(priceListType = PriceListType.ALL_ITEMS) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (isAllSelected) Primary else primary_light, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isAllSelected) whiteBg else Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("All Items", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Apply a universal rule to all products", fontSize = tokens.caption, color = TextSecondary)
                                }
                                if (isAllSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                } else {
                                    Box(modifier = Modifier.size(20.dp).border(1.dp, mutedText, CircleShape))
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Option B: Individual Items
                            val isIndividualSelected = form.priceListType == PriceListType.INDIVIDUAL_ITEMS
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                    .border(
                                        width = if (isIndividualSelected) 2.dp else 1.dp,
                                        color = if (isIndividualSelected) Primary else sectionBorder,
                                        shape = RoundedCornerShape(tokens.cardCornerRadius)
                                    )
                                    .background(if (isIndividualSelected) primary_light else whiteBg)
                                    .clickable { form = form.copy(priceListType = PriceListType.INDIVIDUAL_ITEMS) }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(if (isIndividualSelected) Primary else light_grey, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Assignment,
                                        contentDescription = null,
                                        tint = if (isIndividualSelected) whiteBg else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Individual Items", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Set specific prices for each SKU", fontSize = tokens.caption, color = TextSecondary)
                                }
                                if (isIndividualSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                                } else {
                                    Box(modifier = Modifier.size(20.dp).border(1.dp, mutedText, CircleShape))
                                }
                            }
                        }
                    }
                }

                // SECTION 3: PRICING SCHEME (If Individual Items)
                if (!isAllItems) {
                    item {
                        AccordionSection(
                            icon = Icons.Default.LocalOffer,
                            title = "Pricing Scheme",
                            expanded = schemeExpanded,
                            onHeaderClick = { schemeExpanded = !schemeExpanded }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg)
                                    .padding(vertical = tokens.extraPadding),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val isUnit = form.pricingScheme == PricingScheme.UNIT_PRICING
                                // Unit Pricing Card
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                        .border(
                                            width = if (isUnit) 2.dp else 1.dp,
                                            color = if (isUnit) Primary else sectionBorder,
                                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                                        )
                                        .background(if (isUnit) primary_light else whiteBg)
                                        .clickable { form = form.copy(pricingScheme = PricingScheme.UNIT_PRICING) }
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Fixed height: Checkmark illanalum height maarathu
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(16.dp),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        if (isUnit) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(if (isUnit) Primary else light_grey, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.FlashOn,
                                            contentDescription = null,
                                            tint = if (isUnit) whiteBg else TextSecondary
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Unit Pricing",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                val isVolume = form.pricingScheme == PricingScheme.VOLUME_PRICING
                                // Volume Pricing Card
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(tokens.cardCornerRadius))
                                        .border(
                                            width = if (isVolume) 2.dp else 1.dp,
                                            color = if (isVolume) Primary else sectionBorder,
                                            shape = RoundedCornerShape(tokens.cardCornerRadius)
                                        )
                                        .background(if (isVolume) primary_light else whiteBg)
                                        .clickable { form = form.copy(pricingScheme = PricingScheme.VOLUME_PRICING) }
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Fixed height: Checkmark illanalum height maarathu
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(16.dp),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        if (isVolume) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(if (isVolume) Primary else light_grey, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.CardGiftcard,
                                            contentDescription = null,
                                            tint = if (isVolume) whiteBg else TextSecondary
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Volume Pricing",
                                        fontSize = tokens.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION 4: PRICING RULE (Only if All Items)
                if (isAllItems) {
                    item {
                        var adjExpanded by remember { mutableStateOf(false) }
                        var basisExpanded by remember { mutableStateOf(false) }

                        AccordionSection(
                            icon = Icons.AutoMirrored.Filled.ReceiptLong,
                            title = "3. Pricing Rule",
                            expanded = ruleExpanded,
                            onHeaderClick = { ruleExpanded = !ruleExpanded }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg)
                                    .padding(vertical = tokens.extraPadding)
                            ) {
                                FormDropdown(
                                    label = "Adjustment Type",
                                    value = form.adjustmentType.label,
                                    expanded = adjExpanded,
                                    onExpandChange = { adjExpanded = it },
                                    options = AdjustmentType.entries.map { it.label },
                                    onOptionSelected = { label -> form = form.copy(adjustmentType = AdjustmentType.entries.first { it.label == label }) }
                                )

                                Spacer(Modifier.height(14.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormLabel("Value")
                                        FormTextField(
                                            value = form.adjustmentValue,
                                            onValueChange = { form = form.copy(adjustmentValue = it) },
                                            placeholder = "15"
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        FormDropdown(
                                            label = "Based On",
                                            value = form.adjustmentBasis.label,
                                            expanded = basisExpanded,
                                            onExpandChange = { basisExpanded = it },
                                            options = AdjustmentBasis.entries.map { it.label },
                                            onOptionSelected = { label -> form = form.copy(adjustmentBasis = AdjustmentBasis.entries.first { it.label == label }) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 5: CURRENCY & ROUNDING
                item {
                    var currExpanded by remember { mutableStateOf(false) }
                    var roundExpanded by remember { mutableStateOf(false) }

                    AccordionSection(
                        icon = Icons.Default.Refresh,
                        title = "4. Currency & Rounding",
                        expanded = currencyExpanded,
                        onHeaderClick = { currencyExpanded = !currencyExpanded }
                    ) {
                        Column(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .background(whiteBg)
                                    .padding(vertical = tokens.extraPadding)
                        ) {
                            FormDropdown(
                                label = "Currency",
                                value = form.currency.label,
                                expanded = currExpanded,
                                onExpandChange = { currExpanded = it },
                                options = DefaultCurrencyOptions.map { it.label },
                                onOptionSelected = { label -> form = form.copy(currency = DefaultCurrencyOptions.first { it.label == label }) }
                            )

                            Spacer(Modifier.height(14.dp))
                            FormDropdown(
                                label = "Round Off To",
                                value = form.roundOff.label,
                                expanded = roundExpanded,
                                onExpandChange = { roundExpanded = it },
                                options = RoundOffOption.entries.map { it.label },
                                onOptionSelected = { label -> form = form.copy(roundOff = RoundOffOption.entries.first { it.label == label }) }
                            )

                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                    .background(whiteBg)
                                    .padding(horizontal = 14.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Automatic Conversion", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Use daily market exchange rates", fontSize = tokens.caption, color = TextSecondary)
                                }

                                MiniSwitch(
                                    checked = form.automaticConversion,
                                    onCheckedChange = { form = form.copy(automaticConversion = it) }
                                )
                            }
                        }
                    }
                }

                // PRICE PREVIEW SIMULATOR CONTAINER
                item {
                    Spacer(Modifier.height(tokens.extraPadding))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.screenPadding)
                            .border(1.dp, sectionBorder, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(whiteBg)
                    ) {
                        // Blue Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Primary)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = whiteBg, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "PRICE PREVIEW",
                                    color = whiteBg,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .background(whiteBg.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .clickable { }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Live Simulator", color = whiteBg, fontSize = tokens.caption, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Simulator Inner Fields
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("SEARCH SAMPLE ITEM", fontSize = tokens.caption, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
//                            OutlinedTextField(
//                                value = "Cloud Storage Unit X1",
//                                onValueChange = {},
//                                modifier = Modifier.fillMaxWidth(),
//                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
//                                textStyle = LocalTextStyle.current.copy(fontSize = tokens.bodyMedium),
//                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    unfocusedBorderColor = sectionBorder,
//                                    focusedBorderColor = Primary,
//                                    focusedContainerColor = light_grey,
//                                    unfocusedContainerColor = light_grey
//                                )
//                            )
                            SearchFilterBar(
                                query = "Cloud Storage Unit X1",
                                onQueryChange = {},
                                placeholder = "Cloud Storage Unit X1",
                                isSearchBarAlone = true

                            )

                            Spacer(Modifier.height(14.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(light_grey, RoundedCornerShape(8.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Base Price", fontSize = tokens.bodyMedium, color = TextSecondary)
                                    Text("$100.00", fontSize = tokens.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Adjustment", fontSize = tokens.bodyMedium, color = TextSecondary)
                                    Text("+15%", fontSize = tokens.bodyMedium, color = Primary, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Rounding", fontSize = tokens.bodyMedium, color = TextSecondary)
                                    Text(".99", fontSize = tokens.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            Text("FINAL COMPUTED PRICE", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = tokens.caption, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("$114.99", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = tokens.h1, fontWeight = FontWeight.ExtraBold, color = TextPrimary)

                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(greenBg, RoundedCornerShape(8.dp))
                                    .border(1.dp, greenBg, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Margin: 22%", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = darkGreenBg)
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = greentext, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Healthy Margin Range", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontSize = tokens.caption, color = darkGreenBg)

                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {},
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                colors = ButtonDefaults.buttonColors(containerColor = light_grey)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Recalculate Preview", color = TextPrimary, fontSize = tokens.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // BOTTOM SKUS PRODUCT SECTION (If Individual Items Selected)
                if (!isAllItems) {
                    item {
                        Column(modifier = Modifier.padding()) {
                            SearchFilterBar(
                                query = itemSearch,
                                onQueryChange = { itemSearch = it },
                                placeholder = "Search items by name, SKU or category...",
                                isSearchBarAlone = true
                            )
                            Spacer(Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 15.dp, vertical = 0.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // ── Left Side: Import CSV & Bulk Update ──
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Import CSV Button
                                    OutlinedButton(
                                        onClick = { /* TODO: Import CSV */ },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, sectionBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFF334155)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FileUpload,
                                            contentDescription = null,
                                            tint = Color(0xFF475569),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Import CSV",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF334155)
                                        )
                                    }

                                    // Bulk Update Button
                                    OutlinedButton(
                                        onClick = { /* TODO: Bulk Update */ },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, sectionBorder),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = Color.White,
                                            contentColor = Color(0xFF334155)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color(0xFF475569),
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Bulk Update",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF334155)
                                        )
                                    }
                                }

                                // ── Right Side: + Add Item & Settings ──
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // + Add Item Button
                                    Button(
                                        onClick = { /* TODO: Add Item */ },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Primary // Or Color(0xFF2E3BE8)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "+ Add Item",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }

                                    // Settings Icon Button
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White)
                                            .border(1.dp, sectionBorder, RoundedCornerShape(10.dp))
                                            .clickable { /* TODO: Settings */ },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Settings",
                                            tint = Color(0xFF475569),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    val filteredSkus = itemsList.filter {
                        itemSearch.isBlank() || it.name.contains(itemSearch, ignoreCase = true) || it.sku.contains(itemSearch, ignoreCase = true)
                    }

                    items(filteredSkus, key = { it.id }) { itemSku ->
                        SkuProductCardRow(
                            itemSku = itemSku,
                            tokens = tokens,
                            onCheckedChange = { checked ->
                                itemsList = itemsList.map { if (it.id == itemSku.id) it.copy(isSelected = checked) else it }
                            }
                        )
                    }
                }
            }
        }

        StepNavigationFab(
            showBack = true,
            backLabel = "Cancel",
            onBack = onClose,
            showBackArrow = false,
            trailingAction = TrailingFabAction.Update(
                label = "Save Pricelist",
                onClick = { onSave(form.copy(items = itemsList)) }
            ),
            backWidthFraction = 0.25f,
            trailingWidthFraction = 0.35f
        )
    }
}

@Composable
private fun SkuProductCardRow(
    itemSku: PricingLineItem,
    tokens: com.cuso.tailor.adaptive_screen.AppDesignTokens,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding, vertical = 6.dp)
            .border(1.dp, sectionBorder, RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
            .background(whiteBg)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppCheckbox(
                checked = itemSku.isSelected,
                onCheckedChange = onCheckedChange
            )
            Spacer(Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(itemSku.name, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("SKU: ${itemSku.sku}", fontSize = tokens.caption, color = TextSecondary)
            }
            Box(
                modifier = Modifier
                    .background(greenBg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("20.0%", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = darkGreenBg)
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Base Cost", fontSize = tokens.caption, color = TextSecondary)
                Text("$${"%.2f".format(itemSku.baseCost)}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Box(
                modifier = Modifier
                    .border(1.dp, sectionBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("MARKUP %", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = Primary)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Primary)
                }
            }

            Box(
                modifier = Modifier
                    .width(60.dp)
                    .background(light_grey, RoundedCornerShape(6.dp))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("${itemSku.markupPercent.toInt()}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Computed", fontSize = tokens.caption, color = TextSecondary)
                Text("$${"%.2f".format(itemSku.computedPrice)}", fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}

private fun sampleLineItems() = listOf(
    PricingLineItem("1", "OptiCore Processor V2", "OC-1002-GEN2", baseCost = 420.0, markupPercent = 25.0),
    PricingLineItem("2", "Nexus Display Panel 27\"", "NX-DP27-B", baseCost = 180.0, markupPercent = 10.0),
    PricingLineItem("3", "Titanium Shell Chassis", "CH-11-750", baseCost = 89.50, markupPercent = 28.0),
    PricingLineItem("4", "Ultra-Silent Cooling Fan", "FAN-US-120", baseCost = 12.0, markupPercent = 50.0)
)