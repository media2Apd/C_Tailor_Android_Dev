package com.cuso.mobile.view.home.inventory.procurement.location_management

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.model.inventory.LocationStockDto
import com.cuso.mobile.model.inventory.StockLocationOverviewDto
import com.cuso.mobile.model.inventory.StockLocationVariantDto
import com.cuso.mobile.model.inventory.VariantBreakdownDto
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*
import com.cuso.mobile.viewmodel.InventoryViewModel

@Composable
fun StockLocationDetailsScreen(
    itemId: String? = null,
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    onClose: () -> Unit
) {
    val tokens = LocalAppTokens.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "Location", "Variants")

    val selectedLocationItem by inventoryViewModel.selectedStockLocationItem.collectAsState()
    val detailData by inventoryViewModel.selectedStockLocationDetail.collectAsState()
    val isLoading by inventoryViewModel.isLoadingStockLocationDetail.collectAsState()
    val errorMessage by inventoryViewModel.stockLocationDetailError.collectAsState()

    // Resolve target itemId from parameter or ViewModel selected item
    val targetItemId = itemId ?: selectedLocationItem?.id

    LaunchedEffect(targetItemId) {
        Log.d("STOCK_LOC_DETAIL", "--------------------------------------------------")
        Log.d("STOCK_LOC_DETAIL", "StockLocationDetailsScreen LaunchedEffect triggered")
        Log.d("STOCK_LOC_DETAIL", "Passed parameter itemId: '$itemId'")
        Log.d("STOCK_LOC_DETAIL", "ViewModel selectedLocationItem.id: '${selectedLocationItem?.id}'")
        Log.d("STOCK_LOC_DETAIL", "Resolved targetItemId: '$targetItemId'")

        if (targetItemId.isNullOrBlank()) {
            Log.e("STOCK_LOC_DETAIL", "ERROR: targetItemId is NULL or BLANK. API call was NOT triggered.")
        } else {
            Log.i("STOCK_LOC_DETAIL", "SUCCESS: Triggering fetchStockLocationViewOne with ID: $targetItemId")
            inventoryViewModel.fetchStockLocationViewOne(targetItemId)
        }
        Log.d("STOCK_LOC_DETAIL", "--------------------------------------------------")
    }

    val overview = detailData?.overview
    val locationStockList = detailData?.locationStock.orEmpty()
    val variantsList = detailData?.variants.orEmpty()

    val productName = overview?.name ?: selectedLocationItem?.name ?: "Stock Item"
    val productSubtitle = listOfNotNull(
        overview?.brand ?: selectedLocationItem?.brand,
        overview?.category ?: selectedLocationItem?.category,
        overview?.itemType
    ).filter { it.isNotBlank() }.joinToString(" - ").ifBlank { "General Apparel" }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar("Stock Location Details", onClose = {
                    inventoryViewModel.clearSelectedStockLocationDetail()
                    onClose()
                })
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && detailData == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary, strokeWidth = 2.5.dp)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
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
                                text = productName,
                                fontSize = tokens.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = productSubtitle,
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Navigation Tabs
                    AppUnderlineTabRow(
                        tabs = tabTitles,
                        selectedIndex = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )

                    Spacer(Modifier.height(10.dp))

                    // Dynamic Tab Content
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = tokens.screenPadding * 1.5f),
                        verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
                    ) {
                        when (selectedTab) {
                            0 -> {
                                item {
                                    OverviewLocationTabContent(
                                        overview = overview,
                                        sourceType = detailData?.sourceType,
                                        tokens = tokens
                                    )
                                }
                            }
                            1 -> {
                                item {
                                    LocationBreakdownTabContent(
                                        locations = locationStockList,
                                        tokens = tokens
                                    )
                                }
                            }
                            2 -> {
                                item {
                                    VariantsLocationTabContent(
                                        variants = variantsList,
                                        tokens = tokens
                                    )
                                }
                            }
                        }
                    }
                }
            }

            DynamicIslandError(
                message = errorMessage,
                onDismiss = { inventoryViewModel.clearSelectedStockLocationDetail() }
            )
        }
    }
}

