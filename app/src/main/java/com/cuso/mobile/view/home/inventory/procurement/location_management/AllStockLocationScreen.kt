package com.cuso.mobile.view.home.inventory.procurement.location_management

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.AppDesignTokens
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.*

data class StockLocationItemModel(
    val id: String,
    val category: String = "APPAREL",
    val brand: String = "ABC Fashion",
    val status: String = "Active",
    val title: String = "Men Formal Shirt",
    val code: String = "Shirt-Men-Formal",
    val hasVariants: Boolean = true,
    val totalStock: String = "150 Piece",
    val locationCount: String = "2 location"
)

@Composable
fun AllStockLocationScreen(
    onClose: () -> Unit,
    onItemClick: (StockLocationItemModel) -> Unit = {},
    onLocationClick: (StockLocationItemModel) -> Unit = {},
    onOptionsClick: (StockLocationItemModel) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    var searchQuery by remember { mutableStateOf("") }
    var selectAll by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }

    val stockList = remember {
        listOf(
            StockLocationItemModel(id = "1"),
            StockLocationItemModel(id = "2"),
            StockLocationItemModel(id = "3")
        )
    }

    Scaffold(
        containerColor = Primary_background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(whiteBg)) {
                TitleBar("All stock Location", onClose)
                HorizontalDivider(color = title_border)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search and Filter Bar
            SearchFilterBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search Customers...",
                showFilterIcon = true,
                onFilterClick = { },
                height = tokens.fieldHeight * 1.1f
            )

            HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 1.dp)

            // Select All Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = tokens.screenPadding, vertical = tokens.extraPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppCheckbox(
                        checked = selectAll,
                        onCheckedChange = { checked ->
                            selectAll = checked
                            selectedItemIds = if (checked) stockList.map { it.id }.toSet() else emptySet()
                        }
                    )
                    Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                    Text(
                        text = "Select All (${stockList.size} items)",
                        fontSize = tokens.caption,
                        fontWeight = FontWeight.Medium,
                        color = title_color
                    )
                }
                Text(
                    text = "Total: 450 Pcs",
                    fontSize = tokens.caption,
                    color = mutedText
                )
            }

            // Cards List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = tokens.screenPadding * 1.5f),
                verticalArrangement = Arrangement.spacedBy(tokens.extraPadding * 1.2f)
            ) {
                itemsIndexed(stockList, key = { _, item -> item.id }) { _, item ->
                    val isChecked = selectedItemIds.contains(item.id)
                    StockLocationCardItem(
                        item = item,
                        isChecked = isChecked,
                        tokens = tokens,
                        onCheckedChange = { checked ->
                            selectedItemIds = if (checked) selectedItemIds + item.id else selectedItemIds - item.id
                            selectAll = selectedItemIds.size == stockList.size
                        },
                        onClick = { onItemClick(item) },
                        onLocationClick = { onLocationClick(item) },
                        onOptionsClick = { onOptionsClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StockLocationCardItem(
    item: StockLocationItemModel,
    isChecked: Boolean,
    tokens: AppDesignTokens,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onLocationClick: () -> Unit,
    onOptionsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = tokens.screenPadding)
            .clickable { onClick() },
        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.8f),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            // Row 1: Checkbox + Badges + Options Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppCheckbox(checked = isChecked, onCheckedChange = onCheckedChange)
                Spacer(Modifier.width(tokens.extraPadding * 0.8f))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                        .background(activity_purple_bg)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 2.dp)
                ) {
                    Text(item.category, fontSize = tokens.label, fontWeight = FontWeight.Bold, color = activity_purple)
                }

                Spacer(Modifier.width(6.dp))
                Text("•", color = mutedText, fontSize = tokens.label)
                Spacer(Modifier.width(6.dp))
                Text(item.brand, fontSize = tokens.caption, color = TextSecondary)

                Spacer(Modifier.weight(1f))

                // Active Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                        .background(greenBg)
                        .padding(horizontal = tokens.extraPadding * 0.8f, vertical = 3.dp)
                ) {
                    Text(item.status, fontSize = tokens.label, fontWeight = FontWeight.Medium, color = darkGreenBg)
                }

                Spacer(Modifier.width(tokens.extraPadding * 0.4f))

                IconButton(onClick = onOptionsClick, modifier = Modifier.size(tokens.iconSize * 1.3f)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = mutedText, modifier = Modifier.size(tokens.iconSize))
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 0.8f))

            // Row 2: Title
            Text(item.title, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)

            Spacer(Modifier.height(4.dp))

            // Row 3: Subtitle + Has Variants Tag
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.code, fontSize = tokens.caption, color = mutedText)
                if (item.hasVariants) {
                    Spacer(Modifier.width(tokens.extraPadding * 0.8f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.35f))
                            .background(light_grey)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Has Variants", fontSize = tokens.label, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(tokens.extraPadding * 1.2f))
            HorizontalDivider(color = grey_border.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(Modifier.height(tokens.extraPadding * 1.2f))

            // Row 4: Total Stock & Location Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Stock", fontSize = tokens.label, color = mutedText)
                    Spacer(Modifier.height(2.dp))
                    Text(item.totalStock, fontSize = tokens.bodyMedium, fontWeight = FontWeight.Bold, color = title_color)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Stock Location", fontSize = tokens.label, color = mutedText)
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(tokens.cardCornerRadius * 2f))
                            .border(1.dp, Primary, RoundedCornerShape(tokens.cardCornerRadius * 2f))
                            .clickable { onLocationClick() }
                            .padding(horizontal = 12.dp, vertical = 3.dp)
                    ) {
                        Text(item.locationCount, fontSize = tokens.caption, fontWeight = FontWeight.Medium, color = Primary)
                    }
                }
            }
        }
    }
}