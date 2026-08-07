@file:Suppress(
    "UNUSED_VALUE",
    "SpellCheckingInspection",
    "GrazieInspection",
    "AssignedValueIsNeverRead"
)
package com.cuso.mobile.view.home.sales.pricing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuso.mobile.model.sales.GarmentPricingListItemDto
import com.cuso.mobile.ui.theme.mutedText
import com.cuso.mobile.ui.theme.whiteBg
import com.cuso.mobile.view.composable.FabConfig
import com.cuso.mobile.view.composable.FabScaffold
import com.cuso.mobile.view.composable.ListSkeleton
import com.cuso.mobile.viewmodel.GarmentPricingListUiState
import com.cuso.mobile.viewmodel.PricingQuotationViewModel

private val Primary = Color(0xFF3B3BF9)
  private val TextDark = Color(0xFF111827)
private val CardBorder = Color(0xFFF0F0F0)
private val StripBg = Color(0xFFF9FAFB)

@Composable
fun GarmentPricingListScreen(
    onBack: () -> Unit,
    onAddNewPricing: () -> Unit = {},            // ── navigates to AddGarmentPricingScreen(pricingId = null) ──
    onCardClick: (String) -> Unit = {}           // ── navigates to AddGarmentPricingScreen(pricingId = item.id) ──
) {
    val viewModel: PricingQuotationViewModel = hiltViewModel()
    val listState by viewModel.garmentPricingListState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchGarmentPricingList()
    }

    FabScaffold(
        fab = FabConfig(
            label = "Add New Pricing",
            icon = Icons.Default.Add,
            onClick = onAddNewPricing,
            bottomPadding = 24.dp
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(whiteBg)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextDark,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { onBack() }
                )
                Spacer(Modifier.width(12.dp))
                Text("Garment Wise Pricing", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
            HorizontalDivider(color = CardBorder)

            when (val state = listState) {
                is GarmentPricingListUiState.Loading -> {
                    ListSkeleton()

                }
                is GarmentPricingListUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(state.message, color = Color.Red, fontSize = 13.sp)
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { viewModel.fetchGarmentPricingList() }) { Text("Retry") }
                        }
                    }
                }
                is GarmentPricingListUiState.Success -> {
                    val uniqueItems = state.items.distinctBy { it.id }   //   NEW — dedupe by id

                    if (uniqueItems.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No pricing records yet", color = mutedText, fontSize = 14.sp)
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            uniqueItems.forEach { item ->   //   CHANGED
                                GarmentPricingCard(
                                    item = item,
                                    onClick = { onCardClick(item.id) }
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                            Spacer(Modifier.height(90.dp)) // space for FAB
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GarmentPricingCard(item: GarmentPricingListItemDto, onClick: () -> Unit) {
    var clicked by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !clicked) {
                clicked = true
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = whiteBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.itemName.ifBlank { "-" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Text(
                        "Base ₹${item.basePrice.toInt()}",
                        fontSize = 12.sp,
                        color = mutedText
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = mutedText,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Total price strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StripBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Price", fontSize = 12.sp, color = mutedText)
                Text(
                    "₹${item.totalPrice.toInt()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
            }
        }
    }
}