// ── Tab 1: Overview ──
@Composable
private fun OverviewLocationTabContent(
    overview: StockLocationOverviewDto?,
    sourceType: String?,
    tokens: AppDesignTokens
) {
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
                        Text(
                            text = sourceType?.uppercase() ?: "GENERAL",
                            fontSize = tokens.label,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 1.4f))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ITEM TYPE", fontSize = tokens.label, color = mutedText)
                        Text(
                            text = overview?.itemType ?: "Finished Goods",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PRIMARY UOM", fontSize = tokens.label, color = mutedText)
                        Text(
                            text = overview?.unit ?: "Piece",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
                    }
                }

                Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                Text("CATEGORY", fontSize = tokens.label, color = mutedText)
                Text(
                    text = overview?.category ?: "Not Assigned",
                    fontSize = tokens.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = title_color
                )

                Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("BRAND", fontSize = tokens.label, color = mutedText)
                        Text(
                            text = overview?.brand?.takeIf { it.isNotBlank() } ?: "N/A",
                            fontSize = tokens.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = title_color
                        )
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
                            Text(
                                text = "${overview?.reorderLevel?.toInt() ?: 0} piece",
                                fontSize = tokens.caption,
                                fontWeight = FontWeight.SemiBold,
                                color = yellowText
                            )
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
                            Text(
                                text = overview?.gstRate?.takeIf { it.isNotBlank() } ?: "N/A",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
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
                            Text(
                                text = overview?.hsnCode?.takeIf { it.isNotBlank() } ?: "N/A",
                                fontSize = tokens.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
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
    locations: List<LocationStockDto>,
    tokens: AppDesignTokens
) {
    if (locations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding * 2),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No stock locations found for this item",
                fontSize = tokens.bodyMedium,
                color = mutedText
            )
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.4f)) {
            locations.forEach { location ->
                val conditionText = location.stockCondition?.replaceFirstChar { it.uppercase() } ?: "Good"
                val rotationText = location.rotationMethod?.uppercase() ?: "FIFO"
                val isCentral = location.warehouseName?.contains("Central", ignoreCase = true) ?: false

                WarehouseLocationCard(
                    tokens = tokens,
                    name = location.warehouseName ?: "Warehouse",
                    address = "Stock Condition: $conditionText",
                    isCentral = isCentral,
                    totalQty = "${location.totalQuantity.toInt()} Piece",
                    available = "${location.available.toInt()} Piece",
                    reserved = "${location.totalReserved.toInt()} Piece",
                    stockCondition = conditionText,
                    rotationMethod = rotationText,
                    variantBreakdown = location.variantBreakdown
                )
            }
        }
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
    stockCondition: String,
    rotationMethod: String,
    variantBreakdown: List<VariantBreakdownDto>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.65f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(tokens.screenPadding)) {
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
                    Text(name, fontSize = tokens.bodySmall, fontWeight = FontWeight.Bold, color = title_color)
                    Spacer(Modifier.height(2.dp))
                    Text(address, fontSize = tokens.label, color = mutedText)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TagPill(text = stockCondition, tokens = tokens)
                    TagPill(text = rotationMethod, tokens = tokens)
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
            HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

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

            if (variantBreakdown.isNotEmpty()) {
                Spacer(Modifier.height(tokens.extraPadding * 1.2f))
                Text("Variant Breakdown", fontSize = tokens.caption, fontWeight = FontWeight.Bold, color = title_color)
                Spacer(Modifier.height(tokens.extraPadding * 0.8f))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    variantBreakdown.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(badgeGrey, RoundedCornerShape(tokens.cardCornerRadius * 0.4f))
                                .padding(horizontal = tokens.extraPadding * 1.2f, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.label ?: "Default",
                                fontSize = tokens.caption,
                                color = TextSecondary
                            )
                            Text(
                                text = "${item.qty.toInt()} Pcs",
                                fontSize = tokens.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = title_color
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Tab 3: Variants ──
@Composable
private fun VariantsLocationTabContent(
    variants: List<StockLocationVariantDto>,
    tokens: AppDesignTokens
) {
    if (variants.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding * 2),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No variants found for this item",
                fontSize = tokens.bodyMedium,
                color = mutedText
            )
        }
    } else {
        val totalVariantStock = variants.sumOf { it.totalStock.toLong() }

        Column(verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.extraPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VARIANT ITEMS (${variants.size})",
                    fontSize = tokens.caption,
                    fontWeight = FontWeight.Bold,
                    color = mutedText
                )
                Text(
                    text = "Total: $totalVariantStock Pieces",
                    fontSize = tokens.caption,
                    color = mutedText
                )
            }

            variants.forEach { variant ->
                val isActive = variant.status.equals("active", ignoreCase = true)
                val statusText = variant.status.replaceFirstChar { it.uppercase() }

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
                                Text(
                                    text = variant.variantLabel?.takeIf { it.isNotBlank() } ?: variant.sku ?: "Variant",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.SemiBold,
                                    color = title_color
                                )
                            }

                            Spacer(Modifier.weight(1f))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                                    .background(if (isActive) greenBg else light_grey)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = statusText,
                                    fontSize = tokens.label,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isActive) darkGreenBg else mutedText
                                )
                            }

                            Spacer(Modifier.width(4.dp))
                            IconButton(onClick = { }, modifier = Modifier.size(tokens.iconSize * 1.2f)) {
                                Icon(Icons.Default.MoreVert, contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize))
                            }
                        }

                        if (variant.isCurrentItem) {
                            Spacer(Modifier.height(4.dp))
                            Text("Current Selected Item", fontSize = tokens.label, color = Primary)
                        }

                        Spacer(Modifier.height(tokens.extraPadding * 1.2f))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("SKU", fontSize = tokens.label, color = mutedText)
                                Text(
                                    text = variant.sku ?: "-",
                                    fontSize = tokens.caption,
                                    fontWeight = FontWeight.Medium,
                                    color = title_color
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("TOTAL STOCK", fontSize = tokens.label, color = mutedText)
                                Text(
                                    text = "${variant.totalStock.toInt()} Piece",
                                    fontSize = tokens.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = title_color
                                )
                            }
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