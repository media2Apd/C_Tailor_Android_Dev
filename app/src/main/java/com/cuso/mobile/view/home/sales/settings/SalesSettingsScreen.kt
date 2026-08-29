@file:Suppress("UNUSED_PARAMETER","unused","unusedVariable")

package com.cuso.mobile.view.home.sales.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.cuso.mobile.R
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.SearchFilterBar
import com.cuso.mobile.view.composable.TitleBar
import com.cuso.mobile.view.composable.dashedBorder
import com.cuso.mobile.viewmodel.SalesViewModel

data class GarmentCategoryItem(
    val id: String,
    val name: String,
    val fieldsCount: Int,
    val measurementsCount: Int,
    val status: String,
    val iconRes: Int
)

@Composable
fun SalesSettingsScreen(
    navController: NavController,
    onClose: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    GarmentTypeContent(onClose = onClose)
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun GarmentTypeContent(
    onClose: () -> Unit = {},
    onAddSegmentClick: () -> Unit = {},
    onAddGarmentClick: () -> Unit = {},
    onConfigureClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current
    val salesViewModel: SalesViewModel = hiltViewModel()
    var searchQuery by remember { mutableStateOf("") }


    val segments = listOf("Men", "Women", "Kids")
    var selectedSegmentIndex by remember { mutableIntStateOf(0) }

    val displayCategories = remember {
        listOf(
            GarmentCategoryItem("1", "Shirt", 45, 12, "CONFIGURED", R.drawable.ic_shirts),
            GarmentCategoryItem("2", "Polo", 30, 8, "CONFIGURED", R.drawable.ic_shirts),
            GarmentCategoryItem("3", "Kurta", 28, 10, "CONFIGURED", R.drawable.ic_shirts),
            GarmentCategoryItem("4", "Trouser", 35, 10, "PENDING", R.drawable.ic_pants),
            GarmentCategoryItem("5", "Pyjama", 15, 6, "PENDING", R.drawable.ic_pants),
            GarmentCategoryItem("6", "Blazer", 20, 14, "NOT STARTED", R.drawable.ic_shirts),
            GarmentCategoryItem("7", "Sherwani", 12, 16, "NOT STARTED", R.drawable.ic_shirts),
            GarmentCategoryItem("8", "Waistcoat", 18, 8, "NOT STARTED", R.drawable.ic_shirts)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        FabScaffold(
            fab = FabConfig(
                label = "Add Garment",
                icon = Icons.Default.Add,
                onClick = onAddGarmentClick,
                bottomPadding = 40.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                // Header Bar
                TitleBar(
                    title = "Garment Categories",
                    onClose = onClose
                )
                SearchFilterBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search Service Status...",
                    accentColor = Primary,
                    borderColor = BorderGray,
                    textSecondaryColor = TextSecondary,
                    onFilterClick = { }
                )

                // Segment Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedSegmentIndex,
                    edgePadding = tokens.screenPadding,
                    containerColor = whiteBg,
                    divider = { HorizontalDivider(color = title_border, thickness = 1.dp) },
                    indicator = { tabPositions ->
                        if (selectedSegmentIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedSegmentIndex]),
                                color = Primary,
                                height = 2.5.dp
                            )
                        }
                    }
                ) {
                    segments.forEachIndexed { index, segmentTitle ->
                        val isSelected = selectedSegmentIndex == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedSegmentIndex = index },
                            text = {
                                Text(
                                    text = segmentTitle,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Primary else TextSecondary
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // List Items
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Add Segment Dashed Button
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(tokens.buttonHeight)
                                .dashedBorder(
                                    color = Primary,
                                    shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                                    strokeWidth = 1.2.dp,
                                    cornerRadius = tokens.cardCornerRadius * 0.5f
                                )
                                .clip(RoundedCornerShape(tokens.cardCornerRadius * 0.5f))
                                .clickable { onAddSegmentClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(tokens.iconSize)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Add Segment",
                                    color = Primary,
                                    fontSize = tokens.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Render Garment Cards
                    itemsIndexed(
                        items = displayCategories,
                        key = { _, item -> item.id }
                    ) { _, item ->
                        GarmentCategoryCard(
                            title = item.name,
                            subtitle = "${item.fieldsCount} Fields · ${item.measurementsCount} Measurements",
                            status = item.status,
                            iconRes = item.iconRes,
                            onConfigureClick = { onConfigureClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GarmentCategoryCard(
    title: String,
    subtitle: String,
    status: String,
    iconRes: Int,
    onConfigureClick: () -> Unit
) {
    val tokens = LocalAppTokens.current

    Card(
        shape = RoundedCornerShape(tokens.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, grey_border, RoundedCornerShape(tokens.cardCornerRadius))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(tokens.screenPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Rounded Icon Box
                Box(
                    modifier = Modifier
                        .size(tokens.fieldHeight)
                        .background(background_light_purple, RoundedCornerShape(tokens.cardCornerRadius * 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = Primary,
                        modifier = Modifier.size(tokens.iconSize)
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Title & Subtitle Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = tokens.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = tokens.caption,
                        color = TextSecondary
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Status Pill Badge
                CategoryStatusBadge(status = status)
            }

            Spacer(Modifier.height(10.dp))

            // Action Link
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onConfigureClick() }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configure →",
                    color = Primary,
                    fontSize = tokens.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun CategoryStatusBadge(status: String) {
    val tokens = LocalAppTokens.current
    val (bgColor, textColor) = when (status.uppercase()) {
        "CONFIGURED" -> Pair(greenBg, darkGreenBg)
        "PENDING" -> Pair(yellowBg, yellowText)
        else -> Pair(primary_light, Primary)
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = tokens.label,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp
        )
    }
}