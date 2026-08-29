@file:Suppress("UNUSED_PARAMETER")

package com.cuso.mobile.view.home.sales.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cuso.mobile.adaptive_screen.LocalAppTokens
import com.cuso.mobile.ui.theme.*
import com.cuso.mobile.view.composable.TitleBar

data class CategoryDetailGarmentItem(
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val metaInfo: String
)

@Composable
fun GarmentCategoryDetailScreen(
    categoryTitle: String = "Shirts Category",
    onClose: () -> Unit = {},
    onAddGarmentClick: () -> Unit = {},
    onAddGarmentCategoryClick: () -> Unit = onAddGarmentClick,
    onConfigureGarmentClick: (String) -> Unit = {}
) {
    val tokens = LocalAppTokens.current

    val garmentList = remember {
        listOf(
            CategoryDetailGarmentItem(
                id = "1",
                title = "Casual Shirt",
                description = "For customers with default relaxed/\neveryday profiles",
                status = "ACTIVE",
                metaInfo = "4 Sizes · 2 Fits · Customizable · 2 Fabrics · 8 Add-ons"
            ),
            CategoryDetailGarmentItem(
                id = "2",
                title = "Formal Shirt",
                description = "Part of the formal business apparel\ncollection",
                status = "ACTIVE",
                metaInfo = "3 Sizes · 3 Fits · Customizable · 3 Fabrics · 12 Add-ons"
            ),
            CategoryDetailGarmentItem(
                id = "3",
                title = "Pyjama",
                description = "Comfortable everyday loungewear\nand sleepwear",
                status = "DRAFT",
                metaInfo = "2 Sizes · 1 Fit · Standard · 1 Fabric · 4 Add-ons"
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TitleBar(
                    title = categoryTitle,
                    onClose = onClose
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                // Header Count Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(horizontal = tokens.screenPadding, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${garmentList.size} Garments",
                        fontSize = tokens.bodyMedium,
                        color = TextPrimary
                    )

                    OutlinedButton(
                        onClick = onAddGarmentCategoryClick,
                        shape = RoundedCornerShape(tokens.cardCornerRadius * 0.5f),
                        border = BorderStroke(1.2.dp, Primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = whiteBg,
                            contentColor = Primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(tokens.iconSize * 0.85f)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Add Garment Category",
                            color = Primary,
                            fontSize = tokens.bodySmall,
                        )
                    }
                }

                // Garment Cards List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = tokens.screenPadding),
                    contentPadding = PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(garmentList, key = { it.id }) { item ->
                        CategoryGarmentCard(
                            item = item,
                            onConfigureClick = { onConfigureGarmentClick(item.id) }
                        )
                    }
                }
            }
        }

//        // Floating Step Navigation FAB
//        StepNavigationFab(
//            showBack = true,
//            onBack = onClose,
//            backLabel = "Back",
//            showBackArrow = true,
//            showTrailingArrow = false,
//            trailingAction = TrailingFabAction.Next(
//                label = "Add Garment",
//                onClick = onAddGarmentClick
//            )
//        )
    }
}

@Composable
private fun CategoryGarmentCard(
    item: CategoryDetailGarmentItem,
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    fontSize = tokens.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                GarmentStatusBadge(status = item.status)
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = item.description,
                fontSize = tokens.bodySmall,
                color = TextSecondary,
                lineHeight = tokens.bodySmall.value.sp * 1.35f
            )

            Spacer(Modifier.height(10.dp))

            HorizontalDivider(color = title_border, thickness = 1.dp)

            Spacer(Modifier.height(10.dp))

            Text(
                text = item.metaInfo,
                fontSize = tokens.caption,
                color = mutedText,
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(12.dp))

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
private fun GarmentStatusBadge(status: String) {
    val tokens = LocalAppTokens.current
    val (bgColor, textColor) = when (status.uppercase()) {
        "ACTIVE" -> Pair(greenBg, darkGreenBg)
        "DRAFT" -> Pair(yellowBg, yellowText)
        else -> Pair(modelGray, TextSecondary)
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
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